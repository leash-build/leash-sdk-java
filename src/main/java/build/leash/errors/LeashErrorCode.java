package build.leash.errors;

/**
 * Stable, machine-readable identifiers for {@link LeashException}.
 *
 * <p>Strings match {@code leash-sdk-ts/src/errors.ts} and
 * {@code leash-sdk-python/leash/errors.py} exactly so consumers can switch
 * on the same codes regardless of language.
 */
public final class LeashErrorCode {

    private LeashErrorCode() {}

    public static final String NO_API_KEY = "NO_API_KEY";
    public static final String NO_REQUEST_SERVER_CONSTRUCT = "NO_REQUEST_SERVER_CONSTRUCT";
    public static final String BROWSER_MODE_UNSUPPORTED = "BROWSER_MODE_UNSUPPORTED";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String NO_AUTH_CONTEXT = "NO_AUTH_CONTEXT";
    public static final String INTEGRATION_NOT_ENABLED = "INTEGRATION_NOT_ENABLED";
    public static final String INTEGRATION_ERROR = "INTEGRATION_ERROR";
    public static final String UPGRADE_REQUIRED = "UPGRADE_REQUIRED";
    public static final String NETWORK_ERROR = "NETWORK_ERROR";
    public static final String KEY_NOT_DECLARED = "KEY_NOT_DECLARED";
    public static final String INVALID_KEY = "INVALID_KEY";
    public static final String SOURCE_RESYNC_FAILED = "SOURCE_RESYNC_FAILED";
    public static final String ENV_FETCH_ERROR = "ENV_FETCH_ERROR";
}
