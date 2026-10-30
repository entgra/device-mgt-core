# Enterprise Application Creation

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Scope and authority

This contract covers enterprise application creation, initial release persistence,
additional enterprise releases and publication through the core service boundary.
It supplements [APP-CRT-001 through APP-CRT-004](creation-workflow.md),
[APP-REL-001/002](../releases/release-management.md),
[APP-LC-001 through APP-LC-003](../lifecycle/lifecycle.md), APP-STO-001/002 and APP-X-001
through APP-X-004. Those requirements remain canonical; scenarios below refine them.

The described branches are source-observed compatibility behavior. Proposed recovery
and concurrency guarantees remain decisions in the gap register. Review and
verification are collaborative; documenting a branch does not approve an anomaly
or establish that acceptance has passed.

PUBLIC, WEB_CLIP/WEB_APP and CUSTOM creation are separate contracts. Device
installation and subscription status reporting are outside this contract. Publication
means lifecycle state/history persistence, not deployment to a device.

## Entry points and caller obligations

| Operation | Input | Observed result |
| --- | --- | --- |
| validateAppCreatingRequest | ApplicationWrapper in established Carbon tenant/user context | Validates application and first release; returns no entity or throws a domain/validation exception |
| createApplication | ApplicationWrapper, isPublished | Application response; does not call the application validator itself |
| validateEntAppReleaseCreatingRequest | EntAppReleaseWrapper, deviceType | Validates the enterprise release; does not persist it |
| createEntAppRelease | Existing app ID, EntAppReleaseWrapper, isPublished | Synchronous ApplicationRelease, or a new empty ApplicationRelease after remote dispatch |
| changeLifecycleState | Release UUID and LifecycleChanger | Updated release after transition; see the lifecycle contract for permission and transaction differences |

Callers establish authenticated tenant/user context and invoke the applicable
validator before creation. Core methods have different validation responsibilities;
calling createRelease with arbitrary DTOs is not equivalent to using the validated
enterprise pipeline. getApplication resolves the parent under the current tenant,
but its lookup does not itself enforce unrestricted-role visibility. Caller-side
authorization is a separate trust boundary (APP-GAP-10).

The wrapper classes are the field/schema reference; do not infer enforcement from
@NotNull annotations alone. In this link-based pipeline artifactLink, iconLink,
bannerLink and screenshotLinks are downloaded through the configured downloader.
The presence of Base64 fields and separate artifact validators does not mean this
entry point consumes those fields or automatically runs those validators.

## Validation and identity

| Concern | Source-observed rule | Boundary or exceptional behavior |
| --- | --- | --- |
| Name | Null/empty rejected; maximum 20 characters before conversion; DTO conversion trims the name | Whitespace-only input is not explicitly rejected here; name precheck uses the original input. Normalization and collation equivalence need characterization |
| Categories | Non-null/nonempty list; names must match registered tenant categories | No registered categories raises ApplicationManagementException; an unregistered requested name raises BadRequestException |
| Device type | Null/empty rejected; resolved through device-management services | Registered type is not proof that the installer parser supports that platform |
| Roles | Supplied nonempty roles must exist; caller must hold at least one unless allowed to view all roles | Invalid role names raise ApplicationManagementException; lack of membership raises BadRequestException |
| Initial releases | Empty list creates an application without a release; validation and persistence select the first entry | Multiple entries are not a batch-create contract. Explicit null lists/elements and extra entries require negative characterization |
| Supported OS | Nonempty; lower endpoint must exist; upper endpoint must exist unless exact ALL | Parser splits on '-' and indexes two elements. It does not explicitly establish ordered bounds or robust malformed-range errors |
| Windows identity | Version and package name must be non-null in the validator | Empty strings are not rejected by these null checks |
| Installer platform | Android/iOS version and package come from parsed binary; Windows retains wrapper identity and permits MSI/APPX/EXE extensions ignoring case | Missing links/files, malformed installers and unsupported platforms are not uniformly request-validation exceptions |
| Package precheck | Initial synchronous creation checks same package in the tenant with state different from configured end state | Query is not limited to ENTERPRISE/device type. Additional releases and the remote initial-release path use isNewRelease=true and skip this package precheck |
| Binary identity | MD5 calculated from installer; matching tenant release hash rejected, regardless of parent app or lifecycle state | Precheck does not reserve the hash; MD5 is duplicate identity, not an authenticity guarantee |
| Release metadata | Description, type, price, sharing, metadata and OS values copied from wrapper | Enterprise create validator does not validate all these fields; DAO conversion/nulls and schema limits can fail later |

