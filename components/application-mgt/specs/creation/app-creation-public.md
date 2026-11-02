# Public App Creation (Backend) Spec

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

Requirements: [APP-CRT-001/002](creation-workflow.md),
[APP-REL-001](../releases/release-management.md).
Package/cardinality
prechecks do not establish cross-node uniqueness; see APP-GAP-02.

## Purpose

Public app creation registers a store-linked application (Google Play / App Store /
Microsoft Store) identified by package name / store id, without uploading a private
installer binary. The service accepts PublicAppWrapper and PublicAppReleaseWrapper.

See also the shared pipeline: [app-creation.md](app-creation.md).

## Source Files

| Area | Path |
| --- | --- |
| Core | `ApplicationManagerImpl` public upload / `createPubAppRelease` paths |
| Wrapper | `PublicAppWrapper`, `PublicAppReleaseWrapper` |

## Request Shape

`PublicAppWrapper` includes application metadata (name, categories, device type, …) and
optional `PublicAppReleaseWrapper` data:

- `packageName` (store package / id)
- `version`
- `supportedOsVersions`
- Image artifact links (icon / screenshots / banner)

No private installer binary is uploaded. The release installer reference is derived as the
public store URL for the device type plus package name.

## Validation

### Application

Shared app-level rules from [app-creation.md](app-creation.md) (name, categories, device type,
duplicate display name, roles).

### Release

| Rule | On failure |
| --- | --- |
| Supported OS versions required and valid | `BadRequestException` |
| Version required | `BadRequestException` |
| Package name required | `BadRequestException` |
| Package name already used for a public release | `ConflictException` |

## Artifacts And Identity

| Concern | Behaviour |
| --- | --- |
| Binary | Not uploaded |
| Installer name / URL | Store base URL + package name |
| Hash | MD5 of the store URL string |
| Images | Icon / screenshots / banner via links |

## Release Limit

A public application can have only **one** release. Attempting to add another release when
one already exists raises `BadRequestException`.

## Lifecycle (isPublished)

When `isPublished=true`, the release is advanced to **PUBLISHED**.

## Acceptance Criteria

- [ ] The service creates the expected PUBLIC application and returns its domain result.
- [ ] Missing package name / version / OS versions return `BadRequestException`.
- [ ] Duplicate public package name raises `ConflictException`.
- [ ] Second release for the same public app raises `BadRequestException`.
- [ ] `isPublished=true` advances lifecycle to published as implemented.

## Maintenance Rules

- Keep public package uniqueness as `ConflictException`.
- Do not require private binary upload for public create.
- Update this spec when public create validation or store-URL construction changes.
