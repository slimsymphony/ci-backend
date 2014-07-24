package com.nokia.ci.ui.jenkins;

import com.nokia.ci.ejb.util.HttpUtil;
import com.nokia.ci.ui.testresults.AbstractBuildArtifactResolver;

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
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author miikka
 *
 */
public class BuildDetailResolver extends AbstractBuildArtifactResolver {

    //@Resource(lookup = "java:jboss/infinispan/cache/ci20/session-cache")
    private Cache<String, String> cache;
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(BuildDetailResolver.class);
    private String buildDetails = null;
    private List<JSONChange> changes;
    private List<JSONParam> parameters;
    private List<Artifact> artifacts;
    private List<Artifact> testResults;
    private int socketTimeout;
    private int connectionTimeout;

    public BuildDetailResolver(String url, int socketTimeout, int connectionTimeout) {
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

    private void fetchBuildDetailsJSON() {
        if (StringUtils.isEmpty(this.getUrl())) {
            return;
        }
        buildDetails = cache.get(this.getUrl());

        boolean fetchSuccess = true;

        if (buildDetails == null) {
            fetchSuccess = fetchDetailsFromJenkins();
        }

        if (fetchSuccess == false) {
            return;
        }

        changes = initJSONChanges();
        parameters = initJSONParameters();
        artifacts = initArtifacts(this.getUrl());
        testResults = initTestResults(this.getUrl());

        if (validateData()) {
            cache.put(this.getUrl(), buildDetails);
        }
    }

    private boolean validateData() {
        if (!getParameterAuthor().isEmpty()) {
            return true;
        }
        if (changes != null && !changes.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean fetchDetailsFromJenkins() throws IllegalStateException {
        boolean success = false;
        long startTime = System.currentTimeMillis();
        log.debug("Fetching build details from URL: {}", this.getUrl());

        if (!serverAvailable()) {
            log.warn("Could not get build details from jenkins {}", this.getUrl());
            return false;
        }

        try {
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
            HttpConnectionParams.setSoTimeout(params, socketTimeout);

            HttpClient client = HttpUtil.getHttpClient(params);
            HttpGet method = new HttpGet(this.getUrl() + "api/json");
            HttpResponse response = client.execute(method);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                buildDetails = convertStreamToString(response.getEntity().getContent());;
                success = true;
            }
        } catch (IOException ex) {
            log.warn("Fetching JSON from URL: {} failed! {}",
                    new Object[]{this.getUrl(), ex.getMessage()});
        }

        log.debug("Fetching finished in {}ms", System.currentTimeMillis() - startTime);
        return success;
    }

    private ArrayList<JSONChange> initJSONChanges() {
        if (buildDetails == null) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        log.debug("Init json changes");

        ArrayList<JSONChange> parsedChanges = new ArrayList<JSONChange>();
        try {
            JSONObject changeSet = new JSONObject(buildDetails).getJSONObject("changeSet");
            JSONArray itemArray = changeSet.getJSONArray("items");
            if (itemArray == null || itemArray.length() < 1) {
                return null;
            }
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject item = itemArray.getJSONObject(i);
                JSONChange change = new JSONChange(item);
                change.setAuthor(item.getJSONObject("author").getString("fullName"));
                change.setPaths(parsePaths(item));
                parsedChanges.add(change);
            }
        } catch (JSONException ex) {
            log.warn("Cannot fetch changes array from JSON", ex);
            return new ArrayList<JSONChange>();
        }

        log.debug("Initialized json changes in {}ms", System.currentTimeMillis() - startTime);
        return parsedChanges;
    }

    private List<Artifact> initArtifacts(String buildUrl) {
        if (buildDetails == null) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        log.debug("Init artifacts");

        List<Artifact> arts = new ArrayList<Artifact>();
        try {
            JSONArray artifactJSON = new JSONObject(buildDetails).getJSONArray("artifacts");
            if (artifactJSON == null || artifactJSON.length() < 1) {
                return null;
            }
            for (int i = 0; i < artifactJSON.length(); i++) {
                JSONObject artifact = artifactJSON.getJSONObject(i);
                String path = artifact.getString("relativePath");
                if (path != null && !path.startsWith("test_results/")) {
                    String name = artifact.getString("fileName");
                    URL url = new URL(buildUrl + "artifact/" + path);
                    arts.add(new Artifact(name, url, path));
                }
            }
        } catch (MalformedURLException ex) {
            log.warn("URL parsing failed", ex);
            return new ArrayList<Artifact>();
        } catch (JSONException ex) {
            log.warn("Cannot fetch changes array from JSON", ex);
            return new ArrayList<Artifact>();
        }

        log.debug("Initialized artifacts in {}ms", System.currentTimeMillis() - startTime);
        return arts;
    }

    private List<Artifact> initTestResults(String buildUrl) {
        if (buildDetails == null) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        log.debug("Init test results");

        List<Artifact> arts = new ArrayList<Artifact>();
        try {
            JSONArray artifactJSON = new JSONObject(buildDetails).getJSONArray("artifacts");
            if (artifactJSON == null || artifactJSON.length() < 1) {
                return null;
            }
            for (int i = 0; i < artifactJSON.length(); i++) {
                JSONObject artifact = artifactJSON.getJSONObject(i);
                String path = artifact.getString("relativePath");
                if (path != null && path.startsWith("test_results/")) {
                    String name = artifact.getString("fileName");
                    URL artifactUrl = new URL(buildUrl + "artifact/" + path);
                    arts.add(new Artifact(name, artifactUrl, path));
                }
            }
        } catch (MalformedURLException ex) {
            log.warn("URL parsing failed", ex);
            return new ArrayList<Artifact>();
        } catch (JSONException ex) {
            log.warn("Cannot fetch changes array from JSON", ex);
            return new ArrayList<Artifact>();
        }

        log.debug("Initialized artifacts in {}ms", System.currentTimeMillis() - startTime);
        return arts;
    }

    private ArrayList<JSONParam> initJSONParameters() {
        if (buildDetails == null) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        log.debug("Init json parameters");

        ArrayList<JSONParam> parsedParameters = new ArrayList<JSONParam>();
        try {
            JSONArray actionsArray = new JSONObject(buildDetails).getJSONArray("actions");
            if (actionsArray == null) {
                return null;
            }
            JSONArray paramsArray = findParametersArray(actionsArray);
            if (paramsArray == null || paramsArray.length() < 1) {
                return null;
            }

            for (int i = 0; i < paramsArray.length(); i++) {
                JSONObject p = paramsArray.getJSONObject(i);
                String name = p.getString("name");
                String value = p.getString("value");
                if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
                    continue;
                }
                JSONParam param = new JSONParam(name, value);
                parsedParameters.add(param);
            }
        } catch (JSONException ex) {
            log.warn("Cannot fetch parameters from JSON", ex);
            return new ArrayList<JSONParam>();
        }

        log.debug("Initialized json parameters in {}ms", System.currentTimeMillis() - startTime);
        return parsedParameters;
    }

