# OSGi Component Constitution

This constitution defines the engineering rules for creating Entgra Device Management Core OSGi components that run inside Carbon-based servers.

It is intentionally platform-specific. These rules optimize for Entgra's Maven multi-module structure, Carbon runtime, OSGi Declarative Services, JNDI datasources, tenant-aware execution, and feature-based deployment.

## Scope

This constitution applies to backend component modules that expose business capability through OSGi services and persist data through DAO layers.

Typical modules covered by this constitution:

- `components/<domain>/io.entgra.device.mgt.core.<domain>.common`
- `components/<domain>/io.entgra.device.mgt.core.<domain>.core`
- `features/<domain>/io.entgra.device.mgt.core.<domain>.server.feature`

This constitution does not define CXF/JAX-RS WAR rules. Those are covered by:

`docs/constitutions/CXF_WEBAPP_CONSTITUTION.md`

## Core Principle

An OSGi component MUST expose stable service contracts, hide implementation and persistence details, initialize cleanly in the Carbon runtime, and keep tenant, datasource, transaction, and configuration behavior explicit.

The component boundary matters. API WARs, other components, and feature packaging should depend on service contracts, not implementation classes.

## Module Structure

A component family SHOULD follow the established structure:

- `common`: shared DTOs, public contracts, reusable exceptions, constants, and model classes.
- `core`: service implementations, DAO layer, config loading, OSGi activation, internal runtime state, tasks, cache, and utilities.
- `api` or `admin.api`: CXF/JAX-RS webapp modules when HTTP exposure is needed.
- `feature`: deployment packaging for bundles, webapps, config files, templates, and DB scripts.

When a domain is small, a `common` module MAY be omitted if the core service contract and DTOs are already naturally owned by `core`. Do not create empty or cosmetic modules.

The root component POM MUST aggregate only modules that belong to that component family.

## Package Boundaries

Core modules SHOULD use package names that make ownership clear:

- `service`: OSGi service interfaces and service implementations.
- `dao`: DAO interfaces, DAO factory, DAO utilities, DAO exceptions.
- `dao.impl`: common and database-specific DAO implementations.
- `internal`: OSGi component, data holder, internal lifecycle-only code.
- `config`: JAXB-bound or equivalent configuration model and config manager.
- `util`: local helpers and constants.
- `dto` or `bean`: persisted or returned data objects.
- `cache`: cache contracts and implementations.
- `task`: scheduled task implementations when needed.
- `listener` or observer-owned packages: lifecycle hooks, startup observers, and plugin registration callbacks when the component supports extensions.

`internal` packages MUST NOT be treated as public API. They should be private in bundle exports.

## Service Contracts

Every component capability intended for external use MUST be exposed through a service interface.

Service interfaces MUST:

- describe business operations, not HTTP or SQL details,
- use domain DTOs and domain exceptions,
- avoid leaking JDBC, servlet, CXF, or implementation-specific types,
- preserve tenant-aware semantics in method naming and documentation where relevant,
- be stable enough for API WARs and other components to consume.

Service implementations MUST:

- implement the public service interface,
- own transaction boundaries,
- call DAOs through DAO interfaces/factories,
- translate DAO exceptions into domain exceptions,
- hide implementation helpers from API modules,
- avoid returning mutable internal state.

API modules and other components MUST use OSGi service contracts. They MUST NOT directly instantiate core implementation classes, DAO classes, generators, managers, or helper classes that bypass the service facade.

Allowed direct construction:

- The OSGi component MAY construct the implementation it registers.
- A service implementation MAY construct private helper classes inside the same module.
- DAO factories MAY construct DAO implementation classes.

Forbidden direct construction:

- API WAR code MUST NOT construct core implementation/helper classes directly.
- Other OSGi components MUST NOT construct another component's implementation classes directly.
- Code outside the DAO layer MUST NOT construct DAO implementations directly.

## OSGi Component Lifecycle

Each core bundle MUST have a clear Declarative Services component under `internal`.

The component SHOULD use:

```java
@Component(
        name = "fully.qualified.ComponentName",
        immediate = true)
public class ExampleServiceComponent {
    @Activate
    protected void activate(ComponentContext componentContext) {
        ...
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        ...
    }
}
```

Activation SHOULD perform only runtime initialization needed before service use:

- load configuration,
- resolve datasource,
- initialize DAO factories,
- initialize caches/repositories/tasks if needed,
- optionally run schema setup when supported by product startup flags,
- register OSGi services,
- register startup observers or lifecycle listeners,
- initialize bounded background workers only after their dependencies and configuration are ready.

When one component registers multiple OSGi services, activation SHOULD keep the order explicit:

- configuration and datasource initialization first,
- DAO factories and cache/repository initialization next,
- service instance creation and data holder wiring next,
- OSGi service registration after the service is safe to consume,
- post-registration startup notifications or background scheduling last.

