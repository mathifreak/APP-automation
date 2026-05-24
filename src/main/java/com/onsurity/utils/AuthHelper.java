package com.onsurity.utils;

import com.onsurity.config.ConfigManager;
import com.onsurity.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Generic authentication helper providing reusable login/logout methods
 * that can be used across all test classes.
 *
 * <p><b>TODO:</b> Implement login/logout flows for your specific app.
 * This is a framework stub — override/implement the methods below
 * with your app's authentication flow (page objects, locators, etc.).
 *
 * <p><b>Usage examples:</b>
 * <pre>{@code
 * // Login with default credentials from config
 * AuthHelper.login();
 *
 * // Login with specific credentials
 * AuthHelper.login("phone_number", "otp");
 *
 * // Logout from anywhere in the app
 * AuthHelper.logout();
 *
 * // Ensure app is on home screen (login if needed, reuse session if possible)
 * AuthHelper.ensureLoggedIn();
 *
 * // Check current state
 * boolean loggedIn = AuthHelper.isOnHomeScreen();
 * boolean onLogin = AuthHelper.isOnLoginScreen();
 * }</pre>
 *
 * <p><b>Design:</b> All methods are static for convenience. The class handles:
 * <ul>
 *   <li>Google phone hint dialog dismissal</li>
 *   <li>System permission popups</li>
 *   <li>Onboarding flow</li>
 *   <li>Notification allow/deny popups</li>
 *   <li>OTP entry via Android KeyEvents</li>
 *   <li>Logout navigation</li>
 * </ul>
 */
public final class AuthHelper {

    private static final Logger log = LogManager.getLogger(AuthHelper.class);

    private AuthHelper() {
        // Utility class — no instantiation
    }

    // ==================== Login Methods ====================

    /**
     * Perform a full login with default credentials from config.
     * Reads {@code login.mobile.number} and {@code login.otp} from the active config.
     *
     * <p><b>TODO:</b> Implement with your app's LoginPage and HomePage page objects.
     */
    public static void login() {
        String mobile = ConfigManager.get("login.mobile.number", "");
        String otp = ConfigManager.get("login.otp", "");
        login(mobile, otp);
    }

    /**
     * Perform a full login with the specified credentials.
     * Handles all intermediate screens: onboarding, popups, phone hint dialog.
     *
     * <p><b>TODO:</b> Implement with your app's login flow.
     * Original pattern:
     * <pre>{@code
     * navigateToLoginScreen();
     * LoginPage loginPage = new LoginPage();
     * loginPage.dismissPhoneHintDialog();
     * HomePage homePage = loginPage.loginWith(mobileNumber, otp);
     * homePage.waitForHomeScreen();
     * }</pre>
     *
     * @param mobileNumber the mobile number to login with
     * @param otp          the OTP code
     */
    public static void login(String mobileNumber, String otp) {
        log.info("========== AuthHelper.login({}) ==========", mobileNumber);

        // Navigate through any popups/screens to reach the login screen
        navigateToLoginScreen();

        // TODO: Implement your app's login flow here
        // Example:
        // LoginPage loginPage = new LoginPage();
        // loginPage.dismissPhoneHintDialog();
        // HomePage homePage = loginPage.loginWith(mobileNumber, otp);
        // homePage.waitForHomeScreen();

        log.info("========== Login flow completed ==========");
    }

    /**
     * Ensure the app is on the Home screen — login only if needed.
     * Uses default credentials from config.
     *
     * <p>This is the recommended method for test setup — it avoids
     * unnecessary re-login when the session is already active.
     */
    public static void ensureLoggedIn() {
        String mobile = ConfigManager.get("login.mobile.number", "");
        String otp = ConfigManager.get("login.otp", "");
        ensureLoggedIn(mobile, otp);
    }

    /**
     * Ensure the app is on the Home screen — login only if needed.
     *
     * <p>If already on the home screen, returns immediately.
     * Otherwise, navigates to login and performs full authentication.
     *
     * @param mobileNumber the mobile number to login with
     * @param otp          the OTP code
     */
    public static void ensureLoggedIn(String mobileNumber, String otp) {
        log.info("AuthHelper: Ensuring logged in state for {}...", mobileNumber);

        // Bring app to foreground
        ensureAppForeground();
        sleep(3000);

        // Check if already on home screen
        if (isOnHomeScreen()) {
            log.info("Already on home screen — no login needed");
            return;
        }

        // Not on home screen — perform login
        log.info("Not on home screen — performing login...");
        login(mobileNumber, otp);
    }

    // ==================== Logout Methods ====================

    /**
     * Perform a full logout from the app.
     *
     * <p><b>TODO:</b> Implement with your app's logout flow.
     * Original pattern: Home → Profile tab → Settings → Logout → confirm.
     */
    public static void logout() {
        log.info("========== AuthHelper.logout() ==========");
        AppiumDriver driver = DriverManager.getDriver();

        // TODO: Implement your app's logout flow here
        // Example:
        // 1. Navigate to Profile tab
        // 2. Tap Settings
        // 3. Tap Logout
        // 4. Handle confirmation dialog
        // 5. Verify login screen is displayed

        log.info("========== Logout flow completed ==========");
    }

