package io.github.vinipx.quantaf.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vinipx.quantaf.core.model.OrderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

/**
 * NLP-to-FIX translation agent. Takes natural language intent and produces
 * structured {@link OrderConfiguration} objects.
 * <p>
 * Supports two modes:
 * <ul>
 *   <li><b>LLM mode</b>: Uses a pluggable LLM provider for intelligent generation</li>
 *   <li><b>Template mode</b>: Uses predefined templates (for CI/deterministic tests)</li>
 * </ul>
 */
public class FixScenarioAgent {

    private static final Logger log = LoggerFactory.getLogger(FixScenarioAgent.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            You are a QA expert in FIX protocol. Translate natural language scenarios into structured JSON
            configurations for NewOrderSingle. Rules:
            - If user says 'Market On Close', set timeInForce to 'AT_CLOSE'.
            - If user says 'Limit', set orderType to 'LIMIT' and require a price.
            - If user says 'Market', set orderType to 'MARKET'.
            - If user says 'Rejection' or 'reject', set expectedExecType to 'REJECTED'.
            - If user says 'Buy', set side to 'BUY'. If 'Sell', set side to 'SELL'.
            - Default currency is 'USD' unless specified.
            - Default quantity is 100 unless specified.
            Return ONLY valid JSON matching this structure:
            {"symbol":"...","side":"BUY|SELL","orderType":"MARKET|LIMIT|STOP","price":null|number,
             "quantity":number,"timeInForce":"DAY|GTC|IOC|FOK|GTD|AT_CLOSE",
             "currency":"USD","expectedExecType":"NEW|FILL|REJECTED|null"}
            """;

    private final LlmProvider llmProvider;
    private final boolean fallbackToTemplates;

    /**
     * Creates a FixScenarioAgent with LLM support and optional template fallback.
     */
    public FixScenarioAgent(LlmProvider llmProvider, boolean fallbackToTemplates) {
        this.llmProvider = llmProvider;
        this.fallbackToTemplates = fallbackToTemplates;
    }

    /**
     * Creates a template-only FixScenarioAgent (no LLM dependency).
     */
    public FixScenarioAgent() {
        this.llmProvider = null;
        this.fallbackToTemplates = true;
    }

    /**
     * Generates an OrderConfiguration from a natural language intent.
     *
     * @param naturalLanguageIntent the scenario description (e.g., "Limit Buy 500 shares of AAPL at 150")
     * @return the structured OrderConfiguration
     */
    public OrderConfiguration generateOrderConfig(String naturalLanguageIntent) {
        log.info("Generating order config from intent: '{}'", naturalLanguageIntent);

        // Try LLM first
        if (llmProvider != null) {
            try {
                if (llmProvider.isAvailable()) {
                    return generateFromLlm(naturalLanguageIntent);
                }
                log.warn("LLM provider not available, falling back to templates");
            } catch (Exception e) {
                log.warn("LLM generation failed: {}", e.getMessage());
                if (!fallbackToTemplates) {
                    throw new RuntimeException("LLM generation failed and template fallback is disabled", e);
                }
            }
        }

        // Fallback to template-based generation
        if (fallbackToTemplates) {
            return generateFromTemplate(naturalLanguageIntent);
        }

        throw new IllegalStateException("No LLM provider configured and template fallback is disabled");
    }

    private OrderConfiguration generateFromLlm(String intent) {
        String response = llmProvider.complete(SYSTEM_PROMPT, intent);
        log.debug("LLM raw response: {}", response);

        // Extract JSON from response (handle markdown code blocks)
        String json = extractJson(response);

        try {
            OrderConfiguration config = objectMapper.readValue(json, OrderConfiguration.class);
            log.info("LLM generated config: {}", config);
            return config;
        } catch (Exception e) {
            log.error("Failed to parse LLM response as OrderConfiguration: {}", json, e);
            throw new RuntimeException("Invalid LLM response format", e);
        }
    }

    /**
     * Template-based generation using keyword matching.
     * Deterministic and fast -- suitable for CI pipelines.
     */
    OrderConfiguration generateFromTemplate(String intent) {
        log.info("Using template-based generation for: '{}'", intent);
        String lower = intent.toLowerCase();

        OrderConfiguration config = OrderConfiguration.create();

        // Determine side
        if (lower.contains("sell") || lower.contains("short")) {
            config.side(OrderConfiguration.Side.SELL);
        } else {
            config.side(OrderConfiguration.Side.BUY);
        }

        // Determine order type
        if (lower.contains("limit")) {
            config.orderType(OrderConfiguration.OrderType.LIMIT);
            config.price(extractPrice(lower));
        } else if (lower.contains("stop")) {
            config.orderType(OrderConfiguration.OrderType.STOP);
            config.price(extractPrice(lower));
        } else {
            config.orderType(OrderConfiguration.OrderType.MARKET);
        }

        // Determine time in force
        if (lower.contains("close") || lower.contains("moc")) {
            config.timeInForce(OrderConfiguration.TimeInForce.AT_CLOSE);
        } else if (lower.contains("gtc")) {
            config.timeInForce(OrderConfiguration.TimeInForce.GTC);
        } else if (lower.contains("ioc") || lower.contains("immediate")) {
            config.timeInForce(OrderConfiguration.TimeInForce.IOC);
        } else {
            config.timeInForce(OrderConfiguration.TimeInForce.DAY);
        }

        // Extract symbol (look for common ticker patterns)
        config.symbol(extractSymbol(lower));

        // Extract quantity
        config.quantity(extractQuantity(lower));

        // Determine expected exec type
        if (lower.contains("reject") || lower.contains("fat-finger") || lower.contains("fat finger")) {
            config.expectedExecType(OrderConfiguration.ExecType.REJECTED);
        } else if (lower.contains("fill")) {
            config.expectedExecType(OrderConfiguration.ExecType.FILL);
        }

        config.currency("USD");

        log.info("Template generated config: {}", config);
        return config;
    }

    private static final Map<String, String> KNOWN_SYMBOLS = Map.of(
            "aapl", "AAPL", "apple", "AAPL",
            "goog", "GOOG", "google", "GOOG",
            "msft", "MSFT", "microsoft", "MSFT",
            "tsla", "TSLA", "tesla", "TSLA",
            "amzn", "AMZN", "amazon", "AMZN"
    );

    private String extractSymbol(String lower) {
        for (Map.Entry<String, String> entry : KNOWN_SYMBOLS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        // Try to find an uppercase ticker pattern in the original text
        return "UNKNOWN";
    }

    private int extractQuantity(String lower) {
        // Look for patterns like "500 shares", "1000 units", or just numbers
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*(?:shares?|units?|lots?)?")
                .matcher(lower);
        while (m.find()) {
            int qty = Integer.parseInt(m.group(1));
            if (qty > 0 && qty < 10_000_000) { // Reasonable range
                return qty;
            }
        }
        return 100; // Default
    }

    private BigDecimal extractPrice(String lower) {
        // Look for patterns like "at 150", "@ 305.50", "price 150"
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:at|@|price)\\s+(\\d+\\.?\\d*)")
                .matcher(lower);
        if (m.find()) {
            return new BigDecimal(m.group(1));
        }
        return BigDecimal.valueOf(100.0); // Default
    }

    private String extractJson(String response) {
        // Strip markdown code blocks if present
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}