Activation MUST log failures and continue without crashing the Carbon server. This matches Entgra's Carbon runtime behavior. The log message MUST identify the component and failed initialization step clearly.

Activation MUST NOT silently ignore failures. If a service cannot be safely registered, the activation code SHOULD avoid registering a partially usable service or make the failure obvious through logs.

Default runtime data created during activation, such as default groups, default tenant metadata, or default status filters, MUST be idempotent. Startup may run more than once across nodes or restarts, so duplicate rows and already-existing defaults must be handled deliberately.

Deactivation SHOULD release resources only when the component owns them. Empty deactivation is acceptable when the component only registered OSGi services and borrowed platform-managed resources.

If activation starts executors, schedulers, service trackers, listeners, or other owned runtime resources, deactivation MUST stop or unregister them where the runtime does not manage their lifecycle automatically.

## OSGi References

External component dependencies MUST be declared with `@Reference` when the service is required at activation/runtime.

Reference methods SHOULD:

- use the existing project convention for bind/unbind names, typically `set<ServiceName>` and `unset<ServiceName>`,
- store services in a component data holder only when needed beyond activation,
- log debug-level bind/unbind events when useful,
- set the stored reference to `null` on unbind when the reference is stored as a single service.

The `set<ServiceName>` and `unset<ServiceName>` names are placeholders, not literal method names. They must match the method names declared in `@Reference(unbind = "...")`.

Multiple-cardinality references SHOULD add and remove the specific service instance from a synchronized registry or collection. They MUST notify dependent listeners consistently during both bind and unbind.

Mandatory dependencies SHOULD use:

```java
cardinality = ReferenceCardinality.MANDATORY,
policy = ReferencePolicy.DYNAMIC
```

Optional dependencies MAY be used only when the component has a defined fallback or degraded behavior.

Do not retrieve required services ad hoc from random locations when they can be declared as OSGi references in the component lifecycle.

Runtime OSGi service lookup from `PrivilegedCarbonContext` MAY be used for late-bound or optional services where the component has a clear missing-service failure path. Prefer `@Reference` for required startup/runtime dependencies.

## Data Holders and Singletons

The `DataHolder` pattern is accepted for Carbon/OSGi integration, but it MUST be constrained.

Data holders MAY store:

- OSGi service references,
- initialized managers owned by the component,
- runtime registries,
- listener or plugin registration maps,
- shared platform services needed by implementation code.

Data holders MUST NOT become general global state containers.

Data holder getters for mandatory services SHOULD fail fast with a clear `IllegalStateException` when the service is unavailable. Getters for optional services SHOULD either return `null` with documented fallback behavior or provide a clear missing-service exception.

Mutable data holder registries that are updated from bind/unbind callbacks, tasks, or request threads MUST be synchronized or use concurrent collections.

Singletons MAY be used for:

- configuration managers,
- stateless service facades,
- local utility managers that match existing component style.

Singletons MUST NOT hide tenant-specific mutable state unless that state is explicitly keyed by tenant and lifecycle-managed.

## Plugin Registries and Startup Observers

Components that support device-type plugins or other extension providers MAY maintain listener registries and startup observers.

Rules:

- Register and unregister plugin services through the OSGi lifecycle, not through direct construction by consumers.
- Protect shared listener/provider collections with a lock or concurrent collection.
- When a listener registers after providers are already available, replay existing providers to that listener.
- When a provider unbinds, remove or unregister only that provider and notify interested listeners.
- Startup observers MUST be idempotent and tolerate being invoked after partial startup failures.
- Listener callbacks SHOULD not run while holding locks if they can call back into component code or perform long-running work.

## Configuration

Components that need product configuration MUST define a typed configuration model and a configuration manager.

Configuration managers SHOULD:

- load from Carbon configuration locations,
- parse XML through structured parsing such as JAXB when XML is used,
- expose typed getters,
- initialize once and lazily reload only if the product explicitly supports reload,
- wrap configuration failures in domain exceptions.

Configuration files packaged by features SHOULD also have matching config templates where the product supports templating.

Sensitive values MUST support the product-approved secret-resolution mechanism, such as Secure Vault, environment variables, system properties, deployment secret injection, or another approved equivalent.

New code MUST NOT require plaintext production secrets in committed configuration, including `deployment.toml`, XML files, `.j2` templates, default packaged configs, or generated runtime config.

For TOML-driven configuration, templates MUST allow approved secret placeholders, such as `$secret{alias}`, environment variables, or system properties, to flow through to the generated runtime configuration without being logged or transformed into plaintext in source-controlled files.

Default config files MAY include local placeholder values only when required by existing product conventions. Production-ready templates MUST make secret handling clear.

## Datasources

Components that persist data MUST use Carbon-managed datasources, normally through JNDI.

