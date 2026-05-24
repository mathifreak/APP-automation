# 📱 App Automation Framework

Appium-based mobile app test automation framework for **Android + iOS**.

Built with industry-standard patterns: Page Object Model, TestNG lifecycle, ExtentReports, and thread-safe driver management.

---

## 🏗️ Architecture

```
src/
├── main/java/com/onsurity/
│   ├── config/              # ConfigManager, AppCapabilities
│   ├── driver/              # DriverFactory, DriverManager, DeviceType
│   ├── exceptions/          # Custom framework exceptions
│   ├── pages/               # BasePage (abstract) — extend for your pages
│   └── utils/               # AppUtils, GestureUtils, WaitUtils, AuthHelper, etc.
│
└── test/
    ├── java/com/onsurity/
    │   ├── base/            # BaseTest (TestNG lifecycle)
    │   ├── constants/       # TestGroups
    │   └── listeners/       # ExtentReportListener, RetryAnalyzer
    │
    └── resources/
        ├── config/          # config.properties, staging.properties
        ├── capabilities/    # android.json, ios.json
        ├── apps/            # Place your APK/IPA here
        └── testng.xml       # TestNG suite definition
```

---

## 🚀 Quick Start

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17+ |
| Maven | 3.8+ |
| Appium | 2.x |
| Node.js | 18+ (for Appium) |
| Android SDK | API 28+ |

### 1. Setup

```bash
# Clone or copy this project
cd APP-automation

# Install dependencies
mvn clean compile

# Place your APK in the apps directory
mkdir -p src/test/resources/apps
cp /path/to/your-app.apk src/test/resources/apps/app-release.apk

# Copy and configure environment properties
cp src/test/resources/config/staging.properties.template src/test/resources/config/staging.properties
# Edit staging.properties with your values
```

### 2. Configure

Edit `src/test/resources/config/config.properties`:

```properties
app.name=Your App Name
app.bundle.id=com.your.app.package
login.mobile.number=your_test_number
login.otp=your_test_otp
```

Edit `src/test/resources/capabilities/android.json`:

```json
{
  "appium:deviceName": "Your_Device",
  "appium:platformVersion": "14",
  "appium:appPackage": "com.your.app.package",
  "appium:appActivity": "com.your.app.MainActivity"
}
```

### 3. Create Your First Test

```bash
make new-test NAME=Login MODULE=login
```

This creates `src/test/java/com/onsurity/tests/login/LoginE2ETest.java` with a ready-to-fill template.

### 4. Run

```bash
# Run a specific test
make run TEST=com.onsurity.tests.login.LoginE2ETest

# Run all tests
make test-all

# Run by group
make test-by-group GROUP=smoke
```

---

## 📝 Creating Page Objects

Extend `BasePage` for each screen:

```java
package com.onsurity.pages.login;

import com.onsurity.pages.BasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    // React Native testID locators (preferred)
    private static final String PHONE_INPUT = "phone-input";
    private static final String SUBMIT_BTN = "submit-button";

    @Override
    protected By getPageLoadedLocator() {
        return AppiumBy.accessibilityId(PHONE_INPUT);
    }

    public LoginPage() {
        waitForPageLoad();
    }

    public void enterPhone(String phone) {
        type(PHONE_INPUT, phone);
    }

    public void tapSubmit() {
        tap(SUBMIT_BTN);
    }
}
```

---

## 🧪 Creating Tests

Extend `BaseTest` for each test class:

```java
package com.onsurity.tests.login;

import com.onsurity.base.BaseTest;
import com.onsurity.constants.TestGroups;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(groups = {TestGroups.SMOKE, TestGroups.LOGIN})
    public void testLoginWithValidCredentials() {
        // Your test logic here
    }
}
```

---

## 📊 Reports

After test execution, open the ExtentReport:

```bash
make report
```

Reports are generated at `test-output/AppAutomationReport.html`.

---

## 🛠️ Available Make Commands

| Command | Description |
|---------|-------------|
| `make compile` | Compile test sources |
| `make build` | Clean + compile |
| `make run TEST=<class>` | Run a specific test class |
| `make test-all` | Run full TestNG suite |
| `make test-e2e` | Run E2E group tests |
| `make test-regression` | Run regression tests |
| `make test-by-group GROUP=smoke` | Run tests by group |
| `make devices` | List connected devices |
| `make emulator-start` | Start default emulator |
| `make report` | Open ExtentReport |
| `make new-test NAME=X MODULE=Y` | Scaffold new test class |

---

## 📁 Framework Components

| Component | Description |
|-----------|-------------|
| **ConfigManager** | Environment-aware config loading (global → env → CLI overrides) |
| **DriverFactory** | Creates platform-specific Appium drivers (Android/iOS) |
| **DriverManager** | Thread-safe driver storage via ThreadLocal |
| **BasePage** | Abstract page with resilient actions (tap, type, wait, scroll) |
| **BaseTest** | TestNG lifecycle management (suite → test → class → method) |
| **ExtentReportListener** | Auto-screenshot on failure, device info, retry-aware reports |
| **RetryAnalyzer** | Configurable test retry for flaky mobile tests |
| **AppUtils** | App lifecycle (foreground, reset, hide keyboard) |
| **GestureUtils** | Swipe and scroll gestures |
| **WaitUtils** | Explicit waits and element presence checks |
| **AuthHelper** | Login/logout flow (stub — implement for your app) |
| **ApiUtils** | REST API utilities for test data setup |
| **ExcelDataProvider** | Excel-based test data reading |
