# ============================================================================
#  App Automation Framework — Makefile
#  Quick-reference commands for running tests, managing the environment,
#  and scaffolding new test classes.
# ============================================================================

# ---- Environment (override via CLI: make run ENV=staging) ----
JAVA_HOME   ?= /Library/Java/JavaVirtualMachines/jdk-18.jdk/Contents/Home
ANDROID_HOME ?= /Users/Mathi/Library/Android/sdk
MVN          ?= /Users/Mathi/tools/apache-maven-3.9.8/bin/mvn
ADB          ?= $(ANDROID_HOME)/platform-tools/adb
PLATFORM     ?= android
ENV          ?= staging
POM          ?= pom.xml

# ---- Maven base command ----
MVN_CMD = JAVA_HOME=$(JAVA_HOME) ANDROID_HOME=$(ANDROID_HOME) $(MVN) -f $(POM)

# ---- Fully qualified test class names ----
PKG_BASE     = com.pice.tests

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

.PHONY: run test-all test-e2e test-regression test-by-group \
        test-module test-batch-smoke test-batch-login test-batch-login-e2e test-batch-api test-batch-dashboard test-batch-cards test-batch-history \
        test-batch-login-otp test-batch-login-security

## Run a specific test class (usage: make run TEST=com.pice.tests.module.MyTest)
run: compile
	@echo "▶️  Running $(TEST) — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtest=$(TEST) -Dplatform=$(PLATFORM) -Denv=$(ENV)

## Run all tests via TestNG suite XML (all batches in sequence)
test-all: compile
	@echo "🚀 Running full TestNG suite (all batches) — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/testng.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## ── Module-wise Batch Targets ─────────────────────────────────────────────
## Run a specific module batch by name
##   MODULE options: smoke | login | login-e2e | login-otp | login-security | api | dashboard | cards | history
##   Usage: make test-module MODULE=login-otp
test-module: compile
	@if [ -z "$(MODULE)" ]; then echo "❌ MODULE is required. E.g.: make test-module MODULE=login"; exit 1; fi
	@SUITE=src/test/resources/suites/$(MODULE).xml; \
	if [ ! -f "$$SUITE" ]; then echo "❌ Suite not found: $$SUITE"; exit 1; fi; \
	echo "🎯 Running batch: $(MODULE) — env=$(ENV), platform=$(PLATFORM)"; \
	$(MVN_CMD) test -Dtestng.suite.file=$$SUITE -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-1] Smoke — app launch + framework sanity
test-batch-smoke: compile
	@echo "🔥 [Batch-1] Smoke tests — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/smoke.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-2+3] Login — Smoke + Regression (emulator-safe)
test-batch-login: compile
	@echo "🔐 [Batch-2+3] Login module (Smoke + Regression) — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/login.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-4] Login E2E — Full flow: Login → OTP → Permission → Dashboard
##   Requires physical device + test.mobile.number + test.otp
test-batch-login-e2e: compile
	@echo "📲 [Batch-4] Login E2E flow — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/login-e2e.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-5] API — REST Assured backend tests
test-batch-api: compile
	@echo "🌐 [Batch-5] API tests — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/api.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-6] Home Dashboard — click all buttons, navigate all tabs
##   Requires physical device + test.mobile.number + test.otp
test-batch-dashboard: compile
	@echo "🏠 [Batch-6] Home Dashboard tests — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/dashboard.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-7] Cards — Add Card flow tests
##   Requires physical device + test.mobile.number + test.otp
test-batch-cards: compile
	@echo "💳 [Batch-7] Cards — Add Card Flow tests — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/cards.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-8] Pay Bills — Pay Bill flow tests (card 7649 + IndusInd, negative screenshots)
##   Requires physical device + test.mobile.number + test.otp
test-batch-paybill: compile
	@echo "💳 [Batch-8] Pay Bills — Pay Bill Flow tests — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/paybill.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-9] History — History screen tests
##   Requires physical device + test.mobile.number + test.otp
##   Tests: smoke, filter tabs (All/Payments/Cards/Loan), transaction list, scroll, E2E
test-batch-history: compile
	@echo "📋 [Batch-9] History Screen tests — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/history.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-OTP] OTP Validation — OTP entry, resend timer, network edge cases
##   Requires physical device + valid registered phone number (test.mobile.number)
##   Runs: OtpValidationTest (excludes enabled=false stubs)
test-batch-login-otp: compile
	@echo "🔑 [Batch-OTP] OTP Validation tests — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/login-otp.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)

## [Batch-Security] Login Security — OTP masking, brute-force UI, attempt limit
##   Requires physical device + valid registered phone number (test.mobile.number)
##   Note: Most security tests require OTP screen access (non-rooted device)
test-batch-login-security: compile
	@echo "🛡️  [Batch-Security] Login Security tests — env=$(ENV), platform=$(PLATFORM)"
	$(MVN_CMD) test -Dtestng.suite.file=src/test/resources/suites/login-security.xml -Dplatform=$(PLATFORM) -Denv=$(ENV)


