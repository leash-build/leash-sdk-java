package build.leash;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

/**
 * Auth precedence + the "Bearer is NEVER forwarded on integration POSTs"
 * contract. These are the load-bearing tests the prior reviews surfaced
 * as must-haves.
 */
class AuthPrecedenceTest {

    private WireMockServer wm;

    @BeforeEach
    void setUp() {
        wm = TestSupport.startWireMock();
    }

    @AfterEach
    void tearDown() {
        if (wm != null) wm.stop();
    }

    @Test
    void integrationPost_sendsXApiKey_neverAuthorizationBearer() {
        // Construct with BOTH api key + inbound Bearer JWT — verify the POST
        // carries X-API-Key but no Authorization header.
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer user-jwt");
        headers.put("Cookie", "leash-auth=cookie-jwt");

        Leash leash = TestSupport.leashFor(wm, "lsk_live_xxx", headers, java.util.Collections.emptyMap());

        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/list-messages"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"messages\":[]}")));

        leash.integrations().gmail().listMessages();

        // Negative assertion: NO Authorization header on this request.
        verify(postRequestedFor(urlEqualTo("/api/integrations/gmail/list-messages"))
                .withHeader("X-API-Key", equalTo("lsk_live_xxx"))
                .withHeader("Cookie", matching(".*leash-auth=cookie-jwt.*"))
                .withoutHeader("Authorization"));
    }

    @Test
    void envFetch_usesApiKeyAsBearer_whenPresent() {
        Leash leash = TestSupport.leashFor(wm, "lsk_live_envkey");

        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/MY_KEY"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\":\"hello\"}")));

        java.util.Optional<String> v = leash.env().get("MY_KEY");
        org.junit.jupiter.api.Assertions.assertTrue(v.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals("hello", v.get());

        verify(getRequestedFor(urlPathEqualTo("/api/apps/me/secrets/MY_KEY"))
                .withHeader("Authorization", equalTo("Bearer lsk_live_envkey")));
    }

    @Test
    void envFetch_fallsBackToInboundBearer_whenNoApiKey() {
        // No LEASH_API_KEY but the request carries a Bearer — the env-fetch
        // path should forward that bearer to the platform.
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer fallback-jwt");

        Leash leash = Leash.builder()
                .platformUrl(wm.baseUrl())
                .httpClient(TestSupport.http())
                .request(headers, java.util.Collections.emptyMap())
                .build();

        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/KEY2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\":\"bar\"}")));

        leash.env().get("KEY2");

        verify(getRequestedFor(urlPathEqualTo("/api/apps/me/secrets/KEY2"))
                .withHeader("Authorization", equalTo("Bearer fallback-jwt")));
    }

    @Test
    void cookieIsForwardedOnIntegrationPosts() {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("leash-auth", "session-jwt");

        Leash leash = TestSupport.leashFor(wm, "lsk_live_yyy",
                java.util.Collections.emptyMap(), cookies);

        wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_teams"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"teams\":[]}")));

        leash.integrations().linear().listTeams();

        verify(postRequestedFor(urlEqualTo("/api/integrations/linear/list_teams"))
                .withHeader("Cookie", matching(".*leash-auth=session-jwt.*"))
                .withHeader("X-API-Key", equalTo("lsk_live_yyy"))
                .withoutHeader("Authorization"));
    }

    @Test
    void noApiKey_noCookie_sendsNeitherHeader() {
        // Pure CLI flow: no key, no cookie. Integration call still goes through;
        // the platform will 401, but the SDK should not invent headers.
        Leash leash = Leash.builder()
                .platformUrl(wm.baseUrl())
                .httpClient(TestSupport.http())
                .request(java.util.Collections.emptyMap(), java.util.Collections.emptyMap())
                .build();

        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/list-labels"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"labels\":[]}")));

        leash.integrations().gmail().listLabels();

        verify(postRequestedFor(urlEqualTo("/api/integrations/gmail/list-labels"))
                .withHeader("X-API-Key", absent())
                .withHeader("Cookie", absent())
                .withoutHeader("Authorization"));
    }
}
