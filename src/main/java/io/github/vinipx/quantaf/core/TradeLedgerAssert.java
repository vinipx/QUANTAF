package io.github.vinipx.quantaf.core;

import io.github.vinipx.quantaf.core.model.ReconciliationResult;
import io.github.vinipx.quantaf.core.model.ReconciliationResult.FieldComparison;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Assertion DSL for trade reconciliation results.
 * Provides fluent, descriptive assertions for cross-source comparisons.
 */
public class TradeLedgerAssert {

    private static final Logger log = LoggerFactory.getLogger(TradeLedgerAssert.class);

    private final ReconciliationResult result;

    private TradeLedgerAssert(ReconciliationResult result) {
        this.result = result;
    }

    /**
     * Creates a new assertion for the given reconciliation result.
     */
    public static TradeLedgerAssert assertThat(ReconciliationResult result) {
        return new TradeLedgerAssert(result);
    }

    /**
     * Asserts that all fields match across all three sources (full parity).
     *
     * @return this for chaining
     * @throws AssertionError if any field has a mismatch
     */
    public TradeLedgerAssert assertParity() {
        if (!result.isPassed()) {
            List<FieldComparison> mismatches = result.getMismatches();
            StringBuilder sb = new StringBuilder();
            sb.append("Reconciliation FAILED for key '").append(result.getCorrelationKey()).append("':\n");
            for (FieldComparison m : mismatches) {
                sb.append(String.format("  [%s] FIX=%s | MQ=%s | API=%s%n",
                        m.fieldName(), m.fixValue(), m.mqValue(), m.apiValue()));
            }
            String message = sb.toString();
            log.error(message);
            throw new AssertionError(message);
        }
        log.info("Parity assertion PASSED for key '{}'", result.getCorrelationKey());
        return this;
    }

    /**
     * Asserts that the amount field matches across sources within the given tolerance.
     *
     * @param tolerance the maximum allowed difference
     * @return this for chaining
     */
    public TradeLedgerAssert assertAmountMatch(BigDecimal tolerance) {
        for (FieldComparison comp : result.getComparisons()) {
            if ("amount".equals(comp.fieldName()) || "price".equals(comp.fieldName())) {
                if (comp.fixValue() != null && comp.mqValue() != null) {
                    BigDecimal fixVal = new BigDecimal(comp.fixValue());
                    BigDecimal mqVal = new BigDecimal(comp.mqValue());
                    BigDecimal delta = fixVal.subtract(mqVal).abs();
                    if (delta.compareTo(tolerance) > 0) {
                        String msg = String.format("Amount mismatch for '%s': FIX=%s, MQ=%s (delta=%s > tolerance=%s)",
                                comp.fieldName(), comp.fixValue(), comp.mqValue(), delta, tolerance);
                        log.error(msg);
                        throw new AssertionError(msg);
                    }
                }
                if (comp.fixValue() != null && comp.apiValue() != null) {
                    BigDecimal fixVal = new BigDecimal(comp.fixValue());
                    BigDecimal apiVal = new BigDecimal(comp.apiValue());
                    BigDecimal delta = fixVal.subtract(apiVal).abs();
                    if (delta.compareTo(tolerance) > 0) {
                        String msg = String.format("Amount mismatch for '%s': FIX=%s, API=%s (delta=%s > tolerance=%s)",
                                comp.fieldName(), comp.fixValue(), comp.apiValue(), delta, tolerance);
                        log.error(msg);
                        throw new AssertionError(msg);
                    }
                }
            }
        }
        log.info("Amount match assertion PASSED for key '{}'", result.getCorrelationKey());
        return this;
    }

    /**
     * Asserts that the settlement dates match across all sources.
     */
    public TradeLedgerAssert assertSettlementDateMatch() {
        for (FieldComparison comp : result.getComparisons()) {
            if ("settlementDate".equals(comp.fieldName()) && !comp.isMatch()) {
                String msg = String.format("Settlement date mismatch: FIX=%s, MQ=%s, API=%s",
                        comp.fixValue(), comp.mqValue(), comp.apiValue());
                log.error(msg);
                throw new AssertionError(msg);
            }
        }
        log.info("Settlement date assertion PASSED for key '{}'", result.getCorrelationKey());
        return this;
    }

    /**
     * Asserts that a specific named field matches across all sources.
     */
    public TradeLedgerAssert assertFieldMatch(String fieldName) {
        for (FieldComparison comp : result.getComparisons()) {
            if (fieldName.equals(comp.fieldName()) && !comp.isMatch()) {
                String msg = String.format("Field '%s' mismatch: FIX=%s, MQ=%s, API=%s",
                        fieldName, comp.fixValue(), comp.mqValue(), comp.apiValue());
                log.error(msg);
                throw new AssertionError(msg);
            }
        }
        log.info("Field '{}' assertion PASSED for key '{}'", fieldName, result.getCorrelationKey());
        return this;
    }

    /**
     * Returns the underlying reconciliation result.
     */
    public ReconciliationResult getResult() {
        return result;
    }
}
