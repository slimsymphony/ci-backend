package com.nokia.ci.integration.account;

import com.nokia.ci.integration.exception.UnauthorizedException;
import com.nokia.ci.integration.exception.UnexpectedException;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Created by Ari Suoyrjo Date: 2013.07.16
 */
// Login with a username 'project'
public class ProjectManagerLoginIntegrationTest extends AbstractAccountTest {

    @Test
    public void shouldLogIn() throws UnauthorizedException, UnexpectedException {
        logIn("project", "project");

        waitUntilAjaxRequestCompletes();

        boolean isAdminMenuLinkPresented = isElementPresentByXpath(".//*[@id = 'mainmenuForm:mainmenu:adminMenuLink']");
        assertTrue("Admin toolbar should be rendered", isAdminMenuLinkPresented);
    }
}
