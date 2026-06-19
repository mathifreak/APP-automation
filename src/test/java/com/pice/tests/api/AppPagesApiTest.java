package com.pice.tests.api;

import com.pice.api.ApiResponse;
import com.pice.base.BaseApiTest;
import com.pice.constants.TestGroups;
import com.pice.utils.SoftAssertUtils;
import io.restassured.path.json.JsonPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Premium API Automation suite validating staging endpoints corresponding to App Pages.
 * Focuses on detailed structure validation and business logic consistency checking.
 */
public class AppPagesApiTest extends BaseApiTest {

    private static final Logger log = LogManager.getLogger(AppPagesApiTest.class);

    @BeforeMethod(alwaysRun = true)
    public void configureStagingHeaders() {
        // Base URL discovered from active inspector server requests
        apiClient = createClientFor("https://pre-credit.pice.one");

        // Inject active inspect session headers to authenticate live requests
        apiClient.addHeader("X-Pice-Platform", "ANDROID")
                 .addHeader("X-Pice-Api-Version", "2")
                 .addHeader("X-Pice-App-Version", "112")
                 .addHeader("X-Pice-OS", "15")
                 .addHeader("X-Pice-User-Id", "9fe74cce-d9c8-4a7c-bf1d-72fac40de310")
                 .addHeader("X-Pice-Device-Id", "e5064f9d29d24e6f")
                 .addHeader("X-Pice-Session-Id", "IWmLkaRp4W52jMHYUSelTHnb316wjX4sbPJG4YWcklJSz9MIKgknGM2Owi2Hds3BfJXPA2wtCH6+Dtl5aUYmrLIVwjcz9eDqPKMES5f5cbiLX2EgDrTi8AuUVXlNaXd804NXYm6ZSWqrHDZRvhFuSHT7GCZ7gQ+G8h5BL8dHjtKGUJAdVXEAtlB38qKcRcrRko1SBpk=")
                 .addHeader("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 15; V2509 Build/AP3A.240905.015.A2_V000L1)")
                 .addHeader("Origin", "one.pice.pice_business_loan.pre");
    }

    /**
     * Verifies the Home/Dashboard API endpoint.
     * Corresponds to DashboardPage.
     */
    @Test(groups = {TestGroups.SMOKE, "api", "dashboard"})
    public void verifyDashboardPageApi() {
        log.info("Executing API verification for Dashboard Page...");

        ApiResponse apiResponse = apiClient.get("/home/dashboard");
        SoftAssertUtils.assertEquals(apiResponse.getStatusCode(), 200, "Dashboard API status code should be 200");

        JsonPath json = apiResponse.getOriginalResponse().jsonPath();
        
        // Metadata validation
        SoftAssertUtils.assertTrue(json.getBoolean("meta.success"), "Meta success should be true");
        SoftAssertUtils.assertEquals(json.getString("meta.code"), "SUCCESS", "Meta code should be SUCCESS");

        // User profile info validation
        SoftAssertUtils.assertEquals(json.getString("data.user.name"), "KARMUGELAN ANBAZHAGAN", "Incorrect user name");
        SoftAssertUtils.assertEquals(json.getString("data.user.mobile"), "9962063736", "Incorrect user mobile");
        SoftAssertUtils.assertEquals(json.getString("data.user.persona"), "PERSONA_INDIVIDUAL", "Incorrect user persona");

        // Onboarding info validation
        SoftAssertUtils.assertEquals(json.getString("data.onboarding.pan"), "AXUPK1424F", "Incorrect PAN registered");
        SoftAssertUtils.assertEquals(json.getString("data.onboarding.panStatus"), "PAN_AUTO_FETCHED", "Incorrect PAN auto-fetch status");

        // Coins balance validation
        SoftAssertUtils.assertTrue(json.getInt("data.reward.currentCoins") >= 0, "Coins balance should be positive or zero");

        // Recent Contacts validation
        List<Map<String, Object>> recentContacts = json.getList("data.recentContacts");
        SoftAssertUtils.assertNotNull(recentContacts, "Recent contacts list should not be null");
        SoftAssertUtils.assertFalse(recentContacts.isEmpty(), "Recent contacts list should not be empty");

        // Verify specific contacts are present in the recent list
        boolean hasMathivanan = false;
        boolean hasBlinkCommerce = false;
        for (Map<String, Object> contact : recentContacts) {
            String nickname = (String) contact.get("nickname");
            if ("Mathivanan A".equals(nickname)) {
                hasMathivanan = true;
            } else if ("Blink Commerce Private Limited".equals(nickname)) {
                hasBlinkCommerce = true;
            }
        }
        SoftAssertUtils.assertTrue(hasMathivanan, "Dashboard recent contacts should contain 'Mathivanan A'");
        SoftAssertUtils.assertTrue(hasBlinkCommerce, "Dashboard recent contacts should contain 'Blink Commerce Private Limited'");

        SoftAssertUtils.assertAll();
    }

