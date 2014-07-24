package com.nokia.ci.integration.uimodel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.Select;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 1/16/13
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class ComboBoxHelper {

    private static final String LI_TEXT = ".//li[text() = '%s']";

    public static WebElement findComboBoxOptionByName(WebDriver driver, WebElement comboBox, String text) {
        String comboBoxId = comboBox.getAttribute("id");
        WebElement panelUlElement = driver.findElement(By.id(comboBoxId + "_panel")).findElement(By.tagName("ul"));
        WebElement groupComboBoxOption = panelUlElement.findElement(By.xpath(String.format(LI_TEXT, text)));
        return groupComboBoxOption;
    }

    public static void rollDownComboBox(WebDriver driver, WebElement comboBox){
        String comboBoxId = comboBox.getAttribute("id");
        WebElement comboBoxElement = driver.findElement(By.id(comboBoxId));
        WebElement comboBoxTriangle = comboBoxElement.findElement(By.className("ui-icon-triangle-1-s"));
        comboBoxTriangle.click();
    }

    public static void chooseOption(WebDriver driver, WebElement comboBox, String text){
        WebElement comboBoxOptionByName = findComboBoxOptionByName(driver, comboBox, text);
        rollDownComboBox(driver,comboBox);
        comboBoxOptionByName.click();
    }

    public static boolean hasSelectedOption(WebDriver driver, WebElement comboBox, String text) {
        String comboBoxId = comboBox.getAttribute("id");
        WebElement labelUlElement = driver.findElement(By.id(comboBoxId + "_label"));
        return labelUlElement.getText().contains(text);
    }
    
    public static void selectObjectByIndex(WebElement selectID, int selectIndex) {
        Select select = new Select(selectID);
        select.selectByIndex(selectIndex);
    }
}
