# Enterprise App Creation (Backend) Spec

## Purpose

Enterprise app creation registers a privately hosted application (APK / IPA / Windows
installer) and optionally its first release. This is the backend contract used by the
Publisher **Enterprise** create wizard (`POST /applications/ent-app`).

See also the shared pipeline: [app-creation.md](app-creation.md).

## App And Audience

| Concern | Detail |
| --- | --- |
| UI flow | Publisher → Add New App → Enterprise |
| Persisted type | `ENTERPRISE` |
| Users | Publisher users with enterprise create/update scopes |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| Publisher create | `ApplicationManagementPublisherAPIImpl.createEntApp` |
| Release create | `createEntAppRelease` |
| Core create | `ApplicationManagerImpl.createApplication` / `createEntAppRelease` |
| Wrapper | `ApplicationWrapper`, `EntAppReleaseWrapper` |
| Artifact upload | `uploadEntAppReleaseArtifacts` / `addApplicationReleaseArtifacts` |

## API

| Action | Method | Path | Body | Query |
| --- | --- | --- | --- | --- |
| Create app (+ optional first release) | `POST` | `/applications/ent-app` | `ApplicationWrapper` | `is-published={true\|false}` |
| Create additional release | `POST` | `/applications/{deviceType}/ent-app/{appId}` | `EntAppReleaseWrapper` | `is-published={true\|false}` |

Success: **201 Created** with `Application` or `ApplicationRelease`.

## Request Shape (Create App)

`ApplicationWrapper` typically includes:

- Application fields: name, description, categories, device type, tags / roles as applicable
- Optional first release list: `EntAppReleaseWrapper` entries with version metadata,
  supported OS versions, and artifact links (`artifactLink`, icon / screenshot links)

Artifacts are usually prepared earlier via `POST /applications/upload-links`, then referenced
by link fields on the release wrapper.

## Validation

### Application

| Rule | On failure |
| --- | --- |
| Name required, max length 20 | **400** |
| Categories required and valid | **400** |
| Device type required | **400** |
| Duplicate app display name for device type | **400** |
| Unrestricted role membership rules | **400** |
| If release present | Release validation below |

### Release (`EntAppReleaseWrapper`)

| Rule | On failure |
| --- | --- |
| Supported OS versions required and in valid range | **400** |
| Windows: version and package name required | **400** |
| Windows installer extension must be `.msi` / `.appx` / `.exe` | **400** |

## Artifacts And Identity

| Concern | Behaviour |
| --- | --- |
| Binary | Required for enterprise create with a release (from `artifactLink`) |
| Package name | Extracted from installer metadata (non-Windows); used for uniqueness |
| App hash | MD5 of the uploaded binary |
| Images | Icon / screenshots / banner via links |

## Conflict Checks (Create With Installer)

Checked while adding release artifacts (`isNewRelease = false` on first create):

| Condition | Exception | HTTP |
| --- | --- | --- |
| An active release already exists for the package name | `ConflictException` | **409** |
| A release with the same binary MD5 already exists (tenant-wide) | `ConflictException` | **409** |

Server log / entity text may still mention device type for the binary case; clients should
treat **409** as the contract, not parse message text for control flow.

Early UI pre-check (same core method): Publisher
`GET /applications/release-hash/{hash}`. Details:
[../add-release/release-binary-hash.md](../add-release/release-binary-hash.md).

## Lifecycle (`is-published`)

When `is-published=true`, the initial release is advanced through review states to
**PUBLISHED** (enterprise store-style publish path).

Enterprise apps may receive additional releases later (not limited to a single release).

## HTTP Status Summary

| Status | Meaning |
| --- | --- |
| **201** | App or release created |
| **400** | Invalid wrapper / missing fields / invalid OS or Windows payload |
| **409** | Package or binary already exists |
| **500** | Unexpected persistence / storage failure |

## Acceptance Criteria

- [ ] `POST /applications/ent-app` creates an `ENTERPRISE` application and returns **201**.
- [ ] Invalid name, categories, device type, or release fields return **400**.
- [ ] Duplicate active package name returns **409**.
- [ ] Duplicate binary hash returns **409**.
- [ ] `is-published=true` advances lifecycle to published as implemented.
- [ ] Failed create cleans up uploaded artifacts on rollback.

## Maintenance Rules

- Keep binary and package collisions as **409 + ConflictException**.
- Keep invalid input as **400 + BadRequestException**.
- Update this spec when enterprise create validation or conflict rules change.
