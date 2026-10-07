# App Creation (Backend) Spec

## Purpose

App creation registers a new application (and optionally its first release) in
Application Management. Publisher UI flows (Enterprise, Public, Web Clip, Custom)
all call into this shared backend pipeline with type-specific wrappers.

This document describes the **shared** create contract. Type-specific rules are in:

- [app-creation-enterprise.md](app-creation-enterprise.md)
- [app-creation-public.md](app-creation-public.md)
- [app-creation-web.md](app-creation-web.md)
- [app-creation-custom.md](app-creation-custom.md)

## Audience And Components

| Concern | Detail |
| --- | --- |
| Layer | Publisher API + Application Management core |
| Consumers | Application Publisher UI (and other Publisher API clients) |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| Publisher API | `proprietary-commons/.../publisher.api/.../ApplicationManagementPublisherAPI.java` |
| Publisher impl | `.../publisher.api.impl/ApplicationManagementPublisherAPIImpl.java` |
| Manager interface | `application.mgt.common/.../services/ApplicationManager.java` |
| Manager impl | `application.mgt.core/.../impl/ApplicationManagerImpl.java` |
| Wrappers | `application.mgt.common/.../wrapper/` |
| DTO conversion | `application.mgt.core/.../util/APIUtil.java` |
| Exceptions | `application.mgt.core/.../exception/` (`BadRequestException`, `ConflictException`, …) |
| App types | `application.mgt.common/.../ApplicationType.java` |

## Application Types (Aligned With Publisher UI)

| Publisher UI flow | Persisted type | Create endpoint |
| --- | --- | --- |
| Enterprise | `ENTERPRISE` | `POST /applications/ent-app` |
| Public | `PUBLIC` | `POST /applications/public-app` |
| Web Clip | `WEB_CLIP` (also accepts `WEB_APP`) | `POST /applications/web-app` |
| Custom (firmware) | `CUSTOM` | `POST /applications/custom-app` |

Base Publisher API path (typical):

`/api/application-mgt-publisher/v1.0/applications`

## Shared Create Pipeline

```
Publisher create*App(wrapper, is-published)
  → validateAppCreatingRequest(wrapper)
  → ApplicationManager.createApplication(wrapper, isPublished)
       → createApplicationBasedOnRemoteStatus(...)
       → triggerApplicationCreation(...)
            → uploadReleaseArtifactIfExist(app)   // type-specific artifact branch
            → DB txn: addAppDataIntoDB(...)
            → on failure: rollback + delete uploaded artifacts
  → HTTP 201 + Application entity
```

### Query parameter

| Param | Meaning |
| --- | --- |
| `is-published` | When `true`, advance the initial release lifecycle after create (exact end state is type-specific: typically `PUBLISHED` for store-style apps, `RELEASED` for custom/firmware) |

### Layers

| Layer | Responsibility |
| --- | --- |
| Publisher APIImpl | Route by type, validate request wrapper, map exceptions to HTTP statuses |
| ApplicationManagerImpl | Business validation, artifact handling, package/hash checks, DB persistence, lifecycle |

## Shared Validation (App Level)

Applied by `validateAppCreatingRequest` (with type-specific additions):

| Rule | Failure |
| --- | --- |
| Name required and length within limit (≤ 20) | `BadRequestException` → **400** |
| Categories required and registered | `BadRequestException` → **400** |
| Device type required (non-web types) | `BadRequestException` → **400** |
| Duplicate application display name for device type | `BadRequestException` → **400** |
| Caller must belong to at least one unrestricted role (unless “view all”) | `BadRequestException` → **400** |
| If first release is present | Run type-specific release validation |

## HTTP Status Contract (Create App)

| Status | When |
| --- | --- |
| **201 Created** | Application created successfully; response entity is `Application` |
| **400 Bad Request** | `BadRequestException` or `RequestValidatingException` (invalid payload / missing required fields / artifacts) |
| **409 Conflict** | `ConflictException` (duplicate package name and/or duplicate release binary hash, depending on type) |
| **500 Internal Server Error** | Unexpected `ApplicationManagementException` / storage failures; null application result |

Create handlers do not map `ForbiddenException` to **403**; create authorization is enforced by Publisher scopes before the resource (for example `am:pub:ent-app:update` and related scopes).

## Conflict Rules (Overview)

| Conflict | Typical types | Exception | HTTP |
| --- | --- | --- | --- |
| Active release already exists for package name | Enterprise (create with installer) | `ConflictException` | **409** |
| Package name already used (public store package) | Public | `ConflictException` | **409** |
| Release binary hash (MD5) already exists | Enterprise, Custom (APP_STORE delivery) | `ConflictException` | **409** |

Exact checks are documented per type.

## Related Endpoints (Not Full Specs Yet)

| Action | Method | Path |
| --- | --- | --- |
| Pre-create artifact upload links | `POST` | `/applications/upload-links` |
| App name availability | `GET` | `/applications/device-type/{deviceType}/app-name?appName={name}` |
| Add release to existing app | `POST` | Type-specific `.../{deviceType}/.../{appId}` or `/web-app/{appId}` |

Add-release binary-hash uniqueness and pre-check:
[../add-release/release-binary-hash.md](../add-release/release-binary-hash.md).

## Acceptance Criteria (Shared)

- [ ] Each Publisher create type maps to the correct POST endpoint and wrapper.
- [ ] Successful create returns **201** with an `Application` entity.
- [ ] Invalid payload returns **400**.
- [ ] Package / binary conflicts return **409** via `ConflictException`.
- [ ] Failed create after artifact upload cleans up artifacts (rollback path).
- [ ] Type-specific rules remain documented in the matching type spec.

## Maintenance Rules

- Keep create success as **201**.
- Keep conflicts as **409 + ConflictException**, not **400**, for package/hash collisions.
- Keep invalid input as **400 + BadRequestException**.
- When adding a new creatable type, add a type-specific spec and link it from this overview and `README.md`.
- Update this spec when the shared pipeline or status mapping changes.
