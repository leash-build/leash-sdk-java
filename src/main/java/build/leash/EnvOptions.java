package build.leash;

/**
 * Per-call options for {@code leash.env().get(key, opts)}.
 *
 * <p>Mirrors the TS {@code EnvGetOptions} interface and Python
 * {@code fresh=True} kwarg.
 */
public final class EnvOptions {

    private static final EnvOptions DEFAULT = new EnvOptions(false);
    private static final EnvOptions FRESH = new EnvOptions(true);

    private final boolean fresh;

    private EnvOptions(boolean fresh) {
        this.fresh = fresh;
    }

    /** Default options — read from the TTL cache when fresh enough. */
    public static EnvOptions defaults() {
        return DEFAULT;
    }

    /**
     * Skip the TTL cache for this call. The freshly-fetched value is still
     * written back to the cache for subsequent reads.
     */
    public static EnvOptions fresh() {
        return FRESH;
    }

    public boolean isFresh() {
        return fresh;
    }
}
