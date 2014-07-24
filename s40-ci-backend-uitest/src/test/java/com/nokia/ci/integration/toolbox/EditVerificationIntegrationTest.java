package com.nokia.ci.integration.toolbox;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.ComboBoxHelper;
import com.nokia.ci.integration.uimodel.MatrixHelper;
import com.nokia.ci.integration.uimodel.TabsHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/22/13 Time: 10:44 AM To change
 * this template use File | Settings | File Templates.
 */
public class EditVerificationIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldCreateVerification() {
        navigate(UrlConstants.VERIFICATION_EDIT + 11);

        WebElement verificationNameInput = findElementById("verificationEditorForm:viewSelect:verificationDisplayNameInput");
        boolean nameFieldIsOk = verificationNameInput.getAttribute("value").contains("User toolbox job 1");

        assertTrue("name field has incorrect value", nameFieldIsOk);

        WebElement branchComboBox = findElementById("verificationEditorForm:viewSelect:jobBranchInput");
        ComboBoxHelper.hasSelectedOption(driver, branchComboBox, "toolbox");

        WebElement tabsComponent = findElementById("verificationEditorForm:viewSelect");
        WebElement verificationsTab = TabsHelper.findTabByName(tabsComponent, "Verifications");
        verificationsTab.click();
        waitUntilAjaxRequestCompletes();
        WebElement matrix = findElementById("confMatrixTable");
        WebElement cell = MatrixHelper.getCell(matrix, 0, 0);
        assertTrue("Cell should be enabled", !MatrixHelper.isCellDisabled(cell));
        assertTrue("Cell should selected", MatrixHelper.isCellSelected(cell));
        assertTrue("Cell should display as optional", !MatrixHelper.isCellMandatory(cell));
    }
}
