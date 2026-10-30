# Application Management Spec Register

## Authority and maintenance

This is the authoritative review-status, ownership and reviewed-baseline register
for the feature specifications below. Review and verification are collaborative;
role labels identify relevant perspectives rather than required named assignments.
Approval outcomes remain explicit and pending where not recorded. This register
does not approve behavior or add runtime evidence.

Baseline B1: core `293d35abfdf57fb461a1b745fbe735b581555be4`, reviewed 2026-10-27;
consumer references use proprietary commons
`79b2891a67df7f71627b1d3456132107290e6bc7`. This is the recorded source-inspection
baseline, not a claim that the current checkout or released product matches it.
When reviewing a spec against newer sources, update its row and record the new
baseline here without changing other rows implicitly.

| Specification | Review status / qualification | Accountable and participating roles | Reviewed baseline |
| --- | --- | --- | --- |
| [Application Management Architecture Baseline](architecture.md) | Draft, source-observed; approval pending. | Application Management component owner (assignment pending). | B1 |
| [Cross-Cutting Application Management Requirements](cross-cutting-requirements.md) | Draft; all proposed guarantees require owner approval. | component owner with security and deployment reviewers (assignment pending). | B1 |
| [Application Creation Workflow](creation/creation-workflow.md) | Draft; enterprise contract source-reviewed; collaborative approval and execution pending. | Shared team review, including relevant API, security, data and runtime perspectives. | B3 (enterprise branches only) |
| [Release Management Baseline](releases/release-management.md) | Draft; enterprise contract source-reviewed; collaborative approval and execution pending. | Shared team review, including relevant API, security, data and runtime perspectives. | B3 (enterprise branches only) |
| [Publication and Lifecycle](lifecycle/lifecycle.md) | Draft; enterprise contract source-reviewed; collaborative approval and execution pending. | Shared team review, including relevant API, security, data and runtime perspectives. | B3 (enterprise branches only) |
| [Artifacts, Transfer and Storage](storage/storage.md) | Draft; enterprise contract source-reviewed; collaborative approval and execution pending. | Shared team review, including relevant API, security, data and runtime perspectives. | B3 (enterprise branches only) |
| [Application Catalog and Organization](catalog/catalog.md) | Draft baseline, approval pending. | component owner with Publisher/Store owners (assignment pending). | B1 |
| [Subscriptions, Installation and Scheduling](subscriptions/subscriptions.md) | Draft baseline, approval pending. | component owner with device-operation and Store owners (assignment pending). | B1 |
| [Reviews, Replies and Ratings](reviews/reviews.md) | Draft baseline, approval pending. | component owner with Store/product owners (assignment pending). | B1 |
| [Firmware Models and Release Availability](firmware/firmware.md) | Draft baseline, approval pending. | component owner with firmware/device-model owner (assignment pending). | B1 |
| [Identity-Service-Provider and VPP Integrations](integrations/integrations.md) | Draft source baseline; external contract approval pending. | component owner with identity/VPP integration and security owners (assignment pending). | B1 |
| [Runtime, Deployment and Tenant Cleanup](runtime/runtime.md) | Draft baseline, approval pending. | component owner with platform/deployment owner (assignment pending). | B1 |
| [App Creation (Backend) Spec](creation/app-creation.md) | Draft; enterprise contract source-reviewed; collaborative approval and execution pending. | Shared team review, including relevant API, security, data and runtime perspectives. | B3 (enterprise branches only) |
| [Enterprise App Creation (Backend) Spec](creation/app-creation-enterprise.md) | Draft; enterprise contract source-reviewed; collaborative approval and execution pending. | Shared team review, including relevant API, security, data and runtime perspectives. | B3 (enterprise branches only) |
| [Public App Creation (Backend) Spec](creation/app-creation-public.md) | Draft source-derived baseline; owner approval pending. | Application Management / Publisher API owners (assignment pending). | B1 |
| [Web Clip / Web App Creation (Backend) Spec](creation/app-creation-web.md) | Draft source-derived baseline; owner approval pending. | Application Management / Publisher API owners (assignment pending). | B1 |
| [Custom (Firmware) App Creation (Backend) Spec](creation/app-creation-custom.md) | Draft source-derived baseline; owner approval pending. | Application Management / firmware / Publisher owners (assignment pending). | B1 |
| [Release Binary Hash Uniqueness Spec](releases/release-binary-hash.md) | Draft; enterprise contract source-reviewed; collaborative approval and execution pending. | Shared team review, including relevant API, security, data and runtime perspectives. | B3 (enterprise branches only) |
| [Single Installable Release Spec](releases/single-installable-release.md) | Draft; enterprise contract source-reviewed; collaborative approval and execution pending. | Shared team review, including relevant API, security, data and runtime perspectives. | B3 (enterprise branches only) |
| [Favourites Cleanup On Release Delete Spec](catalog/release-delete-cleanup.md) | Draft source-derived baseline; owner approval pending. | Application Management / Store product owner (assignment pending). | B1 |
| [Application list: releases vs categories](catalog/releases-vs-categories.md) | Draft source-derived baseline; owner approval pending. | Application Management / Store API owner (assignment pending). | B1 |

