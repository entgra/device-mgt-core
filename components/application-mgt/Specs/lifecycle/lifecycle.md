# Publication and Lifecycle

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md) | [Sources](../source-map.md)

## Configured graph

The following is the packaged XML graph, not a guarantee for every deployment.
The manager normalizes/configures state handling; applicable types, permissions
and state flags also affect valid transitions.

| State | Configured successors | Notable flag |
| --- | --- | --- |
| Created | In-Review | Initial, updatable, deletable |
| In-Review | Rejected, Approved, Created | Review permission |
| Approved | In-Review, Published, Released | Approval permission |
| Published | Blocked, Deprecated | Installable for ENTERPRISE/PUBLIC/WEB_CLIP/WEB_APP |
| Released | Blocked, Deprecated | Installable for CUSTOM |
| Rejected | In-Review | Deletable |
| Blocked | Published, Deprecated | No configured Released edge |
| Deprecated | Published, Retired | No configured Released edge |
| Retired | None | End state; not equivalent to hard delete |

CUSTOM recovery from Blocked/Deprecated and per-type graph enforcement need an
owner decision; do not invent additional edges (APP-GAP-05).

## Observed execution

changeLifecycleState(uuid, changer) owns a transaction and loads current state.
It validates action and transition/permission, checks another installable release
for non-CUSTOM apps, updates the release and adds lifecycle history before commit.
The DTO overload does not open/commit its own transaction: creation callers supply
the transaction. Non-CUSTOM uniqueness uses a read/check before update; concurrent
proof is absent. CUSTOM deliberately skips that check.

createRelease auto-publication uses hard-coded IN-REVIEW -> APPROVED -> PUBLISHED
(or RELEASED for CUSTOM), despite configurable lifecycle states. Therefore custom
configuration compatibility needs verification, not a promise of arbitrary graphs.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-LC-001 | Observed | Validate transitions and caller permission against configured lifecycle rules; record previous/new state, actor and reason. | AC-APP-LC-001: allowed/disallowed edge and authorized/unauthorized actor; successful transition updates current state and history together; failed transition changes neither. |
| APP-LC-002 | Existing product rule; concurrent guarantee proposed | At most one non-CUSTOM release is installable for an application; retain the CUSTOM exception unless separately approved. | AC-APP-LC-002: publish with/without an existing installable release; race two releases; assert invariant and defined loser response, not only sequential prechecks. |
| APP-LC-003 | Observed defaults; configuration gap | Publish-on-create follows type-specific targets and must expose failure without a partial release/history result. | AC-APP-LC-003: enterprise/custom auto-publish, forbidden final step and modified lifecycle configuration; record rollback and artifact compensation outcomes. |

Existing [single-installable spec](../releases/single-installable-release.md) remains
the compatibility reference. APP-X-002/003 and APP-GAP-01/02/05 apply. Existing
LifecycleManagementTest predicates are partial evidence only; see [matrix](../verification-matrix.md).
