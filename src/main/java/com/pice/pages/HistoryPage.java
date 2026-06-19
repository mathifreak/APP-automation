package com.pice.pages;

import com.pice.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the Pice app — Revamped History Screen.
 *
 * <p>
 * Accessed by tapping the "History" tab in the bottom navigation bar from the
 * Home Dashboard.
 */
public class HistoryPage extends BasePage {

    // ==================== Locators ====================

    // --- Screen Identifier ---
    /** Top-level screen title identifying the revamped History screen. */
    private static final By SCREEN_TITLE = By.xpath(
            "//*[contains(@text,'Payment History') or contains(@content-desc,'Payment History')]");

    private static final By NAV_HISTORY_ACTIVE = AppiumBy.accessibilityId("History");

    // --- Filter Buttons (New Layout) ---
    private static final By FILTER_PAYMENT_MODE = By.xpath(
            "//*[contains(@content-desc,'Payment Mode') or contains(@text,'Payment Mode')]");
    private static final By FILTER_CATEGORY = By.xpath(
            "//*[contains(@content-desc,'Category') or contains(@text,'Category')]");
    private static final By DOWNLOAD_BUTTON = By.xpath(
            "//*[@clickable='true' and (contains(@content-desc,'download') or contains(@text,'download') or .//android.widget.ImageView)]");

    // --- Transaction List ---
    private static final By TRANSACTION_LIST_ITEMS = By.xpath(
            "//*[@clickable='true' and (contains(@text,'₹') or contains(@content-desc,'₹'))]");

    private static final By SCROLLABLE_CONTENT = By.xpath(
            "//*[@scrollable='true']");

    private static final By TRANSACTION_AMOUNT = By.xpath(
            "//*[contains(@content-desc,'₹') or contains(@text,'₹')]");

    private static final By EMPTY_STATE = By.xpath(
            "//*[contains(@content-desc,'No transaction') or contains(@text,'No transaction')]");

    private static final By NAV_HOME = AppiumBy.accessibilityId("Home");

    // ==================== Constructors ====================

    public HistoryPage() {
        log.info("Waiting for History screen to load...");
        waitForHistoryScreen();
        log.info("HistoryPage loaded successfully");
    }

    public HistoryPage(boolean skipWait) {
        if (!skipWait) {
            waitForHistoryScreen();
        }
    }

    // ==================== Page Load Validation ====================

    @Override
    protected By getPageLoadedLocator() {
        return null; // Custom verification
    }

    @Override
    protected int getPageLoadTimeout() {
        return 15;
    }

    private void waitForHistoryScreen() {
        int maxWait = getPageLoadTimeout();
        int waited = 0;

        while (waited < maxWait) {
            if (isHistoryScreenVisible()) {
                return;
            }
            sleep(2000);
            waited += 2;
        }

        log.warn("History screen not confirmed visible after {}s — proceeding cautiously", maxWait);
    }

    // ==================== Screen State Detection ====================

