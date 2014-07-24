package com.nokia.ci.integration.uimodel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 1/21/13
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class MatrixHelper extends BaseHelper {

    private static String COLUMNS = ".//thead/th";
    private static String ROWS = ".//tbody/tr/td/span";
    private static String CELL = ".//tbody/tr[%d]/td[%d]";
    private static final String CELL_SELECTED = ".//input[@type='checkbox'][@checked='checked']";
    private static final String CELL_DISABLED = ".//input[@type='checkbox'][@disabled='disabled']";



    public static List<String> getColumnNames(WebElement matrix){
        List<String> ret = new ArrayList<String>();

        List<WebElement> elements = matrix.findElements(By.xpath(COLUMNS));
        for (WebElement element : elements) {
            String text = element.getText();
            ret.add(text);
        }
        return ret;
    }

    public static List<String> getRowNames(WebElement matrix){
        List<String> ret = new ArrayList<String>();
        List<WebElement> elements = matrix.findElements(By.xpath(ROWS));
        for (WebElement element : elements) {
            String text = element.getText();
            ret.add(text);
        }
        return ret;
    }

    public static WebElement getCell(WebElement matrix,int row_index,int column_index){
        String cellXpath = String.format(CELL,row_index+1,column_index+2);
        WebElement cellElement = matrix.findElement(By.xpath(cellXpath));
        return cellElement;
    }

    public static boolean isCellMandatory(WebElement cell){
        return isElementPresentByClassName(cell,"verificationConfMandatory");
    }

    public static boolean isCellSelected(WebElement cell){
        return isElementPresentByClassName(cell,"ui-icon-check");
    }

    public static boolean isCellDisabled(WebElement cell){
        return isElementPresentByXpath(cell,CELL_DISABLED);
    }

    public static WebElement getCheckBox(WebElement cell) {
        return cell.findElement(By.className("ui-chkbox-box"));
    }
}
