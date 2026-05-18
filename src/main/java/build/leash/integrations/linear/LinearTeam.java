package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** A Linear team. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LinearTeam {

    private final String id;
    private final String key;
    private final String name;
    private final String description;
    private final boolean isPrivate;
    private final String icon;
    private final String color;

    public LinearTeam(
            @JsonProperty("id") String id,
            @JsonProperty("key") String key,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("private") Boolean isPrivate,
            @JsonProperty("icon") String icon,
            @JsonProperty("color") String color) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate != null && isPrivate;
        this.icon = icon;
        this.color = color;
    }

    public String getId() { return id; }
    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isPrivate() { return isPrivate; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }
}
