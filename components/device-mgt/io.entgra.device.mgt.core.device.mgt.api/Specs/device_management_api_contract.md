# Device Management API Contract

## Purpose

Contract for the Device Management REST API implemented by
`DeviceManagementServiceImpl`. Use this when calling device list, detail,
operation, policy, compliance, app, or status endpoints from Endpoint Manager
or other clients.

## App And Audience

| Concern | Detail |
| --- | --- |
| API name | DeviceManagement |
| Base path | `/api/device-mgt/v1.0/devices` |
| Interface | `.../jaxrs/service/api/DeviceManagementService.java` |
| Implementation | `.../jaxrs/service/impl/DeviceManagementServiceImpl.java` |
| Media type | `application/json` (unless noted) |
| Default role | `Internal/devicemgt-user` |

Paths below are relative to the base path.

## Source Files

| Area | Path |
| --- | --- |
| JAX-RS interface | `device.mgt.api/.../service/api/DeviceManagementService.java` |
| JAX-RS impl | `device.mgt.api/.../service/impl/DeviceManagementServiceImpl.java` |
| Request beans | `device.mgt.api/.../jaxrs/beans/` |
| Disenroll body | `.../service/impl/util/DisenrollRequest.java` |
| Operation status service | `device.mgt.core/.../service/DeviceManagementProviderService` (`updateOperationStatus`, `updateOperationStatuses`) |
| Subscription sync (API util) | `device.mgt.api/.../jaxrs/util/DeviceMgtAPIUtils.updateApplicationSubscriptionStatusIfRequired` |

## Scopes And Permissions

All scopes below are declared on `DeviceManagementService` and typically map to
role `Internal/devicemgt-user`.

| Scope key | Name | Permission |
| --- | --- | --- |
| `dm:devices:view` | Getting Details of Registered Devices | `/device-mgt/devices/owning-device/view` |
| `dm:devices:details` | Getting Details of a Device | `/device-mgt/devices/owning-device/details/view` |
| `dm:devices:update` | Update the device specified by device id | `/device-mgt/devices/owning-device/update` |
| `dm:devices:delete` | Delete the device specified by device id | `/device-mgt/devices/owning-device/delete` |
| `dm:devices:features:view` | Getting Feature Details of a Device | `/device-mgt/devices/owning-device/features/view` |
| `dm:devices:search` | Advanced Search for Devices | `/device-mgt/devices/owning-device/search` |
| `dm:devices:app:view` | Getting Installed Application Details of a Device | `/device-mgt/devices/owning-device/apps/view` |
| `dm:devices:ops:view` | Getting Device Operation Details | `/device-mgt/devices/owning-device/operations/view` |
| `dm:devices:policy:view` | Get the details of the policy that is enforced on a device | `/device-mgt/devices/owning-device/policies/view` |
| `dm:devices:compliance:view` | Getting Policy Compliance Details of a Device | `/device-mgt/devices/owning-device/compliance/view` |
| `dm:devices:status:change` | Change device status | `/device-mgt/devices/change-status` |
| `dm:devices:ops:status:update` | Update status of a given operation | `/device-mgt/devices/operations/status-update` |
| `dm:device:enroll` | Enroll Device | `/device-mgt/devices/owning-device/add` |
| `dm:devices:enrollment-guide:view` | Viewing Enrollment Guide | `/device-mgt/devices/enrollment-guide/view` |

## Common Conventions

| Topic | Detail |
| --- | --- |
| Device type | Values such as `android`, `ios`, `windows` |
| Device id | Platform device identifier string |
| Pagination | Most list APIs use `offset` and `limit` |
| Conditional GET | Several GETs accept `If-Modified-Since` |
| Enrolment status values | `CREATED`, `ACTIVE`, `INACTIVE`, `UNREACHABLE`, `UNCLAIMED`, `SUSPENDED`, `BLOCKED`, `REMOVED`, `DISENROLLMENT_REQUESTED` |
| Operation status values | `IN_PROGRESS`, `PENDING`, `COMPLETED`, `ERROR`, `REPEATED`, `NOTNOW`, `REQUIRED_CONFIRMATION`, `CONFIRMED` |

## Request Body Models

### `Device` (rename)

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `name` | string | yes (for rename) | New display name |

