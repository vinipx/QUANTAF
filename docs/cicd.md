# CI/CD & Docker Compose Setup

## Overview

QUANTAF is designed for seamless integration into modern CI/CD pipelines with Docker Compose for local development and Testcontainers for ephemeral infrastructure.

## Docker Compose Local Development

### Setup Local Services

The `docker-compose.yml` file provides all services needed for local development:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: quantaf
      POSTGRES_USER: quantaf
      POSTGRES_PASSWORD: quantaf_dev
    volumes:
      - postgres_data:/var/lib/postgresql/data

  activemq:
    image: activemq:5.18-alpine
    ports:
      - "61616:61616"
      - "8161:8161"
    environment:
      ACTIVEMQ_ADMIN_LOGIN: admin
      ACTIVEMQ_ADMIN_PASSWORD: admin

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  mock-exchange:
    image: quantaf/mock-exchange:latest
    ports:
      - "9876:9876"
    environment:
      FIX_PORT: 9876

volumes:
  postgres_data:
```

### Start Services

```bash
# Start all services in background
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Clean up volumes
docker-compose down -v
```

### Environment Configuration

Services automatically configure environment variables for tests:

```bash
# Inside docker-compose containers
export FIX_HOST=mock-exchange
export FIX_PORT=9876
export DB_URL=jdbc:postgresql://postgres:5432/quantaf
export DB_USER=quantaf
export DB_PASSWORD=quantaf_dev
export MQ_HOST=activemq
export MQ_PORT=61616
export REDIS_HOST=redis
export REDIS_PORT=6379
```

## Testcontainers for CI/CD

Testcontainers provides ephemeral containers for CI pipelines without docker-compose.

### Database Testing

```java
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class TradeLedgerTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("quantaf_test")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    public void testTradeStorage() {
        String jdbcUrl = postgres.getJdbcUrl();
        TradeLedger ledger = new TradeLedger(jdbcUrl);
        
        Trade trade = new Trade("TRADE123", "AAPL", 1000, 150.25);
        ledger.recordTrade(trade);
        
        Trade retrieved = ledger.findById("TRADE123");
        assertThat(retrieved).isNotNull();
    }
}
```

### Message Broker Testing

```java
@Testcontainers
public class MqAdapterTest {
    
    @Container
    static GenericContainer<?> activemq = 
        new GenericContainer<>(DockerImageName.parse("activemq:5.18-alpine"))
            .withExposedPorts(61616);
    
    @Test
    public void testMessageRouting() {
        String brokerUrl = "tcp://" + activemq.getHost() + ":" + 
                          activemq.getMappedPort(61616);
        
        MqAdapter adapter = new MqAdapter(brokerUrl);
        adapter.send("orders_queue", "ORDER123");
        
        String message = adapter.receive("orders_queue", 5000);
        assertThat(message).isEqualTo("ORDER123");
    }
}
```

## GitHub Actions CI/CD

### Build & Test Workflow

Create `.github/workflows/build.yml`:

```yaml
name: Build & Test

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: quantaf
          POSTGRES_USER: quantaf
          POSTGRES_PASSWORD: quantaf_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
      
      activemq:
        image: activemq:5.18-alpine
        ports:
          - 61616:61616

    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Build with Gradle
        run: ./gradlew build --info
      
      - name: Run Tests
        env:
          DB_URL: jdbc:postgresql://postgres:5432/quantaf
          DB_USER: quantaf
          DB_PASSWORD: quantaf_test
          MQ_HOST: activemq
          MQ_PORT: 61616
        run: ./gradlew test
      
      - name: Generate Allure Report
        if: always()
        run: ./gradlew allureReport
      
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: allure-report
          path: build/reports/allure-report/
      
      - name: Publish to GitHub Pages
        if: github.ref == 'refs/heads/main' && success()
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./build/reports/allure-report
```

### Deployment Workflow

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Production

on:
  push:
    branches: [main]
    tags: ['v*']

jobs:
  deploy:
    runs-on: ubuntu-latest
    if: github.event_name == 'push' && startsWith(github.ref, 'refs/tags/v')
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Build Release
        run: ./gradlew build -Prelease
      
      - name: Create Release
        uses: softprops/action-gh-release@v1
        with:
          files: build/libs/*.jar
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

## Jenkins Pipeline

### Jenkinsfile

```groovy
pipeline {
    agent any
    
    environment {
        GRADLE_HOME = '/opt/gradle'
        DB_URL = 'jdbc:postgresql://postgres:5432/quantaf'
        DB_USER = 'quantaf'
        DB_PASSWORD = credentials('quantaf_db_password')
    }
    
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/vinipx/QUANTAF.git'
            }
        }
        
        stage('Setup Services') {
            steps {
                sh 'docker-compose up -d'
                sh 'sleep 10'  // Wait for services to start
            }
        }
        
        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }
        
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        
        stage('Report') {
            steps {
                sh './gradlew allureReport'
                publishAllure([
                    includeProperties: false,
                    jdk: '',
                    results: [[path: 'build/allure-results']]
                ])
            }
        }
        
        stage('Cleanup') {
            steps {
                sh 'docker-compose down'
            }
        }
    }
    
    post {
        always {
            junit 'build/test-results/test/*.xml'
            archiveArtifacts artifacts: 'build/reports/allure-report/**', 
                             allowEmptyArchive: true
        }
    }
}
```

## GitLab CI

### .gitlab-ci.yml

```yaml
image: gradle:8-jdk21

