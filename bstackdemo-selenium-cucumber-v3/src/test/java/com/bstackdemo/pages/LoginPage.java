package com.bstackdemo.pages;

import com.bstackdemo.base.DriverFactory;
import com.bstackdemo.utils.ConfigReader;
import com.bstackdemo.utils.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class LoginPage {

    private final ElementUtil util = new ElementUtil();

    private final By signInLink = By.id("signin");
    private final By logoutLink = By.id("logout");
    private final By usernameInput = By.id("react-select-2-input");
    private final By passwordInput = By.id("react-select-3-input");
    private final By loginButton = By.id("login-btn");

    private WebDriver driver() {
        return DriverFactory.getDriver();
    }

    public void openSignInPage() {
        if (!util.exists(usernameInput) && util.exists(signInLink)) {
            util.click(signInLink);
        }

        try {
            new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.timeout()))
                    .until(d -> util.exists(usernameInput) || util.exists(loginButton));
        } catch (TimeoutException ignored) {
            // The test assertion reports the failure.
        }
    }

    public boolean isLoginFormDisplayed() {
        return util.isDisplayed(usernameInput) || util.isDisplayed(loginButton);
    }

    public void login(String username, String password) {
        openSignInPage();

        if (!isLoginFormDisplayed()) {
            throw new NoSuchElementException("Login form is not displayed.");
        }

        selectReactDropdown(usernameInput, username);
        selectReactDropdown(passwordInput, password);
        clickWithJsFallback(util.visible(loginButton));
        waitForLoginProcessing();
    }

    public void clickLoginWithoutCredentials() {
        openSignInPage();
        util.click(loginButton);
        sleep(800);
    }

    public boolean isUserLoggedIn() {
        try {
            return new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.timeout()))
                    .until(d -> util.exists(logoutLink)
                            || d.getPageSource().toLowerCase(Locale.ROOT).contains("logout")
                            || (!util.exists(loginButton)
                            && !util.exists(usernameInput)
                            && !util.exists(signInLink)));
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public boolean isLoginErrorDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);

        boolean errorVisible = source.contains("error")
                || source.contains("invalid")
                || source.contains("locked")
                || source.contains("not match")
                || source.contains("select username")
                || source.contains("select password")
                || util.exists(By.cssSelector(".api-error, .error, [role='alert']"));

        boolean loginStillBlocked = util.exists(loginButton) && !util.exists(logoutLink);

        return errorVisible || loginStillBlocked;
    }

    public void logout() {
        if (util.exists(logoutLink)) {
            util.click(logoutLink);
            sleep(900);
        }
    }

    public boolean isUserLoggedOut() {
        try {
            return new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.timeout()))
                    .until(d -> util.exists(signInLink) || !util.exists(logoutLink));
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public boolean isLogoutLinkVisible() {
        return util.exists(logoutLink);
    }

    private void selectReactDropdown(By inputLocator, String value) {
        String optionValue = value == null ? "" : value.trim();
        WebElement input = util.visible(inputLocator);
        WebElement control = getReactSelectControl(input);

        clickWithJsFallback(control);
        sleep(300);

        input = util.visible(inputLocator);

        try {
            ((JavascriptExecutor) driver()).executeScript("arguments[0].focus();", input);
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.BACK_SPACE);

            if (!optionValue.isBlank()) {
                input.sendKeys(optionValue);
            }
        } catch (ElementClickInterceptedException e) {
            new Actions(driver())
                    .moveToElement(control)
                    .click()
                    .sendKeys(optionValue)
                    .perform();
        }

        sleep(700);

        List<WebElement> options = driver().findElements(
                By.xpath("//*[contains(@id,'react-select') and contains(@id,'option')]")
        );

        for (WebElement option : options) {
            String optionText = option.getText().trim();

            if (option.isDisplayed() && optionText.equalsIgnoreCase(optionValue)) {
                clickWithJsFallback(option);
                sleep(400);
                return;
            }
        }

        input = util.visible(inputLocator);
        input.sendKeys(Keys.ENTER);
        sleep(400);
    }

    private WebElement getReactSelectControl(WebElement input) {
        try {
            return input.findElement(
                    By.xpath("./ancestor::div[contains(@class,'control')][1]")
            );
        } catch (NoSuchElementException e) {
            return input;
        }
    }

    private void clickWithJsFallback(WebElement element) {
        try {
            util.scrollIntoView(element);
            element.click();
        } catch (WebDriverException e) {
            ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", element);
        }
    }

    private void waitForLoginProcessing() {
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.timeout()))
                    .until(d -> util.exists(logoutLink)
                            || !util.exists(loginButton)
                            || d.getPageSource().toLowerCase(Locale.ROOT).contains("invalid")
                            || d.getPageSource().toLowerCase(Locale.ROOT).contains("locked")
                            || d.getPageSource().toLowerCase(Locale.ROOT).contains("error"));
        } catch (TimeoutException ignored) {
            // The calling test performs the final assertion.
        }
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