Other `Device` fields may be present; rename validates `name`.

### `DisenrollRequest`

```json
{
  "deviceTypeWithDeviceIds": {
    "android": ["device-id-1", "device-id-2"],
    "windows": ["device-id-3"]
  }
}
```

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `deviceTypeWithDeviceIds` | map of string → string[] | yes | Device type to device id list |

### `SearchContext`

```json
{
  "conditions": [
    {
      "key": "DEVICE_MODEL",
      "value": "Pixel",
      "operator": "=",
      "state": "AND"
    }
  ]
}
```

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `conditions` | array | yes | Search conditions |
| `conditions[].key` | string | yes | Property key |
| `conditions[].value` | string | yes | Match value |
| `conditions[].operator` | string | yes | Comparison operator |
| `conditions[].state` | string | yes | `AND` or `OR` |

### `PropertyMap`

```json
{
  "properties": {
    "IMEI": "123456789012345"
  }
}
```

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `properties` | map of string → string | yes | Device property filters |

### `OperationRequest`

```json
{
  "deviceIdentifiers": ["device-id-1", "device-id-2"],
  "operation": {
    "code": "DEVICE_RING",
    "type": "COMMAND"
  }
}
```

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `deviceIdentifiers` | string[] | yes | Target device ids |
| `operation` | `Operation` | yes | Operation to send |

### `OperationStatusBean`

```json
{
  "operationId": 123,
  "status": "ERROR",
  "operationCode": "DEVICE_MUTE"
}
```

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `operationId` | int | yes | Operation id |
| `status` | string | yes | See operation status values |
| `operationCode` | string | no | Needed for install/uninstall subscription side effects |

### `BulkOperationStatusBean`

```json
{
  "status": "ERROR",
  "operations": [
    { "operationId": 123, "operationCode": "DEVICE_MUTE" },
    { "operationId": 124, "operationCode": "DEVICE_RING" }
  ]
}
```

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `status` | string | yes | Applied to every listed operation |
| `operations` | `OperationStatusBean[]` | yes | Each item needs `operationId`; `operationCode` optional |

---

## Endpoints

### Device list and search

#### `GET /`

List enrolled devices with optional filters.

| | |
| --- | --- |
| Method | `getDevices` |
| Scope | `dm:devices:view` |
| Permission | `/device-mgt/devices/owning-device/view` |
| Success body | `DeviceList` |

**Query parameters**

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `name` | string | — | Device name filter |
| `type` | string | — | Device type |
| `user` | string | — | Exact owner username |
| `userPattern` | string | — | Owner username pattern |
| `role` | string | — | Owner role |
| `ownership` | string | — | e.g. `BYOD`, `COPE` |
| `serialNumber` | string | — | Serial number |
| `customProperty` | string | — | Custom property filter |
| `status` | string[] | — | Enrolment status values |
| `groupId` | int | — | Include devices in this group |
| `excludeGroupId` | int | — | Exclude devices in this group |
| `since` | string | — | Modified since (`EEE, d MMM yyyy HH:mm:ss Z`) |
| `requireDeviceInfo` | boolean | — | Include extended device info |
| `tag` | string[] | — | Tag filters |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `5` | Page size |

**Headers:** `If-Modified-Since` (optional)

---

#### `GET /user-devices`

List devices owned by the authenticated user.

| | |
| --- | --- |
| Method | `getDeviceByUser` |
| Scope | `dm:devices:view` |
| Permission | `/device-mgt/devices/owning-device/view` |
| Success body | `DeviceList` |

| Query | Type | Default | Description |
| --- | --- | --- | --- |
| `requireDeviceInfo` | boolean | — | Include extended device info |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `5` | Page size |

---

#### `POST /search-devices`

Advanced search using condition list.

| | |
| --- | --- |
| Method | `searchDevices` |
| Scope | `dm:devices:search` |
| Permission | `/device-mgt/devices/owning-device/search` |
| Body | `SearchContext` |
| Success body | `DeviceList` |

| Query | Type | Default | Description |
| --- | --- | --- | --- |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `5` | Page size |

---

#### `POST /query-devices`

Search devices by property map.

| | |
| --- | --- |
| Method | `queryDevicesByProperties` |
| Scope | `dm:devices:search` |
| Permission | `/device-mgt/devices/owning-device/search` |
| Body | `PropertyMap` |
| Success body | `DeviceList` |

