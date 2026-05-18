package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Minimal team reference returned on issues. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LinearTeamRef {

    private final String id;
    private final String key;
    private final String name;

    public LinearTeamRef(
            @JsonProperty("id") String id,
            @JsonProperty("key") String key,
            @JsonProperty("name") String name) {
        this.id = id;
        this.key = key;
        this.name = name;
    }

    public String getId() { return id; }
    public String getKey() { return key; }
    public String getName() { return name; }
}
