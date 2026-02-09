---
sidebar_position: 10
title: CI/CD & Docker
description: GitHub Actions workflows, Docker Compose, and Testcontainers
---

# CI/CD & Docker Compose Setup

## Overview

QUANTAF is designed for seamless CI/CD integration with GitHub Actions for automated builds and testing, Docker Compose for local development, and Testcontainers for ephemeral CI infrastructure.

## Docker Compose Local Development

### Actual `docker-compose.yml`

The project ships with a Docker Compose file that provides an ActiveMQ Artemis instance:

```yaml
version: '3.8'

services:
  activemq:
    image: apache/activemq-artemis:latest
    container_name: quantaf-activemq
    ports:
      - "61616:61616"   # AMQP / OpenWire
      - "8161:8161"     # Web Console
    environment:
      ARTEMIS_USER: admin
      ARTEMIS_PASSWORD: admin
    volumes:
      - activemq-data:/var/lib/artemis-instance
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8161/console"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  activemq-data:
```

### Start Services

```bash
docker-compose up -d           # Start ActiveMQ Artemis
docker-compose logs -f activemq # View logs
docker-compose ps              # Check health status
open http://localhost:8161     # Web console (admin/admin)
docker-compose down            # Stop services
docker-compose down -v         # Clean up volumes
```

| Service | Port | Purpose | Credentials |
|---------|------|---------|-------------|
| ActiveMQ Artemis | 61616 | AMQP/OpenWire messaging | admin/admin |
| ActiveMQ Web Console | 8161 | Admin UI | admin/admin |

:::note[Minimal Infrastructure]
QUANTAF's Docker Compose intentionally provides only ActiveMQ Artemis. The framework does not require a database — `TradeLedger` stores records in-memory. FIX sessions use local QuickFIX/J file stores.
:::

## GitHub Actions CI/CD

### Build & Test Workflow (`.github/workflows/ci.yml`)

The actual CI pipeline:

```yaml
name: QUANTAF CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  JAVA_VERSION: '21'
  GRADLE_OPTS: '-Dorg.gradle.jvmargs=-Xmx1024m'

jobs:
  build:
    name: Build & Test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
      - uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle.kts') }}
      - run: chmod +x gradlew
      - run: ./gradlew build -x test
      - run: ./gradlew test
        env:
          CI: true    # Triggers EnvironmentResolver CI mode
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: |
            build/reports/tests/
            build/allure-results/
          retention-days: 14
```

**Key features:**
- Triggers on push to `main`/`develop` and PRs to `main`
- Temurin JDK 21 with Gradle caching
- `CI=true` triggers `EnvironmentResolver.isCi()` → template mode for AI
- Test results uploaded as 14-day artifacts

### Documentation Deployment (`.github/workflows/docs.yml`)

The documentation workflow builds Docusaurus and deploys to GitHub Pages:

```yaml
name: Docs
on:
  push:
    branches: [ main ]
    paths: [ "documentation/**" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: documentation
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: "20"
          cache: "npm"
          cache-dependency-path: documentation/package-lock.json
      - run: npm ci
      - run: npm run build:prod
        env:
          BASE_URL: /QUANTAF/
      - uses: actions/upload-pages-artifact@v3
        with:
          path: documentation/build

  deploy:
    needs: build
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - uses: actions/deploy-pages@v4
```

**Key features:**
- Triggers on changes to `documentation/` directory or manual dispatch
- Node.js 20 with npm caching
- `BASE_URL=/QUANTAF/` ensures correct asset paths for GitHub Pages
- Deploys via GitHub Pages Actions (no `gh-pages` branch needed)

## EnvironmentResolver Integration

| Environment | Detection | `useDockerCompose()` | `useTestcontainers()` | `useAiTemplates()` |
|-------------|-----------|---------------------|-----------------------|---------------------|
| `LOCAL` | Default | ✅ | ❌ | ❌ |
| `CI` | `CI=true` or `GITHUB_ACTIONS` env var | ❌ | ✅ | ✅ |
| `STAGING` | Explicit in `quantaf.yml` | ❌ | ❌ | ❌ |

## Testcontainers for CI/CD

```java
@Container
static GenericContainer<?> artemis =
    new GenericContainer<>("apache/activemq-artemis:latest")
        .withExposedPorts(61616, 8161)
        .withEnv("ARTEMIS_USER", "admin")
        .withEnv("ARTEMIS_PASSWORD", "admin");

String brokerUrl = "tcp://" + artemis.getHost() + ":" + artemis.getMappedPort(61616);
ActiveMqBroker broker = new ActiveMqBroker(brokerUrl, "admin", "admin");
```

## Documentation Server

```bash
./docs.sh              # Install deps + dev server on http://localhost:3000
./docs.sh serve        # Same as above
./docs.sh build        # Build static site to ./documentation/build
./docs.sh preview      # Build + serve production build
./docs.sh stop         # Stop the docs server
./docs.sh clean        # Clear Docusaurus cache
```

## Next Steps

- Review **[Configuration](configuration.md)** for environment-specific settings
- Check **[Examples](examples.md)** for CI-compatible test patterns
- Explore **[Allure](allure.md)** for report configuration
