/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import static com.nokia.ci.ejb.CITestBase.createEntity;
import static com.nokia.ci.ejb.EJBTestBase.createTypedQueryMock;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.util.LDAPUser;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class SysUserEJBTest extends EJBTestBase {

    private SysUserEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();
        ejb = new SysUserEJB();
        ejb.em = em;
        ejb.cb = cb;
        ejb.context = context;
    }

    @Test
    public void getSysUserByLoginName() throws BackendAppException {
        // setup
        SysUser user = createEntity(SysUser.class, 1L);
        populateSysUser(user);

        TypedQuery mockQuery = createTypedQueryMock(user);
        mockCriteriaQuery(SysUser.class, mockQuery, SysUser.class);

        // run
        SysUser result = ejb.getSysUser(user.getLoginName());

        // verify
        verifySysUser(user, result);
    }

    @Test(expected = NotFoundException.class)
    public void getSysUserByLoginNameNotFound() throws NotFoundException {
        // setup
        TypedQuery mockQuery = createTypedQueryMock(new NoResultException());
        mockCriteriaQuery(SysUser.class, mockQuery, SysUser.class);

        // run
        ejb.getSysUser("not found");
    }

    @Test
    public void getSysUserBySecretKey() throws BackendAppException {
        // setup
        SysUser user = createEntity(SysUser.class, 1L);
        populateSysUser(user);

        TypedQuery mockQuery = createTypedQueryMock(user);
        mockCriteriaQuery(SysUser.class, mockQuery, SysUser.class);

        // run
        SysUser result = ejb.getSysUserWithSecretKey(user.getSecretKey());

        // verify
        verifySysUser(user, result);
    }

    @Test(expected = NotFoundException.class)
    public void getSysUserBySecretKeyNotFound() throws NotFoundException {
        // setup
        TypedQuery mockQuery = createTypedQueryMock(new NoResultException());
        mockCriteriaQuery(SysUser.class, mockQuery, SysUser.class);

        // run
        ejb.getSysUserWithSecretKey("not found");
    }

    @Test
    public void createUser() {
        // setup
        LDAPUser ldapUser = new LDAPUser();
        ldapUser.setEmail("test@test.com");
        ldapUser.setNokiaSite("FIHEL08");
        ldapUser.setRealname("Test user");
        ldapUser.setUsername("user");

        // run
        SysUser result = ejb.createUser(ldapUser);

        // verify
        Assert.assertEquals(ldapUser.getEmail(), result.getEmail());
        Assert.assertEquals("Europe/Helsinki", result.getTimezone());
        Assert.assertEquals(ldapUser.getUsername(), result.getLoginName());
        Assert.assertEquals(ldapUser.getRealname(), result.getRealName());
    }
}