stages:
  - build
  - test
  - report

variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

services:
  - postgres:15-alpine
  - docker:activemq:5.18-alpine

build:
  stage: build
  script:
    - ./gradlew clean build
  artifacts:
    paths:
      - build/libs/

test:
  stage: test
  script:
    - ./gradlew test
  artifacts:
    reports:
      junit: build/test-results/test/TEST-*.xml

report:
  stage: report
  script:
    - ./gradlew allureReport
  artifacts:
    paths:
      - build/reports/allure-report/
    reports:
      junit: build/test-results/test/TEST-*.xml
  allow_failure: true
```

## Performance Optimization

### Parallel Test Execution

```bash
# Run tests in parallel with 8 workers
./gradlew test --max-workers=8

# Configure in gradle.properties
org.gradle.parallel=true
org.gradle.workers.max=8
```

### Caching Dependencies

GitHub Actions cache Gradle dependencies:

```yaml
- name: Cache Gradle packages
  uses: actions/cache@v3
  with:
    path: ~/.gradle/caches
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

### Build Optimization

```gradle
// build.gradle.kts
configurations.all {
    resolutionStrategy.cacheDynamicVersionsFor(0, 'seconds')
}

tasks.withType<Test> {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    useTestNG {
        suiteXmlBuilder().parallel = "methods"
        suiteXmlBuilder().threadCount = 8
    }
}
```

## Troubleshooting

### Docker Services Won't Start

```bash
# Check Docker daemon
docker ps

# View service logs
docker-compose logs postgres

# Rebuild images
docker-compose up -d --build
```

### Port Conflicts

```bash
# Find process using port 5432
lsof -i :5432

# Kill process
kill -9 <PID>

# Or use different port in docker-compose
ports:
  - "5433:5432"
```

### Gradle Out of Memory

```bash
# Increase JVM heap
export GRADLE_OPTS="-Xmx4g"
./gradlew test
```

### Test Timeouts in CI

Increase timeout values:

```yaml
# In quantaf.yml
fix:
  connection:
    timeout_ms: 60000

rest:
  timeout_ms: 60000
```

## Best Practices

!!! success "CI/CD Best Practices"
    - **Immutable Builds**: Same version produces same artifacts
    - **Fast Feedback**: Run quick unit tests first, integration tests later
    - **Automated Reporting**: Always generate and archive test reports
    - **Clean State**: Start with clean database/message queue each run
    - **Resource Cleanup**: Kill containers and ports after tests

## Next Steps

- Review **[Configuration](configuration.md)** for environment setup
- Check **[Examples](examples.md)** for CI integration patterns
- Explore **[Development](development.md)** for local debugging
