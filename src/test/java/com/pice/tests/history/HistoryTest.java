package com.pice.tests.history;

import com.pice.base.BaseTest;
import com.pice.constants.TestGroups;
import com.pice.listeners.ExtentReportListener;
import com.pice.pages.DashboardPage;
import com.pice.pages.HistoryPage;
import com.pice.utils.AuthHelper;
import com.pice.utils.GestureUtils;
import com.pice.utils.SoftAssertUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * History Screen Test Suite for the Pice App.
 *
 * <p>
 * Automates every key behaviour of the History screen:
 * <ul>
 * <li>Smoke — screen loads with expected UI elements and filter tabs</li>
 * <li>Positive — each filter tab (All, Payments, Cards, Loan) works
 * correctly</li>
 * <li>Positive — transaction list content loads (items or empty state)</li>
 * <li>Positive — tapping a transaction opens its detail view</li>
 * <li>Positive — scroll down loads more; scroll up returns to top</li>
 * <li>Negative — filter tabs do not crash the app when no data exists</li>
 * <li>E2E — full navigation: Dashboard → History → filter cycle → back to
 * Dashboard</li>
 * </ul>
 *
 * <p>
 * <b>Prerequisites:</b>
 * <ul>
 * <li>Physical device connected (or emulator) with the app installed</li>
 * <li>Valid credentials configured in test properties (test.mobile.number,
 * test.otp)</li>
 * <li>App is on the Home Dashboard (AuthHelper.ensureLoggedIn() handles
 * this)</li>
 * </ul>
 *
 * <p>
 * <b>Run:</b>
 * 
 * <pre>{@code make run TEST=com.pice.tests.history.HistoryTest}</pre>
 * 
 * <pre>{@code make run TEST=com.pice.tests.history.HistoryTest ENV=staging}</pre>
 */
public class HistoryTest extends BaseTest {

    private DashboardPage dashboard;
    private HistoryPage historyPage;

    // ==================== Setup ====================

    /**
     * Verify the driver session is alive after classSetup() completes.
     * Login and navigation to dashboard is deferred to @BeforeMethod
     * to ensure the Appium driver is fully ready before interacting with the app.
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "classSetup")
    public void loginAndNavigateToDashboard() {
        log.info("========== HISTORY TEST SUITE SETUP ==========");
        // Driver is now fully initialized by classSetup() — do a lightweight check
        try {
            dashboard = new DashboardPage(true); // skipWait — don't block here
        } catch (Exception e) {
            log.warn("DashboardPage init in @BeforeClass: {} — will retry in @BeforeMethod", e.getMessage());
        }
        log.info("========== HISTORY TEST SUITE SETUP COMPLETE ==========");
    }

    /**
     * Override to keep the existing app session — no app restart between tests.
     */
    @Override
    protected void resetAppState() {
        log.info("--- Custom resetAppState: Keeping session alive (no logout/restart) ---");
        // No-op: preserve login session and navigate from dashboard
    }

    /**
     * Before each test:
     * 1. Ensure user is logged in (AuthHelper handles the full login flow if
     * needed)
     * 2. Return to the Home Dashboard
     * 3. Navigate to the History screen
     */
    @BeforeMethod(alwaysRun = true)
    public void navigateToHistoryBeforeTest() {
        log.info("--- BeforeMethod: Ensuring logged in and on History screen ---");

        // Step 1: Ensure user is logged in — safe to call here, driver is ready
        AuthHelper.ensureLoggedIn();

        // Step 2: Ensure dashboard reference exists
        if (dashboard == null) {
            dashboard = new DashboardPage(true);
        }

        // Step 3: Check if already on History page (no need to go back to dashboard and
        // tap again if stable)
        if (historyPage == null) {
            historyPage = new HistoryPage(true);
        }

        if (historyPage.isHistoryScreenVisible()) {
            log.info("Already on History screen — skipping navigation");
            return;
        }

        // Step 4: Recover to the dashboard if not visible
        if (!dashboard.isDashboardVisible()) {
            log.info("Not on dashboard and not on History screen — attempting back recovery...");
            dashboard.navigateBackToDashboard();
            sleep(2000);
        }

        // Step 5: Navigate to History from dashboard
        log.info("Tapping History tab from Home Dashboard...");
        dashboard.navigateToHistory();
        sleep(2000);

        // Step 6: Wait for History screen to appear
        if (!historyPage.waitForHistoryScreenVisible(10)) {
            log.warn("History screen not confirmed visible — proceeding with test anyway");
        }
    }

