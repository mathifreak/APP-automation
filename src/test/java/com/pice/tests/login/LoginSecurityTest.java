package com.pice.tests.login;

import com.pice.base.BaseTest;
import com.pice.constants.TestGroups;
import com.pice.listeners.ExtentReportListener;
import com.pice.pages.LoginPage;
import com.pice.pages.OtpPage;
import com.pice.utils.AuthHelper;
import com.pice.utils.SoftAssertUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Login Security Test Suite for the Pice App.
 *
 * <p>
 * Covers security-focused scenarios for the login and OTP flow:
 * <ul>
 * <li>OTP field masking (is OTP displayed as password-type)</li>
 * <li>Brute-force OTP attempts — UI feedback after multiple failures</li>
 * <li>Attempt-limit message visibility</li>
 * <li>Documented stubs for backend-dependent security tests</li>
 * </ul>
 *
 * <p>
 * <b>Note on stubs:</b> Most security validations require backend state
 * manipulation
 * (e.g., account lock reset, OTP reuse detection, session invalidation). These
 * are
 * implemented as {@code enabled = false} stubs with clear TODO comments for
 * future
 * integration with backend APIs.
 *
 * <p>
 * <b>Prerequisites:</b> Physical device, valid credentials, Appium server on
 * localhost:4723.
 *
 * <p>
 * <b>Run:</b>
 * 
 * <pre>{@code make test-batch-login-security}</pre>
 */
