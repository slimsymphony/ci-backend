package com.nokia.ci.integration.product;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;


import static org.junit.Assert.assertTrue;

/**
 * User: suoyrjo Date: 02.07.2013 Time: 10:00
 */
//edit existing product
public class EditProductIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldFullFillProductInfo() {
        navigate(UrlConstants.PRODUCTS);
        WebElement productsDataTable = findElementById("productsForm:productsTable");
        WebElement rightRow = DataTableHelper.findRowByLabels(productsDataTable, "Product 1");
        WebElement editLink = DataTableHelper.findEditLink(rightRow, "editProduct");
        clickAndWaitForNewPage(editLink);

        WebElement displayNameInput = findElementById("productEditForm:productDisplayNameInput");
        boolean displayNameFieldIsOk = displayNameInput.getAttribute("value").contains("Product 1");
        assertTrue("Display name field has incorrect value", displayNameFieldIsOk);

        WebElement nameInput = findElementById("productEditForm:productNameInput");
        boolean nameFieldIsOk = nameInput.getAttribute("value").contains("product_1");
        assertTrue("Name field has incorrect value", nameFieldIsOk);

        WebElement RMCodeInput = findElementById("productEditForm:productRmCodeInput");
        boolean RMCodeFieldIsOk = RMCodeInput.getAttribute("value").contains("rm-01");
        assertTrue("Name field has incorrect value", RMCodeFieldIsOk);
    }

    @Test
    public void shouldEditProductInfo() {
        navigate(UrlConstants.PRODUCTS);
        WebElement productsDataTable = findElementById("productsForm:productsTable");
        WebElement rightRow = DataTableHelper.findRowByLabels(productsDataTable, "Product 1");
        WebElement editLink = DataTableHelper.findEditLink(rightRow, "editProduct");
        clickAndWaitForNewPage(editLink);

        WebElement nameInput = findElementById("productEditForm:productDisplayNameInput");
        nameInput.clear();
        nameInput.sendKeys("Product-displayName");

        WebElement productSaveButton = findElementById("productEditForm:productSaveButton");
        clickAndWaitForNewPage(productSaveButton);

        WebElement dataTable = findElementById("productsForm:productsTable");
        boolean isDisplayNamePresented = isElementPresentByXpath(dataTable, ".//*[text() = 'Product-displayName']");
        assertTrue("After editing product - Product missing on product admin page", isDisplayNamePresented);
    }
}
