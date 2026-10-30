# Reviews, Replies and Ratings

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Observed behavior

addReview rejects a null wrapper and a non-positive rating, resolves the release
in the tenant, and when allowMultipleReviews is false checks reviews across the
application's releases for the username. It inserts a root review in a transaction.
After commit it starts a new thread to recalculate ratings. The inspected core
guard does not itself establish an upper rating bound; the allowed range must be agreed before choosing a product maximum.

Updates/deletes enforce review ownership unless the caller supplies the privileged
flag. That flag is an internal trust boundary; callers must derive it from actual
authorization. Reply hierarchy uses root/immediate parent IDs. Root deletion
removes children; reply deletion traverses descendants. Rating calculation is
asynchronous; updateReview can dispatch recalculation before updateReviewInDB.
Therefore immediate aggregate consistency is not an established guarantee.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-REV-001 | Observed | Enforce tenant/release existence, positive rating and allowMultipleReviews policy; only owner or trusted privileged caller changes a review. | AC-APP-REV-001: first/second review across releases, foreign owner/tenant, missing UUID and invalid rating; include privileged caller authorization checks. |
| APP-REV-002 | Observed hierarchy; aggregate guarantee proposed | Preserve reply ownership/tree integrity and define eventual rating convergence and recovery after asynchronous failure. | AC-APP-REV-002: nested replies, root/subtree delete, failed review update and delayed/concurrent rating workers; compare aggregate to persisted valid reviews after the approved convergence window. |

APP-GAP-08 records rating range, uniqueness races, update ordering and recovery
decisions. APP-X-001/002/003 apply. No phase-one test evidence establishes these
edge guarantees; see [matrix](../verification-matrix.md).
