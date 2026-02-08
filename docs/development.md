# Development Reference

## Project Structure

```
QUANTAF/
├── src/main/java/io/github/vinipx/quantaf/
│   ├── core/                      # Business logic layer
│   │   ├── MarketMaker.java
│   │   ├── TradeLedger.java
│   │   ├── domain/                # Domain models
│   │   │   ├── Trade.java
│   │   │   ├── Order.java
│   │   │   ├── Settlement.java
│   │   │   └── Instrument.java
│   │   └── rules/                 # Business rules
│   │       └── ValidationRules.java
│   ├── protocol/                  # Protocol adapters layer
│   │   ├── ProtocolAdapter.java   # Interface for all adapters
│   │   ├── fix/                   # FIX protocol implementation
│   │   │   ├── FixAdapter.java
│   │   │   ├── FixMessageBuilder.java
│   │   │   └── StubRegistry.java
│   │   ├── swift/                 # SWIFT protocol implementation
│   │   │   ├── SwiftAdapter.java
│   │   │   └── SwiftMessageValidator.java
│   │   ├── mq/                    # Message queue adapter
│   │   │   ├── MqAdapter.java
│   │   │   ├── RabbitMqImpl.java
│   │   │   └── ActiveMqImpl.java
│   │   └── rest/                  # REST API adapter
│   │       ├── RestAdapter.java
│   │       └── OAuth2Handler.java
│   ├── ai/                        # AI Cortex layer
│   │   ├── LlmProvider.java       # LLM interface
│   │   ├── FixScenarioAgent.java  # NLP to FIX
│   │   ├── SmartStubGenerator.java
│   │   └── providers/
│   │       ├── OpenAiProvider.java
│   │       └── OllamaProvider.java
│   ├── config/                    # Configuration layer
│   │   ├── ConfigLoader.java
│   │   ├── QuantafConfig.java
│   │   └── EnvironmentResolver.java
│   └── reporting/                 # Allure integration
│       └── AllureHelper.java
├── src/test/java/io/github/vinipx/quantaf/
│   ├── tests/
│   │   ├── FixAdapterTest.java
│   │   ├── TradeLedgerTest.java
│   │   └── EndToEndTest.java
│   └── steps/                     # Cucumber step definitions
│       ├── TradeSteps.java
│       ├── SettlementSteps.java
│       └── Hooks.java
├── src/test/resources/
│   ├── features/                  # Cucumber feature files
│   │   ├── trading.feature
│   │   └── settlement.feature
│   └── quantaf.yml                # Test configuration
├── build.gradle.kts               # Build configuration
├── mkdocs.yml                     # Documentation config
├── docs/                          # Documentation source
├── docker-compose.yml             # Local services
└── README.md
```

## Key Classes & Interfaces

### ProtocolAdapter (Layer 1)

```java
public interface ProtocolAdapter {
    void connect(String host, int port) throws IOException;
    void send(Message message) throws IOException;
    Message receive(long timeoutMs) throws IOException;
    void disconnect();
}
```

**Implementations:**
- `FixAdapter`: FIX protocol handler
- `SwiftAdapter`: SWIFT message handler
- `MqAdapter`: Message queue abstraction
- `RestAdapter`: HTTP/REST client

### TradeLedger (Layer 2)

```java
public class TradeLedger {
    public void recordTrade(Trade trade);
    public Trade findById(String tradeId);
    public List<Trade> findLatest(int limit);
    public TradeStatus getStatus(String tradeId);
    public void reconcile() throws ReconciliationException;
}
```

**Responsibility:** Maintain immutable ledger of all trades and their state transitions.

### MarketMaker (Layer 2)

```java
public class MarketMaker {
    public BigDecimal generatePrice(String instrument);
    public int generateQuantity(int min, int max);
    public Quote generateQuote(String instrument);
    public List<Trade> generateLoadTest(int volumePerSecond, int durationSeconds);
}
```

**Responsibility:** Generate realistic market data using statistical distributions.

### FixScenarioAgent (Layer 3)

```java
public class FixScenarioAgent {
    public FixMessage translateNlp(String description);
    public List<FixMessage> generateScenario(String businessRequirement);
    public FixMessage buildOrder(OrderRequest request);
}
```

**Responsibility:** Convert natural language to FIX messages using LLM.

### LlmProvider (Layer 3)

```java
public interface LlmProvider {
    String query(String prompt);
    List<String> queryBatch(List<String> prompts);
    void close();
}
```

**Implementations:**
- `OpenAiProvider`: GPT-4, GPT-3.5 support
- `OllamaProvider`: Local LLM inference

## Extension Points

### Adding a New Protocol Adapter

1. **Implement ProtocolAdapter:**
```java
public class CustomProtocolAdapter implements ProtocolAdapter {
    @Override
    public void connect(String host, int port) throws IOException {
        // Protocol-specific connection logic
    }
    
    @Override
    public void send(Message message) throws IOException {
        // Serialize and send message
    }
    
    @Override
    public Message receive(long timeoutMs) throws IOException {
        // Receive and deserialize message
    }
    
    @Override
    public void disconnect() {
        // Clean shutdown
    }
}
```

