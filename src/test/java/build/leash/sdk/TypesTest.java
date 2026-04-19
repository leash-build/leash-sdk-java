package build.leash.sdk;

import build.leash.sdk.types.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for parameter type builders and ApiResponse / ConnectionStatus.
 */
class TypesTest {

    @Nested
    class ListMessagesParamsTest {

        @Test
        void buildWithAllFields() {
            ListMessagesParams params = ListMessagesParams.builder()
                    .query("from:test@example.com")
                    .maxResults(10)
                    .labelIds(List.of("INBOX", "UNREAD"))
                    .pageToken("token123")
                    .build();

            assertEquals("from:test@example.com", params.getQuery());
            assertEquals(10, params.getMaxResults());
            assertEquals(List.of("INBOX", "UNREAD"), params.getLabelIds());
            assertEquals("token123", params.getPageToken());
        }

        @Test
        void buildWithNoFields() {
            ListMessagesParams params = ListMessagesParams.builder().build();
            assertNull(params.getQuery());
            assertNull(params.getMaxResults());
            assertNull(params.getLabelIds());
            assertNull(params.getPageToken());
        }
    }

    @Nested
    class SendMessageParamsTest {

        @Test
        void buildWithRequiredFields() {
            SendMessageParams params = SendMessageParams.builder()
                    .to("user@example.com")
                    .subject("Hello")
                    .body("World")
                    .build();

            assertEquals("user@example.com", params.getTo());
            assertEquals("Hello", params.getSubject());
            assertEquals("World", params.getBody());
            assertNull(params.getCc());
            assertNull(params.getBcc());
        }

        @Test
        void buildWithAllFields() {
            SendMessageParams params = SendMessageParams.builder()
                    .to("user@example.com")
                    .subject("Hello")
                    .body("World")
                    .cc("cc@example.com")
                    .bcc("bcc@example.com")
                    .build();

            assertEquals("cc@example.com", params.getCc());
            assertEquals("bcc@example.com", params.getBcc());
        }

