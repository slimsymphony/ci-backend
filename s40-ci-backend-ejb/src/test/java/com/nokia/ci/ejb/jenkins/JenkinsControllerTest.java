/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.jenkins;

import com.nokia.ci.ejb.cicontroller.CIControllerException;
import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.ejb.model.BuildGroupCIServer;
import com.nokia.ci.ejb.model.CIServer;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class JenkinsControllerTest {

    protected final static String JOB = "job";
    protected final static String SERVER_URL = "localhost";
    protected final static int SERVER_PORT = 8080;
    protected final static String CHILD_JOB_1 = "cj1";
    protected final static String CHILD_JOB_2 = "cj2";
    protected final static String CHILD_JOB_3 = "cj3";
    private Map<String, String> params;
    private JenkinsClient jenkinsClientMock;
    private JenkinsController testSubject;

    @Before
    public void before() {
        params = new HashMap<String, String>();
        params.put(CIParam.GERRIT_REFSPEC.toString(), "<REFSPEC>");

        BuildGroupCIServer buildGroupCIServer = new BuildGroupCIServer();
        buildGroupCIServer.setUrl(SERVER_URL);
        buildGroupCIServer.setPort(SERVER_PORT);
        testSubject = new JenkinsController(buildGroupCIServer);

        jenkinsClientMock = Mockito.mock(JenkinsClient.class);
        testSubject.setJenkinsClient(jenkinsClientMock);
    }

    @Test
    public void getSetServer() {
        BuildGroupCIServer ciServer = new BuildGroupCIServer();
        testSubject.setServer(ciServer);
        Assert.assertTrue(testSubject.getServer() == ciServer);
    }

    @Test
    public void build() throws CIControllerException, JenkinsClientException {
        // run
        testSubject.build(JOB);

        // verify
        Mockito.verify(jenkinsClientMock, Mockito.atLeastOnce()).build(
                JOB, null);
    }

    @Test
    public void buildWithParams() throws CIControllerException,
            JenkinsClientException {

        // run
        testSubject.build(JOB, params);

        // verify
        Mockito.verify(jenkinsClientMock, Mockito.atLeastOnce()).build(
                JOB, params);
    }

    @Test(expected = CIControllerException.class)
    public void buildJobWithNull() throws CIControllerException,
            JenkinsClientException {

        // run
        testSubject.build(null);
    }

    @Test(expected = CIControllerException.class)
    public void buildWithClientFailure() throws CIControllerException,
            JenkinsClientException {

        // setup exception to be thrown when client is accessed.        
        Mockito.doThrow(new JenkinsClientException("")).when(
                jenkinsClientMock).build(JOB, null);

        // run
        testSubject.build(JOB);
    }
}
