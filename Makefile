# ============================================================================
#  App Automation Framework — Makefile
#  Quick-reference commands for running tests, managing the environment,
#  and scaffolding new test classes.
# ============================================================================

# ---- Environment (override via CLI: make run ENV=staging) ----
JAVA_HOME   ?= /Users/Boobala/Library/Java/JavaVirtualMachines/corretto-17.0.18/Contents/Home
ANDROID_HOME ?= /Users/Boobala/Library/Android/sdk
MVN          ?= /opt/homebrew/bin/mvn
ADB          ?= $(ANDROID_HOME)/platform-tools/adb
PLATFORM     ?= android
ENV          ?= staging
POM          ?= pom.xml

# ---- Maven base command ----
MVN_CMD = JAVA_HOME=$(JAVA_HOME) ANDROID_HOME=$(ANDROID_HOME) $(MVN) -f $(POM)

# ---- Fully qualified test class names ----
PKG_BASE     = com.onsurity.tests

# ============================================================================
#  🏗️  BUILD
# ============================================================================

.PHONY: compile clean build

## Compile test sources (no test execution)
compile:
	@echo "🔨 Compiling..."
	@$(MVN_CMD) test-compile -q

## Full clean + compile
clean:
	@echo "🧹 Cleaning target..."
	@$(MVN_CMD) clean -q

## Clean + compile
build: clean compile
	@echo "✅ Build complete"

# ============================================================================
#  🧪  TEST TARGETS
# ============================================================================

.PHONY: run test-all test-e2e test-regression test-by-group

## Run a specific test class (usage: make run TEST=com.onsurity.tests.module.MyTest)
run: compile
	@echo "▶️  Running $(TEST) — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtest=$(TEST) -Dplatform=$(PLATFORM) -Denv=$(ENV)

## Run all tests via TestNG suite XML
test-all: compile
	@echo "🚀 Running full TestNG suite — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -DsuiteXmlFile=src/test/resources/testng.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## Run only E2E tests (by group)
test-e2e: compile
	@echo "🔄 Running E2E group — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dgroups=e2e -Dplatform=$(PLATFORM) -Denv=$(ENV)

## Run regression tests (by group)
test-regression: compile
	@echo "📋 Running Regression group — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dgroups=regression -Dplatform=$(PLATFORM) -Denv=$(ENV)

## Run tests by custom group (usage: make test-by-group GROUP=smoke)
test-by-group: compile
	@echo "🏷️  Running group '$(GROUP)' — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dgroups=$(GROUP) -Dplatform=$(PLATFORM) -Denv=$(ENV)

# ============================================================================
#  📱  DEVICE & EMULATOR
# ============================================================================

.PHONY: devices emulator-start emulator-list clear-pdfs app-reset

## List connected devices / emulators
devices:
	@$(ADB) devices -l

## List available AVDs
emulator-list:
	@$(ANDROID_HOME)/emulator/emulator -list-avds

## Start the default emulator (override: make emulator-start AVD=Pixel_6)
AVD ?= $(shell $(ANDROID_HOME)/emulator/emulator -list-avds | head -1)
emulator-start:
	@echo "📱 Starting emulator: $(AVD)"
	$(ANDROID_HOME)/emulator/emulator -avd $(AVD) -no-snapshot-load &

## Clear old PDFs from device downloads
clear-pdfs:
	@echo "🗑️  Clearing PDFs from device..."
	@$(ADB) shell "rm -f /sdcard/Download/*.pdf" 2>/dev/null || true
	@$(ADB) shell "rm -f /sdcard/Downloads/*.pdf" 2>/dev/null || true
	@echo "✅ Device PDFs cleared"

## Reset app data on device (update package name for your app)
APP_PACKAGE ?= com.your.app.package
app-reset:
	@echo "🔄 Resetting app data..."
	@$(ADB) shell pm clear $(APP_PACKAGE) || true
	@echo "✅ App data cleared"

# ============================================================================
#  📊  REPORTS & LOGS
# ============================================================================

.PHONY: report logs logs-tail

## Open the latest ExtentReport in browser
report:
	@echo "📊 Opening report..."
	@open test-output/AppAutomationReport.html 2>/dev/null || echo "No report found. Run a test first."

## View last 100 lines of the log
logs:
	@tail -100 logs/app-automation.log

## Follow the log in real-time
logs-tail:
	@tail -f logs/app-automation.log

# ============================================================================
#  🆕  SCAFFOLD NEW TEST CLASS
# ============================================================================

.PHONY: new-test

## Scaffold a new E2E test class
## Usage: make new-test NAME=MyNewFeature MODULE=your_module
##   Creates: src/test/java/com/onsurity/tests/<MODULE>/<NAME>E2ETest.java
new-test:
ifndef NAME
	$(error ❌ NAME is required. Usage: make new-test NAME=MyFeature MODULE=your_module)
endif
ifndef MODULE
	$(error ❌ MODULE is required. Usage: make new-test NAME=MyFeature MODULE=your_module)
endif
	@mkdir -p src/test/java/com/onsurity/tests/$(MODULE)
	@TEST_FILE=src/test/java/com/onsurity/tests/$(MODULE)/$(NAME)E2ETest.java; \
	if [ -f "$$TEST_FILE" ]; then \
		echo "❌ File already exists: $$TEST_FILE"; \
		exit 1; \
	fi; \
	cat > "$$TEST_FILE" <<'TEMPLATE_EOF'
