package com.pice.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration manager.
 * Loads environment-specific properties (staging, production) and
 * provides type-safe access to configuration values.
 *
 * <p>Load order:
 * <ol>
 *   <li>config.properties (global defaults)</li>
 *   <li>{env}.properties (environment overrides)</li>
 *   <li>System properties (CLI overrides via -D flags)</li>
 * </ol>
 */
public final class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();
    private static boolean initialized = false;

    private ConfigManager() {
        // Prevent instantiation
    }

    /**
     * Initialize configuration by loading global + environment-specific properties.
     * System properties (-D flags) override file-based properties.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }

        // 1. Load global config
        loadProperties("config/config.properties");

        // 2. Load environment-specific config (overrides global)
        String env = System.getProperty("env", properties.getProperty("env", "staging"));
        loadProperties("config/" + env + ".properties");

        // 3. System properties override everything
        properties.putAll(System.getProperties());

        initialized = true;
        log.info("Configuration initialized for environment: {}", env);
    }

    private static void loadProperties(String resourcePath) {
        try (InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                properties.load(is);
                log.debug("Loaded properties from: {}", resourcePath);
            } else {
                log.warn("Properties file not found: {}", resourcePath);
            }
        } catch (IOException e) {
            log.error("Failed to load properties from: {}", resourcePath, e);
        }
    }

    // ==================== Getters ====================

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Set a configuration property at runtime.
     * Useful for dynamically updating values (e.g., API-onboarded phone numbers).
     *
     * @param key   the property key
     * @param value the property value
     */
    public static void set(String key, String value) {
        properties.setProperty(key, value);
        log.info("Config updated: {} = {}", key, value);
    }

    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer for key '{}': '{}'. Using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value.trim());
    }

    // ==================== Convenience Accessors ====================

    public static String getAppiumServerUrl() {
        return get("appium.server.url", "http://127.0.0.1:4723");
    }

    public static String getPlatform() {
        return get("platform", "android");
    }

    public static String getEnvironment() {
        return get("env", "staging");
    }

    public static int getExplicitWait() {
        return getInt("explicit.wait.seconds", 15);
    }

    public static int getImplicitWait() {
        return getInt("implicit.wait.seconds", 10);
    }

    public static String getAndroidAppPath() {
        return get("android.app.path", "src/test/resources/apps/app-staging.apk");
    }

    public static String getIosAppPath() {
        return get("ios.app.path", "src/test/resources/apps/app-staging.ipa");
    }

    public static boolean isCloudExecution() {
        return getBoolean("cloud.execution", false);
    }

    public static String getCloudUrl() {
        return get("cloud.url", "");
    }

    public static String getCloudUsername() {
        return get("cloud.username", "");
    }

    public static String getCloudAccessKey() {
        return get("cloud.access.key", "");
    }

    public static int getRetryCount() {
        return getInt("retry.count", 1);
    }

    /**
     * Reset configuration (useful for testing).
     */
    public static synchronized void reset() {
        properties.clear();
        initialized = false;
    }
}
