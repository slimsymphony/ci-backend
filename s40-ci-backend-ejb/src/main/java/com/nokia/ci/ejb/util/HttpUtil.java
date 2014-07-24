/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class HttpUtil {

    private static Logger log = LoggerFactory.getLogger(HttpUtil.class);

    public static AbstractHttpClient getHttpClient() {
        return getHttpClient(null);
    }
    
    public static AbstractHttpClient getHttpClient(HttpParams params) {
        ClientConnectionManager cm = null;

        log.info("Creating new http client");

        try {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            schemeRegistry.register(new Scheme("https", 443, new FakeSSLSocketFactory()));
            cm = new SingleClientConnManager(schemeRegistry);
        } catch (Exception e) {
            log.error("Failed to create socket factory");
            return null;
        }
        
        DefaultHttpClient client = null;

        if(params == null) {
            client = new DefaultHttpClient(cm);
        } else {
            client = new DefaultHttpClient(cm, params);
        }
        
        client.getParams().setParameter(
                ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        return client;
    }

    public static void createAuth(AbstractHttpClient client, String username, String password) {
        client.getCredentialsProvider().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));
    }
}
