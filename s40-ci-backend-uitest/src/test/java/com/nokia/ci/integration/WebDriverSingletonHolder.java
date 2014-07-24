package com.nokia.ci.integration;

import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 1/4/13
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class WebDriverSingletonHolder {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    private WebDriver driver;
    private Selenium selenium;
    private static WebDriverSingletonHolder webDriverSingletonHolder;


    private WebDriverSingletonHolder() {
        init();
    }

    public static WebDriverSingletonHolder getInstance() {
        if (webDriverSingletonHolder == null) {
            webDriverSingletonHolder = new WebDriverSingletonHolder();
        }
        return webDriverSingletonHolder;
    }


    public Selenium getSelenium() {
        return selenium;
    }

    public WebDriver getDriver() {
        return driver;
    }


    private void init() {
        driver = createDriver();
        selenium = new WebDriverBackedSelenium(driver, AbstractIntegrationTest.CI_BACKEND_UI_BASE_URL);
        selenium.getEval("window.resizeTo(" + WIDTH + "," + HEIGHT + "); window.moveTo(0,0);");
    }

    private WebDriver createDriver() {

        String type = System.getProperty("integrationTestsDriverType");
        String path = System.getProperty("integrationTestsDriverPath");

        if (type != null) {
            if (type.equalsIgnoreCase("chrome")) {

                System.setProperty("webdriver.chrome.driver", path);

                return new ChromeDriver();
            } else if (type.equalsIgnoreCase("win32ie")) {
                DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
                ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                return new InternetExplorerDriver(ieCapabilities);
            } else if (type.equalsIgnoreCase("xvfb")) {
                String X_PORT = System.getProperty("lmportal.xvfb.id", ":20");
                String FIREFOX_PATH = System.getProperty("lmportal.deploy.firefox.path", "/var/tmp/firefox3.6.24/firefox");
                FirefoxBinary firefoxBinary = new FirefoxBinary(new File(FIREFOX_PATH));
                firefoxBinary.setEnvironmentProperty("DISPLAY", X_PORT);
                return new FirefoxDriver(firefoxBinary, null);
            }
        }

        FirefoxDriver ff = new FirefoxDriver(prepareFirefoxProfileForFileDownload());
        return ff;
    }

    private static FirefoxProfile prepareFirefoxProfileForFileDownload() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "image/jpg, text/csv,text/xml,application/xml,application/vnd.ms-excel,application/x-excel,application/x-msexcel,application/excel,application/pdf");
        profile.setPreference("browser.download.dir", System.getProperty("user.home"));
        profile.setPreference("toolkit.telemetry.rejected", true);
        profile.setPreference("toolkit.telemetry.enabled", false);
        return profile;
    }
}
