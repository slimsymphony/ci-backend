package com.nokia.ci.ui.testresults;

import com.nokia.ci.ejb.util.HttpUtil;
import com.nokia.ci.ui.jenkins.Artifact;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author miikka
 *
 */
public class ProxyArtifactResolver extends AbstractBuildArtifactResolver {

    private static final long serialVersionUID = 1L;
    //@Resource(lookup = "java:jboss/infinispan/cache/ci20/session-cache")
    private Cache<String, String> cache;
    private static final Logger log = LoggerFactory.getLogger(ProxyArtifactResolver.class);
    private List<Artifact> artifacts;
    private int socketTimeout;
    private int connectionTimeout;
    private List<Artifact> testResults;

    public ProxyArtifactResolver(String url, int socketTimeout, int connectionTimeout) {
        super(url);
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        try {
            InitialContext ic = new InitialContext();
            cache = (Cache) ic.lookup("java:jboss/infinispan/cache/ci20/session-cache");
        } catch (NamingException e) {
            log.error("yay we have an error", e);
        }
    }

    private void fetchBuildDetails() {
        if (StringUtils.isEmpty(this.getUrl())) {
            return;
        }

        // Fetch artifacts
        String buildDetails = cache.get(this.getUrl());
        if (buildDetails == null) {
            buildDetails = fetchArtifactListFromProxy(this.getUrl());
        }

        if (!StringUtils.isEmpty(buildDetails)) {
            artifacts = initArtifacts(this.getUrl(), buildDetails);
            cache.put(this.getUrl(), buildDetails);
        }

        // Fetch test results
        String testDetails = cache.get(this.getUrl() + "test_results/");
        if (testDetails == null) {
            testDetails = fetchArtifactListFromProxy(this.getUrl() + "test_results/");
        }

        if (!StringUtils.isEmpty(testDetails)) {
            testResults = initArtifacts(this.getUrl(), testDetails, "test_results/");
            cache.put(this.getUrl() + "test_results/", testDetails);
        }
    }

    private String fetchArtifactListFromProxy(String url) throws IllegalStateException {
        long startTime = System.currentTimeMillis();
        String artifacts = "";
        log.debug("Fetching build artifact list from URL: {}", url);

        if (!serverAvailable()) {
            log.warn("Could not get build artifact list from proxy {}", url);
            return artifacts;
        }

        try {
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
            HttpConnectionParams.setSoTimeout(params, socketTimeout);

            HttpClient client = HttpUtil.getHttpClient(params);
            HttpGet method = new HttpGet(url + ".files");
            HttpResponse response = client.execute(method);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                artifacts = convertStreamToString(response.getEntity().getContent());;
            }
        } catch (IOException ex) {
            log.warn("Fetching artifact list from URL: {} failed! {}",
                    new Object[]{url, ex.getMessage()});
        }

        log.debug("Fetching finished in {}ms", System.currentTimeMillis() - startTime);
        return artifacts;
    }
    
    private List<Artifact> initArtifacts(String artifactsUrl, String details) {
        return initArtifacts(artifactsUrl, details, "");
    }
    
    private List<Artifact> initArtifacts(String artifactsUrl, String details, String pathToArtifacts) {
        if (details == null) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        log.debug("Init artifacts");

        List<Artifact> arts = new ArrayList<Artifact>();
        try {
            for (String path : details.split("\n")) {
                if (!StringUtils.isEmpty(path)) {
                    String name = StringUtils.substringAfterLast(path, "/");
                    if (StringUtils.isEmpty(name)) {
                        name = path;
                    }
                    URL url = new URL(artifactsUrl + pathToArtifacts + path);
                    arts.add(new Artifact(name, url, pathToArtifacts + path));
                }
            }
        } catch (MalformedURLException ex) {
            log.warn("URL parsing failed", ex);
            return new ArrayList<Artifact>();
        }
        
        log.debug("Initialized artifacts in {}ms", System.currentTimeMillis() - startTime);
        return arts;
    }

    public List<Artifact> fetchArtifacts() {
        if (artifacts == null) {
            fetchBuildDetails();
        }
        return artifacts;
    }

    public List<Artifact> fetchTestResults() {
        if (testResults == null) {
            fetchBuildDetails();
        }
        return testResults;
    }
}
