package com.nokia.ci.integration.project;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.ComboBoxHelper;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.EditorHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;


import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/7/13 Time: 2:28 PM To change
 * this template use File | Settings | File Templates.
 */
//create project for Sample group 2
public class AddProjectIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldCreateProject() {
        navigate(UrlConstants.PROJECT_ADD);
        WebElement displayNameInput = findElementById("projectEditorForm:projectDisplayNameInput");
        displayNameInput.clear();
        displayNameInput.sendKeys("SampleProject-displayName");

        WebElement nameInput = findElementById("projectEditorForm:projectNameInput");
        nameInput.clear();
        nameInput.sendKeys("SampleProject-name");

        WebElement descriptionInput = findElementById("projectEditorForm:projectDescriptionInput");
        EditorHelper.placeText(driver, descriptionInput, "SampleProject-description");

        WebElement groupComboBox = findElementById("projectEditorForm:projectGroupInput");
        ComboBoxHelper.chooseOption(driver, groupComboBox, "Sample group 2");

        WebElement gerritComboBox = findElementById("projectEditorForm:projectGerritInput");
        ComboBoxHelper.chooseOption(driver, gerritComboBox, "localhost:1338");

        waitUntilAjaxRequestCompletes();

        WebElement projectSaveButton = findElementById("projectEditorForm:projectSaveButton");

        clickAndWaitForNewPage(projectSaveButton);

        WebElement dataTable = DataTableHelper.findByTitle(driver, "Sample group 2");
        boolean displayNamePresented = isElementPresentByXpath(dataTable, "//*[text() = 'SampleProject-displayName']");
        assertTrue("After create project - displayName missing on projects admin page", displayNamePresented);
        boolean namePresented = isElementPresentByXpath(dataTable, ".//*[text() = 'SampleProject-name']");
        assertTrue("After create project - name missing on projects admin page", namePresented);
        boolean descriptionPresented = isElementPresentByXpath(dataTable, ".//*[text() = 'SampleProject-description']");
        assertTrue("After create project - description missing on projects admin page", descriptionPresented);
    }
}
