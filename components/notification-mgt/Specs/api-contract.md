# Notification Management — API Contract

## Base

| Item | Value |
| --- | --- |
| Context | `/api/notification-mgt/v1.0` |
| WAR | `api#notification-mgt#v1.0` |
| Transport | CXF JAX-RS, `/*`, HTTPS transport-guarantee |
| Media type | `application/json` |

Example: `https://host:9443/api/notification-mgt/v1.0/notifications`

Auth: managed API (`doAuthentication=true`). Username for user-scoped
notification APIs comes from the authenticated session
(`NotificationHelper.getAuthenticatedUser()`), not from request body fields.

---

## Scopes

### Notifications (`NotificationService`)

| Scope | Permission |
| --- | --- |
| `dm:notifications:view` | view notifications |
| `dm:notif:mark-checked` | mark read/unread |
| `dm:notif:delete` | delete user notifications |
| `dm:notif:archive` | archive user notifications |

### Configurations (`NotificationConfigurationService`)

| Scope | Permission path |
| --- | --- |
| `dm:notificationConfig:view` | `/device-mgt/notifications/notification-configurations/view` |
| `dm:notificationConfig:create` | `/device-mgt/notifications/notification-configurations/create` |
| `dm:notificationConfig:update` | `/device-mgt/notifications/notification-configurations/update` |
| `dm:notificationConfig:delete` | `/device-mgt/notifications/notification-configurations/delete` |

---

## Notifications — `/notifications`

### `GET /notifications`

| | |
| --- | --- |
| Scope | `dm:notifications:view` |
| Query | `offset` (default 0), `limit` (default 5) |
| Success | **200** list of notifications for tenant |
| Error | **500** |

### `POST /notifications/user-notifications`

| | |
| --- | --- |
| Scope | `dm:notifications:view` |
| Body | `UserNotificationsRequest`: `username`, `isRead`, `limit`, `offset` |
| Notes | `username` in body is ignored; uses authenticated user |
| Success | **200** `PaginatedUserNotificationResponse` |
| Error | **400** null body; **500** |

### `PUT /notifications/action`

| | |
| --- | --- |
| Scope | `dm:notif:mark-checked` |
| Body | `NotificationActionRequest`: `notificationIds`, `isRead` (`username` ignored) |
| Success | **200**; pushes unread-count SSE update |
| Error | **400**; **500** |

### `PUT /notifications/action/all`

| | |
| --- | --- |
| Scope | `dm:notif:mark-checked` |
| Query | `read` (boolean, required) |
| Success | **200**; pushes unread-count SSE update |
| Error | **500** |

### `DELETE /notifications/user-notifications`

| | |
| --- | --- |
| Scope | `dm:notif:delete` |
| Body | `UsernameWithNotificationIdsRequest`: `notificationIds` |
| Success | **200** map `{ "deleted": [...], "invalid": [...] }` |
| Error | **400**; **500** |

### `DELETE /notifications/user-notifications/all`

| | |
| --- | --- |
| Scope | `dm:notif:delete` |
| Query | `read` (`Boolean`, optional — omit to delete all) |
| Success | **200** |
| Error | **500** |

### `POST /notifications/archive-selected`

| | |
| --- | --- |
| Scope | `dm:notif:archive` |
| Body | `UsernameWithNotificationIdsRequest` |
| Success | **200** map `{ "archived": [...], "invalid": [...] }` |
| Error | **400**; **500** |

### `POST /notifications/archive-all`

| | |
| --- | --- |
| Scope | `dm:notif:archive` |
| Body | none |
| Success | **200** |
| Error | **500** |

---

## Configurations — `/notification-configuration`

### `GET /notification-configuration`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:view` |
| Query | `offset`, `limit`, `name`, `type`, `deviceType`, `code` |
| Success | **200** `NotificationConfigurationList` |
| Error | **404** none for tenant; **500** |

`type` filter: `operation` | `task` (and any other stored type values).

### `POST /notification-configuration`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:create` |
| Body | `NotificationConfigurationList` |
| Success | **201** |
| Error | **400**; **500** |

### `PUT /notification-configuration`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:update` |
| Body | `NotificationConfig` |
| Success | **200** |
| Error | **400**; **404**; **500** |

### `GET /notification-configuration/{configId}`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:view` |
| Success | **200** |
| Error | **400**; **404**; **500** |

### `DELETE /notification-configuration/{configId}`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:delete` |
| Success | **200** |
| Error | **400**; **404**; **500** |

### `DELETE /notification-configuration`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:delete` |
| Notes | Deletes all configs for tenant (keeps / resets archive defaults per impl) |
| Success | **204** |
| Error | **404** |

### `PUT /notification-configuration/defaults`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:update` |
| Body | `NotificationConfigurationList` with `defaultArchiveType`, `defaultArchiveAfter` |
| Success | **200** |
| Error | **400**; **500** |

### `GET /notification-configuration/check`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:view` |
| Query | `deviceType`, `code` |
| Exists | **409** |
| Not exists | **204** |
| Error | **400**; **404**; **500** |

---

## Payload shapes (summary)

### `NotificationConfig` (config entry)

Typical fields: `id`, `name`, `code`, `type` (`operation`|`task`),
`deviceType`, `enabled`, `description`, `recipients` (users/roles),
`notificationTriggerPoints` (e.g. `immediate`, `postSync`),
`criticalCriteria`, `batchNotifications`, archive settings
(`archiveType`, `archiveAfter`).

### `NotificationConfigurationList`

`notificationConfigurations[]`, `defaultArchiveType`,
`defaultArchiveAfter` (`{value, unit}`), `totalCount`.

### User notification list item

Includes notification id, type, created timestamp, rendered/description
context, `isRead`, and related device info when present.

### SSE payloads (not REST)

See [sse.md](sse.md): `{ message, unreadCount }` or `{ unreadCount }`.

---

## Exception → HTTP (impl conventions)

| Situation | HTTP |
| --- | --- |
| Validation / null body | **400** |
| Config / resource missing | **404** |
| Config already exists (check) | **409** |
| Create config success | **201** |
| Delete all configs success | **204** |
| Check: config not present | **204** |
| Unhandled service errors | **500** |

---

## Acceptance criteria

- [ ] All endpoints above require the listed scopes.
- [ ] User notification mutating APIs act on the authenticated user only.
- [ ] List/filter configs by `type=task|operation` returns only matching entries.
- [ ] Check endpoint returns **409** when a config for `(deviceType, code)` exists, else **204**.
