# Acceptance Verification Matrix

Status: Acceptance planning and candidate evidence only. No tests added, changed or executed.

The [spec register](spec-register.md) describes review scope and remaining work.
Review and verification are collaborative; no named assignment is a prerequisite.

Each acceptance ID is `AC-` plus the requirement ID. The governing specification
contains the fixture/action/expected-outcome scenario; the Phase 2 work item supplies
execution grouping. Existing test references are candidates/partial evidence, never
Passed. Spec review and runtime verification are separate states.

Existing test aliases below identify candidate evidence, not passed acceptance.
Check inclusion in the [configured TestNG suite](../io.entgra.device.mgt.core.application.mgt.core/src/test/resources/testng.xml)
and inspect assertions before relying on a test.

- AMT: [ApplicationManagementTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/management/ApplicationManagementTest.java).
- DAOT: [ApplicationManagementDAOTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/dao/ApplicationManagementDAOTest.java).
- LCT: [LifecycleManagementTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/LifecycleManagementTest.java).
- PAR: [ArtifactParserTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/ArtifactParserTest.java).
- STU: [StorageManagementUtilTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/StorageManagementUtilTest.java).
- CFG: [ConfigurationTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/ConfigurationTest.java).

| Requirement / acceptance suffix | Governing spec | Existing evidence (not run) | Required verification | Phase 2 |
| --- | --- | --- | --- | --- |
| APP-CRT-001 | [Creation](creation/creation-workflow.md) | AMT.createApplication partial enterprise fixture; DAOT.testAddApplication insertion only | Unit validation by type + service/DB fixtures | P2-02 |
| APP-CRT-002 | [Creation](creation/creation-workflow.md) | No mapped full service-boundary assertions | Validator/result/exception fixtures for all four wrapper families | P2-03 |
| APP-CRT-003 | [Creation](creation/creation-workflow.md) | No mapped failure injection | DB/storage fault injection | P2-04 |
| APP-CRT-004 | [Creation](creation/creation-workflow.md) | No mapped async completion assertions | Controlled downloader/worker + restart integration | P2-04 |
| APP-REL-001 | [Release](releases/release-management.md) | No mapped cardinality/race assertions | Unit type checks + competing DB transactions | P2-05 |
| APP-REL-002 | [Release](releases/release-management.md) | No mapped concurrent hash assertions | Same/different tenant/app hash fixtures + storage ownership | P2-05 |
| APP-REL-003 | [Release](releases/release-management.md) | AMT.deleteApplicationRelease disabled placeholder | Guard/related-row/storage failure integration | P2-06 |
| APP-REL-004 | [Release](releases/release-management.md) | No mapped type/state update assertions | Update matrix + replacement failure injection | P2-06 |
| APP-LC-001 | [Lifecycle](lifecycle/lifecycle.md) | LCT.checkValidNextLifecycleState/checkInvalidNextLifecycleState partial predicates | Authorization and persisted history/state integration | P2-05 |
| APP-LC-002 | [Lifecycle](lifecycle/lifecycle.md) | LCT.CheckInstallableState checks flag only | Two releases/two transactions, CUSTOM exception | P2-05 |
| APP-LC-003 | [Lifecycle](lifecycle/lifecycle.md) | AMT.createApplicationAndPublish disabled placeholder | Auto-publish with failure and altered configuration | P2-05 |
| APP-STO-001 | [Storage](storage/storage.md) | PAR.testReadAndroidManifestFile/testReadiOSManifestFile and invalid-file cases; STU.testSaveFile partial | Chunk/access/path/tenant/parser negative fixtures | P2-07 |
| APP-STO-002 | [Storage](storage/storage.md) | STU.testCreateArtifactDirectory is not compensation coverage | Filesystem/DB fault matrix | P2-04 |
| APP-STO-003 | [Storage](storage/storage.md) | No mapped topology/security assertions | Two-node download/access and URL-policy tests | P2-07 |
| APP-CAT-001 | [Catalog](catalog/catalog.md) | AMT.getApplications disabled placeholder | Role/tenant/filter/count/paging/join regression | P2-08 |
| APP-CAT-002 | [Catalog](catalog/catalog.md) | AMT.addApplicationCategories creates fixtures; not comprehensive validation | Label/app update integrity and collision tests | P2-08 |
| APP-CAT-003 | [Catalog](catalog/catalog.md) | No mapped multi-user favourite assertions | Favourite CRUD and deletion/retirement integration | P2-08 |
| APP-CAT-004 | [Catalog](catalog/catalog.md) | AMT.deleteApplication disabled placeholder | Multi-release deletion and partial-failure tests | P2-06 |
| APP-SUB-001 | [Subscriptions](subscriptions/subscriptions.md) | No mapped acceptance evidence | Target/action unit matrix + fake device dispatch | P2-09 |
| APP-SUB-002 | [Subscriptions](subscriptions/subscriptions.md) | No mapped acceptance evidence | Concurrent/retried dispatch and uncertain outcome | P2-09 |
| APP-SUB-003 | [Subscriptions](subscriptions/subscriptions.md) | No mapped acceptance evidence | Delayed/out-of-order responses + split update failure | P2-09 |
| APP-SCH-001 | [Subscriptions](subscriptions/subscriptions.md) | No mapped ownership evidence | Two executors, clock control, crash/restart/cleanup | P2-09 |
| APP-REV-001 | [Reviews](reviews/reviews.md) | No mapped acceptance evidence | Ownership/range/release/duplicate-policy tests | P2-10 |
| APP-REV-002 | [Reviews](reviews/reviews.md) | No mapped acceptance evidence | Reply tree + failed/concurrent rating recalculation | P2-10 |
| APP-FW-001 | [Firmware](firmware/firmware.md) | No mapped acceptance evidence | Model/version/type/role/operation matrix | P2-10 |
| APP-FW-002 | [Firmware](firmware/firmware.md) | No mapped acceptance evidence | Model-ID scope + device/AppM split failure | P2-10 |
| APP-INT-001 | [Integrations](integrations/integrations.md) | No mapped acceptance evidence | Stub identity provider + local mapping failure | P2-11 |
| APP-INT-002 | [Integrations](integrations/integrations.md) | No mapped acceptance evidence | Stub paginated VPP + auth/timeout/reconciliation | P2-11 |
| APP-RUN-001 | [Runtime](runtime/runtime.md) | CFG partial configuration fixtures | OSGi activation/deactivation/missing-dependency smoke | P2-12 |
| APP-RUN-002 | [Runtime](runtime/runtime.md) | LCT/CFG use fixtures, not installed product | Java 11 + deployment/schema/topology matrix | P2-12 |
| APP-RUN-003 | [Runtime](runtime/runtime.md) | No mapped cleanup evidence | Two-tenant data/artifact interrupted-cleanup integration | P2-12 |
| APP-X-001 | [Cross-cutting](cross-cutting-requirements.md) | No full boundary matrix mapped | Tenant/role checks across service entry points | P2-03 |
| APP-X-002 | [Cross-cutting](cross-cutting-requirements.md) | No mapped transaction fault assertions | Mock connection faults + real-pool leak assertions | P2-01 |
| APP-X-003 | [Cross-cutting](cross-cutting-requirements.md) | No mapped multi-node evidence | Independent DB transactions and two JVMs | P2-05, P2-09 |
| APP-X-004 | [Cross-cutting](cross-cutting-requirements.md) | No full compatibility/security evidence | Contract snapshots + storage/provider boundary faults | P2-03, P2-07, P2-11 |

