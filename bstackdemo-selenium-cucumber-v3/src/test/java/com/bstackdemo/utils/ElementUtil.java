package com.bstackdemo.utils;

import com.bstackdemo.base.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class ElementUtil {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public ElementUtil() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.timeout()));
    }

    public WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public List<WebElement> all(By locator) {
        waitForPageReady();
        return new ArrayList<>(driver.findElements(locator));
    }

    public boolean exists(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (StaleElementReferenceException e) {
            return !driver.findElements(locator).isEmpty();
        }
    }

    public boolean isDisplayed(By locator) {
        try {
            return visible(locator).isDisplayed();
        } catch (NoSuchElementException | TimeoutException e) {
            return false;
        }
    }

    public void click(By locator) {
        WebElement element = clickable(locator);
        scrollIntoView(element);

        try {
            element.click();
        } catch (WebDriverException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    public void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        scrollIntoView(element);

        try {
            element.click();
        } catch (WebDriverException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    public void type(By locator, String value) {
        WebElement element = visible(locator);
        scrollIntoView(element);
        element.clear();
        element.sendKeys(value == null ? "" : value);
    }

    public String text(By locator) {
        return visible(locator).getText().trim();
    }

    public void waitForPageReady() {
        ExpectedCondition<Boolean> condition = driver -> {
            if (driver == null) {
                return false;
            }

            Object result = ((JavascriptExecutor) driver).executeScript("return document.readyState");

            return "complete".equals(result) || "interactive".equals(result);
        };

        wait.until(condition);
    }

    public void waitForAtLeastOne(By locator) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    public void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                element
        );
    }

    public void clickFirstAvailable(By... locators) {
        for (By locator : locators) {
            if (exists(locator)) {
                click(locator);
                return;
            }
        }

        throw new NoSuchElementException("None of the clickable locators were found.");
    }
}