package build.leash;

import build.leash.errors.LeashErrorCode;
import build.leash.errors.LeashException;
import build.leash.errors.PlanBlockException;
import build.leash.errors.UnauthorizedException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvNamespaceTest {

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
    void get_returnsValue() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/OPENAI_API_KEY"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\":\"sk-xyz\"}")));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        Optional<String> v = leash.env().get("OPENAI_API_KEY");
        assertTrue(v.isPresent());
        assertEquals("sk-xyz", v.get());
    }

    @Test
    void get_returnsEmptyOn404() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/MISSING"))
                .willReturn(aResponse().withStatus(404)));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        assertTrue(leash.env().get("MISSING").isEmpty());
    }

    @Test
    void get_cachesByDefault() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/CACHED"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\":\"v1\"}")));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        leash.env().get("CACHED");
        leash.env().get("CACHED");
        leash.env().get("CACHED");

        verify(1, getRequestedFor(urlPathEqualTo("/api/apps/me/secrets/CACHED")));
    }

    @Test
    void get_freshBypassesCache() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/FRESH"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\":\"v1\"}")));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        leash.env().get("FRESH");
        leash.env().get("FRESH", EnvOptions.fresh());
        leash.env().get("FRESH", EnvOptions.fresh());

        verify(3, getRequestedFor(urlPathEqualTo("/api/apps/me/secrets/FRESH")));
    }

    @Test
    void getMany_returnsMapPreservingMissing() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/A"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\":\"a\"}")));
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/B"))
                .willReturn(aResponse().withStatus(404)));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        Map<String, Optional<String>> many = leash.env().getMany(List.of("A", "B"));

        assertEquals("a", many.get("A").orElseThrow());
        assertTrue(many.get("B").isEmpty());
    }

    @Test
    void get_throwsUnauthorizedOn401() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/SECRET"))
                .willReturn(aResponse().withStatus(401)));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> leash.env().get("SECRET"));
        assertEquals(LeashErrorCode.UNAUTHORIZED, ex.getCode());
        assertEquals(Integer.valueOf(401), ex.getStatus());
    }

    @Test
    void get_throwsPlanBlockOn402() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/PAID"))
                .willReturn(aResponse()
                        .withStatus(402)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"requiredPlan\":\"Growth\"}")));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        PlanBlockException ex = assertThrows(PlanBlockException.class,
                () -> leash.env().get("PAID"));
        assertEquals(LeashErrorCode.UPGRADE_REQUIRED, ex.getCode());
        assertTrue(ex.getMessage().contains("Growth"));
    }

    @Test
    void get_throwsInvalidKeyOn400() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/bad-key"))
                .willReturn(aResponse().withStatus(400)));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        LeashException ex = assertThrows(LeashException.class,
                () -> leash.env().get("bad-key"));
        assertEquals(LeashErrorCode.INVALID_KEY, ex.getCode());
    }

    @Test
    void get_throwsSourceResyncOn502() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/RESYNC"))
                .willReturn(aResponse()
                        .withStatus(502)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"vault unreachable\"}")));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        LeashException ex = assertThrows(LeashException.class,
                () -> leash.env().get("RESYNC"));
        assertEquals(LeashErrorCode.SOURCE_RESYNC_FAILED, ex.getCode());
        assertTrue(ex.getMessage().contains("vault unreachable"));
    }

    @Test
    void get_throwsEnvFetchErrorOnOther500() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/CRASH"))
                .willReturn(aResponse().withStatus(503)));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        LeashException ex = assertThrows(LeashException.class,
                () -> leash.env().get("CRASH"));
        assertEquals(LeashErrorCode.ENV_FETCH_ERROR, ex.getCode());
    }

    @Test
    void get_throwsNoApiKey_whenNoCredentialsAtAll() {
        Leash leash = Leash.builder()
                .platformUrl(wm.baseUrl())
                .httpClient(TestSupport.http())
                .request(java.util.Collections.emptyMap(), java.util.Collections.emptyMap())
                .build();

        LeashException ex = assertThrows(LeashException.class,
                () -> leash.env().get("ANY"));
        assertEquals(LeashErrorCode.NO_API_KEY, ex.getCode());
    }

    @Test
    void get_handlesUnexpectedShape() {
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/WEIRD"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"wrong\":\"shape\"}")));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        LeashException ex = assertThrows(LeashException.class,
                () -> leash.env().get("WEIRD"));
        assertEquals(LeashErrorCode.ENV_FETCH_ERROR, ex.getCode());
    }

    @Test
    void get_urlEncodesSpecialKey() {
        // Key with special chars exercises the URLEncoder path; the platform
        // still rejects them, but we want to make sure no crash on the client.
        wm.stubFor(get(urlPathEqualTo("/api/apps/me/secrets/A%24B"))
                .willReturn(aResponse().withStatus(400)));

        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        LeashException ex = assertThrows(LeashException.class,
                () -> leash.env().get("A$B"));
        assertEquals(LeashErrorCode.INVALID_KEY, ex.getCode());
    }

    @Test
    void envNamespace_isAccessibleFromLeash() {
        Leash leash = TestSupport.leashFor(wm, "lsk_live_test");
        assertNotNull(leash.env());
    }
}
