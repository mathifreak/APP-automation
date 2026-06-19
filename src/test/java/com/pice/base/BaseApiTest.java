package com.pice.base;

import com.pice.api.ApiClient;
import com.pice.utils.SoftAssertUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Base class for all API automation tests.
 * Initializes SoftAsserts and provides a default ApiClient.
 * This class DOES NOT launch an Appium session, keeping API tests fast.
 */
@Listeners(com.pice.listeners.ExtentReportListener.class)
public abstract class BaseApiTest {

    protected ApiClient apiClient;

    @BeforeMethod(alwaysRun = true)
    public void setupApiTest() {
        SoftAssertUtils.init();
        apiClient = new ApiClient(); // Uses default base URL from config
    }
    
    /**
     * Optional: Creates a new ApiClient with a specific base URL for 3rd party APIs.
     */
    protected ApiClient createClientFor(String baseUrl) {
        return new ApiClient(baseUrl);
    }
}
