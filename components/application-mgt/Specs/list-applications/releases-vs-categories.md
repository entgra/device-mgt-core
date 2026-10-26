# Application list: releases vs categories

## Purpose

`getApplications` returns each application release once. Category assignment on
an app does not change how many releases are returned in the list or Publisher
drawer.

## Behaviour

| Setup | `applicationReleases` count |
| --- | --- |
| 1 release, 1 category | 1 |
| 1 release, N categories | 1 |
| R releases, N categories | R (each unique once) |

- Category **filter** on list queries uses category joins only in the filter
  subquery (`GROUP BY AP_APP.ID`).
- Category **values** on the response come from `getAppCategories()` /
  `setApplicationProperties()`, not from the outer release join.

## Source

| DAO | Method |
| --- | --- |
| `GenericApplicationDAOImpl` | `getApplications` |
| `OracleApplicationDAOImpl` | `getApplications` |
| `SQLServerApplicationDAOImpl` | `getApplications` |

## Acceptance

- [ ] App with one release and one category shows one release.
- [ ] Adding or removing categories does not change release count.
- [ ] Filtering apps by category still works.
