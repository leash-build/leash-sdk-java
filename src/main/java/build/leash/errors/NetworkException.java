package build.leash.errors;

/**
 * The SDK couldn't reach the Leash platform — DNS failure, refused
 * connection, TLS error, or interrupted I/O.
 *
 * <p>Always carries code {@link LeashErrorCode#NETWORK_ERROR}.
 */
public class NetworkException extends LeashException {

    private static final long serialVersionUID = 1L;

    public NetworkException(String message, Throwable cause) {
        super(LeashErrorCode.NETWORK_ERROR, message,
                "Check your network connection and that the Leash platform is reachable.",
                "https://leash.build/docs/sdk", null, cause);
    }
}
