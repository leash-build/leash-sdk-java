package build.leash;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiny helpers shared by the test classes.
 */
public final class TestSupport {

    private TestSupport() {}

    public static WireMockServer startWireMock() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        // Configure the static WireMock client so verify() / stubFor() reach this
        // server's admin API instead of the default localhost:8080.
        WireMock.configureFor("localhost", server.port());
        return server;
    }

    public static HttpClient http() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /** Build a Leash via the public builder, scoped to a WireMock platform. */
    public static Leash leashFor(WireMockServer server, String apiKey) {
        return Leash.builder()
                .apiKey(apiKey)
                .platformUrl(server.baseUrl())
                .httpClient(http())
                .request(emptyHeaders(), Collections.emptyMap())
                .build();
    }

    /** Same as above but with cookie / headers injected on the request. */
    public static Leash leashFor(WireMockServer server, String apiKey,
                                 Map<String, String> headers,
                                 Map<String, String> cookies) {
        return Leash.builder()
                .apiKey(apiKey)
                .platformUrl(server.baseUrl())
                .httpClient(http())
                .request(headers, cookies)
                .build();
    }

    public static Map<String, String> emptyHeaders() {
        return new HashMap<>();
    }
}
