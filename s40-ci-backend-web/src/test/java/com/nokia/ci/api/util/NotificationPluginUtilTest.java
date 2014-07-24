/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.client.model.notificationplugin.NpBuildPhase;
import com.nokia.ci.client.model.notificationplugin.NpBuildStatus;
import com.nokia.ci.client.model.notificationplugin.NpBuildView;
import com.nokia.ci.client.model.notificationplugin.NpJobView;
import com.nokia.ci.ejb.BuildNotification;
import com.nokia.ci.ejb.CreatorNotification;
import com.nokia.ci.ejb.GerritTriggerNotification;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import java.util.HashMap;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class NotificationPluginUtilTest {

    final String JOB_NAME = "testJob";
    final String JOB_DISPLAY_NAME = "displayName";
    final int BUILD_NUMBER = 100;
    final String JOB_URL = "http://localhost/testJob/";
    final String BUILD_FULL_URL = JOB_URL + BUILD_NUMBER;
    final NpBuildPhase BUILD_PHASE = NpBuildPhase.FINISHED;
    final NpBuildStatus BUILD_STATUS = NpBuildStatus.SUCCESS;
    final String BUILD_REFSPEC = "REFSPEC";
    final String BUILD_ID = "5000";
    final String BUILD_GROUP_ID = "1000";

    @Test
    public void convertBuildPhaseStarted() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildPhase(NpBuildPhase.STARTED),
                BuildPhase.STARTED);
    }

    @Test
    public void convertBuildPhaseCompleted() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildPhase(NpBuildPhase.COMPLETED),
                BuildPhase.COMPLETED);
    }

    @Test
    public void convertBuildPhaseFinished() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildPhase(NpBuildPhase.FINISHED),
                BuildPhase.FINISHED);
    }

    @Test
    public void convertBuildPhaseNull() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildPhase(null),
                null);
    }

    @Test
    public void convertBuildStatusSuccess() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildStatus(NpBuildStatus.SUCCESS),
                BuildStatus.SUCCESS);
    }

    @Test
    public void convertBuildStatusFailure() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildStatus(NpBuildStatus.FAILURE),
                BuildStatus.FAILURE);
    }

    @Test
    public void convertBuildStatusAborted() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildStatus(NpBuildStatus.ABORTED),
                BuildStatus.ABORTED);
    }

    @Test
    public void convertBuildStatusNotBuilt() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildStatus(NpBuildStatus.NOT_BUILT),
                BuildStatus.NOT_BUILT);
    }

    @Test
    public void convertBuildStatusUnstable() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildStatus(NpBuildStatus.UNSTABLE),
                BuildStatus.UNSTABLE);
    }

    @Test
    public void convertBuildStatusNull() {
        Assert.assertEquals(
                NotificationPluginUtil.convertBuildStatus(null),
                null);
    }

    @Test
    public void toNotificationHappyPath() {

        NpJobView jobView = constructNpJobView();

        // run
        BuildNotification n = NotificationPluginUtil.toBuildNotification(jobView);

        // verify
        Assert.assertEquals(n.getBuildNumber(), BUILD_NUMBER);
        Assert.assertEquals(n.getBuildPhase(),
                NotificationPluginUtil.convertBuildPhase(BUILD_PHASE));
        Assert.assertEquals(n.getBuildStatus(),
                NotificationPluginUtil.convertBuildStatus(BUILD_STATUS));
        Assert.assertEquals(n.getBuildUrl(), BUILD_FULL_URL);
        Assert.assertEquals(n.getJobDisplayName(), JOB_DISPLAY_NAME);
        Assert.assertEquals(n.getJobName(), JOB_NAME);
        Assert.assertEquals(n.getJobUrl(), JOB_URL);
        Assert.assertEquals(n.getBuildRefSpec(), BUILD_REFSPEC);
        Assert.assertEquals(n.getBuildId(), Long.valueOf(BUILD_ID));
        Assert.assertEquals(n.getBuildGroupId(), Long.valueOf(BUILD_GROUP_ID));
    }

    @Test
    public void creatorNotificationTest() {
        NpJobView jobView = constructNpJobView();

        CreatorNotification n = NotificationPluginUtil.toCreatorNotification(jobView);

        // verify
        Assert.assertEquals(n.getBuildNumber(), BUILD_NUMBER);
        Assert.assertEquals(n.getBuildPhase(),
                NotificationPluginUtil.convertBuildPhase(BUILD_PHASE));
        Assert.assertEquals(n.getBuildStatus(),
                NotificationPluginUtil.convertBuildStatus(BUILD_STATUS));
        Assert.assertEquals(n.getBuildUrl(), BUILD_FULL_URL);
        Assert.assertEquals(n.getJobDisplayName(), JOB_DISPLAY_NAME);
        Assert.assertEquals(n.getJobName(), JOB_NAME);
        Assert.assertEquals(n.getJobUrl(), JOB_URL);
        Assert.assertEquals(n.getBuildRefSpec(), BUILD_REFSPEC);
        Assert.assertEquals(n.getBuildId(), Long.valueOf(BUILD_ID));
        Assert.assertEquals(n.getBuildGroupId(), Long.valueOf(BUILD_GROUP_ID));
    }

    @Test
    public void toGerritTriggerNotificationTest() {
        final String MONITOR = "monitor";
        final String TRIGGER = "triggerHappy";

        NpJobView jobView = constructNpJobView();

        jobView.getBuild().getParameters().put(CIParam.MONITOR.toString(),
                MONITOR);
        jobView.getBuild().getParameters().put(CIParam.TRIGGER.toString(),
                TRIGGER);

        GerritTriggerNotification n = NotificationPluginUtil.toGerritTriggerNotification(jobView);

        Assert.assertEquals(n.getBuildPhase(),
                NotificationPluginUtil.convertBuildPhase(BUILD_PHASE));
        Assert.assertEquals(n.getMonitor(), MONITOR);
        Assert.assertEquals(n.getTrigger(), TRIGGER);
    }

    @Test
    public void toNotificationNull() {
        // run
        BuildNotification n = NotificationPluginUtil.toBuildNotification(null);

        // verify
        Assert.assertNull(n);
    }

    private NpJobView constructNpJobView() {
        // setup
        NpJobView jobView = new NpJobView();
        jobView.setName(JOB_NAME);
        jobView.setUrl(JOB_URL);
        jobView.setBuild(new NpBuildView());
        jobView.getBuild().setFull_url(BUILD_FULL_URL);
        jobView.getBuild().setNumber(BUILD_NUMBER);
        jobView.getBuild().setPhase(BUILD_PHASE);
        jobView.getBuild().setStatus(BUILD_STATUS);
        jobView.getBuild().setUrl("<unused data>");
        jobView.getBuild().setParameters(new HashMap<String, String>());
        jobView.getBuild().getParameters().put(CIParam.DISPLAY_NAME.toString(),
                JOB_DISPLAY_NAME);
        jobView.getBuild().getParameters().put(CIParam.GERRIT_REFSPEC.toString(),
                BUILD_REFSPEC);
        jobView.getBuild().getParameters().put(CIParam.BUILD_ID.toString(),
                BUILD_ID);
        jobView.getBuild().getParameters().put(CIParam.BUILD_GROUP_ID.toString(),
                BUILD_GROUP_ID);

        return jobView;
    }
}
