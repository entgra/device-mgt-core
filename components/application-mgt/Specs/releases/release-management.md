# Release Management Baseline

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md) | [Sources](../source-map.md)

## Observed contract

Type-specific entry points resolve the parent app and prepare artifacts before
createRelease. The release type must agree with the application's type. Enterprise
and custom allow multiple releases; public/web creation rejects another release
when one already exists. These are checks against loaded state, not proof of
concurrency-safe cardinality.

createRelease begins an APPM transaction, inserts a release and initial lifecycle
history, optionally advances states using the DTO lifecycle overload, then commits.
It explicitly preserves ForbiddenException; the synchronous enterprise wrapper
attempts artifact deletion when createRelease fails. Other paths require separate
failure characterization rather than an assumption of universal compensation.

Update operations have type-specific wrappers. Installer/image replacement is
distinct from metadata modification. Deletion first loads the app/release, checks
deletable state and device-subscription history, then removes lifecycle/release
records and application-level favourites. Artifact deletion happens before commit;
icon-info cleanup can happen after the main operation. Failure can cross storage
and transaction boundaries (APP-GAP-04).

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-REL-001 | Observed; concurrent guarantee proposed | Reject type mismatch and a second public/web release; enterprise/custom retain multiple-release support. | AC-APP-REL-001: create by type with zero/one existing release; race two public/web requests and verify the approved one-release invariant. |
| APP-REL-002 | Existing spec intent; race protection unverified | Stored installer hash checks are tenant-wide, including across apps; a UI precheck does not reserve a hash. See [hash spec](release-binary-hash.md). | AC-APP-REL-002: same/different app and tenant fixtures, skipped precheck, and competing identical uploads; verify conflict handling and surviving artifact ownership. |
| APP-REL-003 | Observed guards; recovery proposed | Delete only permitted, unsubscribed releases; preserve related-row consistency and explicitly handle storage failure. | AC-APP-REL-003: delete allowed/blocked/subscription-history releases; assert lifecycle/favourites behavior; inject storage failure and commit failure independently. |
| APP-REL-004 | Observed entry points; full edge validation pending | Release updates follow type-specific identity and conditional editability checks; see APP-REL-006 for observed fields and anomalies. Artifact isolation remains a proposed guarantee pending APP-GAP-04. | AC-APP-REL-004: metadata and artifact updates per type in editable/noneditable states; missing/cross-tenant UUID; fail between replacement and DB update. |

## Boundaries

Apply APP-X-001 through APP-X-004. [Lifecycle](../lifecycle/lifecycle.md) owns transition rules;
[storage](../storage/storage.md) owns file behavior; [favourite cleanup](../catalog/release-delete-cleanup.md)
records the existing all-users/app-level deletion policy. Whether that policy should
change when another usable release remains is APP-GAP-07, not an automatic fix.

HTTP mapping is adapter-specific. The inspected enterprise/custom add-release
handlers map ForbiddenException to 403; do not assume every adapter has identical
exception handling. Full adapter matrix completion is APP-GAP-10.

Detailed compatibility: [release updates](release-updates.md) and [release search](release-search.md).
