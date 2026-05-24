package com.onsurity.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Annotation transformer that automatically applies RetryAnalyzer
 * to all test methods without needing to annotate each one individually.
 *
 * <p>Register in testng.xml:
 * <pre>
 * {@code <listener class-name="com.onsurity.listeners.RetryTransformer"/>}
 * </pre>
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        if (annotation.getRetryAnalyzerClass() == null) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}
