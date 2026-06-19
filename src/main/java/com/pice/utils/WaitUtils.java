package com.pice.utils;

import com.pice.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Explicit wait utilities — eliminates the need for Thread.sleep().
 * All wait methods use configurable timeouts with sensible defaults.
 */
public final class WaitUtils {

    private static final Logger log = LogManager.getLogger(WaitUtils.class);
    private static final int DEFAULT_TIMEOUT = 8;
    private static final int DEFAULT_POLL_INTERVAL = 500; // ms

    private WaitUtils() {
        // Prevent instantiation
    }

    private static WebDriverWait getWait(int timeoutSeconds) {
        AppiumDriver driver = DriverManager.getDriver();
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds),
                Duration.ofMillis(DEFAULT_POLL_INTERVAL));
    }

    // ==================== Visibility ====================

    public static WebElement waitForVisible(By locator) {
        return waitForVisible(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForVisible(By locator, int timeoutSeconds) {
        log.debug("Waiting for element to be visible: {} (timeout: {}s)", locator, timeoutSeconds);
        return getWait(timeoutSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static List<WebElement> waitForAllVisible(By locator) {
        return waitForAllVisible(locator, DEFAULT_TIMEOUT);
    }

    public static List<WebElement> waitForAllVisible(By locator, int timeoutSeconds) {
        log.debug("Waiting for all elements to be visible: {} (timeout: {}s)", locator, timeoutSeconds);
        return getWait(timeoutSeconds).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    // ==================== Clickability ====================

    public static WebElement waitForClickable(By locator) {
        return waitForClickable(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForClickable(By locator, int timeoutSeconds) {
        log.debug("Waiting for element to be clickable: {} (timeout: {}s)", locator, timeoutSeconds);
        return getWait(timeoutSeconds).until(ExpectedConditions.elementToBeClickable(locator));
    }

    // ==================== Presence ====================

    public static WebElement waitForPresence(By locator) {
        return waitForPresence(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForPresence(By locator, int timeoutSeconds) {
        log.debug("Waiting for element presence: {} (timeout: {}s)", locator, timeoutSeconds);
        return getWait(timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ==================== Text ====================

    public static boolean waitForTextPresent(By locator, String text) {
        return waitForTextPresent(locator, text, DEFAULT_TIMEOUT);
    }

    public static boolean waitForTextPresent(By locator, String text, int timeoutSeconds) {
        log.debug("Waiting for text '{}' in element: {} (timeout: {}s)", text, locator, timeoutSeconds);
        return getWait(timeoutSeconds).until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    // ==================== Invisibility ====================

    public static boolean waitForInvisible(By locator) {
        return waitForInvisible(locator, DEFAULT_TIMEOUT);
    }

    public static boolean waitForInvisible(By locator, int timeoutSeconds) {
        log.debug("Waiting for element to become invisible: {} (timeout: {}s)", locator, timeoutSeconds);
        return getWait(timeoutSeconds).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ==================== Conditional Checks ====================

    /**
     * Check if an element is displayed without waiting.
     *
     * @param locator the element locator
     * @return true if the element exists and is displayed
     */
    public static boolean isElementDisplayed(By locator) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if an element is present (in DOM) within a short timeout.
     *
     * @param locator        the element locator
     * @param timeoutSeconds max wait time
     * @return true if the element is found
     */
    public static boolean isElementPresent(By locator, int timeoutSeconds) {
        try {
            getWait(timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
