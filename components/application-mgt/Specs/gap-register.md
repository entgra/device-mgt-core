# Application Management Gap and Decision Register

Status: Draft handover; no issue below is fixed or accepted by Phase 1.
Baseline: core `293d35abfd`, proprietary commons `79b2891a67`.
Source keys resolve through [source map](source-map.md). Priority is recommended,
not a production incident attribution. Named assignees and approval dates are pending.

| ID | Classification / priority | Evidence and gap | Decision or next action | Accountable role |
| --- | --- | --- | --- | --- |
| APP-GAP-01 | Confirmed source behavior / P0 | TX.commitDBTransaction logs SQLException and returns; closeDBConnection throws without a connection. Shared constitution write example omits cleanup. Service catches/ownership vary. | Approve APP-X-002; characterize commit/acquisition/cleanup failures; then fix affected paths with preserved exceptions. Shared constitution correction is separate, not part of this documentation-only scope. | Component/platform owner |
| APP-GAP-02 | Concurrency risk, not reproduced / P0 | AM hash/cardinality/installable checks precede writes. MySQL AP_APP_RELEASE has no tenant/hash unique key. No cross-node proof is supplied by existing specs. | Decide duplicate request response and invariant enforcement per database. Test two concurrent publications/uploads; do not blindly add a wide hash index or generic distributed lock. | Component/database owner |
| APP-GAP-03 | Confirmed async contract gap / P0 | AM.createApplicationBasedOnRemoteStatus and triggerReleaseAsynchronously can commit an app then create the remote release on a new Thread; failure is logged. | Specify what initial success means, how users observe failure/completion, and restart/retry behavior. Preserve existing response until an approved compatibility decision. | Product/Publisher owner |
| APP-GAP-04 | Confirmed non-atomic boundaries / P0 | AM uploads before DB create; deleteApplication/deleteApplicationRelease remove files before commit. Cleanup attempts do not make filesystem and JDBC atomic. | Approve recovery and artifact ownership rules. Inject failures first; decide compensation/reconciliation rather than claiming rollback restores files. | Component/storage owner |
| APP-GAP-05 | Configuration/product decision / P1 | createRelease hard-codes publish steps. Packaged CUSTOM Released state exists, but Blocked/Deprecated successors name Published, not Released. | Validate per-type configured graph and decide CUSTOM recovery edges and custom-config support; no state/config changes authorized here. | Lifecycle/product owner |
| APP-GAP-06 | Scheduling/status consistency unknown / P0 | SUB filters existing operations before dispatch; status sync crosses APPM and device management; schedules are read/create-or-update by task name. | Define repeat request, terminal-state precedence, scheduler ownership, tenant key, timezone and crash/replay/cancellation semantics; test with two nodes. | Component/device-task owner |
| APP-GAP-07 | Existing behavior needing product confirmation / P1 | AM.deleteApplicationRelease clears all favourites for the parent app even when other releases may remain. Existing spec explicitly requires this. | Confirm intentional policy before changing it. Tests must distinguish retirement, hard deletion, deleting one release and deleting the app. | Product/Store owner |
| APP-GAP-08 | Source-observed async/range gaps / P1 | REVIEW.addReview guards rating > 0; duplicate policy is a precheck. updateReview dispatches rating recalculation before updateReviewInDB; add/delete also use threads. | Verify adapter upper bound; approve range, multi-review semantics and convergence/recovery behavior. Test concurrent/failed updates before claiming aggregate accuracy. | Review/product owner |
| APP-GAP-09 | Identity/version semantics unknown / P1 | AM firmware availability updates device version before APPM lookup. MySQL firmware model mapping is globally unique by model ID. | Confirm model-ID scope, version ordering, fallback policy and partial-failure implications with device-model owner. Do not presume semantic versions or per-tenant model IDs. | Firmware owner |
| APP-GAP-10 | Cross-repository contract coverage / P0 | Publisher creation/release handlers were sampled; full Store/admin/artifact endpoint and deployed-scope matrix was not exhaustively traced. Some older spec paths are abbreviated. | Inventory exact verbs/routes/scopes/DTO/error shapes for all consumers at pinned commits; authorize adapter work separately. UI behavior and runtime auth remain unverified. | API/security owners |
| APP-GAP-11 | Deployment/approval unknown / P0 | Production DB/isolation, applied migrations, storage topology, lifecycle overrides and nTask configuration are not supplied. No named spec approvers assigned. | Supply supported matrix and named reviewers; approve specs/deviations; obtain installed-product evidence in Phase 2. | Platform/component owner |
| APP-GAP-12 | External integration contracts unknown / P1 | SP and VPP entry points/local persistence inventoried; no live provider validation or exhaustive credential/error-path audit performed. | Pin provider versions, auth/secret policy, rate limits, timeouts, retries and reconciliation contracts; stub before live verification. | Integration/security owner |
| APP-GAP-13 | Confirmed test-evidence gap / P0 | ApplicationManagementTest has disabled empty methods; testAddApplication exercises DAO, not the full API. Lifecycle predicates and parser/storage utilities are only partial evidence. | Implement assertion-rich tests for acceptance criteria in Phase 2. Do not count named placeholders or a green build as acceptance coverage. | QA/component owner |

