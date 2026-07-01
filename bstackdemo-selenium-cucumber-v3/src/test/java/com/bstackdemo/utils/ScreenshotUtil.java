package com.bstackdemo.utils;

import com.bstackdemo.base.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtil {
    private ScreenshotUtil() {
    }

    public static String capture(String scenarioName) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver == null) {
            return "Driver is null. Screenshot not captured.";
        }

        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String safeName = scenarioName.replaceAll("[^a-zA-Z0-9-_]", "_");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path destination = Path.of("screenshots", safeName + "_" + timestamp + ".png");

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(source.toPath(), destination);
            return destination.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to capture screenshot", e);
        }
    }
}
