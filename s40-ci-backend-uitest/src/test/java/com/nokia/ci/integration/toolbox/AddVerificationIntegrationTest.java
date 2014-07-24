package com.nokia.ci.integration.toolbox;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.ComboBoxHelper;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.MatrixHelper;
import com.nokia.ci.integration.uimodel.TabsHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/18/13 Time: 2:49 PM To change
 * this template use File | Settings | File Templates.
 */
public class AddVerificationIntegrationTest extends AbstractIntegrationTest {

    private final String VERIFICATION_NAME = "User toolbox job 2";

    @Test
    public void shouldCreateVerification() {
        navigate(UrlConstants.VERIFICATION_ADD + 1);

        WebElement verificationNameInput = findElementById("verificationEditorForm:viewSelect:verificationDisplayNameInput");
        verificationNameInput.sendKeys(VERIFICATION_NAME);

        WebElement branchComboBox = findElementById("verificationEditorForm:viewSelect:jobBranchInput");
        ComboBoxHelper.chooseOption(driver, branchComboBox, "TBV");

        waitUntilAjaxRequestCompletes();

        WebElement tabsComponent = findElementById("verificationEditorForm:viewSelect");
        WebElement verificationsTab = TabsHelper.findTabByName(tabsComponent, "Verifications");
        verificationsTab.click();
        waitUntilAjaxRequestCompletes();
        WebElement matrix = findElementById("confMatrixTable");
        WebElement cell = MatrixHelper.getCell(matrix, 0, 0);
        assertTrue("Cell should be enabled", !MatrixHelper.isCellDisabled(cell));
        assertTrue("Cell should be unselected by default", !MatrixHelper.isCellSelected(cell));

        WebElement checkBox = MatrixHelper.getCheckBox(cell);
        checkBox.click();
        waitUntilAjaxRequestCompletes();

        WebElement matrixAfter = findElementById("confMatrixTable");
        WebElement cellAfter = MatrixHelper.getCell(matrixAfter, 0, 0);
        assertTrue("Cell should be enabled", !MatrixHelper.isCellDisabled(cellAfter));
        assertTrue("Cell should be selected", MatrixHelper.isCellSelected(cellAfter));
        assertTrue("Cell should display as mandatory", MatrixHelper.isCellMandatory(cellAfter));

        WebElement saveButton = findElementById("verificationEditorForm:verificationSaveButton");
        clickAndWaitForNewPage(saveButton);
        navigate(UrlConstants.USER_PROJECT + 1);

        WebElement dataTable = findElementById("jobDataTableComp:jobDatatableForm:jobDatatable_jobDataTableComp");
        boolean isVerificationListedOk = DataTableHelper.hasRowWithText(dataTable, VERIFICATION_NAME);
        assertTrue("New verification is missing, in project dataTable", isVerificationListedOk);
    }
}
