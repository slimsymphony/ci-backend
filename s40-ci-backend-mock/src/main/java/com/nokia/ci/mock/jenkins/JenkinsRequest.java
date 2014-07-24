/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.jenkins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.parse.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class JenkinsRequest implements Runnable {

    private static Logger log = LoggerFactory.getLogger(JenkinsRequest.class);
    private final Response response;
    private final Request request;
    private final JenkinsServer server;
    private Status curStatus;
    private String responseContent;
    private List<String> buildStatusPossibilities;

    public JenkinsRequest(JenkinsServer server, Request request, Response response) {
        this.response = response;
        this.request = request;
        this.server = server;
        curStatus = server.getStatus();
        responseContent = "";
        /* SUCCESS, UNSTABLE, FAILURE, NOT_BUILT, ABORTED */
        String[] s = {"SUCCESS", "UNSTABLE", "FAILURE", "NOT_BUILT", "ABORTED"};
        buildStatusPossibilities = Arrays.asList(s);
    }

    private void populateResponse(Response response) {
        long time = System.currentTimeMillis();
        response.set("Content-Type", "text/plain");
        response.set("Server", "Jenkins Mock server");
        response.set("X-Jenkins", server.getVersion().toString());
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
        response.setCode(curStatus.getCode());

        PrintStream body = null;
        try {
            log.debug("Sending response with content: "
                    + responseContent);
            body = response.getPrintStream();
            body.println(responseContent);
        } catch (IOException ex) {
        } finally {
            if (body != null) {
                body.close();
            }
        }
    }

    private void handleBuildDetails(List<String> pathParams, Map<String, String> params) {
        log.info("Handling build details request with {} path params", pathParams);
        if ("job".equals(pathParams.get(1))) {
            // Use jenkins api
            String job = pathParams.get(2);
            String id = pathParams.get(3);
        
            if (pathParams.size() < 6 || !pathParams.get(4).equals("api") || !pathParams.get(5).equals("json")) {
                log.warn("Incorrect path parameters!");
                curStatus = Status.OK;
                return;
            }
        
            log.info("Getting Jenkins build with id {}...", id);
            JenkinsBuild build = server.getBuilds().get(id);
            if (build != null) {
                log.info("Generating JSON for build {}...", build.getName());
                responseContent = build.generateDetailsJson();
            } else {
                log.warn("No Jenkins build found with id: {}!", id);
                responseContent = "<response body>";
            }
        } else {
            // Use proxy
            String buildGroupId = pathParams.get(2);
            String buildId = pathParams.get(3);
            for (JenkinsBuild build : server.getBuilds().values()) {
                if (buildGroupId.equals(build.getParameters().get("CI20_BUILD_GROUP_ID")) &&
                        buildId.equals(build.getParameters().get("CI20_BUILD_ID"))) {
                    log.info("Generating details for build {}...", build.getName());
                    if (pathParams.size() > 4 && pathParams.get(4).contains("test_results")) {
                        responseContent = build.generateTestDetails();
                    } else {
                        responseContent = build.generateBuildDetails();
                    }
                    return;
                }
            }
            log.warn("No Jenkins build found with id: {}!", buildId);
            responseContent = "<response body>";
        }
    }

    private void handleNewJob(List<String> pathParams, Map<String, String> params) {
        log.info("Handling new job request with {} path params and {} number of parameters", pathParams.toString(), params.size());

        String job = pathParams.get(2);
        Boolean startBuild = false;
        String action = pathParams.get(3);
        Long bDuration = server.getBuildDuration();
        String bStatus = server.getBuildStatus();

        if (action.equals("buildWithParameters") || action.equals("build")) {
            startBuild = true;
        } else {
            curStatus = Status.OK;
        }

        if (params.containsKey("CI20_PRODUCT")) {
            String product = params.get("CI20_PRODUCT");
            JenkinsSingleBuildConfiguration conf = server.getBuildProductConfigurations().get(product);
            if (conf != null) {
                bDuration = conf.getDuration();
                bStatus = conf.getStatus();
            }
        }

        if (params.containsKey("CI20_BUILD_ID")) {
            String buildId = params.get("CI20_BUILD_ID");
            JenkinsSingleBuildConfiguration conf = server.getBuildConfigurations().get(buildId);
            if (conf != null) {
                bDuration = conf.getDuration();
                bStatus = conf.getStatus();
                server.getBuildConfigurations().remove(buildId);
            }
        }

        if (startBuild == true) {
            Random rnd = new Random();
            List<JenkinsArtifact> artifacts = new ArrayList<JenkinsArtifact>();
            if (job.contains(("ttcn"))) {
                JenkinsArtifact a1 = new JenkinsArtifact("isaTtcnTestResult1.xml", "test_results/isattcn1.xml");
                artifacts.add(a1);
            }

            if (job.contains("cppunit")) {
                String artifact = "test_results/cppunit1.xml";
                if (rnd.nextBoolean()) {
                    artifact = "test_results/cppunit2.xml";
                }
                JenkinsArtifact a1 = new JenkinsArtifact("cppUnitTestResult1.xml", artifact);
                artifacts.add(a1);
            }

            if (job.contains("eno") || job.contains("target")) {
                String artifact = "test_results/memusage.json";
                if (rnd.nextBoolean()) {
                    artifact = "test_results/memusage2.json";
                }

                JenkinsArtifact a1 = new JenkinsArtifact("memUsage.json", artifact);
                artifacts.add(a1);

                artifact = "test_results/arm_warnings.json";
                if (rnd.nextBoolean()) {
                    artifact = "test_results/arm_warnings2.json";
                }
                a1 = new JenkinsArtifact("warnings.json", artifact);
                artifacts.add(a1);
            }

            if (job.contains("granite")) {
                JenkinsArtifact a1 = new JenkinsArtifact("njunit.xml", "test_results/granite_njunit.xml");
                artifacts.add(a1);

                // Granite jobs always UNSTABLE so we can test classification
                bStatus = "UNSTABLE";
            }

            params.put("CI20_HAS_TEST_RESULTS", "TRUE");
            JenkinsArtifact a2 = new JenkinsArtifact("build_" + params.get("CI20_BUILD_ID") + ".jar", "target/build_" + params.get("CI20_BUILD_ID") + ".jar");
            artifacts.add(a2);
            Map changeSet = new HashMap();
            Object[] items = {};
            changeSet.put("items", items);
            JenkinsBuild build = new JenkinsBuild(bDuration, job,
                    params, bStatus, artifacts, "http://localhost:" + server.getPort(), changeSet);
            build.start();
            server.getBuilds().put(build.getBuildId(), build);
        }

        responseContent = "<response body>";
    }

    private void handleServerSetup(List<String> pathParams, Map<String, String> params) {
        log.info("Handling server setup request with {} path params", pathParams);
        String newStatus = server.getBuildStatus();
        Long newDuration = server.getBuildDuration();
        Boolean singleConf = false;
        responseContent += "JENKINS MOCK SERVER v." + server.getVersion();
        responseContent += "\n--------------------------------------------";

        if (params.containsKey("status")) {
            String changeStatus = params.get("status").toUpperCase();
            if (buildStatusPossibilities.contains(changeStatus)) {
                newStatus = changeStatus;
            } else {
                responseContent += "\nERROR! Status " + changeStatus
                        + " is not correct. Please use one of these: "
                        + buildStatusPossibilities.toString();
            }
        }

        if (params.containsKey("version")) {
            String newVersion = params.get("version");
            try {
                JenkinsVersion v = new JenkinsVersion(newVersion);
                server.setVersion(v);
            } catch (IllegalArgumentException e) {
                log.info("Not correct version number: " + newVersion);
            }
        }

        if (params.containsKey("duration")) {
            String duration = params.get("duration");
            try {
                Long d = Long.parseLong(duration);
                newDuration = d;
            } catch (NumberFormatException e) {
                responseContent += "\nERROR! Format of duration "
                        + duration + " could not be parsed as Long.";
            }
        }

        if (params.containsKey("server")) {
            String serverStatus = params.get("server");
            if (serverStatus.equals("UP")) {
                server.setStatus(Status.OK);
                responseContent += "\nServer is now up";
            } else if (serverStatus.equals("DOWN")) {
                server.setStatus(Status.NOT_FOUND);
                responseContent += "\nServer is now down";
            } else {
                responseContent += "\nERROR! Could not set server status with "
                        + serverStatus + " parameter. Defaulting to UP.";
                server.setStatus(Status.OK);
            }
        }

        if (params.containsKey("buildProduct")) {
            String product = params.get("buildProduct");
            JenkinsSingleBuildConfiguration conf = server.getBuildProductConfigurations().get(product);
            if (conf == null) {
                conf = new JenkinsSingleBuildConfiguration(newDuration, newStatus);
                server.getBuildProductConfigurations().put(product, conf);
            } else {
                conf.setDuration(newDuration);
                conf.setStatus(newStatus);
            }

            singleConf = true;
            responseContent += "\nStatus for product " + product + " builds will be " + newStatus;
            responseContent += "\nDuration for product " + product + " builds will be " + newDuration;
        }

        if (params.containsKey("buildId")) {
            String buildId = params.get("buildId");
            JenkinsSingleBuildConfiguration conf = server.getBuildConfigurations().get(buildId);
            if (conf == null) {
                conf = new JenkinsSingleBuildConfiguration(newDuration, newStatus);
                server.getBuildConfigurations().put(buildId, conf);
            } else {
                conf.setDuration(newDuration);
                conf.setStatus(newStatus);
            }

            singleConf = true;
            responseContent += "\nBuild " + buildId + " status will be " + newStatus;
            responseContent += "\nBuild " + buildId + " duration will be " + newDuration;
        }

        if (singleConf == false) {
            server.setBuildDuration(newDuration);
            server.setBuildStatus(newStatus);

            responseContent += "\nNew build status for all builds is " + newStatus;
            responseContent += "\nNew build duration for all builds is " + newDuration;
        }
    }

    private void handleTestResults(List<String> pathParams, Map<String, String> params) {
        if (pathParams.isEmpty()) {
            log.info("No path set, returning");
            return;
        }
        String file = pathParams.get(pathParams.size() - 1);

        log.info("Reading file from s40-ci-backend-mock/data/" + file);

        File responseFile = new File("s40-ci-backend-mock/data/" + file);

        if (!responseFile.exists()) {
            log.info("-- 404 - File not found.");
            responseContent = "404 - File not found";
            populateResponse(response);
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(responseFile.getAbsoluteFile()));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                responseContent = sb.toString();
            } finally {
                br.close();
            }
        } catch (FileNotFoundException e) {
            log.error("Could not find file " + responseFile.getAbsoluteFile());
        } catch (IOException ex) {
        } finally {
            populateResponse(response);
        }
    }

    private void handleRequest(List<String> pathParams, Map<String, String> params) {
        if ((pathParams.contains("job") || pathParams.contains(".files")) && curStatus == Status.OK) {
            if (pathParams.contains("buildWithParameters")) {
                handleNewJob(pathParams, params);
            } else if (pathParams.contains(".files") || (pathParams.contains("api"))) {
                handleBuildDetails(pathParams, params);
            } else if (pathParams.contains("test_results")) {
                handleTestResults(pathParams, params);
            }
        } else if (params.containsKey("setup")) {
            handleServerSetup(pathParams, params);
        } else {
            responseContent = "This is Jenkins mock server\n";
            responseContent += "To configure please use get parameters:\n";
            responseContent += "/?setup=1 - NEEDED ALWAYS TO CONFIGURE\n";
            responseContent += "\nOPTIONAL PARAMETERS:\n";
            responseContent += "&buildId=1006 - sets the duration and status for single build\n";
            responseContent += "&buildProduct=product_1 - sets the duration and status for all"
                    + "builds with this product\n";
            responseContent += "&duration=20 - sets the duration of future mock builds\n";
            responseContent += "&status=FAILURE - sets the build status of future mock builds\n";
            responseContent += "&server=DOWN/UP - sets the server to be up or down for requests\n";
            responseContent += "&version=1.3456 - sets the Jenkins server version";
        }

        populateResponse(response);
    }

    @Override
    public void run() {
        synchronized (server) {

            Map<String, String> params = new HashMap<String, String>();
            // Post params from Jenkins version 1.480.1 ->
            if (server.getVersion().newerThan(new JenkinsVersion("1.480.1"))) {
                try {
                    String content = request.getContent();
                    params = parsePostParameters(content);
                } catch (IOException ex) {
                    log.error("Could not parse parts of the request!");
                }
            } else {
                Query query = request.getQuery();
                params = new QueryParser(query.toString());
            }
            String path = request.getPath().getPath();
            if (path.equals("/favicon.ico")) {
                return;
            }

            List<String> pathParams = Arrays.asList(path.split("/"));
            handleRequest(pathParams, params);
        }
    }

    private static Map<String, String> parsePostParameters(String content) throws UnsupportedEncodingException {
        log.info("Handling HTTP POST parameters {}", content);
        Map<String, String> params = new HashMap<String, String>();
        for (String param : content.split("&")) {
            String pair[] = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = "";
            if (pair.length > 1) {
                value = URLDecoder.decode(pair[1], "UTF-8");
            }

            params.put(key, value);
        }
        return params;
    }
}
