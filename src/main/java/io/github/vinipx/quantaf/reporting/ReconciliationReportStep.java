package io.github.vinipx.quantaf.reporting;

import io.github.vinipx.quantaf.core.model.ReconciliationResult;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Allure reporting utility for reconciliation results.
 * Attaches reconciliation details as custom Allure steps with tabular data.
 */
public class ReconciliationReportStep {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationReportStep.class);

    /**
     * Attaches a reconciliation result as an Allure step with detailed comparison data.
     */
    public static void report(ReconciliationResult result) {
        String stepName = String.format("Reconciliation: %s [%s]",
                result.getCorrelationKey(), result.isPassed() ? "PASSED" : "FAILED");

        Status status = result.isPassed() ? Status.PASSED : Status.FAILED;

        Allure.getLifecycle().startStep(UUID.randomUUID().toString(),
                new StepResult().setName(stepName).setStatus(status));

        // Attach the detailed report
        String report = result.toDetailedReport();
        Allure.addAttachment("Reconciliation Details", "text/plain", report);

        Allure.getLifecycle().stopStep();
        log.debug("Reconciliation report attached to Allure: {}", stepName);
    }

    /**
     * Attaches an MQ payload to the current Allure step.
     */
    public static void attachMqPayload(String name, String payload) {
        Allure.addAttachment(name, "application/json", payload);
        log.debug("MQ payload attached to Allure: {}", name);
    }
}
