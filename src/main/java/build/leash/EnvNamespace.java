package build.leash;

import build.leash.errors.LeashErrorCode;
import build.leash.errors.LeashException;
import build.leash.errors.NetworkException;
import build.leash.errors.PlanBlockException;
import build.leash.errors.UnauthorizedException;
import build.leash.internal.Json;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code leash.env()} — runtime env-var fetcher with a 60-second per-instance
 * TTL cache.
 *
 * <p>Mirrors the TS {@code leash.env} namespace and Python
 * {@code EnvNamespace}. Returns {@link Optional#empty()} for the
 * {@code KEY_NOT_DECLARED} case (HTTP 404) so callers can branch with a
 * clean {@code if (value.isEmpty())} — matches Python's {@code Optional[str]}
 * pattern. All other failures raise {@link LeashException}.
 */
public final class EnvNamespace {

    static final long CACHE_TTL_MS = 60_000L;

    private final String platformUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    EnvNamespace(String platformUrl, String apiKey, HttpClient httpClient) {
        this.platformUrl = platformUrl;
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    /** {@link #get(String, EnvOptions)} with default options. */
    public Optional<String> get(String key) {
        return get(key, EnvOptions.defaults());
    }

    /**
     * Resolve a single env-var value.
     *
     * @param key  env-var name
     * @param opts per-call options ({@link EnvOptions#fresh()} bypasses the cache)
     * @return the value, or empty when the platform reports it as not declared
     */
    public Optional<String> get(String key, EnvOptions opts) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(opts, "opts");

        long now = System.currentTimeMillis();
        if (!opts.isFresh()) {
            CacheEntry cached = cache.get(key);
            if (cached != null && cached.expiresAt > now) {
                return cached.value;
            }
        }
        Optional<String> resolved = fetch(key);
        cache.put(key, new CacheEntry(resolved, now + CACHE_TTL_MS));
        return resolved;
    }

    /**
     * Bulk variant — resolves multiple keys sequentially. Each key uses the
     * shared TTL cache. If any key fails, the whole call throws.
     */
    public Map<String, Optional<String>> getMany(List<String> keys) {
        Objects.requireNonNull(keys, "keys");
        Map<String, Optional<String>> out = new LinkedHashMap<>(keys.size());
        for (String k : keys) {
            out.put(k, get(k));
        }
        return Collections.unmodifiableMap(out);
    }

    private Optional<String> fetch(String key) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new LeashException(
                    LeashErrorCode.NO_API_KEY,
                    "LEASH_API_KEY is required to call leash.env().get().",
                    "Set LEASH_API_KEY in your environment or pass it explicitly via Leash.fromApiKey(...).",
                    "https://leash.build/dashboard/organization",
                    null,
                    null);
        }
        String encoded = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String url = platformUrl + "/api/apps/me/secrets/" + encoded;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.io.IOException e) {
            throw new NetworkException(e.getMessage() == null ? "Failed to reach the Leash platform." : e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Interrupted while calling the Leash platform.", e);
        }

        int status = response.statusCode();
        String body = response.body();
        JsonNode parsed = (body == null || body.isEmpty()) ? null : Json.readTree(body);

        switch (status) {
            case 200:
                if (parsed == null || !parsed.isObject()) {
                    throw envFetchError(key, status, "Platform returned an unparseable response.");
                }
                JsonNode value = parsed.get("value");
                if (value == null || !value.isTextual()) {
                    throw envFetchError(key, status, "Platform returned an unexpected response shape.");
                }
                return Optional.of(value.asText());

            case 400:
                throw new LeashException(
                        LeashErrorCode.INVALID_KEY,
                        "Invalid env-var key: '" + key + "'.",
                        "Env-var names must match /^[A-Za-z_][A-Za-z0-9_]*$/ and be no longer than 100 characters.",
                        "https://leash.build/docs/sdk",
                        status,
                        null);

            case 401:
                throw new UnauthorizedException(
                        "Missing or invalid LEASH_API_KEY.",
                        "Mint a fresh API key at /dashboard/organization.",
                        "https://leash.build/dashboard/organization",
                        status);

            case 402: {
                String suffix = "";
                if (parsed != null && parsed.isObject()) {
                    JsonNode plan = parsed.get("requiredPlan");
                    if (plan != null && plan.isTextual() && !plan.asText().isEmpty()) {
                        suffix = " (requiredPlan: " + plan.asText() + ")";
                    }
                }
                throw new PlanBlockException(
                        "leash.env().get requires the Growth plan or above" + suffix + ".",
                        "Upgrade at https://leash.build/dashboard/billing.",
                        "https://leash.build/dashboard/billing",
                        status);
            }

            case 404:
                // Adapted behaviour: return Optional.empty() so Java callers can
                // branch on missing keys naturally — matches Python.
                return Optional.empty();

            case 502: {
                String msg = "Secret source resync failed on the platform side.";
                if (parsed != null && parsed.isObject()) {
                    JsonNode err = parsed.get("error");
                    if (err != null && err.isTextual() && !err.asText().isEmpty()) {
                        msg = err.asText();
                    }
                }
                throw new LeashException(
                        LeashErrorCode.SOURCE_RESYNC_FAILED,
                        msg,
                        "Check your secret source configuration in the Leash dashboard.",
                        "https://leash.build/dashboard",
                        status,
                        null);
            }

            default:
                throw new LeashException(
                        LeashErrorCode.ENV_FETCH_ERROR,
                        "Unexpected response from platform: HTTP " + status,
                        "Check the Leash platform status and your configuration.",
                        "https://leash.build/docs/sdk",
                        status,
                        null);
        }
    }

    private static LeashException envFetchError(String key, int status, String message) {
        return new LeashException(
                LeashErrorCode.ENV_FETCH_ERROR,
                message + " (key=" + key + ")",
                "Check the Leash platform status and your configuration.",
                "https://leash.build/docs/sdk",
                status,
                null);
    }

    /** Cleared between tests via package-private hook. */
    void clearCache() {
        cache.clear();
    }

    private static final class CacheEntry {
        final Optional<String> value;
        final long expiresAt;

        CacheEntry(Optional<String> value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
