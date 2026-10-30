# Application Creation Service Contract

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

Canonical requirements: [APP-CRT-001 through APP-CRT-004](creation-workflow.md).

## Scope and inputs

ApplicationManager creates an application and optionally its first release using
one of four wrapper families. Callers establish tenant/user context and invoke
validateAppCreatingRequest before createApplication; creation does not repeat the
entire validator. Field definitions live in the common wrapper/DTO classes.

| Wrapper | Persisted type | Detailed service contract |
| --- | --- | --- |
| ApplicationWrapper | ENTERPRISE | [Enterprise](app-creation-enterprise.md) |
| PublicAppWrapper | PUBLIC | [Public](app-creation-public.md) |
| WebAppWrapper | WEB_CLIP or WEB_APP | [Web](app-creation-web.md) |
| CustomAppWrapper | CUSTOM | [Custom](app-creation-custom.md) |

## Execution

```text
Caller establishes tenant/user context
  -> validateAppCreatingRequest(wrapper)
  -> createApplication(wrapper, isPublished)
     -> select synchronous or remote path
     -> prepare artifacts when release is present
     -> APPM transaction: application, mappings, optional release/history
     -> optional lifecycle publication
     -> attempt commit and return Application
```

For synchronous creation, artifact preparation precedes the transaction try block.
Selected caught failures attempt rollback and artifact cleanup. Those operations
are separate resources; neither cleanup nor preservation of the primary exception
is universally guaranteed. Initial enterprise icon persistence also uses a separate
device-management transaction. See APP-GAP-01/04/21.

For remote enterprise/custom creation, application persistence can complete before
a background worker creates the release. The initial Application result does not
prove release completion or publication. See APP-CRT-004 and APP-GAP-03.

## Validation and service failures

| Condition | Observed service outcome |
| --- | --- |
| Null/empty or overlength name | BadRequestException; exact normalization edges are type-specific |
| Null/empty categories or unknown requested category | BadRequestException |
| No registered tenant categories | ApplicationManagementException |
| Missing required device type or duplicate display name | BadRequestException on the described validation path |
| Invalid supplied role names | ApplicationManagementException |
| Caller lacks required unrestricted-role membership | BadRequestException unless allowed to view all roles |
| First release present | Type-specific release validation |
| Package/binary collision where checked | ConflictException |
| Persistence, storage or configuration failure | Domain management/resource exception, possibly wrapped by caller path |

Null wrappers, null list elements and malformed metadata/ranges need explicit
characterization; annotations and these examples do not establish controlled
errors for every malformed input. See the enterprise validation matrix for the
precise distinctions traced for that type.

## Publication and results

isPublished controls advancement of a present initial release through the
configured checks and hard-coded publication sequence. Enterprise/public/web
normally target PUBLISHED; custom targets RELEASED. An empty release list produces
an application without lifecycle history even when publication is requested.

## Acceptance

- [ ] Each wrapper creates the matching persisted type and tenant-owned records.
- [ ] Callers can distinguish validation, conflicts and management failures by
      service exception type where the implementation preserves it.
- [ ] Synchronous results and later remote completion are tested separately.
- [ ] Failure tests inspect rows, files, independent icon persistence and connection
      ownership rather than assuming JDBC rollback restores every resource.
- [ ] Type-specific rules and open decisions remain in their governing contracts.
