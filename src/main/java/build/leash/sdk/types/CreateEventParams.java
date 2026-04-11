package build.leash.sdk.types;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Parameters for creating a Google Calendar event.
 */
public class CreateEventParams {

    @SerializedName("calendarId")
    private String calendarId;

    private String summary;
    private String description;
    private String location;
    private Map<String, String> start;
    private Map<String, String> end;
    private List<Map<String, String>> attendees;

    private CreateEventParams() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getCalendarId() { return calendarId; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Map<String, String> getStart() { return start; }
    public Map<String, String> getEnd() { return end; }
    public List<Map<String, String>> getAttendees() { return attendees; }

    public static class Builder {
        private final CreateEventParams params = new CreateEventParams();

        public Builder calendarId(String calendarId) { params.calendarId = calendarId; return this; }
        public Builder summary(String summary) { params.summary = summary; return this; }
        public Builder description(String description) { params.description = description; return this; }
        public Builder location(String location) { params.location = location; return this; }

        /**
         * Set event start time.
         * @param dateTime RFC 3339 timestamp (for timed events), or null
         * @param date date string "YYYY-MM-DD" (for all-day events), or null
         * @param timeZone IANA time zone (e.g. "America/New_York"), or null
         */
        public Builder start(String dateTime, String date, String timeZone) {
            params.start = buildDateTime(dateTime, date, timeZone);
            return this;
        }

        /**
         * Set event end time.
         */
        public Builder end(String dateTime, String date, String timeZone) {
            params.end = buildDateTime(dateTime, date, timeZone);
            return this;
        }

        public Builder attendees(List<String> emails) {
            params.attendees = emails.stream()
                    .map(e -> Map.of("email", e))
                    .toList();
            return this;
        }

        public CreateEventParams build() {
            if (params.summary == null || params.summary.isEmpty()) {
                throw new IllegalArgumentException("'summary' is required");
            }
            if (params.start == null) {
                throw new IllegalArgumentException("'start' is required");
            }
            if (params.end == null) {
                throw new IllegalArgumentException("'end' is required");
            }
            return params;
        }

        private static Map<String, String> buildDateTime(String dateTime, String date, String timeZone) {
            var map = new java.util.LinkedHashMap<String, String>();
            if (dateTime != null) map.put("dateTime", dateTime);
            if (date != null) map.put("date", date);
            if (timeZone != null) map.put("timeZone", timeZone);
            return map;
        }
    }
}
