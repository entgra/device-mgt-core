# Realtime Notification Management — Services & Publish Flows

## Purpose

Document OSGi service contracts, publish/purge/archival behaviour, and
external callers.

## OSGi lifecycle

`RealtimeNotificationManagementServiceComponent.activate`:

1. Init config / archival config managers and DAO factories.
2. Register `RealtimeNotificationManagementService`, `RealtimeNotificationConfigService`.
3. Start per-tenant `RealtimeNotificationArchivalTask` via
   `RealtimeNotificationArchivalTaskManagerImpl`.

New tenants (`TenantManagerImpl.addDefaultNotificationArchivalMetadata`):

1. `setDefaultNotificationArchiveMetadata`
2. `ensureDefaultTaskNotificationConfigs` — seed task configs from catalog
   with recipient `admin`

Existing tenants: use migration SQL under
`product-uem/.../cdm-migration-mysql.sql` (no activate-time all-tenant backfill).

Data holder: `RealtimeNotificationManagementDataHolder` (device-mgt, metadata,
realm, task service, feature operations).

---

## `RealtimeNotificationConfigService`

Impl: `RealtimeNotificationConfigServiceImpl`

| Method | Behaviour |
| --- | --- |
| `setDefaultNotificationArchiveMetadata` | Ensures `defaultArchiveType` / `defaultArchiveAfter` on meta |
| `ensureDefaultTaskNotificationConfigs` | Idempotent seed of missing task configs from catalog (`admin` recipient) |
| `addNotificationConfigContext` | Append configs; assign ids; persist meta |
| `updateNotificationConfigContext` | Replace one config by id |
| `deleteNotificationConfigContext` | Remove one by id |
| `deleteNotificationConfigurations` | Clear tenant configs |
| `getNotificationConfigurations` | Full list for tenant |
| `getNotificationConfigByID` | Single config |
| `getFilteredNotificationConfigurations` | Filter by name/type/deviceType/code + pagination |
| `configExists` | Exists for `(deviceType, code)` |

All persistence goes through metadata key `notification-config`.

---

## `RealtimeNotificationManagementService`

Impl: `RealtimeNotificationManagementServiceImpl`

### Config availability

| Method | Behaviour |
| --- | --- |
| `isNotificationConfigAvailable(code)` | `true` when tenant metadata has an **enabled** config matching the operation/task code (case-insensitive). |

### Inbox / user actions

| Method | Persistence | SSE |
| --- | --- | --- |
| `getAllNotifications` | Select tenant notifications | — |
| `getUserNotificationsWithStatus` | Join user actions + filter `isRead` | — |
| `getUserNotificationCount` | Count user actions | — |
| `updateNotificationActionForUser` | Update `IS_READ` for ids | unread-count payload |
| `updateAllNotificationActionForUser` | Update all for user | unread-count payload |
| `deleteUserNotifications` | Delete user-action rows; return `{deleted,invalid}` | unread-count when changed |
| `deleteAllUserNotifications` | Delete all (optional read filter) | unread-count |
| `archiveUserNotifications` | Move to arch; `{archived,invalid}` | unread-count |
| `archiveAllUserNotifications` | Move all for user to arch | unread-count |

Unread push helper: `pushUnreadCount` →
`RealtimeNotificationEventPayloadBuilder.buildUnreadCountPayload`.

### Publish

#### Operation — `handleOperationNotificationIfApplicable`

Guards: config by code exists; enabled; device type matches; trigger point
in `notificationTriggerPoints`; optional critical status criteria.

Then either:

- **Batch** (`batchNotifications.enabled`):
  `handleBatchOperationNotificationIfApplicable` — one notification for
  many devices
- **Default**: `publishNotification` for the device id list

`publishNotification`:

1. Resolve `deviceLabels` only when `includeDeviceList` is true; omit them if
   context JSON would exceed `VARCHAR(1000)` (`NOTIFICATION_CONTEXT`)
2. `insertNotification(tenantId, configId, type, contextJson)`
3. `insertNotificationDeviceMappings`
4. Resolve recipients (`RealtimeNotificationHelper.extractUsernamesFromRecipients`)
5. `insertNotificationUserActions`
6. Render description from context `deviceLabels`; for each user push
   `buildMessagePayload(description, unreadCount)`

Context built via `buildOperationContext` (operation fields, not free-form
message).

#### Task — `handleTaskNotificationIfApplicable`

Guards: non-blank task code; config exists; `enabled`; `type == task`.

