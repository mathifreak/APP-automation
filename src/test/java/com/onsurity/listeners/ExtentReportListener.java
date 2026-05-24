package com.onsurity.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.onsurity.config.ConfigManager;
import com.onsurity.driver.DriverManager;
import com.onsurity.utils.ScreenshotUtils;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.testng.*;

import java.util.Set;

/**
 * Industry-standard TestNG listener that generates ExtentReports
 * with comprehensive mobile test execution data.
 *
 * <p>Features:
 * <ul>
 *   <li>Auto-screenshot on test failure</li>
 *   <li>Device/platform metadata in report (device model, OS version, UDID)</li>
 *   <li>Color-coded test status (pass/fail/skip)</li>
 *   <li>Execution timestamps and duration</li>
 *   <li>Test categorization by group and platform</li>
 *   <li>Device assignment per test (for multi-device reporting)</li>
 *   <li>Retry-aware: removes stale retried test entries from final report</li>
 *   <li>Handles skipped tests even when @BeforeMethod fails</li>
 *   <li>Externalized report configuration via config.properties</li>
 * </ul>
 */
public class ExtentReportListener implements ITestListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(ExtentReportListener.class);
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // ==================== Configurable Defaults ====================

    private static final String DEFAULT_REPORT_DIR = "test-output/";
    private static final String DEFAULT_REPORT_NAME = "AppAutomationReport.html";
    private static final String DEFAULT_REPORT_TITLE = "App Automation Report";
    private static final String DEFAULT_REPORT_HEADING = "Mobile App Test Results";

    // Cached device info (populated once after first driver creation)
    private static volatile String deviceName;
    private static volatile String deviceModel;
    private static volatile String deviceOsVersion;
    private static volatile String deviceUdid;
    private static volatile boolean deviceInfoCaptured = false;

    // ==================== Suite Lifecycle ====================

    @Override
    public void onStart(ISuite suite) {
        log.info("Initializing ExtentReport for suite: {}", suite.getName());

        String reportDir = ConfigManager.get("report.dir", DEFAULT_REPORT_DIR);
        String reportTitle = ConfigManager.get("report.title", DEFAULT_REPORT_TITLE);
        String reportHeading = ConfigManager.get("report.heading", DEFAULT_REPORT_HEADING);
        String reportName = ConfigManager.get("report.name", DEFAULT_REPORT_NAME);

        String reportPath = reportDir + reportName;

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setDocumentTitle(reportTitle);
        spark.config().setReportName(reportHeading);
        spark.config().setTheme(Theme.DARK);
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        spark.config().setEncoding("UTF-8");

        extent = new ExtentReports();
        extent.attachReporter(spark);

        // Add system/environment info
        extent.setSystemInfo("Application", ConfigManager.get("app.name", "Mobile App"));
        extent.setSystemInfo("Environment", ConfigManager.getEnvironment());
        extent.setSystemInfo("Platform", ConfigManager.getPlatform());
        extent.setSystemInfo("Appium Server", ConfigManager.getAppiumServerUrl());
        extent.setSystemInfo("Cloud Execution", String.valueOf(ConfigManager.isCloudExecution()));
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("Flushing ExtentReport for suite: {}", suite.getName());

        // ---- Industry standard: Remove retried (stale) test results ----
        // When RetryAnalyzer retries a test, both the failed attempt and the
        // retry appear in the results. We remove the stale (intermediate) failures
        // so the report only shows the final outcome of each test.
        for (ISuiteResult suiteResult : suite.getResults().values()) {
            removeRetriedTests(suiteResult.getTestContext());
        }

        if (extent != null) {
            extent.flush();
        }
    }

    // ==================== Test Lifecycle ====================

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();
        log.info("▶ Starting test: {}.{}", className, testName);

        // Capture device info on first test (driver is available by now)
        captureDeviceInfo();

        ExtentTest test = extent.createTest(className + " :: " + testName,
                result.getMethod().getDescription());

        // ---- Industry standard: assign test categories ----
        test.assignCategory(ConfigManager.getPlatform().toUpperCase());
        String[] groups = result.getMethod().getGroups();
        for (String group : groups) {
            test.assignCategory(group);
        }

        // ---- Industry standard: assign device to test ----
        if (deviceName != null) {
            test.assignDevice(deviceName);
        }

        extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✅ PASSED: {}", result.getMethod().getMethodName());
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.log(Status.PASS, "Test passed successfully");
            test.log(Status.INFO, "Duration: " + getTestDuration(result) + "s");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("❌ FAILED: {}", result.getMethod().getMethodName());
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.log(Status.FAIL, "Test failed: " + result.getThrowable().getMessage());
            test.fail(result.getThrowable());

            // Capture screenshot on failure
            if (DriverManager.hasDriver()) {
                String base64Screenshot = ScreenshotUtils.captureScreenshotAsBase64();
                if (base64Screenshot != null) {
                    test.fail("Failure Screenshot",
                            MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
                }

                // Also save screenshot to file
                ScreenshotUtils.captureScreenshot(
                        result.getTestClass().getName() + "_" + result.getMethod().getMethodName() + "_FAILED");
            }

            test.log(Status.INFO, "Duration: " + getTestDuration(result) + "s");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⏭ SKIPPED: {}", result.getMethod().getMethodName());
        ExtentTest test = extentTest.get();

        // ---- Industry standard: handle skips when @BeforeMethod fails ----
        // If onTestStart never fired (e.g., @BeforeMethod threw an exception),
        // extentTest.get() will be null. We must create the test entry manually
        // so the skip is visible in the report.
        if (test == null) {
            String testName = result.getMethod().getMethodName();
            String className = result.getTestClass().getRealClass().getSimpleName();
            test = extent.createTest(className + " :: " + testName,
                    result.getMethod().getDescription());
            test.assignCategory(ConfigManager.getPlatform().toUpperCase());
            String[] groups = result.getMethod().getGroups();
            for (String group : groups) {
                test.assignCategory(group);
            }
            if (deviceName != null) {
                test.assignDevice(deviceName);
            }
            extentTest.set(test);
        }

        test.log(Status.SKIP, "Test skipped");
        if (result.getThrowable() != null) {
            test.skip(result.getThrowable());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Not commonly used, but available
    }

    // ==================== Public Utility Methods ====================

    /**
     * Get the ExtentTest for the current thread (for logging from test code).
     */
    public static ExtentTest getCurrentTest() {
        return extentTest.get();
    }

    /**
     * Log an info message to the current test report.
     */
    public static void logInfo(String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.log(Status.INFO, message);
        }
    }

    /**
     * Log a step with screenshot to the current test report.
     * Use {@link #logStep(String, boolean)} to skip screenshot for backend-only steps.
     */
    public static void logStep(String stepDescription) {
        logStep(stepDescription, true);
    }

    /**
     * Log a step to the current test report with optional screenshot.
     *
     * @param stepDescription description of the step
     * @param captureScreenshot true to attach a device screenshot, false for backend-only steps
     */
    public static void logStep(String stepDescription, boolean captureScreenshot) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.log(Status.INFO, "Step: " + stepDescription);
            if (captureScreenshot && DriverManager.hasDriver()) {
                String base64 = ScreenshotUtils.captureScreenshotAsBase64();
                if (base64 != null) {
                    test.info(stepDescription,
                            MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
                }
            }
        }
    }

    // ==================== Private Helpers ====================

    private double getTestDuration(ITestResult result) {
        return (result.getEndMillis() - result.getStartMillis()) / 1000.0;
    }

    /**
     * Capture device info from the live Appium session capabilities.
     * Called once and cached for all subsequent tests in the suite.
     *
     * <p>Industry standard for mobile reports — allows correlating
     * failures to specific device models, OS versions, and UDIDs.
     */
    private synchronized void captureDeviceInfo() {
        if (deviceInfoCaptured || !DriverManager.hasDriver()) {
            return;
        }

        try {
            AppiumDriver driver = DriverManager.getDriver();
            Capabilities caps = driver.getCapabilities();

            deviceName = getCapability(caps, "appium:deviceName", "deviceName");
            deviceModel = getCapability(caps, "appium:deviceModel", "deviceModel");
            deviceOsVersion = getCapability(caps, "appium:platformVersion", "platformVersion");
            deviceUdid = getCapability(caps, "appium:deviceUDID", "udid");

            String screenSize = getCapability(caps, "appium:deviceScreenSize", "deviceScreenSize");

            // Add device-level system info to the report
            if (deviceName != null) {
                extent.setSystemInfo("Device Name", deviceName);
            }
            if (deviceModel != null) {
                extent.setSystemInfo("Device Model", deviceModel);
            }
            if (deviceOsVersion != null) {
                extent.setSystemInfo("OS Version", ConfigManager.getPlatform() + " " + deviceOsVersion);
            }
            if (deviceUdid != null) {
                extent.setSystemInfo("Device UDID", deviceUdid);
            }
            if (screenSize != null) {
                extent.setSystemInfo("Screen Size", screenSize);
            }

            deviceInfoCaptured = true;
            log.info("Captured device info — name={}, model={}, os={}, udid={}",
                    deviceName, deviceModel, deviceOsVersion, deviceUdid);
        } catch (Exception e) {
            log.warn("Could not capture device info for report: {}", e.getMessage());
        }
    }

    /**
     * Safely read a capability value, trying multiple possible key formats.
     */
    private String getCapability(Capabilities caps, String... keys) {
        for (String key : keys) {
            Object value = caps.getCapability(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    /**
     * Remove retried (stale) test results from the TestNG context.
     *
     * <p>When {@link RetryAnalyzer} retries a failed test, TestNG keeps
     * intermediate failures in {@code getFailedTests()}. If the test
     * eventually passes, the stale failure is left behind — causing
     * the report to show both a FAIL and a PASS for the same test.
     *
     * <p>This method removes those stale entries so the report reflects
     * the actual final outcome. This is an industry-standard practice
     * for any framework that uses retry analyzers.
     */
    private void removeRetriedTests(ITestContext context) {
        Set<ITestResult> failedTests = context.getFailedTests().getAllResults();
        Set<ITestResult> passedTests = context.getPassedTests().getAllResults();
        Set<ITestResult> skippedTests = context.getSkippedTests().getAllResults();

        // If a test passed on a retry, remove the earlier failure
        for (ITestResult passedTest : passedTests) {
            failedTests.removeIf(failedTest ->
                    failedTest.getMethod().getMethodName().equals(passedTest.getMethod().getMethodName())
                            && failedTest.getTestClass().getName().equals(passedTest.getTestClass().getName()));
            skippedTests.removeIf(skippedTest ->
                    skippedTest.getMethod().getMethodName().equals(passedTest.getMethod().getMethodName())
                            && skippedTest.getTestClass().getName().equals(passedTest.getTestClass().getName()));
        }

        // If a test ended as failed, remove duplicate failures from retries
        for (ITestResult failedTest : failedTests) {
            skippedTests.removeIf(skippedTest ->
                    skippedTest.getMethod().getMethodName().equals(failedTest.getMethod().getMethodName())
                            && skippedTest.getTestClass().getName().equals(failedTest.getTestClass().getName()));
        }
    }
}
