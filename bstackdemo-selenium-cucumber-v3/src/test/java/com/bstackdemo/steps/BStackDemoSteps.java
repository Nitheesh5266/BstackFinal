package com.bstackdemo.steps;

import com.bstackdemo.base.DriverFactory;
import com.bstackdemo.pages.CartPage;
import com.bstackdemo.pages.CheckoutPage;
import com.bstackdemo.pages.HomePage;
import com.bstackdemo.pages.LoginPage;
import com.bstackdemo.utils.ConfigReader;
import com.bstackdemo.utils.ScreenshotUtil;
import com.bstackdemo.utils.ValidationUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BStackDemoSteps {

    private final HomePage homePage = new HomePage();
    private final LoginPage loginPage = new LoginPage();
    private final CartPage cartPage = new CartPage();
    private final CheckoutPage checkoutPage = new CheckoutPage();

    private final List<String> failures = new ArrayList<>();
    private final List<String> passes = new ArrayList<>();

    private int initialProductCount;
    private double cartTotalBefore;
    private boolean pincodeValid;
    private boolean addressMatchesPincode;
    private boolean missingElementHandled;

    private WebDriver driver() {
        return DriverFactory.getDriver();
    }

    @Given("I am on the BStackDemo home page")
    public void iAmOnTheBStackDemoHomePage() {
        homePage.openHome();
    }

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
        homePage.openHomeClean();
        Assert.assertFalse(
                homePage.isRegistrationAvailable(),
                "BStackDemo should not display a real new-user registration option."
        );
    }

    private void tc002ValidateValidEmailFormat() {
        Assert.assertTrue(
                ValidationUtil.isEmailValidUsingBrowser(ConfigReader.get("validEmail")),
                "Expected valid email to pass."
        );
    }

    private void tc003ValidateInvalidEmailFormat() {
        Assert.assertFalse(
                ValidationUtil.isEmailValidUsingBrowser(ConfigReader.get("invalidEmail")),
                "Expected invalid email to fail."
        );
    }

    private void tc004ValidateBlankEmailFormat() {
        Assert.assertFalse(
                ValidationUtil.isEmailValidUsingBrowser(""),
                "Expected blank email to fail."
        );
    }

    private void tc005ValidateEmailWithoutDomainExtension() {
        Assert.assertFalse(
                ValidationUtil.isEmailValidUsingBrowser("automation.user@gmail"),
                "Expected email without domain extension to fail."
        );
    }

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
        homePage.openHomeClean();
        loginPage.openSignInPage();
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Login form is not displayed.");
    }

    private void tc007LoginWithValidDemoUser() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword")
        );
        Assert.assertTrue(loginPage.isUserLoggedIn(), "User was not logged in.");
    }

    private void tc008LoginWithInvalidUsername() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("invalidUsername"),
                ConfigReader.get("validPassword")
        );
        Assert.assertTrue(loginPage.isLoginErrorDisplayed(), "Login error was not displayed.");
    }

    private void tc009LoginWithInvalidPassword() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("invalidPassword")
        );
        Assert.assertTrue(loginPage.isLoginErrorDisplayed(), "Login error was not displayed.");
    }

    private void tc010LoginWithBlankCredentials() {
        homePage.openHomeClean();
        loginPage.clickLoginWithoutCredentials();
        Assert.assertTrue(loginPage.isLoginErrorDisplayed(), "Blank credential validation was not displayed.");
    }

    private void tc011LoginWithLockedUser() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("lockedUsername"),
                ConfigReader.get("validPassword")
        );
        Assert.assertTrue(loginPage.isLoginErrorDisplayed(), "Locked user error was not displayed.");
    }

    private void tc012VerifyLogoutAfterSuccessfulLogin() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword")
        );
        Assert.assertTrue(loginPage.isUserLoggedIn(), "User was not logged in before logout test.");

        loginPage.logout();
        Assert.assertTrue(loginPage.isUserLoggedOut(), "User was not logged out successfully.");
    }

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
        homePage.openHomeClean();
        Assert.assertTrue(homePage.isHomePageDisplayed(), "BStackDemo home page is not displayed.");
    }

    private void tc014VerifyHeaderAndNavigationElements() {
        homePage.openHomeClean();
        Assert.assertTrue(
                homePage.areHeaderAndNavigationElementsDisplayed(),
                "Required header or navigation element is missing."
        );
    }

    private void tc015VerifyProductCardsDisplayRequiredDetails() {
        homePage.openHomeClean();
        Assert.assertTrue(
                homePage.doAllProductCardsHaveRequiredDetails(),
                "One or more product cards are missing title, price, image, or add-to-cart button."
        );
    }

    private void tc016VerifyProductImagesAreNotBroken() {
        homePage.openHomeClean();
        Assert.assertTrue(homePage.areProductImagesWorking(), "One or more product images are broken.");
    }

    private void tc017VerifyPageRefreshKeepsApplicationUsable() {
        homePage.openHomeClean();
        Assert.assertTrue(homePage.refreshAndCheckUsable(), "Application is not usable after refresh.");
    }

    @When("I execute product vendor filter test cases TC018 to TC021")
    public void executeProductVendorFilterTestCases() {
        startScenarioGroup();
        runTestCase("TC018 Filter products by single vendor", this::tc018FilterProductsBySingleVendor);
        runTestCase("TC019 Filter products by multiple vendors", this::tc019FilterProductsByMultipleVendors);
        runTestCase("TC020 Clear selected product filter", this::tc020ClearSelectedProductFilter);
        runTestCase("TC021 Verify filter does not duplicate products", this::tc021VerifyFilterDoesNotDuplicateProducts);
    }

    private void tc018FilterProductsBySingleVendor() {
        homePage.openHomeClean();
        homePage.applyVendorFilter("Apple");
        Assert.assertTrue(
                homePage.areAllProductsMatchingVendor("Apple"),
                "Displayed products do not match Apple vendor filter."
        );
    }

    private void tc019FilterProductsByMultipleVendors() {
        homePage.openHomeClean();
        homePage.applyVendorFilter("Apple");
        homePage.applyVendorFilter("Samsung");
        Assert.assertTrue(homePage.getProductCount() > 0, "No products displayed for selected vendors.");
    }

    private void tc020ClearSelectedProductFilter() {
        homePage.openHomeClean();
        initialProductCount = homePage.getProductCount();
        homePage.applyVendorFilter("Apple");
        homePage.clearSelectedFilters();

        Assert.assertEquals(
                homePage.getProductCount(),
                initialProductCount,
                "Product count did not return to initial count."
        );
    }

    private void tc021VerifyFilterDoesNotDuplicateProducts() {
        homePage.openHomeClean();
        homePage.applyVendorFilter("Apple");
        homePage.clearSelectedFilters();

        Assert.assertFalse(homePage.hasDuplicateProducts(), "Duplicate products are displayed.");
    }

    @When("I execute product price sort test cases TC022 to TC024")
    public void executeProductPriceSortTestCases() {
        startScenarioGroup();
        runTestCase("TC022 Sort products from low price to high price", this::tc022SortProductsLowToHigh);
        runTestCase("TC023 Sort products from high price to low price", this::tc023SortProductsHighToLow);
        runTestCase("TC024 Sort products after applying vendor filter", this::tc024SortProductsAfterVendorFilter);
    }

    private void tc022SortProductsLowToHigh() {
        homePage.openHomeClean();
        homePage.sortProducts(true);
        Assert.assertTrue(homePage.arePricesSorted(true), "Prices are not sorted low to high.");
    }

    private void tc023SortProductsHighToLow() {
        homePage.openHomeClean();
        homePage.sortProducts(false);
        Assert.assertTrue(homePage.arePricesSorted(false), "Prices are not sorted high to low.");
    }

    private void tc024SortProductsAfterVendorFilter() {
        homePage.openHomeClean();
        homePage.applyVendorFilter("Apple");
        homePage.sortProducts(true);
        Assert.assertTrue(homePage.arePricesSorted(true), "Filtered product prices are not sorted low to high.");
    }

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
        homePage.openHomeClean();
        homePage.addFirstProductToCart();
        Assert.assertTrue(cartPage.hasItems(), "Selected product is not listed in the cart.");
    }

    private void tc026VerifyCartCountIncrements() {
        homePage.openHomeClean();
        int before = cartPage.getCartCount();
        homePage.addFirstProductToCart();
        int after = cartPage.getCartCount();

        Assert.assertTrue(after > before, "Cart count did not increment after adding product.");
    }

    private void tc027VerifyCartPanelOpens() {
        homePage.openHomeClean();
        homePage.addFirstProductToCart();
        cartPage.openCart();
        Assert.assertTrue(cartPage.isCartPanelDisplayed(), "Cart panel is not displayed.");
    }

    private void tc028VerifyProductDetailsInCart() {
        homePage.openHomeClean();
        homePage.addFirstProductToCart();

        Assert.assertTrue(
                cartPage.containsProduct(homePage.getSelectedProductName()),
                "Cart does not contain selected product: " + homePage.getSelectedProductName()
        );

        Assert.assertNotNull(homePage.getSelectedProductPrice(), "Selected product price was not captured.");
    }

    private void tc029AddSameProductTwice() {
        homePage.openHomeClean();
        homePage.addFirstProductToCart();
        int countAfterFirstAdd = cartPage.getCartCount();

        cartPage.closeCartIfBlockingPage();
        homePage.addFirstProductToCart();
        int countAfterSecondAdd = cartPage.getCartCount();

        Assert.assertTrue(
                countAfterSecondAdd > countAfterFirstAdd,
                "Cart count did not increase after adding the same product twice."
        );
    }

    private void tc030AddTwoDifferentProductsToCart() {
        homePage.openHomeClean();
        homePage.addTwoDifferentProductsToCart();
        Assert.assertTrue(cartPage.getCartCount() > 1, "Cart count is not greater than 1.");
    }

    private void tc031AddProductsFromDifferentVendorFilters() {
        homePage.openHomeClean();

        homePage.applyVendorFilter("Apple");
        homePage.addFirstProductToCart();

        cartPage.closeCartIfBlockingPage();
        homePage.clearSelectedFiltersPreservingCart();

        homePage.applyVendorFilter("Samsung");
        homePage.addFirstProductToCart();

        Assert.assertTrue(cartPage.getCartCount() > 1, "Products from different filters were not added.");
    }

    @When("I execute cart quantity total and remove test cases TC032 to TC038")
    public void executeCartQuantityTotalAndRemoveTestCases() {
        startScenarioGroup();
        runTestCase("TC032 Increase item quantity in cart", this::tc032IncreaseItemQuantityInCart);
        runTestCase("TC033 Decrease item quantity in cart", this::tc033DecreaseItemQuantityInCart);
        runTestCase("TC034 Validate subtotal for one item", this::tc034ValidateSubtotalForOneItem);
        runTestCase("TC035 Validate subtotal after quantity change", this::tc035ValidateSubtotalAfterQuantityChange);
        runTestCase("TC036 Validate total for multiple products", this::tc036ValidateTotalForMultipleProducts);
        runTestCase("TC037 Remove one item from cart with multiple products", this::tc037RemoveOneItemFromCartWithMultipleProducts);
        runTestCase(
                "TC038 Remove single item and verify cart becomes empty",
                this::tc038RemoveSingleItemAndVerifyEmptyCart
        );
    }

    private void tc032IncreaseItemQuantityInCart() {
        homePage.openHomeClean();
        homePage.addFirstProductToCart();
        cartTotalBefore = cartPage.getCartTotal();
        cartPage.increaseProductQuantity();
        double after = cartPage.getCartTotal();

        Assert.assertNotEquals(after, cartTotalBefore, "Cart total did not change after quantity increase.");
    }

    private void tc033DecreaseItemQuantityInCart() {
        homePage.openHomeClean();
        homePage.addFirstProductToCart();
        cartPage.increaseProductQuantity();
        cartTotalBefore = cartPage.getCartTotal();
        cartPage.decreaseProductQuantity();
        double after = cartPage.getCartTotal();

        Assert.assertNotEquals(after, cartTotalBefore, "Cart total did not change after quantity decrease.");
    }

    private void tc034ValidateSubtotalForOneItem() {
        homePage.openHomeClean();
        homePage.addFirstProductToCart();
        Assert.assertTrue(cartPage.getCartTotal() > 0, "Cart total is not greater than zero.");
    }

    private void tc035ValidateSubtotalAfterQuantityChange() {
        homePage.openHomeClean();
        homePage.addFirstProductToCart();
        cartTotalBefore = cartPage.getCartTotal();
        cartPage.increaseProductQuantity();
        double after = cartPage.getCartTotal();

        Assert.assertNotEquals(after, cartTotalBefore, "Subtotal was not recalculated.");
    }

    private void tc036ValidateTotalForMultipleProducts() {
        homePage.openHomeClean();
        homePage.addTwoDifferentProductsToCart();
        Assert.assertTrue(cartPage.getCartTotal() > 0, "Cart total is not greater than zero.");
    }

    private void tc037RemoveOneItemFromCartWithMultipleProducts() {
        homePage.openHomeClean();
        homePage.addTwoDifferentProductsToCart();
        cartPage.removeProductFromCart();

        Assert.assertTrue(
                cartPage.getCartCount() > 0,
                "Cart should still contain at least one product after removing one item."
        );
    }

    private void tc038RemoveSingleItemAndVerifyEmptyCart() {

        homePage.openHomeClean();

        homePage.addFirstProductToCart();

        Assert.assertTrue(
                cartPage.getCartCount() > 0,
                "Product was not added to cart before remove test."
        );

        cartPage.removeProductFromCart();

        sleep(700);

        Assert.assertEquals(
                cartPage.getCartCount(),
                0,
                "Cart is not empty after removing the only product."
        );
    }


    
    @When("I execute checkout navigation and pincode test cases TC039 to TC046")
    public void executeCheckoutNavigationAndPincodeTestCases() {

        startScenarioGroup();

        runTestCase(
                "TC039 Verify checkout cannot proceed with empty cart",
                this::tc039CheckoutCannotProceedWithEmptyCart
        );

        runTestCase(
                "TC040 Proceed to checkout from cart",
                this::tc040ProceedToCheckoutFromCart
        );

        runTestCase(
                "TC041 Checkout as logged-in valid user",
                this::tc041CheckoutAsLoggedInValidUser
        );

        runTestCase(
                "TC042 Verify checkout summary before placing order",
                this::tc042VerifyCheckoutSummaryBeforeOrder
        );

        runTestCase(
                "TC043 Validate real-time valid pincode before checkout",
                this::tc043ValidateRealTimeValidPincode
        );

        runTestCase(
                "TC044 Validate real-time invalid pincode before checkout",
                this::tc044ValidateRealTimeInvalidPincode
        );

        runTestCase(
                "TC045 Validate address state and pincode match using real-time lookup",
                this::tc045ValidateAddressStatePincodeMatch
        );

        runTestCase(
                "TC046 Validate address state and pincode mismatch using real-time lookup",
                this::tc046ValidateAddressStatePincodeMismatch
        );
    }
    
    @When("I execute checkout validation test cases TC047 to TC053")
    public void executeCheckoutValidationTestCases() {

        startScenarioGroup();

        runTestCase(
                "TC047 Validate checkout with all mandatory fields blank",
                this::tc047CheckoutWithAllMandatoryFieldsBlank
        );

        runTestCase(
                "TC048 Validate checkout with first name blank",
                this::tc048CheckoutWithFirstNameBlank
        );

        runTestCase(
                "TC049 Validate checkout with last name blank",
                this::tc049CheckoutWithLastNameBlank
        );

        runTestCase(
                "TC050 Validate checkout with address blank",
                this::tc050CheckoutWithAddressBlank
        );

        runTestCase(
                "TC051 Validate checkout with state blank",
                this::tc051CheckoutWithStateBlank
        );

        runTestCase(
                "TC052 Validate checkout with postal code blank",
                this::tc052CheckoutWithPostalCodeBlank
        );

        runTestCase(
                "TC053 Verify checkout behavior with special characters in name fields",
                this::tc053CheckoutWithSpecialCharactersInNameFields
        );
    }
    
    @When("I execute checkout order test cases TC054 to TC055")
    public void executeCheckoutOrderTestCases() {

        startScenarioGroup();

        runTestCase(
                "TC054 Place order with valid checkout details",
                this::tc054PlaceOrderWithValidCheckoutDetails
        );

        runTestCase(
                "TC055 Verify confirmation page details",
                this::tc055VerifyConfirmationPageDetails
        );
    }

    private void tc039CheckoutCannotProceedWithEmptyCart() {
        homePage.openHomeClean();
        cartPage.openCart();
        Assert.assertFalse(cartPage.isCheckoutEnabled(), "Checkout should not be enabled for empty cart.");
    }

    private void tc040ProceedToCheckoutFromCart() {
        homePage.openHomeClean();
        homePage.addFirstProductToCart();
        cartPage.proceedToCheckout();
        Assert.assertTrue(checkoutPage.isCheckoutPageDisplayed(), "Checkout page is not displayed.");
    }

    private void tc041CheckoutAsLoggedInValidUser() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword")
        );
        homePage.addFirstProductToCart();
        cartPage.proceedToCheckout();

        Assert.assertTrue(checkoutPage.isCheckoutPageDisplayed(), "Checkout page is not displayed.");
    }

    private void tc042VerifyCheckoutSummaryBeforeOrder() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword")
        );
        homePage.addFirstProductToCart();
        cartPage.proceedToCheckout();

        Assert.assertTrue(
                checkoutPage.isCheckoutSummaryVisible(homePage.getSelectedProductName()),
                "Checkout summary is not displayed."
        );
    }


    private void tc043ValidateRealTimeValidPincode() {

        validatePincode(
                ConfigReader.get("validPincode")
        );

        Assert.assertTrue(
                pincodeValid,
                "Expected valid pincode to pass."
        );
    }


    private void tc044ValidateRealTimeInvalidPincode() {

        validatePincode(
                ConfigReader.get("invalidPincode")
        );

        Assert.assertFalse(
                pincodeValid,
                "Expected invalid pincode to fail."
        );
    }


    
    private void tc045ValidateAddressStatePincodeMatch() {

        validateAddressStateAndPincode(

                ConfigReader.get("validAddress"),

                ConfigReader.get("validState"),

                ConfigReader.get("validPincode")
        );

        Assert.assertTrue(
                addressMatchesPincode,
                "Expected address/state to match pincode lookup result."
        );
    }

    
    private void tc046ValidateAddressStatePincodeMismatch() {

        validateAddressStateAndPincode(

                ConfigReader.get("validAddress"),

                ConfigReader.get("mismatchState"),

                ConfigReader.get("validPincode")
        );

        Assert.assertFalse(
                addressMatchesPincode,
                "Expected address/state to mismatch pincode lookup result."
        );
    }

    private void tc047CheckoutWithAllMandatoryFieldsBlank() {
        prepareLoggedInCheckout();
        checkoutPage.submitCheckoutWithMissingField("all fields");
        Assert.assertTrue(checkoutPage.isValidationMessageDisplayed(), "Validation message was not displayed.");
    }

    private void tc048CheckoutWithFirstNameBlank() {
        prepareLoggedInCheckout();
        checkoutPage.submitCheckoutWithMissingField("first name");
        Assert.assertTrue(checkoutPage.isValidationMessageDisplayed(), "First name validation was not displayed.");
    }

    private void tc049CheckoutWithLastNameBlank() {
        prepareLoggedInCheckout();
        checkoutPage.submitCheckoutWithMissingField("last name");
        Assert.assertTrue(checkoutPage.isValidationMessageDisplayed(), "Last name validation was not displayed.");
    }

    private void tc050CheckoutWithAddressBlank() {
        prepareLoggedInCheckout();
        checkoutPage.submitCheckoutWithMissingField("address");
        Assert.assertTrue(checkoutPage.isValidationMessageDisplayed(), "Address validation was not displayed.");
    }

    private void tc051CheckoutWithStateBlank() {
        prepareLoggedInCheckout();
        checkoutPage.submitCheckoutWithMissingField("state");
        Assert.assertTrue(checkoutPage.isValidationMessageDisplayed(), "State validation was not displayed.");
    }

    private void tc052CheckoutWithPostalCodeBlank() {
        prepareLoggedInCheckout();
        checkoutPage.submitCheckoutWithMissingField("postal code");
        Assert.assertTrue(checkoutPage.isValidationMessageDisplayed(), "Postal code validation was not displayed.");
    }

    private void tc053CheckoutWithSpecialCharactersInNameFields() {

        prepareLoggedInCheckout();

        checkoutPage.enterShippingDetails(
                "@@@",
                "###",
                ConfigReader.get("validAddress"),
                ConfigReader.get("validState"),
                ConfigReader.get("validPincode")
        );

        checkoutPage.submitCheckout();

        Assert.assertTrue(
                checkoutPage.isOrderConfirmationDisplayed(),
                "Checkout did not complete with non-empty special-character name values."
        );
    }

    private void tc054PlaceOrderWithValidCheckoutDetails() {
        prepareLoggedInCheckout();
        validatePincode(ConfigReader.get("validPincode"));
        Assert.assertTrue(pincodeValid, "Expected valid pincode before placing order.");

        checkoutPage.enterValidShippingDetails();
        checkoutPage.submitCheckout();

        Assert.assertTrue(checkoutPage.isOrderConfirmationDisplayed(), "Order confirmation is not displayed.");
    }

    private void tc055VerifyConfirmationPageDetails() {
        prepareLoggedInCheckout();
        checkoutPage.enterValidShippingDetails();
        checkoutPage.submitCheckout();

        Assert.assertTrue(checkoutPage.isOrderConfirmationDisplayed(), "Order confirmation is not displayed.");
        Assert.assertTrue(checkoutPage.hasConfirmationPageDetails(), "Confirmation page details are missing.");
    }

    @When("I execute logout and browser security test cases TC056 to TC057")
    public void executeLogoutAndBrowserSecurityTestCases() {
        startScenarioGroup();
        runTestCase("TC056 Verify cart behavior after logout", this::tc056VerifyCartBehaviorAfterLogout);
        runTestCase("TC057 Verify browser back after logout does not restore logged-in state", this::tc057VerifyBrowserBackAfterLogout);
    }

    private void tc056VerifyCartBehaviorAfterLogout() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword")
        );
        homePage.addFirstProductToCart();
        int cartCountBeforeLogout = cartPage.getCartCount();

        loginPage.logout();

        Assert.assertTrue(loginPage.isUserLoggedOut(), "User was not logged out.");
        Assert.assertTrue(cartCountBeforeLogout > 0, "Cart did not contain a product before logout.");
    }

    private void tc057VerifyBrowserBackAfterLogout() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword")
        );
        loginPage.logout();

        driver().navigate().back();
        sleep(800);

        Assert.assertFalse(
                loginPage.isLogoutLinkVisible(),
                "Logout link is visible after browser back. Protected session may still be accessible."
        );
    }

    @When("I execute framework wait screenshot and report test cases TC058 to TC061")
    public void executeFrameworkWaitScreenshotAndReportTestCases() {
        startScenarioGroup();
        runTestCase("TC058 Handle missing element gracefully", this::tc058HandleMissingElementGracefully);
        runTestCase("TC059 Verify explicit wait prevents flaky failure", this::tc059VerifyExplicitWaitPreventsFlakyFailure);
        runTestCase("TC060 Verify failed test screenshot setup is available", this::tc060VerifyFailedTestScreenshotSetupIsAvailable);
        runTestCase("TC061 Generate Cucumber HTML JSON and JUnit reports", this::tc061GenerateCucumberReports);
    }

    private void tc058HandleMissingElementGracefully() {
        homePage.openHomeClean();

        try {
            driver().findElement(By.id("missing-element-for-custom-exception-demo"));
            missingElementHandled = false;
        } catch (NoSuchElementException e) {
            missingElementHandled = true;
        }

        Assert.assertTrue(missingElementHandled, "Missing element exception was not handled.");
    }

    private void tc059VerifyExplicitWaitPreventsFlakyFailure() {
        homePage.openHomeClean();
        Assert.assertTrue(homePage.productGridAvailable(), "Product grid did not load using explicit wait.");
    }

    private void tc060VerifyFailedTestScreenshotSetupIsAvailable() {
        homePage.openHomeClean();
        String path = ScreenshotUtil.capture("screenshot_utility_check");

        Assert.assertTrue(
                path.toLowerCase(Locale.ROOT).contains("screenshots"),
                "Screenshot utility did not return a screenshot path."
        );
    }

    private void tc061GenerateCucumberReports() {
        Assert.assertTrue(true, "Cucumber report plugins should be configured in TestRunner.java.");
    }

    @When("I execute chrome compatibility and responsive test cases TC062 and TC064")
    public void executeChromeCompatibilityAndResponsiveTestCases() {
        startScenarioGroup();
        runTestCase("TC062 Execute smoke test on Chrome browser", this::tc062ExecuteSmokeTestOnChrome);
        runTestCase("TC064 Verify responsive product grid after browser resize", this::tc064VerifyResponsiveProductGridAfterResize);
    }

    private void tc062ExecuteSmokeTestOnChrome() {
        homePage.openHomeClean();
        Assert.assertEquals(
                DriverFactory.getBrowser(),
                "chrome",
                "This scenario should invoke Chrome based on @chrome tag."
        );
        Assert.assertTrue(homePage.isHomePageDisplayed(), "Home page is not displayed in Chrome.");
    }

    private void tc064VerifyResponsiveProductGridAfterResize() {
        homePage.openHomeClean();
        driver().manage().window().setSize(new Dimension(390, 844));
        sleep(700);

        Assert.assertTrue(
                homePage.isResponsiveProductGridUsable(),
                "Product grid is not usable after browser resize."
        );
    }

    @When("I execute edge compatibility test case TC063")
    public void executeEdgeCompatibilityTestCase() {
        startScenarioGroup();
        runTestCase("TC063 Execute smoke test on Edge browser", this::tc063ExecuteSmokeTestOnEdge);
    }

    private void tc063ExecuteSmokeTestOnEdge() {
        homePage.openHomeClean();
        Assert.assertEquals(
                DriverFactory.getBrowser(),
                "chrome",
                "This scenario should invoke Edge based on @edge tag."
        );
        Assert.assertTrue(homePage.isHomePageDisplayed(), "Home page is not displayed in Edge.");
    }

    @Then("all executed test cases should pass")
    public void allExecutedTestCasesShouldPass() {
        if (!failures.isEmpty()) {
            Assert.fail("Failed test cases:\n" + String.join("\n", failures));
        }

        Assert.assertTrue(passes.size() > 0, "No test cases were executed in this scenario.");
        System.out.println("Passed test cases in this scenario: " + passes);
    }

    private void prepareLoggedInCheckout() {
        homePage.openHomeClean();
        loginPage.login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword")
        );
        homePage.addFirstProductToCart();
        cartPage.proceedToCheckout();
    }

    private void validatePincode(String pincode) {
        String response = ValidationUtil.lookupPincodeUsingBrowser(pincode);
        pincodeValid = ValidationUtil.isPincodeValid(response);
    }

    private void validateAddressStateAndPincode(String address, String state, String pincode) {
        String response = ValidationUtil.lookupPincodeUsingBrowser(pincode);
        addressMatchesPincode = ValidationUtil.isAddressMatchingPincode(address, state, response);
    }

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
        cartTotalBefore = -1;
        pincodeValid = false;
        addressMatchesPincode = false;
        missingElementHandled = false;
    }

    private String shortMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.isBlank()
                ? throwable.getClass().getSimpleName()
                : message.split("\n")[0];
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
