package io.github.vinipx.quantaf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves environment-specific settings and determines runtime context.
 */
public class EnvironmentResolver {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentResolver.class);

    public enum Environment {
        LOCAL, CI, STAGING
    }

    private final Environment current;

    public EnvironmentResolver(String environmentName) {
        this.current = resolveEnvironment(environmentName);
        log.info("Resolved environment: {}", current);
    }

    public Environment getCurrent() {
        return current;
    }

    public boolean isLocal() {
        return current == Environment.LOCAL;
    }

    public boolean isCi() {
        return current == Environment.CI;
    }

    public boolean isStaging() {
        return current == Environment.STAGING;
    }

    /**
     * Returns true if Testcontainers should be used (CI environment).
     */
    public boolean useTestcontainers() {
        return current == Environment.CI;
    }

    /**
     * Returns true if Docker Compose services are expected (local environment).
     */
    public boolean useDockerCompose() {
        return current == Environment.LOCAL;
    }

    /**
     * Returns true if AI calls should use templates instead of live LLM.
     */
    public boolean useAiTemplates() {
        return current == Environment.CI;
    }

    private static Environment resolveEnvironment(String name) {
        if (name == null || name.isBlank()) {
            // Auto-detect from CI environment variables
            if (System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null) {
                return Environment.CI;
            }
            return Environment.LOCAL;
        }
        try {
            return Environment.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown environment '{}', defaulting to LOCAL", name);
            return Environment.LOCAL;
        }
    }
}
