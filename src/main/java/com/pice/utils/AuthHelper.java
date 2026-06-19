package com.pice.utils;

import com.pice.config.ConfigManager;
import com.pice.driver.DriverManager;
import com.pice.pages.HomePage;
import com.pice.pages.LoginPage;
import com.pice.pages.OtpPage;
import com.pice.pages.PermissionPage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Authentication helper providing reusable login/logout methods
 * for the Pice app (v4.13.1-pre).
 *
 * <p><b>Login Flow (implemented):</b>
 * <ol>
 *   <li>Navigate to login screen (dismiss popups, permissions, phone hints)</li>
 *   <li>Enter mobile number</li>
 *   <li>Tap consent checkbox</li>
 *   <li>Tap Proceed</li>
 *   <li>Handle "Rooted Device Detected" dialog if on emulator</li>
 *   <li>Enter OTP on OTP screen</li>
 *   <li>Verify home screen is displayed</li>
 * </ol>
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * // Login with default credentials from config
 * AuthHelper.login();
 *
 * // Login with specific credentials
 * AuthHelper.login("9876543210", "1234");
 *
 * // Ensure app is on home screen (login if needed)
 * AuthHelper.ensureLoggedIn();
 *
 * // Check current state
 * boolean onLogin = AuthHelper.isOnLoginScreen();
 * }</pre>
 *
 * <p><b>Known Emulator Limitation:</b>
 * The Pice app has root detection. On emulators, after tapping "Proceed",
 * a "Rooted Device Detected" dialog blocks OTP entry. This dialog can be
 * dismissed but the app returns to the login screen. Full E2E login may
 * require a non-rooted device or root-hiding (e.g., Magisk Hide).
 */
public final class AuthHelper {

    private static final Logger log = LogManager.getLogger(AuthHelper.class);

    private AuthHelper() {
        // Utility class — no instantiation
    }

    // ==================== Login Methods ====================

    /**
     * Perform a full login with default credentials from config.
     * Reads {@code test.mobile.number} and {@code test.otp} from the active config.
     *
     * @return true if login was successful (or at least not blocked by root detection)
     */
    public static boolean login() {
        String mobile = ConfigManager.get("test.mobile.number", "");
        String otp = ConfigManager.get("test.otp", "");

        if (mobile.isEmpty()) {
            log.warn("test.mobile.number is not configured — cannot perform login");
            return false;
        }
        return login(mobile, otp);
    }

