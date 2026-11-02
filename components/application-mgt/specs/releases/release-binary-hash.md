# Release Binary Hash Uniqueness Spec

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

Requirement: [APP-REL-002](release-management.md).
The tenant-wide rule below is the intended invariant. The inspected prechecks
do not reserve a hash and are not proof of simultaneous-upload safety. APP-GAP-02
requires concurrent tests and an approved enforcement/error-handling decision.

This hash query has no lifecycle exclusion: an end-state release can still
conflict. The separate initial-enterprise package check excludes the configured
end state and is skipped by additional and remote-initial release paths. Do not
generalize package rules from hash uniqueness. See the
[enterprise identity matrix](../creation/app-creation-enterprise.md#validation-and-identity).

## Purpose

A tenant must not store two application releases that share the same installer
binary MD5. This applies across **all applications** in the tenant (not only
releases of the app currently being edited).

Callers may precheck a hash before creation, but the artifact-add path repeats
the check. The early result does not reserve a hash.

## Product Rule

`ApplicationReleaseDAO.verifyReleaseExistenceByHash(hash, tenantId)` is
**tenant-wide**. If any release row already uses that `APP_HASH_VALUE`, adding
another release with the same binary must fail with `ConflictException`.

Checks apply across applications within the tenant, not only to the current app.

## Source Files

| Area | Path |
| --- | --- |
| Manager | `ApplicationManagerImpl.validateReleaseBinaryFileHash` |
| Manager interface | `ApplicationManager.validateReleaseBinaryFileHash` |
| Used on artifact add | `ApplicationManagerImpl.addApplicationReleaseArtifacts` (and create paths that call it) |
| DAO | `verifyReleaseExistenceByHash` |

## Behaviour

### Core validation

`validateReleaseBinaryFileHash(String hash)`:

1. Open APPM DB connection for the current tenant.
2. If `verifyReleaseExistenceByHash(hash, tenantId)` is true → throw
   `ConflictException` (logged as error).
3. Otherwise return successfully (hash is free).

Create / add-release artifact pipelines call this (or equivalent) when
persisting a stored installer so submit still enforces uniqueness even if the
caller skips the precheck.

## Correct Behaviour

| Case | Expected |
| --- | --- |
| Hash unused in tenant | Validator returns normally; create/release may proceed |
| Hash already used by same app | ConflictException from validation/artifact add |
| Hash already used by another app | ConflictException from validation/artifact add |
| Caller skips precheck | Artifact add still raises ConflictException |

## Acceptance Criteria

- [ ] Duplicate binary MD5 in the tenant throws `ConflictException`.
- [ ] Uniqueness is tenant-wide (cross-application).
- [ ] validateReleaseBinaryFileHash exposes the same tenant-wide check to service callers.
- [ ] Create / add-release submit remains authoritative if the pre-check is skipped.

## Maintenance Rules

- Preserve ConflictException for duplicate binary identity on the described paths.
- Do not narrow tenant-wide uniqueness to a single application without an explicit
  compatibility decision and updated requirements and regression tests.
- Keep the precheck and artifact-add behavior aligned.
