# Policy Management Spec

## Purpose

Policies define a set of device features (restrictions, configurations,
compliance rules) for one platform and assign them to devices through roles,
users, device groups, or individual devices. The backend stores policies,
publishes changes to devices as operations, decides which policy is effective
for each device, and monitors compliance.

This file describes the domain behaviour. The REST contract (paths, scopes,
status codes) lives in
[policy_management_api_contract.md](../io.entgra.device.mgt.core.device.mgt.api/Specs/policy_management_api_contract.md).

## Audience And Components

| Concern | Detail |
| --- | --- |
| Layer | Policy Management core + Device Management API |
| Consumers | Endpoint Manager Policies pages, Groups view (assigned policies), device agents (via policy operations) |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| REST API | `device.mgt.api/.../service/impl/PolicyManagementServiceImpl.java` |
| List filter model | `device.mgt.common/.../PolicyPaginationRequest.java` |
| Admin point (PAP) | `policy.mgt.core/.../impl/PolicyAdministratorPointImpl.java` |
| Core manager | `policy.mgt.core/.../mgt/impl/PolicyManagerImpl.java` |
| Service facade | `policy.mgt.core/.../PolicyManagerServiceImpl.java` |
| Policy DAO | `policy.mgt.core/.../dao/impl/policy/` (`AbstractPolicyDAOImpl` + Generic / PostgreSQL / Oracle / SQLServer) |
| Enforcement | `policy.mgt.core/.../enforcement/PolicyEnforcementDelegatorImpl.java` |
| Decision point (PDP) | `policy.decision.point/.../simple/`, `.../merged/` |
| Information point (PIP) | `policy.information.point/` |
| Cache | `policy.mgt.core/.../cache/impl/PolicyCacheManagerImpl.java` |
| Compliance monitoring | `policy.mgt.core/.../mgt/impl/MonitoringManagerImpl.java`, `.../task/MonitoringTask.java` |

## Concepts

| Concept | Meaning |
| --- | --- |
| Policy | Name, description, priority, type, compliance mode, ownership type, active / updated flags, assignments |
| Profile | One per policy; holds the device type and the feature list |
| Profile feature | Feature code + payload for one platform; all features in a profile share the device type |
| Policy type | `GENERAL` or `CORRECTIVE` (corrective policies are linked through corrective actions) |
| Compliance | How non-compliance is handled (e.g. enforce / warn / monitor) |
| Ownership type | Restricts by enrolment ownership (e.g. `ANY`, `BYOD`, `COPE`) |
| Priority | Lower number = higher priority; used to pick the effective policy and to order lists |
| `active` | Policy participates in evaluation once published |
| `updated` | Policy has unpublished changes (create / update / activate / deactivate) |

## Assignment

A policy can target any combination of:

| Target | Table |
| --- | --- |
| Roles | `DM_ROLE_POLICY` |
| Users | `DM_USER_POLICY` |
| Device groups | `DM_DEVICE_GROUP_POLICY` |
| Devices | `DM_DEVICE_POLICY` |

Assignments are set through the `roles`, `users`, `deviceGroups` and
`deviceIdentifiers` fields of create / update. There are no separate
assignment endpoints.

## Lifecycle

1. **Create** (`POST /policies`) — validates the feature list, stores the policy,
   profile, features and assignments.
2. **Update** (`PUT /policies/{id}`) — replaces the policy definition and records
   it as updated.
3. **Activate / deactivate** — toggles `active`; takes effect on devices only
   after publishing.
4. **Apply changes** (`PUT /policies/apply-changes`) — validates the ids against
   the tenant (current policies or recently deleted ones), then schedules a
   one-shot delegation task. The task re-evaluates affected devices and sends
   policy operations (bundle or `POLICY_REVOKE`).
5. **Priorities** (`PUT /policies/priorities`) — rewrites priorities; the cache is
   repopulated when enabled.
6. **Remove** (`POST /policies/remove-policy`) — deletes policies when every
   requested id exists.

## Evaluation And Enforcement

| Step | Behaviour |
| --- | --- |
| Candidate policies | Active policies whose assignment (role, user, group, device) and ownership match the device |
| Effective policy | Chosen by the configured decision point: simple (highest priority) or merged (combined features) |
| Enforcement | `PolicyEnforcementDelegatorImpl` adds the policy operation, or `POLICY_REVOKE` when no policy applies |
| Applied record | `DM_DEVICE_POLICY_APPLIED` holds the policy currently applied per device / enrolment |
| Effective policy API | `GET /policies/effective-policy/{type}/{id}` reads the applied record |

## Compliance

`MonitoringTask` triggers compliance checks. Results are stored in
`DM_POLICY_COMPLIANCE_STATUS` and `DM_POLICY_COMPLIANCE_FEATURES`, and exposed by
the device APIs (`/devices/{type}/{id}/compliance-data`,
`/devices/compliance/{status}`).

## Persistence

