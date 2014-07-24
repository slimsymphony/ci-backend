package com.nokia.ci.integration.uimodel;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 3/14/13
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class MockJenkinsHelper {

    public static boolean setupJenkinsMockStatus(String status){
        AbstractHttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpResponse response = null;
        try {
           URI baseUri = new URI("http://localhost");
           URIBuilder ub = new URIBuilder(baseUri);
           ub.setPort(1337);
           ub.addParameter("setup","1");
           ub.addParameter("status",status);
           ub.addParameter("duration","10");
           URI uri = ub.build();
           response = httpClient.execute(new HttpGet(uri), httpContext);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        StatusLine sl = response.getStatusLine();
        return sl.getStatusCode() == HttpStatus.SC_OK;
    }

}
