# Repository Agent Guidance

## Scope and discovery

This Maven repository contains Entgra device-management core components and OSGi
feature packaging. Follow this guidance throughout the repository, plus any
applicable component-level AGENTS.md. Explicit task scope still governs the work;
a documentation-only task does not authorize implementation changes.

Assess changes within this repository's core/base responsibilities. Trace affected
downstream API, UI, plugin, device-agent and product-packaging contracts when needed;
do not infer complete product behavior or supported deployments from core code,
packaged defaults or database scripts alone. Use POMs and feature resources for
artifact, dependency and packaging facts rather than duplicating version inventories.

Before editing, inspect Git status, the relevant implementation, module POMs and
tests. Preserve unrelated staged, unstaged and untracked work. Discover component
specifications under `components/<component>/Specs/`; coverage varies, so do not
assume every component has an approved specification.

- [Application Management specs](components/application-mgt/specs/README.md)
- [Realtime Notification Management specs](components/realtime-notification-mgt/Specs/README.md)
- [OSGi component constitution](docs/constitutions/OSGI_COMPONENT_CONSTITUTION.md)
- [CXF webapp constitution](docs/constitutions/CXF_WEBAPP_CONSTITUTION.md)

Read applicable constitutions and component qualifications. If examples, specs
and implementation disagree, report the discrepancy rather than silently choosing
new product behavior. Existing code is evidence, not proof of intended behavior.

## Spec-driven changes and review

1. Identify affected capabilities, requirements, callers and compatibility boundaries.
2. Separate approved intent, source-observed behavior, proposals and unknowns.
   Record missing requirements or decisions; do not invent approval.
3. For behavioral changes, update relevant specs and acceptance criteria together
   with authorized implementation work. Preserve stable requirement IDs.
4. For bugs, distinguish reproduction from hypothesis; add focused regression
   coverage when implementation/testing is in scope. Otherwise record the test gap.
5. Review tenant isolation, authorization, connection ownership, failure cleanup,
   concurrency, migrations and public API compatibility as applicable. Java locks
   and thread-local state do not establish cross-JVM coordination; a transaction
   alone does not make an ordinary read a locking read.
6. Report requirement-to-change mapping, checks performed, unresolved decisions
   and remaining risks. Never mark acceptance passed from a test name or file count.

Keep edits scoped. Follow local patterns and license headers for new source files.
Do not change sibling repositories, introduce dependencies, alter release versions
or fix unrelated defects without task authorization. Do not stage, commit, push,
merge or publish unless requested. Never commit secrets or unsanitized customer data.

## Build and verification

Use JDK 11 for builds and tests. Set `JAVA_HOME` to an installed JDK 11 and put its
`bin` directory first on `PATH`; confirm `java -version` and `mvn -version` both
report Java 11. Do not assume the terminal's default JDK is correct.

Run from the repository root, selecting scope according to change risk:

```sh
# Selected module and its reactor dependencies; replace the placeholder.
mvn -pl <module-path> -am test
# Full reactor tests.
mvn test
# Broader clean build and verification when warranted.
mvn clean verify
```

Inspect the module's configured test suite before selecting individual tests.
Do not bypass failing tests or change build settings merely to obtain a green
result. Report dependency/environment failures separately from assertion failures.
Record the command, JDK, source revision and working-tree changes, test counts,
result and any skipped checks. Passing unit tests is not proof of multi-node or
deployed-product behavior. For documentation-only edits, validate links and diff
whitespace; state explicitly when builds/tests were not run.

## Documentation and marketing outputs

Use release-specific, traceable evidence and approved claims. Distinguish code
availability from packaged, enabled and supported product functionality. Do not
infer performance, security, scale or competitive claims from implementation.
Keep unverified content labeled as internal draft, preserve known limitations,
and require the appropriate human approval before publication. Changes to source
requirements or evidence require dependent content to be re-reviewed.

These instructions guide agents; they do not replace CI, tests or human review.
