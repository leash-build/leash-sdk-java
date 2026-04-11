package build.leash.sdk.types;

import com.google.gson.annotations.SerializedName;

/**
 * Parameters for listing Google Calendar events.
 */
public class ListEventsParams {

    @SerializedName("calendarId")
    private String calendarId;

    @SerializedName("timeMin")
    private String timeMin;

    @SerializedName("timeMax")
    private String timeMax;

    @SerializedName("maxResults")
    private Integer maxResults;

    @SerializedName("singleEvents")
    private Boolean singleEvents;

    @SerializedName("orderBy")
    private String orderBy;

    private ListEventsParams() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getCalendarId() { return calendarId; }
    public String getTimeMin() { return timeMin; }
    public String getTimeMax() { return timeMax; }
    public Integer getMaxResults() { return maxResults; }
    public Boolean getSingleEvents() { return singleEvents; }
    public String getOrderBy() { return orderBy; }

    public static class Builder {
        private final ListEventsParams params = new ListEventsParams();

        public Builder calendarId(String calendarId) { params.calendarId = calendarId; return this; }
        public Builder timeMin(String timeMin) { params.timeMin = timeMin; return this; }
        public Builder timeMax(String timeMax) { params.timeMax = timeMax; return this; }
        public Builder maxResults(int maxResults) { params.maxResults = maxResults; return this; }
        public Builder singleEvents(boolean singleEvents) { params.singleEvents = singleEvents; return this; }
        public Builder orderBy(String orderBy) { params.orderBy = orderBy; return this; }

        public ListEventsParams build() { return params; }
    }
}
