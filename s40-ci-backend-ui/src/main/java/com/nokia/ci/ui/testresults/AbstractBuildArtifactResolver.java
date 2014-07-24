package com.nokia.ci.ui.testresults;

import com.nokia.ci.ejb.util.HttpUtil;
import com.nokia.ci.ui.jenkins.Artifact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author miikka
 *
 */
public abstract class AbstractBuildArtifactResolver implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AbstractBuildArtifactResolver.class);
    private final static int testSocketTimeout = 5000;
    private final static int testConnectionTimeout = 5000;
    private String url;

    public AbstractBuildArtifactResolver(String url) {
        this.url = StringUtils.endsWith(url, "/") ? url : url + "/";
    }

    public abstract List<Artifact> fetchArtifacts();

    public abstract List<Artifact> fetchTestResults();
    
    protected boolean serverAvailable() {
        try {
            HttpParams testParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(testParams, testConnectionTimeout);
            HttpConnectionParams.setSoTimeout(testParams, testSocketTimeout);

            HttpClient testClient = HttpUtil.getHttpClient(testParams);
            HttpGet testMethod = new HttpGet(url);
            HttpResponse testResponse = testClient.execute(testMethod);
            if (testResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            }
        } catch (IOException ex) {
            log.warn("Fetching not possible to address {}: {}", url, ex.getMessage());
        }
        return false;
    }

    protected String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            log.warn("Read failure", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                log.warn("Inputstream close failure", e);
            }
        }
        return sb.toString();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
}
