package build.leash;

import build.leash.internal.AuthExtractor;
import build.leash.internal.RequestAdapter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Fallback adapter for non-Servlet frameworks (Spark, Javalin, plain HTTP,
 * Lambda events). Wraps a header map (and optional cookie map) so the
 * {@link Leash} constructor can build identity off any request shape.
 *
 * <p>Header lookups are case-insensitive.
 */
public final class HeaderMapRequestAdapter implements RequestAdapter {

    private final Map<String, String> headersLower;
    private final Map<String, String> cookies;

    public HeaderMapRequestAdapter(Map<String, String> headers) {
        this(headers, Collections.emptyMap());
    }

    public HeaderMapRequestAdapter(Map<String, String> headers, Map<String, String> cookies) {
        Objects.requireNonNull(headers, "headers");
        this.headersLower = new LinkedHashMap<>();
        headers.forEach((k, v) -> {
            if (k != null) headersLower.put(k.toLowerCase(java.util.Locale.ROOT), v);
        });
        this.cookies = cookies == null ? Collections.emptyMap() : new LinkedHashMap<>(cookies);
    }

    @Override
    public Optional<String> getCookie(String name) {
        if (cookies.containsKey(name)) {
            return Optional.ofNullable(cookies.get(name));
        }
        // Fall back to parsing the Cookie header.
        Optional<String> raw = getHeader("cookie");
        if (raw.isEmpty()) return Optional.empty();
        return AuthExtractor.parseCookieValue(raw.get(), name);
    }

    @Override
    public Optional<String> getHeader(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(headersLower.get(name.toLowerCase(java.util.Locale.ROOT)));
    }
}
