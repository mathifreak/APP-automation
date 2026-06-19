package com.pice.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the Add New Beneficiary flow.
 *
 * <p>This screen is reached from: Dashboard → Make Payment → Add New Beneficiary.
 * It guides the user through entering bank account details:
 * <ul>
 *   <li>Account Number input field</li>
 *   <li>IFSC Code input field</li>
 *   <li>Confirm / Verify button</li>
 *   <li>PAN verification step (optional, may appear for certain accounts)</li>
 *   <li>Beneficiary name confirmation</li>
 * </ul>
 *
 * <p><b>Flow:</b> Dashboard → Make Payment → <b>Add New Beneficiary</b>
 * → Enter Account Number → Enter IFSC → Confirm → [PAN step] → Done
 */
public class AddBeneficiaryPage extends BasePage {

    // ==================== Locators ====================

    // --- Screen Identifiers ---
    private static final By SCREEN_TITLE = By.xpath(
            "//*[contains(@content-desc,'Add New Beneficiary') or contains(@content-desc,'Add Beneficiary') "
                    + "or contains(@text,'Add New Beneficiary') or contains(@text,'Add Beneficiary') "
                    + "or contains(@content-desc,'Beneficiary Details') or contains(@text,'Beneficiary Details')]"
    );

    // --- Account Number Field ---
    private static final By ACCOUNT_NUMBER_FIELD = By.xpath(
            "//*[contains(@content-desc,'Account Number') or contains(@content-desc,'account number') "
                    + "or contains(@text,'Account Number') or contains(@text,'account number')]"
                    + "/following-sibling::*[1]//android.widget.EditText "
                    + "| //android.widget.EditText[contains(@content-desc,'Account') or contains(@text,'Account')]"
    );

    // Broader fallback — first EditText on screen (account number is typically the first input)
    private static final By FIRST_EDIT_TEXT = By.xpath(
            "(//android.widget.EditText)[1]"
    );

    // Second EditText on screen (IFSC is typically the second input)
    private static final By SECOND_EDIT_TEXT = By.xpath(
            "(//android.widget.EditText)[2]"
    );

    // --- Account Number by accessibility or hint ---
    private static final By ACCOUNT_NUMBER_INPUT = By.xpath(
            "//android.widget.EditText[contains(@content-desc,'Account') "
                    + "or contains(@content-desc,'account') "
                    + "or contains(@text,'Account Number') "
                    + "or contains(@text,'Enter account')]"
    );

    // --- IFSC Code Field ---
    private static final By IFSC_CODE_INPUT = By.xpath(
            "//android.widget.EditText[contains(@content-desc,'IFSC') "
                    + "or contains(@content-desc,'ifsc') "
                    + "or contains(@text,'IFSC') "
                    + "or contains(@text,'Enter IFSC')]"
    );

