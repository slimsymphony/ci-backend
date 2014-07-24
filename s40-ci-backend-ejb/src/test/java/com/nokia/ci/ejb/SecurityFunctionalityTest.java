/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import static com.nokia.ci.ejb.CITestBase.createEntity;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.exception.UserSessionException;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SecurityEntityImpl;
import com.nokia.ci.ejb.model.SysUser;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Predicate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 *
 * @author hhellgre
 */
public class SecurityFunctionalityTest extends EJBTestBase {

    SecurityFunctionalityImpl impl;

    @Override
    @Before
    public void before() {
        super.before();

        impl = new SecurityFunctionalityImpl();
        impl.em = em;
        impl.cb = cb;
        impl.context = context;
        impl.sysUserCache = new CacheImpl();
    }

    @Test
    public void init() {
        Mockito.when(em.getCriteriaBuilder()).thenReturn(cb);
        impl.init();
        Assert.assertNotNull(impl.cb);
    }

    @Test
    public void create() {
        // setup
        SecurityEntityImpl entityImpl = createEntity(SecurityEntityImpl.class, 1L);

        entityImpl.setProjectId(1L);

        // run
        impl.create(entityImpl);

        // verify
        ArgumentCaptor<SecurityEntityImpl> persistedEntityImpl = ArgumentCaptor.forClass(SecurityEntityImpl.class);
        Mockito.verify(em, Mockito.atLeastOnce()).persist(persistedEntityImpl.capture());
        SecurityEntityImpl result = persistedEntityImpl.getValue();
        Assert.assertTrue(entityImpl == result);
        Assert.assertTrue(result.getProjectId() == 1L);

        Mockito.verify(em, Mockito.atLeastOnce()).flush();
    }

    @Test
    public void readSecureSuccess() throws Exception {
        // setup
        SysUser mockUser = mockUser("user");
        List<Project> projectAccess = new ArrayList<Project>();
        projectAccess.add(getMockProject(1L));
        mockUser.setProjectAccess(projectAccess);
        impl.sysUserCache.put("user", mockUser);

        SecurityEntityImpl entityImpl = createEntity(SecurityEntityImpl.class, 1L);
        entityImpl.setProjectId(1L);
        Mockito.when(em.find(SecurityEntityImpl.class, 1L)).thenReturn(entityImpl);

        // run
        impl.create(entityImpl);
        SecurityEntityImpl result = impl.readSecure(1L);

        Assert.assertTrue(result.getId() == 1L);
        Assert.assertTrue(result.getProjectId() == 1L);
    }

    @Test(expected = UnauthorizedException.class)
    public void readSecureFailure() throws Exception {
        SysUser mockUser = mockUser("user");
        impl.sysUserCache.put("user", mockUser);

        SecurityEntityImpl entityImpl = createEntity(SecurityEntityImpl.class, 1L);
        entityImpl.setProjectId(1L);
        Mockito.when(em.find(SecurityEntityImpl.class, 1L)).thenReturn(entityImpl);

        // run
        impl.create(entityImpl);
        SecurityEntityImpl result = impl.readSecure(1L);
    }

    @Test(expected = UserSessionException.class)
    public void readSecureSessionException() throws Exception {
        SysUser mockUser = mockUser("user");
        mockUser.setProjectAccess(null);
        impl.sysUserCache.put("user", mockUser);

        SecurityEntityImpl entityImpl = createEntity(SecurityEntityImpl.class, 1L);
        entityImpl.setProjectId(1L);
        Mockito.when(em.find(SecurityEntityImpl.class, 1L)).thenReturn(entityImpl);

        // run
        impl.create(entityImpl);
        SecurityEntityImpl result = impl.readSecure(1L);
    }

    @Test(expected = UserSessionException.class)
    public void readSecureNullSession() throws Exception {
        // setup
        CallerPrincipalMock p = new CallerPrincipalMock();
        p.setName(null);
        Mockito.when(impl.context.getCallerPrincipal()).thenReturn(p);

        SecurityEntityImpl entityImpl = createEntity(SecurityEntityImpl.class, 1L);
        entityImpl.setProjectId(1L);
        Mockito.when(em.find(SecurityEntityImpl.class, 1L)).thenReturn(entityImpl);

        // run
        impl.readSecure(1L);
    }

    @Test
    public void projectAdminReadSecure() throws Exception {
        // setup
        SysUser mockUser = mockUser("user");
        List<Project> projectAccess = new ArrayList<Project>();
        projectAccess.add(getMockProject(1L));
        mockUser.setProjectAdminAccess(projectAccess);
        mockUser.setUserRole(RoleType.PROJECT_ADMIN);
        impl.sysUserCache.put("user", mockUser);

        SecurityEntityImpl entityImpl = createEntity(SecurityEntityImpl.class, 1L);
        entityImpl.setProjectId(1L);
        Mockito.when(em.find(SecurityEntityImpl.class, 1L)).thenReturn(entityImpl);

        // run
        impl.create(entityImpl);
        SecurityEntityImpl result = impl.readSecure(1L);

        Assert.assertTrue(result.getId() == 1L);
        Assert.assertTrue(result.getProjectId() == 1L);
    }

    @Test
    public void adminReadSecure() throws Exception {
        // setup
        SysUser mockUser = mockUser("user");
        mockUser.setUserRole(RoleType.SYSTEM_ADMIN);
        impl.sysUserCache.put("user", mockUser);

        SecurityEntityImpl entityImpl = createEntity(SecurityEntityImpl.class, 1L);
        entityImpl.setProjectId(1L);
        Mockito.when(em.find(SecurityEntityImpl.class, 1L)).thenReturn(entityImpl);

        // run
        impl.create(entityImpl);
        SecurityEntityImpl result = impl.readSecure(1L);

        Assert.assertTrue(result.getId() == 1L);
        Assert.assertTrue(result.getProjectId() == 1L);
    }

    @Test
    public void adminSecurityPredicate() throws Exception {
        // setup
        SysUser mockUser = mockUser("user");
        mockUser.setUserRole(RoleType.SYSTEM_ADMIN);
        impl.sysUserCache.put("user", mockUser);

        // run
        Predicate pred = impl.getSecurityPredicate(null);

        Assert.assertEquals(pred, cb.conjunction());
    }

    @Test
    public void noUserSecurityPredicate() throws Exception {
        // run
        Predicate pred = impl.getSecurityPredicate(null);

        Assert.assertEquals(pred, cb.disjunction());
    }

    private Project getMockProject(Long id) {
        Project p = new Project();
        p.setId(id);
        return p;
    }

    private SysUser mockUser(String name) {
        SysUser mockUser = new SysUser();
        mockUser.setLoginName(name);
        mockUser.setId(1L);
        List<Project> projectAccess = new ArrayList<Project>();
        mockUser.setProjectAccess(projectAccess);
        mockUser.setProjectAdminAccess(projectAccess);
        CallerPrincipalMock p = new CallerPrincipalMock();
        p.setName(name);
        Mockito.when(impl.context.getCallerPrincipal()).thenReturn(p);
        return mockUser;
    }
}
