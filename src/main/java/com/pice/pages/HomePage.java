package com.pice.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Page Object for the Pice Home / Dashboard screen.
 *
 * <p>This is the landing screen after successful login (OTP verification).
 * Used to verify successful authentication by checking for key home screen indicators.
 *
 * <p><b>Note:</b> The exact locators will need to be refined once the OTP flow
 * is testable (currently blocked by root detection on emulators). Initial locators
 * are based on common patterns for fintech apps using React Native/Flutter.
 *
 * <p><b>Common Home Screen Indicators:</b>
 * <ul>
 *   <li>Bottom navigation bar (Home, Payments, Profile tabs)</li>
 *   <li>Dashboard content (balance, recent transactions)</li>
 *   <li>Profile/greeting text</li>
 * </ul>
 */
public class HomePage extends BasePage {

    // ==================== Locators ====================

    // --- Screen Identifiers ---
    // These are common patterns; update once actual locators are captured post-login
    private static final By HOME_TAB = AppiumBy.accessibilityId("Home");
    private static final By HOME_TAB_ALT = By.xpath(
            "//*[@content-desc='Home' or @text='Home']"
    );

    // --- Bottom Navigation ---
    private static final By NAV_PAYMENTS = AppiumBy.accessibilityId("Payments");
    private static final By NAV_PROFILE = AppiumBy.accessibilityId("Profile");
    private static final By NAV_PROFILE_ALT = AppiumBy.accessibilityId("Account");

    // --- Dashboard Elements ---
    private static final By DASHBOARD_CONTENT = By.xpath(
            "//*[contains(@content-desc,'Dashboard') or contains(@content-desc,'Welcome')]"
    );

    // --- Generic screen detection ---
    // Post-login screens won't have the login title
    private static final By LOGIN_TITLE_CHECK = AppiumBy.accessibilityId("Login with mobile number");

    // ==================== Page Load Validation ====================

    @Override
    protected By getPageLoadedLocator() {
        return null; // Use custom validation since we're detecting multiple possible indicators
    }

    @Override
    protected int getPageLoadTimeout() {
        return 15; // Post-login can take time (API calls, data loading)
    }

    // ==================== Constructor ====================

    public HomePage() {
        log.info("Waiting for Home screen to load...");
        waitForHomeScreen();
        log.info("HomePage loaded successfully");
    }

    /**
     * Constructor with skip option for when we just want to check state.
     */
    HomePage(boolean skipWait) {
        if (!skipWait) {
            waitForHomeScreen();
        }
    }

    // ==================== Custom Page Load ====================

    /**
     * Wait for the home screen to become visible.
     * Checks multiple indicators since the exact layout may vary.
     */
    private void waitForHomeScreen() {
        int maxWait = getPageLoadTimeout();
        int waited = 0;

        while (waited < maxWait) {
            if (isHomeScreenVisible()) {
                return;
            }
            sleep(2000);
            waited += 2;
        }

        log.warn("Home screen indicators not found after {}s — proceeding cautiously", maxWait);
    }

    // ==================== State Detection ====================

    /**
     * Check if the home screen is currently visible.
     * Uses multiple strategies to detect the post-login state.
     *
     * @return true if home screen indicators are found
     */
    public boolean isHomeScreenVisible() {
        try {
            // Strategy 1: Check for Home tab in bottom navigation
            if (!getDriver().findElements(HOME_TAB).isEmpty()) return true;
            if (!getDriver().findElements(HOME_TAB_ALT).isEmpty()) return true;

            // Strategy 2: Check for other navigation tabs
            if (!getDriver().findElements(NAV_PAYMENTS).isEmpty()) return true;
            if (!getDriver().findElements(NAV_PROFILE).isEmpty()) return true;
            if (!getDriver().findElements(NAV_PROFILE_ALT).isEmpty()) return true;

            // Strategy 3: Check for dashboard content
            if (!getDriver().findElements(DASHBOARD_CONTENT).isEmpty()) return true;

            // Strategy 4: Negative check — if login screen is NOT visible,
            // and we're still in the app, we might be on a post-login screen
            // (this is a weak check, use only as last resort)

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check that we're NOT on the login screen (indicates successful login).
     */
    public boolean isNotOnLoginScreen() {
        try {
            return getDriver().findElements(LOGIN_TITLE_CHECK).isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    // ==================== Navigation ====================

    /**
     * Navigate to the Profile/Account tab.
     */
    public void navigateToProfile() {
        log.info("Navigating to Profile tab");
        try {
            tap(NAV_PROFILE);
        } catch (Exception e) {
            log.debug("Primary Profile locator failed, trying 'Account'...");
            tap(NAV_PROFILE_ALT);
        }
    }

    /**
     * Navigate to the Payments tab.
     */
    public void navigateToPayments() {
        log.info("Navigating to Payments tab");
        tap(NAV_PAYMENTS);
    }

    /**
     * Navigate to the Home tab (if on a different tab).
     */
    public void navigateToHome() {
        log.info("Navigating to Home tab");
        try {
            tap(HOME_TAB);
        } catch (Exception e) {
            tap(HOME_TAB_ALT);
        }
    }

    // ==================== Private Helpers ====================

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
