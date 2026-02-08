package io.github.vinipx.quantaf.unit;

import io.github.vinipx.quantaf.protocol.fix.FixMessageBuilder;
import io.github.vinipx.quantaf.protocol.fix.FixStubRegistry;
import io.github.vinipx.quantaf.protocol.fix.FixVersion;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import quickfix.Message;
import quickfix.field.*;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the FIX Stub Registry (WireMock-like API).
 */
public class FixStubRegistryTest {

    private FixStubRegistry registry;

    @BeforeMethod
    public void setUp() {
        registry = new FixStubRegistry();
    }

    @Test
    public void whenNoStubsRegistered_shouldReturnNull() {
        Message order = FixMessageBuilder.newOrderSingle(FixVersion.FIX44)
                .symbol("AAPL")
                .side('1')
                .orderType('2')
                .price(BigDecimal.valueOf(150.0))
                .quantity(100)
                .clOrdId("TEST-001")
                .transactTimeNow()
                .build();

        assertThat(registry.findMatch(order)).isNull();
    }

    @Test
    public void whenStubMatchesSymbol_shouldReturnMapping() {
        registry.when(msg -> {
            try {
                return msg.getString(Symbol.FIELD).equals("AAPL");
            } catch (Exception e) {
                return false;
            }
        }).respondWith(req -> FixMessageBuilder.executionReport(FixVersion.FIX44)
                .execType('0')
                .ordStatus('0')
                .symbol("AAPL")
                .side('1')
                .leavesQty(100)
                .cumQty(0)
                .avgPx(BigDecimal.ZERO)
                .build()
        ).describedAs("AAPL new order ack").register();

        Message order = FixMessageBuilder.newOrderSingle(FixVersion.FIX44)
                .symbol("AAPL")
                .side('1')
                .orderType('2')
                .price(BigDecimal.valueOf(150.0))
                .quantity(100)
                .clOrdId("TEST-001")
                .transactTimeNow()
                .build();

        FixStubRegistry.StubMapping match = registry.findMatch(order);
        assertThat(match).isNotNull();
        assertThat(match.getDescription()).isEqualTo("AAPL new order ack");
    }

    @Test
    public void whenStubDoesNotMatch_shouldReturnNull() {
        registry.when(msg -> {
            try {
                return msg.getString(Symbol.FIELD).equals("GOOG");
            } catch (Exception e) {
                return false;
            }
        }).respondWith(req -> new Message()).describedAs("GOOG stub").register();

        Message order = FixMessageBuilder.newOrderSingle(FixVersion.FIX44)
                .symbol("AAPL")
                .side('1')
                .orderType('2')
                .price(BigDecimal.valueOf(150.0))
                .quantity(100)
                .clOrdId("TEST-002")
                .transactTimeNow()
                .build();

        assertThat(registry.findMatch(order)).isNull();
    }

    @Test
    public void sequentialResponses_shouldCycleThroughGenerators() throws Exception {
        registry.when(msg -> true)
                .respondWith(req -> {
                    Message m = new Message();
                    m.setString(Text.FIELD, "response-1");
                    return m;
                })
                .thenRespondWith(req -> {
                    Message m = new Message();
                    m.setString(Text.FIELD, "response-2");
                    return m;
                })
                .describedAs("sequential stub")
                .register();

        Message request = new Message();
        FixStubRegistry.StubMapping match = registry.findMatch(request);
        assertThat(match).isNotNull();

        // First call should return response-1
        Message resp1 = match.generateResponse(request);
        assertThat(resp1.getString(Text.FIELD)).isEqualTo("response-1");

        // Second call should return response-2
        Message resp2 = match.generateResponse(request);
        assertThat(resp2.getString(Text.FIELD)).isEqualTo("response-2");

        // Third call should still return response-2 (last generator sticks)
        Message resp3 = match.generateResponse(request);
        assertThat(resp3.getString(Text.FIELD)).isEqualTo("response-2");
    }

