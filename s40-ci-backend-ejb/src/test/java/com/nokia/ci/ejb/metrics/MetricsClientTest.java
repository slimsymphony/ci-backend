/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.http.Status;

/**
 *
 * @author jajuutin
 */
public class MetricsClientTest {

    private final static String SERVER_URL = "http://localhost";
    private final static int SERVER_PORT = 8111;
    private MetricsClient testSubject;
    private FakeMetricsServer fakeMetricsServer;
    final Date date = new Date();
    final String PROJECT_NAME = "project";
    final Long BUILD_ID = 1L;
    final String BUILD_TYPE = "SINGLE_COMMIT";
    final String BUILD_PHASE = "STARTED";
    final String BUILD_STATUS = "SUCCESSFUL";
    final String CHANGESET_1 = "change1";
    final String CHANGESET_2 = "change2";    
    List<String> changeSetIds = new ArrayList<String>();
    
    @Before
    public void before() throws IOException {
        testSubject = new MetricsClient(SERVER_URL + ":" + Integer.toString(SERVER_PORT), 60000, 60000);

        // init fake server.
        
        fakeMetricsServer = new FakeMetricsServer(SERVER_PORT);
        fakeMetricsServer.start();        
        
        changeSetIds.add(CHANGESET_1);
        changeSetIds.add(CHANGESET_2);

        fakeMetricsServer.setExpectedDate(constructDateString(date));
        fakeMetricsServer.setExpectedProjectName(PROJECT_NAME);
        fakeMetricsServer.setExpectedBuildId(BUILD_ID.toString());
        fakeMetricsServer.setExpectedBuildType(BUILD_TYPE);
        fakeMetricsServer.setExpectedBuildPhase(BUILD_PHASE);
        fakeMetricsServer.setExpectedBuildStatus(BUILD_STATUS);
        fakeMetricsServer.setExpectedChangeSetIds(constructChangeSetString(changeSetIds));        
    }

    @After
    public void after() {
        try {
            fakeMetricsServer.stop();
        } catch (IOException ex) {
        }
        fakeMetricsServer = null;
    }

    @Test
    public void log() {
        boolean result = testSubject.log(date, PROJECT_NAME, BUILD_ID, BUILD_TYPE, BUILD_PHASE, BUILD_STATUS, changeSetIds);        
        Assert.assertTrue(result);
    }

    @Test
    public void logBadRequest() {
        fakeMetricsServer.setCustomResponse(Status.BAD_REQUEST);
        boolean result = testSubject.log(date, PROJECT_NAME, BUILD_ID, BUILD_TYPE, BUILD_PHASE, BUILD_STATUS, changeSetIds);        
        Assert.assertFalse(result);
    }    
    
    private String constructChangeSetString(List<String> source) {
        StringBuilder sb = new StringBuilder();
        for(String string : source) {
            if(sb.length() > 0 ){
                sb.append(';');
            }
            sb.append(string);
        }
        return sb.toString();
    }
    
    private String constructDateString(Date source) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(source);        
    }
}