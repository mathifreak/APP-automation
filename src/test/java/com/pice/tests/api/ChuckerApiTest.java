package com.pice.tests.api;

import com.pice.api.ApiResponse;
import com.pice.base.BaseApiTest;
import com.pice.constants.TestGroups;
import com.pice.utils.SoftAssertUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * API Tests constructed from active inspect traffic in Alice Inspector.
 * Uses RestAssured framework via ApiClient to validate live staging endpoints.
 */
public class ChuckerApiTest extends BaseApiTest {

    @BeforeMethod(alwaysRun = true)
    public void setupHeaders() {
        // Base URL discovered from active inspector server requests
        apiClient = createClientFor("https://pre-credit.pice.one");
        
        // Staging credentials and device context headers extracted from the inspect logs
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

    @Test(groups = {TestGroups.SMOKE, "api"})
    public void testGetDashboard() {
        ApiResponse response = apiClient.get("/home/dashboard");
        SoftAssertUtils.assertEquals(response.getStatusCode(), 200, "Dashboard API should return 200 OK");
        SoftAssertUtils.assertTrue(response.getBodyAsString().contains("recentContacts"), "Response should contain recentContacts");
        SoftAssertUtils.assertAll();
    }

    @Test(groups = {TestGroups.SMOKE, "api"})
    public void testGetCardList() {
        ApiResponse response = apiClient.get("/bbps/bill-account/card/list");
        SoftAssertUtils.assertEquals(response.getStatusCode(), 200, "Card List API should return 200 OK");
        SoftAssertUtils.assertTrue(response.getBodyAsString().contains("ccAccounts"), "Response should contain ccAccounts");
        SoftAssertUtils.assertAll();
    }

    @Test(groups = {TestGroups.SMOKE, "api"})
    public void testGetOnboardStatus() {
        ApiResponse response = apiClient.get("/onboard/status");
        SoftAssertUtils.assertEquals(response.getStatusCode(), 200, "Onboard Status API should return 200 OK");
        SoftAssertUtils.assertTrue(response.getBodyAsString().contains("meta"), "Response should contain meta object");
        SoftAssertUtils.assertAll();
    }
}
