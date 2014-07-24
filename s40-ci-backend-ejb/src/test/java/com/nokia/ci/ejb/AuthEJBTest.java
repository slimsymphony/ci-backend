/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.AuthException;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.LoginNotAllowedException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.gerrit.GerritProjectAccess;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.util.LDAPUser;
import com.nokia.ci.ejb.util.LDAPUtil;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.enterprise.event.Event;
import javax.persistence.EntityManager;
import org.infinispan.Cache;

/**
 *
 * @author miikka
 */
public class AuthEJBTest {

    private AuthEJB ejb;
    private static final String LOGIN_NAME = "testName";
    private static final String REAL_NAME = "Test Name";
    private static final String PASSWORD = "testPw";
    private static final String EMAIL = "test@email.com";
    private static final String NOKIASITE = "FIOUL08";
    private final Long SYSUSER_ID = 1L;
    private static final String NEXT_LOGIN_NAME = "e0123456";
    private static final String NEXT_REAL_NAME = "Next Test Name";
    private static final String NEXT_PASSWORD = "testNextPw";
    private static final String NEXT_EMAIL = "ext-test@ext.test.email.com";
    private static final String NEXT_NOKIASITE = "EXTSITE01";
    private final Long NEXT_SYSUSER_ID = 2L;

    @Before
    public void before() {
        ejb = new AuthEJB();
        ejb.ldapUtil = Mockito.mock(LDAPUtil.class);
        ejb.sysUserEJB = Mockito.mock(SysUserEJB.class);
        ejb.cache = Mockito.mock(Cache.class);
        ejb.sysConfigEJB = Mockito.mock(SysConfigEJB.class);
        ejb.gerritEJB = Mockito.mock(GerritEJB.class);
        ejb.projectEJB = Mockito.mock(ProjectEJB.class);
        ejb.loginSucceedEvents = (Event<Long>) Mockito.mock(Event.class);
        ejb.loginFailureEvents = (Event<String>) Mockito.mock(Event.class);
        ejb.gerritProjectAccess = Mockito.mock(GerritProjectAccess.class);
        ejb.cache = Mockito.mock(Cache.class);
        ejb.em = Mockito.mock(EntityManager.class);
    }

    SysUser createTestSysUser(RoleType roleType) {
        SysUser sysUser = new SysUser();
        sysUser.setId(SYSUSER_ID);
        sysUser.setLoginName(LOGIN_NAME);
        sysUser.setRealName(REAL_NAME);
        sysUser.setEmail(EMAIL);
        sysUser.setUserRole(roleType);
        return sysUser;
    }

    SysUser createNextTestSysUser(RoleType roleType) {
        SysUser sysUser = new SysUser();
        sysUser.setId(NEXT_SYSUSER_ID);
        sysUser.setLoginName(NEXT_LOGIN_NAME);
        sysUser.setRealName(NEXT_REAL_NAME);
        sysUser.setEmail(NEXT_EMAIL);
        sysUser.setUserRole(roleType);
        return sysUser;
    }

    @Test
    public void authOkTest() throws Exception {
        // setup
        LDAPUser ldapUser = new LDAPUser(LOGIN_NAME, EMAIL, REAL_NAME, NOKIASITE);
        Mockito.when(ejb.ldapUtil.authenticate(LOGIN_NAME, PASSWORD)).thenReturn(ldapUser);

        SysUser sysUser = createTestSysUser(RoleType.USER);
        Mockito.when(ejb.sysUserEJB.getSysUser(LOGIN_NAME)).thenReturn(sysUser);
        Mockito.when(ejb.sysUserEJB.update(sysUser)).thenReturn(sysUser);

        Mockito.when(ejb.sysConfigEJB.isLoginAllowed()).thenReturn(true);

        // run
        SysUser user = ejb.authenticate(LOGIN_NAME, PASSWORD);

        // verify
        Assert.assertEquals(sysUser.getLoginName(), user.getLoginName());
        Assert.assertEquals(sysUser.getEmail(), user.getEmail());
    }

    @Test(expected = AuthException.class)
    public void ldapNullTest() throws Exception {
        // setup
        Mockito.when(ejb.ldapUtil.authenticate(LOGIN_NAME, PASSWORD)).thenReturn(null);

        // run
        SysUser user = ejb.authenticate(LOGIN_NAME, PASSWORD);
    }

