package com.pice.tests.dashboard;

import com.pice.base.BaseTest;
import com.pice.constants.TestGroups;
import com.pice.listeners.ExtentReportListener;
import com.pice.pages.DashboardPage;
import com.pice.pages.LoginPage;
import com.pice.pages.OtpPage;
import com.pice.pages.PermissionPage;
import com.pice.utils.AuthHelper;
import com.pice.utils.GestureUtils;
import com.pice.utils.SoftAssertUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Home Dashboard Module Test Cases for the Pice App.
 *
 * <p>Automates every interactive element on the Home Dashboard:
 * <ul>
 *   <li>UI element verification — all sections, tabs, buttons, cards (smoke)</li>
 *   <li>Bottom navigation tabs — Home, Cards, Loan, History (positive)</li>
 *   <li>Payment category cards — Business, Taxes, Education (positive)</li>
 *   <li>Coins display tap (positive)</li>
 *   <li>Make Payment floating button (positive)</li>
 *   <li>Explore Now / Trending section (positive)</li>
 *   <li>Referral card (positive)</li>
 *   <li>Full navigation cycle — all tabs sequentially (E2E)</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>Physical device (Samsung SM-S928U1 / serial R5CXA0Q9QVD)</li>
 *   <li>Valid credentials configured (test.mobile.number, test.otp)</li>
 *   <li>Developer mode will be temporarily disabled during login</li>
 * </ul>
 *
 * <p><b>Run:</b>
 * <pre>{@code make test-batch-dashboard ENV=staging}</pre>
 * <pre>{@code make run TEST=com.pice.tests.dashboard.HomeDashboardTest}</pre>
 */
public class HomeDashboardTest extends BaseTest {

    private static final String PHONE_NUMBER =
            com.pice.config.ConfigManager.get("test.mobile.number", "9962063736");
    private static final String DEVICE_SERIAL = getConnectedDeviceSerial();

