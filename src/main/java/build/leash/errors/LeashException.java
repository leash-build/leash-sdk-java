package build.leash.errors;

/**
 * Base unchecked exception for every Leash SDK call site.
 *
 * <p>Mirrors {@code LeashError} in
 * {@code leash-sdk-ts/src/errors.ts} and {@code LeashError} in
 * {@code leash-sdk-python/leash/errors.py}. Subclasses carry the same
 * stable {@code code} string ({@link LeashErrorCode}) so consumers can
 * branch identically across languages.
 *
 * <p>Java idiom: {@code RuntimeException} — checked exceptions in builder
 * chains are ergonomic poison, and the JVM ecosystem (Spring, Reactor,
 * functional pipelines) treats unchecked errors as the default.
 *
 * <p>The {@link #code} field is the load-bearing identifier — switch on it,
 * not on the exception subclass type alone. Helper subclasses
 * ({@link PlanBlockException}, {@link UnauthorizedException}, etc.) exist
 * for {@code catch}-by-type ergonomics; their code matches the parent base
 * code.
 */
public class LeashException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final String action;
    private final String seeAlso;
    private final Integer status;

    public LeashException(String code, String message) {
        this(code, message, null, null, null, null);
    }

    public LeashException(String code, String message, Throwable cause) {
        this(code, message, null, null, null, cause);
    }

    public LeashException(
            String code,
            String message,
            String action,
            String seeAlso,
            Integer status,
            Throwable cause) {
        super(buildMessage(message, action, seeAlso), cause);
        this.code = code;
        this.action = action;
        this.seeAlso = seeAlso;
        this.status = status;
    }

    /** Stable machine-readable identifier — see {@link LeashErrorCode}. */
    public String getCode() {
        return code;
    }

    /** Optional remediation hint shown after the message. */
    public String getAction() {
        return action;
    }

    /** Optional URL for further reading. */
    public String getSeeAlso() {
        return seeAlso;
    }

    /** HTTP status code when the error originated from the platform. */
    public Integer getStatus() {
        return status;
    }

    private static String buildMessage(String message, String action, String seeAlso) {
        StringBuilder sb = new StringBuilder();
        sb.append(message == null ? "" : message);
        if (action != null && !action.isEmpty()) {
            sb.append("\n  Fix: ").append(action);
        }
        if (seeAlso != null && !seeAlso.isEmpty()) {
            sb.append("\n  See: ").append(seeAlso);
        }
        return sb.toString();
    }
}
