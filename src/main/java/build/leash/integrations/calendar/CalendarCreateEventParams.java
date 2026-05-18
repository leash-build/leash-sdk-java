package build.leash.integrations.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/** Body for {@code Calendar.createEvent(...)}. {@code summary}, {@code start}, {@code end} are required. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CalendarCreateEventParams {

    @JsonProperty("calendarId")
    private final String calendarId;

    @JsonProperty("summary")
    private final String summary;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("location")
    private final String location;

    @JsonProperty("start")
    private final CalendarEventDateTime start;

    @JsonProperty("end")
    private final CalendarEventDateTime end;

    @JsonProperty("attendees")
    private final List<CalendarAttendee> attendees;

    private CalendarCreateEventParams(Builder b) {
        this.calendarId = b.calendarId;
        this.summary = Objects.requireNonNull(b.summary, "summary");
        this.description = b.description;
        this.location = b.location;
        this.start = Objects.requireNonNull(b.start, "start");
        this.end = Objects.requireNonNull(b.end, "end");
        this.attendees = b.attendees == null ? null : List.copyOf(b.attendees);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCalendarId() { return calendarId; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public CalendarEventDateTime getStart() { return start; }
    public CalendarEventDateTime getEnd() { return end; }
    public List<CalendarAttendee> getAttendees() { return attendees; }

    public static final class Builder {
        private String calendarId;
        private String summary;
        private String description;
        private String location;
        private CalendarEventDateTime start;
        private CalendarEventDateTime end;
        private List<CalendarAttendee> attendees;

        public Builder calendarId(String calendarId) { this.calendarId = calendarId; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder start(CalendarEventDateTime start) { this.start = start; return this; }
        public Builder end(CalendarEventDateTime end) { this.end = end; return this; }
        public Builder attendees(List<CalendarAttendee> attendees) { this.attendees = attendees; return this; }

        public CalendarCreateEventParams build() {
            return new CalendarCreateEventParams(this);
        }
    }
}
