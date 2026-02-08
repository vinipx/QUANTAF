# QUANTAF Documentation

Welcome to the official documentation for **QUANTAF**, the High-Frequency Assurance Engine for Financial Systems.

## 🚀 Quick Start

Get up and running in seconds:

```bash
# Serve documentation locally
./docs.sh

# Build the project
./gradlew build

# Run tests
./gradlew test

# Generate Allure reports
./gradlew allureReport
```

---

## 📚 Documentation Guide

### Core Concepts
- **[Overview](overview.md)** — Purpose, key features, and quick introduction
- **[Architecture](architecture.md)** — 4-layer concentric design with detailed explanations
- **[Features & Capabilities](features.md)** — Protocol support, AI, reporting, extensibility

### Getting Started
- **[Tech Stack](tech-stack.md)** — Dependencies, versions, and library details
- **[Configuration Guide](configuration.md)** — Setup, environment variables, multi-environment
- **[Usage Examples](examples.md)** — TestNG, Cucumber, AI-powered test scenarios

### Advanced Topics
- **[Development Reference](development.md)** — Project structure, extension points, testing guidelines
- **[Allure Reporting](allure.md)** — Report generation, enrichment, failure analysis
- **[CI/CD & Docker](cicd.md)** — GitHub Actions, Jenkins, GitLab, Testcontainers
- **[Contributing & License](contributing.md)** — MIT License, contribution guide, code standards

---

## ✨ Key Highlights

### Protocol Support 🌐
- **FIX** (4.2, 4.4, 5.0) via QuickFIX/J
- **SWIFT** with message validation
- **Message Queues** (ActiveMQ, RabbitMQ, custom)
- **REST** with OAuth2 security

### AI & Intelligent Features 🤖
- NLP-to-FIX translation
- Smart SWIFT stub generation
- LLM-powered scenario generation
- Pluggable LLM providers (OpenAI, Ollama)

### Testing Modes
- **TestNG**: Direct Java testing with full control
- **Cucumber**: BDD scenarios with human-readable steps

### Enterprise Features
- Rich Allure Reports with timeline and trends
- Docker Compose for local development
- Testcontainers for CI/CD
- GitHub Actions, Jenkins, GitLab CI support

---

## 🏗️ Architecture at a Glance

```
Layer 4: Test Definition (TestNG, Cucumber)
    ↓
Layer 3: AI Cortex (LLM, NLP, Smart Stubs)
    ↓
Layer 2: Logic Core (MarketMaker, TradeLedger)
    ↓
Layer 1: Protocol Adapters (FIX, SWIFT, MQ, REST)
    ↓
Financial Systems (Trading, Settlement, Reconciliation)
```

[Learn more →](architecture.md)

---

## 🎯 Use Cases

- **Trading Platform Testing**: Validate order execution, fills, and settlements
- **Reconciliation Systems**: Test transaction matching and discrepancy handling
- **Settlement Engines**: Verify settlement workflows and status transitions
- **High-Frequency Testing**: Load test with thousands of transactions per second
- **Regulatory Compliance**: Generate and test edge cases for compliance scenarios
- **Integration Testing**: Test multiple protocols and services together

---

## 📋 Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| **Java** | 21 (LTS) | Primary language |
| **Gradle** | Latest | Build automation |
| **QuickFIX/J** | 2.3.1 | FIX protocol |
| **RestAssured** | 5.4.0 | REST API testing |
| **LangChain4j** | 0.35.0 | LLM integration |
| **Allure** | 2.27.0 | Test reporting |
| **Docker** | Latest | Container runtime |
| **mkdocs** | Latest | Documentation |

[Full details →](tech-stack.md)

---

## 🔧 Configuration

QUANTAF uses flexible YAML-based configuration with environment variable support:

```yaml
fix:
  host: ${FIX_HOST:localhost}
  port: 9876
rest:
  baseUrl: ${REST_BASE_URL}
  oauth2:
    clientId: ${OAUTH_CLIENT_ID}
ai:
  provider: openai
  openai:
    apiKey: ${OPENAI_API_KEY}
```

[Configuration guide →](configuration.md)

---

## 💡 What's Included

✅ **Multi-Protocol Support** — FIX, SWIFT, MQ, REST in one framework  
✅ **AI-Powered Testing** — NLP translation and scenario generation  
✅ **Rich Reporting** — Allure Reports with analytics and trends  
✅ **CI/CD Ready** — GitHub Actions, Jenkins, GitLab pipelines  
✅ **Docker Support** — Docker Compose for local, Testcontainers for CI  
✅ **Extensible** — Add custom protocols, adapters, and business logic  
✅ **Production Ready** — Used in enterprise financial systems  
✅ **Well Documented** — Comprehensive guides and examples  

---

## 🚀 Getting Help

- **Questions?** Check [FAQ & Troubleshooting](development.md#troubleshooting)
- **Examples?** See [Usage Examples](examples.md)
- **Configuration Help?** Review [Configuration Guide](configuration.md)
- **Contributing?** Read [Contributing Guide](contributing.md)

---

## 📄 License

QUANTAF is distributed under the **MIT License**. See [Contributing & License](contributing.md) for details.

---

**Ready to get started?** [Begin with Overview →](overview.md) or [Jump to Examples →](examples.md)