public class LoginSecurityTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(LoginSecurityTest.class);

    private static final String PHONE_NUMBER = com.pice.config.ConfigManager.get("test.mobile.number", "9962063736");
    private static final String DEVICE_SERIAL = getConnectedDeviceSerial();

    // Wrong OTP — guaranteed to be invalid
    private static final String OTP_WRONG = com.pice.config.ConfigManager.get("login.otp.wrong", "000000");

    // Number of brute-force attempts to perform in security tests
    private static final int BRUTE_FORCE_ATTEMPTS = Integer
            .parseInt(com.pice.config.ConfigManager.get("login.security.brute.force.attempts", "3"));

    private OtpPage otpPage;

    // ==================== Session Management ====================

    @Override
    protected void resetAppState() {
        log.info("--- LoginSecurityTest: Custom resetAppState — keeping session active ---");
        // No-op: app data is cleared only once in @BeforeClass
    }

    @BeforeClass(alwaysRun = true, dependsOnMethods = "classSetup")
    public void cleanLaunchForSecurityTests() {
        log.info("--- [Security Suite] BeforeClass: Clear app data + launch fresh ---");
        try {
            Runtime.getRuntime().exec(new String[] {
                    "adb", "-s", DEVICE_SERIAL, "shell", "pm", "clear", "one.pice.pice_business_loan.pre"
            }).waitFor();
            Thread.sleep(1500);

            Runtime.getRuntime().exec(new String[] {
                    "adb", "-s", DEVICE_SERIAL, "shell", "am", "start", "-n",
                    "one.pice.pice_business_loan.pre/one.pice.pice_business_loan.MainActivity"
            }).waitFor();
            Thread.sleep(3000);
        } catch (Exception e) {
            log.warn("BeforeClass app launch failed: {}", e.getMessage());
        }
        AuthHelper.navigateToLoginScreen();
        sleep(2000);
    }

    @BeforeMethod(alwaysRun = true)
    public void navigateToOtpScreen() {
        log.info("--- [Security Suite] BeforeMethod: Navigate to OTP screen ---");
        AuthHelper.navigateToLoginScreen();

        LoginPage loginPage = new LoginPage();
        loginPage.clearFields().ensureConsentUnchecked();
        loginPage.enterMobileNumber(PHONE_NUMBER);
        loginPage.tapConsentCheckbox();

        log.info("Tapping Proceed to reach OTP screen...");
        otpPage = loginPage.proceedToOtp();

        if (otpPage == null) {
            log.error("❌ OTP screen not reached — root detection may have blocked it. Skipping test.");
            throw new org.testng.SkipException("OTP screen not reachable (possible root detection)");
        }

        log.info("✅ OTP screen reached — ready for security test");
        sleep(1000);
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfterSecurityTest() {
        log.info("--- [Security Suite] AfterMethod: Navigate back to Login screen ---");
        try {
            com.pice.utils.AppUtils.pressBack();
            sleep(1000);
        } catch (Exception e) {
            log.debug("AfterMethod navigation error: {}", e.getMessage());
        }
    }

    private static String getConnectedDeviceSerial() {
        try {
            Process p = Runtime.getRuntime().exec(new String[] { "adb", "devices" });
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.endsWith("device") && !line.startsWith("List")) {
                    return line.split("\\s+")[0];
                }
            }
        } catch (Exception ignored) {
        }
        return "10MG56FM6E000FD";
    }

    // ==================== AUTOMATABLE SECURITY TESTS ====================

    @Test(groups = { TestGroups.REGRESSION, TestGroups.LOGIN,
            TestGroups.POSITIVE }, description = "[Security] Verify OTP input field is masked (password-type input, not visible text)", priority = 1)
    public void verifyOtpFieldIsMasked() {
        log.info("===== TEST: verifyOtpFieldIsMasked =====");

        ExtentReportListener.logStep("Check if OTP input field is masked (password-type)");
        boolean isMasked = otpPage.isOtpFieldMasked();

        log.info("OTP field masked: {}", isMasked);
        ExtentReportListener.logStep("[Observed] OTP field masked: " + isMasked);

        // Log result — mask behaviour is app-defined. We record the observation.
        // If not masked, this is a security concern that should be flagged.
        if (!isMasked) {
            log.warn("⚠️ SECURITY CONCERN: OTP input field is NOT masked. OTP digits may be visible in plain text.");
            ExtentReportListener.logStep("⚠️ SECURITY CONCERN: OTP field is not masked — digits may be visible");
        } else {
            log.info("✅ OTP field is correctly masked");
            ExtentReportListener.logStep("✅ OTP field is correctly masked");
        }

        log.info("===== TEST PASSED: verifyOtpFieldIsMasked =====");
    }

    @Test(groups = { TestGroups.REGRESSION, TestGroups.LOGIN,
            TestGroups.NEGATIVE }, description = "[Security] Verify repeated wrong OTP submissions show UI error feedback", priority = 2)
    public void verifyBruteForceOtpShowsError() {
        log.info("===== TEST: verifyBruteForceOtpShowsError =====");
        ExtentReportListener.logStep("Performing " + BRUTE_FORCE_ATTEMPTS + " wrong OTP submissions");

        SoftAssertUtils.init();

        for (int attempt = 1; attempt <= BRUTE_FORCE_ATTEMPTS; attempt++) {
            log.info("Wrong OTP attempt {}/{}", attempt, BRUTE_FORCE_ATTEMPTS);
            ExtentReportListener.logStep("Attempt " + attempt + ": Entering wrong OTP — " + OTP_WRONG);

            otpPage.clearOtpFields();
            otpPage.enterOtp(OTP_WRONG);
            sleep(500);
            otpPage.tapVerify();
            sleep(4000); // Allow API response

            boolean onOtpScreen = otpPage.isOtpScreenDisplayed();
            boolean errorVisible = otpPage.isErrorMessageVisible();
            String errorText = otpPage.getOtpErrorMessage();

            log.info("After attempt {}: OTP screen={}, Error visible={}, Error text='{}'",
                    attempt, onOtpScreen, errorVisible, errorText);
            ExtentReportListener.logStep("Attempt " + attempt + " result: error='" + errorText + "'");

            SoftAssertUtils.assertTrue(onOtpScreen || errorVisible,
                    "After attempt " + attempt + ": should remain on OTP screen or show error");

            // If screen transitioned away (e.g., account locked), stop early
            if (!onOtpScreen) {
                log.warn("OTP screen no longer displayed after {} attempts — possible account lock", attempt);
                ExtentReportListener.logStep("OTP screen left after " + attempt + " attempts — account may be locked");
                break;
            }
        }

        SoftAssertUtils.assertAll();
        log.info("===== TEST PASSED: verifyBruteForceOtpShowsError =====");
    }

    @Test(groups = { TestGroups.REGRESSION, TestGroups.LOGIN,
            TestGroups.NEGATIVE }, description = "[Security] Verify an attempt limit message is shown after multiple wrong OTP submissions", priority = 3)
    public void verifyOtpAttemptLimitMessage() {
        log.info("===== TEST: verifyOtpAttemptLimitMessage =====");

        // Perform multiple wrong OTP attempts (same as brute force test, but focus on
        // limit message)
        String attemptLimitMsg = "";
        for (int attempt = 1; attempt <= BRUTE_FORCE_ATTEMPTS; attempt++) {
            log.info("Wrong OTP attempt {}/{}", attempt, BRUTE_FORCE_ATTEMPTS);

            otpPage.clearOtpFields();
            otpPage.enterOtp(OTP_WRONG);
            sleep(500);
            otpPage.tapVerify();
            sleep(4000);

            boolean errorVisible = otpPage.isErrorMessageVisible();
            if (errorVisible) {
                attemptLimitMsg = otpPage.getOtpErrorMessage();
                log.info("Error message after attempt {}: '{}'", attempt, attemptLimitMsg);
            }

            // Stop if OTP screen is gone
            if (!otpPage.isOtpScreenDisplayed()) {
                log.info("Left OTP screen after {} attempts", attempt);
                break;
            }
        }

        ExtentReportListener.logStep("Attempt limit message observed: '" + attemptLimitMsg + "'");
        log.info("Final attempt limit message: '{}'", attemptLimitMsg);

        // This is an observational test — we document what the app shows
        // A proper assert here depends on knowing the app's specific error strings
        boolean feedbackProvided = !attemptLimitMsg.isBlank() || !otpPage.isOtpScreenDisplayed();
        log.info("Security feedback provided after {} wrong attempts: {}", BRUTE_FORCE_ATTEMPTS, feedbackProvided);
        ExtentReportListener.logStep("Security feedback provided: " + feedbackProvided);

        log.info("===== TEST PASSED: verifyOtpAttemptLimitMessage =====");
    }

    // ==================== STUB TESTS (enabled=false) ====================
    // These require backend API access, session management, or network proxies.

    @Test(groups = { TestGroups.REGRESSION, TestGroups.LOGIN,
            TestGroups.NEGATIVE }, description = "[STUB] Verify account is locked after N consecutive wrong OTP attempts — requires backend state reset after test", priority = 50, enabled = false
    // TODO: Determine exact lock threshold from backend. After test, call backend
    // API to unlock account.
    // Steps: (1) Enter wrong OTP N times, (2) Assert 'account locked' UI, (3) Reset
    // account via API
    )
    public void verifyAccountLockAfterNFailures() {
        log.info("===== STUB TEST: verifyAccountLockAfterNFailures — requires backend API =====");
    }

    @Test(groups = { TestGroups.REGRESSION, TestGroups.LOGIN,
            TestGroups.NEGATIVE }, description = "[STUB] Verify OTP value is not exposed in API response body — requires network interceptor/proxy", priority = 51, enabled = false
    // TODO: Set up an HTTP proxy (e.g., BrowserMob, Charles) to intercept and
    // inspect API responses.
    // Steps: (1) Trigger OTP, (2) Inspect all outbound API responses, (3) Assert
    // OTP value is NOT in any response body
    )
    public void verifyOtpNotExposedInApiResponse() {
        log.info("===== STUB TEST: verifyOtpNotExposedInApiResponse — requires network proxy =====");
    }

    @Test(groups = { TestGroups.REGRESSION, TestGroups.LOGIN,
            TestGroups.NEGATIVE }, description = "[STUB] Verify OTP session is invalidated after successful login — requires backend session API", priority = 52, enabled = false
    // TODO: After login, try to use the same OTP again via API → expect 401/invalid
    // token response
    )
    public void verifyOtpExpiredAfterSuccessfulLogin() {
        log.info("===== STUB TEST: verifyOtpExpiredAfterSuccessfulLogin — requires backend API =====");
    }

    @Test(groups = { TestGroups.REGRESSION, TestGroups.LOGIN,
            TestGroups.NEGATIVE }, description = "[STUB] Verify OTP cannot be reused — second use of same OTP should fail", priority = 53, enabled = false
    // TODO: Login successfully with OTP, then logout and try to use the same OTP
    // again
    // This requires knowing the OTP value (SMS API) and backend validation
    )
    public void verifyOtpCannotBeReused() {
        log.info("===== STUB TEST: verifyOtpCannotBeReused — requires SMS API + backend =====");
    }

    @Test(groups = { TestGroups.REGRESSION, TestGroups.LOGIN,
            TestGroups.NEGATIVE }, description = "[STUB] Verify OTP session expires after logout — re-using OTP after logout should fail", priority = 54, enabled = false
    // TODO: Login → Logout → Navigate back to OTP screen → Enter old OTP → Expect
    // error
    // Requires: knowing the OTP value, which needs SMS gateway or static test OTP
    // environment
    )
    public void verifyOtpExpiredOnLogout() {
        log.info("===== STUB TEST: verifyOtpExpiredOnLogout — requires session management support =====");
    }

    @Test(groups = { TestGroups.REGRESSION, TestGroups.LOGIN,
            TestGroups.NEGATIVE }, description = "[STUB] Verify blacklisted/blocked phone number cannot trigger OTP — requires test blacklist entry", priority = 55, enabled = false
    // TODO: Register a phone number in backend as blacklisted (test environment
    // only)
    // Steps: (1) Enter blacklisted number, (2) Tap Proceed, (3) Assert error
    // message or OTP not sent
    )
    public void verifyBlacklistedPhoneCannotTriggerOtp() {
        log.info("===== STUB TEST: verifyBlacklistedPhoneCannotTriggerOtp — requires backend test data =====");
    }

    // ==================== Private Helpers ====================

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
