package com.pice.utils;

import com.pice.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Programmatic Appium server lifecycle manager.
 *
 * <p>Handles auto-start and stop of the Appium server for local test execution.
 * Controlled via configuration properties:
 * <ul>
 *   <li>{@code appium.auto.manage} — Enable/disable auto-management (default: true)</li>
 *   <li>{@code appium.server.host} — Server host (default: 127.0.0.1)</li>
 *   <li>{@code appium.server.port} — Server port (default: 4723)</li>
 *   <li>{@code appium.node.path} — Path to Node.js binary</li>
 *   <li>{@code appium.binary.path} — Path to Appium binary</li>
 * </ul>
 *
 * <p>For CI/CD pipelines where Appium is managed externally, set
 * {@code appium.auto.manage=false} to skip server management.
 */
public class AppiumServerManager {

    private static final Logger log = LogManager.getLogger(AppiumServerManager.class);

    private static Process appiumProcess;
    private static boolean serverStartedByUs = false;

    // ==================== Configuration ====================

    private static String getHost() {
        return ConfigManager.get("appium.server.host", "127.0.0.1");
    }

    private static int getPort() {
        return Integer.parseInt(ConfigManager.get("appium.server.port", "4723"));
    }

    private static boolean isAutoManageEnabled() {
        return Boolean.parseBoolean(ConfigManager.get("appium.auto.manage", "true"));
    }

    private static String getNodePath() {
        return ConfigManager.get("appium.node.path", "node");
    }

    private static String getAppiumBinaryPath() {
        return ConfigManager.get("appium.binary.path", "appium");
    }

    // ==================== Public API ====================

    /**
     * Start the Appium server if auto-management is enabled.
     * If the server is already running, this is a no-op.
     *
     * @throws RuntimeException if the server fails to start within the timeout
     */
    public static synchronized void startServer() {
        if (!isAutoManageEnabled()) {
            log.info("Appium auto-manage is disabled — skipping server start");
            return;
        }

        if (isServerRunning()) {
            log.info("Appium server is already running at {}:{}", getHost(), getPort());
            return;
        }

        log.info("Starting Appium server at {}:{}...", getHost(), getPort());

        try {
            String nodePath = getNodePath();
            String appiumPath = getAppiumBinaryPath();
            String host = getHost();
            int port = getPort();

            ProcessBuilder pb = new ProcessBuilder(
                    nodePath, appiumPath,
                    "--address", host,
                    "--port", String.valueOf(port),
                    "--allow-cors",
                    "--relaxed-security"
            );

            // Set environment variables
            pb.environment().put("ANDROID_HOME",
                    ConfigManager.get("android.home", System.getenv("ANDROID_HOME") != null
                            ? System.getenv("ANDROID_HOME")
                            : "/Users/" + System.getProperty("user.name") + "/Library/Android/sdk"));
            pb.environment().put("JAVA_HOME",
                    ConfigManager.get("java.home.path", System.getProperty("java.home")));

            pb.redirectErrorStream(true);
            appiumProcess = pb.start();
            serverStartedByUs = true;

            // Consume stdout/stderr in a background thread to prevent blocking
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(appiumProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("[Appium] {}", line);
                    }
                } catch (Exception ignored) {}
            }, "appium-output-reader");
            outputThread.setDaemon(true);
            outputThread.start();

            // Wait for server to become ready
            if (!waitForServerReady(30)) {
                throw new RuntimeException("Appium server failed to start within 30 seconds");
            }

            log.info("Appium server started successfully at {}:{}", host, port);

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to start Appium server: " + e.getMessage(), e);
        }
    }

    /**
     * Stop the Appium server if it was started by this manager.
     * Safe to call multiple times.
     */
    public static synchronized void stopServer() {
        if (!serverStartedByUs) {
            log.info("Appium server was not started by us — skipping stop");
            return;
        }

        log.info("Stopping Appium server...");

        try {
            if (appiumProcess != null && appiumProcess.isAlive()) {
                appiumProcess.destroy();

                // Wait up to 10 seconds for graceful shutdown
                if (!appiumProcess.waitFor(10, TimeUnit.SECONDS)) {
                    log.warn("Appium server did not stop gracefully — force killing");
                    appiumProcess.destroyForcibly();
                    appiumProcess.waitFor(5, TimeUnit.SECONDS);
                }

                log.info("Appium server stopped");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while stopping Appium server");
        } finally {
            appiumProcess = null;
            serverStartedByUs = false;
        }
    }

    /**
     * Check if the Appium server is currently running and responding.
     *
     * @return true if the server responds with ready=true
     */
    public static boolean isServerRunning() {
        try {
            String statusUrl = String.format("http://%s:%d/status", getHost(), getPort());
            HttpURLConnection connection = (HttpURLConnection) new URL(statusUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String body = response.toString();
                return body.contains("\"ready\":true");
            }
        } catch (Exception ignored) {
            // Server not reachable
        }
        return false;
    }

    // ==================== Private Helpers ====================

    /**
     * Poll the server status endpoint until it responds with ready=true.
     *
     * @param timeoutSeconds maximum time to wait
     * @return true if server became ready, false if timeout
     */
    private static boolean waitForServerReady(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        int attempt = 0;

        while (System.currentTimeMillis() < deadline) {
            attempt++;
            if (isServerRunning()) {
                log.info("Appium server ready after {} attempts", attempt);
                return true;
            }

            // Check if process died
            if (appiumProcess != null && !appiumProcess.isAlive()) {
                log.error("Appium process exited with code: {}", appiumProcess.exitValue());
                return false;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        log.error("Appium server did not become ready within {} seconds", timeoutSeconds);
        return false;
    }
}
