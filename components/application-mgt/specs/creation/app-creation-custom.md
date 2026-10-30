# Custom (Firmware) App Creation (Backend) Spec

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

Requirements: [APP-CRT-001 through APP-CRT-004](creation-workflow.md),
[APP-FW-001/002](../firmware/firmware.md).
Remote releases are
asynchronous; model identity and CUSTOM lifecycle recovery need APP-GAP-05/09 review.

## Purpose

Custom app creation registers a firmware-style application tied to firmware models. The service accepts CustomAppWrapper and CustomAppReleaseWrapper.

See also the shared pipeline: [app-creation.md](app-creation.md).

## Source Files

| Area | Path |
| --- | --- |
| Core | `ApplicationManagerImpl` custom upload / `createCustomAppRelease` paths |
| Wrapper | `CustomAppWrapper`, `CustomAppReleaseWrapper` |

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
| Firmware model ids must be present for DB insert | ApplicationManagementException wrapping persistence failure when required by the DAO path |

### Release

| Rule | On failure |
| --- | --- |
| Version required | `BadRequestException` |
| Package name required | `BadRequestException` |

## Artifacts And Identity

Behaviour depends on firmware delivery method:

| Delivery | Behaviour |
| --- | --- |
| App-store style delivery | Binary uploaded; MD5 uniqueness checked → duplicate hash `ConflictException` |
| External artifact link | Installer name from external link; metadata fetched (for example HEAD content type/length); hash derived from installer name |

## Conflicts

| Condition | Exception |
| --- | --- |
| Duplicate binary MD5 when storing via APP_STORE delivery | `ConflictException` |

Custom apps may have **multiple** releases (unlike public/web).

## Lifecycle (isPublished)

When `isPublished=true`, the release is advanced to **RELEASED** (firmware release path),
not the store **PUBLISHED** end state used by enterprise/public/web.

## Acceptance Criteria

- [ ] The service creates the expected CUSTOM application and returns its domain result.
- [ ] Missing version / package name on release raises `BadRequestException`.
- [ ] Firmware model ids are persisted with the application.
- [ ] Duplicate installer hash for APP_STORE delivery raises `ConflictException`.
- [ ] Multiple releases are allowed for custom apps.
- [ ] `isPublished=true` advances lifecycle to **RELEASED** as implemented.

## Maintenance Rules

- Keep custom publish end state as **RELEASED** unless product behaviour changes.
- Keep hash conflicts as `ConflictException` for stored binaries.
- Update this spec when firmware delivery modes or model mapping rules change.