2. **Register in Configuration:**
```yaml
protocols:
  custom:
    class: io.github.vinipx.quantaf.protocol.custom.CustomProtocolAdapter
    enabled: true
```

3. **Use in Tests:**
```java
ProtocolAdapter adapter = new CustomProtocolAdapter();
adapter.connect("host", 1234);
adapter.send(message);
```

### Adding Custom Business Logic

Extend `TradeLedger` for domain-specific logic:

```java
public class CustomTradeLedger extends TradeLedger {
    @Override
    public void reconcile() throws ReconciliationException {
        // Custom reconciliation logic
        super.reconcile();
        // Additional validations
    }
}
```

### Adding Custom LLM Provider

Implement `LlmProvider` for new LLM sources:

```java
public class CustomLlmProvider implements LlmProvider {
    @Override
    public String query(String prompt) {
        // Call custom LLM API
        return response;
    }
    
    @Override
    public void close() {
        // Cleanup
    }
}
```

## Testing Guidelines

### Unit Tests

Test individual classes in isolation:

```java
@Test
public void testMarketMakerPriceGeneration() {
    MarketMaker mm = new MarketMaker();
    BigDecimal price = mm.generatePrice("AAPL");
    
    assertThat(price)
        .isGreaterThan(BigDecimal.ZERO)
        .isLessThan(new BigDecimal("500"));
}
```

### Integration Tests

Test adapters with real/mock services:

```java
@Test
public void testFixAdapterIntegration() throws IOException {
    FixAdapter adapter = new FixAdapter();
    adapter.connect("localhost", 9876);
    
    FixMessage msg = new FixMessageBuilder()
        .setOrderType(OrderType.NEW)
        .setQuantity(1000)
        .build();
    
    adapter.send(msg);
    FixMessage response = adapter.receive(5000);
    
    assertThat(response).isNotNull();
    adapter.disconnect();
}
```

### End-to-End Tests

Test complete scenarios:

```java
@Test
public void testTradeSettlement() {
    // Setup
    FixAdapter fix = new FixAdapter();
    RestAdapter rest = new RestAdapter();
    TradeLedger ledger = new TradeLedger();
    
    // Execute
    fix.send(orderMessage);
    Trade trade = ledger.findLatest();
    String settlementStatus = rest.querySettlement(trade.getId());
    
    // Assert
    assertThat(settlementStatus).isEqualTo("CONFIRMED");
}
```

## Build & Test Commands

### Build Project
```bash
./gradlew build
```

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "io.github.vinipx.quantaf.tests.FixAdapterTest"
```

### Run Cucumber Tests
```bash
./gradlew test --tests "io.github.vinipx.quantaf.CucumberRunner"
```

### Generate Allure Report
```bash
./gradlew allureReport
```

### Run with Specific Configuration
```bash
export QUANTAF_CONFIG=src/test/resources/quantaf-staging.yml
./gradlew test
```

## Debugging

### Enable Debug Logging
```bash
export LOG_LEVEL=DEBUG
./gradlew test
```

### IntelliJ IDEA Debugging

1. Set breakpoints in code
2. Run `./gradlew test --debug-jvm`
3. Attach IDE debugger to localhost:5005

### View Live Logs
```bash
tail -f build/logs/quantaf.log
```

## Code Style & Standards

### Java Code Style

- Follow Google Java Style Guide
- Use meaningful variable names
- Write javadoc for public APIs
- Keep methods under 20 lines

### Naming Conventions

- **Classes**: PascalCase (e.g., `FixAdapter`)
- **Methods**: camelCase (e.g., `sendMessage()`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRIES`)
- **Packages**: lowercase (e.g., `protocol.fix`)

### Testing Best Practices

- One assertion per test when possible
- Use descriptive test names
- Mock external dependencies
- Clean up resources in `@AfterTest`

## Performance Optimization

### Ledger Performance

For high-volume ledger operations:

```java
TradeLedger ledger = new TradeLedger();
ledger.enableBatchMode(1000); // Batch 1000 trades before persisting
ledger.recordTrades(trades);
ledger.flush();
```

### Concurrent Testing

Use parallel test execution:

```bash
./gradlew test --max-workers=8
```

### Memory Profiling

Monitor memory usage:

```bash
./gradlew test -Xmx2g -Xms1g
```

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Port already in use | Kill process: `lsof -ti tcp:9876 \| xargs kill` |
| FIX session timeout | Increase timeout: `fix.session_timeout=30000` |
| LLM API errors | Check API key: `echo $OPENAI_API_KEY` |
| Test flakiness | Add retries: `@Test(retryAnalyzer = RetryAnalyzer.class)` |

## Contributing

When contributing:

1. Create a feature branch
2. Add tests for new functionality
3. Ensure all tests pass: `./gradlew test`
4. Update documentation
5. Submit pull request

## Next Steps

- Review **[Configuration](configuration.md)** for setup details
- Check **[Examples](examples.md)** for code samples
- Explore **[Architecture](architecture.md)** for design patterns
