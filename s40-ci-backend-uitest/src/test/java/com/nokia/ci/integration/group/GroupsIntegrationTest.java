package com.nokia.ci.integration.group;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/17/13 Time: 2:39 PM To change
 * this template use File | Settings | File Templates.
 */
public class GroupsIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldRedirectForAdd() {
        navigate(UrlConstants.GROUPS);
        WebElement addNewGroup = findElementByIdSuffix("addButton");

        clickAndWaitForNewPage(addNewGroup);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.GROUP_ADD);
        assertTrue("After Create New Group click, redirection failed", isRedirectOK);
    }

    @Test
    public void shouldRedirectForEdit() {
        navigate(UrlConstants.GROUPS);
        WebElement groupsDataTable = findElementById("projectGroupsTableForm:projectGroupsTable");
        WebElement rightRow = DataTableHelper.findRowByLabels(groupsDataTable, "Sample group 1");
        WebElement editLink = DataTableHelper.findEditLink(rightRow, "editProjectGroup");

        clickAndWaitForNewPage(editLink);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.GROUP_EDIT + 1);
        assertTrue("After edit link redirection failed", isRedirectOK);
    }

    @Test
    public void checkOrderOfGroups() {
        navigate(UrlConstants.GROUPS);
        WebElement groupsDataTable = findElementById("projectGroupsTableForm:projectGroupsTable");
        List<WebElement> rows = DataTableHelper.findRows(groupsDataTable);
        boolean isCountTitlesOk = rows.size() == 2;

        assertTrue("Table should have two groups", isCountTitlesOk);

        WebElement firstRow = rows.get(0);
        WebElement secondRow = rows.get(1);

        boolean hasFirstRowSimpleGroup1 = isTextPresent(firstRow, "Sample group 1");
        boolean hasFirstRowSimpleGroup2 = isTextPresent(secondRow, "Sample group 2");

        boolean isOrderOfGroupsOk = hasFirstRowSimpleGroup1 && hasFirstRowSimpleGroup2;
        assertTrue("Table default order not working", isOrderOfGroupsOk);
    }
}
