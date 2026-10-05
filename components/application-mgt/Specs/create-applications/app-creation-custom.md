# Custom (Firmware) App Creation (Backend) Spec

## Purpose

Custom app creation registers a firmware-style application tied to firmware models. This is
the backend contract used by the Publisher **Custom** create wizard
(`POST /applications/custom-app`).

See also the shared pipeline: [app-creation.md](app-creation.md).

## App And Audience

| Concern | Detail |
| --- | --- |
| UI flow | Publisher → Add New App → Custom |
| Persisted type | `CUSTOM` |
| Users | Publisher users with custom-app create/update scopes |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| Publisher create | `ApplicationManagementPublisherAPIImpl` custom-app create (`/custom-app`) |
| Release create | `POST /applications/{deviceType}/custom-app/{appId}` |
| Core | `ApplicationManagerImpl` custom upload / `createCustomAppRelease` paths |
| Wrapper | `CustomAppWrapper`, `CustomAppReleaseWrapper` |

## API

| Action | Method | Path | Body | Query |
| --- | --- | --- | --- | --- |
| Create app (+ optional first release) | `POST` | `/applications/custom-app` | `CustomAppWrapper` | `is-published={true\|false}` |
| Create additional release | `POST` | `/applications/{deviceType}/custom-app/{appId}` | `CustomAppReleaseWrapper` | `is-published={true\|false}` |

Success: **201 Created**.

## Request Shape

`CustomAppWrapper` includes:

- Application metadata (name, categories, device type, …)
- `firmwareModelIds` (required for persistence mapping)
- Optional first release: version, package name, artifact link / delivery fields

## Validation

### Application

Shared app-level rules from [app-creation.md](app-creation.md) (name, categories, device type,
duplicate display name, roles).

| Extra rule | On failure |
| --- | --- |
| Firmware model ids must be present for DB insert | Persistence failure / **500** if empty when required by DAO path |

### Release

| Rule | On failure |
| --- | --- |
| Version required | **400** |
| Package name required | **400** |

## Artifacts And Identity

Behaviour depends on firmware delivery method:

| Delivery | Behaviour |
| --- | --- |
| App-store style delivery | Binary uploaded; MD5 uniqueness checked → duplicate hash **409** |
| External artifact link | Installer name from external link; metadata fetched (for example HEAD content type/length); hash derived from installer name |

## Conflicts

| Condition | Exception | HTTP |
| --- | --- | --- |
| Duplicate binary MD5 when storing via APP_STORE delivery | `ConflictException` | **409** |

Custom apps may have **multiple** releases (unlike public/web).

## Lifecycle (`is-published`)

When `is-published=true`, the release is advanced to **RELEASED** (firmware release path),
not the store **PUBLISHED** end state used by enterprise/public/web.

## HTTP Status Summary

| Status | Meaning |
| --- | --- |
| **201** | App or release created |
| **400** | Invalid wrapper / missing version or package name |
| **409** | Duplicate binary hash (APP_STORE delivery) |
| **500** | Unexpected persistence / storage / firmware mapping failure |

## Acceptance Criteria

- [ ] `POST /applications/custom-app` creates a `CUSTOM` application and returns **201**.
- [ ] Missing version / package name on release returns **400**.
- [ ] Firmware model ids are persisted with the application.
- [ ] Duplicate installer hash for APP_STORE delivery returns **409**.
- [ ] Multiple releases are allowed for custom apps.
- [ ] `is-published=true` advances lifecycle to **RELEASED** as implemented.

## Maintenance Rules

- Keep custom publish end state as **RELEASED** unless product behaviour changes.
- Keep hash conflicts as **409 + ConflictException** for stored binaries.
- Update this spec when firmware delivery modes or model mapping rules change.
