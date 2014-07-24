/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

/**
 *
 * @author Miikka Liukkonen
 */
import com.unboundid.ldap.sdk.DereferencePolicy;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class LDAPUtil extends AbstractLDAPUtil {

    private final String server = "nedi.europe.nokia.com";
    private final int port = 636;

    @Override
    public LDAPUser authenticate(String username, String password) throws LDAPException, GeneralSecurityException {

        LDAPConnection ldap = null;
        try {
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            ldap = new LDAPConnection(sslUtil.createSSLSocketFactory());
            ldap.connect(server, port);
            String[] searchParams = {"mail", "cn", "nokiasite"};
            SearchResult sr = ldap.search("o=Nokia", SearchScope.SUB, "(uid=" + username + ")", searchParams);
            if (sr.getEntryCount() == 0) {
                return null;
            }
            SearchResultEntry entry = sr.getSearchEntries().get(0);
            String dn = entry.getDN();
            String email = entry.getAttributeValue("mail");
            String realname = entry.getAttributeValue("cn");
            String nokiaSite = entry.getAttributeValue("nokiasite");

            ldap.bind(dn, password);

            return new LDAPUser(username, email, realname, nokiaSite);
        } finally {
            if (ldap != null) {
                ldap.close();
            }
        }
    }

    @Override
    public List<LDAPUser> search(String query) throws LDAPException, GeneralSecurityException {
        List<LDAPUser> results = new ArrayList<LDAPUser>();

        LDAPConnection ldap = null;
        try {
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            ldap = new LDAPConnection(sslUtil.createSSLSocketFactory());
            ldap.connect(server, port);
            String[] searchParams = {"mail", "cn", "nokiasite", "uid"};
            String searchQuery = "(|(uid=" + query + "*)(cn=" + query + "*)(mail=" + query + "*))";
            SearchResult sr = null;
            try {
                sr = ldap.search("o=Nokia", SearchScope.SUB, DereferencePolicy.SEARCHING, 10, 60, false,
                        searchQuery, searchParams);
            } catch (LDAPSearchException e) {
                // Only if the size limit has been exceeded
                sr = e.getSearchResult();
            }

            if (sr != null) {
                for (SearchResultEntry e : sr.getSearchEntries()) {
                    String dn = e.getAttributeValue("uid");
                    String email = e.getAttributeValue("mail");
                    String realname = e.getAttributeValue("cn");
                    String nokiaSite = e.getAttributeValue("nokiasite");
                    LDAPUser user = new LDAPUser(dn, email, realname, nokiaSite);
                    results.add(user);
                }
            }
        } finally {
            if (ldap != null) {
                ldap.close();
            }
        }
        return results;
    }

    @Override
    public LDAPUser getByUsername(String username) throws LDAPException, GeneralSecurityException {
        LDAPConnection ldap = null;
        try {
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            ldap = new LDAPConnection(sslUtil.createSSLSocketFactory());
            ldap.connect(server, port);
            String[] searchParams = {"mail", "cn", "nokiasite", "uid"};
            String searchQuery = "(uid=" + username + ")";
            SearchResult sr = ldap.search("o=Nokia", SearchScope.SUB, DereferencePolicy.SEARCHING, 1, 60, false,
                    searchQuery, searchParams);
            if (sr.getEntryCount() == 0) {
                return null;
            }
            SearchResultEntry entry = sr.getSearchEntries().get(0);
            String email = entry.getAttributeValue("mail");
            String realname = entry.getAttributeValue("cn");
            String nokiaSite = entry.getAttributeValue("nokiasite");

            return new LDAPUser(username, email, realname, nokiaSite);
        } finally {
            if (ldap != null) {
                ldap.close();
            }
        }
    }

    public String getUserIdByEmail(String email) throws LDAPException {

        LDAPConnection ldap = null;
        try {
            ldap = new LDAPConnection(server, port);
            String[] searchParams = {"uid"};
            String searchQuery = "(mail=" + email + ")";
            SearchResult sr = ldap.search("o=Nokia", SearchScope.SUB, DereferencePolicy.SEARCHING, 1, 60, false,
                    searchQuery, searchParams);
            if (sr.getEntryCount() == 0) {
                return null;
            }
            SearchResultEntry entry = sr.getSearchEntries().get(0);
            return entry.getAttributeValue("uid");

        } finally {
            if (ldap != null) {
                ldap.close();
            }
        }
    }
}