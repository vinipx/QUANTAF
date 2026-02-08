package io.github.vinipx.quantaf.protocol.mq;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Pluggable interface for message broker interactions.
 * Implementations include ActiveMQ, IBM MQ, Kafka, etc.
 */
public interface MessageBroker {

    /**
     * Publishes a message payload to the given destination (queue or topic).
     *
     * @param destination the queue/topic name
     * @param payload     the message payload
     */
    void publish(String destination, String payload);

    /**
     * Listens for a single message on the given destination with a timeout.
     *
     * @param destination the queue/topic name
     * @param timeout     the maximum time to wait for a message
     * @return a CompletableFuture that completes with the message payload
     */
    CompletableFuture<String> listen(String destination, Duration timeout);

    /**
     * Listens for a message matching a filter on the given destination.
     *
     * @param destination the queue/topic name
     * @param filter      a predicate to match the desired message
     * @param timeout     the maximum time to wait
     * @return a CompletableFuture that completes with the matching message payload
     */
    CompletableFuture<String> listenWithFilter(String destination, Predicate<String> filter, Duration timeout);

    /**
     * Checks if the broker connection is active.
     */
    boolean isConnected();

    /**
     * Closes the broker connection and releases resources.
     */
    void close();
}
