# Policy Management API Contract

## Purpose

Contract for the Policy Management REST API implemented by
`PolicyManagementServiceImpl`. Use this when creating, listing, updating,
publishing, prioritising or removing policies from Endpoint Manager or other
clients.

Domain behaviour (assignment, publishing, evaluation, persistence) is described
in [policy-mgt.md](../../Specs/policy-mgt.md).

## App And Audience

| Concern | Detail |
| --- | --- |
| API name | Device Policy Management |
| Base path | `/api/device-mgt/v1.0/policies` |
| Interface | `.../jaxrs/service/api/PolicyManagementService.java` |
| Implementation | `.../jaxrs/service/impl/PolicyManagementServiceImpl.java` |
| Media type | `application/json` (consumes and produces) |
| Default role | `Internal/devicemgt-user` |

Paths below are relative to the base path.

## Source Files

| Area | Path |
| --- | --- |
| JAX-RS interface | `device.mgt.api/.../service/api/PolicyManagementService.java` |
| JAX-RS impl | `device.mgt.api/.../service/impl/PolicyManagementServiceImpl.java` |
| Request validation | `device.mgt.api/.../service/impl/util/RequestValidationUtil.java` |
| Request beans | `device.mgt.api/.../jaxrs/beans/PolicyWrapper.java`, `PriorityUpdatedPolicyWrapper.java`, `Profile.java`, `ProfileFeature.java` |
| List filter model | `device.mgt.common/.../PolicyPaginationRequest.java` |
| Core entry point | `policy.mgt.core/.../impl/PolicyAdministratorPointImpl.java` (PAP) |

## Scopes And Permissions

All scopes are declared on `PolicyManagementService` and map to role
`Internal/devicemgt-user`.

| Scope key | Name | Permission |
| --- | --- | --- |
| `pm:policies:add` | Adding a Policy | `/device-mgt/policies/add` |
| `dm:policies:view` | Getting Details of Policies | `/device-mgt/policies/view` |
| `pm:policies:details:view` | Getting Details of a Policy | `/device-mgt/policies/view-details` |
| `pm:policies:update` | Updating a Policy | `/device-mgt/policies/update` |
| `pm:policies:remove` | Removing Multiple Policies | `/device-mgt/policies/remove` |
| `pm:policies:activate` | Activating Policies | `/device-mgt/policies/activate` |
| `pm:policies:deactivate` | Deactivating Policies | `/device-mgt/policies/deactivate` |
| `pm:policies:change` | Applying Changes on Policies | `/device-mgt/policies/apply-changes` |
| `pm:policies:priorities:update` | Updating the Policy Priorities | `/device-mgt/policies/update-priority` |
| `pm:policies:effective-policy` | Fetching the Effective Policy | `/device-mgt/policies/view-effective-policy` |

## Common Conventions

| Topic | Detail |
| --- | --- |
| Pagination | `offset` ≥ 0, `0` ≤ `limit` ≤ 100; violations return `400` from `validatePaginationParameters` |
| Policy id list bodies | JSON array of integers, e.g. `[1, 2, 3]`; empty or null returns `400` (`Policy Id list is empty.`) |
| Validation errors | `InputValidationException` → `400` with `ErrorResponse` JSON |
| Error body shape | Mixed: most failures return `ErrorResponse` JSON; some return a plain string (noted per endpoint) |
| Conditional GET | `If-Modified-Since` is accepted on several GETs but not evaluated (see Gaps) |
| Policy status | `ACTIVE` / `INACTIVE` via `active` flag; `updated` flag marks unpublished changes |
| Policy type | `GENERAL`, `CORRECTIVE` |

## Request Body Models

### `PolicyWrapper` (create / update)

