package io.github.vinipx.quantaf.protocol.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages multiple FIX sessions across different protocol versions.
 * Handles session lifecycle: logon, logout, heartbeat, and message routing.
 */
public class FixSessionManager {

    private static final Logger log = LoggerFactory.getLogger(FixSessionManager.class);

    private final Map<FixVersion, SocketInitiator> initiators = new ConcurrentHashMap<>();
    private final Map<FixVersion, SocketAcceptor> acceptors = new ConcurrentHashMap<>();
    private final FixStubRegistry stubRegistry;

    public FixSessionManager(FixStubRegistry stubRegistry) {
        this.stubRegistry = stubRegistry;
    }

    /**
     * Starts an initiator session (client/trader side) for the given FIX version.
     */
    public void startInitiator(FixVersion version, Application application) throws ConfigError {
        SessionSettings settings = loadSettings(version);
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        SocketInitiator initiator = new SocketInitiator(
                application, storeFactory, settings, logFactory, messageFactory);
        initiator.start();
        initiators.put(version, initiator);
        log.info("FIX Initiator started [version={}, sessions={}]", version, initiator.getSessions());
    }

    /**
     * Starts an acceptor session (exchange stub side) for the given FIX version.
     */
    public void startAcceptor(FixVersion version, Application application) throws ConfigError {
        SessionSettings settings = loadSettings(version);
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        SocketAcceptor acceptor = new SocketAcceptor(
                application, storeFactory, settings, logFactory, messageFactory);
        acceptor.start();
        acceptors.put(version, acceptor);
        log.info("FIX Acceptor started [version={}, sessions={}]", version, acceptor.getSessions());
    }

    /**
     * Stops all initiators and acceptors.
     */
    public void stopAll() {
        initiators.forEach((version, initiator) -> {
            log.info("Stopping FIX Initiator [version={}]", version);
            initiator.stop();
        });
        acceptors.forEach((version, acceptor) -> {
            log.info("Stopping FIX Acceptor [version={}]", version);
            acceptor.stop();
        });
        initiators.clear();
        acceptors.clear();
        log.info("All FIX sessions stopped");
    }

    /**
     * Stops the initiator for the given FIX version.
     */
    public void stopInitiator(FixVersion version) {
        SocketInitiator initiator = initiators.remove(version);
        if (initiator != null) {
            initiator.stop();
            log.info("FIX Initiator stopped [version={}]", version);
        }
    }

    /**
     * Stops the acceptor for the given FIX version.
     */
    public void stopAcceptor(FixVersion version) {
        SocketAcceptor acceptor = acceptors.remove(version);
        if (acceptor != null) {
            acceptor.stop();
            log.info("FIX Acceptor stopped [version={}]", version);
        }
    }

    /**
     * Checks if an initiator session is logged on.
     */
    public boolean isInitiatorLoggedOn(FixVersion version) {
        SocketInitiator initiator = initiators.get(version);
        return initiator != null && initiator.isLoggedOn();
    }

    /**
     * Checks if an acceptor session is logged on.
     */
    public boolean isAcceptorLoggedOn(FixVersion version) {
        SocketAcceptor acceptor = acceptors.get(version);
        return acceptor != null && acceptor.isLoggedOn();
    }

    public FixStubRegistry getStubRegistry() {
        return stubRegistry;
    }

    private SessionSettings loadSettings(FixVersion version) throws ConfigError {
        String configFile = version.getConfigFile();
        InputStream is = getClass().getClassLoader().getResourceAsStream(configFile);
        if (is == null) {
            throw new ConfigError("Configuration file not found: " + configFile);
        }
        return new SessionSettings(is);
    }
}
