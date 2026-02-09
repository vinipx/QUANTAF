---
sidebar_position: 6
title: Features
description: Protocol support, AI, reporting, and extensibility
---

# Features & Capabilities

QUANTAF provides a comprehensive suite of features for enterprise financial system testing.

## Protocol Support 🌐

### FIX Protocol

:::tip[Multi-Version Support]
Full FIX 4.2, 4.4, and 5.0 support through QuickFIX/J integration.
:::

**Session Management (`FixSessionManager`):**

- Manages multiple FIX sessions across different protocol versions simultaneously
- Starts/stops `SocketInitiator` (trader side) and `SocketAcceptor` (exchange stub) instances
- Each version has its own config file (`quickfix-FIX42.cfg`, `quickfix-FIX44.cfg`, `quickfix-FIX50.cfg`)

**WireMock-like Stubbing (`FixStubRegistry`):**

- Predicate-based message matching (`when(predicate).respondWith(generator)`)
- Sequential response support (`respondWith()` → `thenRespondWith()`)
- Configurable response delays (`withDelay(Duration.ofMillis(500))`)
- Call counting and descriptive naming
- Thread-safe via `CopyOnWriteArrayList`

**Message Building (`FixMessageBuilder`):**

- Fluent builder for NewOrderSingle (`35=D`), ExecutionReport (`35=8`), OrderCancelRequest (`35=F`)
- Type-safe enum setters for `Side`, `OrderType`, `TimeInForce`, `ExecType`
- Convenience methods: `fromOrderConfig()`, `rejectionFor()`, `fillFor()`

**Client-Server Architecture:**

- `FixInitiatorWrapper` — Client-side Application that sends orders and correlates responses by ClOrdID using `CompletableFuture`
- `FixAcceptorWrapper` — Server-side Application (exchange stub) that routes incoming messages through `FixInterceptor`
- `FixInterceptor` — Evaluates incoming messages against the stub registry, auto-generates responses with CompID swapping

### Message Queue (MQ)

:::tip[Pluggable MQ Support]
Generic `MessageBroker` interface with ActiveMQ Artemis implementation.
:::

**`MessageBroker` interface provides:**

- `publish(destination, payload)` — Send messages to queues/topics
- `listen(destination, timeout)` — Receive single message with timeout
- `listenWithFilter(destination, predicate, timeout)` — Filtered message receive
- `isConnected()` — Connectivity check
- `close()` — Resource cleanup

**Implementations:**

| Broker | Class | Status |
|--------|-------|--------|
| ActiveMQ Artemis | `ActiveMqBroker` | Fully implemented (Jakarta JMS) |
| IBM MQ | `IbmMqBroker` | Skeleton — ready for IBM MQ client libs |
| Kafka | — | Configuration placeholder in `quantaf.yml` |

### REST API

:::tip[OAuth2-Ready HTTP Testing]
RestAssured wrapper with automatic token lifecycle management.
:::

**`RestClientWrapper` provides:**

- `get(path)`, `get(path, queryParams)` — GET requests with optional query parameters
- `post(path, body)` — POST with JSON body
- `put(path, body)` — PUT with JSON body
- `delete(path)` — DELETE requests
- Automatic OAuth2 bearer token injection via `OAuth2TokenManager`

**`OAuth2TokenManager` features:**

- Acquires tokens via `client_credentials` grant type
- Caches tokens in memory
- Auto-refreshes 60 seconds before expiry
- Thread-safe (`synchronized` methods)
- Manual invalidation support

**`PortfolioApiClient` — typed REST client example:**

- `getPortfolioPosition(accountId)` — Account-level positions
- `getPositionForSymbol(accountId, symbol)` — Symbol-specific positions
- `isTradeSettled(accountId, orderId)` — Settlement status check

### ISO 20022 (SWIFT) XML Generation

:::tip[AI-Powered ISO 20022 Messages]
Generate SWIFT XML messages from natural language intent via `SmartStub`.
:::

**Supported message types (templates):**

| Message | Schema | Description |
|---------|--------|-------------|
| `pacs.008` | `urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02` | FI to FI Customer Credit Transfer |
| `camt.053` | `urn:iso:std:iso:20022:tech:xsd:camt.053.001.02` | Bank to Customer Statement |
| `sese.023` | `urn:iso:std:iso:20022:tech:xsd:sese.023.001.01` | Securities Settlement Transaction |

**Features:**

- LLM-powered generation with SWIFT-specific system prompt
- Template fallback for deterministic CI runs
- Response caching for reproducible test execution
- Pre-loading cached responses for test setup (`cacheResponse(intent, xml)`)

