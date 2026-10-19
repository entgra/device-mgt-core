# Notification Management — Architecture

## Purpose

Describe how notification-mgt is packaged, how notifications are configured,
persisted, published, read/archived, and pushed live to the console via SSE.

## Bundle layout

| Module | Artifact | Role |
| --- | --- | --- |
| common | `io.entgra.device.mgt.core.notification.mgt.common` | Beans, DTOs, service interfaces, exceptions |
| core | `io.entgra.device.mgt.core.notification.mgt.core` | OSGi component, service impls, DAOs, broker, archival task |
| api | `io.entgra.device.mgt.core.notification.mgt.api` | JAX-RS WAR `api#notification-mgt#v1.0` |

**Registered OSGi services** (on core activate):

- `NotificationManagementService` → `NotificationManagementServiceImpl`
- `NotificationConfigService` → `NotificationConfigServiceImpl`

**Mandatory OSGi references:** `DeviceManagementProviderService`,
`MetadataManagementService`, `RealmService`, `TaskService`,
`DeviceFeatureOperations`.

**Not OSGi-published:** `NotificationArchivalService` (created inside
`NotificationArchivalTask`).

**SSE lives outside this module** in
`ui-request-interceptor` (`SSEHandler` → `/ConnectSSE`), and listens to
`NotificationEventBroker` from notification-mgt core. See [sse.md](sse.md).

## High-level component view

```text
┌──────────────────────────────────────────────────────────────────────┐
│ Console / Endpoint Manager UI                                        │
│  REST: /api/notification-mgt/v1.0/...                                │
│  SSE:  /entgra-ui-request-handler/ConnectSSE                         │
└───────────────┬───────────────────────────────┬──────────────────────┘
                │                               │
                ▼                               ▼
┌───────────────────────────┐     ┌─────────────────────────────────────┐
│ notification-mgt.api WAR  │     │ ui-request-interceptor SSEHandler   │
│ NotificationService       │     │  implements NotificationListener    │
│ NotificationConfiguration │     │  userStreams: user → AsyncContexts  │
│ Service                   │     └──────────────────▲──────────────────┘
└─────────────┬─────────────┘                        │
              │                                      │ onMessage()
              ▼                                      │
┌────────────────────────────────────────────────────┴─────────────────┐
│ notification-mgt.core                                                │
│  NotificationConfigService  ←→ Metadata (key: notification-config)   │
│  NotificationManagementService                                       │
│    ├─ publish (operation / task) → DAO → EventBroker.pushMessage     │
│    └─ read / mark / delete / user-archive → DAO (+ unread push)      │
│  NotificationArchivalTask (ntask cron) → NotificationArchivalService │
└───────────────┬──────────────────────────────────┬───────────────────┘
                │                                  │
                ▼                                  ▼
        CDM / source DB                      Archival dest DB
        DM_NOTIFICATION*                     DM_NOTIFICATION*_ARCH
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

On activate / new tenant: archive defaults are ensured; task default configs
are seeded (see device-mgt
[task-notification-configs.md](../../device-mgt/Specs/task-notification-configs.md)).

## End-to-end flows

### 1. Operation notification (device operation lifecycle)

1. Device-mgt operation path calls
   `NotificationManagementService.handleOperationNotificationIfApplicable(...)`
   with `operationCode`, `operationStatus`, `deviceType`, `deviceIds`,
   `tenantId`, and trigger (`immediate` or `postSync`).
2. Service loads config by code; skips if missing, disabled, wrong device
   type, trigger not listed, or critical-status filter fails.
3. If batch notifications are enabled →
   `handleBatchOperationNotificationIfApplicable`; else per publish.
4. `publishNotification`:
   - Insert `DM_NOTIFICATION` (`TYPE` from config, usually `operation`)
   - Insert `DM_NOTIFICATION_DEVICE` mappings
   - Resolve recipients → insert `DM_NOTIFICATION_USER_ACTION` (`IS_READ=false`)
   - Render description → `NotificationEventBroker.pushMessage` per user
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
2. API impl uses authenticated user (`NotificationHelper.getAuthenticatedUser()`);
   body `username` fields are ignored for authorization.
3. Service updates `DM_NOTIFICATION_USER_ACTION` (or deletes / moves to arch).
4. After mark / delete / archive that changes unread state, service pushes
   unread-count-only payload via the broker.

### 4. Scheduled archival (tenant-wide)

1. On activate, `NotificationArchivalTaskManagerImpl` registers ntask
   `NOTIFICATION_ARCHIVAL_TASK_<tenantId>` with cron `0 0 2 * * ?`.
2. Task runs `archiveOldNotifications` then
   `deleteExpiredArchivedNotifications`.
3. Rows move source → dest archival tables per config / default cutoffs.
   Details: [data-model.md](data-model.md), [services.md](services.md).

### 5. Device purge

When devices are removed from the system,
`purgeNotificationsForDevices(deviceIds, tenantId)` deletes
`DM_NOTIFICATION_DEVICE` rows for those devices and removes orphan
`DM_NOTIFICATION` rows that no longer reference any device.
Task notifications (no device mappings) are unaffected.

## Persistence summary

| Concern | Store |
| --- | --- |
| Config + archive defaults | Metadata `notification-config` |
| Active notifications | `DM_NOTIFICATION`, `DM_NOTIFICATION_DEVICE`, `DM_NOTIFICATION_USER_ACTION` |
| Archived | `DM_NOTIFICATION_ARCH`, `DM_NOTIFICATION_USER_ACTION_ARCH` |
| Feature catalog (task/operation codes) | `DM_FEATURE_DETAILS` (device-mgt) |

## Real-time path (SSE)

Publish and unread-count updates never write HTTP responses from the REST
WAR. They call `NotificationEventBroker.pushMessage(json, usernames)`.
`SSEHandler` is registered as a `NotificationListener` at servlet init and
fans out to in-memory `AsyncContext` streams keyed by username.

Full SSE contract: [sse.md](sse.md).

## Acceptance criteria

- [ ] Operation and task publish paths persist rows and push SSE only when an enabled matching config exists.
- [ ] Config CRUD goes through metadata key `notification-config` for the current tenant.
- [ ] User mark/delete/archive updates user-action rows and refreshes unread count over SSE when applicable.
- [ ] Nightly archival task moves aged rows to dest DB and deletes expired archives.
- [ ] Open `/ConnectSSE` with a valid UI session receives an initial connected payload including unread count.