| Query | Type | Default | Description |
| --- | --- | --- | --- |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `5` | Page size |

---

#### `GET /filters`

Return filter options for device UI (types, ownerships, statuses).

| | |
| --- | --- |
| Method | `getDeviceFilters` |
| Scope | `dm:devices:view` |
| Permission | `/device-mgt/devices/owning-device/view` |
| Success body | `DeviceFilters` |

---

### Single device details

#### `GET /{type}/{id}`

Get one device by type and identifier.

| | |
| --- | --- |
| Method | `getDevice` |
| Scope | `dm:devices:details` |
| Permission | `/device-mgt/devices/owning-device/details/view` |
| Success body | `Device` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

| Query | Type | Description |
| --- | --- | --- |
| `owner` | string | Optional owner |
| `ownership` | string | Optional ownership |

**Headers:** `If-Modified-Since` (optional)

---

#### `GET /type/any/id/{id}`

Get device by identifier only (any type).

| | |
| --- | --- |
| Method | `getDeviceByID` |
| Scope | `dm:devices:details` |
| Permission | `/device-mgt/devices/owning-device/details/view` |
| Success body | `Device` |

| Path | Type | Description |
| --- | --- | --- |
| `id` | string | Device identifier |

| Query | Type | Description |
| --- | --- | --- |
| `requireDeviceInfo` | boolean | Include extended device info |

**Headers:** `If-Modified-Since` (optional)

---

#### `POST /type/any/list`

Get multiple devices by identifier list.

| | |
| --- | --- |
| Method | `getDeviceByIdList` |
| Scope | `dm:devices:details` |
| Permission | `/device-mgt/devices/owning-device/details/view` |
| Body | `string[]` device ids |
| Success body | device list / `Device` payload |

---

#### `GET /{type}/{id}/status`

Check whether the device is enrolled.

| | |
| --- | --- |
| Method | `isEnrolled` |
| Scope | `dm:devices:view` |
| Permission | `/device-mgt/devices/owning-device/view` |
| Success | `200` enrolled, `204` not enrolled |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

---

#### `GET /{type}/{id}/info`

Get device information details.

| | |
| --- | --- |
| Method | `getDeviceInformation` |
| Scope | `dm:devices:details` |
| Permission | `/device-mgt/devices/owning-device/details/view` |
| Success body | `DeviceInfo` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

**Headers:** `If-Modified-Since` (optional)

---

#### `GET /{type}/{id}/config`

Get device configuration details.

| | |
| --- | --- |
| Method | `getDeviceConfiguration` |
| Scope | `dm:devices:details` |
| Permission | `/device-mgt/devices/owning-device/details/view` |
| Success body | `DeviceInfo` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

**Headers:** `If-Modified-Since` (optional)

---

#### `GET /{type}/{id}/location`

Get current / latest location for a device.

| | |
| --- | --- |
| Method | `getDeviceLocation` |
| Scope | `dm:devices:details` |
| Permission | `/device-mgt/devices/owning-device/details/view` |
| Success body | location / device payload |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

**Headers:** `If-Modified-Since` (optional)

---

#### `GET /{deviceType}/{deviceId}/location-history`

Location history for one device in a time range.

| | |
| --- | --- |
| Method | `getDeviceLocationInfo` |
| Scope | `dm:devices:details` |
| Permission | `/device-mgt/devices/owning-device/details/view` |

| Path | Type | Description |
| --- | --- | --- |
| `deviceType` | string | Device type |
| `deviceId` | string | Device identifier |

| Query | Type | Description |
| --- | --- | --- |
| `from` | long | Start time (ms) |
| `to` | long | End time (ms) |
| `type` | string | Response shape / history type |

---

#### `GET /{groupId}/location-history`

Location history for devices in a group.

| | |
| --- | --- |
| Method | `getDevicesGroupLocationInfo` |
| Scope | `dm:devices:details` |
| Permission | `/device-mgt/devices/owning-device/details/view` |

| Path | Type | Description |
| --- | --- | --- |
| `groupId` | int | Group id |

| Query | Type | Default | Description |
| --- | --- | --- | --- |
| `from` | long | — | Start time (ms), required |
| `to` | long | — | End time (ms), required |
| `type` | string | — | Response shape, required |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `100` | Page size |

