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
| Status validation | `device.mgt.api/.../DeviceMgtAPIUtils` (`validateOperationStatus`, `validateOperationStatusBean`) |
| Core service | `device.mgt.core/.../DeviceManagementProviderService` (`updateOperationStatus`, `updateOperationStatuses`) |
| Operation manager | `device.mgt.core/.../OperationManagerImpl.updateOperationStatuses` |
| DAO | `device.mgt.core/.../OperationDAO.updateOperationStatuses` |
| Subscription sync | `device.mgt.api/.../DeviceMgtAPIUtils.updateApplicationSubscriptionStatusIfRequired` — single: `ApplicationManager.updateSubsStatus`; bulk: `ApplicationManager.updateSubStatus` |
| API contract | [device_management_api_contract.md](../io.entgra.device.mgt.core.device.mgt.api/Specs/device_management_api_contract.md) |

## Behaviour

### Single update — `PUT /{deviceType}/{id}/operation`

1. Reject null body with `400`.
2. Map `OperationStatusBean` status string to `Operation.Status` (invalid → `400`).
3. Core service validates device type exists, loads the device, updates the operation.
4. If the operation code is an install/uninstall opcode (Android or Windows), sync
   application subscription status via the API util.

### Bulk update — `PUT /{deviceType}/{id}/operations/status/{status}`

Body: JSON array of operations, each with `id` and `code`
(for example `[{"id": 12, "code": "INSTALL_APPLICATION"}]`). Any `status` in the
body is ignored; the path `{status}` applies to every listed operation.

1. Reject null or empty body with `400`.
2. Map path `{status}` to `Operation.Status` (missing/invalid → `400`).
3. Core service validates device type exists and loads the device (no enrolled
   device → `404`).
4. Operation manager updates all listed operations in one transaction with a
   single `UPDATE DM_ENROLMENT_OP_MAPPING ... WHERE ENROLMENT_ID = ? AND
   OPERATION_ID IN (...)`. Fewer matched rows than requested is logged as a warning.
5. After commit, post-sync notifications are triggered per operation, and a
   completed `POLICY_REVOKE` removes a device whose enrolment is
   `DISENROLLMENT_REQUESTED` (same side effects as the single update).
6. Install/uninstall operations among the list are synced to application
   subscriptions in one application-mgt transaction (`updateSubStatus`).

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
| No enrolled device (bulk) | `404` |
| Device retrieval failure | `500` |
| Operation update failure | `500` |
| Subscription sync failure | `500` |

Service-layer catch blocks log a message and rethrow; JAX-RS maps exceptions to
the HTTP statuses above.

## Correct Behaviour

| Scenario | Expected |
| --- | --- |
| Valid single status update | `200`; operation status persisted |
| Valid bulk Pending → Error | `200`; all listed operations updated in one transaction |
| Install/uninstall opcode status change | Operation updated and subscription status synced |
| Invalid status string | `400` |
| Unknown device type | `400` |
