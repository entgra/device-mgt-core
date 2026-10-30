# Application Management Capability Register

Status: Draft; collaborative review pending.
All rows are source-documented, not runtime-verified. Review and verification are shared team responsibilities, including security,
verification and integration perspectives as described in [workflow](contributing.md).

The denominator is 14 grouped capabilities, not Java classes or equal-sized work
items. Coverage below means a baseline exists; open decisions and incomplete
verification prevent calling these approved/completed product capabilities.

| Capability | Scope | Governing specification | Priority / verification |
| --- | --- | --- | --- |
| APP-CAP-01 | Shared create and all four wrapper families | [Creation](creation/app-creation.md), [workflow](creation/creation-workflow.md), existing type specs | P0 / partial test mapping |
| APP-CAP-02 | Release create/update/delete and hash checks | [Release management](releases/release-management.md), existing add-release specs | P0 / planned |
| APP-CAP-03 | Lifecycle, publication, permissions and history | [Lifecycle](lifecycle/lifecycle.md) | P0 / partial predicate tests |
| APP-CAP-04 | Transfer links, parsing, remote acquisition, artifact reads/deletion | [Storage](storage/storage.md) | P0 / partial utility tests |
| APP-CAP-05 | App list/detail/update/delete/retire, categories/tags/favourites | [Catalog](catalog/catalog.md), existing list/favourites specs | P1 / partial setup tests |
| APP-CAP-06 | Bulk and enrollment install/uninstall, subscriber targeting/status/reporting | [Subscriptions](subscriptions/subscriptions.md) | P0 / planned |
| APP-CAP-07 | Scheduled subscriptions, cleanup and restart behavior | [Subscriptions](subscriptions/subscriptions.md) | P0 / planned |
| APP-CAP-08 | Reviews, replies, ownership and ratings | [Reviews](reviews/reviews.md) | P1 / planned |
| APP-CAP-09 | Firmware models, release versions, availability and device matching | [Firmware](firmware/firmware.md) | P1 / planned |
| APP-CAP-10 | Identity-server configuration and SP app mapping | [Integrations](integrations/integrations.md) | P1 / planned |
| APP-CAP-11 | VPP users/assets/licenses/associations and backend calls | [Integrations](integrations/integrations.md) | P1 / planned |
| APP-CAP-12 | Activation, configuration, extension services, packaging and migrations | [Architecture](architecture.md), [runtime](runtime/runtime.md) | P0 / partial config tests |
| APP-CAP-13 | Tenant identity, role visibility, transactions and shared-state safety | [Cross-cutting](cross-cutting-requirements.md) | P0 / planned |
| APP-CAP-14 | Tenant application-data/artifact cleanup and dependent records | [Runtime](runtime/runtime.md), [catalog](catalog/catalog.md) | P0 / planned |

## Boundaries and exclusions

Device operations, firmware model ownership and user/role services are dependencies,
not reimplemented here. Device-agent execution is outside these backend service contracts. Product migration execution, external
provider contracts, notification internals and production topology are outside
Phase 1 execution, but their assumptions are tracked as gaps.

## Reporting

Baseline document coverage: 14/14 grouped capabilities have a governing document.
This is not 100% behavioral completeness: detailed caller obligations, failure
branches and integration semantics retain explicit gaps. Owner-approved coverage
and newly runtime-verified acceptance coverage are both 0 in this effort. Do not
convert this register into a product implementation percentage.
