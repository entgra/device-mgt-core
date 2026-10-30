# Release Management Baseline

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

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
| APP-REL-002 | Existing spec intent; race protection unverified | Stored installer hash checks are tenant-wide, including across apps; a caller precheck does not reserve a hash. See [hash spec](release-binary-hash.md). | AC-APP-REL-002: same/different app and tenant fixtures, skipped precheck, and competing identical uploads; verify conflict handling and surviving artifact ownership. |
| APP-REL-003 | Observed guards; recovery proposed | Delete only permitted, unsubscribed releases; preserve related-row consistency and explicitly handle storage failure. | AC-APP-REL-003: delete allowed/blocked/subscription-history releases; assert lifecycle/favourites behavior; inject storage failure and commit failure independently. |
| APP-REL-004 | Observed entry points; full edge validation pending | Release updates follow type-specific identity and conditional editability checks; see APP-REL-006 for observed fields and anomalies. Artifact isolation remains a proposed guarantee pending APP-GAP-04. | AC-APP-REL-004: metadata and artifact updates per type in editable/noneditable states; missing/cross-tenant UUID; fail between replacement and DB update. |

## Boundaries

Apply APP-X-001 through APP-X-004. [Lifecycle](../lifecycle/lifecycle.md) owns transition rules;
[storage](../storage/storage.md) owns file behavior; [favourite cleanup](../catalog/release-delete-cleanup.md)
records the existing all-users/app-level deletion policy. Whether that policy should
change when another usable release remains is APP-GAP-07, not an automatic fix.

Exception propagation is path-specific: the enterprise additional-release wrapper
preserves ForbiddenException, while initial creation can wrap broader management
failures. Compare service exceptions at the actual entry point.

Detailed compatibility: [release updates](release-updates.md) and [release search](release-search.md).

## Enterprise release persistence

The [enterprise contract](../creation/app-creation-enterprise.md) owns validation,
artifact identity and initial/additional/remote caller behavior. The following
defines the persistence boundary under APP-REL-001/002 and APP-LC-001/003.

| Concern | Initial application with release | Additional enterprise release |
| --- | --- | --- |
| Transaction owner | triggerApplicationCreation | createRelease |
| Parent | Insert AP_APP using authenticated tenant, then category/tag/visibility mappings | Resolve tenant parent first; exact ENTERPRISE type required; no new AP_APP row |
| Release | addAppDataIntoDB selects the first DTO | Supplied prepared DTO; no one-release cardinality restriction for ENTERPRISE |
| State/history | Configured initial state; initial history current=previous; optional hard-coded publication steps | Same state/history sequence |
| Completion entity | Application with zero/one selected release | ApplicationRelease; remote dispatch has separate empty response behavior |
| Other repository write | Initial enterprise icon info through device-management service | No corresponding persistAppIconInfo call in createRelease |

GenericApplicationReleaseDAOImpl.createRelease binds the supplied tenant and app
ID, generated UUID, type/identity, price, artifact names, metadata, OS range and
uppercased initial state. It inserts AP_APP_RELEASE and assigns the generated ID.
The lifecycle DAO inserts AP_APP_LIFECYCLE_STATE with tenant, actor, timestamp,
release ID, previous/current state and optional reason. DAOs use the factory's
connection and do not commit; addAppDataIntoDB and the DTO transition overload
depend on their caller's transaction. Direct DTO callers must honor these
preconditions rather than assume those helpers are standalone atomic services.

The supplied MySQL schema has a release-to-application FK and history-to-release
FK, both NO ACTION. It does not define unique keys for UUID, tenant/hash,
application/installable state or application/version. Do not infer DB-enforced
uniqueness, version deduplication, or a supported product schema solely from this
script. Other engines and applied migrations require their own evidence.

Hash existence is tenant-wide with no end-state exclusion. The first-application
package check excludes the configured end state, while isNewRelease=true skips it.
Remote initial releases use that latter path too. No check here establishes that
every additional release retains its parent's prior package identity.

## Transaction and retry acceptance

Under ordinary successful execution, unpublished enterprise persistence creates
one initial history row; publish-on-create adds three transition rows. Test both
creation paths and inspect the persisted rows, not only the returned DTO.
For acquisition, insert, history, transition, icon-write, commit, rollback and close
failures, inspect surviving APPM rows, device icon rows, files and connection state.
Explicit rollback catches differ by path; a connection close is not evidence of
successful rollback, and commit return is qualified by APP-GAP-01.

Concurrent name/package/hash/publication checks require independent connections
and processes. A prior read is not a reservation. Repeated calls have no declared
idempotency key; record current outcomes before choosing duplicate-response or
reconciliation behavior. See [verification cases](../verification-matrix.md#enterprise-creation-and-publication-cases)
and APP-GAP-01/02/04/19/21. No new enforcement mechanism is selected by this contract.
