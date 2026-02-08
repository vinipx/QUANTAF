# Allure Reporting Integration

## Overview

QUANTAF integrates with Allure Reports to provide rich, interactive test execution reports with detailed failure analysis, timeline visualization, and trend analytics.

## Setup

Allure is automatically configured via the Gradle plugin in `build.gradle.kts`:

```gradle
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

**Automatic integration:**
- TestNG adapter for Java tests
- Cucumber adapter for BDD scenarios
- Result generation to `build/allure-results/`

## Generating Reports

### Build and Generate

```bash
# Run tests
./gradlew test

# Generate Allure report
./gradlew allureReport

# Open report in browser
open build/reports/allure-report/index.html
```

### Gradle Tasks

```bash
# Clean previous results
./gradlew clean

# Run tests and generate report in one step
./gradlew test allureReport

# View report with live server
./gradlew allureServe
```

## Enriching Reports with Annotations

### TestNG Annotations

Add descriptive annotations to tests for richer reports:

```java
import io.qameta.allure.*;

@Feature("Trading")
@Story("Order Execution")
public class TradeExecutionTest {
    
    @Test
    @Description("Verify buy order execution with correct price")
    @Severity(SeverityLevel.CRITICAL)
    @Link("https://jira.example.com/browse/TRADE-123")
    public void testBuyOrderExecution() {
        // ...
    }
    
    @Test
    @Description("Test order rejection when balance insufficient")
    @Severity(SeverityLevel.BLOCKER)
    public void testInsufficientBalance() {
        // ...
    }
}
```

### Step-by-Step Reporting

Break down test logic into steps for detailed reporting:

```java
import io.qameta.allure.Step;

@Test
public void testTradeSettlement() {
    setupTestData();
    submitOrder();
    verifyExecution();
    confirmSettlement();
}

@Step("Setup test data and market conditions")
private void setupTestData() {
    // ...
}

@Step("Submit buy order for {quantity} shares of {symbol}")
private void submitOrder() {
    // ...
}

@Step("Verify execution in trade ledger")
private void verifyExecution() {
    // ...
}

@Step("Confirm settlement status")
private void confirmSettlement() {
    // ...
}
```

**Report Result:**
- Each step appears as a collapsible section
- Execution time tracked per step
- Failed steps clearly marked

### Attaching Evidence

Attach logs, screenshots, or data to reports:

```java
import io.qameta.allure.Allure;

@Test
public void testTradeDetails() {
    // ... test logic ...
    
    // Attach trade details
    Allure.addAttachment(
        "Trade Details",
        "application/json",
        tradeJson.getBytes(),
        ".json"
    );
    
    // Attach protocol logs
    Allure.addAttachment(
        "FIX Protocol Log",
        "text/plain",
        fixLogContent,
        ".log"
    );
    
    // Attach screenshot (if using Selenium)
    Allure.addAttachment(
        "Order Screen",
        "image/png",
        screenshotBytes,
        ".png"
    );
}
```

## Report Navigation

### Overview Dashboard

- **Total Tests**: Passed, failed, skipped count
- **Success Rate**: Percentage of passing tests
- **Execution Time**: Total duration and timeline
- **Severity Distribution**: Critical, blocker, normal breakdown

### Test Details View

For each test, Allure shows:
- **Description**: Test purpose and scope
- **Steps**: Detailed execution steps with timings
- **Attachments**: Logs, screenshots, data files
- **Parameters**: Test input data
- **Failure Details**: Stack trace and root cause

### Timeline View

Chronological visualization of test execution:
- Start time and duration
- Parallel execution detection
- Slow test identification

### Trends View

Historical analytics:
- Pass/fail trends over time
- Performance metrics history
- Flaky test identification

### Categories

Filter and organize tests:
- By feature/story
- By severity (blocker, critical, normal, minor)
- By suite or label
- By status (passed, failed, skipped)

## Cucumber Integration

Allure automatically captures Cucumber scenarios and steps:

```gherkin
Feature: Order Processing
  Background:
    Given the FIX system is ready
  
  @critical
  Scenario: Process buy order
    When I place a buy order for 100 shares at $150
    Then the order should be confirmed
    And the ledger should record the trade
