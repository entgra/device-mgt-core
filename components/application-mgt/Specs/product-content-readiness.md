# Product Documentation and Automation Readiness

Status: **Draft proposal; product, engineering, QA and marketing approval pending**.
Scope: Application Management; a candidate pattern for other components, not a
product-wide readiness assessment. Prepared 2026-10-27 against the
[Phase 1 baseline](phase-1-report.md). Named owners and target releases are pending.

## Purpose and boundaries

Use a shared, traceable specification base to support development, bug fixing,
review, product documentation and marketing content. Audience-specific outputs
must link to evidence rather than turn source observations into product promises.

The current baseline supplies capability descriptions, implementation references,
acceptance criteria and engineering gaps. It supports investigation and drafting;
it does not establish complete deployed-product behavior, release support,
runtime verification or approval to publish. This document records missing inputs
and proposed work only. It adds no implementation, tests or publishing automation.

## Readiness by use

| Use | Available foundation | Required before relying on the output |
| --- | --- | --- |
| Development | Behavioral specs, requirement IDs, source map | Approved intent, resolved decisions, compatibility constraints and acceptance evidence |
| Bug fixing | Expected behavior and implementation locations | Incident reproduction, affected versions, configuration, sanitized logs and regression evidence |
| Review | Requirements and documented risks | Change-to-requirement mapping, review rules and meaningful executable checks |
| API documentation | Core contracts and sampled adapter mappings | Complete routes, scopes, payloads, errors, examples and version applicability |
| User documentation | Capabilities and lifecycle rules | Validated user journeys, prerequisites, UI evidence, configuration and recovery instructions |
| Marketing | Technical capability descriptions | Audience, customer problem, approved benefit/claim, release scope and supporting evidence |
| Automated publication | Linked source documents | Approval gates, reproducible generation, freshness checks and controlled publication |

## Linked information model

Keep these records linked rather than duplicating all information in every feature
spec. Existing APP-CAP and APP requirement IDs remain the engineering identifiers.
The fields below are proposed record templates, not completed records.

| Record | Required fields |
| --- | --- |
| Capability brief | Capability ID; persona; problem; intended outcome; supported use cases; non-goals; owner; approval |
| Release/support record | Capability ID; product edition/version; component and consumer commits; platforms; deployment modes; permissions; dependencies; defaults/overrides; limitations; evidence |
| Journey record | Capability/requirement IDs; actor; prerequisites; UI/API/agent versions; actions; observable outcomes; asynchronous completion; failure/recovery paths; examples/screenshots; evidence |
| Verification record | Requirement IDs; test/manual procedure; environment and topology; commits; execution date; result; evidence location; verifier; known deviations |
| Claim record | Exact proposed wording; audience/channel; capability IDs; supported release scope; evidence; qualifications; approver/date; review trigger |
| Generated artifact record | Artifact type/audience; input IDs and revisions; template/generator revision; generation date; reviewer; approval; published version/location; stale status |

Use explicit states: draft, approved intent, source-observed implementation,
runtime-verified and released. These are separate dimensions: approval does not
prove implementation, and passing a test does not prove release availability.
Unknown values remain "pending"; do not infer support from a class or method name.

## Missing inputs and next actions

All items below are **open**. Owners are proposed roles, not assignments. Priority
indicates the recommended sequence, not production defect severity. Existing
APP-GAP items remain authoritative for engineering issues; these CONTENT items
track additional evidence and content needs without closing those issues.

| ID | Priority | Missing input / required action | Proposed owner and evidence source | Closure evidence |
| --- | --- | --- | --- | --- |
| APP-CONTENT-01 | P0 | Define personas, customer problems, outcomes and non-goals for each capability | Product owner; approved requirements and customer/support research | Reviewed capability briefs linked to APP-CAP IDs |
| APP-CONTENT-02 | P0 | Establish edition/version/platform/topology support and packaging/configuration dependencies | Release/platform owner; product builds, packaging, agents and supported-environment records | Approved release/support matrix; relates to APP-GAP-11 |
| APP-CONTENT-03 | P0 | Trace complete API contracts and UI-to-agent journeys, including authorization and asynchronous completion | API/UI/device owners; pinned consumer repos and a deployed environment | Reviewed routes/scopes/examples and validated journey records; relates to APP-GAP-03/10 |
| APP-CONTENT-04 | P0 | Supply verification evidence for acceptance criteria and supported deployment modes | QA/component owner; Phase 2 tests and recorded manual runs | Dated results mapped in the verification matrix, with failures/limitations retained; relates to APP-GAP-13 |
| APP-CONTENT-05 | P0 | Resolve intended behavior and assign named approvers; retain explicit limitations for unresolved decisions | Product/component owners; engineering gap register | Approved decisions or documented exclusions, with compatibility implications; relates to APP-GAP-01 through APP-GAP-12 |
| APP-CONTENT-06 | P1 | Collect prerequisites, configuration examples, screenshots and safe troubleshooting/recovery procedures | Documentation/support owners; validated product workflows and sanitized incidents | Reproducible documentation examples and reviewed troubleshooting records |
| APP-CONTENT-07 | P1 | Define positioning and substantiate each benefit, performance or comparative claim | Product/marketing owners; approved research, measurements and permission-cleared customer evidence | Claims register containing exact wording, scope, evidence and approval; unsupported claims excluded |
| APP-CONTENT-08 | P0 | Define traceability, review gates, change impact and staleness handling for generated outputs | Engineering/documentation leads; repository workflow and publication process | Approved generation/review policy and templates; automation implementation separately authorized |

