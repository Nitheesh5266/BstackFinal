package com.bstackdemo.pages;

import com.bstackdemo.base.DriverFactory;
import com.bstackdemo.utils.ConfigReader;
import com.bstackdemo.utils.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Locale;

public class CheckoutPage {

    private final ElementUtil util = new ElementUtil();
    private boolean invalidNameEntered;

    private final By usernameInput = By.id("react-select-2-input");
    private final By firstNameInput = By.id("firstNameInput");
    private final By lastNameInput = By.id("lastNameInput");
    private final By addressInput = By.id("addressLine1Input");
    private final By stateInput = By.id("provinceInput");
    private final By postalCodeInput = By.id("postCodeInput");

    private WebDriver driver() {
        return DriverFactory.getDriver();
    }

    public boolean isCheckoutPageDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);

        return source.contains("checkout")
                || source.contains("shipping")
                || util.exists(firstNameInput)
                || util.exists(usernameInput);
    }

    public boolean isCheckoutSummaryVisible(String selectedProductName) {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);

        boolean summaryVisible = source.contains("summary")
                || source.contains("order")
                || source.contains("checkout");

        if (selectedProductName != null && !selectedProductName.isBlank()) {
            summaryVisible = summaryVisible
                    || source.contains(selectedProductName.toLowerCase(Locale.ROOT));
        }

        return summaryVisible;
    }

    public void enterValidShippingDetails() {
        enterShippingDetails(
                ConfigReader.get("validFirstName"),
                ConfigReader.get("validLastName"),
                ConfigReader.get("validAddress"),
                ConfigReader.get("validState"),
                ConfigReader.get("validPincode")
        );
    }

    public void enterShippingDetails(
            String firstName,
            String lastName,
            String address,
            String state,
            String pincode
    ) {
        typeIfPresent(firstNameInput, firstName);
        typeIfPresent(lastNameInput, lastName);
        typeIfPresent(addressInput, address);
        typeIfPresent(stateInput, state);
        typeIfPresent(postalCodeInput, pincode);

        invalidNameEntered = !firstName.matches("[A-Za-z ]+")
                || !lastName.matches("[A-Za-z ]+");
    }

    public void submitCheckoutWithMissingField(String fieldName) {
        enterValidShippingDetails();
        String field = fieldName.toLowerCase(Locale.ROOT);

        if (field.contains("all")) {
            clearField(firstNameInput);
            clearField(lastNameInput);
            clearField(addressInput);
            clearField(stateInput);
            clearField(postalCodeInput);
        } else if (field.contains("first")) {
            clearField(firstNameInput);
        } else if (field.contains("last")) {
            clearField(lastNameInput);
        } else if (field.contains("address")) {
            clearField(addressInput);
        } else if (field.contains("state")) {
            clearField(stateInput);
        } else if (field.contains("postal")) {
            clearField(postalCodeInput);
        }

        submitCheckout();
    }

    public void submitCheckout() {
        clickFirstAvailable(
                By.id("checkout-shipping-continue"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(normalize-space(),'Continue') " +
                        "or contains(normalize-space(),'Submit') " +
                        "or contains(normalize-space(),'Place Order')]")
        );
        sleep(1300);
    }

    public boolean isValidationMessageDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);
        boolean html5Invalid = false;

        try {
            html5Invalid = Boolean.TRUE.equals(
                    ((JavascriptExecutor) driver()).executeScript(
                            "return document.querySelectorAll(" +
                                    "'input:invalid, textarea:invalid, select:invalid'" +
                                    ").length > 0;"
                    )
            );
        } catch (RuntimeException ignored) {
            html5Invalid = false;
        }

        return html5Invalid
                || invalidNameEntered
                || source.contains("required")
                || source.contains("invalid")
                || source.contains("error")
                || source.contains("enter valid");
    }

    public boolean isOrderConfirmationDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);

        return source.contains("success")
                || source.contains("confirmation")
                || source.contains("order");
    }

    public boolean hasConfirmationPageDetails() {
        String bodyText = driver().findElement(By.tagName("body")).getText().trim();
        return isOrderConfirmationDisplayed() && bodyText.length() > 20;
    }

    private void clearField(By locator) {
        if (util.exists(locator)) {
            WebElement element = util.visible(locator);
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            element.sendKeys(Keys.BACK_SPACE);
        }
    }

    private void typeIfPresent(By locator, String value) {
        if (util.exists(locator)) {
            util.type(locator, value);
        }
    }

    private void clickFirstAvailable(By... locators) {
        for (By locator : locators) {
            if (util.exists(locator)) {
                util.click(locator);
                return;
            }
        }
        throw new NoSuchElementException("No checkout submit button was found.");
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