---

#### `GET /{deviceType}/locations/{exactTime}`

Location snapshot for devices of a type near an exact time.

| | |
| --- | --- |
| Method | `getAllDeviceLocationHistory` |
| Scope | `dm:devices:details` |
| Permission | `/device-mgt/devices/owning-device/details/view` |
| Success body | `DeviceLocationForExactTimeSnapshotWrapper` |

| Path | Type | Description |
| --- | --- | --- |
| `deviceType` | string | Device type |
| `exactTime` | long | Timestamp (ms) |

| Query | Type | Default | Description |
| --- | --- | --- | --- |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `10` | Page size |
| `timeWindow` | int | `1800000` | Window around exact time (ms) |

---

### Device lifecycle

#### `POST /type/{deviceType}/id/{deviceId}/rename`

Rename a device.

| | |
| --- | --- |
| Method | `renameDevice` |
| Scope | `dm:devices:update` |
| Permission | `/device-mgt/devices/owning-device/update` |
| Body | `Device` with `name` |
| Typical success | `201 Created` |

| Path | Type | Description |
| --- | --- | --- |
| `deviceType` | string | Device type |
| `deviceId` | string | Device identifier |

---

#### `DELETE /type/{deviceType}/id/{deviceId}`

Delete / remove a device.

| | |
| --- | --- |
| Method | `deleteDevice` |
| Scope | `dm:devices:delete` |
| Permission | `/device-mgt/devices/owning-device/delete` |
| Success body | `Device` |

| Path | Type | Description |
| --- | --- | --- |
| `deviceType` | string | Device type |
| `deviceId` | string | Device identifier |

---

#### `PUT /disenroll`

Disenroll multiple devices, grouped by type.

| | |
| --- | --- |
| Method | `disenrollMultipleDevices` |
| Scope | `dm:devices:delete` |
| Permission | `/device-mgt/devices/owning-device/delete` |
| Body | `DisenrollRequest` |

---

#### `PUT /{type}/{id}/changestatus`

Change enrolment status of one device.

| | |
| --- | --- |
| Method | `changeDeviceStatus` |
| Scope | `dm:devices:status:change` |
| Permission | `/device-mgt/devices/change-status` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

| Query | Type | Description |
| --- | --- | --- |
| `newStatus` | `EnrolmentInfo.Status` | Target status |

---

#### `GET /{type}/{id}/status-history`

Full status history for a device.

| | |
| --- | --- |
| Method | `getDeviceStatusHistory` |
| Scope | `dm:devices:view` |
| Permission | `/device-mgt/devices/owning-device/view` |
| Success body | `List` of device status records |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

---

#### `GET /{type}/{id}/enrolment-status-history`

Status history for the current enrolment only.

| | |
| --- | --- |
| Method | `getCurrentEnrolmentDeviceStatusHistory` |
| Scope | `dm:devices:view` |
| Permission | `/device-mgt/devices/owning-device/view` |
| Success body | `List` of status records |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

---

#### `GET /type/{type}/status/{status}/count`

Count devices of a type with a given status.

| | |
| --- | --- |
| Method | `getDeviceCountByStatus` |
| Scope | `dm:devices:view` |
| Permission | `/device-mgt/devices/owning-device/view` |
| Success body | `int` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `status` | string | Enrolment status |

---

#### `GET /type/{type}/status/{status}/ids`

List device identifiers of a type with a given status.

| | |
| --- | --- |
| Method | `getDeviceIdentifiersByStatus` |
| Scope | `dm:devices:view` |
| Permission | `/device-mgt/devices/owning-device/view` |
| Success body | `string[]` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `status` | string | Enrolment status |

---

#### `PUT /type/{type}/status/{status}`

Bulk update enrolment status for a list of device ids.

| | |
| --- | --- |
| Method | `bulkUpdateDeviceStatus` |
| Scope | `dm:devices:status:change` |
| Permission | `/device-mgt/devices/change-status` |
| Body | `string[]` device identifiers |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `status` | string | Target enrolment status |

---

### Operations

#### `GET /{type}/{id}/operations`

List operations for a device (operation log).

