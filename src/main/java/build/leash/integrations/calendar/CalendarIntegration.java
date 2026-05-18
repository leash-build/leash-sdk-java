package build.leash.integrations.calendar;

import build.leash.internal.Json;
import build.leash.internal.Transport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * Typed Google Calendar provider client (platform id {@code google_calendar}).
 * Exposes the 4 verbs the TS surface has: {@code listCalendars},
 * {@code listEvents}, {@code createEvent}, {@code getEvent}.
 */
public final class CalendarIntegration {

    private static final String PROVIDER = "google_calendar";

    private final Transport transport;

    public CalendarIntegration(Transport transport) {
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    public CalendarList listCalendars() {
        JsonNode raw = transport.integrationsCall(PROVIDER, "list-calendars", null);
        return Json.treeToValue(raw, CalendarList.class);
    }

    public CalendarEventList listEvents() {
        return listEvents(null);
    }

    public CalendarEventList listEvents(CalendarListEventsParams params) {
        JsonNode raw = transport.integrationsCall(PROVIDER, "list-events", params);
        return Json.treeToValue(raw, CalendarEventList.class);
    }

    public CalendarEvent createEvent(CalendarCreateEventParams params) {
        Objects.requireNonNull(params, "params");
        JsonNode raw = transport.integrationsCall(PROVIDER, "create-event", params);
        return Json.treeToValue(raw, CalendarEvent.class);
    }

    public CalendarEvent getEvent(String eventId) {
        return getEvent(eventId, null);
    }

    public CalendarEvent getEvent(String eventId, String calendarId) {
        Objects.requireNonNull(eventId, "eventId");
        ObjectNode body = Json.newObject();
        body.put("eventId", eventId);
        if (calendarId != null && !calendarId.isEmpty()) {
            body.put("calendarId", calendarId);
        }
        JsonNode raw = transport.integrationsCall(PROVIDER, "get-event", body);
        return Json.treeToValue(raw, CalendarEvent.class);
    }
}
