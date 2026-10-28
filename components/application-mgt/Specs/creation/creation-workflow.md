# Application Creation Workflow

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md) | [Sources](../source-map.md)

Existing field/type contracts: [shared](app-creation.md),
[enterprise](app-creation-enterprise.md), [public](app-creation-public.md),
[web](app-creation-web.md), [custom](app-creation-custom.md).

## Scope and observed behavior

Publisher create handlers call validateAppCreatingRequest before createApplication.
The core create entry point itself delegates to createApplicationBasedOnRemoteStatus;
internal callers must not assume it automatically repeats all API validation.
Validation checks name length (20), categories, device type where applicable,
visibility and type-specific release fields. Null/blank and wrapper combinations
still require negative tests; this baseline does not promise all malformed payloads
already produce a controlled domain exception.

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
| APP-CRT-002 | Observed API mapping; pending contract verification | Synchronous create returns 201 plus Application; validation maps to 400, package/hash ConflictException to 409, unexpected management/storage failures to 500. Do not generalize add-release 403 handling to initial create. | AC-APP-CRT-002: invoke all four Publisher adapters and assert status/entity for success and each exception category, including an internal caller bypassing API validation as a separate characterization case. |
| APP-CRT-003 | Observed attempt; proposed recovery guarantee | Failed synchronous creation must not leave a committed partial application; artifact compensation failures must be visible and recoverable. | AC-APP-CRT-003: fail after upload, after application insert and before commit; inspect all related rows and storage; separately fail compensation and verify the approved diagnostic/recovery path. |
| APP-CRT-004 | Observed async split; UX decision pending | Distinguish remote release dispatch from release completion; retain tenant context and do not advertise remote completion from the initial response. | AC-APP-CRT-004: pause/fail the remote downloader, observe the initial app response and later release state, verify tenant context and classify an interrupted worker after restart. |

## Boundaries and open decisions

APP-X-001/002/003/004 apply. Name/hash checks are not a cross-node guarantee.
Do not invent durable job status, automatic retries or a new 202 response: these
are APP-GAP-02/03/04 decisions. Detailed UI behavior is owned by proprietary
commons. Verification is in [matrix](../verification-matrix.md); no tests run here.
