package build.leash.errors;

import build.leash.Leash;
import build.leash.TestSupport;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeashExceptionTest {

    @Test
    void baseException_carriesCodeMessageActionStatus() {
        LeashException ex = new LeashException(
                "TEST", "test message", "do X", "https://leash.build", 500, null);
        assertEquals("TEST", ex.getCode());
        assertEquals("do X", ex.getAction());
        assertEquals("https://leash.build", ex.getSeeAlso());
        assertEquals(Integer.valueOf(500), ex.getStatus());
        assertTrue(ex.getMessage().contains("test message"));
        assertTrue(ex.getMessage().contains("Fix: do X"));
        assertTrue(ex.getMessage().contains("See: https://leash.build"));
    }

    @Test
    void planBlockSubclass_carriesUpgradeRequiredCode() {
        PlanBlockException ex = new PlanBlockException("upgrade", "u", "s", 402);
        assertEquals(LeashErrorCode.UPGRADE_REQUIRED, ex.getCode());
        // Subclass hierarchy: UpgradeRequired -> PlanBlock -> LeashException
        assertTrue(ex instanceof LeashException);
    }

    @Test
    void upgradeRequired_isPlanBlock() {
        UpgradeRequiredException ex = new UpgradeRequiredException("u", "a", "s", 402);
        assertTrue(ex instanceof PlanBlockException);
        assertEquals(LeashErrorCode.UPGRADE_REQUIRED, ex.getCode());
    }

    @Test
    void connectionRequired_carriesIntegrationNotEnabledCode() {
        ConnectionRequiredException ex = new ConnectionRequiredException("nope", "a", "s", 403);
        assertEquals(LeashErrorCode.INTEGRATION_NOT_ENABLED, ex.getCode());
    }

    @Test
    void unauthorized_carriesUnauthorizedCode() {
        UnauthorizedException ex = new UnauthorizedException("no", "a", "s", 401);
        assertEquals(LeashErrorCode.UNAUTHORIZED, ex.getCode());
    }

    @Test
    void keyNotDeclared_carriesKeyNotDeclaredCode() {
        KeyNotDeclaredException ex = new KeyNotDeclaredException("missing", "a", "s", 404);
        assertEquals(LeashErrorCode.KEY_NOT_DECLARED, ex.getCode());
    }

    @Test
    void networkException_carriesNetworkErrorCode_andCause() {
        Throwable cause = new java.net.UnknownHostException("nope");
        NetworkException ex = new NetworkException("boom", cause);
        assertEquals(LeashErrorCode.NETWORK_ERROR, ex.getCode());
        assertNotNull(ex.getCause());
    }

    @Test
    void integrationCall_mapsToSubclasses() throws Exception {
        WireMockServer wm = TestSupport.startWireMock();
        try {
            Leash leash = TestSupport.leashFor(wm, "k");

            wm.stubFor(post(urlEqualTo("/api/integrations/gmail/list-labels"))
                    .willReturn(aResponse().withStatus(401)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"error\":\"unauth\"}")));
            assertThrows(UnauthorizedException.class,
                    () -> leash.integrations().gmail().listLabels());

            wm.stubFor(post(urlEqualTo("/api/integrations/gmail/get-profile"))
                    .willReturn(aResponse().withStatus(402)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"message\":\"Need Growth\"}")));
            PlanBlockException pb = assertThrows(PlanBlockException.class,
                    () -> leash.integrations().gmail().getProfile());
            assertTrue(pb.getMessage().contains("Need Growth"));

            wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_teams"))
                    .willReturn(aResponse().withStatus(403)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"error\":\"not connected\"}")));
            assertThrows(ConnectionRequiredException.class,
                    () -> leash.integrations().linear().listTeams());

            wm.stubFor(post(urlEqualTo("/api/integrations/linear/get_issue"))
                    .willReturn(aResponse().withStatus(500)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"error\":\"oops\"}")));
            LeashException base = assertThrows(LeashException.class,
                    () -> leash.integrations().linear().getIssue("i1"));
            assertEquals(LeashErrorCode.INTEGRATION_ERROR, base.getCode());
        } finally {
            wm.stop();
        }
    }

    @Test
    void integrationCall_envelopeWithSuccessFalse_throws() {
        WireMockServer wm = TestSupport.startWireMock();
        try {
            Leash leash = TestSupport.leashFor(wm, "k");
            wm.stubFor(post(urlEqualTo("/api/integrations/gmail/list-labels"))
                    .willReturn(aResponse().withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"success\":false,\"error\":\"upstream broke\",\"code\":\"INTEGRATION_ERROR\"}")));
            LeashException ex = assertThrows(LeashException.class,
                    () -> leash.integrations().gmail().listLabels());
            assertEquals(LeashErrorCode.INTEGRATION_ERROR, ex.getCode());
            assertTrue(ex.getMessage().contains("upstream broke"));
        } finally {
            wm.stop();
        }
    }
}
