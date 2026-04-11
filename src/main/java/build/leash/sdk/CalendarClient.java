package build.leash.sdk;

import build.leash.sdk.types.CreateEventParams;
import build.leash.sdk.types.ListEventsParams;
import com.google.gson.JsonElement;

import java.util.Map;

/**
 * Provides methods for interacting with Google Calendar via the Leash platform proxy.
 *
 * <p>Obtain an instance by calling {@link LeashIntegrations#calendar()}.
 */
public class CalendarClient {

    private static final String PROVIDER = "google_calendar";

    private final LeashIntegrations client;

    CalendarClient(LeashIntegrations client) {
        this.client = client;
    }

    /**
     * Returns all calendars accessible to the user.
     *
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement listCalendars() throws LeashError {
        return client.callInternal(PROVIDER, "list-calendars", null);
    }

    /**
     * Returns events from a calendar.
     *
     * @param params filter parameters, or null for server defaults
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement listEvents(ListEventsParams params) throws LeashError {
        return client.callInternal(PROVIDER, "list-events", params);
    }

    /**
     * Creates a new calendar event.
     *
     * @param params the event parameters
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement createEvent(CreateEventParams params) throws LeashError {
        return client.callInternal(PROVIDER, "create-event", params);
    }

    /**
     * Retrieves a single event by ID.
     *
     * @param eventId    the event ID
     * @param calendarId the calendar ID, or null for "primary"
     * @return raw JSON data from the API
     * @throws LeashError if the API returns an error
     */
    public JsonElement getEvent(String eventId, String calendarId) throws LeashError {
        var body = new java.util.LinkedHashMap<String, String>();
        body.put("eventId", eventId);
        if (calendarId != null && !calendarId.isEmpty()) {
            body.put("calendarId", calendarId);
        }
        return client.callInternal(PROVIDER, "get-event", body);
    }
}
