/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 *
 * @author jajuutin
 */
public class FakeMetricsServer implements Container {

    int port;
    Connection connection;
    private String expectedDate;
    private String expectedProjectName;
    private String expectedBuildId;
    private String expectedBuildType;
    private String expectedBuildPhase;
    private String expectedBuildStatus;
    private String expectedChangeSetIds;
    private Status customResponse;
    
    public FakeMetricsServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        connection = new SocketConnection(this);
        SocketAddress a = new InetSocketAddress(port);
        connection.connect(a);
    }

    public void stop() throws IOException {
        connection.close();
        connection = null;
    }

    @Override
    public void handle(Request request, Response response) {
        if(customResponse != null) {
            populateResponse(response, customResponse);
            return;
        }
        
        Status status = Status.OK;

        // verify path component
        StringBuilder requiredPath = new StringBuilder();
        requiredPath.append("/s40-ci-metrics-restful-service/metrics/be_event");
        String actualPath = request.getPath().getPath();
        if (!actualPath.equals(requiredPath.toString())) {
            status = Status.BAD_REQUEST;
        }

        // verify post content
        String content = null;
        if(status == Status.OK) {            
            try {
                content = request.getContent();            
            } catch (IOException ex) {            
                status = Status.BAD_REQUEST;
            }
        }

        if(status == Status.OK) {
            if (!checkContent(content)) {
                status = Status.BAD_REQUEST;
            }
        }
        
        populateResponse(response, status);
    }

    private static void populateResponse(Response response,
            Status status) {
        long time = System.currentTimeMillis();
        response.set("Content-Type", "text/plain");
        response.set("Server", "Fake jenkins");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
        response.setCode(status.getCode());

        PrintStream body = null;
        try {
            body = response.getPrintStream();
            body.println("<response body>");
        } catch (IOException ex) {
        } finally {
            if (body != null) {
                body.close();
            }
        }
    }

    private boolean checkContent(String content) {
        Map<String, String> params = null;
        try {
            params = getPostParams(content);
        } catch (UnsupportedEncodingException ex) {
            return false;
        }

        String dateTime = params.get("dateTime");
        String projectName = params.get("projectName");
        String buildId = params.get("buildId");
        String buildType = params.get("buildType");
        String buildPhase = params.get("buildPhase");
        String buildStatus = params.get("buildStatus");
        String changeSetIdsArray = params.get("changeSetIdsArray");

        if (expectedDate.equals(dateTime)
                && expectedProjectName.equals(projectName)
                && expectedBuildId.equals(buildId)
                && expectedBuildType.equals(buildType)
                && expectedBuildPhase.equals(buildPhase)
                && expectedBuildStatus.equals(buildStatus)
                && expectedChangeSetIds.equals(changeSetIdsArray)) {            
            return true;
        }
        
        return false;
    }

    private Map<String, String> getPostParams(String content) throws UnsupportedEncodingException {
        String decoded = URLDecoder.decode(content, "UTF-8");

        Map<String, String> params = new HashMap<String, String>();

        for (String param : decoded.split("&")) {
            String keyValuePair[] = param.split("=");
            params.put(keyValuePair[0], keyValuePair[1]);
        }

        return params;
    }

    /**
     * @return the expectedDate
     */
    public String getExpectedDate() {
        return expectedDate;
    }

    /**
     * @param expectedDate the expectedDate to set
     */
    public void setExpectedDate(String expectedDate) {
        this.expectedDate = expectedDate;
    }

    /**
     * @return the expectedProjectName
     */
    public String getExpectedProjectName() {
        return expectedProjectName;
    }

    /**
     * @param expectedProjectName the expectedProjectName to set
     */
    public void setExpectedProjectName(String expectedProjectName) {
        this.expectedProjectName = expectedProjectName;
    }

    /**
     * @return the expectedBuildId
     */
    public String getExpectedBuildId() {
        return expectedBuildId;
    }

    /**
     * @param expectedBuildId the expectedBuildId to set
     */
    public void setExpectedBuildId(String expectedBuildId) {
        this.expectedBuildId = expectedBuildId;
    }

    /**
     * @return the expectedBuildType
     */
    public String getExpectedBuildType() {
        return expectedBuildType;
    }

    /**
     * @param expectedBuildType the expectedBuildType to set
     */
    public void setExpectedBuildType(String expectedBuildType) {
        this.expectedBuildType = expectedBuildType;
    }

    /**
     * @return the expectedBuildPhase
     */
    public String getExpectedBuildPhase() {
        return expectedBuildPhase;
    }

    /**
     * @param expectedBuildPhase the expectedBuildPhase to set
     */
    public void setExpectedBuildPhase(String expectedBuildPhase) {
        this.expectedBuildPhase = expectedBuildPhase;
    }

    /**
     * @return the expectedBuildStatus
     */
    public String getExpectedBuildStatus() {
        return expectedBuildStatus;
    }

    /**
     * @param expectedBuildStatus the expectedBuildStatus to set
     */
    public void setExpectedBuildStatus(String expectedBuildStatus) {
        this.expectedBuildStatus = expectedBuildStatus;
    }

    /**
     * @return the expectedChangeSetIds
     */
    public String getExpectedChangeSetIds() {
        return expectedChangeSetIds;
    }

    /**
     * @param expectedChangeSetIds the expectedChangeSetIds to set
     */
    public void setExpectedChangeSetIds(String expectedChangeSetIds) {
        this.expectedChangeSetIds = expectedChangeSetIds;
    }

    /**
     * @return the customResponse
     */
    public Status getCustomResponse() {
        return customResponse;
    }

    /**
     * @param customResponse the customResponse to set
     */
    public void setCustomResponse(Status customResponse) {
        this.customResponse = customResponse;
    }
}
