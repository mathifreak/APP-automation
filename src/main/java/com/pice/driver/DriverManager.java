package com.pice.driver;

import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Thread-safe driver manager using ThreadLocal.
 * Ensures parallel test execution doesn't cause driver instance contention.
 *
 * <p>Usage:
 * <pre>
 *   DriverManager.setDriver(driver);
 *   AppiumDriver driver = DriverManager.getDriver();
 *   DriverManager.removeDriver(); // Quits and cleans up
 * </pre>
 */
public final class DriverManager {

    private static final Logger log = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();

    private DriverManager() {
        // Prevent instantiation
    }

    /**
     * Get the AppiumDriver for the current thread.
     *
     * @return the AppiumDriver instance bound to this thread
     * @throws IllegalStateException if no driver is set for this thread
     */
    public static AppiumDriver getDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "No AppiumDriver found for thread [" + Thread.currentThread().getName()
                            + "]. Call DriverManager.setDriver() first.");
        }
        return driver;
    }

    /**
     * Set the AppiumDriver for the current thread.
     *
     * @param driver the AppiumDriver instance to bind
     */
    public static void setDriver(AppiumDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }
        log.info("Setting AppiumDriver for thread [{}]", Thread.currentThread().getName());
        driverThreadLocal.set(driver);
    }

    /**
     * Quit the driver and remove it from the current thread.
     * Safe to call even if no driver is set.
     */
    public static void removeDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                log.info("Quitting AppiumDriver for thread [{}]", Thread.currentThread().getName());
                driver.quit();
            } catch (Exception e) {
                log.warn("Error quitting driver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    /**
     * Check if a driver is available for the current thread.
     *
     * @return true if a driver is set
     */
    public static boolean hasDriver() {
        return driverThreadLocal.get() != null;
    }
}
