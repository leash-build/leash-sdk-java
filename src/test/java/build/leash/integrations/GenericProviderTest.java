package build.leash.integrations;

import build.leash.Leash;
import build.leash.TestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GenericProviderTest {

    private WireMockServer wm;
    private Leash leash;

    @BeforeEach
    void setUp() {
        wm = TestSupport.startWireMock();
        leash = TestSupport.leashFor(wm, "lsk_live_test");
    }

    @AfterEach
    void tearDown() {
        if (wm != null) wm.stop();
    }

    @Test
    void provider_call_routesToCorrectUrl() {
        wm.stubFor(post(urlEqualTo("/api/integrations/slack/post_message"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ts\":\"1234.5\"}")));

        JsonNode resp = leash.integrations()
                .provider("slack")
                .call("post_message", Map.of("channel", "#general", "text", "hi"));
        assertEquals("1234.5", resp.get("ts").asText());

        verify(postRequestedFor(urlEqualTo("/api/integrations/slack/post_message"))
                .withRequestBody(equalToJson("{\"channel\":\"#general\",\"text\":\"hi\"}")));
    }

    @Test
    void provider_name_returnsBoundName() {
        assertEquals("github", leash.integrations().provider("github").name());
    }
}
