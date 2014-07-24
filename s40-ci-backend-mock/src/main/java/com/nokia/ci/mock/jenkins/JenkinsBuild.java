/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class JenkinsBuild extends Thread {

    private static Logger log = LoggerFactory.getLogger(JenkinsBuild.class);
    private Long duration;
    private String jobName;
    private String status;
    private String phase;
    private String buildId;
    private static String API_BASE_URL = "http://localhost:8888/s40ci/api/";
    private AbstractHttpClient httpClient;
    private Map<String, String> parameters;
    private List<JenkinsArtifact> artifacts;
    private Map<String, String> changes;
    private String serverUrl;

    JenkinsBuild() {
    }

    JenkinsBuild(Long duration, String job, Map<String, String> parameters, String status, List<JenkinsArtifact> artifacts,
            String serverUrl, Map<String, String> changes) {
        this.duration = (duration > 10) ? duration : 10;
        jobName = job;
        this.status = status;
        phase = "STARTED";
        this.parameters = parameters;
        this.artifacts = artifacts;
        this.changes = changes;
        this.serverUrl = serverUrl;
        if (this.parameters.containsKey("CI20_BUILD_ID")) {
            this.buildId = parameters.get("CI20_BUILD_ID");
        } else {
            this.buildId = Long.toString(System.currentTimeMillis());
        }

    }

    @Override
    public void run() {
        Long runTime = 0L;
        String req = "";
        log.info("Starting Mock Jenkins build for job {} running for {} seconds",
                jobName, duration);

        try {
            if (!jobName.equals("ci20_job_creator")) {
                req = generateJson();
                sendRequest(req, API_BASE_URL + "jobnotification/");
                phase = "FINISHED";
                req = generateJson();
            }

            while (true) {
                if (jobName.equals("ci20_job_creator") && runTime > 5) {
                    phase = "FINISHED";
                    req = generateJson();
                    sendRequest(req, API_BASE_URL + "jobnotification/creator/");
                    return;
                } else if (runTime == this.duration) {
                    log.debug("Build in job {} finished, sending request", jobName);
                    sendRequest(req, API_BASE_URL + "jobnotification/");
                    return;
                }
                runTime++;
                sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRequest(String req, String uri) throws Exception {
        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(
                ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        HttpResponse response = null;
        HttpPost post = new HttpPost(uri);
        StringEntity input = new StringEntity(req);
        post.setEntity(input);
        log.info("Build {} executing request {}", jobName, req);

        try {
            response = httpClient.execute(post);
        } catch (IOException ex) {
        }
    }

    private String generateJson() {
        Map j = new HashMap();
        j.put("name", jobName);
        j.put("url", this.serverUrl + "/job/" + jobName + "/");
        Map b = new HashMap();
        b.put("full_url", this.serverUrl + "/job/" + jobName + "/" + buildId + "/");
        b.put("phase", phase);
        b.put("status", status);
        b.put("url", this.serverUrl + "/job/" + jobName + "/" + buildId + "/");
        b.put("parameters", this.parameters);
        j.put("build", b);

        JSONObject obj = JSONObject.fromObject(j);
        return obj.toString();
    }

    public String getBuildId() {
        return buildId;
    }

    public String generateDetailsJson() {


        Map b = new HashMap();
        List actions = new ArrayList();
        actions.add(this.parameters);
        b.put("actions", actions.toArray());
        b.put("artifacts", this.artifacts.toArray());
        b.put("fullDisplayName", this.jobName);
        b.put("number", this.buildId);
        b.put("url", this.serverUrl + "/job/" + jobName + "/" + buildId);
        b.put("builtOn", "1_slave_master_8080");
        b.put("changeSet", this.changes);

        JSONObject obj = JSONObject.fromObject(b);
        return obj.toString();
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String generateTestDetails() {
        StringBuilder sb = new StringBuilder();
        for (JenkinsArtifact artifact : artifacts) {
            if (StringUtils.contains(artifact.getRelativePath(), "test_results")) {
                sb.append(StringUtils.remove(artifact.getRelativePath(), "test_results/"));
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    public String generateBuildDetails() {
        StringBuilder sb = new StringBuilder();
        for (JenkinsArtifact artifact : artifacts) {
            if (!StringUtils.contains(artifact.getRelativePath(), "test_results")) {
                sb.append(artifact.getRelativePath());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
}
