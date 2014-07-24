package com.nokia.ci.integration.group;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;


import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/7/13 Time: 2:28 PM To change
 * this template use File | Settings | File Templates.
 */
//create new group
public class AddGroupIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldCreateGroup() {
        navigate(UrlConstants.GROUP_ADD);
        WebElement nameInput = findElementById("groupEditorForm:projectGroupNameInput");
        nameInput.clear();
        nameInput.sendKeys("SampleGroup-name");

        WebElement descriptionInput = findElementById("groupEditorForm:projectGroupDescriptionInput");
        descriptionInput.clear();
        descriptionInput.sendKeys("SampleGroup-description");

        WebElement projectSaveButton = findElementById("groupEditorForm:projectGroupSaveButton");

        clickAndWaitForNewPage(projectSaveButton);

        WebElement dataTable = findElementById("projectGroupsTableForm:projectGroupsTable");
        boolean isNamePresented = isElementPresentByXpath(dataTable, ".//*[text() = 'SampleGroup-name']");
        assertTrue("After create group - name missing on groups admin page", isNamePresented);
        boolean isDescriptionPresented = isElementPresentByXpath(dataTable, ".//*[text() = 'SampleGroup-description']");
        assertTrue("After create project - name missing on groups admin page", isDescriptionPresented);
    }

    @Test
    public void shouldCancel() {
        navigate(UrlConstants.GROUP_ADD);
        WebElement projectCancelButton = findElementById("groupEditorForm:projectGroupCancelButton");

        clickAndWaitForNewPage(projectCancelButton);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.GROUPS);
        assertTrue("After cancel btn click, redirection failed", isRedirectOK);
    }
}
