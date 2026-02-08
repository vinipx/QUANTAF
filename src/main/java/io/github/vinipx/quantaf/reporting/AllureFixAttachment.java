package io.github.vinipx.quantaf.reporting;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;

/**
 * Allure reporting utility for FIX message attachments.
 * Attaches FIX messages as formatted text to Allure reports.
 */
public class AllureFixAttachment {

    private static final Logger log = LoggerFactory.getLogger(AllureFixAttachment.class);

    /**
     * Attaches a FIX message to the current Allure step as formatted text.
     *
     * @param name    the attachment name (e.g., "NewOrderSingle", "ExecutionReport")
     * @param message the FIX message to attach
     */
    public static void attachFixMessage(String name, Message message) {
        try {
            String formatted = formatFixMessage(message);
            Allure.addAttachment(name, "text/plain", formatted);
            log.debug("Attached FIX message to Allure: {}", name);
        } catch (Exception e) {
            log.warn("Failed to attach FIX message to Allure: {}", e.getMessage());
        }
    }

    /**
     * Attaches a raw FIX string to the current Allure step.
     */
    public static void attachRawFix(String name, String rawFixMessage) {
        Allure.addAttachment(name, "text/plain", rawFixMessage);
    }

    /**
     * Formats a FIX message for human-readable display.
     * Replaces SOH delimiter with newlines and adds tag name labels.
     */
    static String formatFixMessage(Message message) {
        String raw = message.toString();
        // Replace SOH (ASCII 1) delimiter with pipe + newline for readability
        return raw.replace("\001", "|\n");
    }
}