## Test Definition Modes

### TestNG Framework

Run tests by extending `QuantafBaseTest`:

```java
@Epic("Order Lifecycle")
@Feature("High Frequency Orders")
public class HighFrequencyOrderTest extends QuantafBaseTest {

    @BeforeMethod
    public void resetState() {
        stubRegistry.reset();
        ledger.clear();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Order Fill")
    @Description("Verifies a successful limit order fill with cross-source reconciliation")
    public void testLimitOrderFillReconciliation() {
        String clOrdId = marketMaker.generateClOrdId();
        BigDecimal price = marketMaker.generatePrice(150.0, 2.0);
        int volume = marketMaker.generateVolume(500);
        LocalDate settlementDate = marketMaker.generateTradeDate(SettlementType.T2);

        // Configure stub, build order, ingest records, reconcile
        // ...

        TradeLedgerAssert.assertThat(result)
            .assertParity()
            .assertSettlementDateMatch()
            .assertAmountMatch(BigDecimal.valueOf(0.01));
    }
}
```

**TestNG Suite Configuration (`testng.xml`):**

```xml
<suite name="QUANTAF Test Suite" parallel="classes" thread-count="4">
    <test name="Unit Tests">
        <classes>
            <class name="io.github.vinipx.quantaf.unit.MarketMakerTest"/>
            <class name="io.github.vinipx.quantaf.unit.FixStubRegistryTest"/>
            <class name="io.github.vinipx.quantaf.unit.TradeLedgerAssertTest"/>
        </classes>
    </test>
    <test name="Scenario Tests">
        <classes>
            <class name="io.github.vinipx.quantaf.scenarios.HighFrequencyOrderTest"/>
        </classes>
    </test>
    <test name="BDD Tests">
        <classes>
            <class name="io.github.vinipx.quantaf.bdd.runners.CucumberTestRunner"/>
        </classes>
    </test>
</suite>
```

### Cucumber BDD

Write business-readable Gherkin scenarios:

```gherkin
Feature: Order Lifecycle
  As a trading platform QA engineer
  I want to validate the full order lifecycle
  So that I can ensure correct trade processing across FIX, MQ, and API

  @smoke @fix
  Scenario: Successful limit order fill
    Given a FIX session is available for version "FIX44"
    When I submit a "BUY" "LIMIT" order for "AAPL" at price 150.00 with quantity 100
    Then the order should be filled at price 150.00
    And the trade should reconcile across all sources

  @regression @ai
  Scenario: AI-generated order scenario
    Given the AI scenario agent is available
    When I generate an order from intent "Buy 500 shares of MSFT Market On Close"
    Then the generated order should have side "BUY"
    And the generated order should have symbol "MSFT"
    And the generated order should have time in force "AT_CLOSE"

  @regression @reconciliation
  Scenario: Cross-source trade reconciliation
    Given matching trade records exist for order "RECON-001"
      | source | symbol | price  | quantity | settlementDate |
      | FIX    | GOOG   | 175.50 | 200      | 2026-02-10     |
      | MQ     | GOOG   | 175.50 | 200      | 2026-02-10     |
      | API    | GOOG   | 175.50 | 200      | 2026-02-10     |
    When I reconcile the trade "RECON-001"
    Then the reconciliation should pass
    And all fields should match across sources
```

## AI & Intelligent Features 🤖

### NLP-to-FIX Translation (`FixScenarioAgent`)

Convert natural language descriptions to structured `OrderConfiguration` objects:

| Input | Extracted Fields |
|-------|-----------------|
| `"Limit Buy 500 shares of AAPL at 150"` | Side=BUY, Type=LIMIT, Symbol=AAPL, Qty=500, Price=150 |
| `"Sell TSLA stop at 200"` | Side=SELL, Type=STOP, Symbol=TSLA, Price=200 |
| `"Buy GOOG Market On Close"` | Side=BUY, Type=MARKET, Symbol=GOOG, TIF=AT_CLOSE |
| `"Order for MSFT that triggers a fat-finger rejection at 9999"` | Symbol=MSFT, ExecType=REJECTED, Price=9999 |

**Dual-mode operation:**

1. **LLM mode**: Sends structured system prompt to LLM, parses JSON response into `OrderConfiguration`
2. **Template mode**: Keyword-based deterministic extraction using regex patterns and a known symbol dictionary

**Known symbol dictionary:** AAPL (Apple), GOOG (Google), MSFT (Microsoft), TSLA (Tesla), AMZN (Amazon)

