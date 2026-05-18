package build.leash.integrations.gmail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Single Gmail label. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GmailLabel {

    private final String id;
    private final String name;
    private final String type;

    public GmailLabel(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
