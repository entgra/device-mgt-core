# Identity-Service-Provider and VPP Integrations

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Observed responsibilities

SPApplicationManager manages configured identity servers, validates server URLs
and requests, retrieves remote service-provider applications, and attaches/detaches
local applications through AP_IS_SP_APP_MAPPING. createSPApplication creates the
application and mapping through a distinct path; failure must be analyzed across
artifact/application creation and mapping, not assumed to be remote-transactional.

VppApplicationManager registers/updates/synchronizes users, synchronizes paginated
assets, obtains tokens and creates associations with a remote backend. Local data
uses AP_VPP_USER, AP_ASSETS and AP_VPP_ASSOCIATION. Provider responses, credentials
and remote effects cannot participate automatically in the local JDBC transaction.

This baseline inventories contracts; it does not certify the current external
provider API version, rate limits, token lifetime or live compatibility.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-INT-001 | Observed operations; security/atomicity proposed | Validate identity-server configuration and authorized app/SP mapping; preserve tenant boundaries and define create/attach partial-failure recovery. | AC-APP-INT-001: create/update/list identity servers, attach/detach valid/invalid/foreign apps, duplicate mappings, unreachable provider and failure after app creation before attachment. |
| APP-INT-002 | Observed operations; retry semantics proposed | Synchronize VPP users/assets and associations without silently duplicating provider effects; separate auth, validation, transient and uncertain-outcome failures. | AC-APP-INT-002: stub paginated provider, expired token, rate limit, duplicate request and timeout after remote success; reconcile provider and local IDs before retry. |

## Open decisions

APP-GAP-12: integration owner must identify supported provider versions, credential
storage/redaction, timeout/retry budgets, pagination terminal conditions and
idempotency/reconciliation capabilities. These are proposed requirements, not
claims that retries currently exist. Use stubs first in Phase 2; live-provider tests
require dedicated tenant credentials and explicit authorization. APP-X-001/002/004 apply.
