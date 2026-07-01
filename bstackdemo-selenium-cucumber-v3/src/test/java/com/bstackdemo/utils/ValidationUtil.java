package com.bstackdemo.utils;

import com.bstackdemo.base.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern STATUS_PATTERN = Pattern.compile("\\\"Status\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern STATE_PATTERN = Pattern.compile("\\\"State\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern DISTRICT_PATTERN = Pattern.compile("\\\"District\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"", Pattern.CASE_INSENSITIVE);

    private ValidationUtil() {
    }

    public static boolean isEmailValidUsingBrowser(String email) {
        String value = email == null ? "" : email.trim();

        WebDriver driver = DriverFactory.getDriver();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object browserResult = js.executeScript(
                "const input = document.createElement('input');" +
                        "input.type = 'email';" +
                        "input.required = true;" +
                        "input.value = arguments[0];" +
                        "return input.checkValidity();",
                value
        );

        // HTML5 allows a single-label domain like user@gmail.
        // For this project we apply stricter business validation and require a domain extension.
        boolean strictBusinessRule = value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return Boolean.TRUE.equals(browserResult) && strictBusinessRule;
    }

    public static String lookupPincodeUsingBrowser(String pincode) {
        WebDriver driver = DriverFactory.getDriver();
        String originalWindow = driver.getWindowHandle();
        Set<String> existingWindows = driver.getWindowHandles();

        String encodedPincode = URLEncoder.encode(pincode == null ? "" : pincode, StandardCharsets.UTF_8);
        String apiUrl = ConfigReader.get("postalApiBaseUrl", "https://api.postalpincode.in/pincode/") + encodedPincode;

        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", apiUrl);

        String newWindow = driver.getWindowHandles()
                .stream()
                .filter(handle -> !existingWindows.contains(handle))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to open postal lookup browser tab"));

        String responseText;
        try {
            driver.switchTo().window(newWindow);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.timeout()));
            WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
            responseText = body.getText();
        } catch (TimeoutException e) {
            responseText = "";
        } finally {
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        return responseText == null ? "" : responseText;
    }

    public static boolean isPincodeValid(String postalLookupResponse) {
        String status = extractFirst(STATUS_PATTERN, postalLookupResponse);
        return "success".equalsIgnoreCase(status)
                && postalLookupResponse != null
                && postalLookupResponse.toLowerCase(Locale.ROOT).contains("postoffice")
                && !postalLookupResponse.toLowerCase(Locale.ROOT).contains("\"postoffice\":null");
    }

    public static boolean isAddressMatchingPincode(String address, String state, String postalLookupResponse) {
        if (!isPincodeValid(postalLookupResponse)) {
            return false;
        }

        String expectedState = normalize(state);
        String normalizedAddress = normalize(address);
        List<String> states = extractAll(STATE_PATTERN, postalLookupResponse);
        List<String> districts = extractAll(DISTRICT_PATTERN, postalLookupResponse);

        boolean stateMatchesApi = states.stream().map(ValidationUtil::normalize).anyMatch(apiState -> apiState.equals(expectedState));
        boolean addressContainsState = normalizedAddress.contains(expectedState);
        boolean addressContainsDistrict = districts.stream()
                .map(ValidationUtil::normalize)
                .filter(value -> !value.isBlank())
                .anyMatch(normalizedAddress::contains);

        return stateMatchesApi && (addressContainsState || addressContainsDistrict);
    }

    private static String extractFirst(Pattern pattern, String input) {
        if (input == null) {
            return "";
        }
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static List<String> extractAll(Pattern pattern, String input) {
        List<String> values = new ArrayList<>();
        if (input == null) {
            return values;
        }
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            values.add(matcher.group(1));
        }
        return values;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "").trim();
    }
}
