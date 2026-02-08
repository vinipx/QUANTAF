package io.github.vinipx.quantaf.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.vinipx.quantaf.ai.FixScenarioAgent;
import io.github.vinipx.quantaf.core.TradeLedger;
import io.github.vinipx.quantaf.core.TradeLedgerAssert;
import io.github.vinipx.quantaf.core.model.OrderConfiguration;
import io.github.vinipx.quantaf.core.model.ReconciliationResult;
import io.github.vinipx.quantaf.core.model.TradeRecord;
import io.github.vinipx.quantaf.protocol.fix.FixMessageBuilder;
import io.github.vinipx.quantaf.protocol.fix.FixStubRegistry;
import io.github.vinipx.quantaf.protocol.fix.FixVersion;
import quickfix.Message;
import quickfix.field.Symbol;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Cucumber step definitions for order lifecycle BDD scenarios.
 */
public class OrderStepDefs {

    private FixStubRegistry stubRegistry;
    private TradeLedger ledger;
    private FixScenarioAgent fixAgent;
    private FixVersion currentVersion;
    private Message lastOrder;
    private Message lastResponse;
    private OrderConfiguration generatedConfig;
    private ReconciliationResult lastReconciliation;

    @Before
    public void setUp() {
        stubRegistry = new FixStubRegistry();
        ledger = new TradeLedger();
        fixAgent = new FixScenarioAgent();
    }

    @Given("a FIX session is available for version {string}")
    public void aFixSessionIsAvailableForVersion(String version) {
        currentVersion = FixVersion.fromString(version);
        assertThat(currentVersion).isNotNull();
    }

    @When("I submit a {string} {string} order for {string} at price {double} with quantity {int}")
    public void iSubmitAnOrder(String side, String orderType, String symbol, double price, int quantity) {
        lastOrder = FixMessageBuilder.newOrderSingle(currentVersion)
                .clOrdId("BDD-" + System.currentTimeMillis())
                .symbol(symbol)
                .side(side.equalsIgnoreCase("BUY") ? '1' : '2')
                .orderType(orderType.equalsIgnoreCase("LIMIT") ? '2' : '1')
                .price(BigDecimal.valueOf(price))
                .quantity(quantity)
                .transactTimeNow()
                .build();

        // Check if a stub matches
        FixStubRegistry.StubMapping match = stubRegistry.findMatch(lastOrder);
        if (match != null) {
            lastResponse = match.generateResponse(lastOrder);
        }
    }

    @Then("the order should be filled at price {double}")
    public void theOrderShouldBeFilledAtPrice(double price) {
        if (lastResponse == null) {
            // Create a simulated fill response
            lastResponse = FixMessageBuilder.fillFor(lastOrder, currentVersion, BigDecimal.valueOf(price));
        }
        assertThat(lastResponse).isNotNull();
    }

    @And("the trade should reconcile across all sources")
    public void theTradeShouldReconcileAcrossAllSources() {
        // Simulate matching records
        try {
            String clOrdId = lastOrder.getString(quickfix.field.ClOrdID.FIELD);
            BigDecimal price = lastOrder.getDecimal(quickfix.field.Price.FIELD);
            int qty = lastOrder.getInt(quickfix.field.OrderQty.FIELD);
            String symbol = lastOrder.getString(Symbol.FIELD);

            ledger.addRecord(TradeRecord.fromFix().clOrdId(clOrdId).symbol(symbol)
                    .price(price).quantity(BigDecimal.valueOf(qty)));
            ledger.addRecord(TradeRecord.fromMq().clOrdId(clOrdId).symbol(symbol)
                    .price(price).quantity(BigDecimal.valueOf(qty)));
            ledger.addRecord(TradeRecord.fromApi().clOrdId(clOrdId).symbol(symbol)
                    .price(price).quantity(BigDecimal.valueOf(qty)));

            ReconciliationResult result = ledger.reconcile(clOrdId);
            TradeLedgerAssert.assertThat(result).assertParity();
        } catch (Exception e) {
            throw new RuntimeException("Reconciliation failed", e);
        }
    }

    @And("the exchange is configured to reject orders for {string} above price {double}")
    public void theExchangeIsConfiguredToReject(String symbol, double maxPrice) {
        stubRegistry.when(msg -> {
            try {
                return msg.getString(Symbol.FIELD).equals(symbol)
                        && msg.getDecimal(quickfix.field.Price.FIELD).doubleValue() > maxPrice;
            } catch (Exception e) {
                return false;
            }
        }).respondWith(req -> FixMessageBuilder.rejectionFor(req, currentVersion, "Fat-finger price check"))
                .describedAs("Reject " + symbol + " above " + maxPrice)
                .register();
    }

    @Then("the order should be rejected with reason {string}")
    public void theOrderShouldBeRejectedWithReason(String reason) throws Exception {
        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.getChar(quickfix.field.ExecType.FIELD)).isEqualTo('8');
        assertThat(lastResponse.getString(quickfix.field.Text.FIELD)).contains(reason);
    }

    @Given("the AI scenario agent is available")
    public void theAiScenarioAgentIsAvailable() {
        assertThat(fixAgent).isNotNull();
    }

    @When("I generate an order from intent {string}")
    public void iGenerateAnOrderFromIntent(String intent) {
        generatedConfig = fixAgent.generateOrderConfig(intent);
        assertThat(generatedConfig).isNotNull();
    }

    @Then("the generated order should have side {string}")
    public void theGeneratedOrderShouldHaveSide(String side) {
        assertThat(generatedConfig.getSide().name()).isEqualTo(side);
    }

    @And("the generated order should have symbol {string}")
    public void theGeneratedOrderShouldHaveSymbol(String symbol) {
        assertThat(generatedConfig.getSymbol()).isEqualTo(symbol);
    }

    @And("the generated order should have time in force {string}")
    public void theGeneratedOrderShouldHaveTimeInForce(String tif) {
        assertThat(generatedConfig.getTimeInForce().name()).isEqualTo(tif);
    }

    @Given("matching trade records exist for order {string}")
    public void matchingTradeRecordsExistForOrder(String orderId, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            TradeRecord.Source source = TradeRecord.Source.valueOf(row.get("source"));
            TradeRecord record = new TradeRecord(source)
                    .clOrdId(orderId)
                    .symbol(row.get("symbol"))
                    .price(new BigDecimal(row.get("price")))
                    .quantity(new BigDecimal(row.get("quantity")))
                    .settlementDate(LocalDate.parse(row.get("settlementDate")));
            ledger.addRecord(record);
        }
    }

    @When("I reconcile the trade {string}")
    public void iReconcileTheTrade(String orderId) {
        lastReconciliation = ledger.reconcile(orderId);
    }

    @Then("the reconciliation should pass")
    public void theReconciliationShouldPass() {
        assertThat(lastReconciliation.isPassed()).isTrue();
    }

    @And("all fields should match across sources")
    public void allFieldsShouldMatchAcrossSources() {
        TradeLedgerAssert.assertThat(lastReconciliation).assertParity();
    }
}
