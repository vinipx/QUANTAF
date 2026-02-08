package io.github.vinipx.quantaf.protocol.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side FIX wrapper that receives incoming messages and routes them
 * through the {@link FixInterceptor} for stub-based responses.
 */
public class FixAcceptorWrapper implements Application {

    private static final Logger log = LoggerFactory.getLogger(FixAcceptorWrapper.class);

    private final FixInterceptor interceptor;
    private final List<Message> receivedMessages = new ArrayList<>();
    private SessionID activeSessionId;

    public FixAcceptorWrapper(FixStubRegistry stubRegistry) {
        this.interceptor = new FixInterceptor(stubRegistry);
    }

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("Acceptor session created: {}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        this.activeSessionId = sessionId;
        log.info("Acceptor logon: {}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("Acceptor logout: {}", sessionId);
        if (sessionId.equals(activeSessionId)) {
            activeSessionId = null;
        }
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        // Admin messages to counterparty
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        // Admin messages from counterparty
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        log.debug("Acceptor sending message: {}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        log.info("Acceptor received message: {}", message);
        receivedMessages.add(message);

        // Route through stub interceptor
        boolean handled = interceptor.intercept(message, sessionId);
        if (!handled) {
            log.info("Message not handled by any stub [session={}]", sessionId);
        }
    }

    /**
     * Returns all messages received by this acceptor.
     */
    public List<Message> getReceivedMessages() {
        return List.copyOf(receivedMessages);
    }

    /**
     * Clears the received messages buffer.
     */
    public void clearReceivedMessages() {
        receivedMessages.clear();
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
