# Release Binary Hash Uniqueness Spec

## Purpose

A tenant must not store two application releases that share the same installer
binary MD5. This applies across **all applications** in the tenant (not only
releases of the app currently being edited).

Clients need a cheap pre-check so the Uploads step can block Continue before
the final create-release submit.

## App And Audience

| Concern | Detail |
| --- | --- |
| Apps | Application Publisher (create app / add release) |
| Entry | Uploading an enterprise or APP_STORE custom installer |
| Users | Publisher users |
| Status | API-backed |

## Product Rule

`ApplicationReleaseDAO.verifyReleaseExistenceByHash(hash, tenantId)` is
**tenant-wide**. If any release row already uses that `APP_HASH_VALUE`, adding
another release with the same binary must fail with `ConflictException`.

Same-app path hashing in the UI is **not** sufficient: a binary already used
by a *different* app must also be rejected early.

## Source Files

| Area | Path |
| --- | --- |
| Manager | `ApplicationManagerImpl.validateReleaseBinaryFileHash` |
| Manager interface | `ApplicationManager.validateReleaseBinaryFileHash` |
| Used on artifact add | `ApplicationManagerImpl.addApplicationReleaseArtifacts` (and create paths that call it) |
| DAO | `verifyReleaseExistenceByHash` |
| Publisher API | `GET /applications/release-hash/{hash}` |

## Behaviour

### Core validation

`validateReleaseBinaryFileHash(String hash)`:

1. Open APPM DB connection for the current tenant.
2. If `verifyReleaseExistenceByHash(hash, tenantId)` is true → throw
   `ConflictException` (logged as error).
3. Otherwise return successfully (hash is free).

Create / add-release artifact pipelines call this (or equivalent) when
persisting a stored installer so submit still enforces uniqueness even if the
UI skips the pre-check.

### Publisher pre-check API

| Item | Value |
| --- | --- |
| Method / path | `GET /applications/release-hash/{hash}` |
| Scope | `am:pub:app:upload` |
| Success | **200** empty (hash available) |
| Conflict | **409** plain string (`ConflictException` message) |
| Failure | **500** on unexpected `ApplicationManagementException` |

Full HTTP contract:
`proprietary-commons/.../publisher.api/Specs/api-contract.md` § Artifacts And Uploads.

## Correct Behaviour

| Case | Expected |
| --- | --- |
| Hash unused in tenant | Pre-check **200**; create/release may proceed |
| Hash already used by same app | Pre-check **409**; create/release **409** |
| Hash already used by another app | Pre-check **409**; create/release **409** |
| UI skips pre-check | Submit still **409** via artifact add |

## Acceptance Criteria

- [ ] Duplicate binary MD5 in the tenant throws `ConflictException` (**409** via Publisher).
- [ ] Uniqueness is tenant-wide (cross-application).
- [ ] `GET /applications/release-hash/{hash}` exposes the same check for early UI validation.
- [ ] Create / add-release submit remains authoritative if the pre-check is skipped.

## Maintenance Rules

- Keep hash collisions as **409 + ConflictException**, not **400**.
- Do not narrow uniqueness to “same application only” without updating this
  spec, the Publisher API contract, and UI specs.
- When changing hash storage or verification, update this file and the
  Publisher UI add-new-release / create-applications conflict sections.
