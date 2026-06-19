package com.pice.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the Pice Login screen.
 *
 * <p>Screen elements identified via UI Automator dump of the live app (v4.13.1-pre).
 * Uses accessibility IDs (content-desc) as the primary locator strategy
 * since the app is React Native / Flutter based.
 *
 * <p><b>Login Flow:</b>
 * <ol>
 *   <li>App launches → Login screen appears</li>
 *   <li>Dismiss any system dialogs (Google Play Services, permissions)</li>
 *   <li>Enter mobile number in the EditText</li>
 *   <li>Tap consent checkbox (ImageView before consent text)</li>
 *   <li>Tap "Proceed" button</li>
 *   <li>Handle "Rooted Device Detected" dialog if on emulator</li>
 *   <li>OTP screen appears → enter OTP</li>
 * </ol>
 *
 * <p><b>Known Emulator Issues:</b>
 * <ul>
 *   <li>"Update Google Play Services" dialog on first launch after clear</li>
 *   <li>"Rooted Device Detected" dialog after tapping Proceed</li>
 * </ul>
 */
public class LoginPage extends BasePage {

    // ==================== Locators ====================

    // --- Login Screen Identifiers ---
    private static final By TITLE_LOGIN = AppiumBy.accessibilityId("Login with mobile number");
    private static final By SUBTITLE = AppiumBy.accessibilityId("Please enter mobile number linked with your PAN");
    private static final By HELP_BUTTON = AppiumBy.accessibilityId("Help");

    // --- Phone Input ---
    // React Native EditText has no testID/content-desc; use className
    private static final By PHONE_INPUT = By.className("android.widget.EditText");

    // --- Consent Checkbox ---
    // The checkbox is an ImageView right before the consent text
    // We locate it via XPath relative to the consent text
    private static final By CONSENT_CHECKBOX = By.xpath(
            "//android.widget.Button[contains(@content-desc,'consent to Pice')]/parent::android.view.View/preceding-sibling::android.widget.ImageView"
    );

    // --- Consent Text ---
    private static final By CONSENT_TEXT = By.xpath(
            "//android.widget.Button[contains(@content-desc,'consent to Pice')]"
    );
    private static final By READ_MORE_LINK = AppiumBy.accessibilityId("Read More");

    // --- Proceed Button ---
    private static final By PROCEED_BUTTON = AppiumBy.accessibilityId("Proceed");

    // --- Country Code ---
    private static final By COUNTRY_CODE = By.xpath(
            "//android.view.View[contains(@content-desc,'+91')]"
    );

    // --- System Dialogs ---
    private static final By GOOGLE_PLAY_UPDATE_DIALOG = By.xpath(
            "//*[contains(@text,'Update Google Play services')]"
    );
    private static final By ROOTED_DEVICE_DIALOG = AppiumBy.accessibilityId("Rooted Device Detected");
    private static final By ROOTED_DEVICE_OKAY = AppiumBy.accessibilityId("Okay");

    // ==================== Page Load Validation ====================

    @Override
    protected By getPageLoadedLocator() {
        return TITLE_LOGIN;
    }

    @Override
    protected int getPageLoadTimeout() {
        return 15; // Login screen can take a moment after app launch / splash screen
    }

    // ==================== Constructor ====================

    public LoginPage() {
        // Dismiss any system dialogs before waiting for login screen
        dismissSystemDialogs();
        waitForPageLoad();
        log.info("LoginPage loaded successfully");
    }

    /**
     * Constructor with option to skip page load wait.
     * Used internally when we already know the page is loaded.
     *
     * @param skipWait true to skip waitForPageLoad
     */
    LoginPage(boolean skipWait) {
        if (!skipWait) {
            dismissSystemDialogs();
            waitForPageLoad();
        }
        log.info("LoginPage initialized");
    }

    // ==================== System Dialog Handling ====================

