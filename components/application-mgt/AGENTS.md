# Application Management Agent Guidance

Applies to this directory and its descendants, in addition to the repository
[AGENTS.md](../../AGENTS.md). This scope does not include sibling repositories or
`features/application-mgt`; coordinate those changes explicitly when needed.

## Start with the specification baseline

Read [the index](specs/README.md), [workflow](specs/contributing.md),
[capability register](specs/capability-register.md) and relevant feature specs.
Use [the source map](specs/source-map.md) to locate implementations and consumers;
verify references against the current checkout rather than assuming the recorded
baseline is current.

- [Architecture](specs/architecture.md) and
  [cross-cutting requirements](specs/cross-cutting-requirements.md) define shared concerns.
- [Gap register](specs/gap-register.md) records unresolved behavior and risks.
- [Verification matrix](specs/verification-matrix.md) separates existing evidence
  from planned acceptance tests.
- [Phase 2 backlog](specs/phase-2-backlog.md) proposes tests and fixes; it is not
  blanket authorization to implement them.
- [Content readiness](specs/product-content-readiness.md) records missing product,
  documentation, marketing and automation inputs.

Phase 1 was documentation-only. Preserve that restriction for Phase 1 work, not
as a permanent ban on explicitly authorized future implementation or tests.
Draft specs are not approved product requirements. Keep requirement definitions
canonical, retain APP identifiers and update related evidence/gaps when behavior
changes. Do not close gaps merely because documentation has been written.

## Component-specific checks

- Follow tenant and user context through APIs, services, DAOs and asynchronous work.
- Identify connection/transaction ownership, including acquisition failures,
  nested callers, commit failures and cleanup. See APP-GAP-01 before copying
  transaction examples from shared constitutions.
- Check release hash/cardinality and installable-state rules across concurrent
  transactions; do not claim cross-node guarantees from prechecks alone.
- Distinguish initial remote creation acceptance from completed release creation,
  and subscription dispatch from confirmed device installation.
- Treat filesystem/artifact operations, JDBC updates and external services as
  separate failure boundaries unless atomicity is actually demonstrated.
- Validate lifecycle rules against applicable configuration and application type.
  Trace API/UI/device-agent behavior in the owning repository before asserting
  end-to-end contracts; reading those sources does not authorize editing them.

## Verification and handover

With Java 11 selected as described in the root guidance, a focused reactor test
command from the repository root is:

```sh
mvn -pl components/application-mgt/io.entgra.device.mgt.core.application.mgt.core -am test
```

Inspect [the configured TestNG suite](io.entgra.device.mgt.core.application.mgt.core/src/test/resources/testng.xml)
and actual assertions. Disabled or empty tests are not acceptance evidence.
Choose additional API, storage, integration or multi-JVM checks according to the
affected requirements; the focused command alone does not verify those boundaries.

In the handover, list affected requirements, spec/code changes, executed checks
and pending decisions. Keep review approval, implementation, runtime verification
and released support separate. For user guides or marketing drafts, use the
content-readiness records and explicitly identify missing release, journey or
claim evidence instead of filling gaps with assumptions.
