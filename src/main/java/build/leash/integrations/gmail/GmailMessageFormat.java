package build.leash.integrations.gmail;

/**
 * Detail level for {@code Gmail.getMessage(...)}.
 *
 * <p>Mirrors the Gmail REST API enum; the platform forwards the wire value
 * verbatim.
 */
public enum GmailMessageFormat {
    FULL("full"),
    METADATA("metadata"),
    MINIMAL("minimal"),
    RAW("raw");

    private final String wire;

    GmailMessageFormat(String wire) {
        this.wire = wire;
    }

    public String wireValue() {
        return wire;
    }
}
