# Acceptance Verification Matrix

Status: Phase 1 source mapping only. No tests added, changed or executed.
Baseline: core `293d35abfd`. Owner: QA with component owner (assignment pending).

Each acceptance ID is `AC-` plus the requirement ID. The governing specification
contains the fixture/action/expected-outcome scenario; the Phase 2 work item supplies
execution grouping. Existing test references are candidates/partial evidence, never
Passed. Spec review and runtime verification are separate states.

Test aliases resolve to exact files in [source map / Tests](source-map.md#tests):
AMT = ApplicationManagementTest; DAOT = ApplicationManagementDAOTest;
LCT = LifecycleManagementTest; PAR = ArtifactParserTest;
STU = StorageManagementUtilTest; CFG = ConfigurationTest.

| Requirement / acceptance suffix | Governing spec | Existing evidence (not run) | Required verification | Phase 2 |
| --- | --- | --- | --- | --- |
| APP-CRT-001 | [Creation](creation/creation-workflow.md) | AMT.createApplication partial enterprise fixture; DAOT.testAddApplication insertion only | Unit validation by type + service/DB fixtures | P2-02 |
| APP-CRT-002 | [Creation](creation/creation-workflow.md) | No mapped adapter assertions | Publisher contract fixtures for all four handlers | P2-03 |
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
| APP-X-001 | [Cross-cutting](cross-cutting-requirements.md) | No full boundary matrix mapped | Tenant/role checks across service and adapters | P2-03 |
| APP-X-002 | [Cross-cutting](cross-cutting-requirements.md) | No mapped transaction fault assertions | Mock connection faults + real-pool leak assertions | P2-01 |
| APP-X-003 | [Cross-cutting](cross-cutting-requirements.md) | No mapped multi-node evidence | Independent DB transactions and two JVMs | P2-05, P2-09 |
| APP-X-004 | [Cross-cutting](cross-cutting-requirements.md) | No full compatibility/security evidence | Contract snapshots + storage/provider boundary faults | P2-03, P2-07, P2-11 |

## Implementation-alignment acceptance additions

No checks below have been executed. These are characterization scenarios, not
authority to change compatibility behavior or repair identified anomalies.

| Requirement | Governing spec | Evidence status | Phase 2 |
| --- | --- | --- | --- |
| APP-REL-005 | [Release search](releases/release-search.md) | Source mapped; runtime not run | P2-14 |
| APP-REL-006 | [Release update compatibility](releases/release-updates.md) | Source mapped; runtime not run | P2-14 |
| APP-STO-004 | [Platform artifacts](storage/platform-artifacts.md) | Source mapped; runtime not run | P2-15 |
| APP-STO-005 | [Platform artifacts](storage/platform-artifacts.md) | Source mapped; runtime not run | P2-15 |
| APP-CAT-005 | [Retirement](catalog/retirement.md) | Source mapped; runtime not run | P2-14 |
| APP-SUB-004 | [Subscription reporting](subscriptions/reporting.md) | Source mapped; runtime not run | P2-16 |
| APP-SUB-005 | [Subscription reporting](subscriptions/reporting.md) | Source mapped; runtime not run | P2-16 |

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
