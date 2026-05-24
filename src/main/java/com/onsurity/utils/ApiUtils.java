package com.onsurity.utils;

import com.onsurity.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

/**
 * REST API utility for test data setup.
 *
 * <p>Primary use case: Onboarding fresh member accounts before E2E tests
 * to avoid "Active Membership Detected" and stale data issues.
 *
 * <p><b>Phone Number Strategy:</b>
 * Uses an auto-incrementing counter starting from {@code 6000010000}.
 * The counter is persisted to a file so each test run gets a unique phone number.
 *
 * <p><b>Configuration (staging.properties):</b>
 * <ul>
 *   <li>{@code api.invite.base.url} — Base URL for partner integration API</li>
 *   <li>{@code api.invite.account.id} — x-ons-account-id header value</li>
 *   <li>{@code api.invite.ignore.signature} — Skip signature validation (default: true)</li>
 *   <li>{@code api.phone.counter.file} — Path to counter persistence file</li>
 * </ul>
 */
public class ApiUtils {

    private static final Logger log = LogManager.getLogger(ApiUtils.class);

    // Phone number counter — starts at 6000010000, increments by 1 per invocation
    private static final long PHONE_NUMBER_BASE = 7000900000L;
    private static final AtomicLong phoneCounter = new AtomicLong(-1); // -1 = not initialized

    // Counter persistence file (default location)
    private static final String DEFAULT_COUNTER_FILE = "src/test/resources/config/.phone_counter";

    // ==================== Phone Number Management ====================

    /**
     * Generate the next unique phone number in the 60000xxxxx series.
     * Thread-safe and persisted across test runs.
     *
     * @return a 10-digit phone number string like "6000010001"
     */
    public static synchronized String getNextPhoneNumber() {
        if (phoneCounter.get() == -1) {
            loadCounter();
        }
        long nextValue = phoneCounter.incrementAndGet();
        saveCounter(nextValue);

        String phone = String.valueOf(PHONE_NUMBER_BASE + nextValue);
        log.info("Generated next phone number: {} (counter={})", phone, nextValue);
        return phone;
    }

    /**
     * Get the current phone number without incrementing.
     * Useful for referencing the last created user.
     *
     * @return the current phone number, or the base number if none generated yet
     */
    public static String getCurrentPhoneNumber() {
        if (phoneCounter.get() == -1) {
            loadCounter();
        }
        long currentValue = phoneCounter.get();
        return String.valueOf(PHONE_NUMBER_BASE + Math.max(0, currentValue));
    }

    // ==================== Member Onboarding API ====================

    /** Maximum retry attempts to find an unused phone number. */
    private static final int MAX_INVITE_RETRIES = 50;

    /**
     * Invite (onboard) a new member via the partner integration API.
     * Keeps incrementing the phone counter and retrying until the API
     * returns {@code isSuccess: true}. This ensures we always get a
     * fresh, unused phone number even if some numbers in the series
     * are already taken.
     *
     * @return the successfully onboarded phone number
     * @throws RuntimeException if max retries exhausted without success
     */
    public static String inviteNewMember() {
        for (int attempt = 1; attempt <= MAX_INVITE_RETRIES; attempt++) {
            String phone = getNextPhoneNumber();
            log.info("Invite attempt {}/{} — trying phone: {}", attempt, MAX_INVITE_RETRIES, phone);

            boolean success = inviteMember(phone, "Automation user", "27/10/1989", "Male");
            if (success) {
                log.info("Member onboarded successfully on attempt {} — phone: {}", attempt, phone);
                return phone;
            }

            log.warn("Phone {} already exists or invite failed — incrementing counter and retrying", phone);
        }

        throw new RuntimeException("Failed to onboard a new member after " + MAX_INVITE_RETRIES
                + " attempts. All phone numbers in range may be exhausted.");
    }

