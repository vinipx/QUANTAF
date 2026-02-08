# Usage Examples

## Quick Start

### 1. Clone & Setup

```bash
git clone https://github.com/vinipx/QUANTAF.git
cd QUANTAF

# Serve documentation locally
./docs.sh

# Build project
./gradlew build
```

### 2. Run Your First Test

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "io.github.vinipx.quantaf.tests.FixAdapterTest"

# Generate Allure report
./gradlew allureReport
```

## TestNG Examples

### Basic FIX Order Test

```java
import org.testng.annotations.Test;
import io.github.vinipx.quantaf.protocol.fix.FixAdapter;
import io.github.vinipx.quantaf.core.TradeLedger;

public class TradeExecutionTest {
    
    private FixAdapter fixAdapter;
    private TradeLedger tradeLedger;
    
    @BeforeTest
    public void setup() throws IOException {
        fixAdapter = new FixAdapter();
        fixAdapter.connect("localhost", 9876);
        tradeLedger = new TradeLedger();
    }
    
    @Test
    public void testBuyOrderExecution() throws IOException {
        // Build FIX order
        FixMessage order = new FixMessageBuilder()
            .setMsgType("D")  // New Order Single
            .setClOrdId("ORDER123")
            .setSymbol("AAPL")
            .setSide("1")     // Buy
            .setOrderQty(1000)
            .setPrice("150.25")
            .build();
        
        // Send order
        fixAdapter.send(order);
        
        // Receive execution
        FixMessage execution = fixAdapter.receive(5000);
        
        // Verify in ledger
        Trade trade = tradeLedger.findById("ORDER123");
        
        assertThat(execution).isNotNull();
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.EXECUTED);
        assertThat(trade.getQuantity()).isEqualTo(1000);
    }
    
    @Test
    public void testTradeSettlement() throws IOException {
        // ... setup ...
        
        // Initiate settlement
        Trade trade = tradeLedger.findLatest();
        RestAdapter rest = new RestAdapter();
        
        String settlementId = rest.initiateSettlement(trade.getId());
        
        // Poll settlement status
        int attempts = 0;
        while (attempts < 10) {
            String status = rest.getSettlementStatus(settlementId);
            if ("CONFIRMED".equals(status)) {
                break;
            }
            Thread.sleep(1000);
            attempts++;
        }
        
        // Verify settlement
        assertThat(rest.getSettlementStatus(settlementId))
            .isEqualTo("CONFIRMED");
    }
    
    @AfterTest
    public void teardown() throws IOException {
        fixAdapter.disconnect();
    }
}
```

### Market Maker Data Generation

```java
import org.testng.annotations.Test;
import io.github.vinipx.quantaf.core.MarketMaker;

public class MarketDataTest {
    
    @Test
    public void testPriceGeneration() {
        MarketMaker mm = new MarketMaker();
        
        // Generate realistic price
        BigDecimal price = mm.generatePrice("AAPL");
        assertThat(price).isBetween(new BigDecimal("100"), new BigDecimal("300"));
    }
    
    @Test
    public void testLoadTestGeneration() {
        MarketMaker mm = new MarketMaker();
        
        // Generate 1000 trades/sec for 10 seconds (10,000 trades)
        List<Trade> trades = mm.generateLoadTest(1000, 10);
        
        assertThat(trades).hasSize(10000);
        assertThat(trades).allSatisfy(trade -> 
            assertThat(trade.getPrice()).isGreaterThan(BigDecimal.ZERO)
        );
    }
    
    @Test
    public void testQuoteGeneration() {
        MarketMaker mm = new MarketMaker();
        
        Quote quote = mm.generateQuote("EUR/USD");
        
        assertThat(quote.getBid()).isLessThan(quote.getAsk());
        assertThat(quote.getSpread()).isPositive();
    }
}
```

### REST API Testing with OAuth2

```java
import org.testng.annotations.Test;
import io.github.vinipx.quantaf.protocol.rest.RestAdapter;
import io.rest-assured.response.Response;

public class RestApiTest {
    
    private RestAdapter restAdapter;
    
    @BeforeTest
    public void setup() {
        restAdapter = new RestAdapter();
        // OAuth2 setup happens automatically
    }
    
    @Test
    public void testGetAccountBalance() {
        Response response = restAdapter
            .get("/accounts/{id}/balance", 12345)
            .expectStatus(200)
            .getResponse();
        
        BigDecimal balance = response.jsonPath().getDouble("balance");
        assertThat(balance).isGreaterThan(BigDecimal.ZERO);
    }
    
    @Test
    public void testPlaceOrder() {
        Map<String, Object> orderRequest = Map.ofEntries(
            Map.entry("symbol", "AAPL"),
            Map.entry("quantity", 100),
            Map.entry("price", 150.25),
            Map.entry("side", "BUY")
        );
        
        Response response = restAdapter
            .post("/orders", orderRequest)
            .expectStatus(201)
            .getResponse();
        
        String orderId = response.jsonPath().getString("id");
        assertThat(orderId).isNotEmpty();
    }
}
```

## Cucumber BDD Examples

### Trading Feature File

**File: `src/test/resources/features/trading.feature`**

```gherkin
Feature: Trading Operations
  As a trader
  I want to execute trades
  So that I can conduct business

  Background:
    Given the FIX session is connected
    And the market is open

  Scenario: Execute a buy order
    When I send a buy order for 1000 shares of AAPL at $150.25
    Then the order should be confirmed
    And the trade ledger should record the trade
    And the execution price should be $150.25

  Scenario: Execute multiple orders
    When I send the following orders:
      | Symbol | Quantity | Price | Side |
      | AAPL   | 1000     | 150.25 | BUY  |
      | GOOGL  | 500      | 2800.00| SELL |
    Then all orders should be confirmed
    And the trade ledger should contain 2 trades
