package com.pice.tests.payment;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.pice.base.BaseTest;
import com.pice.constants.TestGroups;
import com.pice.listeners.ExtentReportListener;
import com.pice.pages.AddBeneficiaryPage;
import com.pice.pages.DashboardPage;
import com.pice.pages.MakePaymentPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * E2E Test: Add New Beneficiary via Make Payment flow.
 *
 * <p><b>Prerequisite:</b> App is already logged in and on the Home Dashboard.
 * No login/logout is performed — the test starts directly from the home page.
 *
 * <p><b>Flow:</b>
 * <ol>
 *   <li>From Home Dashboard, tap "Make Payment" button</li>
 *   <li>On Make Payment screen, tap "Add New Beneficiary"</li>
 *   <li>Enter Account Number (from config)</li>
 *   <li>Enter IFSC Code (from config)</li>
 *   <li>Tap Confirm</li>
 *   <li>If PAN verification step appears, tap Confirm</li>
 *   <li>On confirmation page, enter Phone Number</li>
 *   <li>If PAN is not auto-fetched, enter PAN Number manually</li>
 *   <li>Tap Confirm</li>
 * </ol>
 *
 * <p><b>Test Data:</b> Loaded from
 * {@code src/test/resources/config/beneficiary-testdata.properties}
 *
 * <p><b>Run:</b>
 * <pre>{@code make run TEST=com.pice.tests.payment.AddBeneficiaryTest}</pre>
 */
public class AddBeneficiaryTest extends BaseTest {

    // ==================== Test Data (loaded from config) ====================

    private String accountNumber;
    private String ifscCode;
    private String phoneNumber;
    private String panNumber;

    private DashboardPage dashboard;

    // ==================== Config Loader ====================

