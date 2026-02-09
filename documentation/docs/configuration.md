---
sidebar_position: 4
title: Configuration
description: Setup, environment variables, and YAML structure
---

# Configuration Guide

## Overview

QUANTAF uses centralized YAML-based configuration loaded at startup by the `QuantafConfig` singleton. The configuration file supports environment variable interpolation via `${VAR_NAME}` syntax and is organized into sections matching the framework's architectural layers.

## Main Configuration File

**Location:** `src/main/resources/quantaf.yml`

The `QuantafConfig` class loads this file using Jackson YAML and provides typed accessor objects (`fix()`, `messaging()`, `rest()`, `ai()`, `ledger()`).

## Complete Configuration Reference

Below is the **actual** `quantaf.yml` shipped with QUANTAF, annotated with explanations:

```yaml
quantaf:
  environment: local  # local | ci | staging — drives EnvironmentResolver

  # ─── Layer 1: FIX Protocol ───────────────────────────────
  fix:
    defaultVersion: FIX44           # FIX42 | FIX44 | FIX50
    sessions:
      trader:                       # Initiator session (sends orders)
        senderCompId: QUANTAF_CLIENT
        targetCompId: TARGET_PLATFORM
        host: localhost
        port: 9876
      exchange:                     # Acceptor session (exchange stub)
        type: acceptor
        senderCompId: QUANTAF_EXCHANGE
        targetCompId: TARGET_PLATFORM_OUTBOUND
        port: 9877

  # ─── Layer 1: Message Queue ──────────────────────────────
  messaging:
    provider: activemq              # activemq | ibmmq | kafka
    activemq:
      brokerUrl: tcp://localhost:61616
      user: admin
      password: admin
    ibmmq:
      host: localhost
      port: 1414
      queueManager: QM1
      channel: DEV.APP.SVRCONN
    kafka:
      bootstrapServers: localhost:9092

  # ─── Layer 1: REST + OAuth2 ──────────────────────────────
  rest:
    baseUrl: http://localhost:8080/api
    oauth2:
      tokenUrl: http://localhost:8080/oauth/token
      clientId: ${QUANTAF_CLIENT_ID}
      clientSecret: ${QUANTAF_CLIENT_SECRET}

  # ─── Layer 3: AI / LLM ──────────────────────────────────
  ai:
    provider: ollama                # openai | ollama
    model: llama3
    baseUrl: http://localhost:11434
    apiKey: ${QUANTAF_AI_API_KEY}
    fallbackToTemplates: true       # Use templates when LLM unavailable
    cacheResponses: true            # Cache SmartStub responses

  # ─── Layer 2: Reconciliation ─────────────────────────────
  ledger:
    amountPrecision: 8              # Decimal precision for comparisons
    defaultTolerance: 0.0001        # Max allowed difference for amounts
    timezone: UTC

  # ─── Reporting ───────────────────────────────────────────
  reporting:
    allure:
      resultsDir: build/allure-results
      attachFixMessages: true
      attachMqPayloads: true
```

## Configuration Sections Detail

### Environment

```yaml
quantaf:
  environment: local  # local | ci | staging
```

The `EnvironmentResolver` uses this value to determine runtime behavior:

| Environment | Detection | Behavior |
|-------------|-----------|----------|
| `LOCAL` | Default, or when no `CI` env var | Docker Compose services expected |
| `CI` | Auto-detected from `CI` or `GITHUB_ACTIONS` env vars | Testcontainers used, AI templates forced |
| `STAGING` | Explicit configuration | Connects to staging infrastructure |

The resolver also provides helper methods:

```java
EnvironmentResolver resolver = new EnvironmentResolver("local");
resolver.useDockerCompose();   // true for LOCAL
resolver.useTestcontainers();  // true for CI
resolver.useAiTemplates();     // true for CI
```

### FIX Protocol

Accessed via `QuantafConfig.getInstance().fix()`:

```java
FixConfig fix = config.fix();
String version = fix.getDefaultVersion();           // "FIX44"
String sender = fix.getSenderCompId("trader");      // "QUANTAF_CLIENT"
String target = fix.getTargetCompId("trader");      // "TARGET_PLATFORM"
String host = fix.getHost("trader");                // "localhost"
int port = fix.getPort("trader");                   // 9876
```

Each FIX version also has a dedicated QuickFIX/J configuration file:

