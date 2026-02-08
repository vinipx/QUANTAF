package io.github.vinipx.quantaf.scenarios;

import io.github.vinipx.quantaf.ai.FixScenarioAgent;
import io.github.vinipx.quantaf.ai.SmartStub;
import io.github.vinipx.quantaf.config.QuantafConfig;
import io.github.vinipx.quantaf.core.MarketMaker;
import io.github.vinipx.quantaf.core.TradeLedger;
import io.github.vinipx.quantaf.protocol.fix.FixStubRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.math.BigDecimal;

/**
 * Base test class for all QUANTAF TestNG scenarios.
 * Sets up and tears down framework components (FIX sessions, MQ connections, etc.).
 */
public abstract class QuantafBaseTest {

    private static final Logger log = LoggerFactory.getLogger(QuantafBaseTest.class);

    protected static QuantafConfig config;
    protected static MarketMaker marketMaker;
    protected static FixStubRegistry stubRegistry;
    protected static TradeLedger ledger;
    protected static FixScenarioAgent fixAgent;
    protected static SmartStub smartStub;

    @BeforeSuite
    public void setUpSuite() {
        log.info("=== QUANTAF Test Suite Starting ===");

        config = QuantafConfig.getInstance();
        marketMaker = new MarketMaker();
        stubRegistry = new FixStubRegistry();

        int precision = config.ledger().getAmountPrecision();
        double tolerance = config.ledger().getDefaultTolerance();
        ledger = new TradeLedger(precision, BigDecimal.valueOf(tolerance));

        // Template-only mode (no LLM dependency for test execution)
        fixAgent = new FixScenarioAgent();
        smartStub = new SmartStub();

        log.info("QUANTAF components initialized [env={}]", config.getEnvironment());
    }

    @AfterSuite
    public void tearDownSuite() {
        log.info("=== QUANTAF Test Suite Complete ===");
        stubRegistry.reset();
        ledger.clear();
        QuantafConfig.reset();
    }
}
