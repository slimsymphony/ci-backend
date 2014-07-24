/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the client wrapper for metrics rest points in BE.
 *
 * @author larryang
 */
public class MetricsClient {

    private static Logger log = LoggerFactory.getLogger(MetricsClient.class);
    private String metricsUrl;
    private HttpClient httpClient;
    private int connectionTimeout;
    private int socketTimeout;

    public MetricsClient(String metricsSvrUrl, int connectionTimeout, int socketTimeout) {
        this.metricsUrl = metricsSvrUrl + "/s40-ci-metrics-restful-service/metrics/be_event";
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public boolean log(Date date, String projectName, Long buildId,
            String buildType, String buildPhase, String buildStatus,
            List<String> changeSetIds) {

        // Verify and process input parameters 
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStampStr = dateFormat.format(date);

        if (StringUtils.isEmpty(projectName)) {
            log.error("Project name not specified for metrics logging.");
            return false;
        }

        if (buildId == null) {
            log.error("Build ID not specified for metrics logging.");
            return false;
        }

        if (StringUtils.isEmpty(buildType)) {
            log.error("Build type not specified for metrics logging.");
            return false;
        }

        if (StringUtils.isEmpty(buildPhase)) {
            log.error("Build type not specified for metrics logging.");
            return false;
        }

        if (StringUtils.isEmpty(this.metricsUrl)) {
            log.error("Metrics URL not specified for metrics logging.");
            return false;
        }

        String changeIdsStr = "";
        if (changeSetIds != null) {
            for (int i = 0; i < changeSetIds.size(); i++) {
                if (i == (changeSetIds.size() - 1)) {
                    changeIdsStr += changeSetIds.get(i);
                } else {
                    changeIdsStr += (changeSetIds.get(i) + ";");
                }
            }
        }

        // Post data to rest point

        if (this.httpClient == null) {
            this.httpClient = new DefaultHttpClient();
        }

        HttpPost post = new HttpPost(this.metricsUrl);
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeout);

        try {
            // setup post content.
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("dateTime", timeStampStr));
            nameValuePairs.add(new BasicNameValuePair("projectName", projectName));
            nameValuePairs.add(new BasicNameValuePair("buildId", Long.toString(buildId)));
            nameValuePairs.add(new BasicNameValuePair("buildType", buildType));
            nameValuePairs.add(new BasicNameValuePair("buildPhase", buildPhase));
            nameValuePairs.add(new BasicNameValuePair("buildStatus", buildStatus));
            nameValuePairs.add(new BasicNameValuePair("changeSetIdsArray", changeIdsStr));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            // Logging.
            StringBuilder postToString = new StringBuilder();
            postToString.append("Executing http post to metrics: ");
            postToString.append(post.getRequestLine().toString()).append(", ");
            postToString.append("un-encoded data: ").append(nameValuePairs.toString());
            log.info(postToString.toString());

            // Execute.
            HttpResponse response = httpClient.execute(post);

            // Check result.
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            } else {
                log.error("HTTP post failed with return code: {}.",
                        response.getStatusLine().getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Metrics rest API invoke exception: {}.", e);
            return false;
        }
    }
}
