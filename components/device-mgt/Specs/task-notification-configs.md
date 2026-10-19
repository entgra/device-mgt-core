# Task Notification Configurations

## Purpose

Platform-agnostic console **tasks** (delete, remove, disenroll, role
permission update, etc.) can publish admin notifications when a matching
**task** notification configuration is enabled.

## Feature catalog and `DM_FEATURE_DETAILS`

On Device Task Manager startup,
`DeviceFeatureOperationsImpl.getDeviceFeatureOperations()`:

1. Loads per-device-type **operation** features (`TYPE = operation`).
2. Loads **notification task** features from
   `mdm-ui-config.xml` → `<NotificationTaskFeatures>`
   (`TYPE = task`). Optional XML attribute `deviceType` defaults to `ALL`.
3. Inserts missing task rows into `DM_FEATURE_DETAILS` (one row per task
   code). Existing task rows sync `DEVICE_TYPE` / name / description when
   the catalog changes.

Task codes used for console device lifecycle (see also
[device-delete-notifications.md](device-delete-notifications.md)):

| Code | `DEVICE_TYPE` | Meaning |
| --- | --- | --- |
| `REMOVE_DEVICE` | `ALL` | Soft-remove (status `REMOVED`) |
| `DELETE_DEVICE` | `ALL` | Permanent delete |
| `DISENROLL_DEVICE` | `ALL` | Disenroll |
| `ROLE_PERMISSION_UPDATE` | `NONE` | Role UI permission update (not device-scoped) |

## Tenant defaults and existing-tenant backfill

| Path | Behaviour |
| --- | --- |
| New tenant | `DeviceMgtTenantListener` → `ensureDefaultTaskNotificationConfigs()` after archive defaults |
| Existing tenants | On notification-mgt activate: refresh feature catalog, then seed defaults for carbon.super and every active tenant |

Default task configs: `enabled=true`, recipient user `admin`,
`deviceType` taken from the catalog (`ALL` for device tasks,
`NONE` for `ROLE_PERMISSION_UPDATE`). Existing tenant configs sync catalog
`deviceType`; duplicate task configs for the same code are collapsed to the
highest id (user-created wins over seed).
`ROLE_PERMISSION_UPDATE` is forced to `NONE` in catalog load even if XML
omits `deviceType`.

## DB migration

- New installs: `DM_FEATURE_DETAILS` with `TYPE` in CDM scripts.
- Upgrades: `product-uem/.../migration-uem-6.4.0-to-uem-6.5.0/mysql/cdm-migration-mysql.sql` creates `DM_FEATURE_DETAILS`. If an earlier snapshot already created `DM_OPERATION_DETAILS`, copy/drop that table using the commented SQL in that migration file.
- Optional offline backfill of default task notification configs into
  `DM_METADATA` (`METADATA_KEY = notification-config`) for existing tenants:
  commented sample INSERT at the end of that same `cdm-migration-mysql.sql`
  (replace placeholder tenant IDs; skips tenants that already have the key).
  Prefer runtime `ensureDefaultTaskNotificationConfigs()` when the server can start.

Code search for the UI uses
`GET .../device-operations?featureType=operation|task`.

## Notification configuration

- Config `type` must be `task` (operations use `type=operation`).
- Task configs use catalog `deviceType` (`ALL` or `NONE`).
- List/filter API:
  `GET .../notification-configuration?type=task|operation&...`
- At publish time,
  `NotificationManagementService.handleTaskNotificationIfApplicable(tenantId, taskCode, message)`
  loads the enabled config for that code and notifies its recipients.
  If no enabled task config exists, the notification is skipped (logged at debug).

## Acceptance Criteria

- [ ] Task features from `NotificationTaskFeatures` appear in `DM_FEATURE_DETAILS` after startup migration.
- [ ] `ROLE_PERMISSION_UPDATE` is stored with `DEVICE_TYPE=NONE` (and existing `ALL` rows are synced).
- [ ] Endpoint Manager can create/list task configs separately from operation configs.
- [ ] Console delete / remove / disenroll publish only when a matching enabled task config exists.
