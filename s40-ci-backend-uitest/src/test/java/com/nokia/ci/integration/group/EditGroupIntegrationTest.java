package com.nokia.ci.integration.group;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;


import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/8/13 Time: 3:43 PM To change
 * this template use File | Settings | File Templates.
 */
//edit existing group
public class EditGroupIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldFullFillGroupInfo() {
        navigate(UrlConstants.GROUP_EDIT + 1);

        WebElement nameField = findElementById("groupEditorForm:projectGroupNameInput");
        boolean nameFieldIsOk = nameField.getAttribute("value").contains("Sample group 1");

        assertTrue("name field has incorrect value", nameFieldIsOk);

        WebElement descriptionField = findElementById("groupEditorForm:projectGroupDescriptionInput");
        boolean descriptionFieldIsOk = descriptionField.getText().contains("Sample group 1 description");

        assertTrue("description field has incorrect value", descriptionFieldIsOk);
    }
}
