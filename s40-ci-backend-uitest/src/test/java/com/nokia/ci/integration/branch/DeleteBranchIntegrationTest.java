package com.nokia.ci.integration.branch;

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
//delete branch
public class DeleteBranchIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldDeleteBranch() throws Exception {
        navigate(UrlConstants.BRANCHES);

        WebElement branchesDataTable = findElementById("branchesTableForm:branchesTable");
        WebElement rightRow = DataTableHelper.findRowByLabels(branchesDataTable, "develop", "SCV", "X");
        DataTableHelper.processDelete(driver, rightRow);
        waitUntilAjaxRequestCompletes();

        WebElement branchesDataTableAfter = findElementById("branchesTableForm:branchesTable");
        boolean branchesDataTableHasBranchDevelop = DataTableHelper.hasRowWithLabels(branchesDataTableAfter, "develop", "SCV", "X");

        assertTrue("Branch (displayName='develop', name='SCV') defined for project 'X' was not removed", !branchesDataTableHasBranchDevelop);
    }
}
