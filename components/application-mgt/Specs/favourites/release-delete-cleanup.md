# Favourites Cleanup On Release Delete Spec

## Purpose

When a Publisher user deletes an application release, Store favourites for that
application must be cleared so Favourites does not keep an entry that opens with
“no release found”.

## App And Audience

| Concern | Detail |
| --- | --- |
| Apps | Application Publisher (delete release) / Application Store (Favourites) |
| Entry | Publisher deletes a release; Store **Favourites** no longer lists that app |
| Users | Any Store user who favourited the app |
| Status | API-backed (core cleanup on delete) |

## Product Rule

Favourites are stored per **application id** (`AP_APP_FAVOURITES`), not per release.
Store Favourites cards navigate using an installable `applicationReleases[0]`.

**RETIRED** is the lifecycle end state (row still exists). It is **not** a hard delete.
Hard-deleting a release removes favourite rows so Store does not keep a stale favourite
that would open with “no release found”.

Listing favourites for Store still filters to the installable release state (see Store
`favourites.md`); retiring alone does not require deleting favourite rows.

When an application release is **hard-deleted**, all favourite rows for that application
(all users of the tenant) are deleted in the same DB transaction.

When the entire application is deleted, favourites for that application are
deleted as part of application cleanup.

## Source Files

| Area | Path |
| --- | --- |
| Manager | `.../ApplicationManagerImpl.java` (`deleteApplicationRelease`, `deleteApplication`) |
| DAO | `ApplicationDAO.deleteAppFavouritesByAppId` |
| DAO impl | `GenericApplicationDAOImpl.deleteAppFavouritesByAppId` |
| Table | `AP_APP_FAVOURITES` |

## Behaviour

### Delete application release

In `deleteApplicationRelease(releaseUuid)`, after the release row is deleted:

1. `applicationDAO.deleteAppFavouritesByAppId(appId, tenantId)`
2. Commit with the same transaction as release / lifecycle delete

### Delete application

In `deleteApplication(applicationDTO, tenantId)`, before deleting the app row:

1. `applicationDAO.deleteAppFavouritesByAppId(appId, tenantId)`

### DAO

```sql
DELETE FROM AP_APP_FAVOURITES
WHERE AP_APP_ID = ?
AND TENANT_ID = ?
```

## Correct Behaviour

| Action | Expected |
| --- | --- |
| Delete a release of a favourited app | App disappears from Store → Favourites for all users |
| Call Favourites after that delete | App is not listed; no “no release found” from stale favourite |
| Delete entire application | Favourites for that app are removed |
| App not favourited | Delete is unchanged; favourite delete is a no-op |

## Acceptance Criteria

- [ ] Deleting an application release removes that app from `AP_APP_FAVOURITES` for the tenant.
- [ ] Store Favourites no longer shows the app after release delete + refresh.
- [ ] Favourite cleanup runs in the same transaction as release delete.
- [ ] Deleting an application also removes its favourite rows.

## Maintenance Rules

- Keep favourite cleanup in core `ApplicationManagerImpl` delete paths, not in Store UI.
- Do not require the deleting user to have been the one who favourited the app.
- When delete behaviour changes, update this spec in the same change.
