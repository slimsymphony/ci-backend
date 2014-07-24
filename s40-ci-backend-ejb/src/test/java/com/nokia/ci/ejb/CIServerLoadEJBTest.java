/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import static com.nokia.ci.ejb.CITestBase.createEntity;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CIServer;
import java.util.UUID;
import javax.persistence.LockModeType;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author hhellgre
 */
public class CIServerLoadEJBTest extends EJBTestBase {

    private CIServerLoadEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new CIServerLoadEJB();
        ejb.em = em;
        ejb.cb = cb;
        ejb.context = context;
    }

    @Test
    public void buildGroupStartOnCIServer() throws NotFoundException {
        CIServer s1 = createEntity(CIServer.class, 1L);
        s1.setUuid(UUID.randomUUID().toString());
        s1.setBuildsRunning(0);
        Mockito.when(em.find(CIServer.class, s1.getId(), LockModeType.PESSIMISTIC_WRITE)).thenReturn(s1);

        ejb.startBuild(1L);

        Assert.assertEquals(1, s1.getBuildsRunning());
    }

    @Test
    public void buildGroupFinalizeOnCIServer() throws NotFoundException {
        CIServer s1 = createEntity(CIServer.class, 1L);
        s1.setBuildsRunning(20);
        s1.setUuid(UUID.randomUUID().toString());
        Mockito.when(em.find(CIServer.class, s1.getId(), LockModeType.PESSIMISTIC_WRITE)).thenReturn(s1);

        ejb.finalizeBuild(1L);

        Assert.assertEquals(19, s1.getBuildsRunning());
    }

    @Test
    public void buildGroupFinalizeOnCIServerZero() throws NotFoundException {
        CIServer s1 = createEntity(CIServer.class, 1L);
        s1.setBuildsRunning(0);
        s1.setUuid(UUID.randomUUID().toString());
        Mockito.when(em.find(CIServer.class, s1.getId(), LockModeType.PESSIMISTIC_WRITE)).thenReturn(s1);

        ejb.finalizeBuild(1L);

        Assert.assertEquals(0, s1.getBuildsRunning());
    }
}
