package build.leash.integrations.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Optional knobs for {@code Calendar.listEvents(...)}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CalendarListEventsParams {

    @JsonProperty("calendarId")
    private final String calendarId;

    @JsonProperty("timeMin")
    private final String timeMin;

    @JsonProperty("timeMax")
    private final String timeMax;

    @JsonProperty("maxResults")
    private final Integer maxResults;

    @JsonProperty("query")
    private final String query;

    @JsonProperty("singleEvents")
    private final Boolean singleEvents;

    @JsonProperty("orderBy")
    private final String orderBy;

    private CalendarListEventsParams(Builder b) {
        this.calendarId = b.calendarId;
        this.timeMin = b.timeMin;
        this.timeMax = b.timeMax;
        this.maxResults = b.maxResults;
        this.query = b.query;
        this.singleEvents = b.singleEvents;
        this.orderBy = b.orderBy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCalendarId() { return calendarId; }
    public String getTimeMin() { return timeMin; }
    public String getTimeMax() { return timeMax; }
    public Integer getMaxResults() { return maxResults; }
    public String getQuery() { return query; }
    public Boolean getSingleEvents() { return singleEvents; }
    public String getOrderBy() { return orderBy; }

    public static final class Builder {
        private String calendarId;
        private String timeMin;
        private String timeMax;
        private Integer maxResults;
        private String query;
        private Boolean singleEvents;
        private String orderBy;

        public Builder calendarId(String calendarId) { this.calendarId = calendarId; return this; }
        public Builder timeMin(String timeMin) { this.timeMin = timeMin; return this; }
        public Builder timeMax(String timeMax) { this.timeMax = timeMax; return this; }
        public Builder maxResults(Integer maxResults) { this.maxResults = maxResults; return this; }
        public Builder query(String query) { this.query = query; return this; }
        public Builder singleEvents(Boolean singleEvents) { this.singleEvents = singleEvents; return this; }
        public Builder orderBy(String orderBy) { this.orderBy = orderBy; return this; }

        public CalendarListEventsParams build() {
            return new CalendarListEventsParams(this);
        }
    }
}
