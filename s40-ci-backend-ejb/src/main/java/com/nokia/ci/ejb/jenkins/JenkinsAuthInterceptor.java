/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.jenkins;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * Preemptive authentication interceptor
 * @author jajuutin
 */
public class JenkinsAuthInterceptor implements HttpRequestInterceptor {

    @Override
    public void process(HttpRequest request, HttpContext context)
            throws HttpException, IOException {


        AuthState authState = (AuthState) context.getAttribute(
                ClientContext.TARGET_AUTH_STATE);

        if (authState.getAuthScheme() != null) {
            return;
        }

        AuthScheme authScheme = (AuthScheme) context.getAttribute(
                "preemptive-auth");

        CredentialsProvider credsProvider =
                (CredentialsProvider) context.getAttribute(
                ClientContext.CREDS_PROVIDER);

        HttpHost targetHost =
                (HttpHost) context.getAttribute(
                ExecutionContext.HTTP_TARGET_HOST);

        if (authScheme != null) {
            Credentials creds = credsProvider.getCredentials(new AuthScope(
                    targetHost.getHostName(), targetHost.getPort()));

            if (creds == null) {
                throw new HttpException("No credentials");
            }

            authState.setAuthScheme(authScheme);
            authState.setCredentials(creds);
        }
    }
}
