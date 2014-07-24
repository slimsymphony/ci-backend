package com.nokia.ci.integration.project;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/17/13 Time: 2:39 PM To change
 * this template use File | Settings | File Templates.
 */
public class ProjectsIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldRedirectForAdd() {
        navigate(UrlConstants.PROJECTS);

        WebElement addNewProject = findElementByIdSuffix("addButton");

        clickAndWaitForNewPage(addNewProject);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.PROJECT_ADD);
        assertTrue("After add new project click redirection failed", isRedirectOK);
    }

    @Test
    public void shouldRedirectForEdit() {
        navigate(UrlConstants.PROJECTS);

        WebElement sampleGroup1DataTable = DataTableHelper.findByTitle(driver, "Sample group 1");
        WebElement rowWithXproject = DataTableHelper.findRowByLabels(sampleGroup1DataTable, "X");
        WebElement editLink = DataTableHelper.findEditLink(rowWithXproject, "editProject");

        clickAndWaitForNewPage(editLink);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.PROJECT_EDIT + 1);
        assertTrue("After edit link redirection failed", isRedirectOK);
    }

    @Test
    public void checkOrderOfGroups() {
        navigate(UrlConstants.PROJECTS);

        List<WebElement> titles = findElementsByXpath(".//*[contains(@class , 'contentTitleText')]");

        boolean isCountTitlesOk = titles.size() == 3;

        assertTrue("Page doesn't contain tree groups title", isCountTitlesOk);

        WebElement firstTitle = titles.get(0);
        WebElement secondTitle = titles.get(1);
        WebElement thirdTitle = titles.get(2);

        boolean isFirstGroupSimpleGroup1 = firstTitle.getText().contains("Sample group 1");
        boolean isSecondGroupSimpleGroup2 = secondTitle.getText().contains("Sample group 2");
        boolean isThirdGroupNoGroup = thirdTitle.getText().contains("No group");
        boolean isOrderOfGroupsOk = isFirstGroupSimpleGroup1 && isSecondGroupSimpleGroup2 && isThirdGroupNoGroup;
        assertTrue("Order of groups are wrong", isOrderOfGroupsOk);
    }
}
