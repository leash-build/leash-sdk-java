package build.leash.errors;

/**
 * The current plan does not include the requested feature (HTTP 402).
 *
 * <p>Always carries code {@link LeashErrorCode#UPGRADE_REQUIRED}. Catch this
 * to surface an upgrade CTA without parsing the message text.
 */
public class PlanBlockException extends LeashException {

    private static final long serialVersionUID = 1L;

    public PlanBlockException(String message, String action, String seeAlso, Integer status) {
        super(LeashErrorCode.UPGRADE_REQUIRED, message, action, seeAlso, status, null);
    }
}
