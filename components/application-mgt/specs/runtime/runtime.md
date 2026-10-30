# Runtime, Deployment and Tenant Cleanup

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md) | [Sources](../source-map.md)

## Observed baseline

Activation initializes the configured datasource/lifecycle/extensions, registers
services and schedules subscription cleanup. XML/defaults and Jinja templates are
packaged by the server feature. Database implementations/scripts cover H2, MySQL,
PostgreSQL, SQL Server and Oracle. Packaged defaults and test fixture configuration
must not be substituted for actual deployment values.

AM exposes deleteApplicationDataOfTenant, deleteApplicationDataByTenantId and
deleteApplicationArtifactsByTenantId. STORE deletes tenant artifact folders.
Their presence does not prove all dependent data/external effects are removed in
one transaction. These are privileged internal maintenance boundaries, not client
permission to select arbitrary tenant IDs.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-RUN-001 | Observed activation; safe lifecycle proposed | Initialize required dependencies before exposing usable services; make missing datasource/config/extension failures diagnosable and bound owned worker lifecycles. | AC-APP-RUN-001: start with valid/missing/invalid dependencies, activate/deactivate/restart and inspect service availability, scheduled work and owned threads. |
| APP-RUN-002 | Proposed deployment contract | Keep bundle versions, OSGi wiring, configuration/templates and supported schema/migrations compatible; declare shared storage and scheduler topology. | AC-APP-RUN-002: Java 11 build plus installed-product smoke test, fresh/upgrade schema checks and two-node artifact/task scenarios on the supported matrix. |
| APP-RUN-003 | Observed cleanup entry points; recovery proposed | Tenant cleanup requires trusted authorization, removes only its owned tenant data/artifacts and has defined retry/recovery behavior. | AC-APP-RUN-003: two tenants with apps/subscriptions/reviews/SP/VPP references; clean one, interrupt between DB/filesystem steps, retry and verify foreign data survives. |

## Boundaries

APP-GAP-04/11/12 apply. Product upgrade scripts and consumer WAR deployments are
owned outside this component and require cross-repository sign-off. No performance
SLO, maximum worker count, lock timeout or cache-coherence guarantee is approved
here; record production requirements before adding corresponding Phase 2 tests.
