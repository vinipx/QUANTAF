package io.github.vinipx.quantaf.scenarios;

import io.github.vinipx.quantaf.core.MarketMaker.SettlementType;
import io.github.vinipx.quantaf.core.TradeLedgerAssert;
import io.github.vinipx.quantaf.core.model.OrderConfiguration;
import io.github.vinipx.quantaf.core.model.ReconciliationResult;
import io.github.vinipx.quantaf.core.model.TradeRecord;
import io.github.vinipx.quantaf.protocol.fix.FixMessageBuilder;
import io.github.vinipx.quantaf.protocol.fix.FixStubRegistry;
import io.github.vinipx.quantaf.protocol.fix.FixVersion;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import quickfix.Message;
import quickfix.field.Symbol;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * High-frequency order scenario tests demonstrating the full QUANTAF workflow:
 * AI-driven scenario generation, FIX stub configuration, order execution,
 * and cross-source reconciliation.
 */
@Epic("Order Lifecycle")
@Feature("High Frequency Orders")
public class HighFrequencyOrderTest extends QuantafBaseTest {

    @BeforeMethod
    public void resetState() {
        stubRegistry.reset();
        ledger.clear();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Order Rejection")
    @Description("Verifies that a fat-finger rejection is correctly processed across FIX, MQ, and API sources")
    public void testHighFrequencyOrderRejection() {
        // 1. AI: Generate a complex scenario using template engine
        OrderConfiguration config = fixAgent.generateOrderConfig(
                "Limit Order for AAPL that triggers a fat-finger rejection at 9999");

        assertThat(config).isNotNull();
        assertThat(config.getSymbol()).isEqualTo("AAPL");
        assertThat(config.getExpectedExecType()).isEqualTo(OrderConfiguration.ExecType.REJECTED);

        // 2. Stub: Configure the Exchange to reject this specific order
        stubRegistry.when(msg -> {
            try {
                return msg.getString(Symbol.FIELD).equals("AAPL");
            } catch (Exception e) {
                return false;
            }
        }).respondWith(req -> FixMessageBuilder.rejectionFor(req, FixVersion.FIX44, "Fat-finger price check failed"))
                .describedAs("AAPL fat-finger rejection")
                .register();

        // 3. Build the order message
        String clOrdId = marketMaker.generateClOrdId();
        Message order = FixMessageBuilder.fromOrderConfig(
                config.clOrdId(clOrdId), FixVersion.FIX44);
        assertThat(order).isNotNull();

        // 4. Simulate the rejection response
        FixStubRegistry.StubMapping match = stubRegistry.findMatch(order);
        assertThat(match).isNotNull();
        Message rejection = match.generateResponse(order);
        assertThat(rejection).isNotNull();

        // 5. Record the rejection in the ledger
        ledger.addRecord(TradeRecord.fromFix().clOrdId(clOrdId).symbol("AAPL")
                .execType("8").price(config.getPrice()).quantity(BigDecimal.valueOf(config.getQuantity())));

        // 6. Verify the rejection was handled
        assertThat(ledger.verifyRejectionHandled("AAPL")).isTrue();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Order Fill")
    @Description("Verifies a successful limit order fill with cross-source reconciliation")
    public void testLimitOrderFillReconciliation() {
        // Generate test data
        String clOrdId = marketMaker.generateClOrdId();
        BigDecimal price = marketMaker.generatePrice(150.0, 2.0);
        int volume = marketMaker.generateVolume(500);
        LocalDate settlementDate = marketMaker.generateTradeDate(SettlementType.T2);

        // Configure stub to respond with a fill
        stubRegistry.when(msg -> {
            try {
                return msg.getString(Symbol.FIELD).equals("MSFT");
            } catch (Exception e) {
                return false;
            }
        }).respondWith(req -> FixMessageBuilder.fillFor(req, FixVersion.FIX44, price))
                .describedAs("MSFT fill")
                .register();

        // Create the order
        Message order = FixMessageBuilder.newOrderSingle(FixVersion.FIX44)
                .clOrdId(clOrdId)
                .symbol("MSFT")
                .side(OrderConfiguration.Side.BUY)
                .orderType(OrderConfiguration.OrderType.LIMIT)
                .price(price)
                .quantity(volume)
                .transactTimeNow()
                .build();

        // Verify stub match
        FixStubRegistry.StubMapping match = stubRegistry.findMatch(order);
        assertThat(match).isNotNull();

        BigDecimal amount = price.multiply(BigDecimal.valueOf(volume));

        // Simulate trade records from all sources (matching)
        ledger.addRecord(TradeRecord.fromFix().clOrdId(clOrdId).symbol("MSFT")
                .price(price).quantity(BigDecimal.valueOf(volume)).amount(amount)
                .currency("USD").settlementDate(settlementDate));

        ledger.addRecord(TradeRecord.fromMq().clOrdId(clOrdId).symbol("MSFT")
                .price(price).quantity(BigDecimal.valueOf(volume)).amount(amount)
                .currency("USD").settlementDate(settlementDate));

        ledger.addRecord(TradeRecord.fromApi().clOrdId(clOrdId).symbol("MSFT")
                .price(price).quantity(BigDecimal.valueOf(volume)).amount(amount)
                .currency("USD").settlementDate(settlementDate));

        // Reconcile and assert parity
        ReconciliationResult result = ledger.reconcile(clOrdId);
        TradeLedgerAssert.assertThat(result)
                .assertParity()
                .assertSettlementDateMatch()
                .assertAmountMatch(BigDecimal.valueOf(0.01));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Market Maker Data Generation")
    @Description("Validates that MarketMaker generates realistic financial test data")
    public void testMarketMakerDataGeneration() {
        // Price generation
        BigDecimal price = marketMaker.generatePrice(100.0, 5.0);
        assertThat(price).isPositive();

        // Volume generation
        int volume = marketMaker.generateVolume(1000);
        assertThat(volume).isPositive();

        // Correlated prices
        var prices = marketMaker.generateCorrelatedPrices(100.0, 5.0, 0.8, 20);
        assertThat(prices).hasSize(20);
        assertThat(prices).allSatisfy(p -> assertThat(p).isPositive());

        // Settlement date
        LocalDate settle = marketMaker.generateTradeDate(SettlementType.T2);
        assertThat(settle).isAfter(LocalDate.now());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("AI Template Generation")
    @Description("Validates the FixScenarioAgent template-based order generation")
    public void testAiTemplateOrderGeneration() {
        OrderConfiguration buyMarket = fixAgent.generateOrderConfig("Buy 200 shares of MSFT at market");
        assertThat(buyMarket.getSide()).isEqualTo(OrderConfiguration.Side.BUY);
        assertThat(buyMarket.getOrderType()).isEqualTo(OrderConfiguration.OrderType.MARKET);
        assertThat(buyMarket.getSymbol()).isEqualTo("MSFT");
        assertThat(buyMarket.getQuantity()).isEqualTo(200);

        OrderConfiguration sellLimit = fixAgent.generateOrderConfig("Sell 500 shares of AAPL limit at 180");
        assertThat(sellLimit.getSide()).isEqualTo(OrderConfiguration.Side.SELL);
        assertThat(sellLimit.getOrderType()).isEqualTo(OrderConfiguration.OrderType.LIMIT);
        assertThat(sellLimit.getSymbol()).isEqualTo("AAPL");
        assertThat(sellLimit.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(180));

        OrderConfiguration mocOrder = fixAgent.generateOrderConfig("Buy GOOG Market On Close");
        assertThat(mocOrder.getTimeInForce()).isEqualTo(OrderConfiguration.TimeInForce.AT_CLOSE);
    }
}
