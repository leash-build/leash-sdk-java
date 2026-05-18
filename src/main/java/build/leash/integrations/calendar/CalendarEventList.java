package build.leash.integrations.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Response from {@code Calendar.listEvents(...)}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CalendarEventList {

    private final List<CalendarEvent> events;
    private final String nextPageToken;

    public CalendarEventList(
            @JsonProperty("events") List<CalendarEvent> events,
            @JsonProperty("nextPageToken") String nextPageToken) {
        this.events = events == null ? Collections.emptyList() : List.copyOf(events);
        this.nextPageToken = nextPageToken;
    }

    public List<CalendarEvent> getEvents() {
        return events;
    }

    public Optional<String> getNextPageToken() {
        return Optional.ofNullable(nextPageToken);
    }
}