    // ==================== State Detection Methods ====================

    /**
     * Check if the app is currently on the home screen.
     *
     * <p><b>TODO:</b> Update the XPath selectors to match your app's home screen indicators.
     *
     * @return true if home screen is visible
     */
    public static boolean isOnHomeScreen() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            // TODO: Update these selectors for your app's home screen
            List<WebElement> homeIndicators = driver.findElements(
                    By.xpath("//*[@text='Home']"));
            return !homeIndicators.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the app is currently on the login screen.
     *
     * <p><b>TODO:</b> Update the XPath selectors to match your app's login screen indicators.
     *
     * @return true if login screen is visible
     */
    public static boolean isOnLoginScreen() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            // TODO: Update these selectors for your app's login screen
            List<WebElement> loginIndicators = driver.findElements(
                    By.xpath("//*[@text='Enter Mobile Number' or contains(@text,'get Started')]"));
            if (!loginIndicators.isEmpty()) return true;

            // Also check for EditText (mobile number input)
            return !driver.findElements(By.className("android.widget.EditText")).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Internal Helper Methods ====================

    /**
     * Ensure the app is in the foreground.
     * Handles the case where driver recreation leaves us on the Android launcher.
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
     * Handles: Google phone hint dialog, notification popups, system permissions,
     * onboarding, and error/retry states.
     *
     * <p><b>TODO:</b> Customize for your app's specific popup/dialog flow.
     */
    public static void navigateToLoginScreen() {
        AppiumDriver driver = DriverManager.getDriver();
        log.info("Navigating to login screen...");

        for (int attempt = 0; attempt < 15; attempt++) {
            try {
                // 1. Check for Google phone hint dialog — must dismiss first
                List<WebElement> phoneHints = driver.findElements(
                        By.xpath("//*[contains(@text,'Choose a phone number')]"));
                if (!phoneHints.isEmpty()) {
                    log.info("Google phone hint dialog detected — dismissing");
                    dismissPhoneHintOverlay(driver);
                    sleep(2000);
                    continue;
                }

                // 2. Check if login screen EditText is visible
                List<WebElement> editTexts = driver.findElements(
                        By.className("android.widget.EditText"));
                if (!editTexts.isEmpty()) {
                    log.info("Login screen detected (attempt {})", attempt + 1);
                    return;
                }

                // 3. Check for login screen by title text
                // TODO: Update these selectors for your app
                List<WebElement> loginTitle = driver.findElements(
                        By.xpath("//*[@text='Enter Mobile Number' or contains(@text,'get Started')]"));
                if (!loginTitle.isEmpty()) {
                    log.info("Login screen detected via title (attempt {})", attempt + 1);
                    return;
                }

                // 4. Check for interactive elements (popups, onboarding, error)
                List<WebElement> elements = driver.findElements(
                        By.xpath("//*[@text='Allow' or @text='Get Started' or @text='Retry']"));

                if (!elements.isEmpty()) {
                    for (WebElement el : elements) {
                        String text = "";
                        try { text = el.getText(); } catch (Exception ignored) {}

                        if ("Allow".equals(text)) {
                            log.info("Popup — tapping 'Allow'");
                            el.click();
                            sleep(2000);
                            break;
                        }
                        if ("Retry".equals(text)) {
                            log.info("Error — tapping 'Retry'");
                            el.click();
                            sleep(5000);
                            break;
                        }
                        if ("Get Started".equals(text)) {
                            log.info("Onboarding — tapping 'Get Started'");
                            el.click();
                            sleep(3000);
                            break;
                        }
                    }
                } else {
                    // 5. Check system permission dialog
                    List<WebElement> sysPerms = driver.findElements(
                            By.xpath("//*[@resource-id='com.android.permissioncontroller:id/permission_allow_button']"));
                    if (!sysPerms.isEmpty()) {
                        log.info("System permission — tapping Allow");
                        sysPerms.get(0).click();
                        sleep(2000);
                    } else {
                        log.info("Waiting for screen... (attempt {}/15)", attempt + 1);
                        sleep(3000);
                    }
                }
            } catch (Exception e) {
                log.debug("Screen check error: {}", e.getMessage());
                sleep(3000);
            }
        }
        log.warn("Login screen not detected after retries — proceeding anyway");
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
                driver.findElement(By.className("android.widget.ImageView")).click();
            } catch (Exception e2) {
                // Last resort: press back
                if (driver instanceof AndroidDriver androidDriver) {
                    androidDriver.pressKey(
                            new io.appium.java_client.android.nativekey.KeyEvent(
                                    io.appium.java_client.android.nativekey.AndroidKey.BACK));
                }
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
