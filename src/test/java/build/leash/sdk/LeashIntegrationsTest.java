package build.leash.sdk;

import build.leash.sdk.types.CustomMcpServerConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LeashIntegrations client initialization, URL construction,
 * connect URL generation, and provider client access.
 */
class LeashIntegrationsTest {

    @Nested
    class ClientInitialization {

        @Test
        void defaultPlatformUrl() {
            assertEquals("https://leash.build", LeashIntegrations.DEFAULT_PLATFORM_URL);
        }

        @Test
        void buildWithAuthTokenOnly() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            assertNotNull(client);
        }

        @Test
        void buildWithApiKey() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .apiKey("my-api-key")
                    .build();
            assertNotNull(client);
        }

        @Test
        void buildWithCustomPlatformUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .platformUrl("https://custom.example.com")
                    .authToken("test-token")
                    .build();
            assertNotNull(client);
        }

        @Test
        void buildWithNoTokens() {
            // Builder should not throw even with no tokens set
            LeashIntegrations client = LeashIntegrations.builder().build();
            assertNotNull(client);
        }

        @Test
        void buildWithCustomHttpClient() {
            java.net.http.HttpClient customClient = java.net.http.HttpClient.newHttpClient();
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .httpClient(customClient)
                    .build();
            assertNotNull(client);
        }
    }

    @Nested
    class ConnectUrlGeneration {

        @Test
        void connectUrlWithDefaultPlatformUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", null);
            assertEquals("https://leash.build/api/integrations/connect/gmail", url);
        }

        @Test
        void connectUrlWithCustomPlatformUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .platformUrl("https://custom.example.com")
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", null);
            assertEquals("https://custom.example.com/api/integrations/connect/gmail", url);
        }

        @Test
        void connectUrlWithReturnUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", "https://myapp.com/callback");
            assertEquals(
                    "https://leash.build/api/integrations/connect/gmail?return_url=https%3A%2F%2Fmyapp.com%2Fcallback",
                    url);
        }

        @Test
        void connectUrlWithEmptyReturnUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", "");
            assertEquals("https://leash.build/api/integrations/connect/gmail", url);
        }

        @Test
        void connectUrlForDifferentProviders() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();

            assertEquals(
                    "https://leash.build/api/integrations/connect/google_calendar",
                    client.getConnectUrl("google_calendar", null));
            assertEquals(
                    "https://leash.build/api/integrations/connect/google_drive",
                    client.getConnectUrl("google_drive", null));
        }

        @Test
        void connectUrlTrailingSlashStripped() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .platformUrl("https://custom.example.com///")
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", null);
            assertEquals("https://custom.example.com/api/integrations/connect/gmail", url);
        }
    }

    @Nested
    class ProviderClients {

        @Test
        void gmailClientIsNotNull() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            GmailClient gmail = client.gmail();
            assertNotNull(gmail);
        }

        @Test
        void calendarClientIsNotNull() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            CalendarClient calendar = client.calendar();
            assertNotNull(calendar);
        }

        @Test
        void driveClientIsNotNull() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            DriveClient drive = client.drive();
            assertNotNull(drive);
        }

        @Test
        void customIntegrationIsNotNull() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            CustomIntegration integration = client.integration("stripe");
            assertNotNull(integration);
        }

        @Test
        void eachCallReturnsNewInstance() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            GmailClient g1 = client.gmail();
            GmailClient g2 = client.gmail();
            assertNotSame(g1, g2);
        }
    }

    @Nested
    class GetAccessToken {

        @Test
        void returnsAccessTokenOnSuccess() throws Exception {
            AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
            try (LocalServer server = LocalServer.start("/api/integrations/token", recorded,
                    200, "{\"success\":true,\"data\":{\"accessToken\":\"xoxb-abc123\",\"provider\":\"slack\"}}")) {

                LeashIntegrations client = LeashIntegrations.builder()
                        .platformUrl(server.baseUrl())
                        .authToken("test-token")
                        .apiKey("test-api-key")
                        .build();

                String token = client.getAccessToken("slack");
                assertEquals("xoxb-abc123", token);

                RecordedRequest req = recorded.get();
                assertEquals("POST", req.method);
                assertEquals("Bearer test-token", req.headers.get("Authorization"));
                assertEquals("test-api-key", req.headers.get("X-API-Key"));
                assertEquals("application/json", req.headers.get("Content-Type"));
                assertTrue(req.body.contains("\"provider\":\"slack\""), "body should include provider: " + req.body);
            }
        }

        @Test
        void throwsLeashErrorWhenNotConnected() throws Exception {
            AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
            try (LocalServer server = LocalServer.start("/api/integrations/token", recorded,
                    400, "{\"success\":false,\"error\":\"User has not connected slack\","
                            + "\"code\":\"not_connected\","
                            + "\"connectUrl\":\"https://leash.build/api/integrations/connect/slack\"}")) {

                LeashIntegrations client = LeashIntegrations.builder()
                        .platformUrl(server.baseUrl())
                        .authToken("test-token")
                        .build();

                LeashError err = assertThrows(LeashError.class, () -> client.getAccessToken("slack"));
                assertEquals("not_connected", err.getCode());
                assertEquals("https://leash.build/api/integrations/connect/slack", err.getConnectUrl());
            }
        }

        @Test
        void throwsLeashErrorWhenAccessTokenMissing() throws Exception {
            AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
            try (LocalServer server = LocalServer.start("/api/integrations/token", recorded,
                    200, "{\"success\":true,\"data\":{\"provider\":\"slack\"}}")) {

                LeashIntegrations client = LeashIntegrations.builder()
                        .platformUrl(server.baseUrl())
                        .authToken("test-token")
                        .build();

                LeashError err = assertThrows(LeashError.class, () -> client.getAccessToken("slack"));
                assertEquals("invalid_response", err.getCode());
            }
        }
    }

    @Nested
    class GetCustomMcpConfig {

        @Test
        void returnsConfigOnSuccess() throws Exception {
            AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
            try (LocalServer server = LocalServer.start("/api/integrations/mcp-config/acme-notion", recorded,
                    200, "{\"success\":true,\"data\":{"
                            + "\"slug\":\"acme-notion\","
                            + "\"displayName\":\"Acme Notion\","
                            + "\"url\":\"https://mcp.acme.com/notion\","
                            + "\"headers\":{\"Authorization\":\"Bearer secret-token\"}"
                            + "}}")) {

                LeashIntegrations client = LeashIntegrations.builder()
                        .platformUrl(server.baseUrl())
                        .authToken("test-token")
                        .apiKey("test-api-key")
                        .build();

                CustomMcpServerConfig config = client.getCustomMcpConfig("acme-notion");
                assertEquals("acme-notion", config.getSlug());
                assertEquals("Acme Notion", config.getDisplayName());
                assertEquals("https://mcp.acme.com/notion", config.getUrl());
                assertNotNull(config.getHeaders());
                assertEquals("Bearer secret-token", config.getHeaders().get("Authorization"));

                RecordedRequest req = recorded.get();
                assertEquals("GET", req.method);
                assertEquals("Bearer test-token", req.headers.get("Authorization"));
                assertEquals("test-api-key", req.headers.get("X-API-Key"));
            }
        }

        @Test
        void throwsLeashErrorOnUnknownMcpServer() throws Exception {
            AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
            try (LocalServer server = LocalServer.start("/api/integrations/mcp-config/does-not-exist", recorded,
                    404, "{\"success\":false,\"error\":\"Unknown MCP server\","
                            + "\"code\":\"unknown_mcp_server\"}")) {

                LeashIntegrations client = LeashIntegrations.builder()
                        .platformUrl(server.baseUrl())
                        .authToken("test-token")
                        .build();

                LeashError err = assertThrows(LeashError.class,
                        () -> client.getCustomMcpConfig("does-not-exist"));
                assertEquals("unknown_mcp_server", err.getCode());
            }
        }

    }

    // --- Test helpers ---

    /** A request captured by the local HTTP server for assertions. */
    static class RecordedRequest {
        final String method;
        final String path;
        final java.util.Map<String, String> headers;
        final String body;

        RecordedRequest(String method, String path, java.util.Map<String, String> headers, String body) {
            this.method = method;
            this.path = path;
            this.headers = headers;
            this.body = body;
        }
    }

    /** Minimal in-process HTTP server for testing without external deps. */
    static class LocalServer implements AutoCloseable {
        private final HttpServer server;
        private final int port;

        private LocalServer(HttpServer server, int port) {
            this.server = server;
            this.port = port;
        }

        static LocalServer start(String expectedPath,
                                 AtomicReference<RecordedRequest> recorded,
                                 int status,
                                 String responseBody) throws IOException {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            // Use a root handler so we can match the raw request URI exactly,
            // including any percent-encoding.
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String rawPath = exchange.getRequestURI().getRawPath();
                    java.util.Map<String, String> headers =
                            new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    exchange.getRequestHeaders().forEach((k, v) -> {
                        if (!v.isEmpty()) headers.put(k, v.get(0));
                    });
                    byte[] reqBody = exchange.getRequestBody().readAllBytes();
                    recorded.set(new RecordedRequest(
                            exchange.getRequestMethod(),
                            rawPath,
                            headers,
                            new String(reqBody, StandardCharsets.UTF_8)));

                    if (!rawPath.equals(expectedPath)) {
                        exchange.sendResponseHeaders(404, -1);
                        exchange.close();
                        return;
                    }

                    byte[] respBytes = responseBody.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(status, respBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(respBytes);
                    }
                }
            });
            server.start();
            return new LocalServer(server, server.getAddress().getPort());
        }

        String baseUrl() {
            return "http://127.0.0.1:" + port;
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
