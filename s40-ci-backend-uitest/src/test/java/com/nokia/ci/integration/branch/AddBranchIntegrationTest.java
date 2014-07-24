package com.nokia.ci.integration.branch;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.ComboBoxHelper;
import com.nokia.ci.integration.uimodel.PickListHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import java.util.List;
import org.junit.Test;
import org.openqa.selenium.WebElement;


import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/7/13 Time: 2:28 PM To change
 * this template use File | Settings | File Templates.
 */
//create new branch
public class AddBranchIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldCreateBranch() {
        navigate(UrlConstants.BRANCH_ADD);

        WebElement projectList = findElementById("branchEditorForm:projectSelectInput");
        ComboBoxHelper.chooseOption(driver, projectList, "Y");

        WebElement nameInput = findElementById("branchEditorForm:displayNameInput");
        nameInput.clear();
        nameInput.sendKeys("Branch-displayName");

        WebElement descriptionInput = findElementById("branchEditorForm:branchNameInput");
        descriptionInput.clear();
        descriptionInput.sendKeys("Branch-name");

        WebElement serversPickList = findElementById("branchEditorForm:serversPickList");
        List<String> sourceOptionsAsString = PickListHelper.getSourceOptionsAsString(serversPickList);
        String firstOption = sourceOptionsAsString.get(0);
        WebElement firstSourceOption = PickListHelper.findSourceByLabel(serversPickList, firstOption);
        firstSourceOption.click();
        WebElement addBtn = PickListHelper.getAddBtn(serversPickList);
        addBtn.click();

        WebElement branchSaveButton = findElementById("branchEditorForm:branchSaveButton");
        clickAndWaitForNewPage(branchSaveButton);

        WebElement dataTable = findElementById("branchesTableForm:branchesTable");
        boolean isDisplayNamePresented = isElementPresentByXpath(dataTable, ".//*[text() = 'Branch-displayName']");
        assertTrue("After create branch - displayName missing on branches admin page", isDisplayNamePresented);
        boolean isNamePresented = isElementPresentByXpath(dataTable, ".//*[text() = 'Branch-name']");
        assertTrue("After create branch - name missing on branches admin page", isNamePresented);
    }

    @Test
    public void shouldCancel() {
        navigate(UrlConstants.BRANCH_ADD);
        WebElement branchCancelButton = findElementById("branchEditorForm:branchCancelButton");

        clickAndWaitForNewPage(branchCancelButton);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.BRANCHES);
        assertTrue("After cancel btn click, redirection failed", isRedirectOK);
    }
}
