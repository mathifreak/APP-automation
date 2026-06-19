package com.pice.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the Pice Permission screen.
 *
 * <p>This screen appears after successful login (OTP verified) and asks
 * the user to grant permissions for SMS, Location, and Installed Applications.
 *
 * <p><b>Screen Layout (from UI Automator dump):</b>
 * <ul>
 *   <li>Title: "Welcome"</li>
 *   <li>Subtitle: "Please Enable Permissions"</li>
 *   <li>SMS permission card — transactional SMS access</li>
 *   <li>Location permission card — account security</li>
 *   <li>Installed Applications card — financial profile</li>
 *   <li>"Proceed" button at bottom</li>
 *   <li>"Help" button at top-right</li>
 * </ul>
 *
 * <p><b>Flow:</b> Login → OTP → Permission Screen → Home Dashboard
 */
public class PermissionPage extends BasePage {

    // ==================== Locators ====================

    // --- Screen Identifiers ---
    private static final By TITLE_WELCOME = AppiumBy.accessibilityId("Welcome");
    private static final By SUBTITLE_PERMISSIONS = AppiumBy.accessibilityId("Please Enable Permissions");

    // --- Permission Cards ---
    private static final By SMS_PERMISSION_CARD = By.xpath(
            "//android.widget.ImageView[contains(@content-desc,'SMS')]"
    );
    private static final By LOCATION_PERMISSION_CARD = By.xpath(
            "//android.widget.ImageView[contains(@content-desc,'Location')]"
    );
    private static final By INSTALLED_APPS_CARD = By.xpath(
            "//android.widget.ImageView[contains(@content-desc,'Installed Applications')]"
    );

    // --- Action Buttons ---
    private static final By PROCEED_BUTTON = AppiumBy.accessibilityId("Proceed");
    private static final By HELP_BUTTON = AppiumBy.accessibilityId("Help");
    private static final By BACK_BUTTON = By.xpath(
            "//android.widget.Button[@bounds='[21,84][147,210]']"
    );

    // --- System Permission Dialogs ---
    private static final By ALLOW_BUTTON = By.id("com.android.permissioncontroller:id/permission_allow_foreground_only_button");
    private static final By ALLOW_BUTTON_ALT = By.id("com.android.permissioncontroller:id/permission_allow_button");
    private static final By DENY_BUTTON = By.id("com.android.permissioncontroller:id/permission_deny_button");
    private static final By WHILE_USING_APP = By.xpath(
            "//*[contains(@text,'While using the app') or contains(@text,'Allow only while using')]"
    );
    private static final By ALLOW_TEXT = By.xpath(
            "//*[@text='Allow' or @text='ALLOW' or @content-desc='Allow' or @content-desc='ALLOW']"
    );

    // ==================== Page Load Validation ====================

    @Override
    protected By getPageLoadedLocator() {
        return TITLE_WELCOME;
    }

    @Override
    protected int getPageLoadTimeout() {
        return 15;
    }

    // ==================== Constructor ====================

    public PermissionPage() {
        waitForPageLoad();
        log.info("PermissionPage loaded successfully");
    }

    /**
     * Constructor with skip option.
     */
    public PermissionPage(boolean skipWait) {
        if (!skipWait) {
            waitForPageLoad();
        }
        log.info("PermissionPage initialized");
    }

    // ==================== Actions ====================

    /**
     * Tap the Proceed button to begin the permission granting flow.
     * After tapping, system-level permission dialogs may appear.
     */
    public void tapProceed() {
        log.info("Tapping Proceed on Permission screen");
        safeClick(PROCEED_BUTTON);
    }

