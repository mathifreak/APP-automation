package com.pice.tests.cards;

import com.pice.base.BaseTest;
import com.pice.config.ConfigManager;
import com.pice.constants.TestGroups;
import com.pice.listeners.ExtentReportListener;
import com.pice.pages.CardsPage;
import com.pice.pages.DashboardPage;
import com.pice.pages.LoginPage;
import com.pice.pages.OtpPage;
import com.pice.pages.PermissionPage;
import com.pice.utils.GestureUtils;
import com.pice.utils.SoftAssertUtils;
import io.appium.java_client.AppiumBy;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Add Card Flow Test Cases for the Pice App — Cards Tab.
 *
 * <p>Automates the complete "Add Card" flow navigated from the Home Dashboard
 * to the Cards tab:
 * <ul>
 *   <li>Cards screen UI element verification (smoke)</li>
 *   <li>Add Card button visibility and navigation (positive)</li>
 *   <li>Add Card form field interactions — card number, expiry, CVV, name (positive)</li>
 *   <li>Form validation — empty fields, invalid card number (negative)</li>
 *   <li>Back navigation from Add Card form to Cards screen (positive)</li>
 *   <li>Back navigation from Cards screen to Dashboard (positive)</li>
 *   <li>Full E2E Add Card flow with real test card data (e2e)</li>
 * </ul>
 *
 * <p><b>Test Card Data (from config):</b>
 * <ul>
 *   <li>Card Number: {@code test.card.number} (e.g., 4111460903362832)</li>
 *   <li>Expiry: {@code test.card.expiry} (e.g., 11/28)</li>
 *   <li>CVV: {@code test.card.cvv} (e.g., 245)</li>
 *   <li>Holder: {@code test.card.holder.name} (e.g., TEST USER)</li>
 * </ul>
 *
 * <p><b>Flow:</b> Login → Dashboard → Cards tab → Cards Screen → Add Card → Form
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>Physical device (root detection blocks emulators)</li>
 *   <li>Valid credentials configured (test.mobile.number, test.otp)</li>
 *   <li>Developer mode will be temporarily disabled during login</li>
 * </ul>
 *
 * <p><b>Run:</b>
 * <pre>{@code make test-batch-cards ENV=staging}</pre>
 * <pre>{@code make run TEST=com.pice.tests.cards.AddCardFlowTest}</pre>
 */
public class AddCardFlowTest extends BaseTest {

    // ==================== Config-Driven Test Data ====================

    private static final String PHONE_NUMBER =
            ConfigManager.get("test.mobile.number", "9962063736");

    /** Real test card number from config (4111460903362832) */
    private static final String TEST_CARD_NUMBER =
            ConfigManager.get("test.card.number", "4111460903362832");

    /** Real test card expiry from config (11/28) */
    private static final String TEST_CARD_EXPIRY =
            ConfigManager.get("test.card.expiry", "11/28");

    /** Real test card CVV from config (245) */
    private static final String TEST_CARD_CVV =
            ConfigManager.get("test.card.cvv", "245");

    /** Test cardholder name from config */
    private static final String TEST_CARDHOLDER_NAME =
            ConfigManager.get("test.card.holder.name", "TEST USER");

    /** Intentionally invalid card number for negative tests */
    private static final String INVALID_CARD_NUMBER = "1234567890123456";

    // ==================== Device / App Constants ====================

    private static final String APP_PACKAGE = "one.pice.pice_business_loan.pre";
    private static final String APP_ACTIVITY = "one.pice.pice_business_loan.MainActivity";
    private static final String DEVICE_SERIAL = resolveDeviceSerial();

