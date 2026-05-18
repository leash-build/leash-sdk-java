package build.leash.internal;

import java.util.Optional;

/**
 * Cookie + bearer-token extraction off a {@link RequestAdapter}.
 *
 * <p>Mirrors the multi-framework strategy in
 * {@code leash-sdk-ts/src/server/auth.ts}'s {@code extractToken} /
 * {@code leash-sdk-python/leash/auth.py}'s {@code extract_cookie} /
 * {@code leash-sdk-go/auth.go}'s {@code extractCookie}.
 */
public final class AuthExtractor {

    public static final String COOKIE_NAME = "leash-auth";

    private AuthExtractor() {}

    /** Read the {@code leash-auth} cookie or fall back to parsing the {@code Cookie} header. */
    public static Optional<String> extractLeashAuthCookie(RequestAdapter adapter) {
        if (adapter == null) return Optional.empty();
        Optional<String> direct = adapter.getCookie(COOKIE_NAME);
        if (direct.isPresent()) return direct;

        // Fallback: parse the raw Cookie header (servlet variants may not expose
        // a cookie API, plain-header adapters won't either).
        Optional<String> raw = adapter.getHeader("cookie");
        if (raw.isEmpty()) return Optional.empty();
        return parseCookieValue(raw.get(), COOKIE_NAME);
    }

    /** Read the JWT portion of {@code Authorization: Bearer …}, or empty. */
    public static Optional<String> extractBearerToken(RequestAdapter adapter) {
        if (adapter == null) return Optional.empty();
        Optional<String> raw = adapter.getHeader("authorization");
        if (raw.isEmpty()) return Optional.empty();
        String value = raw.get().trim();
        int sp = value.indexOf(' ');
        if (sp < 0) return Optional.empty();
        String scheme = value.substring(0, sp);
        if (!"bearer".equalsIgnoreCase(scheme)) return Optional.empty();
        String token = value.substring(sp + 1).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    /** Parse a raw {@code Cookie} header string and pluck out a single value. */
    public static Optional<String> parseCookieValue(String cookieHeader, String name) {
        if (cookieHeader == null || cookieHeader.isEmpty()) return Optional.empty();
        for (String part : cookieHeader.split(";")) {
            String trimmed = part.trim();
            int eq = trimmed.indexOf('=');
            if (eq <= 0) continue;
            String k = trimmed.substring(0, eq);
            if (k.equals(name)) {
                return Optional.of(trimmed.substring(eq + 1));
            }
        }
        return Optional.empty();
    }
}
