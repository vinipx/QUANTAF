---
slug: /
sidebar_position: 1
title: Home
description: QUANTAF — High-Frequency Assurance Engine for Financial Systems
---

# QUANTAF Documentation

Welcome to the official documentation for **QUANTAF**, the High-Frequency Assurance Engine for Financial Systems.

## 🚀 Quick Start

Get up and running in seconds:

```bash
# Clone the repository
git clone https://github.com/vinipx/QUANTAF.git
cd QUANTAF

# Build the project
./gradlew build

# Run tests
./gradlew test

# Generate Allure reports
./gradlew allureReport

# Serve documentation locally (http://localhost:3000)
./docs.sh
```

---

## 📚 Documentation Guide

### Core Concepts
- **[Overview](overview.md)** — Purpose, key features, and quick introduction
- **[Architecture](architecture.md)** — 4-layer concentric design with detailed class mappings
- **[Features & Capabilities](features.md)** — Protocol support, AI, reporting, extensibility

### Getting Started
- **[Tech Stack](tech-stack.md)** — Dependencies, versions, and library details
- **[Configuration Guide](configuration.md)** — Setup, environment variables, YAML structure
- **[Usage Examples](examples.md)** — TestNG, Cucumber BDD, AI-powered test scenarios

### Advanced Topics
- **[Development Reference](development.md)** — Project structure, extension points, testing guidelines
- **[Allure Reporting](allure.md)** — Report generation, enrichment, failure analysis
- **[CI/CD & Docker](cicd.md)** — GitHub Actions workflows, Docker Compose, Testcontainers
- **[Contributing & License](contributing.md)** — MIT License, contribution guide, code standards

---

## ✨ Key Highlights

### Protocol Support 🌐
- **FIX** (4.2, 4.4, 5.0) via QuickFIX/J with WireMock-like stubbing
- **Message Queues** — ActiveMQ Artemis (Jakarta JMS), IBM MQ skeleton
- **REST** with OAuth2 token management via RestAssured
- **ISO 20022 (SWIFT)** — AI-powered XML message generation (`pacs.008`, `camt.053`, `sese.023`)

### AI & Intelligent Features 🤖
- NLP-to-FIX translation via `FixScenarioAgent`
- Smart ISO 20022 stub generation via `SmartStub`
- LLM-powered scenario generation with template fallback
- Pluggable LLM providers (OpenAI, Ollama)

### Testing Modes
- **TestNG**: Direct Java testing with `QuantafBaseTest` base class
- **Cucumber BDD**: Gherkin scenarios with `OrderStepDefs` step definitions

### Enterprise Features
- Rich Allure Reports with FIX message attachments and reconciliation details
- Docker Compose for local ActiveMQ Artemis
- Testcontainers for ephemeral CI infrastructure
- GitHub Actions CI/CD with Gradle caching

---

## 🏗️ Architecture at a Glance

```
Layer 4: Test Definition (TestNG, Cucumber BDD, Allure Reporting)
    ↓
Layer 3: AI Cortex (FixScenarioAgent, SmartStub, LlmProvider)
    ↓
Layer 2: Logic Core (MarketMaker, TradeLedger, BusinessCalendar)
    ↓
Layer 1: Protocol Adapters (FIX, MQ, REST)
    ↓
Financial Systems (Trading, Settlement, Reconciliation)
```

[Learn more →](architecture.md)

---

## 🎯 Use Cases

- **Trading Platform Testing**: Validate order execution, fills, and rejections via FIX stubs
- **Cross-Source Reconciliation**: Three-way comparison of FIX, MQ, and API trade records
- **Settlement Verification**: T+N business day calculations with NYSE/LSE/TSE calendars
- **Realistic Data Generation**: Statistical market data using Normal and Poisson distributions
- **AI-Driven Scenario Design**: Generate order configurations from natural language intent
- **Integration Testing**: Test across FIX, MQ, and REST protocols in a single test

---

## 📋 Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| **Java** | 21 (LTS) | Primary language |
| **Gradle** | 9.3.1 | Build automation (Kotlin DSL) |
| **QuickFIX/J** | 2.3.1 | FIX protocol (4.2, 4.4, 5.0) |
| **RestAssured** | 5.4.0 | REST API testing |
| **LangChain4j** | 0.35.0 | LLM integration (OpenAI, Ollama) |
| **Allure** | 2.27.0 | Test reporting |
| **TestNG** | 7.10.2 | Test execution framework |
| **Cucumber** | 7.18.0 | BDD scenario execution |
| **Testcontainers** | 1.20.0 | Ephemeral CI infrastructure |

[Full details →](tech-stack.md)

---

## 🔧 Configuration

QUANTAF uses a centralized `quantaf.yml` with environment variable interpolation via `QuantafConfig`:

```yaml
quantaf:
  environment: local

  fix:
    defaultVersion: FIX44
    sessions:
      trader:
        senderCompId: QUANTAF_CLIENT
        targetCompId: TARGET_PLATFORM
        host: localhost
        port: 9876

  ai:
    provider: ollama
    model: llama3
    fallbackToTemplates: true

  ledger:
    amountPrecision: 8
    defaultTolerance: 0.0001
```

[Configuration guide →](configuration.md)

---

## 💡 What's Included

✅ **Multi-Protocol Support** — FIX, MQ, REST, ISO 20022 in one framework  
✅ **WireMock-like FIX Stubbing** — `FixStubRegistry` with predicate matching and sequential responses  
✅ **Cross-Source Reconciliation** — Three-way field-by-field comparison with configurable tolerance  
✅ **AI-Powered Testing** — NLP translation and ISO 20022 generation with template fallback  
✅ **Statistical Data Generation** — Gaussian prices, Poisson volumes, Cholesky-correlated series  
✅ **Rich Allure Reporting** — FIX message attachments, reconciliation detail steps  
✅ **CI/CD Ready** — GitHub Actions workflows for build, test, and docs deployment  
✅ **Docker Support** — Docker Compose for ActiveMQ Artemis local development  
✅ **Extensible** — Add custom `MessageBroker`, `LlmProvider`, `BusinessCalendar` implementations  
✅ **Well Documented** — Comprehensive guides with real code examples  

---

## 📄 License

QUANTAF is distributed under the **MIT License**. See [Contributing & License](contributing.md) for details.

---

**Ready to get started?** [Begin with Overview →](overview.md) or [Jump to Examples →](examples.md)
