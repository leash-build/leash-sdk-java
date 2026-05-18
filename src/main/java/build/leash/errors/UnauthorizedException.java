package build.leash.errors;

/**
 * The platform rejected the request as unauthenticated (HTTP 401).
 *
 * <p>Always carries code {@link LeashErrorCode#UNAUTHORIZED}.
 */
public class UnauthorizedException extends LeashException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message, String action, String seeAlso, Integer status) {
        super(LeashErrorCode.UNAUTHORIZED, message, action, seeAlso, status, null);
    }
}
