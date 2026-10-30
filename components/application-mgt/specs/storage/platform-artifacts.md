# Platform Artifact Contracts

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Windows MSI metadata

In installer-add and enterprise replacement paths, a Windows installer whose
name ends in .msi ignoring case triggers syncHashInMetadata after binary MD5
calculation. Blank metadata is left unchanged. Otherwise metadata must parse as
a JSON array of objects. The first object with key equal to MSI_FILE_HASH is
replaced by an object containing key and the calculated hash value; if absent,
one is appended. Other entries remain; duplicate later hash entries are not removed.
Replacement discards any other properties on the first matching object.

JsonSyntaxException and IllegalStateException are wrapped in
ApplicationManagementException. This is not a claim that every malformed element
is handled uniformly. Hash synchronization is distinct from tenant-wide duplicate
binary rejection and is not a cryptographic authenticity guarantee.

## iOS plist generation

getPlistArtifact resolves the release's application in the current tenant; a missing
application produces NotFoundException. It builds XML with one software-package
asset and metadata containing bundle identifier, bundle version, kind=software
and application title. The URL is the configured artifact download base followed
by tenant ID, release UUID, APP_ARTIFACT and installer name.

The implementation substitutes values into an escaped template and unescapes the
result. No explicit platform/lifecycle authorization or per-value XML escaping is
performed by this method. Do not infer public download authorization or valid XML
for arbitrary special characters; service access and escaping need verification.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-STO-004 | Observed MSI behavior | Describe and preserve hash metadata synchronization independently of duplicate checking. | AC-APP-STO-004: Windows MSI add/replacement, other extensions, blank metadata, existing/missing/duplicate hash entries, invalid JSON and non-array input; compare all retained fields and failure outcomes. |
| APP-STO-005 | Observed plist behavior | Generate the tenant/release-specific installation asset and metadata from the resolved application; retain missing-release failure behavior. | AC-APP-STO-005: known/unknown/foreign release, configured URL base and XML-sensitive package/version/title values; parse generated XML and record deviations; test service access and caller-context requirements separately. |

Sources: AM.syncHashInMetadata, addApplicationReleaseArtifacts,
updateEntAppReleaseArtifact and getPlistArtifact.
APP-GAP-16 tracks malformed metadata/XML and service-boundary questions.
