package com.nokia.ci.integration.product;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;


import static org.junit.Assert.assertTrue;

/**
 * User: suoyrjo Date: 02.07.2013 Time: 10:00
 */
//create new product
public class AddProductIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldCreateProduct() {
        navigate(UrlConstants.PRODUCTS_ADD);
        WebElement nameInput = findElementById("productEditForm:productDisplayNameInput");
        nameInput.clear();
        nameInput.sendKeys("Product-displayName");

        WebElement descriptionInput = findElementById("productEditForm:productNameInput");
        descriptionInput.clear();
        descriptionInput.sendKeys("Product-name");

        WebElement rmCodeInput = findElementById("productEditForm:productRmCodeInput");
        rmCodeInput.clear();
        rmCodeInput.sendKeys("Product-RM-code");

        WebElement productSaveButton = findElementById("productEditForm:productSaveButton");
        clickAndWaitForNewPage(productSaveButton);

        WebElement dataTable = findElementById("productsForm:productsTable");
        boolean isDisplayNamePresented = isElementPresentByXpath(dataTable, ".//*[text() = 'Product-displayName']");
        assertTrue("After create product - displayName missing on product admin page", isDisplayNamePresented);
    }

    @Test
    public void shouldCancel() {
        navigate(UrlConstants.PRODUCTS_ADD);
        WebElement productCancelButton = findElementById("productEditForm:productCancelButton");

        clickAndWaitForNewPage(productCancelButton);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.PRODUCTS);
        assertTrue("After cancel btn click, redirection failed", isRedirectOK);
    }
}
