/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.resource;

import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.client.model.notificationplugin.NpBuildPhase;
import com.nokia.ci.client.model.notificationplugin.NpBuildStatus;
import com.nokia.ci.client.model.notificationplugin.NpBuildView;
import com.nokia.ci.client.model.notificationplugin.NpJobView;
import com.nokia.ci.ejb.BuildNotification;
import com.nokia.ci.ejb.NotificationPluginEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.jms.JenkinsNotificationProducer;
import com.nokia.ci.ejb.jms.NotificationMessageType;
import java.util.HashMap;
import javax.jms.JMSException;
import javax.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class NotificationPluginResourceTest extends WebTestBase {

    private static final String JOB_NOTIFICATION_BASE_URL = "/jobnotification/";

    @Test
    public void JobNotification() throws BackendAppException, JMSException {
        // Define constants
        final String jobName = "testJob";
        final int buildNumber = 100;
        final String jobUrl = "http://localhost/testJob/";
        final String fullUrl = jobUrl + buildNumber;
        final NpBuildPhase buildPhase = NpBuildPhase.FINISHED;
        final NpBuildStatus buildStatus = NpBuildStatus.SUCCESS;
        
        JenkinsNotificationProducer jenkinsNotificationProducer = registerResource();

        NpJobView jobView = new NpJobView();
        jobView.setName(jobName);
        jobView.setUrl("<unused data>");
        jobView.setBuild(new NpBuildView());
        jobView.getBuild().setFull_url(fullUrl);
        jobView.getBuild().setNumber(buildNumber);
        jobView.getBuild().setPhase(buildPhase);
        jobView.getBuild().setStatus(buildStatus);
        jobView.getBuild().setUrl("<unused data>");
        jobView.getBuild().setParameters(new HashMap<String, String>());
        jobView.getBuild().getParameters().put(CIParam.DISPLAY_NAME.toString(),
                "display name");

        // Run
        MockHttpRequest request = createJsonPostRequest(JOB_NOTIFICATION_BASE_URL,
                jobView);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);

        // Verify
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        Mockito.verify(jenkinsNotificationProducer, Mockito.atLeastOnce())
                .sendNotification(Mockito.eq(NotificationMessageType.BUILD), Mockito.any(BuildNotification.class));
    }

    @Test
    public void NonJsonJobNotification() {
        // Define constants
        final byte[] input = {'w', 'r', 'o', 'n', 'g'};

        //Setup
        registerResource();

        // Run
        MockHttpRequest request = createJsonPostRequest(JOB_NOTIFICATION_BASE_URL,
                input);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        // Verify
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST,
                response.getStatus());
    }

    @Test
    public void IrrelevantJsonNotification() {
        // Define constants
        final String input = "{\"nonexisting_member\":\"value\"}";

        //Setup
        registerResource();

        // Run
        MockHttpRequest request = createJsonPostRequest(JOB_NOTIFICATION_BASE_URL,
                input.getBytes());
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        // Verify
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST,
                response.getStatus());
    }

    private JenkinsNotificationProducer registerResource() {
        JenkinsNotificationProducer mockJenkinsNotificationProducer = 
                Mockito.mock(JenkinsNotificationProducer.class);

        NotificationPluginResourceImpl notificationPluginResourceImpl =
                new NotificationPluginResourceImpl();

        notificationPluginResourceImpl.jenkinsNotificationProducer =
                mockJenkinsNotificationProducer;

        dispatcher.getRegistry().addSingletonResource(
                notificationPluginResourceImpl);

        return mockJenkinsNotificationProducer;
    }    
}
