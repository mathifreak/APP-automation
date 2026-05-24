package com.onsurity.listeners;

import com.onsurity.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * TestNG retry analyzer for automatically retrying failed tests.
 *
 * <p>Mobile tests are inherently flaky due to device state, network conditions,
 * and animation timing. This analyzer provides a safety net by retrying
 * failed tests a configurable number of times.
 *
 * <p>Usage in test class:
 * <pre>
 * {@code @Test(retryAnalyzer = RetryAnalyzer.class)}
 * public void testLogin() { ... }
 * </pre>
 *
 * <p>Or configure globally via testng.xml listener.
 * Retry count is configurable via `retry.count` property (default: 1).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetryCount = ConfigManager.getRetryCount();

        if (retryCount < maxRetryCount) {
            retryCount++;
            log.warn("Retrying test '{}' — attempt {}/{} due to: {}",
                    result.getMethod().getMethodName(),
                    retryCount,
                    maxRetryCount,
                    result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown error");
            return true;
        }
        return false;
    }
}
