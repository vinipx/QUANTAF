package io.github.vinipx.quantaf.unit;

import io.github.vinipx.quantaf.core.TradeLedger;
import io.github.vinipx.quantaf.core.TradeLedgerAssert;
import io.github.vinipx.quantaf.core.model.ReconciliationResult;
import io.github.vinipx.quantaf.core.model.TradeRecord;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the TradeLedger reconciliation engine and assertion DSL.
 */
public class TradeLedgerAssertTest {

    private TradeLedger ledger;

    @BeforeMethod
    public void setUp() {
        ledger = new TradeLedger();
    }

    @Test
    public void reconcile_shouldPassWhenAllSourcesMatch() {
        String key = "ORD-001";
        LocalDate settleDate = LocalDate.of(2026, 2, 10);

        ledger.addRecord(TradeRecord.fromFix().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)).quantity(BigDecimal.valueOf(100))
                .amount(BigDecimal.valueOf(15000.00)).currency("USD").settlementDate(settleDate));

        ledger.addRecord(TradeRecord.fromMq().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)).quantity(BigDecimal.valueOf(100))
                .amount(BigDecimal.valueOf(15000.00)).currency("USD").settlementDate(settleDate));

        ledger.addRecord(TradeRecord.fromApi().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)).quantity(BigDecimal.valueOf(100))
                .amount(BigDecimal.valueOf(15000.00)).currency("USD").settlementDate(settleDate));

        ReconciliationResult result = ledger.reconcile(key);
        assertThat(result.isPassed()).isTrue();
        assertThat(result.getMismatches()).isEmpty();

        // Assertion DSL should not throw
        TradeLedgerAssert.assertThat(result).assertParity();
    }

    @Test
    public void reconcile_shouldFailOnPriceMismatch() {
        String key = "ORD-002";

        ledger.addRecord(TradeRecord.fromFix().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)));
        ledger.addRecord(TradeRecord.fromMq().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(151.00)));
        ledger.addRecord(TradeRecord.fromApi().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)));

        ReconciliationResult result = ledger.reconcile(key);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).isNotEmpty();
    }

    @Test
    public void assertParity_shouldThrowOnMismatch() {
        String key = "ORD-003";

        ledger.addRecord(TradeRecord.fromFix().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)));
        ledger.addRecord(TradeRecord.fromMq().clOrdId(key).symbol("GOOG")
                .price(BigDecimal.valueOf(150.00)));
        ledger.addRecord(TradeRecord.fromApi().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)));

        ReconciliationResult result = ledger.reconcile(key);

        assertThatThrownBy(() -> TradeLedgerAssert.assertThat(result).assertParity())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("FAILED");
    }

    @Test
    public void assertSettlementDateMatch_shouldPassWhenDatesMatch() {
        String key = "ORD-004";
        LocalDate date = LocalDate.of(2026, 3, 1);

        ledger.addRecord(TradeRecord.fromFix().clOrdId(key).settlementDate(date));
        ledger.addRecord(TradeRecord.fromMq().clOrdId(key).settlementDate(date));
        ledger.addRecord(TradeRecord.fromApi().clOrdId(key).settlementDate(date));

        ReconciliationResult result = ledger.reconcile(key);
        TradeLedgerAssert.assertThat(result).assertSettlementDateMatch();
    }

    @Test
    public void assertSettlementDateMatch_shouldThrowOnDateMismatch() {
        String key = "ORD-005";

        ledger.addRecord(TradeRecord.fromFix().clOrdId(key)
                .settlementDate(LocalDate.of(2026, 3, 1)));
        ledger.addRecord(TradeRecord.fromMq().clOrdId(key)
                .settlementDate(LocalDate.of(2026, 3, 2)));
        ledger.addRecord(TradeRecord.fromApi().clOrdId(key)
                .settlementDate(LocalDate.of(2026, 3, 1)));

        ReconciliationResult result = ledger.reconcile(key);

        assertThatThrownBy(() -> TradeLedgerAssert.assertThat(result).assertSettlementDateMatch())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Settlement date mismatch");
    }

    @Test
    public void assertAmountMatch_shouldPassWithinTolerance() {
        String key = "ORD-006";

        ledger.addRecord(TradeRecord.fromFix().clOrdId(key)
                .price(BigDecimal.valueOf(150.001)));
        ledger.addRecord(TradeRecord.fromMq().clOrdId(key)
                .price(BigDecimal.valueOf(150.002)));
        ledger.addRecord(TradeRecord.fromApi().clOrdId(key)
                .price(BigDecimal.valueOf(150.001)));

        ReconciliationResult result = ledger.reconcile(key);
        TradeLedgerAssert.assertThat(result).assertAmountMatch(BigDecimal.valueOf(0.01));
    }

    @Test
    public void assertFieldMatch_shouldPassForMatchingField() {
        String key = "ORD-007";

        ledger.addRecord(TradeRecord.fromFix().clOrdId(key).symbol("TSLA"));
        ledger.addRecord(TradeRecord.fromMq().clOrdId(key).symbol("TSLA"));
        ledger.addRecord(TradeRecord.fromApi().clOrdId(key).symbol("TSLA"));

        ReconciliationResult result = ledger.reconcile(key);
        TradeLedgerAssert.assertThat(result).assertFieldMatch("symbol");
    }

    @Test
    public void reconcileAll_shouldReturnResultsForAllKeys() {
        ledger.addRecord(TradeRecord.fromFix().clOrdId("A").symbol("AAPL"));
        ledger.addRecord(TradeRecord.fromFix().clOrdId("B").symbol("GOOG"));
        ledger.addRecord(TradeRecord.fromMq().clOrdId("A").symbol("AAPL"));

        var results = ledger.reconcileAll();
        assertThat(results).hasSize(2);
    }

    @Test
    public void verifyRejectionHandled_shouldReturnTrueWhenRejectionExists() {
        ledger.addRecord(TradeRecord.fromFix().clOrdId("REJ-001").symbol("AAPL").execType("8"));

        assertThat(ledger.verifyRejectionHandled("AAPL")).isTrue();
        assertThat(ledger.verifyRejectionHandled("GOOG")).isFalse();
    }

    @Test
    public void detailedReport_shouldContainAllFields() {
        String key = "RPT-001";

        ledger.addRecord(TradeRecord.fromFix().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)));
        ledger.addRecord(TradeRecord.fromMq().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)));
        ledger.addRecord(TradeRecord.fromApi().clOrdId(key).symbol("AAPL")
                .price(BigDecimal.valueOf(150.00)));

        ReconciliationResult result = ledger.reconcile(key);
        String report = result.toDetailedReport();

        assertThat(report).contains("PASSED");
        assertThat(report).contains("price");
        assertThat(report).contains("symbol");
        assertThat(report).contains("AAPL");
    }

    @Test
    public void clear_shouldRemoveAllRecords() {
        ledger.addRecord(TradeRecord.fromFix().clOrdId("CLR-001").symbol("X"));
        ledger.clear();
        assertThat(ledger.reconcileAll()).isEmpty();
    }
}
