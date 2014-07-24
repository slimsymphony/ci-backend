package com.nokia.ci.integration.uimodel;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.support.ui.Select;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/8/13 Time: 2:52 PM To change
 * this template use File | Settings | File Templates.
 */
public class DataTableHelper extends BaseHelper {

    private static final String TITLE = ".//*[contains(@class , 'contentTitleText')][text() = '%s']";
    private static final String DATA_TABLE = ".//*[contains(@class,'ui-datatable')]";
    private static final String ROW = ".//tbody/tr[@role = 'row']";
    private static final String COLUMN = ".//td[@role = 'gridcell']";
    private static final String ACTION_COLUMN = ".//td[@role = 'gridcell'][contains(@class, 'actionColumn')]";
    private static final String EDIT_LINK = ".//a[img[substring(@id, string-length(@id)-%d)='%s']]";

    public static WebElement findByTitle(WebDriver driver, String title) {
        try {
            String titleXpath = String.format(TITLE, title);
            WebElement tableTitle = driver.findElement(By.xpath(titleXpath));
            WebElement parentOfTableTitle = tableTitle.findElement(By.xpath(".."));
            WebElement dataTable = parentOfTableTitle.findElement(By.xpath(DATA_TABLE));
            return dataTable;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static boolean hasRowWithText(WebElement dataTable, String textToFind) {
        if (dataTable == null) {
            return false;
        }
        List<WebElement> rows = findRows(dataTable);
        boolean textFound = false;
        String textSearch = getXpathForTextSearch(textToFind);
        for (WebElement row : rows) {
            if (isElementPresentByXpath(row, textSearch)) {
                textFound = true;
            }
        }
        return textFound;
    }

    public static WebElement findActionColumnCell(WebElement row) {
        return row.findElement(By.xpath(ACTION_COLUMN));
    }

    public static WebElement findEditLink(WebElement row, String linkClass) {
        try {
            String deleteLink = String.format(EDIT_LINK, linkClass.length() - 1, linkClass);
            WebElement actionColumn = row.findElement(By.xpath(ACTION_COLUMN));
            return actionColumn.findElement(By.xpath(deleteLink));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static List<WebElement> findRows(WebElement dataTable) {
        return dataTable.findElements(By.xpath(ROW));
    }

    public static List<WebElement> findColumns(WebElement row) {
        return row.findElements(By.xpath(COLUMN));
    }

    public static List<Integer> getOptionsForPaginatorSize(WebElement dataTable) {
        List<Integer> ret = new ArrayList<Integer>();
        String dataTableId = dataTable.getAttribute("id");
        WebElement paginator_top = dataTable.findElement(By.id(dataTableId + "_paginator_top"));
        List<WebElement> options = paginator_top.findElements(By.xpath(".//select/option"));
        for (WebElement option : options) {
            ret.add(Integer.parseInt(option.getText()));
        }
        return ret;
    }

    public static void choosePaginatorSize(WebElement dataTable, int paginatorOption) {
        String dataTableId = dataTable.getAttribute("id");
        WebElement paginator_top = dataTable.findElement(By.id(dataTableId + "_paginator_top"));
        Select select = new Select(paginator_top.findElement(By.tagName("select")));
        select.selectByValue(String.valueOf(paginatorOption));
    }

    public static WebElement findNextPageButton(WebDriver driver, WebElement dataTable) {
        String dataTableId = dataTable.getAttribute("id");
        WebElement paginatorTop = dataTable.findElement(By.id(dataTableId + "_paginator_top"));
        waitUntilSubElementExists(driver, paginatorTop, By.className("ui-paginator-next"));
        WebElement nextButton = paginatorTop.findElement(By.className("ui-paginator-next"));
        return nextButton;
    }

    public static WebElement findRowByLabels(WebElement dataTable, String... labels) {
        List<WebElement> rows = findRows(dataTable);
        for (WebElement row : rows) {
            if (hasRowAllLabels(row, labels)) {
                return row;
            }
        }
        return null;
    }

    private static boolean hasRowAllLabels(WebElement row, String[] labels) {
        boolean ret = true;
        for (String label : labels) {
            String labelAsXpath = getXpathForTextSearch(label);
            if (!isElementPresentByXpath(row, labelAsXpath)) {
                ret = false;
            }
        }
        return ret;
    }

    public static boolean hasRowWithLabels(WebElement dataTable, String... labels) {
        if (dataTable == null) {
            return false;
        }
        WebElement foundedRow = findRowByLabels(dataTable, labels);
        return foundedRow != null;
    }

    public static void processDelete(WebDriver driver, WebElement rightRow) {
        WebElement actionColumnCell = DataTableHelper.findActionColumnCell(rightRow);
        WebElement deleteLink = DeleteBtnWithConfirmDlgHelper.findDeleteLink(actionColumnCell);
        deleteLink.click();
        WebElement dialog = DeleteBtnWithConfirmDlgHelper.findDialog(actionColumnCell);
        DeleteBtnWithConfirmDlgHelper.clickYesBtn(driver, dialog);
    }
}
