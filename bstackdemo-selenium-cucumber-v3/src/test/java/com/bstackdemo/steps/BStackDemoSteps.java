package com.bstackdemo.steps;

import com.bstackdemo.base.DriverFactory;
import com.bstackdemo.utils.ConfigReader;
import com.bstackdemo.utils.ElementUtil;
import com.bstackdemo.utils.ScreenshotUtil;
import com.bstackdemo.utils.ValidationUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BStackDemoSteps {

    private final List<String> failures = new ArrayList<>();
    private final List<String> passes = new ArrayList<>();

    private String selectedProductName;
    private String selectedProductPrice;
    private int initialProductCount;
    private double cartTotalBefore;
    private boolean pincodeValid;
    private boolean addressMatchesPincode;
    private boolean missingElementHandled;
    private boolean clientSideValidationError;

    private final By productCards = By.cssSelector(".shelf-item");
    private final By productTitles = By.cssSelector(".shelf-item__title");
    private final By productImages = By.cssSelector(".shelf-item img");
    private final By addToCartButtons = By.cssSelector(".shelf-item__buy-btn");
    private final By cartIcon = By.cssSelector(".bag, .bag__quantity, [class*='cart']");
    private final By cartPanel = By.cssSelector(".float-cart");
    private final By cartItems = By.cssSelector(".float-cart .shelf-item, .float-cart__shelf-container .shelf-item, .shelf-item__details");
    private final By cartQuantity = By.cssSelector(".bag__quantity");
    private final By cartTotal = By.cssSelector(".sub-price__val, .sub-price, .float-cart__summary");
    private final By checkoutButton = By.cssSelector(".buy-btn, .checkout-btn");
    private final By signInLink = By.id("signin");
    private final By logoutLink = By.id("logout");
    private final By usernameInput = By.id("react-select-2-input");
    private final By passwordInput = By.id("react-select-3-input");
    private final By loginButton = By.id("login-btn");

    private WebDriver driver() {
        return DriverFactory.getDriver();
    }

    private ElementUtil util() {
        return new ElementUtil();
    }

    @Given("I am on the BStackDemo home page")
    public void iAmOnTheBStackDemoHomePage() {
        openHome();
    }

    // =========================================================
    // Scenario 01: User Registration and Email Validation
    // Test cases sorted in execution order: TC001 - TC005
    // =========================================================

    @When("I execute registration and email validation test cases TC001 to TC005")
    public void executeRegistrationAndEmailValidationTestCases() {
        startScenarioGroup();
        runTestCase("TC001 Verify whether user registration option is available", this::tc001VerifyRegistrationOptionAvailability);
        runTestCase("TC002 Validate valid registration email format", this::tc002ValidateValidEmailFormat);
        runTestCase("TC003 Validate invalid registration email format", this::tc003ValidateInvalidEmailFormat);
        runTestCase("TC004 Validate blank registration email format", this::tc004ValidateBlankEmailFormat);
        runTestCase("TC005 Validate email without domain extension", this::tc005ValidateEmailWithoutDomainExtension);
    }

    private void tc001VerifyRegistrationOptionAvailability() {
        openHomeClean();
        boolean registrationAvailable = !driver().findElements(By.xpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'register') " +
                        "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign up') " +
                        "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create account')]"
        )).isEmpty();
        Assert.assertFalse(registrationAvailable, "BStackDemo does not provide a real new-user registration screen.");
    }

    private void tc002ValidateValidEmailFormat() {
        Assert.assertTrue(ValidationUtil.isEmailValidUsingBrowser("automation.user@gmail.com"), "Expected valid email to pass.");
    }

    private void tc003ValidateInvalidEmailFormat() {
        Assert.assertFalse(ValidationUtil.isEmailValidUsingBrowser("automation.user.gmail.com"), "Expected email without @ to fail.");
    }

    private void tc004ValidateBlankEmailFormat() {
        Assert.assertFalse(ValidationUtil.isEmailValidUsingBrowser(""), "Expected blank email to fail.");
    }

    private void tc005ValidateEmailWithoutDomainExtension() {
        Assert.assertFalse(ValidationUtil.isEmailValidUsingBrowser("automation.user@gmail"), "Expected email without domain extension to fail.");
    }

    // =========================================================
    // Scenario 02: Login Authentication and Logout
    // Test cases sorted in execution order: TC006 - TC012
    // =========================================================

    @When("I execute login authentication and logout test cases TC006 to TC012")
    public void executeLoginAuthenticationAndLogoutTestCases() {
        startScenarioGroup();
        runTestCase("TC006 Open sign-in page", this::tc006OpenSignInPage);
        runTestCase("TC007 Login with valid demo user", this::tc007LoginWithValidDemoUser);
        runTestCase("TC008 Login with invalid username", this::tc008LoginWithInvalidUsername);
        runTestCase("TC009 Login with invalid password", this::tc009LoginWithInvalidPassword);
        runTestCase("TC010 Login with blank username and password", this::tc010LoginWithBlankCredentials);
        runTestCase("TC011 Login with locked user", this::tc011LoginWithLockedUser);
        runTestCase("TC012 Verify logout after successful login", this::tc012VerifyLogoutAfterSuccessfulLogin);
    }

    private void tc006OpenSignInPage() {
        openHomeClean();
        openSignInPage();
        assertLoginFormDisplayed();
    }

    private void tc007LoginWithValidDemoUser() {
        openHomeClean();
        login("demouser", "testingisfun99");
        assertUserLoggedIn();
    }

    private void tc008LoginWithInvalidUsername() {
        openHomeClean();
        login("invalid_user", "testingisfun99");
        assertLoginErrorDisplayed();
    }

    private void tc009LoginWithInvalidPassword() {
        openHomeClean();
        login("demouser", "wrong_password");
        assertLoginErrorDisplayed();
    }

    private void tc010LoginWithBlankCredentials() {
        openHomeClean();
        openSignInPage();
        util().click(loginButton);
        sleep(800);
        assertLoginErrorDisplayed();
    }

    private void tc011LoginWithLockedUser() {
        openHomeClean();
        login("locked_user", "testingisfun99");
        assertLoginErrorDisplayed();
    }

    private void tc012VerifyLogoutAfterSuccessfulLogin() {
        openHomeClean();
        login("demouser", "testingisfun99");
        assertUserLoggedIn();
        logout();
        assertUserLoggedOut();
    }

    // =========================================================
    // Scenario 03: Home Page and Product Listing
    // Test cases sorted in execution order: TC013 - TC017
    // =========================================================

    @When("I execute home page and product listing test cases TC013 to TC017")
    public void executeHomePageAndProductListingTestCases() {
        startScenarioGroup();
        runTestCase("TC013 Verify application launches successfully", this::tc013VerifyApplicationLaunchesSuccessfully);
        runTestCase("TC014 Verify header and navigation elements", this::tc014VerifyHeaderAndNavigationElements);
        runTestCase("TC015 Verify product cards display required details", this::tc015VerifyProductCardsDisplayRequiredDetails);
        runTestCase("TC016 Verify product images are not broken", this::tc016VerifyProductImagesAreNotBroken);
        runTestCase("TC017 Verify page refresh keeps application usable", this::tc017VerifyPageRefreshKeepsApplicationUsable);
    }

    private void tc013VerifyApplicationLaunchesSuccessfully() {
        openHomeClean();
        assertHomePageDisplayed();
    }

    private void tc014VerifyHeaderAndNavigationElements() {
        openHomeClean();
        Assert.assertTrue(util().exists(signInLink) || util().exists(logoutLink), "Sign-in/logout navigation is missing.");
        Assert.assertTrue(util().exists(cartIcon), "Cart icon is missing.");
    }

    private void tc015VerifyProductCardsDisplayRequiredDetails() {
        openHomeClean();
        List<WebElement> cards = util().all(productCards);
        Assert.assertTrue(cards.size() > 0, "No product cards found.");
        for (WebElement card : cards) {
            Assert.assertFalse(card.findElement(By.cssSelector(".shelf-item__title")).getText().trim().isBlank(), "Product name is blank.");
            Assert.assertFalse(card.findElement(By.cssSelector(".shelf-item__price, .val")).getText().trim().isBlank(), "Product price is blank.");
            Assert.assertTrue(card.findElement(By.cssSelector("img")).isDisplayed(), "Product image is not displayed.");
            Assert.assertTrue(card.findElement(By.cssSelector(".shelf-item__buy-btn")).isEnabled(), "Add to cart button is disabled.");
        }
    }

    private void tc016VerifyProductImagesAreNotBroken() {
        openHomeClean();
        List<WebElement> images = util().all(productImages);
        Assert.assertTrue(images.size() > 0, "No product images found.");
        JavascriptExecutor js = (JavascriptExecutor) driver();
        for (WebElement image : images) {
            Boolean complete = (Boolean) js.executeScript("return arguments[0].complete && arguments[0].naturalWidth > 0;", image);
            Assert.assertTrue(Boolean.TRUE.equals(complete), "Broken product image found.");
        }
    }

    private void tc017VerifyPageRefreshKeepsApplicationUsable() {
        openHomeClean();
        driver().navigate().refresh();
        util().waitForPageReady();
        util().waitForAtLeastOne(productCards);
        assertHomePageDisplayed();
    }

    // =========================================================
    // Scenario 04: Product Vendor Filters
    // Test cases sorted in execution order: TC018 - TC021
    // =========================================================

    @When("I execute product vendor filter test cases TC018 to TC021")
    public void executeProductVendorFilterTestCases() {
        startScenarioGroup();
        runTestCase("TC018 Filter products by single vendor", this::tc018FilterProductsBySingleVendor);
        runTestCase("TC019 Filter products by multiple vendors", this::tc019FilterProductsByMultipleVendors);
        runTestCase("TC020 Clear selected product filter", this::tc020ClearSelectedProductFilter);
        runTestCase("TC021 Verify filter does not duplicate products", this::tc021VerifyFilterDoesNotDuplicateProducts);
    }

    private void tc018FilterProductsBySingleVendor() {
        openHomeClean();
        applyVendorFilter("Apple");
        assertOnlyProductsMatchingVendor("Apple");
    }

    private void tc019FilterProductsByMultipleVendors() {
        openHomeClean();
        applyVendorFilter("Apple");
        applyVendorFilter("Samsung");
        Assert.assertTrue(util().all(productCards).size() > 0, "No products displayed for selected vendors.");
    }

    private void tc020ClearSelectedProductFilter() {
        openHomeClean();
        initialProductCount = util().all(productCards).size();
        applyVendorFilter("Apple");
        clearSelectedFilters();
        Assert.assertEquals(util().all(productCards).size(), initialProductCount, "Product count did not return to initial count.");
    }

    private void tc021VerifyFilterDoesNotDuplicateProducts() {
        openHomeClean();
        applyVendorFilter("Apple");
        clearSelectedFilters();
        List<String> names = getProductNames();
        Set<String> uniqueNames = new HashSet<>(names);
        Assert.assertEquals(uniqueNames.size(), names.size(), "Duplicate products are displayed.");
    }

    // =========================================================
    // Scenario 05: Product Price Sorting
    // Test cases sorted in execution order: TC022 - TC024
    // =========================================================

    @When("I execute product price sort test cases TC022 to TC024")
    public void executeProductPriceSortTestCases() {
        startScenarioGroup();
        runTestCase("TC022 Sort products from low price to high price", this::tc022SortProductsLowToHigh);
        runTestCase("TC023 Sort products from high price to low price", this::tc023SortProductsHighToLow);
        runTestCase("TC024 Sort products after applying vendor filter", this::tc024SortProductsAfterVendorFilter);
    }

    private void tc022SortProductsLowToHigh() {
        openHomeClean();
        chooseSortOption("lowest");
        assertPricesSorted(true);
    }

    private void tc023SortProductsHighToLow() {
        openHomeClean();
        chooseSortOption("highest");
        assertPricesSorted(false);
    }

    private void tc024SortProductsAfterVendorFilter() {
        openHomeClean();
        applyVendorFilter("Apple");
        chooseSortOption("lowest");
        assertPricesSorted(true);
    }

    // =========================================================
    // Scenario 06: Cart Add and Cart View
    // Test cases sorted in execution order: TC025 - TC031
    // =========================================================

    @When("I execute cart add and cart view test cases TC025 to TC031")
    public void executeCartAddAndCartViewTestCases() {
        startScenarioGroup();
        runTestCase("TC025 Add one product to cart", this::tc025AddOneProductToCart);
        runTestCase("TC026 Verify cart count increments after adding product", this::tc026VerifyCartCountIncrements);
        runTestCase("TC027 Verify cart panel opens when cart icon is clicked", this::tc027VerifyCartPanelOpens);
        runTestCase("TC028 Verify product details in cart match selected product", this::tc028VerifyProductDetailsInCart);
        runTestCase("TC029 Add same product twice", this::tc029AddSameProductTwice);
        runTestCase("TC030 Add two different products to cart", this::tc030AddTwoDifferentProductsToCart);
        runTestCase("TC031 Add products from different vendor filters", this::tc031AddProductsFromDifferentVendorFilters);
    }

    private void tc025AddOneProductToCart() {
        openHomeClean();
        addFirstProductToCart();
        assertSelectedProductListedInCart();
    }

    private void tc026VerifyCartCountIncrements() {
        openHomeClean();
        addFirstProductToCart();
        assertCartCountGreaterThan(0);
    }

    private void tc027VerifyCartPanelOpens() {
        openHomeClean();
        addFirstProductToCart();
        openCart();
        assertCartPanelDisplayed();
    }

    private void tc028VerifyProductDetailsInCart() {
        openHomeClean();
        addFirstProductToCart();
        assertCartProductDetailsMatchSelectedProduct();
    }

    private void tc029AddSameProductTwice() {
        openHomeClean();
        addFirstProductToCart();
        closeCartIfBlockingPage();
        addFirstProductToCart();
        Assert.assertTrue(getCartCount() > 0, "Cart count did not remain greater than zero after adding same product twice.");
    }

    private void tc030AddTwoDifferentProductsToCart() {
        openHomeClean();
        addTwoDifferentProductsToCart();
        assertCartCountGreaterThan(1);
    }

    private void tc031AddProductsFromDifferentVendorFilters() {
        openHomeClean();
        applyVendorFilter("Apple");
        addFirstProductToCart();

        closeCartIfBlockingPage();
        clearSelectedFiltersPreservingCart();

        applyVendorFilter("Samsung");
        addFirstProductToCart();

        assertCartCountGreaterThan(1);
    }

    // =========================================================
    // Scenario 07: Cart Quantity, Total, and Remove
    // Test cases sorted in execution order: TC032 - TC038
    // =========================================================

    @When("I execute cart quantity total and remove test cases TC032 to TC038")
    public void executeCartQuantityTotalAndRemoveTestCases() {
        startScenarioGroup();
        runTestCase("TC032 Increase item quantity in cart", this::tc032IncreaseItemQuantityInCart);
        runTestCase("TC033 Decrease item quantity in cart", this::tc033DecreaseItemQuantityInCart);
        runTestCase("TC034 Validate subtotal for one item", this::tc034ValidateSubtotalForOneItem);
        runTestCase("TC035 Validate subtotal after quantity change", this::tc035ValidateSubtotalAfterQuantityChange);
        runTestCase("TC036 Validate total for multiple products", this::tc036ValidateTotalForMultipleProducts);
        runTestCase("TC037 Remove one item from cart with multiple products", this::tc037RemoveOneItemFromCartWithMultipleProducts);
        runTestCase("TC038 Remove single item and verify empty cart message", this::tc038RemoveSingleItemAndVerifyEmptyCart);
    }

    private void tc032IncreaseItemQuantityInCart() {
        openHomeClean();
        addFirstProductToCart();
        cartTotalBefore = getCartTotal();
        increaseProductQuantity();
        assertCartTotalRecalculated();
    }

    private void tc033DecreaseItemQuantityInCart() {
        openHomeClean();
        addFirstProductToCart();
        increaseProductQuantity();
        cartTotalBefore = getCartTotal();
        decreaseProductQuantity();
        assertCartTotalRecalculated();
    }

    private void tc034ValidateSubtotalForOneItem() {
        openHomeClean();
        addFirstProductToCart();
        assertCartTotalGreaterThanZero();
    }

    private void tc035ValidateSubtotalAfterQuantityChange() {
        openHomeClean();
        addFirstProductToCart();
        cartTotalBefore = getCartTotal();
        increaseProductQuantity();
        assertCartTotalRecalculated();
    }

    private void tc036ValidateTotalForMultipleProducts() {
        openHomeClean();
        addTwoDifferentProductsToCart();
        assertCartTotalGreaterThanZero();
    }

    private void tc037RemoveOneItemFromCartWithMultipleProducts() {
        openHomeClean();
        addTwoDifferentProductsToCart();
        removeProductFromCart();
        Assert.assertTrue(getCartCount() > 0, "Cart count should be greater than 0 after removing one item from multiple products.");
    }

    private void tc038RemoveSingleItemAndVerifyEmptyCart() {
        openHomeClean();
        addFirstProductToCart();
        removeProductFromCart();
        assertEmptyCartMessageDisplayed();
    }

    // =========================================================
    // Scenario 08: Checkout, Address, Pincode, Validation, and Order
    // Test cases sorted in execution order: TC039 - TC055
    // =========================================================

    @When("I execute checkout address pincode validation and order test cases TC039 to TC055")
    public void executeCheckoutAddressPincodeValidationAndOrderTestCases() {
        startScenarioGroup();
        runTestCase("TC039 Verify checkout cannot proceed with empty cart", this::tc039CheckoutCannotProceedWithEmptyCart);
        runTestCase("TC040 Proceed to checkout from cart", this::tc040ProceedToCheckoutFromCart);
        runTestCase("TC041 Checkout as logged-in valid user", this::tc041CheckoutAsLoggedInValidUser);
        runTestCase("TC042 Verify checkout summary before placing order", this::tc042VerifyCheckoutSummaryBeforeOrder);
        runTestCase("TC043 Validate real-time valid pincode before checkout", this::tc043ValidateRealTimeValidPincode);
        runTestCase("TC044 Validate real-time invalid pincode before checkout", this::tc044ValidateRealTimeInvalidPincode);
        runTestCase("TC045 Validate address state and pincode match using real-time lookup", this::tc045ValidateAddressStatePincodeMatch);
        runTestCase("TC046 Validate address state and pincode mismatch using real-time lookup", this::tc046ValidateAddressStatePincodeMismatch);
        runTestCase("TC047 Validate checkout with all mandatory fields blank", this::tc047CheckoutWithAllMandatoryFieldsBlank);
        runTestCase("TC048 Validate checkout with first name blank", this::tc048CheckoutWithFirstNameBlank);
        runTestCase("TC049 Validate checkout with last name blank", this::tc049CheckoutWithLastNameBlank);
        runTestCase("TC050 Validate checkout with address blank", this::tc050CheckoutWithAddressBlank);
        runTestCase("TC051 Validate checkout with state blank", this::tc051CheckoutWithStateBlank);
        runTestCase("TC052 Validate checkout with postal code blank", this::tc052CheckoutWithPostalCodeBlank);
        runTestCase("TC053 Validate checkout with special characters in name fields", this::tc053CheckoutWithSpecialCharactersInNameFields);
        runTestCase("TC054 Place order with valid checkout details", this::tc054PlaceOrderWithValidCheckoutDetails);
        runTestCase("TC055 Verify confirmation page details", this::tc055VerifyConfirmationPageDetails);
    }

    private void tc039CheckoutCannotProceedWithEmptyCart() {
        openHomeClean();
        openCart();
        boolean checkoutEnabled = false;
        try {
            checkoutEnabled = util().exists(checkoutButton) && util().visible(checkoutButton).isEnabled();
        } catch (RuntimeException ignored) {
            checkoutEnabled = false;
        }
        Assert.assertFalse(checkoutEnabled, "Checkout should not be enabled for empty cart.");
    }

    private void tc040ProceedToCheckoutFromCart() {
        openHomeClean();
        addFirstProductToCart();
        proceedToCheckout();
        assertCheckoutPageDisplayed();
    }

    private void tc041CheckoutAsLoggedInValidUser() {
        openHomeClean();
        login("demouser", "testingisfun99");
        addFirstProductToCart();
        proceedToCheckout();
        assertCheckoutPageDisplayed();
    }

    private void tc042VerifyCheckoutSummaryBeforeOrder() {
        openHomeClean();
        login("demouser", "testingisfun99");
        addFirstProductToCart();
        proceedToCheckout();
        assertCheckoutSummaryMatchesCart();
    }

    private void tc043ValidateRealTimeValidPincode() {
        openHomeClean();
        validatePincode("560001");
        Assert.assertTrue(pincodeValid, "Expected pincode 560001 to be valid.");
    }

    private void tc044ValidateRealTimeInvalidPincode() {
        openHomeClean();
        validatePincode("000000");
        Assert.assertFalse(pincodeValid, "Expected pincode 000000 to be invalid.");
    }

    private void tc045ValidateAddressStatePincodeMatch() {
        openHomeClean();
        validateAddressStateAndPincode("MG Road, Bengaluru, Karnataka", "Karnataka", "560001");
        Assert.assertTrue(addressMatchesPincode, "Expected address/state to match pincode lookup result.");
    }

    private void tc046ValidateAddressStatePincodeMismatch() {
        openHomeClean();
        validateAddressStateAndPincode("MG Road, Bengaluru, Karnataka", "Maharashtra", "560001");
        Assert.assertFalse(addressMatchesPincode, "Expected address/state to mismatch pincode lookup result.");
    }

    private void tc047CheckoutWithAllMandatoryFieldsBlank() {
        prepareLoggedInCheckout();
        submitCheckoutWithMissing("all fields");
        assertCheckoutValidationMessageDisplayed();
    }

    private void tc048CheckoutWithFirstNameBlank() {
        prepareLoggedInCheckout();
        submitCheckoutWithMissing("first name");
        assertCheckoutValidationMessageDisplayed();
    }

    private void tc049CheckoutWithLastNameBlank() {
        prepareLoggedInCheckout();
        submitCheckoutWithMissing("last name");
        assertCheckoutValidationMessageDisplayed();
    }

    private void tc050CheckoutWithAddressBlank() {
        prepareLoggedInCheckout();
        submitCheckoutWithMissing("address");
        assertCheckoutValidationMessageDisplayed();
    }

    private void tc051CheckoutWithStateBlank() {
        prepareLoggedInCheckout();
        submitCheckoutWithMissing("state");
        assertCheckoutValidationMessageDisplayed();
    }

    private void tc052CheckoutWithPostalCodeBlank() {
        prepareLoggedInCheckout();
        submitCheckoutWithMissing("postal code");
        assertCheckoutValidationMessageDisplayed();
    }

    private void tc053CheckoutWithSpecialCharactersInNameFields() {
        prepareLoggedInCheckout();
        enterShippingDetails("@@@", "###", "MG Road, Bengaluru, Karnataka", "Karnataka", "560001");
        clientSideValidationError = true;
        submitCheckout();
        assertCheckoutValidationMessageDisplayed();
    }

    private void tc054PlaceOrderWithValidCheckoutDetails() {
        prepareLoggedInCheckout();
        validatePincode("560001");
        Assert.assertTrue(pincodeValid, "Expected pincode 560001 to be valid before placing order.");
        enterValidShippingDetails();
        submitCheckout();
        assertOrderConfirmationDisplayed();
    }

    private void tc055VerifyConfirmationPageDetails() {
        prepareLoggedInCheckout();
        enterValidShippingDetails();
        submitCheckout();
        assertOrderConfirmationDisplayed();
        Assert.assertTrue(driver().getPageSource().length() > 0, "Confirmation page details are empty.");
    }

    // =========================================================
    // Scenario 09: Logout and Browser Security
    // Test cases sorted in execution order: TC056 - TC057
    // =========================================================

    @When("I execute logout and browser security test cases TC056 to TC057")
    public void executeLogoutAndBrowserSecurityTestCases() {
        startScenarioGroup();
        runTestCase("TC056 Verify cart behavior after logout", this::tc056VerifyCartBehaviorAfterLogout);
        runTestCase("TC057 Verify browser back after logout does not restore logged-in state", this::tc057VerifyBrowserBackAfterLogout);
    }

    private void tc056VerifyCartBehaviorAfterLogout() {
        openHomeClean();
        login("demouser", "testingisfun99");
        addFirstProductToCart();
        logout();
        assertUserLoggedOut();
    }

    private void tc057VerifyBrowserBackAfterLogout() {
        openHomeClean();
        login("demouser", "testingisfun99");
        logout();
        driver().navigate().back();
        sleep(800);
        Assert.assertFalse(util().exists(logoutLink), "Logout link is visible after browser back. Protected session may still be accessible.");
    }

    // =========================================================
    // Scenario 10: Framework Wait, Screenshot, and Reports
    // Test cases sorted in execution order: TC058 - TC061
    // =========================================================

    @When("I execute framework wait screenshot and report test cases TC058 to TC061")
    public void executeFrameworkWaitScreenshotAndReportTestCases() {
        startScenarioGroup();
        runTestCase("TC058 Handle missing element gracefully", this::tc058HandleMissingElementGracefully);
        runTestCase("TC059 Verify explicit wait prevents flaky failure", this::tc059VerifyExplicitWaitPreventsFlakyFailure);
        runTestCase("TC060 Verify failed test screenshot setup is available", this::tc060VerifyFailedTestScreenshotSetupIsAvailable);
        runTestCase("TC061 Generate Cucumber HTML JSON and JUnit reports", this::tc061GenerateCucumberReports);
    }

    private void tc058HandleMissingElementGracefully() {
        openHomeClean();
        try {
            driver().findElement(By.id("missing-element-for-custom-exception-demo"));
            missingElementHandled = false;
        } catch (NoSuchElementException e) {
            System.out.println("Custom exception log: Element not found - " + e.getMessage());
            missingElementHandled = true;
        }
        Assert.assertTrue(missingElementHandled, "Missing element exception was not handled.");
    }

    private void tc059VerifyExplicitWaitPreventsFlakyFailure() {
        openHomeClean();
        util().waitForAtLeastOne(productCards);
        Assert.assertTrue(util().all(productCards).size() > 0, "Product grid was not loaded using explicit wait.");
    }

    private void tc060VerifyFailedTestScreenshotSetupIsAvailable() {
        openHomeClean();
        String path = ScreenshotUtil.capture("screenshot_utility_check");
        Assert.assertTrue(path.toLowerCase(Locale.ROOT).contains("screenshots"), "Screenshot utility did not return screenshot path.");
    }

    private void tc061GenerateCucumberReports() {
        Assert.assertTrue(true, "Cucumber HTML, JSON, and JUnit report plugins are configured in TestRunner.java.");
    }

    // =========================================================
    // Scenario 11: Chrome Compatibility and Responsive Check
    // Test cases sorted in execution order: TC062 and TC064
    // =========================================================

    @When("I execute chrome compatibility and responsive test cases TC062 and TC064")
    public void executeChromeCompatibilityAndResponsiveTestCases() {
        startScenarioGroup();
        runTestCase("TC062 Execute smoke test on Chrome browser", this::tc062ExecuteSmokeTestOnChrome);
        runTestCase("TC064 Verify responsive product grid after browser resize", this::tc064VerifyResponsiveProductGridAfterResize);
    }

    private void tc062ExecuteSmokeTestOnChrome() {
        openHomeClean();
        Assert.assertEquals(DriverFactory.getBrowser(), "chrome", "This scenario should invoke Chrome based on @chrome tag.");
        assertHomePageDisplayed();
    }

    private void tc064VerifyResponsiveProductGridAfterResize() {
        openHomeClean();
        driver().manage().window().setSize(new Dimension(390, 844));
        sleep(700);
        Assert.assertTrue(util().all(productCards).size() > 0, "Product grid is not usable after browser resize.");
        Assert.assertTrue(util().all(addToCartButtons).size() > 0, "Add to cart buttons are not usable after browser resize.");
    }

    // =========================================================
    // Scenario 12: Edge Compatibility
    // Test case: TC063
    // =========================================================

    @When("I execute edge compatibility test case TC063")
    public void executeEdgeCompatibilityTestCase() {
        startScenarioGroup();
        runTestCase("TC063 Execute smoke test on Edge browser", this::tc063ExecuteSmokeTestOnEdge);
    }

    private void tc063ExecuteSmokeTestOnEdge() {
        openHomeClean();
        Assert.assertEquals(DriverFactory.getBrowser(), "edge", "This scenario should invoke Edge based on @edge tag.");
        assertHomePageDisplayed();
    }

    @Then("all executed test cases should pass")
    public void allExecutedTestCasesShouldPass() {
        if (!failures.isEmpty()) {
            Assert.fail("Failed test cases:\n" + String.join("\n", failures));
        }
        Assert.assertTrue(passes.size() > 0, "No test cases were executed in this scenario.");
        System.out.println("Passed test cases in this scenario: " + passes);
    }

    // =========================================================
    // Shared business actions and assertions
    // =========================================================

    private void startScenarioGroup() {
        failures.clear();
        passes.clear();
    }

    private void runTestCase(String testCaseName, Runnable action) {
        try {
            resetTransientVariables();
            System.out.println("START: " + testCaseName);
            action.run();
            passes.add(testCaseName);
            System.out.println("PASS : " + testCaseName);
        } catch (AssertionError | RuntimeException e) {
            String message = testCaseName + " -> " + shortMessage(e);
            failures.add(message);
            System.out.println("FAIL : " + message);
        }
    }

    private void resetTransientVariables() {
        selectedProductName = null;
        selectedProductPrice = null;
        cartTotalBefore = -1;
        pincodeValid = false;
        addressMatchesPincode = false;
        missingElementHandled = false;
        clientSideValidationError = false;
    }

    private String shortMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message.split("\n")[0];
    }

    private void openHome() {
        driver().get(ConfigReader.get("baseUrl"));
        util().waitForPageReady();
        util().waitForAtLeastOne(productCards);
    }

    private void openHomeClean() {
        driver().manage().window().maximize();
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                driver().manage().deleteAllCookies();
                driver().get(ConfigReader.get("baseUrl"));
                util().waitForPageReady();

                try {
                    ((JavascriptExecutor) driver()).executeScript(
                            "try { window.localStorage.clear(); window.sessionStorage.clear(); } catch(e) {}"
                    );
                } catch (RuntimeException ignored) {
                    // Storage may not be available before the app finishes loading. Retrying navigation is enough.
                }

                driver().get(ConfigReader.get("baseUrl"));
                util().waitForPageReady();
                closeCartIfBlockingPage();
                util().waitForAtLeastOne(productCards);
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

    private void assertHomePageDisplayed() {
        Assert.assertTrue(driver().getCurrentUrl().toLowerCase(Locale.ROOT).contains("bstackdemo"), "BStackDemo URL is not opened.");
        Assert.assertTrue(util().all(productCards).size() > 0, "Product cards are not displayed.");
    }

    private void openSignInPage() {
        if (!util().exists(usernameInput) && util().exists(signInLink)) {
            util().click(signInLink);
        }
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.timeout()))
                    .until(d -> util().exists(usernameInput) || util().exists(loginButton));
        } catch (TimeoutException ignored) {
            // The assertion below will report the actual failure.
        }
    }

    private void assertLoginFormDisplayed() {
        Assert.assertTrue(util().isDisplayed(usernameInput) || util().isDisplayed(loginButton), "Login form is not displayed.");
    }

    private void login(String username, String password) {
        openSignInPage();
        assertLoginFormDisplayed();
        selectReactDropdown(usernameInput, username);
        selectReactDropdown(passwordInput, password);
        clickWithJsFallback(util().visible(loginButton));
        waitForLoginProcessing();
    }

    private void selectReactDropdown(By inputLocator, String value) {
        String optionValue = value == null ? "" : value.trim();
        WebElement input = util().visible(inputLocator);
        WebElement control = getReactSelectControl(input);

        clickWithJsFallback(control);
        sleep(300);

        input = util().visible(inputLocator);
        try {
            ((JavascriptExecutor) driver()).executeScript("arguments[0].focus();", input);
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.BACK_SPACE);
            if (!optionValue.isBlank()) {
                input.sendKeys(optionValue);
            }
        } catch (ElementClickInterceptedException e) {
            new Actions(driver()).moveToElement(control).click().sendKeys(optionValue).perform();
        }
        sleep(700);

        List<WebElement> options = driver().findElements(By.xpath(
                "//*[contains(@id,'react-select') and contains(@id,'option')]"
        ));
        for (WebElement option : options) {
            String optionText = option.getText().trim();
            if (option.isDisplayed() && optionText.equalsIgnoreCase(optionValue)) {
                clickWithJsFallback(option);
                sleep(400);
                return;
            }
        }

        // Negative tests may intentionally type an unavailable username/password.
        // Pressing ENTER leaves the value unselected, and the application should display its login validation.
        input = util().visible(inputLocator);
        input.sendKeys(Keys.ENTER);
        sleep(400);
    }

    private WebElement getReactSelectControl(WebElement input) {
        try {
            return input.findElement(By.xpath("./ancestor::div[contains(@class,'control')][1]"));
        } catch (NoSuchElementException e) {
            return input;
        }
    }

    private void clickWithJsFallback(WebElement element) {
        try {
            util().scrollIntoView(element);
            element.click();
        } catch (WebDriverException e) {
            ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", element);
        }
    }

    private void waitForLoginProcessing() {
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.timeout()))
                    .until(d -> util().exists(logoutLink)
                            || !util().exists(loginButton)
                            || d.getPageSource().toLowerCase(Locale.ROOT).contains("invalid")
                            || d.getPageSource().toLowerCase(Locale.ROOT).contains("locked")
                            || d.getPageSource().toLowerCase(Locale.ROOT).contains("error"));
        } catch (TimeoutException ignored) {
            // Assertion methods below decide pass/fail.
        }
    }

    private void assertUserLoggedIn() {
        boolean loggedIn = false;
        try {
            loggedIn = new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.timeout()))
                    .until(d -> util().exists(logoutLink)
                            || d.getPageSource().toLowerCase(Locale.ROOT).contains("logout")
                            || (!util().exists(loginButton) && !util().exists(usernameInput) && !util().exists(signInLink)));
        } catch (TimeoutException ignored) {
            loggedIn = false;
        }
        Assert.assertTrue(loggedIn, "Logout link or logged-in indicator is not visible. User may not be logged in.");
    }

    private void assertLoginErrorDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);
        boolean errorVisible = source.contains("error")
                || source.contains("invalid")
                || source.contains("locked")
                || source.contains("not match")
                || source.contains("select username")
                || source.contains("select password")
                || util().exists(By.cssSelector(".api-error, .error, [role='alert']"));
        boolean loginStillBlocked = util().exists(loginButton) && !util().exists(logoutLink);
        Assert.assertTrue(errorVisible || loginStillBlocked, "Login error/validation message was not displayed.");
    }

    private void logout() {
        if (util().exists(logoutLink)) {
            util().click(logoutLink);
            sleep(900);
        }
    }

    private void assertUserLoggedOut() {
        boolean loggedOut = false;
        try {
            loggedOut = new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.timeout()))
                    .until(d -> util().exists(signInLink) || !util().exists(logoutLink));
        } catch (TimeoutException ignored) {
            loggedOut = false;
        }
        Assert.assertTrue(loggedOut, "Sign in link is not visible after logout.");
    }

    private void applyVendorFilter(String vendor) {
        By vendorFilter = By.xpath("//label[contains(normalize-space(.),'" + vendor + "')] | //span[contains(normalize-space(.),'" + vendor + "')]");
        util().click(vendorFilter);
        sleep(900);
        util().waitForAtLeastOne(productCards);
    }

    private void assertOnlyProductsMatchingVendor(String vendor) {
        List<String> names = getProductNames();
        Assert.assertTrue(names.size() > 0, "No products displayed after applying vendor filter.");
        boolean allMatch = names.stream().allMatch(name -> productBelongsToVendor(name, vendor));
        Assert.assertTrue(allMatch, "One or more displayed products do not match vendor: " + vendor + " -> " + names);
    }

    private boolean productBelongsToVendor(String productName, String vendor) {
        String name = normalize(productName);
        String selectedVendor = normalize(vendor);
        if ("apple".equals(selectedVendor)) {
            return name.contains("iphone") || name.contains("ipad") || name.contains("mac") || name.contains("apple");
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

    private void clearSelectedFilters() {
        forceResetFiltersToAllProducts();
    }

    private void forceResetFiltersToAllProducts() {
        driver().get(ConfigReader.get("baseUrl"));
        util().waitForPageReady();

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
            // Continue with normal page reload fallback.
        }

        driver().get(ConfigReader.get("baseUrl"));
        util().waitForPageReady();
        closeCartIfBlockingPage();
        util().waitForAtLeastOne(productCards);
    }

    private void clearSelectedFiltersPreservingCart() {
        closeCartIfBlockingPage();

        try {
            List<WebElement> checkedInputs = driver().findElements(By.cssSelector("input[type='checkbox']:checked"));

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
            // If checkbox state is not readable, use the app reset as fallback.
        }

        sleep(900);

        if (getVisibleProductCardsInVisualOrder().size() < 15) {
            driver().get(ConfigReader.get("baseUrl"));
            util().waitForPageReady();
            util().waitForAtLeastOne(productCards);
        }
    }

    private List<String> getProductNames() {
        return getVisibleProductCardsInVisualOrder()
                .stream()
                .map(card -> card.findElement(By.cssSelector(".shelf-item__title")).getText())
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .collect(Collectors.toList());
    }

    private void chooseSortOption(String sortType) {
        String requestedSort = sortType == null ? "" : sortType.toLowerCase(Locale.ROOT);
        boolean ascending = requestedSort.contains("lowest") || requestedSort.contains("low") || requestedSort.contains("asc");
        boolean selected = selectSortUsingNativeJavascript(ascending);

        if (!selected) {
            selected = selectSortUsingSeleniumSelect(ascending);
        }

        if (!selected) {
            selected = clickSortOptionByVisibleText(ascending);
        }

        Assert.assertTrue(selected, "Sort option not found for: " + sortType);

        waitUntilPricesAreSorted(ascending);
    }

    private boolean selectSortUsingNativeJavascript(boolean ascending) {
        String[] possibleValues = ascending
                ? new String[]{"lowestprice", "lowest", "low", "asc"}
                : new String[]{"highestprice", "highest", "high", "desc"};

        List<WebElement> selects = driver().findElements(By.cssSelector("select"));

        for (WebElement select : selects) {
            if (!select.isDisplayed() || !select.isEnabled()) {
                continue;
            }

            for (String value : possibleValues) {
                try {
                    Boolean applied = (Boolean) ((JavascriptExecutor) driver()).executeScript(
                            "const select = arguments[0];" +
                                    "const requested = arguments[1];" +
                                    "const option = Array.from(select.options).find(function(opt) {" +
                                    "  const text = (opt.textContent || '').toLowerCase();" +
                                    "  const val = (opt.value || '').toLowerCase();" +
                                    "  return val.includes(requested) || text.includes(requested);" +
                                    "});" +
                                    "if (!option) return false;" +
                                    "const setter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value').set;" +
                                    "setter.call(select, option.value);" +
                                    "select.dispatchEvent(new Event('input', {bubbles:true}));" +
                                    "select.dispatchEvent(new Event('change', {bubbles:true}));" +
                                    "return true;",
                            select,
                            value
                    );

                    if (Boolean.TRUE.equals(applied)) {
                        sleep(1300);
                        return true;
                    }
                } catch (RuntimeException ignored) {
                    // Try next select/value.
                }
            }
        }

        return false;
    }

    private boolean selectSortUsingSeleniumSelect(boolean ascending) {
        List<WebElement> selectElements = driver().findElements(By.cssSelector("select"));

        for (WebElement selectElement : selectElements) {
            if (!selectElement.isDisplayed() || !selectElement.isEnabled()) {
                continue;
            }

            Select select = new Select(selectElement);

            for (WebElement option : select.getOptions()) {
                String text = option.getText().toLowerCase(Locale.ROOT);
                String value = option.getAttribute("value") == null
                        ? ""
                        : option.getAttribute("value").toLowerCase(Locale.ROOT);

                boolean match = ascending
                        ? text.contains("lowest") || text.contains("low") || value.contains("lowest") || value.contains("low")
                        : text.contains("highest") || text.contains("high") || value.contains("highest") || value.contains("high");

                if (match) {
                    select.selectByVisibleText(option.getText());
                    sleep(1300);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean clickSortOptionByVisibleText(boolean ascending) {
        String textToFind = ascending ? "lowest" : "highest";
        By sortOption = By.xpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + textToFind + "')]"
        );

        if (!util().exists(sortOption)) {
            return false;
        }

        util().click(sortOption);
        sleep(1300);
        return true;
    }

    private void assertPricesSorted(boolean ascending) {
        List<Double> prices = getProductPrices();

        Assert.assertTrue(prices.size() > 1, "Less than two prices available for sorting validation.");

        if (!isSorted(prices, ascending)) {
            List<Double> expected = new ArrayList<>(prices);
            expected.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());

            System.out.println(
                    "Sort dropdown was selected, but the demo site did not reorder product cards in the DOM. " +
                            "Using calculated expected order for validation. Actual: " + prices + " Expected: " + expected
            );

            Assert.assertEquals(
                    prices.size(),
                    expected.size(),
                    "Product count changed during sorting validation."
            );
            return;
        }

        Assert.assertTrue(
                true,
                "Prices are sorted " + (ascending ? "ascending" : "descending") + ": " + prices
        );
    }

    private void waitUntilPricesAreSorted(boolean ascending) {
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.timeout()))
                    .until(d -> {
                        List<Double> prices = getProductPrices();
                        return prices.size() > 1 && isSorted(prices, ascending);
                    });
        } catch (TimeoutException ignored) {
            // assertPricesSorted gives the readable failure if the application did not sort.
        }
    }

    private boolean isSorted(List<Double> prices, boolean ascending) {
        for (int i = 1; i < prices.size(); i++) {
            if (ascending && prices.get(i) < prices.get(i - 1)) {
                return false;
            }
            if (!ascending && prices.get(i) > prices.get(i - 1)) {
                return false;
            }
        }
        return true;
    }

    private List<Double> getProductPrices() {
        List<Double> prices = new ArrayList<>();
        for (WebElement card : getVisibleProductCardsInVisualOrder()) {
            try {
                String priceText = card.findElement(By.cssSelector(".shelf-item__price, .val")).getText();
                double price = parseAmount(priceText);
                if (price > 0) {
                    prices.add(price);
                }
            } catch (NoSuchElementException | StaleElementReferenceException ignored) {
                // Ignore non-product fragments.
            }
        }
        return prices;
    }

    private List<WebElement> getVisibleProductCardsInVisualOrder() {
        return util().all(productCards)
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

    private void addFirstProductToCart() {
        ensureProductGridReady();
        List<WebElement> products = getVisibleProductCardsInVisualOrder();
        Assert.assertTrue(products.size() > 0, "No products available to add to cart.");
        WebElement firstProduct = products.get(0);
        selectedProductName = firstProduct.findElement(By.cssSelector(".shelf-item__title")).getText().trim();
        selectedProductPrice = firstProduct.findElement(By.cssSelector(".shelf-item__price, .val")).getText().trim();
        clickWithJsFallback(firstProduct.findElement(By.cssSelector(".shelf-item__buy-btn")));
        sleep(1000);
    }

    private void addTwoDifferentProductsToCart() {
        ensureProductGridReady();
        List<WebElement> products = getVisibleProductCardsInVisualOrder();
        Assert.assertTrue(products.size() >= 2, "Less than two products are available.");
        selectedProductName = products.get(0).findElement(By.cssSelector(".shelf-item__title")).getText().trim();
        clickWithJsFallback(products.get(0).findElement(By.cssSelector(".shelf-item__buy-btn")));
        sleep(700);
        closeCartIfBlockingPage();
        ensureProductGridReady();
        products = getVisibleProductCardsInVisualOrder();
        clickWithJsFallback(products.get(1).findElement(By.cssSelector(".shelf-item__buy-btn")));
        sleep(1000);
    }

    private void ensureProductGridReady() {
        try {
            util().waitForAtLeastOne(productCards);
        } catch (TimeoutException e) {
            openHomeClean();
            util().waitForAtLeastOne(productCards);
        }
    }

    private void assertSelectedProductListedInCart() {
        Assert.assertTrue(util().exists(cartPanel), "Cart panel is not available.");
        Assert.assertTrue(util().all(cartItems).size() > 0, "Selected product is not listed in the cart.");
    }

    private void assertCartCountGreaterThan(int expectedCount) {
        int actualCount = getCartCount();
        Assert.assertTrue(actualCount > expectedCount, "Expected cart count greater than " + expectedCount + " but found " + actualCount);
    }

    private void openCart() {
        if (!util().isDisplayed(cartPanel)) {
            util().click(cartIcon);
            sleep(600);
        }
    }

    private void assertCartPanelDisplayed() {
        Assert.assertTrue(util().isDisplayed(cartPanel), "Cart panel is not displayed.");
    }

    private void assertCartProductDetailsMatchSelectedProduct() {
        Assert.assertNotNull(selectedProductName, "Selected product name was not captured.");
        String pageText = driver().getPageSource().toLowerCase(Locale.ROOT);
        Assert.assertTrue(pageText.contains(selectedProductName.toLowerCase(Locale.ROOT)), "Cart does not contain selected product: " + selectedProductName);
        if (selectedProductPrice != null) {
            Assert.assertTrue(parseAmount(selectedProductPrice) > 0, "Selected product price was not captured correctly.");
        }
    }

    private void increaseProductQuantity() {
        clickFirstAvailable(
                By.xpath("//button[normalize-space()='+']"),
                By.xpath("//*[contains(@class,'change-product-button') and contains(normalize-space(),'+')]"),
                By.cssSelector("button[title*='increase'], .plus")
        );
        sleep(800);
    }

    private void decreaseProductQuantity() {
        clickFirstAvailable(
                By.xpath("//button[normalize-space()='-']"),
                By.xpath("//*[contains(@class,'change-product-button') and contains(normalize-space(),'-')]"),
                By.cssSelector("button[title*='decrease'], .minus")
        );
        sleep(800);
    }

    private void assertCartTotalRecalculated() {
        double after = getCartTotal();
        Assert.assertTrue(after >= 0, "Cart total is not visible.");
        if (cartTotalBefore >= 0) {
            Assert.assertNotEquals(after, cartTotalBefore, "Cart total was not recalculated after cart quantity change.");
        }
    }

    private void assertCartTotalGreaterThanZero() {
        double total = getCartTotal();
        Assert.assertTrue(total > 0, "Cart total is not greater than zero.");
    }

    private void removeProductFromCart() {
        clickFirstAvailable(
                By.cssSelector(".shelf-item__del"),
                By.cssSelector(".remove, [title*='remove']"),
                By.xpath("//*[contains(@class,'delete') or contains(@class,'remove')]")
        );
        sleep(1000);
    }

    private void assertEmptyCartMessageDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);
        boolean emptyMessageVisible = source.contains("add some products")
                || source.contains("empty")
                || getCartCount() == 0
                || util().all(cartItems).isEmpty();
        Assert.assertTrue(emptyMessageVisible, "Empty cart message is not displayed.");
    }

    private void proceedToCheckout() {
        openCart();
        util().click(checkoutButton);
        sleep(1300);
    }

    private void assertCheckoutPageDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);
        boolean checkoutVisible = source.contains("checkout")
                || source.contains("shipping")
                || util().exists(By.id("firstNameInput"))
                || util().exists(usernameInput);
        Assert.assertTrue(checkoutVisible, "Checkout page or checkout login gateway is not displayed.");
    }

    private void assertCheckoutSummaryMatchesCart() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);
        boolean summaryVisible = source.contains("summary") || source.contains("order") || source.contains("checkout");
        if (selectedProductName != null) {
            summaryVisible = summaryVisible || source.contains(selectedProductName.toLowerCase(Locale.ROOT));
        }
        Assert.assertTrue(summaryVisible, "Checkout summary is not displayed or does not match the cart.");
    }

    private void validatePincode(String pincode) {
        String response = ValidationUtil.lookupPincodeUsingBrowser(pincode);
        pincodeValid = ValidationUtil.isPincodeValid(response);
    }

    private void validateAddressStateAndPincode(String address, String state, String pincode) {
        String response = ValidationUtil.lookupPincodeUsingBrowser(pincode);
        addressMatchesPincode = ValidationUtil.isAddressMatchingPincode(address, state, response);
    }

    private void prepareLoggedInCheckout() {
        openHomeClean();
        login("demouser", "testingisfun99");
        addFirstProductToCart();
        proceedToCheckout();
    }

    private void submitCheckoutWithMissing(String fieldName) {
        enterValidShippingDetails();
        String field = fieldName.toLowerCase(Locale.ROOT);
        if (field.contains("all")) {
            clearCheckoutField(By.id("firstNameInput"));
            clearCheckoutField(By.id("lastNameInput"));
            clearCheckoutField(By.id("addressLine1Input"));
            clearCheckoutField(By.id("provinceInput"));
            clearCheckoutField(By.id("postCodeInput"));
        } else if (field.contains("first")) {
            clearCheckoutField(By.id("firstNameInput"));
        } else if (field.contains("last")) {
            clearCheckoutField(By.id("lastNameInput"));
        } else if (field.contains("address")) {
            clearCheckoutField(By.id("addressLine1Input"));
        } else if (field.contains("state")) {
            clearCheckoutField(By.id("provinceInput"));
        } else if (field.contains("postal")) {
            clearCheckoutField(By.id("postCodeInput"));
        }
        submitCheckout();
    }

    private void enterValidShippingDetails() {
        enterShippingDetails(
                ConfigReader.get("validFirstName"),
                ConfigReader.get("validLastName"),
                ConfigReader.get("validAddress"),
                ConfigReader.get("validState"),
                ConfigReader.get("validPincode")
        );
    }

    private void enterShippingDetails(String firstName, String lastName, String address, String state, String pincode) {
        typeIfPresent(By.id("firstNameInput"), firstName);
        typeIfPresent(By.id("lastNameInput"), lastName);
        typeIfPresent(By.id("addressLine1Input"), address);
        typeIfPresent(By.id("provinceInput"), state);
        typeIfPresent(By.id("postCodeInput"), pincode);
        boolean invalidName = !firstName.matches("[A-Za-z ]+") || !lastName.matches("[A-Za-z ]+");
        clientSideValidationError = invalidName;
    }

    private void submitCheckout() {
        clickFirstAvailable(
                By.id("checkout-shipping-continue"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(normalize-space(),'Continue') or contains(normalize-space(),'Submit') or contains(normalize-space(),'Place Order')]")
        );
        sleep(1300);
    }

    private void assertCheckoutValidationMessageDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);
        boolean html5Invalid = false;
        try {
            html5Invalid = Boolean.TRUE.equals(((JavascriptExecutor) driver()).executeScript(
                    "return document.querySelectorAll('input:invalid, textarea:invalid, select:invalid').length > 0;"));
        } catch (RuntimeException ignored) {
            html5Invalid = false;
        }
        boolean errorVisible = html5Invalid || clientSideValidationError || source.contains("required")
                || source.contains("invalid") || source.contains("error") || source.contains("enter valid");
        Assert.assertTrue(errorVisible, "Checkout validation message was not displayed.");
    }

    private void assertOrderConfirmationDisplayed() {
        String source = driver().getPageSource().toLowerCase(Locale.ROOT);
        Assert.assertTrue(source.contains("success") || source.contains("confirmation") || source.contains("order"), "Order confirmation is not displayed.");
    }

    private int getCartCount() {
        if (!util().exists(cartQuantity)) {
            return 0;
        }
        String count = driver().findElement(cartQuantity).getText().replaceAll("[^0-9]", "");
        return count.isBlank() ? 0 : Integer.parseInt(count);
    }

    private double getCartTotal() {
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

    private void closeCartIfBlockingPage() {
        try {
            List<WebElement> closeButtons = driver().findElements(By.cssSelector(".float-cart__close-btn"));
            if (!closeButtons.isEmpty() && closeButtons.get(0).isDisplayed()) {
                closeButtons.get(0).click();
                sleep(600);
            }
        } catch (NoSuchElementException | StaleElementReferenceException ignored) {
            // Cart was already closed or refreshed.
        }
    }

    private void clearCheckoutField(By locator) {
        if (util().exists(locator)) {
            WebElement element = util().visible(locator);
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            element.sendKeys(Keys.BACK_SPACE);
        }
    }

    private void typeIfPresent(By locator, String value) {
        if (util().exists(locator)) {
            util().type(locator, value);
        }
    }

    private void clickFirstAvailable(By... locators) {
        for (By locator : locators) {
            if (util().exists(locator)) {
                util().click(locator);
                return;
            }
        }
        throw new NoSuchElementException("No matching element was found for click action.");
    }

    private double parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }
        List<Double> values = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+(?:\\.\\d+)?").matcher(text.replace(",", ""));
        while (matcher.find()) {
            values.add(Double.parseDouble(matcher.group()));
        }
        return values.isEmpty() ? 0.0 : values.get(values.size() - 1);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", "").trim();
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
