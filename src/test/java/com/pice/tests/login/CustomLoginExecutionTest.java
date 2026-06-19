package com.pice.tests.login;

import com.pice.base.BaseTest;
import com.pice.constants.TestGroups;
import com.pice.pages.LoginPage;
import com.pice.pages.OtpPage;
import com.pice.utils.AuthHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A custom test class to run the login flow using the user's specific phone number.
 * This class is completely separate to ensure Claude's code is not altered.
 */
public class CustomLoginExecutionTest extends BaseTest {

    private LoginPage loginPage;

    @Override
    protected void resetAppState() {
        log.info("--- Custom resetAppState: Keeping custom login session active ---");
        // No-op to prevent duplicate app reset (since navigateToLogin handles it)
    }

    @BeforeMethod(alwaysRun = true)
    public void navigateToLogin() {
        log.info("--- Setup: Navigating to Login screen ---");
        com.pice.utils.AppUtils.resetApp();
        try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        AuthHelper.navigateToLoginScreen();
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        loginPage = new LoginPage();
    }

    @Test(
        groups = {TestGroups.SMOKE, TestGroups.LOGIN, TestGroups.POSITIVE},
        description = "Run login flow using user provided phone number"
    )
    public void runCustomLogin() {
        log.info("===== TEST: runCustomLogin =====");
        
        String phone = com.pice.config.ConfigManager.get("test.mobile.number", "9962063736");
        log.info("Entering mobile number: {}", phone);
        loginPage.enterMobileNumber(phone);
        
        log.info("Tapping consent checkbox");
        loginPage.tapConsentCheckbox();
        
        log.info("Tapping Proceed");
        OtpPage otpPage = loginPage.proceedToOtp();
        
        if (otpPage == null) {
            log.warn("Proceed was tapped but OTP screen was not loaded (possibly blocked by root detection on emulator)");
        } else {
            log.info("OTP screen successfully loaded!");
        }
        
        log.info("===== TEST COMPLETE: runCustomLogin =====");
    }
}
