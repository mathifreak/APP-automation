package com.onsurity.utils;

import com.onsurity.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Application lifecycle utilities for managing app state during tests.
 * Handles app install, reset, background/foreground, and activity management.
 *
 * <p>Uses Appium 2.x mobile extension commands (executeScript "mobile: *")
 * which are compatible with Appium Java Client 10.x where legacy convenience
 * methods were removed from AppiumDriver.</p>
 */
public final class AppUtils {

    private static final Logger log = LogManager.getLogger(AppUtils.class);

    private AppUtils() {
        // Prevent instantiation
    }

    /**
     * Reset the app to its initial state (terminate + re-activate).
     * Useful for ensuring a clean state between test scenarios.
     */
    public static void resetApp() {
        log.info("Resetting app...");
        AppiumDriver driver = DriverManager.getDriver();
        String bundleId = getAppBundleId();

        Map<String, Object> terminateParams = new HashMap<>();
        terminateParams.put("appId", bundleId);
        terminateParams.put("timeout", 5000);
        driver.executeScript("mobile: terminateApp", terminateParams);

        Map<String, Object> activateParams = new HashMap<>();
        activateParams.put("appId", bundleId);
        driver.executeScript("mobile: activateApp", activateParams);

        log.info("App reset complete");
    }

    /**
     * Clear all app data (login state, cache, preferences) and reactivate.
     * This forces a fresh login on next app launch.
     * Uses ADB 'pm clear' which is equivalent to clearing data from Android Settings.
     */
    public static void clearAppDataAndRestart() {
        log.info("Clearing app data for fresh login...");
        AppiumDriver driver = DriverManager.getDriver();
        String bundleId = getAppBundleId();

        // Terminate the app first
        try {
            Map<String, Object> terminateParams = new HashMap<>();
            terminateParams.put("appId", bundleId);
            terminateParams.put("timeout", 5000);
            driver.executeScript("mobile: terminateApp", terminateParams);
        } catch (Exception e) {
            log.debug("Terminate before clear failed (may already be stopped): {}", e.getMessage());
        }

        // Clear app data using ADB
        if (driver instanceof AndroidDriver androidDriver) {
            try {
                Map<String, Object> clearArgs = new HashMap<>();
                clearArgs.put("command", "pm");
                clearArgs.put("args", java.util.List.of("clear", bundleId));
                androidDriver.executeScript("mobile: shell", clearArgs);
                log.info("App data cleared via mobile:shell pm clear");
            } catch (Exception e) {
                // Fallback: use Runtime.exec if mobile:shell is blocked
                log.debug("mobile:shell blocked, using direct ADB: {}", e.getMessage());
                try {
                    String androidHome = System.getenv("ANDROID_HOME");
                    if (androidHome == null) androidHome = System.getProperty("user.home") + "/Library/Android/sdk";
                    String adb = androidHome + "/platform-tools/adb";
                    Process proc = Runtime.getRuntime().exec(new String[]{adb, "shell", "pm", "clear", bundleId});
                    proc.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
                    log.info("App data cleared via direct ADB");
                } catch (Exception ex) {
                    log.error("Failed to clear app data: {}", ex.getMessage());
                }
            }
        }

        // Wait for clear to complete
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

        // Reactivate the app (fresh start — will show onboarding/login)
        Map<String, Object> activateParams = new HashMap<>();
        activateParams.put("appId", bundleId);
        driver.executeScript("mobile: activateApp", activateParams);

        log.info("App data cleared and restarted — ready for fresh login");
    }

    /**
     * Send the app to background for the specified duration.
     *
     * @param durationSeconds seconds to keep in background (-1 for indefinite)
     */
    public static void backgroundApp(int durationSeconds) {
        log.info("Sending app to background for {}s", durationSeconds);
        AppiumDriver driver = DriverManager.getDriver();

        Map<String, Object> params = new HashMap<>();
        params.put("seconds", durationSeconds);
        driver.executeScript("mobile: backgroundApp", params);
    }

    /**
     * Bring the app back to foreground.
     */
    public static void foregroundApp() {
        log.info("Bringing app to foreground");
        AppiumDriver driver = DriverManager.getDriver();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", getAppBundleId());
        driver.executeScript("mobile: activateApp", params);
    }

    /**
     * Check if the app is currently installed on the device.
     *
     * @return true if installed
     */
    public static boolean isAppInstalled() {
        AppiumDriver driver = DriverManager.getDriver();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", getAppBundleId());
        Object result = driver.executeScript("mobile: isAppInstalled", params);
        return Boolean.TRUE.equals(result);
    }

    /**
     * Terminate the app without uninstalling.
     */
    public static void terminateApp() {
        log.info("Terminating app");
        AppiumDriver driver = DriverManager.getDriver();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", getAppBundleId());
        driver.executeScript("mobile: terminateApp", params);
    }

    /**
     * Press the Android back button.
     * No-op on iOS (iOS has no hardware back button).
     */
    public static void pressBack() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver instanceof AndroidDriver androidDriver) {
            log.debug("Pressing Android back button");
            androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
        } else {
            log.debug("Back button press not applicable on iOS");
        }
    }

    /**
     * Press the Android home button.
     */
    public static void pressHome() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver instanceof AndroidDriver androidDriver) {
            log.debug("Pressing Android home button");
            androidDriver.pressKey(new KeyEvent(AndroidKey.HOME));
        }
    }

    /**
     * Hide the keyboard if it's currently visible.
     */
    public static void hideKeyboard() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            driver.executeScript("mobile: hideKeyboard");
            log.debug("Keyboard hidden");
        } catch (Exception e) {
            log.debug("Keyboard was not visible or could not be hidden");
        }
    }

    /**
     * Get the current activity (Android only).
     *
     * @return current activity name, or null for iOS
     */
    public static String getCurrentActivity() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver instanceof AndroidDriver androidDriver) {
            return androidDriver.currentActivity();
        }
        return null;
    }

    /**
     * Execute a deep link URL to navigate to a specific screen.
     * Useful for React Native apps that support deep linking.
     *
     * @param deepLinkUrl the deep link URL (e.g., "yourapp://dashboard")
     */
    public static void openDeepLink(String deepLinkUrl) {
        log.info("Opening deep link: {}", deepLinkUrl);
        AppiumDriver driver = DriverManager.getDriver();

        if (driver instanceof AndroidDriver androidDriver) {
            // Android: use mobile:deepLink
            Map<String, Object> params = new HashMap<>();
            params.put("url", deepLinkUrl);
            params.put("package", getAppBundleId());
            androidDriver.executeScript("mobile: deepLink", params);
        } else {
            // iOS: use mobile:deepLink
            Map<String, Object> params = new HashMap<>();
            params.put("url", deepLinkUrl);
            driver.executeScript("mobile: deepLink", params);
        }
    }

    /**
     * Get the app package/bundle ID from configuration.
     */
    private static String getAppBundleId() {
        return com.onsurity.config.ConfigManager.get("app.bundle.id", "com.your.app.package");
    }
}

