package io.github.vinipx.quantaf.core.model;

import java.math.BigDecimal;

/**
 * Represents a structured order configuration, typically generated from
 * natural language intent via the AI Cortex or constructed programmatically.
 */
public class OrderConfiguration {

    private String symbol;
    private Side side;
    private OrderType orderType;
    private BigDecimal price;
    private int quantity;
    private TimeInForce timeInForce;
    private String account;
    private String clOrdId;
    private String currency;
    private ExecType expectedExecType;

    public OrderConfiguration() {
    }

    // --- Fluent Builder Pattern ---

    public static OrderConfiguration create() {
        return new OrderConfiguration();
    }

    public OrderConfiguration symbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public OrderConfiguration side(Side side) {
        this.side = side;
        return this;
    }

    public OrderConfiguration orderType(OrderType orderType) {
        this.orderType = orderType;
        return this;
    }

    public OrderConfiguration price(BigDecimal price) {
        this.price = price;
        return this;
    }

    public OrderConfiguration quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderConfiguration timeInForce(TimeInForce tif) {
        this.timeInForce = tif;
        return this;
    }

    public OrderConfiguration account(String account) {
        this.account = account;
        return this;
    }

    public OrderConfiguration clOrdId(String clOrdId) {
        this.clOrdId = clOrdId;
        return this;
    }

    public OrderConfiguration currency(String currency) {
        this.currency = currency;
        return this;
    }

    public OrderConfiguration expectedExecType(ExecType execType) {
        this.expectedExecType = execType;
        return this;
    }

    // --- Getters ---

    public String getSymbol() { return symbol; }
    public Side getSide() { return side; }
    public OrderType getOrderType() { return orderType; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public TimeInForce getTimeInForce() { return timeInForce; }
    public String getAccount() { return account; }
    public String getClOrdId() { return clOrdId; }
    public String getCurrency() { return currency; }
    public ExecType getExpectedExecType() { return expectedExecType; }

    // --- Setters (for Jackson deserialization) ---

    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setSide(Side side) { this.side = side; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setTimeInForce(TimeInForce timeInForce) { this.timeInForce = timeInForce; }
    public void setAccount(String account) { this.account = account; }
    public void setClOrdId(String clOrdId) { this.clOrdId = clOrdId; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setExpectedExecType(ExecType expectedExecType) { this.expectedExecType = expectedExecType; }

    @Override
    public String toString() {
        return String.format("OrderConfig{symbol='%s', side=%s, type=%s, price=%s, qty=%d, tif=%s, account='%s', clOrdId='%s'}",
                symbol, side, orderType, price, quantity, timeInForce, account, clOrdId);
    }

    // --- Enums ---

    public enum Side {
        BUY('1'), SELL('2'), SHORT_SELL('5');

        private final char fixValue;

        Side(char fixValue) { this.fixValue = fixValue; }

        public char getFixValue() { return fixValue; }
    }

    public enum OrderType {
        MARKET('1'), LIMIT('2'), STOP('3'), STOP_LIMIT('4');

        private final char fixValue;

        OrderType(char fixValue) { this.fixValue = fixValue; }

        public char getFixValue() { return fixValue; }
    }

    public enum TimeInForce {
        DAY('0'), GTC('1'), IOC('3'), FOK('4'), GTD('6'), AT_CLOSE('7');

        private final char fixValue;

        TimeInForce(char fixValue) { this.fixValue = fixValue; }

        public char getFixValue() { return fixValue; }
    }

    public enum ExecType {
        NEW('0'), PARTIAL_FILL('1'), FILL('2'), CANCELED('4'),
        REPLACED('5'), PENDING_CANCEL('6'), REJECTED('8');

        private final char fixValue;

        ExecType(char fixValue) { this.fixValue = fixValue; }

        public char getFixValue() { return fixValue; }
    }
}
