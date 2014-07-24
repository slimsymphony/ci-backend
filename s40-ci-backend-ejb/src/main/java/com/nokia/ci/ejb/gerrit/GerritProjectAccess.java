package com.nokia.ci.ejb.gerrit;

import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.event.AuthSuccessEvent;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.ssh.SshClient;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import org.apache.commons.lang3.StringUtils;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
public class GerritProjectAccess {

    private static Logger log = LoggerFactory.getLogger(GerritProjectAccess.class);
    private static final int DEFAULT_GERRIT_PROJECT_ACCESS_PORT = 22;
    private static final String DEFAULT_GERRIT_PROJECT_ACCESS_USERNAME = "snc";
    @EJB
    private GerritEJB gerritEJB;
    @EJB
    private ProjectEJB projectEJB;
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private SysUserEJB sysUserEJB;
    @Resource(lookup = "java:jboss/infinispan/cache/ci20/project-access-cache")
    private Cache<String, SysUser> cache;

    @Asynchronous
    public void loginSuccess(@Observes(during = TransactionPhase.AFTER_SUCCESS) @AuthSuccessEvent Long id) {
        log.info("Updating project access for user: {}", id);

        SysUser user;
        try {
            user = sysUserEJB.read(id);
        } catch (NotFoundException ex) {
            log.error("Could not find user with id {}, stopping project access update", id);
            return;
        }

        log.info("Starting to update project access from Gerrit for user {}", user);
        List<Gerrit> gerrits = gerritEJB.readAll();

        for (Gerrit g : gerrits) {
            if (StringUtils.isEmpty(g.getPrivateKeyPath())) {
                log.warn("Private key path not set for gerrit {}", g);
                continue;
            }
            File privateKeyPath = new File(g.getPrivateKeyPath());
            String projectAccessHost = g.getProjectAccessHost();
            Integer projectAccessPort = g.getProjectAccessPort();
            if (StringUtils.isEmpty(projectAccessHost)) {
                log.warn("Project access host not set for gerrit {}", g);
                continue;
            }
            if (projectAccessPort == null) {
                int defaultProjectAccessPort = sysConfigEJB.getValue(SysConfigKey.GERRIT_PROJECT_ACCESS_PORT, DEFAULT_GERRIT_PROJECT_ACCESS_PORT);
                projectAccessPort = new Integer(defaultProjectAccessPort);
                log.warn("Project access port not set for gerrit {}, using default value {}", g, projectAccessPort);
            }
            String projectAccessUsername = sysConfigEJB.getValue(SysConfigKey.GERRIT_PROJECT_ACCESS_USERNAME, DEFAULT_GERRIT_PROJECT_ACCESS_USERNAME);
            SshClient ssh = new SshClient(projectAccessHost, projectAccessPort, projectAccessUsername, privateKeyPath);
            String access = ssh.execute(user.getLoginName());

            if (access == null || access.isEmpty()) {
                continue;
            }

            try {
                sysUserEJB.clearProjectAccess(user.getId(), g);
            } catch (NotFoundException ex) {
                log.error("Could not clear project access for user " + user.getLoginName());
                continue;
            }

            List<String> projects = Arrays.asList(access.split("\\r?\\n"));

            for (String project : projects) {
                List<Project> ps = projectEJB.getProjectsByGerritProject(project, g);
                for (Project p : ps) {
                    RelationUtil.relate(user, p);
                }
            }
        }

        user.setProjectAccess(sysUserEJB.getProjectAccess(user.getId()));
        user.setProjectAdminAccess(sysUserEJB.getProjectAdminAccess(user.getId()));

        // Save project access to infinispan
        cache.put(user.getLoginName(), user);

        log.info("Project access update completed for user {}", user);
    }
}
