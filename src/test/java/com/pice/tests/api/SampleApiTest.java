package com.pice.tests.api;

import com.pice.api.ApiResponse;
import com.pice.base.BaseApiTest;
import com.pice.constants.TestGroups;
import com.pice.utils.SoftAssertUtils;
import org.testng.annotations.Test;

/**
 * Sample API test to demonstrate the REST Assured integration.
 */
public class SampleApiTest extends BaseApiTest {

    @Test(groups = {TestGroups.SMOKE, "api"})
    public void verifyPublicApiEndpoint() {
        // We override the default API client for this public mock API test
        apiClient = createClientFor("https://jsonplaceholder.typicode.com");

        // 1. Make GET request
        ApiResponse response = apiClient.get("/posts/1");

        // 2. Verify status code
        SoftAssertUtils.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 OK");

        // 3. Verify response body contains expected fields
        String body = response.getBodyAsString();
        SoftAssertUtils.assertTrue(body.contains("userId"), "Response should contain userId");
        SoftAssertUtils.assertTrue(body.contains("title"), "Response should contain title");

        // 4. Assert all soft assertions
        SoftAssertUtils.assertAll();
    }
}
