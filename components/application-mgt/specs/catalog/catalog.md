# Application Catalog and Organization

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Observed behavior

getApplications queries by tenant/filter/device type, loads categories/tags/roles,
then applies additional Java visibility and release filtering. End-state apps are
omitted. CUSTOM TEST releases are hidden without test roles. Count is fetched by
a separate DAO query; visible result count must not be assumed equal to DB count
without checking the same predicates. getFavouriteApplications adds the current
username to the filter before using the same listing flow.

Favourites are application/user/tenant scoped. Existing release deletion clears
all favourites for the app, not just the deleting user's favourite. Category/tag
CRUD and app mappings are separate service methods. Category deletion rejects
in-use categories; exact rename/case/collision behavior across engines needs tests.

Application deletion checks every release's deletable state and device subscription
history, removes related records and artifacts, then commits. Retirement is a
distinct operation and must not be described as physical deletion. Tenant-wide
cleanup is privileged internal work, covered by runtime requirements.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-CAT-001 | Observed filtering; count consistency proposed | Lists/details respect tenant, visibility, release state/type and test-role rules; joins do not duplicate releases. Define count as visible matches before paging. | AC-APP-CAT-001: multi-category/multi-release fixture with restricted roles, retired app and CUSTOM TEST release; compare list/detail/count across pages and roles. |
| APP-CAT-002 | Observed operations; edge contract pending | App metadata, tags and categories remain tenant-scoped and preserve mappings/validation; deletion of in-use categories is rejected. | AC-APP-CAT-002: add/rename/map/remove labels and update app; duplicate/case/foreign-tenant inputs; assert mapping integrity and service exceptions. |
| APP-CAT-003 | Observed favourite behavior | Add/remove/check/list favourites under the caller identity; preserve existing release/app delete cleanup pending APP-GAP-07 decision. | AC-APP-CAT-003: two users and tenants favourite an app; hard-delete a permitted release and compare favourite rows; retirement is tested separately. |
| APP-CAT-004 | Observed guards; failure guarantee proposed | Reject deletion when any release is not deletable or has subscription history; do not leave an unrecoverable DB/storage split. | AC-APP-CAT-004: mixed-state app, historical subscription, eligible app and injected storage/commit failure; inspect related records and artifact reachability. |

See [release/category regression](releases-vs-categories.md) and
[favourite cleanup](release-delete-cleanup.md). APP-X-001/002/004 and
APP-GAP-04/07/10 apply. Listing count alignment is a verification question, not a
confirmed defect based solely on separate queries.

[Retirement contract](retirement.md) defines the end-state precondition separately from deletion.
