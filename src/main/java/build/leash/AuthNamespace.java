package build.leash;

import build.leash.internal.JwtDecoder;

import java.util.Optional;

/**
 * {@code leash.auth()} — identity helpers scoped to the request the
 * parent {@link Leash} was constructed from.
 *
 * <p>Mirrors the TS {@code leash.auth} surface and Python
 * {@code leash.auth.user()}. Decode failures are swallowed — callers branch
 * cleanly with:
 *
 * <pre>{@code
 * Optional<LeashUser> user = leash.auth().user();
 * if (user.isEmpty()) {
 *     // unauthenticated request
 * }
 * }</pre>
 */
public final class AuthNamespace {

    private final String cookieValue;

    AuthNamespace(String cookieValue) {
        this.cookieValue = cookieValue;
    }

    /**
     * Return the authenticated user, or {@link Optional#empty()} when no
     * valid {@code leash-auth} cookie was present on the request.
     */
    public Optional<LeashUser> user() {
        if (cookieValue == null || cookieValue.isEmpty()) return Optional.empty();
        try {
            return Optional.of(JwtDecoder.decodeToUser(cookieValue));
        } catch (Exception ignore) {
            return Optional.empty();
        }
    }

    /** True when {@link #user()} would return a non-empty {@link LeashUser}. */
    public boolean isAuthenticated() {
        return user().isPresent();
    }
}