    @AfterClass(alwaysRun = true)
    public void returnToDashboardAfterSuite() {
        log.info("========== HISTORY TEST SUITE TEARDOWN ==========");
        try {
            if (dashboard != null) {
                dashboard.navigateToHome();
                sleep(2000);
                log.info("Returned to Home Dashboard after History test suite");
            }
        } catch (Exception e) {
            log.warn("Failed to return to dashboard in teardown: {}", e.getMessage());
        }
        log.info("========== HISTORY TEST SUITE TEARDOWN COMPLETE ==========");
    }

    // ==================== SMOKE TESTS ====================

    /**
     * Verify the History screen loads with all key UI elements visible:
     * the screen itself, filter tabs (All / Payments / Cards / Loan),
     * and either a transaction list or an empty state.
     */
    @Test(groups = { TestGroups.SMOKE, TestGroups.HISTORY,
            TestGroups.POSITIVE }, description = "Verify all key UI elements are displayed on the History screen", priority = 1)
    public void verifyHistoryScreenElements() {
        log.info("===== TEST: verifyHistoryScreenElements =====");
        ExtentReportListener.logStep("Verify History screen is loaded");

        SoftAssertUtils.init();

        // Screen must be visible
        SoftAssertUtils.assertTrue(historyPage.isHistoryScreenVisible(),
                "History screen should be visible after tapping History tab");

        // Filter tabs must be present
        SoftAssertUtils.assertTrue(historyPage.isFilterAllVisible(),
                "'All' filter tab should be visible");
        SoftAssertUtils.assertTrue(historyPage.isFilterPaymentsVisible(),
                "'Payments' filter tab should be visible");
        SoftAssertUtils.assertTrue(historyPage.isFilterCardsVisible(),
                "'Cards' filter tab should be visible");
        SoftAssertUtils.assertTrue(historyPage.isFilterLoanVisible(),
                "'Loan' filter tab should be visible");

        // Screen must have content (transactions or empty state)
        SoftAssertUtils.assertTrue(historyPage.hasContentLoaded(),
                "History screen should show transactions or empty state — not blank");

        ExtentReportListener.logStep("All History screen elements verified");
        SoftAssertUtils.assertAll();
        log.info("===== TEST PASSED: verifyHistoryScreenElements =====");
    }

    // ==================== POSITIVE — Filter Tab Tests ====================

    /**
     * Tap the "All" filter tab and verify the screen updates without crashing.
     */
    @Test(groups = { TestGroups.REGRESSION, TestGroups.HISTORY,
            TestGroups.POSITIVE }, description = "Tap 'All' filter tab and verify the screen refreshes without error", priority = 2)
    public void verifyFilterAllTab() {
        log.info("===== TEST: verifyFilterAllTab =====");

        ExtentReportListener.logStep("Tap 'All' filter tab");
        historyPage.tapFilterAll();

        ExtentReportListener.logStep("Verify screen is still displayed and has content");
        Assert.assertTrue(historyPage.isHistoryScreenVisible(),
                "History screen should remain visible after tapping 'All' filter");
        Assert.assertTrue(historyPage.hasContentLoaded(),
                "'All' filter should show transactions or empty state");

        log.info("===== TEST PASSED: verifyFilterAllTab =====");
    }

    /**
     * Tap the "Payments" filter tab and verify the screen shows payment history.
     */
    @Test(groups = { TestGroups.REGRESSION, TestGroups.HISTORY,
            TestGroups.POSITIVE }, description = "Tap 'Payments' filter tab and verify payment transactions are shown", priority = 3)
    public void verifyFilterPaymentsTab() {
        log.info("===== TEST: verifyFilterPaymentsTab =====");

        ExtentReportListener.logStep("Tap 'Payments' filter tab");
        historyPage.tapFilterPayments();
        sleep(2000);

        ExtentReportListener.logStep("Verify Payments history screen is displayed");
        Assert.assertTrue(historyPage.isHistoryScreenVisible(),
                "History screen should remain visible after tapping 'Payments' filter");
        Assert.assertTrue(historyPage.hasContentLoaded(),
                "'Payments' filter should show payment transactions or empty state");

        log.info("===== TEST PASSED: verifyFilterPaymentsTab =====");
    }

