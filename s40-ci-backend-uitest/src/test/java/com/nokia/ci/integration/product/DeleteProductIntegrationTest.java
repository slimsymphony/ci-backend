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
//delete product
public class DeleteProductIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldDeleteProduct() {
        navigate(UrlConstants.PRODUCTS);
        WebElement productsDataTable = findElementById("productsForm:productsTable");
        WebElement rightRow = DataTableHelper.findRowByLabels(productsDataTable, "Product 5");
        DataTableHelper.processDelete(driver, rightRow);
        waitUntilAjaxRequestCompletes();

        WebElement productsDataTableAfter = findElementById("productsForm:productsTable");
        boolean productsDataTableHasProductDevelop = DataTableHelper.hasRowWithLabels(productsDataTableAfter, ".//*[text() = 'Product-displayName']");

        assertTrue("Product (displayName='Product-displayName') defined for project 'X' was not removed", !productsDataTableHasProductDevelop);
    }
}