## Implementation-alignment expansion

Baseline B2: core `9479230e961922e7170da25adb9aaf17278a1c70`, inspected
2026-10-28; commons remains `79b2891a67df7f71627b1d3456132107290e6bc7`.
Application-management Java is unchanged from B1; the core POM's notification
test dependency changed. B2 is a source baseline, not production-version confirmation.
Existing rows retain their historical review baseline. The following additions
are source-described compatibility contracts with owner review and execution pending.

| Specification | Review status | Owner | Baseline |
| --- | --- | --- | --- |
| [Release search](releases/release-search.md) | Draft; source inspected, not executed | Component owner and relevant API/platform/QA owners; assignment pending | B2 |
| [Release update compatibility](releases/release-updates.md) | Draft; source inspected, not executed | Component owner and relevant API/platform/QA owners; assignment pending | B2 |
| [Platform artifacts](storage/platform-artifacts.md) | Draft; source inspected, not executed | Component owner and relevant API/platform/QA owners; assignment pending | B2 |
| [Retirement](catalog/retirement.md) | Draft; source inspected, not executed | Component owner and relevant API/platform/QA owners; assignment pending | B2 |
| [Subscription reporting](subscriptions/reporting.md) | Draft; source inspected, not executed | Component owner and relevant API/platform/QA owners; assignment pending | B2 |

## Where information belongs

- Feature specs own scope, behavioral rules, failure handling and acceptance criteria.
  Keep proposed or unresolved guarantees labeled in context.
- [Gap register](gap-register.md) owns engineering questions, decisions and deviations,
  with links back to affected specs. Product/content gaps are in
  [content readiness](product-content-readiness.md).
- [Verification matrix](verification-matrix.md) owns planned checks and execution
  evidence. Detailed feature scenarios supplement the canonical requirement rows.
- [Source map](source-map.md) owns implementation references and evidence locations.
- [Capability register](capability-register.md) groups features; it is not an approval
  or verification counter.

After an owner resolves a clarification, update the affected spec and record the
decision against its gap. Close implementation or verification gaps only with
their required evidence; a spec edit alone is insufficient. Record approval by
name/date and scope in this register when actually supplied. Supporting registers,
plans and historical handovers retain their own document-level scope/status notes.

## Enterprise contract review

Baseline B3: core `4db344a2a94ed4483c44e56adb7a31c8d0dabb7a`; sampled Publisher
adapter in commons `875cea5ab95de2e5067b73fdd22e73dc3e935ff8`. Only enterprise
creation, release persistence and lifecycle-publication branches in the B3 rows
were re-inspected. Public/web/custom branches and other rows retain their earlier
review scope. Full deployed API/UI/agent behavior is not established.

The enterprise contract, persistence/lifecycle/storage details, source references,
gap decisions and verification subcases now describe synchronous initial creation,
additional releases and remote completion boundaries. Existing requirement IDs
are retained; E01-E16 are subcases, not new canonical requirements. APP-GAP-19/20/21
record malformed-input/path distinctions, lifecycle repeat/type/error behavior and
independent icon persistence. No gap is closed by this source review alone.

Review and verification belong to the collaborating team; no named reviewer
assignment is required. Proposed guarantees and changes to observed behavior still
need an explicit decision. Status: source-described, collaborative approval pending,
runtime verification not executed. This record is contract provenance, not a
product release/support declaration.
