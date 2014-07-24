package com.nokia.ci.integration.project;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/7/13 Time: 9:00 AM To change
 * this template use File | Settings | File Templates.
 */
//delete project X
public class DeleteProjectIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldDeleteProject() {
        navigate(UrlConstants.PROJECTS);

        WebElement sampleGroup1DataTable = DataTableHelper.findByTitle(driver, "Sample group 1");
        WebElement rowWithXproject = DataTableHelper.findRowByLabels(sampleGroup1DataTable, "X");
        DataTableHelper.processDelete(driver, rowWithXproject);
        waitUntilAjaxRequestCompletes();

        WebElement sampleGroup1DataTableAfter = DataTableHelper.findByTitle(driver, "Sample group 1");
        boolean found = DataTableHelper.hasRowWithText(sampleGroup1DataTableAfter, "X");

        assertTrue("Deletion of Project X failed", !found);
    }
}
