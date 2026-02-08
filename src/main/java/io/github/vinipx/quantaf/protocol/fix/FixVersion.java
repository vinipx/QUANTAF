package io.github.vinipx.quantaf.protocol.fix;

/**
 * Supported FIX protocol versions with their corresponding configuration
 * paths and data dictionary references.
 */
public enum FixVersion {

    FIX42("FIX.4.2", "quickfix-FIX42.cfg", "FIX42.xml"),
    FIX44("FIX.4.4", "quickfix-FIX44.cfg", "FIX44.xml"),
    FIX50("FIXT.1.1", "quickfix-FIX50.cfg", "FIX50.xml");

    private final String beginString;
    private final String configFile;
    private final String dataDictionary;

    FixVersion(String beginString, String configFile, String dataDictionary) {
        this.beginString = beginString;
        this.configFile = configFile;
        this.dataDictionary = dataDictionary;
    }

    public String getBeginString() {
        return beginString;
    }

    public String getConfigFile() {
        return configFile;
    }

    public String getDataDictionary() {
        return dataDictionary;
    }

    /**
     * Resolves a FixVersion from a configuration string (e.g., "FIX44", "FIX.4.4").
     */
    public static FixVersion fromString(String value) {
        if (value == null) {
            return FIX44;
        }
        String normalized = value.toUpperCase().replace(".", "").replace("FIX", "");
        return switch (normalized) {
            case "42" -> FIX42;
            case "44" -> FIX44;
            case "50", "T11" -> FIX50;
            default -> FIX44;
        };
    }
}
