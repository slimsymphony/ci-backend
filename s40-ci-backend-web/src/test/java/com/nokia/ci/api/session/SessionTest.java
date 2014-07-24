/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.session;

import com.nokia.ci.api.session.SessionManager.Session;
import com.nokia.ci.ejb.testtools.PojoTestUtil;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class SessionTest {
    @Test
    public void testSession() throws Exception {
        PojoTestUtil.testGetAndSet(Session.class);
    }
}
