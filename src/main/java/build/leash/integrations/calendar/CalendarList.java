package build.leash.integrations.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/** Response from {@code Calendar.listCalendars()}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CalendarList {

    private final List<CalendarListEntry> calendars;

    public CalendarList(@JsonProperty("calendars") List<CalendarListEntry> calendars) {
        this.calendars = calendars == null ? Collections.emptyList() : List.copyOf(calendars);
    }

    public List<CalendarListEntry> getCalendars() {
        return calendars;
    }
}
