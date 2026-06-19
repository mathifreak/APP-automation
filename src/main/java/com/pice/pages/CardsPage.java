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
 * Page Object for the Pice Cards screen and Add Card flow.
 *
 * <p>This screen is reached from: Home Dashboard → Cards (bottom nav tab).
 * It displays the user's existing cards and provides the ability to add new cards.
 *
 * <p><b>Screen Layout (expected):</b>
 * <ul>
 *   <li>Header: "Cards" / "My Cards" title</li>
 *   <li>Existing cards list (if any) or empty state</li>
 *   <li>Add Card button / "+" FAB</li>
 *   <li>Bottom navigation bar (shared with Dashboard)</li>
 * </ul>
 *
 * <p><b>Add Card Form (expected):</b>
 * <ul>
 *   <li>Card number input (16 digits)</li>
 *   <li>Expiry date input (MM/YY)</li>
 *   <li>CVV input (3-4 digits)</li>
 *   <li>Cardholder name input</li>
 *   <li>Submit / Add Card button</li>
 * </ul>
 *
 * <p><b>Flow:</b> Dashboard → Cards tab → <b>Cards Screen</b>
 *    → Add Card → <b>Add Card Form</b> → Fill Details → Submit
 */
public class CardsPage extends BasePage {

    // ==================== Locators — Cards Screen ====================

    // --- Screen Identifiers ---
    private static final By CARDS_SCREEN_TITLE = By.xpath(
            "//*[contains(@content-desc,'Cards') or contains(@content-desc,'My Cards') "
                    + "or contains(@content-desc,'Card Management') "
                    + "or contains(@text,'Cards') or contains(@text,'My Cards')]"
    );

    // --- Bottom Navigation (shared with Dashboard) ---
    private static final By NAV_HOME = AppiumBy.accessibilityId("Home");
    private static final By NAV_CARDS = AppiumBy.accessibilityId("Cards");

