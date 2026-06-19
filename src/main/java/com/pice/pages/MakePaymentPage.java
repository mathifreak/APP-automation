package com.pice.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the Make Payment screen.
 *
 * <p>This screen is reached by tapping the "Make Payment" floating button
 * on the Home Dashboard. It displays:
 * <ul>
 *   <li>Search bar for finding existing beneficiaries</li>
 *   <li>"Add New Beneficiary" button/card</li>
 *   <li>List of previously added beneficiaries</li>
 *   <li>Back / navigation arrow</li>
 * </ul>
 *
 * <p><b>Flow:</b> Dashboard → <b>Make Payment</b> → Add New Beneficiary
 */
public class MakePaymentPage extends BasePage {

    // ==================== Locators ====================

    // --- Screen Identifiers ---
    private static final By SCREEN_TITLE = By.xpath(
            "//*[contains(@content-desc,'Make Payment') or contains(@text,'Make Payment')]"
    );

    // --- Add New Beneficiary ---
    private static final By ADD_NEW_BENEFICIARY = By.xpath(
            "//*[contains(@content-desc,'Add New Beneficiary') or contains(@content-desc,'Add new beneficiary') "
                    + "or contains(@text,'Add New Beneficiary') or contains(@text,'Add new beneficiary')]"
    );

    // Alternative locators for Add New Beneficiary (React Native variations)
    private static final By ADD_BENEFICIARY_ICON = By.xpath(
            "//*[contains(@content-desc,'Add') and contains(@content-desc,'Beneficiary')]"
    );

    // --- Search ---
    private static final By SEARCH_BAR = By.xpath(
            "//*[contains(@content-desc,'Search') or contains(@text,'Search')]"
    );

    // --- Navigation ---
    private static final By BACK_BUTTON = By.xpath(
            "//*[contains(@content-desc,'Back') or contains(@content-desc,'back') "
                    + "or contains(@content-desc,'Navigate up')]"
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

    public MakePaymentPage() {
        log.info("Waiting for Make Payment screen to load...");
        waitForMakePaymentScreen();
        log.info("MakePaymentPage loaded successfully");
    }

    public MakePaymentPage(boolean skipWait) {
        if (!skipWait) {
            waitForMakePaymentScreen();
        }
    }

    // ==================== Custom Page Load ====================

    private void waitForMakePaymentScreen() {
        int maxWait = getPageLoadTimeout();
        int waited = 0;

        while (waited < maxWait) {
            if (isMakePaymentScreenVisible()) {
                return;
            }
            sleep(2000);
            waited += 2;
        }

        log.warn("Make Payment screen not detected after {}s — proceeding cautiously", maxWait);
    }

    // ==================== State Detection ====================

    /**
     * Check if the Make Payment screen is currently visible.
     */
    public boolean isMakePaymentScreenVisible() {
        try {
            getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ofMillis(500));
            // Check for screen title or Add New Beneficiary button
            if (!getDriver().findElements(SCREEN_TITLE).isEmpty()) {
                return true;
            }
            if (!getDriver().findElements(ADD_NEW_BENEFICIARY).isEmpty()) {
                return true;
            }
            if (!getDriver().findElements(ADD_BENEFICIARY_ICON).isEmpty()) {
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
     * Tap the "Add New Beneficiary" button/card.
     *
     * @return AddBeneficiaryPage after navigation
     */
    public AddBeneficiaryPage tapAddNewBeneficiary() {
        log.info("Tapping Add New Beneficiary");

        // Try multiple locator strategies
        WebElement element = findAddNewBeneficiaryElement();
        if (element != null) {
            element.click();
            sleep(2000);
            return new AddBeneficiaryPage();
        }

        // Fallback: scroll and retry
        log.info("Add New Beneficiary not immediately visible — scrolling to find it");
        swipeUp();
        sleep(1500);
        element = findAddNewBeneficiaryElement();
        if (element != null) {
            element.click();
            sleep(2000);
            return new AddBeneficiaryPage();
        }

        // Last resort: tap by accessibility id patterns
        log.warn("Falling back to broad search for Add New Beneficiary");
        tap(ADD_NEW_BENEFICIARY);
        sleep(2000);
        return new AddBeneficiaryPage();
    }

    /**
     * Search for a beneficiary by name or account number.
     *
     * @param query the search text
     */
    public void searchBeneficiary(String query) {
        log.info("Searching for beneficiary: {}", query);
        try {
            WebElement searchField = getDriver().findElement(SEARCH_BAR);
            searchField.click();
            sleep(500);
            searchField.clear();
            searchField.sendKeys(query);
            sleep(1000);
        } catch (Exception e) {
            log.warn("Search bar not found: {}", e.getMessage());
        }
    }

    /**
     * Tap the back button to return to the previous screen.
     */
    public void tapBack() {
        log.info("Tapping back on Make Payment screen");
        try {
            tap(BACK_BUTTON);
        } catch (Exception e) {
            log.warn("Back button not found, using Android back: {}", e.getMessage());
            pressAndroidBack();
        }
    }

    // ==================== Verification Methods ====================

    /**
     * Check if Add New Beneficiary button is visible.
     */
    public boolean isAddNewBeneficiaryVisible() {
        return findAddNewBeneficiaryElement() != null;
    }

    /**
     * Check if the search bar is visible.
     */
    public boolean isSearchBarVisible() {
        try {
            return !getDriver().findElements(SEARCH_BAR).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Private Helpers ====================

    private WebElement findAddNewBeneficiaryElement() {
        try {
            List<WebElement> elements = getDriver().findElements(ADD_NEW_BENEFICIARY);
            if (!elements.isEmpty()) {
                return elements.get(0);
            }
            elements = getDriver().findElements(ADD_BENEFICIARY_ICON);
            if (!elements.isEmpty()) {
                return elements.get(0);
            }
        } catch (Exception e) {
            log.debug("Add New Beneficiary element search failed: {}", e.getMessage());
        }
        return null;
    }

    private void pressAndroidBack() {
        try {
            io.appium.java_client.AppiumDriver driver = getDriver();
            if (driver instanceof io.appium.java_client.android.AndroidDriver androidDriver) {
                androidDriver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                        io.appium.java_client.android.nativekey.AndroidKey.BACK));
            }
        } catch (Exception e) {
            log.warn("Android back press failed: {}", e.getMessage());
        }
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