    /**
     * Tap Proceed and handle all system permission dialogs that appear.
     * Grants all permissions (SMS, Location, Installed Apps) automatically.
     *
     * @return true if all permissions were handled successfully
     */
    public boolean proceedAndGrantAllPermissions() {
        log.info("Proceeding with permission grants...");
        int permissionsHandled = 0;

        // 1. Handle any pre-existing system permission dialogs first (if app auto-prompted)
        for (int i = 0; i < 5; i++) {
            boolean handled = handleSystemPermissionDialog();
            if (handled) {
                permissionsHandled++;
                log.info("Pre-existing system permission dialog #{} handled", permissionsHandled);
                sleep(1500);
            } else {
                break;
            }
        }

        // 2. Tap Proceed if visible to trigger remaining prompts
        try {
            if (isDisplayed(PROCEED_BUTTON)) {
                log.info("Proceed button is visible — tapping it to trigger permission dialogs");
                tapProceed();
                sleep(2000);
            }
        } catch (Exception e) {
            log.debug("Proceed button click skipped or not needed: {}", e.getMessage());
        }

        // 3. Handle subsequent system permission dialogs
        for (int i = 0; i < 5; i++) {
            sleep(1500);
            boolean handled = handleSystemPermissionDialog();
            if (handled) {
                permissionsHandled++;
                log.info("System permission dialog #{} handled", permissionsHandled);
            } else {
                log.debug("No more system permission dialogs detected after {} grants", permissionsHandled);
                break;
            }
        }

        log.info("Total permissions handled: {}", permissionsHandled);
        return permissionsHandled > 0;
    }

    /**
     * Handle a single system-level Android permission dialog.
     * Tries multiple strategies to find and tap the "Allow" button.
     *
     * @return true if a dialog was found and handled
     */
    public boolean handleSystemPermissionDialog() {
        try {
            // Strategy 1: "Allow only while using the app" (Location)
            List<WebElement> whileUsing = getDriver().findElements(WHILE_USING_APP);
            if (!whileUsing.isEmpty()) {
                whileUsing.get(0).click();
                log.info("Tapped 'While using the app'");
                return true;
            }

            // Strategy 2: Standard "Allow" button by resource-id
            List<WebElement> allowBtns = getDriver().findElements(ALLOW_BUTTON);
            if (!allowBtns.isEmpty()) {
                allowBtns.get(0).click();
                log.info("Tapped 'Allow' (foreground only)");
                return true;
            }

            // Strategy 3: Alternative Allow button
            List<WebElement> allowAltBtns = getDriver().findElements(ALLOW_BUTTON_ALT);
            if (!allowAltBtns.isEmpty()) {
                allowAltBtns.get(0).click();
                log.info("Tapped 'Allow'");
                return true;
            }

            // Strategy 4: Text-based "Allow"
            List<WebElement> allowText = getDriver().findElements(ALLOW_TEXT);
            if (!allowText.isEmpty()) {
                allowText.get(0).click();
                log.info("Tapped 'Allow' (text-based)");
                return true;
            }

            return false;
        } catch (Exception e) {
            log.debug("Permission dialog handling error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Tap the Help button.
     */
    public void tapHelp() {
        log.info("Tapping Help on Permission screen");
        tap(HELP_BUTTON);
    }

    // ==================== Verification Methods ====================

    /**
     * Check if the permission screen is displayed.
     */
    public boolean isPermissionScreenDisplayed() {
        try {
            if (com.pice.utils.WaitUtils.isElementPresent(TITLE_WELCOME, 2)
                    || com.pice.utils.WaitUtils.isElementPresent(SUBTITLE_PERMISSIONS, 2)
                    || com.pice.utils.WaitUtils.isElementPresent(PROCEED_BUTTON, 2)) {
                return true;
            }

            // Fallback: check if any system permission dialog elements are active
            boolean systemDialogActive = !getDriver().findElements(WHILE_USING_APP).isEmpty()
                    || !getDriver().findElements(ALLOW_BUTTON).isEmpty()
                    || !getDriver().findElements(ALLOW_BUTTON_ALT).isEmpty()
                    || !getDriver().findElements(ALLOW_TEXT).isEmpty();

            if (systemDialogActive) {
                log.info("isPermissionScreenDisplayed: System permission dialog is active");
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the Welcome title is displayed.
     */
    public boolean isWelcomeTitleDisplayed() {
        return isDisplayed(TITLE_WELCOME);
    }

    /**
     * Check if SMS permission card is visible.
     */
    public boolean isSmsPermissionCardVisible() {
        try {
            return !getDriver().findElements(SMS_PERMISSION_CARD).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if Location permission card is visible.
     */
    public boolean isLocationPermissionCardVisible() {
        try {
            return !getDriver().findElements(LOCATION_PERMISSION_CARD).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if Installed Applications card is visible.
     */
    public boolean isInstalledAppsCardVisible() {
        try {
            return !getDriver().findElements(INSTALLED_APPS_CARD).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the Proceed button is visible.
     */
    public boolean isProceedButtonVisible() {
        return isDisplayed(PROCEED_BUTTON);
    }

    // ==================== Private Helpers ====================

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
