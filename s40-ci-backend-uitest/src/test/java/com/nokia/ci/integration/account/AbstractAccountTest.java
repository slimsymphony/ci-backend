package com.nokia.ci.integration.account;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.exception.UnauthorizedException;
import com.nokia.ci.integration.exception.UnexpectedException;
import com.nokia.ci.integration.sqlImport.DBConnector;
import com.nokia.ci.integration.uimodel.UrlConstants;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;

/**
 * Created by Ari Suoyrjo Date: 2013.07.16
 */
public class AbstractAccountTest extends AbstractIntegrationTest {

    private static List<String> allPages = new ArrayList<String>();
    private static List<String> allowedPages = new ArrayList<String>();
    private static final String ACCESS_PROJECT_AS_ADMIN_QUERY = "select p.ID from S40CICORE.PROJECT p inner join S40CICORE.PROJECT_ADMIN_ACCESS paa ON paa.PROJECTADMINACCESS_ID = p.ID inner join S40CICORE.SYS_USER u ON paa.ADMINACCESS_ID = u.ID WHERE LOGINNAME = '%s'";
    private static final String ACCESS_PROJECT_AS_USER_QUERY = "select p.ID from S40CICORE.PROJECT p inner join S40CICORE.PROJECT_USER_ACCESS pua ON pua.PROJECTACCESS_ID = p.ID inner join S40CICORE.SYS_USER u ON pua.USERACCESS_ID = u.ID WHERE LOGINNAME = '%s'";
    private static final String USER_ROLE_QUERY = "select USERROLE from S40CICORE.SYS_USER where LOGINNAME = '%s' LIMIT 1";
    private static final String ALL_PROJECT_IDS_QUERY = "select ID from S40CICORE.PROJECT";

    public void logIn(String username, String password) throws UnexpectedException, UnauthorizedException {
        logOut();

        WebElement txtLogin = findElementById("login:usernameInput");
        txtLogin.sendKeys(username);
        waitForOneSecond();
        WebElement txtPassword = findElementById("login:passwordInput");
        txtPassword.sendKeys(password);
        WebElement btnSubmit = findElementById("login:loginButton");
        clickAndWaitForNewPage(btnSubmit);

        // Hide tutorial
        WebElement hideTutorial = findElementById("welcomeForm:hideTutorial");
        if (hideTutorial != null) {
            hideTutorial.click();
            waitUntilAjaxRequestCompletes();
        }

        user = username;

        waitForSeconds(3);
        updateUserRole();
        populatePages();

        log.info("Logged in as: " + user);
        log.info("User role: " + userRole);
    }

    public void logOut() throws UnexpectedException {
        try {
            loadPage(AbstractIntegrationTest.CI_BACKEND_UI_BASE_URL);
        } catch (UnauthorizedException ex) {
            waitUntilAjaxRequestCompletes();
            waitUntilAllAnimationsComplete();
        }

        user = "";
        userRole = "";
    }

    public void populatePages() {
        allPages.clear();
        addPages(allPages, UrlConstants.PROJECT_EDIT, ALL_PROJECT_IDS_QUERY);
        addPages(allPages, UrlConstants.USER_PROJECT, ALL_PROJECT_IDS_QUERY);

        allowedPages.clear();
        if (getCurrentRole().equals("SYSTEM_ADMIN")) {
            allowedPages.addAll(allPages);
        } else if (getCurrentRole().equals("PROJECT_ADMIN")) {
            addPages(allowedPages, UrlConstants.PROJECT_EDIT, ACCESS_PROJECT_AS_ADMIN_QUERY);
            addPages(allowedPages, UrlConstants.USER_PROJECT, ACCESS_PROJECT_AS_ADMIN_QUERY);
        } else {
            addPages(allowedPages, UrlConstants.USER_PROJECT, ACCESS_PROJECT_AS_USER_QUERY);
        }

        log.info("Current users list of accessable pages has been updated");
    }

    private void addPages(List<String> pages, String baseURL, String queryIDs) {
        List<Integer> pageIDs = DBConnector.getColumnAsIntegerList(String.format(queryIDs, this.user), "ID");
        for (Integer i : pageIDs) {
            pages.add(String.format(baseURL + i));
        }
    }

    private void updateUserRole() {
        userRole = DBConnector.getColumnAsStringList(String.format(USER_ROLE_QUERY, user), "USERROLE").get(0);
        log.info("The role of current user is " + userRole);
    }

    public List<String> getAllowedPages() {
        return allowedPages;
    }

    public List<String> getAllPages() {
        return allPages;
    }
}
