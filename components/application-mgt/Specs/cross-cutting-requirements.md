# Cross-Cutting Application Management Requirements

[Tracking](spec-register.md) | [Gaps](gap-register.md) | [Verification](verification-matrix.md) | [Sources](source-map.md)

## Observed baseline

Services use Carbon tenant/username context. TX.getDBConnection may borrow lazily;
beginDBTransaction can use an already borrowed non-transactional connection.
commitDBTransaction logs SQL exceptions without propagating them; closeDBConnection
throws when no connection is associated. Rollback handling varies by service path.
Thread-local connections and Java synchronization are not cross-node ownership.
Artifact and remote-service side effects do not roll back with APPM JDBC work.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-X-001 | Proposed guarantee; tenant-aware paths observed | Use authenticated tenant/user identity and enforce ownership/visibility for every public capability; explicit cross-tenant operations require trusted authorization. | AC-APP-X-001: create fixtures in tenants A/B with overlapping identifiers; A cannot read, mutate, download or target B's data through each relevant adapter; trusted tenant cleanup is tested separately. |
| APP-X-002 | Proposed; current deviation APP-GAP-01 | The service owns acquisition/commit/rollback/close; report failure if commit fails, preserve primary errors, and release only its owned connection on every exit. | AC-APP-X-002: inject acquisition, auto-commit, write, commit and cleanup failures; verify no false success, leaked checkout, masked primary exception or damage to an outer caller's connection. |
| APP-X-003 | Proposed; enforcement unverified | Explicitly enforce each declared uniqueness/state invariant across independent nodes. Define retry/conflict behavior, not merely prechecks. | AC-APP-X-003: two connections synchronize before conflicting writes; assert the approved final invariant and response; retry from fresh state after a classified conflict. |
| APP-X-004 | Proposed compatibility/security baseline | Preserve documented caller contracts unless approved/versioned; validate untrusted input, protect credentials and define recovery for non-DB side effects. | AC-APP-X-004: compare contract fixtures, deny unauthorized artifact access, inspect client-safe failure responses, and inject a failure between storage/remote work and DB commit. |

## Design constraints for Phase 2

Do not introduce Spring transactions or a blanket distributed lock into this
Carbon/OSGi component. Choose row locks, conditional updates, constraints or durable
work claims per approved invariant. DB engine/isolation, migration compatibility,
timeout and retry limits must be recorded before implementation. Never promise
exactly-once remote effects without a corresponding provider/reconciliation design.

Do not copy the constitution's abbreviated transaction snippets as complete safe
implementations: the write example lacks cleanup and the read example assumes
unconditional cleanup is safe. This local qualification does not edit that shared
document or silently waive architectural rules.

## Gaps and evidence

APP-GAP-01/02/03/04/10/11 apply. Verification is planned in
[matrix](verification-matrix.md); unit/integration test implementation is Phase 2.
