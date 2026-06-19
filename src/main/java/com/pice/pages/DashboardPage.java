package com.pice.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the Pice Home Dashboard screen.
 *
 * <p>This is the main landing screen after login + permission granting.
 * Contains the primary navigation, payment options, and user status.
 *
 * <p><b>Screen Layout (from live UI Automator dump on SM-S928U1):</b>
 * <ul>
 *   <li>Top bar: Coins display (e.g., "9159 Coins")</li>
 *   <li>Content area: "MAKE PAYMENTS", "RECENT", referral cards</li>
 *   <li>Payment categories: Business, Taxes, Education</li>
 *   <li>Trending: My Business Circle card</li>
 *   <li>Bottom nav: Home | Cards | Loan | History</li>
 *   <li>Floating: "Make Payment" button</li>
 * </ul>
 *
 * <p><b>Flow:</b> Login → OTP → Permissions → <b>Home Dashboard</b>
 */
public class DashboardPage extends BasePage {

    // ==================== Locators ====================

    // --- Screen Identifiers (from live dump) ---
    private static final By COINS_DISPLAY = By.xpath(
            "//android.widget.ImageView[contains(@content-desc,'Coins')]"
    );
    private static final By MAKE_PAYMENTS_SECTION = By.xpath(
            "//*[contains(@content-desc,'MAKE PAYMENTS')]"
    );
    private static final By RECENT_SECTION = By.xpath(
            "//*[contains(@content-desc,'RECENT')]"
    );

    // --- Referral Card ---
    private static final By REFERRAL_CARD = By.xpath(
            "//*[contains(@content-desc,'Refer') or contains(@content-desc,'Referral') or contains(@content-desc,'refer')]"
    );

    // --- Bottom Navigation Bar ---
    private static final By NAV_HOME = AppiumBy.accessibilityId("Home");
    private static final By NAV_CARDS = AppiumBy.accessibilityId("Cards");
    private static final By NAV_LOAN = AppiumBy.accessibilityId("Loan");
    private static final By NAV_HISTORY = AppiumBy.accessibilityId("History");

    // --- Payment Category Cards ---
    private static final By CARD_BUSINESS = By.xpath(
            "//android.widget.ImageView[contains(@content-desc,'Business')]"
    );
    private static final By CARD_TAXES = By.xpath(
            "//android.widget.ImageView[contains(@content-desc,'Taxes')]"
    );
    private static final By CARD_EDUCATION = By.xpath(
            "//android.widget.ImageView[contains(@content-desc,'Education')]"
    );

    // --- Action Elements ---
    private static final By MAKE_PAYMENT_BUTTON = AppiumBy.accessibilityId("Make Payment");
    private static final By TRENDING_SECTION = By.xpath(
            "//*[contains(@content-desc,'TRENDING')]"
    );
    private static final By EXPLORE_NOW = By.xpath(
            "//*[contains(@content-desc,'EXPLORE NOW')]"
    );

    // --- Login screen check (negative validation) ---
    private static final By LOGIN_TITLE = AppiumBy.accessibilityId("Login with mobile number");

    // ==================== Page Load Validation ====================

    @Override
    protected By getPageLoadedLocator() {
        return null; // Use custom multi-strategy validation
    }

    @Override
    protected int getPageLoadTimeout() {
        return 15;
    }

    // ==================== Constructor ====================

    public DashboardPage() {
        log.info("Waiting for Dashboard to load...");
        waitForDashboard();
        log.info("DashboardPage loaded successfully");
    }

    /**
     * Constructor with skip option.
     */
    public DashboardPage(boolean skipWait) {
        if (!skipWait) {
            waitForDashboard();
        }
    }

    // ==================== Custom Page Load ====================

    private void waitForDashboard() {
        int maxWait = getPageLoadTimeout();
        int waited = 0;

        while (waited < maxWait) {
            if (isDashboardVisible()) {
                return;
            }
            sleep(2000);
            waited += 2;
        }

        log.warn("Dashboard indicators not found after {}s — proceeding cautiously", maxWait);
    }

    // ==================== State Detection ====================

