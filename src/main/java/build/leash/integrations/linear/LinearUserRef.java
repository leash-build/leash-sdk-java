package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Minimal user reference Linear returns on issues + comments. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LinearUserRef {

    private final String id;
    private final String name;
    private final String email;
    private final String displayName;

    public LinearUserRef(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("displayName") String displayName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.displayName = displayName;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
}