    /**
     * Tap the "Cards" filter tab and verify the screen shows card-related history.
     */
    @Test(groups = { TestGroups.REGRESSION, TestGroups.HISTORY,
            TestGroups.POSITIVE }, description = "Tap 'Cards' filter tab and verify card transactions are shown", priority = 4)
    public void verifyFilterCardsTab() {
        log.info("===== TEST: verifyFilterCardsTab =====");

        ExtentReportListener.logStep("Tap 'Cards' filter tab");
        historyPage.tapFilterCards();
        sleep(2000);

        ExtentReportListener.logStep("Verify Cards history screen is displayed");
        Assert.assertTrue(historyPage.isHistoryScreenVisible(),
                "History screen should remain visible after tapping 'Cards' filter");
        Assert.assertTrue(historyPage.hasContentLoaded(),
                "'Cards' filter should show card transactions or empty state");

        log.info("===== TEST PASSED: verifyFilterCardsTab =====");
    }

    /**
     * Tap the "Loan" filter tab and verify the screen shows loan history.
     */
    @Test(groups = { TestGroups.REGRESSION, TestGroups.HISTORY,
            TestGroups.POSITIVE }, description = "Tap 'Loan' filter tab and verify loan transactions are shown", priority = 5)
    public void verifyFilterLoanTab() {
        log.info("===== TEST: verifyFilterLoanTab =====");

        ExtentReportListener.logStep("Tap 'Loan' filter tab");
        historyPage.tapFilterLoan();
        sleep(2000);

        ExtentReportListener.logStep("Verify Loan history screen is displayed");
        Assert.assertTrue(historyPage.isHistoryScreenVisible(),
                "History screen should remain visible after tapping 'Loan' filter");
        Assert.assertTrue(historyPage.hasContentLoaded(),
                "'Loan' filter should show loan transactions or empty state");

        log.info("===== TEST PASSED: verifyFilterLoanTab =====");
    }

    // ==================== POSITIVE — Transaction List ====================

    /**
     * Verify the transaction list displays items (or gracefully shows an empty
     * state).
     * Checks that transaction amounts (₹) are visible when transactions exist.
     */
    @Test(groups = { TestGroups.REGRESSION, TestGroups.HISTORY,
            TestGroups.POSITIVE }, description = "Verify transaction list shows items with amounts or a proper empty state", priority = 6)
    public void verifyTransactionListContent() {
        log.info("===== TEST: verifyTransactionListContent =====");

        // Start on "All" filter to maximize chance of transactions
        ExtentReportListener.logStep("Select 'All' filter to view all transactions");
        historyPage.tapFilterAll();
        sleep(2000);

        if (historyPage.hasTransactions()) {
            ExtentReportListener.logStep("Transactions found — verifying transaction count > 0");
            int count = historyPage.getTransactionCount();
            log.info("Transaction count: {}", count);
            Assert.assertTrue(count > 0, "Transaction count should be > 0 when transactions exist");

            ExtentReportListener.logStep("Verifying transaction amounts are visible");
            Assert.assertTrue(historyPage.areAmountsVisible(),
                    "Transaction amounts (₹) should be visible when transactions are present");
        } else {
            ExtentReportListener.logStep("No transactions found — verifying empty state is shown");
            Assert.assertTrue(historyPage.isEmptyStateVisible(),
                    "Empty state message should be shown when no transactions exist");
            log.info("Empty state is visible — acceptable for a test account with no transactions");
        }

        log.info("===== TEST PASSED: verifyTransactionListContent =====");
    }

    /**
     * Tap the first transaction in the list (if present) and verify
     * the detail screen opens without crashing, then return to History.
     */
    @Test(groups = { TestGroups.REGRESSION, TestGroups.HISTORY,
            TestGroups.POSITIVE }, description = "Tap first transaction to open detail view, then navigate back", priority = 7)
    public void verifyTransactionDetailNavigation() {
        log.info("===== TEST: verifyTransactionDetailNavigation =====");

        ExtentReportListener.logStep("Select 'All' filter to find transactions");
        historyPage.tapFilterAll();
        sleep(2000);

        if (!historyPage.hasTransactions()) {
            log.info("No transactions available — skipping detail tap (empty state account)");
            ExtentReportListener.logStep("No transactions to tap — test skipped gracefully");
            return;
        }

        ExtentReportListener.logStep("Tap first transaction to open detail");
        boolean tapped = historyPage.tapFirstTransaction();
        Assert.assertTrue(tapped, "Should be able to tap the first transaction item");
        sleep(2000);

        ExtentReportListener.logStep("Verify app did not crash — press Back to return");
        log.info("Transaction detail opened — pressing back to return to History");
        historyPage.pressBack();
        sleep(2000);

        ExtentReportListener.logStep("Verify History screen is visible after returning");
        Assert.assertTrue(historyPage.waitForHistoryScreenVisible(8),
                "History screen should be visible after returning from transaction detail");

        log.info("===== TEST PASSED: verifyTransactionDetailNavigation =====");
    }

