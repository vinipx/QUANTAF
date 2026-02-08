package io.github.vinipx.quantaf.core.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a normalized trade record from any source (FIX, MQ, API).
 * Used by the TradeLedger for cross-source reconciliation.
 */
public class TradeRecord {

    public enum Source {
        FIX, MQ, API
    }

    private final Source source;
    private String orderId;
    private String clOrdId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private String currency;
    private LocalDate settlementDate;
    private LocalDateTime executionTime;
    private String account;
    private String execType;
    private final Map<String, String> additionalFields;

    public TradeRecord(Source source) {
        this.source = source;
        this.additionalFields = new HashMap<>();
    }

    // --- Fluent Builder ---

    public static TradeRecord fromFix() {
        return new TradeRecord(Source.FIX);
    }

    public static TradeRecord fromMq() {
        return new TradeRecord(Source.MQ);
    }

    public static TradeRecord fromApi() {
        return new TradeRecord(Source.API);
    }

    public TradeRecord orderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public TradeRecord clOrdId(String clOrdId) {
        this.clOrdId = clOrdId;
        return this;
    }

    public TradeRecord symbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public TradeRecord quantity(BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    public TradeRecord price(BigDecimal price) {
        this.price = price;
        return this;
    }

    public TradeRecord amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TradeRecord currency(String currency) {
        this.currency = currency;
        return this;
    }

    public TradeRecord settlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
        return this;
    }

    public TradeRecord executionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
        return this;
    }

    public TradeRecord account(String account) {
        this.account = account;
        return this;
    }

    public TradeRecord execType(String execType) {
        this.execType = execType;
        return this;
    }

    public TradeRecord withField(String key, String value) {
        this.additionalFields.put(key, value);
        return this;
    }

    // --- Getters ---

    public Source getSource() { return source; }
    public String getOrderId() { return orderId; }
    public String getClOrdId() { return clOrdId; }
    public String getSymbol() { return symbol; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public LocalDateTime getExecutionTime() { return executionTime; }
    public String getAccount() { return account; }
    public String getExecType() { return execType; }
    public Map<String, String> getAdditionalFields() { return additionalFields; }
    public String getField(String key) { return additionalFields.get(key); }

    /**
     * Returns the correlation key for matching records across sources.
     * Uses ClOrdID if available, otherwise OrderID.
     */
    public String getCorrelationKey() {
        return clOrdId != null ? clOrdId : orderId;
    }

    @Override
    public String toString() {
        return String.format("TradeRecord{source=%s, clOrdId='%s', symbol='%s', qty=%s, price=%s, amount=%s, settle=%s}",
                source, clOrdId, symbol, quantity, price, amount, settlementDate);
    }
}