:::tip[AI Fallback Strategy]
When `fallbackToTemplates` is enabled (default: `true`), the agent first tries LLM. If unavailable, it falls back to templates — ensuring tests **never depend on external LLM availability**.
:::

### Smart ISO 20022 Stub Generation (`SmartStub`)

Generate ISO 20022 (SWIFT) XML messages from intent descriptions:

```java
SmartStub stub = new SmartStub(); // template-only

// Generate from intent
String xml = stub.generateSwiftMessage("Generate a credit transfer for $1000");
// → pacs.008 XML with credit transfer structure

// Pre-load cached responses for deterministic tests
stub.cacheResponse("settlement instruction", myXml);
```

## Cross-Source Reconciliation 🔄

### Three-Way Comparison (`TradeLedger`)

The reconciliation engine compares trade records from three sources (FIX, MQ, API):

**Compared fields:**

| Field | Comparison Type | Notes |
|-------|----------------|-------|
| `price` | Numeric (within tolerance) | Normalized to configurable precision |
| `quantity` | Numeric (within tolerance) | |
| `amount` | Numeric (within tolerance) | |
| `settlementDate` | Exact match | `LocalDate` comparison |
| `symbol` | Exact match | String equality |
| `currency` | Exact match | String equality |
| `account` | Exact match | String equality |

**Numeric tolerance:** Default `0.0001` with 8-digit precision (`MathContext(8, HALF_EVEN)`).

### Assertion DSL (`TradeLedgerAssert`)

Fluent assertion API for reconciliation results:

```java
TradeLedgerAssert.assertThat(result)
    .assertParity()                             // All fields match
    .assertSettlementDateMatch()                // Settlement dates match
    .assertAmountMatch(BigDecimal.valueOf(0.01))// Amounts within tolerance
    .assertFieldMatch("symbol");                // Specific field match
```

## Realistic Data Generation 📊

### Statistical Distributions (`MarketMaker`)

| Method | Distribution | Parameters | Output |
|--------|-------------|------------|--------|
| `generatePrice(mean, stdDev)` | Normal (Gaussian) | mean, standard deviation | `BigDecimal` price (always positive) |
| `generateVolume(lambda)` | Poisson | expected mean | `int` volume (minimum 1) |
| `generateCorrelatedPrices(mean, stdDev, corr, count)` | Cholesky decomposition | correlation coefficient | `List<BigDecimal>` correlated series |
| `generateTradeDate(settlementType)` | — | T0, T1, T2 | `LocalDate` (business day adjusted) |
| `generateTradeTimestamp()` | Uniform | Market hours 9:30–16:00 ET | `LocalDateTime` |
| `generateClOrdId()` | — | — | `String` `"QUANTAF-{timestamp}-{random}"` |
| `generateAccountId(prefix)` | — | prefix string | `String` `"{prefix}-{8digits}"` |

### Business Calendar Support

Pre-configured market calendars:

| Calendar | Factory Method | Holidays |
|----------|---------------|----------|
| NYSE | `BusinessCalendar.nyse()` | New Year, July 4th, Christmas |
| LSE | `BusinessCalendar.lse()` | New Year, Christmas, Boxing Day |
| TSE | `BusinessCalendar.tse()` | New Year (Jan 1-3), New Year's Eve |

Custom calendars:

```java
BusinessCalendar hkex = new BusinessCalendar("HKEX", Set.of(), Set.of(
    MonthDay.of(10, 1), MonthDay.of(7, 1)));
hkex.withHoliday(LocalDate.of(2026, 2, 12));

LocalDate settlement = hkex.addBusinessDays(LocalDate.now(), 2);
int days = hkex.businessDaysBetween(start, end);
boolean isOpen = hkex.isBusinessDay(date);
```

## Extensibility

Extend QUANTAF at every layer:

| Extension Point | Interface/Class | How |
|----------------|----------------|-----|
| New message broker | `MessageBroker` | Implement `publish()`, `listen()`, `listenWithFilter()`, `isConnected()`, `close()` |
| New LLM provider | `LlmProvider` | Implement `complete()`, `getProviderName()`, `getModelName()`, `isAvailable()` |
| New market calendar | `BusinessCalendar` | Create with constructor or add factory method |
| New test scenarios | `QuantafBaseTest` | Extend and get all framework components for free |
| Custom FIX stubs | `FixStubRegistry` | Register with `when(predicate).respondWith(generator)` |

## Next Steps

- See **[Development](development.md)** for extending QUANTAF with code examples
- Review **[Examples](examples.md)** for real test patterns from the codebase
- Check **[Configuration](configuration.md)** for the full `quantaf.yml` reference
