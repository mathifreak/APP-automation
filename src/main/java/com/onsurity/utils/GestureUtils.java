package com.onsurity.utils;

import com.onsurity.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.List;

/**
 * Mobile gesture utilities using W3C Actions API.
 * Provides swipe, scroll, tap, long press, and drag-and-drop gestures.
 *
 * <p>All gestures are platform-agnostic and work with both Android and iOS.
 */
public final class GestureUtils {

    private static final Logger log = LogManager.getLogger(GestureUtils.class);
    private static final PointerInput FINGER = new PointerInput(PointerInput.Kind.TOUCH, "finger");

    private GestureUtils() {
        // Prevent instantiation
    }

    // ==================== Swipe Gestures ====================

    /**
     * Swipe up on the screen (scroll content down).
     */
    public static void swipeUp() {
        swipeUp(0.75, 0.25);
    }

    /**
     * Swipe up with custom start/end ratios.
     *
     * @param startRatio Y-position ratio to start (0.0 = top, 1.0 = bottom)
     * @param endRatio   Y-position ratio to end
     */
    public static void swipeUp(double startRatio, double endRatio) {
        log.debug("Swiping up: startRatio={}, endRatio={}", startRatio, endRatio);
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int centerX = size.width / 2;
        int startY = (int) (size.height * startRatio);
        int endY = (int) (size.height * endRatio);
        performSwipe(centerX, startY, centerX, endY);
    }

    /**
     * Swipe down on the screen (scroll content up).
     */
    public static void swipeDown() {
        swipeDown(0.25, 0.75);
    }

    public static void swipeDown(double startRatio, double endRatio) {
        log.debug("Swiping down: startRatio={}, endRatio={}", startRatio, endRatio);
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int centerX = size.width / 2;
        int startY = (int) (size.height * startRatio);
        int endY = (int) (size.height * endRatio);
        performSwipe(centerX, startY, centerX, endY);
    }

    /**
     * Swipe left on the screen.
     */
    public static void swipeLeft() {
        log.debug("Swiping left");
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int centerY = size.height / 2;
        int startX = (int) (size.width * 0.8);
        int endX = (int) (size.width * 0.2);
        performSwipe(startX, centerY, endX, centerY);
    }

    /**
     * Swipe right on the screen.
     */
    public static void swipeRight() {
        log.debug("Swiping right");
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int centerY = size.height / 2;
        int startX = (int) (size.width * 0.2);
        int endX = (int) (size.width * 0.8);
        performSwipe(startX, centerY, endX, centerY);
    }

    /**
     * Swipe on a specific element.
     *
     * @param element   the element to swipe on
     * @param direction UP, DOWN, LEFT, RIGHT
     */
    public static void swipeOnElement(WebElement element, SwipeDirection direction) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        int centerX = location.getX() + size.getWidth() / 2;
        int centerY = location.getY() + size.getHeight() / 2;

        int offsetX = 0, offsetY = 0;
        switch (direction) {
            case UP -> offsetY = -(size.getHeight() / 2);
            case DOWN -> offsetY = size.getHeight() / 2;
            case LEFT -> offsetX = -(size.getWidth() / 2);
            case RIGHT -> offsetX = size.getWidth() / 2;
        }

        performSwipe(centerX, centerY, centerX + offsetX, centerY + offsetY);
    }

    // ==================== Scroll Until Visible ====================

    /**
     * Scroll down repeatedly until an element with the given text is visible.
     *
     * @param text       the text to search for
     * @param maxSwipes  maximum number of swipe attempts
     * @return true if the element was found
     */
    public static boolean scrollDownToText(String text, int maxSwipes) {
        log.debug("Scrolling down to find text: '{}' (max swipes: {})", text, maxSwipes);
        for (int i = 0; i < maxSwipes; i++) {
            try {
                AppiumDriver driver = DriverManager.getDriver();
                List<WebElement> elements = driver.findElements(
                        org.openqa.selenium.By.xpath("//*[contains(@text, '" + text + "') or contains(@label, '" + text + "')]"));
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    log.info("Found text '{}' after {} swipe(s)", text, i);
                    return true;
                }
            } catch (Exception ignored) {
            }
            swipeUp(0.7, 0.3);
        }
        log.warn("Text '{}' not found after {} swipes", text, maxSwipes);
        return false;
    }

    // ==================== Tap Gestures ====================

    /**
     * Tap at specific coordinates.
     */
    public static void tapAt(int x, int y) {
        log.debug("Tapping at coordinates: ({}, {})", x, y);
        Sequence tap = new Sequence(FINGER, 0)
                .addAction(FINGER.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
                .addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(FINGER, Duration.ofMillis(100)))
                .addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        DriverManager.getDriver().perform(List.of(tap));
    }

    /**
     * Long press on an element.
     *
     * @param element    the element to long press
     * @param durationMs how long to hold (milliseconds)
     */
    public static void longPress(WebElement element, int durationMs) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        int centerX = location.getX() + size.getWidth() / 2;
        int centerY = location.getY() + size.getHeight() / 2;

        log.debug("Long pressing at ({}, {}) for {}ms", centerX, centerY, durationMs);

        Sequence longPress = new Sequence(FINGER, 0)
                .addAction(FINGER.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, centerY))
                .addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(FINGER, Duration.ofMillis(durationMs)))
                .addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        DriverManager.getDriver().perform(List.of(longPress));
    }

    // ==================== Drag and Drop ====================

    /**
     * Drag an element to a target location.
     */
    public static void dragAndDrop(WebElement source, WebElement target) {
        Point sourceCenter = getCenter(source);
        Point targetCenter = getCenter(target);

        log.debug("Drag from ({},{}) to ({},{})",
                sourceCenter.getX(), sourceCenter.getY(),
                targetCenter.getX(), targetCenter.getY());

        Sequence drag = new Sequence(FINGER, 0)
                .addAction(FINGER.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                        sourceCenter.getX(), sourceCenter.getY()))
                .addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(FINGER, Duration.ofMillis(600)))
                .addAction(FINGER.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(),
                        targetCenter.getX(), targetCenter.getY()))
                .addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        DriverManager.getDriver().perform(List.of(drag));
    }

    // ==================== Internal Helpers ====================

    private static void performSwipe(int startX, int startY, int endX, int endY) {
        Sequence swipe = new Sequence(FINGER, 0)
                .addAction(FINGER.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY))
                .addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(FINGER.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(), endX, endY))
                .addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        DriverManager.getDriver().perform(List.of(swipe));
    }

    private static Point getCenter(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        return new Point(location.getX() + size.getWidth() / 2,
                location.getY() + size.getHeight() / 2);
    }

    /**
     * Swipe directions.
     */
    public enum SwipeDirection {
        UP, DOWN, LEFT, RIGHT
    }
}
