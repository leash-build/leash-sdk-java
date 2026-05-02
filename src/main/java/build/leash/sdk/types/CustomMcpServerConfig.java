package build.leash.sdk.types;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Resolved config for a customer-registered MCP server (LEA-143).
 *
 * <p>Returned by {@code LeashIntegrations#getCustomMcpConfig(String)}. Contains
 * the customer's MCP endpoint plus any auth headers (including resolved
 * {@code Authorization: Bearer ...} for bearer-auth servers) — feed this directly
 * into your MCP client. Leash isn't on the MCP request path.
 */
public class CustomMcpServerConfig {

    private String slug;

    @SerializedName("displayName")
    private String displayName;

    /** Customer's MCP endpoint. */
    private String url;

    /**
     * Headers to attach to every request, including resolved Authorization
     * for bearer-auth servers.
     */
    private Map<String, String> headers;

    public String getSlug() {
        return slug;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
