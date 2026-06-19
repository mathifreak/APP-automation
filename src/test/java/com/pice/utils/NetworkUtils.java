package com.pice.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ADB-based network control utility for login/OTP network edge case tests.
 *
 * <p>Provides airplane mode toggle and WiFi control via ADB shell commands.
 * Used by {@code OtpValidationTest} to simulate no-network scenarios.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * NetworkUtils.enableAirplaneMode("10MG56FM6E000FD");
 * // ... test network-failure behaviour ...
 * NetworkUtils.disableAirplaneMode("10MG56FM6E000FD");
 * }</pre>
 *
 * <p><b>Note:</b> ADB shell airplane mode changes require broadcasting an intent
 * AND setting the global setting. Both are done here for reliability.
 */
public class NetworkUtils {

    private static final Logger log = LogManager.getLogger(NetworkUtils.class);

    // Prevent instantiation
    private NetworkUtils() {}

    // ==================== Airplane Mode ====================

    /**
     * Enable airplane mode on the device.
     * This disconnects all radios (WiFi, cellular, Bluetooth).
     * Tests should call {@link #disableAirplaneMode} in their @AfterMethod to restore connectivity.
     *
     * @param deviceSerial the ADB device serial (e.g., "10MG56FM6E000FD")
     */
    public static void enableAirplaneMode(String deviceSerial) {
        log.info("Enabling airplane mode on device: {}", deviceSerial);
        try {
            // Step 1: Set the global setting
            runAdb(deviceSerial, "shell", "settings", "put", "global", "airplane_mode_on", "1");
            // Step 2: Broadcast the change so the OS applies it immediately
            runAdb(deviceSerial, "shell", "am", "broadcast",
                    "-a", "android.intent.action.AIRPLANE_MODE", "--ez", "state", "true");
            Thread.sleep(2000); // Allow connectivity to drop
            log.info("Airplane mode ENABLED on device: {}", deviceSerial);
        } catch (Exception e) {
            log.error("Failed to enable airplane mode: {}", e.getMessage());
        }
    }

    /**
     * Disable airplane mode on the device and restore network connectivity.
     *
     * @param deviceSerial the ADB device serial
     */
    public static void disableAirplaneMode(String deviceSerial) {
        log.info("Disabling airplane mode on device: {}", deviceSerial);
        try {
            // Step 1: Clear the global setting
            runAdb(deviceSerial, "shell", "settings", "put", "global", "airplane_mode_on", "0");
            // Step 2: Broadcast the change
            runAdb(deviceSerial, "shell", "am", "broadcast",
                    "-a", "android.intent.action.AIRPLANE_MODE", "--ez", "state", "false");
            Thread.sleep(3000); // Allow network to reconnect
            log.info("Airplane mode DISABLED on device: {}", deviceSerial);
        } catch (Exception e) {
            log.error("Failed to disable airplane mode: {}", e.getMessage());
        }
    }

    // ==================== WiFi ====================

    /**
     * Enable WiFi on the device.
     *
     * @param deviceSerial the ADB device serial
     */
    public static void enableWifi(String deviceSerial) {
        log.info("Enabling WiFi on device: {}", deviceSerial);
        try {
            runAdb(deviceSerial, "shell", "svc", "wifi", "enable");
            Thread.sleep(2000);
            log.info("WiFi ENABLED on device: {}", deviceSerial);
        } catch (Exception e) {
            log.error("Failed to enable WiFi: {}", e.getMessage());
        }
    }

    /**
     * Disable WiFi on the device.
     *
     * @param deviceSerial the ADB device serial
     */
    public static void disableWifi(String deviceSerial) {
        log.info("Disabling WiFi on device: {}", deviceSerial);
        try {
            runAdb(deviceSerial, "shell", "svc", "wifi", "disable");
            Thread.sleep(2000);
            log.info("WiFi DISABLED on device: {}", deviceSerial);
        } catch (Exception e) {
            log.error("Failed to disable WiFi: {}", e.getMessage());
        }
    }

    // ==================== Status ====================

    /**
     * Check if the device has an active network connection.
     * Uses ADB to ping the DNS server as a quick connectivity probe.
     *
     * @param deviceSerial the ADB device serial
     * @return true if network is available, false otherwise
     */
    public static boolean isNetworkAvailable(String deviceSerial) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{
                    "adb", "-s", deviceSerial, "shell",
                    "ping", "-c", "1", "-W", "3", "8.8.8.8"
            });
            int exitCode = p.waitFor();
            boolean available = (exitCode == 0);
            log.info("Network available on device {}: {}", deviceSerial, available);
            return available;
        } catch (Exception e) {
            log.warn("Network availability check failed: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Private Helpers ====================

    /**
     * Execute an ADB command against a specific device.
     *
     * @param deviceSerial ADB device serial
     * @param args         ADB command arguments (without "adb -s <serial>")
     */
    private static void runAdb(String deviceSerial, String... args) throws Exception {
        String[] cmd = new String[2 + args.length];
        cmd[0] = "adb";
        cmd[1] = "-s";
        // Shift: insert deviceSerial as 3rd element, then append args
        String[] fullCmd = new String[3 + args.length];
        fullCmd[0] = "adb";
        fullCmd[1] = "-s";
        fullCmd[2] = deviceSerial;
        System.arraycopy(args, 0, fullCmd, 3, args.length);

        Process p = Runtime.getRuntime().exec(fullCmd);
        int exit = p.waitFor();
        if (exit != 0) {
            log.warn("ADB command returned non-zero exit code {}: {}", exit, String.join(" ", fullCmd));
        }
    }
}