    // --- Add Card Button ---
    private static final By ADD_CARD_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Add Card') or contains(@content-desc,'Add card') "
                    + "or contains(@content-desc,'Add New Card') or contains(@content-desc,'add card') "
                    + "or contains(@content-desc,'Link Card') or contains(@content-desc,'Link card') "
                    + "or contains(@content-desc,'link card') "
                    + "or contains(@text,'Add Card') or contains(@text,'Add card') "
                    + "or contains(@text,'Add New Card') or contains(@text,'+ Add Card') "
                    + "or contains(@text,'Link Card') or contains(@text,'Link card')]"
    );

    // FAB / Plus icon for adding card
    private static final By ADD_CARD_FAB = By.xpath(
            "//*[contains(@content-desc,'+') or contains(@content-desc,'Add') "
                    + "or contains(@content-desc,'add')]"
                    + "[contains(@class,'ImageView') or contains(@class,'Button')]"
    );

    // --- Existing Cards List ---
    private static final By CARD_ITEM = By.xpath(
            "//*[contains(@content-desc,'card') or contains(@content-desc,'Card') "
                    + "or contains(@content-desc,'****') or contains(@content-desc,'ending')]"
    );

    // --- Empty State ---
    private static final By EMPTY_STATE = By.xpath(
            "//*[contains(@content-desc,'No cards') or contains(@content-desc,'no cards') "
                    + "or contains(@content-desc,'Add your first card') "
                    + "or contains(@text,'No cards') or contains(@text,'no cards') "
                    + "or contains(@text,'Add your first card')]"
    );

    // ==================== Locators — Add Card Form ====================

    // --- Add Card Form Screen Identifier ---
    private static final By ADD_CARD_FORM_TITLE = By.xpath(
            "//*[contains(@content-desc,'Add Card') or contains(@content-desc,'Add New Card') "
                    + "or contains(@content-desc,'Card Details') or contains(@content-desc,'Enter Card Details') "
                    + "or contains(@text,'Add Card') or contains(@text,'Add New Card') "
                    + "or contains(@text,'Card Details') or contains(@text,'Enter Card Details')]"
    );

    // --- Card Number Input ---
    private static final By CARD_NUMBER_INPUT = By.xpath(
            "//android.widget.EditText[contains(@content-desc,'Card Number') "
                    + "or contains(@content-desc,'card number') "
                    + "or contains(@content-desc,'Card number') "
                    + "or contains(@text,'Card Number') "
                    + "or contains(@text,'card number') "
                    + "or contains(@text,'Enter card number') "
                    + "or contains(@text,'XXXX XXXX XXXX XXXX')]"
    );

    // --- Expiry Date Input ---
    private static final By EXPIRY_DATE_INPUT = By.xpath(
            "//android.widget.EditText[contains(@content-desc,'Expiry') "
                    + "or contains(@content-desc,'expiry') "
                    + "or contains(@content-desc,'Valid') "
                    + "or contains(@content-desc,'MM/YY') "
                    + "or contains(@text,'Expiry') "
                    + "or contains(@text,'expiry') "
                    + "or contains(@text,'MM/YY') "
                    + "or contains(@text,'Valid Thru')]"
    );

    // --- CVV Input ---
    private static final By CVV_INPUT = By.xpath(
            "//android.widget.EditText[contains(@content-desc,'CVV') "
                    + "or contains(@content-desc,'cvv') "
                    + "or contains(@content-desc,'CVC') "
                    + "or contains(@content-desc,'Security Code') "
                    + "or contains(@text,'CVV') "
                    + "or contains(@text,'cvv') "
                    + "or contains(@text,'CVC') "
                    + "or contains(@text,'Security Code')]"
    );

    // --- Cardholder Name Input ---
    private static final By CARDHOLDER_NAME_INPUT = By.xpath(
            "//android.widget.EditText[contains(@content-desc,'Cardholder') "
                    + "or contains(@content-desc,'cardholder') "
                    + "or contains(@content-desc,'Card Holder') "
                    + "or contains(@content-desc,'Name on Card') "
                    + "or contains(@content-desc,'name on card') "
                    + "or contains(@text,'Cardholder') "
                    + "or contains(@text,'cardholder') "
                    + "or contains(@text,'Card Holder') "
                    + "or contains(@text,'Name on Card') "
                    + "or contains(@text,'name on card') "
                    + "or contains(@text,'Enter name')]"
    );

    // --- Submit / Add Card Button (on form) ---
    private static final By SUBMIT_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Submit') or contains(@content-desc,'Add Card') "
                    + "or contains(@content-desc,'Proceed') or contains(@content-desc,'Continue') "
                    + "or contains(@content-desc,'Save Card') or contains(@content-desc,'Confirm') "
                    + "or contains(@text,'Submit') or contains(@text,'Add Card') "
                    + "or contains(@text,'Proceed') or contains(@text,'Continue') "
                    + "or contains(@text,'Save Card') or contains(@text,'Confirm')]"
    );

    // --- EditText fallbacks (positional) ---
    private static final By FIRST_EDIT_TEXT = By.xpath("(//android.widget.EditText)[1]");
    private static final By SECOND_EDIT_TEXT = By.xpath("(//android.widget.EditText)[2]");
    private static final By THIRD_EDIT_TEXT = By.xpath("(//android.widget.EditText)[3]");
    private static final By FOURTH_EDIT_TEXT = By.xpath("(//android.widget.EditText)[4]");

    // --- Error Messages ---
    private static final By ERROR_MESSAGE = By.xpath(
            "//*[contains(@content-desc,'Invalid') or contains(@content-desc,'invalid') "
                    + "or contains(@content-desc,'Error') or contains(@content-desc,'error') "
                    + "or contains(@content-desc,'required') or contains(@content-desc,'Required') "
                    + "or contains(@text,'Invalid') or contains(@text,'invalid') "
                    + "or contains(@text,'Error') or contains(@text,'error') "
                    + "or contains(@text,'required') or contains(@text,'Required') "
                    + "or contains(@text,'Please enter')]"
    );

    // --- Success Indicator ---
    private static final By SUCCESS_INDICATOR = By.xpath(
            "//*[contains(@content-desc,'Success') or contains(@content-desc,'Card Added') "
                    + "or contains(@content-desc,'successfully') or contains(@content-desc,'Card added') "
                    + "or contains(@text,'Success') or contains(@text,'Card Added') "
                    + "or contains(@text,'successfully') or contains(@text,'Card added')]"
    );

    // --- Back / Navigation ---
    private static final By BACK_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Back') or contains(@content-desc,'back') "
                    + "or contains(@content-desc,'Navigate up') or contains(@content-desc,'Close')]"
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

    public CardsPage() {
        log.info("Waiting for Cards screen to load...");
        waitForCardsScreen();
        log.info("CardsPage loaded successfully");
    }

    public CardsPage(boolean skipWait) {
        if (!skipWait) {
            waitForCardsScreen();
        }
    }

    // ==================== Custom Page Load ====================

    private void waitForCardsScreen() {
        int maxWait = getPageLoadTimeout();
        int waited = 0;

        while (waited < maxWait) {
            if (isCardsScreenVisible()) {
                return;
            }
            sleep(2000);
            waited += 2;
        }

        log.warn("Cards screen not detected after {}s — proceeding cautiously", maxWait);
    }

    // ==================== State Detection ====================

    /**
     * Check if the Cards screen is currently visible.
     * Uses multiple strategies for resilient detection.
     */
    public boolean isCardsScreenVisible() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));

            // Strategy 1: Cards tab is selected/active + bottom nav visible
            if (!getDriver().findElements(NAV_CARDS).isEmpty()
                    && !getDriver().findElements(NAV_HOME).isEmpty()) {
                // Check for cards-specific content (not dashboard content)
                if (!getDriver().findElements(CARDS_SCREEN_TITLE).isEmpty()) {
                    return true;
                }
                if (!getDriver().findElements(ADD_CARD_BUTTON).isEmpty()) {
                    return true;
                }
                if (!getDriver().findElements(EMPTY_STATE).isEmpty()) {
                    return true;
                }
                if (!getDriver().findElements(CARD_ITEM).isEmpty()) {
                    return true;
                }
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
     * Wait for the Cards screen to become visible.
     *
     * @param timeoutSeconds max wait time
     * @return true if visible within timeout
     */
    public boolean waitForCardsScreenVisible(int timeoutSeconds) {
        log.info("Waiting for Cards screen to be visible (timeout={}s)...", timeoutSeconds);
        for (int i = 0; i < timeoutSeconds; i++) {
            if (isCardsScreenVisible()) {
                log.info("Cards screen detected after {}s", i);
                return true;
            }
            sleep(1000);
        }
        return false;
    }

    /**
     * Check if the Add Card form/screen is currently visible.
     */
    public boolean isAddCardFormVisible() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));

            // Check for form title
            if (!getDriver().findElements(ADD_CARD_FORM_TITLE).isEmpty()) {
                return true;
            }

            // Check for card number input field
            if (!getDriver().findElements(CARD_NUMBER_INPUT).isEmpty()) {
                return true;
            }

            // Fallback: check for multiple EditText fields (form inputs)
            List<WebElement> editTexts = getDriver().findElements(By.className("android.widget.EditText"));
            if (editTexts.size() >= 3) {
                // Likely a form with card number, expiry, CVV fields
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
     * Check if the Add Card button is visible on the Cards screen.
     */
    public boolean isAddCardButtonVisible() {
        try {
            if (!getDriver().findElements(ADD_CARD_BUTTON).isEmpty()) {
                return true;
            }
            if (!getDriver().findElements(ADD_CARD_FAB).isEmpty()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the empty state is shown (no cards added yet).
     */
    public boolean isEmptyStateVisible() {
        try {
            return !getDriver().findElements(EMPTY_STATE).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if any existing card items are visible.
     */
    public boolean hasExistingCards() {
        try {
            return !getDriver().findElements(CARD_ITEM).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the Cards screen title is visible.
     */
    public boolean isCardsScreenTitleVisible() {
        try {
            return !getDriver().findElements(CARDS_SCREEN_TITLE).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a success indicator is visible after adding a card.
     */
    public boolean isSuccessIndicatorVisible() {
        try {
            return !getDriver().findElements(SUCCESS_INDICATOR).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if an error message is visible on the form.
     */
    public boolean isErrorMessageVisible() {
        try {
            return !getDriver().findElements(ERROR_MESSAGE).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the error message text if visible.
     */
    public String getErrorMessageText() {
        try {
            List<WebElement> errors = getDriver().findElements(ERROR_MESSAGE);
            if (!errors.isEmpty()) {
                String text = errors.get(0).getText();
                if (text == null || text.isEmpty()) {
                    text = errors.get(0).getAttribute("content-desc");
                }
                return text != null ? text : "";
            }
        } catch (Exception e) {
            log.debug("Could not read error message: {}", e.getMessage());
        }
        return "";
    }

    // ==================== Card Number Field ====================

    /**
     * Check if the card number input field is visible.
     */
    public boolean isCardNumberFieldVisible() {
        try {
            if (!getDriver().findElements(CARD_NUMBER_INPUT).isEmpty()) {
                return true;
            }
            // Fallback: first EditText on the add card form
            return !getDriver().findElements(FIRST_EDIT_TEXT).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the expiry date input field is visible.
     */
    public boolean isExpiryDateFieldVisible() {
        try {
            if (!getDriver().findElements(EXPIRY_DATE_INPUT).isEmpty()) {
                return true;
            }
            return !getDriver().findElements(SECOND_EDIT_TEXT).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the CVV input field is visible.
     */
    public boolean isCvvFieldVisible() {
        try {
            if (!getDriver().findElements(CVV_INPUT).isEmpty()) {
                return true;
            }
            return !getDriver().findElements(THIRD_EDIT_TEXT).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the cardholder name input field is visible.
     */
    public boolean isCardholderNameFieldVisible() {
        try {
            if (!getDriver().findElements(CARDHOLDER_NAME_INPUT).isEmpty()) {
                return true;
            }
            return !getDriver().findElements(FOURTH_EDIT_TEXT).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the submit button is visible on the Add Card form.
     */
    public boolean isSubmitButtonVisible() {
        try {
            return !getDriver().findElements(SUBMIT_BUTTON).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the submit button is enabled/clickable.
     */
    public boolean isSubmitButtonEnabled() {
        try {
            List<WebElement> buttons = getDriver().findElements(SUBMIT_BUTTON);
            if (!buttons.isEmpty()) {
                return buttons.get(0).isEnabled();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Actions — Cards Screen ====================

    /**
     * Tap the Add Card button to navigate to the Add Card form.
     */
    public void tapAddCard() {
        log.info("Tapping Add Card button");
        try {
            // Try primary Add Card button
            List<WebElement> addBtn = getDriver().findElements(ADD_CARD_BUTTON);
            if (!addBtn.isEmpty()) {
                addBtn.get(0).click();
                sleep(2000);
                return;
            }

            // Try FAB / Plus button
            List<WebElement> fab = getDriver().findElements(ADD_CARD_FAB);
            if (!fab.isEmpty()) {
                fab.get(0).click();
                sleep(2000);
                return;
            }

            // Try scrolling to find the button
            log.info("Add Card button not immediately visible — scrolling to find it");
            for (int i = 0; i < 3; i++) {
                swipeUp();
                sleep(1500);
                addBtn = getDriver().findElements(ADD_CARD_BUTTON);
                if (!addBtn.isEmpty()) {
                    addBtn.get(0).click();
                    sleep(2000);
                    return;
                }
            }

            log.error("Could not find Add Card button after scrolling");
            throw new com.pice.exceptions.ElementInteractionException(
                    "tap", "Add Card button", new RuntimeException("Button not found"));

        } catch (com.pice.exceptions.ElementInteractionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to tap Add Card button: {}", e.getMessage());
            throw new com.pice.exceptions.ElementInteractionException(
                    "tap", "Add Card button", e);
        }
    }

    /**
     * Navigate to the Home tab from Cards screen.
     */
    public void navigateToHome() {
        log.info("Navigating from Cards to Home tab");
        try {
            tap(NAV_HOME);
        } catch (Exception e) {
            log.warn("Failed to tap Home tab directly, attempting recovery: {}", e.getMessage());
            try {
                com.pice.utils.AppUtils.hideKeyboard();
            } catch (Exception ignored) {}
            try {
                AppiumDriver driver = getDriver();
                if (driver instanceof AndroidDriver androidDriver) {
                    androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                }
                sleep(2000);
            } catch (Exception ignored) {}
            tap(NAV_HOME);
        }
    }

    /**
     * Tap the Back button to navigate back from the Add Card form.
     */
    public void tapBack() {
        log.info("Tapping Back button");
        try {
            List<WebElement> backBtn = getDriver().findElements(BACK_BUTTON);
            if (!backBtn.isEmpty()) {
                backBtn.get(0).click();
                sleep(2000);
                return;
            }

            // Fallback: press Android back key
            log.info("Back button not found — pressing Android Back key");
            AppiumDriver driver = getDriver();
            if (driver instanceof AndroidDriver androidDriver) {
                androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                sleep(2000);
            }
        } catch (Exception e) {
            log.warn("Back navigation failed: {}", e.getMessage());
            // Last resort: Android back key
            try {
                AppiumDriver driver = getDriver();
                if (driver instanceof AndroidDriver androidDriver) {
                    androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                    sleep(2000);
                }
            } catch (Exception ignored) {}
        }
    }

    // ==================== Actions — Add Card Form ====================

    /**
     * Find the card number input field using multiple strategies.
     */
    private WebElement findCardNumberField() {
        try {
            List<WebElement> fields = getDriver().findElements(CARD_NUMBER_INPUT);
            if (!fields.isEmpty()) return fields.get(0);
        } catch (Exception ignored) {}

        try {
            List<WebElement> fields = getDriver().findElements(FIRST_EDIT_TEXT);
            if (!fields.isEmpty()) return fields.get(0);
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Find the expiry date input field using multiple strategies.
     */
    private WebElement findExpiryDateField() {
        try {
            List<WebElement> fields = getDriver().findElements(EXPIRY_DATE_INPUT);
            if (!fields.isEmpty()) return fields.get(0);
        } catch (Exception ignored) {}

        try {
            List<WebElement> fields = getDriver().findElements(SECOND_EDIT_TEXT);
            if (!fields.isEmpty()) return fields.get(0);
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Find the CVV input field using multiple strategies.
     */
    private WebElement findCvvField() {
        try {
            List<WebElement> fields = getDriver().findElements(CVV_INPUT);
            if (!fields.isEmpty()) return fields.get(0);
        } catch (Exception ignored) {}

        try {
            List<WebElement> fields = getDriver().findElements(THIRD_EDIT_TEXT);
            if (!fields.isEmpty()) return fields.get(0);
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Find the cardholder name input field using multiple strategies.
     */
    private WebElement findCardholderNameField() {
        try {
            List<WebElement> fields = getDriver().findElements(CARDHOLDER_NAME_INPUT);
            if (!fields.isEmpty()) return fields.get(0);
        } catch (Exception ignored) {}

        try {
            List<WebElement> fields = getDriver().findElements(FOURTH_EDIT_TEXT);
            if (!fields.isEmpty()) return fields.get(0);
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Enter the card number.
     *
     * @param cardNumber the card number to enter (e.g., "4111111111111111")
     */
    public void enterCardNumber(String cardNumber) {
        log.info("Entering card number: {}****", cardNumber.substring(0, Math.min(4, cardNumber.length())));
        WebElement field = findCardNumberField();
        if (field != null) {
            field.click();
            sleep(500);
            field.clear();
            field.sendKeys(cardNumber);
            log.info("Card number entered successfully");
            hideKeyboard();
            sleep(1000);
        } else {
            log.error("Could not find card number input field");
            throw new com.pice.exceptions.ElementInteractionException(
                    "type", "Card Number field", new RuntimeException("Field not found"));
        }
    }

    /**
     * Enter the expiry date.
     *
     * @param expiryDate the expiry date in MM/YY format (e.g., "12/28")
     */
    public void enterExpiryDate(String expiryDate) {
        log.info("Entering expiry date: {}", expiryDate);
        WebElement field = findExpiryDateField();
        if (field != null) {
            field.click();
            sleep(500);
            field.clear();
            field.sendKeys(expiryDate);
            log.info("Expiry date entered successfully");
            hideKeyboard();
            sleep(1000);
        } else {
            log.error("Could not find expiry date input field");
            throw new com.pice.exceptions.ElementInteractionException(
                    "type", "Expiry Date field", new RuntimeException("Field not found"));
        }
    }

    /**
     * Enter the CVV.
     *
     * @param cvv the CVV code (e.g., "123")
     */
    public void enterCvv(String cvv) {
        log.info("Entering CVV");
        WebElement field = findCvvField();
        if (field != null) {
            field.click();
            sleep(500);
            field.clear();
            field.sendKeys(cvv);
            log.info("CVV entered successfully");
            hideKeyboard();
            sleep(1000);
        } else {
            log.error("Could not find CVV input field");
            throw new com.pice.exceptions.ElementInteractionException(
                    "type", "CVV field", new RuntimeException("Field not found"));
        }
    }

    /**
     * Enter the cardholder name.
     *
     * @param name the cardholder name (e.g., "JOHN DOE")
     */
    public void enterCardholderName(String name) {
        log.info("Entering cardholder name: {}", name);
        WebElement field = findCardholderNameField();
        if (field != null) {
            field.click();
            sleep(500);
            field.clear();
            field.sendKeys(name);
            log.info("Cardholder name entered successfully");
            hideKeyboard();
            sleep(1000);
        } else {
            log.error("Could not find cardholder name input field");
            throw new com.pice.exceptions.ElementInteractionException(
                    "type", "Cardholder Name field", new RuntimeException("Field not found"));
        }
    }

    /**
     * Fill all card details at once.
     *
     * @param cardNumber     the card number
     * @param expiryDate     the expiry date (MM/YY)
     * @param cvv            the CVV
     * @param cardholderName the cardholder name
     */
    public void fillCardDetails(String cardNumber, String expiryDate, String cvv, String cardholderName) {
        log.info("Filling card details...");
        enterCardNumber(cardNumber);
        sleep(500);
        enterExpiryDate(expiryDate);
        sleep(500);
        enterCvv(cvv);
        sleep(500);
        enterCardholderName(cardholderName);
        log.info("All card details filled");
    }

    /**
     * Tap the Submit / Add Card button on the form.
     */
    public void tapSubmit() {
        log.info("Tapping Submit button");
        try {
            sleep(1000);
            tap(SUBMIT_BUTTON);
            sleep(2000);
        } catch (Exception e) {
            log.warn("Submit button tap failed, trying scroll and retry: {}", e.getMessage());
            swipeUp();
            sleep(1500);
            tap(SUBMIT_BUTTON);
            sleep(2000);
        }
    }

    // ==================== Navigation Helpers ====================

    /**
     * Navigate back to the Cards screen from the Add Card form.
     * Tries Back button, then Android Back key.
     *
     * @return true if Cards screen is visible after navigation
     */
    public boolean navigateBackToCardsScreen() {
        log.info("Navigating back to Cards screen...");
        sleep(1000);

        // If already on cards screen
        if (isCardsScreenVisible()) {
            log.info("Already on Cards screen");
            return true;
        }

        // Strategy 1: Tap Back button
        tapBack();
        sleep(2000);
        if (isCardsScreenVisible()) {
            log.info("Returned to Cards screen via Back button");
            return true;
        }

        // Strategy 2: Press Android Back key
        try {
            AppiumDriver driver = getDriver();
            if (driver instanceof AndroidDriver androidDriver) {
                androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                sleep(2000);
                if (isCardsScreenVisible()) {
                    log.info("Returned to Cards screen via Android Back key");
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Android Back key failed: {}", e.getMessage());
        }

        // Strategy 3: Tap Cards tab in bottom nav
        try {
            List<WebElement> cardsTab = getDriver().findElements(NAV_CARDS);
            if (!cardsTab.isEmpty()) {
                cardsTab.get(0).click();
                sleep(2000);
                if (isCardsScreenVisible()) {
                    log.info("Returned to Cards screen via Cards tab");
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Cards tab tap failed: {}", e.getMessage());
        }

        log.warn("Could not return to Cards screen");
        return false;
    }

    /**
     * Get the text value from the card number field (for verification).
     */
    public String getCardNumberFieldValue() {
        try {
            WebElement field = findCardNumberField();
            if (field != null) {
                String text = field.getText();
                if (text == null || text.isEmpty()) {
                    text = field.getAttribute("content-desc");
                }
                return text != null ? text : "";
            }
        } catch (Exception e) {
            log.debug("Could not read card number field value: {}", e.getMessage());
        }
        return "";
    }

    /**
     * Get the text value from the expiry date field.
     */
    public String getExpiryDateFieldValue() {
        try {
            WebElement field = findExpiryDateField();
            if (field != null) {
                String text = field.getText();
                if (text == null || text.isEmpty()) {
                    text = field.getAttribute("content-desc");
                }
                return text != null ? text : "";
            }
        } catch (Exception e) {
            log.debug("Could not read expiry date field value: {}", e.getMessage());
        }
        return "";
    }

    /**
     * Get the text value from the CVV field.
     */
    public String getCvvFieldValue() {
        try {
            WebElement field = findCvvField();
            if (field != null) {
                String text = field.getText();
                if (text == null || text.isEmpty()) {
                    text = field.getAttribute("content-desc");
                }
                return text != null ? text : "";
            }
        } catch (Exception e) {
            log.debug("Could not read CVV field value: {}", e.getMessage());
        }
        return "";
    }

    /**
     * Get the text value from the cardholder name field.
     */
    public String getCardholderNameFieldValue() {
        try {
            WebElement field = findCardholderNameField();
            if (field != null) {
                String text = field.getText();
                if (text == null || text.isEmpty()) {
                    text = field.getAttribute("content-desc");
                }
                return text != null ? text : "";
            }
        } catch (Exception e) {
            log.debug("Could not read cardholder name field value: {}", e.getMessage());
        }
        return "";
    }

    // ==================== Private Helpers ====================

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
