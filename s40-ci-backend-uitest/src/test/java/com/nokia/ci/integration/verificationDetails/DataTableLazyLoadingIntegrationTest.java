package com.nokia.ci.integration.verificationDetails;

import com.nokia.ci.integration.AbstractIntegrationTest;
import com.nokia.ci.integration.uimodel.DataTableHelper;
import com.nokia.ci.integration.uimodel.UrlConstants;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static junit.framework.Assert.assertTrue;

// use paginator for Project X and verification 'Single commit verification'
public class DataTableLazyLoadingIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void shouldChangeRows() {
        navigate(UrlConstants.VERIFICATION + 2);

        WebElement dataTable = findElementById("buildsTable:buildDatatableForm:buildsTable");
        List<Integer> paginatorSizes = DataTableHelper.getOptionsForPaginatorSize(dataTable);

        DataTableHelper.choosePaginatorSize(dataTable, paginatorSizes.get(0));
        waitUntilAjaxRequestCompletes();
        dataTable = findElementById("buildsTable:buildDatatableForm:buildsTable");
        List<WebElement> rowsWhenFirstOption = DataTableHelper.findRows(dataTable);
        assertTrue("Found " + rowsWhenFirstOption.size() + " rows but paginator option was " + paginatorSizes.get(0), rowsWhenFirstOption.size() == paginatorSizes.get(0));

        DataTableHelper.choosePaginatorSize(dataTable, paginatorSizes.get(1));
        waitUntilAjaxRequestCompletes();
        dataTable = findElementById("buildsTable:buildDatatableForm:buildsTable");
        List<WebElement> rowsWhenSecondOption = DataTableHelper.findRows(dataTable);
        assertTrue("Found " + rowsWhenSecondOption.size() + " rows but paginator option was " + paginatorSizes.get(1), rowsWhenSecondOption.size() == paginatorSizes.get(1));

        DataTableHelper.choosePaginatorSize(dataTable, paginatorSizes.get(2));
        waitUntilAjaxRequestCompletes();
        dataTable = findElementById("buildsTable:buildDatatableForm:buildsTable");
        List<WebElement> rowsWhenThirdOption = DataTableHelper.findRows(dataTable);
        assertTrue("Found " + rowsWhenThirdOption.size() + " rows but paginator option was " + paginatorSizes.get(2), rowsWhenThirdOption.size() == paginatorSizes.get(2));

        dataTable = findElementById("buildsTable:buildDatatableForm:buildsTable");
        List<WebElement> rows = DataTableHelper.findRows(dataTable);
        String firstCellText = rows.get(0).findElements(By.tagName("td")).get(0).getText();
        WebElement nextBtn = DataTableHelper.findNextPageButton(driver, dataTable);
        scrollToDefaults();
        nextBtn.click();
        waitUntilAjaxRequestCompletes();
        dataTable = findElementById("buildsTable:buildDatatableForm:buildsTable");
        List<WebElement> rowsAfter = DataTableHelper.findRows(dataTable);
        String firstCellTextAfter = rowsAfter.get(0).findElements(By.tagName("td")).get(0).getText();
        assertTrue("Next button point to same data", !firstCellText.equals(firstCellTextAfter));
    }
}