| | |
| --- | --- |
| Method | `getDeviceOperations` |
| Scope | `dm:devices:ops:view` |
| Permission | `/device-mgt/devices/owning-device/operations/view` |
| Success body | operation list / `List<Operation>` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

| Query | Type | Default | Description |
| --- | --- | --- | --- |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `5` | Page size |
| `owner` | string | `""` | Device owner |
| `ownership` | string | — | Ownership |
| `createdFrom` | long | — | Created from (epoch seconds) |
| `createdTo` | long | — | Created to (epoch seconds) |
| `updatedFrom` | long | — | Updated from (epoch seconds) |
| `updatedTo` | long | — | Updated to (epoch seconds) |
| `operationCode` | string[] | — | Filter by operation codes |
| `operationStatus` | string[] | — | Filter by statuses (e.g. `PENDING`) |

**Headers:** `If-Modified-Since` (optional)

---

#### `POST /{type}/operations`

Send an operation to one or more devices of a type.

| | |
| --- | --- |
| Method | `addOperation` |
| Scope | `dm:devices:ops:view` |
| Permission | `/device-mgt/devices/owning-device/operations/view` |
| Body | `OperationRequest` |
| Success | `201` with `Activity` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |

---

#### `PUT /{deviceType}/{id}/operation`

Update status of a single operation on a device.

| | |
| --- | --- |
| Method | `updateOperationStatus` |
| Scope | `dm:devices:ops:status:update` |
| Permission | `/device-mgt/devices/operations/status-update` |
| Body | `OperationStatusBean` |
| Success | `200` message |

| Path | Type | Description |
| --- | --- | --- |
| `deviceType` | string | Device type |
| `id` | string | Device identifier |

**Flow**

1. JAX-RS validates the payload and builds an `Operation` (`validateOperationStatusBean`).
2. Core service `DeviceManagementProviderService.updateOperationStatus` validates the
   device type, loads the device, and updates the operation.
3. API util `updateApplicationSubscriptionStatusIfRequired` syncs application
   subscription status when the operation code is an install/uninstall opcode
   (Android / Windows). Kept in the API util because `ApplicationManager` cannot
   be depended on from `device-mgt.core` without a circular dependency.

| Failure | HTTP |
| --- | --- |
| Missing / invalid payload or status | `400` |
| Device type does not exist / bad request | `400` |
| Device retrieval failure | `500` |
| Operation update failure | `500` |
| Subscription status update failure | `500` |

---

#### `PUT /{deviceType}/{id}/operations/status`

Update status of multiple operations on a device in one request.

| | |
| --- | --- |
| Method | `updateBulkOperationStatus` |
| Scope | `dm:devices:ops:status:update` |
| Permission | `/device-mgt/devices/operations/status-update` |
| Body | `BulkOperationStatusBean` |
| Success | `200` message |

| Path | Type | Description |
| --- | --- | --- |
| `deviceType` | string | Device type |
| `id` | string | Device identifier |

Shared `status` is applied to every item in `operations`. JAX-RS builds the
operation list, then calls core `updateOperationStatuses`, then runs the same
install/uninstall subscription sync per operation as the single-operation update.

Same HTTP failure mapping as `PUT /{deviceType}/{id}/operation`.

---

### Policy and compliance

#### `GET /{type}/{id}/effective-policy`

Get the policy currently enforced on the device.

| | |
| --- | --- |
| Method | `getEffectivePolicyOfDevice` |
| Scope | `dm:devices:policy:view` |
| Permission | `/device-mgt/devices/owning-device/policies/view` |
| Success body | `Policy` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

**Headers:** `If-Modified-Since` (optional)

---

#### `GET /{type}/{id}/compliance-data`

Get compliance data for a device.

| | |
| --- | --- |
| Method | `getComplianceDataOfDevice` |
| Scope | `dm:devices:compliance:view` |
| Permission | `/device-mgt/devices/owning-device/compliance/view` |
| Success body | `NonComplianceData` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

---

#### `GET /compliance/{complianceStatus}`

List devices by compliance status.

| | |
| --- | --- |
| Method | `getPolicyCompliance` |
| Scope | `dm:devices:compliance:view` |
| Permission | `/device-mgt/devices/owning-device/compliance/view` |
| Success body | compliance device list |

