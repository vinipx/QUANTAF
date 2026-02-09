---
sidebar_position: 9
title: Allure Reporting
description: Report generation, enrichment, and failure analysis
---

# Allure Reporting Integration

## Overview

QUANTAF integrates with Allure Reports to provide rich, interactive test execution reports. The framework includes two dedicated reporting utilities: `AllureFixAttachment` for FIX message attachments and `ReconciliationReportStep` for reconciliation detail steps.

## Setup

Allure is configured via the Gradle plugin in `build.gradle.kts`:

```kotlin
plugins {
    id("io.qameta.allure") version "2.12.0"
}

allure {
    version.set("2.27.0")
    adapter {
        autoconfigure.set(true)
        aspectjWeaver.set(true)
    }
}
```

**Dependencies (also in `build.gradle.kts`):**

```kotlin
implementation("io.qameta.allure:allure-testng:2.27.0")
implementation("io.qameta.allure:allure-cucumber7-jvm:2.27.0")
```

## Generating Reports

```bash
# Run tests (results → build/allure-results/)
./gradlew test

# Generate HTML report
./gradlew allureReport

# Open report in browser
open build/reports/allure-report/allureReport/index.html

# View report with live server
./gradlew allureServe
```

## QUANTAF Reporting Utilities

### AllureFixAttachment

Located at `io.github.vinipx.quantaf.reporting.AllureFixAttachment`.

Attaches formatted FIX messages to Allure reports for inspection:

```java
AllureFixAttachment.attachFixMessage("NewOrderSingle", order);
AllureFixAttachment.attachFixMessage("ExecutionReport (Rejection)", rejection);
AllureFixAttachment.attachRawFix("Raw FIX Log", rawFixString);
```

**How formatting works:** The `formatFixMessage()` method replaces the SOH delimiter (ASCII character `\001`) with `|\n` (pipe + newline), making FIX messages human-readable:

```
Before: 8=FIX.4.4\0019=100\00135=D\00149=QUANTAF_CLIENT\001...
After:
8=FIX.4.4|
9=100|
35=D|
49=QUANTAF_CLIENT|
...
```

### ReconciliationReportStep

Located at `io.github.vinipx.quantaf.reporting.ReconciliationReportStep`.

Creates custom Allure steps with reconciliation detail data:

```java
ReconciliationReportStep.report(result);
// Creates step: "Reconciliation: ORD-001 [PASSED]" or "[FAILED]"
// Attaches detailed tabular comparison

ReconciliationReportStep.attachMqPayload("Trade Confirmation", jsonPayload);
```

The detailed report produces:

```
=== Reconciliation: ORD-001 [PASSED] ===
Field                | FIX                  | MQ                   | API                  | Status
----------------------------------------------------------------------------------------------------
price                | 150.00               | 150.00               | 150.00               | MATCH
quantity             | 100                  | 100                  | 100                  | MATCH
amount               | 15000.00             | 15000.00             | 15000.00             | MATCH
settlementDate       | 2026-02-10           | 2026-02-10           | 2026-02-10           | MATCH
symbol               | AAPL                 | AAPL                 | AAPL                 | MATCH
currency             | USD                  | USD                  | USD                  | MATCH
```

## Enriching Reports with Annotations

QUANTAF tests use Allure annotations extensively:

```java
@Epic("Order Lifecycle")           // Top-level grouping
@Feature("High Frequency Orders")  // Feature grouping
public class HighFrequencyOrderTest extends QuantafBaseTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)    // Priority
    @Story("Order Rejection")             // Scenario
    @Description("Verifies that a fat-finger rejection is correctly processed")
    public void testHighFrequencyOrderRejection() {
        // ...
    }
}
```

| Annotation | Purpose | Placement |
|------------|---------|-----------|
| `@Epic("...")` | Top-level test grouping | Class level |
| `@Feature("...")` | Feature within an epic | Class level |
| `@Story("...")` | User story/scenario | Method level |
| `@Severity(SeverityLevel.CRITICAL)` | Priority level | Method level |
| `@Description("...")` | Detailed test description | Method level |

## Cucumber Integration

The `CucumberTestRunner` is configured with the Allure Cucumber 7 plugin:

```java
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "io.github.vinipx.quantaf.bdd.steps",
    plugin = {
        "pretty",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
        "html:build/reports/cucumber/cucumber.html",
        "json:build/reports/cucumber/cucumber.json"
    },
    tags = "@smoke or @regression"
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

## CI/CD Integration

The CI workflow uploads Allure results as artifacts:

```yaml
- name: Upload Test Results
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: test-results
    path: |
      build/reports/tests/
      build/allure-results/
    retention-days: 14
```

## Next Steps

- Review **[Examples](examples.md)** for tests with Allure annotations
- Check **[CI/CD](cicd.md)** for report publishing in pipelines