Datasource configuration SHOULD include:

- datasource name or JNDI lookup definition,
- optional JNDI properties when required,
- no embedded production credentials in component code.

DAO factories MUST resolve the datasource once during component activation or initialization.

DAO factories MUST determine the database engine from datasource metadata and select DAO implementations accordingly.

Unsupported database engines MUST fail through a clear domain/runtime exception.

## DAO Layer

The DAO layer MUST be the only layer that knows SQL details.

DAO interfaces MUST:

- describe persistence operations,
- avoid business orchestration,
- avoid HTTP/API concepts,
- throw DAO-specific exceptions.

DAO implementations MUST:

- use prepared statements for all parameters,
- apply tenant filters where data is tenant-scoped,
- close statements and result sets reliably,
- use the connection supplied by the DAO factory,
- map rows to DTOs/beans consistently,
- avoid starting or committing transactions.

DAO implementations MUST NOT:

- access HTTP request state,
- call API classes,
- perform authorization decisions,
- open independent JDBC connections outside the DAO factory,
- swallow SQL exceptions without wrapping/logging appropriately.

## DAO Factories

Each persistent component SHOULD have one DAO factory per datasource/repository boundary.

DAO factories are responsible for:

- datasource resolution,
- database engine detection,
- DAO implementation selection,
- connection opening,
- transaction begin/commit/rollback,
- connection cleanup.

The factory SHOULD expose methods such as:

- `init(...)`
- `getXDAO()`
- `openConnection()`
- `beginTransaction()`
- `getConnection()`
- `commitTransaction()`
- `rollbackTransaction()`
- `closeConnection()`

DAO factories MAY use `ThreadLocal<Connection>` when following existing Carbon component style. If `ThreadLocal` is used, every service method MUST close/commit/rollback the connection before returning.

Nested transaction/open-connection calls in the same thread MUST fail fast with a clear illegal transaction state exception.

## Transaction Rules

Service methods own transaction boundaries.

Read-only operations MUST follow this pattern:

```java
try {
    ExampleDAOFactory.openConnection();
    ExampleDAO dao = ExampleDAOFactory.getExampleDAO();
    return dao.read(...);
} catch (SQLException e) {
    throw new ExampleManagementException("Error occurred while opening a connection ...", e);
} catch (ExampleDAOException e) {
    throw new ExampleManagementException("Error occurred while reading ...", e);
} finally {
    ExampleDAOFactory.closeConnection();
}
```

Write operations MUST follow this pattern:

```java
try {
    ExampleDAOFactory.beginTransaction();
    ExampleDAO dao = ExampleDAOFactory.getExampleDAO();
    dao.write(...);
    ExampleDAOFactory.commitTransaction();
} catch (TransactionManagementException e) {
    throw new ExampleManagementException("Error occurred while starting transaction ...", e);
} catch (ExampleDAOException e) {
    ExampleDAOFactory.rollbackTransaction();
    throw new ExampleManagementException("Error occurred while writing ...", e);
}
```

When multiple DAO calls are part of one business operation, they MUST share the same transaction opened by the service method.

DAO methods MUST NOT commit or roll back transactions themselves.

When a business operation spans multiple DAO factories, the service MUST define the transaction order and failure behavior explicitly. Commit, rollback, and close calls MUST be paired for each factory so one repository cannot leave an open `ThreadLocal` connection after another repository fails.

## Tenant Context

Tenant-scoped data access MUST use Carbon tenant context deliberately.

Rules:

- DAO methods MUST include tenant predicates for tenant-owned data.
- Services MUST document when a method uses current tenant context.
- Cross-tenant methods MUST require explicit tenant identity and explain why.
- Tenant flows MUST always end in `finally`.
- Super-tenant flows MUST be limited to the smallest possible block.

Never trust tenant IDs supplied by API payloads when the Carbon context already determines the authenticated tenant.

Background tasks that process data for multiple tenants MUST establish tenant context explicitly for each tenant or operation and MUST end each tenant flow in `finally`.

## Exception Model

Each component SHOULD define domain exception types.

Recommended layers:

- DAO exceptions for persistence failures.
- Transaction exceptions for transaction setup/control failures.
- Management/service exceptions for business/service failures.
- Specialized exceptions for component-specific subsystems such as keystore, SCEP, task, or external service failures.

Exception rules:

- DAO implementations throw DAO exceptions, not raw `SQLException`.
- Service implementations wrap DAO exceptions in domain service exceptions.
- Helpers wrap low-level checked exceptions in subsystem/domain exceptions.
- Exceptions crossing the OSGi service boundary SHOULD be domain exceptions.
- Log messages SHOULD include the operation and safe identifiers such as ID, type, tenant, or serial number.
- Logs MUST NOT include secrets, private keys, tokens, passwords, or full sensitive payloads.

## Schema and Migration Rules

