# Application Creation Workflow

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

Existing field/type contracts: [shared](app-creation.md),
[enterprise](app-creation-enterprise.md), [public](app-creation-public.md),
[web](app-creation-web.md), [custom](app-creation-custom.md).

## Scope and observed behavior

Callers invoke validateAppCreatingRequest before createApplication.
The core create entry point itself delegates to createApplicationBasedOnRemoteStatus;
callers must not assume it automatically repeats all validation.
Validation checks name length (20), categories, device type where applicable,
visibility and type-specific release fields. Null/blank and wrapper combinations
still require negative tests; this baseline does not promise all malformed payloads
already produce a controlled domain exception.

For the complete enterprise branch, including first-entry-only release handling,
null/empty validation distinctions and caller obligations, use the
[enterprise contract](app-creation-enterprise.md). It defines service inputs,
results, exceptions and caller obligations.

For synchronous creation, triggerApplicationCreation uploads/prepares artifacts
before opening a DB transaction, calls addAppDataIntoDB, and commits. Its caught
failure branches attempt artifact cleanup; filesystem cleanup is not transactional.

For remote enterprise/custom creation, the application can be committed without
its release, after which triggerReleaseAsynchronously starts a new Thread. Tenant
and username are copied and tenant flow ends in finally. Worker failures are
logged, not returned to the original caller. The initiating response is therefore
not proof that a usable/published release exists. This qualifies the older shared
pipeline diagram and type-specific synchronous success descriptions.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-CRT-001 | Observed, preserve pending review | Route enterprise/public/web/custom wrappers through their matching validation and creation paths; preserve type-specific release cardinality and identity rules. | AC-APP-CRT-001: valid fixture per type persists the expected type, tenant and initial release shape; invalid name/category/type/artifact fixtures are rejected without residual application data. |
| APP-CRT-002 | Observed service boundary; malformed-input edges need characterization | Preserve Application results and distinguish BadRequestException, ConflictException and broader management failures on the described creation paths. Do not assume every caller repeats validation or every exception subtype survives wrapping. | AC-APP-CRT-002: invoke validators and create services for each wrapper family; assert result type and propagated/wrapped domain exceptions, including callers bypassing validation as separate characterization cases. |
| APP-CRT-003 | Observed attempt; proposed recovery guarantee | Failed synchronous creation must not leave a committed partial application; artifact compensation failures must be visible and recoverable. | AC-APP-CRT-003: fail after upload, after application insert and before commit; inspect all related rows and storage; separately fail compensation and verify the approved diagnostic/recovery path. |
| APP-CRT-004 | Observed async split; completion decision pending | Distinguish remote release dispatch from release completion; retain tenant context and do not advertise remote completion from the initial response. | AC-APP-CRT-004: pause/fail the remote downloader, observe the initial app response and later release state, verify tenant context and classify an interrupted worker after restart. |

## Boundaries and open decisions

APP-X-001/002/003/004 apply. Name/hash checks are not a cross-node guarantee.
APP-CRT-003 is a proposed guarantee: artifact preparation occurs before the
transaction try block, and initial enterprise icon-info persistence uses a separate
device-management transaction. Neither is covered by an APPM rollback guarantee.
See APP-GAP-19/21 and the enterprise failure matrix.
Do not invent durable job status or automatic retries: these are
APP-GAP-02/03/04 decisions. Verification is in [matrix](../verification-matrix.md); no tests run here.
