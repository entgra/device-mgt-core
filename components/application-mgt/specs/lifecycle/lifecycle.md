# Publication and Lifecycle

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

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

## Enterprise transition contract

LifecycleStateManager indexes configured names/successors in uppercase and matches
state changes ignoring case. getInitialState returns a configured initial state;
it does not validate that exactly one initial state exists. getInstallableState
requires exactly one installable state applicable to the requested known app type.
The transition predicate itself does not enforce ApplicableTypes for every edge.
Thus the packaged graph is not a complete per-type authorization policy.

| Condition | Source-observed behavior |
| --- | --- |
| Missing changer or null/empty action | BadRequestException before transaction acquisition; whitespace is not normalized by this check |
| Unknown/missing tenant release in UUID entry point | NotFoundException; no foreign-tenant fallback |
| Target permission | Resolve permission for requested destination; authorize current user in current tenant realm |
| Missing target permission/state | LifecycleManagementException |
| Permission denied or invalid edge | Predicate false; manager throws ApplicationManagementException rather than a dedicated ForbiddenException |
| Same state requested with permission | Predicate true without successor lookup; persistence/uniqueness checks still run |
| Enter any installable state for non-CUSTOM | Query for a release at the app type's configured installable state; any match causes ForbiddenException |
| Query includes current release | Repeating PUBLISHED on the already-PUBLISHED release can reject itself; this is not an idempotent publication guarantee |
| Successful transition | Update current release state; append history with previous/current state, actor and supplied reason |

The UUID overload begins/attempts commit/closes its own APPM transaction. The DTO
overload neither acquires nor completes a transaction and uses the caller's DTO
and connection. Neither path establishes a locking read or optimistic version
check. Two same-release transitions or two competing publications can read stale
state; concurrency outcomes require APP-X-003 evidence.

The installable query's outer predicate uses tenant ID, but its app-ID subquery
uses UUID alone; the inspected MySQL schema has no UUID unique key. Generated UUIDs
and tenant lookups are not a substitute for testing duplicate/imported identifiers
and actual schema constraints (APP-GAP-02/20).

## Publication and history acceptance

For the packaged enterprise defaults, create without publication stores CREATED
and one CREATED->CREATED history row. Auto-publication requests IN-REVIEW,
APPROVED, PUBLISHED; each success adds a row with reason `Updated to <STATE>`.
With successful persistence/commit, four total rows describe creation plus those
three transitions. isPublished=true with no release writes no lifecycle history.

Auto-publication requires permission at every destination; it does not bypass
review/approval permission merely because creation was authorized. No permission
check for the initial CREATED->CREATED insert is performed by the state-change
predicate. Effective caller authentication remains a separate trust boundary.

For manual transitions, the manager assigns the caller's action to the DTO and
retains the supplied reason. The generic release/history DAOs uppercase persisted
states, while the returned DTO can retain the action's original casing. Check
stored state, history and returned entity together with mixed-case input instead
of assuming identical casing or synthesized reasons. Permission and graph
rejection must be characterized separately from the installable guard.

APP-LC-001/003's no-partial-state acceptance is an intended guarantee requiring
fault evidence: the UUID/createRelease paths do not explicitly roll back every
broad management/lifecycle exception, and commit errors can be logged without
propagation. No failed-transition atomicity claim follows solely from transaction
syntax. APP-GAP-01/05/20 retain these decisions.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-LC-001 | Observed | Validate transitions and caller permission against configured lifecycle rules; record previous/new state, actor and reason. | AC-APP-LC-001: allowed/disallowed edge and authorized/unauthorized actor; successful transition updates current state and history together; failed transition changes neither. |
| APP-LC-002 | Existing product rule; concurrent guarantee proposed | At most one non-CUSTOM release is installable for an application; retain the CUSTOM exception unless separately approved. | AC-APP-LC-002: publish with/without an existing installable release; race two releases; assert invariant and defined loser response, not only sequential prechecks. |
| APP-LC-003 | Observed defaults; configuration gap | Publish-on-create follows type-specific targets and must expose failure without a partial release/history result. | AC-APP-LC-003: enterprise/custom auto-publish, forbidden final step and modified lifecycle configuration; record rollback and artifact compensation outcomes. |

Existing [single-installable spec](../releases/single-installable-release.md) remains
the compatibility reference. APP-X-002/003 and APP-GAP-01/02/05 apply. Existing
LifecycleManagementTest predicates are partial evidence only; see [matrix](../verification-matrix.md).
Enterprise subcases include permission denial, missing configuration, repeated
publication, same-release races and history failures; see the
[case matrix](../verification-matrix.md#enterprise-creation-and-publication-cases).
