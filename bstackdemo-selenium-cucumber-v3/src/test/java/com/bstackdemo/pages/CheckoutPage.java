package com.bstackdemo.pages;

import com.bstackdemo.base.DriverFactory;
import com.bstackdemo.utils.ConfigReader;
import com.bstackdemo.utils.ElementUtil;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class CheckoutPage {

    private final ElementUtil util = new ElementUtil();

    private final By usernameInput =
            By.id("react-select-2-input");

    private final By firstNameInput =
            By.id("firstNameInput");

    private final By lastNameInput =
            By.id("lastNameInput");

    private final By addressInput =
            By.id("addressLine1Input");

    private final By stateInput =
            By.id("provinceInput");

    private final By postalCodeInput =
            By.id("postCodeInput");

    private final By validationMessages =
            By.cssSelector(
                    ".error, " +
                    ".api-error, " +
                    "[role='alert'], " +
                    ".invalid-feedback"
            );

    private final By confirmationMessage =
            By.id("confirmation-message");

    private final By downloadReceiptLink =
            By.id("downloadpdf");


    private WebDriver driver() {

        return DriverFactory.getDriver();
    }


    // =========================================================
    // Check whether checkout page is displayed
    // =========================================================

    public boolean isCheckoutPageDisplayed() {

        String source =
                driver()
                        .getPageSource()
                        .toLowerCase(Locale.ROOT);

        return source.contains("checkout")
                || source.contains("shipping")
                || util.exists(firstNameInput)
                || util.exists(usernameInput);
    }


    // =========================================================
    // Check checkout summary
    // =========================================================

    public boolean isCheckoutSummaryVisible(
            String selectedProductName) {

        String source =
                driver()
                        .getPageSource()
                        .toLowerCase(Locale.ROOT);

        boolean checkoutContentVisible =
                source.contains("summary")
                        || source.contains("checkout");

        boolean productVisible = false;

        if (selectedProductName != null
                && !selectedProductName.isBlank()) {

            productVisible =
                    source.contains(
                            selectedProductName
                                    .toLowerCase(Locale.ROOT)
                    );
        }

        return checkoutContentVisible
                || productVisible;
    }


    // =========================================================
    // Enter valid shipping details from config.properties
    // =========================================================

    public void enterValidShippingDetails() {

        enterShippingDetails(

                ConfigReader.get("validFirstName"),

                ConfigReader.get("validLastName"),

                ConfigReader.get("validAddress"),

                ConfigReader.get("validState"),

                ConfigReader.get("validPincode")
        );
    }


    // =========================================================
    // Enter shipping details
    // =========================================================

    public void enterShippingDetails(
            String firstName,
            String lastName,
            String address,
            String state,
            String pincode) {

        typeIfPresent(
                firstNameInput,
                firstName
        );

        typeIfPresent(
                lastNameInput,
                lastName
        );

        typeIfPresent(
                addressInput,
                address
        );

        typeIfPresent(
                stateInput,
                state
        );

        typeIfPresent(
                postalCodeInput,
                pincode
        );
    }


    // =========================================================
    // Submit checkout by keeping one field blank
    // =========================================================

    public void submitCheckoutWithMissingField(
            String fieldName) {

        enterValidShippingDetails();

        String field =
                fieldName.toLowerCase(Locale.ROOT);

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


    // =========================================================
    // Click checkout submit button
    // =========================================================

    public void submitCheckout() {

        clickFirstAvailable(

                By.id(
                        "checkout-shipping-continue"
                ),

                By.cssSelector(
                        "button[type='submit']"
                ),

                By.xpath(
                        "//button[" +
                                "contains(normalize-space(),'Continue') " +
                                "or contains(normalize-space(),'Submit') " +
                                "or contains(normalize-space(),'Place Order')" +
                                "]"
                )
        );

        sleep(1300);
    }


    // =========================================================
    // Check checkout validation
    // =========================================================

    public boolean isValidationMessageDisplayed() {

        boolean html5Invalid =
                hasHtml5InvalidField();

        boolean visibleError =
                hasVisibleValidationMessage();

        String source =
                driver()
                        .getPageSource()
                        .toLowerCase(Locale.ROOT);

        boolean validationTextPresent =
                source.contains("required")
                        || source.contains("invalid")
                        || source.contains("error")
                        || source.contains("enter valid");

        return html5Invalid
                || visibleError
                || validationTextPresent;
    }


    // =========================================================
    // Check browser HTML5 validation
    // =========================================================

    private boolean hasHtml5InvalidField() {

        try {

            return Boolean.TRUE.equals(

                    ((JavascriptExecutor) driver())
                            .executeScript(

                                    "return document.querySelectorAll(" +
                                            "'input:invalid, " +
                                            "textarea:invalid, " +
                                            "select:invalid'" +
                                            ").length > 0;"
                            )
            );

        } catch (RuntimeException e) {

            return false;
        }
    }


    // =========================================================
    // Check visible error message
    // =========================================================

    private boolean hasVisibleValidationMessage() {

        List<WebElement> messages =
                driver()
                        .findElements(validationMessages);

        for (WebElement message : messages) {

            try {

                if (message.isDisplayed()
                        && !message
                                .getText()
                                .trim()
                                .isBlank()) {

                    return true;
                }

            } catch (RuntimeException ignored) {

            }
        }

        return false;
    }


    // =========================================================
    // Check order confirmation
    // =========================================================

    public boolean isOrderConfirmationDisplayed() {

        try {

            return new WebDriverWait(
                    driver(),
                    Duration.ofSeconds(
                            ConfigReader.timeout()
                    )
            ).until(d -> {

                String url =
                        d.getCurrentUrl()
                                .toLowerCase(Locale.ROOT);

                if (!url.contains("/confirmation")) {

                    return false;
                }

                List<WebElement> messages =
                        d.findElements(
                                confirmationMessage
                        );

                return !messages.isEmpty()
                        && messages.get(0).isDisplayed()
                        && messages
                                .get(0)
                                .getText()
                                .toLowerCase(Locale.ROOT)
                                .contains(
                                        "successfully placed"
                                );
            });

        } catch (TimeoutException e) {

            return false;
        }
    }


    // =========================================================
    // Check confirmation page details
    // =========================================================

    public boolean hasConfirmationPageDetails() {

        if (!isOrderConfirmationDisplayed()) {

            return false;
        }

        String bodyText =
                driver()
                        .findElement(
                                By.tagName("body")
                        )
                        .getText()
                        .toLowerCase(Locale.ROOT);

        return bodyText.contains(
                "your order number is"
        )
                && util.exists(
                        downloadReceiptLink
                );
    }


    // =========================================================
    // Clear a checkout field
    // =========================================================

    private void clearField(By locator) {

        if (util.exists(locator)) {

            WebElement element =
                    util.visible(locator);

            element.sendKeys(
                    Keys.chord(
                            Keys.CONTROL,
                            "a"
                    )
            );

            element.sendKeys(
                    Keys.BACK_SPACE
            );
        }
    }


    // =========================================================
    // Enter value only when field exists
    // =========================================================

    private void typeIfPresent(
            By locator,
            String value) {

        if (util.exists(locator)) {

            util.type(
                    locator,
                    value == null
                            ? ""
                            : value
            );
        }
    }


    // =========================================================
    // Click first available button
    // =========================================================

    private void clickFirstAvailable(
            By... locators) {

        for (By locator : locators) {

            if (util.exists(locator)) {

                util.click(locator);

                return;
            }
        }

        throw new NoSuchElementException(
                "No checkout submit button was found."
        );
    }


    // =========================================================
    // Simple sleep method
    // =========================================================

    private void sleep(long milliseconds) {

        try {

            Thread.sleep(milliseconds);

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }
    }
}