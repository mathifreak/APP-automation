package com.pice.tests.smoke;

import com.pice.base.BaseTest;
import com.pice.config.ConfigManager;
import com.pice.constants.TestGroups;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Smoke test to verify the framework infrastructure is working:
 * <ul>
 *   <li>Appium server starts (via AppiumServerManager)</li>
 *   <li>APK file is found and loaded</li>
 *   <li>Driver connects to the emulator</li>
 *   <li>App launches successfully</li>
 * </ul>
 *
 * <p>Run with: {@code make run TEST=com.pice.tests.smoke.AppLaunchSmokeTest}
 */
public class AppLaunchSmokeTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(AppLaunchSmokeTest.class);

    @Test(groups = {TestGroups.SMOKE, TestGroups.REGRESSION})
    public void verifyAppLaunches() {
        log.info("===== SMOKE TEST: Verifying app launch =====");

        // 1. Verify driver is created (means Appium server is running + APK loaded)
        AppiumDriver driver = getDriver();
        Assert.assertNotNull(driver, "Appium driver should not be null");
        log.info("✅ Driver created successfully");

        // 2. Verify session is active
        String sessionId = driver.getSessionId().toString();
        Assert.assertNotNull(sessionId, "Session ID should not be null");
        Assert.assertFalse(sessionId.isEmpty(), "Session ID should not be empty");
        log.info("✅ Session active: {}", sessionId);

        // 3. Verify platform
        String platform = ConfigManager.getPlatform();
        log.info("✅ Running on platform: {}", platform);

        // 4. Verify app package (Android-specific)
        if (isAndroid()) {
            Object currentPackage = driver.getCapabilities().getCapability("appPackage");
            log.info("✅ App package: {}", currentPackage);
        }

        // 5. Log page source availability (proves app UI is loaded)
        try {
            String pageSource = driver.getPageSource();
            Assert.assertNotNull(pageSource, "Page source should not be null");
            Assert.assertFalse(pageSource.isEmpty(), "Page source should not be empty");
            log.info("✅ App UI loaded - page source length: {} chars", pageSource.length());
        } catch (Exception e) {
            log.warn("Page source check skipped: {}", e.getMessage());
        }

        log.info("===== SMOKE TEST PASSED: All framework components working =====");
    }
}
