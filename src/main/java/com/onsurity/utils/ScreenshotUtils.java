package com.onsurity.utils;

import com.onsurity.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Screenshot capture utilities for failure debugging and report evidence.
 * Supports full-page screenshots with organized directory structure.
 */
public final class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "test-output/screenshots";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private ScreenshotUtils() {
        // Prevent instantiation
    }

    /**
     * Capture a screenshot and save it with the given name.
     *
     * @param screenshotName descriptive name for the screenshot
     * @return absolute path to the saved screenshot, or null on failure
     */
    public static String captureScreenshot(String screenshotName) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            File srcFile = driver.getScreenshotAs(OutputType.FILE);

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String fileName = sanitizeFileName(screenshotName) + "_" + timestamp + ".png";

            Path destDir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(destDir);

            Path destPath = destDir.resolve(fileName);
            Files.copy(srcFile.toPath(), destPath);

            String absolutePath = destPath.toAbsolutePath().toString();
            log.info("Screenshot saved: {}", absolutePath);
            return absolutePath;
        } catch (IOException e) {
            log.error("Failed to save screenshot: {}", screenshotName, e);
            return null;
        } catch (Exception e) {
            log.error("Failed to capture screenshot: {}", screenshotName, e);
            return null;
        }
    }

    /**
     * Capture a screenshot and return it as Base64 string.
     * Useful for embedding directly in ExtentReports.
     *
     * @return Base64-encoded screenshot string, or null on failure
     */
    public static String captureScreenshotAsBase64() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            return driver.getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            log.error("Failed to capture Base64 screenshot", e);
            return null;
        }
    }

    /**
     * Sanitize filename by replacing special characters.
     */
    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
