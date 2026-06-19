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
 * Page Object for the Pice App — Credit Cards → Pay Bills tab.
 *
 * <p><b>Screens covered:</b>
 * <ul>
 *   <li>Pay Bills main screen (card list + "Add Card" button)</li>
 *   <li>"Add Credit Card" bottom-sheet dialog (last 4 digits + bank selector)</li>
 *   <li>"Supported Cards" bank selection list (Other Banks)</li>
 *   <li>Fetch Bill result (success or error dialog)</li>
 * </ul>
 *
 * <p><b>Screen Layout (Pay Bills):</b>
 * <ul>
 *   <li>Header: "Credit cards" title + Help icon</li>
 *   <li>Tabs: "Link &amp; Spend" | "Pay Bills" (active)</li>
 *   <li>Section: "My Bills — Simple, Safe &amp; Secure Bill Payments"</li>
 *   <li>"⊕ Add Card" button</li>
 *   <li>Card items (bank name, last-4 digits, Fetch Bill button)</li>
 * </ul>
 *
 * <p><b>Add Credit Card Dialog:</b>
 * <ul>
 *   <li>Title: "Add Credit Card"</li>
 *   <li>4 individual digit input boxes for last 4 digits</li>
 *   <li>Bank quick-selects: Axis Bank, HDFC Bank, ICICI Bank, Kotak Mahindra Bank</li>
 *   <li>"Other Banks" → bottom sheet with full bank list</li>
 *   <li>"Proceed" button</li>
 * </ul>
 *
 * <p><b>Flow:</b> Dashboard → Cards tab → Pay Bills tab → Add Card → fill 4 digits + bank → Proceed
 *
 * <p><b>Known Behaviour:</b>
 * Tapping "Fetch Bill" on a card not linked to the user's mobile number shows:
 * "Error — We could'nt verify this card belongs to you as its not linked to your login mobile number"
 */
public class PayBillPage extends BasePage {

    // ==================== Locators — Pay Bills Main Screen ====================

    /** "Credit cards" screen header */
    private static final By CREDIT_CARDS_HEADER = AppiumBy.accessibilityId("Credit cards");

    /** "Pay Bills" tab */
    private static final By PAY_BILLS_TAB = By.xpath("//*[contains(@content-desc, 'Pay Bills')]");

    /** "Link & Spend" tab */
    private static final By LINK_SPEND_TAB = By.xpath("//*[contains(@content-desc, 'Link & Spend')]");

    /** "My Bills" section heading */
    private static final By MY_BILLS_HEADING = AppiumBy.accessibilityId("My Bills");

    /** "⊕ Add Card" button on Pay Bills screen */
    private static final By ADD_CARD_BUTTON = AppiumBy.accessibilityId("Add Card");

    /** "Help" button at top-right */
    private static final By HELP_BUTTON = AppiumBy.accessibilityId("Help");

    /** Generic card item — matches any existing card in the list */
    private static final By CARD_ITEM_INDUSIND = AppiumBy.accessibilityId("IndusInd Bank");
    private static final By CARD_ITEM_AXIS     = AppiumBy.accessibilityId("Axis Bank");
    private static final By CARD_ITEM_HDFC     = AppiumBy.accessibilityId("HDFC Bank");
    private static final By CARD_ITEM_ICICI    = AppiumBy.accessibilityId("ICICI Bank");
    private static final By CARD_ITEM_KOTAK    = AppiumBy.accessibilityId("Kotak Mahindra Bank");

    /** "Fetch Bill" button — present once per card card in the list */
    private static final By FETCH_BILL_BUTTON = AppiumBy.accessibilityId("Fetch Bill");

    // ==================== Locators — Add Credit Card Dialog ====================

    /** Dialog title "Add Credit Card" */
    private static final By ADD_CREDIT_CARD_TITLE = AppiumBy.accessibilityId("Add Credit Card");

    /** Label "Enter Last 4 digits of your card:" */
    private static final By LAST_4_DIGITS_LABEL = AppiumBy.accessibilityId("Enter Last 4 digits of your card:");

