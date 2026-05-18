package build.leash.errors;

/**
 * The integration is not connected for this user (HTTP 403).
 *
 * <p>Always carries code {@link LeashErrorCode#INTEGRATION_NOT_ENABLED}.
 */
public class ConnectionRequiredException extends LeashException {

    private static final long serialVersionUID = 1L;

    public ConnectionRequiredException(String message, String action, String seeAlso, Integer status) {
        super(LeashErrorCode.INTEGRATION_NOT_ENABLED, message, action, seeAlso, status, null);
    }
}