## ── Legacy group-based targets ────────────────────────────────────────────
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
APP_PACKAGE ?= one.pice.pice_business_loan.pre
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
##   Creates: src/test/java/com/pice/tests/<MODULE>/<NAME>E2ETest.java
new-test:
ifndef NAME
	$(error ❌ NAME is required. Usage: make new-test NAME=MyFeature MODULE=your_module)
endif
ifndef MODULE
	$(error ❌ MODULE is required. Usage: make new-test NAME=MyFeature MODULE=your_module)
endif
	@mkdir -p src/test/java/com/pice/tests/$(MODULE)
	@TEST_FILE=src/test/java/com/pice/tests/$(MODULE)/$(NAME)E2ETest.java; \
	if [ -f "$$TEST_FILE" ]; then \
		echo "❌ File already exists: $$TEST_FILE"; \
		exit 1; \
	fi; \
	printf 'package com.pice.tests.%s;\n\nimport com.pice.base.BaseTest;\nimport com.pice.config.ConfigManager;\nimport com.pice.constants.TestGroups;\nimport com.pice.listeners.ExtentReportListener;\nimport com.pice.utils.*;\nimport io.appium.java_client.AppiumDriver;\nimport org.openqa.selenium.By;\nimport org.openqa.selenium.WebElement;\nimport org.testng.Assert;\nimport org.testng.annotations.AfterClass;\nimport org.testng.annotations.BeforeClass;\nimport org.testng.annotations.BeforeSuite;\nimport org.testng.annotations.Test;\n\nimport java.util.List;\nimport java.util.Map;\n\npublic class %sE2ETest extends BaseTest {\n    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(%sE2ETest.class);\n    private String phoneNumber;\n\n    @BeforeSuite\n    public void suiteSetup() {\n        log.info("========== SUITE SETUP ==========");\n        phoneNumber = ConfigManager.get("login.mobile.number", "");\n        log.info("========== SUITE SETUP COMPLETE ==========");\n    }\n\n    @BeforeClass\n    public void setupDriver() {\n        createDriver(null);\n        AppUtils.bringToForeground();\n        AuthHelper.ensureLoggedIn(phoneNumber, ConfigManager.get("login.otp", ""));\n    }\n\n    @AfterClass(alwaysRun = true)\n    public void tearDown() {\n        try { AuthHelper.logout(); } catch (Exception e) {} \n    }\n\n    @Test(groups = {TestGroups.E2E, TestGroups.REGRESSION})\n    public void %sE2E_Success() {\n        log.info("===== Starting %s E2E Test =====");\n        ExtentReportListener.logStep("Step 1: Login completed");\n    }\n}\n' "$(MODULE)" "$(NAME)" "$(NAME)" "$(NAME)" "$(NAME)" > "$$TEST_FILE"
	@echo "✅ Created: src/test/java/com/pice/tests/$(MODULE)/$(NAME)E2ETest.java"
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
	@echo "  make test-all                Run full TestNG suite (all batches)"
	@echo "  make test-e2e                Run all E2E group tests"
	@echo "  make test-regression         Run all regression tests"
	@echo ""
	@echo "  📦 MODULE-WISE BATCH TARGETS"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo "  make test-module MODULE=<m>  Run a named module batch"
	@echo "       MODULE options: smoke | login | login-e2e | api | dashboard | cards"
	@echo "  make test-batch-smoke        [Batch-1] Smoke — app launch"
	@echo "  make test-batch-login        [Batch-2+3] Login smoke + regression"
	@echo "  make test-batch-login-e2e    [Batch-4] Full login E2E flow"
	@echo "  make test-batch-api          [Batch-5] API tests"
	@echo "  make test-batch-dashboard    [Batch-6] Home Dashboard — all buttons"
	@echo "  make test-batch-cards        [Batch-7] Cards — Add Card flow"
	@echo "  make test-batch-history       [Batch-9] History — filters, transactions, E2E"
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
	@echo "  make test-batch-smoke"
	@echo "  make test-batch-login"
	@echo "  make test-batch-login-e2e"
	@echo "  make test-batch-api"
	@echo "  make test-batch-dashboard"
	@echo "  make test-module MODULE=dashboard ENV=staging"
	@echo "  make test-module MODULE=cards ENV=staging"
	@echo "  make test-module MODULE=login ENV=staging"
	@echo "  make run TEST=com.pice.tests.login.LoginTest"
	@echo "  make run TEST=com.pice.tests.dashboard.HomeDashboardTest"
	@echo "  make run TEST=com.pice.tests.cards.AddCardFlowTest"
	@echo "  make new-test NAME=HealthCheckup MODULE=health"
	@echo ""
