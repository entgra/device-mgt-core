# Application Management Capability Register

Status: Draft baseline; owner approval pending. Reviewed core: `293d35abfd`.
All rows are source-documented, not runtime-verified. Accountable role for every
row: Application Management component owner; named assignment is pending. API,
security, QA and integration owners participate as described in [workflow](contributing.md).

The denominator is 14 grouped capabilities, not Java classes or equal-sized work
items. Coverage below means a baseline exists; open decisions and incomplete
verification prevent calling these approved/completed product capabilities.

| Capability | Scope and source keys | Governing specification | Priority / verification |
| --- | --- | --- | --- |
| APP-CAP-01 | Shared create and all four wrapper families; AM/Publisher | [Creation](creation/app-creation.md), [workflow](creation/creation-workflow.md), existing type specs | P0 / partial test mapping |
| APP-CAP-02 | Release create/update/delete and hash checks; AM | [Release management](releases/release-management.md), existing add-release specs | P0 / planned |
| APP-CAP-03 | Lifecycle, publication, permissions and history; AM/LC | [Lifecycle](lifecycle/lifecycle.md) | P0 / partial predicate tests |
| APP-CAP-04 | Transfer links, parsing, remote acquisition, artifact reads/deletion; STORE/TRANSFER/DATA | [Storage](storage/storage.md) | P0 / partial utility tests |
| APP-CAP-05 | App list/detail/update/delete/retire, categories/tags/favourites; AM | [Catalog](catalog/catalog.md), existing list/favourites specs | P1 / partial setup tests |
| APP-CAP-06 | Bulk and enrollment install/uninstall, subscriber targeting/status/reporting; SUB | [Subscriptions](subscriptions/subscriptions.md) | P0 / planned |
| APP-CAP-07 | Scheduled subscriptions, cleanup and restart behavior; TASK/SCHED/CLEAN | [Subscriptions](subscriptions/subscriptions.md) | P0 / planned |
| APP-CAP-08 | Reviews, replies, ownership and ratings; REVIEW | [Reviews](reviews/reviews.md) | P1 / planned |
| APP-CAP-09 | Firmware models, release versions, availability and device matching; AM | [Firmware](firmware/firmware.md) | P1 / planned |
| APP-CAP-10 | Identity-server configuration and SP app mapping; SP | [Integrations](integrations/integrations.md) | P1 / planned |
| APP-CAP-11 | VPP users/assets/licenses/associations and backend calls; VPP | [Integrations](integrations/integrations.md) | P1 / planned |
| APP-CAP-12 | Activation, configuration, extension services, packaging and migrations; DS | [Architecture](architecture.md), [runtime](runtime/runtime.md) | P0 / partial config tests |
| APP-CAP-13 | Tenant identity, role visibility, transactions and shared-state safety; TX/AM | [Cross-cutting](cross-cutting-requirements.md) | P0 / planned |
| APP-CAP-14 | Tenant application-data/artifact cleanup and dependent records; AM/STORE | [Runtime](runtime/runtime.md), [catalog](catalog/catalog.md) | P0 / planned |

## Boundaries and exclusions

Device operations, firmware model ownership and user/role services are dependencies,
not reimplemented here. Publisher/Store UI workflows and device-agent installation
behavior need consumer-owner confirmation. Product migration execution, external
provider contracts, notification internals and production topology are outside
Phase 1 execution, but their assumptions are tracked as gaps.

## Reporting

Baseline document coverage: 14/14 grouped capabilities have a governing document.
This is not 100% behavioral completeness: detailed adapter contracts, failure
branches and integration semantics retain explicit gaps. Owner-approved coverage
and newly runtime-verified acceptance coverage are both 0 in this effort. Do not
convert this register into a product implementation percentage.
