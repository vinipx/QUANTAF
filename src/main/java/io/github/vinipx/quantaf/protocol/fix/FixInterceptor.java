package io.github.vinipx.quantaf.protocol.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.ClOrdID;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

import java.time.Duration;

/**
 * FIX message interceptor that evaluates incoming messages against the
 * {@link FixStubRegistry} and auto-generates responses.
 * <p>
 * This class should be called from the {@code fromApp(Message, SessionID)} callback
 * of a QuickFIX/J Application.
 */
public class FixInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FixInterceptor.class);
    private final FixStubRegistry registry;

    public FixInterceptor(FixStubRegistry registry) {
        this.registry = registry;
    }

    /**
     * Processes an incoming FIX message against the stub registry.
     *
     * @param message   the incoming FIX message
     * @param sessionId the QuickFIX/J session ID
     * @return true if the message was handled by a stub, false otherwise
     */
    public boolean intercept(Message message, SessionID sessionId) {
        FixStubRegistry.StubMapping mapping = registry.findMatch(message);
        if (mapping == null) {
            log.debug("No stub match for message on session {}", sessionId);
            return false;
        }

        try {
            // Apply delay if configured
            Duration delay = mapping.getDelay();
            if (delay != null && !delay.isZero()) {
                log.debug("Applying stub delay: {} ms", delay.toMillis());
                Thread.sleep(delay.toMillis());
            }

            // Generate response
            Message response = mapping.generateResponse(message);
            if (response == null) {
                log.warn("Stub generated null response for: {}", mapping.getDescription());
                return false;
            }

            // Swap sender/target CompIDs
            swapCompIds(response, sessionId);

            // Copy correlation fields from request to response
            copyCorrelationFields(message, response);

            // Send the response
            Session.sendToTarget(response, sessionId);
            log.info("Stub response sent for: {} [session={}]", mapping.getDescription(), sessionId);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Stub delay interrupted", e);
            return false;
        } catch (SessionNotFound e) {
            log.error("Session not found when sending stub response: {}", sessionId, e);
            return false;
        }
    }

    /**
     * Swaps SenderCompID and TargetCompID so the response goes back to the sender.
     */
    private void swapCompIds(Message response, SessionID sessionId) {
        response.getHeader().setString(SenderCompID.FIELD, sessionId.getTargetCompID());
        response.getHeader().setString(TargetCompID.FIELD, sessionId.getSenderCompID());
    }

    /**
     * Copies correlation fields (ClOrdID, etc.) from request to response if present.
     */
    private void copyCorrelationFields(Message request, Message response) {
        // Copy ClOrdID
        trySetField(request, response, ClOrdID.FIELD);
    }

    private void trySetField(Message source, Message target, int tag) {
        try {
            if (source.isSetField(tag)) {
                String value = source.getString(tag);
                target.setString(tag, value);
            }
        } catch (FieldNotFound e) {
            // Field not present, skip
        }
    }
}