A release UUID is generated during artifact preparation. Installer storage is
keyed by tenant/hash, not by UUID. Windows MSI hash metadata follows
[platform artifact contracts](../storage/platform-artifacts.md).

## Execution and completion

### Synchronous initial creation

1. Prepare the first release if present: resolve artifact links, parse identity,
   check package/hash, store the installer and optional images.
2. Begin the APPM transaction after artifact preparation.
3. Insert the application, category mappings, optional unrestricted roles and tags.
4. If a release exists, insert it in the configured initial state and add a history
   row whose current and previous states both equal that initial state.
5. If isPublished=true, invoke the DTO lifecycle overload for IN-REVIEW, APPROVED,
   PUBLISHED in order. Each step checks the destination permission and graph.
6. Persist application icon information through device management for the initial
   enterprise release. This uses a separate DAO factory/transaction.
7. Convert the application response and attempt the APPM commit.

With an empty release list, isPublished=true does not create a release or lifecycle
history. With ordinary successful commit and packaged defaults, unpublished creation
has one CREATED release/history row; published creation has one PUBLISHED release
and four history rows. APP-GAP-01 qualifies any success inferred from commit return.

### Additional enterprise release

The core resolves artifact links and the tenant parent app, derives the device type
from that parent, prepares artifacts with isNewRelease=true, then calls createRelease.
The parent must be ENTERPRISE; multiple enterprise releases are allowed. Release and
history use a new APPM transaction; the existing application is not recreated.
The synchronous wrapper attempts hash-directory deletion if createRelease throws
ForbiddenException, BadRequestException or another ApplicationManagementException.
The independent icon-info call made during initial creation is not made by this path.

### Remote enterprise release

For initial creation, remoteStatus on the first wrapper causes the release list to
be temporarily removed while an application-only transaction commits. The original
list is restored for dispatch of its first release. A new thread copies tenant ID
and username, starts a tenant flow and ends it in finally. The response is the
previously created application response; it is not refreshed after worker completion.

Additional-release remote dispatch returns a new empty ApplicationRelease. Parent
lookup, artifact checks and publication occur in the worker, so immediate success
can precede even detection of an unknown app or conflicting binary. Caught worker
management/resource failures are logged rather than returned to the initial caller.
No durable completion record, restart replay or bounded retry is established by this
path. Retrying initial creation can encounter the already-created application name.
Do not invent durable completion tracking or automatic retry policy (APP-GAP-03).

## Failure boundaries

| Boundary | Observed handling | Acceptance implication |
| --- | --- | --- |
| Artifact preparation before APPM transaction | Runs outside triggerApplicationCreation's try/finally | Do not promise its transaction catch blocks clean partial downloads/uploads |
| Initial APPM creation | Selected failures attempt rollback and artifact deletion; management exceptions are wrapped | Cleanup can fail or mask the primary exception; no filesystem/JDBC atomicity |
| Additional release persistence | Forbidden/DAO/history failures explicitly roll back; some broader lifecycle/management failures reach close without an explicit rollback branch | Assert actual cleanup and pool behavior; do not infer universal rollback from finally |
| Publication permission/graph | Rejected predicate produces ApplicationManagementException; installable guard produces ForbiddenException | Authorization denial is not uniformly ForbiddenException |
| Initial icon-info write | Device-management transaction can commit before APPM commit | APPM rollback cannot establish icon-info rollback; APP-GAP-21 |
| Commit/acquisition/cleanup | Existing connection utility can log commit errors and throw on close without a connection | False success, masked errors and ownership remain APP-GAP-01; proposed safe outcomes are not current guarantees |
| Concurrent/repeated creation | Name/package/hash/installable checks are ordinary reads before writes | No cross-node uniqueness, idempotency key or exactly-once guarantee is established |

## Acceptance scenarios

Executable scenario details and evidence status are maintained in the
[enterprise verification cases](../verification-matrix.md#enterprise-creation-and-publication-cases).
They cover empty/single/multiple release input, platform identity, validation,
sequential conflicts, tenant boundaries, initial/additional/remote creation,
publication history/permissions, fault injection and independent transactions.
No scenario is marked passed from source inspection. Decisions for malformed input,
repeat publication and icon consistency remain in APP-GAP-19/20/21; recovery and
concurrency decisions remain APP-GAP-01 through APP-GAP-05.
