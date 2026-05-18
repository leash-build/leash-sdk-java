package build.leash.internal;

import build.leash.errors.ConnectionRequiredException;
import build.leash.errors.LeashErrorCode;
import build.leash.errors.LeashException;
import build.leash.errors.NetworkException;
import build.leash.errors.PlanBlockException;
import build.leash.errors.UnauthorizedException;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Shared HTTP carrier for the integrations namespace.
 *
 * <p>Mirrors {@code _Transport} in
 * {@code leash-sdk-python/leash/integrations/base.py} and the {@code _call}
 * / {@code _post} helpers on the TS {@code Leash} class. The auth header
 * contract is load-bearing:
 *
 * <ul>
 *   <li>{@code X-API-Key} carries the app key ({@code LEASH_API_KEY}).</li>
 *   <li>{@code Cookie: leash-auth=…} forwards the browser session.</li>
 *   <li>{@code Authorization: Bearer} is <strong>never</strong> forwarded on
 *       integration POSTs — the platform's {@code verifyToken} can reject a
 *       JWT before the API-key check runs, so the inbound bearer is only
 *       used for env-fetch fallback.</li>
 * </ul>
 */
public final class Transport {

    private final String platformUrl;
    private final String apiKey;
    private final String cookieValue;
    private final HttpClient httpClient;

    public Transport(String platformUrl, String apiKey, String cookieValue, HttpClient httpClient) {
        this.platformUrl = stripTrailingSlash(platformUrl);
        this.apiKey = apiKey;
        this.cookieValue = cookieValue;
        this.httpClient = httpClient != null ? httpClient : buildDefaultClient();
    }

    private static HttpClient buildDefaultClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private static String stripTrailingSlash(String url) {
        if (url == null) return null;
        int end = url.length();
        while (end > 0 && url.charAt(end - 1) == '/') end--;
        return url.substring(0, end);
    }

    public String getPlatformUrl() {
        return platformUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    /**
     * POST to {@code /api/integrations/{provider}/{action}} and return the
     * unwrapped response data (or the raw envelope when there's no
     * {@code data} field).
     */
    public JsonNode integrationsCall(String provider, String action, Object body) {
        String url = platformUrl + "/api/integrations/" + provider + "/" + action;
        String docsUrl = "https://leash.build/docs/integrations/" + provider;
        return post(url, body, docsUrl);
    }

    private JsonNode post(String url, Object body, String docsUrl) {
        String json = body == null ? "{}" : Json.writeValueAsString(body);

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (apiKey != null && !apiKey.isEmpty()) {
            reqBuilder.header("X-API-Key", apiKey);
        }
        if (cookieValue != null && !cookieValue.isEmpty()) {
            reqBuilder.header("Cookie", AuthExtractor.COOKIE_NAME + "=" + cookieValue);
        }
        // Critical: no Authorization: Bearer header here. See class javadoc.

        HttpResponse<String> response;
        try {
            response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (java.io.IOException e) {
            throw new NetworkException(e.getMessage() == null ? "Failed to reach the Leash platform." : e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Interrupted while calling the Leash platform.", e);
        }

        int status = response.statusCode();
        String rawBody = response.body();
        JsonNode parsed = rawBody == null || rawBody.isEmpty() ? null : Json.readTree(rawBody);

        if (status >= 400) {
            throw mapIntegrationStatus(status, parsed, docsUrl);
        }

        // Success envelope: { success, data } | { data } | raw shape.
        if (parsed == null) return null;
        if (parsed.isObject()) {
            JsonNode successNode = parsed.get("success");
            if (successNode != null && successNode.isBoolean() && !successNode.asBoolean()) {
                String errorMsg = textOr(parsed, "error", "Integration error");
                String code = textOr(parsed, "code", LeashErrorCode.INTEGRATION_ERROR);
                throw new LeashException(code, errorMsg,
                        "Check your integration configuration and try again.",
                        docsUrl, status, null);
            }
            JsonNode data = parsed.get("data");
            if (data != null) return data;
        }
        return parsed;
    }

    private static LeashException mapIntegrationStatus(int status, JsonNode parsed, String docsUrl) {
        String message = "HTTP " + status;
        if (parsed != null && parsed.isObject()) {
            JsonNode err = parsed.get("error");
            if (err != null && err.isTextual()) {
                message = err.asText();
            }
        }
        switch (status) {
            case 401:
                return new UnauthorizedException(
                        message,
                        "Ensure the leash-auth cookie is present, or open your app from the Leash dashboard to get a valid session.",
                        "https://leash.build/docs/sdk",
                        status);
            case 402: {
                String msg = "This feature requires a higher plan.";
                if (parsed != null && parsed.isObject()) {
                    JsonNode m = parsed.get("message");
                    if (m != null && m.isTextual() && !m.asText().isEmpty()) {
                        msg = m.asText();
                    }
                }
                return new PlanBlockException(
                        msg,
                        "Upgrade your plan at https://leash.build/dashboard/billing.",
                        "https://leash.build/pricing",
                        status);
            }
            case 403:
                return new ConnectionRequiredException(
                        message,
                        "Connect the integration at /dashboard/integrations and make sure this app is on the allow-list.",
                        "https://leash.build/dashboard/integrations",
                        status);
            default:
                return new LeashException(
                        LeashErrorCode.INTEGRATION_ERROR,
                        message,
                        "Check your integration configuration and try again — the upstream provider returned an error.",
                        docsUrl,
                        status,
                        null);
        }
    }

    private static String textOr(JsonNode node, String field, String fallback) {
        JsonNode v = node.get(field);
        if (v != null && v.isTextual()) {
            String s = v.asText();
            if (!s.isEmpty()) return s;
        }
        return fallback;
    }
}
