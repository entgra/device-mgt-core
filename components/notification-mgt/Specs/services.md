# Notification Management — Services & Publish Flows

## Purpose

Document OSGi service contracts, publish/purge/archival behaviour, and
external callers.

## OSGi lifecycle

`NotificationManagementServiceComponent.activate`:

1. Init config / archival config managers and DAO factories.
2. Register `NotificationManagementService`, `NotificationConfigService`.
3. Ensure default archive metadata for carbon.super / active tenants as needed.
4. Refresh feature catalog path used for task defaults; seed default task
   notification configs (`ensureDefaultTaskNotificationConfigs`).
5. Start per-tenant `NotificationArchivalTask` via
   `NotificationArchivalTaskManagerImpl`.

Data holder: `NotificationManagementDataHolder` (device-mgt, metadata,
realm, task service, feature operations).

---

## `NotificationConfigService`

Impl: `NotificationConfigServiceImpl`

| Method | Behaviour |
| --- | --- |
| `setDefaultNotificationArchiveMetadata` | Ensures `defaultArchiveType` / `defaultArchiveAfter` on meta |
| `addNotificationConfigContext` | Append configs; assign ids; persist meta |
| `updateNotificationConfigContext` | Replace one config by id |
| `deleteNotificationConfigContext` | Remove one by id |
| `deleteNotificationConfigurations` | Clear tenant configs |
| `getNotificationConfigurations` | Full list for tenant |
| `getNotificationConfigByID` | Single config |
| `getFilteredNotificationConfigurations` | Filter by name/type/deviceType/code + pagination |
| `configExists` | Exists for `(deviceType, code)` |
| `ensureDefaultTaskNotificationConfigs` | Idempotent seed of default task configs |

All persistence goes through metadata key `notification-config`.

---

## `NotificationManagementService`

Impl: `NotificationManagementServiceImpl`

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
`NotificationEventPayloadBuilder.buildUnreadCountPayload`.

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

1. `insertNotification(tenantId, configId, type, contextJson)`
2. `insertNotificationDeviceMappings`
3. Resolve recipients (`NotificationHelper.extractUsernamesFromRecipients`)
4. `insertNotificationUserActions`
5. Render description; for each user push
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

Default codes / device types also defined in `Constants` /
`DM_FEATURE_DETAILS` catalog (`REMOVE_DEVICE`, `DELETE_DEVICE`,
`DISENROLL_DEVICE` → `ALL`; `ROLE_PERMISSION_UPDATE` → `NONE`).

---

## Archival service & task

### `NotificationArchivalService` (not OSGi-exported)

Impl: `NotificationArchivalServiceImpl`

| Method | Behaviour |
| --- | --- |
| `archiveOldNotifications(tenantId)` | Per-config `archiveType=DB` + `archiveAfter` cutoffs; then default-type leftovers excluding already handled config ids |
| `deleteExpiredArchivedNotifications(tenantId)` | Delete dest rows older than 5 years |

### Task registration

`NotificationArchivalTaskManagerImpl.startTask`:

| Property | Value |
| --- | --- |
| Task type | `NOTIFICATION_ARCHIVAL_TASK_TYPE` |
| Task name | `NOTIFICATION_ARCHIVAL_TASK_<tenantId>` |
| Class | `...task.NotificationArchivalTask` |
| Cron | `0 0 2 * * ?` |
| Property | `tenantId` |

`executeDynamicTask`: archive old → delete expired archives.

Manual user archive (REST) uses `NotificationManagementService` archival
DAO paths and is separate from the nightly age-based job.

---

## Event broker

`NotificationEventBroker` (static, in-process):

- `registerListener(NotificationListener)`
- `pushMessage(String message, List<String> usernames)` →
  `listener.onMessage(...)` for each listener

Primary listener: `SSEHandler` (ui-request-interceptor). See [sse.md](sse.md).

Helpers:

- `NotificationEventPayloadBuilder.buildMessagePayload(message, unreadCount)`
- `NotificationEventPayloadBuilder.buildUnreadCountPayload(unreadCount)`
- `NotificationMessageRenderer` — context JSON ↔ rendered description

---

## Acceptance criteria

- [ ] Disabled or missing configs cause publish methods to no-op without error (warn/debug as coded).
- [ ] Operation publish creates device mappings; task publish does not.
- [ ] Mark/delete/archive refresh unread count over the broker when counts change.
- [ ] Archival ntask runs daily at 02:00 and uses per-config then default cutoffs.
- [ ] Role permission updates go through `APIPublisherService.notifyRolePermissionUpdate` → task notification path (not a direct dependency on a removed service).
