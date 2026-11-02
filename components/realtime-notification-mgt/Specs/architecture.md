# Realtime Notification Management — Architecture

## Purpose

Describe how realtime-notification-mgt is packaged, how notifications are configured,
persisted, published, read/archived, and pushed live to the console via SSE.

## Bundle layout

| Module | Artifact | Role |
| --- | --- | --- |
| common | `io.entgra.device.mgt.core.realtime.notification.mgt.common` | Beans, DTOs, service interfaces, exceptions |
| core | `io.entgra.device.mgt.core.realtime.notification.mgt.core` | OSGi component, service impls, DAOs, broker, archival task |
| api | `io.entgra.device.mgt.core.realtime.notification.mgt.api` | JAX-RS WAR `api#realtime-notification-mgt#v1.0` |

**Registered OSGi services** (on core activate):

- `RealtimeNotificationManagementService` → `RealtimeNotificationManagementServiceImpl`
- `RealtimeNotificationConfigService` → `RealtimeNotificationConfigServiceImpl`

**Mandatory OSGi references:** `DeviceManagementProviderService`,
`MetadataManagementService`, `RealmService`, `TaskService`,
`DeviceFeatureOperations`.

**Not OSGi-published:** `RealtimeNotificationArchivalService` (created inside
`RealtimeNotificationArchivalTask`).

**SSE lives outside this module** in
`ui-request-interceptor` (`SSEHandler` → `/ConnectSSE`), and listens to
`RealtimeNotificationEventBroker` from realtime-notification-mgt core. See [sse.md](sse.md).

## High-level component view

```text
┌──────────────────────────────────────────────────────────────────────┐
│ Console / Endpoint Manager UI                                        │
│  REST: /api/realtime-notification-mgt/v1.0/...                                │
│  SSE:  /entgra-ui-request-handler/ConnectSSE                         │
└───────────────┬───────────────────────────────┬──────────────────────┘
                │                               │
                ▼                               ▼
┌───────────────────────────┐     ┌─────────────────────────────────────┐
│ realtime-notification-mgt.api WAR  │     │ ui-request-interceptor SSEHandler   │
│ RealtimeNotificationService       │     │  implements RealtimeNotificationListener    │
│ NotificationConfiguration │     │  userStreams: user → AsyncContexts  │
│ Service                   │     └──────────────────▲──────────────────┘
└─────────────┬─────────────┘                        │
              │                                      │ onMessage()
              ▼                                      │
┌────────────────────────────────────────────────────┴─────────────────┐
│ realtime-notification-mgt.core                                                │
│  RealtimeNotificationConfigService  ←→ Metadata (key: notification-config)   │
│  RealtimeNotificationManagementService                                       │
│    ├─ publish (operation / task) → DAO → EventBroker.pushMessage     │
│    └─ read / mark / delete / user-archive → DAO (+ unread push)      │
│  RealtimeNotificationArchivalTask (ntask cron) → RealtimeNotificationArchivalService │
└───────────────┬──────────────────────────────────┬───────────────────┘
                │                                  │
                ▼                                  ▼
        CDM / source DB                      Archival dest DB
        DM_REALTIME_NOTIFICATION*                     DM_REALTIME_NOTIFICATION*_ARCH
```

## Configuration model

Tenant notification configuration is stored as **metadata**, not in a
dedicated config table:

| Item | Value |
| --- | --- |
| Metadata key | `notification-config` |
| Default archive type | `DB` |
| Default archive after | `{ value: 12, unit: "months" }` |
| Archive delete retention | `{ value: 5, unit: "years" }` |

Each config entry includes: id, name, code, type (`operation` | `task`),
deviceType, enabled, description, recipients, trigger points (operations),
critical criteria, batch settings, and per-config archive settings.

On activate: archival task starts. New tenants get archive defaults and
default task configs (`ensureDefaultTaskNotificationConfigs`) from tenant
creation; existing tenants use migration SQL (see device-mgt
[task-notification-configs.md](../../device-mgt/Specs/task-notification-configs.md)).

## End-to-end flows

### 1. Operation notification (device operation lifecycle)

1. Device-mgt operation path calls
   `RealtimeNotificationManagementService.handleOperationNotificationIfApplicable(...)`
   with `operationCode`, `operationStatus`, `deviceType`, `deviceIds`,
   `tenantId`, and trigger (`immediate` or `postSync`).