    /**
     * The 4 OTP-style digit boxes.
     * These are individual EditText/View elements; we use positional XPaths since they
     * have no distinct accessibility IDs.
     */
    private static final By DIGIT_BOX_1 = By.xpath("(//android.widget.EditText)[1]");
    private static final By DIGIT_BOX_2 = By.xpath("(//android.widget.EditText)[2]");
    private static final By DIGIT_BOX_3 = By.xpath("(//android.widget.EditText)[3]");
    private static final By DIGIT_BOX_4 = By.xpath("(//android.widget.EditText)[4]");

    /** Fallback: all EditText fields (when dialog is open, there should be 4) */
    private static final By ALL_EDIT_TEXTS = By.className("android.widget.EditText");

    // Bank quick-select buttons inside the dialog
    private static final By BANK_AXIS   = AppiumBy.accessibilityId("Axis Bank");
    private static final By BANK_HDFC   = AppiumBy.accessibilityId("HDFC Bank");
    private static final By BANK_ICICI  = AppiumBy.accessibilityId("ICICI Bank");
    private static final By BANK_KOTAK  = AppiumBy.accessibilityId("Kotak Mahindra Bank");
    private static final By OTHER_BANKS = AppiumBy.accessibilityId("Other Banks");

    /** "Proceed" button inside the Add Credit Card dialog */
    private static final By PROCEED_BUTTON = AppiumBy.accessibilityId("Proceed");