package com.onsurity.tests.$(MODULE);

import com.onsurity.base.BaseTest;
import com.onsurity.config.ConfigManager;
import com.onsurity.constants.TestGroups;
import com.onsurity.listeners.ExtentReportListener;
import com.onsurity.utils.*;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * E2E Test: $(NAME)
 *
 * <p>Flow: Login → [TODO: define steps] → Validation → Logout</p>
 *
 * <p>Environment: Run with -Denv=staging -Dplatform=android</p>
 */
public class $(NAME)E2ETest extends BaseTest {

    private static final org.apache.logging.log4j.Logger log =
            org.apache.logging.log4j.LogManager.getLogger($(NAME)E2ETest.class);

    private String phoneNumber;

    // ==================== Setup ====================

    @BeforeSuite
    public void suiteSetup() {
        log.info("========== SUITE SETUP ==========");
        phoneNumber = ConfigManager.get("login.mobile.number", "");
        log.info("Phone: {}, Env: {}, Platform: {}",
                phoneNumber, ConfigManager.getEnvironment(), ConfigManager.get("platform"));
        log.info("========== SUITE SETUP COMPLETE ==========");
    }

    @BeforeClass
    public void setupDriver() {
        log.info("---------- CREATING DRIVER ----------");
        createDriver(null);
        log.info("---------- DRIVER CREATED ----------");

        // Ensure logged in
        AppUtils.bringToForeground();
        AuthHelper.ensureLoggedIn(phoneNumber, ConfigManager.get("login.otp", ""));
        log.info("---------- TEST READY ----------");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        log.info("===== AFTER CLASS: Logging out =====");
        try {
            AuthHelper.logout();
        } catch (Exception e) {
            log.warn("Logout failed: {}", e.getMessage());
        }
        // Driver is destroyed by BaseTest.classTearDown()
    }

    // ==================== Test ====================

    @Test(groups = {TestGroups.E2E, TestGroups.REGRESSION})
    public void $(NAME)E2E_Success() {
        log.info("===== Starting $(NAME) E2E Test =====");

        // ======== STEP 1: Verify on Home Screen ========
        log.info("===== STEP 1: Login verified =====");
        ExtentReportListener.logStep("Step 1: Login completed with " + phoneNumber);

        // ======== STEP 2: Navigate to feature ========
        log.info("===== STEP 2: Navigate to feature =====");
        // TODO: Add navigation steps

        // ======== STEP 3: Perform actions ========
        log.info("===== STEP 3: Perform actions =====");
        // TODO: Add feature-specific actions

        // ======== STEP 4: Validation ========
        log.info("===== STEP 4: Validation =====");
        // TODO: Add assertions and validations

        log.info("===== $(NAME) E2E Test COMPLETED =====");
    }
}
TEMPLATE_EOF
	@echo "✅ Created: src/test/java/com/onsurity/tests/$(MODULE)/$(NAME)E2ETest.java"
	@echo "📝 Next steps:"
	@echo "   1. Edit the test class and fill in the TODO sections"
	@echo "   2. Run: make run TEST=$(PKG_BASE).$(MODULE).$(NAME)E2ETest"

# ============================================================================
#  ℹ️  HELP
# ============================================================================

.PHONY: help
.DEFAULT_GOAL := help

## Show this help
help:
	@echo ""
	@echo "╔══════════════════════════════════════════════════════════════╗"
	@echo "║          App Automation Framework — Makefile                ║"
	@echo "╚══════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "  🧪 TEST TARGETS"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo "  make test-all            Run full TestNG suite"
	@echo "  make test-e2e            Run all E2E group tests"
	@echo "  make test-regression     Run all regression tests"
	@echo ""
	@echo "  🎯 CUSTOM RUN"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo "  make run TEST=<class>    Run a specific test class"
	@echo "  make test-by-group GROUP=smoke  Run tests by group"
	@echo ""
	@echo "  🏗️  BUILD"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo "  make compile             Compile test sources"
	@echo "  make clean               Clean target directory"
	@echo "  make build               Clean + compile"
	@echo ""
	@echo "  📱 DEVICE"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo "  make devices             List connected devices"
	@echo "  make emulator-list       List available AVDs"
	@echo "  make emulator-start      Start the default emulator"
	@echo "  make clear-pdfs          Clear PDFs from device"
	@echo "  make app-reset           Reset app data on device"
	@echo ""
	@echo "  📊 REPORTS & LOGS"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo "  make report              Open ExtentReport in browser"
	@echo "  make logs                View last 100 log lines"
	@echo "  make logs-tail           Follow log in real-time"
	@echo ""
	@echo "  🆕 SCAFFOLD"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo "  make new-test NAME=MyFeature MODULE=your_module"
	@echo "                           Create a new E2E test class"
	@echo ""
	@echo "  ⚙️  OVERRIDES (append to any command)"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo "  ENV=staging|production   Target environment (default: staging)"
	@echo "  PLATFORM=android|ios     Target platform (default: android)"
	@echo ""
	@echo "  📌 EXAMPLES"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo "  make test-all"
	@echo "  make test-all ENV=production"
	@echo "  make run TEST=com.onsurity.tests.login.LoginTest"
	@echo "  make new-test NAME=HealthCheckup MODULE=health"
	@echo ""
