package build.leash.integrations.gmail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Paginated list shape returned by {@code listMessages} / {@code searchMessages}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GmailMessageList {

    private final List<GmailMessage> messages;
    private final String nextPageToken;
    private final Integer resultSizeEstimate;

    public GmailMessageList(
            @JsonProperty("messages") List<GmailMessage> messages,
            @JsonProperty("nextPageToken") String nextPageToken,
            @JsonProperty("resultSizeEstimate") Integer resultSizeEstimate) {
        this.messages = messages == null ? Collections.emptyList() : List.copyOf(messages);
        this.nextPageToken = nextPageToken;
        this.resultSizeEstimate = resultSizeEstimate;
    }

    public List<GmailMessage> getMessages() {
        return messages;
    }

    public Optional<String> getNextPageToken() {
        return Optional.ofNullable(nextPageToken);
    }

    public Optional<Integer> getResultSizeEstimate() {
        return Optional.ofNullable(resultSizeEstimate);
    }
}
