package com.bstackdemo.utils;

import com.bstackdemo.base.DriverFactory;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9._%+-]+@" +
                    "[A-Za-z0-9.-]+\\." +
                    "[A-Za-z]{2,}$"
            );

    private static WebDriver driver() {
        return DriverFactory.getDriver();
    }


    // =========================================================
    // Email validation
    // =========================================================

    public static boolean isEmailValidUsingBrowser(String email) {

        String value = email == null ? "" : email.trim();

        if (value.isBlank()) {
            return false;
        }

        boolean browserValid = false;

        try {

            Object result =
                    ((JavascriptExecutor) driver())
                            .executeScript(
                                    "const input = document.createElement('input');" +
                                    "input.type = 'email';" +
                                    "input.required = true;" +
                                    "input.value = arguments[0];" +
                                    "return input.checkValidity();",
                                    value
                            );

            browserValid =
                    Boolean.TRUE.equals(result);

        } catch (RuntimeException e) {

            browserValid = true;
        }

        return browserValid
                && EMAIL_PATTERN.matcher(value).matches();
    }


    // =========================================================
    // Real-time pincode lookup
    // =========================================================

    public static String lookupPincodeUsingBrowser(String pincode) {

        String value =
                pincode == null ? "" : pincode.trim();

        if (!value.matches("\\d{6}")) {
            return "";
        }

        String apiUrl =
                ConfigReader.get("postalApiBaseUrl")
                        + value;

        try {

            driver()
                    .manage()
                    .timeouts()
                    .scriptTimeout(
                            Duration.ofSeconds(
                                    ConfigReader.timeout()
                            )
                    );

            Object result =
                    ((JavascriptExecutor) driver())
                            .executeAsyncScript(
                                    "const url = arguments[0];" +
                                    "const done = arguments[arguments.length - 1];" +
                                    "fetch(url)" +
                                    ".then(response => response.text())" +
                                    ".then(text => done(text))" +
                                    ".catch(error => done('ERROR:' + error.message));",
                                    apiUrl
                            );

            return result == null
                    ? ""
                    : result.toString();

        } catch (RuntimeException e) {

            return "";
        }
    }


    // =========================================================
    // Validate pincode API response
    // =========================================================

    public static boolean isPincodeValid(String response) {

        if (response == null || response.isBlank()) {
            return false;
        }

        String normalized =
                response
                        .replaceAll("\\s+", "")
                        .toLowerCase(Locale.ROOT);

        boolean success =
                normalized.contains("\"status\":\"success\"");

        boolean postOfficeFound =
                normalized.contains("\"postoffice\":[{")
                        || normalized.contains("\"postoffice\":[");

        boolean postOfficeNull =
                normalized.contains("\"postoffice\":null");

        return success
                && postOfficeFound
                && !postOfficeNull;
    }


    // =========================================================
    // Validate address, state and pincode relationship
    // =========================================================

    public static boolean isAddressMatchingPincode(
            String address,
            String state,
            String response) {

        if (!isPincodeValid(response)) {
            return false;
        }

        String normalizedState =
                normalize(state);

        if (normalizedState.isBlank()) {
            return false;
        }

        List<String> responseStates =
                extractJsonValues(
                        response,
                        "State"
                );

        boolean stateMatches =
                responseStates
                        .stream()
                        .map(ValidationUtil::normalize)
                        .anyMatch(normalizedState::equals);

        if (!stateMatches) {
            return false;
        }

        String normalizedAddress =
                normalize(address);

        if (normalizedAddress.isBlank()) {
            return false;
        }

        List<String> locationValues =
                new ArrayList<>();

        locationValues.addAll(
                extractJsonValues(response, "Name")
        );

        locationValues.addAll(
                extractJsonValues(response, "District")
        );

        locationValues.addAll(
                extractJsonValues(response, "Division")
        );

        locationValues.addAll(
                extractJsonValues(response, "Region")
        );

        locationValues.addAll(
                extractJsonValues(response, "Block")
        );

        for (String location : locationValues) {

            String normalizedLocation =
                    normalize(location);

            if (normalizedLocation.isBlank()) {
                continue;
            }

            if (normalizedAddress.contains(normalizedLocation)
                    || normalizedLocation.contains(normalizedAddress)) {

                return true;
            }
        }

        Set<String> meaningfulAddressWords =
                getMeaningfulAddressWords(
                        normalizedAddress,
                        normalizedState
                );

        for (String location : locationValues) {

            String normalizedLocation =
                    normalize(location);

            for (String word : meaningfulAddressWords) {

                if (normalizedLocation.contains(word)) {
                    return true;
                }
            }
        }

        return false;
    }


    // =========================================================
    // Read values from simple JSON response
    // =========================================================

    private static List<String> extractJsonValues(
            String response,
            String key) {

        List<String> values =
                new ArrayList<>();

        Pattern pattern =
                Pattern.compile(
                        "\"" + Pattern.quote(key) +
                        "\"\\s*:\\s*\"([^\"]*)\"",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(response);

        while (matcher.find()) {

            String value =
                    matcher.group(1);

            if (value != null
                    && !value.isBlank()) {

                values.add(value);
            }
        }

        return values;
    }


    // =========================================================
    // Keep useful location words from address
    // =========================================================

    private static Set<String> getMeaningfulAddressWords(
            String normalizedAddress,
            String normalizedState) {

        Set<String> ignoredWords =
                Set.of(
                        "road",
                        "street",
                        "lane",
                        "main",
                        "near",
                        "area",
                        "house",
                        "flat",
                        "apartment",
                        "building",
                        "block",
                        "district",
                        "state",
                        "india"
                );

        Set<String> words =
                new HashSet<>();

        for (String word :
                normalizedAddress.split("\\s+")) {

            if (word.length() >= 4
                    && !ignoredWords.contains(word)
                    && !word.equals(normalizedState)) {

                words.add(word);
            }
        }

        return words;
    }


    // =========================================================
    // Normalize text before comparison
    // =========================================================

    private static String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
