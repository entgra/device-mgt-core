# Application Management Specs

## Phase 1 baseline

Review status, owners and reviewed revisions are maintained in the
[spec register](spec-register.md).

This is a documentation-only baseline. Existing test sources were inspected, but
no tests, implementation, SQL, configuration or CI were changed or executed.
"Documented" does not mean approved, implemented without deviations, or verified.

Start with [workflow and status conventions](contributing.md), then locate the
capability and its requirements below. All AI-assisted development must preserve
the distinction between source-observed behavior and proposed requirements.

| Baseline artifact | Purpose |
| --- | --- |
| [Operation coverage](operation-coverage.md) | Public service operation inventory, owning specs and explicit partial coverage |
| [Spec register](spec-register.md) | Per-spec review status, ownership and source baseline |
| [Capability register](capability-register.md) | 14 grouped capabilities, owners, priorities and coverage limits |
| [Architecture](architecture.md) | Services, persistence, storage and deployment boundaries |
| [Source map](source-map.md) | Resolvable implementation/test references and sibling repository evidence |
| [Cross-cutting requirements](cross-cutting-requirements.md) | Tenant/security, transaction, concurrency and compatibility rules |
| [Gap register](gap-register.md) | Source deviations, unverified risks and product/deployment decisions |
| [Verification matrix](verification-matrix.md) | 42 acceptance criteria with source mappings and planned checks; execution pending |
| [Phase 2 backlog](phase-2-backlog.md) | Test and approved implementation work, not executed in Phase 1 |
| [Phase 1 handover](phase-1-report.md) | S01-S13 execution ledger, limitations and approval checklist |
| [Product content and automation readiness](product-content-readiness.md) | Missing product inputs, documentation/marketing evidence and proposed follow-up execution items |

## Capability folders

Functional specs are grouped by capability. Shared architecture, cross-cutting
rules, status, gaps and verification remain at this root. Each folder index links
to its governing spec and any detailed contracts.

- [Application Creation](creation/README.md)
- [Release Management](releases/README.md)
- [Publication and Lifecycle](lifecycle/README.md)
- [Firmware](firmware/README.md)
- [Artifact Storage](storage/README.md)
- [Application Catalog](catalog/README.md)
- [Subscriptions and Scheduling](subscriptions/README.md)
- [Reviews and Ratings](reviews/README.md)
- [External Integrations](integrations/README.md)
- [Runtime and Deployment](runtime/README.md)

## Capability specifications

| Capability | Governing spec |
| --- | --- |
| Creation, including remote completion | [creation/creation-workflow.md](creation/creation-workflow.md) |
| Release create/update/delete | [releases/release-management.md](releases/release-management.md) |
| Publication and configured lifecycle | [lifecycle/lifecycle.md](lifecycle/lifecycle.md) |
| Artifact transfer, storage and download | [storage/storage.md](storage/storage.md) |
| Listing, visibility, app CRUD, tags/categories/favourites | [catalog/catalog.md](catalog/catalog.md) |
| Installation, subscriptions, status and scheduling | [subscriptions/subscriptions.md](subscriptions/subscriptions.md) |
| Reviews, replies and ratings | [reviews/reviews.md](reviews/reviews.md) |
| Firmware mapping and availability | [firmware/firmware.md](firmware/firmware.md) |
| Identity service-provider and VPP | [integrations/integrations.md](integrations/integrations.md) |
| Runtime, packaging and tenant cleanup | [runtime/runtime.md](runtime/runtime.md) |

Existing detailed feature specs below supplement these governing requirements.
Their unchecked lists are verification prompts, not implementation-status claims.

## Purpose

This folder holds backend feature specs for Application Management
(`device-mgt-core/components/application-mgt`).

Each file explains one feature: validation, persistence, and what
correct behaviour looks like in the core services and Publisher API layer.

## How to use these specs

- Before changing create / release / lifecycle behaviour, read the matching spec.
- When behaviour changes, update its spec in the same change.
- Prefer one file per feature area, grouped by feature folder.

## Spec index

### Create applications

| Feature | Spec |
| --- | --- |
| App creation (shared pipeline) | [creation/app-creation.md](creation/app-creation.md) |
| Enterprise app creation | [creation/app-creation-enterprise.md](creation/app-creation-enterprise.md) |
| Public app creation | [creation/app-creation-public.md](creation/app-creation-public.md) |
| Web clip / web app creation | [creation/app-creation-web.md](creation/app-creation-web.md) |
| Custom (firmware) app creation | [creation/app-creation-custom.md](creation/app-creation-custom.md) |

### Add release

| Feature | Spec |
| --- | --- |
| Release binary hash uniqueness (tenant-wide + pre-check) | [releases/release-binary-hash.md](releases/release-binary-hash.md) |
| Single installable (PUBLISHED) release | [releases/single-installable-release.md](releases/single-installable-release.md) |

### List applications

| Feature | Spec |
| --- | --- |
| Releases must not multiply by category count | [catalog/releases-vs-categories.md](catalog/releases-vs-categories.md) |

### Favourites

| Feature | Spec |
| --- | --- |
| Favourites cleanup on release / app delete | [catalog/release-delete-cleanup.md](catalog/release-delete-cleanup.md) |

## Remaining work

Baseline documents now exist for the previously planned areas. Remaining detailed
contract, approval and evidence work is tracked in the gap register and Phase 2
backlog; the presence of a document does not close those gaps.

## Writing rules

- Describe current backend behaviour, APIs, permissions/scopes, and error cases.
- Use HTTP status codes for API contracts (`201`, `400`, `409`, `500`, …).
- When behaviour changes, update the matching spec in the same change.
- Group related specs under a feature folder (for example `creation/`).
- UI Publisher specs live separately under
  `proprietary-commons/.../application.mgt.publisher.ui/Specs/`.

## Implementation alignment

The B2 expansion adds release search/update detail, retirement, platform artifacts
and subscription reporting. The [spec register](spec-register.md) records its revision.
There are 42 canonical acceptance requirements (35 original plus 7 additions);
none were newly runtime-verified by documentation work. Use the
[operation inventory](operation-coverage.md) to find uncovered branches before
changing an existing workflow. This is an SDD working baseline, not a claim that
every public method or deployed-product scenario is fully specified.
