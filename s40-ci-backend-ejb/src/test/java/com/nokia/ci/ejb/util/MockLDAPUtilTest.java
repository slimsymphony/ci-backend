/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class MockLDAPUtilTest {

    private LDAPUtil util;

    @Before
    public void before() {
        util = new LDAPUtil();
    }

    @Test
    public void nextUserTest() {
        boolean result = LDAPUtil.isNextUser("e1235790");
        Assert.assertEquals(true, result);
    }

    @Test
    public void notNextUserTest() {
        boolean result = LDAPUtil.isNextUser("teuvo");
        Assert.assertEquals(false, result);
    }

    @Test
    public void getByUsernameTest() throws Exception {
        LDAPUser user = util.getByUsername("admin");
        verifyMockUser(user, "admin");
    }

    @Test
    public void authenticateOkTest() throws Exception {
        LDAPUser user = util.authenticate("admin", "admin");
        verifyMockUser(user, "admin");
    }

    @Test
    public void authenticateFailTest() throws Exception {
        LDAPUser user = util.authenticate("admin", "user");
        Assert.assertNull(user);
    }

    @Test
    public void searchTest() throws Exception {
        List<LDAPUser> users = util.search("admin");
        verifyMockUser(users.get(0), "admin");

        users = util.search("admin@mock.com");
        verifyMockUser(users.get(0), "admin");
    }

    @Test
    public void getUserIdByEmailTest() throws Exception {
        String name = util.getUserIdByEmail("admin@mock.com");
        Assert.assertEquals("admin", name);

        name = util.getUserIdByEmail("admin");
        Assert.assertNull(name);
    }

    private void verifyMockUser(LDAPUser user, String username) {
        Assert.assertNotNull(user);
        Assert.assertEquals(username, user.getUsername());
        Assert.assertEquals("FIHEL00", user.getNokiaSite());
        Assert.assertEquals(username + " MOCK", user.getRealname());
        Assert.assertEquals(username + "@mock.com", user.getEmail());
    }
}
