package com.nokia.ci.integration.project;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.ComboBoxHelper;
import com.nokia.ci.integration.uimodel.EditorHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;


import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/8/13 Time: 3:43 PM To change
 * this template use File | Settings | File Templates.
 */
//edit existing project
public class EditProjectIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldFullFillProjectInfo() {
        navigate(UrlConstants.PROJECT_EDIT + 1);

        WebElement displayNameInput = findElementById("projectEditorForm:projectDisplayNameInput");
        boolean displayNameFieldIsOk = displayNameInput.getAttribute("value").contains("X");

        assertTrue("display name field has incorrect value", displayNameFieldIsOk);

        WebElement nameInput = findElementById("projectEditorForm:projectNameInput");
        boolean nameFieldIsOk = nameInput.getAttribute("value").contains("x_project");

        assertTrue("name field has incorrect value", nameFieldIsOk);

        WebElement descriptionInput = findElementById("projectEditorForm:projectDescriptionInput");
        String descriptionInputText = EditorHelper.getText(driver, descriptionInput, "Project X description.");
        boolean descriptionFieldIsOk = descriptionInputText.contains("Project X description.");

        assertTrue("description field has incorrect value", descriptionFieldIsOk);

        WebElement groupComboBox = findElementById("projectEditorForm:projectGroupInput");
        boolean groupFieldIsOk = ComboBoxHelper.hasSelectedOption(driver, groupComboBox, "Sample group 1");

        assertTrue("group field has incorrect value", groupFieldIsOk);

        WebElement gerritComboBox = findElementById("projectEditorForm:projectGerritInput");
        boolean gerritFieldIsOk = ComboBoxHelper.hasSelectedOption(driver, gerritComboBox, "localhost:1338");

        assertTrue("gerrit field has incorrect value", gerritFieldIsOk);
    }
}
