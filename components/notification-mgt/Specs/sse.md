# Notification Management — Server-Sent Events (SSE)

## Purpose

Document real-time delivery of notification events to the console UI.
SSE is implemented in **ui-request-interceptor**, not in the
notification-mgt WAR, but it is part of the notification delivery contract.

## Endpoint

| Item | Value |
| --- | --- |
| Servlet | `io.entgra.device.mgt.core.ui.request.interceptor.SSEHandler` |
| Annotation | `@WebServlet(urlPatterns = {"/ConnectSSE"}, asyncSupported = true)` |
| Typical URL | `/entgra-ui-request-handler/ConnectSSE` |
| Method | `GET` |
| Response type | `text/event-stream; charset=UTF-8` |

## Connection flow

```text
Browser (EventSource / fetch SSE)
        │
        ▼ GET /ConnectSSE  (UI session cookie)
SSEHandler.doGet
  ├─ same-origin check (Origin present → must match) else 403
  ├─ HttpSession required else 401
  ├─ Resolve username from AuthData / USERNAME_WITH_DOMAIN else 401
  ├─ startAsync(); register AsyncContext under username key(s)
  └─ write initial payload: message + unreadCount
        │
        ▼
NotificationEventBroker.pushMessage(...)
        │
        ▼
SSEHandler.onMessage → writeSseData to matching user streams
```

### Auth & username keys

Session attributes tried in order:

1. `SESSION_AUTH_DATA_KEY`
2. `SESSION_TENANT_CONTEXT_AUTH_DATA_KEY`
3. `SESSION_DEFAULT_AUTH_DATA_KEY`
4. Fallback: `USERNAME_WITH_DOMAIN`

Username is normalized for notifications (`normalizeUsernameForNotifications`).
The same `AsyncContext` may be registered under **both** the session
username and the normalized form (e.g. `admin` and `admin@carbon.super`)
so publishers using either format still match.

### Headers set on stream

- `Cache-Control: no-store, no-cache, must-revalidate, private`
- `Pragma: no-cache`
- `X-Content-Type-Options: nosniff`
- `X-Accel-Buffering: no` (disable proxy buffering)
- `Content-Type: text/event-stream`

### Timeout & keep-alive

From `DeviceManagementConfig` → `UIRequestConfiguration`:

| Setting | Use |
| --- | --- |
| `sseTimeout` | `AsyncContext.setTimeout` |
| `sseKeepAliveInterval` | Scheduled comment pings `: keep-alive\n\n` |

Failed keep-alive / write errors remove the stream from `userStreams`.

On complete / timeout / error, `AsyncListener` removes the context from all
username keys (a connection can be multi-keyed).

---

## Broker integration

On `init`, `SSEHandler` calls
`NotificationEventBroker.registerListener(this)`.

`NotificationListener.onMessage(String message, List<String> usernames)`:

- For each username, iterate that user’s `AsyncContext` list
- `writeSseData(out, message)`; on IO / `checkError`, close stream

Publishers never talk to HTTP directly; they only call
`NotificationEventBroker.pushMessage`.

---

## Wire format

SSE event body written as:

```text
data: <json>\n\n
```

### Initial connect payload

Built in `doGet` after unread count query:

```json
{
  "message": "Connected to notification service.",
  "unreadCount": 3
}
```

### New notification payload

`NotificationEventPayloadBuilder.buildMessagePayload`:

```json
{
  "message": "<rendered description>",
  "unreadCount": 4
}
```

### Unread-count-only payload

After mark/delete/archive:

```json
{
  "unreadCount": 2
}
```

Keep-alive frames are SSE **comments**, not data events:

```text
: keep-alive

```

---

## In-memory state

| Structure | Meaning |
| --- | --- |
| `userStreams: Map<String, List<AsyncContext>>` | Concurrent map; per-user fan-out |
| Multiple tabs | Multiple contexts under the same username |
| Node scope | **Process-local** — not clustered; sticky sessions / single node assumed for live push |

Unread count on connect is loaded via
`NotificationManagementDAO.getUnreadNotificationCountForUser` using the
notification-mgt DAO factory (source DB).

---

## Failure behaviour

| Case | Behaviour |
| --- | --- |
| No session / no username | **401**, no stream |
| Cross-origin | **403** |
| Unread count DAO failure on connect | Log error; close stream |
| Write failure on push / keep-alive | Remove that `AsyncContext` |
| No open stream for user | Push is a no-op for that user (events not queued) |

Clients should reconnect (standard EventSource behaviour) after disconnect;
they re-fetch unread count on connect. Missed in-flight events while
disconnected are recovered via REST list APIs, not an SSE replay buffer.

---

## Relationship to REST

| Concern | Channel |
| --- | --- |
| Config CRUD, list history, mark, delete, archive | REST `/api/notification-mgt/v1.0` |
| Live toast / badge update | SSE `/ConnectSSE` |
| Authoritative unread count after reconnect | SSE initial payload + REST counts |

---

## Acceptance criteria

- [ ] Authenticated same-origin `GET /ConnectSSE` opens `text/event-stream` and sends initial `{message, unreadCount}`.
- [ ] Publish and unread updates from notification-mgt appear as `data:` JSON on that user’s open streams.
- [ ] Keep-alive comments are sent when `sseKeepAliveInterval > 0`.
- [ ] Disconnect / timeout does not leave orphaned contexts in `userStreams`.
- [ ] Users without an open SSE connection still receive persisted notifications via REST on next load.
