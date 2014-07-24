/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.jenkins;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
public class FakeJenkins implements Container {

    private Map<String, String> expectedParameters;
    int port;
    String job;
    Connection connection;

    public FakeJenkins(int port, String job) {
        this.port = port;
        this.expectedParameters = new HashMap<String, String>();
        this.job = job;
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
        Status status = Status.OK;

        // verify path component
        String action = expectedParameters.isEmpty()
                ? "build" : "buildWithParameters";

        StringBuilder requiredPath = new StringBuilder();
        requiredPath.append("/job/").append(job).append('/').append(action);

        if (request.getPath().getPath().equals(requiredPath.toString())
                == false) {
            status = Status.BAD_REQUEST;
        }

        // verify parameters
        for (Map.Entry<String, String> entry : expectedParameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            String receivedValue = null;
            try {
                receivedValue = request.getParameter(key);
            } catch (IOException ex) {
            }

            if (receivedValue == null
                    || receivedValue.equals(value) == false) {
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

    public Map<String, String> getExpectedParameters() {
        return expectedParameters;
    }

    public void setExpectedParameters(Map<String, String> expectedParameters) {
        this.expectedParameters = expectedParameters;
    }
}
