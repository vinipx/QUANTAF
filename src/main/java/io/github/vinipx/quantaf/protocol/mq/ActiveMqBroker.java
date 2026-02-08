package io.github.vinipx.quantaf.protocol.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.*;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 * ActiveMQ Artemis implementation of the {@link MessageBroker} interface.
 * Uses JMS (Jakarta Messaging) for communication.
 */
public class ActiveMqBroker implements MessageBroker {

    private static final Logger log = LoggerFactory.getLogger(ActiveMqBroker.class);

    private final Connection connection;
    private final Session session;
    private final ExecutorService executor;

    public ActiveMqBroker(String brokerUrl, String username, String password) {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            this.connection = factory.createConnection(username, password);
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.executor = Executors.newCachedThreadPool();
            log.info("ActiveMQ broker connected [url={}]", brokerUrl);
        } catch (JMSException e) {
            throw new RuntimeException("Failed to connect to ActiveMQ broker: " + brokerUrl, e);
        }
    }

    @Override
    public void publish(String destination, String payload) {
        try {
            Queue queue = session.createQueue(destination);
            MessageProducer producer = session.createProducer(queue);
            TextMessage message = session.createTextMessage(payload);
            producer.send(message);
            producer.close();
            log.debug("Published message to {} ({} bytes)", destination, payload.length());
        } catch (JMSException e) {
            throw new RuntimeException("Failed to publish message to: " + destination, e);
        }
    }

    @Override
    public CompletableFuture<String> listen(String destination, Duration timeout) {
        return listenWithFilter(destination, msg -> true, timeout);
    }

    @Override
    public CompletableFuture<String> listenWithFilter(String destination, Predicate<String> filter, Duration timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Queue queue = session.createQueue(destination);
                MessageConsumer consumer = session.createConsumer(queue);
                long deadline = System.currentTimeMillis() + timeout.toMillis();

                while (System.currentTimeMillis() < deadline) {
                    long remaining = deadline - System.currentTimeMillis();
                    if (remaining <= 0) break;

                    Message message = consumer.receive(Math.min(remaining, 1000));
                    if (message instanceof TextMessage textMessage) {
                        String payload = textMessage.getText();
                        if (filter.test(payload)) {
                            consumer.close();
                            log.debug("Received matching message from {} ({} bytes)", destination, payload.length());
                            return payload;
                        }
                    }
                }

                consumer.close();
                log.warn("Timeout waiting for message on {} ({}ms)", destination, timeout.toMillis());
                return null;
            } catch (JMSException e) {
                throw new RuntimeException("Error listening on: " + destination, e);
            }
        }, executor);
    }

    @Override
    public boolean isConnected() {
        try {
            // Attempt to create a temporary session to check connectivity
            Session testSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            testSession.close();
            return true;
        } catch (JMSException e) {
            return false;
        }
    }

    @Override
    public void close() {
        try {
            session.close();
            connection.close();
            executor.shutdown();
            log.info("ActiveMQ broker connection closed");
        } catch (JMSException e) {
            log.warn("Error closing ActiveMQ connection", e);
        }
    }
}
