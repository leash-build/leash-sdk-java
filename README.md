# Leash Java SDK

Java SDK for the Leash platform. Unified `Leash` client with auth, env, and
typed integrations (Gmail, Calendar, Drive, Linear) — plus a generic
provider escape hatch.

## Requirements

- Java 11+ (the SDK uses `java.net.http`)
- Maven 3.6+

## Installation

```xml
<dependency>
  <groupId>build.leash</groupId>
  <artifactId>leash-sdk</artifactId>
  <version>0.4.0</version>
</dependency>
```

If you want the `Leash(HttpServletRequest)` constructor, you also need a
Servlet API on the classpath — Spring Boot, Jakarta EE, Jetty, Tomcat, and
Quarkus all ship it, so you usually don't have to add it yourself.

## Quick Start — Spring Boot

```java
import build.leash.Leash;
import build.leash.LeashUser;
import build.leash.integrations.gmail.GmailMessageList;
import build.leash.integrations.gmail.GmailListParams;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class InboxController {

    @GetMapping("/inbox")
    public GmailMessageList inbox(HttpServletRequest request) {
        Leash leash = new Leash(request);

        Optional<LeashUser> user = leash.auth().user();
        if (user.isEmpty()) throw new RuntimeException("Not signed in");

        return leash.integrations().gmail().listMessages(
            GmailListParams.builder().maxResults(5).build()
        );
    }
}
```

## Quick Start — plain Servlet / Jetty / Tomcat

```java
import build.leash.Leash;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class WhoAmIServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Leash leash = new Leash(req);
        resp.setContentType("application/json");
        leash.auth().user().ifPresentOrElse(
            u -> resp.getWriter().write("{\"id\":\"" + u.getId() + "\"}"),
            () -> resp.setStatus(401)
        );
    }
}
```

## Quick Start — non-Servlet (Javalin / Spark / Lambda)

```java
import build.leash.Leash;
import java.util.Map;

Map<String, String> headers = Map.of(
    "cookie", "leash-auth=" + jwt,
    "authorization", "Bearer " + apiKey
);

Leash leash = new Leash(headers, Map.of());
```

## Quick Start — server-to-server

```java
import build.leash.Leash;

Leash leash = Leash.fromApiKey(System.getenv("LEASH_API_KEY"));
String openAiKey = leash.env().get("OPENAI_API_KEY").orElseThrow();
```

## Surface

### Auth

```java
Optional<LeashUser> user = leash.auth().user();
boolean signedIn = leash.auth().isAuthenticated();
```

### Env

```java
Optional<String> key = leash.env().get("OPENAI_API_KEY");
Optional<String> fresh = leash.env().get("STRIPE_SECRET_KEY", EnvOptions.fresh());
Map<String, Optional<String>> many = leash.env().getMany(List.of("A", "B"));
```

Returns `Optional.empty()` when the platform reports the key as not
declared (HTTP 404). All other failures throw `LeashException` (or one of
its subclasses — `PlanBlockException`, `UnauthorizedException`, etc.).

Values are cached per-`Leash`-instance for 60 seconds. Pass
`EnvOptions.fresh()` to bypass the cache for one call.

### Integrations — Gmail (6 verbs)

```java
leash.integrations().gmail().listMessages(GmailListParams.builder().maxResults(5).build());
leash.integrations().gmail().getMessage(messageId);
leash.integrations().gmail().sendMessage(
    GmailSendMessageParams.builder().to("a@b.com").subject("Hi").body("hello").build()
);
leash.integrations().gmail().searchMessages("from:me");
leash.integrations().gmail().listLabels();
leash.integrations().gmail().getProfile();
```

### Integrations — Calendar (4 verbs)

```java
leash.integrations().calendar().listCalendars();
leash.integrations().calendar().listEvents(
    CalendarListEventsParams.builder().maxResults(10).build()
);
leash.integrations().calendar().createEvent(
    CalendarCreateEventParams.builder()
        .summary("Standup")
        .start(CalendarEventDateTime.builder().dateTime("2026-05-15T10:00:00Z").build())
        .end(CalendarEventDateTime.builder().dateTime("2026-05-15T10:30:00Z").build())
        .build()
);
leash.integrations().calendar().getEvent(eventId);

// alias for the long-form provider id
leash.integrations().googleCalendar().listCalendars();
```

