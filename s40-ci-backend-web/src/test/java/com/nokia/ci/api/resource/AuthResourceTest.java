/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.resource;

import com.nokia.ci.api.session.SessionManager;
import com.nokia.ci.api.session.SessionManager.Session;
import com.nokia.ci.client.model.AuthRequestView;
import com.nokia.ci.client.model.AuthResponseView;
import com.nokia.ci.ejb.AuthEJB;
import com.nokia.ci.ejb.exception.AuthException;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.LoginNotAllowedException;
import com.nokia.ci.ejb.model.SysUser;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jgroups.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class AuthResourceTest extends WebTestBase {

    private static final String AUTH_LOGIN_URL = "/auth/login";
    private static final String AUTH_LOGOUT_URL = "/auth/logout";
    AuthResourceImpl authResourceImpl;
    static final String LOGIN_NAME = "loginname";
    static final String PASSWORD = "password";
    static final Long SYSUSER_ID = 1L;
    static final String TOKEN = UUID.randomUUID().toString();

    @Before
    @Override
    public void before() {
        super.before();
        authResourceImpl = new AuthResourceImpl();
        authResourceImpl.authEJB = Mockito.mock(AuthEJB.class);
        authResourceImpl.sessionManager = Mockito.mock(SessionManager.class);
        dispatcher.getRegistry().addSingletonResource(authResourceImpl);
    }

    @Test
    public void authNullTest() throws InvalidArgumentException, AuthException, LoginNotAllowedException {
        invokeLogin(null, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void authFailedTest() throws InvalidArgumentException, AuthException, LoginNotAllowedException {
        // Setup
        SysUser sysUser = new SysUser();
        sysUser.setLoginName(LOGIN_NAME);
        sysUser.setId(SYSUSER_ID);

        Mockito.when(authResourceImpl.authEJB.authenticate(LOGIN_NAME, PASSWORD)).
                thenThrow(new AuthException());

        // Run & verify return code.
        AuthRequestView requestView = new AuthRequestView();
        requestView.setLoginName(LOGIN_NAME);
        requestView.setPassword(PASSWORD);
        invokeLogin(requestView, HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void authInvalidArgumentTest() throws InvalidArgumentException, AuthException, LoginNotAllowedException {
        // Setup
        SysUser sysUser = new SysUser();
        sysUser.setLoginName(LOGIN_NAME);
        sysUser.setId(SYSUSER_ID);

        Mockito.when(authResourceImpl.authEJB.authenticate(LOGIN_NAME, PASSWORD)).
                thenThrow(new InvalidArgumentException());

        // Run & verify return code.
        AuthRequestView requestView = new AuthRequestView();
        requestView.setLoginName(LOGIN_NAME);
        requestView.setPassword(PASSWORD);
        invokeLogin(requestView, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void authLoginNotAllowedTest() throws InvalidArgumentException, AuthException, LoginNotAllowedException {
        // Setup
        SysUser sysUser = new SysUser();
        sysUser.setLoginName(LOGIN_NAME);
        sysUser.setId(SYSUSER_ID);

        Mockito.when(authResourceImpl.authEJB.authenticate(LOGIN_NAME, PASSWORD)).
                thenThrow(new LoginNotAllowedException());

        // Run & verify return code.
        AuthRequestView requestView = new AuthRequestView();
        requestView.setLoginName(LOGIN_NAME);
        requestView.setPassword(PASSWORD);
        invokeLogin(requestView, HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void authOkTest() throws InvalidArgumentException, AuthException, LoginNotAllowedException {
        // Setup
        SysUser sysUser = new SysUser();
        sysUser.setLoginName(LOGIN_NAME);
        sysUser.setId(SYSUSER_ID);

        Mockito.when(authResourceImpl.authEJB.authenticate(LOGIN_NAME, PASSWORD)).
                thenReturn(sysUser);

        Session session = new Session();
        session.setToken(TOKEN);
        session.setSysUserId(SYSUSER_ID);
        Mockito.when(authResourceImpl.sessionManager.newSession(SYSUSER_ID)).
                thenReturn(session);

        // Run
        AuthRequestView requestView = new AuthRequestView();
        requestView.setLoginName(LOGIN_NAME);
        requestView.setPassword(PASSWORD);
        AuthResponseView responseView = invokeLogin(requestView,
                HttpServletResponse.SC_OK);

        // Verify
        Assert.assertEquals(responseView.getToken(), session.getToken());
    }

    @Test
    public void logoutTest() throws InvalidArgumentException, AuthException,
            URISyntaxException {

        // Setup
        Session session = new Session();
        session.setToken(TOKEN);
        session.setSysUserId(SYSUSER_ID);
        Mockito.when(authResourceImpl.sessionManager.getSession(TOKEN)).
                thenReturn(session);

        // Run
        MockHttpRequest request = MockHttpRequest.get(AUTH_LOGOUT_URL + '/'
                + TOKEN);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    private AuthResponseView invokeLogin(AuthRequestView requestView,
            int expectedStatusCode) {

        MockHttpRequest request = createJsonPostRequest(AUTH_LOGIN_URL,
                requestView);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);

        Assert.assertEquals(expectedStatusCode, response.getStatus());

        AuthResponseView responseView = gson.fromJson(
                response.getContentAsString(), AuthResponseView.class);

        return responseView;
    }
}
