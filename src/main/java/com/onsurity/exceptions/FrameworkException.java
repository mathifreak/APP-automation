package com.onsurity.exceptions;

/**
 * Base exception for all framework-level errors.
 * Distinguishes framework issues (driver, config, setup) from test assertion failures.
 *
 * <p>Hierarchy:
 * <pre>
 *   FrameworkException
 *   ├── PageNotLoadedException
 *   └── ElementInteractionException
 * </pre>
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