    @Test
    public void reset_shouldClearAllMappings() {
        registry.when(msg -> true).respondWith(req -> new Message()).describedAs("test").register();
        assertThat(registry.size()).isEqualTo(1);

        registry.reset();
        assertThat(registry.size()).isEqualTo(0);
    }

    @Test
    public void callCount_shouldTrackInvocations() {
        registry.when(msg -> true)
                .respondWith(req -> new Message())
                .describedAs("counter stub")
                .register();

        Message request = new Message();
        FixStubRegistry.StubMapping match = registry.findMatch(request);
        assertThat(match.getCallCount()).isEqualTo(0);

        match.generateResponse(request);
        assertThat(match.getCallCount()).isEqualTo(1);

        match.generateResponse(request);
        assertThat(match.getCallCount()).isEqualTo(2);
    }

    @Test
    public void withDelay_shouldSetDelay() {
        registry.when(msg -> true)
                .respondWith(req -> new Message())
                .withDelay(Duration.ofMillis(500))
                .describedAs("delayed stub")
                .register();

        FixStubRegistry.StubMapping match = registry.findMatch(new Message());
        assertThat(match.getDelay()).isEqualTo(Duration.ofMillis(500));
    }

    @Test
    public void registerWithoutResponse_shouldThrow() {
        assertThatIllegalStateException()
                .isThrownBy(() -> registry.when(msg -> true).register())
                .withMessageContaining("response generator");
    }

    @Test
    public void fixMessageBuilder_shouldCreateNewOrderSingle() throws Exception {
        Message order = FixMessageBuilder.newOrderSingle(FixVersion.FIX44)
                .clOrdId("ORD-001")
                .symbol("MSFT")
                .side('1')
                .orderType('2')
                .price(BigDecimal.valueOf(305.50))
                .quantity(500)
                .account("FUND-001")
                .timeInForce('0')
                .transactTimeNow()
                .build();

        assertThat(order.getString(ClOrdID.FIELD)).isEqualTo("ORD-001");
        assertThat(order.getString(Symbol.FIELD)).isEqualTo("MSFT");
        assertThat(order.getChar(Side.FIELD)).isEqualTo('1');
        assertThat(order.getChar(OrdType.FIELD)).isEqualTo('2');
        assertThat(order.getDecimal(Price.FIELD)).isEqualByComparingTo(BigDecimal.valueOf(305.50));
        assertThat(order.getInt(OrderQty.FIELD)).isEqualTo(500);
    }

    @Test
    public void fixMessageBuilder_rejectionFor_shouldCreateRejectReport() throws Exception {
        Message order = FixMessageBuilder.newOrderSingle(FixVersion.FIX44)
                .clOrdId("ORD-002")
                .symbol("TSLA")
                .side('1')
                .orderType('1')
                .quantity(1000)
                .transactTimeNow()
                .build();

        Message rejection = FixMessageBuilder.rejectionFor(order, FixVersion.FIX44, "Fat finger check");

        assertThat(rejection.getString(ClOrdID.FIELD)).isEqualTo("ORD-002");
        assertThat(rejection.getString(Symbol.FIELD)).isEqualTo("TSLA");
        assertThat(rejection.getChar(ExecType.FIELD)).isEqualTo('8');
        assertThat(rejection.getString(Text.FIELD)).isEqualTo("Fat finger check");
    }

    @Test
    public void fixVersion_fromString_shouldResolveCorrectly() {
        assertThat(FixVersion.fromString("FIX44")).isEqualTo(FixVersion.FIX44);
        assertThat(FixVersion.fromString("FIX.4.4")).isEqualTo(FixVersion.FIX44);
        assertThat(FixVersion.fromString("FIX42")).isEqualTo(FixVersion.FIX42);
        assertThat(FixVersion.fromString("FIX50")).isEqualTo(FixVersion.FIX50);
        assertThat(FixVersion.fromString(null)).isEqualTo(FixVersion.FIX44);
    }
}