| Path | Type | Description |
| --- | --- | --- |
| `complianceStatus` | boolean | `true` = compliant devices |

| Query | Type | Default | Description |
| --- | --- | --- | --- |
| `policy` | string | — | Policy id filter |
| `pending` | boolean | `false` | Include pending |
| `from` | string | — | From date |
| `to` | string | — | To date |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `10` | Page size (impl default) |

---

#### `GET /{id}/features`

Get non-compliance features for a device id.

| | |
| --- | --- |
| Method | `getNoneComplianceFeatures` |
| Scope | `dm:devices:compliance:view` |
| Permission | `/device-mgt/devices/owning-device/compliance/view` |
| Success body | `NonComplianceData` |

| Path | Type | Description |
| --- | --- | --- |
| `id` | int | Device id |

---

### Applications and features

#### `GET /{type}/{id}/applications`

Installed applications on one device.

| | |
| --- | --- |
| Method | `getInstalledApplications` |
| Scope | `dm:devices:app:view` |
| Permission | `/device-mgt/devices/owning-device/apps/view` |
| Success body | `List<Application>` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

| Query | Type | Default | Description |
| --- | --- | --- | --- |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `5` | Page size |

**Headers:** `If-Modified-Since` (optional)

---

#### `POST /{type}/{id}/uninstallation`

Uninstall an application from a device (store-aware).

| | |
| --- | --- |
| Method | `uninstallation` |
| Scope | `dm:devices:app:view` |
| Permission | `/device-mgt/devices/owning-device/apps/view` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |
| `id` | string | Device identifier |

| Query | Type | Required | Description |
| --- | --- | --- | --- |
| `packageName` | string | yes | App package name |
| `platform` | string | no | Platform |
| `name` | string | no | App name |
| `version` | string | no | App version |
| `user` | string | no | User |

---

#### `GET /{deviceType}/applications`

Applications installed across devices of a platform.

| | |
| --- | --- |
| Method | `getApplications` |
| Scope | `dm:devices:app:view` |
| Permission | `/device-mgt/devices/owning-device/apps/view` |
| Success body | `ApplicationList` |

| Path | Type | Description |
| --- | --- | --- |
| `deviceType` | string | Platform / device type |

| Query | Type | Default | Description |
| --- | --- | --- | --- |
| `offset` | int | `0` | Pagination offset |
| `limit` | int | `10` | Page size |
| `appName` | string | — | Filter by app name |
| `packageName` | string | — | Filter by package |

---

#### `GET /application/{packageName}/versions`

Known versions of an application package across devices.

| | |
| --- | --- |
| Method | `getAppVersions` |
| Scope | `dm:devices:app:view` |
| Permission | `/device-mgt/devices/owning-device/apps/view` |
| Success body | `string[]` versions |

| Path | Type | Description |
| --- | --- | --- |
| `packageName` | string | Application package name |

---

#### `GET /device-type/{type}/features`

Features supported for a device type / platform.

| | |
| --- | --- |
| Method | `getFeaturesOfDevice` |
| Scope | `dm:devices:features:view` |
| Permission | `/device-mgt/devices/owning-device/features/view` |
| Success body | `List<Feature>` |

| Path | Type | Description |
| --- | --- | --- |
| `type` | string | Device type |

**Headers:** `If-Modified-Since` (optional)

---

### Enrollment helpers

#### `POST /enrollment/guide`

Send / record enrollment guide content.

| | |
| --- | --- |
| Method | `sendEnrollmentGuide` |
| Scope | `dm:devices:enrollment-guide:view` |
| Permission | `/device-mgt/devices/enrollment-guide/view` |
| Consumes | `multipart/form-data` |
| Body | enrollment guide string payload |

---

#### `GET /{clientId}/{clientSecret}/default-token`

Obtain a default access token for enrollment using client credentials.

| | |
| --- | --- |
| Method | `getDefaultToken` |
| Scope | `dm:device:enroll` |
| Permission | `/device-mgt/devices/owning-device/add` |

| Path | Type | Description |
| --- | --- | --- |
| `clientId` | string | OAuth client id |
| `clientSecret` | string | OAuth client secret |

| Query | Type | Description |
| --- | --- | --- |
| `scopes` | string | Requested OAuth scopes |

---

## Endpoint Index

