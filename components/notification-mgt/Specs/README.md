# Notification Management Specs

## Purpose

This folder documents the **notification-mgt** module
(`device-mgt-core/components/notification-mgt`): REST APIs, OSGi services,
DAO/DB, tenant configuration metadata, archival, and real-time delivery via SSE.

Related feature specs outside this folder:

| Topic | Spec |
| --- | --- |
| Task notification configs + `DM_FEATURE_DETAILS` | [../../device-mgt/Specs/task-notification-configs.md](../../device-mgt/Specs/task-notification-configs.md) |
| Console delete / disenroll task notifications | [../../device-mgt/Specs/device-delete-notifications.md](../../device-mgt/Specs/device-delete-notifications.md) |

## How to use these specs

- Before changing publish, archival, config metadata, or SSE behaviour, read the matching spec.
- When behaviour changes, update its spec in the same change.
- Prefer status codes and scope keys over parsing raw server text.

## Spec index

| Spec | Contents |
| --- | --- |
| [architecture.md](architecture.md) | Bundle layout, end-to-end flows (operation / task / read-update / archival), component diagram |
| [api-contract.md](api-contract.md) | REST base path, scopes, endpoints, request/response shapes, HTTP statuses |
| [data-model.md](data-model.md) | Tables, archival schema, tenant metadata, DAO responsibilities |
| [services.md](services.md) | OSGi services, publish/purge/archival internals, callers |
| [sse.md](sse.md) | SSE endpoint, broker, payloads, session/username matching, keep-alive |
