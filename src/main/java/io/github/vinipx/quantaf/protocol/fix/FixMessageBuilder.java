package io.github.vinipx.quantaf.protocol.fix;

import io.github.vinipx.quantaf.core.model.OrderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;
import quickfix.field.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fluent builder for constructing common FIX message types.
 * Supports NewOrderSingle, ExecutionReport, and OrderCancelRequest.
 */
public class FixMessageBuilder {

    private static final Logger log = LoggerFactory.getLogger(FixMessageBuilder.class);

    private final Message message;
    private final String beginString;

    private FixMessageBuilder(String msgType, String beginString) {
        this.message = new Message();
        this.beginString = beginString;
        message.getHeader().setString(MsgType.FIELD, msgType);
        message.getHeader().setString(BeginString.FIELD, beginString);
    }

    // --- Factory Methods ---

    /**
     * Creates a NewOrderSingle (MsgType=D) builder.
     */
    public static FixMessageBuilder newOrderSingle(FixVersion version) {
        return new FixMessageBuilder(MsgType.ORDER_SINGLE, version.getBeginString());
    }

    /**
     * Creates an ExecutionReport (MsgType=8) builder.
     */
    public static FixMessageBuilder executionReport(FixVersion version) {
        return new FixMessageBuilder(MsgType.EXECUTION_REPORT, version.getBeginString());
    }

    /**
     * Creates an OrderCancelRequest (MsgType=F) builder.
     */
    public static FixMessageBuilder orderCancelRequest(FixVersion version) {
        return new FixMessageBuilder(MsgType.ORDER_CANCEL_REQUEST, version.getBeginString());
    }

    // --- Common Fields ---

    public FixMessageBuilder clOrdId(String clOrdId) {
        message.setString(ClOrdID.FIELD, clOrdId);
        return this;
    }

    public FixMessageBuilder orderId(String orderId) {
        message.setString(OrderID.FIELD, orderId);
        return this;
    }

    public FixMessageBuilder execId(String execId) {
        message.setString(ExecID.FIELD, execId);
        return this;
    }

    public FixMessageBuilder symbol(String symbol) {
        message.setString(Symbol.FIELD, symbol);
        return this;
    }

    public FixMessageBuilder side(char side) {
        message.setChar(Side.FIELD, side);
        return this;
    }

    public FixMessageBuilder side(OrderConfiguration.Side side) {
        message.setChar(Side.FIELD, side.getFixValue());
        return this;
    }

    public FixMessageBuilder orderType(char ordType) {
        message.setChar(OrdType.FIELD, ordType);
        return this;
    }

    public FixMessageBuilder orderType(OrderConfiguration.OrderType orderType) {
        message.setChar(OrdType.FIELD, orderType.getFixValue());
        return this;
    }

    public FixMessageBuilder price(BigDecimal price) {
        message.setDecimal(Price.FIELD, price);
        return this;
    }

    public FixMessageBuilder quantity(int qty) {
        message.setInt(OrderQty.FIELD, qty);
        return this;
    }

    public FixMessageBuilder timeInForce(char tif) {
        message.setChar(TimeInForce.FIELD, tif);
        return this;
    }

    public FixMessageBuilder timeInForce(OrderConfiguration.TimeInForce tif) {
        message.setChar(TimeInForce.FIELD, tif.getFixValue());
        return this;
    }

    public FixMessageBuilder account(String account) {
        message.setString(Account.FIELD, account);
        return this;
    }

    public FixMessageBuilder transactTime(LocalDateTime time) {
        message.setUtcTimeStamp(TransactTime.FIELD, time);
        return this;
    }

    public FixMessageBuilder transactTimeNow() {
        message.setUtcTimeStamp(TransactTime.FIELD, LocalDateTime.now());
        return this;
    }

    // --- Execution Report Specific ---

    public FixMessageBuilder execType(char execType) {
        message.setChar(ExecType.FIELD, execType);
        return this;
    }

    public FixMessageBuilder execType(OrderConfiguration.ExecType execType) {
        message.setChar(ExecType.FIELD, execType.getFixValue());
        return this;
    }

    public FixMessageBuilder ordStatus(char ordStatus) {
        message.setChar(OrdStatus.FIELD, ordStatus);
        return this;
    }

