# Web Clip / Web App Creation (Backend) Spec

## Purpose

Web create registers a URL-based application (web clip or web app) without a native
installer binary. This is the backend contract used by the Publisher **Web Clip** create
wizard (`POST /applications/web-app`).

See also the shared pipeline: [app-creation.md](app-creation.md).

## App And Audience

| Concern | Detail |
| --- | --- |
| UI flow | Publisher → Add New App → Web Clip |
| Persisted types | `WEB_CLIP` or `WEB_APP` (from wrapper `type`) |
| Users | Publisher users with web-app create/update scopes |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| Publisher create | `ApplicationManagementPublisherAPIImpl` web-app create (`/web-app`) |
| Release create | `POST /applications/web-app/{appId}` |
| Core | `ApplicationManagerImpl` web upload / release create paths |
| Wrapper | `WebAppWrapper`, `WebAppReleaseWrapper` |

## API

| Action | Method | Path | Body | Query |
| --- | --- | --- | --- | --- |
| Create app (+ optional first release) | `POST` | `/applications/web-app` | `WebAppWrapper` | `is-published={true\|false}` |
| Create release | `POST` | `/applications/web-app/{appId}` | `WebAppReleaseWrapper` | `is-published={true\|false}` |

Success: **201 Created**.

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
| Name required, max length 20 | **400** |
| Categories required and valid | **400** |
| `type` must be `WEB_CLIP` or `WEB_APP` | **400** |

### Release

| Rule | On failure |
| --- | --- |
| Version required | **400** |
| URL required | **400** |
| URL must pass URL validation | **400** |

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
exists returns **400**.

## Lifecycle (`is-published`)

When `is-published=true`, the release is advanced to **PUBLISHED**.

## HTTP Status Summary

| Status | Meaning |
| --- | --- |
| **201** | App or release created |
| **400** | Invalid type / URL / version / second release not allowed |
| **409** | Not used for normal web URL create package checks |
| **500** | Unexpected failure |

## Acceptance Criteria

- [ ] `POST /applications/web-app` creates a web application (`WEB_CLIP` or `WEB_APP`) and returns **201**.
- [ ] Invalid or missing URL / version / type returns **400**.
- [ ] Second release for the same web app returns **400**.
- [ ] `is-published=true` advances lifecycle to published as implemented.

## Maintenance Rules

- Keep `type` restricted to `WEB_CLIP` and `WEB_APP`.
- Keep URL validation on release create.
- Update this spec when web create validation or single-release limits change.