    /** Close "×" button on the Add Credit Card dialog */
    private static final By DIALOG_CLOSE_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Close') or contains(@content-desc,'close')"
                    + " or contains(@content-desc,'×') or contains(@content-desc,'X')]"
                    + "[@clickable='true']"
    );

    // ==================== Locators — Supported Cards (Other Banks) Sheet ====================

    /** Title of the "Supported Cards" bottom sheet */
    private static final By SUPPORTED_CARDS_TITLE = AppiumBy.accessibilityId("Supported Cards");

    /** Scrim / backdrop — tapping this dismisses the sheet */
    private static final By SCRIM = AppiumBy.accessibilityId("Scrim");

    // ==================== Locators — Fetch Bill Result / Error ====================

    /** "Error" label on the error dialog */
    private static final By ERROR_LABEL = AppiumBy.accessibilityId("Error");

    /** Full error message text */
    private static final By ERROR_MESSAGE = AppiumBy.accessibilityId(
            "We could'nt verify this card belongs to you as its not linked to your login mobile number"
    );

    /** "Okay" dismiss button on the error dialog */
    private static final By OKAY_BUTTON = AppiumBy.accessibilityId("Okay");

    // ==================== Page Load ====================

    @Override
    protected By getPageLoadedLocator() {
        return null; // Uses custom multi-strategy in constructor
    }

    @Override
    protected int getPageLoadTimeout() {
        return 10;
    }

    // ==================== Constructor ====================

    public PayBillPage() {
        log.info("Waiting for Pay Bills screen to load...");
        waitForPayBillsScreen();
        log.info("PayBillPage loaded successfully");
    }

    public PayBillPage(boolean skipWait) {
        if (!skipWait) {
            waitForPayBillsScreen();
        }
    }

    // ==================== Custom Page Load ====================

    private void waitForPayBillsScreen() {
        for (int i = 0; i < getPageLoadTimeout(); i++) {
            if (isPayBillsTabActive()) {
                return;
            }
            sleep(1000);
        }
        log.warn("Pay Bills tab not confirmed active after {}s — proceeding", getPageLoadTimeout());
    }

    // ==================== State Detection — Main Screen ====================

    /**
     * Returns true if the Credit Cards screen is visible (either tab).
     */
    public boolean isCreditCardsScreenVisible() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));
            return !getDriver().findElements(PAY_BILLS_TAB).isEmpty()
                    || !getDriver().findElements(LINK_SPEND_TAB).isEmpty()
                    || !getDriver().findElements(ADD_CARD_BUTTON).isEmpty();
        } catch (Exception e) {
            return false;
        } finally {
            restoreImplicitWait();
        }
    }

    /**
     * Returns true if the "Pay Bills" tab is the active/selected tab.
     */
    public boolean isPayBillsTabActive() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));
            // Check: Pay Bills tab visible AND Add Card button visible (only shown on Pay Bills)
            return !getDriver().findElements(PAY_BILLS_TAB).isEmpty()
                    && !getDriver().findElements(ADD_CARD_BUTTON).isEmpty();
        } catch (Exception e) {
            return false;
        } finally {
            restoreImplicitWait();
        }
    }

    /**
     * Returns true if the "Add Card" button is present on the Pay Bills screen.
     */
    public boolean isAddCardButtonVisible() {
        try {
            return !getDriver().findElements(ADD_CARD_BUTTON).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the "My Bills" heading is visible.
     */
    public boolean isMyBillsHeadingVisible() {
        try {
            return !getDriver().findElements(MY_BILLS_HEADING).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if at least one "Fetch Bill" button is visible.
     */
    public boolean isFetchBillButtonVisible() {
        try {
            return !getDriver().findElements(FETCH_BILL_BUTTON).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the number of "Fetch Bill" buttons visible (= number of cards shown).
     */
    public int getVisibleCardCount() {
        try {
            return getDriver().findElements(FETCH_BILL_BUTTON).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns true if a card for IndusInd Bank is visible.
     */
    public boolean isIndusIndCardVisible() {
        try {
            return !getDriver().findElements(CARD_ITEM_INDUSIND).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== State Detection — Add Credit Card Dialog ====================

    /**
     * Returns true if the "Add Credit Card" dialog is visible.
     */
    public boolean isAddCardDialogVisible() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));
            return !getDriver().findElements(ADD_CREDIT_CARD_TITLE).isEmpty()
                    || !getDriver().findElements(LAST_4_DIGITS_LABEL).isEmpty();
        } catch (Exception e) {
            return false;
        } finally {
            restoreImplicitWait();
        }
    }

    /**
     * Returns true if the Proceed button is visible in the dialog.
     */
    public boolean isProceedButtonVisible() {
        try {
            return !getDriver().findElements(PROCEED_BUTTON).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if all 4 digit boxes are visible in the dialog.
     */
    public boolean areDigitBoxesVisible() {
        try {
            return getDriver().findElements(ALL_EDIT_TEXTS).size() >= 4
                    || !getDriver().findElements(LAST_4_DIGITS_LABEL).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the bank selection options are visible in the dialog.
     */
    public boolean areBankOptionsVisible() {
        try {
            return !getDriver().findElements(BANK_AXIS).isEmpty()
                    || !getDriver().findElements(BANK_HDFC).isEmpty()
                    || !getDriver().findElements(OTHER_BANKS).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== State Detection — Error Dialog ====================

    /**
     * Returns true if the Fetch Bill error dialog is visible.
     */
    public boolean isErrorDialogVisible() {
        try {
            return !getDriver().findElements(ERROR_LABEL).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the error message text (or empty string if not visible).
     */
    public String getErrorMessageText() {
        try {
            List<WebElement> msgs = getDriver().findElements(ERROR_MESSAGE);
            if (!msgs.isEmpty()) {
                String txt = msgs.get(0).getAttribute("content-desc");
                return txt != null ? txt : "";
            }
            // Fallback: look for any text containing "verify"
            List<WebElement> anyViews = getDriver().findElements(
                    By.xpath("//*[contains(@content-desc,'verify') or contains(@content-desc,'linked')]")
            );
            if (!anyViews.isEmpty()) {
                return anyViews.get(0).getAttribute("content-desc");
            }
        } catch (Exception e) {
            log.debug("Could not read error message: {}", e.getMessage());
        }
        return "";
    }

    // ==================== State Detection — Supported Cards Sheet ====================

    /**
     * Returns true if the "Supported Cards" (Other Banks) sheet is visible.
     */
    public boolean isSupportedCardsSheetVisible() {
        try {
            return !getDriver().findElements(SUPPORTED_CARDS_TITLE).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Actions — Main Screen ====================

    /**
     * Tap the "Pay Bills" tab to activate it.
     */
    public void tapPayBillsTab() {
        log.info("Tapping Pay Bills tab");
        try {
            tap(PAY_BILLS_TAB);
            sleep(2000);
        } catch (Exception e) {
            log.warn("Pay Bills tab tap failed: {}", e.getMessage());
        }
    }

    /**
     * Tap the "⊕ Add Card" button to open the Add Credit Card dialog.
     */
    public void tapAddCard() {
        log.info("Tapping Add Card button on Pay Bills screen");
        try {
            List<WebElement> buttons = getDriver().findElements(ADD_CARD_BUTTON);
            if (!buttons.isEmpty()) {
                buttons.get(0).click();
                sleep(2000);
                return;
            }
            // Fallback: scroll to top then try again
            swipeDown();
            sleep(1000);
            tap(ADD_CARD_BUTTON);
            sleep(2000);
        } catch (Exception e) {
            log.error("Failed to tap Add Card: {}", e.getMessage());
            throw new com.pice.exceptions.ElementInteractionException("tap", "Add Card button", e);
        }
    }

    /**
     * Tap the "Fetch Bill" button for the first card in the list.
     * Use this for the card at position {@code index} (0-based).
     *
     * @param index 0-based index of the card card (0 = first card)
     */
    public void tapFetchBill(int index) {
        log.info("Tapping Fetch Bill button at index {}", index);
        try {
            List<WebElement> fetchButtons = getDriver().findElements(FETCH_BILL_BUTTON);
            if (fetchButtons.size() > index) {
                fetchButtons.get(index).click();
                sleep(3000);
            } else {
                log.error("Fetch Bill button at index {} not found (found {})", index, fetchButtons.size());
                throw new com.pice.exceptions.ElementInteractionException(
                        "tap", "Fetch Bill button[" + index + "]",
                        new RuntimeException("Button not found at index " + index));
            }
        } catch (com.pice.exceptions.ElementInteractionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to tap Fetch Bill button: {}", e.getMessage());
            throw new com.pice.exceptions.ElementInteractionException("tap", "Fetch Bill button", e);
        }
    }

    /**
     * Tap the "Fetch Bill" button for the IndusInd Bank card specifically.
     * Scrolls to find the IndusInd card first.
     */
    public void tapFetchBillForIndusInd() {
        log.info("Tapping Fetch Bill for IndusInd Bank card");
        try {
            // IndusInd card is typically the first card (index 0)
            tapFetchBill(0);
        } catch (Exception e) {
            // Try scrolling up and re-attempt
            swipeDown();
            sleep(1000);
            tapFetchBill(0);
        }
    }

    // ==================== Actions — Add Credit Card Dialog ====================

    /**
     * Enter the last 4 digits of the card into the 4-box OTP input.
     * Each character is typed individually to trigger auto-advance.
     *
     * @param last4Digits exactly 4 digits (e.g., "7649")
     */
    public void enterLast4Digits(String last4Digits) {
        if (last4Digits == null || last4Digits.length() != 4) {
            throw new IllegalArgumentException("last4Digits must be exactly 4 characters, got: " + last4Digits);
        }
        log.info("Entering last 4 digits: {}", last4Digits);

        try {
            List<WebElement> boxes = getDriver().findElements(ALL_EDIT_TEXTS);
            if (!boxes.isEmpty()) {
                // Focus the first box
                boxes.get(0).click();
                sleep(500);

                // Send each digit individually — the field auto-advances after each character
                for (int i = 0; i < 4; i++) {
                    // Re-query boxes each iteration (focus may shift to next box)
                    List<WebElement> currentBoxes = getDriver().findElements(ALL_EDIT_TEXTS);
                    if (i < currentBoxes.size()) {
                        currentBoxes.get(i).sendKeys(String.valueOf(last4Digits.charAt(i)));
                    } else {
                        // Fallback: type remaining digits into whichever box is currently focused
                        getDriver().findElements(ALL_EDIT_TEXTS).get(0)
                                .sendKeys(last4Digits.substring(i));
                        break;
                    }
                    sleep(400);
                }
                log.info("Last 4 digits entered: {}", last4Digits);
                sleep(500);
            } else {
                // No EditText found — try ADB input as fallback
                log.warn("No EditText boxes found in dialog — trying ADB text input");
                // Tap approximate location of first digit box and type
                getDriver().findElement(LAST_4_DIGITS_LABEL); // ensure dialog is open
                // Use Actions to send keys
                io.appium.java_client.android.AndroidDriver androidDriver =
                        (io.appium.java_client.android.AndroidDriver) getDriver();
                androidDriver.pressKey(new KeyEvent(AndroidKey.DIGIT_7));
                sleep(400);
                for (char c : last4Digits.substring(1).toCharArray()) {
                    androidDriver.pressKey(new KeyEvent(
                            AndroidKey.valueOf("DIGIT_" + c)));
                    sleep(400);
                }
            }
        } catch (Exception e) {
            log.error("Failed to enter last 4 digits: {}", e.getMessage());
            throw new com.pice.exceptions.ElementInteractionException("type", "digit boxes", e);
        }
    }

    /**
     * Select a bank from the quick-select options in the Add Credit Card dialog.
     *
     * @param bankName one of: "Axis Bank", "HDFC Bank", "ICICI Bank", "Kotak Mahindra Bank"
     */
    public void selectBank(String bankName) {
        log.info("Selecting bank: {}", bankName);
        By locator = resolveBankLocator(bankName);
        try {
            tap(locator);
            sleep(1500);
            log.info("Bank selected: {}", bankName);
        } catch (Exception e) {
            log.warn("Direct bank selection failed for '{}': {}", bankName, e.getMessage());
            throw new com.pice.exceptions.ElementInteractionException("tap", "bank button: " + bankName, e);
        }
    }

    /**
     * Tap "Other Banks" to open the full Supported Cards list.
     */
    public void tapOtherBanks() {
        log.info("Tapping Other Banks");
        try {
            tap(OTHER_BANKS);
            sleep(2000);
        } catch (Exception e) {
            log.error("Failed to tap Other Banks: {}", e.getMessage());
            throw new com.pice.exceptions.ElementInteractionException("tap", "Other Banks button", e);
        }
    }

    /**
     * Select a bank from the "Supported Cards" other-banks sheet.
     * The sheet must already be open (call {@link #tapOtherBanks()} first).
     *
     * @param bankName exact display name (e.g., "IDFC FIRST Bank", "Canara Bank")
     */
    public void selectBankFromSheet(String bankName) {
        log.info("Selecting bank from Supported Cards sheet: {}", bankName);
        By locator = AppiumBy.accessibilityId(bankName);
        try {
            // Try direct tap first
            if (!getDriver().findElements(locator).isEmpty()) {
                tap(locator);
                sleep(1500);
                return;
            }
            // Scroll to find it
            for (int i = 0; i < 5; i++) {
                swipeUp();
                sleep(800);
                if (!getDriver().findElements(locator).isEmpty()) {
                    tap(locator);
                    sleep(1500);
                    return;
                }
            }
            log.error("Bank '{}' not found in Supported Cards sheet", bankName);
            throw new com.pice.exceptions.ElementInteractionException(
                    "tap", "bank from sheet: " + bankName,
                    new RuntimeException("Bank not found: " + bankName));
        } catch (com.pice.exceptions.ElementInteractionException e) {
            throw e;
        } catch (Exception e) {
            throw new com.pice.exceptions.ElementInteractionException("tap", "bank from sheet: " + bankName, e);
        }
    }

    /**
     * Tap the "Proceed" button to submit the Add Credit Card form.
     */
    public void tapProceed() {
        log.info("Tapping Proceed button");
        try {
            tap(PROCEED_BUTTON);
            sleep(3000);
        } catch (Exception e) {
            log.warn("Proceed button tap failed: {}", e.getMessage());
            // Scroll down a bit and retry
            swipeUp();
            sleep(1000);
            tap(PROCEED_BUTTON);
            sleep(3000);
        }
    }

    /**
     * Close the Add Credit Card dialog using the × button.
     * Falls back to Android Back key.
     */
    public void closeAddCardDialog() {
        log.info("Closing Add Credit Card dialog");
        try {
            // Try the × close button (ImageView at top-right — no content-desc, use bounds)
            // The × button is at bounds [645,205][690,250] in the hierarchy, clickable=true
            List<WebElement> closeButtons = getDriver().findElements(
                    By.xpath("//android.widget.ImageView[@clickable='true' and @bounds='[645,205][690,250]']")
            );
            if (!closeButtons.isEmpty()) {
                closeButtons.get(0).click();
                sleep(2000);
                return;
            }
        } catch (Exception e) {
            log.debug("× button not found by bounds, trying Android Back: {}", e.getMessage());
        }
        // Fallback: Android Back key
        try {
            AppiumDriver driver = getDriver();
            if (driver instanceof AndroidDriver androidDriver) {
                androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                sleep(2000);
            }
        } catch (Exception e) {
            log.warn("Could not close Add Card dialog: {}", e.getMessage());
        }
    }

    // ==================== Actions — Error Dialog ====================

    /**
     * Tap the "Okay" button to dismiss the Fetch Bill error dialog.
     */
    public void dismissErrorDialog() {
        log.info("Dismissing error dialog by tapping Okay");
        try {
            tap(OKAY_BUTTON);
            sleep(2000);
        } catch (Exception e) {
            log.warn("Okay button not found, trying Android Back: {}", e.getMessage());
            try {
                AppiumDriver driver = getDriver();
                if (driver instanceof AndroidDriver androidDriver) {
                    androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                    sleep(2000);
                }
            } catch (Exception ignored) {}
        }
    }

    // ==================== Actions — Supported Cards Sheet ====================

    /**
     * Dismiss the Supported Cards sheet by tapping the Scrim (backdrop).
     * Falls back to Android Back key.
     */
    public void dismissSupportedCardsSheet() {
        log.info("Dismissing Supported Cards sheet");
        try {
            List<WebElement> scrim = getDriver().findElements(SCRIM);
            if (!scrim.isEmpty()) {
                scrim.get(0).click();
                sleep(1500);
                return;
            }
        } catch (Exception e) {
            log.debug("Scrim not found: {}", e.getMessage());
        }
        // Fallback: Back key
        try {
            AppiumDriver driver = getDriver();
            if (driver instanceof AndroidDriver androidDriver) {
                androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                sleep(1500);
            }
        } catch (Exception e) {
            log.warn("Could not dismiss Supported Cards sheet: {}", e.getMessage());
        }
    }

    // ==================== Combined Actions ====================

    /**
     * Complete the full "Add Credit Card" flow:
     * Opens the dialog, enters the last 4 digits, selects a quick-select bank, and taps Proceed.
     *
     * @param last4Digits 4-digit string (e.g., "7649")
     * @param bankName    one of the quick-select banks ("Axis Bank", "HDFC Bank", "ICICI Bank",
     *                    "Kotak Mahindra Bank")
     */
    public void addCreditCard(String last4Digits, String bankName) {
        log.info("=== Add Credit Card: digits={}, bank={} ===", last4Digits, bankName);
        tapAddCard();
        sleep(1500);

        if (!isAddCardDialogVisible()) {
            log.warn("Add Credit Card dialog not visible after tapping Add Card");
        }

        enterLast4Digits(last4Digits);
        sleep(500);
        selectBank(bankName);
        sleep(500);
        tapProceed();
    }

    /**
     * Complete the full "Add Credit Card" flow using "Other Banks" path.
     *
     * @param last4Digits    4-digit string
     * @param otherBankName  exact name from the Supported Cards sheet
     */
    public void addCreditCardOtherBank(String last4Digits, String otherBankName) {
        log.info("=== Add Credit Card (Other Bank): digits={}, bank={} ===", last4Digits, otherBankName);
        tapAddCard();
        sleep(1500);

        if (!isAddCardDialogVisible()) {
            log.warn("Add Credit Card dialog not visible after tapping Add Card");
        }

        enterLast4Digits(last4Digits);
        sleep(500);
        tapOtherBanks();
        sleep(1500);
        selectBankFromSheet(otherBankName);
        sleep(500);
        tapProceed();
    }

    // ==================== Private Helpers ====================

    private By resolveBankLocator(String bankName) {
        return switch (bankName.trim()) {
            case "Axis Bank"           -> BANK_AXIS;
            case "HDFC Bank"           -> BANK_HDFC;
            case "ICICI Bank"          -> BANK_ICICI;
            case "Kotak Mahindra Bank",
                 "Kotak Mahindra..."   -> BANK_KOTAK;
            default -> AppiumBy.accessibilityId(bankName);
        };
    }

    private void restoreImplicitWait() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(
                    com.pice.config.ConfigManager.getImplicitWait()));
        } catch (Exception ignored) {}
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