    private List<JSONObject> parsePaths(JSONObject item) throws JSONException {
        if (item == null) {
            return null;
        }
        List<JSONObject> paths = new ArrayList<JSONObject>();
        JSONArray pathArray = item.getJSONArray("paths");
        if (pathArray == null) {
            return null;
        }

        for (int i = 0; i < pathArray.length(); i++) {
            paths.add(pathArray.getJSONObject(i));
        }
        return paths;
    }

    private JSONArray findParametersArray(JSONArray actionsArray) throws JSONException {
        if (actionsArray == null) {
            return null;
        }
        for (int i = 0; i < actionsArray.length(); i++) {
            JSONObject action = actionsArray.getJSONObject(i);
            if (action.has("parameters")) {
                return action.getJSONArray("parameters");
            }
        }
        return null;
    }

    public List<JSONChange> fetchChanges() {
        if (changes == null) {
            fetchBuildDetailsJSON();
        }
        return changes;
    }

    public List<Artifact> fetchArtifacts() {
        if (artifacts == null) {
            fetchBuildDetailsJSON();
        }
        return artifacts;
    }

    public List<Artifact> fetchTestResults() {
        if (testResults == null) {
            fetchBuildDetailsJSON();
        }
        return testResults;
    }

    public String getParameterAuthor() {
        if (parameters == null) {
            return "";
        }

        long startTime = System.currentTimeMillis();
        log.debug("Get parameter author");

        String author = "";
        for (JSONParam param : parameters) {
            if (param.getName().equals("GERRIT_CHANGE_OWNER_EMAIL")) {
                String email = param.getValue();
                if (!StringUtils.isEmpty(email) && email.contains("@")) {
                    author = email.substring(0, email.indexOf("@"));
                    break;
                }
            }
        }

        log.debug("Initialized json parameters in {}ms", System.currentTimeMillis() - startTime);
        return author;
    }
}
