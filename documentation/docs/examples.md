---
sidebar_position: 8
title: Examples
description: TestNG, Cucumber BDD, and AI-powered test scenarios
---

# Usage Examples

All examples below use actual QUANTAF classes and APIs. They are drawn from the real test code in the repository.

## Quick Start

### 1. Clone & Build

```bash
git clone https://github.com/vinipx/QUANTAF.git
cd QUANTAF
./gradlew build
./gradlew test
./gradlew allureReport
```

### 2. Run Specific Tests

```bash
# Unit tests only
./gradlew test --tests "io.github.vinipx.quantaf.unit.*"

# Scenario tests
./gradlew test --tests "io.github.vinipx.quantaf.scenarios.HighFrequencyOrderTest"

# BDD tests
./gradlew test --tests "io.github.vinipx.quantaf.bdd.runners.CucumberTestRunner"
```

## TestNG Examples

### Example 1: AI-Driven Order Rejection (from `HighFrequencyOrderTest`)

```java
@Test
@Severity(SeverityLevel.CRITICAL)
@Story("Order Rejection")
@Description("Verifies that a fat-finger rejection is correctly processed")
public void testHighFrequencyOrderRejection() {
    // 1. AI: Generate order config from natural language
    OrderConfiguration config = fixAgent.generateOrderConfig(
            "Limit Order for AAPL that triggers a fat-finger rejection at 9999");

    assertThat(config).isNotNull();
    assertThat(config.getSymbol()).isEqualTo("AAPL");
    assertThat(config.getExpectedExecType()).isEqualTo(OrderConfiguration.ExecType.REJECTED);

    // 2. Stub: Configure the exchange to reject AAPL orders
    stubRegistry.when(msg -> {
        try {
            return msg.getString(Symbol.FIELD).equals("AAPL");
        } catch (Exception e) {
            return false;
        }
    }).respondWith(req -> FixMessageBuilder.rejectionFor(
            req, FixVersion.FIX44, "Fat-finger price check failed"))
      .describedAs("AAPL fat-finger rejection")
      .register();

    // 3. Build the FIX order message
    String clOrdId = marketMaker.generateClOrdId();
    Message order = FixMessageBuilder.fromOrderConfig(
            config.clOrdId(clOrdId), FixVersion.FIX44);

    // 4. Simulate the rejection response
    FixStubRegistry.StubMapping match = stubRegistry.findMatch(order);
    assertThat(match).isNotNull();
    Message rejection = match.generateResponse(order);
    assertThat(rejection).isNotNull();

    // 5. Record rejection in the ledger
    ledger.addRecord(TradeRecord.fromFix().clOrdId(clOrdId).symbol("AAPL")
            .execType("8").price(config.getPrice())
            .quantity(BigDecimal.valueOf(config.getQuantity())));

    // 6. Verify the rejection was handled
    assertThat(ledger.verifyRejectionHandled("AAPL")).isTrue();
}
```

### Example 2: Cross-Source Reconciliation (from `HighFrequencyOrderTest`)

```java
@Test
@Severity(SeverityLevel.CRITICAL)
@Story("Order Fill")
@Description("Verifies a successful limit order fill with cross-source reconciliation")
public void testLimitOrderFillReconciliation() {
    String clOrdId = marketMaker.generateClOrdId();
    BigDecimal price = marketMaker.generatePrice(150.0, 2.0);
    int volume = marketMaker.generateVolume(500);
    LocalDate settlementDate = marketMaker.generateTradeDate(SettlementType.T2);

    stubRegistry.when(msg -> {
        try {
            return msg.getString(Symbol.FIELD).equals("MSFT");
        } catch (Exception e) {
            return false;
        }
    }).respondWith(req -> FixMessageBuilder.fillFor(req, FixVersion.FIX44, price))
      .describedAs("MSFT fill")
      .register();

    Message order = FixMessageBuilder.newOrderSingle(FixVersion.FIX44)
            .clOrdId(clOrdId)
            .symbol("MSFT")
            .side(OrderConfiguration.Side.BUY)
            .orderType(OrderConfiguration.OrderType.LIMIT)
            .price(price)
            .quantity(volume)
            .transactTimeNow()
            .build();

    BigDecimal amount = price.multiply(BigDecimal.valueOf(volume));

    // Simulate trade records from all three sources (matching)
    ledger.addRecord(TradeRecord.fromFix().clOrdId(clOrdId).symbol("MSFT")
            .price(price).quantity(BigDecimal.valueOf(volume)).amount(amount)
            .currency("USD").settlementDate(settlementDate));
    ledger.addRecord(TradeRecord.fromMq().clOrdId(clOrdId).symbol("MSFT")
            .price(price).quantity(BigDecimal.valueOf(volume)).amount(amount)
            .currency("USD").settlementDate(settlementDate));
    ledger.addRecord(TradeRecord.fromApi().clOrdId(clOrdId).symbol("MSFT")
            .price(price).quantity(BigDecimal.valueOf(volume)).amount(amount)
            .currency("USD").settlementDate(settlementDate));

    ReconciliationResult result = ledger.reconcile(clOrdId);
    TradeLedgerAssert.assertThat(result)
            .assertParity()
            .assertSettlementDateMatch()
            .assertAmountMatch(BigDecimal.valueOf(0.01));
}
```