Customer anecdotes, performance percentages and competitive comparisons cannot be
derived from source code. Quantified claims need a reproducible measurement method,
environment and limitations. Customer references and screenshots need permission
and sanitization; never commit credentials, tokens, personal data or private logs.

## Proposed execution items

These are follow-up deliverables, not completed Phase 1 tasks. Documentation work
may proceed alongside the existing [Phase 2 backlog](phase-2-backlog.md); tests and
implementation remain there and require their own authorization.

| Item | Execution | Dependencies | Completion condition |
| --- | --- | --- | --- |
| CONTENT-EXEC-01 | Assign owners and select one product release/environment for the pilot | CONTENT-02/05 | Named reviewers, pinned sources and explicit support boundaries |
| CONTENT-EXEC-02 | Prepare a capability brief and journey for creating, publishing and installing an enterprise application | EXEC-01; CONTENT-01/03 | Actors, prerequisites, API/UI/agent steps and failure paths reviewed; request acceptance distinguished from actual installation |
| CONTENT-EXEC-03 | Link acceptance criteria to verification evidence; document unresolved limitations | EXEC-02; CONTENT-04/05; relevant P2 work | Evidence records identify what passed, failed or remains unverified |
| CONTENT-EXEC-04 | Prepare documentation inputs and an approved claims register for the pilot | EXEC-02/03; CONTENT-06/07 | Examples reproducible, claims scoped and unsupported assertions removed |
| CONTENT-EXEC-05 | Draft a user guide, troubleshooting article and marketing feature brief from the same linked records | EXEC-04 | Each output traces to its inputs; documentation, QA/product and marketing reviewers approve their respective outputs |
| CONTENT-EXEC-06 | Define generation templates, impact checks and approval gates; compare with the notification pilot | EXEC-05; CONTENT-08 | Reviewed workflow identifies manual and automatable steps; no automatic merge/publication assumed |
| CONTENT-EXEC-07 | Extend the validated pattern to remaining application-management capabilities | EXEC-06 | Coverage tracked per capability and output type; missing inputs remain visible |

## Automation safeguards

- Development proposals must identify affected requirements, open decisions and
  tests. Generated code requires review and verification before merge.
- Bug-fix proposals must distinguish reproduced failures from hypotheses, link the
  affected release and include regression evidence before declaring resolution.
- Reviews must compare changes with approved intent and report source/spec drift;
  current implementation is not automatically the correct behavior.
- Documentation generation must select an explicit audience and target release.
  Unverified workflows can appear only in clearly labeled internal drafts.
- Marketing generation must use approved claims and preserve their qualifications.
  Unsupported superiority, security, scale or performance claims must be omitted.
- Changes to linked requirements, APIs, UI, agents, configuration, evidence or
  release scope mark dependent outputs stale and require re-review. Approval of
  an older artifact does not transfer automatically to newly generated content.
- Publication and code merge remain human-approved until a separately approved
  automation policy defines permissions, checks and failure handling.

## Acceptance and progress reporting

- [ ] Named owners approve the pilot scope and information model.
- [ ] Pilot records link capability, requirements, release, journey and evidence.
- [ ] Initial success, installation completion and failure recovery are distinct.
- [ ] Each public claim is substantiated and applicable to the named release.
- [ ] Sample outputs have their audience-specific approvals and no sensitive data.
- [ ] An input change can be traced to outputs requiring revalidation.
- [ ] Remaining capabilities have explicit open items rather than inferred coverage.

Report documentation coverage, approval, runtime verification and released support
separately, with named denominators and evidence dates. Do not turn the presence
of this document or the Phase 1 capability count into a product-readiness percentage.
All checkboxes above are pending; writing this plan does not satisfy them.
