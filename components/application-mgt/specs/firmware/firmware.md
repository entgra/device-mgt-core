# Firmware Models and Release Availability

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Observed behavior

Firmware is CUSTOM application behavior, not ordinary enterprise publication.
Availability resolves the device and records its reported current firmware version
through the device-model service before querying APPM. No model or no matching
application returns an empty list; a non-CUSTOM model application is rejected.

Without test roles, release selection is restricted to PRODUCTION. The installable
state comes from the CUSTOM lifecycle configuration. An unknown supplied version
falls back to the latest eligible release; without a supplied version the method
consults installed subscription history. For a known version it queries subsequent
releases and can attach pending-operation status. Do not replace this with assumed
semantic-version ordering: DAO ordering and supported version formats need explicit
owner confirmation.

Available-model listing excludes model IDs already mapped in the current tenant.
The supplied MySQL mapping constraint is unique on FIRMWARE_DEVICE_MODEL_ID alone;
whether global model identity is intended must be confirmed before suggesting a
tenant-aware constraint change.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-FW-001 | Observed | Select eligible CUSTOM firmware by model, configured installable state, test-role visibility and supplied/history version; retain documented empty/error outcomes. | AC-APP-FW-001: no device/model/app, unknown/known/absent version, test/non-test caller, pending install and multiple eligible releases; assert ordering against the agreed DAO/version contract. |
| APP-FW-002 | Observed mapping; identity decision pending | Keep model association and release/device matching consistent with model ownership; distinguish reported-version persistence from firmware-list retrieval. | AC-APP-FW-002: already-mapped model, cross-tenant/global identity fixture, match-type/group pagination and failure after device-version update before APPM read. |

APP-GAP-05/09/10 apply. Device-model storage and agent update behavior are dependent
contracts, not modified here. Apply APP-X-001/003/004 and [lifecycle](../lifecycle/lifecycle.md).
