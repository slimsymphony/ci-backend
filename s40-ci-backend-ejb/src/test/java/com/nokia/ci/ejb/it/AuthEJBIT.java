package com.nokia.ci.ejb.it;

import com.nokia.ci.ejb.AuthEJB;
import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.AuthException;
import com.nokia.ci.ejb.exception.LoginNotAllowedException;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.ssh.SshClient;
import java.io.File;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.inject.Inject;
import junit.framework.Assert;
import org.codehaus.plexus.util.StringUtils;
import org.infinispan.Cache;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptAfter;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.TransactionMode;
import org.jboss.arquillian.persistence.Transactional;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for AuthEJB class. Tests are running inside container.
 *
 * @author vrouvine
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@ApplyScriptAfter(value = {"set_referential_integrity_false.sql"})
@UsingDataSet(value = {"users_dataset.yml"})
public class AuthEJBIT {

    private static final Logger log = LoggerFactory.getLogger(AuthEJBIT.class);
    
    private static final int WAIT_TIME = 4;
    @Inject
    private AuthEJB authEJB;
    @Inject
    private SysUserEJB sysUserEJB;
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    private GerritEJB gerritEJB;
    @Resource(lookup = "java:jboss/infinispan/cache/ci20/project-access-cache")
    Cache<String, SysUser> cache;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return EJBDeploymentCreator.createEJBDeployment("pom.xml");
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void authenticateExistingAdminUser() throws Exception {
        Date stamp = new Date();
        SysUser user = authEJB.authenticate("admin", "admin");
        assertUser(user, stamp);
        List<Project> projectAccess = sysUserEJB.getProjectAccess(user.getId());
        Assert.assertTrue("Project access for admin should be emtpy!", projectAccess.isEmpty());
        // Waiting for asynchronous project access updating
        sleep(WAIT_TIME);
        projectAccess = sysUserEJB.getProjectAccess(user.getId());
        log.info("Project access {}", projectAccess);
        Assert.assertNotNull("Cache entry for admin should not be null!", cache.get(user.getLoginName()));
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void authenticateExistingUser() throws Exception {
        Gerrit gerrit = gerritEJB.read(1L);
        setMockGerritResponseDelay(gerrit, 1000);
        
        Date stamp = new Date();
        List<Project> projectAccess = sysUserEJB.getProjectAccess(2L);
        SysUser user = authEJB.authenticate("user", "user");
        SysUser cachedUser = cache.get(user.getLoginName());
        log.info("Cached projects {}", cachedUser.getProjectAccess().size());
        Assert.assertEquals("Project count should match!", projectAccess.size(), cachedUser.getProjectAccess().size());
        assertUser(user, stamp);
        // Waiting for asynchronous project access updating
        sleep(WAIT_TIME);
        projectAccess = sysUserEJB.getProjectAccess(user.getId());
        log.info("Project access {}", projectAccess);
        Assert.assertEquals("Project access count should match!", 2, projectAccess.size());
        cachedUser = cache.get(user.getLoginName());
        Assert.assertNotNull("Cache entry for admin should be null!", cachedUser);
        Assert.assertEquals("Project count should match!", 2, cachedUser.getProjectAccess().size());
        
        setMockGerritResponseDelay(gerrit, 100);
    }
    
    @Test
    @Transactional(TransactionMode.DISABLED)
    public void authenticateNotExistingUser() throws Exception {
        Gerrit gerrit = gerritEJB.read(1L);
        setMockGerritResponseDelay(gerrit, 1000);
        
        Date stamp = new Date();
        SysUser user = authEJB.authenticate("newuser", "newuser");
        Assert.assertNotNull("There should be user created to database!", sysUserEJB.read(user.getId()));
        
        SysUser cachedUser = cache.get(user.getLoginName());
        log.info("Cached projects {}", cachedUser.getProjectAccess().size());
        Assert.assertEquals("Project count should match!", 0, cachedUser.getProjectAccess().size());
        assertUser(user, stamp);
        // Waiting for asynchronous project access updating
        sleep(WAIT_TIME);
        List<Project> projectAccess = sysUserEJB.getProjectAccess(user.getId());
        log.info("Project access {}", projectAccess);
        Assert.assertEquals("Project access count should match!", 1, projectAccess.size());
        cachedUser = cache.get(user.getLoginName());
        Assert.assertNotNull("Cache entry for admin should be null!", cachedUser);
        Assert.assertEquals("Project count should match!", 1, cachedUser.getProjectAccess().size());
        
        setMockGerritResponseDelay(gerrit, 100);
    }

    @Test(expected = LoginNotAllowedException.class)
    @Transactional(TransactionMode.DISABLED)
    public void authenticateUserNotAllowed() throws Exception {
        // Disallow login
        SysConfig sysConfig = sysConfigEJB.getSysConfig(SysConfigKey.ALLOW_LOGIN);
        sysConfig.setConfigValue(Boolean.FALSE.toString());
        sysConfigEJB.update(sysConfig);

        authEJB.authenticate("user", "user");
    }

    @Transactional(TransactionMode.DISABLED)
    public void authenticateAdminNotAllowed() throws Exception {
        Date stamp = new Date();
        // Disallow login
        SysConfig sysConfig = sysConfigEJB.getSysConfig(SysConfigKey.ALLOW_LOGIN);
        sysConfig.setConfigValue(Boolean.FALSE.toString());
        sysConfigEJB.update(sysConfig);
        SysUser user = authEJB.authenticate("admin", "admin");
        assertUser(user, stamp);
    }

    @Transactional(TransactionMode.DISABLED)
    public void authenticateNextUserAllowed() throws Exception {
        Date stamp = new Date();
        // Allow next user login
        SysConfig sysConfig = sysConfigEJB.getSysConfig(SysConfigKey.ALLOW_NEXT_USERS);
        sysConfig.setConfigValue(Boolean.TRUE.toString());
        sysConfigEJB.update(sysConfig);
        SysUser user = authEJB.authenticate("e1234next", "e1234next");
        assertUser(user, stamp);
    }

    @Test(expected = AuthException.class)
    @Transactional(TransactionMode.DISABLED)
    public void authenticateNextUserNotAllowed() throws Exception {
        authEJB.authenticate("e1234next", "e1234next");
    }

    private void assertUser(SysUser user, Date stamp) {
        Assert.assertNotNull("User should not be null!", user);
        Assert.assertNotNull("Last login should not be null!", user.getLastLogin());
        Assert.assertTrue("Last login should be updated!", stamp.before(user.getLastLogin()));
        Assert.assertTrue("There should be secret key generated!", StringUtils.isNotEmpty(user.getSecretKey()));
        Assert.assertTrue("There should be email!", StringUtils.isNotEmpty(user.getEmail()));
        Assert.assertTrue("There should be real name!", StringUtils.isNotEmpty(user.getRealName()));
    }

    private void sleep(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000);
    }

    private void setMockGerritResponseDelay(Gerrit gerrit, int delay) {
        SshClient ssh = new SshClient(gerrit.getProjectAccessHost(), gerrit.getProjectAccessPort(), "snc", new File(gerrit.getPrivateKeyPath()));
        String response = ssh.execute("server event delay " + delay);
        log.info("Mock gerrit response: {}", response);
    }
}
