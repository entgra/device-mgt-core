# Artifacts, Transfer and Storage

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Observed boundaries

FileTransferService generates upload links, resolves chunks and writes them;
release wrappers carry artifact/image links. ApplicationStorageManager handles
installer parsing, images, hash-oriented paths, release deletion and tenant folders.
AppmDataHandler serves artifact and agent streams. The packaged default storage
path is repository/resources/apps/. FileDownloaderServiceProvider selects the
download implementation for a URL. Remote creation behavior is specified separately
in [creation workflow](../creation/creation-workflow.md).

Tenant path parameters alone are not proof that every artifact read is
authorized. Upload link lifetime, partial-upload cleanup, path traversal, URL
fetch policy, maximum sizes and streaming limits require service/configuration review.
Do not assume local files exist on all nodes or that MD5 is a security signature.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-STO-001 | Observed pipeline; boundary guarantees proposed | Resolve uploaded links/chunks and parse type-appropriate installers without accepting unauthorized paths or cross-tenant artifacts. | AC-APP-STO-001: valid Android/iOS fixtures, malformed/missing installer, incomplete chunk, forbidden path and tenant mismatch; assert controlled failure without foreign file access. |
| APP-STO-002 | Proposed recovery guarantee | Treat DB changes and artifact operations as separate resources; define compensation/reconciliation and ownership before removing files. | AC-APP-STO-002: fail upload, DB insert, commit, replacement, deletion and compensation; verify reachable artifacts and DB references or an explicitly recoverable deviation. |
| APP-STO-003 | Proposed deployment/security contract | Declare shared-storage or routing assumptions and remote URL controls; protect artifact access independently of guessing a URL/hash. | AC-APP-STO-003: upload on node A/download on B under supported topology, unauthenticated/foreign-tenant access and prohibited remote destinations; document allowed URL/size/timeout rules before implementation. |

## Decisions and compatibility

APP-GAP-03/04/10/11 cover completion signaling, crash recovery, artifact-read callers
and deployment settings. Keep current link formats and download contracts unless
owners approve a versioned change. No storage migration or security policy is
implemented in Phase 1. Utility/parser tests map only part of APP-STO-001.

[Platform artifact contracts](platform-artifacts.md) specify MSI metadata and iOS plist generation.

## Enterprise creation boundaries

The link-based enterprise pipeline resolves installer/image URLs through the
configured downloader before APPM persistence. Installer output is stored under
the configured root, tenant ID, binary hash and APP_ARTIFACT directory. Release
UUID and storage hash are distinct identities. Android/iOS package/version are
parsed from installers; Windows uses the wrapper and filename-extension checks.

Initial creation prepares artifacts before entering its transaction try block.
Failures during that preparation do not reach the later APPM rollback/compensation
catches. After preparation, selected initial-creation failures call
deleteArtifactIfExist; additional enterprise release failures in createRelease are
caught by triggerEntAppRelease, which attempts hash-directory deletion. These
helpers can fail; neither owns a demonstrated atomic download/filesystem/DB unit.
Concurrent identical hashes also require surviving-file ownership assertions.

Initial enterprise creation additionally saves icon information through a separate
device-management transaction before APPM commits. A successful icon write can
outlive an APPM failure. Include both repositories in APP-STO-002/APP-X-004 failure
checks; APP-GAP-21 records the unresolved reconciliation contract. See the
[enterprise failure matrix](../creation/app-creation-enterprise.md#failure-boundaries).