Then: insert notification with `TYPE=task` and context `{ message }`;
user actions only; push message payload per recipient. No device mappings.

#### Purge — `purgeNotificationsForDevices`

Removes device mappings for given `DM_DEVICE.ID`s; deletes notifications
that no longer reference any device. Task rows without mappings remain.

Called from device-mgt **service** layer on permanent device delete
(`DeviceManagementProviderServiceImpl`, `PrivacyComplianceProviderImpl`).
Device-mgt DAOs must **not** touch `DM_REALTIME_*` tables.

#### Tenant delete — `deleteNotificationsByTenantId`

Deletes all `DM_REALTIME_NOTIFICATION` rows for a tenant (mappings /
user actions CASCADE). Called from tenant-mgt / device-mgt tenant cleanup.

---

## Operation publish callers

| Class | Method | Trigger |
| --- | --- | --- |
| `OperationManagerImpl` | `persistsOperation` | `immediate` |
| `OperationManagerImpl` | `updateOperation(...)` | `postSync` |
| `OperationManagerImpl` | `updateOperationStatuses` | `postSync` |
| `DeviceManagementStartupHandler` | `operationStatusChangeObserver` | `postSync` |

---

## Task publish callers

<a id="task-publish-callers"></a>

| Class | Method | Task code |
| --- | --- | --- |
| `DeviceManagerUtil` | `publishDeviceNotification` | passthrough |
| `DeviceManagementProviderServiceImpl` | `disenrollDevice` | `DISENROLL_DEVICE` |
| `DeviceManagementProviderServiceImpl` | `removeDevice` | `REMOVE_DEVICE` |
| `DeviceManagementProviderServiceImpl` | `deleteDevices` | `DELETE_DEVICE` |
| `PrivacyComplianceProviderImpl` | `deleteDevicesOfUser`, `deleteDeviceDetails` | `DELETE_DEVICE` |
| `APIPublisherServiceImpl` | `notifyRolePermissionUpdate` | `ROLE_PERMISSION_UPDATE` |

Default codes / device types also defined in `RealtimeConstants` /
`DM_FEATURE_DETAILS` catalog (`REMOVE_DEVICE`, `DELETE_DEVICE`,
`DISENROLL_DEVICE` → `ALL`; `ROLE_PERMISSION_UPDATE` → `NONE`).

---

## Archival service & task

### `RealtimeNotificationArchivalService` (not OSGi-exported)

Impl: `RealtimeNotificationArchivalServiceImpl`

| Method | Behaviour |
| --- | --- |
| `archiveOldNotifications(tenantId)` | Per-config `archiveType=DB` + `archiveAfter` cutoffs; then default-type leftovers excluding already handled config ids |
| `deleteExpiredArchivedNotifications(tenantId)` | Delete dest rows older than 5 years |

### Task registration

`RealtimeNotificationArchivalTaskManagerImpl.startTask`:

| Property | Value |
| --- | --- |
| Task type | `NOTIFICATION_ARCHIVAL_TASK_TYPE` |
| Task name | `NOTIFICATION_ARCHIVAL_TASK_<tenantId>` |
| Class | `...task.RealtimeNotificationArchivalTask` |
| Cron | `0 0 2 * * ?` |
| Property | `tenantId` |

`executeDynamicTask`: archive old → delete expired archives.

Manual user archive (REST) uses `RealtimeNotificationManagementService` archival
DAO paths and is separate from the nightly age-based job.

---

## Event broker

`RealtimeNotificationEventBroker` (static, in-process):

- `registerListener(RealtimeNotificationListener)`
- `pushMessage(String message, List<String> usernames)` →
  `listener.onMessage(...)` for each listener

Primary listener: `SSEHandler` (ui-request-interceptor). See [sse.md](sse.md).

Helpers:

- `RealtimeNotificationEventPayloadBuilder.buildMessagePayload(message, unreadCount)`
- `RealtimeNotificationEventPayloadBuilder.buildUnreadCountPayload(unreadCount)`
- `RealtimeNotificationMessageRenderer` — context JSON ↔ rendered description

---

## Acceptance criteria

- [ ] Disabled or missing configs cause publish methods to no-op without error (warn/debug as coded).
- [ ] Operation publish creates device mappings; task publish does not.
- [ ] Mark/delete/archive refresh unread count over the broker when counts change.
- [ ] Archival ntask runs daily at 02:00 and uses per-config then default cutoffs.
- [ ] Role permission updates go through `APIPublisherService.notifyRolePermissionUpdate` → task notification path (not a direct dependency on a removed service).
