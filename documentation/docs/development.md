---
sidebar_position: 7
title: Development
description: Project structure, extension points, and testing guidelines
---

# Development Reference

## Project Structure

```
QUANTAF/
├── src/main/java/io/github/vinipx/quantaf/
│   ├── config/                        # Configuration & environment
│   │   ├── QuantafConfig.java         # Singleton YAML config loader
│   │   └── EnvironmentResolver.java   # LOCAL/CI/STAGING detection
│   ├── core/                          # Business logic layer
│   │   ├── MarketMaker.java           # Statistical data generation
│   │   ├── TradeLedger.java           # Cross-source reconciliation
│   │   ├── TradeLedgerAssert.java     # Assertion DSL for reconciliation
│   │   ├── BusinessCalendar.java      # NYSE/LSE/TSE business day logic
│   │   └── model/                     # Domain models
│   │       ├── OrderConfiguration.java  # Order config with Side/OrderType/TIF enums
│   │       ├── TradeRecord.java         # Normalized trade record (FIX/MQ/API)
│   │       └── ReconciliationResult.java# Field-level comparison results
│   ├── protocol/                      # Protocol adapters layer
│   │   ├── fix/                       # FIX protocol (QuickFIX/J)
│   │   │   ├── FixSessionManager.java   # Multi-version session lifecycle
│   │   │   ├── FixInitiatorWrapper.java # Client-side (sends orders)
│   │   │   ├── FixAcceptorWrapper.java  # Server-side (exchange stub)
│   │   │   ├── FixInterceptor.java      # Evaluates stubs, sends responses
│   │   │   ├── FixStubRegistry.java     # WireMock-like predicate→response registry
│   │   │   ├── FixMessageBuilder.java   # Fluent builder for FIX messages
│   │   │   └── FixVersion.java          # FIX42/FIX44/FIX50 enum
│   │   ├── mq/                        # Message queue adapters
│   │   │   ├── MessageBroker.java       # Pluggable interface
│   │   │   ├── ActiveMqBroker.java      # ActiveMQ Artemis (Jakarta JMS)
│   │   │   └── IbmMqBroker.java         # IBM MQ skeleton
│   │   └── rest/                      # REST API adapter
│   │       ├── RestClientWrapper.java   # RestAssured wrapper with OAuth2
│   │       ├── OAuth2TokenManager.java  # Token lifecycle management
│   │       └── PortfolioApiClient.java  # Typed portfolio REST client
│   ├── ai/                            # AI Cortex layer
│   │   ├── LlmProvider.java            # Pluggable LLM interface
│   │   ├── FixScenarioAgent.java        # NLP → OrderConfiguration
│   │   ├── SmartStub.java               # Intent → ISO 20022 XML
│   │   └── providers/
│   │       ├── OpenAiProvider.java      # OpenAI via LangChain4j
│   │       └── OllamaProvider.java      # Local Ollama via LangChain4j
│   └── reporting/                     # Allure integration
│       ├── AllureFixAttachment.java     # FIX message formatting & attachment
│       └── ReconciliationReportStep.java# Reconciliation as Allure steps
├── src/main/resources/
│   ├── quantaf.yml                    # Framework configuration
│   ├── quickfix-FIX42.cfg            # QuickFIX/J config for FIX 4.2
│   ├── quickfix-FIX44.cfg            # QuickFIX/J config for FIX 4.4
│   ├── quickfix-FIX50.cfg            # QuickFIX/J config for FIX 5.0
│   └── logback.xml                    # Logging configuration
├── src/test/java/io/github/vinipx/quantaf/
│   ├── unit/                          # Unit tests
│   │   ├── MarketMakerTest.java         # Statistical validation
│   │   ├── FixStubRegistryTest.java     # Stub matching & message builder
│   │   └── TradeLedgerAssertTest.java   # Reconciliation & assertions
│   ├── scenarios/                     # TestNG scenario tests
│   │   ├── QuantafBaseTest.java         # Abstract base (BeforeSuite/AfterSuite)
│   │   └── HighFrequencyOrderTest.java  # Full workflow tests
│   └── bdd/                           # Cucumber BDD
│       ├── runners/
│       │   └── CucumberTestRunner.java  # TestNG-Cucumber bridge
│       └── steps/
│           └── OrderStepDefs.java       # Step definitions
├── src/test/resources/
│   ├── features/
│   │   └── order_lifecycle.feature    # BDD feature file (4 scenarios)
│   └── testng.xml                     # TestNG suite configuration
├── build.gradle.kts                   # Gradle build (Kotlin DSL)
├── settings.gradle.kts                # Project name: QUANTAF
├── gradle.properties                  # JVM args, parallel, caching
├── docker-compose.yml                 # ActiveMQ Artemis local service
├── documentation/                     # Documentation site (Docusaurus)
│   ├── docs/                          # Documentation source (Markdown)
│   ├── src/                           # React components & custom CSS
│   ├── static/                        # Static assets (logo, images)
│   ├── docusaurus.config.js           # Site configuration
│   ├── sidebars.js                    # Navigation sidebar
│   └── package.json                   # Node.js dependencies
├── docs.sh                            # Documentation server launcher (zsh)
├── .github/workflows/
│   ├── ci.yml                         # Build & test pipeline
│   └── docs.yml                       # Docs deployment to GitHub Pages
└── README.md
```