| File | Version | BeginString |
|------|---------|-------------|
| `quickfix-FIX42.cfg` | FIX 4.2 | `FIX.4.2` |
| `quickfix-FIX44.cfg` | FIX 4.4 | `FIX.4.4` |
| `quickfix-FIX50.cfg` | FIX 5.0 | `FIXT.1.1` |

These files define session-level settings (HeartBtInt, ReconnectInterval, DataDictionary, FileStore/FileLog paths) and are loaded by `FixSessionManager`.

### Message Queue

Accessed via `QuantafConfig.getInstance().messaging()`:

```java
MessagingConfig mq = config.messaging();
String provider = mq.getProvider();      // "activemq"
String brokerUrl = mq.getBrokerUrl();    // "tcp://localhost:61616"
```

The `provider` field selects which broker configuration block to use. Currently supported:

| Provider | Implementation | Status |
|----------|---------------|--------|
| `activemq` | `ActiveMqBroker` via Apache Artemis (Jakarta JMS) | Fully implemented |
| `ibmmq` | `IbmMqBroker` | Skeleton (awaiting IBM MQ client libraries) |
| `kafka` | — | Configuration placeholder (not yet implemented) |

### REST + OAuth2

Accessed via `QuantafConfig.getInstance().rest()`:

```java
RestConfig rest = config.rest();
String baseUrl = rest.getBaseUrl();          // "http://localhost:8080/api"
String tokenUrl = rest.getTokenUrl();        // "http://localhost:8080/oauth/token"
String clientId = rest.getClientId();        // resolved from ${QUANTAF_CLIENT_ID}
String clientSecret = rest.getClientSecret();// resolved from ${QUANTAF_CLIENT_SECRET}
```

The `OAuth2TokenManager` handles the token lifecycle:

- Acquires tokens via `client_credentials` grant
- Caches tokens in memory
- Auto-refreshes 60 seconds before expiry
- Thread-safe via `synchronized` methods

### AI / LLM

Accessed via `QuantafConfig.getInstance().ai()`:

```java
AiConfig ai = config.ai();
String provider = ai.getProvider();            // "ollama"
String model = ai.getModel();                  // "llama3"
String baseUrl = ai.getBaseUrl();              // "http://localhost:11434"
String apiKey = ai.getApiKey();                // resolved from ${QUANTAF_AI_API_KEY}
boolean fallback = ai.isFallbackToTemplates(); // true
boolean cache = ai.isCacheResponses();         // true
```

| Setting | Default | Purpose |
|---------|---------|---------|
| `provider` | `ollama` | LLM backend: `openai` or `ollama` |
| `model` | `llama3` | Model identifier |
| `baseUrl` | `http://localhost:11434` | Ollama server URL |
| `apiKey` | — | OpenAI API key (via env var) |
| `fallbackToTemplates` | `true` | Use deterministic templates when LLM unavailable |
| `cacheResponses` | `true` | Cache SmartStub ISO 20022 responses |

:::tip[CI Pipeline Strategy]
In CI environments, set `fallbackToTemplates: true` to ensure tests never depend on external LLM availability. The `FixScenarioAgent` template engine uses keyword-based deterministic generation that produces identical results across runs.
:::

### Ledger / Reconciliation

Accessed via `QuantafConfig.getInstance().ledger()`:

```java
LedgerConfig ledger = config.ledger();
int precision = ledger.getAmountPrecision();     // 8
double tolerance = ledger.getDefaultTolerance(); // 0.0001
String tz = ledger.getTimezone();                // "UTC"
```

These values are passed to the `TradeLedger` constructor by `QuantafBaseTest`:

```java
ledger = new TradeLedger(precision, BigDecimal.valueOf(tolerance));
```

The precision creates a `MathContext(8, RoundingMode.HALF_EVEN)` for normalizing numeric values before comparison.

## Environment Variable Interpolation

The `QuantafConfig.resolveEnvVar()` method supports `${VAR_NAME}` substitution:

```yaml
# Resolved from environment variable
clientId: ${QUANTAF_CLIENT_ID}

# Falls back to system property, then empty string
apiKey: ${QUANTAF_AI_API_KEY}
```

:::warning[No Default Value Syntax]
Unlike some frameworks, the current `resolveEnvVar()` implementation does not support `${VAR:default}` syntax with colon-separated defaults. If the environment variable is not set, the value resolves to an empty string. Set all required variables before running tests.
:::