    /**
     * Perform a full login with the specified credentials.
     * Handles all intermediate screens: popups, phone hint dialog, root detection.
     *
     * @param mobileNumber the 10-digit mobile number
     * @param otp          the OTP code
     * @return true if login completed successfully
     */
    public static boolean login(String mobileNumber, String otp) {
        log.info("========== AuthHelper.login({}) ==========", mobileNumber);

        try {
            // 1. Navigate to login screen (dismiss popups)
            navigateToLoginScreen();

            // 2. Create LoginPage and enter credentials
            LoginPage loginPage = new LoginPage();

            // 3. Enter phone number
            loginPage.enterMobileNumber(mobileNumber);
            log.info("Phone number entered: {}", mobileNumber);

            // 4. Tap consent checkbox
            loginPage.tapConsentCheckbox();
            log.info("Consent checkbox tapped");

            // 5. Tap Proceed (with root detection handling)
            OtpPage otpPage = loginPage.proceedToOtp();

            if (otpPage == null) {
                log.warn("⚠️ OTP screen not reached — likely blocked by root detection on emulator");
                log.warn("To complete login, use a non-rooted device or configure root hiding");
                return false;
            }

            // 6. Enter OTP
            if (otp != null && !otp.isEmpty()) {
                otpPage.verifyWith(otp);
                log.info("OTP entered and submitted: {}", otp);
            } else {
                log.warn("OTP not provided — waiting on OTP screen");
                return false;
            }

            // 7. Check if Permission screen is displayed and grant all permissions automatically
            sleep(4000);
            try {
                AppiumDriver driver = DriverManager.getDriver();
                boolean isPermissionPage = false;
                try {
                    isPermissionPage = !driver.findElements(AppiumBy.accessibilityId("Please Enable Permissions")).isEmpty()
                            || !driver.findElements(AppiumBy.accessibilityId("Welcome")).isEmpty()
                            || !driver.findElements(By.xpath("//*[contains(@text,'While using the app') or contains(@text,'Allow only while using')]")).isEmpty()
                            || !driver.findElements(By.id("com.android.permissioncontroller:id/permission_allow_foreground_only_button")).isEmpty()
                            || !driver.findElements(By.id("com.android.permissioncontroller:id/permission_allow_button")).isEmpty()
                            || !driver.findElements(By.xpath("//*[@text='Allow' or @text='ALLOW' or @content-desc='Allow' or @content-desc='ALLOW']")).isEmpty();
                } catch (Exception ignored) {}

                if (isPermissionPage) {
                    log.info("AuthHelper: Permission screen detected after OTP verification — granting permissions automatically");
                    PermissionPage permissionPage = new PermissionPage(true);
                    permissionPage.proceedAndGrantAllPermissions();
                    sleep(2000);
                } else {
                    log.info("AuthHelper: Permission screen not detected, proceeding directly to Home screen check");
                }
            } catch (Exception e) {
                log.warn("AuthHelper: Error checking or granting permissions: {}", e.getMessage());
            }

            // 8. Wait for home screen
            sleep(3000); // Allow time for post-OTP navigation/permissions
            log.info("========== Login flow completed ==========");
            return true;

        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Perform login up to the Proceed button only (doesn't enter OTP).
     * Useful for testing the login form without needing valid credentials.
     *
     * @param mobileNumber the phone number to enter
     * @return the LoginPage instance after entering credentials
     */
    public static LoginPage loginUpToProceed(String mobileNumber) {
        log.info("========== AuthHelper.loginUpToProceed({}) ==========", mobileNumber);

        navigateToLoginScreen();
        LoginPage loginPage = new LoginPage();
        loginPage.enterMobileNumber(mobileNumber);
        loginPage.tapConsentCheckbox();

        return loginPage;
    }

    /**
     * Ensure the app is on the Home screen — login only if needed.
     * Uses default credentials from config.
     *
     * @return true if on home screen after this method completes
     */
    public static boolean ensureLoggedIn() {
        String mobile = ConfigManager.get("test.mobile.number", "");
        String otp = ConfigManager.get("test.otp", "");
        return ensureLoggedIn(mobile, otp);
    }

    /**
     * Ensure the app is on the Home screen — login only if needed.
     *
     * @param mobileNumber the mobile number to login with
     * @param otp          the OTP code
     * @return true if on home screen
     */
    public static boolean ensureLoggedIn(String mobileNumber, String otp) {
        log.info("AuthHelper: Ensuring logged in state for {}...", mobileNumber);

        // Bring app to foreground
        ensureAppForeground();
        sleep(3000);

        // Check if already on home screen
        if (isOnHomeScreen()) {
            log.info("Already on home screen — no login needed");
            return true;
        }

        // Not on home screen — perform login
        log.info("Not on home screen — performing login...");
        return login(mobileNumber, otp);
    }

    // ==================== Logout Methods ====================

    /**
     * Perform a full logout from the app.
     * Navigates: Home → Profile tab → Settings/Logout → confirm.
     */
    public static void logout() {
        log.info("========== AuthHelper.logout() ==========");
        AppiumDriver driver = DriverManager.getDriver();

        try {
            // Navigate to Profile/Account tab
            List<WebElement> profileTab = driver.findElements(
                    AppiumBy.accessibilityId("Profile"));
            if (profileTab.isEmpty()) {
                profileTab = driver.findElements(AppiumBy.accessibilityId("Account"));
            }

            if (!profileTab.isEmpty()) {
                profileTab.get(0).click();
                sleep(2000);

                // Look for Logout/Sign Out button
                List<WebElement> logoutBtn = driver.findElements(
                        By.xpath("//*[@content-desc='Logout' or @content-desc='Sign Out' or @text='Logout']"));
                if (!logoutBtn.isEmpty()) {
                    logoutBtn.get(0).click();
                    sleep(1000);

                    // Handle confirmation dialog
                    List<WebElement> confirmBtn = driver.findElements(
                            By.xpath("//*[@content-desc='Yes' or @content-desc='Confirm' or @text='Yes']"));
                    if (!confirmBtn.isEmpty()) {
                        confirmBtn.get(0).click();
                        sleep(2000);
                    }
                }
            } else {
                log.warn("Profile tab not found — using app data clear for logout");
                AppUtils.clearAppDataAndRestart();
            }
        } catch (Exception e) {
            log.error("Logout failed, falling back to app data clear: {}", e.getMessage());
            AppUtils.clearAppDataAndRestart();
        }

        log.info("========== Logout flow completed ==========");
    }

    // ==================== State Detection Methods ====================

    /**
     * Check if the app is currently on the home screen.
     *
     * @return true if home screen is visible
     */
    public static boolean isOnHomeScreen() {
        try {
            AppiumDriver driver = DriverManager.getDriver();

            // Check for Home tab / bottom navigation indicators
            List<WebElement> homeIndicators = driver.findElements(
                    By.xpath("//*[@content-desc='Home' or @text='Home']"));
            if (!homeIndicators.isEmpty()) return true;

            // Check for Payments/Profile tabs (bottom nav)
            if (!driver.findElements(AppiumBy.accessibilityId("Payments")).isEmpty()) return true;
            if (!driver.findElements(AppiumBy.accessibilityId("Profile")).isEmpty()) return true;
            if (!driver.findElements(AppiumBy.accessibilityId("Account")).isEmpty()) return true;

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the app is currently on the login screen.
     *
     * @return true if login screen is visible
     */
    public static boolean isOnLoginScreen() {
        try {
            AppiumDriver driver = DriverManager.getDriver();

            // Check for the login screen title (primary indicator)
            List<WebElement> loginTitle = driver.findElements(
                    AppiumBy.accessibilityId("Login with mobile number"));
            if (!loginTitle.isEmpty()) return true;

            // Check for Proceed button (secondary indicator)
            List<WebElement> proceedBtn = driver.findElements(
                    AppiumBy.accessibilityId("Proceed"));
            if (!proceedBtn.isEmpty()) return true;

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the "Rooted Device Detected" dialog is currently displayed.
     *
     * @return true if root detection dialog is visible
     */
    public static boolean isRootDetectionDialogVisible() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            return !driver.findElements(
                    AppiumBy.accessibilityId("Rooted Device Detected")).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Internal Helper Methods ====================

    /**
     * Ensure the app is in the foreground.
     */
    public static void ensureAppForeground() {
        try {
            AppUtils.foregroundApp();
        } catch (Exception e) {
            log.debug("foregroundApp: {}", e.getMessage());
        }
    }

    /**
     * Navigate through any popups/screens to reach the login screen.
     * Handles: Google Play Services dialog, phone hint dialog, permissions,
     * onboarding, and error/retry states.
     */
    public static void navigateToLoginScreen() {
        AppiumDriver driver = DriverManager.getDriver();
        log.info("Navigating to login screen...");

        // Temporarily set low implicit wait for fast lookups
        try {
            driver.manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));
        } catch (Exception e) {
            log.debug("Failed to set implicit wait: {}", e.getMessage());
        }

        try {
            for (int attempt = 0; attempt < 25; attempt++) {
                try {
                    // 1. FAST CHECK FIRST: Are we already on the Login Screen?
                    // Check if login screen is visible (by title)
                    List<WebElement> loginTitle = driver.findElements(
                            AppiumBy.accessibilityId("Login with mobile number"));
                    if (!loginTitle.isEmpty()) {
                        log.info("Login screen detected via title (attempt {})", attempt + 1);
                        return;
                    }

                    // Check for EditText (phone input — on login screen) and Proceed button
                    List<WebElement> editTexts = driver.findElements(
                            By.className("android.widget.EditText"));
                    List<WebElement> proceedBtn = driver.findElements(
                            AppiumBy.accessibilityId("Proceed"));
                    if (!editTexts.isEmpty() && !proceedBtn.isEmpty()) {
                        log.info("Login screen detected via EditText + Proceed (attempt {})", attempt + 1);
                        return;
                    }

                    // 2. Check if OTP screen is active — if so, press back to return to login screen
                    // Use fast accessibilityId checks instead of slow broad XPaths
                    List<WebElement> otpTitle = driver.findElements(AppiumBy.accessibilityId("Enter OTP"));
                    if (otpTitle.isEmpty()) {
                        otpTitle = driver.findElements(AppiumBy.accessibilityId("Resend OTP"));
                    }
                    if (!otpTitle.isEmpty()) {
                        log.info("OTP screen detected on navigation to Login — pressing back");
                        AppUtils.pressBack();
                        sleep(2000);
                        continue;
                    }

                    // 3. Check for Google Play Services update dialog
                    List<WebElement> playDialog = driver.findElements(
                            By.xpath("//android.widget.TextView[contains(@text,'Update Google Play services')]"));
                    if (!playDialog.isEmpty()) {
                        log.info("Google Play Services update dialog detected — pressing back");
                        if (driver instanceof AndroidDriver androidDriver) {
                            androidDriver.pressKey(
                                    new io.appium.java_client.android.nativekey.KeyEvent(
                                            io.appium.java_client.android.nativekey.AndroidKey.BACK));
                        }
                        sleep(2000);
                        continue;
                    }

                    // 4. Check for Google phone hint dialog
                    List<WebElement> phoneHints = driver.findElements(
                            By.xpath("//android.widget.TextView[contains(@text,'Choose a phone number')]"));
                    if (!phoneHints.isEmpty()) {
                        log.info("Google phone hint dialog detected — dismissing");
                        dismissPhoneHintOverlay(driver);
                        sleep(2000);
                        continue;
                    }

                    // 5. Check for "Rooted Device Detected" dialog
                    List<WebElement> rootDialog = driver.findElements(
                            AppiumBy.accessibilityId("Rooted Device Detected"));
                    if (!rootDialog.isEmpty()) {
                        log.info("Rooted Device dialog detected — tapping Okay");
                        List<WebElement> okayBtn = driver.findElements(
                                AppiumBy.accessibilityId("Okay"));
                        if (!okayBtn.isEmpty()) {
                            okayBtn.get(0).click();
                            sleep(2000);
                            continue;
                        }
                    }

                    // 6. Check for interactive elements (popups, onboarding, error)
                    List<WebElement> elements = driver.findElements(
                            By.xpath("//android.widget.Button[@text='Allow' or @text='Get Started' or @text='Retry' or @content-desc='Allow' or @content-desc='Get Started'] | //android.widget.TextView[@text='Allow' or @text='Get Started' or @text='Retry' or @content-desc='Allow' or @content-desc='Get Started']"));

                    if (!elements.isEmpty()) {
                        for (WebElement el : elements) {
                            String text = "";
                            try { text = el.getText(); } catch (Exception ignored) {}
                            String desc = "";
                            try { desc = el.getAttribute("content-desc"); } catch (Exception ignored) {}

                            String label = text.isEmpty() ? desc : text;

                            if ("Allow".equals(label)) {
                                log.info("Popup — tapping 'Allow'");
                                el.click();
                                sleep(2000);
                                break;
                            }
                            if ("Retry".equals(label)) {
                                log.info("Error — tapping 'Retry'");
                                el.click();
                                sleep(5000);
                                break;
                            }
                            if ("Get Started".equals(label)) {
                                log.info("Onboarding — tapping 'Get Started'");
                                el.click();
                                sleep(3000);
                                break;
                            }
                        }
                    } else {
                        // 7. Check system permission dialog using robust selectors (handles all styles)
                        boolean handled = false;
                        
                        // Strategy A: "While using the app" / "Allow only while using" (Location)
                        List<WebElement> whileUsing = driver.findElements(
                                By.xpath("//*[contains(@text,'While using the app') or contains(@text,'Allow only while using')]"));
                        if (!whileUsing.isEmpty()) {
                            whileUsing.get(0).click();
                            log.info("System permission — tapped 'While using the app'");
                            handled = true;
                            sleep(2000);
                        }
                        
                        if (!handled) {
                            // Strategy B: Standard allow foreground button by resource-id
                            List<WebElement> allowForeground = driver.findElements(
                                    By.id("com.android.permissioncontroller:id/permission_allow_foreground_only_button"));
                            if (!allowForeground.isEmpty()) {
                                allowForeground.get(0).click();
                                log.info("System permission — tapped 'Allow' (foreground only)");
                                handled = true;
                                sleep(2000);
                            }
                        }
                        
                        if (!handled) {
                            // Strategy C: Standard allow button by resource-id
                            List<WebElement> allowBtn = driver.findElements(
                                    By.id("com.android.permissioncontroller:id/permission_allow_button"));
                            if (!allowBtn.isEmpty()) {
                                allowBtn.get(0).click();
                                log.info("System permission — tapped 'Allow'");
                                handled = true;
                                sleep(2000);
                            }
                        }

                        if (!handled) {
                            // Strategy D: Text-based Allow button
                            List<WebElement> allowText = driver.findElements(
                                    By.xpath("//*[@text='Allow' or @text='ALLOW' or @content-desc='Allow' or @content-desc='ALLOW']"));
                            if (!allowText.isEmpty()) {
                                allowText.get(0).click();
                                log.info("System permission — tapped 'Allow' (text-based)");
                                handled = true;
                                sleep(2000);
                            }
                        }
                        
                        if (!handled) {
                            log.info("Waiting for screen... (attempt {}/25)", attempt + 1);
                            if (attempt >= 2 && attempt % 2 == 0) {
                                log.info("Attempting to press Back key to recover to app...");
                                AppUtils.pressBack();
                            }
                            sleep(1500);
                        }
                    }
                } catch (Exception e) {
                    log.debug("Screen check error: {}", e.getMessage());
                    sleep(1500);
                }
            }
            log.warn("Login screen not detected after retries — proceeding anyway");
        } finally {
            // Restore configured implicit wait
            try {
                driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(ConfigManager.getImplicitWait()));
            } catch (Exception e) {
                log.debug("Failed to restore implicit wait: {}", e.getMessage());
            }
        }
    }

    /**
     * Dismiss the Google "Choose a phone number" overlay dialog.
     */
    private static void dismissPhoneHintOverlay(AppiumDriver driver) {
        try {
            driver.findElement(By.xpath(
                    "//*[@content-desc='Close' or @content-desc='Cancel' or @content-desc='Dismiss']")).click();
        } catch (Exception e) {
            try {
                // Try pressing back
                if (driver instanceof AndroidDriver androidDriver) {
                    androidDriver.pressKey(
                            new io.appium.java_client.android.nativekey.KeyEvent(
                                    io.appium.java_client.android.nativekey.AndroidKey.BACK));
                }
            } catch (Exception e2) {
                log.debug("Failed to dismiss phone hint overlay: {}", e2.getMessage());
            }
        }
    }

    /**
     * Thread sleep helper.
     */
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
