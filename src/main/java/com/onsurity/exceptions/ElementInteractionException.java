package com.onsurity.exceptions;

/**
 * Thrown when an element interaction (tap, type, scroll) fails.
 * Wraps Selenium/Appium exceptions with contextual info about which element and action failed.
 */
public class ElementInteractionException extends FrameworkException {

    public ElementInteractionException(String action, String element) {
        super("Failed to " + action + " on element: " + element);
    }

    public ElementInteractionException(String action, String element, Throwable cause) {
        super("Failed to " + action + " on element: " + element + ". " + cause.getMessage(), cause);
    }
}
