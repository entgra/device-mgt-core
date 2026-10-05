# CXF Webapp Constitution

This constitution defines the engineering rules for creating Entgra Device Management Core CXF/JAX-RS webapps deployed on Carbon-based servers.

It is intentionally platform-specific. These rules optimize for Entgra's WAR deployment model, CXF descriptors, Swagger annotations, managed API publishing, OSGi-backed service access, and WAR-name-based API versioning.

## Scope

This constitution applies to HTTP API modules packaged as WARs, including user APIs, admin APIs, config APIs, and other CXF/JAX-RS webapps.

Typical modules covered by this constitution:

- `components/<domain>/io.entgra.device.mgt.core.<domain>.api`
- `components/<domain>/io.entgra.device.mgt.core.<domain>.admin.api`
- `features/<domain>/io.entgra.device.mgt.core.<domain>.api.feature`

This constitution does not define OSGi service/DAO construction rules inside core bundles. Those are covered by:

`docs/constitutions/OSGI_COMPONENT_CONSTITUTION.md`

## Core Principle

A CXF webapp MUST be a thin HTTP adapter over OSGi services. It MUST define clear API contracts, validate requests, call service interfaces only, and translate service results/exceptions into stable HTTP responses.

API modules MUST NOT contain business persistence logic.

## WAR Naming and Versioning

API versioning is WAR-name based.

WAR names MUST follow the Carbon deployment convention:

```xml
<warName>api#<api-name>#v<major.minor></warName>
```

Examples:

- `api#certificate-mgt#v1.0`
- `api#scep-mgt#v1.0`

The WAR name is the authoritative API version boundary. Breaking API changes MUST use a new WAR version.

When changing API version:

- update `maven-war-plugin` `warName`,
- update feature packaging references,
- update Swagger context/base path metadata,
- keep old WARs available when backward compatibility is required.

Do not rely only on class-level `@Path` changes for versioning.

## Module Structure

API modules SHOULD use this package structure:

- `api` or `service`: annotated JAX-RS service interfaces.
- `api.impl` or `service.impl`: JAX-RS implementations.
- `beans`: request, response, pagination, and error DTOs.
- `common`: message body providers, exception mappers, common API exceptions.
- `exception`: HTTP-facing exception types.
- `util`: request validation, OSGi service lookup helpers, response helpers.
- `extension`: Swagger extensions or API documentation hooks.
- `src/main/webapp/WEB-INF`: `web.xml` and `cxf-servlet.xml`.
- `src/main/webapp/META-INF`: `webapp-classloading.xml`.

Keep names consistent with the surrounding component. Do not create package layers that only rename existing concepts.

## API Contract Pattern

Every endpoint SHOULD be declared in an annotated interface.

The interface owns:

- class-level `@Path`,
- HTTP method annotations,
- `@Consumes` and `@Produces`,
- `@PathParam`, `@QueryParam`, `@HeaderParam`, and body parameter declarations,
- Swagger annotations,
- scope metadata,
- documented response codes.

The implementation owns:

- request validation orchestration,
- OSGi service lookup,
- service calls,
- HTTP response mapping,
- exception-to-response mapping.

The implementation MUST implement the interface and keep method signatures aligned.

Do not put business rules only in Swagger notes. The implementation/service layer must enforce required behavior.

## Service-Only Core Access

API modules MUST use OSGi services only.

API modules MUST NOT directly construct:

- core service implementation classes,
- core helper/manager/generator classes,
- DAO factories,
- DAO implementations,
- internal data holders from other components.

Allowed API dependencies:

- public service interfaces,
- public DTOs/contracts,
- API-local utilities,
- platform service interfaces.

Required lookup pattern:

- use an API utility class such as `DeviceMgtAPIUtils` or `CertificateMgtAPIUtils`, or
- retrieve services through `PrivilegedCarbonContext#getOSGiService`.

If an OSGi service is missing:

- log an error identifying the missing service,
- fail the request with a server-side error response,
- do not fall back to constructing implementation classes.

If a needed core operation does not exist, add it to the core service contract.

