# QUANTAF - The High-Frequency Assurance Engine

[![Java 21](https://img.shields.io/badge/Java-21_LTS-ED8936?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-Latest-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=github&logoColor=white)](/.github/workflows/)
[![Docker](https://img.shields.io/badge/Docker-Latest-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![QuickFIX/J](https://img.shields.io/badge/QuickFIX%2FJ-2.3.1-blue)](https://github.com/quickfix-j/quickfixj)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-0.35.0-orange)](https://github.com/langchain4j/langchain4j)
[![Allure Reports](https://img.shields.io/badge/Allure-2.27.0-FF6B00)](https://qameta.io/allure/)

> A robust, enterprise-grade test automation framework for validating complex financial transaction lifecycles across heterogeneous protocols (FIX, SWIFT, MQ, REST) with mathematical precision.

---

## 🎯 Overview

QUANTAF is the **High-Frequency Assurance Engine** designed for testing mission-critical financial systems. It provides a comprehensive, layered architecture that separates protocol handling, business logic, AI/ML features, and test execution into cleanly decoupled, independently testable components.

### Perfect For:

- 📈 **Trading Platforms** — End-to-end order execution testing
- 🔄 **Settlement Engines** — Reconciliation and settlement validation
- 💰 **Financial Institutions** — Compliance and regulatory testing
- ⚡ **High-Frequency Systems** — Load testing with 1000s of transactions/sec
- 🤖 **AI-Driven Testing** — NLP-powered scenario generation

---

## 🏗️ Architecture

QUANTAF is structured in **4 Concentric Layers** for clean separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│  Layer 4: Test Definition                               │
│  TestNG | Cucumber BDD | Scenario-Driven Tests          │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│  Layer 3: AI Cortex                                     │
│  NLP-to-FIX | Smart SWIFT | LLM Providers               │
│  LangChain4j | OpenAI | Ollama                          │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│  Layer 2: Logic Core                                    │
│  MarketMaker | TradeLedger | Domain Models              │
│  Business Rules | Reconciliation                        │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│  Layer 1: Protocol Adapters                             │
│  FIX (4.2/4.4/5.0) | SWIFT | MQ | REST OAuth2          │
│  QuickFIX/J | RestAssured | JMS                         │
└──────────────────────┬──────────────────────────────────┘
                       ↓
            Financial Systems Under Test
```

[📖 Learn More About Architecture →](docs/architecture.md)

---

## ✨ Key Features

### Protocol Support 🌐

| Protocol | Version | Features |
|----------|---------|----------|
| **FIX** | 4.2, 4.4, 5.0 | Full message types, session management, persistence |
| **SWIFT** | All MT Categories | Message validation, schema compliance, stubs |
| **Message Queue** | ActiveMQ, RabbitMQ, Custom | JMS, broker abstraction, pluggable |
| **REST** | HTTP/HTTPS | OAuth2, request/response logging, assertions |

### AI & Intelligent Features 🤖

- **NLP-to-FIX Translation** — Convert natural language to FIX messages
- **Smart SWIFT Generation** — Context-aware, realistic SWIFT message stubs
- **Scenario Generation** — AI-powered edge case and load test generation
- **Pluggable LLMs** — OpenAI (GPT-4), Ollama (local), custom providers

### Testing Modes

- **TestNG** — Direct Java testing with full IDE support and debugging
- **Cucumber BDD** — Human-readable feature files for non-technical stakeholders

### Enterprise Features

✅ **Rich Reporting** — Allure Reports with timeline, trends, and analytics  
✅ **CI/CD Ready** — GitHub Actions, Jenkins, GitLab CI/CD integration  
✅ **Local Development** — Docker Compose with pre-configured services  
✅ **Container Testing** — Testcontainers for ephemeral infrastructure  
✅ **Security** — OAuth2, TLS/SSL, encrypted messaging  
✅ **Extensibility** — Custom protocol adapters, business logic, assertions  
✅ **Performance** — Sub-millisecond latency, 1000s of transactions/sec  
✅ **Documentation** — Comprehensive guides and 100+ code examples  

---

## 🛠️ Tech Stack

### Core Technologies

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 21 (LTS) |
| **Build** | Gradle | Latest |
| **FIX Protocol** | QuickFIX/J | 2.3.1 |
| **HTTP Client** | RestAssured | 5.4.0 |
| **Math/Stats** | Apache Commons Math | 3.6.1 |
| **LLM Integration** | LangChain4j | 0.35.0 |
| **Test Runners** | TestNG | 7.10.2 |
| **BDD Framework** | Cucumber | 7.18.0 |
| **Reporting** | Allure | 2.27.0 |
| **Messaging** | Jakarta JMS | 3.1.0 |
| **Containers** | Testcontainers | 1.20.0 |
| **Docker** | Docker Compose | Latest |

[📚 Full Tech Stack Details →](docs/tech-stack.md)

---

## 🚀 Quick Start

### Prerequisites

- **Java 21+** ([Install](https://www.oracle.com/java/technologies/downloads/))
- **Gradle** (included via gradlew)
- **Docker** & **Docker Compose** (for local services)
- **mkdocs** is no longer required — documentation uses [Docusaurus](https://docusaurus.io/) (requires Node.js ≥ 18)

### Setup & Run

```bash
# 1. Clone the repository
git clone https://github.com/vinipx/QUANTAF.git
cd QUANTAF

# 2. Serve documentation locally
./docs.sh
# Opens http://localhost:3000

# 3. Start local services
docker-compose up -d

# 4. Build project
./gradlew build

# 5. Run tests
./gradlew test

# 6. Generate Allure report
./gradlew allureReport
# Open: build/reports/allure-report/index.html

# 7. Stop documentation server
./docs.sh stop
```

### Simple Example

```java
@Test
public void testBuyOrderExecution() throws IOException {
    // Setup
    FixAdapter adapter = new FixAdapter();
    adapter.connect("localhost", 9876);
    
    // Create order
    FixMessage order = new FixMessageBuilder()
        .setSymbol("AAPL")
        .setOrderQty(1000)
        .setPrice("150.25")
        .setSide("BUY")
        .build();
    
    // Send & verify
    adapter.send(order);
    FixMessage execution = adapter.receive(5000);
    
    assertThat(execution).isNotNull();
    assertThat(execution.getString(35)).isEqualTo("8"); // ExecutionReport
}
```

[📖 More Examples →](docs/examples.md)

---

## 📖 Documentation

Complete, professional documentation is included and available online:

### Core Concepts
- **[Overview](docs/overview.md)** — Purpose, benefits, target audience
- **[Architecture](docs/architecture.md)** — 4-layer design with data flow
- **[Features](docs/features.md)** — Complete feature list and capabilities

### Getting Started
- **[Tech Stack](docs/tech-stack.md)** — Dependencies and versions
- **[Configuration](docs/configuration.md)** — YAML setup, env vars, multi-environment
- **[Examples](docs/examples.md)** — TestNG, Cucumber, AI-powered scenarios

### Advanced
- **[Development Reference](docs/development.md)** — Project structure, extensions, testing
- **[Allure Reporting](docs/allure.md)** — Report generation and analysis
- **[CI/CD & Docker](docs/cicd.md)** — GitHub Actions, Jenkins, GitLab, Testcontainers
- **[Contributing](docs/contributing.md)** — MIT License, contribution guide

**[👉 View Full Documentation](docs/index.md)**

---

## 🏢 Project Structure

```
QUANTAF/
├── src/main/java/io/github/vinipx/quantaf/
│   ├── core/                 # Business logic (MarketMaker, TradeLedger)
│   ├── protocol/
│   │   ├── fix/              # FIX protocol adapter
│   │   ├── swift/            # SWIFT protocol adapter
│   │   ├── mq/               # Message queue adapter
│   │   └── rest/             # REST client with OAuth2
│   ├── ai/                   # AI/LLM integration (NLP, scenario gen)
│   ├── config/               # Configuration management
│   └── reporting/            # Allure integration
│
├── src/test/java/            # Test suites (TestNG & Cucumber)
├── src/test/resources/
│   ├── features/             # Cucumber feature files
│   └── quantaf.yml           # Test configuration
│
├── documentation/            # Documentation site (Docusaurus)
│   ├── docs/                 # Documentation source (Markdown)
│   ├── src/                  # React components & custom CSS
│   └── docusaurus.config.js  # Site configuration
├── docker-compose.yml        # Local services
├── build.gradle.kts          # Build configuration
├── docs.sh                   # Documentation server launcher
└── README.md                 # This file
```

[📂 Development Reference →](docs/development.md#directory-structure)

---

## 🔧 Configuration

QUANTAF uses **YAML-based configuration** with environment variable support:

```yaml
# src/main/resources/quantaf.yml
fix:
  host: ${FIX_HOST:localhost}
  port: 9876
  sender_id: TEST_CLIENT

rest:
  baseUrl: ${REST_BASE_URL}
  oauth2:
    clientId: ${OAUTH_CLIENT_ID}
    clientSecret: ${OAUTH_CLIENT_SECRET}

ai:
  provider: openai
  openai:
    apiKey: ${OPENAI_API_KEY}
    model: gpt-4

database:
  url: ${DB_URL:jdbc:postgresql://localhost:5432/quantaf}
```

**Environment Variables:**
```bash
export FIX_HOST=trading-server.example.com
export OPENAI_API_KEY=sk-...
export OAUTH_CLIENT_ID=...
```

[🔧 Full Configuration Guide →](docs/configuration.md)

---

## 🤖 AI-Powered Testing

### NLP-to-FIX Translation

Convert natural language to FIX messages automatically:

```java
FixScenarioAgent agent = new FixScenarioAgent();
String description = "Send a buy order for 1000 shares of AAPL at $150.25";
FixMessage message = agent.translateNlp(description);
// Automatically generates proper FIX message with all fields
```

### Smart SWIFT Stub Generation

Generate realistic SWIFT messages based on context:

```java
SmartStubGenerator generator = new SmartStubGenerator();
SwiftMessage stub = generator.generate(trade, settlementDetails);
// Creates valid, context-aware SWIFT message
```

### Scenario Generation

Use LLMs to create edge cases and load test scenarios:

```java
List<FixMessage> scenarios = agent.generateScenario(
    "Test partial fills with price slippage and order rejection"
);
```

[🤖 AI Features Details →](docs/features.md#ai--intelligent-features-)

---

## 📊 CI/CD & Automation

### GitHub Actions
Automatic build, test, and documentation publish on push:

```yaml
# .github/workflows/build.yml
- Build project
- Run all tests  
- Generate Allure reports
- Publish docs to GitHub Pages
```

### Docker & Testcontainers
- **Docker Compose** for local development
- **Testcontainers** for ephemeral infrastructure in CI
- Automatic service startup and cleanup

### Multi-Pipeline Support
✅ GitHub Actions | ✅ Jenkins | ✅ GitLab CI | ✅ Custom

[🚀 CI/CD Details →](docs/cicd.md)

---

## 🧪 Testing Modes

### TestNG (Direct Java)

```java
@Test
public void testTradeExecution() {
    // Full IDE support, debugging, assertions
    Trade trade = tradeLedger.findLatest();
    assertThat(trade.getStatus()).isEqualTo(EXECUTED);
}
```

### Cucumber BDD (Human-Readable)

```gherkin
Feature: Trade Settlement
  Scenario: Successfully settle a trade
    Given a trader sends a buy order for 1000 shares at $150.25
    When the market accepts the order
    Then the trade ledger should record a confirmed trade
```

[📋 Examples & Patterns →](docs/examples.md)

---

## 📈 Reporting

### Allure Reports
Rich, interactive HTML reports with:

✨ **Timeline View** — Visualize test execution over time  
✨ **Trends** — Historical pass/fail analytics  
✨ **Severity Filtering** — Focus on critical tests  
✨ **Attachments** — Logs, data, screenshots  
✨ **Step-by-Step Breakdown** — Detailed execution traces  

```bash
./gradlew allureReport
open build/reports/allure-report/index.html
```

[📊 Reporting Details →](docs/allure.md)

---

## 🔒 Security

- **OAuth2** — Secure REST API testing
- **TLS/SSL** — Encrypted connections
- **Message Encryption** — PGP signing support
- **Credential Management** — Environment variable isolation
- **Regular CVE Scanning** — Dependency security checks

---

## 🎯 Use Cases

| Use Case | How QUANTAF Helps |
|----------|-------------------|
| **Trading System Testing** | Multi-protocol support, realistic data generation, high-frequency capable |
| **Settlement Validation** | Reconciliation logic, ledger tracking, end-to-end scenarios |
| **Compliance Testing** | Edge case generation, regulatory scenario creation, audit trails |
| **Load Testing** | 1000s transactions/sec, statistical data, performance monitoring |
| **Integration Testing** | Multi-protocol, Docker services, CI/CD ready |
| **Regression Testing** | BDD scenarios, detailed reporting, trend analysis |

---

## 🤝 Contributing

We welcome contributions! Here's how:

1. **Fork** the repository
2. **Create** a feature branch (`feature/amazing-feature`)
3. **Code** with tests and documentation
4. **Commit** with clear messages
5. **Push** to your fork
6. **Create** a Pull Request

### Code Standards
- Follow Google Java Style Guide
- Write tests for all features
- Update documentation
- Run full test suite before PR

[📝 Full Contributing Guide →](docs/contributing.md)

---

## 📄 License

QUANTAF is distributed under the **MIT License** — free to use, modify, and distribute.

See [LICENSE](LICENSE) file for full details.

---

## 🚀 Next Steps

- **📖 [Read the Documentation](docs/index.md)** — Comprehensive guides and examples
- **⚡ [Try Quick Start](#-quick-start)** — Get running in 5 minutes
- **💬 [Join Discussions](https://github.com/vinipx/QUANTAF/discussions)** — Ask questions
- **🐛 [Report Issues](https://github.com/vinipx/QUANTAF/issues)** — Help us improve
- **⭐ [Star the Repo](https://github.com/vinipx/QUANTAF)** — Show your support

---

## 📞 Support

- **Questions?** → [GitHub Discussions](https://github.com/vinipx/QUANTAF/discussions)
- **Found a bug?** → [GitHub Issues](https://github.com/vinipx/QUANTAF/issues)
- **Documentation?** → [Full Docs](docs/index.md)
- **Examples?** → [Usage Examples](docs/examples.md)

---

<div align="center">

**Made with ❤️ for Financial Systems Testing**

[⭐ Star us on GitHub](https://github.com/vinipx/QUANTAF) • [📖 Read the Docs](docs/index.md) • [💬 Discussions](https://github.com/vinipx/QUANTAF/discussions)

</div>
