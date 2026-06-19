package com.pice.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Annotation transformer that:
 * <ol>
 *   <li>Applies {@link RetryAnalyzer} to all test methods automatically.</li>
 *   <li>Reads <b>skip</b> and <b>priority</b> overrides from
 *       {@code config/test-execution.properties} and applies them at runtime,
 *       so test execution can be configured without editing Java source.</li>
 * </ol>
 *
 * <p><b>Property format:</b>
 * <pre>
 *   &lt;SimpleClassName&gt;.&lt;methodName&gt;.skip     = true | false
 *   &lt;SimpleClassName&gt;.&lt;methodName&gt;.priority  = &lt;int&gt;
 * </pre>
 *
 * <p>If a key is absent the code-level {@code @Test} annotation value is kept.
 *
 * <p>Register in testng.xml:
 * <pre>
 * {@code <listener class-name="com.pice.listeners.RetryTransformer"/>}
 * </pre>
 */
public class RetryTransformer implements IAnnotationTransformer {

    private static final Logger log = LogManager.getLogger(RetryTransformer.class);
    private static final String CONFIG_FILE = "config/test-execution.properties";

    /** Loaded once (lazy) and cached for the lifetime of the JVM. */
    private static volatile Properties executionConfig;

    // ==================== IAnnotationTransformer ====================

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {

        // 1. Retry analyser (original behaviour)
        if (annotation.getRetryAnalyzerClass() == null) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }

        // 2. Skip / Priority overrides from external config
        if (testMethod == null) {
            return; // safety — transformer may be called for constructors
        }

        Properties config = getExecutionConfig();
        if (config.isEmpty()) {
            return; // no config file found — keep code-level values
        }

        String className  = testMethod.getDeclaringClass().getSimpleName();
        String methodName = testMethod.getName();
        String prefix     = className + "." + methodName;

        // --- Skip override ---
        String skipValue = config.getProperty(prefix + ".skip");
        if (skipValue != null) {
            boolean shouldSkip = Boolean.parseBoolean(skipValue.trim());
            if (shouldSkip) {
                annotation.setEnabled(false);
                log.info("[TestExecConfig] SKIP  {} (set enabled=false)", prefix);
            }
            // If skip=false and annotation was already enabled, no change needed.
            // If skip=false but annotation had enabled=false in code, we honour
            // the external config and re-enable:
            if (!shouldSkip && !annotation.getEnabled()) {
                annotation.setEnabled(true);
                log.info("[TestExecConfig] RE-ENABLE {} (override enabled=true)", prefix);
            }
        }

        // --- Priority override ---
        String priorityValue = config.getProperty(prefix + ".priority");
        if (priorityValue != null) {
            try {
                int priority = Integer.parseInt(priorityValue.trim());
                if (priority != annotation.getPriority()) {
                    log.info("[TestExecConfig] PRIORITY {} → {} (was {})",
                            prefix, priority, annotation.getPriority());
                    annotation.setPriority(priority);
                }
            } catch (NumberFormatException e) {
                log.warn("[TestExecConfig] Invalid priority for {}: '{}'", prefix, priorityValue);
            }
        }
    }

    // ==================== Config Loader ====================

    /**
     * Lazy-load and cache the test-execution properties file.
     * Thread-safe via double-checked locking on the volatile field.
     */
    private static Properties getExecutionConfig() {
        if (executionConfig == null) {
            synchronized (RetryTransformer.class) {
                if (executionConfig == null) {
                    executionConfig = loadConfig();
                }
            }
        }
        return executionConfig;
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream is = RetryTransformer.class.getClassLoader()
                                                     .getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
                log.info("[TestExecConfig] Loaded {} entries from {}",
                        props.size(), CONFIG_FILE);
            } else {
                log.warn("[TestExecConfig] {} not found on classpath — "
                        + "using code-level @Test values", CONFIG_FILE);
            }
        } catch (IOException e) {
            log.error("[TestExecConfig] Failed to load {}: {}", CONFIG_FILE, e.getMessage());
        }
        return props;
    }
}