## CXF Descriptor Rules

Each WAR MUST define `WEB-INF/cxf-servlet.xml`.

The descriptor SHOULD define:

- one or more `jaxrs:server` entries,
- service beans,
- providers,
- Swagger resources when the API is published/documented.

Common providers:

- JSON message body handler,
- error handler or exception mapper,
- Swagger serializers.

Service bean class names MUST point to implementation classes, not interfaces.

When multiple `jaxrs:server` entries exist, their addresses and resource `@Path` values MUST be reviewed together to avoid accidental path shifts.

Do not add a new CXF server when a service bean under the existing server is enough.

## Web Descriptor Rules

Each WAR MUST define `WEB-INF/web.xml`.

The descriptor SHOULD include:

- `CXFServlet`,
- servlet mapping to `/*`,
- session timeout,
- authentication-related context params,
- managed API publishing params where the API must be published,
- required security filters,
- transport requirements where needed by product security.

Managed API publishing settings SHOULD be explicit:

- `managed-api-enabled`
- `managed-api-owner`
- `isSharedWithAllTenants`

Authentication settings SHOULD be explicit:

- `doAuthentication`
- `basicAuth` when required by the API design.

Do not rely on implicit container defaults for security-sensitive behavior.

## Classloading Rules

Each WAR SHOULD include `META-INF/webapp-classloading.xml` when it depends on Carbon/CXF runtime behavior.

Current standard:

- `ParentFirst=false`
- environments include `CXF3,Carbon`

Do not package Carbon/CXF runtime libraries into the WAR when they are expected from the server runtime. Use `provided` scope and WAR packaging excludes as established in the project.

## POM Rules

API POMs MUST package as `war`.

API POMs SHOULD:

- configure `maven-war-plugin` with WAR name,
- exclude server-provided CXF libraries from `WEB-INF/lib`,
- declare core/service dependencies with `provided` scope when provided by OSGi runtime,
- depend on Swagger annotations/core/JAX-RS as needed,
- avoid adding implementation-only dependencies that belong in core.

Do not add DAO or database driver dependencies to API WARs unless the API module itself is explicitly a low-level integration endpoint, which should be rare and reviewed.

## Swagger and Scope Rules

Public APIs MUST include Swagger annotations matching the established project style.

Service interfaces SHOULD define:

- `@SwaggerDefinition`,
- `@Api`,
- operation-level `@ApiOperation`,
- operation-level `@ApiResponses`,
- API tags,
- API context extension property,
- scope extension property.

Every protected operation MUST have a scope key.

Scope declarations MUST include:

- name,
- description,
- key,
- roles,
- permissions.

Operation-level Swagger metadata MUST reference the same scope key as the class-level `@Scopes` declaration.

Permission paths SHOULD follow the existing product permission tree for the component.

## Request Validation

API modules MUST validate request shape before calling core services.

Validation SHOULD cover:

- required path parameters,
- required query parameters,
- pagination ranges,
- allowed enum/type values,
- required request body fields,
- malformed IDs or serials,
- unsupported media or payload shape where applicable.

Validation SHOULD live in an API-local utility when reused by multiple endpoints.

Validation failures MUST return JSON error responses for new endpoints.

Do not depend on core exceptions as the first line of request validation when the API can cheaply reject bad input.

## Response Standards

New APIs SHOULD return typed JSON responses for normal object/list results.

Plain string responses SHOULD be treated as legacy compatibility behavior and MUST NOT be copied into new endpoints unless the endpoint contract specifically requires `text/plain`, such as certificate/CSR material exchange.

Pagination responses SHOULD include:

- total count,
- data list under a stable JSON property,
- next/previous fields only when actually populated or documented as optional.

Response DTOs MUST be stable and documented through Swagger annotations where practical.

Do not return internal persistence beans directly when a response DTO is needed.

## JSON Error Standard

New API errors SHOULD use JSON error responses.

A standard error response SHOULD include:

- `code`
- `message`
- `description` when useful
- `moreInfo` when useful
- `errorItems` for field-level validation failures

Rules:

