/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.jenkins;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class JenkinsClientTest {

    private final static String JOB = "job_name";
    private final static String SERVER_URL = "localhost";
    private final static int SERVER_PORT = 8111;
    private final static String REFSPEC = "REFSPEC";
    private final static String REFSPEC_VALUE = "REFSPEC_VALUE";
    private JenkinsClient testSubject;
    private FakeJenkins fakeJenkins;

    @Before
    public void before() throws IOException {
        fakeJenkins = new FakeJenkins(SERVER_PORT, JOB);
        fakeJenkins.start();
        testSubject = new JenkinsClient(SERVER_URL, SERVER_PORT);
    }

    @After
    public void after() {
        try {
            fakeJenkins.stop();
        } catch (IOException ex) {
        }

        fakeJenkins = null;
    }

    @Test
    public void build() throws JenkinsClientException {
        testSubject.build(JOB, null);
    }

    @Test
    public void buildMulti() throws JenkinsClientException,
            InterruptedException {
        final long sleepMs = 100;
        final int reps = 3;

        for (int i = 0; i < reps; i++) {
            testSubject.build(JOB, null);
            Thread.sleep(sleepMs);
        }
    }

    @Test
    public void buildWithParameters() throws JenkinsClientException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(REFSPEC, REFSPEC_VALUE);

        fakeJenkins.setExpectedParameters(parameters);

        testSubject.build(JOB, parameters);
    }

    @Test(expected = JenkinsClientException.class)
    public void buildWithNullJob() throws JenkinsClientException {
        testSubject.build(null, null);
    }

    @Test(expected = JenkinsClientException.class)
    public void buildWithInvalidServerPort() throws JenkinsClientException {
        testSubject = new JenkinsClient(SERVER_URL, 12345);

        testSubject.build(JOB, null);
    }
    
    @Test
    public void createURI() throws URISyntaxException {
        String serverUrl = SERVER_URL;
        String action = "action";
        // Without scheme information
        URI createdURI = JenkinsClient.createURI(serverUrl, SERVER_PORT, JOB, action, null);
        Assert.assertEquals("URI should match!", "http://" + serverUrl + ":" + SERVER_PORT + "/job/" + JOB + "/" + action, createdURI.toString());
        
        //With scheme information
        serverUrl = "https://" + SERVER_URL;
        createdURI = JenkinsClient.createURI(serverUrl, SERVER_PORT, JOB, action, null);
        Assert.assertEquals("URI should match!", serverUrl + ":" + SERVER_PORT + "/job/" + JOB + "/" + action, createdURI.toString());
    }
}
