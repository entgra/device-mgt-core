# Operation Status Update Spec

## Purpose

Documents how a device operation status is updated from the Device Management
API: single-operation and bulk updates used by Endpoint Manager (for example the
single-device Operation Log “Mark as Error” flow).

## App And Audience

| Concern | Detail |
| --- | --- |
| API | Device Management REST API |
| Callers | Endpoint Manager Operation Log (and other clients with the status-update scope) |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| JAX-RS | `device.mgt.api/.../DeviceManagementServiceImpl` (`updateOperationStatus`, `updateBulkOperationStatus`) |
| Bean validation | `device.mgt.api/.../DeviceMgtAPIUtils.validateOperationStatusBean` |
| Core service | `device.mgt.core/.../DeviceManagementProviderService` (`updateOperationStatus`, `updateOperationStatuses`) |
| Subscription sync | `device.mgt.api/.../DeviceMgtAPIUtils.updateApplicationSubscriptionStatusIfRequired` |
| API contract | [device_management_api_contract.md](../io.entgra.device.mgt.core.device.mgt.api/Specs/device_management_api_contract.md) |

## Behaviour

### Single update — `PUT /{deviceType}/{id}/operation`

1. Reject null body with `400`.
2. Map `OperationStatusBean` status string to `Operation.Status` (invalid → `400`).
3. Core service validates device type exists, loads the device, updates the operation.
4. If the operation code is an install/uninstall opcode (Android or Windows), sync
   application subscription status via the API util.

### Bulk update — `PUT /{deviceType}/{id}/operations/status`

1. Reject null/blank shared `status` or empty `operations` with `400`.
2. Apply shared `status` to each item, validate each bean, then call core
   `updateOperationStatuses`.
3. Run subscription sync for each updated operation when required.

### Why subscription sync stays in the API util

`ApplicationManager` lives in application-mgt. `application-mgt.common` already
depends on `device-mgt.core`, so calling it from the core service would create a
circular dependency. Existing core `updateOperation` signatures are unchanged;
new dedicated service methods were added instead.

## Errors

| Case | HTTP |
| --- | --- |
| Missing / invalid payload or status | `400` |
| Unknown device type / bad request | `400` |
| Device retrieval failure | `500` |
| Operation update failure | `500` |
| Subscription sync failure | `500` |

Service-layer catch blocks log a message and rethrow; JAX-RS maps exceptions to
the HTTP statuses above.

## Correct Behaviour

| Scenario | Expected |
| --- | --- |
| Valid single status update | `200`; operation status persisted |
| Valid bulk Pending → Error | `200`; all listed operations updated |
| Install/uninstall opcode status change | Operation updated and subscription status synced |
| Invalid status string | `400` |
| Unknown device type | `400` |
