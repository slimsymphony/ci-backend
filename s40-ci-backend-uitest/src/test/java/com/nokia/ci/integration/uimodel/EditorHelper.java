package com.nokia.ci.integration.uimodel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 2/8/13
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditorHelper extends BaseHelper {

    private static String IFRAME = ".//iframe";

    public static void placeText(WebDriver driver, WebElement editorElement, String text) {
        WebElement editorIframe = editorElement.findElement(By.xpath(IFRAME));
        driver.switchTo().frame(editorIframe);
        driver.findElement(By.xpath("html/body")).sendKeys(text);
        driver.switchTo().defaultContent();
    }

    public static String getText(WebDriver driver, WebElement editorElement, String s) {
        WebElement editorIframe = editorElement.findElement(By.xpath(IFRAME));
        driver.switchTo().frame(editorIframe);
        String editorText = driver.findElement(By.xpath("html/body")).getText();
        driver.switchTo().defaultContent();
        return editorText;
    }
}
