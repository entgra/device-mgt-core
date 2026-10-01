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

## Planned feature areas (add specs when touched)

- Device enrollment
- Device list / search / filters
- Device details / properties
- Operations and notifications
- Device access authorization

## Writing rules

- Describe current backend behaviour, APIs, permissions/scopes, and error cases.
- Use HTTP status codes for API contracts (`200`, `201`, `400`, `404`, `409`, `500`, …).
- When behaviour changes, update the matching spec in the same change.
- UI Endpoint Manager specs live separately under
  `emm-proprietary-plugins/.../endpoint.mgt.ui/Specs/`.
