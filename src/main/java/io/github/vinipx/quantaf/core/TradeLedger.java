package io.github.vinipx.quantaf.core;

import io.github.vinipx.quantaf.core.model.ReconciliationResult;
import io.github.vinipx.quantaf.core.model.ReconciliationResult.FieldComparison;
import io.github.vinipx.quantaf.core.model.TradeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * The Ledger: Cross-source reconciliation engine.
 * Accepts trade records from FIX, MQ, and API sources, normalizes them,
 * and performs field-by-field comparison.
 */
public class TradeLedger {

    private static final Logger log = LoggerFactory.getLogger(TradeLedger.class);

    private final int precision;
    private final BigDecimal tolerance;
    private final MathContext mathContext;

    private final Map<String, TradeRecord> fixRecords = new LinkedHashMap<>();
    private final Map<String, TradeRecord> mqRecords = new LinkedHashMap<>();
    private final Map<String, TradeRecord> apiRecords = new LinkedHashMap<>();

    public TradeLedger() {
        this(8, BigDecimal.valueOf(0.0001));
    }

    public TradeLedger(int precision, BigDecimal tolerance) {
        this.precision = precision;
        this.tolerance = tolerance;
        this.mathContext = new MathContext(precision, RoundingMode.HALF_EVEN);
    }

    /**
     * Adds a trade record to the ledger. The record's source determines
     * which internal map it is stored in.
     */
    public void addRecord(TradeRecord record) {
        String key = record.getCorrelationKey();
        if (key == null) {
            throw new IllegalArgumentException("Trade record must have a correlation key (ClOrdID or OrderID)");
        }
        switch (record.getSource()) {
            case FIX -> fixRecords.put(key, record);
            case MQ -> mqRecords.put(key, record);
            case API -> apiRecords.put(key, record);
        }
        log.debug("Added {} record for key: {}", record.getSource(), key);
    }

    /**
     * Reconciles all records for a given correlation key across all three sources.
     *
     * @param correlationKey the ClOrdID or OrderID to reconcile
     * @return the reconciliation result with field-level comparisons
     */
    public ReconciliationResult reconcile(String correlationKey) {
        log.info("Reconciling records for key: {}", correlationKey);

        TradeRecord fix = fixRecords.get(correlationKey);
        TradeRecord mq = mqRecords.get(correlationKey);
        TradeRecord api = apiRecords.get(correlationKey);

        ReconciliationResult result = new ReconciliationResult(correlationKey);

        // Compare amounts
        compareAmounts(result, "price", fix != null ? fix.getPrice() : null,
                mq != null ? mq.getPrice() : null, api != null ? api.getPrice() : null);
        compareAmounts(result, "quantity", fix != null ? fix.getQuantity() : null,
                mq != null ? mq.getQuantity() : null, api != null ? api.getQuantity() : null);
        compareAmounts(result, "amount", fix != null ? fix.getAmount() : null,
                mq != null ? mq.getAmount() : null, api != null ? api.getAmount() : null);

        // Compare settlement dates
        compareDates(result, "settlementDate", fix != null ? fix.getSettlementDate() : null,
                mq != null ? mq.getSettlementDate() : null, api != null ? api.getSettlementDate() : null);

        // Compare string fields
        compareStrings(result, "symbol", fix != null ? fix.getSymbol() : null,
                mq != null ? mq.getSymbol() : null, api != null ? api.getSymbol() : null);
        compareStrings(result, "currency", fix != null ? fix.getCurrency() : null,
                mq != null ? mq.getCurrency() : null, api != null ? api.getCurrency() : null);
        compareStrings(result, "account", fix != null ? fix.getAccount() : null,
                mq != null ? mq.getAccount() : null, api != null ? api.getAccount() : null);

        log.info("Reconciliation result for {}: {}", correlationKey, result.isPassed() ? "PASSED" : "FAILED");
        return result;
    }

    /**
     * Reconciles all known correlation keys.
     */
    public List<ReconciliationResult> reconcileAll() {
        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(fixRecords.keySet());
        allKeys.addAll(mqRecords.keySet());
        allKeys.addAll(apiRecords.keySet());

        return allKeys.stream().map(this::reconcile).toList();
    }

    /**
     * Checks if a rejection was properly handled for the given symbol.
     */
    public boolean verifyRejectionHandled(String symbol) {
        return fixRecords.values().stream()
                .anyMatch(r -> symbol.equals(r.getSymbol()) && "8".equals(r.getExecType()));
    }

    /**
     * Clears all records from the ledger.
     */
    public void clear() {
        fixRecords.clear();
        mqRecords.clear();
        apiRecords.clear();
        log.info("Ledger cleared");
    }

    // --- Comparison Methods ---

    private void compareAmounts(ReconciliationResult result, String fieldName,
                                BigDecimal fixVal, BigDecimal mqVal, BigDecimal apiVal) {
        BigDecimal normFix = normalize(fixVal);
        BigDecimal normMq = normalize(mqVal);
        BigDecimal normApi = normalize(apiVal);

        boolean match = isAmountMatch(normFix, normMq) && isAmountMatch(normFix, normApi) && isAmountMatch(normMq, normApi);

        result.addComparison(match
                ? FieldComparison.match(fieldName, str(normFix), str(normMq), str(normApi))
                : FieldComparison.mismatch(fieldName, str(normFix), str(normMq), str(normApi)));
    }

    private void compareDates(ReconciliationResult result, String fieldName,
                              LocalDate fixVal, LocalDate mqVal, LocalDate apiVal) {
        boolean match = Objects.equals(fixVal, mqVal) && Objects.equals(fixVal, apiVal) && Objects.equals(mqVal, apiVal);
        result.addComparison(match
                ? FieldComparison.match(fieldName, str(fixVal), str(mqVal), str(apiVal))
                : FieldComparison.mismatch(fieldName, str(fixVal), str(mqVal), str(apiVal)));
    }

    private void compareStrings(ReconciliationResult result, String fieldName,
                                String fixVal, String mqVal, String apiVal) {
        boolean match = Objects.equals(fixVal, mqVal) && Objects.equals(fixVal, apiVal) && Objects.equals(mqVal, apiVal);
        result.addComparison(match
                ? FieldComparison.match(fieldName, fixVal, mqVal, apiVal)
                : FieldComparison.mismatch(fieldName, fixVal, mqVal, apiVal));
    }

    private BigDecimal normalize(BigDecimal value) {
        return value != null ? value.round(mathContext) : null;
    }

    private boolean isAmountMatch(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.subtract(b).abs().compareTo(tolerance) <= 0;
    }

    private String str(Object value) {
        return value != null ? value.toString() : null;
    }
}
