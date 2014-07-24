package com.nokia.ci.integration.uimodel;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 2/20/13
 * Time: 1:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteBtnWithConfirmDlgHelper extends BaseHelper {


    public static WebElement findDeleteLink(WebElement actionColumnCell) {
        return findElementByIdSuffix(actionColumnCell, "a", "deleteLink");
    }

    public static WebElement findConfirmDlgYesBtn(WebElement actionColumnCell) {
        return findElementByIdSuffix(actionColumnCell, "button", "confirm");
    }

    public static WebElement findDialog(WebElement actionColumnCell) {
        return findElementByIdMid(actionColumnCell, "div", "confirmDialog_");
    }

    public static void clickYesBtn(WebDriver driver, WebElement dialog) {
        WebElement confirmDlgYesBtn = findConfirmDlgYesBtn(dialog);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", confirmDlgYesBtn);
    }
}