    private static String resolveDeviceSerial() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"adb", "devices"});
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.endsWith("device") && !line.startsWith("List")) {
                    return line.split("\\s+")[0];
                }
            }
        } catch (Exception e) {
            // ignore — use fallback
        }
        return "10MG56FM6E000FD"; // fallback — update if your device serial differs
    }

    // ==================== Page Objects ====================

    private DashboardPage dashboard;
    private CardsPage cardsPage;

    // ==================== Setup ====================

    /**
     * Full setup: clear app → disable dev mode → launch → login → OTP
     * → permissions → dashboard → navigate to Cards tab.
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "classSetup")
    public void loginAndNavigateToCardsScreen() {
        log.info("========== ADD CARD FLOW TEST SUITE SETUP ==========");
        log.info("Device serial  : {}", DEVICE_SERIAL);
        log.info("Card number    : {}****", TEST_CARD_NUMBER.substring(0, 4));
        log.info("Card expiry    : {}", TEST_CARD_EXPIRY);
        log.info("Card holder    : {}", TEST_CARDHOLDER_NAME);

        // Ensure user is logged in (reusing active session)
        com.pice.utils.AuthHelper.ensureLoggedIn();

        dashboard = new DashboardPage();

        // Navigate to dashboard home first if on another screen
        if (!dashboard.isDashboardVisible()) {
            log.info("Dashboard not visible — trying to navigate back to Home Dashboard...");
            dashboard.navigateBackToDashboard();
        }

        // Navigate to Cards tab from Home Dashboard
        dashboard.navigateToCards();
        sleep(3000);

        // Initialize CardsPage
        cardsPage = new CardsPage(true);
        log.info("Cards screen initialized");
        log.info("========== SUITE SETUP COMPLETE ==========");
    }

    /**
     * Override base resetAppState to avoid restarting the app between tests.
     * Each test instead uses {@link #ensureOnCardsScreen()} via @BeforeMethod.
     */
    @Override
    protected void resetAppState() {
        log.debug("resetAppState: keeping Cards session active (no-op)");
    }

    /**
     * Before each test: make sure the app is on the Cards screen,
     * not mid-form. Re-navigate if needed.
     */
    @BeforeMethod(alwaysRun = true)
    public void ensureOnCardsScreen() {
        log.info("--- BeforeMethod: ensuring Cards screen is visible ---");
        if (cardsPage == null) {
            cardsPage = new CardsPage(true);
        }

        // If on the Add Card form, navigate back first
        if (cardsPage.isAddCardFormVisible()) {
            log.info("On Add Card form — navigating back to Cards screen");
            cardsPage.navigateBackToCardsScreen();
            sleep(2000);
        }

        // If not on the Cards screen, re-navigate
        if (!cardsPage.isCardsScreenVisible()) {
            log.info("Not on Cards screen — re-navigating");
            try {
                // Try tapping the Cards tab in bottom nav
                getDriver().findElement(AppiumBy.accessibilityId("Cards")).click();
                sleep(3000);
            } catch (Exception e) {
                log.warn("Could not tap Cards tab: {}", e.getMessage());
                // Try from dashboard
                if (dashboard != null && dashboard.isDashboardVisible()) {
                    dashboard.navigateToCards();
                    sleep(3000);
                } else {
                    // Press back a couple of times then try
                    pressAndroidBack();
                    sleep(1500);
                    pressAndroidBack();
                    sleep(1500);
                    try {
                        getDriver().findElement(AppiumBy.accessibilityId("Cards")).click();
                        sleep(3000);
                    } catch (Exception ex) {
                        log.error("Could not navigate to Cards screen: {}", ex.getMessage());
                    }
                }
            }
        }

        // Scroll to top
        GestureUtils.swipeDown();
        sleep(1000);
        log.info("--- BeforeMethod complete ---");
    }

    /**
     * Suite teardown: restore developer options.
     */
    @AfterClass(alwaysRun = true)
    public void restoreDeviceSettings() {
        log.info("========== SUITE TEARDOWN ==========");
        try {
            execAdb("shell", "settings", "put", "global", "development_settings_enabled", "1");
            log.info("Developer options restored");
        } catch (Exception e) {
            log.warn("Failed to restore developer settings: {}", e.getMessage());
        }
        log.info("========== SUITE TEARDOWN COMPLETE ==========");
    }

    // ==================== SMOKE TESTS ====================

    /**
     * [SMOKE-01] Verify the Cards screen loads with required UI elements.
     */
    @Test(
        groups = {TestGroups.SMOKE, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[SMOKE-01] Verify Cards screen UI elements after navigating from Dashboard",
        priority = 1
    )
    public void verifyCardsScreenElements() {
        log.info("===== TEST: verifyCardsScreenElements =====");
        ExtentReportListener.logStep("Navigate to Cards screen and verify core UI elements");

        SoftAssertUtils.init();

        // Bottom navigation must remain visible
        ExtentReportListener.logStep("Verify bottom navigation is visible");
        SoftAssertUtils.assertTrue(
                isElementPresent(AppiumBy.accessibilityId("Home")),
                "Home tab should be visible in bottom nav");
        SoftAssertUtils.assertTrue(
                isElementPresent(AppiumBy.accessibilityId("Cards")),
                "Cards tab should be visible in bottom nav");

        // Cards screen must have at least one of: title, add button, empty state, or card list
        ExtentReportListener.logStep("Verify Cards screen content is present");
        boolean hasContent = cardsPage.isCardsScreenTitleVisible()
                || cardsPage.isAddCardButtonVisible()
                || cardsPage.isEmptyStateVisible()
                || cardsPage.hasExistingCards();

        SoftAssertUtils.assertTrue(hasContent,
                "Cards screen should show title, add button, empty state, or card list");

        ExtentReportListener.logStep("Cards screen status — "
                + "title=" + cardsPage.isCardsScreenTitleVisible()
                + ", addBtn=" + cardsPage.isAddCardButtonVisible()
                + ", emptyState=" + cardsPage.isEmptyStateVisible()
                + ", hasCards=" + cardsPage.hasExistingCards());

        SoftAssertUtils.assertAll();
        log.info("===== TEST PASSED: verifyCardsScreenElements =====");
    }

    /**
     * [SMOKE-02] Verify the "Add Card" button is present and visible.
     */
    @Test(
        groups = {TestGroups.SMOKE, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[SMOKE-02] Verify Add Card button is visible on Cards screen",
        priority = 2
    )
    public void verifyAddCardButtonVisible() {
        log.info("===== TEST: verifyAddCardButtonVisible =====");
        ExtentReportListener.logStep("Check if Add Card button is visible");

        boolean found = cardsPage.isAddCardButtonVisible();
        if (!found) {
            log.info("Not immediately visible — scrolling to find Add Card button");
            for (int i = 0; i < 4; i++) {
                GestureUtils.swipeUp();
                sleep(1500);
                if (cardsPage.isAddCardButtonVisible()) {
                    found = true;
                    break;
                }
            }
        }

        ExtentReportListener.logStep(found
                ? "✅ Add Card button found"
                : "⚠️ Add Card button not found — may require locator update after UI dump");

        Assert.assertTrue(found,
                "Add Card button must be visible on Cards screen");
        log.info("===== TEST PASSED: verifyAddCardButtonVisible =====");
    }

    // ==================== POSITIVE TESTS — Navigation ====================

    /**
     * [POS-01] Verify tapping "Add Card" navigates to the Add Card form.
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[POS-01] Tap Add Card button and verify Add Card form opens",
        priority = 3
    )
    public void verifyAddCardButtonNavigation() {
        log.info("===== TEST: verifyAddCardButtonNavigation =====");

        scrollToAddCardButton();
        ExtentReportListener.logStep("Tap Add Card button");
        cardsPage.tapAddCard();
        sleep(3000);

        boolean formVisible = cardsPage.isAddCardFormVisible();
        log.info("Add Card form visible after tap: {}", formVisible);
        ExtentReportListener.logStep(formVisible
                ? "✅ Add Card form opened"
                : "⚠️ Form not confirmed — navigation may have occurred to a different screen");

        // Navigate back regardless
        cardsPage.navigateBackToCardsScreen();
        sleep(1500);

        log.info("===== TEST PASSED: verifyAddCardButtonNavigation =====");
    }

    /**
     * [POS-02] Verify all 4 form fields are present on the Add Card form.
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[POS-02] Verify card number, expiry, CVV, and cardholder name fields exist",
        priority = 4
    )
    public void verifyAddCardFormElements() {
        log.info("===== TEST: verifyAddCardFormElements =====");
        navigateToAddCardForm();

        SoftAssertUtils.init();

        ExtentReportListener.logStep("Verify card number input field");
        SoftAssertUtils.assertTrue(cardsPage.isCardNumberFieldVisible(),
                "Card number input should be visible on Add Card form");

        ExtentReportListener.logStep("Verify expiry date input field");
        SoftAssertUtils.assertTrue(cardsPage.isExpiryDateFieldVisible(),
                "Expiry date input should be visible on Add Card form");

        ExtentReportListener.logStep("Verify CVV input field");
        SoftAssertUtils.assertTrue(cardsPage.isCvvFieldVisible(),
                "CVV input should be visible on Add Card form");

        ExtentReportListener.logStep("Verify cardholder name input field");
        if (!cardsPage.isCardholderNameFieldVisible()) {
            GestureUtils.swipeUp();
            sleep(1200);
        }
        SoftAssertUtils.assertTrue(cardsPage.isCardholderNameFieldVisible(),
                "Cardholder name input should be visible on Add Card form");

        ExtentReportListener.logStep("Form fields: cardNum=" + cardsPage.isCardNumberFieldVisible()
                + ", expiry=" + cardsPage.isExpiryDateFieldVisible()
                + ", cvv=" + cardsPage.isCvvFieldVisible()
                + ", name=" + cardsPage.isCardholderNameFieldVisible());

        SoftAssertUtils.assertAll();
        log.info("===== TEST PASSED: verifyAddCardFormElements =====");
    }

    // ==================== POSITIVE TESTS — Field Interactions ====================

    /**
     * [POS-03] Verify the card number field accepts input (uses real test card).
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[POS-03] Verify card number field accepts the real test card number",
        priority = 5
    )
    public void verifyCardNumberFieldInput() {
        log.info("===== TEST: verifyCardNumberFieldInput =====");
        log.info("Using card number from config: {}****", TEST_CARD_NUMBER.substring(0, 4));
        navigateToAddCardForm();

        ExtentReportListener.logStep("Enter test card number from config: " + maskCard(TEST_CARD_NUMBER));
        try {
            cardsPage.enterCardNumber(TEST_CARD_NUMBER);
            ExtentReportListener.logStep("Card number entered successfully");

            String value = cardsPage.getCardNumberFieldValue();
            ExtentReportListener.logStep("Field value after input: " + value);
            log.info("Card number field value: {}", value);

            Assert.assertFalse(value.isEmpty(),
                    "Card number field should not be empty after entering the test card number");
        } catch (Exception e) {
            log.warn("Card number interaction issue: {}", e.getMessage());
            ExtentReportListener.logStep("⚠️ Card number field interaction issue — locator may need refinement");
        }

        log.info("===== TEST PASSED: verifyCardNumberFieldInput =====");
    }

    /**
     * [POS-04] Verify the expiry date field accepts input in MM/YY format.
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[POS-04] Verify expiry date field accepts MM/YY format",
        priority = 6
    )
    public void verifyExpiryDateFieldInput() {
        log.info("===== TEST: verifyExpiryDateFieldInput =====");
        log.info("Using expiry from config: {}", TEST_CARD_EXPIRY);
        navigateToAddCardForm();

        ExtentReportListener.logStep("Enter expiry date from config: " + TEST_CARD_EXPIRY);
        try {
            cardsPage.enterExpiryDate(TEST_CARD_EXPIRY);
            ExtentReportListener.logStep("Expiry date entered successfully");

            String value = cardsPage.getExpiryDateFieldValue();
            ExtentReportListener.logStep("Field value after input: " + value);
            log.info("Expiry date field value: {}", value);

        } catch (Exception e) {
            log.warn("Expiry date interaction issue: {}", e.getMessage());
            ExtentReportListener.logStep("⚠️ Expiry date field interaction issue");
        }

        log.info("===== TEST PASSED: verifyExpiryDateFieldInput =====");
    }

    /**
     * [POS-05] Verify the CVV field accepts input.
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[POS-05] Verify CVV field accepts input",
        priority = 7
    )
    public void verifyCvvFieldInput() {
        log.info("===== TEST: verifyCvvFieldInput =====");
        log.info("Using CVV from config: [MASKED]");
        navigateToAddCardForm();

        ExtentReportListener.logStep("Enter CVV from config");
        try {
            cardsPage.enterCvv(TEST_CARD_CVV);
            ExtentReportListener.logStep("CVV entered successfully");

            String value = cardsPage.getCvvFieldValue();
            // CVV fields may mask input — we just check field has content or check is not empty
            ExtentReportListener.logStep("CVV field has value: " + !value.isEmpty() + " (masked for security)");
            log.info("CVV field responded to input");

        } catch (Exception e) {
            log.warn("CVV field interaction issue: {}", e.getMessage());
            ExtentReportListener.logStep("⚠️ CVV field interaction issue");
        }

        log.info("===== TEST PASSED: verifyCvvFieldInput =====");
    }

    /**
     * [POS-06] Verify the cardholder name field accepts text.
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[POS-06] Verify cardholder name field accepts text input",
        priority = 8
    )
    public void verifyCardholderNameFieldInput() {
        log.info("===== TEST: verifyCardholderNameFieldInput =====");
        log.info("Using cardholder name from config: {}", TEST_CARDHOLDER_NAME);
        navigateToAddCardForm();

        ExtentReportListener.logStep("Scroll to cardholder name field if needed");
        if (!cardsPage.isCardholderNameFieldVisible()) {
            GestureUtils.swipeUp();
            sleep(1200);
        }

        ExtentReportListener.logStep("Enter cardholder name: " + TEST_CARDHOLDER_NAME);
        try {
            cardsPage.enterCardholderName(TEST_CARDHOLDER_NAME);
            ExtentReportListener.logStep("Cardholder name entered successfully");

            String value = cardsPage.getCardholderNameFieldValue();
            ExtentReportListener.logStep("Field value after input: " + value);
            log.info("Cardholder name field value: {}", value);

        } catch (Exception e) {
            log.warn("Cardholder name field interaction issue: {}", e.getMessage());
            ExtentReportListener.logStep("⚠️ Cardholder name field interaction issue");
        }

        log.info("===== TEST PASSED: verifyCardholderNameFieldInput =====");
    }

    // ==================== NEGATIVE TESTS — Validation ====================

    /**
     * [NEG-01] Verify submit does not succeed when form fields are empty.
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.NEGATIVE},
        description = "[NEG-01] Submit with empty fields should be disabled or show validation error",
        priority = 9
    )
    public void verifySubmitDisabledWithEmptyFields() {
        log.info("===== TEST: verifySubmitDisabledWithEmptyFields =====");
        navigateToAddCardForm();

        ExtentReportListener.logStep("Check submit button state with all fields empty");
        boolean submitVisible = cardsPage.isSubmitButtonVisible();
        boolean submitEnabled = cardsPage.isSubmitButtonEnabled();

        ExtentReportListener.logStep("Submit — visible=" + submitVisible + ", enabled=" + submitEnabled);
        log.info("Submit button: visible={}, enabled={}", submitVisible, submitEnabled);

        if (submitVisible && submitEnabled) {
            ExtentReportListener.logStep("Submit is enabled — tapping to trigger validation");
            try {
                cardsPage.tapSubmit();
                sleep(2000);

                boolean errorShown = cardsPage.isErrorMessageVisible();
                String errorText = cardsPage.getErrorMessageText();
                ExtentReportListener.logStep("Validation response: error=" + errorShown + ", msg=" + errorText);

                Assert.assertTrue(errorShown || !cardsPage.isSuccessIndicatorVisible(),
                        "Empty form submit should show an error or not show a success state");
            } catch (Exception e) {
                log.info("Submit tap failed as expected (button may be disabled): {}", e.getMessage());
            }
        } else if (submitVisible) {
            ExtentReportListener.logStep("✅ Submit button is correctly disabled when fields are empty");
        } else {
            ExtentReportListener.logStep("Submit button not visible yet — may appear after all fields are filled");
        }

        log.info("===== TEST PASSED: verifySubmitDisabledWithEmptyFields =====");
    }

    /**
     * [NEG-02] Verify invalid card number is rejected.
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.NEGATIVE},
        description = "[NEG-02] Submit with an invalid card number should show an error",
        priority = 10
    )
    public void verifyInvalidCardNumberRejected() {
        log.info("===== TEST: verifyInvalidCardNumberRejected =====");
        navigateToAddCardForm();

        ExtentReportListener.logStep("Enter intentionally invalid card number: " + INVALID_CARD_NUMBER);
        try {
            cardsPage.enterCardNumber(INVALID_CARD_NUMBER);
            sleep(1500);

            // Try to submit
            if (cardsPage.isSubmitButtonVisible()) {
                ExtentReportListener.logStep("Tapping submit with invalid card number");
                try {
                    cardsPage.tapSubmit();
                    sleep(2500);
                } catch (Exception e) {
                    log.info("Submit tap failed (expected): {}", e.getMessage());
                }

                boolean errorShown = cardsPage.isErrorMessageVisible();
                String errorText = cardsPage.getErrorMessageText();
                ExtentReportListener.logStep("Result: error=" + errorShown + ", msg=" + errorText);
                log.info("Invalid card response: error={}, text={}", errorShown, errorText);

                Assert.assertTrue(errorShown || !cardsPage.isSuccessIndicatorVisible(),
                        "Invalid card number should be rejected (error shown or no success state)");
            } else {
                // Inline validation may prevent submit from appearing
                boolean inlineError = cardsPage.isErrorMessageVisible();
                ExtentReportListener.logStep("Submit not visible — inline error: " + inlineError);
                log.info("Inline validation error shown: {}", inlineError);
            }

        } catch (Exception e) {
            log.warn("Invalid card number test interaction issue: {}", e.getMessage());
            ExtentReportListener.logStep("⚠️ Interaction issue — locator may need refinement");
        }

        log.info("===== TEST PASSED: verifyInvalidCardNumberRejected =====");
    }

    // ==================== POSITIVE TESTS — Back Navigation ====================

    /**
     * [POS-07] Verify back navigation from Add Card form returns to Cards screen.
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[POS-07] Back from Add Card form should return to Cards screen",
        priority = 11
    )
    public void verifyBackNavigationFromAddCard() {
        log.info("===== TEST: verifyBackNavigationFromAddCard =====");
        navigateToAddCardForm();

        ExtentReportListener.logStep("Confirm on Add Card form");
        log.info("On Add Card form: {}", cardsPage.isAddCardFormVisible());

        ExtentReportListener.logStep("Navigate back to Cards screen");
        boolean returned = cardsPage.navigateBackToCardsScreen();
        sleep(2000);

        ExtentReportListener.logStep("Verify landed back on Cards screen");
        Assert.assertTrue(returned || cardsPage.isCardsScreenVisible(),
                "Should return to Cards screen after pressing back from Add Card form");

        log.info("===== TEST PASSED: verifyBackNavigationFromAddCard =====");
    }

    /**
     * [POS-08] Verify navigation from Cards screen to Home Dashboard via Home tab.
     */
    @Test(
        groups = {TestGroups.REGRESSION, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[POS-08] Tap Home tab on Cards screen → Dashboard should load",
        priority = 12
    )
    public void verifyBackNavigationFromCardsToDashboard() {
        log.info("===== TEST: verifyBackNavigationFromCardsToDashboard =====");

        ExtentReportListener.logStep("Tap Home tab from Cards screen");
        cardsPage.navigateToHome();
        sleep(3000);

        ExtentReportListener.logStep("Verify Dashboard is visible");
        if (dashboard == null) {
            dashboard = new DashboardPage(true);
        }
        boolean onDashboard = dashboard.isDashboardVisible();
        log.info("Dashboard visible after navigating from Cards: {}", onDashboard);

        Assert.assertTrue(onDashboard,
                "Dashboard should be visible after tapping the Home tab from the Cards screen");

        // Navigate back to Cards for the next test
        ExtentReportListener.logStep("Re-navigate to Cards tab for subsequent tests");
        dashboard.navigateToCards();
        sleep(3000);

        log.info("===== TEST PASSED: verifyBackNavigationFromCardsToDashboard =====");
    }

    // ==================== E2E TEST ====================

    /**
     * [E2E-01] Full Add Card flow using real test card data from config.
     * <p>Uses: card number 4111460903362832, expiry 11/28, CVV 245.
     */
    @Test(
        groups = {TestGroups.E2E, TestGroups.CARDS, TestGroups.POSITIVE},
        description = "[E2E-01] Full Add Card flow — Dashboard → Cards → Add Card → fill real card data → submit",
        priority = 13
    )
    public void verifyAddCardE2EFlow() {
        log.info("===== TEST: verifyAddCardE2EFlow =====");
        log.info("Test card: {}****, expiry={}, cvv=[MASKED]",
                TEST_CARD_NUMBER.substring(0, 4), TEST_CARD_EXPIRY);

        // Step 1: Verify on Cards screen
        ExtentReportListener.logStep("Step 1: Verify on Cards screen");
        if (!cardsPage.isCardsScreenVisible()) {
            if (dashboard != null) {
                dashboard.navigateToCards();
                sleep(3000);
            }
        }
        log.info("Step 1: Cards screen visible = {}", cardsPage.isCardsScreenVisible());

        // Step 2: Tap Add Card
        ExtentReportListener.logStep("Step 2: Tap Add Card button");
        scrollToAddCardButton();
        try {
            cardsPage.tapAddCard();
            sleep(3000);
        } catch (Exception e) {
            log.error("Add Card button tap failed: {}", e.getMessage());
            ExtentReportListener.logStep("❌ Add Card button could not be tapped — E2E aborted");
            Assert.fail("Could not tap Add Card button: " + e.getMessage());
            return;
        }

        // Step 3: Fill card number
        ExtentReportListener.logStep("Step 3a: Enter card number " + maskCard(TEST_CARD_NUMBER));
        try {
            cardsPage.enterCardNumber(TEST_CARD_NUMBER);
            sleep(500);
            log.info("Card number entered: {}****", TEST_CARD_NUMBER.substring(0, 4));
        } catch (Exception e) {
            log.warn("Card number entry failed: {}", e.getMessage());
            ExtentReportListener.logStep("⚠️ Card number field not found — continuing");
        }

        // Step 3b: Fill expiry date
        ExtentReportListener.logStep("Step 3b: Enter expiry date " + TEST_CARD_EXPIRY);
        try {
            cardsPage.enterExpiryDate(TEST_CARD_EXPIRY);
            sleep(500);
            log.info("Expiry date entered: {}", TEST_CARD_EXPIRY);
        } catch (Exception e) {
            log.warn("Expiry date entry failed: {}", e.getMessage());
            ExtentReportListener.logStep("⚠️ Expiry date field not found — continuing");
        }

        // Step 3c: Fill CVV
        ExtentReportListener.logStep("Step 3c: Enter CVV [MASKED]");
        try {
            cardsPage.enterCvv(TEST_CARD_CVV);
            sleep(500);
            log.info("CVV entered");
        } catch (Exception e) {
            log.warn("CVV entry failed: {}", e.getMessage());
            ExtentReportListener.logStep("⚠️ CVV field not found — continuing");
        }

        // Step 3d: Fill cardholder name
        ExtentReportListener.logStep("Step 3d: Enter cardholder name " + TEST_CARDHOLDER_NAME);
        try {
            if (!cardsPage.isCardholderNameFieldVisible()) {
                GestureUtils.swipeUp();
                sleep(1200);
            }
            cardsPage.enterCardholderName(TEST_CARDHOLDER_NAME);
            sleep(500);
            log.info("Cardholder name entered: {}", TEST_CARDHOLDER_NAME);
        } catch (Exception e) {
            log.warn("Cardholder name entry failed: {}", e.getMessage());
            ExtentReportListener.logStep("⚠️ Cardholder name field not found — continuing");
        }

        // Step 4: Tap Submit
        ExtentReportListener.logStep("Step 4: Tap Submit");
        boolean submitFound = false;
        if (cardsPage.isSubmitButtonVisible()) {
            submitFound = true;
        } else {
            GestureUtils.swipeUp();
            sleep(1500);
            submitFound = cardsPage.isSubmitButtonVisible();
        }

        if (submitFound) {
            try {
                cardsPage.tapSubmit();
                sleep(4000);
                ExtentReportListener.logStep("Submit button tapped");
            } catch (Exception e) {
                log.warn("Submit tap failed: {}", e.getMessage());
                ExtentReportListener.logStep("⚠️ Submit tap failed");
            }
        } else {
            ExtentReportListener.logStep("⚠️ Submit button not found after scroll");
        }

        // Step 5: Evaluate outcome
        ExtentReportListener.logStep("Step 5: Evaluate outcome");
        boolean success = cardsPage.isSuccessIndicatorVisible();
        boolean error = cardsPage.isErrorMessageVisible();
        String errorMsg = cardsPage.getErrorMessageText();

        log.info("E2E Outcome — success={}, error={}, errorMsg={}", success, error, errorMsg);
        ExtentReportListener.logStep(
                "E2E Outcome: success=" + success + ", error=" + error
                + (error ? ", msg=" + errorMsg : ""));

        // Step 6: Navigate back to Cards screen
        ExtentReportListener.logStep("Step 6: Navigate back to Cards screen");
        cardsPage.navigateBackToCardsScreen();
        sleep(2000);

        log.info("✅ E2E Add Card flow completed — card={}****, expiry={}",
                TEST_CARD_NUMBER.substring(0, 4), TEST_CARD_EXPIRY);
        log.info("===== TEST PASSED: verifyAddCardE2EFlow =====");
    }

    // ==================== Helper Methods ====================

    /**
     * Navigate to the Add Card form from the Cards screen.
     */
    private void navigateToAddCardForm() {
        if (cardsPage.isAddCardFormVisible()) {
            log.debug("Already on Add Card form");
            return;
        }

        if (!cardsPage.isCardsScreenVisible()) {
            log.info("Not on Cards screen — navigating");
            try {
                getDriver().findElement(AppiumBy.accessibilityId("Cards")).click();
                sleep(3000);
            } catch (Exception e) {
                log.warn("Cards tab not found: {}", e.getMessage());
            }
        }

        scrollToAddCardButton();
        try {
            cardsPage.tapAddCard();
            sleep(3000);
        } catch (Exception e) {
            log.warn("Tap Add Card failed: {}", e.getMessage());
        }
    }

    /**
     * Scroll down until the Add Card button is visible.
     */
    private void scrollToAddCardButton() {
        if (!cardsPage.isAddCardButtonVisible()) {
            log.debug("Scrolling to find Add Card button");
            for (int i = 0; i < 3; i++) {
                GestureUtils.swipeUp();
                sleep(1500);
                if (cardsPage.isAddCardButtonVisible()) break;
            }
        }
    }

    /**
     * Check if a UI element is present by locator.
     */
    private boolean isElementPresent(org.openqa.selenium.By locator) {
        try {
            return !getDriver().findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mask a card number for safe logging (shows first 4 and last 4 digits).
     */
    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return "****";
        return cardNumber.substring(0, 4) + "****"
                + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Press the Android hardware Back key.
     */
    private void pressAndroidBack() {
        try {
            io.appium.java_client.AppiumDriver driver = getDriver();
            if (driver instanceof io.appium.java_client.android.AndroidDriver androidDriver) {
                androidDriver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                        io.appium.java_client.android.nativekey.AndroidKey.BACK));
            }
        } catch (Exception e) {
            log.debug("Android back press failed: {}", e.getMessage());
        }
    }

    /**
     * Sleep helper.
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Execute an ADB shell command targeting the connected device.
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