    // --- Confirm / Verify / Continue Button ---
    private static final By CONFIRM_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Confirm') or contains(@content-desc,'Verify') "
                    + "or contains(@content-desc,'Continue') or contains(@content-desc,'Submit') "
                    + "or contains(@content-desc,'Next') or contains(@content-desc,'Proceed') "
                    + "or contains(@text,'Confirm') or contains(@text,'Verify') "
                    + "or contains(@text,'Continue') or contains(@text,'Submit') "
                    + "or contains(@text,'Next') or contains(@text,'Proceed')]"
    );

    // --- PAN Step Elements ---
    private static final By PAN_SCREEN_INDICATOR = By.xpath(
            "//*[contains(@content-desc,'PAN') or contains(@text,'PAN') "
                    + "or contains(@content-desc,'pan') or contains(@text,'pan card') "
                    + "or contains(@content-desc,'Permanent Account Number')]"
    );

    private static final By PAN_CONFIRM_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Confirm') or contains(@content-desc,'Submit') "
                    + "or contains(@content-desc,'Verify') or contains(@content-desc,'Continue') "
                    + "or contains(@content-desc,'Proceed') or contains(@content-desc,'Done') "
                    + "or contains(@text,'Confirm') or contains(@text,'Submit') "
                    + "or contains(@text,'Verify') or contains(@text,'Continue') "
                    + "or contains(@text,'Proceed') or contains(@text,'Done')]"
    );

    // --- PAN Input Field (when PAN is not auto-fetched) ---
    private static final By PAN_INPUT_FIELD = By.xpath(
            "//android.widget.EditText[contains(@content-desc,'PAN') "
                    + "or contains(@content-desc,'pan') "
                    + "or contains(@text,'PAN') "
                    + "or contains(@text,'Enter PAN') "
                    + "or contains(@text,'Enter your PAN')]"
    );

    // --- Verify with GST/PAN Button ---
    private static final By VERIFY_WITH_PAN_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Verify with their GST/PAN') or contains(@text,'Verify with their GST/PAN')]"
    );

    // --- Confirm Beneficiary Button (on confirmation page) ---
    private static final By CONFIRM_BENEFICIARY_BUTTON = By.xpath(
            "//*[@content-desc='Confirm Beneficiary']/parent::*[@clickable='true'] "
                    + "| //*[contains(@content-desc,'Confirm Beneficiary') or contains(@text,'Confirm Beneficiary')]"
    );

    // --- Phone Number / Mobile Number Field (on beneficiary confirmation page) ---
    private static final By PHONE_NUMBER_FIELD = By.xpath(
            "//android.widget.EditText[contains(@content-desc,'Phone') "
                    + "or contains(@content-desc,'phone') "
                    + "or contains(@content-desc,'Mobile') "
                    + "or contains(@content-desc,'mobile') "
                    + "or contains(@text,'Phone') "
                    + "or contains(@text,'phone') "
                    + "or contains(@text,'Mobile') "
                    + "or contains(@text,'mobile') "
                    + "or contains(@text,'Enter phone') "
                    + "or contains(@text,'Enter mobile')]"
    );

    // Broader label-based phone field fallback
    private static final By PHONE_LABEL_FIELD = By.xpath(
            "//*[contains(@content-desc,'Phone') or contains(@content-desc,'Mobile') "
                    + "or contains(@text,'Phone') or contains(@text,'Mobile')]"
                    + "/following-sibling::*[1]//android.widget.EditText"
    );

    // --- Beneficiary Confirmation Page Indicator ---
    private static final By CONFIRMATION_PAGE_INDICATOR = By.xpath(
            "//*[contains(@content-desc,'Confirm Beneficiary') or contains(@text,'Confirm Beneficiary') "
                    + "or contains(@content-desc,'Beneficiary Confirmation') or contains(@text,'Beneficiary Confirmation') "
                    + "or contains(@content-desc,'Verify Beneficiary') or contains(@text,'Verify Beneficiary') "
                    + "or contains(@content-desc,'Confirm Details') or contains(@text,'Confirm Details')]"
    );

    // --- Success / Beneficiary Added Confirmation ---
    private static final By SUCCESS_INDICATOR = By.xpath(
            "//*[contains(@content-desc,'Success') or contains(@content-desc,'Beneficiary Added') "
                    + "or contains(@content-desc,'successfully') or contains(@text,'Success') "
                    + "or contains(@text,'Beneficiary Added') or contains(@text,'successfully')]"
    );

    // --- Back / Navigation ---
    private static final By BACK_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Back') or contains(@content-desc,'back') "
                    + "or contains(@content-desc,'Navigate up')]"
    );

    // --- Discard Changes Popup ---
    private static final By EXIT_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Exit') or contains(@text,'Exit')]"
    );

    // ==================== Page Load Validation ====================

    @Override
    protected By getPageLoadedLocator() {
        return null; // Use custom multi-strategy validation
    }

    @Override
    protected int getPageLoadTimeout() {
        return 10;
    }

    // ==================== Constructor ====================

    public AddBeneficiaryPage() {
        log.info("Waiting for Add Beneficiary screen to load...");
        waitForAddBeneficiaryScreen();
        log.info("AddBeneficiaryPage loaded successfully");
    }

    public AddBeneficiaryPage(boolean skipWait) {
        if (!skipWait) {
            waitForAddBeneficiaryScreen();
        }
    }

    // ==================== Custom Page Load ====================

    private void waitForAddBeneficiaryScreen() {
        int maxWait = getPageLoadTimeout();
        int waited = 0;

        while (waited < maxWait) {
            if (isAddBeneficiaryScreenVisible()) {
                return;
            }
            sleep(2000);
            waited += 2;
        }

        log.warn("Add Beneficiary screen not detected after {}s — proceeding cautiously", maxWait);
    }

    // ==================== State Detection ====================

    /**
     * Check if the Add Beneficiary screen is currently visible.
     */
    public boolean isAddBeneficiaryScreenVisible() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));
            if (!getDriver().findElements(SCREEN_TITLE).isEmpty()) {
                return true;
            }
            // Fallback: check if EditText fields are present (input form)
            if (!getDriver().findElements(FIRST_EDIT_TEXT).isEmpty()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(
                        com.pice.config.ConfigManager.getImplicitWait()));
            } catch (Exception ignored) {}
        }
    }

    // ==================== Actions ====================

    /**
     * Enter the bank account number.
     *
     * @param accountNumber the account number to enter (e.g., "40963433113")
     */
    public void enterAccountNumber(String accountNumber) {
        log.info("Entering account number: {}", accountNumber);

        WebElement field = findAccountNumberField();
        if (field != null) {
            field.click();
            sleep(500);
            field.clear();
            field.sendKeys(accountNumber);
            log.info("Account number entered successfully");
            hideKeyboard();
            sleep(1000);
        } else {
            log.error("Could not find account number input field");
            throw new com.pice.exceptions.ElementInteractionException(
                    "type", "Account Number field", new RuntimeException("Field not found"));
        }
    }

    /**
     * Enter the IFSC code.
     *
     * @param ifscCode the IFSC code to enter (e.g., "SBIN0061739")
     */
    public void enterIfscCode(String ifscCode) {
        log.info("Entering IFSC code: {}", ifscCode);

        WebElement field = findIfscField();
        if (field != null) {
            field.click();
            sleep(500);
            field.clear();
            field.sendKeys(ifscCode);
            log.info("IFSC code entered successfully");
            hideKeyboard();
            sleep(1000);
        } else {
            log.error("Could not find IFSC code input field");
            throw new com.pice.exceptions.ElementInteractionException(
                    "type", "IFSC Code field", new RuntimeException("Field not found"));
        }
    }

    /**
     * Enter both account number and IFSC code.
     *
     * @param accountNumber the bank account number
     * @param ifscCode      the IFSC code
     */
    public void enterBeneficiaryDetails(String accountNumber, String ifscCode) {
        enterAccountNumber(accountNumber);
        sleep(1000);
        enterIfscCode(ifscCode);
    }

    /**
     * Tap the Confirm/Verify/Continue button to proceed.
     */
    public void tapConfirm() {
        log.info("Tapping Confirm button");
        try {
            // Wait a moment for button to be enabled
            sleep(1000);
            tap(CONFIRM_BUTTON);
            sleep(2000);
        } catch (Exception e) {
            log.warn("Confirm button tap failed, trying scroll and retry: {}", e.getMessage());
            swipeUp();
            sleep(1500);
            tap(CONFIRM_BUTTON);
            sleep(2000);
        }
    }

    /**
     * Handle the PAN verification step if it appears.
     * If the PAN step is detected, tap the Confirm button on it.
     *
     * @return true if PAN step was detected and handled, false if not present
     */
    public boolean handlePanStepIfPresent() {
        log.info("Checking for PAN verification step...");
        sleep(3000); // Wait for potential PAN screen to appear

        if (isPanStepVisible()) {
            log.info("PAN step detected — tapping Confirm");
            tapPanConfirm();
            sleep(2000);
            return true;
        }

        log.info("No PAN step detected — proceeding");
        return false;
    }

    /**
     * Check if the PAN verification step is currently displayed.
     */
    public boolean isPanStepVisible() {
        try {
            return !getDriver().findElements(PAN_SCREEN_INDICATOR).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tap Confirm on the PAN verification step.
     */
    public void tapPanConfirm() {
        log.info("Tapping Confirm on PAN step");
        try {
            tap(PAN_CONFIRM_BUTTON);
        } catch (Exception e) {
            log.warn("PAN Confirm button tap failed: {}", e.getMessage());
            // Try scrolling to find the button
            swipeUp();
            sleep(1000);
            tap(PAN_CONFIRM_BUTTON);
        }
    }

    /**
     * Tap the "Confirm Beneficiary" button on the confirmation page.
     */
    public void tapConfirmBeneficiary() {
        log.info("Tapping Confirm Beneficiary button");
        try {
            sleep(1000);
            tap(CONFIRM_BENEFICIARY_BUTTON);
            sleep(2000);
        } catch (Exception e) {
            log.warn("Confirm Beneficiary button tap failed, trying scroll and retry: {}", e.getMessage());
            swipeUp();
            sleep(1500);
            tap(CONFIRM_BENEFICIARY_BUTTON);
            sleep(2000);
        }
    }

    // ==================== Confirmation Page Actions ====================

    /**
     * Enter phone number on the beneficiary confirmation page.
     *
     * @param phoneNumber the phone number to enter (e.g., "9962063736")
     */
    public void enterPhoneNumber(String phoneNumber) {
        log.info("Entering phone number: {}", phoneNumber);

        WebElement field = findPhoneNumberField();
        if (field != null) {
            field.click();
            sleep(500);
            field.clear();
            field.sendKeys(phoneNumber);
            log.info("Phone number entered successfully");
            hideKeyboard();
            sleep(1000);
        } else {
            log.error("Could not find phone number input field");
            throw new com.pice.exceptions.ElementInteractionException(
                    "type", "Phone Number field", new RuntimeException("Field not found"));
        }
    }

    /**
     * Enter PAN number on the beneficiary confirmation page.
     * Used when PAN is not auto-fetched.
     *
     * @param panNumber the PAN number to enter (e.g., "ABCDE1234F")
     */
    public void clickVerifyWithPanIfPresent() {
        try {
            List<WebElement> buttons = getDriver().findElements(VERIFY_WITH_PAN_BUTTON);
            if (!buttons.isEmpty()) {
                log.info("Found 'Verify with their GST/PAN' button — clicking it to switch input mode");
                buttons.get(0).click();
                sleep(2000); // Wait for the screen transition to GST/PAN input mode
            } else {
                log.info("'Verify with their GST/PAN' button not visible — already in PAN/GST input mode or fetched");
            }
        } catch (Exception e) {
            log.warn("Failed to check/click 'Verify with their GST/PAN' button: {}", e.getMessage());
        }
    }

    public void enterPanNumber(String panNumber) {
        log.info("Entering PAN number: {}", panNumber);

        // Click "Verify with their GST/PAN" button if it is present to reveal the PAN input field
        clickVerifyWithPanIfPresent();

        WebElement field = findPanInputField();
        if (field != null) {
            field.click();
            sleep(500);
            field.clear();
            field.sendKeys(panNumber);
            log.info("PAN number entered successfully");
            pressAndroidEnter();
            hideKeyboard();
            sleep(1000);

            // Dismiss focus from the input field by clicking the header name
            try {
                List<WebElement> headers = getDriver().findElements(By.xpath("//*[contains(@text,'Mathivanan') or contains(@content-desc,'Mathivanan')]"));
                if (!headers.isEmpty()) {
                    headers.get(0).click();
                    sleep(1000);
                }
            } catch (Exception e) {
                log.debug("Header click fallback failed: {}", e.getMessage());
            }
        } else {
            log.error("Could not find PAN input field");
            throw new com.pice.exceptions.ElementInteractionException(
                    "type", "PAN Number field", new RuntimeException("Field not found"));
        }
    }

    /**
     * Check if the PAN input field is present and empty (not auto-fetched).
     *
     * @return true if PAN input field exists and has no pre-filled value
     */
    public boolean isPanNotFetched() {
        log.info("Checking if PAN is auto-fetched...");
        try {
            // If the 'Verify with their GST/PAN' button is visible, it means the PAN is NOT auto-fetched
            // (since the user still has the option to switch to GST/PAN verification mode).
            List<WebElement> verifyPanButtons = getDriver().findElements(VERIFY_WITH_PAN_BUTTON);
            if (!verifyPanButtons.isEmpty()) {
                log.info("'Verify with their GST/PAN' button is visible — PAN is NOT auto-fetched");
                return true;
            }

            // Otherwise, check the PAN input field itself if it is visible
            WebElement panField = findPanInputField();
            if (panField != null) {
                String currentValue = panField.getText();
                boolean isEmpty = currentValue == null || currentValue.trim().isEmpty()
                        || currentValue.contains("Enter") || currentValue.contains("PAN");
                log.info("PAN field value: '{}', isEmpty: {}", currentValue, isEmpty);
                return isEmpty;
            }
            // If neither the button nor the field is found, let's assume it was fetched
            log.info("Neither 'Verify with their GST/PAN' button nor PAN input field found — assuming auto-fetched");
            return false;
        } catch (Exception e) {
            log.warn("Error checking PAN field: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Check if the beneficiary confirmation page is visible.
     */
    public boolean isConfirmationPageVisible() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));
            
            // Check 1: CONFIRM_BENEFICIARY_BUTTON (button is present)
            if (!getDriver().findElements(CONFIRM_BENEFICIARY_BUTTON).isEmpty()) {
                return true;
            }
            // Check 2: Confirmation page text indicators
            if (!getDriver().findElements(CONFIRMATION_PAGE_INDICATOR).isEmpty()) {
                return true;
            }
            // Fallback: check if phone number field is present (strong indicator of confirmation page)
            if (findPhoneNumberField() != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(
                        com.pice.config.ConfigManager.getImplicitWait()));
            } catch (Exception ignored) {}
        }
    }

    /**
     * Wait for the beneficiary confirmation page to appear.
     *
     * @param timeoutSeconds max time to wait
     * @return true if the confirmation page became visible
     */
    public boolean waitForConfirmationPage(int timeoutSeconds) {
        log.info("Waiting up to {}s for beneficiary confirmation page...", timeoutSeconds);
        int waited = 0;
        while (waited < timeoutSeconds) {
            if (isConfirmationPageVisible()) {
                log.info("Beneficiary confirmation page detected after {}s", waited);
                return true;
            }
            sleep(1000);
            waited++;
        }
        log.warn("Beneficiary confirmation page not detected after {}s", timeoutSeconds);
        return false;
    }

    /**
     * Handle the beneficiary confirmation page:
     * 1. Enter phone number
     * 2. If PAN is not auto-fetched, enter PAN number manually
     * 3. Tap Confirm
     *
     * @param phoneNumber the phone number to enter
     * @param panNumber   the PAN to enter if not auto-fetched
     * @return true if PAN was entered manually, false if PAN was auto-fetched
     */
    public boolean handleConfirmationPage(String phoneNumber, String panNumber) {
        log.info("Handling beneficiary confirmation page...");
        boolean panEnteredManually = false;

        // Step 1: Enter phone number
        enterPhoneNumber(phoneNumber);
        sleep(1000);

        // Step 2: Check if PAN is auto-fetched; if not, enter PAN manually
        if (isPanNotFetched()) {
            log.info("PAN not auto-fetched — entering PAN manually: {}", panNumber);
            enterPanNumber(panNumber);
            panEnteredManually = true;
        } else {
            log.info("PAN was auto-fetched — no manual entry needed");
        }

        // Step 3: Tap Confirm on the confirmation page
        tapConfirmBeneficiary();

        log.info("Beneficiary confirmation page handled. PAN entered manually: {}", panEnteredManually);
        return panEnteredManually;
    }

    /**
     * Complete the full Add Beneficiary flow:
     * 1. Enter account number
     * 2. Enter IFSC code
     * 3. Tap Confirm
     * 4. Handle PAN step if it appears
     *
     * @param accountNumber the bank account number
     * @param ifscCode      the IFSC code
     */
    public void addBeneficiary(String accountNumber, String ifscCode) {
        log.info("Starting full Add Beneficiary flow");

        // Step 1 & 2: Enter details
        enterBeneficiaryDetails(accountNumber, ifscCode);

        // Step 3: Confirm
        tapConfirm();

        // Step 4: Handle PAN step
        handlePanStepIfPresent();

        log.info("Add Beneficiary flow completed");
    }

    /**
     * Complete the full Add Beneficiary flow with phone number and PAN:
     * 1. Enter account number
     * 2. Enter IFSC code
     * 3. Tap Confirm
     * 4. Handle PAN step if it appears
     * 5. On confirmation page: enter phone, enter PAN if not fetched, confirm
     *
     * @param accountNumber the bank account number
     * @param ifscCode      the IFSC code
     * @param phoneNumber   the phone number for confirmation page
     * @param panNumber     the PAN number if not auto-fetched
     */
    public void addBeneficiaryFull(String accountNumber, String ifscCode,
                                   String phoneNumber, String panNumber) {
        log.info("Starting full Add Beneficiary flow (with phone + PAN)");

        // Step 1 & 2: Enter bank details
        enterBeneficiaryDetails(accountNumber, ifscCode);

        // Step 3: Confirm
        tapConfirm();

        // Step 4: Handle PAN step
        handlePanStepIfPresent();

        // Step 5: Handle confirmation page
        handleConfirmationPage(phoneNumber, panNumber);

        log.info("Full Add Beneficiary flow completed");
    }

    /**
     * Tap back to return to the previous screen.
     */
    public void tapBack() {
        log.info("Tapping back on Add Beneficiary screen");
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));
            List<WebElement> elements = getDriver().findElements(BACK_BUTTON);
            if (!elements.isEmpty()) {
                elements.get(0).click();
            } else {
                log.info("Back button locator not found, pressing Android back directly");
                pressAndroidBack();
            }
        } catch (Exception e) {
            log.warn("Back navigation failed: {}", e.getMessage());
            pressAndroidBack();
        } finally {
            try {
                getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(
                        com.pice.config.ConfigManager.getImplicitWait()));
            } catch (Exception ignored) {}
        }
    }

    /**
     * Handle Discard Changes popup if it appears when navigating back.
     */
    public void handleDiscardChangesPopup() {
        try {
            sleep(1000);
            List<WebElement> exitButtons = getDriver().findElements(EXIT_BUTTON);
            if (!exitButtons.isEmpty()) {
                log.info("Discard changes popup detected — tapping Exit");
                exitButtons.get(0).click();
                sleep(2000);
            } else {
                log.info("Discard changes popup not visible");
            }
        } catch (Exception e) {
            log.warn("Failed to handle discard changes popup: {}", e.getMessage());
        }
    }

    // ==================== Verification Methods ====================

    /**
     * Check if the success indicator is visible (beneficiary added successfully).
     */
    public boolean isSuccessVisible() {
        try {
            return !getDriver().findElements(SUCCESS_INDICATOR).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the Confirm button is visible.
     */
    public boolean isConfirmButtonVisible() {
        try {
            return !getDriver().findElements(CONFIRM_BUTTON).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Private Helpers ====================

    /**
     * Find the account number input field using multiple strategies.
     */
    private WebElement findAccountNumberField() {
        // Strategy 1: By accessibility / content-desc containing "Account"
        try {
            List<WebElement> elements = getDriver().findElements(ACCOUNT_NUMBER_INPUT);
            if (!elements.isEmpty()) {
                log.debug("Found account number field via ACCOUNT_NUMBER_INPUT locator");
                return elements.get(0);
            }
        } catch (Exception e) {
            log.debug("ACCOUNT_NUMBER_INPUT locator failed: {}", e.getMessage());
        }

        // Strategy 2: By the labeled field (content-desc / text containing "Account Number")
        try {
            List<WebElement> elements = getDriver().findElements(ACCOUNT_NUMBER_FIELD);
            if (!elements.isEmpty()) {
                log.debug("Found account number field via ACCOUNT_NUMBER_FIELD locator");
                return elements.get(0);
            }
        } catch (Exception e) {
            log.debug("ACCOUNT_NUMBER_FIELD locator failed: {}", e.getMessage());
        }

        // Strategy 3: First EditText on screen (most common layout pattern)
        try {
            List<WebElement> elements = getDriver().findElements(FIRST_EDIT_TEXT);
            if (!elements.isEmpty()) {
                log.debug("Found account number field via FIRST_EDIT_TEXT fallback");
                return elements.get(0);
            }
        } catch (Exception e) {
            log.debug("FIRST_EDIT_TEXT fallback failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Find the IFSC code input field using multiple strategies.
     */
    private WebElement findIfscField() {
        // Strategy 1: By accessibility / content-desc containing "IFSC"
        try {
            List<WebElement> elements = getDriver().findElements(IFSC_CODE_INPUT);
            if (!elements.isEmpty()) {
                log.debug("Found IFSC field via IFSC_CODE_INPUT locator");
                return elements.get(0);
            }
        } catch (Exception e) {
            log.debug("IFSC_CODE_INPUT locator failed: {}", e.getMessage());
        }

        // Strategy 2: Second EditText on screen (IFSC is typically the second input)
        try {
            List<WebElement> elements = getDriver().findElements(SECOND_EDIT_TEXT);
            if (!elements.isEmpty()) {
                log.debug("Found IFSC field via SECOND_EDIT_TEXT fallback");
                return elements.get(0);
            }
        } catch (Exception e) {
            log.debug("SECOND_EDIT_TEXT fallback failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Find the phone number input field using multiple strategies.
     */
    private WebElement findPhoneNumberField() {
        // Strategy 1: By accessibility / content-desc containing "Phone" or "Mobile"
        try {
            List<WebElement> elements = getDriver().findElements(PHONE_NUMBER_FIELD);
            if (!elements.isEmpty()) {
                log.debug("Found phone number field via PHONE_NUMBER_FIELD locator");
                return elements.get(0);
            }
        } catch (Exception e) {
            log.debug("PHONE_NUMBER_FIELD locator failed: {}", e.getMessage());
        }

        // Strategy 2: By label-based field (Phone/Mobile label + following EditText)
        try {
            List<WebElement> elements = getDriver().findElements(PHONE_LABEL_FIELD);
            if (!elements.isEmpty()) {
                log.debug("Found phone number field via PHONE_LABEL_FIELD locator");
                return elements.get(0);
            }
        } catch (Exception e) {
            log.debug("PHONE_LABEL_FIELD locator failed: {}", e.getMessage());
        }

        // Strategy 3: Try finding EditText fields and pick the one likely for phone
        try {
            List<WebElement> editTexts = getDriver().findElements(By.xpath("//android.widget.EditText"));
            for (WebElement et : editTexts) {
                String desc = et.getAttribute("content-desc");
                String text = et.getText();
                if ((desc != null && (desc.toLowerCase().contains("phone") || desc.toLowerCase().contains("mobile")))
                        || (text != null && (text.toLowerCase().contains("phone") || text.toLowerCase().contains("mobile")))) {
                    log.debug("Found phone number field via EditText scan");
                    return et;
                }
            }
            // If only one EditText is present on the page (likely the phone field on confirmation)
            if (editTexts.size() == 1) {
                log.debug("Found single EditText — assuming it is the phone number field");
                return editTexts.get(0);
            }
        } catch (Exception e) {
            log.debug("EditText scan for phone field failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Find the PAN input field on the confirmation page.
     */
    private WebElement findPanInputField() {
        // Strategy 1: By accessibility / content-desc containing "PAN"
        try {
            List<WebElement> elements = getDriver().findElements(PAN_INPUT_FIELD);
            if (!elements.isEmpty()) {
                log.debug("Found PAN input field via PAN_INPUT_FIELD locator");
                return elements.get(0);
            }
        } catch (Exception e) {
            log.debug("PAN_INPUT_FIELD locator failed: {}", e.getMessage());
        }

        // Strategy 2: Scan all EditText fields for PAN-related hints
        try {
            List<WebElement> editTexts = getDriver().findElements(By.xpath("//android.widget.EditText"));
            for (WebElement et : editTexts) {
                String desc = et.getAttribute("content-desc");
                String text = et.getText();
                if ((desc != null && desc.toLowerCase().contains("pan"))
                        || (text != null && text.toLowerCase().contains("pan"))) {
                    log.debug("Found PAN input field via EditText scan");
                    return et;
                }
            }
        } catch (Exception e) {
            log.debug("EditText scan for PAN field failed: {}", e.getMessage());
        }

        // Strategy 3: If we are on the confirmation screen and there's only one EditText
        try {
            List<WebElement> editTexts = getDriver().findElements(By.xpath("//android.widget.EditText"));
            if (editTexts.size() == 1) {
                log.debug("Found single EditText — assuming it is the PAN/GST input field");
                return editTexts.get(0);
            }
        } catch (Exception e) {
            log.debug("Fallback to single EditText failed: {}", e.getMessage());
        }

        return null;
    }

    private void pressAndroidBack() {
        try {
            AppiumDriver driver = getDriver();
            if (driver instanceof AndroidDriver androidDriver) {
                androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
            }
        } catch (Exception e) {
            log.warn("Android back press failed: {}", e.getMessage());
        }
    }

    private void pressAndroidEnter() {
        try {
            AppiumDriver driver = getDriver();
            if (driver instanceof AndroidDriver androidDriver) {
                androidDriver.pressKey(new KeyEvent(AndroidKey.ENTER));
            }
        } catch (Exception e) {
            log.warn("Android enter press failed: {}", e.getMessage());
        }
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
