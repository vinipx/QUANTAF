package io.github.vinipx.quantaf.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Structured result of a cross-source reconciliation.
 * Contains matches and mismatches with detailed field-level comparison.
 */
public class ReconciliationResult {

    private final String correlationKey;
    private final List<FieldComparison> comparisons;
    private boolean passed;

    public ReconciliationResult(String correlationKey) {
        this.correlationKey = correlationKey;
        this.comparisons = new ArrayList<>();
        this.passed = true;
    }

    public void addComparison(FieldComparison comparison) {
        this.comparisons.add(comparison);
        if (!comparison.isMatch()) {
            this.passed = false;
        }
    }

    public boolean isPassed() {
        return passed;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public List<FieldComparison> getComparisons() {
        return comparisons;
    }

    public List<FieldComparison> getMismatches() {
        return comparisons.stream().filter(c -> !c.isMatch()).toList();
    }

    /**
     * Returns a formatted summary of the reconciliation result.
     */
    public String toDetailedReport() {
        StringJoiner report = new StringJoiner("\n");
        report.add(String.format("=== Reconciliation: %s [%s] ===", correlationKey, passed ? "PASSED" : "FAILED"));
        report.add(String.format("%-20s | %-20s | %-20s | %-20s | %s", "Field", "FIX", "MQ", "API", "Status"));
        report.add("-".repeat(100));
        for (FieldComparison c : comparisons) {
            report.add(String.format("%-20s | %-20s | %-20s | %-20s | %s",
                    c.fieldName(),
                    c.fixValue() != null ? c.fixValue() : "N/A",
                    c.mqValue() != null ? c.mqValue() : "N/A",
                    c.apiValue() != null ? c.apiValue() : "N/A",
                    c.isMatch() ? "MATCH" : "MISMATCH"));
        }
        return report.toString();
    }

    @Override
    public String toString() {
        return String.format("ReconciliationResult{key='%s', passed=%s, comparisons=%d, mismatches=%d}",
                correlationKey, passed, comparisons.size(), getMismatches().size());
    }

    /**
     * Represents a single field comparison across three sources.
     */
    public record FieldComparison(
            String fieldName,
            String fixValue,
            String mqValue,
            String apiValue,
            boolean isMatch
    ) {
        public static FieldComparison match(String fieldName, String fixValue, String mqValue, String apiValue) {
            return new FieldComparison(fieldName, fixValue, mqValue, apiValue, true);
        }

        public static FieldComparison mismatch(String fieldName, String fixValue, String mqValue, String apiValue) {
            return new FieldComparison(fieldName, fixValue, mqValue, apiValue, false);
        }
    }
}
