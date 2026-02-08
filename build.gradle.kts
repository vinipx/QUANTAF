plugins {
    java
    id("io.qameta.allure") version "2.12.0"
}

group = "io.github.vinipx.quantaf"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // FIX Protocol
    implementation("org.quickfixj:quickfixj-core:2.3.1")
    implementation("org.quickfixj:quickfixj-messages-fix42:2.3.1")
    implementation("org.quickfixj:quickfixj-messages-fix44:2.3.1")
    implementation("org.quickfixj:quickfixj-messages-fix50:2.3.1")

    // Statistical / Math
    implementation("org.apache.commons:commons-math3:3.6.1")

    // REST
    implementation("io.rest-assured:rest-assured:5.4.0")

    // Messaging (JMS + providers)
    implementation("jakarta.jms:jakarta.jms-api:3.1.0")
    implementation("org.apache.activemq:artemis-jakarta-client:2.37.0")

    // AI (LangChain4j - pluggable)
    implementation("dev.langchain4j:langchain4j:0.35.0")
    implementation("dev.langchain4j:langchain4j-open-ai:0.35.0")
    implementation("dev.langchain4j:langchain4j-ollama:0.35.0")

    // Config & Logging
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.13")

    // Test Runners
    testImplementation("org.testng:testng:7.10.2")
    testImplementation("io.cucumber:cucumber-java:7.18.0")
    testImplementation("io.cucumber:cucumber-testng:7.18.0")

    // Reporting
    implementation("io.qameta.allure:allure-testng:2.27.0")
    implementation("io.qameta.allure:allure-cucumber7-jvm:2.27.0")

    // Infrastructure (CI)
    testImplementation("org.testcontainers:testcontainers:1.20.0")

    // Assertions
    testImplementation("org.assertj:assertj-core:3.26.0")
}

tasks.withType<Test> {
    useTestNG()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

allure {
    version.set("2.27.0")
    adapter {
        autoconfigure.set(true)
        aspectjWeaver.set(true)
    }
}
