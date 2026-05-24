package com.onsurity.base;

import com.onsurity.config.ConfigManager;
import com.onsurity.driver.DeviceType;
import com.onsurity.driver.DriverFactory;
import com.onsurity.driver.DriverManager;
import com.onsurity.utils.AppUtils;
import com.onsurity.utils.AppiumServerManager;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.*;

/**
 * Base test class that manages the complete TestNG lifecycle for mobile automation.
 *
 * <p><b>Lifecycle (Industry Standard TestNG Flow):</b>
 * <pre>
 * @BeforeSuite   → Start Appium Server + Initialize Config + Reports
 * @BeforeTest    → Configure Test Group Context
 * @BeforeClass   → Initialize Appium Driver (Launch Session)
 * @BeforeMethod  → Open App / Reset to Home Screen
 * @Test          → Execute Mobile Actions & Assertions
 * @AfterMethod   → Capture Screenshot on Failure
 * @AfterClass    → cleanupTestResources() + driver.quit()
 * @AfterTest     → Clear Test Group Resources
 * @AfterSuite    → Stop Appium Server + Save Reports
 * </pre>
 *
 * <p><b>Session Strategy:</b>
 * <ul>
 *   <li><b>Class-level (default)</b> — One Appium session per test class.
 *       Tests share the session for ~5x faster execution.
 *       Use {@link #resetAppState()} in @BeforeMethod to reset between tests.</li>
 *   <li><b>Method-level (opt-in)</b> — Override {@link #isSessionPerMethod()}
 *       to return {@code true} for full isolation (new session per test).</li>
 * </ul>
 *
 * <p><b>Extension Points for Subclasses:</b>
 * <ul>
 *   <li>{@link #resetAppState()} — Override to set up the app's starting screen (e.g., login + navigate)</li>
 *   <li>{@link #cleanupTestResources()} — Override for class-level cleanup (e.g., logout). Called before driver.quit()</li>
 * </ul>
 */
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());

    // ==================== @BeforeSuite ====================
    // Start Appium Server + Initialize Config

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        log.info("========== SUITE SETUP START ==========");

        // 1. Initialize configuration
        ConfigManager.init();
        log.info("Environment: {}", ConfigManager.getEnvironment());
        log.info("Platform: {}", ConfigManager.getPlatform());
        log.info("Appium Server: {}", ConfigManager.getAppiumServerUrl());
        log.info("Cloud Execution: {}", ConfigManager.isCloudExecution());

        // 2. Start Appium server (if auto-manage is enabled)
        try {
            AppiumServerManager.startServer();
        } catch (Exception e) {
            log.error("Failed to start Appium server: {}", e.getMessage());
            throw new RuntimeException("Appium server startup failed — cannot proceed with tests", e);
        }

        // 3. TODO: Add any project-specific suite setup here
        // Example: API-based test data setup, user onboarding, etc.

        log.info("========== SUITE SETUP COMPLETE ==========");
    }

    // ==================== @AfterSuite ====================
    // Stop Appium Server + Save Reports

    @AfterSuite(alwaysRun = true)
    public void suiteTearDown() {
        log.info("========== SUITE TEARDOWN START ==========");

        // 1. TODO: Add any project-specific resource cleanup here
        // Example: Close database connections, flush caches, etc.

        // 2. Stop Appium server (if we started it)
        try {
            AppiumServerManager.stopServer();
        } catch (Exception e) {
            log.warn("Appium server stop failed: {}", e.getMessage());
        }

        log.info("========== SUITE TEARDOWN COMPLETE ==========");
    }

    // ==================== @BeforeTest ====================
    // Configure Test Group Context

    @BeforeTest(alwaysRun = true)
    public void testGroupSetup(ITestContext context) {
        log.info("---------- TEST GROUP SETUP: {} ----------", context.getName());
        log.info("Test group: {}", context.getName());
        log.info("Included groups: {}", String.join(", ",
                context.getIncludedGroups().length > 0
                        ? context.getIncludedGroups()
                        : new String[]{"ALL"}));
        log.info("Platform: {}, Environment: {}",
                ConfigManager.getPlatform(), ConfigManager.getEnvironment());
    }

    // ==================== @AfterTest ====================
    // Log Test Group Summary

    @AfterTest(alwaysRun = true)
    public void testGroupTearDown(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total = passed + failed + skipped;

        log.info("---------- TEST GROUP SUMMARY: {} ----------", context.getName());
        log.info("Total: {} | Passed: {} | Failed: {} | Skipped: {}",
                total, passed, failed, skipped);
        log.info("---------- TEST GROUP COMPLETE ----------");
    }

    // ==================== @BeforeClass ====================
    // Initialize Appium Driver (Launch Session)

    @BeforeClass(alwaysRun = true)
    @Parameters({"platform"})
    public void classSetup(@Optional String platform) {
        if (!isSessionPerMethod()) {
            createDriver(platform);
        }
    }

    // ==================== @AfterClass ====================
    // cleanupTestResources() + driver.quit()

    @AfterClass(alwaysRun = true)
    public void classTearDown() {
        if (!isSessionPerMethod()) {
            // 1. Run subclass-specific cleanup (logout, etc.)
            try {
                cleanupTestResources();
            } catch (Exception e) {
                log.warn("cleanupTestResources() failed: {}", e.getMessage());
            }

            // 2. Quit driver
            destroyDriver();
        }
    }

    // ==================== @BeforeMethod ====================
    // Open App / Reset to Home Screen

    @BeforeMethod(alwaysRun = true)
    @Parameters({"platform"})
    public void setUp(@Optional String platform) {
        if (isSessionPerMethod()) {
            createDriver(platform);
        } else {
            // Session reuse — just reset the app to a known state
            resetAppState();
        }
        log.info("---------- TEST READY ----------");
    }

    // ==================== @AfterMethod ====================
    // Log test duration (screenshot capture is in ExtentReportListener)

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (isSessionPerMethod()) {
            // 1. Run subclass-specific cleanup
            try {
                cleanupTestResources();
            } catch (Exception e) {
                log.warn("cleanupTestResources() failed: {}", e.getMessage());
            }

            // 2. Quit driver
            destroyDriver();
        }
        // For session reuse mode, driver stays alive — only reset in @BeforeMethod
        // Screenshot on failure is handled by ExtentReportListener.onTestFailure()
    }

    // ==================== Session Strategy ====================

    /**
     * Override this to use method-level driver isolation (new session per test).
     * Default is {@code false} (class-level session reuse for ~5x faster execution).
     *
     * <p>Set to {@code true} for test classes that require absolute isolation
     * (e.g., app install/uninstall tests, deep reset scenarios).
     *
     * @return true for session-per-method, false for session-per-class (default)
     */
    protected boolean isSessionPerMethod() {
        return false;
    }

    /**
     * Reset app state between tests when using session reuse.
     * Default implementation terminates and re-activates the app.
     *
     * <p>Subclasses should override this to navigate to the correct starting screen.
     * For example, offer tests override to login and navigate to the home screen.
     */
    protected void resetAppState() {
        log.debug("Resetting app state for next test...");
        try {
            AppUtils.resetApp();
        } catch (Exception e) {
            log.warn("App reset failed, recreating driver: {}", e.getMessage());
            destroyDriver();
            createDriver(null);
            // After driver recreation, ensure the app is in the foreground
            try {
                AppUtils.foregroundApp();
            } catch (Exception ex) {
                log.debug("foregroundApp after driver recreate: {}", ex.getMessage());
            }
        }
    }

    /**
     * Clean up test-specific resources before the driver is destroyed.
     * Called in @AfterClass (session reuse) or @AfterMethod (session per method).
     *
     * <p>Override this in subclasses for cleanup like:
     * <ul>
     *   <li>Logging out of the app</li>
     *   <li>Resetting test data</li>
     *   <li>Closing test-specific connections</li>
     * </ul>
     *
     * <p><b>Note:</b> Shared resource pools are closed in
     * {@code @AfterSuite} — do NOT close them here.
     */
    protected void cleanupTestResources() {
        // Default: no-op. Subclasses override for specific cleanup.
    }

    // ==================== Driver Management ====================

    protected void createDriver(String platform) {
        log.info("---------- CREATING DRIVER ----------");
        String resolvedPlatform = platform != null ? platform : ConfigManager.getPlatform();
        DeviceType deviceType = DeviceType.fromString(resolvedPlatform);

        log.info("Creating {} driver...", deviceType);
        AppiumDriver driver = DriverFactory.createDriver(deviceType);
        DriverManager.setDriver(driver);
        log.info("---------- DRIVER CREATED ----------");
    }

    private void destroyDriver() {
        log.info("---------- DESTROYING DRIVER ----------");
        DriverManager.removeDriver();
        log.info("---------- DRIVER DESTROYED ----------");
    }

    // ==================== Helper Methods for Tests ====================

    /**
     * Get the current AppiumDriver.
     */
    protected AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }

    /**
     * Get the platform being tested.
     */
    protected DeviceType getPlatformType() {
        return DeviceType.fromString(ConfigManager.getPlatform());
    }

    /**
     * Check if the current platform is Android.
     */
    protected boolean isAndroid() {
        return getPlatformType() == DeviceType.ANDROID;
    }

    /**
     * Check if the current platform is iOS.
     */
    protected boolean isIOS() {
        return getPlatformType() == DeviceType.IOS;
    }
}
