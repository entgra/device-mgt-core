# Application Management Specification Register

This register records the review scope and remaining work for backend service
specifications. Review and verification are collaborative; no named assignments
are required. All specifications remain drafts until an explicit review outcome
is recorded. Source inspection and successful test execution are separate evidence.

## Governing specifications

| Specification | Current review scope | Remaining work |
| --- | --- | --- |
| [Architecture](architecture.md) | Service, persistence, artifact and runtime boundaries described | Validate effective runtime configuration when changing deployment behavior |
| [Cross-cutting](cross-cutting-requirements.md) | Tenant/authorization, transaction, consistency and compatibility requirements defined; some guarantees proposed | Resolve and verify transaction ownership and cross-node invariants |
| [Creation workflow](creation/creation-workflow.md) and [shared creation](creation/app-creation.md) | Shared service obligations and initial/remote completion boundaries described | Negative-input and failure evidence; review other type-specific edges when affected |
| [Enterprise creation](creation/app-creation-enterprise.md) | Initial/additional/remote service paths traced through release persistence and publication | Collaborative decisions for malformed input, recovery and concurrency; acceptance execution |
| [Release management](releases/release-management.md) | Enterprise persistence, related rows and transaction boundaries traced | Failure/concurrency verification; remaining update/delete edge cases |
| [Lifecycle](lifecycle/lifecycle.md) | Enterprise permissions, state/history writes, repeat transitions and publication traced | Repeat/type/error semantics, configuration variants and race evidence |
| [Storage](storage/storage.md) | Transfer, parsing, artifact ownership and enterprise failure boundaries described | Recovery, access and topology guarantees require decisions and tests |
| [Catalog](catalog/catalog.md) | Visibility, organization, favourites and deletion described | Counts, label collisions, ownership and partial-failure verification |
| [Subscriptions](subscriptions/subscriptions.md) | Dispatch, status synchronization and scheduling described | Retry/replay, terminal states, ownership and reconciliation decisions |
| [Reviews](reviews/reviews.md) | Ownership, reply hierarchy and asynchronous ratings described | Rating bounds, duplicate policy and convergence evidence |
| [Firmware](firmware/firmware.md) | Model association and eligibility described | Model identity, version ordering and split-failure decisions |
| [Integrations](integrations/integrations.md) | Local identity/VPP responsibilities described | Provider failure, credential, timeout and reconciliation verification |
| [Runtime](runtime/runtime.md) | Activation, configuration, packaging and cleanup described | Installed wiring, schema/topology and interrupted-cleanup evidence |

## Detailed contracts

These supplement their governing requirements rather than defining competing rules.

| Area | Specifications | Review qualification |
| --- | --- | --- |
| Creation by type | [Public](creation/app-creation-public.md), [web](creation/app-creation-web.md), [custom](creation/app-creation-custom.md) | Source-derived service contracts; detailed edge verification remains pending |
| Release invariants | [Hash uniqueness](releases/release-binary-hash.md), [single installable release](releases/single-installable-release.md) | Sequential checks described; cross-node enforcement not established |
| Release queries/updates | [Search](releases/release-search.md), [updates](releases/release-updates.md) | Observed compatibility behavior and anomalies retained; tests pending |
| Catalog | [Category joins](catalog/releases-vs-categories.md), [favourites cleanup](catalog/release-delete-cleanup.md), [retirement](catalog/retirement.md) | Narrow behavior documented; count, deletion and retirement edge cases remain |
| Platform artifacts | [MSI metadata and plist](storage/platform-artifacts.md) | Service generation/parsing behavior described; malformed-input/access checks pending |
| Subscription reporting | [Reporting](subscriptions/reporting.md) | Helper-specific results and anomalies described; membership/status/count tests pending |

## Maintenance

- Keep behavioral requirements and acceptance scenarios in the governing specs.
- Record discrepancies and unresolved decisions in the [gap register](gap-register.md).
- Link actual checks and outcomes in the [verification matrix](verification-matrix.md).
  E01-E16 refine existing enterprise requirements; they are not new requirements.
- Inspect the affected implementation and tests before changing behavior. Keep
  specifications focused on contracts, architecture and decisions; Git history
  tracks document changes without a separate implementation inventory.
- Record review outcomes with their scope. A documentation edit does not close an
  implementation or verification gap, and no pending test is implicitly passed.
