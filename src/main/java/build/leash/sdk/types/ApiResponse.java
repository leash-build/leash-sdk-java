package build.leash.sdk.types;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Standard envelope returned by all Leash platform API endpoints.
 */
public class ApiResponse {

    private boolean success;
    private JsonElement data;
    private String error;
    private String code;

    @SerializedName("connectUrl")
    private String connectUrl;

    public boolean isSuccess() {
        return success;
    }

    public JsonElement getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public String getCode() {
        return code;
    }

    public String getConnectUrl() {
        return connectUrl;
    }
}
