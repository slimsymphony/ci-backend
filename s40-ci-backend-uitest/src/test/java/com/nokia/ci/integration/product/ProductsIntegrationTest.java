package com.nokia.ci.integration.product;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: suoyrjo Date: 1/17/13 Time: 2:39 PM To change
 * this template use File | Settings | File Templates.
 */
public class ProductsIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldRedirectForAdd() {
        navigate(UrlConstants.PRODUCTS);
        WebElement addNewProduct = findElementByIdSuffix("addButton");

        clickAndWaitForNewPage(addNewProduct);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.PRODUCTS_ADD);
        assertTrue("After Add New Product click, redirection failed", isRedirectOK);
    }

    @Test
    public void shouldRedirectForEdit() {
        navigate(UrlConstants.PRODUCTS);
        WebElement productsDataTable = findElementByIdSuffix("productsTable");
        WebElement rightRow = DataTableHelper.findRowByLabels(productsDataTable, "Product 1");
        WebElement editLink = DataTableHelper.findEditLink(rightRow, "editProduct");

        clickAndWaitForNewPage(editLink);

        boolean isRedirectOK = driver.getCurrentUrl().contains(UrlConstants.PRODUCTS_EDIT + 1);
        assertTrue("After edit link redirection failed", isRedirectOK);
    }
}
