package com.nokia.ci.ejb.util;

/**
 * Data holder for LDAP user information.
 *
 * @author vrouvine
 */
public class LDAPUser {

    private String username;
    private String realname;
    private String email;
    private String nokiaSite;


    public LDAPUser() {
    }

    public LDAPUser(String username, String email, String realname, String nokiaSite) {
        this.username = username;
        this.email = email;
        this.realname = realname;
        this.nokiaSite = nokiaSite;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }
    
    public String getNokiaSite() {
        return nokiaSite;
    }

    public void setNokiaSite(String nokiaSite) {
        this.nokiaSite = nokiaSite;
    }
}
