# Configuration Guide

## Overview

QUANTAF uses YAML-based configuration with environment variable support for flexible, environment-aware setup. Configuration controls protocol connections, AI providers, logging, and more.

## Main Configuration File

**Location:** `src/main/resources/quantaf.yml`

The configuration file uses YAML format with environment variable interpolation via `${VAR_NAME}` syntax.

## Core Sections

### FIX Protocol Configuration

```yaml
fix:
  enabled: true
  host: ${FIX_HOST:localhost}
  port: ${FIX_PORT:9876}
  sender_id: TEST_CLIENT
  target_id: EXCHANGE_SERVER
  
  # Connection settings
  connection:
    reconnect_interval_ms: 5000
    heartbeat_interval: 30
    max_retries: 3
  
  # Message validation
  validation:
    strict_mode: true
    check_version: true
```

**Environment Variables:**
- `FIX_HOST`: FIX server hostname
- `FIX_PORT`: FIX server port

### REST/HTTP Configuration

```yaml
rest:
  enabled: true
  baseUrl: ${REST_BASE_URL:http://localhost:8080}
  timeout_ms: 30000
  
  # OAuth2 setup
  oauth2:
    enabled: true
    token_endpoint: ${OAUTH_TOKEN_ENDPOINT}
    client_id: ${OAUTH_CLIENT_ID}
    client_secret: ${OAUTH_CLIENT_SECRET}
    scopes:
      - read
      - write
  
  # SSL/TLS
  ssl:
    enabled: true
    verify_certificate: true
    trust_store: ${SSL_TRUST_STORE:/etc/ssl/certs/ca-certificates.crt}
```

**Environment Variables:**
- `REST_BASE_URL`: REST API base URL
- `OAUTH_TOKEN_ENDPOINT`: OAuth2 token endpoint
- `OAUTH_CLIENT_ID`: OAuth2 client ID
- `OAUTH_CLIENT_SECRET`: OAuth2 client secret
- `SSL_TRUST_STORE`: Path to SSL trust store

### Message Queue Configuration

```yaml
mq:
  enabled: true
  broker: activemq
  
  # ActiveMQ settings
  activemq:
    host: ${MQ_HOST:localhost}
    port: ${MQ_PORT:61616}
    connection_string: failover://tcp://${MQ_HOST}:${MQ_PORT}
  
  # Queue/Topic settings
  queues:
    orders: orders_queue
    executions: executions_queue
    settlements: settlements_queue
```

**Environment Variables:**
- `MQ_HOST`: Message broker hostname
- `MQ_PORT`: Message broker port

### AI & LLM Configuration

```yaml
ai:
  enabled: true
  provider: openai  # openai, ollama, custom
  
  # OpenAI settings
  openai:
    api_key: ${OPENAI_API_KEY}
    model: gpt-4
    temperature: 0.7
    max_tokens: 2000
  
  # Ollama settings (local LLM)
  ollama:
    endpoint: ${OLLAMA_ENDPOINT:http://localhost:11434}
    model: mistral
    timeout_ms: 60000
  
  # Features
  features:
    nlp_to_fix_translation: true
    swift_stub_generation: true
    scenario_generation: true
```

**Environment Variables:**
- `OPENAI_API_KEY`: OpenAI API key
- `OLLAMA_ENDPOINT`: Ollama server endpoint

### Database Configuration

```yaml
database:
  type: postgresql
  url: ${DB_URL:jdbc:postgresql://localhost:5432/quantaf}
  username: ${DB_USER:quantaf}
  password: ${DB_PASSWORD}
  
  # Connection pool
  pool:
    min_idle: 5
    max_pool_size: 20
    idle_timeout_ms: 300000
  
  # Ledger storage
  ledger:
    table_name: trades_ledger
    batch_size: 1000
    async_persist: true
```

**Environment Variables:**
- `DB_URL`: Database connection URL
- `DB_USER`: Database username
- `DB_PASSWORD`: Database password

### Logging Configuration

