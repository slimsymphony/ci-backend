package com.nokia.ci.integration.branch;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.PickListHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/8/13 Time: 3:43 PM To change
 * this template use File | Settings | File Templates.
 */
//edit existing branch
public class EditBranchIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldFullFillBranchInfo() {
        navigate(UrlConstants.BRANCH_EDIT + 1);

        WebElement displayNameInput = findElementById("branchEditorForm:displayNameInput");
        boolean displayNameFieldIsOk = displayNameInput.getAttribute("value").contains("SCV");
        assertTrue("Display name field has incorrect value", displayNameFieldIsOk);

        WebElement nameInput = findElementById("branchEditorForm:branchNameInput");
        boolean nameFieldIsOk = nameInput.getAttribute("value").contains("develop");
        assertTrue("Name field has incorrect value", nameFieldIsOk);

        WebElement jobsPickList = findElementById("branchEditorForm:jobsPickList");
        List<String> targetOptionsAsString = PickListHelper.getTargetOptionsAsString(jobsPickList);
        String firstOption = targetOptionsAsString.get(0);
        boolean hasSelectedRightJob = firstOption.contains("X single commit verification");
        assertTrue("Jobs pickList field should contain job 'X single commit verification'", hasSelectedRightJob);

        WebElement serversPickList = findElementById("branchEditorForm:serversPickList");
        List<String> serversTargetOptionsAsString = PickListHelper.getTargetOptionsAsString(serversPickList);
        String serversFirstOption = serversTargetOptionsAsString.get(0);
        boolean hasSelectedRightServer = serversFirstOption.contains("localhost:1337");
        assertTrue("Servers pickList field should contain server 'localhost:1337'", hasSelectedRightServer);
    }
}
