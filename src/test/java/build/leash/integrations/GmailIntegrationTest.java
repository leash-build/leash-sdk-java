package build.leash.integrations;

import build.leash.Leash;
import build.leash.TestSupport;
import build.leash.integrations.gmail.GmailLabelList;
import build.leash.integrations.gmail.GmailListParams;
import build.leash.integrations.gmail.GmailMessageFormat;
import build.leash.integrations.gmail.GmailMessageList;
import build.leash.integrations.gmail.GmailSendMessageParams;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GmailIntegrationTest {

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
    void listMessages_defaults() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/list-messages"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"messages\":[{\"id\":\"m1\",\"threadId\":\"t1\"}]}")));

        GmailMessageList list = leash.integrations().gmail().listMessages();
        assertEquals(1, list.getMessages().size());
        assertEquals("m1", list.getMessages().get(0).getId());
    }

    @Test
    void listMessages_sendsParams() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/list-messages"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"messages\":[]}")));

        leash.integrations().gmail().listMessages(
                GmailListParams.builder()
                        .query("from:me")
                        .maxResults(5)
                        .labelIds(List.of("INBOX"))
                        .build());

        verify(postRequestedFor(urlEqualTo("/api/integrations/gmail/list-messages"))
                .withRequestBody(equalToJson(
                        "{\"query\":\"from:me\",\"maxResults\":5,\"labelIds\":[\"INBOX\"]}")));
    }

    @Test
    void getMessage_sendsIdAndFormat() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/get-message"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"m1\",\"snippet\":\"hi\"}")));

        JsonNode node = leash.integrations().gmail().getMessage("m1", GmailMessageFormat.METADATA);
        assertEquals("m1", node.get("id").asText());

        verify(postRequestedFor(urlEqualTo("/api/integrations/gmail/get-message"))
                .withRequestBody(equalToJson("{\"messageId\":\"m1\",\"format\":\"metadata\"}")));
    }

    @Test
    void getMessage_defaultsToFullFormat() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/get-message"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        leash.integrations().gmail().getMessage("xyz");
        verify(postRequestedFor(urlEqualTo("/api/integrations/gmail/get-message"))
                .withRequestBody(equalToJson("{\"messageId\":\"xyz\",\"format\":\"full\"}")));
    }

    @Test
    void sendMessage_serialisesAllFields() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/send-message"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"sent-1\"}")));

        leash.integrations().gmail().sendMessage(
                GmailSendMessageParams.builder()
                        .to("a@b.com")
                        .subject("Hi")
                        .body("hello")
                        .cc("c@b.com")
                        .build());

        verify(postRequestedFor(urlEqualTo("/api/integrations/gmail/send-message"))
                .withRequestBody(equalToJson(
                        "{\"to\":\"a@b.com\",\"subject\":\"Hi\",\"body\":\"hello\",\"cc\":\"c@b.com\"}")));
    }

    @Test
    void searchMessages_minimalCall() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/search-messages"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"messages\":[]}")));

        GmailMessageList list = leash.integrations().gmail().searchMessages("from:me");
        assertNotNull(list);
        assertTrue(list.getMessages().isEmpty());

        verify(postRequestedFor(urlEqualTo("/api/integrations/gmail/search-messages"))
                .withRequestBody(equalToJson("{\"query\":\"from:me\"}")));
    }

    @Test
    void searchMessages_withMaxResults() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/search-messages"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"messages\":[]}")));

        leash.integrations().gmail().searchMessages("subject:test", 10);

        verify(postRequestedFor(urlEqualTo("/api/integrations/gmail/search-messages"))
                .withRequestBody(equalToJson("{\"query\":\"subject:test\",\"maxResults\":10}")));
    }

    @Test
    void listLabels_returnsTypedList() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/list-labels"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"labels\":[{\"id\":\"INBOX\",\"name\":\"Inbox\",\"type\":\"system\"}]}")));

        GmailLabelList labels = leash.integrations().gmail().listLabels();
        assertEquals(1, labels.getLabels().size());
        assertEquals("Inbox", labels.getLabels().get(0).getName());
    }

    @Test
    void getProfile_returnsRawJson() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/get-profile"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"emailAddress\":\"x@y.com\",\"messagesTotal\":100}")));

        JsonNode profile = leash.integrations().gmail().getProfile();
        assertEquals("x@y.com", profile.get("emailAddress").asText());
        assertEquals(100, profile.get("messagesTotal").asInt());
    }

    @Test
    void unwraps_successDataEnvelope() {
        wm.stubFor(post(urlEqualTo("/api/integrations/gmail/list-labels"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"data\":{\"labels\":[]}}")));

        GmailLabelList labels = leash.integrations().gmail().listLabels();
        assertNotNull(labels);
        assertTrue(labels.getLabels().isEmpty());
    }
}