```

**Allure Report includes:**
- Feature name and description
- Scenario title
- Step-by-step execution
- Step parameters
- Execution time per step
- Tags (e.g., @critical)

## Configuration

### Allure Configuration in quantaf.yml

```yaml
reporting:
  allure:
    enabled: true
    results_dir: build/allure-results
    report_dir: build/allure-reports
    
    features:
      timeline: true
      history: true
      trends: true
      categories: true
```

### Custom Report Title and Logo

Customize report appearance:

```gradle
allure {
    version.set("2.27.0")
    adapter {
        autoconfigure.set(true)
    }
    commandLine {
        reportVersion.set("2.27.0")
        version.set("2.27.0")
    }
}
```

## CI/CD Integration

### GitHub Actions

Publish Allure reports to GitHub Pages:

```yaml
- name: Generate Allure Report
  run: ./gradlew allureReport

- name: Deploy to GitHub Pages
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: ./build/reports/allure-report
```

### GitLab CI

Archive Allure reports as artifacts:

```yaml
test:
  script:
    - ./gradlew test allureReport
  artifacts:
    paths:
      - build/reports/allure-report/
```

### Jenkins

Configure Jenkins to display Allure reports:

```groovy
pipeline {
    post {
        always {
            publishAllure([
                includeProperties: false,
                jdk: '',
                results: [[path: 'build/allure-results']]
            ])
        }
    }
}
```

## Analyzing Failures

### Root Cause Analysis

Allure shows:
1. **Failed Assertion**: Which assertion failed
2. **Expected vs Actual**: Side-by-side comparison
3. **Stack Trace**: Full Java stack trace
4. **Step Context**: Which step failed and why
5. **Attachments**: Logs or screenshots at failure time

### Flaky Test Detection

Allure identifies tests that pass and fail inconsistently:
- Historical data analysis
- Trend graphs
- Recommendations for stabilization

### Test Coverage

View which areas of the system are well-tested:
- Features with multiple scenarios
- Edge cases covered
- Critical paths validated

## Advanced Features

### Custom Metrics

Add custom metrics to reports:

```java
@Test
public void testHighFrequencyTrading() {
    long startTime = System.currentTimeMillis();
    
    // ... execute 1000 trades ...
    
    long duration = System.currentTimeMillis() - startTime;
    double throughput = 1000.0 / (duration / 1000.0);
    
    Allure.addDescription("Throughput: " + throughput + " trades/sec");
}
```

### Custom Categories

Define custom failure categories:

Create `allure/categories.json`:

```json
[
  {
    "name": "Protocol Errors",
    "matchedStatuses": ["failed"],
    "messageRegex": ".*IOException.*"
  },
  {
    "name": "Timeout Issues",
    "matchedStatuses": ["failed"],
    "messageRegex": ".*TimeoutException.*"
  }
]
```

## Troubleshooting

### Report Not Generating

1. **Check results directory**: Verify `build/allure-results/` has JSON files
2. **Check Gradle plugin**: Ensure plugin is in `build.gradle.kts`
3. **Run clean build**: `./gradlew clean build allureReport`

### Missing Steps in Report

- Add `@Step` annotations to methods
- Ensure Aspect-J weaver is enabled
- Check `aspectjWeaver.set(true)` in allure block

### Report Customization

Customize colors, logos, and branding by editing Allure configuration or using custom templates.

## Next Steps

- Review **[Configuration](configuration.md)** for advanced settings
- Check **[Examples](examples.md)** for report annotation patterns
- Explore **[CI/CD](cicd.md)** for deployment configurations