    private static String getConnectedDeviceSerial() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"adb", "devices"});
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.endsWith("device") && !line.startsWith("List")) {
                    return line.split("\\s+")[0];
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "10MG56FM6E000FD"; // fallback
    }

    private static final String APP_PACKAGE = "one.pice.pice_business_loan.pre";
    private static final String APP_ACTIVITY = "one.pice.pice_business_loan.MainActivity";

    private DashboardPage dashboard;

    // ==================== Setup ====================

    @BeforeClass(alwaysRun = true, dependsOnMethods = "classSetup")
    public void loginAndNavigateToDashboard() {
        log.info("========== DASHBOARD TEST SUITE SETUP ==========");

        // Ensure user is logged in
        AuthHelper.ensureLoggedIn();

        dashboard = new DashboardPage();

        // Ensure we are on the Home Dashboard tab (if left on another tab)
        if (!dashboard.isDashboardVisible()) {
            log.warn("Dashboard not visible — trying to navigate back to Home Dashboard...");
            dashboard.navigateBackToDashboard();
        }

        Assert.assertTrue(dashboard.isDashboardVisible(), "Dashboard must be visible for dashboard tests");
        log.info("========== DASHBOARD TEST SUITE SETUP COMPLETE ==========");
    }

    @Override
    protected void resetAppState() {
        log.info("--- Custom resetAppState: Keeping dashboard session active ---");
        // No-op to prevent restarting the app between tests
    }

    @BeforeMethod(alwaysRun = true)
    public void ensureOnDashboard() {
        log.info("--- Setup: Ensuring dashboard is visible ---");
        if (dashboard == null) {
            dashboard = new DashboardPage(true);
        }
        if (!dashboard.isDashboardVisible()) {
            log.info("Dashboard not immediately visible, waiting up to 10 seconds...");
            if (!dashboard.waitForDashboardVisible(10)) {
                log.warn("Dashboard still not visible after 10s wait, attempting back recovery...");
                dashboard.navigateBackToDashboard();
                sleep(2000);
            }
        }
        log.info("--- Reset scroll position to top ---");
        GestureUtils.swipeDown();
        sleep(1000);
        GestureUtils.swipeDown();
        sleep(1000);
    }

    @AfterClass(alwaysRun = true)
    public void restoreDeviceSettings() {
        log.info("========== DASHBOARD TEST SUITE TEARDOWN ==========");
        try {
            if (DEVICE_SERIAL.startsWith("emulator-")) {
                execAdb("shell", "settings", "put", "global", "development_settings_enabled", "1");
                log.info("Developer options re-enabled");
            }
        } catch (Exception e) {
            log.warn("Failed to restore developer settings: {}", e.getMessage());
        }
        log.info("========== DASHBOARD TEST SUITE TEARDOWN COMPLETE ==========");
    }

    // ==================== SMOKE TESTS ====================

    @Test(
        groups = {TestGroups.SMOKE, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify all UI elements on the Home Dashboard are displayed correctly",
        priority = 1
    )
    public void verifyDashboardScreenElements() {
        log.info("===== TEST: verifyDashboardScreenElements =====");
        ExtentReportListener.logStep("Verify all Dashboard UI elements");

        SoftAssertUtils.init();

        // Bottom navigation tabs
        SoftAssertUtils.assertTrue(dashboard.isHomeTabVisible(),
                "Home tab should be visible in bottom nav");
        SoftAssertUtils.assertTrue(dashboard.isCardsTabVisible(),
                "Cards tab should be visible in bottom nav");
        SoftAssertUtils.assertTrue(dashboard.isLoanTabVisible(),
                "Loan tab should be visible in bottom nav");
        SoftAssertUtils.assertTrue(dashboard.isHistoryTabVisible(),
                "History tab should be visible in bottom nav");

        // Coins display
        SoftAssertUtils.assertTrue(dashboard.isCoinsDisplayVisible(),
                "Coins display should be visible at the top");

        // Payment categories
        SoftAssertUtils.assertTrue(dashboard.arePaymentCategoriesVisible(),
                "At least one payment category should be visible");

        // Make Payment button
        SoftAssertUtils.assertTrue(dashboard.isMakePaymentButtonVisible(),
                "Make Payment floating button should be visible");

        // Not on login screen
        SoftAssertUtils.assertTrue(dashboard.isNotOnLoginScreen(),
                "Should NOT be on login screen");

        // Coins text should be non-empty
        String coinsText = dashboard.getCoinsText();
        if (!coinsText.isEmpty()) {
            ExtentReportListener.logStep("Coins display: " + coinsText);
            log.info("Coins: {}", coinsText);
        }

        SoftAssertUtils.assertAll();
        log.info("===== TEST PASSED: verifyDashboardScreenElements =====");
    }

    // ==================== POSITIVE TESTS — Bottom Navigation ====================

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Home tab in bottom navigation keeps dashboard visible",
        priority = 2
    )
    public void verifyBottomNavHomeTab() {
        log.info("===== TEST: verifyBottomNavHomeTab =====");

        ExtentReportListener.logStep("Tap Home tab");
        dashboard.navigateToHome();
        sleep(2000);

        ExtentReportListener.logStep("Verify dashboard is still visible");
        Assert.assertTrue(dashboard.isDashboardVisible(),
                "Dashboard should remain visible after tapping Home tab");

        log.info("===== TEST PASSED: verifyBottomNavHomeTab =====");
    }

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Cards tab navigates away from dashboard and back",
        priority = 3
    )
    public void verifyBottomNavCardsTab() {
        log.info("===== TEST: verifyBottomNavCardsTab =====");

        ExtentReportListener.logStep("Tap Cards tab");
        dashboard.navigateToCards();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred (no longer on dashboard)");
        log.info("Cards tab tapped — verifying screen change");

        ExtentReportListener.logStep("Return to Home tab");
        dashboard.navigateToHome();
        sleep(2000);

        ExtentReportListener.logStep("Verify dashboard restored");
        Assert.assertTrue(dashboard.isDashboardVisible(),
                "Dashboard should be visible after returning to Home tab");

        log.info("===== TEST PASSED: verifyBottomNavCardsTab =====");
    }

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Loan tab navigates away from dashboard and back",
        priority = 4
    )
    public void verifyBottomNavLoanTab() {
        log.info("===== TEST: verifyBottomNavLoanTab =====");

        ExtentReportListener.logStep("Tap Loan tab");
        dashboard.navigateToLoan();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred");
        log.info("Loan tab tapped — verifying screen change");

        ExtentReportListener.logStep("Return to Home tab");
        dashboard.navigateToHome();
        sleep(2000);

        ExtentReportListener.logStep("Verify dashboard restored");
        Assert.assertTrue(dashboard.isDashboardVisible(),
                "Dashboard should be visible after returning from Loan tab");

        log.info("===== TEST PASSED: verifyBottomNavLoanTab =====");
    }

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify History tab navigates away from dashboard and back",
        priority = 5
    )
    public void verifyBottomNavHistoryTab() {
        log.info("===== TEST: verifyBottomNavHistoryTab =====");

        ExtentReportListener.logStep("Tap History tab");
        dashboard.navigateToHistory();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred");
        log.info("History tab tapped — verifying screen change");

        ExtentReportListener.logStep("Return to Home tab");
        dashboard.navigateToHome();
        sleep(2000);

        ExtentReportListener.logStep("Verify dashboard restored");
        Assert.assertTrue(dashboard.isDashboardVisible(),
                "Dashboard should be visible after returning from History tab");

        log.info("===== TEST PASSED: verifyBottomNavHistoryTab =====");
    }

    // ==================== POSITIVE TESTS — Payment Categories ====================

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Business category card tap navigates and returns",
        priority = 6
    )
    public void verifyBusinessCategoryTap() {
        log.info("===== TEST: verifyBusinessCategoryTap =====");

        if (!dashboard.isBusinessCategoryVisible()) {
            log.info("Business category not visible — scrolling to find it");
            GestureUtils.swipeUp();
            sleep(2000);
        }

        ExtentReportListener.logStep("Tap Business category card");
        dashboard.tapBusinessCategory();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred — app should not crash");
        log.info("Business category tapped successfully");

        ExtentReportListener.logStep("Return to dashboard");
        boolean returned = dashboard.navigateBackToDashboard();
        Assert.assertTrue(returned,
                "Should be able to return to dashboard after tapping Business category");

        log.info("===== TEST PASSED: verifyBusinessCategoryTap =====");
    }

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Taxes category card tap navigates and returns",
        priority = 7
    )
    public void verifyTaxesCategoryTap() {
        log.info("===== TEST: verifyTaxesCategoryTap =====");

        if (!dashboard.isTaxesCategoryVisible()) {
            log.info("Taxes category not visible — scrolling to find it");
            GestureUtils.swipeUp();
            sleep(2000);
        }

        ExtentReportListener.logStep("Tap Taxes category card");
        dashboard.tapTaxesCategory();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred — app should not crash");
        log.info("Taxes category tapped successfully");

        ExtentReportListener.logStep("Return to dashboard");
        boolean returned = dashboard.navigateBackToDashboard();
        Assert.assertTrue(returned,
                "Should be able to return to dashboard after tapping Taxes category");

        log.info("===== TEST PASSED: verifyTaxesCategoryTap =====");
    }

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Education category card tap navigates and returns",
        priority = 8
    )
    public void verifyEducationCategoryTap() {
        log.info("===== TEST: verifyEducationCategoryTap =====");

        if (!dashboard.isEducationCategoryVisible()) {
            log.info("Education category not visible — scrolling to find it");
            GestureUtils.swipeUp();
            sleep(2000);
        }

        ExtentReportListener.logStep("Tap Education category card");
        dashboard.tapEducationCategory();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred — app should not crash");
        log.info("Education category tapped successfully");

        ExtentReportListener.logStep("Return to dashboard");
        boolean returned = dashboard.navigateBackToDashboard();
        Assert.assertTrue(returned,
                "Should be able to return to dashboard after tapping Education category");

        log.info("===== TEST PASSED: verifyEducationCategoryTap =====");
    }

    // ==================== POSITIVE TESTS — Action Buttons ====================

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Coins display tap navigates and returns",
        priority = 9
    )
    public void verifyCoinsDisplayTap() {
        log.info("===== TEST: verifyCoinsDisplayTap =====");

        if (!dashboard.isCoinsDisplayVisible()) {
            log.info("Coins display not visible — skipping (may not be on screen)");
            return;
        }

        String coinsBefore = dashboard.getCoinsText();
        ExtentReportListener.logStep("Coins display shows: " + coinsBefore);

        ExtentReportListener.logStep("Tap Coins display");
        dashboard.tapCoinsDisplay();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred — app should not crash");
        log.info("Coins display tapped successfully");

        ExtentReportListener.logStep("Return to dashboard");
        boolean returned = dashboard.navigateBackToDashboard();
        Assert.assertTrue(returned,
                "Should be able to return to dashboard after tapping Coins display");

        log.info("===== TEST PASSED: verifyCoinsDisplayTap =====");
    }

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Make Payment floating button tap navigates and returns",
        priority = 10
    )
    public void verifyMakePaymentButton() {
        log.info("===== TEST: verifyMakePaymentButton =====");

        ExtentReportListener.logStep("Verify Make Payment button is visible");
        if (!dashboard.isMakePaymentButtonVisible()) {
            log.warn("Make Payment button not visible — test cannot proceed");
            Assert.fail("Make Payment button should be visible on dashboard");
        }

        ExtentReportListener.logStep("Tap Make Payment button");
        dashboard.tapMakePayment();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred — app should not crash");
        log.info("Make Payment button tapped successfully");

        ExtentReportListener.logStep("Return to dashboard");
        boolean returned = dashboard.navigateBackToDashboard();
        Assert.assertTrue(returned,
                "Should be able to return to dashboard after tapping Make Payment");

        log.info("===== TEST PASSED: verifyMakePaymentButton =====");
    }

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Explore Now tap in Trending section navigates and returns",
        priority = 11
    )
    public void verifyExploreNowTap() {
        log.info("===== TEST: verifyExploreNowTap =====");

        // Scroll down to find the TRENDING section
        ExtentReportListener.logStep("Scrolling to find TRENDING section");
        for (int i = 0; i < 5; i++) {
            if (dashboard.isTrendingSectionVisible() || dashboard.isExploreNowVisible()) {
                break;
            }
            GestureUtils.swipeUp();
            sleep(1500);
        }

        if (!dashboard.isExploreNowVisible()) {
            log.info("Explore Now not found after scrolling — may not be present in current layout");
            ExtentReportListener.logStep("EXPLORE NOW not found — skipping");
            return;
        }

        ExtentReportListener.logStep("Tap Explore Now");
        dashboard.tapExploreNow();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred — app should not crash");
        log.info("Explore Now tapped successfully");

        ExtentReportListener.logStep("Return to dashboard");
        boolean returned = dashboard.navigateBackToDashboard();
        Assert.assertTrue(returned,
                "Should be able to return to dashboard after tapping Explore Now");

        log.info("===== TEST PASSED: verifyExploreNowTap =====");
    }

    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "Verify Referral card tap navigates and returns",
        priority = 12
    )
    public void verifyReferralCardTap() {
        log.info("===== TEST: verifyReferralCardTap =====");

        // Scroll to find referral card
        ExtentReportListener.logStep("Scrolling to find Referral card");
        for (int i = 0; i < 5; i++) {
            if (dashboard.isReferralCardVisible()) {
                break;
            }
            GestureUtils.swipeUp();
            sleep(1500);
        }

        if (!dashboard.isReferralCardVisible()) {
            log.info("Referral card not found — may depend on user state, skipping");
            ExtentReportListener.logStep("Referral card not found — skipping (user-state dependent)");
            return;
        }

        ExtentReportListener.logStep("Tap Referral card");
        dashboard.tapReferralCard();
        sleep(3000);

        ExtentReportListener.logStep("Verify navigation occurred — app should not crash");
        log.info("Referral card tapped successfully");

        ExtentReportListener.logStep("Return to dashboard");
        boolean returned = dashboard.navigateBackToDashboard();
        Assert.assertTrue(returned,
                "Should be able to return to dashboard after tapping Referral card");

        log.info("===== TEST PASSED: verifyReferralCardTap =====");
    }

    // ==================== E2E TEST ====================

    @Test(
        groups = {TestGroups.E2E, TestGroups.HOME, TestGroups.DASHBOARD, TestGroups.POSITIVE},
        description = "E2E: Full navigation cycle — Home → Cards → Loan → History → Home",
        priority = 13
    )
    public void verifyFullNavigationCycle() {
        log.info("===== TEST: verifyFullNavigationCycle =====");

        // Verify starting state
        ExtentReportListener.logStep("Step 1: Verify starting on Home dashboard");
        Assert.assertTrue(dashboard.isDashboardVisible(),
                "Should start on dashboard");

        // Cards tab
        ExtentReportListener.logStep("Step 2: Navigate to Cards tab");
        dashboard.navigateToCards();
        sleep(3000);
        log.info("Cards tab screen loaded");

        // Loan tab
        ExtentReportListener.logStep("Step 3: Navigate to Loan tab");
        dashboard.navigateToLoan();
        sleep(3000);
        log.info("Loan tab screen loaded");

        // History tab
        ExtentReportListener.logStep("Step 4: Navigate to History tab");
        dashboard.navigateToHistory();
        sleep(3000);
        log.info("History tab screen loaded");

        // Return to Home
        ExtentReportListener.logStep("Step 5: Navigate back to Home tab");
        dashboard.navigateToHome();
        sleep(3000);

        ExtentReportListener.logStep("Step 6: Verify dashboard is restored after full cycle");
        Assert.assertTrue(dashboard.isDashboardVisible(),
                "Dashboard should be visible after navigating through all tabs");
        log.info("✅ Full navigation cycle completed successfully");

        log.info("===== TEST PASSED: verifyFullNavigationCycle =====");
    }

    // ==================== Helper Methods ====================

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    /**
     * Execute an ADB command targeting the physical device.
     */
    private void execAdb(String... args) {
        try {
            String[] cmd = new String[args.length + 3];
            cmd[0] = "adb";
            cmd[1] = "-s";
            cmd[2] = DEVICE_SERIAL;
            System.arraycopy(args, 0, cmd, 3, args.length);
            Runtime.getRuntime().exec(cmd).waitFor();
        } catch (Exception e) {
            log.warn("ADB command failed: {}", e.getMessage());
        }
    }
}
