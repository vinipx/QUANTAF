package io.github.vinipx.quantaf.protocol.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Client-side FIX wrapper that sends messages and correlates responses via ClOrdID.
 * Supports async response awaiting via {@link CompletableFuture}.
 */
public class FixInitiatorWrapper implements Application {

    private static final Logger log = LoggerFactory.getLogger(FixInitiatorWrapper.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;

    private final Map<String, CompletableFuture<Message>> pendingResponses = new ConcurrentHashMap<>();
    private SessionID activeSessionId;

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("Session created: {}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        this.activeSessionId = sessionId;
        log.info("Logon successful: {}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("Logout: {}", sessionId);
        if (sessionId.equals(activeSessionId)) {
            activeSessionId = null;
        }
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        // Logon/logout/heartbeat messages
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        // Admin messages from counterparty
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        log.debug("Sending message: {}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        log.debug("Received message: {}", message);

        // Correlate response by ClOrdID
        try {
            if (message.isSetField(quickfix.field.ClOrdID.FIELD)) {
                String clOrdId = message.getString(quickfix.field.ClOrdID.FIELD);
                CompletableFuture<Message> future = pendingResponses.remove(clOrdId);
                if (future != null) {
                    future.complete(message);
                    log.debug("Response correlated for ClOrdID: {}", clOrdId);
                }
            }
        } catch (FieldNotFound e) {
            log.warn("Could not extract ClOrdID from response", e);
        }
    }

    /**
     * Sends a FIX message and returns a Future that completes when the response arrives.
     *
     * @param message the FIX message to send
     * @param clOrdId the ClOrdID for response correlation
     * @return a CompletableFuture that will contain the response message
     * @throws SessionNotFound if the session is not available
     */
    public CompletableFuture<Message> sendAndAwait(Message message, String clOrdId) throws SessionNotFound {
        if (activeSessionId == null) {
            throw new SessionNotFound("No active FIX session");
        }
        CompletableFuture<Message> future = new CompletableFuture<>();
        pendingResponses.put(clOrdId, future);
        Session.sendToTarget(message, activeSessionId);
        log.info("Message sent, awaiting response for ClOrdID: {}", clOrdId);

        // Auto-timeout
        future.orTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return future;
    }

    /**
     * Sends a FIX message without waiting for a response (fire-and-forget).
     */
    public void send(Message message) throws SessionNotFound {
        if (activeSessionId == null) {
            throw new SessionNotFound("No active FIX session");
        }
        Session.sendToTarget(message, activeSessionId);
        log.info("Message sent (fire-and-forget)");
    }

    /**
     * Returns the active session ID, or null if not logged on.
     */
    public SessionID getActiveSessionId() {
        return activeSessionId;
    }

    /**
     * Checks if the session is currently logged on.
     */
    public boolean isLoggedOn() {
        return activeSessionId != null;
    }
}
