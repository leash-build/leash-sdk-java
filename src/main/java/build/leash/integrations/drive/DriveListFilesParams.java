package build.leash.integrations.drive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Optional knobs for {@code Drive.listFiles(...)}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class DriveListFilesParams {

    @JsonProperty("query")
    private final String query;

    @JsonProperty("maxResults")
    private final Integer maxResults;

    @JsonProperty("folderId")
    private final String folderId;

    private DriveListFilesParams(Builder b) {
        this.query = b.query;
        this.maxResults = b.maxResults;
        this.folderId = b.folderId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getQuery() { return query; }
    public Integer getMaxResults() { return maxResults; }
    public String getFolderId() { return folderId; }

    public static final class Builder {
        private String query;
        private Integer maxResults;
        private String folderId;

        public Builder query(String query) { this.query = query; return this; }
        public Builder maxResults(Integer maxResults) { this.maxResults = maxResults; return this; }
        public Builder folderId(String folderId) { this.folderId = folderId; return this; }

        public DriveListFilesParams build() {
            return new DriveListFilesParams(this);
        }
    }
}