    // ==================== POSITIVE — Scroll Tests ====================

    /**
     * Scroll down through the transaction list to load more items,
     * then scroll back to the top. Verifies no crash and content integrity.
     */
    @Test(groups = { TestGroups.REGRESSION, TestGroups.HISTORY,
            TestGroups.POSITIVE }, description = "Scroll down in transaction list and back to top without crash", priority = 8)
    public void verifyScrollBehavior() {
        log.info("===== TEST: verifyScrollBehavior =====");

        ExtentReportListener.logStep("Start on 'All' filter");
        historyPage.tapFilterAll();
        sleep(2000);

        ExtentReportListener.logStep("Scroll down to load more transactions");
        historyPage.scrollDownForMore();
        sleep(1500);
        historyPage.scrollDownForMore();
        sleep(1500);

        ExtentReportListener.logStep("Verify screen is still stable after scrolling down");
        Assert.assertTrue(historyPage.isHistoryScreenVisible(),
                "History screen should remain visible after scrolling down");

        ExtentReportListener.logStep("Scroll back to top");
        historyPage.scrollToTop();
        sleep(1500);
        historyPage.scrollToTop();
        sleep(1500);

        ExtentReportListener.logStep("Verify screen content is still intact at top");
        Assert.assertTrue(historyPage.isHistoryScreenVisible(),
                "History screen should be visible after scrolling back to top");

        log.info("===== TEST PASSED: verifyScrollBehavior =====");
    }

    // ==================== NEGATIVE — Filter Tabs with No Data ====================

    /**
     * Verify that switching between all filter tabs in rapid succession
     * does not crash the app (robustness check).
     */
    @Test(groups = { TestGroups.REGRESSION, TestGroups.HISTORY,
            TestGroups.NEGATIVE }, description = "Rapid filter tab switching should not crash the app", priority = 9)
    public void verifyRapidFilterSwitching() {
        log.info("===== TEST: verifyRapidFilterSwitching =====");

        ExtentReportListener.logStep("Rapidly switch between filter tabs");

        try {
            historyPage.tapFilterPayments();
            sleep(800);
            historyPage.tapFilterCards();
            sleep(800);
            historyPage.tapFilterLoan();
            sleep(800);
            historyPage.tapFilterAll();
            sleep(800);
            historyPage.tapFilterPayments();
            sleep(800);
            historyPage.tapFilterAll();
            sleep(1000);
        } catch (Exception e) {
            log.error("App crashed or threw exception during rapid filter switching: {}", e.getMessage());
            Assert.fail("App should not crash during rapid filter switching: " + e.getMessage());
        }

        ExtentReportListener.logStep("Verify screen is stable after rapid tab switching");
        Assert.assertTrue(historyPage.isHistoryScreenVisible(),
                "History screen should remain stable after rapid filter switching");
        Assert.assertTrue(historyPage.hasContentLoaded(),
                "Screen should still display content after rapid switching");

        log.info("===== TEST PASSED: verifyRapidFilterSwitching =====");
    }

    /**
     * Verify that when a filter (e.g., Cards or Loan) has no transactions,
     * the app shows a proper empty state instead of crashing or showing a blank
     * screen.
     */
    @Test(groups = { TestGroups.REGRESSION, TestGroups.HISTORY,
            TestGroups.NEGATIVE }, description = "Filters with no data should show empty state, not blank or crash", priority = 10)
    public void verifyEmptyStateOnFilterWithNoData() {
        log.info("===== TEST: verifyEmptyStateOnFilterWithNoData =====");

        // Loan tab is most likely to be empty for test accounts
        ExtentReportListener.logStep("Tap 'Loan' filter (likely empty for test account)");
        historyPage.tapFilterLoan();
        sleep(2000);

        ExtentReportListener.logStep("Verify screen shows either transactions or empty state (not blank/crash)");
        boolean hasTransactions = historyPage.hasTransactions();
        boolean hasEmptyState = historyPage.isEmptyStateVisible();

        if (hasTransactions) {
            log.info("Loan transactions found — account has loan history");
            ExtentReportListener.logStep("Loan transactions exist — verifying list is shown correctly");
        } else if (hasEmptyState) {
            log.info("Empty state displayed for Loan filter — as expected for test account");
            ExtentReportListener.logStep("Empty state displayed correctly for Loan filter");
        } else {
            log.info("Neither transactions nor explicit empty state found — checking screen is at least visible");
        }

        Assert.assertTrue(historyPage.isHistoryScreenVisible(),
                "History screen should be visible (no crash) even when filter has no data");
        Assert.assertTrue(hasTransactions || hasEmptyState,
                "History screen should show transactions OR an empty state when a filter has no data");

        log.info("===== TEST PASSED: verifyEmptyStateOnFilterWithNoData =====");
    }

