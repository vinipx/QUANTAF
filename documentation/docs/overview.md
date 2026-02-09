---
sidebar_position: 2
title: Overview
description: What is QUANTAF and why it exists
---

# Overview

## What is QUANTAF?

QUANTAF (**QU**antitative **A**ssurance **N**ext-Gen **T**est **A**utomation **F**ramework) is an enterprise-grade test automation framework designed specifically for validating complex financial systems. It provides a robust, scalable solution for testing trading platforms, settlement engines, reconciliation systems, and other mission-critical financial applications.

## Purpose

The primary purpose of QUANTAF is to:

- **Validate Financial Transaction Lifecycles**: End-to-end testing of trades from order submission through fill/rejection and cross-source reconciliation.
- **Support Multiple Financial Protocols**: FIX (4.2, 4.4, 5.0), ActiveMQ/IBM MQ messaging, REST with OAuth2, and ISO 20022 (SWIFT) XML generation.
- **Enable Realistic Data Generation**: Statistical market data using Normal distributions (prices), Poisson distributions (volumes), and Cholesky decomposition (correlated price series).
- **Automate Complex Scenarios**: Use AI-powered NLP-to-FIX translation and ISO 20022 message generation with deterministic template fallback for CI.
- **Provide Rich Insights**: Allure Reports integration with FIX message attachments and reconciliation detail steps.

## Key Features

:::tip[FIX Protocol Stubbing]
WireMock-like `FixStubRegistry` with predicate-based matching, sequential responses, configurable delays, and call counting. Configure the exchange stub to respond with fills, rejections, or custom messages.
:::

:::tip[Cross-Source Reconciliation]
`TradeLedger` performs three-way field-by-field comparison of trade records from FIX, MQ, and API sources with configurable tolerance for numeric fields. `TradeLedgerAssert` provides a fluent assertion DSL.
:::

:::tip[AI-Powered Scenario Generation]
`FixScenarioAgent` converts natural language like *"Limit Buy 500 shares of AAPL at 150"* into structured `OrderConfiguration` objects. `SmartStub` generates ISO 20022 XML messages from intent descriptions.
:::

:::tip[Dual-Mode Testing]
Run tests with TestNG (extending `QuantafBaseTest`) or Cucumber BDD (with `OrderStepDefs` and Gherkin feature files).
:::

:::tip[Rich Allure Reporting]
`AllureFixAttachment` attaches formatted FIX messages to reports. `ReconciliationReportStep` attaches reconciliation results as custom Allure steps with pass/fail status.
:::

:::tip[Business Calendar Support]
`BusinessCalendar` with factory methods for NYSE, LSE, and TSE calendars. Supports T+N settlement date calculations skipping weekends and holidays (both specific dates and recurring MonthDay patterns).
:::

## Target Audience

QUANTAF is designed for:

- **QA Engineers** building test suites for financial systems using FIX, MQ, and REST.
- **DevOps Teams** implementing CI/CD pipelines with GitHub Actions and Testcontainers.
- **Developers** extending framework capabilities with custom protocol adapters and LLM providers.
- **Architects** designing test strategies for mission-critical financial applications.

## Quick Start

Get started in seconds:

```bash
# Clone and build
git clone https://github.com/vinipx/QUANTAF.git
cd QUANTAF

# Build the project
./gradlew build

# Run all tests (unit + scenario + BDD)
./gradlew test

# Generate Allure reports
./gradlew allureReport

# Serve documentation locally (http://localhost:3000)
./docs.sh
```

## What Gets Executed

The `testng.xml` suite configuration organizes tests into three groups:

| Test Group | Classes | Purpose |
|------------|---------|---------|
| **Unit Tests** | `MarketMakerTest`, `FixStubRegistryTest`, `TradeLedgerAssertTest` | Validate core components in isolation |
| **Scenario Tests** | `HighFrequencyOrderTest` | End-to-end workflows: AI → FIX stub → reconciliation |
| **BDD Tests** | `CucumberTestRunner` | Gherkin scenarios in `order_lifecycle.feature` |

Tests run in parallel across classes with 4 threads (configured in `testng.xml`).

## Next Steps

- Explore the **[Architecture](architecture.md)** to understand QUANTAF's 4-layer design.
- Review the **[Tech Stack](tech-stack.md)** for dependency and version details.
- Check **[Features](features.md)** for comprehensive capability overview.
- Start building with **[Examples](examples.md)**.
