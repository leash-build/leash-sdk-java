package build.leash.integrations.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Single entry in {@code CalendarList}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CalendarListEntry {

    private final String id;
    private final String summary;
    private final String description;
    private final String timeZone;
    private final boolean primary;
    private final String backgroundColor;
    private final String foregroundColor;

    public CalendarListEntry(
            @JsonProperty("id") String id,
            @JsonProperty("summary") String summary,
            @JsonProperty("description") String description,
            @JsonProperty("timeZone") String timeZone,
            @JsonProperty("primary") Boolean primary,
            @JsonProperty("backgroundColor") String backgroundColor,
            @JsonProperty("foregroundColor") String foregroundColor) {
        this.id = id;
        this.summary = summary;
        this.description = description;
        this.timeZone = timeZone;
        this.primary = primary != null && primary;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
    }

    public String getId() { return id; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getTimeZone() { return timeZone; }
    public boolean isPrimary() { return primary; }
    public String getBackgroundColor() { return backgroundColor; }
    public String getForegroundColor() { return foregroundColor; }
}