### Example 3: FIX Stub Sequential Responses (from `FixStubRegistryTest`)

```java
@Test
public void sequentialResponses_shouldCycleThroughGenerators() throws Exception {
    registry.when(msg -> true)
            .respondWith(req -> {
                Message m = new Message();
                m.setString(Text.FIELD, "response-1");
                return m;
            })
            .thenRespondWith(req -> {
                Message m = new Message();
                m.setString(Text.FIELD, "response-2");
                return m;
            })
            .describedAs("sequential stub")
            .register();

    Message request = new Message();
    FixStubRegistry.StubMapping match = registry.findMatch(request);

    // First call returns response-1
    Message resp1 = match.generateResponse(request);
    assertThat(resp1.getString(Text.FIELD)).isEqualTo("response-1");

    // Second call returns response-2
    Message resp2 = match.generateResponse(request);
    assertThat(resp2.getString(Text.FIELD)).isEqualTo("response-2");

    // Third call still returns response-2 (last generator sticks)
    Message resp3 = match.generateResponse(request);
    assertThat(resp3.getString(Text.FIELD)).isEqualTo("response-2");
}
```

### Example 4: Business Calendar (from `MarketMakerTest`)

```java
@Test
public void generateTradeDate_T2_shouldSkipWeekends() {
    BusinessCalendar calendar = new BusinessCalendar();
    LocalDate friday = LocalDate.of(2026, 2, 6); // A Friday
    LocalDate result = calendar.addBusinessDays(friday, 2);

    // T+2 from Friday → skips Saturday + Sunday → Tuesday
    assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
    assertThat(result).isEqualTo(LocalDate.of(2026, 2, 10));
}

@Test
public void nyseCalendar_shouldSkipJuly4th() {
    BusinessCalendar nyse = BusinessCalendar.nyse();
    LocalDate july4 = LocalDate.of(2026, 7, 4);
    assertThat(nyse.isBusinessDay(july4)).isFalse();
}
```

## Cucumber BDD Examples

### Feature File (`src/test/resources/features/order_lifecycle.feature`)

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

## ISO 20022 (SWIFT) Generation Example

```java
// Template-only mode (no LLM required)
SmartStub stub = new SmartStub();

String creditTransfer = stub.generateSwiftMessage("Generate a credit transfer");
// Returns pacs.008 XML

String statement = stub.generateSwiftMessage("Generate a bank statement");
// Returns camt.053 XML

// Pre-load cached responses for deterministic tests
stub.cacheResponse("my custom intent", "<Document>...</Document>");
```

## Allure Reporting Integration

```java
// Attach FIX messages to reports
AllureFixAttachment.attachFixMessage("NewOrderSingle", order);
AllureFixAttachment.attachFixMessage("ExecutionReport", rejection);

// Attach reconciliation results as Allure steps
ReconciliationReportStep.report(result);
// Creates step: "Reconciliation: ORD-001 [PASSED]" with detailed tabular data

// Attach MQ payloads
ReconciliationReportStep.attachMqPayload("MQ Trade Confirmation", jsonPayload);
```

## Next Steps

- Review **[Configuration](configuration.md)** for the full `quantaf.yml` reference
- Check **[Features](features.md)** for capability overview
- Explore **[Development](development.md)** for extension patterns
