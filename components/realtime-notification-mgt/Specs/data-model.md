# Realtime Notification Management — Data Model & DAO

## Purpose

Document active and archival tables, tenant metadata shape, and which DAO
touches which store.

## Tenant metadata (configuration)

| Item | Exact value |
| --- | --- |
| Meta key | `notification-config` (`RealtimeConstants.NOTIFICATION_CONFIG_META_KEY`) |
| Default archive type | `DB` |
| Default archive after | `{ "value": 12, "unit": "months" }` |
| Archive delete retention | `{ "value": 5, "unit": "years" }` |

Stored via `MetadataManagementService` as JSON matching
`RealtimeNotificationConfigurationList`:

- `notificationConfigurations[]` — per-feature configs
- `defaultArchiveType`, `defaultArchiveAfter`
- `totalCount` (list responses)

Per-config archive overrides: `settings.archiveType`, `settings.archiveAfter`.

There is **no** `DM_REALTIME_NOTIFICATION_CONFIG` table; configs are metadata-only.

---

## Active schema (CDM / source DB)

Scripts: `device-mgt.basics.feature/.../dbscripts/cdm/<db>.sql`

Legacy inbox (unchanged table name, coexists with realtime):

### `DM_NOTIFICATION` (legacy)

| Column | Notes |
| --- | --- |
| `NOTIFICATION_ID` | PK |
| `DEVICE_ID` | FK → `DM_DEVICE(ID)` |
| `OPERATION_ID` | Optional |
| `TENANT_ID` | Tenant |
| `STATUS` | e.g. `NEW` |
| `DESCRIPTION` | Free-text |
| `LAST_UPDATED_TIMESTAMP` | |

Served by device-mgt `NotificationManagementService` / legacy Device Management
notification API. Realtime tables below are prefixed `DM_REALTIME_*`.

### `DM_REALTIME_NOTIFICATION`

| Column | Notes |
| --- | --- |
| `NOTIFICATION_ID` | PK, auto-increment |
| `NOTIFICATION_CONFIG_ID` | Config id from metadata |
| `TENANT_ID` | Tenant |
| `NOTIFICATION_CONTEXT` | JSON (`RealtimeNotificationContext`), up to 1000 chars |
| `TYPE` | e.g. `operation`, `task` |
| `CREATED_TIMESTAMP` | Default `CURRENT_TIMESTAMP` |

Indexes: config id, tenant id, created timestamp.

### `DM_REALTIME_NOTIFICATION_DEVICE`

| Column | Notes |
| --- | --- |
| `NOTIFICATION_ID` | FK → `DM_REALTIME_NOTIFICATION` ON DELETE CASCADE |
| `DEVICE_ID` | FK → `DM_DEVICE(ID)` ON DELETE CASCADE |
| PK | `(NOTIFICATION_ID, DEVICE_ID)` |

Used for **operation** notifications. Task notifications typically have no rows.

### `DM_REALTIME_NOTIFICATION_USER_ACTION`

| Column | Notes |
| --- | --- |
| `ACTION_ID` | PK |
| `NOTIFICATION_ID` | FK → `DM_REALTIME_NOTIFICATION` ON DELETE CASCADE |
| `USERNAME` | Recipient |
| `IS_READ` | Default `false` |
| `ACTION_TIMESTAMP` | Default `CURRENT_TIMESTAMP` |

One row per (notification, user) for inbox / unread counting.

---

## Archival schema (destination DB)

Scripts: `.../dbscripts/cdm/archival/<db>.sql`

| Active | Archive |
| --- | --- |
| `DM_REALTIME_NOTIFICATION` | `DM_REALTIME_NOTIFICATION_ARCH` |
| `DM_REALTIME_NOTIFICATION_USER_ACTION` | `DM_REALTIME_NOTIFICATION_USER_ACTION_ARCH` |

Archive tables mirror the main columns (no device mapping archive table).
DB names for source/dest come from
`DeviceConfigurationManager` → archival `DbConfig`
(`getSourceDB()` / `getDestinationDB()`).

Datasource wiring:

- Source: `RealtimeNotificationConfigurationManager` repository
- Dest: `RealtimeNotificationArchiveConfigManager` repository

---

## `NOTIFICATION_CONTEXT` JSON

Serialized `RealtimeNotificationContext`:

```json
{
  "operationCode": "...",
  "configDescription": "...",
  "operationStatus": "...",
  "deviceType": "...",
  "includeDeviceList": true,
  "message": "..."
}
```

| Type | Typical fields set |
| --- | --- |
| Operation | `operationCode`, `configDescription`, `operationStatus`, `deviceType`, `includeDeviceList` |
| Task | `message` |

UI/push text is produced by `RealtimeNotificationMessageRenderer.render(...)`.
DAO read path maps the context column into DTO `description` after render /
as stored JSON depending on caller.

---

## DAO layer

### `RealtimeNotificationManagementDAO` (active DB)

Factory: `RealtimeNotificationManagementDAOFactory`

| Tables | Operations |
| --- | --- |
| `DM_REALTIME_NOTIFICATION` | insert, select (paginated / by tenant), delete orphans |
| `DM_REALTIME_NOTIFICATION_DEVICE` | insert mappings, delete by device, select for purge |
| `DM_REALTIME_NOTIFICATION_USER_ACTION` | insert, update read flag, delete, unread count, list with status |

Insert notification columns: `NOTIFICATION_CONFIG_ID`, `TENANT_ID`,
`NOTIFICATION_CONTEXT`, `TYPE` (+ generated id / timestamp).

### `RealtimeNotificationArchivalDAO` (source + dest)

Factories: `RealtimeNotificationArchivalSourceDAOFactory`,
`RealtimeNotificationArchivalDestDAOFactory`

| Direction | Behaviour |
| --- | --- |
| Archive old | Copy notification (+ user actions) source → dest; delete from source |
| User archive APIs | Move selected/all user-action (and related) rows to arch tables |
| Delete expired | Remove from dest older than default delete period (5 years) |

SQL uses schema prefixes `SOURCE_DB` / `DESTINATION_DB`.

---

## Related catalog table (device-mgt)

`DM_FEATURE_DETAILS` holds operation and task feature codes used when
building/editing configs (`TYPE = operation|task`). Not owned by
realtime-notification-mgt DAOs; see
[task-notification-configs.md](../../device-mgt/Specs/task-notification-configs.md).

---

## Acceptance criteria

- [ ] New installs create active notification tables in CDM scripts and arch tables in archival scripts.
- [ ] Publish writes notification + user actions; operation publish also writes device mappings.
- [ ] Unread count is derived from `DM_REALTIME_NOTIFICATION_USER_ACTION` where `IS_READ = false`.
- [ ] Scheduled archival moves aged rows to dest and can purge expired archives.
- [ ] Config changes persist only under metadata key `notification-config`.
