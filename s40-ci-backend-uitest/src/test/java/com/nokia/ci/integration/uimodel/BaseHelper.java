package com.nokia.ci.integration.uimodel;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 1/18/13 Time: 3:19 PM To change
 * this template use File | Settings | File Templates.
 */
public class BaseHelper {

    protected static final String TEXT = "//*[text() = '%s']";
    protected static final String TEXT_IN_PARENT = "*//*[contains(text(),%s)] | *[contains(text(),%s)] | *//*[a[contains(text(), %s)]] | *[a[contains(text(), %s)]]";

    protected static String getXpathForTextSearch(String textToFind) {
        return String.format(TEXT_IN_PARENT, convertSingleQuotes(textToFind), convertSingleQuotes(textToFind), convertSingleQuotes(textToFind), convertSingleQuotes(textToFind));
    }

    protected static boolean isElementPresentByXpath(WebElement parent, String xpath) {
        try {
            WebElement element = parent.findElement(By.xpath(xpath));
            return element != null;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected static boolean isElementPresentByClassName(WebElement parent, String className) {
        try {
            WebElement element = parent.findElement(By.className(className));
            return element != null;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected static void waitUntilSubElementExists(WebDriver driver, final WebElement element, final By query) {
        new FluentWait<WebDriver>(driver).withTimeout(Constants.DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).ignoring(NoSuchElementException.class)
                .until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver wd) {
                WebElement subElement = element.findElement(query);
                return subElement != null;
            }
        });
    }
    private static final String ID_SUFFIX = ".//%s[substring(@id, string-length(@id)-%d)='%s']";
    private static final String ID_CONTAINS = ".//%s[contains(@id,'%s')]";

    public static WebElement findElementByIdSuffix(WebElement parentElement, String tagName, String idSuffix) {
        try {
            String idSuffixXpath = String.format(ID_SUFFIX, tagName, idSuffix.length() - 1, idSuffix);
            return parentElement.findElement(By.xpath(idSuffixXpath));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static WebElement findElementByIdSuffix(WebDriver parentElement, String tagName, String idSuffix) {
        try {
            String idSuffixXpath = String.format(ID_SUFFIX, tagName, idSuffix.length() - 1, idSuffix);
            return parentElement.findElement(By.xpath(idSuffixXpath));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static WebElement findElementByIdMid(WebElement parentElement, String tagName, String idSubString) {
        try {
            String idPreffixXpath = String.format(ID_CONTAINS, tagName, idSubString);
            return parentElement.findElement(By.xpath(idPreffixXpath));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Produce an XPath literal equal to the value if possible; if not, produce
     * an XPath expression that will match the value.
     *
     * Note that this function will produce very long XPath expressions if a
     * value contains a long run of double quotes.
     *
     * @name The value to match. return If the value contains only single or
     * double quotes, an XPath literal equal to the value. If it contains both,
     * an XPath expression, using concat(), that evaluates to the value.
     */
    private static String convertSingleQuotes(String value) {
        // if the value contains only single or double quotes, construct
        // an XPath literal
        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        // if the value contains both single and double quotes, construct an
        // expression that concatenates all non-double-quote substrings with
        // the quotes, e.g.:
        //
        //    concat("foo", '"', "bar")
        StringBuilder sb = new StringBuilder();
        sb.append("concat(");
        String[] substrings = value.split("'");
        for (int i = 0; i < substrings.length; i++) {
            boolean needComma = (i > 0);
            if (substrings[i] != "") {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("'");
                sb.append(substrings[i]);
                sb.append("'");
                needComma = true;
            }
            if (i < substrings.length - 1) {
                if (needComma) {
                    sb.append(", ");
                }
                sb.append("\"'\"");
            }

        }
        sb.append(")");
        return sb.toString();
    }
}