        @Test
        void missingToThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    SendMessageParams.builder()
                            .subject("Hello")
                            .body("World")
                            .build());
        }

        @Test
        void emptyToThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    SendMessageParams.builder()
                            .to("")
                            .subject("Hello")
                            .body("World")
                            .build());
        }

        @Test
        void missingSubjectThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    SendMessageParams.builder()
                            .to("user@example.com")
                            .body("World")
                            .build());
        }

        @Test
        void missingBodyThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    SendMessageParams.builder()
                            .to("user@example.com")
                            .subject("Hello")
                            .build());
        }
    }

    @Nested
    class ListEventsParamsTest {

        @Test
        void buildWithAllFields() {
            ListEventsParams params = ListEventsParams.builder()
                    .calendarId("primary")
                    .timeMin("2024-01-01T00:00:00Z")
                    .timeMax("2024-12-31T23:59:59Z")
                    .maxResults(50)
                    .singleEvents(true)
                    .orderBy("startTime")
                    .build();

            assertEquals("primary", params.getCalendarId());
            assertEquals("2024-01-01T00:00:00Z", params.getTimeMin());
            assertEquals("2024-12-31T23:59:59Z", params.getTimeMax());
            assertEquals(50, params.getMaxResults());
            assertTrue(params.getSingleEvents());
            assertEquals("startTime", params.getOrderBy());
        }

        @Test
        void buildWithDefaults() {
            ListEventsParams params = ListEventsParams.builder().build();
            assertNull(params.getCalendarId());
            assertNull(params.getTimeMin());
            assertNull(params.getMaxResults());
        }
    }

    @Nested
    class CreateEventParamsTest {

        @Test
        void buildWithRequiredFields() {
            CreateEventParams params = CreateEventParams.builder()
                    .summary("Team Meeting")
                    .start("2024-06-15T10:00:00-04:00", null, "America/New_York")
                    .end("2024-06-15T11:00:00-04:00", null, "America/New_York")
                    .build();

            assertEquals("Team Meeting", params.getSummary());
            assertNotNull(params.getStart());
            assertNotNull(params.getEnd());
            assertEquals("2024-06-15T10:00:00-04:00", params.getStart().get("dateTime"));
            assertEquals("America/New_York", params.getStart().get("timeZone"));
        }

        @Test
        void buildAllDayEvent() {
            CreateEventParams params = CreateEventParams.builder()
                    .summary("Vacation")
                    .start(null, "2024-06-15", null)
                    .end(null, "2024-06-16", null)
                    .build();

            assertEquals("2024-06-15", params.getStart().get("date"));
            assertFalse(params.getStart().containsKey("dateTime"));
        }

        @Test
        void buildWithAttendees() {
            CreateEventParams params = CreateEventParams.builder()
                    .summary("Meeting")
                    .start("2024-06-15T10:00:00Z", null, null)
                    .end("2024-06-15T11:00:00Z", null, null)
                    .attendees(List.of("a@test.com", "b@test.com"))
                    .build();

            List<Map<String, String>> attendees = params.getAttendees();
            assertEquals(2, attendees.size());
            assertEquals("a@test.com", attendees.get(0).get("email"));
            assertEquals("b@test.com", attendees.get(1).get("email"));
        }

        @Test
        void buildWithOptionalFields() {
            CreateEventParams params = CreateEventParams.builder()
                    .summary("Meeting")
                    .description("Weekly sync")
                    .location("Room 42")
                    .calendarId("work")
                    .start("2024-06-15T10:00:00Z", null, null)
                    .end("2024-06-15T11:00:00Z", null, null)
                    .build();

            assertEquals("Weekly sync", params.getDescription());
            assertEquals("Room 42", params.getLocation());
            assertEquals("work", params.getCalendarId());
        }

        @Test
        void missingSummaryThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    CreateEventParams.builder()
                            .start("2024-06-15T10:00:00Z", null, null)
                            .end("2024-06-15T11:00:00Z", null, null)
                            .build());
        }

        @Test
        void missingStartThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    CreateEventParams.builder()
                            .summary("Meeting")
                            .end("2024-06-15T11:00:00Z", null, null)
                            .build());
        }

        @Test
        void missingEndThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    CreateEventParams.builder()
                            .summary("Meeting")
                            .start("2024-06-15T10:00:00Z", null, null)
                            .build());
        }
    }

    @Nested
    class ListFilesParamsTest {

        @Test
        void buildWithAllFields() {
            ListFilesParams params = ListFilesParams.builder()
                    .query("type:spreadsheet")
                    .maxResults(25)
                    .folderId("folder123")
                    .build();

            assertEquals("type:spreadsheet", params.getQuery());
            assertEquals(25, params.getMaxResults());
            assertEquals("folder123", params.getFolderId());
        }

        @Test
        void buildWithDefaults() {
            ListFilesParams params = ListFilesParams.builder().build();
            assertNull(params.getQuery());
            assertNull(params.getMaxResults());
            assertNull(params.getFolderId());
        }
    }

    @Nested
    class ApiResponseTest {

        @Test
        void deserializesSuccessResponse() {
            String json = """
                    {"success":true,"data":{"key":"value"},"error":null,"code":null,"connectUrl":null}
                    """;
            com.google.gson.Gson gson = new com.google.gson.Gson();
            ApiResponse resp = gson.fromJson(json, ApiResponse.class);

            assertTrue(resp.isSuccess());
            assertNotNull(resp.getData());
            assertNull(resp.getError());
            assertNull(resp.getCode());
            assertNull(resp.getConnectUrl());
        }

        @Test
        void deserializesErrorResponse() {
            String json = """
                    {"success":false,"data":null,"error":"Not connected","code":"not_connected","connectUrl":"https://leash.build/connect"}
                    """;
            com.google.gson.Gson gson = new com.google.gson.Gson();
            ApiResponse resp = gson.fromJson(json, ApiResponse.class);

            assertFalse(resp.isSuccess());
            // Gson deserializes explicit JSON null as JsonNull, not Java null
            assertTrue(resp.getData() == null || resp.getData().isJsonNull());
            assertEquals("Not connected", resp.getError());
            assertEquals("not_connected", resp.getCode());
            assertEquals("https://leash.build/connect", resp.getConnectUrl());
        }
    }

    @Nested
    class ConnectionStatusTest {

        @Test
        void deserializesConnectionStatus() {
            String json = """
                    {"providerId":"gmail","status":"active","email":"user@gmail.com","expiresAt":"2024-12-31T00:00:00Z"}
                    """;
            com.google.gson.Gson gson = new com.google.gson.Gson();
            ConnectionStatus status = gson.fromJson(json, ConnectionStatus.class);

            assertEquals("gmail", status.getProviderId());
            assertEquals("active", status.getStatus());
            assertEquals("user@gmail.com", status.getEmail());
            assertEquals("2024-12-31T00:00:00Z", status.getExpiresAt());
        }

        @Test
        void deserializesWithNullFields() {
            String json = """
                    {"providerId":"gmail","status":"inactive"}
                    """;
            com.google.gson.Gson gson = new com.google.gson.Gson();
            ConnectionStatus status = gson.fromJson(json, ConnectionStatus.class);

            assertEquals("gmail", status.getProviderId());
            assertEquals("inactive", status.getStatus());
            assertNull(status.getEmail());
            assertNull(status.getExpiresAt());
        }
    }
}