    /**
     * Verifies the Cards Page API endpoint.
     * Corresponds to CardsPage.
     */
    @Test(groups = {TestGroups.SMOKE, "api", "cards"})
    public void verifyCardsPageApi() {
        log.info("Executing API verification for Cards Page...");

        ApiResponse apiResponse = apiClient.get("/bbps/bill-account/card/list");
        SoftAssertUtils.assertEquals(apiResponse.getStatusCode(), 200, "Cards API status code should be 200");

        JsonPath json = apiResponse.getOriginalResponse().jsonPath();

        // Metadata validation
        SoftAssertUtils.assertTrue(json.getBoolean("meta.success"), "Meta success should be true");
        SoftAssertUtils.assertEquals(json.getString("meta.code"), "SUCCESS", "Meta code should be SUCCESS");

        // Verify ccAccounts cards details
        List<Map<String, Object>> ccAccounts = json.getList("data.ccAccounts");
        SoftAssertUtils.assertNotNull(ccAccounts, "Credit card accounts list should not be null");
        SoftAssertUtils.assertFalse(ccAccounts.isEmpty(), "Credit card accounts list should not be empty");

        // Validate structure of the first card
        Map<String, Object> firstCard = ccAccounts.get(0);
        SoftAssertUtils.assertNotNull(firstCard.get("id"), "Card ID should not be null");
        SoftAssertUtils.assertNotNull(firstCard.get("last4"), "Card last4 digits should not be null");
        SoftAssertUtils.assertNotNull(firstCard.get("networkProvider"), "Card network provider should not be null");

        // Validate presence of specific configured cards
        boolean hasAxis6401 = false;
        boolean hasHdfc7129 = false;
        for (Map<String, Object> card : ccAccounts) {
            String last4 = (String) card.get("last4");
            Map<String, Object> bank = (Map<String, Object>) card.get("bank");
            String bankName = bank != null ? (String) bank.get("name") : "";
            
            if ("6401".equals(last4) && "Axis Bank".equals(bankName)) {
                hasAxis6401 = true;
            } else if ("7129".equals(last4) && "HDFC Bank".equals(bankName)) {
                hasHdfc7129 = true;
            }
        }
        SoftAssertUtils.assertTrue(hasAxis6401, "Cards list should contain Axis Bank card ending in 6401");
        SoftAssertUtils.assertTrue(hasHdfc7129, "Cards list should contain HDFC Bank card ending in 7129");

        SoftAssertUtils.assertAll();
    }

    /**
     * Verifies the Onboarding / Permissions API endpoint.
     * Corresponds to LoginPage / PermissionPage flow.
     */
    @Test(groups = {TestGroups.SMOKE, "api", "onboard"})
    public void verifyOnboardStatusApi() {
        log.info("Executing API verification for Onboarding/Permissions Page...");

        ApiResponse apiResponse = apiClient.get("/onboard/status");
        SoftAssertUtils.assertEquals(apiResponse.getStatusCode(), 200, "Onboard API status code should be 200");

        JsonPath json = apiResponse.getOriginalResponse().jsonPath();

        // Metadata validation
        SoftAssertUtils.assertTrue(json.getBoolean("meta.success"), "Meta success should be true");
        SoftAssertUtils.assertEquals(json.getString("meta.code"), "SUCCESS", "Meta code should be SUCCESS");

        // Onboarding and User validation
        SoftAssertUtils.assertEquals(json.getString("data.onboarding.state"), "AADHAAR", "Incorrect onboarding state");
        SoftAssertUtils.assertEquals(json.getString("data.onboarding.pan"), "AXUPK1424F", "Incorrect PAN");
        SoftAssertUtils.assertEquals(json.getString("data.onboarding.panStatus"), "PAN_AUTO_FETCHED", "Incorrect PAN status");
        SoftAssertUtils.assertEquals(json.getString("data.user.name"), "KARMUGELAN ANBAZHAGAN", "Incorrect user name");
        SoftAssertUtils.assertEquals(json.getString("data.user.mobile"), "9962063736", "Incorrect user mobile");
        SoftAssertUtils.assertEquals(json.getString("data.referral.referralCode"), "1Ie0g73", "Incorrect referral code");

        SoftAssertUtils.assertAll();
    }
}
