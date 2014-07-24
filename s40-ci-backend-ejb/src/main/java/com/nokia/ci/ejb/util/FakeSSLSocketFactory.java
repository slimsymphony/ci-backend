/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 *
 * @author hhellgre
 */
public class FakeSSLSocketFactory extends SSLSocketFactory {

    public FakeSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(trustStrategy, hostnameVerifier);
    }
    
    private static final X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
        @Override
        public void verify(String host, SSLSocket ssl) throws IOException {
            // Do nothing
        }

        @Override
        public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
            //Do nothing
        }

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }

        @Override
        public void verify(String string, java.security.cert.X509Certificate xc) throws SSLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
            
    private static final TrustStrategy trustStrategy = new TrustStrategy() {
        @Override
        public boolean isTrusted(java.security.cert.X509Certificate[] xcs, String string) throws CertificateException {
            return true;
        }
    };
}