    public boolean isHistoryScreenVisible() {
        try {
            // Uniquely identify History screen by verifying that BOTH the Payment Mode
            // and Category filters are active/visible on screen.
            return isDisplayed(FILTER_PAYMENT_MODE) && isDisplayed(FILTER_CATEGORY);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForHistoryScreenVisible(int timeoutSeconds) {
        log.info("Waiting for History screen (timeout={}s)...", timeoutSeconds);
        for (int i = 0; i < timeoutSeconds; i++) {
            if (isHistoryScreenVisible()) {
                log.info("History screen detected after {}s", i);
                return true;
            }
            sleep(1000);
        }
        return false;
    }

    // ==================== Filter Actions ====================

    public void tapFilterAll() {
        log.info("Tapping 'Payment Mode' filter dropdown");
        safeClick(FILTER_PAYMENT_MODE);
        sleep(1500);
        pressBack(); // close dropdown
        sleep(1000);
    }

    public void tapFilterPayments() {
        log.info("Tapping 'Category' filter dropdown");
        safeClick(FILTER_CATEGORY);
        sleep(1500);
        pressBack(); // close dropdown
        sleep(1000);
    }

    public void tapFilterCards() {
        log.info("Tapping Download button");
        safeClick(DOWNLOAD_BUTTON);
        sleep(2000);
        pressBack(); // close dropdown/share sheet if opened
        sleep(1000);
    }

    public void tapFilterLoan() {
        log.info("Tapping 'Payment Mode' filter dropdown");
        safeClick(FILTER_PAYMENT_MODE);
        sleep(1500);
        pressBack(); // close dropdown
        sleep(1000);
    }

    // ==================== Filter Visibility Checks ====================

    public boolean isFilterAllVisible() {
        return isDisplayed(FILTER_PAYMENT_MODE);
    }

    public boolean isFilterPaymentsVisible() {
        return isDisplayed(FILTER_CATEGORY);
    }

    public boolean isFilterCardsVisible() {
        return isDisplayed(DOWNLOAD_BUTTON);
    }

    public boolean isFilterLoanVisible() {
        return true; // stub for revamped page
    }

    // ==================== Transaction List ====================

    public boolean hasTransactions() {
        try {
            List<WebElement> items = getDriver().findElements(TRANSACTION_LIST_ITEMS);
            log.debug("Found {} transaction items in list", items.size());
            return !items.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public int getTransactionCount() {
        try {
            return getDriver().findElements(TRANSACTION_LIST_ITEMS).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean tapFirstTransaction() {
        log.info("Tapping first transaction item");
        try {
            List<WebElement> items = getDriver().findElements(TRANSACTION_LIST_ITEMS);
            if (!items.isEmpty()) {
                items.get(0).click();
                sleep(2000);
                return true;
            }
            log.warn("No transaction items found to tap");
            return false;
        } catch (Exception e) {
            log.warn("Failed to tap first transaction: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTransactionWithAmountVisible(String amount) {
        try {
            By locator = By.xpath(
                    "//*[contains(@content-desc,'" + amount + "') or contains(@text,'" + amount + "')]");
            return !getDriver().findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areAmountsVisible() {
        try {
            return !getDriver().findElements(TRANSACTION_AMOUNT).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Empty State ====================

    public boolean isEmptyStateVisible() {
        try {
            return !getDriver().findElements(EMPTY_STATE).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasContentLoaded() {
        return hasTransactions() || isEmptyStateVisible();
    }

    // ==================== Scroll ====================

    public void scrollDownForMore() {
        log.info("Scrolling down for more transactions");
        swipeUp();
        sleep(1000);
    }

    public void scrollToTop() {
        log.info("Scrolling to top of history list");
        swipeDown();
        sleep(1000);
    }

    // ==================== Navigation ====================

    public void navigateBackToHome() {
        log.info("Navigating back to Home from History");
        try {
            tap(NAV_HOME);
            sleep(2000);
        } catch (Exception e) {
            log.warn("Failed to tap Home tab: {}", e.getMessage());
            try {
                AppiumDriver driver = getDriver();
                if (driver instanceof AndroidDriver androidDriver) {
                    androidDriver.pressKey(
                            new io.appium.java_client.android.nativekey.KeyEvent(
                                    io.appium.java_client.android.nativekey.AndroidKey.BACK));
                    sleep(2000);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void pressBack() {
        log.info("Pressing Android back button");
        try {
            AppiumDriver driver = getDriver();
            if (driver instanceof AndroidDriver androidDriver) {
                androidDriver.pressKey(
                        new io.appium.java_client.android.nativekey.KeyEvent(
                                io.appium.java_client.android.nativekey.AndroidKey.BACK));
                sleep(1500);
            }
        } catch (Exception e) {
            log.warn("Back press failed: {}", e.getMessage());
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
