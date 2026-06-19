package com.pice.driver;

import com.pice.config.AppCapabilities;
import com.pice.config.ConfigManager;
import com.pice.exceptions.FrameworkException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Factory class that creates platform-specific Appium driver instances.
 * Supports both local and cloud (BrowserStack/Sauce Labs) execution.
 *
 * <p>For React Native apps, both Android and iOS drivers can locate
 * elements using accessibility IDs which map to the `testID` prop.
 *
 * <p>Post-creation, the driver is configured with:
 * <ul>
 *   <li>Implicit wait from {@code implicit.wait.seconds} config</li>
 * </ul>
 */
public final class DriverFactory {

    private static final Logger log = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {
        // Prevent instantiation
    }

    /**
     * Create an AppiumDriver for the specified device type.
     *
     * @param deviceType ANDROID or IOS
     * @return a configured AppiumDriver instance
     */
    public static AppiumDriver createDriver(DeviceType deviceType) {
        MutableCapabilities capabilities = AppCapabilities.getCapabilities(deviceType);
        URL serverUrl = getServerUrl();

        log.info("Creating {} driver connecting to: {}", deviceType, serverUrl);

        AppiumDriver driver = switch (deviceType) {
            case ANDROID -> new AndroidDriver(serverUrl, capabilities);
            case IOS -> new IOSDriver(serverUrl, capabilities);
        };

        // Post-creation configuration
        configureDriver(driver);

        log.info("{} driver created successfully. Session ID: {}", deviceType, driver.getSessionId());
        return driver;
    }

    /**
     * Create a driver based on the platform configured in properties.
     *
     * @return a configured AppiumDriver instance
     */
    public static AppiumDriver createDriver() {
        String platform = ConfigManager.getPlatform();
        DeviceType deviceType = DeviceType.fromString(platform);
        return createDriver(deviceType);
    }

    /**
     * Apply post-creation driver configuration.
     * Sets implicit wait, timeouts, and other driver-level settings from config.
     */
    private static void configureDriver(AppiumDriver driver) {
        int implicitWaitSeconds = ConfigManager.getImplicitWait();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSeconds));
        log.debug("Implicit wait set to {}s", implicitWaitSeconds);
    }

    /**
     * Resolve the Appium server URL.
     * Uses cloud URL if cloud execution is enabled, otherwise local.
     */
    private static URL getServerUrl() {
        String urlString;

        if (ConfigManager.isCloudExecution()) {
            urlString = ConfigManager.getCloudUrl();
            log.info("Using cloud execution URL: {}", urlString);
        } else {
            urlString = ConfigManager.getAppiumServerUrl();
            log.info("Using local Appium server: {}", urlString);
        }

        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new FrameworkException("Invalid Appium server URL: " + urlString, e);
        }
    }
}
