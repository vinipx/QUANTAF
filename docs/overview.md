# Overview

## What is QUANTAF?

QUANTAF (High-Frequency Assurance Engine) is an enterprise-grade test automation framework designed specifically for validating complex financial systems. It provides a robust, scalable solution for testing trading platforms, settlement engines, reconciliation systems, and other mission-critical financial applications.

## Purpose

The primary purpose of QUANTAF is to:

- **Validate Financial Transaction Lifecycles**: End-to-end testing of trades, settlements, and reconciliation with mathematical precision.
- **Support Multiple Financial Protocols**: FIX, SWIFT, MQ, and REST with built-in support for OAuth2 and standard financial messaging standards.
- **Enable High-Frequency Testing**: Optimized for low-latency environments with support for thousands of transactions per second.
- **Automate Complex Scenarios**: Use AI-powered scenario generation and NLP-to-FIX translation for intelligent test design.
- **Provide Rich Insights**: Allure Reports integration for comprehensive test analytics and failure diagnostics.

## Key Features

!!! success "Protocol Agnostic"
    Seamlessly integrate with FIX, SWIFT, MQ, REST, and custom protocols through pluggable adapters.

!!! success "AI-Powered"
    Leverage LangChain4j for NLP-driven test scenario generation and smart SWIFT stub generation.

!!! success "Dual-Mode Testing"
    Run tests with TestNG or Cucumber BDD depending on your team's preferences.

!!! success "Production-Ready Reporting"
    Allure Reports provide rich, actionable insights into test execution and failures.

!!! success "Infrastructure as Code"
    Docker Compose and Testcontainers for ephemeral, reproducible testing environments.

## Target Audience

QUANTAF is designed for:

- **QA Engineers** building test suites for financial systems.
- **DevOps Teams** implementing CI/CD pipelines for trading and settlement platforms.
- **Developers** extending framework capabilities with custom protocol adapters.
- **Architects** designing test strategies for mission-critical financial applications.

## Quick Start

Get started in seconds:

```bash
# Clone and install
git clone https://github.com/vinipx/QUANTAF.git
cd QUANTAF

# Serve documentation locally
./docs.sh

# Run tests
./gradlew test

# Generate Allure reports
./gradlew allureReport
```

## Next Steps

- Explore the **[Architecture](architecture.md)** to understand QUANTAF's layered design.
- Review the **[Tech Stack](tech-stack.md)** for dependency and version details.
- Check **[Features](features.md)** for comprehensive capability overview.
- Start building with **[Examples](examples.md)**.