| Field | Type | Required | Constraints / notes |
| --- | --- | --- | --- |
| `policyName` | string | yes | `@Size(max=45)` |
| `description` | string | no | `@Size(max=1000)` |
| `payloadVersion` | string | no | `@Size(max=45)` |
| `compliance` | string | no | `@Size(max=100)`, e.g. `enforce`, `warn`, `monitor` |
| `ownershipType` | string | no | `@Size(max=45)`, e.g. `ANY`, `BYOD`, `COPE` |
| `active` | boolean | no | Initial active flag |
| `profile` | `Profile` | yes | Must contain a non-empty `profileFeaturesList` |
| `roles` | string[] | no | Role assignment |
| `users` | string[] | no | User assignment |
| `deviceGroups` | `DeviceGroupWrapper[]` | no | Group assignment (`DM_DEVICE_GROUP_POLICY`) |
| `deviceIdentifiers` | `DeviceIdentifier[]` | no | Direct device assignment; each resolved via `getDevice` |
| `policyType` | string | yes | `@NotNull`; `GENERAL` or `CORRECTIVE` |
| `correctiveActions` | `CorrectiveAction[]` | no | Corrective policy links |

### `Profile.profileFeaturesList[]` (`ProfileFeature`)

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `featureCode` | string | yes | Feature / operation code |
| `deviceType` | string | yes | Must be non-blank and the **same** for every feature |
| `content` | object | no | Feature payload |
| `payLoad` | string | no | Raw payload |
| `correctiveActions` | array | no | Per-feature corrective actions |

### `PriorityUpdatedPolicyWrapper`

```json
[{ "id": 12, "priority": 1 }, { "id": 7, "priority": 2 }]
```

## Endpoints

### Create and validate

#### `POST /`

Create a policy.

| | |
| --- | --- |
| Method | `addPolicy` |
| Scope | `pm:policies:add` |
| Body | `PolicyWrapper` |
| Success | `201 Created`, `Location: /policies/{id}`, body = created `Policy` |

| Status | When |
| --- | --- |
| `201` | Created |
| `400` | Null body, empty feature list, blank / mixed feature device types, bean validation failure, or feature validation failure (plain string `Policy feature/s validation failed. Features : …`) |
| `401` | Exactly one device in `deviceIdentifiers` and the caller is not authorised for that device |
| `500` | `PolicyManagementException`, device lookup failure, or Location URI failure |

#### `POST /validate`

Validate a feature list without persisting.

| | |
| --- | --- |
| Method | `validatePolicy` |
| Scope | `pm:policies:add` |
| Body | `ProfileFeature[]` |
| Success | `200`, plain string `Valid request` |

| Status | When |
| --- | --- |
| `200` | Valid |
| `400` | Empty list, blank / mixed device types, or feature validation failure |

---

### Read

#### `GET /`

All policies of the tenant, paginated in memory.

| | |
| --- | --- |
| Method | `getPolicies` |
| Scope | `dm:policies:view` |
| Query | `offset`, `limit` (`offset=0&limit=0` returns all) |
| Headers | `If-Modified-Since` (ignored) |
| Success | `200`, `PolicyList` `{ count, policies }`; `count` = total policies |

| Status | When |
| --- | --- |
| `400` | Invalid pagination |
| `500` | `PolicyManagementException` |

#### `GET /list`

Filtered, DB-paginated policy list. Used by the Endpoint Manager Policies page,
Apply Changes modal, and Groups assigned-policies modal.

| | |
| --- | --- |
| Method | `getPolicyList` |
| Scope | `dm:policies:view` |
| Success | `200`, `PolicyList` `{ count, policies }`; `count` = total matching all filters |

