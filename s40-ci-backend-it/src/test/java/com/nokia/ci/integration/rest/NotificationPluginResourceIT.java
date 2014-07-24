/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.integration.rest;

import com.google.gson.Gson;
import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.client.model.BuildView;
import com.nokia.ci.client.model.notificationplugin.NpBuildPhase;
import com.nokia.ci.client.model.notificationplugin.NpBuildStatus;
import com.nokia.ci.client.model.notificationplugin.NpBuildView;
import com.nokia.ci.client.model.notificationplugin.NpJobView;
import com.nokia.ci.client.rest.BuildResource;
import com.nokia.ci.client.rest.NotificationPluginResource;
import com.nokia.ci.integration.CITestBase;
import java.util.HashMap;
import junit.framework.Assert;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class NotificationPluginResourceIT extends CITestBase {

    private static final String JOB_NAME = "preconfigured_job";
    private static final String JOB_DISPLAY_NAME = "job display name";
    private static final String JOB_URL = "";
    private static final String BUILD_FULL_URL = "";
    private static final int BUILD_NUMBER = 1;
    private static final String BUILD_URL = "";
    private static NotificationPluginResource npProxy;
    private static BuildResource buildProxy;
    private static Gson gson;

    @BeforeClass
    public static void setUpClass() {
        npProxy = ProxyFactory.create(NotificationPluginResource.class, API_BASE_URL);
        buildProxy = ProxyFactory.create(BuildResource.class, API_BASE_URL);
        gson = new Gson();
    }

    private ClientResponse sendJobNotification(NpJobView jobView) {
        String json = gson.toJson(jobView);
        byte[] byteArray = json.getBytes();
        return (ClientResponse) npProxy.processNotification(byteArray);
    }

    private static NpJobView constructJobView() {
        NpJobView jobView = new NpJobView();
        NpBuildView buildView = new NpBuildView();

        jobView.setName(JOB_NAME);
        jobView.setUrl(JOB_URL);
        jobView.setBuild(buildView);

        buildView.setFull_url(BUILD_FULL_URL);
        buildView.setNumber(BUILD_NUMBER);
        buildView.setPhase(NpBuildPhase.STARTED);
        buildView.setStatus(NpBuildStatus.SUCCESS);
        buildView.setUrl(BUILD_URL);
        buildView.setParameters(new HashMap<String, String>());
        buildView.getParameters().put(CIParam.DISPLAY_NAME.toString(), 
                JOB_DISPLAY_NAME);

        return jobView;
    }
    
    @Ignore
    @Test
    public void basicJobNotificationTest() {
        NpJobView jobView = constructJobView();

        jobView.getBuild().setPhase(NpBuildPhase.STARTED);
        jobView.getBuild().setStatus(null);
        ClientResponse r = sendJobNotification(jobView);
        Assert.assertEquals(
                ClientResponse.Status.OK.getStatusCode(),
                r.getStatus());

        jobView.getBuild().setPhase(NpBuildPhase.COMPLETED);
        jobView.getBuild().setStatus(NpBuildStatus.SUCCESS);
        r = sendJobNotification(jobView);
        Assert.assertEquals(
                ClientResponse.Status.OK.getStatusCode(),
                r.getStatus());

        jobView.getBuild().setPhase(NpBuildPhase.FINISHED);
        jobView.getBuild().setStatus(NpBuildStatus.SUCCESS);
        r = sendJobNotification(jobView);
        Assert.assertEquals(
                ClientResponse.Status.OK.getStatusCode(),
                r.getStatus());
    }

    // Test case for bug: "Bug 5053 - Toolbox build number is always 0"
    // Disabled due to nonexisting restpoints.
    @Ignore
    @Test
    public void updateExistingToolboxJobBuildNumber() {
        final Long buildDatabaseId = -13L;
        final int initialBuildNumber = 11;
        final int finalBuildNumber = 512;

        // get target build and verify that build id is set as expected.
        BuildView build = getBuild(buildDatabaseId);
        Assert.assertEquals(build.getBuildNumber(), initialBuildNumber);

        // update target build via notification rest point
        NpJobView jobView = constructJobView();
        jobView.getBuild().setPhase(NpBuildPhase.FINISHED);
        jobView.getBuild().setStatus(NpBuildStatus.SUCCESS);
        jobView.getBuild().getParameters().put(CIParam.BUILD_ID.toString(),
                Long.toString(buildDatabaseId));
        jobView.getBuild().setNumber(finalBuildNumber);

        ClientResponse r = sendJobNotification(jobView);
        Assert.assertEquals(
                ClientResponse.Status.OK.getStatusCode(),
                r.getStatus());

        // get target build and verify that build id is now set.        
        build = getBuild(buildDatabaseId);
        Assert.assertEquals(finalBuildNumber, build.getBuildNumber());
    }

    private BuildView getBuild(Long id) {
        ClientResponse response = (ClientResponse) buildProxy.getBuild(id);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        BuildView build = (BuildView) response.getEntity(BuildView.class);
        return build;
    }
    
    @Ignore
    @Test
    public void faultyJsonJobNotificationTest() {
        class WeirdInput {

            public int integer = 5;
            public String string = "hello error!";
        }

        WeirdInput wi = new WeirdInput();
        String json = gson.toJson(wi);
        byte[] byteArray = json.getBytes();
        ClientResponse r = (ClientResponse) npProxy.processNotification(byteArray);

        Assert.assertEquals(
                ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                r.getStatus());
    }

    // Test case for bug: "Bug 5159 - CI UI shows all sub builds with "CONFIGURED" when first layer build failed"
    // Disabled this trigger after the bug fix had been verified.
    @Ignore
    @Test
    public void bugTestJobNotificationTest() {

        NpJobView jobView = new NpJobView();
        NpBuildView buildView = new NpBuildView();

        jobView.setName(JOB_NAME);
        jobView.setUrl(JOB_URL);
        jobView.setBuild(buildView);

        buildView.setFull_url(BUILD_FULL_URL);
        buildView.setNumber(BUILD_NUMBER);
        buildView.setPhase(NpBuildPhase.STARTED);
        buildView.setStatus(NpBuildStatus.SUCCESS);
        buildView.setUrl(BUILD_URL);
        buildView.setParameters(new HashMap<String, String>());
        buildView.getParameters().put(CIParam.DISPLAY_NAME.toString(), 
                JOB_DISPLAY_NAME);
        buildView.getParameters().put(CIParam.BUILD_GROUP_ID.toString(), 
                "-1");
        buildView.getParameters().put(CIParam.BUILD_ID.toString(), 
                "-14");

        jobView.getBuild().setPhase(NpBuildPhase.FINISHED);
        jobView.getBuild().setStatus(NpBuildStatus.FAILURE);
        ClientResponse r = sendJobNotification(jobView);
        Assert.assertEquals(
                ClientResponse.Status.OK.getStatusCode(),
                r.getStatus());
    }
}
