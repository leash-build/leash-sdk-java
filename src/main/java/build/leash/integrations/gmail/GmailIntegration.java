package build.leash.integrations.gmail;

import build.leash.internal.Json;
import build.leash.internal.Transport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * Typed Gmail provider client. Exposes the 6 verbs the TS surface has:
 * {@code listMessages}, {@code getMessage}, {@code sendMessage},
 * {@code searchMessages}, {@code listLabels}, {@code getProfile}.
 */
public final class GmailIntegration {

    private static final String PROVIDER = "gmail";

    private final Transport transport;

    public GmailIntegration(Transport transport) {
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    /** Convenience overload — uses platform defaults. */
    public GmailMessageList listMessages() {
        return listMessages(null);
    }

    public GmailMessageList listMessages(GmailListParams params) {
        JsonNode raw = transport.integrationsCall(PROVIDER, "list-messages", params);
        return Json.treeToValue(raw, GmailMessageList.class);
    }

    /** Convenience overload — uses {@link GmailMessageFormat#FULL}. */
    public JsonNode getMessage(String messageId) {
        return getMessage(messageId, GmailMessageFormat.FULL);
    }

    public JsonNode getMessage(String messageId, GmailMessageFormat format) {
        Objects.requireNonNull(messageId, "messageId");
        ObjectNode body = Json.newObject();
        body.put("messageId", messageId);
        if (format != null) {
            body.put("format", format.wireValue());
        }
        return transport.integrationsCall(PROVIDER, "get-message", body);
    }

    public JsonNode sendMessage(GmailSendMessageParams params) {
        Objects.requireNonNull(params, "params");
        return transport.integrationsCall(PROVIDER, "send-message", params);
    }

    public GmailMessageList searchMessages(String query) {
        return searchMessages(query, null);
    }

    public GmailMessageList searchMessages(String query, Integer maxResults) {
        Objects.requireNonNull(query, "query");
        ObjectNode body = Json.newObject();
        body.put("query", query);
        if (maxResults != null) {
            body.put("maxResults", maxResults);
        }
        JsonNode raw = transport.integrationsCall(PROVIDER, "search-messages", body);
        return Json.treeToValue(raw, GmailMessageList.class);
    }

    public GmailLabelList listLabels() {
        JsonNode raw = transport.integrationsCall(PROVIDER, "list-labels", null);
        return Json.treeToValue(raw, GmailLabelList.class);
    }

    public JsonNode getProfile() {
        return transport.integrationsCall(PROVIDER, "get-profile", null);
    }
}
