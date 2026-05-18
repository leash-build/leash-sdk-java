package build.leash.integrations.gmail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** Minimal Gmail message reference returned by list calls. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GmailMessage {

    private final String id;
    private final String threadId;

    public GmailMessage(@JsonProperty("id") String id,
                        @JsonProperty("threadId") String threadId) {
        this.id = id;
        this.threadId = threadId;
    }

    public String getId() {
        return id;
    }

    public String getThreadId() {
        return threadId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GmailMessage)) return false;
        GmailMessage that = (GmailMessage) other;
        return Objects.equals(id, that.id) && Objects.equals(threadId, that.threadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, threadId);
    }
}
