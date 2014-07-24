package com.nokia.ci.integration.account;

import com.nokia.ci.integration.exception.UnauthorizedException;
import com.nokia.ci.integration.exception.UnexpectedException;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/16/13 Time: 11:11 AM To change
 * this template use File | Settings | File Templates.
 */
//login as admin
public class AdminLoginIntegrationTest extends AbstractAccountTest {

    @Test
    public void shouldLogIn() throws UnauthorizedException, UnexpectedException {
        logIn("admin", "admin");

        waitUntilAjaxRequestCompletes();

        boolean isAdminMenuLinkPresented = isElementPresentByXpath(".//*[@id = 'mainmenuForm:mainmenu:adminMenuLink']");
        assertTrue("Admin toolbar should be rendered", isAdminMenuLinkPresented);
    }
}