```

### Step Definitions

**File: `src/test/java/io/github/vinipx/quantaf/steps/TradeSteps.java`**

```java
import io.cucumber.java.en.*;
import io.github.vinipx.quantaf.protocol.fix.FixAdapter;
import io.github.vinipx.quantaf.core.TradeLedger;

public class TradeSteps {
    
    private FixAdapter fixAdapter;
    private TradeLedger tradeLedger;
    private FixMessage lastOrder;
    private List<FixMessage> orders;
    
    @Given("the FIX session is connected")
    public void fixSessionConnected() throws IOException {
        fixAdapter = new FixAdapter();
        fixAdapter.connect("localhost", 9876);
        tradeLedger = new TradeLedger();
    }
    
    @Given("the market is open")
    public void marketIsOpen() {
        // Market readiness check
        assertThat(fixAdapter.isConnected()).isTrue();
    }
    
    @When("I send a buy order for {int} shares of {word} at ${double}")
    public void sendBuyOrder(int quantity, String symbol, double price) throws IOException {
        lastOrder = new FixMessageBuilder()
            .setMsgType("D")
            .setSymbol(symbol)
            .setOrderQty(quantity)
            .setPrice(price)
            .setSide("1")  // Buy
            .build();
        
        fixAdapter.send(lastOrder);
    }
    
    @Then("the order should be confirmed")
    public void orderConfirmed() throws IOException {
        FixMessage execution = fixAdapter.receive(5000);
        assertThat(execution).isNotNull();
        assertThat(execution.getString(35)).isEqualTo("8");  // ExecutionReport
    }
    
    @Then("the trade ledger should record the trade")
    public void tradeRecorded() {
        Trade trade = tradeLedger.findLatest();
        assertThat(trade).isNotNull();
    }
    
    @When("I send the following orders:")
    public void sendMultipleOrders(List<Map<String, String>> orderList) throws IOException {
        orders = new ArrayList<>();
        for (Map<String, String> orderData : orderList) {
            FixMessage order = new FixMessageBuilder()
                .setSymbol(orderData.get("Symbol"))
                .setOrderQty(Integer.parseInt(orderData.get("Quantity")))
                .setPrice(Double.parseDouble(orderData.get("Price")))
                .setSide("BUY".equals(orderData.get("Side")) ? "1" : "2")
                .build();
            
            fixAdapter.send(order);
            orders.add(order);
        }
    }
    
    @Then("all orders should be confirmed")
    public void allOrdersConfirmed() throws IOException {
        for (int i = 0; i < orders.size(); i++) {
            FixMessage execution = fixAdapter.receive(5000);
            assertThat(execution).isNotNull();
        }
    }
    
    @Then("the trade ledger should contain {int} trades")
    public void tradeLedgerCount(int expectedCount) {
        List<Trade> trades = tradeLedger.findLatest(expectedCount);
        assertThat(trades).hasSize(expectedCount);
    }
}
```

## AI-Powered Examples

### NLP-to-FIX Translation

```java
import org.testng.annotations.Test;
import io.github.vinipx.quantaf.ai.FixScenarioAgent;

public class AiPoweredTest {
    
    @Test
    public void testNlpToFixTranslation() {
        FixScenarioAgent agent = new FixScenarioAgent();
        
        // Convert natural language to FIX
        String description = "Send a buy order for 1000 shares of AAPL at $150.25";
        FixMessage message = agent.translateNlp(description);
        
        assertThat(message.getString("SYMBOL")).isEqualTo("AAPL");
        assertThat(message.getInt("ORDER_QTY")).isEqualTo(1000);
        assertThat(message.getDouble("PRICE")).isEqualTo(150.25);
    }
    
    @Test
    public void testScenarioGeneration() {
        FixScenarioAgent agent = new FixScenarioAgent();
        
        // Generate realistic trading scenario
        String requirement = "Test edge case: partial fill with price slippage";
        List<FixMessage> scenario = agent.generateScenario(requirement);
        
        assertThat(scenario).isNotEmpty();
        // Each message represents a step in the scenario
    }
}
```

## Environment-Specific Examples

### Development Environment

```bash
# Use local services from docker-compose
docker-compose up -d

# Run tests against local services
export FIX_HOST=localhost
export REST_BASE_URL=http://localhost:8080
export MQ_HOST=localhost
./gradlew test
```

### CI/CD Pipeline

```bash
# Set production-like environment variables
export FIX_HOST=trading-prod.example.com
export REST_BASE_URL=https://api.example.com
export OPENAI_API_KEY=${SECRETS_OPENAI_KEY}

# Run with parallel execution and reporting
./gradlew test --parallel --max-workers=8
./gradlew allureReport
```

## Troubleshooting

### Tests Timeout

Increase timeout in configuration:
```yaml
fix:
  connection:
    timeout_ms: 60000

rest:
  timeout_ms: 60000
```

### Port Already in Use

Kill process using port:
```bash
lsof -ti tcp:9876 | xargs kill
```

### Authentication Failures

Verify credentials:
```bash
echo "OAuth2 Client ID: $OAUTH_CLIENT_ID"
echo "API Key Set: ${OPENAI_API_KEY:0:10}***"
```

## Next Steps

- Review **[Configuration](configuration.md)** for detailed setup
- Check **[Features](features.md)** for full capability overview
- Explore **[Development](development.md)** for extension patterns
