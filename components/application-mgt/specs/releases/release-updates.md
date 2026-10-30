# Release Update Compatibility

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Scope

This supplements APP-REL-004 with the type-specific update methods' observed
behavior. It does not define uniform partial-update semantics or authorize fixing anomalies.
Artifacts are resolved before the transaction; filesystem work and JDBC are not atomic.

## Field and validation matrix

| Concern | Observed behavior |
| --- | --- |
| Type | Validator requires exact parent type; the web entry passes WEB_CLIP, not WEB_APP |
| Editable state | Checked for enterprise/custom when installer name and stream exist; public when packageName is nonblank; web when URL is nonblank. Not a blanket gate for all metadata edits |
| Description and metadata | Nonempty values overwrite existing values; empty strings do not clear them |
| Supported OS versions | Enterprise/public update nonempty values and validate their range |
| Price | Enterprise/public/web assign wrapper price; custom assigns only if nonnull. Validator reads price from enterprise/public; other branches retain its initialized zero |
| Price validation | Negative rejected; zero rejected for PAID; positive rejected for FREE, using the validator's price value |
| Sharing | Existing isSharedWithAllTenants is assigned back to itself, not updated from the wrapper |
| Release type | Guard uses String.valueOf(value); it is not a reliable null-preservation check |
| Enterprise package/version | Windows permits nonempty wrapper overrides; binary replacement also invokes artifact parsing/replacement logic |
| Public/custom package/version | Nonempty version assigns version. The packageName branch also assigns version, not packageName; do not describe it as a supported package rename |
| Web URL/version | Nonempty URL assigns installerName; nonempty version assigns version |
| Installer | Enterprise/custom require installer name plus stream before replacement; public/web construct artifacts without a binary link |
| Images | Each type delegates optional image updates to updateImageArtifacts |
| Persistence result | Null DAO update result triggers rollback and returns null; otherwise commit and DTO conversion are attempted |

NotFoundException exists in the shared validator, but enterprise/custom dereference
the lookup result before invoking it. Missing UUIDs therefore must not be promised
to produce a uniform domain error. Custom replacement also closes a connection
inside its changed-hash branch before the outer flow completes. These are observed
risks, not intended guarantees; APP-GAP-01/04/14 require characterization and decisions.

## Requirement and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-REL-006 | Observed compatibility baseline with anomalies | Preserve the documented per-type field/guard behavior unless an explicit approved change supersedes it; keep anomalies visible rather than silently normalizing them. | AC-APP-REL-006: omitted/null/empty/nonempty fields for each wrapper; metadata-only versus binary/package/URL update in noneditable state; WEB_APP versus WEB_CLIP; missing UUID; same/changed hash; null DAO result and failure between file/DB updates. |

Sources: AM.updateEntAppRelease/updatePubAppRelease/updateWebAppRelease/
updateCustomAppRelease and validateAppReleaseUpdating.