    /**
     * Check if the dashboard is currently visible.
     * Uses multiple strategies to detect the home dashboard.
     */
    public boolean isDashboardVisible() {
        try {
            // 1. Bottom navigation tabs must be visible (excludes Rewards, Help, Profile screens)
            if (getDriver().findElements(NAV_HOME).isEmpty()
                    || getDriver().findElements(NAV_CARDS).isEmpty()) {
                return false;
            }

            // 2. Must be on the Home tab (excludes Cards, Loan, History screens)
            // The Home tab is the only tab that shows the Coins display or MAKE PAYMENTS section
            if (!getDriver().findElements(COINS_DISPLAY).isEmpty()) {
                return true;
            }

            if (!getDriver().findElements(MAKE_PAYMENTS_SECTION).isEmpty()) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait for the dashboard to become visible.
     *
     * @param timeoutSeconds max wait time in seconds
     * @return true if visible within timeout, false otherwise
     */
    public boolean waitForDashboardVisible(int timeoutSeconds) {
        log.info("Waiting for dashboard to be visible (timeout={}s)...", timeoutSeconds);
        for (int i = 0; i < timeoutSeconds; i++) {
            if (isDashboardVisible()) {
                log.info("Dashboard detected as visible after {}s", i);
                return true;
            }
            sleep(1000);
        }
        return false;
    }

    /**
     * Verify we are NOT on the login screen (confirms login success).
     */
    public boolean isNotOnLoginScreen() {
        try {
            return getDriver().findElements(LOGIN_TITLE).isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    // ==================== Bottom Navigation ====================

    /**
     * Resilient navigation helper for bottom tabs.
     * Hides keyboard and handles back press recovery if the tab is not clickable.
     */
    private void safeNavigateToTab(By tabLocator, String tabName) {
        log.info("Navigating to {} tab", tabName);
        try {
            if ("History".equals(tabName)) {
                tapTopLeft(tabLocator);
            } else {
                tap(tabLocator);
            }
        } catch (Exception e) {
            log.warn("Failed to tap {} tab directly, attempting recovery...", tabName);
            try {
                com.pice.utils.AppUtils.hideKeyboard();
            } catch (Exception ignored) {}
            try {
                AppiumDriver driver = getDriver();
                if (driver instanceof AndroidDriver androidDriver) {
                    androidDriver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                            io.appium.java_client.android.nativekey.AndroidKey.BACK));
                }
                sleep(2000);
            } catch (Exception ignored) {}
            if ("History".equals(tabName)) {
                tapTopLeft(tabLocator);
            } else {
                tap(tabLocator);
            }
        }
    }

    /**
     * Tap the Home tab in bottom navigation.
     */
    public void navigateToHome() {
        safeNavigateToTab(NAV_HOME, "Home");
    }

    /**
     * Tap the Cards tab in bottom navigation.
     */
    public void navigateToCards() {
        safeNavigateToTab(NAV_CARDS, "Cards");
    }

    /**
     * Tap the Loan tab in bottom navigation.
     */
    public void navigateToLoan() {
        safeNavigateToTab(NAV_LOAN, "Loan");
    }

    /**
     * Tap the History tab in bottom navigation.
     */
    public void navigateToHistory() {
        safeNavigateToTab(NAV_HISTORY, "History");
    }

    // ==================== Payment Category Actions ====================

    /**
     * Tap the Business payment category card.
     */
    public void tapBusinessCategory() {
        log.info("Tapping Business category");
        tap(CARD_BUSINESS);
    }

    /**
     * Tap the Taxes payment category card.
     */
    public void tapTaxesCategory() {
        log.info("Tapping Taxes category");
        tap(CARD_TAXES);
    }

    /**
     * Tap the Education payment category card.
     */
    public void tapEducationCategory() {
        log.info("Tapping Education category");
        tap(CARD_EDUCATION);
    }

    /**
     * Tap the Make Payment floating button.
     */
    public void tapMakePayment() {
        log.info("Tapping Make Payment button");
        tap(MAKE_PAYMENT_BUTTON);
    }

    /**
     * Tap the Coins display at the top of the dashboard.
     */
    public void tapCoinsDisplay() {
        log.info("Tapping Coins display");
        try {
            WebElement coins = getDriver().findElement(COINS_DISPLAY);
            coins.click();
        } catch (Exception e) {
            log.warn("Failed to tap Coins display: {}", e.getMessage());
        }
    }

    /**
     * Tap Explore Now on the Trending section.
     */
    public void tapExploreNow() {
        log.info("Tapping Explore Now");
        tap(EXPLORE_NOW);
    }

    /**
     * Tap the Referral card.
     */
    public void tapReferralCard() {
        log.info("Tapping Referral card");
        try {
            WebElement card = getDriver().findElement(REFERRAL_CARD);
            card.click();
        } catch (Exception e) {
            log.warn("Failed to tap Referral card: {}", e.getMessage());
        }
    }

    // ==================== Navigation Helpers ====================

    /**
     * Navigate back to the Home Dashboard.
     * Tries pressing Android Back first, then falls back to tapping the Home tab.
     * Waits and validates that the dashboard is visible again.
     *
     * @return true if dashboard is visible after navigation
     */
    public boolean navigateBackToDashboard() {
        log.info("Navigating back to dashboard...");
        sleep(1000);

        // If already on dashboard, nothing to do
        if (isDashboardVisible()) {
            log.info("Already on dashboard");
            return true;
        }

        // Strategy 1: Press Android Back
        try {
            AppiumDriver driver = getDriver();
            if (driver instanceof AndroidDriver androidDriver) {
                androidDriver.pressKey(
                        new io.appium.java_client.android.nativekey.KeyEvent(
                                io.appium.java_client.android.nativekey.AndroidKey.BACK));
                sleep(2000);
                if (isDashboardVisible()) {
                    log.info("Returned to dashboard via Back button");
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Back button failed: {}", e.getMessage());
        }

        // Strategy 2: Tap Home tab in bottom nav
        try {
            List<WebElement> homeTab = getDriver().findElements(NAV_HOME);
            if (!homeTab.isEmpty()) {
                homeTab.get(0).click();
                sleep(2000);
                if (isDashboardVisible()) {
                    log.info("Returned to dashboard via Home tab");
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Home tab tap failed: {}", e.getMessage());
        }

        // Strategy 3: Multiple Back presses
        try {
            AppiumDriver driver = getDriver();
            if (driver instanceof AndroidDriver androidDriver) {
                for (int i = 0; i < 3; i++) {
                    androidDriver.pressKey(
                            new io.appium.java_client.android.nativekey.KeyEvent(
                                    io.appium.java_client.android.nativekey.AndroidKey.BACK));
                    sleep(1500);
                    if (isDashboardVisible()) {
                        log.info("Returned to dashboard after {} back presses", i + 1);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Multiple back presses failed: {}", e.getMessage());
        }

        log.warn("Could not return to dashboard");
        return false;
    }

    /**
     * Check if navigation away from dashboard occurred.
     * Returns true if the dashboard is NO LONGER visible (i.e. a tap caused navigation).
     */
    public boolean hasNavigatedAway() {
        sleep(2000);
        return !isDashboardVisible();
    }

    // ==================== Verification Methods ====================

    /**
     * Check if the Home tab is visible in the bottom nav.
     */
    public boolean isHomeTabVisible() {
        return isDisplayed(NAV_HOME);
    }

    /**
     * Check if the Cards tab is visible.
     */
    public boolean isCardsTabVisible() {
        return isDisplayed(NAV_CARDS);
    }

    /**
     * Check if the Loan tab is visible.
     */
    public boolean isLoanTabVisible() {
        return isDisplayed(NAV_LOAN);
    }

    /**
     * Check if the History tab is visible.
     */
    public boolean isHistoryTabVisible() {
        return isDisplayed(NAV_HISTORY);
    }

    /**
     * Check if the Coins display is visible.
     */
    public boolean isCoinsDisplayVisible() {
        try {
            return !getDriver().findElements(COINS_DISPLAY).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if payment categories are visible (any of Business/Taxes/Education).
     */
    public boolean arePaymentCategoriesVisible() {
        try {
            return !getDriver().findElements(CARD_BUSINESS).isEmpty()
                    || !getDriver().findElements(CARD_TAXES).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the Business category card is visible.
     */
    public boolean isBusinessCategoryVisible() {
        try {
            return !getDriver().findElements(CARD_BUSINESS).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the Taxes category card is visible.
     */
    public boolean isTaxesCategoryVisible() {
        try {
            return !getDriver().findElements(CARD_TAXES).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the Education category card is visible.
     */
    public boolean isEducationCategoryVisible() {
        try {
            return !getDriver().findElements(CARD_EDUCATION).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the MAKE PAYMENTS section header is visible.
     */
    public boolean isMakePaymentsSectionVisible() {
        try {
            return !getDriver().findElements(MAKE_PAYMENTS_SECTION).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the RECENT section is visible.
     */
    public boolean isRecentSectionVisible() {
        try {
            return !getDriver().findElements(RECENT_SECTION).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the Make Payment floating button is visible.
     */
    public boolean isMakePaymentButtonVisible() {
        try {
            return !getDriver().findElements(MAKE_PAYMENT_BUTTON).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the TRENDING section is visible.
     */
    public boolean isTrendingSectionVisible() {
        try {
            return !getDriver().findElements(TRENDING_SECTION).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the EXPLORE NOW button is visible.
     */
    public boolean isExploreNowVisible() {
        try {
            return !getDriver().findElements(EXPLORE_NOW).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the Referral card is visible.
     */
    public boolean isReferralCardVisible() {
        try {
            return !getDriver().findElements(REFERRAL_CARD).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the coins text from the display.
     */
    public String getCoinsText() {
        try {
            return getDriver().findElement(COINS_DISPLAY).getAttribute("content-desc");
        } catch (Exception e) {
            log.debug("Could not read coins text: {}", e.getMessage());
            return "";
        }
    }

    // ==================== Private Helpers ====================

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
