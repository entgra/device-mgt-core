# Device Management Specs

## Purpose

This folder holds backend feature specs for Device Management
(`device-mgt-core/components/device-mgt`).

Each file explains one feature: validation, persistence, and what
correct behaviour looks like in the core services and Device Management API.

## How to use these specs

- Before changing device persist / rename / enrollment behaviour, read the matching spec.
- When behaviour changes, update its spec in the same change.
- Prefer one file per feature area, grouped by feature folder when needed.

## Spec index

### Device identity

| Feature | Spec |
| --- | --- |
| Device name and description persistence | [device-name-description.md](device-name-description.md) |
| Console delete / disenroll task notifications | [device-delete-notifications.md](device-delete-notifications.md) |
| Task notification configs + `DM_FEATURE_DETAILS` | [task-notification-configs.md](task-notification-configs.md) |
| Notification-mgt module (API, DAO, SSE, archival) | [../../notification-mgt/Specs/README.md](../../notification-mgt/Specs/README.md) |

### Operations

| Feature | Spec |
| --- | --- |
| Operation status update (single + bulk) | [operation-status-update.md](operation-status-update.md) |
| Device Management API contract | [io.entgra.device.mgt.core.device.mgt.api/Specs/device_management_api_contract.md](../io.entgra.device.mgt.core.device.mgt.api/Specs/device_management_api_contract.md) |
| Device type OS version labels | [device-type-os-version-labels.md](device-type-os-version-labels.md) |

### Policies

| Feature | Spec |
| --- | --- |
| Policy management (assignment, lifecycle, evaluation, list filters incl. `groupId`) | [policy-mgt.md](policy-mgt.md) |
| Policy Management REST API contract | [policy_management_api_contract.md](../io.entgra.device.mgt.core.device.mgt.api/Specs/policy_management_api_contract.md) |

## Planned feature areas (add specs when touched)

- Device enrollment
- Device list / search / filters
- Device details / properties
- Operations and notifications (partial: status update, delete/disenroll task notifications, and task notification configs covered above)
- Device access authorization

## Writing rules

- Describe current backend behaviour, APIs, permissions/scopes, and error cases.
- Use HTTP status codes for API contracts (`200`, `201`, `400`, `404`, `409`, `500`, …).
- When behaviour changes, update the matching spec in the same change.
- UI Endpoint Manager specs live separately under
  `emm-proprietary-plugins/.../endpoint.mgt.ui/Specs/`.