2. Service loads config by code; skips if missing, disabled, wrong device
   type, trigger not listed, or critical-status filter fails.
3. If batch notifications are enabled →
   `handleBatchOperationNotificationIfApplicable`; else per publish.
4. `publishNotification`:
   - Resolve `deviceLabels` only when `includeDeviceList`; omit if JSON would
     exceed `VARCHAR(1000)`
   - Insert `DM_REALTIME_NOTIFICATION` (`TYPE` from config, usually `operation`)
   - Insert `DM_REALTIME_NOTIFICATION_DEVICE` mappings
   - Resolve recipients → insert `DM_REALTIME_NOTIFICATION_USER_ACTION` (`IS_READ=false`)
   - Render description from context labels → `RealtimeNotificationEventBroker.pushMessage` per user
5. `SSEHandler.onMessage` writes `data: {...}\n\n` to that user’s open streams.

**Primary callers:** `OperationManagerImpl` (`persistsOperation` → immediate;
`updateOperation` / `updateOperationStatuses` → postSync),
`DeviceManagementStartupHandler.operationStatusChangeObserver` → postSync.

### 2. Task notification (console / platform tasks)

1. Caller builds a human-readable message and calls
   `handleTaskNotificationIfApplicable(tenantId, taskCode, message)`.
2. Requires an **enabled** config with `type=task` for that code; otherwise
   skip (debug log).
3. Insert notification (`TYPE=task`) + user actions only (no device rows).
4. Push rendered message + unread count via the event broker → SSE.

**Primary callers / codes:** see [services.md](services.md#task-publish-callers).

### 3. Console list / mark / delete / archive (user)

1. UI calls REST under `/notifications` (scopes in [api-contract.md](api-contract.md)).
2. API impl uses authenticated user (`RealtimeNotificationHelper.getAuthenticatedUser()`);
   body `username` fields are ignored for authorization.
3. Service updates `DM_REALTIME_NOTIFICATION_USER_ACTION` (or deletes / moves to arch).
4. After mark / delete / archive that changes unread state, service pushes
   unread-count-only payload via the broker.

### 4. Scheduled archival (tenant-wide)

1. On activate, `RealtimeNotificationArchivalTaskManagerImpl` registers ntask
   `NOTIFICATION_ARCHIVAL_TASK_<tenantId>` with cron `0 0 2 * * ?`.
2. Task runs `archiveOldNotifications` then
   `deleteExpiredArchivedNotifications`.
3. Rows move source → dest archival tables per config / default cutoffs.
   Details: [data-model.md](data-model.md), [services.md](services.md).

### 5. Device purge

When devices are removed from the system,
`purgeNotificationsForDevices(deviceIds, tenantId)` deletes
`DM_REALTIME_NOTIFICATION_DEVICE` rows for those devices and removes orphan
`DM_REALTIME_NOTIFICATION` rows that no longer reference any device.
Task notifications (no device mappings) are unaffected.

## Persistence summary

| Concern | Store |
| --- | --- |
| Config + archive defaults | Metadata `notification-config` |
| Active notifications | `DM_REALTIME_NOTIFICATION`, `DM_REALTIME_NOTIFICATION_DEVICE`, `DM_REALTIME_NOTIFICATION_USER_ACTION` |
| Archived | `DM_REALTIME_NOTIFICATION_ARCH`, `DM_REALTIME_NOTIFICATION_USER_ACTION_ARCH` |
| Feature catalog (task/operation codes) | `DM_FEATURE_DETAILS` (device-mgt) |

## Real-time path (SSE)

Publish and unread-count updates never write HTTP responses from the REST
WAR. They call `RealtimeNotificationEventBroker.pushMessage(json, usernames)`.
`SSEHandler` is registered as a `RealtimeNotificationListener` at servlet init and
fans out to in-memory `AsyncContext` streams keyed by username.

Full SSE contract: [sse.md](sse.md).

## Acceptance criteria

- [ ] Operation and task publish paths persist rows and push SSE only when an enabled matching config exists.
- [ ] Config CRUD goes through metadata key `notification-config` for the current tenant.
- [ ] User mark/delete/archive updates user-action rows and refreshes unread count over SSE when applicable.
- [ ] Nightly archival task moves aged rows to dest DB and deletes expired archives.
- [ ] Open `/ConnectSSE` with a valid UI session receives an initial connected payload including unread count.