    /**
     * Invite a member with a specific phone number.
     *
     * @param phoneNumber 10-digit phone number
     * @param name        member name
     * @param dob         date of birth (DD/MM/YYYY)
     * @param gender      "Male" or "Female"
     * @return true if the API returned isSuccess=true, false otherwise
     */
    public static boolean inviteMember(String phoneNumber, String name, String dob, String gender) {
        String baseUrl = ConfigManager.get("api.invite.base.url",
                "https://your-api-base-url.example.com");
        String accountId = ConfigManager.get("api.invite.account.id", "OSHE-1779195233573");
        boolean ignoreSignature = Boolean.parseBoolean(
                ConfigManager.get("api.invite.ignore.signature", "true"));

        String endpoint = baseUrl + "/partners-integration/api/invite-member";

        // Build request body
        String requestBody = buildInviteMemberBody(phoneNumber, name, dob, gender);

        log.info("Inviting member via API — phone: {}, endpoint: {}", phoneNumber, endpoint);
        log.debug("Request body: {}", requestBody);

        try {
            long timestamp = System.currentTimeMillis() / 1000;

            Response response = RestAssured.given()
                    .header("Content-Type", "application/json")
                    .header("x-ons-account-id", accountId)
                    .header("x-ons-timestamp", String.valueOf(timestamp))
                    .header("x-ons-signature", "automation")
                    .header("ignore-signature", String.valueOf(ignoreSignature))
                    .header("source", "")
                    .body(requestBody)
                    .when()
                    .post(endpoint);

            int statusCode = response.getStatusCode();
            String responseBody = response.getBody().asString();

            log.info("Invite API response — HTTP {}, body: {}", statusCode, responseBody);

            // Parse isSuccess from JSON response
            boolean isSuccess = response.jsonPath().getBoolean("meta.isSuccess");
            String responseMessage = response.jsonPath().getString("meta.responseMessage");

            if (isSuccess) {
                log.info("✅ SUCCESS — phone: {}, message: {}", phoneNumber, responseMessage);
                return true;
            } else {
                log.warn("❌ FAILED — phone: {}, message: {}", phoneNumber, responseMessage);
                return false;
            }

        } catch (Exception e) {
            log.error("Member invite API call failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Onboard a fresh member and return the phone number + OTP for login.
     * This is the main entry point for tests that need a clean user.
     * Automatically retries with incrementing phone numbers until success.
     *
     * @return String array: [phoneNumber, otp] — OTP is always "3548" for QA
     */
    public static String[] onboardFreshMemberForTest() {
        String phone = inviteNewMember();

        String otp = ConfigManager.get("offers.test.otp", "3548");
        log.info("Fresh member ready — phone: {}, otp: {}", phone, otp);
        return new String[]{phone, otp};
    }

    // ==================== Request Body Builder ====================

    /**
     * Build the JSON request body for the invite-member API.
     */
    private static String buildInviteMemberBody(String phoneNumber, String name, String dob, String gender) {
        return "{\n" +
                "    \"employeeDetails\": {\n" +
                "        \"name\": \"" + name + "\",\n" +
                "        \"phoneNumber\": \"" + phoneNumber + "\",\n" +
                "        \"emailId\": \"\",\n" +
                "        \"dob\": \"" + dob + "\",\n" +
                "        \"gender\": \"" + gender + "\",\n" +
                "        \"employeeNo\": \"EMP" + phoneNumber.substring(phoneNumber.length() - 4) + "\",\n" +
                "        \"designation\": \"Engineer\",\n" +
                "        \"income\": 500000,\n" +
                "        \"nomineeDetails\": {\n" +
                "            \"name\": \"Jane Doe\",\n" +
                "            \"relation\": \"spouse\",\n" +
                "            \"dob\": \"15/06/1992\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"healthPlan\": {\n" +
                "        \"planName\": \"SelfJade\",\n" +
                "        \"policyType\": \"1A\"\n" +
                "    },\n" +
                "    \"dependents\": []\n" +
                "}";
    }

    // ==================== Counter Persistence ====================

    /**
     * Load the counter from the persistence file.
     * If the file doesn't exist, starts from 0.
     */
    private static void loadCounter() {
        String counterFile = ConfigManager.get("api.phone.counter.file", DEFAULT_COUNTER_FILE);
        Path path = Paths.get(counterFile);

        try {
            if (Files.exists(path)) {
                String content = Files.readString(path).trim();
                long value = Long.parseLong(content);
                phoneCounter.set(value);
                log.info("Phone counter loaded from file: {} (value={})", counterFile, value);
            } else {
                phoneCounter.set(0);
                log.info("Phone counter file not found — starting from 0");
            }
        } catch (Exception e) {
            log.warn("Failed to load phone counter from {}: {} — starting from 0", counterFile, e.getMessage());
            phoneCounter.set(0);
        }
    }

    /**
     * Save the counter to the persistence file.
     */
    private static void saveCounter(long value) {
        String counterFile = ConfigManager.get("api.phone.counter.file", DEFAULT_COUNTER_FILE);
        Path path = Paths.get(counterFile);

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, String.valueOf(value));
            log.debug("Phone counter saved: {} (value={})", counterFile, value);
        } catch (IOException e) {
            log.warn("Failed to save phone counter to {}: {}", counterFile, e.getMessage());
        }
    }
}