## Affected specifications

All engineering gaps above remain open pending the stated decisions/evidence.
The links below identify primary affected specs; their linked type-specific
details also apply. Update this mapping when a gap's scope changes.

| Gap | Affected specifications |
| --- | --- |
| APP-GAP-01 | [Cross-cutting](cross-cutting-requirements.md), [release](releases/release-management.md), [runtime](runtime/runtime.md) |
| APP-GAP-02 | [Creation](creation/creation-workflow.md), [release](releases/release-management.md), [lifecycle](lifecycle/lifecycle.md), [hash](releases/release-binary-hash.md), [single installable](releases/single-installable-release.md) |
| APP-GAP-03 | [Creation](creation/creation-workflow.md), [enterprise](creation/app-creation-enterprise.md), [custom](creation/app-creation-custom.md) |
| APP-GAP-04 | [Creation](creation/creation-workflow.md), [release](releases/release-management.md), [storage](storage/storage.md), [catalog](catalog/catalog.md) |
| APP-GAP-05 | [Lifecycle](lifecycle/lifecycle.md), [release](releases/release-management.md), [firmware](firmware/firmware.md), [custom](creation/app-creation-custom.md) |
| APP-GAP-06 | [Subscriptions and scheduling](subscriptions/subscriptions.md) |
| APP-GAP-07 | [Catalog](catalog/catalog.md), [favourite cleanup](catalog/release-delete-cleanup.md), [release](releases/release-management.md) |
| APP-GAP-08 | [Reviews](reviews/reviews.md) |
| APP-GAP-09 | [Firmware](firmware/firmware.md), [custom](creation/app-creation-custom.md) |
| APP-GAP-10 | [Creation](creation/creation-workflow.md), [catalog](catalog/catalog.md), [storage](storage/storage.md), [subscriptions](subscriptions/subscriptions.md), [cross-cutting](cross-cutting-requirements.md) |
| APP-GAP-11 | [Runtime](runtime/runtime.md), [architecture](architecture.md), [cross-cutting](cross-cutting-requirements.md) |
| APP-GAP-12 | [Integrations](integrations/integrations.md) |
| APP-GAP-13 | All requirement-bearing specs in the [verification matrix](verification-matrix.md); supplemental scenarios in the [spec register](spec-register.md) |

Resolving a clarification requires recording the approved decision and updating
the affected specification. Implementation and verification gaps additionally
require their stated evidence; a documentation edit does not close them.

## Corrections to prior documentation

The shared/type creation descriptions now link the remote-creation qualification.
Hash uniqueness and single-installable rules are recorded as intended invariants,
not established cross-node guarantees. Favourite cleanup remains current behavior,
not an invented fix. API references are resolved in source-map; detailed consumer
auditing is still an explicit gap. No prior unchecked acceptance item is marked
passed merely because its implementation path exists.

## Decision record format

For additional product-context, documentation, marketing and automation inputs,
see [product content readiness](product-content-readiness.md). Its APP-CONTENT
items supplement this engineering register; they do not resolve any APP-GAP item
or authorize Phase 2 implementation.

For each approved decision append: gap ID, selected behavior, alternatives rejected,
compatibility/migration impact, approver, date and implementing/verifying commit.
Current decision records: none. Documentation preparation is complete only as a
draft handover; owner sign-off cannot be supplied by an AI agent.

## Implementation-alignment findings

These additional items remain open. The associated functionality is now described;
that closes a documentation omission, not an implementation or verification issue.

| ID | Affected spec / requirements | Observed gap and next step | Owner |
| --- | --- | --- | --- |
| APP-GAP-14 | [Updates](releases/release-updates.md), APP-REL-004/006 | Public/custom package branch assigns version; web validator selects WEB_CLIP; null and sharing behavior need characterization. Custom changed-hash replacement closes inside the outer transaction flow. Record intended compatibility before fixes; coordinate APP-GAP-01/04. | Component/API/QA |
| APP-GAP-15 | [Search](releases/release-search.md), APP-REL-005 | Generic query has no stable ORDER BY; invalid type handling and early-empty pagination differ. Confirm supported dialect/adapter behavior, then decide ordering/error policy. | API/database |
| APP-GAP-16 | [Platform artifacts](storage/platform-artifacts.md), APP-STO-004/005 | Malformed metadata elements and XML-sensitive plist values need negative tests; establish adapter authorization before publishing download guarantees. | Storage/security |
| APP-GAP-17 | [Retirement](catalog/retirement.md), APP-CAT-005 | Eligibility is loaded before transaction; repeated calls, missing apps and visibility across lookup paths need characterization. | Component/product |
| APP-GAP-18 | [Reporting](subscriptions/reporting.md), APP-SUB-004/005 | Device statistics null/list empty; unsubscribe uses subscribed timestamp; effective filter and DB status inputs differ. Confirm intended helper-specific behavior and audit user/group/role membership, scopes and denominators. | Store/device/QA |

The [operation coverage inventory](operation-coverage.md) records partial coverage
rather than treating a mapped method as a fully specified method. Remaining partial
rows require operation-level contract review, including overloads and failure branches.
