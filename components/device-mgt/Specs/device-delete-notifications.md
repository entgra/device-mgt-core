# Console Device Delete / Disenroll Notifications

## Purpose

Admin notifications must indicate success or failure when devices are
disenrolled, soft-removed, or permanently deleted from the console.

## Product Rule

Console flows use:

`NotificationManagementService.handleTaskNotificationIfApplicable(tenantId, message)`

- Recipients: Carbon `admin` role users
- Usernames: tenant-aware (`admin@test.com`), from thread-local Carbon tenant domain
  (`getTenantDomain`) so SSE (`ConnectSSE` / `NotificationEventBroker`) matches the UI session.
  Do not use `TenantManager` here — this OSGi bundle does not import that class.
- Notification publish failure is logged only; it must not fail the device operation

## Source Files

| Area | Path |
| --- | --- |
| Disenroll / remove / permanent delete | `.../DeviceManagementProviderServiceImpl.java` |
| Publish helper | `.../util/DeviceManagerUtil.publishDeviceNotification` |
| Tenant-aware username + SSE | `.../NotificationHelper.toTenantAwareUsernames(usernames, tenantId)` |
| Console APIs | `PUT /devices/disenroll`, `PUT /admin/devices/permanent-delete` |

## Correct Behaviour

| Action | Status | Notification message |
| --- | --- | --- |
| Disenroll | Success | `Device '{id}' ({type}) was disenrolled successfully.` |
| Disenroll | Failure | `Failed to disenroll device '{id}' ({type}).` |
| Remove (`REMOVED`) | Success | `Device '{id}' ({type}) was removed successfully.` |
| Remove | Failure | `Failed to remove device '{id}' ({type}).` |
| Permanent delete | Success | `Device(s) {identifiers} were permanently deleted successfully.` |
| Permanent delete | Failure | `Failed to permanently delete device(s) {identifiers}.` |

Permanent delete success is logged at INFO.

## Acceptance Criteria

- [ ] Disenroll, remove, and permanent delete publish success/failure admin notifications.
- [ ] Messages are plain language (do not start with "Task").
- [ ] SSE delivery uses tenant-aware usernames for sub-tenants.
- [ ] Notification failure does not roll back or fail the device operation.
