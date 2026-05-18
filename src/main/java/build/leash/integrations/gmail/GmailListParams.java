package build.leash.integrations.gmail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Optional knobs for {@code Gmail.listMessages(...)}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class GmailListParams {

    @JsonProperty("query")
    private final String query;

    @JsonProperty("maxResults")
    private final Integer maxResults;

    @JsonProperty("labelIds")
    private final List<String> labelIds;

    @JsonProperty("pageToken")
    private final String pageToken;

    private GmailListParams(Builder b) {
        this.query = b.query;
        this.maxResults = b.maxResults;
        this.labelIds = b.labelIds == null ? null : List.copyOf(b.labelIds);
        this.pageToken = b.pageToken;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getQuery() {
        return query;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public List<String> getLabelIds() {
        return labelIds;
    }

    public String getPageToken() {
        return pageToken;
    }

    public static final class Builder {
        private String query;
        private Integer maxResults;
        private List<String> labelIds;
        private String pageToken;

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder labelIds(List<String> labelIds) {
            this.labelIds = labelIds;
            return this;
        }

        public Builder pageToken(String pageToken) {
            this.pageToken = pageToken;
            return this;
        }

        public GmailListParams build() {
            return new GmailListParams(this);
        }
    }
}
