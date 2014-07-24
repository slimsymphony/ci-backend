/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.event.IncidentEventContent;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.util.RelationUtil;
import javax.enterprise.event.Event;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class NotificationPluginEJBTest extends EJBTestBase {

    private NotificationPluginEJB ejb;
    final static String JOB_NAME = "testJob";
    final static String JOB_DISPLAY_NAME = "testJobDisplayName";
    final static Long JOB_ID = 1L;
    final static Long BUILD_ID = 1L;
    final static Long BUILD_GROUP_ID = 1L;
    final static int BUILD_NUMBER = 1;
    final static String JOB_URL = "/job/" + JOB_NAME + "/";
    final static String EXPECTED_JOB_URL = "http://localhost:9000/job/"
            + JOB_NAME + "/";
    final static String BUILD_URL = "http://localhost:9000/job/" + JOB_NAME
            + "/" + BUILD_NUMBER + "/";

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new NotificationPluginEJB();
        ejb.incidentEvents = Mockito.mock(Event.class);
        ejb.jobEJB = Mockito.mock(JobEJB.class);
        ejb.buildEJB = Mockito.mock(BuildEJB.class);
    }

    private static BuildNotification createNotification(BuildPhase buildPhase,
            BuildStatus buildStatus) {

        BuildNotification n = new BuildNotification();

        n.setJobUrl(JOB_URL);
        n.setBuildUrl(BUILD_URL);
        n.setJobName(JOB_NAME);
        n.setJobDisplayName(JOB_DISPLAY_NAME);
        n.setBuildNumber(BUILD_NUMBER);
        n.setBuildPhase(buildPhase);
        n.setBuildStatus(buildStatus);

        return n;
    }

    @Test
    public void processNotificationWithId() throws BackendAppException {
        // setup
        Build build = createBuildWithJob();
        build.setPhase(BuildPhase.CONFIGURED);

        Mockito.when(ejb.buildEJB.read(BUILD_ID)).thenReturn(build);

        BuildNotification n = createNotification(BuildPhase.STARTED, null);
        n.setBuildId(BUILD_ID);

        // Run
        Build result = ejb.processBuildNotification(n);

        // Verify
        Assert.assertEquals(result.getJobUrl(), EXPECTED_JOB_URL);
        Assert.assertEquals(result.getUrl(), n.getBuildUrl());
        Assert.assertEquals(result.getPhase(), n.getBuildPhase());
        Assert.assertNotNull(result.getStartTime());
        Assert.assertNull(result.getEndTime());
        Assert.assertEquals(build.getJobDisplayName(), n.getJobDisplayName());
    }

    @Test(expected = NotFoundException.class)
    public void processNotificationWithIdNotFound() throws BackendAppException {
        // setup
        Mockito.when(ejb.buildEJB.read(BUILD_ID)).thenThrow(
                new NotFoundException());
        BuildNotification n = new BuildNotification();
        n.setBuildPhase(BuildPhase.FINISHED);
        n.setBuildId(BUILD_ID);

        // run. should throw exception.
        ejb.processBuildNotification(n);
    }

    @Test
    public void multipleFinishedNotifications() throws BackendAppException, InterruptedException {
        // setup
        Build build = createBuildWithJob();
        build.setPhase(BuildPhase.CONFIGURED);

        Mockito.when(ejb.buildEJB.read(BUILD_ID)).thenReturn(build);

        BuildNotification n = createNotification(BuildPhase.STARTED, null);
        BuildNotification finished = createNotification(BuildPhase.FINISHED, null);
        n.setBuildId(BUILD_ID);
        finished.setBuildId(BUILD_ID);

        // Run
        ejb.processBuildNotification(n);
        Thread.sleep(2000L);
        ejb.processBuildNotification(finished);
        Build result = ejb.processBuildNotification(finished);

        // Verify
        Assert.assertEquals(result.getPhase(), BuildPhase.FINISHED);
        Assert.assertNotNull(result.getStartTime());
        Assert.assertNotNull(result.getEndTime());
        Assert.assertEquals(result.getStartTime().before(result.getEndTime()), true);
        Mockito.verify(ejb.incidentEvents).fire(Mockito.any(IncidentEventContent.class));
    }

    @Test
    public void finishBeforeStart() throws BackendAppException {
        // setup
        Build build = createBuildWithJob();
        build.setPhase(BuildPhase.FINISHED);

        Mockito.when(ejb.buildEJB.read(BUILD_ID)).thenReturn(build);

        BuildNotification n = createNotification(BuildPhase.STARTED, null);
        n.setBuildId(BUILD_ID);

        // Run
        Build result = ejb.processBuildNotification(n);

        // Verify
        Assert.assertEquals(result.getPhase(), BuildPhase.FINISHED);
        Assert.assertNotNull(result.getStartTime());
        Assert.assertNull(result.getEndTime());
        Mockito.verify(ejb.incidentEvents).fire(Mockito.any(IncidentEventContent.class));
    }

    @Test
    public void urlParsing() {

        final String jobURLFromNotification = "/job/be-test-job-3/";
        final String expectedJobURL = "http://localhost:9000/job/be-test-job-3/";
        final String buildUrl = "http://localhost:9000/job/be-test-job-3/" + BUILD_NUMBER + "/";

        String jobURL = ejb.createJobURL(jobURLFromNotification, buildUrl);
        Assert.assertEquals(jobURL, expectedJobURL);
    }

    @Test
    public void urlParsingWithOutTrailingSlash() {

        final String jobURLFromNotification = "/job/be-test-job-3/";
        final String expectedJobURL = "http://localhost:9000/job/be-test-job-3/";
        final String buildUrl = "http://localhost:9000/job/be-test-job-3/" + BUILD_NUMBER;

        String jobURL = ejb.createJobURL(jobURLFromNotification, buildUrl);
        Assert.assertEquals(jobURL, expectedJobURL);
    }

    @Test
    public void urlParsingJobNotificationFormatChange() {

        final String jobURLFromNotification = "job/be-test-job-3";
        final String expectedJobURL = "http://localhost:9000/job/be-test-job-3";
        final String buildUrl = "http://localhost:9000/job/be-test-job-3/" + BUILD_NUMBER;

        String jobURL = ejb.createJobURL(jobURLFromNotification, buildUrl);
        Assert.assertEquals(jobURL, expectedJobURL);
    }

    @Test
    public void urlParsingJobSameAsBuild() {

        final String jobURLFromNotification = "job/be-test-job-3";
        final String expectedJobURL = "http://localhost:9000/job/be-test-job-3";
        final String buildUrl = expectedJobURL;

        String jobURL = ejb.createJobURL(jobURLFromNotification, buildUrl);
        Assert.assertEquals(jobURL, expectedJobURL);
    }

    @Test
    public void urlParsingFalseJobName() {

        final String jobURLFromNotification = "/job/be-test-job-2/";
        final String buildUrl = "http://localhost:9000/job/be-test-job-3/" + BUILD_NUMBER;

        String jobURL = ejb.createJobURL(jobURLFromNotification, buildUrl);
        Assert.assertEquals(jobURL, "");
    }

    @Test
    public void urlParsingWithoutJobUrl() {

        final String jobURLFromNotification = "";
        final String buildUrl = "http://localhost:9000/job/be-test-job-3/" + BUILD_NUMBER;

        String jobURL = ejb.createJobURL(jobURLFromNotification, buildUrl);
        Assert.assertEquals(jobURL, "");
    }

    @Test
    public void urlParsingWithoutBuildUrl() {

        final String jobURLFromNotification = "/job/be-test-job-2/";
        final String buildUrl = "";

        String jobURL = ejb.createJobURL(jobURLFromNotification, buildUrl);
        Assert.assertEquals(jobURL, "");
    }

    private static Build createBuildWithJob() {
        Build build = createEntity(Build.class, BUILD_ID);
        build.setBuildNumber(BUILD_NUMBER);
        build.setPhase(BuildPhase.STARTED);

        BuildGroup buildGroup = createEntity(BuildGroup.class, BUILD_GROUP_ID);
        RelationUtil.relate(buildGroup, build);

        Job job = createEntity(Job.class, JOB_ID);
        job.setName(JOB_NAME);

        RelationUtil.relate(job, buildGroup);

        return build;
    }
}