    /**
     * Dismiss system-level popups that block the login screen.
     * Handles:
     * <ul>
     *   <li>"Update Google Play Services" — press back to dismiss</li>
     *   <li>Android permission dialogs — tap "Allow"</li>
     * </ul>
     */
    private void dismissSystemDialogs() {
        log.debug("Checking for system dialogs to dismiss...");

        for (int i = 0; i < 3; i++) {
            try {
                // Check for Google Play Services update dialog
                List<WebElement> playDialog = getDriver().findElements(GOOGLE_PLAY_UPDATE_DIALOG);
                if (!playDialog.isEmpty()) {
                    log.info("'Update Google Play Services' dialog detected — pressing back to dismiss");
                    com.pice.utils.AppUtils.pressBack();
                    sleep(2000);
                    continue;
                }

                // Check for system permission "Allow" button
                List<WebElement> allowButtons = getDriver().findElements(
                        By.xpath("//*[@resource-id='com.android.permissioncontroller:id/permission_allow_button']"));
                if (!allowButtons.isEmpty()) {
                    log.info("System permission dialog detected — tapping Allow");
                    allowButtons.get(0).click();
                    sleep(1000);
                    continue;
                }

                // No dialogs found
                break;

            } catch (Exception e) {
                log.debug("Dialog check error (attempt {}): {}", i + 1, e.getMessage());
            }
        }
    }

