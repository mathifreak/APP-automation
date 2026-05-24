package com.onsurity.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onsurity.driver.DeviceType;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

/**
 * Builds platform-specific Appium capabilities from JSON configuration files.
 *
 * <p>React Native specific: ensures accessibility IDs are enabled for both platforms,
 * which maps to the `testID` prop in React Native components.
 *
 * <p>Capability files:
 * <ul>
 *   <li>capabilities/android.json — Android UiAutomator2 capabilities</li>
 *   <li>capabilities/ios.json — iOS XCUITest capabilities</li>
 * </ul>
 *
 * <p><b>Design:</b> JSON is the source of truth. This class only sets defaults for
 * capabilities NOT already defined in the JSON file. This allows full control
 * from the JSON config without Java overrides.
 */
public final class AppCapabilities {

    private static final Logger log = LogManager.getLogger(AppCapabilities.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private AppCapabilities() {
        // Prevent instantiation
    }

    /**
     * Build capabilities for the specified device type.
     *
     * @param deviceType ANDROID or IOS
     * @return platform-specific MutableCapabilities
     */
    public static MutableCapabilities getCapabilities(DeviceType deviceType) {
        return switch (deviceType) {
            case ANDROID -> buildAndroidCapabilities();
            case IOS -> buildIOSCapabilities();
        };
    }

    /**
     * Build Android UiAutomator2 capabilities.
     * JSON values take precedence — defaults are only applied if not present in JSON.
     */
    private static UiAutomator2Options buildAndroidCapabilities() {
        UiAutomator2Options options = new UiAutomator2Options();
        JsonNode config = loadCapabilitiesFile("capabilities/android.json");

        if (config != null) {
            applyJsonCapabilities(options, config);
        }

        // Defaults — only applied if NOT already set from JSON
        setIfAbsent(options, "appium:automationName", "UiAutomator2");
        setIfAbsent(options, "appium:autoGrantPermissions", true);
        setIfAbsent(options, "appium:noReset", false);
        setIfAbsent(options, "appium:fullReset", false);
        setIfAbsent(options, "appium:chromedriverAutodownload", true);
        // Optional: Set a specific ChromeDriver path for WebView interaction
        String chromedriverPath = Path.of("src/test/resources/chromedriver/chromedriver").toAbsolutePath().toString();
        if (Path.of(chromedriverPath).toFile().exists()) {
            setIfAbsent(options, "appium:chromedriverExecutable", chromedriverPath);
            log.info("ChromeDriver set: {}", chromedriverPath);
        }

        if (options.getCapability("appium:newCommandTimeout") == null) {
            options.setNewCommandTimeout(Duration.ofSeconds(300));
        }

        // Set app path — APK file is MANDATORY for test execution
        String appPath = ConfigManager.getAndroidAppPath();
        if (appPath == null || appPath.isBlank()) {
            throw new RuntimeException(
                    "APK path not configured! Set 'android.app.path' in staging.properties. " +
                    "Expected: src/test/resources/apps/app-release.apk");
        }
        Path resolvedPath = Path.of(appPath).toAbsolutePath();
        if (!resolvedPath.toFile().exists()) {
            throw new RuntimeException(
                    "APK file NOT FOUND at: " + resolvedPath + "\n" +
                    "Place the APK at: src/test/resources/apps/app-release.apk\n" +
                    "Tests cannot run without the APK file.");
        }
        options.setApp(resolvedPath.toString());
        log.info("APK set: {} ({} MB)", resolvedPath,
                resolvedPath.toFile().length() / (1024 * 1024));

        log.info("Android capabilities built: {}", options.toJson());
        return options;
    }

    /**
     * Build iOS XCUITest capabilities.
     * JSON values take precedence — defaults are only applied if not present in JSON.
     */
    private static XCUITestOptions buildIOSCapabilities() {
        XCUITestOptions options = new XCUITestOptions();
        JsonNode config = loadCapabilitiesFile("capabilities/ios.json");

        if (config != null) {
            applyJsonCapabilities(options, config);
        }

        // Defaults — only applied if NOT already set from JSON
        setIfAbsent(options, "appium:automationName", "XCUITest");
        setIfAbsent(options, "appium:noReset", false);
        setIfAbsent(options, "appium:fullReset", false);
        setIfAbsent(options, "appium:usePrebuiltWDA", true);

        if (options.getCapability("appium:newCommandTimeout") == null) {
            options.setNewCommandTimeout(Duration.ofSeconds(300));
        }

        // Set app path
        String appPath = ConfigManager.getIosAppPath();
        if (appPath != null && !appPath.isBlank()) {
            options.setApp(Path.of(appPath).toAbsolutePath().toString());
        }

        log.info("iOS capabilities built: {}", options.toJson());
        return options;
    }

    /**
     * Load capabilities JSON file from classpath.
     */
    private static JsonNode loadCapabilitiesFile(String resourcePath) {
        try (InputStream is = AppCapabilities.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.warn("Capabilities file not found: {}. Using defaults.", resourcePath);
                return null;
            }
            return objectMapper.readTree(is);
        } catch (Exception e) {
            log.error("Failed to parse capabilities file: {}", resourcePath, e);
            return null;
        }
    }

    /**
     * Apply JSON key-value pairs as capabilities.
     */
    private static void applyJsonCapabilities(MutableCapabilities options, JsonNode config) {
        Iterator<Map.Entry<String, JsonNode>> fields = config.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (value.isTextual()) {
                options.setCapability(key, value.asText());
            } else if (value.isBoolean()) {
                options.setCapability(key, value.asBoolean());
            } else if (value.isInt()) {
                options.setCapability(key, value.asInt());
            } else if (value.isDouble()) {
                options.setCapability(key, value.asDouble());
            }
        }
    }

    /**
     * Set a capability only if it has not already been set (from JSON config).
     * This ensures JSON is the source of truth.
     */
    private static void setIfAbsent(MutableCapabilities options, String key, Object value) {
        if (options.getCapability(key) == null) {
            options.setCapability(key, value);
        }
    }
}
