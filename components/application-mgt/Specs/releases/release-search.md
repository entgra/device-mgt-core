# Release Search

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Observed contract

ApplicationManager.getApplicationReleases takes an application ID and a
ReleaseSearchFilter. It resolves the application, then queries releases and a
separate count under the current tenant. Missing application resolution can fail
before the release query. The method argument, not filter.applicationId, selects
the application's releases.

| Input or condition | Current behavior |
| --- | --- |
| Default filter | limit 20, offset 0, package partial match, version full match |
| Caller has test roles; type omitted | TEST and PRODUCTION eligible |
| Caller has test roles; type supplied | AppReleaseType.valueOf; invalid/case-mismatched enum text can throw IllegalArgumentException |
| Caller lacks test roles; type omitted or PRODUCTION ignoring case | PRODUCTION only |
| Caller lacks test roles; other nonempty type | Empty releases and a new default Pagination object; normal pagination population is bypassed |
| Lifecycle | Configured end state excluded; optional installableState further filters state |
| Package/version | Nonempty values enable full equality or partial LIKE matching in the generic DAO |
| Normal response | Pagination.limit/offset from input; count from count query; size from returned list |

The generic DAO has LIMIT/OFFSET but no ORDER BY in this query. Stable ordering
across pages is not an established guarantee. Do not invent service-side range
validation or universal HTTP errors: adapter validation and dialect behavior need
separate evidence. This is release search, not the catalog's application list.

## Requirement and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-REL-005 | Observed compatibility baseline | Preserve role/type selection, lifecycle exclusions, filter semantics and normal versus early-empty pagination shapes. | AC-APP-REL-005: test both role states with omitted/TEST/PRODUCTION/invalid type; package/version match modes; end-state releases; normal and empty pages; record ordering rather than assume it. |

Sources: AM.getApplicationReleases; ReleaseSearchFilter;
GenericApplicationReleaseDAOImpl.getReleases/getReleasesCount, indexed in
[source map](../source-map.md#baseline-expansion). APP-GAP-15 tracks unresolved ordering/error contracts.
