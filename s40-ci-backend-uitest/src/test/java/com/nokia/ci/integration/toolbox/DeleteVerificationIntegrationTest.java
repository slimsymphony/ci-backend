package com.nokia.ci.integration.toolbox;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.exception.UnauthorizedException;
import com.nokia.ci.integration.exception.UnexpectedException;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/22/13 Time: 10:27 AM To change
 * this template use File | Settings | File Templates.
 */
public class DeleteVerificationIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldDeleteVerification() throws UnauthorizedException, UnexpectedException {
        navigate(UrlConstants.USER_PROJECT + 1);

        WebElement dataTable = findElementById("jobDataTableComp:jobDatatableForm:jobDatatable_jobDataTableComp");
        //WebElement dataTable = DataTableHelper.findByTitle(driver, "Project Details");
        WebElement toolBoxJob = DataTableHelper.findRowByLabels(dataTable, "User toolbox job 1");
        DataTableHelper.processDelete(driver, toolBoxJob);
        waitUntilAjaxRequestCompletes();

        WebElement dataTableAfter = findElementById("jobDataTableComp:jobDatatableForm:jobDatatable_jobDataTableComp");
        boolean isToolboxRowExists = DataTableHelper.hasRowWithText(dataTableAfter, "User toolbox job 1");

        assertTrue("Toolbox should be deleted", !isToolboxRowExists);
    }
}
