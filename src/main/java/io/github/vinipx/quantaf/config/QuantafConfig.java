package io.github.vinipx.quantaf.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Centralized configuration for the QUANTAF framework.
 * Loads settings from quantaf.yml with environment variable substitution.
 */
public class QuantafConfig {

    private static final Logger log = LoggerFactory.getLogger(QuantafConfig.class);
    private static final String DEFAULT_CONFIG_FILE = "quantaf.yml";
    private static volatile QuantafConfig instance;

    private final Map<String, Object> root;
    private final FixConfig fix;
    private final MessagingConfig messaging;
    private final RestConfig rest;
    private final AiConfig ai;
    private final LedgerConfig ledger;
    private final String environment;

    @SuppressWarnings("unchecked")
    private QuantafConfig(String configFile) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (is == null) {
                throw new IllegalStateException("Configuration file not found: " + configFile);
            }
            Map<String, Object> rawConfig = mapper.readValue(is, Map.class);
            this.root = (Map<String, Object>) rawConfig.getOrDefault("quantaf", rawConfig);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration from " + configFile, e);
        }

        this.environment = resolveEnvVar(getString("environment", "local"));
        this.fix = new FixConfig(getMap("fix"));
        this.messaging = new MessagingConfig(getMap("messaging"));
        this.rest = new RestConfig(getMap("rest"));
        this.ai = new AiConfig(getMap("ai"));
        this.ledger = new LedgerConfig(getMap("ledger"));

        log.info("QUANTAF configuration loaded [environment={}]", environment);
    }

    public static QuantafConfig getInstance() {
        if (instance == null) {
            synchronized (QuantafConfig.class) {
                if (instance == null) {
                    instance = new QuantafConfig(DEFAULT_CONFIG_FILE);
                }
            }
        }
        return instance;
    }

    public static QuantafConfig load(String configFile) {
        instance = new QuantafConfig(configFile);
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public String getEnvironment() {
        return environment;
    }

    public FixConfig fix() {
        return fix;
    }

    public MessagingConfig messaging() {
        return messaging;
    }

    public RestConfig rest() {
        return rest;
    }

    public AiConfig ai() {
        return ai;
    }

    public LedgerConfig ledger() {
        return ledger;
    }

    // --- Nested Config Classes ---

    public static class FixConfig {
        private final Map<String, Object> data;

        @SuppressWarnings("unchecked")
        FixConfig(Map<String, Object> data) {
            this.data = data != null ? data : Map.of();
        }

        public String getDefaultVersion() {
            return getStringFrom(data, "defaultVersion", "FIX44");
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> getSession(String name) {
            Map<String, Object> sessions = (Map<String, Object>) data.getOrDefault("sessions", Map.of());
            return (Map<String, Object>) sessions.getOrDefault(name, Map.of());
        }

        public String getSenderCompId(String sessionName) {
            return getStringFrom(getSession(sessionName), "senderCompId", "");
        }

        public String getTargetCompId(String sessionName) {
            return getStringFrom(getSession(sessionName), "targetCompId", "");
        }

        public String getHost(String sessionName) {
            return getStringFrom(getSession(sessionName), "host", "localhost");
        }

        public int getPort(String sessionName) {
            Object val = getSession(sessionName).getOrDefault("port", 9876);
            return val instanceof Number ? ((Number) val).intValue() : Integer.parseInt(val.toString());
        }
    }

    public static class MessagingConfig {
        private final Map<String, Object> data;

        MessagingConfig(Map<String, Object> data) {
            this.data = data != null ? data : Map.of();
        }

        public String getProvider() {
            return getStringFrom(data, "provider", "activemq");
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> getProviderConfig() {
            return (Map<String, Object>) data.getOrDefault(getProvider(), Map.of());
        }

        public String getBrokerUrl() {
            return resolveEnvVar(getStringFrom(getProviderConfig(), "brokerUrl", "tcp://localhost:61616"));
        }
    }

    public static class RestConfig {
        private final Map<String, Object> data;

        RestConfig(Map<String, Object> data) {
            this.data = data != null ? data : Map.of();
        }

        public String getBaseUrl() {
            return resolveEnvVar(getStringFrom(data, "baseUrl", "http://localhost:8080/api"));
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> getOAuth2Config() {
            return (Map<String, Object>) data.getOrDefault("oauth2", Map.of());
        }

        public String getTokenUrl() {
            return resolveEnvVar(getStringFrom(getOAuth2Config(), "tokenUrl", ""));
        }

        public String getClientId() {
            return resolveEnvVar(getStringFrom(getOAuth2Config(), "clientId", ""));
        }

        public String getClientSecret() {
            return resolveEnvVar(getStringFrom(getOAuth2Config(), "clientSecret", ""));
        }
    }

    public static class AiConfig {
        private final Map<String, Object> data;

        AiConfig(Map<String, Object> data) {
            this.data = data != null ? data : Map.of();
        }

        public String getProvider() {
            return getStringFrom(data, "provider", "ollama");
        }

        public String getModel() {
            return getStringFrom(data, "model", "llama3");
        }

        public String getBaseUrl() {
            return resolveEnvVar(getStringFrom(data, "baseUrl", "http://localhost:11434"));
        }

        public String getApiKey() {
            return resolveEnvVar(getStringFrom(data, "apiKey", ""));
        }

        public boolean isFallbackToTemplates() {
            Object val = data.getOrDefault("fallbackToTemplates", true);
            return val instanceof Boolean ? (Boolean) val : Boolean.parseBoolean(val.toString());
        }

        public boolean isCacheResponses() {
            Object val = data.getOrDefault("cacheResponses", true);
            return val instanceof Boolean ? (Boolean) val : Boolean.parseBoolean(val.toString());
        }
    }

    public static class LedgerConfig {
        private final Map<String, Object> data;

        LedgerConfig(Map<String, Object> data) {
            this.data = data != null ? data : Map.of();
        }

        public int getAmountPrecision() {
            Object val = data.getOrDefault("amountPrecision", 8);
            return val instanceof Number ? ((Number) val).intValue() : Integer.parseInt(val.toString());
        }

        public double getDefaultTolerance() {
            Object val = data.getOrDefault("defaultTolerance", 0.0001);
            return val instanceof Number ? ((Number) val).doubleValue() : Double.parseDouble(val.toString());
        }

        public String getTimezone() {
            return getStringFrom(data, "timezone", "UTC");
        }
    }

    // --- Utility Methods ---

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(String key) {
        return (Map<String, Object>) root.getOrDefault(key, Map.of());
    }

    private String getString(String key, String defaultValue) {
        Object val = root.getOrDefault(key, defaultValue);
        return val != null ? val.toString() : defaultValue;
    }

    private static String getStringFrom(Map<String, Object> map, String key, String defaultValue) {
        Object val = map.getOrDefault(key, defaultValue);
        return val != null ? val.toString() : defaultValue;
    }

    /**
     * Resolves environment variable placeholders in the form ${VAR_NAME}.
     */
    static String resolveEnvVar(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }
        String result = value;
        while (result.contains("${")) {
            int start = result.indexOf("${");
            int end = result.indexOf("}", start);
            if (end == -1) break;
            String varName = result.substring(start + 2, end);
            String envValue = System.getenv(varName);
            if (envValue == null) {
                envValue = System.getProperty(varName, "");
            }
            result = result.substring(0, start) + envValue + result.substring(end + 1);
        }
        return result;
    }
}
