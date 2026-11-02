# Application Management Specifications

These specifications describe the backend services and business logic provided by
Application Management: validation, persistence, artifacts, lifecycle, subscriptions,
integrations and runtime behavior. Application Management APIs and UIs are maintained
in `proprietary-commons`. API specifications in that repository will define routes,
HTTP responses and scopes. UI specifications there will define use cases, user
personas and user journeys.

## Find the governing contract

| Capability | Governing specification | Detailed contracts |
| --- | --- | --- |
| Application creation | [Creation workflow](creation/creation-workflow.md) | [Shared creation](creation/app-creation.md), [enterprise](creation/app-creation-enterprise.md), [public](creation/app-creation-public.md), [web](creation/app-creation-web.md), [custom](creation/app-creation-custom.md) |
| Release persistence and changes | [Release management](releases/release-management.md) | [Hash uniqueness](releases/release-binary-hash.md), [search](releases/release-search.md), [updates](releases/release-updates.md) |
| Lifecycle and publication | [Lifecycle](lifecycle/lifecycle.md) | [Single installable release](releases/single-installable-release.md) |
| Artifact transfer and storage | [Storage](storage/storage.md) | [Platform artifacts](storage/platform-artifacts.md) |
| Catalog and organization | [Catalog](catalog/catalog.md) | [Retirement](catalog/retirement.md), [category joins](catalog/releases-vs-categories.md), [favourites cleanup](catalog/release-delete-cleanup.md) |
| Subscriptions and scheduling | [Subscriptions](subscriptions/subscriptions.md) | [Reporting](subscriptions/reporting.md) |
| Reviews and ratings | [Reviews](reviews/reviews.md) | Ownership, reply hierarchy and aggregate consistency |
| Firmware | [Firmware](firmware/firmware.md) | Model mapping, eligibility and version selection |
| External integrations | [Integrations](integrations/integrations.md) | Identity-service-provider and VPP service behavior |
| Runtime and cleanup | [Runtime](runtime/runtime.md) | Activation, packaging, configuration and tenant cleanup |

## Shared references

- [Architecture](architecture.md): component and persistence boundaries.
- [Cross-cutting requirements](cross-cutting-requirements.md): tenant context,
  authorization, transactions, concurrency and compatibility.
- [Operation coverage](operation-coverage.md): service signatures and partial contracts.
- [Capability register](capability-register.md): grouped capability scope.

## Review and verification

Use the [spec register](spec-register.md) for review status, the
[gap register](gap-register.md) for unresolved decisions, and the
[verification matrix](verification-matrix.md) for acceptance scenarios and evidence.
The [test backlog](phase-2-backlog.md) groups future verification work.
A source description is not proof that a guarantee is implemented or verified.

Before changing behavior, read the relevant contract and shared requirements,
inspect the current implementation, and define acceptance scenarios. Keep stable
requirement IDs and update specifications, tests and evidence together. Review and
verification are collaborative. Follow [contributing](contributing.md) and the
[component guidance](../AGENTS.md) for the workflow.