    @Test(expected = AuthException.class)
    public void authNextFailTest() throws Exception {
        // setup
        LDAPUser ldapUser = new LDAPUser(NEXT_LOGIN_NAME, NEXT_EMAIL, NEXT_REAL_NAME, NEXT_NOKIASITE);
        Mockito.when(ejb.ldapUtil.authenticate(NEXT_LOGIN_NAME, NEXT_PASSWORD)).thenReturn(ldapUser);

        SysUser sysUser = createNextTestSysUser(RoleType.USER);
        Mockito.when(ejb.sysUserEJB.getSysUser(NEXT_LOGIN_NAME)).thenReturn(sysUser);

        Mockito.when(ejb.sysConfigEJB.isLoginAllowed()).thenReturn(true);

        // run
        ejb.authenticate(NEXT_LOGIN_NAME, NEXT_PASSWORD);
    }

    @Test
    public void authNextOkTest() throws Exception {
        // setup
        LDAPUser ldapUser = new LDAPUser(NEXT_LOGIN_NAME, NEXT_EMAIL, NEXT_REAL_NAME, NEXT_NOKIASITE);
        Mockito.when(ejb.ldapUtil.authenticate(NEXT_LOGIN_NAME, NEXT_PASSWORD)).thenReturn(ldapUser);

        SysUser sysUser = createNextTestSysUser(RoleType.USER);
        Mockito.when(ejb.sysUserEJB.getSysUser(NEXT_LOGIN_NAME)).thenReturn(sysUser);
        Mockito.when(ejb.sysUserEJB.update(sysUser)).thenReturn(sysUser);

        Mockito.when(ejb.sysConfigEJB.isLoginAllowed()).thenReturn(true);
        Mockito.when(ejb.sysConfigEJB.isNextUsersAllowed()).thenReturn(true);

        // run
        SysUser user = ejb.authenticate(NEXT_LOGIN_NAME, NEXT_PASSWORD);

        // verify
        Assert.assertEquals(sysUser.getLoginName(), user.getLoginName());
        Assert.assertEquals(sysUser.getEmail(), user.getEmail());
    }

    @Test(expected = LoginNotAllowedException.class)
    public void authOkLoginNotAllowedUserTest() throws Exception {
        // setup
        LDAPUser ldapUser = new LDAPUser(LOGIN_NAME, EMAIL, REAL_NAME, NOKIASITE);
        Mockito.when(ejb.ldapUtil.authenticate(LOGIN_NAME, PASSWORD)).thenReturn(ldapUser);

        SysUser sysUser = createTestSysUser(RoleType.USER);
        Mockito.when(ejb.sysUserEJB.getSysUser(LOGIN_NAME)).thenReturn(sysUser);
        Mockito.when(ejb.sysUserEJB.update(sysUser)).thenReturn(sysUser);

        Mockito.when(ejb.sysConfigEJB.isLoginAllowed()).thenReturn(false);

        // run
        ejb.authenticate(LOGIN_NAME, PASSWORD);
    }

    @Test
    public void authOkLoginNotAllowedSystemAdminTest() throws Exception {
        // setup
        LDAPUser ldapUser = new LDAPUser(LOGIN_NAME, EMAIL, REAL_NAME, NOKIASITE);
        Mockito.when(ejb.ldapUtil.authenticate(LOGIN_NAME, PASSWORD)).thenReturn(ldapUser);

        SysUser sysUser = createTestSysUser(RoleType.SYSTEM_ADMIN);
        Mockito.when(ejb.sysUserEJB.getSysUser(LOGIN_NAME)).thenReturn(sysUser);
        Mockito.when(ejb.sysUserEJB.update(sysUser)).thenReturn(sysUser);

        Mockito.when(ejb.sysConfigEJB.isLoginAllowed()).thenReturn(false);

        // run
        SysUser user = ejb.authenticate(LOGIN_NAME, PASSWORD);

        // verify
        Assert.assertEquals(sysUser.getLoginName(), user.getLoginName());
        Assert.assertEquals(sysUser.getEmail(), user.getEmail());
    }

