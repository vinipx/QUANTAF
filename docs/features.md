# Features & Capabilities

QUANTAF provides a comprehensive suite of features for enterprise financial system testing.

## Protocol Support 🌐

### FIX Protocol

!!! success "Multi-Version Support"
    Support for FIX 4.2, 4.4, and 5.0 through QuickFIX/J integration.

- **Message Types**: All standard FIX message types (orders, executions, quotes)
- **Session Management**: Automatic logon/logoff, heartbeat handling
- **Persistence**: Optional message storage for audit trails
- **Validation**: Automatic message structure and field validation

### SWIFT

!!! success "SWIFT Message Support"
    Full support for SWIFT message categories and validation.

- **Message Categories**: MT1xx (Customer Payments), MT5xx (Securities)
- **Message Validation**: Schema validation against SWIFT standards
- **Stub Generation**: AI-powered realistic SWIFT message generation

### Message Queue (MQ)

!!! success "Pluggable MQ Support"
    Generic MQ adapter supporting multiple brokers.

- **ActiveMQ Artemis**: Pre-configured support for enterprise messaging
- **RabbitMQ**: Custom adapter for AMQP-based messaging
- **Custom Brokers**: Extensible interface for proprietary systems

### REST API

!!! success "OAuth2-Ready HTTP Testing"
    Modern REST API testing with security built-in.

- **OAuth2 Support**: Automatic token acquisition and refresh
- **Request Building**: Fluent API for crafting HTTP requests
- **Response Validation**: JSON/XML parsing and assertion
- **Logging**: Full request/response logging for debugging

## Test Definition Modes

### TestNG Framework

Run tests with traditional Java/TestNG approach:

```java
@Test
public void testTradePlacement() {
    // Direct Java test execution
    FixMessage order = new FixMessageBuilder()
        .setOrderType(OrderType.NEW)
        .setQuantity(1000)
        .setPrice(150.25)
        .build();
    
    fixAdapter.sendOrder(order);
    Trade trade = tradeLedger.findLatest();
    
    assertThat(trade).isNotNull();
    assertThat(trade.getStatus()).isEqualTo(TradeStatus.CONFIRMED);
}
```

**Advantages:**
- Direct Java development experience
- Full IDE support and debugging
- Granular control over test flow

### Cucumber BDD

Write tests in business-friendly Gherkin syntax:

```gherkin
Feature: Trade Settlement
  Scenario: Successfully settle a trade
    Given a trader sends a buy order for 1000 shares at 150.25
    When the market accepts the order
    Then the trade ledger should record a confirmed trade
    And the settlement status should be PENDING
```

**Advantages:**
- Non-technical stakeholder readability
- Living documentation
- Business-driven test design

## AI & Intelligent Features 🤖

### NLP-to-FIX Translation

Convert natural language to FIX messages automatically:

!!! info "Example"
    Input: "Send a buy order for 1000 shares of AAPL at $150.25"
    Output: FIX Message 35=D (New Order Single) with all proper fields

**Use Cases:**
- Rapid test scenario creation
- Non-technical test author support
- Scenario variation generation

### Smart SWIFT Stub Generation

Generate realistic SWIFT responses based on context:

- **Context-Aware**: Understands settlement status, party details, amounts
- **Realistic Data**: Generates plausible reference numbers, timestamps
- **Validation-Ready**: Generated messages pass schema validation

### Scenario Generation

Use LLMs to generate realistic trading scenarios:

- **Edge Cases**: Automatically identify boundary conditions
- **Load Testing**: Generate high-volume realistic transactions
- **Regulatory Scenarios**: Create compliance-testing scenarios

## Reporting & Analytics 📊

### Allure Reports Integration

Rich, interactive HTML reports for test execution:

**Report Features:**
- Test execution timeline
- Failure root cause analysis
- Test history and trends
- Screenshots and logs
- Custom tags and categories

**Example Report Sections:**
- Overview dashboard with pass/fail rates
- Per-test details with execution time
- Failure stack traces and logs
- Timeline showing test progression
- Severity-based filtering

### Test Metrics

Track critical test metrics:

- **Success Rate**: Percentage of passing tests
- **Execution Time**: Total and per-test duration
- **Flakiness**: Detection of inconsistent tests
- **Coverage**: Protocol and scenario coverage

## Configuration Management 🔧

### YAML-Based Configuration

Flexible, environment-aware configuration:

```yaml
fix:
  host: ${FIX_HOST:localhost}
  port: 9876
  sender_id: TEST_CLIENT
  target_id: EXCHANGE

rest:
  baseUrl: ${REST_BASE_URL}
  oauth:
    clientId: ${OAUTH_CLIENT_ID}
    clientSecret: ${OAUTH_CLIENT_SECRET}

ai:
  provider: openai
  model: gpt-4
  apiKey: ${OPENAI_API_KEY}
```

**Features:**
- Environment variable interpolation
- Type-safe configuration objects
- Multi-environment support
- Default values

### Custom Extensions

!!! success "Extensible Architecture"
    Every layer provides extension points for custom implementations.

## Infrastructure & CI/CD 🚀

### Docker Compose

Pre-configured services for local development:

```bash
docker-compose up -d
```

Includes:
- PostgreSQL database
- ActiveMQ message broker
- Redis cache
- Mock financial services

### Testcontainers Integration

Ephemeral, container-based testing infrastructure:

- **Database Testing**: Spin up PostgreSQL, MySQL, Oracle
- **Broker Testing**: Test with real RabbitMQ, ActiveMQ instances
- **Service Mocking**: Run mock services in containers

### CI/CD Ready

!!! success "Pipeline-Friendly"
    Built for integration with GitHub Actions, Jenkins, GitLab CI.

- Gradle-based build automation
- Exit codes for CI integration
- Test result artifacts
- Parallel test execution

## Performance & Scalability 🚄

### High-Frequency Testing

Optimized for high transaction volumes:

- **Throughput**: Thousands of transactions per test
- **Latency**: Sub-millisecond message handling
- **Concurrency**: Multi-threaded test execution

### Resource Efficiency

- **Memory**: Efficient ledger storage with streaming
- **CPU**: Optimized financial calculations
- **Disk**: Minimal logging footprint

## Security Features 🔐

### OAuth2 Support

Secure REST API testing with token management:

- Automatic token acquisition
- Token refresh handling
- Secure credential storage

### Message Encryption

Support for encrypted FIX/SWIFT messages:

- TLS session management
- PGP message signing (SWIFT)
- Credential isolation

## Extensibility

Extend QUANTAF for your specific needs:

- **Custom Protocol Adapters**: Support proprietary protocols
- **Business Logic Modules**: Implement domain-specific rules
- **Assertion Libraries**: Create financial domain assertions
- **Report Generators**: Custom report formats

## Next Steps

- See **[Development](development.md)** for extending QUANTAF
- Review **[Examples](examples.md)** for usage patterns
- Check **[Configuration](configuration.md)** for detailed setup