### Integrations — Drive (7 verbs)

```java
leash.integrations().drive().listFiles();
leash.integrations().drive().getFile(fileId);
leash.integrations().drive().downloadFile(fileId);
leash.integrations().drive().createFolder("My Folder");
leash.integrations().drive().uploadFile(
    DriveUploadFileParams.builder().name("a.txt").content("hi").mimeType("text/plain").build()
);
leash.integrations().drive().deleteFile(fileId);
leash.integrations().drive().searchFiles("name contains 'report'");

// alias for the long-form provider id
leash.integrations().googleDrive().listFiles();
```

### Integrations — Linear (7 verbs)

```java
leash.integrations().linear().listIssues(
    LinearListIssuesFilter.builder().stateType(LinearStateType.STARTED).build()
);
leash.integrations().linear().getIssue(id);
leash.integrations().linear().createIssue(
    LinearCreateIssueInput.builder().teamId("...").title("Bug: ...").build()
);
leash.integrations().linear().updateIssue(id,
    LinearUpdateIssuePatch.builder().assigneeId("...").build()
);
leash.integrations().linear().addComment(issueId, "ack");
leash.integrations().linear().listTeams();
leash.integrations().linear().listProjects();
```

### Generic provider escape hatch

```java
JsonNode result = leash.integrations().provider("slack").call(
    "post_message",
    Map.of("channel", "#general", "text", "hi")
);
```

## Auth precedence

The SDK looks for credentials in this order (matches the TS / Python / Go
SDKs exactly):

1. `LEASH_API_KEY` env var — sent as `X-API-Key` on integration POSTs and
   as `Authorization: Bearer` on env-fetch.
2. `Authorization: Bearer <jwt>` header on the inbound request — used
   **only** as the env-fetch bearer when no API key is configured.
   **Never** forwarded on integration POSTs (the platform's `verifyToken`
   can reject a user JWT before the API-key check runs).
3. `leash-auth` cookie — forwarded to the platform as
   `Cookie: leash-auth=<value>` on integration POSTs.

## Errors

All errors extend `LeashException` (unchecked — checked exceptions in
builder chains are ergonomic poison). The base carries a stable `code`,
optional `action`, `seeAlso`, and HTTP `status`.

| Code                         | Subclass                       | When                                           |
|------------------------------|--------------------------------|-----------------------------------------------|
| `UPGRADE_REQUIRED`           | `PlanBlockException`           | HTTP 402 from the platform                    |
| `INTEGRATION_NOT_ENABLED`    | `ConnectionRequiredException`  | HTTP 403 from the platform                    |
| `UNAUTHORIZED`               | `UnauthorizedException`        | HTTP 401 from the platform                    |
| `KEY_NOT_DECLARED`           | `KeyNotDeclaredException`      | env-var not declared (lower-level surface)    |
| `NETWORK_ERROR`              | `NetworkException`             | DNS / refused / TLS / I/O                     |

```java
try {
    leash.integrations().linear().createIssue(input);
} catch (PlanBlockException upgrade) {
    // surface an upgrade CTA
} catch (LeashException e) {
    log.error("Leash call failed: code={} status={}", e.getCode(), e.getStatus());
}
```

## What's NOT in 0.4 yet

The following are deferred from the unified client:

- Local-dev cookie exchange flow (`Leash.createDevAuthHandler()` in TS) —
  no Java equivalent yet; use the production cookie path.
- React-style hooks — Java app code is server-rendered, so this isn't on
  the roadmap.
- Streaming responses from integrations — the platform contract is
  request/response JSON; streaming will land alongside the Realtime API.
- Connection-status REST endpoints (`isConnected`, `getConnections`,
  `getConnectUrl`, `getAccessToken`, `getCustomMcpConfig`, `runMcp`) — the
  0.3 surface that the TS, Python, and Go 0.4 SDKs also dropped. File an
  issue if you depend on these.

## License

Apache-2.0
