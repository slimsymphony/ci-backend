package com.nokia.ci.integration.group;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/8/13 Time: 10:46 AM To change
 * this template use File | Settings | File Templates.
 */
//delete group 'Sample group 1'
public class DeleteGroupIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldDeleteGroupAndProjectMoveToNoGroup() {
        navigate(UrlConstants.GROUPS);
        WebElement groupsDataTable = findElementById("projectGroupsTableForm:projectGroupsTable");
        WebElement rightRow = DataTableHelper.findRowByLabels(groupsDataTable, "Sample group 1");
        DataTableHelper.processDelete(driver, rightRow);
        waitUntilAjaxRequestCompletes();

        WebElement groupsDataTableAfter = findElementById("projectGroupsTableForm:projectGroupsTable");
        boolean groupsDataTableHasSampleGroup1 = DataTableHelper.hasRowWithLabels(groupsDataTableAfter, "Sample group 1");
        assertTrue("Sample group 1 was not removed", !groupsDataTableHasSampleGroup1);

        navigate(UrlConstants.PROJECTS);

        WebElement noGroupDataTable = DataTableHelper.findByTitle(driver, "No group");
        boolean noGroupDataTableHasX = DataTableHelper.hasRowWithLabels(noGroupDataTable, "X");
        assertTrue("Project X should be in 'No group' dataTable", noGroupDataTableHasX);

        WebElement sampleGroup1DataTable = DataTableHelper.findByTitle(driver, "Sample group 1");
        boolean sampleGroup2HasX = DataTableHelper.hasRowWithLabels(sampleGroup1DataTable, "X");
        assertTrue("Project X should not be in 'Sample group 2' dataTable", !sampleGroup2HasX);

    }
}
