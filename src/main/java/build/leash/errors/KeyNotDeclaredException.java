package build.leash.errors;

/**
 * The requested env-var key is not declared (HTTP 404 from
 * {@code GET /api/apps/me/secrets/{key}}).
 *
 * <p>{@code leash.env().get(key)} normally returns {@link java.util.Optional#empty()}
 * for this case (matching the Python {@code Optional[str]} pattern). This
 * exception exists for callers that reach the lower-level surface and want
 * to assert on the code.
 */
public class KeyNotDeclaredException extends LeashException {

    private static final long serialVersionUID = 1L;

    public KeyNotDeclaredException(String message, String action, String seeAlso, Integer status) {
        super(LeashErrorCode.KEY_NOT_DECLARED, message, action, seeAlso, status, null);
    }
}
