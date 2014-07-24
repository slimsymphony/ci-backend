package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.AuthException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.exception.UserSessionException;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ui.exception.QueryParamException;
import java.io.IOException;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract super class for all UI beans.
 *
 * @author jajuutin
 */
public abstract class AbstractUIBaseBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(AbstractUIBaseBean.class);
    @Inject
    private HttpSessionBean httpSessionBean;
    @EJB
    private SysConfigEJB sysConfigEJB;
    transient protected HttpServletRequest request;
    transient protected HttpServletResponse response;

    @PostConstruct
    private void initCallback() throws IOException, ServletException {
        request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();
        response = (HttpServletResponse) FacesContext
                .getCurrentInstance().getExternalContext().getResponse();

        try {
            init();
        } catch (Exception ex) {
            String url = "/error.xhtml";

            if (ex instanceof NotFoundException) {
                url = "/error/notFoundError/";
            } else if (ex instanceof UnauthorizedException) {
                if (httpSessionBean.isLoggedIn()) {
                    url = "/error/unauthorizedError/";
                } else {
                    FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
                    logoutUser();
                    url = "/login/";
                }
            } else if (ex instanceof QueryParamException) {
                url = "/error/queryParamError/";
            } else if (ex instanceof UserSessionException || ex instanceof AuthException) {
                FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
                logoutUser();
                url = "/login/";
            } else {
                //This log will help on fine-tune error classification above.
                log.warn("Not handled UI exception " + ex.getMessage() + ". Details: ", ex);
            }

            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        }
    }

    protected abstract void init() throws Exception;

    /**
     * Gets query parameter value from current request context. If there is no
     * parameter with given key in request context {@code null} is return.
     *
     * @param key Parameter key value
     * @return String value of parameter.
     */
    protected String getQueryParam(String key) {
        FacesContext fc = FacesContext.getCurrentInstance();
        return fc.getExternalContext().getRequestParameterMap().get(key);
    }

    /**
     * Gets the referring url for Back button.
     *
     * @return referer url.
     */
    protected String getBackUrl() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String refererUrl = fc.getExternalContext().getRequestHeaderMap().get("referer");
        String baseUrl = sysConfigEJB.getValue(SysConfigKey.BASE_URL, "");

        if (StringUtils.isEmpty(refererUrl) || !refererUrl.contains(baseUrl)) {
            refererUrl = baseUrl;
        }

        return refererUrl;
    }

    /**
     * Gets query parameter value from current request context. If there is no
     * parameter with given key in request context QueryParamException is
     * thrown.
     *
     * @param key Parameter key value
     * @return String value of parameter.
     */
    protected String getMandatoryQueryParam(String key) throws QueryParamException {
        String result = getQueryParam(key);
        if (result == null) {
            throw new QueryParamException("query parameter " + key + " not found.");
        }
        return result;
    }

    /**
     * Adds global message to current faces context.
     *
     * @param severity Severity level of message.
     * @param summary Summary text of message.
     * @param detail Detailed text of message.
     */
    protected void addMessage(Severity severity, String summary, String detail) {
        addMessage(null, severity, summary, detail);
    }

    /**
     * Adds message to current faces context with client identifier.
     *
     * @param clientId Client identifier which message is associated. If not
     * specified global message is created.
     * @param severity Severity level of message.
     * @param summary Summary text of message.
     * @param detail Detailed text of message.
     */
    protected void addMessage(String clientId, Severity severity, String summary, String detail) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(severity, summary, detail);
        fc.addMessage(clientId, facesMessage);
    }

    protected void addCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    protected void deleteCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    protected void logoutUser() throws ServletException {
        httpSessionBean.setSysUser(null);
        request.logout();
    }
}
