package com.nokia.ci.integration.branch;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/17/13 Time: 2:39 PM To change
 * this template use File | Settings | File Templates.
 */
public class BranchesIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldRedirectForAdd() {
        navigate(UrlConstants.BRANCHES);
        WebElement addNewBranch = findElementByIdSuffix("addButton");

        clickAndWaitForNewPage(addNewBranch);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.BRANCH_ADD);
        assertTrue("After Add New Branch click, redirection failed", isRedirectOK);
    }

    @Test
    public void shouldRedirectForEdit() {
        navigate(UrlConstants.BRANCHES);

        WebElement branchesDataTable = findElementById("branchesTableForm:branchesTable");
        WebElement rightRow = DataTableHelper.findRowByLabels(branchesDataTable, "SCV", "develop", "X");
        WebElement editLink = DataTableHelper.findEditLink(rightRow, "editBranch");

        clickAndWaitForNewPage(editLink);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.BRANCH_EDIT + 1);
        assertTrue("After edit link redirection failed", isRedirectOK);
    }
}
