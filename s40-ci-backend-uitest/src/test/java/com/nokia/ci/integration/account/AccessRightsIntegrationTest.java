package com.nokia.ci.integration.account;

import com.nokia.ci.integration.exception.UnauthorizedException;
import com.nokia.ci.integration.exception.UnexpectedException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Created by Ari Suoyrjo Date: 2013.07.23
 */
public class AccessRightsIntegrationTest extends AbstractAccountTest {

    @Test
    public void shouldAllowAccess() throws UnauthorizedException, UnexpectedException {
        populatePages();
        for (String relativeURL : getAllowedPages()) {
            loadPage(toFullUrl(relativeURL));

            boolean isPageLoadOK = driver.getCurrentUrl().contains(relativeURL);
            boolean logInExists = !findElementsById("login").isEmpty();

            if (!isPageLoadOK || logInExists) {
                log.info("User '" + getCurrentUser() + "' could not open page '" + toFullUrl(relativeURL) + "'");
                if (logInExists) {
                    log.info("Login page was displayed while it should not");
                }

                if (!isPageLoadOK) {
                    log.info("Page load was not directed to '" + toFullUrl(relativeURL) + "'");
                }

                isPageLoadOK = false;
            }

            assertTrue("Access rights are not working properly. User '" + getCurrentUser() + "' could not open '" + toFullUrl(relativeURL) + "'", isPageLoadOK);
        }
    }

    @Test
    public void shouldDenyAccess() throws UnauthorizedException, UnexpectedException {
        populatePages();
        List<String> deniedPages = new ArrayList<String>();
        for (String s : getAllPages()) {
            if (!getAllowedPages().contains(s)) {
                deniedPages.add(s);
            }
        }

        boolean isUnauthorizedPageLoaded = false;
        List<String> properAccess = new ArrayList<String>();

        for (String relativeURL : deniedPages) {
            try {
                loadPage(relativeURL, false);
            } catch (UnauthorizedException ex) {
                properAccess.add(relativeURL);
            }
        }

        List<String> problemPages = deniedPages;
        problemPages.removeAll(properAccess);
        if (!problemPages.isEmpty()) {
            isUnauthorizedPageLoaded = true;
        }

        assertTrue("Access rights are not working properly. User '" + getCurrentUser() + "' was able to access '" + problemPages + "' unauthorized", !isUnauthorizedPageLoaded);
    }
}
