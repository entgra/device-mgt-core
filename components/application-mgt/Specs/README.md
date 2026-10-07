# Application Management Specs

## Purpose

This folder holds backend feature specs for Application Management
(`device-mgt-core/components/application-mgt`).

Each file explains one feature: validation, persistence, and what
correct behaviour looks like in the core services and Publisher API layer.

## How to use these specs

- Before changing create / release / lifecycle behaviour, read the matching spec.
- When behaviour changes, update its spec in the same change.
- Prefer one file per feature area, grouped by feature folder.

## Spec index

### Create applications

| Feature | Spec |
| --- | --- |
| App creation (shared pipeline) | [create-applications/app-creation.md](create-applications/app-creation.md) |
| Enterprise app creation | [create-applications/app-creation-enterprise.md](create-applications/app-creation-enterprise.md) |
| Public app creation | [create-applications/app-creation-public.md](create-applications/app-creation-public.md) |
| Web clip / web app creation | [create-applications/app-creation-web.md](create-applications/app-creation-web.md) |
| Custom (firmware) app creation | [create-applications/app-creation-custom.md](create-applications/app-creation-custom.md) |

### Add release

| Feature | Spec |
| --- | --- |
| Release binary hash uniqueness (tenant-wide + pre-check) | [add-release/release-binary-hash.md](add-release/release-binary-hash.md) |
| Single installable (PUBLISHED) release | [add-release/single-installable-release.md](add-release/single-installable-release.md) |

### Favourites

| Feature | Spec |
| --- | --- |
| Favourites cleanup on release / app delete | [favourites/release-delete-cleanup.md](favourites/release-delete-cleanup.md) |

## Planned feature areas (add specs when touched)

- App list / search / filters
- Add release (beyond binary-hash uniqueness)
- Publish and lifecycle transitions
- Artifact upload links / storage
- Subscriptions and installs
- Categories and tags
- Firmware model mapping (beyond create)
- Reviews / ratings
- Favourites (add / remove / list beyond delete cleanup)

## Writing rules

- Describe current backend behaviour, APIs, permissions/scopes, and error cases.
- Use HTTP status codes for API contracts (`201`, `400`, `409`, `500`, …).
- When behaviour changes, update the matching spec in the same change.
- Group related specs under a feature folder (for example `create-applications/`).
- UI Publisher specs live separately under
  `proprietary-commons/.../application.mgt.publisher.ui/Specs/`.