- `400` for request validation and malformed client input.
- `401` or `403` for authorization/authentication failures according to platform behavior.
- `404` for missing resources.
- `409` for conflicts such as duplicate resources where appropriate.
- `500` for unexpected server, service, keystore, persistence, or integration failures.

Exception mappers and `WebApplicationException` subclasses SHOULD preserve JSON response shape.

Do not expose stack traces, secrets, raw SQL, private keys, tokens, or sensitive request bodies in API responses.

## Service Exception Mapping

API implementations SHOULD catch domain exceptions from services and map them to HTTP responses.

Typical mapping:

- validation exception from API layer: `400`
- not-found domain exception: `404`
- conflict domain exception: `409`
- authorization domain exception: `401` or `403`
- component/service failure: `500`

When the core service exposes only broad exceptions, the API MAY inspect the operation result to decide between `404`, `409`, and `500`, but should not parse fragile exception message text unless no better signal exists.

Log the internal exception with enough context to debug, then return a client-safe JSON error.

## Tenant Context

API modules MUST respect Carbon tenant context.

Rules:

- Trust authenticated Carbon context over tenant IDs supplied in request bodies.
- Do not switch tenant flow unless the operation explicitly requires cross-tenant work.
- Any tenant flow switch MUST use `try/finally`.
- Claims or tenant-specific responses MUST be derived from validated service results.
- Do not let API clients arbitrarily select tenant context through payload fields.

## Security Rules

API modules MUST make authentication and managed API behavior explicit in descriptors and annotations.

Rules:

- Protected endpoints MUST declare scopes.
- Scope keys MUST match operation-level Swagger metadata.
- Permission paths MUST be reviewed as part of API changes.
- Admin APIs MUST use admin permission paths.
- Sensitive operations MUST not be exposed without authentication.
- Token, JWT, certificate, and secret values MUST not be logged.

Secrets used by APIs or descriptors MUST support the product-approved secret-resolution mechanism where applicable, such as Secure Vault, `$secret{alias}` references in TOML-driven configuration, environment variables, system properties, or deployment secret injection.

## Feature Packaging

Every deployable API WAR MUST have a matching API feature module.

The feature module SHOULD package:

- the WAR under the expected webapp deployment path,
- p2 metadata,
- dependencies or imported features needed by the WAR.

When the API version changes, feature packaging MUST be updated with the new WAR artifact name.

Do not assume a WAR is deployable just because the component module builds. Product deployment depends on feature packaging.

## Backward Compatibility

API changes MUST preserve existing clients unless the WAR version changes.

Backward-compatible changes MAY include:

- adding optional fields,
- adding optional query parameters,
- adding new endpoints,
- adding more specific error descriptions without changing status code semantics.

Breaking changes include:

- changing WAR version path,
- changing endpoint paths,
- changing required request fields,
- changing response JSON property names,
- changing success status codes,
- changing media types,
- removing scopes or changing permission behavior in a way that breaks clients.

Breaking changes MUST use WAR-name-based versioning.

## Legacy Compatibility

Existing APIs may contain legacy patterns. Do not copy them into new endpoints.

Legacy patterns to avoid:

- direct construction of core implementation/helper classes,
- plain string errors for JSON endpoints,
- unused documented headers,
- documented resource shape that does not match runtime behavior,
- duplicate imports or unused service lookups,
- response DTOs whose `toString()` returns `null`,
- API methods that perform DAO or transaction work directly.

When updating legacy endpoints, improve consistency where possible without breaking existing clients.

## API PR Checklist

Before an API change is merged, verify:

- WAR name and version are correct.
- API interface and implementation signatures match.
- CXF service bean wiring points to implementation classes.
- `web.xml` security and managed API settings are explicit.
- Core access uses OSGi service contracts only.
- No DAO or core implementation classes are constructed in API code.
- Swagger context, tags, scopes, and response codes are updated.
- Scope keys match operation metadata.
- Requests are validated before core service calls.
- New errors use JSON error responses.
- Sensitive data is not logged or returned.
- Feature packaging includes the WAR with the correct versioned name.
