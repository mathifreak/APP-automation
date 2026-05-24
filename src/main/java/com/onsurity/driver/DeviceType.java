package com.onsurity.driver;

/**
 * Enum representing the supported mobile platforms.
 * Used by DriverFactory to instantiate the correct driver.
 */
public enum DeviceType {
    ANDROID,
    IOS;

    /**
     * Parse device type from string (case-insensitive).
     *
     * @param platform the platform string (e.g., "android", "ios")
     * @return the corresponding DeviceType
     * @throws IllegalArgumentException if the platform is not supported
     */
    public static DeviceType fromString(String platform) {
        if (platform == null || platform.isBlank()) {
            throw new IllegalArgumentException("Platform cannot be null or empty");
        }
        return switch (platform.trim().toUpperCase()) {
            case "ANDROID" -> ANDROID;
            case "IOS" -> IOS;
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }
}
