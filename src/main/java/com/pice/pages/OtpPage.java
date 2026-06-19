package com.pice.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the OTP verification screen.
 *
 * <p>This screen appears after tapping "Proceed" on the LoginPage.
 * The user must enter a 4 or 6 digit OTP code to complete authentication.
 *
 * <p><b>OTP Input Strategies (priority order):</b>
 * <ol>
 *   <li>Multi-field layout — one digit per EditText field</li>
 *   <li>Single-field layout — all digits in one EditText</li>
 *   <li>Android KeyEvent fallback — programmatic key presses</li>
 * </ol>
 *
 * <p><b>Note:</b> On emulators with root detection, this screen may not
 * be reachable. The {@link LoginPage#proceedToOtp()} method handles this case.
 */
public class OtpPage extends BasePage {

    // ==================== Locators ====================

    // --- Screen Identifiers ---
    private static final By TITLE_OTP = AppiumBy.accessibilityId("Enter OTP");
    private static final By TITLE_OTP_ALT = By.xpath(
            "//*[contains(@content-desc,'OTP') or contains(@content-desc,'Verify')]"
    );
    // Some apps show "Enter OTP" or "OTP sent to..."
    private static final By OTP_SENT_TEXT = By.xpath(
            "//*[contains(@content-desc,'OTP sent') or contains(@content-desc,'sent to')]"
    );

    // --- OTP Input Fields ---
    // React Native OTP inputs commonly render as multiple EditText fields
    private static final By OTP_INPUTS = By.className("android.widget.EditText");

    // --- Action Buttons ---
    private static final By VERIFY_BUTTON = AppiumBy.accessibilityId("Proceed");
    private static final By VERIFY_BUTTON_ALT = AppiumBy.accessibilityId("Verify");
    private static final By VERIFY_BUTTON_ALT2 = AppiumBy.accessibilityId("Submit");
    private static final By RESEND_OTP = AppiumBy.accessibilityId("Resend OTP");
    private static final By RESEND_OTP_ALT = By.xpath(
            "//*[contains(@content-desc,'Resend') or contains(@content-desc,'resend')]"
    );

    // --- Back Navigation ---
    private static final By BACK_BUTTON = AppiumBy.accessibilityId("Back");
    private static final By BACK_BUTTON_ALT = By.xpath(
            "//android.widget.Button[@content-desc='Back' or @content-desc='back']"
    );

    // --- Timer / Countdown ---
    private static final By TIMER_TEXT = By.xpath(
            "//*[contains(@content-desc,'sec') or contains(@content-desc,'Resend in')]"
    );

    // --- Error Messages ---
    // Covers: "Invalid OTP", "Wrong OTP", "Incorrect OTP", "OTP has expired", "too many attempts"
    private static final By ERROR_MESSAGE = By.xpath(
            "//*[contains(@content-desc,'Invalid') or contains(@content-desc,'incorrect') or " +
            "contains(@content-desc,'Incorrect') or contains(@content-desc,'Wrong') or " +
            "contains(@content-desc,'wrong') or contains(@content-desc,'expired') or " +
            "contains(@content-desc,'attempts') or contains(@content-desc,'locked')]"
    );
    private static final By ERROR_MESSAGE_TEXT = By.xpath(
            "//*[contains(@text,'Invalid') or contains(@text,'incorrect') or " +
            "contains(@text,'Incorrect') or contains(@text,'Wrong') or " +
            "contains(@text,'wrong') or contains(@text,'expired') or " +
            "contains(@text,'attempts') or contains(@text,'locked')]"
    );

    // ==================== Page Load Validation ====================

    @Override
    protected By getPageLoadedLocator() {
        return TITLE_OTP; // OTP screen should show "Enter OTP" title
    }

    @Override
    protected int getPageLoadTimeout() {
        return 10; // OTP screen may take time after API call
    }

    // ==================== Constructor ====================

    public OtpPage() {
        waitForPageLoad();
        log.info("OtpPage loaded successfully");
    }

    // ==================== Actions ====================

    /**
     * Enter OTP digits.
     * Handles both single-field and multi-field OTP layouts.
     *
     * @param otp the OTP string (e.g., "1234" or "123456")
     * @return this page instance for fluent chaining
     */
    public OtpPage enterOtp(String otp) {
        log.info("Entering OTP: {}", otp);

        List<WebElement> otpFields = getDriver().findElements(OTP_INPUTS);
        log.debug("Found {} OTP input field(s)", otpFields.size());

        if (otpFields.size() >= otp.length()) {
            // Multi-field layout: type one digit per field
            log.debug("Using multi-field OTP entry ({}  fields)", otpFields.size());
            for (int i = 0; i < otp.length(); i++) {
                otpFields.get(i).sendKeys(String.valueOf(otp.charAt(i)));
            }
        } else if (!otpFields.isEmpty()) {
            // Single-field layout: type the entire OTP
            log.debug("Using single-field OTP entry");
            WebElement singleField = otpFields.get(0);
            singleField.clear();
            singleField.sendKeys(otp);
        } else {
            // Fallback: use AndroidDriver key events
            log.warn("No OTP EditText found — using AndroidDriver key events");
            enterOtpViaKeyEvents(otp);
        }

        return this;
    }

    /**
     * Enter OTP via Android key events as a fallback.
     */
    private void enterOtpViaKeyEvents(String otp) {
        if (getDriver() instanceof io.appium.java_client.android.AndroidDriver androidDriver) {
            for (char c : otp.toCharArray()) {
                io.appium.java_client.android.nativekey.AndroidKey key = switch (c) {
                    case '0' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_0;
                    case '1' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_1;
                    case '2' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_2;
                    case '3' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_3;
                    case '4' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_4;
                    case '5' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_5;
                    case '6' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_6;
                    case '7' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_7;
                    case '8' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_8;
                    case '9' -> io.appium.java_client.android.nativekey.AndroidKey.DIGIT_9;
                    default -> null;
                };
                if (key != null) {
                    androidDriver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(key));
                }
                sleep(200); // Wait for Flutter to register the input stablely
            }
        } else {
            log.error("Cannot enter OTP — no EditText fields and not an AndroidDriver");
        }
    }

    /**
     * Tap the Verify/Submit button if present.
     * Some OTP screens auto-submit after all digits are entered.
     */
    public void tapVerify() {
        log.info("Tapping Verify button");
        try {
            safeClick(VERIFY_BUTTON);
        } catch (Exception e) {
            log.debug("Primary Verify button not found, trying alt...");
            try {
                safeClick(VERIFY_BUTTON_ALT);
            } catch (Exception e2) {
                log.warn("No Verify/Submit button found — OTP may auto-submit");
            }
        }
    }

    /**
     * Tap the Resend OTP link.
     * Waits for the timer to expire first if a countdown is active.
     */
    public void tapResendOtp() {
        log.info("Tapping Resend OTP");
        try {
            tap(RESEND_OTP);
        } catch (Exception e) {
            log.debug("Primary resend locator failed, trying alt...");
            tap(RESEND_OTP_ALT);
        }
    }

    /**
     * Navigate back to the login screen.
     *
     * @return LoginPage instance
     */
    public LoginPage tapBack() {
        log.info("Navigating back from OTP screen");
        try {
            tap(BACK_BUTTON);
        } catch (Exception e) {
            log.debug("Primary back button failed, trying alt...");
            try {
                tap(BACK_BUTTON_ALT);
            } catch (Exception e2) {
                log.debug("Alt back button failed, using Android back key...");
                com.pice.utils.AppUtils.pressBack();
            }
        }
        return new LoginPage();
    }

    /**
     * Full OTP entry flow: enter OTP → tap verify (if button exists).
     *
     * @param otp the OTP code
     */
    public void verifyWith(String otp) {
        enterOtp(otp);
        sleep(2000); // Wait for key events to complete and UI to update

        // Tap the Proceed button
        try {
            List<WebElement> proceedBtns = getDriver().findElements(VERIFY_BUTTON);
            List<WebElement> verifyBtns = getDriver().findElements(VERIFY_BUTTON_ALT);
            List<WebElement> submitBtns = getDriver().findElements(VERIFY_BUTTON_ALT2);
            if (!proceedBtns.isEmpty() && proceedBtns.get(0).isDisplayed()) {
                proceedBtns.get(0).click();
                log.info("Tapped Proceed button after OTP entry");
            } else if (!verifyBtns.isEmpty() && verifyBtns.get(0).isDisplayed()) {
                verifyBtns.get(0).click();
                log.info("Tapped Verify button after OTP entry");
            } else if (!submitBtns.isEmpty() && submitBtns.get(0).isDisplayed()) {
                submitBtns.get(0).click();
                log.info("Tapped Submit button after OTP entry");
            } else {
                log.info("No visible proceed/verify button — OTP may auto-submit");
            }
        } catch (Exception e) {
            log.debug("No explicit verify button — OTP may auto-submit: {}", e.getMessage());
        }
    }

    // ==================== Verification Methods ====================

    /**
     * Check if the OTP screen is displayed.
     */
    public boolean isOtpScreenDisplayed() {
        try {
            return isDisplayed(TITLE_OTP) || isDisplayed(TITLE_OTP_ALT) || isDisplayed(OTP_SENT_TEXT);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if Resend OTP link is visible.
     */
    public boolean isResendOtpVisible() {
        try {
            return isDisplayed(RESEND_OTP) || isDisplayed(RESEND_OTP_ALT);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the countdown timer is active (Resend OTP not yet available).
     */
    public boolean isTimerActive() {
        try {
            return isDisplayed(TIMER_TEXT);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the number of OTP input fields on screen.
     */
    public int getOtpFieldCount() {
        try {
            return getDriver().findElements(OTP_INPUTS).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Check if any OTP error message is visible on screen.
     * Covers: "Invalid OTP", "Wrong OTP", "Incorrect OTP", "OTP expired", "too many attempts"
     *
     * @return true if an error message is displayed
     */
    public boolean isErrorMessageVisible() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(3));
            boolean found = !getDriver().findElements(ERROR_MESSAGE).isEmpty()
                    || !getDriver().findElements(ERROR_MESSAGE_TEXT).isEmpty();
            log.info("OTP error message visible: {}", found);
            return found;
        } catch (Exception e) {
            log.debug("Error checking OTP error message: {}", e.getMessage());
            return false;
        } finally {
            try {
                getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(
                        com.pice.config.ConfigManager.getImplicitWait()));
            } catch (Exception ignored) {}
        }
    }

    /**
     * Get the text of the OTP error message (if any).
     *
     * @return error message text, or empty string if none visible
     */
    public String getOtpErrorMessage() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(3));
            List<WebElement> errors = getDriver().findElements(ERROR_MESSAGE);
            if (!errors.isEmpty()) {
                String text = errors.get(0).getAttribute("content-desc");
                if (text == null || text.isBlank()) text = errors.get(0).getText();
                log.info("OTP error message text: {}", text);
                return text != null ? text : "";
            }
            List<WebElement> textErrors = getDriver().findElements(ERROR_MESSAGE_TEXT);
            if (!textErrors.isEmpty()) {
                String text = textErrors.get(0).getText();
                log.info("OTP error message text (text attr): {}", text);
                return text != null ? text : "";
            }
        } catch (Exception e) {
            log.debug("Could not get OTP error message: {}", e.getMessage());
        } finally {
            try {
                getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(
                        com.pice.config.ConfigManager.getImplicitWait()));
            } catch (Exception ignored) {}
        }
        return "";
    }

    /**
     * Get the resend countdown timer text (e.g., "Resend in 28 sec").
     *
     * @return timer text, or empty string if timer is not active
     */
    public String getResendTimerText() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(2));
            List<WebElement> timers = getDriver().findElements(TIMER_TEXT);
            if (!timers.isEmpty()) {
                String text = timers.get(0).getAttribute("content-desc");
                if (text == null || text.isBlank()) text = timers.get(0).getText();
                log.debug("Resend timer text: {}", text);
                return text != null ? text : "";
            }
        } catch (Exception e) {
            log.debug("Could not get resend timer text: {}", e.getMessage());
        } finally {
            try {
                getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(
                        com.pice.config.ConfigManager.getImplicitWait()));
            } catch (Exception ignored) {}
        }
        return "";
    }

    /**
     * Check whether the Resend OTP button/link is currently enabled (tappable).
     * Returns true when the countdown timer has expired and Resend is clickable.
     *
     * @return true if Resend button is visible and enabled
     */
    public boolean isResendButtonEnabled() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(2));
            // If timer text is visible, resend is still blocked
            List<WebElement> timers = getDriver().findElements(TIMER_TEXT);
            if (!timers.isEmpty()) {
                log.debug("Resend timer still active — resend is NOT enabled");
                return false;
            }
            // Check if Resend button itself is visible and enabled
            List<WebElement> resend = getDriver().findElements(RESEND_OTP);
            if (resend.isEmpty()) resend = getDriver().findElements(RESEND_OTP_ALT);
            if (!resend.isEmpty()) {
                boolean enabled = "true".equalsIgnoreCase(resend.get(0).getAttribute("enabled"))
                        && "true".equalsIgnoreCase(resend.get(0).getAttribute("clickable"));
                log.info("Resend button enabled: {}", enabled);
                return enabled;
            }
        } catch (Exception e) {
            log.debug("isResendButtonEnabled check error: {}", e.getMessage());
        } finally {
            try {
                getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(
                        com.pice.config.ConfigManager.getImplicitWait()));
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Clear all OTP input fields.
     * Used before re-entering a fresh OTP in back-to-back test scenarios.
     *
     * @return this page instance for fluent chaining
     */
    public OtpPage clearOtpFields() {
        log.info("Clearing all OTP input fields");
        try {
            List<WebElement> otpFields = getDriver().findElements(OTP_INPUTS);
            for (WebElement field : otpFields) {
                field.clear();
            }
        } catch (Exception e) {
            log.debug("Failed to clear OTP fields: {}", e.getMessage());
        }
        return this;
    }

    /**
     * Check if the OTP input field(s) are masked (password-type input).
     * A masked field has inputType attribute ending in "textPassword" or similar.
     *
     * @return true if OTP inputs appear to be masked/password-type
     */
    public boolean isOtpFieldMasked() {
        try {
            List<WebElement> otpFields = getDriver().findElements(OTP_INPUTS);
            if (otpFields.isEmpty()) {
                log.debug("No OTP input fields found for mask check");
                return false;
            }
            // Check 'password' attribute (Appium returns 'true' for password fields)
            String passwordAttr = otpFields.get(0).getAttribute("password");
            // Check 'className' or other attribute as fallback
            String inputType = otpFields.get(0).getAttribute("type");
            boolean masked = "true".equalsIgnoreCase(passwordAttr)
                    || (inputType != null && inputType.contains("password"));
            log.info("OTP field masked: {} (password={}, type={})", masked, passwordAttr, inputType);
            return masked;
        } catch (Exception e) {
            log.debug("isOtpFieldMasked check error: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Private Helpers ====================

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
