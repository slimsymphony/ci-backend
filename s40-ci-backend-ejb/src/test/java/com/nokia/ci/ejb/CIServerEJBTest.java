/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import static com.nokia.ci.ejb.CITestBase.createEntity;
import com.nokia.ci.ejb.model.CIServer;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class CIServerEJBTest extends EJBTestBase {

    private CIServerEJB testSubject;

    @Override
    @Before
    public void before() {
        super.before();

        testSubject = new CIServerEJB();
        testSubject.em = em;
        testSubject.cb = cb;
        testSubject.context = context;
        testSubject.ciServerLoadEJB = Mockito.mock(CIServerLoadEJB.class);
    }

    @Test
    public void resolveCIServerNormal() {
        CIServer s1 = createEntity(CIServer.class, 1L);
        CIServer s2 = createEntity(CIServer.class, 2L);
        CIServer s3 = createEntity(CIServer.class, 3L);
        List<CIServer> servers = new ArrayList<CIServer>();
        servers.add(s1);
        servers.add(s2);
        servers.add(s3);

        CIServer result = testSubject.resolveCIServer(servers);
        Assert.assertTrue(servers.contains(result));
    }

    @Test
    public void resolveCIServerEmpty() {
        List<CIServer> servers = new ArrayList<CIServer>();
        CIServer result = testSubject.resolveCIServer(servers);
        Assert.assertNull(result);
    }

    @Test
    public void resolveBestCIServer() {
        CIServer s1 = createEntity(CIServer.class, 1L);
        s1.setBuildsRunning(20);
        CIServer s2 = createEntity(CIServer.class, 2L);
        s2.setBuildsRunning(0);
        CIServer s3 = createEntity(CIServer.class, 3L);
        s3.setBuildsRunning(10);
        List<CIServer> servers = new ArrayList<CIServer>();
        servers.add(s1);
        servers.add(s2);
        servers.add(s3);

        CIServer result = testSubject.resolveCIServer(servers);
        Assert.assertEquals(s2, result);
    }
}
