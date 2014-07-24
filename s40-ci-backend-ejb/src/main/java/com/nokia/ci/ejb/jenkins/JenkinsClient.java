/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.jenkins;

import com.nokia.ci.ejb.util.HttpUtil;
import com.nokia.ci.ejb.util.Version;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
public class JenkinsClient {

    private static Logger log = LoggerFactory.getLogger(JenkinsClient.class);
    private final static String ACTION_BUILD = "build";
    private final static String ACTION_PARAMETRIZED_BUILD =
            "buildWithParameters";
    private AbstractHttpClient httpClient;
    private HttpContext httpContext;
    private String url;
    private int port;

    /**
     * Constructor
     *
     * @param url url of the Jenkins server
     * @param port port of the Jenkins server
     */
    public JenkinsClient(String url, int port) {
        this.url = url;
        this.port = port;

        httpClient = HttpUtil.getHttpClient();
        httpContext = new BasicHttpContext();
    }

    /**
     * Sets up HTTP authentication for Jenkins
     *
     * @param username
     * @param password
     */
    public void setupAuth(String username, String password) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)
                || httpClient == null) {
            return;
        }

        HttpUtil.createAuth(httpClient, username, password);

        BasicScheme basicAuth = new BasicScheme();
        httpContext.setAttribute("preemptive-auth", basicAuth);

        httpClient.addRequestInterceptor(new JenkinsAuthInterceptor(), 0);
    }

    /**
     * Sends build message to Jenkins with given jobName and parameters
     *
     * @param jobName
     * @param parameters
     * @throws JenkinsClientException
     */
    public void build(String jobName, Map<String, String> parameters)
            throws JenkinsClientException {

        if (jobName == null) {
            throw new JenkinsClientException("Job name set as null");
        }

        final String action = (parameters == null || parameters.isEmpty())
                ? ACTION_BUILD : ACTION_PARAMETRIZED_BUILD;

        Version jenkinsVersion = new Version(getJenkinsVersion(url));
        log.info("Resolved jenkins {} version as {}", url, jenkinsVersion);

        URI uri;
        HttpResponse response = null;

        // Jenkins versions from 1.480.1 use POST instead GET for starting jobs
        boolean usePost = jenkinsVersion.newerThan(new Version("1.480.1"));
        if (usePost) {
            try {
                uri = createURI(url, port, jobName, action, null);
            } catch (URISyntaxException ex) {
                throw new JenkinsClientException(ex);
            }

            HttpPost post = new HttpPost(uri);

            try {
                addParameters(post, parameters);
            } catch (UnsupportedEncodingException ex) {
                throw new JenkinsClientException(ex);
            }

            log.info("Executing following HTTP POST to Jenkins: {}, {}", uri.toASCIIString(), post);

            try {
                response = httpClient.execute(post, httpContext);
            } catch (IOException ex) {
                throw new JenkinsClientException(ex);
            }
        } else {
            try {
                uri = createURI(url, port, jobName, action, parameters);
            } catch (URISyntaxException ex) {
                throw new JenkinsClientException(ex);
            }

            log.info("Executing following HTTP GET to Jenkins: "
                    + uri.toASCIIString());

            try {
                response = httpClient.execute(new HttpGet(uri), httpContext);
            } catch (IOException ex) {
                throw new JenkinsClientException(ex);
            }
        }

        StatusLine sl = response.getStatusLine();

        // https://issues.jenkins-ci.org/browse/JENKINS-18407
        if (usePost && sl.getStatusCode() != HttpStatus.SC_OK && sl.getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY
                && sl.getStatusCode() != HttpStatus.SC_CREATED) {
            throw new JenkinsClientException("Non-OK response: "
                    + sl.toString());
        } else if (!usePost && sl.getStatusCode() != HttpStatus.SC_OK) {
            throw new JenkinsClientException("Non-OK response: "
                    + sl.toString());
        }

        HttpEntity entity = response.getEntity();
        try {
            EntityUtils.consume(entity);
        } catch (IOException ex) {
            throw new JenkinsClientException(ex);
        }
    }

    /**
     * Creates Jenkins specific URI. If given url is not containing scheme part
     * http scheme is used as default. Parameters with {@code null} key or value
     * is not included to created URI.
     *
     * @param url Url string
     * @param port Port number
     * @param jobName Jenkins job name
     * @param action Jenkins action
     * @param parameters Query parameter map.
     * @return Created URI
     * @throws URISyntaxException url is malformed URI string
     */
    public static URI createURI(String url, int port, String jobName,
            String action, Map<String, String> parameters)
            throws URISyntaxException {

        URI baseUri = new URI(url);
        if (baseUri.getScheme() == null) {
            baseUri = new URI("http://" + url);
        }
        URIBuilder ub = new URIBuilder(baseUri);
        ub.setPort(port);

        StringBuilder path = new StringBuilder();
        path.append("/job").append('/').append(jobName);
        path.append('/').append(action);

        ub.setPath(path.toString());
        addParameters(ub, parameters);

        return ub.build();
    }

    private static void addParameters(HttpPost post, Map<String, String> parameters) throws UnsupportedEncodingException {
        if (parameters == null) {
            return;
        }

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            if (key == null || value == null) {
                continue;
            }

            nvps.add(new BasicNameValuePair(key, value));
        }

        post.setEntity(new UrlEncodedFormEntity(nvps));
    }

    private static void addParameters(URIBuilder ub,
            Map<String, String> parameters) {

        if (parameters == null) {
            return;
        }

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            if (key == null || value == null) {
                continue;
            }

            ub.addParameter(key, value);
        }
    }

    private String getJenkinsVersion(String url) {
        HttpResponse response = null;
        try {
            URI baseUri = new URI(url);
            if (baseUri.getScheme() == null) {
                baseUri = new URI("http://" + url);
            }

            URIBuilder ub = new URIBuilder(baseUri);
            ub.setPort(port);

            URI uri = ub.build();

            response = httpClient.execute(new HttpGet(uri), httpContext);

            Header h = response.getFirstHeader("X-Jenkins");

            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);

            if (h != null) {
                String v = h.getValue();
                log.debug("Jenkins server {} version is {}", url, v);
                return v;
            }
        } catch (URISyntaxException ex) {
            log.warn("Could not retrieve jenkins version from {}", url);
        } catch (IOException ex) {
            log.warn("Could not retrieve jenkins version from {}", url);
        }

        return "1.000";
    }
}
