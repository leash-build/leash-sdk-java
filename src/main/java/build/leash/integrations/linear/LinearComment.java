package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** A comment on a Linear issue. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LinearComment {

    private final String id;
    private final String body;
    private final String issueId;
    private final LinearUserRef user;
    private final String createdAt;
    private final String updatedAt;
    private final String url;

    public LinearComment(
            @JsonProperty("id") String id,
            @JsonProperty("body") String body,
            @JsonProperty("issueId") String issueId,
            @JsonProperty("user") LinearUserRef user,
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("updatedAt") String updatedAt,
            @JsonProperty("url") String url) {
        this.id = id;
        this.body = body;
        this.issueId = issueId;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.url = url;
    }

    public String getId() { return id; }
    public String getBody() { return body; }
    public String getIssueId() { return issueId; }
    public LinearUserRef getUser() { return user; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getUrl() { return url; }
}
