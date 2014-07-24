package com.nokia.ci.integration;

import com.nokia.ci.integration.account.AccessRightsIntegrationTest;
import com.nokia.ci.integration.account.AdminLoginIntegrationTest;
import com.nokia.ci.integration.account.ProjectManagerLoginIntegrationTest;
import com.nokia.ci.integration.account.UserLoginIntegrationTest;
import com.nokia.ci.integration.branch.AddBranchIntegrationTest;
import com.nokia.ci.integration.branch.BranchesIntegrationTest;
import com.nokia.ci.integration.branch.DeleteBranchIntegrationTest;
import com.nokia.ci.integration.branch.EditBranchIntegrationTest;
import com.nokia.ci.integration.group.AddGroupIntegrationTest;
import com.nokia.ci.integration.group.DeleteGroupIntegrationTest;
import com.nokia.ci.integration.group.EditGroupIntegrationTest;
import com.nokia.ci.integration.group.GroupsIntegrationTest;
import com.nokia.ci.integration.product.AddProductIntegrationTest;
import com.nokia.ci.integration.product.DeleteProductIntegrationTest;
import com.nokia.ci.integration.product.EditProductIntegrationTest;
import com.nokia.ci.integration.product.ProductsIntegrationTest;
import com.nokia.ci.integration.project.AddProjectIntegrationTest;
import com.nokia.ci.integration.project.DeleteProjectIntegrationTest;
import com.nokia.ci.integration.project.EditProjectIntegrationTest;
import com.nokia.ci.integration.project.ProjectsIntegrationTest;
import com.nokia.ci.integration.toolbox.AddVerificationIntegrationTest;
import com.nokia.ci.integration.toolbox.DeleteVerificationIntegrationTest;
import com.nokia.ci.integration.toolbox.EditVerificationIntegrationTest;
import com.nokia.ci.integration.toolbox.VerificationsIntegrationTest;
import com.nokia.ci.integration.verificationDetails.DataTableLazyLoadingIntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/4/13 Time: 10:39 AM To change
 * this template use File | Settings | File Templates.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(value = {
    AdminLoginIntegrationTest.class,
    AccessRightsIntegrationTest.class,
    BranchesIntegrationTest.class,
    AddBranchIntegrationTest.class,
    EditBranchIntegrationTest.class,
    DeleteBranchIntegrationTest.class,
    GroupsIntegrationTest.class,
    AddGroupIntegrationTest.class,
    EditGroupIntegrationTest.class,
    DeleteGroupIntegrationTest.class,
    ProjectsIntegrationTest.class,
    AddProjectIntegrationTest.class,
    EditProjectIntegrationTest.class,
    DeleteProjectIntegrationTest.class,
    ProductsIntegrationTest.class,
    AddProductIntegrationTest.class,
    EditProductIntegrationTest.class,
    DeleteProductIntegrationTest.class,
    ProjectManagerLoginIntegrationTest.class,
    AccessRightsIntegrationTest.class,
    UserLoginIntegrationTest.class,
    AccessRightsIntegrationTest.class,
    VerificationsIntegrationTest.class,
    AddVerificationIntegrationTest.class,
    EditVerificationIntegrationTest.class,
    DeleteVerificationIntegrationTest.class,
    DataTableLazyLoadingIntegrationTest.class
})
public class MainSuite {

    @BeforeClass
    public static void beforeClass() {
        WebDriverSingletonHolder instance = WebDriverSingletonHolder.getInstance();
        RemoteWebDriver driver = (RemoteWebDriver) instance.getDriver();
        Capabilities caps = driver.getCapabilities();
        String browserName = caps.getBrowserName();
        String browserVersion = caps.getVersion();
        System.out.println("Running with " + browserName + " " + browserVersion);
    }

    @AfterClass
    public static void afterClass() {
        WebDriverSingletonHolder instance = WebDriverSingletonHolder.getInstance();
        WebDriver driver = instance.getDriver();
        driver.close();
    }
}
