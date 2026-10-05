# Public App Creation (Backend) Spec

## Purpose

Public app creation registers a store-linked application (Google Play / App Store /
Microsoft Store) identified by package name / store id, without uploading a private
installer binary. This is the backend contract used by the Publisher **Public** create
wizard (`POST /applications/public-app`).

See also the shared pipeline: [app-creation.md](app-creation.md).

## App And Audience

| Concern | Detail |
| --- | --- |
| UI flow | Publisher → Add New App → Public |
| Persisted type | `PUBLIC` |
| Users | Publisher users with public-app create/update scopes |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| Publisher create | `ApplicationManagementPublisherAPIImpl.createPublicApp` (method naming may vary; path `/public-app`) |
| Release create | public-app release create on `/{deviceType}/public-app/{appId}` |
| Core | `ApplicationManagerImpl` public upload / `createPubAppRelease` paths |
| Wrapper | `PublicAppWrapper`, `PublicAppReleaseWrapper` |

## API

| Action | Method | Path | Body | Query |
| --- | --- | --- | --- | --- |
| Create app (+ optional first release) | `POST` | `/applications/public-app` | `PublicAppWrapper` | `is-published={true\|false}` |
| Create release | `POST` | `/applications/{deviceType}/public-app/{appId}` | `PublicAppReleaseWrapper` | `is-published={true\|false}` |

Success: **201 Created**.

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
| Supported OS versions required and valid | **400** |
| Version required | **400** |
| Package name required | **400** |
| Package name already used for a public release | **409** (`ConflictException`) |

## Artifacts And Identity

| Concern | Behaviour |
| --- | --- |
| Binary | Not uploaded |
| Installer name / URL | Store base URL + package name |
| Hash | MD5 of the store URL string |
| Images | Icon / screenshots / banner via links |

## Release Limit

A public application can have only **one** release. Attempting to add another release when
one already exists returns **400**.

## Lifecycle (`is-published`)

When `is-published=true`, the release is advanced to **PUBLISHED**.

## HTTP Status Summary

| Status | Meaning |
| --- | --- |
| **201** | App or release created |
| **400** | Invalid wrapper / missing fields / second release not allowed |
| **409** | Package name already exists |
| **500** | Unexpected failure |

## Acceptance Criteria

- [ ] `POST /applications/public-app` creates a `PUBLIC` application and returns **201**.
- [ ] Missing package name / version / OS versions return **400**.
- [ ] Duplicate public package name returns **409**.
- [ ] Second release for the same public app returns **400**.
- [ ] `is-published=true` advances lifecycle to published as implemented.

## Maintenance Rules

- Keep public package uniqueness as **409 + ConflictException**.
- Do not require private binary upload for public create.
- Update this spec when public create validation or store-URL construction changes.
