package com.onsurity.constants;

/**
 * Centralized test group constants.
 * Eliminates magic strings scattered across test classes.
 *
 * <p>Usage:
 * <pre>
 * {@code @Test(groups = {TestGroups.SMOKE, TestGroups.LOGIN})}
 * public void testLogin() { ... }
 * </pre>
 */
public interface TestGroups {

    // === Execution Tiers ===
    String SMOKE = "smoke";
    String REGRESSION = "regression";
    String SANITY = "sanity";

    // === Test Types ===
    String POSITIVE = "positive";
    String NEGATIVE = "negative";
    String EDGE = "edge";

    // === Modules ===
    String LOGIN = "login";
    String HOME = "home";
    String ONBOARDING = "onboarding";
    String PROFILE = "profile";
    String CLAIMS = "claims";
    String OFFERS = "offers";
    String PAYMENT = "payment";

    // === Flow Types ===
    String E2E = "e2e";
}
