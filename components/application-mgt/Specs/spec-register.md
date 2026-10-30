# Application Management Spec Register

## Authority and maintenance

This is the authoritative review-status, ownership and reviewed-baseline register
for the feature specifications below. All named assignments and approvals remain
pending. This consolidation does not approve behavior or add runtime evidence.

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
| [Application Creation Workflow](creation/creation-workflow.md) | Draft baseline, owner approval pending. | component owner with Publisher API owner (assignment pending). | B1 |
| [Release Management Baseline](releases/release-management.md) | Draft, approval pending. | component owner with Publisher/storage owners (assignment pending). | B1 |
| [Publication and Lifecycle](lifecycle/lifecycle.md) | Draft baseline; product/security approval pending. | component owner with lifecycle permission owner (assignment pending). | B1 |
| [Artifacts, Transfer and Storage](storage/storage.md) | Draft baseline, approval pending. | component owner with storage/security owner (assignment pending). | B1 |
| [Application Catalog and Organization](catalog/catalog.md) | Draft baseline, approval pending. | component owner with Publisher/Store owners (assignment pending). | B1 |
| [Subscriptions, Installation and Scheduling](subscriptions/subscriptions.md) | Draft baseline, approval pending. | component owner with device-operation and Store owners (assignment pending). | B1 |
| [Reviews, Replies and Ratings](reviews/reviews.md) | Draft baseline, approval pending. | component owner with Store/product owners (assignment pending). | B1 |
| [Firmware Models and Release Availability](firmware/firmware.md) | Draft baseline, approval pending. | component owner with firmware/device-model owner (assignment pending). | B1 |
| [Identity-Service-Provider and VPP Integrations](integrations/integrations.md) | Draft source baseline; external contract approval pending. | component owner with identity/VPP integration and security owners (assignment pending). | B1 |
| [Runtime, Deployment and Tenant Cleanup](runtime/runtime.md) | Draft baseline, approval pending. | component owner with platform/deployment owner (assignment pending). | B1 |
| [App Creation (Backend) Spec](creation/app-creation.md) | Draft source-derived baseline; owner approval pending. | Application Management / Publisher API owners (assignment pending). | B1 |
| [Enterprise App Creation (Backend) Spec](creation/app-creation-enterprise.md) | Draft source-derived baseline; owner approval pending. | Application Management / Publisher API owners (assignment pending). | B1 |
| [Public App Creation (Backend) Spec](creation/app-creation-public.md) | Draft source-derived baseline; owner approval pending. | Application Management / Publisher API owners (assignment pending). | B1 |
| [Web Clip / Web App Creation (Backend) Spec](creation/app-creation-web.md) | Draft source-derived baseline; owner approval pending. | Application Management / Publisher API owners (assignment pending). | B1 |
| [Custom (Firmware) App Creation (Backend) Spec](creation/app-creation-custom.md) | Draft source-derived baseline; owner approval pending. | Application Management / firmware / Publisher owners (assignment pending). | B1 |
| [Release Binary Hash Uniqueness Spec](releases/release-binary-hash.md) | Draft source-derived baseline; owner approval pending. | Application Management / database owner (assignment pending). | B1 |
| [Single Installable Release Spec](releases/single-installable-release.md) | Draft source-derived baseline; owner approval pending. | Application Management / lifecycle owner (assignment pending). | B1 |
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