| HTTP | Path | Method | Scope |
| --- | --- | --- | --- |
| GET | `/` | `getDevices` | `dm:devices:view` |
| GET | `/user-devices` | `getDeviceByUser` | `dm:devices:view` |
| GET | `/filters` | `getDeviceFilters` | `dm:devices:view` |
| POST | `/search-devices` | `searchDevices` | `dm:devices:search` |
| POST | `/query-devices` | `queryDevicesByProperties` | `dm:devices:search` |
| GET | `/{type}/{id}` | `getDevice` | `dm:devices:details` |
| GET | `/type/any/id/{id}` | `getDeviceByID` | `dm:devices:details` |
| POST | `/type/any/list` | `getDeviceByIdList` | `dm:devices:details` |
| GET | `/{type}/{id}/status` | `isEnrolled` | `dm:devices:view` |
| GET | `/{type}/{id}/info` | `getDeviceInformation` | `dm:devices:details` |
| GET | `/{type}/{id}/config` | `getDeviceConfiguration` | `dm:devices:details` |
| GET | `/{type}/{id}/location` | `getDeviceLocation` | `dm:devices:details` |
| GET | `/{deviceType}/{deviceId}/location-history` | `getDeviceLocationInfo` | `dm:devices:details` |
| GET | `/{groupId}/location-history` | `getDevicesGroupLocationInfo` | `dm:devices:details` |
| GET | `/{deviceType}/locations/{exactTime}` | `getAllDeviceLocationHistory` | `dm:devices:details` |
| POST | `/type/{deviceType}/id/{deviceId}/rename` | `renameDevice` | `dm:devices:update` |
| DELETE | `/type/{deviceType}/id/{deviceId}` | `deleteDevice` | `dm:devices:delete` |
| PUT | `/disenroll` | `disenrollMultipleDevices` | `dm:devices:delete` |
| PUT | `/{type}/{id}/changestatus` | `changeDeviceStatus` | `dm:devices:status:change` |
| GET | `/{type}/{id}/status-history` | `getDeviceStatusHistory` | `dm:devices:view` |
| GET | `/{type}/{id}/enrolment-status-history` | `getCurrentEnrolmentDeviceStatusHistory` | `dm:devices:view` |
| GET | `/type/{type}/status/{status}/count` | `getDeviceCountByStatus` | `dm:devices:view` |
| GET | `/type/{type}/status/{status}/ids` | `getDeviceIdentifiersByStatus` | `dm:devices:view` |
| PUT | `/type/{type}/status/{status}` | `bulkUpdateDeviceStatus` | `dm:devices:status:change` |
| GET | `/{type}/{id}/operations` | `getDeviceOperations` | `dm:devices:ops:view` |
| POST | `/{type}/operations` | `addOperation` | `dm:devices:ops:view` |
| PUT | `/{deviceType}/{id}/operation` | `updateOperationStatus` | `dm:devices:ops:status:update` |
| PUT | `/{deviceType}/{id}/operations/status` | `updateBulkOperationStatus` | `dm:devices:ops:status:update` |
| GET | `/{type}/{id}/effective-policy` | `getEffectivePolicyOfDevice` | `dm:devices:policy:view` |
| GET | `/{type}/{id}/compliance-data` | `getComplianceDataOfDevice` | `dm:devices:compliance:view` |
| GET | `/compliance/{complianceStatus}` | `getPolicyCompliance` | `dm:devices:compliance:view` |
| GET | `/{id}/features` | `getNoneComplianceFeatures` | `dm:devices:compliance:view` |
| GET | `/{type}/{id}/applications` | `getInstalledApplications` | `dm:devices:app:view` |
| POST | `/{type}/{id}/uninstallation` | `uninstallation` | `dm:devices:app:view` |
| GET | `/{deviceType}/applications` | `getApplications` | `dm:devices:app:view` |
| GET | `/application/{packageName}/versions` | `getAppVersions` | `dm:devices:app:view` |
| GET | `/device-type/{type}/features` | `getFeaturesOfDevice` | `dm:devices:features:view` |
| POST | `/enrollment/guide` | `sendEnrollmentGuide` | `dm:devices:enrollment-guide:view` |
| GET | `/{clientId}/{clientSecret}/default-token` | `getDefaultToken` | `dm:device:enroll` |
