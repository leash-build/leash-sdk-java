package build.leash.sdk;

import build.leash.sdk.types.ListMessagesParams;
import build.leash.sdk.types.SendMessageParams;
import com.google.gson.JsonElement;

import java.util.Map;

/**
 * Provides methods for interacting with Gmail via the Leash platform proxy.
 *
 * <p>Obtain an instance by calling {@link LeashIntegrations#gmail()}.
 */
public class GmailClient {

    private static final String PROVIDER = "gmail";

    private final LeashIntegrations client;

    GmailClient(LeashIntegrations client) {
        this.client = client;
    }

    /**
     * Returns messages from the user's mailbox.
     *
     * @param params search/filter parameters, or null for server defaults
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement listMessages(ListMessagesParams params) throws LeashError {
        return client.callInternal(PROVIDER, "list-messages", params);
    }

    /**
     * Retrieves a single message by ID.
     *
     * @param messageId the message ID
     * @param format    response format: "full", "metadata", "minimal", or "raw"; null for default
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement getMessage(String messageId, String format) throws LeashError {
        return client.callInternal(PROVIDER, "get-message",
                Map.of("messageId", messageId, "format", format != null ? format : "full"));
    }

    /**
     * Sends an email message.
     *
     * @param params the message parameters
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement sendMessage(SendMessageParams params) throws LeashError {
        return client.callInternal(PROVIDER, "send-message", params);
    }

    /**
     * Searches messages using a Gmail query string.
     *
     * @param query      the Gmail search query
     * @param maxResults maximum number of results to return
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement searchMessages(String query, int maxResults) throws LeashError {
        return client.callInternal(PROVIDER, "search-messages",
                Map.of("query", query, "maxResults", maxResults));
    }

    /**
     * Returns all labels in the user's mailbox.
     *
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement listLabels() throws LeashError {
        return client.callInternal(PROVIDER, "list-labels", null);
    }
}
