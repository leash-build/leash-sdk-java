package build.leash.errors;

/**
 * Alias of {@link PlanBlockException} kept for surface parity with the TS
 * {@code UPGRADE_REQUIRED} naming.
 */
public class UpgradeRequiredException extends PlanBlockException {

    private static final long serialVersionUID = 1L;

    public UpgradeRequiredException(String message, String action, String seeAlso, Integer status) {
        super(message, action, seeAlso, status);
    }
}
