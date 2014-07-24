package com.nokia.ci.integration.uimodel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 1/18/13
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class TabsHelper extends BaseHelper {

    private static final String TAB = ".//li[@role = 'tab']";

    public static WebElement findTabByName(WebElement tabsElement, String textToFind) {
        List<WebElement> tabs = tabsElement.findElements(By.xpath(TAB));
        String xpath = getXpathForTextSearch(textToFind);
        WebElement rightTab = null;
        for (WebElement tab : tabs) {
            if (isElementPresentByXpath(tab, xpath)) {
                rightTab = tab;
            }
        }
        return rightTab;
    }
}