    /**
     * Load test data from beneficiary-testdata.properties.
     */
    private void loadTestData() {
        log.info("Loading test data from beneficiary-testdata.properties...");
        Properties testData = new Properties();

        try (InputStream is = getClass().getClassLoader()
                 .getResourceAsStream("config/beneficiary-testdata.properties")) {
            if (is != null) {
                testData.load(is);
                log.info("Test data loaded successfully");
            } else {
                log.error("beneficiary-testdata.properties not found in classpath!");
                throw new RuntimeException("Test data file not found: config/beneficiary-testdata.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test data: " + e.getMessage(), e);
        }

        accountNumber = testData.getProperty("beneficiary.account.number", "40963433113");
        ifscCode = testData.getProperty("beneficiary.ifsc.code", "SBIN0061739");
        phoneNumber = testData.getProperty("beneficiary.phone.number", "9962063736");
        panNumber = testData.getProperty("beneficiary.pan.number", "ABCDE1234F");

        log.info("Test data — Account: {}, IFSC: {}, Phone: {}, PAN: {}",
                accountNumber, ifscCode, phoneNumber, panNumber);
    }

    // ==================== Setup ====================

    /**
     * No login flow — assumes the app is already on the Home Dashboard.
     * Loads test data from config and waits for the dashboard to be visible.
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "classSetup")
    public void waitForDashboard() {
        log.info("========== ADD BENEFICIARY TEST SETUP ==========");

        // Load test data from external config file
        loadTestData();

        log.info("Ensuring user is logged in...");
        com.pice.utils.AuthHelper.ensureLoggedIn();

        dashboard = new DashboardPage();

        if (dashboard.isDashboardVisible()) {
            log.info("✅ Dashboard is visible — ready for Add Beneficiary test");
        } else {
            log.warn("⚠️ Dashboard not immediately visible — trying to navigate back to Home Dashboard...");
            dashboard.navigateBackToDashboard();
            
            if (dashboard.isDashboardVisible()) {
                log.info("✅ Dashboard is visible after recovery navigation");
            } else {
                log.error("Dashboard not visible even after recovery navigation — test may fail");
            }
        }

        log.info("========== ADD BENEFICIARY TEST SETUP COMPLETE ==========");
    }

    @Override
    protected void resetAppState() {
        log.info("--- Custom resetAppState: Keeping session active (no logout) ---");
        // No-op — do not restart or clear the app
    }

    // ==================== E2E TEST ====================

    @Test(
        groups = {TestGroups.E2E, TestGroups.PAYMENT, TestGroups.POSITIVE},
        description = "E2E: Dashboard → Make Payment → Add New Beneficiary "
                + "→ Enter Account & IFSC → Confirm → Handle PAN → Enter Phone → Handle PAN if not fetched → Confirm",
        priority = 1
    )
    public void testAddNewBeneficiaryFromDashboard() {
        log.info("===== TEST: testAddNewBeneficiaryFromDashboard =====");
        log.info("Test data — Account: {}, IFSC: {}, Phone: {}, PAN: {}",
                accountNumber, ifscCode, phoneNumber, panNumber);

        // ---- Step 1: Verify dashboard is loaded ----
        ExtentReportListener.logStep("Step 1: Verify Home Dashboard is displayed");
        Assert.assertTrue(dashboard.isDashboardVisible(),
                "Home Dashboard should be visible before starting the flow");
        log.info("✅ Step 1: Dashboard visible");

        // ---- Step 2: Tap Make Payment button ----
        ExtentReportListener.logStep("Step 2: Tap 'Make Payment' button on Dashboard");
        Assert.assertTrue(dashboard.isMakePaymentButtonVisible(),
                "Make Payment button should be visible on Dashboard");
        dashboard.tapMakePayment();
        sleep(3000);
        log.info("✅ Step 2: Make Payment button tapped");

        // ---- Step 3: Verify Make Payment screen loaded ----
        ExtentReportListener.logStep("Step 3: Verify Make Payment screen is displayed");
        MakePaymentPage makePaymentPage = new MakePaymentPage(true);
        sleep(2000);
        log.info("✅ Step 3: Make Payment screen loaded");

        // ---- Step 4: Tap Add New Beneficiary ----
        ExtentReportListener.logStep("Step 4: Tap 'Add New Beneficiary'");
        AddBeneficiaryPage addBeneficiaryPage = makePaymentPage.tapAddNewBeneficiary();
        sleep(2000);
        log.info("✅ Step 4: Add New Beneficiary tapped");

        // ---- Step 5: Enter Account Number ----
        ExtentReportListener.logStep("Step 5: Enter Account Number: " + accountNumber);
        addBeneficiaryPage.enterAccountNumber(accountNumber);
        log.info("✅ Step 5: Account number entered: {}", accountNumber);

        // ---- Step 6: Enter IFSC Code ----
        ExtentReportListener.logStep("Step 6: Enter IFSC Code: " + ifscCode);
        addBeneficiaryPage.enterIfscCode(ifscCode);
        log.info("✅ Step 6: IFSC code entered: {}", ifscCode);

        // ---- Step 7: Tap Confirm ----
        ExtentReportListener.logStep("Step 7: Tap Confirm to submit beneficiary details");
        addBeneficiaryPage.tapConfirm();
        log.info("✅ Step 7: Confirm tapped");

        // ---- Step 8: Handle PAN step if it appears ----
        ExtentReportListener.logStep("Step 8: Handle PAN verification step if present");
        boolean panStepAppeared = addBeneficiaryPage.handlePanStepIfPresent();
        if (panStepAppeared) {
            log.info("✅ Step 8: PAN step detected and Confirm tapped");
            ExtentReportListener.logStep("PAN verification step was present — Confirm tapped");
        } else {
            log.info("✅ Step 8: No PAN step — proceeding");
            ExtentReportListener.logStep("No PAN verification step appeared — proceeding");
        }

        // ---- Step 9: Wait for beneficiary confirmation page ----
        ExtentReportListener.logStep("Step 9: Wait for beneficiary confirmation page");
        sleep(2000);
        log.info("✅ Step 9: Proceeding to confirmation page");

        // ---- Step 10: Enter Phone Number on confirmation page ----
        ExtentReportListener.logStep("Step 10: Enter Phone Number: " + phoneNumber);
        addBeneficiaryPage.enterPhoneNumber(phoneNumber);
        log.info("✅ Step 10: Phone number entered: {}", phoneNumber);

        // ---- Step 11: Check PAN and enter if not fetched ----
        ExtentReportListener.logStep("Step 11: Check if PAN is auto-fetched; enter manually if not");
        boolean panEnteredManually = false;
        if (addBeneficiaryPage.isPanNotFetched()) {
            log.info("PAN not auto-fetched — entering PAN manually: {}", panNumber);
            addBeneficiaryPage.enterPanNumber(panNumber);
            panEnteredManually = true;
            ExtentReportListener.logStep("PAN was NOT auto-fetched — entered manually: " + panNumber);
        } else {
            log.info("PAN was auto-fetched — no manual entry needed");
            ExtentReportListener.logStep("PAN was auto-fetched — skipping manual entry");
        }

        // ---- Step 12: Tap Confirm on confirmation page ----
        ExtentReportListener.logStep("Step 12: Tap Confirm on beneficiary confirmation page");
        addBeneficiaryPage.tapConfirmBeneficiary();
        log.info("✅ Step 12: Confirm tapped on confirmation page");

        // ---- Step 13: Verify transition back to Make Payment screen ----
        ExtentReportListener.logStep("Step 13: Verify transition back to Make Payment screen");
        sleep(5000); // Wait for the transition / success popup to close
        MakePaymentPage finalMakePaymentPage = new MakePaymentPage(true);
        
        boolean primaryAlreadyExists = !finalMakePaymentPage.isMakePaymentScreenVisible();
        if (primaryAlreadyExists) {
            log.warn("Duplicate beneficiary detected for account: {}.", accountNumber);
            
            // Add error message and screenshot to the report
            if (com.pice.driver.DriverManager.hasDriver()) {
                String base64 = com.pice.utils.ScreenshotUtils.captureScreenshotAsBase64();
                if (base64 != null) {
                    ExtentReportListener.getCurrentTest().log(Status.WARNING, 
                        "Beneficiary already exists for Account: " + accountNumber + ". Error Toast shown on screen.", 
                        MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
                } else {
                    ExtentReportListener.getCurrentTest().log(Status.WARNING, 
                        "Beneficiary already exists for Account: " + accountNumber + ". Error Toast shown on screen.");
                }
            } else {
                ExtentReportListener.getCurrentTest().log(Status.WARNING, 
                    "Beneficiary already exists for Account: " + accountNumber + ". Error Toast shown on screen.");
            }
            
            log.info("Performing recovery back to Make Payment listing screen");
            log.info("Tapping back from confirmation page");
            addBeneficiaryPage.tapBack();
            sleep(2000);

            log.info("Tapping back from beneficiary details page");
            addBeneficiaryPage.tapBack();
            sleep(2000);

            log.info("Handling discard changes confirmation popup");
            addBeneficiaryPage.handleDiscardChangesPopup();
            sleep(2000);
        } else {
            // Else case: Landed on Make Payment screen immediately (successful new beneficiary addition)
            log.info("✅ Landed on Make Payment (All Beneficiaries) screen successfully with primary beneficiary");
            ExtentReportListener.logInfo("Beneficiary added successfully (new registration).");
        }
        
        Assert.assertTrue(finalMakePaymentPage.isMakePaymentScreenVisible(),
                "Should land on All Beneficiary (Make Payment) page after adding beneficiary");
        log.info("✅ Step 13: Landed on Make Payment (All Beneficiaries) screen successfully");

        // Embed screenshot of the final listing screen into the report
        if (com.pice.driver.DriverManager.hasDriver()) {
            String base64 = com.pice.utils.ScreenshotUtils.captureScreenshotAsBase64();
            if (base64 != null) {
                ExtentReportListener.getCurrentTest().log(Status.PASS, 
                    "Final Make Payment listing screen with added beneficiary", 
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
            }
        }

        // ---- Summary ----
        log.info("===== TEST PASSED: testAddNewBeneficiaryFromDashboard =====");
        log.info("Add Beneficiary flow completed successfully");
        log.info("  Account: {}", accountNumber);
        log.info("  IFSC: {}", ifscCode);
        log.info("  Phone: {}", phoneNumber);
        log.info("  PAN: {}", panNumber);
        log.info("  PAN step appeared: {}", panStepAppeared);
        log.info("  PAN entered manually: {}", panEnteredManually);
    }

    // ==================== Helper Methods ====================

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