## Key Classes & Interfaces

### MessageBroker (Layer 1 — Protocol Adapters)

The pluggable interface for message broker interactions:

```java
public interface MessageBroker {
    void publish(String destination, String payload);
    CompletableFuture<String> listen(String destination, Duration timeout);
    CompletableFuture<String> listenWithFilter(
            String destination, Predicate<String> filter, Duration timeout);
    boolean isConnected();
    void close();
}
```

### FixStubRegistry (Layer 1 — FIX Protocol)

WireMock-like API for configuring exchange stub responses:

```java
// Register a stub: reject AAPL orders with fat-finger prices
stubRegistry.when(msg -> {
    try {
        return msg.getString(Symbol.FIELD).equals("AAPL")
            && msg.getDecimal(Price.FIELD).doubleValue() > 5000;
    } catch (Exception e) {
        return false;
    }
}).respondWith(req -> FixMessageBuilder.rejectionFor(req, FixVersion.FIX44, "Fat-finger check"))
  .describedAs("AAPL fat-finger rejection")
  .register();

// Sequential responses: first partial fill, then full fill
stubRegistry.when(msg -> true)
    .respondWith(req -> buildPartialFill(req))
    .thenRespondWith(req -> buildFill(req))
    .withDelay(Duration.ofMillis(100))
    .describedAs("Two-phase fill")
    .register();
```

### FixMessageBuilder (Layer 1 — FIX Protocol)

Fluent builder for constructing FIX messages:

```java
// Build a NewOrderSingle (35=D)
Message order = FixMessageBuilder.newOrderSingle(FixVersion.FIX44)
        .clOrdId("ORD-001")
        .symbol("MSFT")
        .side(OrderConfiguration.Side.BUY)
        .orderType(OrderConfiguration.OrderType.LIMIT)
        .price(BigDecimal.valueOf(305.50))
        .quantity(500)
        .account("FUND-001")
        .timeInForce(OrderConfiguration.TimeInForce.DAY)
        .transactTimeNow()
        .build();

// Convenience: build from OrderConfiguration
Message order = FixMessageBuilder.fromOrderConfig(config, FixVersion.FIX44);

// Convenience: create rejection/fill ExecutionReports
Message rejection = FixMessageBuilder.rejectionFor(order, FixVersion.FIX44, "Price too high");
Message fill = FixMessageBuilder.fillFor(order, FixVersion.FIX44, BigDecimal.valueOf(305.25));
```

### MarketMaker (Layer 2 — Logic Core)

