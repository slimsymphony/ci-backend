/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.session;

import com.nokia.ci.api.session.SessionManager.Session;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.infinispan.Cache;

/**
 *
 * @author jajuutin
 */
public class SessionManagerTest {

    SessionManager sm;    
    
    @Before
    public void before() {
        sm = new SessionManager();
        sm.setCache(Mockito.mock(Cache.class));
    }

    @Test
    public void newSession() {
        final Long userId = 1L;
        Session s = sm.newSession(userId);
        Assert.assertEquals(s.getSysUserId(), userId);
        Cache<String, Session> cache = sm.getCache();
        Mockito.verify(cache, Mockito.atLeastOnce()).put(s.getToken(),
                s);
    }

    @Test
    public void deleteSession() {
        Session s = new Session();
        s.setSysUserId(1L);
        s.setToken("token");
        sm.deleteSession(s);
        Cache<String, Session> cache = sm.getCache();
        Mockito.verify(cache, Mockito.atLeastOnce()).remove(s.getToken());
    }

    @Test
    public void getCache() {
        Assert.assertNotNull(sm.getCache());
    }
}
