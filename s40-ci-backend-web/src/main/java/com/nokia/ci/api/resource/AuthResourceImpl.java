/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.resource;

import com.nokia.ci.api.session.SessionManager;
import com.nokia.ci.api.session.SessionManager.Session;
import com.nokia.ci.client.model.AuthRequestView;
import com.nokia.ci.client.model.AuthResponseView;
import com.nokia.ci.client.rest.AuthResource;
import com.nokia.ci.ejb.AuthEJB;
import com.nokia.ci.ejb.exception.AuthException;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.LoginNotAllowedException;
import com.nokia.ci.ejb.model.SysUser;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Named
@RequestScoped
public class AuthResourceImpl implements AuthResource {

    @Inject
    AuthEJB authEJB;
    
    @Inject
    SessionManager sessionManager;
    
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(
            AuthResourceImpl.class);

    @Override
    public Response login(AuthRequestView authRequestView) {
        if (authRequestView == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        log.info("Login initiated for user: {}", authRequestView.getLoginName());
        SysUser sysUser = null;
        try {
            sysUser = authEJB.authenticate(authRequestView.getLoginName(),
                    authRequestView.getPassword());
        } catch (InvalidArgumentException ex) {
            log.warn("Error with arguments", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (AuthException ex) {
            log.warn("Auth failed", ex);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (LoginNotAllowedException ex) {
            log.warn("Login not allowed", ex);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Session s = sessionManager.newSession(sysUser.getId());
        
        AuthResponseView authResponse = new AuthResponseView();
        authResponse.setToken(s.getToken());
        return Response.ok(authResponse).build();
    }

    @Override
    public Response logout(String token) {
        Session s = sessionManager.getSession(token);
        if (s != null) {
            sessionManager.deleteSession(s);
        }        
        
        return Response.ok().build();
    }
}
