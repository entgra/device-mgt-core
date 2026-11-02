# Single Installable Release Spec

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

Requirements: [APP-LC-002/003](../lifecycle/lifecycle.md).
The guard below is observed, but concurrent-publication safety is unverified.
The ForbiddenException/cleanup scenario applies to the synchronous enterprise add-release path;
do not apply it universally to initial creation or remote background failures.
Artifact compensation is an attempted separate operation, not JDBC rollback.

## Purpose

Non-CUSTOM applications allow only one **installable** release (typically
`PUBLISHED`) at a time. Creating a new release with `isPublished=true` must not
advance that release into the installable state if another installable release
already exists.

## Product Rule

In `changeLifecycleState`, when the target action is the installable state and
`hasExistInstallableAppRelease(...)` is true for the application, throw
`ForbiddenException`.

`createRelease(..., isPublished=true)` walks `IN-REVIEW` → `APPROVED` →
`PUBLISHED` (enterprise). The Forbidden check fires on the last step.

CUSTOM / firmware skips this installable uniqueness check.

## Behaviour

| Case | Expected |
| --- | --- |
| Create release with `isPublished=false` while one is PUBLISHED | ApplicationRelease returned; new release in initial state |
| Create release with `isPublished=true` while one is PUBLISHED | `ForbiddenException` on synchronous enterprise add-release; release transaction rollback and stored-binary deletion are attempted; cleanup failure is separate |
| Create release with `isPublished=true` and no installable release | ApplicationRelease returned; new release ends PUBLISHED |

## Acceptance Criteria

- [ ] Synchronous enterprise add-release preserves ForbiddenException for the installable conflict.
- [ ] Failed publish-on-create rolls back the release transaction.
- [ ] Artifact cleanup still runs for the failed create path.

The current DAO tests for any release in the application's installable state; it
does not exclude the supplied release UUID. A manual PUBLISHED->PUBLISHED request
can therefore reject itself even though the lifecycle predicate permits a
permission-authorized same-state transition. APP-GAP-20 records the intended
idempotency decision. This is distinct from publishing a new release while
another one is installable; see [lifecycle details](../lifecycle/lifecycle.md#enterprise-transition-contract).
