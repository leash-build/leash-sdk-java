# Leash Java SDK

Java SDK for Leash-hosted integrations.

It provides a unified client for calling provider actions through the Leash platform proxy.

## Requirements

- Java 17+
- Maven 3.6+

## Installation

```xml
<dependency>
  <groupId>build.leash</groupId>
  <artifactId>leash-sdk</artifactId>
  <version>0.2.0</version>
</dependency>
```

## Quick Start

```java
import build.leash.sdk.LeashIntegrations;
import build.leash.sdk.types.ListMessagesParams;

LeashIntegrations client = LeashIntegrations.builder()
    .authToken("your-platform-jwt")
    .apiKey("optional-app-api-key")
    .build();

boolean connected = client.isConnected("gmail");
String connectUrl = client.getConnectUrl("gmail", "https://myapp.example.com/settings");

var messages = client.gmail().listMessages(
    ListMessagesParams.builder().maxResults(5).build()
);
```

## Default Platform URL

- `https://leash.build`

## Capabilities

- Gmail
- Google Calendar
- Google Drive
- connection status lookup
- connect URL generation
- generic provider calls
- custom integration calls
- app env fetch and caching
- MCP execution through the platform

## Notes

- pass a valid Leash platform JWT as `authToken`
- use `apiKey` when app-scoped access is needed
- provider OAuth and token storage are handled by the Leash platform, not by the SDK

## License

Apache-2.0
