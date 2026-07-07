package com.bstackdemo.pages;

import com.bstackdemo.base.DriverFactory;
import com.bstackdemo.utils.ConfigReader;
import com.bstackdemo.utils.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HomePage {

    private final ElementUtil util = new ElementUtil();

    private String selectedProductName;
    private String selectedProductPrice;

    private final By productCards = By.cssSelector(".shelf-item");
    private final By productImages = By.cssSelector(".shelf-item img");
    private final By addToCartButtons = By.cssSelector(".shelf-item__buy-btn");
    private final By cartIcon = By.cssSelector(".bag, .bag__quantity, [class*='cart']");
    private final By signInLink = By.id("signin");
    private final By logoutLink = By.id("logout");
    private final By cartCloseButton = By.cssSelector(".float-cart__close-btn");

    private WebDriver driver() {
        return DriverFactory.getDriver();
    }

    public void openHome() {
        driver().get(ConfigReader.get("baseUrl"));
        util.waitForPageReady();
        util.waitForAtLeastOne(productCards);
    }

    public void openHomeClean() {
        driver().manage().window().maximize();
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                driver().manage().deleteAllCookies();
                driver().get(ConfigReader.get("baseUrl"));
                util.waitForPageReady();

                try {
                    ((JavascriptExecutor) driver()).executeScript(
                            "try { window.localStorage.clear(); window.sessionStorage.clear(); } catch(e) {}"
                    );
                } catch (RuntimeException ignored) {
                    // Continue with navigation retry.
                }

                driver().get(ConfigReader.get("baseUrl"));
                util.waitForPageReady();
                closeCartIfBlockingPage();
                util.waitForAtLeastOne(productCards);
                return;
            } catch (RuntimeException e) {
                lastError = e;
                sleep(1000);
            }
        }

        throw lastError == null
                ? new TimeoutException("Unable to load BStackDemo product grid.")
                : lastError;
    }

    public boolean isRegistrationAvailable() {
        By registrationLinks = By.xpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'register') " +
                        "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign up') " +
                        "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create account')]"
        );
        return !driver().findElements(registrationLinks).isEmpty();
    }

    public boolean isHomePageDisplayed() {
        return driver().getCurrentUrl().toLowerCase(Locale.ROOT).contains("bstackdemo")
                && getProductCount() > 0;
    }

    public boolean areHeaderAndNavigationElementsDisplayed() {
        boolean accountNavigationVisible = util.exists(signInLink) || util.exists(logoutLink);
        return accountNavigationVisible && util.exists(cartIcon);
    }

    public boolean doAllProductCardsHaveRequiredDetails() {
        List<WebElement> cards = util.all(productCards);
        if (cards.isEmpty()) {
            return false;
        }

        for (WebElement card : cards) {
            try {
                String title = card.findElement(By.cssSelector(".shelf-item__title")).getText().trim();
                String price = card.findElement(By.cssSelector(".shelf-item__price, .val")).getText().trim();
                WebElement image = card.findElement(By.cssSelector("img"));
                WebElement button = card.findElement(By.cssSelector(".shelf-item__buy-btn"));

                if (title.isBlank() || price.isBlank() || !image.isDisplayed() || !button.isEnabled()) {
                    return false;
                }
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                return false;
            }
        }
        return true;
    }

    public boolean areProductImagesWorking() {
        List<WebElement> images = util.all(productImages);
        if (images.isEmpty()) {
            return false;
        }

        JavascriptExecutor js = (JavascriptExecutor) driver();
        for (WebElement image : images) {
            Boolean complete = (Boolean) js.executeScript(
                    "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                    image
            );
            if (!Boolean.TRUE.equals(complete)) {
                return false;
            }
        }
        return true;
    }

    public boolean refreshAndCheckUsable() {
        driver().navigate().refresh();
        util.waitForPageReady();
        util.waitForAtLeastOne(productCards);
        return isHomePageDisplayed();
    }

    public int getProductCount() {
        return getVisibleProductCardsInVisualOrder().size();
    }

    public boolean productGridAvailable() {
        util.waitForAtLeastOne(productCards);
        return getProductCount() > 0;
    }

    public void applyVendorFilter(String vendor) {
        By vendorFilter = By.xpath(
                "//label[contains(normalize-space(.),'" + vendor + "')] | " +
                        "//span[contains(normalize-space(.),'" + vendor + "')]"
        );
        util.click(vendorFilter);
        sleep(900);
        util.waitForAtLeastOne(productCards);
    }

    public boolean areAllProductsMatchingVendor(String vendor) {
        List<String> names = getProductNames();
        if (names.isEmpty()) {
            return false;
        }
        return names.stream().allMatch(name -> productBelongsToVendor(name, vendor));
    }

    public void clearSelectedFilters() {
        driver().get(ConfigReader.get("baseUrl"));
        util.waitForPageReady();

        try {
            ((JavascriptExecutor) driver()).executeScript(
                    "try {" +
                            "localStorage.clear();" +
                            "sessionStorage.clear();" +
                            "document.querySelectorAll('input[type=checkbox]').forEach(function(cb) {" +
                            "  if (cb.checked) {" +
                            "    cb.checked = false;" +
                            "    cb.dispatchEvent(new Event('input', {bubbles:true}));" +
                            "    cb.dispatchEvent(new Event('change', {bubbles:true}));" +
                            "  }" +
                            "});" +
                            "} catch(e) {}"
            );
        } catch (RuntimeException ignored) {
            // Normal reload below is the fallback.
        }

        driver().get(ConfigReader.get("baseUrl"));
        util.waitForPageReady();
        closeCartIfBlockingPage();
        util.waitForAtLeastOne(productCards);
    }

    public void clearSelectedFiltersPreservingCart() {
        closeCartIfBlockingPage();

        try {
            List<WebElement> checkedInputs = driver().findElements(
                    By.cssSelector("input[type='checkbox']:checked")
            );

            for (WebElement input : checkedInputs) {
                ((JavascriptExecutor) driver()).executeScript(
                        "arguments[0].checked = false;" +
                                "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));" +
                                "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                        input
                );
                sleep(300);
            }
        } catch (RuntimeException ignored) {
            // Fallback below reloads the application.
        }

        sleep(900);

        if (getVisibleProductCardsInVisualOrder().size() < 15) {
            driver().get(ConfigReader.get("baseUrl"));
            util.waitForPageReady();
            util.waitForAtLeastOne(productCards);
        }
    }

    public List<String> getProductNames() {
        return getVisibleProductCardsInVisualOrder()
                .stream()
                .map(card -> card.findElement(By.cssSelector(".shelf-item__title")).getText())
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .collect(Collectors.toList());
    }

    public boolean hasDuplicateProducts() {
        List<String> names = getProductNames();
        Set<String> uniqueNames = new HashSet<>(names);
        return uniqueNames.size() != names.size();
    }

    public void sortProducts(boolean ascending) {
        WebElement dropdown = new WebDriverWait(
                driver(),
                Duration.ofSeconds(ConfigReader.timeout())
        ).until(d -> d.findElement(By.cssSelector("select")));

        Select select = new Select(dropdown);
        String sortValue = ascending ? "lowestprice" : "highestprice";
        select.selectByValue(sortValue);

        new WebDriverWait(
                driver(),
                Duration.ofSeconds(ConfigReader.timeout())
        ).until(d -> arePricesSorted(ascending));
    }

    public boolean arePricesSorted(boolean ascending) {
        List<Double> prices = getProductPrices();

        if (prices.size() < 2) {
            return false;
        }

        for (int i = 1; i < prices.size(); i++) {
            double previous = prices.get(i - 1);
            double current = prices.get(i);

            if (ascending && current < previous) {
                return false;
            }

            if (!ascending && current > previous) {
                return false;
            }
        }

        return true;
    }

    public List<Double> getProductPrices() {
        By mainProductPrices = By.cssSelector(
                ".shelf-item__price > div.val > b"
        );

        List<Double> prices = new ArrayList<>();

        for (WebElement priceElement : driver().findElements(mainProductPrices)) {
            String priceText = priceElement.getText().trim();
            double price = parseAmount(priceText);

            if (price > 0) {
                prices.add(price);
            }
        }

        return prices;
    }

    public void addFirstProductToCart() {
        ensureProductGridReady();
        List<WebElement> products = getVisibleProductCardsInVisualOrder();

        if (products.isEmpty()) {
            throw new NoSuchElementException("No products available to add to cart.");
        }

        WebElement firstProduct = products.get(0);
        selectedProductName = firstProduct.findElement(
                By.cssSelector(".shelf-item__title")
        ).getText().trim();

        selectedProductPrice = firstProduct.findElement(
                By.cssSelector(".shelf-item__price, .val")
        ).getText().trim();

        clickWithJsFallback(
                firstProduct.findElement(By.cssSelector(".shelf-item__buy-btn"))
        );
        sleep(1000);
    }

    public void addTwoDifferentProductsToCart() {
        ensureProductGridReady();
        List<WebElement> products = getVisibleProductCardsInVisualOrder();

        if (products.size() < 2) {
            throw new NoSuchElementException("Less than two products are available.");
        }

        selectedProductName = products.get(0)
                .findElement(By.cssSelector(".shelf-item__title"))
                .getText()
                .trim();

        clickWithJsFallback(
                products.get(0).findElement(By.cssSelector(".shelf-item__buy-btn"))
        );
        sleep(700);

        closeCartIfBlockingPage();
        ensureProductGridReady();

        products = getVisibleProductCardsInVisualOrder();
        clickWithJsFallback(
                products.get(1).findElement(By.cssSelector(".shelf-item__buy-btn"))
        );
        sleep(1000);
    }

    public String getSelectedProductName() {
        return selectedProductName;
    }

    public String getSelectedProductPrice() {
        return selectedProductPrice;
    }

    public boolean isResponsiveProductGridUsable() {
        return getProductCount() > 0 && util.all(addToCartButtons).size() > 0;
    }

    private boolean productBelongsToVendor(String productName, String vendor) {
        String name = normalize(productName);
        String selectedVendor = normalize(vendor);

        if ("apple".equals(selectedVendor)) {
            return name.contains("iphone") || name.contains("ipad")
                    || name.contains("mac") || name.contains("apple");
        }

        if ("samsung".equals(selectedVendor)) {
            return name.contains("samsung") || name.contains("galaxy");
        }

        if ("google".equals(selectedVendor)) {
            return name.contains("google") || name.contains("pixel") || name.contains("nexus");
        }

        if ("oneplus".equals(selectedVendor) || "one plus".equals(selectedVendor)) {
            return name.contains("oneplus") || name.contains("one plus");
        }

        return name.contains(selectedVendor);
    }

    private List<WebElement> getVisibleProductCardsInVisualOrder() {
        return util.all(productCards)
                .stream()
                .filter(card -> {
                    try {
                        return card.isDisplayed();
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                })
                .sorted(Comparator
                        .comparingInt((WebElement card) -> card.getLocation().getY())
                        .thenComparingInt(card -> card.getLocation().getX()))
                .collect(Collectors.toList());
    }

    private void ensureProductGridReady() {
        try {
            util.waitForAtLeastOne(productCards);
        } catch (TimeoutException e) {
            openHomeClean();
            util.waitForAtLeastOne(productCards);
        }
    }

    private void closeCartIfBlockingPage() {
        try {
            List<WebElement> closeButtons = driver().findElements(cartCloseButton);
            if (!closeButtons.isEmpty() && closeButtons.get(0).isDisplayed()) {
                closeButtons.get(0).click();
                sleep(600);
            }
        } catch (NoSuchElementException | StaleElementReferenceException ignored) {
            // Cart is already closed.
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

    private String normalize(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", "")
                .trim();
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