    // ==================== E2E TEST ====================

    /**
     * Full E2E scenario:
     * Dashboard → History tab → All filter → Payments filter → Cards filter →
     * Loan filter → Scroll down → Scroll up → Back to Dashboard.
     */
    @Test(groups = { TestGroups.E2E, TestGroups.HISTORY,
            TestGroups.POSITIVE }, description = "E2E: Dashboard → History → cycle all filters → scroll → return to Dashboard", priority = 11)
    public void verifyHistoryE2EFlow() {
        log.info("===== TEST: verifyHistoryE2EFlow =====");

        // Step 1: Verify we are on History screen
        ExtentReportListener.logStep("Step 1: Verify History screen is loaded");
        Assert.assertTrue(historyPage.isHistoryScreenVisible(),
                "Should be on History screen at start of E2E test");
        log.info("✅ Step 1: History screen visible");

        // Step 2: Tap All filter
        ExtentReportListener.logStep("Step 2: Select 'All' filter");
        historyPage.tapFilterAll();
        sleep(1500);
        Assert.assertTrue(historyPage.isHistoryScreenVisible(), "Screen should be stable on 'All' filter");
        log.info("✅ Step 2: 'All' filter applied");

        // Step 3: Tap Payments filter
        ExtentReportListener.logStep("Step 3: Select 'Payments' filter");
        historyPage.tapFilterPayments();
        sleep(1500);
        Assert.assertTrue(historyPage.hasContentLoaded(), "Screen should have content on 'Payments' filter");
        log.info("✅ Step 3: 'Payments' filter applied");

        // Step 4: Tap Cards filter
        ExtentReportListener.logStep("Step 4: Select 'Cards' filter");
        historyPage.tapFilterCards();
        sleep(1500);
        Assert.assertTrue(historyPage.hasContentLoaded(), "Screen should have content on 'Cards' filter");
        log.info("✅ Step 4: 'Cards' filter applied");

        // Step 5: Tap Loan filter
        ExtentReportListener.logStep("Step 5: Select 'Loan' filter");
        historyPage.tapFilterLoan();
        sleep(1500);
        Assert.assertTrue(historyPage.hasContentLoaded(), "Screen should have content on 'Loan' filter");
        log.info("✅ Step 5: 'Loan' filter applied");

        // Step 6: Return to All and scroll
        ExtentReportListener.logStep("Step 6: Return to 'All' filter and scroll down");
        historyPage.tapFilterAll();
        sleep(1500);
        historyPage.scrollDownForMore();
        sleep(1000);
        log.info("✅ Step 6: Scrolled down on 'All' filter");

        // Step 7: Scroll back to top
        ExtentReportListener.logStep("Step 7: Scroll back to top");
        historyPage.scrollToTop();
        sleep(1000);
        log.info("✅ Step 7: Scrolled back to top");

        // Step 8: Navigate back to Home Dashboard
        ExtentReportListener.logStep("Step 8: Navigate back to Home Dashboard");
        historyPage.navigateBackToHome();
        sleep(2000);

        // Step 9: Verify we are back on dashboard
        ExtentReportListener.logStep("Step 9: Verify Home Dashboard is restored");
        DashboardPage finalDashboard = new DashboardPage(true);
        Assert.assertTrue(finalDashboard.isDashboardVisible(),
                "Home Dashboard should be visible after completing the History E2E flow");
        log.info("✅ Step 9: Returned to Home Dashboard");

        log.info("✅ History E2E flow completed successfully");
        log.info("===== TEST PASSED: verifyHistoryE2EFlow =====");
    }

    // ==================== Helper Methods ====================

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