Statistical distribution engine for realistic financial test data:

```java
MarketMaker mm = new MarketMaker(BusinessCalendar.nyse());

// Normal (Gaussian) distribution for prices
BigDecimal price = mm.generatePrice(150.0, 2.0);

// Poisson distribution for volumes
int volume = mm.generateVolume(500);

// Cholesky-decomposed correlated price series
List<BigDecimal> prices = mm.generateCorrelatedPrices(100.0, 5.0, 0.8, 20);

// T+N settlement dates (skips weekends + holidays)
LocalDate settle = mm.generateTradeDate(SettlementType.T2);

// Market-hours timestamps (9:30–16:00 ET)
LocalDateTime timestamp = mm.generateTradeTimestamp();

// Unique identifiers
String clOrdId = mm.generateClOrdId();   // "QUANTAF-1738000000-4821"
String account = mm.generateAccountId("FUND"); // "FUND-12345678"
```

### TradeLedger (Layer 2 — Logic Core)

Cross-source reconciliation engine with three-way comparison:

```java
TradeLedger ledger = new TradeLedger(8, BigDecimal.valueOf(0.0001));

// Add records from all three sources
ledger.addRecord(TradeRecord.fromFix().clOrdId("ORD-1").symbol("AAPL")
        .price(price).quantity(qty).amount(amount)
        .currency("USD").settlementDate(date));
ledger.addRecord(TradeRecord.fromMq().clOrdId("ORD-1").symbol("AAPL")
        .price(price).quantity(qty).amount(amount)
        .currency("USD").settlementDate(date));
ledger.addRecord(TradeRecord.fromApi().clOrdId("ORD-1").symbol("AAPL")
        .price(price).quantity(qty).amount(amount)
        .currency("USD").settlementDate(date));

// Reconcile a single key
ReconciliationResult result = ledger.reconcile("ORD-1");

// Fluent assertion DSL
TradeLedgerAssert.assertThat(result)
    .assertParity()
    .assertSettlementDateMatch()
    .assertAmountMatch(BigDecimal.valueOf(0.01))
    .assertFieldMatch("symbol");

// Reconcile all known keys
List<ReconciliationResult> allResults = ledger.reconcileAll();

// Verify rejection handling
boolean rejected = ledger.verifyRejectionHandled("AAPL");
```

### LlmProvider (Layer 3 — AI Cortex)

Pluggable interface for LLM backends:

```java
public interface LlmProvider {
    String complete(String systemPrompt, String userMessage);
    String getProviderName();
    String getModelName();
    boolean isAvailable();
}
```

**Implementations:**

| Class | Backend | Key Config |
|-------|---------|------------|
| `OpenAiProvider` | OpenAI via `langchain4j-open-ai` | API key, model name, temperature=0.1 |
| `OllamaProvider` | Local Ollama via `langchain4j-ollama` | Base URL (default `http://localhost:11434`), model name, temperature=0.1 |

## Build & Test Commands

```bash
./gradlew build          # Build without tests
./gradlew build -x test  # Explicitly skip tests
./gradlew test           # Run all tests
./gradlew test --tests "io.github.vinipx.quantaf.unit.MarketMakerTest"  # Specific class
./gradlew allureReport   # Generate Allure report
./docs.sh                # Serve documentation (http://localhost:3000)
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 9876/9877 already in use | `lsof -ti tcp:9876 \| xargs kill` |
| Gradle out of memory | Increase JVM args in `gradle.properties`: `org.gradle.jvmargs=-Xmx2048m` |
| LLM API errors | Check `QUANTAF_AI_API_KEY` env var, or ensure `fallbackToTemplates: true` in `quantaf.yml` |
| Allure report empty | Verify `build/allure-results/` has JSON files after test run |
| mkdocs not found | mkdocs has been replaced by Docusaurus. Run `./docs.sh` (requires Node.js ≥ 18) |
| Test flakiness in CI | Statistical tests use large sample sizes (10k) with generous tolerances |
