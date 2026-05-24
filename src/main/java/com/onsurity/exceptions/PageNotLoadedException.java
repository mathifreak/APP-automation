package com.onsurity.exceptions;

/**
 * Thrown when a page object fails its load validation check.
 * Indicates the app navigated to an unexpected screen.
 *
 * <p>Example: Instantiating {@code HomePage} but the login screen is still visible.
 */
public class PageNotLoadedException extends FrameworkException {

    public PageNotLoadedException(String pageName) {
        super("Page not loaded: " + pageName + ". Expected screen is not visible.");
    }

    public PageNotLoadedException(String pageName, String detail) {
        super("Page not loaded: " + pageName + ". " + detail);
    }

    public PageNotLoadedException(String pageName, Throwable cause) {
        super("Page not loaded: " + pageName + ". " + cause.getMessage(), cause);
    }
}
