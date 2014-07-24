package com.nokia.ci.integration;

import com.nokia.ci.integration.exception.UnauthorizedException;
import com.nokia.ci.integration.exception.UnexpectedException;
import com.nokia.ci.integration.sqlImport.DBConnector;
import com.nokia.ci.integration.uimodel.BaseHelper;
import com.nokia.ci.integration.uimodel.Constants;
import com.nokia.ci.integration.uimodel.UrlConstants;
import com.thoughtworks.selenium.Selenium;
import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestName;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractIntegrationTest extends BaseHelper {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected static final String CI_BACKEND_UI_BASE_URL = "http://localhost:8888/";

    /* private static final String JQUERY_ACTIVE_CONNECTIONS_QUERY = "def wait_for_ajax(timeout=5000)\n" +
     "\n" +
     "js_condition = 'selenium.browserbot.getCurrentWindow().jQuery.active == 0;'\n" +
     "\n" +
     "$selenium.wait_for_condition(js_condition, timeout);\n" +
     "\n" +
     "end";*/
    private static final String JQUERY_ACTIVE_CONNECTIONS_QUERY = "return $.active == 0;";
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    public static final String SUREFIRE_REPORT_FOLDER = "./target/surefire-reports";
    private static final String PAGE_TIMING_XML_FILE = "pageload-timings.xml";
    private long pageLoadStartTime = 0;
    protected static final boolean INCREASING = true;
    protected static final boolean DECREASING = false;
    protected static String user = "";
    protected static String userRole = "";
    protected static WebDriver driver;
    protected static Selenium selenium;
    @Rule
    public ScreenshotRule screenshotRule = new ScreenshotRule();
    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void beforeClass() {
        WebDriverSingletonHolder instance = WebDriverSingletonHolder.getInstance();
        driver = instance.getDriver();

        // For testing purposes (http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/WebDriver.Timeouts.html):
        // driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        selenium = instance.getSelenium();
    }

    @Before
    public void before() {
        importData();
        initSurefireReportsFolder();
        log.info("Before testcase {}", this.getClass());
    }

    @After
    public void after() {
        log.info("After testcase {}", this.getClass());
    }

    private void importData() {
        try {
            DBConnector.executeSql(getSqlCommands());
        } catch (SQLException e) {
            log.error("Seems like you do not have H2 database running, exiting..");
            System.exit(1);
        }
    }

    private void initSurefireReportsFolder() {
        File folder = new File(System.getProperty("user.dir") + "/" + SUREFIRE_REPORT_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    protected List<String> getSqlCommands() {
        //file import.sql will be copied by maven build from s40-ci-backend-it module
        return DBConnector.getSqlCommandsFromFile("import.sql", false);
    }

    protected List<String> getTrunkSqlCommands() {
        return DBConnector.getSqlCommandsFromFile("/sql/trunk.sql", true);
    }

    public String getCurrentUser() {
        return user;
    }

    public String getCurrentRole() {
        return userRole;
    }

    protected void waitUntilElementExists(final String elementId) {
        new FluentWait<WebDriver>(driver).withTimeout(Constants.DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).ignoring(NoSuchElementException.class)
                .until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver wd) {
                WebElement element = wd.findElement(By.id(elementId));
                return element != null;
            }
        });
    }

    protected void waitUntilSubElementExists(final WebElement element, final By query) {
        new FluentWait<WebDriver>(driver).withTimeout(Constants.DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).ignoring(NoSuchElementException.class)
                .until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver wd) {
                WebElement subElement = element.findElement(query);
                return subElement != null;
            }
        });
    }

    /**
     * Use when element is on the page or will be on the page. Can be used
     * element is not on the page before the ajax call and will be on the page
     * after the ajax call
     *
     * @param elementId
     * @param value
     */
    protected void waitUntilElementGetsValue(final String elementId, final String value) {
        new FluentWait<WebDriver>(driver).withTimeout(Constants.DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).ignoring(NoSuchElementException.class)
                .until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver wd) {
                WebElement element = wd.findElement(By.id(elementId));
                return element.getText().equals(value);
            }
        });
    }

    /**
     * Use when element is already precisely on the page. Throws
     * NoSuchElementException when element is not found
     *
     * @param elementId
     * @param value
     */
    protected void waitUntilElementExistsAndGetsValue(final String elementId, final String value) {
        new FluentWait<WebDriver>(driver).withTimeout(Constants.DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver wd) {
                WebElement element = wd.findElement(By.id(elementId));
                return element.getText().equals(value);
            }
        });
    }

    protected void waitUntilAjaxRequestCompletes() {
        new FluentWait<WebDriver>(driver).withTimeout(Constants.DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                JavascriptExecutor jsExec = (JavascriptExecutor) d;
                return (Boolean) jsExec.executeScript(JQUERY_ACTIVE_CONNECTIONS_QUERY);
            }
        });
    }

    /**
     * Waits until body elements animated with JS.
     */
    protected void waitUntilAllAnimationsComplete() {
        waitUntilAnimationCompletes("body *");
    }

    /**
     * Waits until given selector elements animated with JS.
     *
     * @param selector : jQuery element selector
     */
    protected void waitUntilAnimationCompletes(final String selector) {
        new FluentWait<WebDriver>(driver).withTimeout(Constants.DEFAULT_TIMEOUT_IN_SECONDS * 2, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_ANIMATED_INTERVAL_IN_SECONDS, TimeUnit.MILLISECONDS)
                .until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return (Boolean) ((JavascriptExecutor) d).executeScript("return ! $('" + selector
                        + "').is(':animated');");
            }
        });
    }

    protected boolean isElementPresentByXpath(String xpath) {
        try {
            WebElement element = driver.findElement(By.xpath(xpath));
            return element != null;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static WebElement findElementByIdSuffix(String idSuffix) {
        return findElementByIdSuffix(driver, "*", idSuffix);
    }

    /* protected boolean isElementPresentByXpath(WebElement parent, String xpath) {
     try {
     WebElement element = parent.findElement(By.xpath(xpath));
     return element != null;
     } catch (NoSuchElementException e) {
     return false;
     }
     }*/
    protected WebElement findElementById(String elementId) {
        return driver.findElement(By.id(elementId));
    }

    protected WebElement findElementByLinkText(String linkText) {
        return driver.findElement(By.linkText(linkText));
    }

    protected WebElement findElementByLinkText(WebElement element, String linkText) {
        return element.findElement(By.linkText(linkText));
    }

    protected List<WebElement> findElementsById(String elementId) {
        return driver.findElements(By.id(elementId));
    }

    protected WebElement findElementByTag(String tagName) {
        return driver.findElement(By.tagName(tagName));
    }

    protected List<WebElement> findElementsByTag(String tagName) {
        return driver.findElements(By.tagName(tagName));
    }

    protected WebElement findElementByName(String elementName) {
        return driver.findElement(By.name(elementName));
    }

    protected List<WebElement> findElementsByName(String elementName) {
        return driver.findElements(By.name(elementName));
    }

    protected WebElement findElementByClass(String className) {
        return driver.findElement(By.className(className));
    }

    protected List<WebElement> findElementsByClass(String className) {
        return driver.findElements(By.className(className));
    }

    protected WebElement findElementByXpath(String path) {
        return driver.findElement(By.xpath(path));
    }

    protected List<WebElement> findElementsByXpath(String path) {
        return driver.findElements(By.xpath(path));
    }

    protected WebElement findElementByXpath(WebElement parent, String path) {
        return parent.findElement(By.xpath(path));
    }

    protected List<WebElement> findElementsByXpath(WebElement parent, String path) {
        return parent.findElements(By.xpath(path));
    }

    protected WebElement findElementBySelector(String selector) {
        return driver.findElement(By.cssSelector(selector));
    }

    protected List<WebElement> findElementsBySelector(String selector) {
        return driver.findElements(By.cssSelector(selector));
    }

    protected WebElement findElementBySelector(WebElement parent, String selector) {
        return parent.findElement(By.cssSelector(selector));
    }

    protected List<WebElement> findElementsBySelector(WebElement parent, String selector) {
        return parent.findElements(By.cssSelector(selector));
    }

    protected String toFullUrl(String url) {
        if (url.contains(CI_BACKEND_UI_BASE_URL)) {
            return url;
        }
        return CI_BACKEND_UI_BASE_URL + url;
    }

    protected String toRelativeUrl(String url) {
        if (!url.contains(CI_BACKEND_UI_BASE_URL)) {
            return url;
        }
        return url.replaceFirst(CI_BACKEND_UI_BASE_URL, "");
    }

    protected String escapeClientId(String id) {
        return "#" + id.replaceAll(":", "\\\\:");
    }

    protected String escapeJSId(String id) {
        return "#" + id.replaceAll(":", "\\\\\\\\:");
    }

    protected boolean hasClass(WebElement e, String c) {
        return e.getAttribute("class") != null && e.getAttribute("class").contains(c);
    }

    protected Object executeJS(String js, Object... os) {
        return ((JavascriptExecutor) driver).executeScript(js, os);
    }

    protected void waitForCondition(ExpectedCondition<Boolean> condition) {
        new FluentWait<WebDriver>(driver).withTimeout(Constants.DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).until(condition);
    }

    protected void waitUntilElementExists(final By by) {
        new FluentWait<WebDriver>(driver).withTimeout(Constants.DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).ignoring(NoSuchElementException.class)
                .until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver wd) {
                wd.findElement(by);
                return true;
            }
        });
    }

    protected Integer getAnimationQueueSizeBySelector(String selector, String queue) {
        return ((Long) executeJS("return $('" + selector + "').queue('" + queue + "').length;")).intValue();
    }

    protected Boolean anyAnimationInProgress(String selector, String queue) {
        return (Boolean) executeJS(" var q = $('" + selector + "').queue('" + queue
                + "'); return q.length && q[0] == 'inprogress';");
    }

    protected Boolean anyAnimationInProgress(String selector) {
        return (Boolean) executeJS(" var q = $('" + selector
                + "').queue(); return q.length != 0 && q[0] == 'inprogress';");
    }

    /**
     * Compares given css value before and after a delay time
     *
     * @param WebElement e : UI element to look for
     * @param String cssValue : Style property to compare
     * @param boolean increasing : Should increase or decrease
     * @param long interval : Time in milliseconds to look before and after
     */
    protected Boolean shouldElementAnimating(WebElement e, String cssValue, boolean increasing, long interval)
            throws InterruptedException {

        String initial = e.getCssValue(cssValue);

        Thread.sleep(interval);

        String after = e.getCssValue(cssValue);

        try {

            double init = Double.parseDouble(initial.replaceAll("px", "")), last = Double.parseDouble(after.replaceAll(
                    "px", "")), dif = last - init;

            return dif != 0 && (dif < 0) ^ increasing;

        } catch (NumberFormatException ex) {
            // No action. Try with string compare.
        }

        int diff = initial.compareToIgnoreCase(after);

        return diff != 0 && ((diff > 0) ^ increasing);
    }

    protected WebElement waitUntilElementExistsAndGet(WebElement element, By by) {
        return waitUntilElementExistsAndGet(element, by, 0);
    }

    protected WebElement waitUntilElementExistsAndGet(WebElement element, By by, int waitSecond) {
        WebElement item = null;
        if (element != null && by != null) {
            try {
                Thread.sleep(waitSecond * 1000L);
                item = element.findElement(by);
            } catch (Exception e) {
                return null;
            }
        }

        return item;
    }

    protected void selectElementByValue(WebElement element, String value) {
        new Select(element).selectByValue(value);
    }

    protected void clickToElementById(String elementId) {
        clickWithScroll(findElementById(elementId));
    }

    protected void clickToElementByClass(String elementClass) {
        clickWithScroll(findElementByClass(elementClass));
    }

    protected void waitMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    protected void waitForOneSecond() {
        waitForSeconds(1);
    }

    protected void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
        }
    }

    protected void scrollByOffset(int x, int y) {
        executeJS("window.scrollBy(" + x + "," + y + ")");
    }

    protected Boolean isTextPresent(WebElement element, String text) {
        return element.getText().contains(text);
    }

    protected Boolean isTextsPresent(WebElement element, List<String> strings) {
        String elementStr = element.getText();
        for (String aStr : strings) {
            if (!elementStr.contains(aStr)) {
                return false;
            }
        }
        return true;
    }

    protected void scrollToDefaults() {
        int width = WIDTH * -1;
        int height = HEIGHT * -1;
        executeJS("window.scrollBy(" + width + "," + height + ")");
    }

    protected void clickWithScroll(final WebElement element) {
        scrollToDefaults();
        int x = element.getLocation().getX();
        int y = element.getLocation().getY();
        scrollByOffset(x / 2, y / 2);
        element.click();
    }

    protected void clickAndWaitForNewPage(WebElement linkOrBtn, int timeoutInSeconds) {
        final String previousLocation = selenium.getLocation();

        // Wait until the link or button is visible
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.visibilityOf(linkOrBtn));

        startPageLoadTiming(null);
        linkOrBtn.click();
        new FluentWait<WebDriver>(driver).withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
                .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).ignoring(NoSuchElementException.class)
                .until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver wd) {
                String newLocation = selenium.getLocation();
                boolean hasLocationChanged = !previousLocation.equals(newLocation);
                return hasLocationChanged;
            }
        });
        waitUntilAjaxRequestCompletes();
        waitUntilAllAnimationsComplete();
        finishPageLoadTiming(selenium.getLocation());
    }

    private void startPageLoadTiming(String url) {
        log.info("Starting page load timing: {}", url);
        pageLoadStartTime = System.currentTimeMillis();
    }

    private void finishPageLoadTiming(String url) {
        long loadTime = System.currentTimeMillis() - pageLoadStartTime;
        log.info("Finished page '{}' load timing in {}ms", url, loadTime);

        File reportFile = new File(System.getProperty("user.dir") + "/" + SUREFIRE_REPORT_FOLDER + "/" + PAGE_TIMING_XML_FILE);

        // report
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc;

            if (!reportFile.exists()) {
                doc = docBuilder.newDocument();
            } else {
                doc = docBuilder.parse(reportFile);
            }

            Element root = doc.getDocumentElement();
            if (root == null) {
                root = doc.createElement("timings");
                root.setAttribute("date", new Date().toString());
                doc.appendChild(root);
            }

            Element timing = doc.createElement("timing");
            timing.setAttribute("testClass", this.getClass().toString());
            timing.setAttribute("testName", name.getMethodName());
            if (StringUtils.isNotEmpty(url)) {
                timing.setAttribute("url", toRelativeUrl(url));
            }
            if (StringUtils.isNotEmpty(user)) {
                timing.setAttribute("testUser", user);
            }
            if (StringUtils.isNotEmpty(userRole)) {
                timing.setAttribute("testUserRole", userRole);
            }
            timing.setAttribute("time", String.valueOf(loadTime));
            root.appendChild(timing);

            // Save to xml
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult res = new StreamResult(reportFile);

            log.info("Writing page '{}' load timing to {}", url, reportFile.getAbsolutePath());
            transformer.transform(source, res);

        } catch (Exception e) {
            log.error("Could not save page load timings", e);
        }
    }

    protected void navigate(String url) {
        navigate(url, true);
    }

    protected void navigate(String url, boolean waitForJs) {
        log.info("Navigating to {}", url);
        startPageLoadTiming(url);

        driver.get(toFullUrl(url));

        if (waitForJs) {
            waitUntilAjaxRequestCompletes();
            waitUntilAllAnimationsComplete();
        }

        finishPageLoadTiming(url);
    }

    protected void loadPage(String url) throws UnauthorizedException, UnexpectedException {
        loadPage(url, Constants.DEFAULT_TIMEOUT_IN_SECONDS, true);
    }

    protected void loadPage(String url, boolean waitForJs) throws UnauthorizedException, UnexpectedException {
        loadPage(url, Constants.DEFAULT_TIMEOUT_IN_SECONDS, waitForJs);
    }

    protected void loadPage(String url, int timeoutInSeconds) throws UnauthorizedException, UnexpectedException {
        loadPage(url, timeoutInSeconds, true);
    }

    protected void loadPage(String url, int timeoutInSeconds, boolean waitForJs) throws UnauthorizedException, UnexpectedException {
        final String previousLocation = selenium.getLocation();

        log.info("Loading page: {}, previous location: {}", url, previousLocation);

        startPageLoadTiming(url);
        driver.get(toFullUrl(url));

        if (!previousLocation.equals(url)) {
            new FluentWait<WebDriver>(driver).withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
                    .pollingEvery(Constants.DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS).ignoring(NoSuchElementException.class)
                    .until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver wd) {
                    String newLocation = selenium.getLocation();
                    boolean hasLocationChanged = !previousLocation.equals(newLocation);
                    boolean isErrorPage = !findElementsById("errorDiv").isEmpty();

                    if (hasLocationChanged || isErrorPage) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        if (waitForJs) {
            waitUntilAjaxRequestCompletes();
            waitUntilAllAnimationsComplete();
        }

        finishPageLoadTiming(url);

        boolean isErrorPage = !findElementsById("errorDiv").isEmpty();
        boolean isLoginPage = !findElementsById("login").isEmpty();

        if (isErrorPage) {
            boolean isUnexpectedErrorPageOpened = driver.getCurrentUrl().contains(UrlConstants.UNEXPECTEDERROR);
            boolean isUnauthorizedPageLoaded = driver.getCurrentUrl().contains(UrlConstants.UNAUTHORIZED);
            boolean pageNotFound = driver.getCurrentUrl().contains(UrlConstants.NOTFOUND);
            boolean pageContainsTargetURL = driver.getCurrentUrl().contains(url);

            if (isUnauthorizedPageLoaded || pageContainsTargetURL) {
                throw new UnauthorizedException("User could not access unauthorized page '" + url + "'");
            } else if (isUnexpectedErrorPageOpened) {
                throw new UnexpectedException("There was a problem while loading page '" + url + "'. Please check what is the problem and create a bugzilla report.");
            } else if (pageNotFound) {
                throw new UnexpectedException("Could not access page '" + url + "' because the page does not exist.");
            } else {
                throw new UnexpectedException("Could not find a specific problem for page '" + url + "'. Maybe the page does not exist.");
            }
        } else if (isLoginPage) {
            finishPageLoadTiming(url);
            throw new UnauthorizedException("User could not access unauthorized page '" + url + "'. A login is needed");
        }
    }

    protected void clickAndWaitForNewPage(WebElement linkOrBtn) {
        clickAndWaitForNewPage(linkOrBtn, Constants.DEFAULT_TIMEOUT_IN_SECONDS);
    }

    public class ScreenshotRule implements MethodRule {

        @Override
        public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object o) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        statement.evaluate();
                    } catch (Throwable t) {
                        String testName = frameworkMethod.getMethod().getDeclaringClass().getName() + "-" + frameworkMethod.getName();
                        log.info("Capturing screenshot of failed testcase {}", testName);
                        captureScreenshot(testName);
                        throw t;
                    }
                }

                public void captureScreenshot(String fileName) {
                    try {
                        File file = new File(System.getProperty("user.dir") + "/" + SUREFIRE_REPORT_FOLDER + "/screenshot-" + fileName + ".png");
                        File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                        FileUtils.moveFile(scr, file);
                    } catch (Exception e) {
                        log.warn("Could not take screenshot of failing test case to surefire-reports/screenshot-" + fileName + ".png");
                    }
                }
            };
        }
    }
}