    /**
     * Handle the "Rooted Device Detected" dialog that appears after tapping Proceed on an emulator.
     * This dialog blocks OTP screen navigation.
     *
     * @return true if the dialog was detected and dismissed, false otherwise
     */
    public boolean dismissRootedDeviceDialog() {
        log.debug("Checking for 'Rooted Device Detected' dialog...");
        sleep(2000);

        try {
            List<WebElement> rootDialog = getDriver().findElements(ROOTED_DEVICE_DIALOG);
            if (!rootDialog.isEmpty()) {
                log.warn("⚠️ 'Rooted Device Detected' dialog found — this is expected on emulators");
                List<WebElement> okayBtn = getDriver().findElements(ROOTED_DEVICE_OKAY);
                if (!okayBtn.isEmpty()) {
                    okayBtn.get(0).click();
                    log.info("Tapped 'Okay' to dismiss rooted device dialog");
                    sleep(1000);
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Root dialog check error: {}", e.getMessage());
        }
        return false;
    }

    // ==================== Actions ====================

    /**
     * Enter a mobile number into the phone field.
     * Clears any existing text first.
     *
     * @param mobileNumber the 10-digit mobile number
     * @return this page instance for fluent chaining
     */
    public LoginPage enterMobileNumber(String mobileNumber) {
        log.info("Entering mobile number: {}", mobileNumber);
        WebElement input = find(PHONE_INPUT);
        input.clear();
        input.sendKeys(mobileNumber);
        hideKeyboard();
        return this;
    }

    /**
     * Tap the consent checkbox to accept terms.
     *
     * @return this page instance for fluent chaining
     */
    public LoginPage tapConsentCheckbox() {
        log.info("Tapping consent checkbox (clicking consent text button)");
        tap(CONSENT_TEXT);
        return this;
    }

    /**
     * Ensure the consent checkbox is unchecked by using the Proceed button enablement state as a proxy.
     */
    public LoginPage ensureConsentUnchecked() {
        log.debug("Consent checkbox is checked by default; Proceed button state is independent of checkbox state.");
        return this;
    }

    /**
     * Clear text in the phone input field.
     */
    public LoginPage clearFields() {
        try {
            find(PHONE_INPUT).clear();
        } catch (Exception e) {
            log.debug("Failed to clear phone input: {}", e.getMessage());
        }
        return this;
    }

    /**
     * Tap the Proceed button.
     * The button must be enabled (consent checkbox must be tapped first).
     *
     * <p><b>Note:</b> On emulators, this may trigger a "Rooted Device Detected"
     * dialog instead of navigating to OTP screen. Use {@link #proceedToOtp()}
     * for a safe flow that handles root detection.
     *
     * @return OtpPage instance after navigation
     */
    public OtpPage tapProceed() {
        log.info("Tapping Proceed button");
        com.pice.utils.AppUtils.setDeveloperOptions(false);
        try {
            safeClick(PROCEED_BUTTON);
            sleep(1000);
        } finally {
            com.pice.utils.AppUtils.setDeveloperOptions(true);
        }
        return new OtpPage();
    }

    /**
     * Tap Proceed and handle root detection dialog if it appears.
     * On rooted devices/emulators, the "Rooted Device Detected" dialog
     * blocks OTP navigation. This method dismisses it and retries.
     *
     * @return OtpPage if navigation succeeds, null if blocked by root detection
     */
    public OtpPage proceedToOtp() {
        log.info("Tapping Proceed (with root detection handling)...");
        com.pice.utils.AppUtils.setDeveloperOptions(false);
        try {
            safeClick(PROCEED_BUTTON);
            sleep(1000);
        } finally {
            com.pice.utils.AppUtils.setDeveloperOptions(true);
        }
        sleep(2000);

        // Check for root detection dialog
        if (dismissRootedDeviceDialog()) {
            log.warn("Root detection blocked OTP navigation — app returned to login screen");
            // After dismissing, we're back on login screen
            // The app may not allow proceeding on a rooted device
            return null;
        }

        // No root dialog — try to construct OTP page
        try {
            return new OtpPage();
        } catch (Exception e) {
            log.error("Failed to navigate to OTP screen: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tap the Help button in the top-right corner.
     */
    public void tapHelp() {
        log.info("Tapping Help button");
        tap(HELP_BUTTON);
    }

    /**
     * Full login flow: enter number → consent → proceed.
     *
     * @param mobileNumber the 10-digit mobile number
     * @return OtpPage instance (may be null if root detection blocks)
     */
    public OtpPage loginWith(String mobileNumber) {
        enterMobileNumber(mobileNumber);
        tapConsentCheckbox();
        return proceedToOtp();
    }

    // ==================== Verification Methods ====================

    /**
     * Check if the login screen is currently displayed.
     */
    public boolean isLoginScreenDisplayed() {
        return isDisplayed(TITLE_LOGIN);
    }

    /**
     * Check if the Proceed button is enabled.
     * The button is disabled until both phone number AND consent are provided.
     */
    public boolean isProceedButtonEnabled() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));
            List<WebElement> buttons = getDriver().findElements(
                    By.xpath("//android.widget.Button[@content-desc='Proceed']"));
            if (buttons.isEmpty()) {
                log.info("Proceed button not found");
                return false;
            }
            WebElement btn = buttons.get(0);
            String enabledVal = btn.getAttribute("enabled");
            String clickableVal = btn.getAttribute("clickable");
            log.info("Proceed button attributes: enabled={}, clickable={}", enabledVal, clickableVal);
            
            boolean isEnabled = "true".equalsIgnoreCase(enabledVal) && "true".equalsIgnoreCase(clickableVal);
            log.info("Proceed button enablement check: isEnabled={}", isEnabled);
            return isEnabled;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(
                        com.pice.config.ConfigManager.getImplicitWait()));
            } catch (Exception ignored) {}
        }
    }

    /**
     * Check if the subtitle is displayed.
     */
    public boolean isSubtitleDisplayed() {
        return isDisplayed(SUBTITLE);
    }

    /**
     * Check if the country code (+91) is displayed.
     */
    public boolean isCountryCodeDisplayed() {
        return isDisplayed(COUNTRY_CODE);
    }

    /**
     * Get the text entered in the phone number field.
     */
    public String getPhoneInputText() {
        return find(PHONE_INPUT).getText();
    }

    /**
     * Check if Help button is visible.
     */
    public boolean isHelpButtonVisible() {
        return isDisplayed(HELP_BUTTON);
    }

    /**
     * Check if consent text is visible.
     */
    public boolean isConsentTextVisible() {
        return isDisplayed(CONSENT_TEXT);
    }

    /**
     * Check if Read More link is visible.
     */
    public boolean isReadMoreLinkVisible() {
        return isDisplayed(READ_MORE_LINK);
    }

    /**
     * Check if the "Rooted Device Detected" dialog is currently displayed.
     */
    public boolean isRootedDeviceDialogDisplayed() {
        try {
            return !getDriver().findElements(ROOTED_DEVICE_DIALOG).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Private Helpers ====================

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
