package build.leash.sdk;

import build.leash.sdk.types.ApiResponse;
import build.leash.sdk.types.ConnectionStatus;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main client for accessing Leash platform integrations.
 *
 * <p>Create an instance using the builder:
 * <pre>{@code
 * LeashIntegrations leash = LeashIntegrations.builder()
 *     .authToken("your-jwt-token")
 *     .apiKey("optional-api-key")
 *     .build();
 *
 * JsonElement messages = leash.gmail().listMessages(null);
 * }</pre>
 */
public class LeashIntegrations {

    /** Default Leash platform base URL. */
    public static final String DEFAULT_PLATFORM_URL = "https://leash.build";

    private final String platformUrl;
    private final String authToken;
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private volatile Map<String, String> envCache;

    private LeashIntegrations(Builder builder) {
        this.platformUrl = builder.platformUrl != null
                ? builder.platformUrl.replaceAll("/+$", "")
                : DEFAULT_PLATFORM_URL;
        this.authToken = builder.authToken;
        this.apiKey = builder.apiKey;
        this.httpClient = builder.httpClient != null ? builder.httpClient : HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    /**
     * Creates a new builder for constructing a LeashIntegrations client.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a GmailClient for interacting with the Gmail integration.
     */
    public GmailClient gmail() {
        return new GmailClient(this);
    }

    /**
     * Returns a CalendarClient for interacting with the Google Calendar integration.
     */
    public CalendarClient calendar() {
        return new CalendarClient(this);
    }

    /**
     * Returns a DriveClient for interacting with the Google Drive integration.
     */
    public DriveClient drive() {
        return new DriveClient(this);
    }

    /**
     * Returns a CustomIntegration for the given integration name.
     *
     * <p>This is the escape hatch for custom or untyped integrations that don't
     * have dedicated provider clients.
     *
     * @param name the custom integration name
     * @return a CustomIntegration instance
     */
    public CustomIntegration integration(String name) {
        return new CustomIntegration(name, platformUrl, authToken, apiKey, httpClient, gson);
    }

    /**
     * Performs a generic integration API call.
     *
     * <p>Sends a POST request to {@code /api/integrations/{provider}/{action}}
     * and returns the raw JSON data from the response.
     *
     * @param provider the integration provider (e.g. "gmail", "google_calendar")
     * @param action   the action to perform (e.g. "list-messages")
     * @param body     the request body, or null
     * @return the JSON data from the response
     * @throws LeashError if the API returns an error
     */
    public JsonElement call(String provider, String action, Object body) throws LeashError {
        return callInternal(provider, action, body);
    }

    /**
     * Checks whether a given provider is actively connected for the current user.
     *
     * @param providerId the provider identifier (e.g. "gmail")
     * @return true if the provider is connected and active
     */
    public boolean isConnected(String providerId) {
        try {
            List<ConnectionStatus> connections = getConnections();
            return connections.stream()
                    .anyMatch(c -> providerId.equals(c.getProviderId()) && "active".equals(c.getStatus()));
        } catch (LeashError e) {
            return false;
        }
    }

    /**
     * Retrieves the connection status for all providers.
     *
     * @return list of connection statuses
     * @throws LeashError if the API returns an error
     */
    public List<ConnectionStatus> getConnections() throws LeashError {
        String endpoint = platformUrl + "/api/integrations/connections";

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .GET();

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

            return gson.fromJson(apiResp.getData(),
                    new TypeToken<List<ConnectionStatus>>() {}.getType());

        } catch (IOException | InterruptedException e) {
            throw new LeashError("Request failed: " + e.getMessage(), "request_error");
        }
    }

    /**
     * Returns the URL to initiate an OAuth connection flow for the given provider.
     *
     * @param providerId the provider identifier (e.g. "gmail")
     * @param returnUrl  optional URL to redirect to after connection, or null
     * @return the connect URL
     */
    public String getConnectUrl(String providerId, String returnUrl) {
        String url = platformUrl + "/api/integrations/connect/" + providerId;
        if (returnUrl != null && !returnUrl.isEmpty()) {
            url += "?return_url=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
        }
        return url;
    }

    /**
     * Calls any MCP server tool directly via the Leash platform.
     *
     * <p>Sends a POST to {@code /api/mcp/run} with the npm package name,
     * tool name, and optional arguments.
     *
     * @param npmPackage the npm package name of the MCP server
     * @param tool       the tool name to invoke
     * @param args       optional arguments map, or null
     * @return the JSON data from the response
     * @throws LeashError if the API returns an error
     */
    public Object mcp(String npmPackage, String tool, Map<String, Object> args) throws LeashError {
        String endpoint = platformUrl + "/api/mcp/run";

        Map<String, Object> payload = new HashMap<>();
        payload.put("package", npmPackage);
        payload.put("tool", tool);
        if (args != null) {
            payload.put("args", args);
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

    /**
     * Fetches all environment variables from the Leash platform.
     * The result is cached after the first call.
     *
     * @return a map of environment variable key-value pairs
     * @throws LeashError if the API returns an error
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getEnv() throws LeashError {
        if (envCache != null) {
            return envCache;
        }

        String endpoint = platformUrl + "/api/apps/env";

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .GET();

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

            envCache = gson.fromJson(apiResp.getData(),
                    new TypeToken<Map<String, String>>() {}.getType());
            if (envCache == null) {
                envCache = new HashMap<>();
            }
            return envCache;

        } catch (IOException | InterruptedException e) {
            throw new LeashError("Request failed: " + e.getMessage(), "request_error");
        }
    }

    /**
     * Fetches a single environment variable by key.
     *
     * @param key the environment variable key
     * @return the value, or null if not found
     * @throws LeashError if the API returns an error
     */
    public String getEnv(String key) throws LeashError {
        Map<String, String> env = getEnv();
        return env.get(key);
    }

    // --- Internal ---

    JsonElement callInternal(String provider, String action, Object body) throws LeashError {
        String endpoint = platformUrl + "/api/integrations/" + provider + "/" + action;

        String jsonBody = body != null ? gson.toJson(body) : "{}";

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

    // --- Builder ---

    public static class Builder {
        private String platformUrl;
        private String authToken;
        private String apiKey;
        private HttpClient httpClient;

        /**
         * Sets the Leash platform base URL. Defaults to {@value DEFAULT_PLATFORM_URL}.
         */
        public Builder platformUrl(String platformUrl) {
            this.platformUrl = platformUrl;
            return this;
        }

        /**
         * Sets the JWT auth token for authentication. Required.
         */
        public Builder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        /**
         * Sets an optional API key for service-to-service authentication.
         * When set, it is sent as the X-API-Key header on every request.
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Sets a custom HttpClient. Defaults to {@code HttpClient.newHttpClient()}.
         */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Builds the LeashIntegrations client.
         */
        public LeashIntegrations build() {
            if (apiKey == null) {
                apiKey = System.getenv("LEASH_API_KEY");
            }
            return new LeashIntegrations(this);
        }
    }
}
