package build.leash.sdk.types;

import com.google.gson.annotations.SerializedName;

/**
 * Parameters for listing Google Drive files.
 */
public class ListFilesParams {

    private String query;

    @SerializedName("maxResults")
    private Integer maxResults;

    @SerializedName("folderId")
    private String folderId;

    private ListFilesParams() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getQuery() { return query; }
    public Integer getMaxResults() { return maxResults; }
    public String getFolderId() { return folderId; }

    public static class Builder {
        private final ListFilesParams params = new ListFilesParams();

        public Builder query(String query) { params.query = query; return this; }
        public Builder maxResults(int maxResults) { params.maxResults = maxResults; return this; }
        public Builder folderId(String folderId) { params.folderId = folderId; return this; }

        public ListFilesParams build() { return params; }
    }
}