Any DB schema change MUST include migration/script changes for all supported databases in the same PR.

Supported database scripts commonly include:

- H2
- MySQL
- PostgreSQL
- SQL Server
- Oracle

A schema-changing PR MUST update:

- feature DB scripts,
- migration scripts if the product has separate upgrade migrations,
- component-local SQL resources where they exist,
- DAO SQL and DTO mapping,
- configuration or feature metadata if schema setup behavior changes.

DAO code MUST NOT reference a column that is absent from supported DB scripts.

If a feature supports `-Dsetup` schema creation, setup SQL and upgrade/migration SQL MUST remain compatible.

## Caches

Cache use MUST be a performance optimization, not the source of truth.

Cache rules:

- Cache keys MUST be stable and namespaced.
- Cache entries MUST be tenant-safe.
- Cache configuration MUST be initialized before use.
- Disabled cache behavior MUST still be correct.
- Repository writes that make cached data stale SHOULD evict or update affected cache entries.

Do not introduce hidden cache dependencies that make service behavior fail when cache is disabled.

## Tasks and Background Execution

Components MAY schedule Carbon tasks or local executors for background work.

Rules:

- Task and executor startup MUST be controlled by typed configuration with safe defaults.
- Invalid task configuration MUST be validated at startup and either fail clearly or fall back to documented defaults.
- Cluster-aware or partitioned tasks MUST consult the approved coordination service before processing shared work.
- Tasks that use OSGi services MUST handle temporarily missing optional services clearly.
- Background work MUST set tenant context deliberately and end tenant flows in `finally`.
- Executors owned by the component MUST be bounded and shut down during deactivation unless the platform owns them.
- Task failures MUST be logged with enough context to diagnose the task name, tenant, device type, or operation, without logging secrets.

## Feature Packaging

Every server-side core component intended for product deployment MUST have a matching feature module.

Feature modules SHOULD package:

- OSGi bundles,
- required third-party bundles,
- configuration files,
- config templates,
- DB scripts,
- email templates, registry artifacts, API metadata, or other runtime resources owned by the component,
- p2 metadata.

Feature POMs MUST keep bundle versions aligned with root dependency management.

Do not add runtime config or DB scripts only under `components`; product deployment consumes `features`.

Feature `p2.inf` copy/install instructions MUST be updated when adding, renaming, or relocating deployable resources.

## Logging

Components MUST use the logging framework already used by the module.

Logging rules:

- Use debug logs for lifecycle bind/unbind and low-level diagnostic details.
- Use info logs for successful major startup/setup events.
- Use warn logs for recoverable cleanup or rollback failures.
- Use error logs for failed operations that affect a request, task, or component startup.
- Log with the exception object when stack trace is useful.
- Do not log secrets or full certificate/private key/token payloads.

Startup failure logs MUST include the component class/name and the failed stage.

## Security and Secrets

Components MUST treat secrets as configuration, not code.

Rules:

- Do not hard-code production credentials.
- Support the product-approved secret-resolution mechanism for sensitive config values.
- Do not log resolved secret values.
- Do not expose secrets through service DTOs.
- Keep private key operations inside core service/helper boundaries.
- Do not pass raw private key material into API modules.

## Service Consumer Rules

Any consumer of another component MUST depend on the other component's public service contract.

Consumers MUST:

- retrieve services through OSGi reference or OSGi service lookup,
- handle missing services clearly,
- avoid implementation package imports,
- avoid direct DAO access across component boundaries.

If a needed operation is missing from another component's service interface, add it to that service contract rather than reaching into its implementation.

## Legacy Compatibility

Existing code may contain patterns that this constitution does not allow for new code.

Do not copy legacy shortcuts into new code:

- direct construction of another component's helper/implementation classes,
- stringly typed unvalidated config parsing when typed config is available,
- API-layer DAO calls,
- schema changes in only one database script,
- inconsistent exception response behavior,
- broad catch blocks with weak log messages.

When touching legacy code, prefer incremental alignment with this constitution without creating unnecessary churn.

## Component PR Checklist

Before a component change is merged, verify:

- Public behavior is exposed through service contracts.
- OSGi activation initializes config, datasource, DAO factories, and services in a clear order.
- Activation logs failures and continues.
- Required OSGi dependencies are declared with `@Reference`.
- DAO SQL is isolated to DAO implementations.
- Service methods own transactions and close connections.
- Tenant-scoped data uses tenant predicates.
- Domain exceptions wrap lower-level exceptions.
- Schema changes include all supported DB scripts and migrations.
- Sensitive values support the product-approved secret-resolution mechanism.
- Feature packaging includes required bundles, config, templates, and DB scripts.
- Owned tasks, executors, listeners, and startup observers have clear lifecycle handling.
- Default startup data is idempotent.
- Consumers use services, not implementation classes.
