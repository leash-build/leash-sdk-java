package build.leash.sdk.types;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Parameters for listing Gmail messages.
 */
public class ListMessagesParams {

    private String query;

    @SerializedName("maxResults")
    private Integer maxResults;

    @SerializedName("labelIds")
    private List<String> labelIds;

    @SerializedName("pageToken")
    private String pageToken;

    private ListMessagesParams() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getQuery() { return query; }
    public Integer getMaxResults() { return maxResults; }
    public List<String> getLabelIds() { return labelIds; }
    public String getPageToken() { return pageToken; }

    public static class Builder {
        private final ListMessagesParams params = new ListMessagesParams();

        public Builder query(String query) { params.query = query; return this; }
        public Builder maxResults(int maxResults) { params.maxResults = maxResults; return this; }
        public Builder labelIds(List<String> labelIds) { params.labelIds = labelIds; return this; }
        public Builder pageToken(String pageToken) { params.pageToken = pageToken; return this; }

        public ListMessagesParams build() { return params; }
    }
}
