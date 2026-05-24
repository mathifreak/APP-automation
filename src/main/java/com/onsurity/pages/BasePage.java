package com.onsurity.pages;

import com.onsurity.driver.DriverManager;
import com.onsurity.exceptions.ElementInteractionException;
import com.onsurity.exceptions.PageNotLoadedException;
import com.onsurity.utils.GestureUtils;
import com.onsurity.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Base page class for the Page Object Model.
 * All screen/page classes should extend this to inherit common mobile actions.
 *
 * <p><b>Industry-Standard Features:</b>
 * <ul>
 *   <li>Page Load Validation — auto-verifies the correct screen is loaded</li>
 *   <li>Resilient Actions — waitAndClick, safeClick with retry/fallback</li>
 *   <li>Custom Exceptions — descriptive error context on failures</li>
 *   <li>React Native locator priority — accessibilityId first</li>
 * </ul>
 *
 * <p><b>React Native Locator Strategy:</b>
 * React Native's `testID` prop maps to:
 * <ul>
 *   <li>Android: accessibility ID (content-desc)</li>
 *   <li>iOS: accessibility identifier</li>
 * </ul>
 * Use {@link AppiumBy#accessibilityId(String)} as the primary locator strategy.
 *
 * <p>Priority Order:
 * <ol>
 *   <li>Accessibility ID (testID) — ✅ Best, cross-platform</li>
 *   <li>ID (resource-id / name) — ✅ Good</li>
 *   <li>Class + index — ⚠️ Acceptable</li>
 *   <li>XPath — ❌ Last resort only</li>
 * </ol>
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());

    // ==================== Page Load Validation ====================

    /**
     * Subclasses should override this to return a locator that uniquely identifies the page.
     * Used by {@link #waitForPageLoad()} to verify the correct screen is displayed.
     *
     * <p>Return {@code null} to skip page load validation (not recommended).
     *
     * @return a By locator for a key element on this page, or null to skip validation
     */
    protected By getPageLoadedLocator() {
        return null; // Default: skip validation. Subclasses should override.
    }

    /**
     * Timeout in seconds for page load validation.
     * Subclasses can override for pages that take longer to load.
     *
     * @return page load timeout in seconds
     */
    protected int getPageLoadTimeout() {
        return 10;
    }

    /**
     * Wait for the page to be fully loaded by checking for the page-identifying element.
     * Throws {@link PageNotLoadedException} if the page is not loaded within the timeout.
     */
    protected void waitForPageLoad() {
        By locator = getPageLoadedLocator();
        if (locator == null) {
            return; // Skip validation if no locator defined
        }

        String pageName = getClass().getSimpleName();
        log.debug("Waiting for {} to load (timeout: {}s)", pageName, getPageLoadTimeout());

        if (!WaitUtils.isElementPresent(locator, getPageLoadTimeout())) {
            throw new PageNotLoadedException(pageName,
                    "Expected locator not found: " + locator);
        }
        log.debug("{} loaded successfully", pageName);
    }

    /**
     * Get the current AppiumDriver instance (thread-safe).
     */
    protected AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }

    // ==================== Element Finding ====================

    /**
     * Find element by React Native testID (accessibility ID).
     * This is the PREFERRED locator strategy for React Native apps.
     *
     * @param testId the testID value from the React Native component
     * @return the found WebElement
     */
    protected WebElement findByTestId(String testId) {
        return WaitUtils.waitForVisible(AppiumBy.accessibilityId(testId));
    }

    /**
     * Find element by resource ID (Android) or name (iOS).
     *
     * @param id the resource ID
     * @return the found WebElement
     */
    protected WebElement findById(String id) {
        return WaitUtils.waitForVisible(By.id(id));
    }

    /**
     * Find element by any locator strategy.
     *
     * @param locator the By locator
     * @return the found WebElement
     */
    protected WebElement find(By locator) {
        return WaitUtils.waitForVisible(locator);
    }

    /**
     * Find multiple elements by locator.
     *
     * @param locator the By locator
     * @return list of matching WebElements
     */
    protected List<WebElement> findAll(By locator) {
        return WaitUtils.waitForAllVisible(locator);
    }

    // ==================== Actions ====================

    /**
     * Tap on an element identified by testID.
     *
     * @param testId the React Native testID
     */
    protected void tap(String testId) {
        log.debug("Tapping element with testID: {}", testId);
        WebElement element = findByTestId(testId);
        element.click();
    }

    /**
     * Tap on an element identified by locator.
     *
     * @param locator the By locator
     */
    protected void tap(By locator) {
        log.debug("Tapping element: {}", locator);
        WaitUtils.waitForClickable(locator).click();
    }

    /**
     * Tap on an element by its visible text.
     * Uses XPath text matching — use sparingly.
     *
     * @param text the visible text to find and tap
     */
    protected void tapByText(String text) {
        log.debug("Tapping element with text: '{}'", text);
        By locator = By.xpath("//*[@text='" + text + "']");
        WaitUtils.waitForClickable(locator).click();
    }

    /**
     * Safe click with retry — catches stale element and retries once.
     * Use when dealing with dynamically updating React Native views.
     *
     * @param locator the By locator
     */
    protected void safeClick(By locator) {
        try {
            WaitUtils.waitForClickable(locator).click();
        } catch (Exception e) {
            log.warn("First click attempt failed on {}, retrying...", locator);
            try {
                Thread.sleep(500);
                WaitUtils.waitForClickable(locator, 5).click();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new ElementInteractionException("click", locator.toString(), ie);
            } catch (Exception e2) {
                throw new ElementInteractionException("click", locator.toString(), e2);
            }
        }
    }

    /**
     * Wait for element and click — combines explicit wait + click in one call.
     *
     * @param testId  the React Native testID
     * @param timeout timeout in seconds
     */
    protected void waitAndClick(String testId, int timeout) {
        log.debug("Wait-and-click on testID: {} (timeout: {}s)", testId, timeout);
        By locator = AppiumBy.accessibilityId(testId);
        WaitUtils.waitForClickable(locator, timeout).click();
    }

    /**
     * Wait for element and type — combines explicit wait + clear + sendKeys.
     *
     * @param testId  the React Native testID
     * @param text    the text to type
     * @param timeout timeout in seconds
     */
    protected void waitAndType(String testId, String text, int timeout) {
        log.debug("Wait-and-type '{}' into testID: {} (timeout: {}s)", text, testId, timeout);
        By locator = AppiumBy.accessibilityId(testId);
        WebElement element = WaitUtils.waitForClickable(locator, timeout);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Type text into a field identified by testID.
     * Clears existing text first.
     *
     * @param testId the React Native testID
     * @param text   the text to type
     */
    protected void type(String testId, String text) {
        log.debug("Typing '{}' into element with testID: {}", text, testId);
        WebElement element = findByTestId(testId);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Type text into a field identified by locator.
     *
     * @param locator the By locator
     * @param text    the text to type
     */
    protected void type(By locator, String text) {
        log.debug("Typing '{}' into element: {}", text, locator);
        WebElement element = WaitUtils.waitForClickable(locator);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Get text from an element identified by testID.
     *
     * @param testId the React Native testID
     * @return the element text
     */
    protected String getText(String testId) {
        return findByTestId(testId).getText();
    }

    /**
     * Get text from an element.
     *
     * @param locator the By locator
     * @return the element text
     */
    protected String getText(By locator) {
        return find(locator).getText();
    }

    /**
     * Get an attribute value from an element.
     *
     * @param locator   the By locator
     * @param attribute the attribute name (e.g., "enabled", "selected", "content-desc")
     * @return the attribute value
     */
    protected String getElementAttribute(By locator, String attribute) {
        return find(locator).getAttribute(attribute);
    }

    /**
     * Get an attribute value from an element identified by testID.
     *
     * @param testId    the React Native testID
     * @param attribute the attribute name
     * @return the attribute value
     */
    protected String getElementAttribute(String testId, String attribute) {
        return findByTestId(testId).getAttribute(attribute);
    }

    /**
     * Check if an element identified by testID is displayed.
     *
     * @param testId the React Native testID
     * @return true if displayed
     */
    protected boolean isDisplayed(String testId) {
        return WaitUtils.isElementDisplayed(AppiumBy.accessibilityId(testId));
    }

    /**
     * Check if an element is displayed.
     *
     * @param locator the By locator
     * @return true if displayed
     */
    protected boolean isDisplayed(By locator) {
        return WaitUtils.isElementDisplayed(locator);
    }

    /**
     * Check if an element is enabled (clickable).
     *
     * @param locator the By locator
     * @return true if enabled
     */
    protected boolean isEnabled(By locator) {
        try {
            return getDriver().findElement(locator).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Check if an element identified by testID is enabled.
     *
     * @param testId the React Native testID
     * @return true if enabled
     */
    protected boolean isEnabled(String testId) {
        return isEnabled(AppiumBy.accessibilityId(testId));
    }

    // ==================== Gestures ====================

    /**
     * Swipe up on the screen (scroll content down).
     */
    protected void swipeUp() {
        GestureUtils.swipeUp();
    }

    /**
     * Swipe down on the screen (scroll content up).
     */
    protected void swipeDown() {
        GestureUtils.swipeDown();
    }

    /**
     * Scroll down until text is visible.
     *
     * @param text      the text to find
     * @param maxSwipes maximum swipe attempts
     * @return true if found
     */
    protected boolean scrollToText(String text, int maxSwipes) {
        return GestureUtils.scrollDownToText(text, maxSwipes);
    }

    // ==================== Wait Helpers ====================

    /**
     * Wait for an element to become visible.
     *
     * @param testId  the React Native testID
     * @param timeout timeout in seconds
     * @return the visible WebElement
     */
    protected WebElement waitForElement(String testId, int timeout) {
        return WaitUtils.waitForVisible(AppiumBy.accessibilityId(testId), timeout);
    }

    /**
     * Wait for an element to disappear.
     *
     * @param locator the By locator
     * @param timeout timeout in seconds
     * @return true if element became invisible
     */
    protected boolean waitForElementToDisappear(By locator, int timeout) {
        return WaitUtils.waitForInvisible(locator, timeout);
    }

    // ==================== Keyboard ====================

    /**
     * Hide the soft keyboard if visible.
     */
    protected void hideKeyboard() {
        com.onsurity.utils.AppUtils.hideKeyboard();
    }
}