| Table | Holds |
| --- | --- |
| `DM_POLICY` | Policy row (name, priority, type, `ACTIVE`, `UPDATED`, tenant) |
| `DM_PROFILE` | Profile (device type) |
| `DM_PROFILE_FEATURES` | Profile features |
| `DM_ROLE_POLICY`, `DM_USER_POLICY`, `DM_DEVICE_GROUP_POLICY`, `DM_DEVICE_POLICY` | Assignments |
| `DM_DEVICE_POLICY_APPLIED` | Applied policy per device enrolment |
| `DM_POLICY_CORRECTIVE_ACTION` | Corrective policy links |
| `DM_POLICY_CRITERIA`, `DM_CRITERIA`, `DM_POLICY_CRITERIA_PROPERTIES` | Criteria-based selection |
| `DM_POLICY_CHANGE_MGT` | Changes awaiting publish (used to resolve deleted ids on apply) |
| `DM_POLICY_COMPLIANCE_STATUS`, `DM_POLICY_COMPLIANCE_FEATURES` | Compliance results |

All queries are tenant-scoped (`TENANT_ID`).

## Policy List

`GET /policies/list` is the filtered, DB-paginated list. Filters (`name`,
`type`, `status`, `updated`, `deviceType`, `groupId`) are combined with AND;
`count` is the total of matching policies. Order is `PRIORITY ASC, ID`.

### Group filter on the policy list

`groupId` limits the list to policies assigned **directly** to one device group
(`DM_DEVICE_GROUP_POLICY`). Policies that reach the group's devices only through
roles, users, or direct device assignment are not included. Used by the
`@entgra/antd` `GroupPoliciesModal` (Endpoint Manager Groups view,
`showGroupPolicies`).

| Rule | Behaviour |
| --- | --- |
| Type | Primitive `int`; a missing query param is injected as `0`, so no null handling is needed |
| Disabled | `groupId` absent, `0`, or negative → no clause, identical SQL and parameters to before |
| API | `PolicyManagementServiceImpl.getPolicyList` sets `PolicyPaginationRequest.groupId` only when `> 0` |
| DAO | Every `getAllPolicies` (Generic, PostgreSQL, Oracle, SQLServer) and `getPolicyCount` add the clause and bind under the same `> 0` check |
| Binding order | After `deviceType`, before pagination |
| Duplicates | `EXISTS` keeps one row per policy, so paging and count are accurate |
| Unknown group | `200`, `count = 0`, empty list |
| Non-numeric | `404` (JAX-RS conversion) |

Clause:

```sql
AND EXISTS (SELECT 1 FROM DM_DEVICE_GROUP_POLICY GP
            WHERE GP.POLICY_ID = P.ID AND GP.DEVICE_GROUP_ID = ?)
```

| Case | Expected |
| --- | --- |
| No `groupId` | Same results and count as before the feature |
| Group with 3 assigned policies | `count = 3`; only those policies |
| `groupId` + `status=ACTIVE` | Only active policies assigned to the group |
| Policy reaching the group only via a role | Not returned |

## Gaps To Fill

Areas this spec does not yet pin down, or where current behaviour needs a
decision. API-level gaps are tracked in the
[API contract](../io.entgra.device.mgt.core.device.mgt.api/Specs/policy_management_api_contract.md#gaps-to-fill).

| # | Area | Gap |
| --- | --- | --- |
| 1 | Evaluation | Exact candidate-selection rules (role vs user vs group vs device precedence, ownership, criteria) are not documented; confirm from PIP / PDP code |
| 2 | Evaluation | Which decision point (simple / merged) is configured by default and where the switch lives |
| 3 | Merged policies | How conflicting feature values are merged is not specified |
| 4 | Publishing | Retry / failure behaviour of the delegation task and how failures surface to admins |
| 5 | Publishing | Whether `apply-changes` on a deactivated policy always sends `POLICY_REVOKE` or re-evaluates to the next policy |
| 6 | Corrective policies | Trigger conditions and lifecycle of corrective actions |
| 7 | Compliance | Monitoring frequency, per-platform behaviour, and how `compliance` mode changes enforcement |
| 8 | Cache | When the policy cache is enabled and which operations invalidate it (priorities do; confirm others) |
| 9 | Validation | Duplicate policy names and priority collisions on create are not defined |
| 10 | Group filter | Group existence and caller access to the group are not checked on `/list?groupId` |
| 11 | Group filter | No inherited assignment: policies of a parent group are not returned for a child group |
| 12 | Pagination | PostgreSQL `getAllPolicies` binds offset and limit in the wrong order (`LIMIT ? OFFSET ?`) |
| 13 | Removal | Removal effect on already-applied devices before `apply-changes` is not documented |
| 14 | Criteria | Criteria-based policies (`DM_POLICY_CRITERIA`) are not exposed through the REST API; usage unclear |

## Related Specs

- API contract: `device.mgt.api/Specs/policy_management_api_contract.md`
- Endpoint Manager Groups view: `emm-proprietary-plugins/.../endpoint.mgt.ui/Specs/groups-view.md`
- Endpoint Manager Policies list: `emm-proprietary-plugins/.../endpoint.mgt.ui/Specs/policies-list.md`
