package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.AuthEJB;
import com.nokia.ci.ejb.exception.AuthException;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.LoginNotAllowedException;
import com.nokia.ci.ejb.model.SysUser;
import java.io.IOException;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for login/logout functionalities.
 *
 * @author jajuutin
 */
@Named
@RequestScoped
public class LoginBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(LoginBean.class);
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private String previousPage;
    @Inject
    AuthEJB authEJB;
    @Inject
    private HttpSessionBean httpSessionBean;

    @Override
    protected void init() {
        log.debug("Constructed");
    }

    @PreDestroy
    void preDestroy() {
        log.debug("Destroyed");
    }

    public String login() throws ServletException {
        log.info("Login user {}", username);

        logoutUser();

        boolean success = false;
        SysUser user = null;
        try {
            user = authEJB.authenticate(username, password);
            httpSessionBean.setSysUser(user);
            httpSessionBean.initProjectAccess();
            httpSessionBean.initProjectAdminAccess();
            request.login(username, username);
            success = true;
        } catch (AuthException ex) {
            log.info("Log in failed. Cause: {}", ex.getMessage());
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Login Failed", "Invalid credentials.");
        } catch (InvalidArgumentException ex) {
            log.info("Log in failed. Cause: {}", ex.getMessage());
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Login Failed", "Invalid input.");
        } catch (LoginNotAllowedException ex) {
            log.info("Log in failed. Cause: {}", ex.getMessage());
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Login Failed", "System is currently locked due to maintainance.");
        }

        username = null;
        password = null;

        String action = null;

        if (success) {
            log.debug("Login succeeded.");

            // Quick sanity check for leading "/" (up to browser to decided whether
            // it's in place or not). We urge it to be there.
            if (!previousPage.startsWith("/")) {
                previousPage = "/" + previousPage;
            }

            // We'll want to forward the user to URL he was about
            // to visit prior being interrupted by login screen.
            if (previousPage.startsWith("/secure/")) {
                action = previousPage;
                action += previousPage.contains("?") ? "&" : "?";
                log.debug("Forwarding user to originally requested resource: {}", action);
                action += "faces-redirect=true";
            } else if (previousPage.startsWith("/page/")
                    || previousPage.startsWith("/admin/")) {
                try {
                    log.debug("Forwarding user to originally requested resource: {}", previousPage);
                    response.sendRedirect(previousPage);
                    return null;
                } catch (IOException e) {
                    log.debug("Cannot forward to page {}", previousPage);
                    action = "secure/pages/myToolbox?faces-redirect=true";
                }
            } else {
                // If user landed on some non-secured page, e.g. root,
                // we'll forward him to projects listing.
                String page = "myToolbox";
                if (user != null && !StringUtils.isEmpty(user.getDefaultPage())) {
                    page = user.getDefaultPage();
                }
                action = "secure/pages/" + page + "?faces-redirect=true";
            }
        }

        // Prevent possibility to XSS
        previousPage = "";

        return action;
    }

    public String logout() throws ServletException {
        log.info("Logout user {}", request.getRemoteUser());
        logoutUser();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @return the previous page user was trying to access prior being served
     * with a login page
     */
    public String getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(String previousPage) {
        this.previousPage = previousPage;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