```yaml
logging:
  level: INFO  # DEBUG, INFO, WARN, ERROR
  format: json  # json, text
  
  # File output
  file:
    enabled: true
    path: build/logs/quantaf.log
    max_size_mb: 100
    max_files: 10
  
  # Loggers
  loggers:
    io.github.vinipx.quantaf: DEBUG
    org.quickfixj: WARN
    dev.langchain4j: INFO
```

**Environment Variables:**
- `LOG_LEVEL`: Global logging level

### Allure Reporting Configuration

```yaml
reporting:
  allure:
    enabled: true
    results_dir: build/allure-results
    report_dir: build/allure-reports
    
    # Report features
    features:
      timeline: true
      history: true
      trends: true
      categories: true
```

## Multi-Environment Setup

Create environment-specific configuration files:

```
src/main/resources/
├── quantaf.yml                  # Default/development
├── quantaf-staging.yml          # Staging environment
└── quantaf-production.yml       # Production environment
```

**Select configuration at runtime:**

```bash
export QUANTAF_CONFIG=src/main/resources/quantaf-staging.yml
./gradlew test
```

## Environment Variable Interpolation

QUANTAF supports environment variable substitution with default values:

```yaml
# Uses OPENAI_API_KEY environment variable, defaults to "test-key"
openai:
  api_key: ${OPENAI_API_KEY:test-key}

# Required: fails if not set
rest:
  baseUrl: ${REST_BASE_URL}
```

**Setting environment variables:**

```bash
# macOS/Linux
export FIX_HOST=trading-server.example.com
export OPENAI_API_KEY=sk-...
./gradlew test

# Windows (PowerShell)
$env:FIX_HOST = "trading-server.example.com"
$env:OPENAI_API_KEY = "sk-..."
gradlew.bat test
```

## Complete Example Configuration

```yaml
# QUANTAF Configuration
quantaf:
  version: 1.0
  environment: development

fix:
  enabled: true
  host: ${FIX_HOST:localhost}
  port: 9876
  sender_id: TEST_CLIENT
  target_id: EXCHANGE

rest:
  enabled: true
  baseUrl: ${REST_BASE_URL:http://localhost:8080}
  oauth2:
    enabled: true
    client_id: ${OAUTH_CLIENT_ID}
    client_secret: ${OAUTH_CLIENT_SECRET}

mq:
  enabled: true
  broker: activemq
  activemq:
    host: ${MQ_HOST:localhost}
    port: 61616

ai:
  enabled: true
  provider: openai
  openai:
    api_key: ${OPENAI_API_KEY}
    model: gpt-4

database:
  type: postgresql
  url: ${DB_URL:jdbc:postgresql://localhost:5432/quantaf}
  username: ${DB_USER:quantaf}
  password: ${DB_PASSWORD}

logging:
  level: ${LOG_LEVEL:INFO}
  file:
    enabled: true
    path: build/logs/quantaf.log

reporting:
  allure:
    enabled: true
    results_dir: build/allure-results
```

## Docker Compose Integration

Use `docker-compose.yml` to set up services and environment variables:

```bash
docker-compose up -d
```

This starts:
- PostgreSQL database
- ActiveMQ message broker
- Redis cache
- Mock financial services

Environment variables are automatically configured for local development.

## Configuration Validation

Validate configuration syntax:

```bash
./gradlew validateConfig
```

## Troubleshooting

### Configuration Not Applied

1. **Check file location:** Ensure config file is at `src/main/resources/quantaf.yml`
2. **Check environment variables:** `echo $OPENAI_API_KEY`
3. **Check syntax:** Use YAML validator (e.g., yamllint)

### Connection Failures

| Error | Cause | Solution |
|-------|-------|----------|
| Connection refused | Server not running | Start FIX/REST/MQ server |
| Invalid credentials | Auth failed | Check OAuth2 keys, passwords |
| Timeout | Network issue | Increase timeout_ms |

### LLM Configuration

!!! warning "API Keys"
    Never commit API keys to version control. Use environment variables or secure vaults.

## Next Steps

- See **[Examples](examples.md)** for usage patterns with different configurations
- Review **[Features](features.md)** for configuration-driven capabilities
- Check **[Development](development.md)** for advanced customization
