package build.leash.integrations.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/** Calendar event response shape. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CalendarEvent {

    private final String id;
    private final String summary;
    private final String description;
    private final String location;
    private final CalendarEventDateTime start;
    private final CalendarEventDateTime end;
    private final List<CalendarAttendee> attendees;
    private final String status;
    private final String htmlLink;
    private final String created;
    private final String updated;

    public CalendarEvent(
            @JsonProperty("id") String id,
            @JsonProperty("summary") String summary,
            @JsonProperty("description") String description,
            @JsonProperty("location") String location,
            @JsonProperty("start") CalendarEventDateTime start,
            @JsonProperty("end") CalendarEventDateTime end,
            @JsonProperty("attendees") List<CalendarAttendee> attendees,
            @JsonProperty("status") String status,
            @JsonProperty("htmlLink") String htmlLink,
            @JsonProperty("created") String created,
            @JsonProperty("updated") String updated) {
        this.id = id;
        this.summary = summary;
        this.description = description;
        this.location = location;
        this.start = start;
        this.end = end;
        this.attendees = attendees == null ? Collections.emptyList() : List.copyOf(attendees);
        this.status = status;
        this.htmlLink = htmlLink;
        this.created = created;
        this.updated = updated;
    }

    public String getId() { return id; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public CalendarEventDateTime getStart() { return start; }
    public CalendarEventDateTime getEnd() { return end; }
    public List<CalendarAttendee> getAttendees() { return attendees; }
    public String getStatus() { return status; }
    public String getHtmlLink() { return htmlLink; }
    public String getCreated() { return created; }
    public String getUpdated() { return updated; }
}
