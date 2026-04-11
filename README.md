# Leash Java SDK

Java SDK for the [Leash](https://leash.build) platform integrations API.

Supports Gmail, Google Calendar, and Google Drive integrations through a unified client.

## Requirements

- Java 17+
- Maven 3.6+

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>build.leash</groupId>
    <artifactId>leash-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

```java
import build.leash.sdk.LeashIntegrations;
import build.leash.sdk.LeashError;
import build.leash.sdk.types.*;
import com.google.gson.JsonElement;

// Create the client
LeashIntegrations leash = LeashIntegrations.builder()
    .authToken("your-jwt-token")
    .apiKey("optional-api-key")  // optional
    .build();

// Check connection status
boolean connected = leash.isConnected("gmail");

// List Gmail messages
JsonElement messages = leash.gmail().listMessages(
    ListMessagesParams.builder()
        .maxResults(10)
        .query("from:user@example.com")
        .build()
);

// Send an email
leash.gmail().sendMessage(
    SendMessageParams.builder()
        .to("recipient@example.com")
        .subject("Hello from Leash")
        .body("Sent via the Java SDK!")
        .build()
);

// List calendar events
JsonElement events = leash.calendar().listEvents(
    ListEventsParams.builder()
        .timeMin("2026-04-10T00:00:00Z")
        .timeMax("2026-04-17T00:00:00Z")
        .singleEvents(true)
        .orderBy("startTime")
        .build()
);

// Search Drive files
JsonElement files = leash.drive().searchFiles("quarterly report", 5);
```

## Error Handling

```java
try {
    leash.gmail().listMessages(null);
} catch (LeashError e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("Code: " + e.getCode());

    // If the user needs to connect the integration
    if ("not_connected".equals(e.getCode())) {
        String url = e.getConnectUrl();
        System.out.println("Connect at: " + url);
    }
}
```

## API Reference

### LeashIntegrations

| Method | Description |
|--------|-------------|
| `gmail()` | Returns a `GmailClient` |
| `calendar()` | Returns a `CalendarClient` |
| `drive()` | Returns a `DriveClient` |
| `isConnected(providerId)` | Checks if a provider is connected |
| `getConnections()` | Lists all connection statuses |
| `getConnectUrl(providerId, returnUrl)` | Gets the OAuth connect URL |
| `call(provider, action, body)` | Generic API call for custom actions |

### GmailClient

| Method | Action |
|--------|--------|
| `listMessages(params)` | `list-messages` |
| `getMessage(id, format)` | `get-message` |
| `sendMessage(params)` | `send-message` |
| `searchMessages(query, max)` | `search-messages` |
| `listLabels()` | `list-labels` |

### CalendarClient

| Method | Action |
|--------|--------|
| `listCalendars()` | `list-calendars` |
| `listEvents(params)` | `list-events` |
| `createEvent(params)` | `create-event` |
| `getEvent(id, calendarId)` | `get-event` |

### DriveClient

| Method | Action |
|--------|--------|
| `listFiles(params)` | `list-files` |
| `getFile(fileId)` | `get-file` |
| `searchFiles(query, max)` | `search-files` |

## License

MIT
