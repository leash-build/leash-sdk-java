package build.leash.sdk;

import build.leash.sdk.types.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Untyped client for a custom integration.
 *
 * <p>Obtained via {@link LeashIntegrations#integration(String)}. Proxies requests
 * through the Leash platform at {@code /api/integrations/custom/{name}}.
 *
 * <p>Example:
 * <pre>{@code
 * CustomIntegration stripe = leash.integration("stripe");
 * JsonElement charges = stripe.call("/v1/charges", "GET", null);
 * }</pre>
 */
public class CustomIntegration {

    private final String name;
    private final String platformUrl;
    private final String authToken;
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;

    CustomIntegration(String name, String platformUrl, String authToken,
                      String apiKey, HttpClient httpClient, Gson gson) {
        this.name = name;
        this.platformUrl = platformUrl;
        this.authToken = authToken;
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.gson = gson;
    }

    /**
     * Invoke the custom integration proxy.
     *
     * @param path   the endpoint path to forward (e.g. "/users")
     * @param method HTTP method (e.g. "GET", "POST")
     * @param body   optional request body, or null
     * @return the JSON data from the platform response
     * @throws LeashError if the API returns an error
     */
    public JsonElement call(String path, String method, Object body) throws LeashError {
        return callWithHeaders(path, method, body, null);
    }

    /**
     * Like {@link #call(String, String, Object)} but also forwards custom headers.
     *
     * @param path    the endpoint path to forward
     * @param method  HTTP method
     * @param body    optional request body, or null
     * @param headers optional extra headers to forward, or null
     * @return the JSON data from the platform response
     * @throws LeashError if the API returns an error
     */
    public JsonElement callWithHeaders(String path, String method, Object body,
                                       Map<String, String> headers) throws LeashError {
        String endpoint = platformUrl + "/api/integrations/custom/" + name;

        JsonObject payload = new JsonObject();
        payload.addProperty("path", path);
        payload.addProperty("method", method);
        if (body != null) {
            payload.add("body", gson.toJsonTree(body));
        }
        if (headers != null && !headers.isEmpty()) {
            payload.add("headers", gson.toJsonTree(headers));
        }

        String jsonBody = gson.toJson(payload);

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (authToken != null && !authToken.isEmpty()) {
            reqBuilder.header("Authorization", "Bearer " + authToken);
        }
        if (apiKey != null && !apiKey.isEmpty()) {
            reqBuilder.header("X-API-Key", apiKey);
        }

        try {
            HttpResponse<String> response = httpClient.send(
                    reqBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            ApiResponse apiResp = gson.fromJson(response.body(), ApiResponse.class);

            if (!apiResp.isSuccess()) {
                throw new LeashError(
                        apiResp.getError() != null ? apiResp.getError() : "Unknown error",
                        apiResp.getCode(),
                        apiResp.getConnectUrl());
            }

            return apiResp.getData();

        } catch (IOException | InterruptedException e) {
            throw new LeashError("Request failed: " + e.getMessage(), "request_error");
        }
    }
}
