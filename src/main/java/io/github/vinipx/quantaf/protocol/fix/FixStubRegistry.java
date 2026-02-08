package io.github.vinipx.quantaf.protocol.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Thread-safe registry of FIX stub mappings, providing a WireMock-like API
 * for configuring how the QUANTAF exchange stub responds to incoming messages.
 *
 * <p>Usage example:
 * <pre>
 * registry.when(msg -&gt; msg.getString(Symbol.FIELD).equals("AAPL"))
 *         .respondWith(req -&gt; new ExecutionReportBuilder(req).asRejected().build());
 * </pre>
 */
public class FixStubRegistry {

    private static final Logger log = LoggerFactory.getLogger(FixStubRegistry.class);
    private final CopyOnWriteArrayList<StubMapping> mappings = new CopyOnWriteArrayList<>();

    /**
     * Starts building a new stub mapping with the given criteria.
     *
     * @param criteria predicate to match incoming FIX messages
     * @return a builder for configuring the response
     */
    public StubMappingBuilder when(Predicate<Message> criteria) {
        return new StubMappingBuilder(criteria);
    }

    /**
     * Finds the first matching stub for the given message.
     *
     * @param message the incoming FIX message
     * @return the matching stub mapping, or null if none match
     */
    public StubMapping findMatch(Message message) {
        for (StubMapping mapping : mappings) {
            try {
                if (mapping.matches(message)) {
                    log.debug("Stub match found for message: {}", mapping.getDescription());
                    return mapping;
                }
            } catch (Exception e) {
                log.warn("Error evaluating stub criteria: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Returns all registered stub mappings.
     */
    public List<StubMapping> getMappings() {
        return List.copyOf(mappings);
    }

    /**
     * Removes all registered stub mappings.
     */
    public void reset() {
        mappings.clear();
        log.info("Stub registry reset");
    }

    /**
     * Returns the number of registered stub mappings.
     */
    public int size() {
        return mappings.size();
    }

    // --- Stub Mapping ---

    /**
     * Represents a single stub mapping: a criteria predicate and a response generator.
     */
    public static class StubMapping {
        private final Predicate<Message> criteria;
        private final List<Function<Message, Message>> responseGenerators;
        private final Duration delay;
        private final String description;
        private int callCount;
        private int responseIndex;

        StubMapping(Predicate<Message> criteria, List<Function<Message, Message>> responseGenerators,
                    Duration delay, String description) {
            this.criteria = criteria;
            this.responseGenerators = responseGenerators;
            this.delay = delay;
            this.description = description;
            this.callCount = 0;
            this.responseIndex = 0;
        }

        public boolean matches(Message message) {
            return criteria.test(message);
        }

        /**
         * Generates a response for the given request.
         * Cycles through response generators for sequential response support.
         */
        public Message generateResponse(Message request) {
            Function<Message, Message> generator = responseGenerators.get(
                    Math.min(responseIndex, responseGenerators.size() - 1));
            if (responseIndex < responseGenerators.size() - 1) {
                responseIndex++;
            }
            callCount++;
            return generator.apply(request);
        }

        public Duration getDelay() {
            return delay;
        }

        public String getDescription() {
            return description;
        }

        public int getCallCount() {
            return callCount;
        }
    }

    // --- Builder ---

    public class StubMappingBuilder {
        private final Predicate<Message> criteria;
        private final List<Function<Message, Message>> responseGenerators = new java.util.ArrayList<>();
        private Duration delay = Duration.ZERO;
        private String description = "unnamed stub";

        StubMappingBuilder(Predicate<Message> criteria) {
            this.criteria = criteria;
        }

        /**
         * Sets the response generator function.
         */
        public StubMappingBuilder respondWith(Function<Message, Message> responseGenerator) {
            this.responseGenerators.add(responseGenerator);
            return this;
        }

        /**
         * Adds an additional response for sequential response support.
         * After all previous responses have been used, this one takes over.
         */
        public StubMappingBuilder thenRespondWith(Function<Message, Message> responseGenerator) {
            this.responseGenerators.add(responseGenerator);
            return this;
        }

        /**
         * Adds a delay before the stub sends its response.
         */
        public StubMappingBuilder withDelay(Duration delay) {
            this.delay = delay;
            return this;
        }

        /**
         * Sets a human-readable description for the stub mapping.
         */
        public StubMappingBuilder describedAs(String description) {
            this.description = description;
            return this;
        }

        /**
         * Registers the stub mapping in the registry.
         */
        public void register() {
            if (responseGenerators.isEmpty()) {
                throw new IllegalStateException("At least one response generator must be configured");
            }
            StubMapping mapping = new StubMapping(criteria, responseGenerators, delay, description);
            mappings.add(mapping);
            log.info("Registered stub mapping: {} (total: {})", description, mappings.size());
        }
    }
}
