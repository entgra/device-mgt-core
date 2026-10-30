# Application Management Specification Workflow

Status: Draft baseline; collaborative review pending.
Review and verification: shared team responsibility.

## Scope and authority

Phase 1 changes specifications only. Do not modify Java, tests, SQL, configuration,
build files, CI or other repositories as part of this phase. Reading their sources
is evidence gathering, not runtime verification. Notification management is a
separate pilot; its conventions can be compared without changing its specs.

The baseline includes core/common services, persistence, storage, tasks, feature
packaging and consumer boundaries. Changes outside this component require their own explicit scope.

Code is evidence of current behavior, not automatic approval of product intent.
Existing specs remain compatibility references, subject to the corrections and
qualifications in the [gap register](gap-register.md). A proposed requirement must
not silently replace an existing service behavior.

## Status model

Track these independently:

| Dimension | Values |
| --- | --- |
| Spec review | Draft / Approved / Deprecated |
| Implementation | Observed / Partial / Proposed / Unknown |
| Verification | Mapped-not-run / Planned / Passed-with-evidence / Failed-with-evidence |

All new documents are Draft. Review outcomes must be recorded explicitly. No tests
were added or executed in Phase 1. Historical successful builds do not establish
acceptance coverage at this baseline. Existing unchecked checkboxes are not a
completion counter.

## IDs and template

Capabilities use `APP-CAP-NN`; requirements use `APP-<AREA>-NNN`; acceptance
scenarios use `AC-<requirement ID>`. A requirement table is the canonical definition
of its IDs; other documents reference those IDs rather than redefining them.
Do not renumber IDs when requirements move or retire.

Each feature spec must state scope/exclusions, observed behavior, requirements and
acceptance criteria, tenant/permission boundaries and persistence/failure/concurrency
semantics. Keep proposed or unresolved behavior labeled where it appears.
Link to central tracking instead of repeating administrative headers:
[spec register](spec-register.md) for review scope and status,
[gap register](gap-register.md) for decisions/deviations, and
[verification matrix](verification-matrix.md) for planned checks and results.
Maintain reciprocal links from those records to affected specs. Shared rules may
be linked instead of copied; resolving a clarification does not prove implementation.

`Observed` rows describe an inspected path, not every possible caller or database.
`Proposed` rows are desired guarantees requiring owner approval and possibly code
changes. The verification matrix labels partial evidence explicitly.

## Folder organization

Place functional specs in the matching capability folder and add them to its
README.md. Keep shared architecture, cross-cutting rules and tracking registers
at the Specs root. Split documents only for meaningful behavioral boundaries;
link to canonical requirements instead of duplicating them across folders.
When moving a spec, preserve requirement IDs and update incoming and outgoing
links, folder indexes, the spec register and verification mappings.

## Change workflow

1. Find the capability and governing requirements in the register.
2. Inspect current code and evidence; inspect affected service boundaries.
3. Propose a spec delta, including compatibility and open decisions.
4. Collaboratively review behavior changes with the relevant security, integration,
   product, service-compatibility and deployment perspectives; record the decision and its scope.
5. In Phase 2, implement tests and approved fixes in separately reviewable changes.
6. Record test method, command, environment, result and report location before marking Passed.

For AI-assisted work: read this file, the relevant feature specs, cross-cutting
requirements and both constitutions before proposing implementation. Do not infer
approval, invent endpoint mappings, mark tests passed from their names, or fix
unrelated defects. No new CI enforcement is claimed by this document.

## Review responsibilities

Review and verification are collaborative responsibilities. Include behavior,
architecture, product intent, acceptance completeness, security/tenant boundaries,
Service compatibility, integration and deployment implications according to change
risk. Named reviewer assignments are not prerequisites. Record actual review
outcomes and unresolved decisions; shared responsibility does not imply approval.

Phase 1 documentation is prepared when S01-S13 artifacts exist and links/IDs are
consistent. Product approval and execution of Phase 2 remain separate gates; do
not describe the entire feature as verified or production-safe on that basis.

## Existing-product compatibility workflow

For this established product, observed contracts are the compatibility baseline.
Do not implement proposed guarantees merely because they appear in an acceptance
table. The Basis column and gap/decision record distinguish existing behavior from
future changes. Effective deployment configuration still needs explicit confirmation.

Before implementation, locate the public operation in [operation coverage](operation-coverage.md).
Read its owning spec, characterize missing branches, record anomalies separately
from intended rules, and resolve any compatibility-changing decision. A partial
mapping is a task to inspect more code, not permission to invent a contract.
New public methods and overloads must update that inventory and acceptance mapping.
Review spec/code/test changes together; Phase 2 supplies execution evidence.

Source descriptions can guide development immediately. Full SDD readiness additionally
requires approved contracts for the affected workflow, meaningful tests, and review
enforcement. Neither file counts nor this expansion certify the entire product.
