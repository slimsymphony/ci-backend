package com.nokia.ci.integration.toolbox;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/18/13 Time: 3:04 PM To change
 * this template use File | Settings | File Templates.
 */
public class VerificationsIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void addVerificationRedirect() {
        navigate(UrlConstants.USER_PROJECT + 1);
        WebElement addNewVerificationLink = findElementByClass("contentmenuAdd");
        clickAndWaitForNewPage(addNewVerificationLink);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.VERIFICATION_ADD + 1);
        assertTrue("After Add new verification click redirection failed", isRedirectOK);
    }
}
