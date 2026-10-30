# Web Clip / Web App Creation (Backend) Spec

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

Requirements: [APP-CRT-001/002](creation-workflow.md),
[APP-REL-001](../releases/release-management.md).
Sequential one-release
checks require separate concurrent-request verification (APP-GAP-02).

## Purpose

Web create registers a URL-based application (web clip or web app) without a native
installer binary. The service accepts WebAppWrapper and WebAppReleaseWrapper.

See also the shared pipeline: [app-creation.md](app-creation.md).

## Source Files

| Area | Path |
| --- | --- |
| Core | `ApplicationManagerImpl` web upload / release create paths |
| Wrapper | `WebAppWrapper`, `WebAppReleaseWrapper` |

## Request Shape

`WebAppWrapper` includes:

- Application metadata (name, categories, …)
- `type`: must be `WEB_CLIP` or `WEB_APP`
- Optional release: `WebAppReleaseWrapper` with `url`, `version`, and image links

Device type is not required on the web wrapper the same way as enterprise/public.

## Validation

### Application

| Rule | On failure |
| --- | --- |
| Name required, max length 20 | `BadRequestException` |
| Categories required and valid | `BadRequestException` |
| `type` must be `WEB_CLIP` or `WEB_APP` | `BadRequestException` |

### Release

| Rule | On failure |
| --- | --- |
| Version required | `BadRequestException` |
| URL required | `BadRequestException` |
| URL must pass URL validation | `BadRequestException` |

## Artifacts And Identity

| Concern | Behaviour |
| --- | --- |
| Binary | Not uploaded |
| Installer name | The web URL |
| Hash | MD5 of the URL string |
| Images | Icon / screenshots / banner via links |
| Package uniqueness | No enterprise-style active package check |

## Release Limit

A web application can have only **one** release. Attempting to add another when one already
exists raises `BadRequestException`.

## Lifecycle (isPublished)

When `isPublished=true`, the release is advanced to **PUBLISHED**.

## Acceptance Criteria

- [ ] The service creates the expected WEB_CLIP or WEB_APP application and returns its domain result.
- [ ] Invalid or missing URL / version / type raises `BadRequestException`.
- [ ] Second release for the same web app raises `BadRequestException`.
- [ ] `isPublished=true` advances lifecycle to published as implemented.

## Maintenance Rules

- Keep `type` restricted to `WEB_CLIP` and `WEB_APP`.
- Keep URL validation on release create.
- Update this spec when web create validation or single-release limits change.