    @Test(expected = AuthException.class)
    public void authFailTest() throws Exception {
        // setup
        Mockito.when(ejb.ldapUtil.authenticate(LOGIN_NAME, PASSWORD)).thenThrow(new LDAPException(ResultCode.AUTHORIZATION_DENIED, "No go!"));

        // run
        ejb.authenticate(LOGIN_NAME, PASSWORD);
    }

    @Test(expected = AuthException.class)
    public void authGeneralAuthExTest() throws Exception {
        // setup
        Mockito.when(ejb.ldapUtil.authenticate(LOGIN_NAME, PASSWORD)).thenThrow(new GeneralSecurityException("No go!"));

        // run
        ejb.authenticate(LOGIN_NAME, PASSWORD);
    }

    @Test
    public void authOkTestWithCreateSysUser() throws Exception {
        // setup
        LDAPUser user = new LDAPUser(LOGIN_NAME, EMAIL, REAL_NAME, NOKIASITE);
        SysUser sysUser = new SysUser();
        sysUser.setLoginName(user.getUsername());
        sysUser.setUserRole(RoleType.USER);
        sysUser.setEmail(user.getEmail());

        Mockito.when(ejb.ldapUtil.authenticate(LOGIN_NAME, PASSWORD)).thenReturn(user);
        Mockito.when(ejb.sysUserEJB.getSysUser(LOGIN_NAME)).thenThrow(new NotFoundException());
        Mockito.when(ejb.sysUserEJB.createUser(user)).thenReturn(sysUser);

        Mockito.when(ejb.sysConfigEJB.isLoginAllowed()).thenReturn(true);

        // run
        SysUser ret = ejb.authenticate(LOGIN_NAME, PASSWORD);

        // verify
        Assert.assertNotNull(ret);
        Assert.assertEquals(LOGIN_NAME, ret.getLoginName());
        Assert.assertEquals(EMAIL, ret.getEmail());
    }

    @Test
    public void testNextFilter() {
        ArrayList<String> noeOrFalse = new ArrayList<String>();

        noeOrFalse.add("");
        noeOrFalse.add("ext-mauri.korpkonen@nokia.com");
        noeOrFalse.add("mauri.korpkonen@nokia.com");
        noeOrFalse.add("ext-mauri.korpko123123123123nen@nokia.com");
        noeOrFalse.add("ext-mauri.korpkoneeqweqweqwe312312n@nokia.com");
        noeOrFalse.add("eeext-mauri.li123123123321ukkonen@nokia.com");
        noeOrFalse.add("ext-meee123123iikka.korpkoneneeee12323123@nokia.com");
        noeOrFalse.add("ext-mauri.korpkonenee21312312@nokia.com");
        noeOrFalse.add("ext-mauri.korpeee123konen@nokia.com");
        noeOrFalse.add("ee@nokia.com");
        noeOrFalse.add("ee000@");
        noeOrFalse.add("12312312312@nokia.com");
        noeOrFalse.add("ee---123-12-3123wweee6543@nokia.com");
        noeOrFalse.add("ee1231231312123@nokia.com");
        noeOrFalse.add("ee1234567890@nokia.com");
        noeOrFalse.add("ee123@nokia.com");
        noeOrFalse.add("ee03123@nokia.com");

        ArrayList<String> next = new ArrayList<String>();

        next.add("E2312549@nokia.com");
        next.add("e123123");
        next.add("e123@nokia.com");
        next.add("e12312549@nokia.com");
        next.add("e1231231");

        for (String mail : noeOrFalse) {
            Assert.assertFalse(LDAPUtil.isNextUser(mail));
        }
        for (String mail : next) {
            Assert.assertTrue(LDAPUtil.isNextUser(mail));
        }

    }

    @Test(expected = InvalidArgumentException.class)
    public void authTestInvArg1() throws NotFoundException, InvalidArgumentException, AuthException, LoginNotAllowedException {
        ejb.authenticate(null, "not_null");
    }

    @Test(expected = InvalidArgumentException.class)
    public void authTestInvArg2() throws NotFoundException, InvalidArgumentException, AuthException, LoginNotAllowedException {
        ejb.authenticate("not_null", null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void authTestInvArg3() throws NotFoundException, InvalidArgumentException, AuthException, LoginNotAllowedException {
        ejb.authenticate("", "");
    }
}
