# Phase 2 Test and Implementation Backlog

Status: Proposed work only; not executed or authorized by Phase 1 completion.
Owner: component owner and QA (named assignments pending). Core: `293d35abfd`.
First obtain decisions in [gap register](gap-register.md). Write characterization
tests before changing behavior; implement approved fixes in separate reviewable PRs.

| Item | Priority / dependencies | Setup and execution | Expected result / requirement | Proposed owner |
| --- | --- | --- | --- | --- |
| P2-01 | P0; GAP-01 decision | Mock failures at acquisition/auto-commit/write/commit/rollback/close; real-pool repetition with bounded connections; nested caller fixture | APP-X-002: original error survives, no false success/leak/foreign connection cleanup. Then scope transaction fixes. | Core/QA |
| P2-02 | P0; P2-01 | Independent valid fixtures for each wrapper; null/missing/overlength/category/type/role/artifact cases | APP-CRT-001: expected type/tenant/release cardinality; rejected input leaves no rows. Replace reliance on disabled placeholders. | Core/QA |
| P2-03 | P0; GAP-10/11 | Pin commons commit; build Publisher/Store adapter fixtures with authorized/unauthorized users and two tenants; exercise exception mappings | APP-CRT-002, APP-X-001/004: exact status/entity and isolation; proposed adapter changes need separate authorization. | API/security/QA |
| P2-04 | P0; GAP-03/04 | Temporary storage plus transaction fault hooks; controlled downloader; pause before/after commit; kill/restart remote worker in disposable environment | APP-CRT-003/004, APP-STO-002: compensation or documented recoverability; no initial response falsely interpreted as completed remote release. | Core/storage |
| P2-05 | P0; P2-01, GAP-02/05 | Two connections/transactions and then two JVMs; barrier before hash/cardinality/installable checks; configured lifecycle variants | APP-REL-001/002, APP-LC-001/002/003, APP-X-003: approved invariant, loser response, history consistency; select constraint/lock/CAS changes only after evidence. | Core/database/QA |
| P2-06 | P0; P2-04, GAP-07 | App with multiple states and historical subscriptions; type-specific updates; fail file deletion/replacement and DB commit | APP-REL-003/004, APP-CAT-004: guards hold; surviving references/files are consistent or recoverable. | Core/storage |
| P2-07 | P0; GAP-10/11 | Disposable artifact root, valid/malformed installers, partial chunks, foreign paths/tenants, controlled remote URL server and two nodes | APP-STO-001/003, APP-X-004: correct parsing/access, denied unsafe access, declared topology works. | Storage/security |
| P2-08 | P1; GAP-07 | Multi-category/release/role dataset; favourites from two users/tenants; renamed/duplicate labels and paging | APP-CAT-001/002/003: no join duplication, approved visible count, mappings and favourite cleanup match policy. | Core/Store |
| P2-09 | P0; GAP-06, P2-01 | Fake device/external-store service; concurrent duplicate targets; controlled due time and executor crash; duplicate/out-of-order status events | APP-SUB-001/002/003, APP-SCH-001, APP-X-003: dispatch distinguished from install, approved deduplication/recovery/status ownership. | Device-task/Core |
| P2-10 | P1; GAP-08/09 | Review/reply tree with competing recalculators; firmware model/version/test-role matrix and device-service failure injection | APP-REV-001/002, APP-FW-001/002: ownership/tree and approved rating/version semantics; no accidental tenant crossover. | Core/product/QA |
| P2-11 | P1; GAP-12 | Stub identity/VPP providers with pagination, expired credentials, rate limiting and timeout after remote success | APP-INT-001/002, APP-X-004: no blind duplicate effect; mapping/reconciliation/redaction meet approved contract. Live tests require separate credentials/approval. | Integration/security |
| P2-12 | P0; GAP-11; component fixes | Java 11 baseline, deployed feature startup/restart, schema fresh/upgrade matrix, two-tenant interrupted cleanup and multi-node storage/task smoke | APP-RUN-001/002/003: compatible service wiring/schema, no orphaned owned workers and tenant-safe recovery. Product-repo changes are separate. | Platform/QA |
| P2-13 | P1; pilot evidence | Propose CI metadata/link/ID checks, requirement-impact PR fields and acceptance evidence reporting; compare notification pilot | SDD workflow enforcement without claiming a file edit proves behavioral compliance. Repository-wide changes require separate scope. | Engineering lead |

## Scheduling and completion

Start P2-01/02 and consumer contract review; settle product/deployment decisions
before concurrency, storage and integration changes. P2-08/10/11 can proceed with
independent fixtures once their decisions are approved. Do not estimate remaining
work from spec file count; size each item after decisions and fixture discovery.

An item is done only with approved semantics, meaningful assertions, recorded
environment/commit/results, reviewed implementation if needed, and updated
verification status. Passing unit tests alone does not close deployment or
cross-node items. No checkboxes in this backlog are pre-marked complete.

## Compatibility characterization additions

| Item | Setup / dependency | Required result |
| --- | --- | --- |
| P2-14 | Release filter/type/role fixtures, per-type update payloads and retirement states; APP-GAP-14/15/17 decisions before behavior changes | Characterize APP-REL-005/006 and APP-CAT-005, including anomalies, pagination, missing IDs and transaction/file failures |
| P2-15 | Windows MSI metadata arrays and plist XML fixtures, adapter auth setup; APP-GAP-16 | Characterize APP-STO-004/005, including blank/invalid/duplicate metadata and XML-sensitive values |
| P2-16 | Four subscription helper types, controlled device service, count/history and status fixtures; APP-GAP-18 | Characterize APP-SUB-004/005, explicitly retaining empty/null results and legacy/new dispatch differences unless approved otherwise |
