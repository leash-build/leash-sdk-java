package build.leash.integrations.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Google-style date/time descriptor. Exactly one of dateTime / date is set. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CalendarEventDateTime {

    @JsonProperty("dateTime")
    private final String dateTime;

    @JsonProperty("date")
    private final String date;

    @JsonProperty("timeZone")
    private final String timeZone;

    private CalendarEventDateTime(Builder b) {
        this.dateTime = b.dateTime;
        this.date = b.date;
        this.timeZone = b.timeZone;
    }

    public CalendarEventDateTime(
            @JsonProperty("dateTime") String dateTime,
            @JsonProperty("date") String date,
            @JsonProperty("timeZone") String timeZone) {
        this.dateTime = dateTime;
        this.date = date;
        this.timeZone = timeZone;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getDateTime() { return dateTime; }
    public String getDate() { return date; }
    public String getTimeZone() { return timeZone; }

    public static final class Builder {
        private String dateTime;
        private String date;
        private String timeZone;

        public Builder dateTime(String dateTime) { this.dateTime = dateTime; return this; }
        public Builder date(String date) { this.date = date; return this; }
        public Builder timeZone(String timeZone) { this.timeZone = timeZone; return this; }
        public CalendarEventDateTime build() { return new CalendarEventDateTime(this); }
    }
}
