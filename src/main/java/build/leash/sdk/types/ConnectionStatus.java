package build.leash.sdk.types;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the status of a provider connection.
 */
public class ConnectionStatus {

    @SerializedName("providerId")
    private String providerId;

    private String status;
    private String email;

    @SerializedName("expiresAt")
    private String expiresAt;

    public String getProviderId() {
        return providerId;
    }

    public String getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}
