package build.leash.sdk;

import build.leash.sdk.types.ListFilesParams;
import com.google.gson.JsonElement;

import java.util.Map;

/**
 * Provides methods for interacting with Google Drive via the Leash platform proxy.
 *
 * <p>Obtain an instance by calling {@link LeashIntegrations#drive()}.
 */
public class DriveClient {

    private static final String PROVIDER = "google_drive";

    private final LeashIntegrations client;

    DriveClient(LeashIntegrations client) {
        this.client = client;
    }

    /**
     * Returns files from the user's Drive.
     *
     * @param params filter parameters, or null for server defaults
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement listFiles(ListFilesParams params) throws LeashError {
        return client.callInternal(PROVIDER, "list-files", params);
    }

    /**
     * Retrieves file metadata by ID.
     *
     * @param fileId the file ID
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement getFile(String fileId) throws LeashError {
        return client.callInternal(PROVIDER, "get-file", Map.of("fileId", fileId));
    }

    /**
     * Searches for files using a query string.
     *
     * @param query      the search query
     * @param maxResults maximum number of results to return
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement searchFiles(String query, int maxResults) throws LeashError {
        return client.callInternal(PROVIDER, "search-files",
                Map.of("query", query, "maxResults", maxResults));
    }
}
