package build.leash.integrations;

import build.leash.Leash;
import build.leash.TestSupport;
import build.leash.integrations.calendar.CalendarCreateEventParams;
import build.leash.integrations.calendar.CalendarEvent;
import build.leash.integrations.calendar.CalendarEventDateTime;
import build.leash.integrations.calendar.CalendarEventList;
import build.leash.integrations.calendar.CalendarList;
import build.leash.integrations.calendar.CalendarListEventsParams;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CalendarIntegrationTest {

    private WireMockServer wm;
    private Leash leash;

    @BeforeEach
    void setUp() {
        wm = TestSupport.startWireMock();
        leash = TestSupport.leashFor(wm, "lsk_live_test");
    }

    @AfterEach
    void tearDown() {
        if (wm != null) wm.stop();
    }

    @Test
    void listCalendars_returnsTyped() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_calendar/list-calendars"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"calendars\":[{\"id\":\"primary\",\"summary\":\"Me\",\"primary\":true}]}")));

        CalendarList cl = leash.integrations().calendar().listCalendars();
        assertEquals(1, cl.getCalendars().size());
        assertEquals("primary", cl.getCalendars().get(0).getId());
        assertNotNull(cl.getCalendars().get(0).getSummary());
    }

    @Test
    void listEvents_sendsParams() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_calendar/list-events"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"events\":[]}")));

        leash.integrations().calendar().listEvents(
                CalendarListEventsParams.builder()
                        .calendarId("primary")
                        .maxResults(5)
                        .singleEvents(true)
                        .build());

        verify(postRequestedFor(urlEqualTo("/api/integrations/google_calendar/list-events"))
                .withRequestBody(equalToJson(
                        "{\"calendarId\":\"primary\",\"maxResults\":5,\"singleEvents\":true}")));
    }

    @Test
    void listEvents_withoutParams() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_calendar/list-events"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"events\":[]}")));

        CalendarEventList list = leash.integrations().calendar().listEvents();
        assertNotNull(list);
    }

    @Test
    void createEvent_serialisesBody() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_calendar/create-event"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"ev1\",\"summary\":\"Standup\"}")));

        CalendarEvent ev = leash.integrations().calendar().createEvent(
                CalendarCreateEventParams.builder()
                        .summary("Standup")
                        .start(CalendarEventDateTime.builder().dateTime("2026-05-15T10:00:00Z").build())
                        .end(CalendarEventDateTime.builder().dateTime("2026-05-15T10:30:00Z").build())
                        .build());

        assertEquals("ev1", ev.getId());
        verify(postRequestedFor(urlEqualTo("/api/integrations/google_calendar/create-event")));
    }

    @Test
    void getEvent_sendsParams() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_calendar/get-event"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"ev1\"}")));

        leash.integrations().calendar().getEvent("ev1", "primary");

        verify(postRequestedFor(urlEqualTo("/api/integrations/google_calendar/get-event"))
                .withRequestBody(equalToJson("{\"eventId\":\"ev1\",\"calendarId\":\"primary\"}")));
    }

    @Test
    void googleCalendar_aliasReturnsSameInstance() {
        assertSame(leash.integrations().calendar(), leash.integrations().googleCalendar());
    }
}
