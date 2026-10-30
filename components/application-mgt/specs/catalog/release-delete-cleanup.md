# Favourites Cleanup On Release Delete Spec

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

Requirements: [APP-CAT-003/004](catalog.md), [APP-REL-003](../releases/release-management.md).

Clearing all app favourites on any release hard-delete is existing behavior;
whether to retain it when another usable release exists is APP-GAP-07.

## Service behavior

Favourites are application/user/tenant records in AP_APP_FAVOURITES, not
release-owned records. Hard-deleting a release clears all favourites for its parent
application in the tenant, even if another release remains. Confirm that policy
before changing it (APP-GAP-07). Deleting the application also removes its favourites.
Retirement changes lifecycle/status and is distinct from hard deletion; it does
not itself require favourite rows to be deleted.

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
| Delete a release of a favourited app | All tenant favourite rows for the application are removed |
| Query favourites after that delete | The removed favourite mappings are not returned |
| Delete entire application | Favourites for that app are removed |
| App not favourited | Delete is unchanged; favourite delete is a no-op |

## Acceptance Criteria

- [ ] Deleting an application release removes that app from `AP_APP_FAVOURITES` for the tenant.
- [ ] Favourite service queries do not return removed mappings after deletion.
- [ ] Favourite cleanup runs in the same transaction as release delete.
- [ ] Deleting an application also removes its favourite rows.

## Maintenance Rules

- Keep favourite cleanup in core `ApplicationManagerImpl` delete paths.
- Do not require the deleting user to have been the one who favourited the app.
- When delete behaviour changes, update this spec in the same change.
