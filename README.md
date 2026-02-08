# QUANTAF - The High-Frequency Assurance Engine

A robust, high-performance test automation framework for validating complex financial transaction lifecycles across heterogeneous protocols (FIX, SWIFT, MQ, REST) with mathematical precision.

## Architecture

QUANTAF is designed in **4 Concentric Layers**:

1. **Protocol Adapters** - FIX (4.2/4.4/5.0), MQ (pluggable), REST with OAuth2
2. **Logic Core** - MarketMaker (statistical data), TradeLedger (reconciliation)
3. **AI Cortex** - NLP-to-FIX translation, Smart SWIFT stub generation
4. **Test Definition** - TestNG + Cucumber BDD dual runner

## Tech Stack

- **Java 21** with Gradle (Kotlin DSL)
- **QuickFIX/J** - FIX protocol engine (multi-version)
- **RestAssured** - REST API testing
- **Apache Commons Math** - Statistical distributions
- **LangChain4j** - Pluggable LLM integration
- **Allure Reports** - Rich test reporting
- **Testcontainers** - CI infrastructure
- **Docker Compose** - Local dev services

## Quick Start

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Generate Allure report
./gradlew allureReport
```

## Project Structure

```
src/main/java/io/github/vinipx/quantaf/
├── core/           # MarketMaker, TradeLedger, domain models
├── protocol/
│   ├── fix/        # FIX engine, stub registry, message builder
│   ├── mq/         # Pluggable message broker interface
│   └── rest/       # REST client with OAuth2
├── ai/             # LLM providers, FixScenarioAgent, SmartStub
├── config/         # YAML-based configuration
└── reporting/      # Allure report utilities
```

## Configuration

All settings are in `src/main/resources/quantaf.yml`. Environment variables are supported via `${VAR_NAME}` syntax.

## License

MIT
