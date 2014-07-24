/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.jenkins;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class JenkinsServer extends Thread implements Container {

    private static Logger log = LoggerFactory.getLogger(JenkinsServer.class);
    private Stack<JenkinsRequest> serverQueue;
    private AtomicBoolean serverOn;
    private Connection connection;
    private SocketAddress address;
    private Long buildDuration;
    private String buildStatus;
    private JenkinsVersion version;
    private int port;
    private Status status;
    private Map<String, JenkinsSingleBuildConfiguration> buildConfigurations;
    private Map<String, JenkinsSingleBuildConfiguration> buildProductConfigurations;
    private Map<String, JenkinsBuild> builds;

    public JenkinsServer() {
        init(1337);
    }

    public JenkinsServer(int port) {
        init(port);
    }

    private void init(int port) {
        version = new JenkinsVersion("1.509");
        log.info("Starting Jenkins Mock server v." + version.toString() + " to port " + port);
        buildDuration = 30L;
        buildStatus = "SUCCESS";
        serverOn = new AtomicBoolean(true);
        serverQueue = new Stack<JenkinsRequest>();
        this.port = port;
        status = Status.OK;
        buildConfigurations = new HashMap<String, JenkinsSingleBuildConfiguration>();
        buildProductConfigurations = new HashMap<String, JenkinsSingleBuildConfiguration>();
        builds = new HashMap<String, JenkinsBuild>();
    }

    @Override
    public void handle(Request request, Response response) {
        serverQueue.push(new JenkinsRequest(this, request, response));
    }

    @Override
    public void run() {
        try {
            connection = new SocketConnection(this);
            address = new InetSocketAddress(port);
            connection.connect(address);

            while (serverOn.get()) {
                if (!serverQueue.isEmpty()) {
                    serverQueue.pop().run();
                }
                sleep(500);
            }
        } catch (IOException e) {
            log.error("JenkinsServer IOException: " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("JenkinsServer InterruptedException: " + e.getMessage());
        }
    }

    public void shutDown() {
        serverOn.set(false);
    }

    public Long getBuildDuration() {
        return buildDuration;
    }

    public void setBuildDuration(Long buildDuration) {
        this.buildDuration = buildDuration;
    }

    public String getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(String buildStatus) {
        this.buildStatus = buildStatus;
    }

    public void setSingleBuildConfig(String buildId, Long duration, String status) {
        JenkinsSingleBuildConfiguration conf = buildConfigurations.get(buildId);
        if (conf == null) {
            conf = new JenkinsSingleBuildConfiguration(duration, status);
            buildConfigurations.put(buildId, conf);
        } else {
            conf.setDuration(duration);
            conf.setStatus(status);
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<String, JenkinsSingleBuildConfiguration> getBuildConfigurations() {
        return buildConfigurations;
    }

    public void setBuildConfigurations(Map<String, JenkinsSingleBuildConfiguration> buildConfigurations) {
        this.buildConfigurations = buildConfigurations;
    }

    public Map<String, JenkinsSingleBuildConfiguration> getBuildProductConfigurations() {
        return buildProductConfigurations;
    }

    public void setBuildProductConfigurations(Map<String, JenkinsSingleBuildConfiguration> buildProductConfigurations) {
        this.buildProductConfigurations = buildProductConfigurations;
    }

    public Map<String, JenkinsBuild> getBuilds() {
        return builds;
    }

    public void setBuilds(Map<String, JenkinsBuild> builds) {
        this.builds = builds;
    }

    public int getPort() {
        return port;
    }

    public JenkinsVersion getVersion() {
        return version;
    }

    public void setVersion(JenkinsVersion version) {
        this.version = version;
    }
}
