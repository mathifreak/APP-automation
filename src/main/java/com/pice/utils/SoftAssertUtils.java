package com.pice.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.asserts.SoftAssert;

/**
 * Thread-safe Soft Assertion utility.
 * Collects all assertion failures within a test and reports them together
 * at the end, instead of failing on the first assertion.
 *
 * <p>Ideal for UI verification tests where you want to check multiple
 * elements on a screen and report ALL missing elements, not just the first one.
 *
 * <p>Usage:
 * <pre>
 * SoftAssertUtils.init();
 * SoftAssertUtils.assertTrue(condition, "Title should be visible");
 * SoftAssertUtils.assertEquals(actual, expected, "Label mismatch");
 * SoftAssertUtils.assertAll(); // Throws if any assertion failed
 * </pre>
 */
public final class SoftAssertUtils {

    private static final Logger log = LogManager.getLogger(SoftAssertUtils.class);
    private static final ThreadLocal<SoftAssert> softAssertThreadLocal = new ThreadLocal<>();

    private SoftAssertUtils() {
        // Prevent instantiation
    }

    /**
     * Initialize a new SoftAssert for the current thread/test.
     * Must be called at the beginning of each test that uses soft assertions.
     */
    public static void init() {
        softAssertThreadLocal.set(new SoftAssert());
        log.debug("SoftAssert initialized for thread [{}]", Thread.currentThread().getName());
    }

    /**
     * Get the current thread's SoftAssert instance.
     */
    private static SoftAssert get() {
        SoftAssert sa = softAssertThreadLocal.get();
        if (sa == null) {
            log.warn("SoftAssert not initialized. Call SoftAssertUtils.init() first. Auto-initializing.");
            sa = new SoftAssert();
            softAssertThreadLocal.set(sa);
        }
        return sa;
    }

    public static void assertTrue(boolean condition, String message) {
        get().assertTrue(condition, message);
    }

    public static void assertFalse(boolean condition, String message) {
        get().assertFalse(condition, message);
    }

    public static void assertEquals(Object actual, Object expected, String message) {
        get().assertEquals(actual, expected, message);
    }

    public static void assertNotNull(Object object, String message) {
        get().assertNotNull(object, message);
    }

    /**
     * Assert all collected assertions. Throws AssertionError if any failed.
     * Also cleans up the ThreadLocal.
     */
    public static void assertAll() {
        try {
            get().assertAll();
        } finally {
            softAssertThreadLocal.remove();
        }
    }
}
