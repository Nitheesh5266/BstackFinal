package com.bstackdemo.hooks;

import com.bstackdemo.base.DriverFactory;
import com.bstackdemo.utils.ScreenshotUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.util.Collection;
import java.util.Set;

public class Hooks {

    @Before(order = 0)
    public void invokeBrowserBasedOnScenario(Scenario scenario) {
        String browser = resolveBrowserFromScenarioTags(scenario.getSourceTagNames());
        DriverFactory.initDriver(browser);
        System.out.println("Invoked browser for scenario: " + scenario.getName() + " -> " + browser);
    }

    @After(order = 0)
    public void quitBrowserAfterScenario(Scenario scenario) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver != null && scenario.isFailed()) {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", scenario.getName());
            System.out.println("Screenshot saved at: " + ScreenshotUtil.capture(scenario.getName()));
        }
        DriverFactory.quitDriver();
    }

    private String resolveBrowserFromScenarioTags(Collection<String> collection) {
        if (collection.contains("@edge")) {
            return "edge";
        }
        if (collection.contains("@firefox")) {
            return "firefox";
        }
        if (collection.contains("@chrome")) {
            return "chrome";
        }
        return System.getProperty("browser", "chrome");
    }
}
