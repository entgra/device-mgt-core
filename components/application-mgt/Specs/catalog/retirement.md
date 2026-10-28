# Application Retirement

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Observed contract

retireApplication resolves the application and loads its releases before beginning
its write transaction. Every release must already equal the configured lifecycle
end state. Any other release causes ForbiddenException. An empty release list
passes this loop. The DAO sets the application's status to RETIRED by application
ID; it does not delete the application, releases, favourites or artifact files.

The generic application-list DAO excludes RETIRED applications in its relevant
list/count queries. This is not a blanket assertion that every detail/internal
lookup hides retired applications. Repeated requests are not explicitly rejected
by retireApplication; actual behavior also depends on the initial lookup.

Eligibility is checked against data loaded before the transaction, not against a
locked/reloaded snapshot. Transaction cleanup does not establish concurrent
retirement safety. APP-GAP-01/02/17 retain the failure/race questions.

## Requirement and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-CAT-005 | Observed compatibility baseline | Reject retirement with any non-end-state release; eligible retirement marks app status rather than physically deleting its data/artifacts. | AC-APP-CAT-005: no releases, all end-state, mixed states, unknown/foreign app, repeated request and concurrent release transition; inspect status, list/detail visibility and retained rows/files. |

Sources: AM.retireApplication; GenericApplicationDAOImpl.retireApplication and
list/count queries in [source map](../source-map.md#baseline-expansion).
