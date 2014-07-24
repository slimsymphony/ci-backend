/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import static com.nokia.ci.ejb.CITestBase.createEntity;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.InvalidPhaseException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Release;
import com.nokia.ci.ejb.util.RelationUtil;
import java.util.List;
import javax.enterprise.event.Event;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author hhellgre
 */
public class BuildGroupEJBTest extends EJBTestBase {

    private BuildGroupEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new BuildGroupEJB();
        ejb.em = em;
        ejb.cb = cb;
        ejb.ciServerEJB = Mockito.mock(CIServerEJB.class);
        ejb.bgFinishedEvent = (Event<Long>) Mockito.mock(Event.class);
        ejb.bgReleasedEvent = (Event<Long>) Mockito.mock(Event.class);
    }

    @Test(expected = InvalidPhaseException.class)
    public void alreadyFinishedUpdateTest() throws Exception {
        BuildGroup bg = createEntity(BuildGroup.class, 1L);
        mockBuildGroupRead(bg);
        bg.setPhase(BuildPhase.FINISHED);

        ejb.updateStatus(1L, BuildStatus.SUCCESS);
    }

    @Test
    public void notFinishedBuildsUpdateTest() throws Exception {
        BuildGroup bg = createEntity(BuildGroup.class, 1L);
        mockBuildGroupRead(bg);
        bg.setPhase(BuildPhase.STARTED);

        List<Build> builds = createEntityList(Build.class, 3);
        populateBuilds(builds);

        for (Build b : builds) {
            RelationUtil.relate(bg, b);
        }

        BuildGroup result = ejb.updateStatus(1L, BuildStatus.SUCCESS);

        Assert.assertEquals(bg.getPhase(), result.getPhase());
    }

    @Test
    public void finishBuildGroupTest() throws Exception {
        Job job = createEntity(Job.class, 1L);
        job.setLastFetchHead("123456789");

        BuildGroup bg = createEntity(BuildGroup.class, 1L);
        mockBuildGroupRead(bg);
        bg.setPhase(BuildPhase.STARTED);
        RelationUtil.relate(job, bg);

        List<Build> builds = createEntityList(Build.class, 3);
        populateBuilds(builds);

        for (Build b : builds) {
            b.setStatus(BuildStatus.SUCCESS);
            b.setPhase(BuildPhase.FINISHED);
            RelationUtil.relate(bg, b);
        }

        BuildGroup result = ejb.updateStatus(1L, BuildStatus.SUCCESS);

        Assert.assertEquals(BuildPhase.FINISHED, result.getPhase());
        Assert.assertEquals(job.getLastFetchHead(), job.getLastSuccesfullFetchHead());
        Mockito.verify(ejb.bgFinishedEvent, Mockito.times(1)).fire(1L);
    }

    @Test
    public void finishBuildGroupNullStatusTest() throws Exception {
        Job job = createEntity(Job.class, 1L);
        job.setLastFetchHead("123456789");

        BuildGroup bg = createEntity(BuildGroup.class, 1L);
        mockBuildGroupRead(bg);
        bg.setPhase(BuildPhase.STARTED);
        bg.setStatus(null);
        RelationUtil.relate(job, bg);

        BuildGroup result = ejb.updateStatus(1L, BuildStatus.SUCCESS);

        Assert.assertEquals(BuildPhase.FINISHED, result.getPhase());
        Assert.assertEquals(job.getLastFetchHead(), job.getLastSuccesfullFetchHead());
        Mockito.verify(ejb.bgFinishedEvent, Mockito.times(1)).fire(1L);
    }

    @Test
    public void buildGroupReleaseOkTest() throws Exception {
        BuildGroup bg = createEntity(BuildGroup.class, 1L);
        mockBuildGroupRead(bg);

        ejb.release(1L);

        Assert.assertNotNull(bg.getRelease());
        Mockito.verify(ejb.bgReleasedEvent, Mockito.times(1)).fire(1L);
    }

    @Test(expected = InvalidArgumentException.class)
    public void buildGroupReleaseFailTest() throws Exception {
        BuildGroup bg = createEntity(BuildGroup.class, 1L);
        mockBuildGroupRead(bg);

        Release release = createEntity(Release.class, 1L);
        RelationUtil.relate(bg, release);

        ejb.release(1L);
    }

    private void mockBuildGroupRead(BuildGroup build) {
        Mockito.when(em.find(BuildGroup.class, build.getId())).thenReturn(build);
    }
}
