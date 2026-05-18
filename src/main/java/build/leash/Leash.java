package build.leash;

import build.leash.integrations.IntegrationsNamespace;
import build.leash.internal.AuthExtractor;
import build.leash.internal.RequestAdapter;
import build.leash.internal.Transport;
import jakarta.servlet.http.HttpServletRequest;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * Unified Leash SDK entry point — auth, env, and integrations in one client.
 *
 * <p>Mirrors {@code Leash} in {@code leash-sdk-ts/src/leash.ts},
 * {@code leash-sdk-python/leash/client.py}, and
 * {@code leash-sdk-go/.../client.go}. Method chaining keeps the namespaces
 * obvious:
 *
 * <pre>{@code
 * Leash leash = new Leash(httpServletRequest);
 * Optional<LeashUser> user = leash.auth().user();
 * Optional<String> key = leash.env().get("OPENAI_API_KEY");
 * GmailMessageList msgs = leash.integrations().gmail().listMessages();
 * }</pre>
 *
 * <h2>Auth precedence (matches TS / Python / Go exactly)</h2>
 * <ol>
 *   <li>{@code LEASH_API_KEY} env var (or {@link #fromApiKey(String)}) — sent
 *       as {@code X-API-Key} on integration POSTs and as
 *       {@code Authorization: Bearer} on env-fetch.</li>
 *   <li>{@code Authorization: Bearer <jwt>} header on the inbound request —
 *       used <em>only</em> as the env-fetch bearer when no API key is
 *       configured. It is <strong>never</strong> forwarded on integration
 *       POSTs (the platform's {@code verifyToken} can reject a user JWT
 *       before the API-key check runs).</li>
 *   <li>{@code leash-auth} cookie — forwarded to the platform on integration
 *       calls as {@code Cookie: leash-auth=<value>}.</li>
 * </ol>
 *
 * <h2>Bearer → env fallback</h2>
 * <p>When no {@code LEASH_API_KEY} is configured but the inbound request
 * carries {@code Authorization: Bearer <jwt>}, the SDK uses that bearer to
 * authorise {@code GET /api/apps/me/secrets/*} calls so a JWT-only caller
 * (e.g. a CLI / agent script that holds a user token but no platform key)
 * can still read env vars. It is still never sent on integration POSTs.
 */
public final class Leash {

    private static final String DEFAULT_PLATFORM_URL = "https://leash.build";

    private final Transport transport;
    private final AuthNamespace auth;
    private final EnvNamespace env;
    private final IntegrationsNamespace integrations;

    /**
     * Construct from a Jakarta {@link HttpServletRequest}.
     *
     * <p>Resolves the {@code leash-auth} cookie and the
     * {@code Authorization: Bearer} header off the request, picks up
     * {@code LEASH_API_KEY} from the environment, and bootstraps every
     * namespace.
     *
     * <p>Requires {@code jakarta.servlet:jakarta.servlet-api} on the
     * classpath (Spring Boot, Jakarta EE, Jetty, Tomcat, Quarkus all ship
     * it). Non-Servlet apps should use
     * {@link #Leash(Map, Map)} instead.
     */
    public Leash(HttpServletRequest request) {
        this(new ServletRequestAdapter(request), null, null);
    }

    /**
     * Construct from a plain header map (and optional cookie map). Suits
     * Javalin, Spark, plain HTTP servers, AWS Lambda events, or anywhere a
     * Servlet API isn't on the classpath. Header lookups are
     * case-insensitive.
     *
     * @param headers map of request headers (may contain the {@code Cookie} header)
     * @param cookies map of cookie name → value (may be {@code null} or empty)
     */
    public Leash(Map<String, String> headers, Map<String, String> cookies) {
        this(new HeaderMapRequestAdapter(
                headers == null ? Collections.emptyMap() : headers,
                cookies == null ? Collections.emptyMap() : cookies), null, null);
    }

    /** Construct from a custom {@link RequestAdapter}. Useful in tests. */
    public Leash(RequestAdapter adapter) {
        this(adapter, null, null);
    }

    private Leash(RequestAdapter adapter, String explicitApiKey, String explicitToken) {
        String platformUrl = resolvePlatformUrl(null);
        HttpClient httpClient = buildDefaultClient();

        String apiKey = explicitApiKey != null ? explicitApiKey : System.getenv("LEASH_API_KEY");
        String cookieValue = adapter == null
                ? null
                : AuthExtractor.extractLeashAuthCookie(adapter).orElse(null);

        // Apply explicit token if supplied (Leash.fromToken). Otherwise pull off the request.
        String bearerToken = explicitToken;
        if (bearerToken == null && adapter != null) {
            bearerToken = AuthExtractor.extractBearerToken(adapter).orElse(null);
        }

        this.transport = new Transport(platformUrl, apiKey, cookieValue, httpClient);

        // Env namespace authorises with the API key when present, else falls back to the bearer.
        // Critical: this fallback is for env-fetch ONLY. Integration POSTs still NEVER send Bearer.
        String envKey = (apiKey != null && !apiKey.isEmpty()) ? apiKey : bearerToken;
        this.env = new EnvNamespace(platformUrl, envKey, httpClient);

        // Auth namespace decodes either the request's leash-auth cookie OR (for
        // Leash.fromToken) an explicit JWT passed in directly.
        String authCookie = cookieValue;
        if (authCookie == null && explicitToken != null) {
            authCookie = explicitToken;
        }
        this.auth = new AuthNamespace(authCookie);

        this.integrations = new IntegrationsNamespace(transport);
    }

    /**
     * Server-to-server constructor — uses the supplied API key for both
     * env-fetch ({@code Authorization: Bearer}) and integration POSTs
     * ({@code X-API-Key}). No inbound request, so no {@code leash-auth}
     * cookie is forwarded — integration calls that require user context
     * will fail with {@link build.leash.errors.UnauthorizedException}.
     */
    public static Leash fromApiKey(String apiKey) {
        return new Leash(null, apiKey, null);
    }

    /**
     * Construct from an explicit JWT. The token is forwarded as the
     * {@code leash-auth} cookie value on integration calls and used to
     * decode {@code leash.auth().user()}. Suits CLI / agent flows that
     * hold a user JWT directly without an HTTP request object.
     */
    public static Leash fromToken(String token) {
        return new Leash(null, null, token);
    }

    public AuthNamespace auth() {
        return auth;
    }

    public EnvNamespace env() {
        return env;
    }

    public IntegrationsNamespace integrations() {
        return integrations;
    }

    /** Exposes the resolved platform base URL (no trailing slash). */
    public String platformUrl() {
        return transport.getPlatformUrl();
    }

    /** Internal access for tests — package-private. */
    Transport transport() {
        return transport;
    }

    /* ----------------------------------------------------------- helpers */

    private static String resolvePlatformUrl(String explicit) {
        if (explicit != null && !explicit.isEmpty()) return explicit;
        String env = System.getenv("LEASH_PLATFORM_URL");
        if (env != null && !env.isEmpty()) return env;
        return DEFAULT_PLATFORM_URL;
    }

    private static HttpClient buildDefaultClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Optional access pathway used by tests and advanced callers to inject a
     * custom platform URL / HTTP client. Mirrors the {@code WithPlatformURL}
     * + {@code WithHTTPClient} option chain in the Go SDK.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Mutable builder for advanced construction. */
    public static final class Builder {
        private RequestAdapter adapter;
        private String apiKey;
        private String token;
        private String platformUrl;
        private HttpClient httpClient;

        public Builder request(HttpServletRequest request) {
            this.adapter = new ServletRequestAdapter(request);
            return this;
        }

        public Builder request(Map<String, String> headers, Map<String, String> cookies) {
            this.adapter = new HeaderMapRequestAdapter(
                    headers == null ? Collections.emptyMap() : headers,
                    cookies == null ? Collections.emptyMap() : cookies);
            return this;
        }

        public Builder requestAdapter(RequestAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder platformUrl(String platformUrl) {
            this.platformUrl = platformUrl;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Leash build() {
            String resolvedUrl = Leash.resolvePlatformUrl(platformUrl);
            HttpClient resolvedHttp = httpClient != null ? httpClient : Leash.buildDefaultClient();
            String resolvedApiKey = apiKey != null ? apiKey : System.getenv("LEASH_API_KEY");
            String cookieValue = adapter == null
                    ? null
                    : AuthExtractor.extractLeashAuthCookie(adapter).orElse(null);

            String bearer = token;
            if (bearer == null && adapter != null) {
                bearer = AuthExtractor.extractBearerToken(adapter).orElse(null);
            }

            Transport transport = new Transport(resolvedUrl, resolvedApiKey, cookieValue, resolvedHttp);
            String envKey = (resolvedApiKey != null && !resolvedApiKey.isEmpty()) ? resolvedApiKey : bearer;
            EnvNamespace env = new EnvNamespace(resolvedUrl, envKey, resolvedHttp);

            String authCookie = cookieValue != null ? cookieValue : token;
            AuthNamespace auth = new AuthNamespace(authCookie);

            IntegrationsNamespace integrations = new IntegrationsNamespace(transport);

            return new Leash(transport, auth, env, integrations);
        }
    }

    /** Internal full-arg constructor used by {@link Builder#build()}. */
    private Leash(Transport transport,
                  AuthNamespace auth,
                  EnvNamespace env,
                  IntegrationsNamespace integrations) {
        this.transport = transport;
        this.auth = auth;
        this.env = env;
        this.integrations = integrations;
    }

}
