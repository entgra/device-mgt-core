# Single Installable Release Spec

## Purpose

Non-CUSTOM applications allow only one **installable** release (typically
`PUBLISHED`) at a time. Creating a new release with `isPublished=true` must not
advance that release into the installable state if another installable release
already exists.

## Product Rule

In `changeLifecycleState`, when the target action is the installable state and
`hasExistInstallableAppRelease(...)` is true for the application, throw
`ForbiddenException`.

`createRelease(..., isPublished=true)` walks `IN-REVIEW` → `APPROVED` →
`PUBLISHED` (enterprise). The Forbidden check fires on the last step.

CUSTOM / firmware skips this installable uniqueness check.

## Behaviour

| Case | Expected |
| --- | --- |
| Create release with `isPublished=false` while one is PUBLISHED | **201**, new release in initial state |
| Create release with `isPublished=true` while one is PUBLISHED | **403** `ForbiddenException` (DB transaction rolled back; stored binary cleaned up because upload runs before `createRelease`) |
| Create release with `isPublished=true` and no installable release | **201**, new release ends PUBLISHED |

## Acceptance Criteria

- [ ] Forbidden case returns **403** from Publisher create-release (not **500**).
- [ ] Failed publish-on-create rolls back the release transaction.
- [ ] Artifact cleanup still runs for the failed create path.

## Related

- Publisher UI: `.../publisher.ui/Specs/add-new-release/publish-with-existing-installable.md`
- Publisher API: `api-contract.md` § Release Create