    public FixMessageBuilder lastPx(BigDecimal lastPx) {
        message.setDecimal(LastPx.FIELD, lastPx);
        return this;
    }

    public FixMessageBuilder lastQty(int lastQty) {
        message.setInt(LastQty.FIELD, lastQty);
        return this;
    }

    public FixMessageBuilder leavesQty(int leavesQty) {
        message.setInt(LeavesQty.FIELD, leavesQty);
        return this;
    }

    public FixMessageBuilder cumQty(int cumQty) {
        message.setInt(CumQty.FIELD, cumQty);
        return this;
    }

    public FixMessageBuilder avgPx(BigDecimal avgPx) {
        message.setDecimal(AvgPx.FIELD, avgPx);
        return this;
    }

    public FixMessageBuilder currency(String currency) {
        message.setString(Currency.FIELD, currency);
        return this;
    }

    public FixMessageBuilder text(String text) {
        message.setString(Text.FIELD, text);
        return this;
    }

    // --- Custom Field ---

    public FixMessageBuilder setField(int tag, String value) {
        message.setString(tag, value);
        return this;
    }

    public FixMessageBuilder setField(int tag, int value) {
        message.setInt(tag, value);
        return this;
    }

    public FixMessageBuilder setField(int tag, char value) {
        message.setChar(tag, value);
        return this;
    }

    // --- Convenience Methods ---

    /**
     * Creates a NewOrderSingle from an OrderConfiguration.
     */
    public static Message fromOrderConfig(OrderConfiguration config, FixVersion version) {
        FixMessageBuilder builder = newOrderSingle(version)
                .clOrdId(config.getClOrdId() != null ? config.getClOrdId() : UUID.randomUUID().toString())
                .symbol(config.getSymbol())
                .side(config.getSide())
                .orderType(config.getOrderType())
                .quantity(config.getQuantity())
                .transactTimeNow();

        if (config.getPrice() != null) {
            builder.price(config.getPrice());
        }
        if (config.getTimeInForce() != null) {
            builder.timeInForce(config.getTimeInForce());
        }
        if (config.getAccount() != null) {
            builder.account(config.getAccount());
        }
        if (config.getCurrency() != null) {
            builder.currency(config.getCurrency());
        }

        log.debug("Built NewOrderSingle from OrderConfig: {}", config);
        return builder.build();
    }

    /**
     * Creates a rejection ExecutionReport for a given order.
     */
    public static Message rejectionFor(Message order, FixVersion version, String rejectReason) {
        try {
            return executionReport(version)
                    .clOrdId(order.getString(ClOrdID.FIELD))
                    .orderId(UUID.randomUUID().toString())
                    .execId(UUID.randomUUID().toString())
                    .execType(OrderConfiguration.ExecType.REJECTED)
                    .ordStatus('8') // Rejected
                    .symbol(order.getString(Symbol.FIELD))
                    .side(order.getChar(Side.FIELD))
                    .leavesQty(0)
                    .cumQty(0)
                    .avgPx(BigDecimal.ZERO)
                    .text(rejectReason)
                    .transactTimeNow()
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build rejection ExecutionReport", e);
        }
    }

    /**
     * Creates a fill ExecutionReport for a given order.
     */
    public static Message fillFor(Message order, FixVersion version, BigDecimal fillPrice) {
        try {
            int qty = order.getInt(OrderQty.FIELD);
            return executionReport(version)
                    .clOrdId(order.getString(ClOrdID.FIELD))
                    .orderId(UUID.randomUUID().toString())
                    .execId(UUID.randomUUID().toString())
                    .execType(OrderConfiguration.ExecType.FILL)
                    .ordStatus('2') // Filled
                    .symbol(order.getString(Symbol.FIELD))
                    .side(order.getChar(Side.FIELD))
                    .lastPx(fillPrice)
                    .lastQty(qty)
                    .leavesQty(0)
                    .cumQty(qty)
                    .avgPx(fillPrice)
                    .transactTimeNow()
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build fill ExecutionReport", e);
        }
    }

    /**
     * Builds and returns the FIX message.
     */
    public Message build() {
        return message;
    }
}
