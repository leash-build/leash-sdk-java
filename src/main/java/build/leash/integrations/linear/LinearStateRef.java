package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Minimal workflow-state reference returned on issues. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LinearStateRef {

    private final String id;
    private final String name;
    private final LinearStateType type;
    private final String color;

    public LinearStateRef(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("color") String color) {
        this.id = id;
        this.name = name;
        this.type = LinearStateType.fromWire(type);
        this.color = color;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public LinearStateType getType() { return type; }
    public String getColor() { return color; }
}
