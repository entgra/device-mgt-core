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

| Scope | Used by |
| --- | --- |
| `dm:notifications:view` | list / user-notifications |
| `dm:notif:mark-checked` | mark read/unread |
| `dm:notif:delete` | delete user notifications |
| `dm:notif:archive` | archive user notifications |

### Configurations (`NotificationConfigurationService`)

| Scope | Used by |
| --- | --- |
| `dm:notificationConfig:view` | list / get / check |
| `dm:notificationConfig:create` | create |
| `dm:notificationConfig:update` | update / defaults |
| `dm:notificationConfig:delete` | delete one / delete all |

---

## Notifications — `/notifications`

Statuses below are those returned by the current API impls.

### `GET /notifications`

| | |
| --- | --- |
| Scope | `dm:notifications:view` |
| Query | `offset`, `limit` |
| **200** | List of notifications (empty list if none) |
| **500** | `NotificationManagementException` |

### `POST /notifications/user-notifications`

| | |
| --- | --- |
| Scope | `dm:notifications:view` |
| Body | `UserNotificationsRequest` (`isRead`, `limit`, `offset`; `username` ignored) |
| **200** | `PaginatedUserNotificationResponse` |
| **400** | Missing request body |
| **500** | Service / retrieval failure |

### `PUT /notifications/action`

| | |
| --- | --- |
| Scope | `dm:notif:mark-checked` |
| Body | `NotificationActionRequest` (`notificationIds`, `isRead`) |
| **200** | Marked as READ/UNREAD |
| **400** | Missing request body |
| **500** | Update failure |

### `PUT /notifications/action/all`

| | |
| --- | --- |
| Scope | `dm:notif:mark-checked` |
| Query | `read` (boolean) |
| **200** | All marked as READ/UNREAD |
| **500** | Update failure |

### `DELETE /notifications/user-notifications`

| | |
| --- | --- |
| Scope | `dm:notif:delete` |
| Body | `UsernameWithNotificationIdsRequest` (`notificationIds`) |
| **200** | Map `{ deleted, invalid }` |
| **400** | Missing request body |
| **500** | Delete failure |

### `DELETE /notifications/user-notifications/all`

| | |
| --- | --- |
| Scope | `dm:notif:delete` |
| Query | `read` (`Boolean`, optional) |
| **200** | Delete confirmation message |
| **500** | Delete failure |

### `POST /notifications/archive-selected`

| | |
| --- | --- |
| Scope | `dm:notif:archive` |
| Body | `UsernameWithNotificationIdsRequest` |
| **200** | Map `{ archived, invalid }` |
| **400** | Missing request body |
| **500** | Archival failure or auth user resolution failure |

### `POST /notifications/archive-all`

| | |
| --- | --- |
| Scope | `dm:notif:archive` |
| **200** | Archive confirmation message |
| **500** | Archival failure or auth user resolution failure |

---

## Configurations — `/notification-configuration`

### `GET /notification-configuration`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:view` |
| Query | `offset`, `limit`, `name`, `type`, `deviceType`, `code` |
| **200** | `NotificationConfigurationList` |
| **404** | No configurations for tenant (`NotificationConfigurationNotFoundException`) |
| **500** | Unexpected service error |

### `POST /notification-configuration`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:create` |
| Body | `NotificationConfigurationList` |
| **201** | Created list |
| **400** | Invalid / empty / duplicate config (`InvalidNotificationConfigurationException`) |
| **500** | Persist failure |

### `PUT /notification-configuration`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:update` |
| Body | `NotificationConfig` (must include `id`) |
| **200** | Updated config |
| **400** | Invalid request / id |
| **404** | Config id not found |
| **500** | Update failure |

### `GET /notification-configuration/{configId}`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:view` |
| **200** | `NotificationConfig` |
| **400** | Invalid id |
| **404** | Not found |
| **500** | Unexpected error |

### `DELETE /notification-configuration/{configId}`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:delete` |
| **200** | Deleted successfully |
| **400** | Invalid id |
| **404** | Not found |
| **500** | Delete failure |

### `DELETE /notification-configuration`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:delete` |
| **204** | All configs cleared |
| **404** | No configuration found for tenant |

### `PUT /notification-configuration/defaults`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:update` |
| Body | `defaultArchiveType`, `defaultArchiveAfter` |
| **200** | Updated defaults |
| **400** | Empty / invalid archive type or period |
| **500** | Persist failure |

### `GET /notification-configuration/check`

| | |
| --- | --- |
| Scope | `dm:notificationConfig:view` |
| Query | `deviceType`, `code` |
| **409** | Config already exists for `(deviceType, code)` |
| **204** | Does not exist |
| **400** | Invalid deviceType / code |
| **404** | Tenant has no notification-config metadata |
| **500** | Unexpected error |

---

## Payload notes

- Config `type`: `operation` \| `task`
- Task device types come from catalog (`ALL` / `NONE`); operations use platform types
- SSE payloads (not REST): see [sse.md](sse.md)

## Acceptance criteria

- [ ] Documented statuses match API impl exception mapping
- [ ] User mutation APIs ignore body username and use authenticated user
- [ ] Check endpoint returns **409** / **204** only (not 200 for “exists”)
