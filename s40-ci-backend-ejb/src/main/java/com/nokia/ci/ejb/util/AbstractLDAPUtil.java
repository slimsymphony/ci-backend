package com.nokia.ci.ejb.util;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unboundid.ldap.sdk.LDAPException;

public abstract class AbstractLDAPUtil {

    public static boolean isNextUser(String userId) {
        Pattern pattern = Pattern.compile("^e[0-9]{3,}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(userId);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public abstract LDAPUser getByUsername(String username) throws LDAPException, GeneralSecurityException;

    public abstract List<LDAPUser> search(String query) throws LDAPException, GeneralSecurityException;

    public abstract LDAPUser authenticate(String username, String password) throws LDAPException, GeneralSecurityException;

    public AbstractLDAPUtil() {
        super();
    }

}