**Setting environment variables:**

```bash
# macOS/Linux
export QUANTAF_CLIENT_ID=my-client
export QUANTAF_CLIENT_SECRET=my-secret
export QUANTAF_AI_API_KEY=sk-...
./gradlew test

# Or inline
QUANTAF_AI_API_KEY=sk-... ./gradlew test
```

## QuickFIX/J Configuration

The FIX session configuration files in `src/main/resources/` follow QuickFIX/J format:

```ini
# quickfix-FIX44.cfg
[default]
FileStorePath=build/data/quickfix/fix44
FileLogPath=build/logs/quickfix/fix44
ConnectionType=initiator
StartTime=00:00:00
EndTime=00:00:00
HeartBtInt=30
ReconnectInterval=5
UseDataDictionary=Y
DataDictionary=FIX44.xml
ValidateUserDefinedFields=N
ValidateIncomingMessage=N

# Trader Session (Initiator)
[session]
BeginString=FIX.4.4
SenderCompID=QUANTAF_CLIENT
TargetCompID=TARGET_PLATFORM
SocketConnectHost=localhost
SocketConnectPort=9876

# Exchange Stub Session (Acceptor)
[session]
ConnectionType=acceptor
BeginString=FIX.4.4
SenderCompID=QUANTAF_EXCHANGE
TargetCompID=TARGET_PLATFORM_OUTBOUND
SocketAcceptPort=9877
```

Key settings:

| Setting | Value | Purpose |
|---------|-------|---------|
| `HeartBtInt` | 30 | Heartbeat interval in seconds |
| `ReconnectInterval` | 5 | Seconds between reconnection attempts |
| `ValidateIncomingMessage` | N | Relaxed validation for test stubs |
| `FileStorePath` | `build/data/quickfix/` | Session persistence directory |
| `FileLogPath` | `build/logs/quickfix/` | FIX message log directory |

## Logging Configuration

**Location:** `src/main/resources/logback.xml`

```xml
<!-- Log levels by component -->
<logger name="io.github.vinipx.quantaf" level="DEBUG"/>
<logger name="quickfix" level="INFO"/>
<logger name="org.apache.activemq" level="WARN"/>

<!-- Output: Console + rolling file (7 day retention) -->
<root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
</root>
```

Log output: `build/logs/quantaf.log` with daily rotation.

## Docker Compose Integration

The `docker-compose.yml` provides a local ActiveMQ Artemis instance:

```bash
# Start ActiveMQ Artemis
docker-compose up -d

# Verify it's running
docker-compose ps

# View web console
open http://localhost:8161  # admin/admin

# Stop
docker-compose down
```

Services available after `docker-compose up`:

| Service | Port | Purpose |
|---------|------|---------|
| ActiveMQ Artemis | 61616 | AMQP/OpenWire messaging |
| ActiveMQ Web Console | 8161 | Admin UI (admin/admin) |

:::note[No Database Required]
QUANTAF does not require a database. The `TradeLedger` stores all records in-memory (`LinkedHashMap`). The Docker Compose file only starts ActiveMQ Artemis.
:::

## Troubleshooting

### Configuration Not Loading

1. Verify `quantaf.yml` is at `src/main/resources/quantaf.yml`
2. Check YAML syntax: `python -c "import yaml; yaml.safe_load(open('src/main/resources/quantaf.yml'))"`
3. Check the root key is `quantaf:` (the config loader reads from this root)

### Connection Failures

| Error | Cause | Solution |
|-------|-------|----------|
| `Connection refused` on 61616 | ActiveMQ not running | `docker-compose up -d` |
| `Configuration file not found` | Missing QuickFIX/J config | Verify `quickfix-FIX44.cfg` in `src/main/resources/` |
| `OAuth2 token request failed` | Invalid credentials or token URL | Check `QUANTAF_CLIENT_ID` and `QUANTAF_CLIENT_SECRET` |
| `LLM generation failed` | LLM provider unavailable | Ensure `fallbackToTemplates: true` |

:::warning[API Keys]
Never commit API keys to version control. Use environment variables or secure vaults. The `quantaf.yml` uses `${QUANTAF_AI_API_KEY}` placeholder syntax.
:::

## Next Steps

- See **[Examples](examples.md)** for usage patterns with different configurations
- Review **[Architecture](architecture.md)** for how configuration flows through layers
- Check **[Development](development.md)** for project structure and extension points
