# Subscriptions, Installation and Scheduling

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md) | [Sources](../source-map.md)

## Scope and observed flow

performBulkAppOperation validates target/action inputs, resolves the application,
expands subscribers/devices, filters existing activity, invokes external-store
subscription work and dispatches device actions. Results include activities,
ignored devices and error identifiers; a successful API response does not prove
the device installed the application. Enrollment installation has a separate
installEnrollmentApplications path.

Pending/in-progress matching operations are consulted to avoid redispatch in
ordinary repeated calls. This is not demonstrated as an atomic cross-node claim.
AP_DEVICE_SUBSCRIPTION has a device/release uniqueness constraint in the MySQL
script, but that constraint alone does not make external/device dispatch exactly-once.

Subscription status synchronization bridges APPM and device-management operations.
updateSubscriptionStatus calls ApplicationManager.updateSubStatus and then updates
operation statuses through device management. Separate resources/calls mean a
failure between them needs recovery semantics.

Scheduling reads pending work by task name and creates or updates it; cleanup and
execution use Carbon tasks plus persisted schedule rows. Scheduling, claiming,
device dispatch and completion are separate events. Runtime scheduler topology,
restart replay and stale-owner fencing are not established by this source baseline.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-SUB-001 | Observed | Validate action/target/release, resolve authorized compatible devices and distinguish dispatched, ignored and failed targets; do not equate dispatch with installation. | AC-APP-SUB-001: device/user/role/group inputs, mixed compatibility/authorization, INSTALL/UNINSTALL and enrollment fixtures; inspect activities and eventual reported device state separately. |
| APP-SUB-002 | Observed sequential filtering; concurrent guarantee proposed | Define repeat-request and retry semantics so duplicate operations are prevented or explicitly reconciled for the same target/action/release. | AC-APP-SUB-002: repeat while pending/in-progress, race two nodes, and retry after a dispatch timeout; count operations and subscription rows, not only HTTP success. |
| APP-SUB-003 | Observed two-step sync; recovery proposed | Keep subscription and device-operation status reconcilable when either update fails; define terminal/out-of-order response handling. | AC-APP-SUB-003: duplicate, delayed and out-of-order device responses; fail after APPM update before operation update; assert approved recovery and monotonicity policy. |
| APP-SCH-001 | Observed persisted schedule; ownership proposed | Define schedule identity/tenant/time semantics, claim ownership and restart behavior; expired ownership must not permit conflicting completion. | AC-APP-SCH-001: create/update/cancel-equivalent behavior as supported, two executors at due time, owner crash/restart and cleanup during execution; inspect one logical scheduled result or documented idempotent recovery. |

## Decisions

APP-GAP-06 must settle replay, retry limits, cancellation support, terminal-state
precedence, task-name tenant uniqueness and timezone semantics. Do not invent an
unsupported cancel API or promise singleton execution. APP-X-001/002/003/004 apply;
planned tests are in [verification matrix](../verification-matrix.md).

[Reporting contracts](reporting.md) specify helper dispatch, response differences and history/count semantics.