| Query | Type | Required | Notes |
| --- | --- | --- | --- |
| `name` | string | no | `LIKE %name%` |
| `type` | string | no | `GENERAL` / `CORRECTIVE` |
| `status` | string | no | `ACTIVE` → active = 1; any other value → active = 0 |
| `updated` | boolean | no | Default `false`. With `status`: `(ACTIVE = ? OR UPDATED = 1)`; alone: `UPDATED = 1` |
| `deviceType` | string | no | Profile device type |
| `groupId` | int | no | Only policies assigned directly to this device group. Absent / `≤ 0` = no filter. See [policy-mgt.md](../../Specs/policy-mgt.md#group-filter-on-the-policy-list) |
| `offset`, `limit` | int | no | Validated pagination |
| `If-Modified-Since` | header | no | Ignored |

| Status | When |
| --- | --- |
| `200` | Success (empty list allowed) |
| `400` | Invalid pagination |
| `404` | Non-numeric `groupId` / `offset` / `limit` (JAX-RS conversion) |
| `500` | `PolicyManagementException` |

Order: `PRIORITY ASC, ID`.

#### `GET /type/{policyType}`

All policies of one type, paginated in memory.

| | |
| --- | --- |
| Method | `getPolicies(policyType, …)` |
| Scope | `dm:policies:view` |
| Path | `policyType` — `GENERAL` / `CORRECTIVE` (not validated) |
| Query | `offset`, `limit` |
| Success | `200`, `PolicyList` |

| Status | When |
| --- | --- |
| `400` | Invalid pagination |
| `500` | `PolicyManagementException` |

#### `GET /{id}`

| | |
| --- | --- |
| Method | `getPolicy` |
| Scope | `pm:policies:details:view` |
| Headers | `If-Modified-Since` (ignored) |
| Success | `200`, `Policy` |

| Status | When |
| --- | --- |
| `404` | Policy not found (plain string `Policy not found.`) |
| `500` | `PolicyManagementException` |

#### `GET /effective-policy/{deviceType}/{deviceId}`

Policy currently applied to a device (`DM_DEVICE_POLICY_APPLIED`).

| | |
| --- | --- |
| Method | `getEffectivePolicy` |
| Scope | `pm:policies:effective-policy` |
| Success | `200`, `Policy` |

| Status | When |
| --- | --- |
| `404` | No applied policy (plain string `Policy not found for the requested device.`) |
| `500` | Device lookup or policy lookup failure |

---

### Update

#### `PUT /{id}`

| | |
| --- | --- |
| Method | `updatePolicy` |
| Scope | `pm:policies:update` |
| Body | `PolicyWrapper` |
| Success | `200`, plain string `Policy has successfully been updated.` |

Marks the policy as updated (unpublished) until `PUT /apply-changes`.

| Status | When |
| --- | --- |
| `400` | Same validation as create |
| `404` | Policy not found (plain string) |
| `500` | `PolicyManagementException` / device lookup failure |

#### `PUT /priorities`

| | |
| --- | --- |
| Method | `updatePolicyPriorities` |
| Scope | `pm:policies:priorities:update` |
| Body | `PriorityUpdatedPolicyWrapper[]` |
| Success | `200`, plain string `Policy Priorities successfully updated.` |

| Status | When |
| --- | --- |
| `404` | Core returned `false` (message says "Bad Request") |
| `500` | `PolicyManagementException`, or null body |

---

### Lifecycle

#### `POST /activate-policy`

| | |
| --- | --- |
| Method | `activatePolicies` |
| Scope | `pm:policies:activate` |
| Body | `int[]` policy ids |
| Success | `200` if **at least one** id exists; unknown ids are skipped |

| Status | When |
| --- | --- |
| `400` | Empty / null list |
| `404` | None of the ids exist |
| `500` | `PolicyManagementException` |

#### `POST /deactivate-policy`

Same contract as activate, with scope `pm:policies:deactivate` and method
`deactivatePolicies`. The `404` message currently reads
`Selected policies have not been activated.`

#### `PUT /apply-changes`

Publish pending changes (activate / deactivate / update / delete) to devices.

| | |
| --- | --- |
| Method | `applyChanges` |
| Scope | `pm:policies:change` |
| Body | `int[]` policy ids (set semantics) |
| Success | `200`, plain string `Changes have been successfully updated.` |

| Status | When |
| --- | --- |
| `400` | Null / empty set, null element, or id ≤ 0 (`ErrorResponse` code 400) |
| `400` | `InvalidPolicySelectionException` — ids not found for the tenant (neither current nor recently deleted) |
| `500` | `PolicyManagementException` |

#### `POST /remove-policy`

| | |
| --- | --- |
| Method | `removePolicies` |
| Scope | `pm:policies:remove` |
| Body | `int[]` policy ids |
| Success | `200`, plain string `Policies have been successfully deleted` |

All-or-nothing check: if any id is unknown, nothing is deleted.

| Status | When |
| --- | --- |
| `400` | Empty list, or any id not found (plain string `Policies does not exist.`) |
| `500` | `PolicyManagementException` |

## Endpoint Index

| HTTP | Path | Method | Scope |
| --- | --- | --- | --- |
| POST | `/` | `addPolicy` | `pm:policies:add` |
| POST | `/validate` | `validatePolicy` | `pm:policies:add` |
| GET | `/` | `getPolicies` | `dm:policies:view` |
| GET | `/list` | `getPolicyList` | `dm:policies:view` |
| GET | `/type/{policyType}` | `getPolicies` | `dm:policies:view` |
| GET | `/{id}` | `getPolicy` | `pm:policies:details:view` |
| GET | `/effective-policy/{deviceType}/{deviceId}` | `getEffectivePolicy` | `pm:policies:effective-policy` |
| PUT | `/{id}` | `updatePolicy` | `pm:policies:update` |
| PUT | `/priorities` | `updatePolicyPriorities` | `pm:policies:priorities:update` |
| POST | `/activate-policy` | `activatePolicies` | `pm:policies:activate` |
| POST | `/deactivate-policy` | `deactivatePolicies` | `pm:policies:deactivate` |
| PUT | `/apply-changes` | `applyChanges` | `pm:policies:change` |
| POST | `/remove-policy` | `removePolicies` | `pm:policies:remove` |

## Gaps To Fill

Known differences between current behaviour and a clean contract. Each is a
candidate ticket; update this section when one is fixed.

| # | Endpoint | Gap | Suggested direction |
| --- | --- | --- | --- |
| 1 | All GETs with `If-Modified-Since` | Header accepted, never evaluated; `304` documented in Swagger is never returned | Implement or remove the header / `304` from the interface |
| 2 | Several | Error bodies mix plain strings and `ErrorResponse` JSON | Return `ErrorResponse` everywhere |
| 3 | `POST /` | Unauthorised single-device policy returns `401`; caller is authenticated | Return `403` |
| 4 | `POST /`, `PUT /{id}` | `profile` null → `NullPointerException` in `validatePolicyDetails` → generic `500` | Validate `profile` and return `400` |
| 5 | `PUT /priorities` | No body validation (null → `500`); `false` from core → `404` with "Bad Request" text; missing `@Override` | Validate input, return `400`/`404` with accurate messages |
| 6 | `GET /effective-policy/...` | Unknown device → `null` passed to core → NPE → `500` | Return `404` when the device does not exist |
| 7 | `POST /activate-policy`, `/deactivate-policy` | Partial success returns `200` with no indication of skipped ids; deactivate `404` text says "activated" | Report skipped ids; fix message |
| 8 | `POST /remove-policy` | Builds the invalid-id list but discards it; deletes one-by-one without a transaction | Return the invalid ids; delete atomically |
| 9 | `GET /list` | `status` values other than `ACTIVE` silently mean inactive | Validate `ACTIVE` / `INACTIVE`, else `400` |
| 10 | `GET /list` (PostgreSQL) | `LIMIT ? OFFSET ?` binds offset first and limit second | Swap the bound parameters |
| 11 | `GET /list?groupId` | Group existence and caller access to the group are not checked; unknown ids return an empty list | Decide whether `404` / `403` is required |
| 12 | `GET /type/{policyType}` | Path value not validated | Return `400` for unknown types |
| 13 | `GET /`, `GET /type/...` | Load all policies then paginate in memory | Move to DB pagination (as `/list`) |
| 14 | Assignment | No endpoint to list policies by role, user or device; group filter covers direct group assignment only | Add filters to `/list` when needed |
| 15 | `POST /` | Duplicate policy name behaviour (`409`?) not defined at API level | Confirm with core and document |
