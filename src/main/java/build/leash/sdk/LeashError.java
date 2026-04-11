package build.leash.sdk;

/**
 * Exception thrown when the Leash platform API returns an error response.
 */
public class LeashError extends Exception {

    private final String code;
    private final String connectUrl;

    public LeashError(String message, String code) {
        this(message, code, null);
    }

    public LeashError(String message, String code, String connectUrl) {
        super(formatMessage(message, code));
        this.code = code;
        this.connectUrl = connectUrl;
    }

    /**
     * Returns the machine-readable error code (e.g. "not_connected").
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the human-readable error message from the API.
     */
    public String getErrorMessage() {
        return super.getMessage();
    }

    /**
     * Returns the URL to initiate an OAuth connection flow, if provided by the API.
     * This is typically present when the error code is "not_connected".
     */
    public String getConnectUrl() {
        return connectUrl;
    }

    private static String formatMessage(String message, String code) {
        if (code != null && !code.isEmpty()) {
            return "leash: " + message + " (code: " + code + ")";
        }
        return "leash: " + message;
    }
}
