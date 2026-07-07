package com.bstackdemo.pages;

import com.bstackdemo.base.DriverFactory;
import com.bstackdemo.utils.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CartPage {

    private final ElementUtil util = new ElementUtil();

    private final By cartIcon = By.cssSelector(".bag, .bag__quantity, [class*='cart']");
    private final By cartPanel = By.cssSelector(".float-cart");
    private final By cartItems = By.cssSelector(
            ".float-cart .shelf-item, " +
                    ".float-cart__shelf-container .shelf-item, " +
                    ".shelf-item__details"
    );
    private final By cartQuantity = By.cssSelector(".bag__quantity");
    private final By cartTotal = By.cssSelector(".sub-price__val, .sub-price, .float-cart__summary");
    private final By checkoutButton = By.cssSelector(".buy-btn, .checkout-btn");
    private final By cartCloseButton = By.cssSelector(".float-cart__close-btn");

    private WebDriver driver() {
        return DriverFactory.getDriver();
    }

    public void openCart() {
        if (!util.isDisplayed(cartPanel)) {
            util.click(cartIcon);
            sleep(600);
        }
    }

    public boolean isCartPanelDisplayed() {
        return util.isDisplayed(cartPanel);
    }

    public boolean hasItems() {
        return util.exists(cartPanel) && !util.all(cartItems).isEmpty();
    }

    public int getCartCount() {
        if (!util.exists(cartQuantity)) {
            return 0;
        }

        String count = driver()
                .findElement(cartQuantity)
                .getText()
                .replaceAll("[^0-9]", "");

        return count.isBlank() ? 0 : Integer.parseInt(count);
    }

    public double getCartTotal() {
        List<WebElement> totals = driver().findElements(cartTotal);

        for (int i = totals.size() - 1; i >= 0; i--) {
            WebElement total = totals.get(i);

            if (total.isDisplayed()) {
                double parsed = parseAmount(total.getText());
                if (parsed > 0) {
                    return parsed;
                }
            }
        }
        return 0.0;
    }

    public void increaseProductQuantity() {
        clickFirstAvailable(
                By.xpath("//button[normalize-space()='+']"),
                By.xpath("//*[contains(@class,'change-product-button') and contains(normalize-space(),'+')]"),
                By.cssSelector("button[title*='increase'], .plus")
        );
        sleep(800);
    }

    public void decreaseProductQuantity() {
        clickFirstAvailable(
                By.xpath("//button[normalize-space()='-']"),
                By.xpath("//*[contains(@class,'change-product-button') and contains(normalize-space(),'-')]"),
                By.cssSelector("button[title*='decrease'], .minus")
        );
        sleep(800);
    }

    public void removeProductFromCart() {
        clickFirstAvailable(
                By.cssSelector(".shelf-item__del"),
                By.cssSelector(".remove, [title*='remove']"),
                By.xpath("//*[contains(@class,'delete') or contains(@class,'remove')]")
        );
        sleep(1000);
    }

    public boolean isEmptyCartDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);

        return source.contains("add some products")
                || source.contains("empty")
                || getCartCount() == 0
                || util.all(cartItems).isEmpty();
    }

    public boolean containsProduct(String productName) {
        if (productName == null || productName.isBlank()) {
            return false;
        }

        String pageText = driver().getPageSource().toLowerCase(Locale.ROOT);
        return pageText.contains(productName.toLowerCase(Locale.ROOT));
    }

    public boolean isCheckoutEnabled() {
        try {
            return util.exists(checkoutButton) && util.visible(checkoutButton).isEnabled();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void proceedToCheckout() {
        openCart();
        util.click(checkoutButton);
        sleep(1300);
    }

    public void closeCartIfBlockingPage() {
        try {
            List<WebElement> closeButtons = driver().findElements(cartCloseButton);

            if (!closeButtons.isEmpty() && closeButtons.get(0).isDisplayed()) {
                closeButtons.get(0).click();
                sleep(600);
            }
        } catch (NoSuchElementException | StaleElementReferenceException ignored) {
            
        }
    }

    private void clickFirstAvailable(By... locators) {
        for (By locator : locators) {
            if (util.exists(locator)) {
                util.click(locator);
                return;
            }
        }
        throw new NoSuchElementException("No matching element was found for cart action.");
    }

    private double parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }

        List<Double> values = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+(?:\\.\\d+)?")
                .matcher(text.replace(",", ""));

        while (matcher.find()) {
            values.add(Double.parseDouble(matcher.group()));
        }

        return values.isEmpty() ? 0.0 : values.get(values.size() - 1);
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
