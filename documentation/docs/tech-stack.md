---
sidebar_position: 3
title: Tech Stack
description: Dependencies, versions, and library details
---

# Tech Stack & Dependencies

## Core Technology Stack

QUANTAF is built on a modern, enterprise-grade technology foundation optimized for financial systems testing.

### Languages & Build Tools

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Primary language, latest LTS release |
| Gradle | 9.3.1 | Build automation with Kotlin DSL (`build.gradle.kts`) |
| Python | 3.11 | Not required — documentation uses Docusaurus (Node.js) |

### Financial Protocol Libraries

#### FIX Protocol (Dual/Triple Version Support)

```gradle
org.quickfixj:quickfixj-core:2.3.1
org.quickfixj:quickfixj-messages-fix42:2.3.1  // FIX 4.2
org.quickfixj:quickfixj-messages-fix44:2.3.1  // FIX 4.4
org.quickfixj:quickfixj-messages-fix50:2.3.1  // FIX 5.0
```

**QuickFIX/J Capabilities:**
- Full FIX protocol stack implementation
- Session management and persistence
- Message parsing and validation
- Order routing simulation

#### SWIFT & Messaging

```gradle
jakarta.jms:jakarta.jms-api:3.1.0                        // JMS standard
org.apache.activemq:artemis-jakarta-client:2.37.0       // Broker support
```

**Messaging Capabilities:**
- JMS-compliant message broker support
- ActiveMQ Artemis integration
- Pluggable MQ adapters

#### REST & HTTP

```gradle
io.rest-assured:rest-assured:5.4.0
```

**REST Capabilities:**
- BDD-style HTTP testing
- OAuth2 support
- Request/response validation
- Header and parameter management

### Data & Statistics

```gradle
org.apache.commons:commons-math3:3.6.1
```

**Math & Statistics:**
- Distribution generation (normal, exponential, Poisson)
- Statistical calculations for market data
- Random data synthesis

### AI & Machine Learning

```gradle
dev.langchain4j:langchain4j:0.35.0
dev.langchain4j:langchain4j-open-ai:0.35.0
dev.langchain4j:langchain4j-ollama:0.35.0
```

**LLM Integration:**
- OpenAI GPT models for NLP
- Ollama for local LLM inference
- Pluggable provider architecture
- NLP-to-FIX translation
- Scenario generation

### Configuration & Serialization

```gradle
com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.0
com.fasterxml.jackson.core:jackson-databind:2.17.0
```

**Configuration Features:**
- YAML-based configuration files
- Environment variable interpolation
- Type-safe config objects

### Logging & Observability

```gradle
ch.qos.logback:logback-classic:1.5.6
org.slf4j:slf4j-api:2.0.13
```

**Logging Capabilities:**
- SLF4J facade for logger independence
- Logback configuration
- Structured logging support
- Async logging for high throughput

### Test Runners & BDD

```gradle
org.testng:testng:7.10.2
io.cucumber:cucumber-java:7.18.0
io.cucumber:cucumber-testng:7.18.0
```

**Testing Frameworks:**
- TestNG: Flexible Java testing framework
- Cucumber: BDD scenario support
- Parallel test execution
- Integrated report generation

### Reporting & Analytics

```gradle
io.qameta.allure:allure-testng:2.27.0
io.qameta.allure:allure-cucumber7-jvm:2.27.0
```

**Allure Reports:**
- Rich HTML reports
- Test timeline visualization
- Failure analysis
- Trend analytics
- Integration with CI/CD

### Assertions & Validation

```gradle
org.assertj:assertj-core:3.26.0
```

**Assertion Library:**
- Fluent assertion API
- Custom matcher support
- Clear error messages

### Infrastructure & CI/CD

```gradle
org.testcontainers:testcontainers:1.20.0
```

**Testcontainers:**
- Docker-based testing
- Ephemeral infrastructure
- Database, broker, and service simulation
- CI/CD pipeline integration

## Dependency Graph

```
QUANTAF Test Framework
├── Protocol Layer
│   ├── FIX (QuickFIX/J 2.3.1 — FIX 4.2, 4.4, 5.0)
│   ├── MQ (Jakarta JMS 3.1.0 + ActiveMQ Artemis 2.37.0)
│   └── REST (RestAssured 5.4.0 + OAuth2)
├── Logic Core
│   ├── MarketMaker (Commons Math 3.6.1)
│   ├── TradeLedger (cross-source reconciliation)
│   └── BusinessCalendar (NYSE, LSE, TSE)
├── AI Layer (LangChain4j 0.35.0)
│   ├── FixScenarioAgent (NLP → FIX)
│   ├── SmartStub (ISO 20022 XML)
│   └── Providers (OpenAI, Ollama)
├── Test Execution
│   ├── TestNG 7.10.2 (QuantafBaseTest)
│   ├── Cucumber 7.18.0 (BDD scenarios)
│   └── Allure 2.27.0 (reporting)
├── Configuration
│   ├── Jackson YAML 2.17.0 (quantaf.yml)
│   └── Logback 1.5.6 + SLF4J 2.0.13
└── Infrastructure
    ├── Docker Compose (ActiveMQ Artemis)
    └── Testcontainers 1.20.0 (CI)
```

## Version Management

All dependencies are centrally managed in `build.gradle.kts` using Gradle Kotlin DSL. The project follows these principles:

- **LTS Versions**: Java 21 (LTS), Jakarta EE stable
- **Latest Patches**: Security updates applied regularly
- **CVE Monitoring**: Dependencies checked for known vulnerabilities
- **Compatibility**: All versions tested for inter-dependency compatibility

## External Services

| Service | Purpose | Configuration |
|---------|---------|---------------|
| OpenAI API | LLM inference for NLP-to-FIX and SWIFT generation | `QUANTAF_AI_API_KEY` env var, configured in `quantaf.yml` → `ai.apiKey` |
| Ollama | Local LLM inference (default provider) | Configured via `quantaf.yml` → `ai.baseUrl` (default: `http://localhost:11434`) |
| Docker | Container runtime for ActiveMQ Artemis | Required for `docker-compose up` and Testcontainers |

## Adding New Dependencies

To add a new dependency to `build.gradle.kts`:

1. Edit `build.gradle.kts`
2. Add to appropriate `dependencies {}` block
3. Run `./gradlew build` to verify compatibility
4. Commit and push

Example:
```kotlin
implementation("com.example:library:1.0.0")
```

## Next Steps

- Explore **[Architecture](architecture.md)** for layered design details
- Review **[Features](features.md)** for capability overview
- Check **[Examples](examples.md)** for usage patterns
