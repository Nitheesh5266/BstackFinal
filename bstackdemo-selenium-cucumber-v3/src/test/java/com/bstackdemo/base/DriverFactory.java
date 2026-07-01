package com.bstackdemo.base;

import com.bstackdemo.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.Locale;

public final class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<String> BROWSER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static WebDriver initDriver(String browserName) {
        String browser = browserName == null || browserName.isBlank()
                ? ConfigReader.get("defaultBrowser", "chrome")
                : browserName.toLowerCase(Locale.ROOT);

        BROWSER.set(browser);
        boolean headless = ConfigReader.headless();

        switch (browser) {
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--start-maximized", "--remote-allow-origins=*");
                if (headless) {
                    edgeOptions.addArguments("--headless=new");
                }
                DRIVER.set(new EdgeDriver(edgeOptions));
                break;

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }
                DRIVER.set(new FirefoxDriver(firefoxOptions));
                break;

            case "chrome":
            default:
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized", "--remote-allow-origins=*");
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                DRIVER.set(new ChromeDriver(chromeOptions));
                break;
        }

        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.timeout() + 15L));
        getDriver().manage().window().maximize();
        return getDriver();
    }

    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    public static String getBrowser() {
        return BROWSER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
            BROWSER.remove();
        }
    }
}