## Implementation-alignment acceptance additions

No checks below have been executed. These are characterization scenarios, not
authority to change compatibility behavior or repair identified anomalies.

| Requirement | Governing spec | Evidence status | Phase 2 |
| --- | --- | --- | --- |
| APP-REL-005 | [Release search](releases/release-search.md) | Behavior described; verification not run | P2-14 |
| APP-REL-006 | [Release update compatibility](releases/release-updates.md) | Behavior described; verification not run | P2-14 |
| APP-STO-004 | [Platform artifacts](storage/platform-artifacts.md) | Behavior described; verification not run | P2-15 |
| APP-STO-005 | [Platform artifacts](storage/platform-artifacts.md) | Behavior described; verification not run | P2-15 |
| APP-CAT-005 | [Retirement](catalog/retirement.md) | Behavior described; verification not run | P2-14 |
| APP-SUB-004 | [Subscription reporting](subscriptions/reporting.md) | Behavior described; verification not run | P2-16 |
| APP-SUB-005 | [Subscription reporting](subscriptions/reporting.md) | Behavior described; verification not run | P2-16 |

## Execution requirements for Phase 2

Use Java 11 and record Maven/runtime versions. Inspect TestNG suite inclusion;
disabled methods or classes not selected by the suite are not executed evidence.
Prefer independent fixtures over order-dependent inherited data. Unit tests use
controlled dependencies; integration tests use real transactions for the chosen
production engine. H2-only success does not establish other engine behavior.

