package com.nokia.ci.integration.account;

import com.nokia.ci.integration.exception.UnauthorizedException;
import com.nokia.ci.integration.exception.UnexpectedException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/22/13 Time: 8:55 AM To change
 * this template use File | Settings | File Templates.
 */
public class UserLoginIntegrationTest extends AbstractAccountTest {

    @Test
    public void shouldLogIn() throws UnauthorizedException, UnexpectedException {
        logIn("user", "user");

        waitUntilAjaxRequestCompletes();

        boolean isAdminMenuLinkPresented = isElementPresentByXpath(".//*[@id = 'mainmenuForm:mainmenu:adminMenuLink']");
        assertTrue("Admin toolbar should not be rendered", !isAdminMenuLinkPresented);
    }
}
