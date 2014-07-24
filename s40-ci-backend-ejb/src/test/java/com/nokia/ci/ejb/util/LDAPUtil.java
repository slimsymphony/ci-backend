package com.nokia.ci.ejb.util;

import com.unboundid.ldap.sdk.LDAPException;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author HeikkiHellgren
 */
public class LDAPUtil extends AbstractLDAPUtil {

    private static Logger log = LoggerFactory.getLogger(LDAPUtil.class);

    @Override
    public LDAPUser authenticate(String username, String password) throws LDAPException, GeneralSecurityException {

        log.info("\n"
                + "************************************\n"
                + "*  USING MOCK LDAP IMPLEMENTATION  *\n"
                + "************************************");

        if (username.equals(password)) {
            String email = username + "@mock.com";
            String realname = username + " MOCK";
            String nokiasite = "FIHEL00";
            return new LDAPUser(username, email, realname, nokiasite);
        }

        return null;
    }

    @Override
    public List<LDAPUser> search(String query) throws LDAPException, GeneralSecurityException {
        List<LDAPUser> results = new ArrayList<LDAPUser>();
        String username;
        if (!query.isEmpty() && query.indexOf("@") != -1) {
            username = query.substring(0, query.indexOf("@"));
        } else {
            username = query;
        }

        String email = username + "@mock.com";
        String realname = username + " MOCK";
        String nokiaSite = "FIHEL00";
        LDAPUser user = new LDAPUser(username, email, realname, nokiaSite);
        results.add(user);
        return results;
    }

    @Override
    public LDAPUser getByUsername(String username) throws LDAPException, GeneralSecurityException {
        LDAPUser l = new LDAPUser();
        l.setUsername(username);
        l.setRealname(username + " MOCK");
        l.setEmail(username + "@mock.com");
        l.setNokiaSite("FIHEL00");
        return l;
    }

    public String getUserIdByEmail(String email) throws LDAPException {
        if (email.indexOf("@") > 0) {
            return email.substring(0, email.indexOf("@"));
        } else {
            return null;
        }
    }
}