Concurrency tests synchronize at the conflicting read/claim, then assert final
rows, artifacts and side effects. Failure tests inspect cleanup and the original
exception, not only the returned error. Never run against production databases,
live providers or user data without explicit authorization.

Add evidence columns for command, engine/config, timestamp, commit, result and
report location when executed. Current newly verified criteria: 0/42. This is an
evidence count for this effort, not a claim that the implementation fails all 42.

## Enterprise creation and publication cases

These are subcases of existing canonical acceptance requirements, not additional
requirement IDs. Definitions live in the linked creation/release/lifecycle/storage
specs. All cases are **planned, not executed**. Observed branches are characterization
oracles; proposed stronger guarantees need the linked gap decision before a test
can claim conformance. Existing parser/storage/lifecycle predicate tests are partial
evidence only and do not mark these cases passed.

Use independent tenant A/B fixtures, registered categories/platform OS endpoints,
controlled role permissions, temporary artifact storage, known APK/IPA/Windows
inputs, and a configurable downloader. Assert persisted rows/history, artifacts,
icon repository, returned entity/exception and connection ownership as applicable.

| Case | Requirement / governing contract | Action and expected observation | Follow-up |
| --- | --- | --- | --- |
| E01 | APP-CRT-001, [enterprise](creation/app-creation-enterprise.md#validation-and-identity) | Empty release list with publication off/on: one enterprise app and category/role/tag mappings as supplied; no release/history. Single valid release: one release. Multiple entries: characterize first-only processing; null list/element: capture exception and absence/presence of side effects without assuming a controlled validation exception. | P2-02; GAP-19 |
| E02 | APP-CRT-001/002, [enterprise validation](creation/app-creation-enterprise.md#validation-and-identity) | Name null/empty, 20/21 characters, padded/whitespace-only and duplicate names; null/empty/unknown categories; no registered categories; valid/invalid roles and membership. Assert BadRequest versus broad management failures where documented; characterize unchecked/normalization cases. | P2-02/03; GAP-19 |
| E03 | APP-CRT-001, APP-STO-001, [enterprise validation](creation/app-creation-enterprise.md#validation-and-identity) | Registered OS endpoints, upper ALL, unknown endpoint, missing delimiter, reversed/extra endpoints; Windows null/empty identity and allowed/invalid extension; valid/malformed APK/IPA. Assert parsed identity overrides non-Windows wrapper values; do not infer semantic range ordering. | P2-02/07; GAP-19 |
| E04 | APP-REL-002, [hash](releases/release-binary-hash.md) | Same binary across same/different apps in A is rejected sequentially, including an end-state hash; B's hash alone does not conflict in A. Active-package initial create conflicts; end-state package and additional/remote initial path follow their distinct checks. Assert propagated/wrapped service exceptions. | P2-05; GAP-02/19 |
| E05 | APP-CRT-001, APP-LC-003, [persistence](releases/release-management.md#enterprise-release-persistence) | Synchronous first release without publication: tenant-owned application/release and one initial history row. With packaged default publication: PUBLISHED and four total history rows, correct previous/current state, actor and generated reasons. Inspect committed data rather than response alone. | P2-02/05 |
| E06 | APP-REL-001, [persistence](releases/release-management.md#enterprise-release-persistence) | Add enterprise release: existing app retained, new release/history, other release unchanged. Non-enterprise parent rejects type. Verify independent initial/additional icon-info behavior. Validate caller-supplied device type separately from the parent-derived execution type. | P2-02/03/05; GAP-19/21 |
| E07 | APP-CRT-004, [remote flow](creation/app-creation-enterprise.md#remote-enterprise-release) | Pause downloader: initial app persists without release and response remains that app snapshot; extra remote release returns empty release entity. Resume/fail: inspect later release/history, tenant/user flow and logs. Unknown app/hash conflict after additional dispatch must not be inferred from the initial service result. | P2-04; GAP-03 |
| E08 | APP-CRT-004, APP-X-001/004, [remote flow](creation/app-creation-enterprise.md#remote-enterprise-release) | Interrupt worker/restart and repeat creation/addition. Record surviving app/files and duplicate-name/hash outcomes. No durable replay, bounded retry or exactly-once completion is assumed; required recovery result awaits decision. | P2-04; GAP-02/03/04 |
| E09 | APP-LC-001, [transition contract](lifecycle/lifecycle.md#enterprise-transition-contract) | Allowed/disallowed graph edges; authorized/denied/missing realm; missing permission/state; null/empty changer/action; missing/foreign release. Distinguish BadRequest, NotFound, broad management/lifecycle failure and installable Forbidden. Assert no unauthorized successful state change. | P2-03/05; GAP-01/20 |
| E10 | APP-LC-001/002, [transition contract](lifecycle/lifecycle.md#enterprise-transition-contract) | Same-state request in noninstallable state versus PUBLISHED; mixed-case actions; configured cross-type edges. Record history addition or self-match Forbidden rather than assert universal idempotency/type enforcement. | P2-05; GAP-05/20 |
| E11 | APP-LC-002/003, [installable detail](releases/single-installable-release.md) | Existing PUBLISHED release: unpublished addition can persist; publish-on-add hits Forbidden and leaves existing release intact under ordinary rollback. Fail cleanup separately and inspect new hash files. | P2-04/05; GAP-01/04 |
| E12 | APP-LC-001/003, [publication](lifecycle/lifecycle.md#publication-and-history-acceptance) | Fail each hard-coded publication step, history insertion, release update and altered initial/successor/installable configuration. Check UUID-owned versus caller-owned transactions; characterize broad failure close without explicit rollback before asserting no partial persistence. | P2-01/05; GAP-01/05/20 |
| E13 | APP-CRT-003, APP-STO-002, APP-X-002/004, [failure boundaries](creation/app-creation-enterprise.md#failure-boundaries) | Fail download/parse/upload before transaction; acquisition/auto-commit; application/mapping/release/history writes; commit/rollback/close; artifact compensation. Inspect residual files/rows and primary exception. No false-success/no-leak/no-orphan guarantees are claimed until approved and verified. | P2-01/04; GAP-01/04 |
| E14 | APP-CRT-003, APP-STO-002, [storage](storage/storage.md#enterprise-creation-boundaries) | Fail initial icon write, then fail APPM after successful icon commit; compare initial versus additional release and both repositories. Record independently committed icon data and agreed reconciliation expectation. | P2-04; GAP-21 |
| E15 | APP-X-001/003, APP-REL-002, APP-LC-002, [transactions](releases/release-management.md#transaction-and-retry-acceptance) | Barrier-controlled two-connection/two-JVM hash and publication races plus competing transitions on one release. Inspect invariant, history and surviving files. Test duplicate/imported UUIDs across A/B separately; ordinary tenant lookups must not access B. Final concurrency/identity oracle requires decisions. | P2-03/05; GAP-02/20 |
| E16 | APP-CRT-002, APP-X-004, [service entry points](creation/app-creation-enterprise.md#entry-points-and-caller-obligations) | Invoke initial creation, additional release and lifecycle services with real exception types: compare synchronous/remote result shapes, validator invocation, propagated/wrapped exceptions and null/unchecked inputs. Assert behavior at each service entry point. | P2-03; GAP-10/19/20 |

Before marking a case passed, record test methods, command, fixture configuration,
Java/Maven, DB/isolation/configuration, timestamp, result and report location. Unit
stubs cannot close real-pool, filesystem, two-JVM or runtime service-wiring requirements.
