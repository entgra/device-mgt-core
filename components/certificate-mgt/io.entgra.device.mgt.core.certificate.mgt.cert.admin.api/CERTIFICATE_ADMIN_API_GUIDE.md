# Certificate Management Admin API Guide

This file describes the certificate management admin API module:

`components/certificate-mgt/io.entgra.device.mgt.core.certificate.mgt.cert.admin.api`

This module is a CXF/JAX-RS WAR that exposes administrative certificate operations. It lets administrators add certificates, search/list certificate records, remove certificates when metadata allows deletion, and verify device certificates for Android and iOS flows.

## Module Role

The admin certificate API is responsible for:

- Exposing admin HTTP endpoints under the certificate management API.
- Declaring Swagger documentation for certificate management operations.
- Declaring Entgra API management scopes and permission paths.
- Translating REST requests into certificate core service calls.
- Looking up required OSGi services from `PrivilegedCarbonContext`.
- Validating basic request parameters.
- Formatting certificate list and validation responses.
- Translating core, SCEP, keystore, and JWT failures into HTTP responses.
- Applying webapp authentication, managed API publishing, cache prevention, CORS, and transport security settings.

The module does not own DAO logic, certificate repository transactions, CA/RA keystore access, or certificate signing. Those belong to certificate core.

## Build and Deployment

The module POM packages this module as a WAR.

Important build settings:

- Packaging type: `war`
- WAR name: `api#certificate-mgt#v1.0`
- CXF libraries are excluded from the WAR with `WEB-INF/lib/*cxf*.jar`
- The module depends on certificate core, device management core/common, JWT client extension, and API management annotations with `provided` scope where appropriate.
- Swagger, CXF JAX-RS, Spring web/context, JAXB, commons-codec, and JSR-311 are used.

The WAR name maps to Carbon webapp deployment conventions. At runtime, `api#certificate-mgt#v1.0.war` is exposed as an API webapp path equivalent to `/api/certificate-mgt/v1.0`.

The matching feature module is:

`features/certificate-mgt/io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.feature`

That feature packages this WAR for product installation.

## Important Packages

- `api`: JAX-RS service contract, validation/server exceptions.
- `api.impl`: Runtime JAX-RS implementation.
- `api.beans`: Request/response/error DTOs.
- `api.common`: Gson provider, `MDMAPIException`, and error mapper.
- `api.exception`: Additional web exceptions and simple message payload.
- `api.extension`: Swagger security definition customization.
- `api.util`: OSGi service lookup utilities, request validation, CORS filter, response payload helper.
- `src/main/webapp/WEB-INF`: CXF and webapp descriptors.
- `src/main/webapp/META-INF`: Carbon classloading descriptor.

## HTTP Contract

Service contract:

`CertificateManagementAdminService`

Implementation:

`CertificateManagementAdminServiceImpl`

Class-level path:

`/admin/certificates`

Swagger metadata declares:

- API display name: `Certificate Management`
- Context: `/api/certificate-mgt/v1.0/admin/certificates`
- Base path from security configurator: `/api/certificate-mgt/v1.0`

### Add Certificates

Operation:

`POST /admin/certificates`

Consumes and produces:

`application/json`

Scope:

`cm:cert:add`

Permission:

`/device-mgt/admin/certificates/add`

Input:

- JSON array of `EnrollmentCertificate`.
- Each item supports `serial`, `pem`, and `tenantId`.
- Runtime implementation ignores input `tenantId` and uses the current Carbon tenant ID.

Behavior:

1. Resolve `CertificateManagementService`.
2. Convert each Base64 PEM string to `X509Certificate`.
3. Check whether the serial already exists.
4. Build core certificate beans with current tenant ID.
5. Save the certificate list through certificate core.

Responses:

- `201 Created` with `Added successfully.`
- `400 Bad Request` if a certificate with the same serial already exists.
- `500 Internal Server Error` if PEM conversion or keystore handling fails.

### Get/Search Certificate By Serial

Operation:

`GET /admin/certificates/{serialNumber}`

Scope:

`cm:cert:details:get`

Permission:

`/device-mgt/admin/certificates/details`

Behavior:

1. Validate that `serialNumber` is not null or empty.
2. Resolve `CertificateManagementService`.
3. Calls `searchCertificates(serialNumber)`.
4. Returns a list of matching `CertificateResponse` records.

Responses:

- `200 OK` with a list of certificate responses.
- `400 Bad Request` for missing serial number.
- `500 Internal Server Error` on certificate management failure.

Note: Despite the contract name `getCertificate`, implementation performs a serial search instead of exact `retrieveCertificate`.

### List Certificates

Operation:

`GET /admin/certificates`

Scope:

`cm:cert:view`

Permission:

`/device-mgt/admin/certificates/view`

Query parameters:

- `serialNumber`
- `deviceIdentifier`
- `username`
- `offset`
- `limit`

Behavior:

1. Validate pagination values are not negative.
2. Build `CertificatePaginationRequest`.
3. Add optional filters when non-empty.
4. Call `CertificateManagementService#getAllCertificates`.
5. Wrap results in `CertificateList`.

Response:

- `200 OK` with `CertificateList`.
- `400 Bad Request` for invalid pagination.
- `500 Internal Server Error` on core failure.

`CertificateList` extends `BasePaginatedResult` and serializes certificate rows under JSON property `certificates`. It sets `count` from `PaginationResult#getRecordsTotal`.

### Delete Certificate

Operation:

`DELETE /admin/certificates?serialNumber=...`

Scope:

`cm:cert:delete`

Permission:

`/device-mgt/admin/certificates/delete`

Behavior:

1. Validate `serialNumber`.
2. Resolve `CertificateManagementService`.
3. Call `getValidateMetaValue()`.
4. If metadata decision is false, return `401 Unauthorized`.
5. If allowed, call `removeCertificate(serialNumber)`.
6. Return `404` if no certificate was removed.
7. Return `200` if deletion succeeded.

Deletion authorization is controlled by certificate core metadata lookup for:

- metadata key: `CERTIFICATE_DELETE`
- value property: `IS_CERTIFICATE_DELETE_ENABLE`

This is in addition to the API scope/permission.

### Verify Certificate

Operation:

`POST /admin/certificates/verify/{type}`

Scope:

`cm:cert:verify`

Permission:

`/device-mgt/admin/certificates/verify`

Path parameter:

- `type`: documented as `android`, `ios`, or `windows`.

Input:

- JSON `EnrollmentCertificate` with `serial` and `pem`.

Android behavior:

1. Resolve `CertificateManagementService`.
2. If `serial` contains `proxy-mutual-auth-header`, call `verifySubjectDN(pem)`.
3. Otherwise convert `pem` to `X509Certificate`.
4. Call `verifyPEMSignature`.
5. If the returned `CertificateResponse` has a non-empty common name, return `valid`.
6. Otherwise return `invalid`.

iOS behavior:

1. Extract certificate from the submitted signature/PEM field.
2. Extract challenge token from certificate extension.
3. Normalize challenge token using a regex and substring logic.
4. Resolve `SCEPManager`.
5. Build a `DeviceIdentifier` with type `ios`.
6. Validate device and resolve tenant using `SCEPManager#getValidatedDevice`.
7. Build JWT claims for tenant ID, end user, device identifier, and device type.
8. Start tenant flow for the validated tenant.
9. Resolve `JWTClientManagerService`.
10. Issue a JWT token.
11. Return `ValidationResponse` with token, device ID/type, and tenant ID.

Responses:

- `200 OK` with `ValidationResponse` for valid iOS certificates.
- `200 OK` with string `valid` for valid Android certificates.
- `200 OK` with string `invalid` for unsupported/invalid cases.
- `500 Internal Server Error` for SCEP, keystore, or JWT errors.

## Scopes and Permissions

Declared scopes:

- `cm:cert:add`: add certificates, role `Internal/devicemgt-admin`, permission `/device-mgt/admin/certificates/add`
- `cm:cert:details:get`: get certificate details, role `Internal/devicemgt-admin`, permission `/device-mgt/admin/certificates/details`
- `cm:cert:view`: list/search certificates, role `Internal/devicemgt-admin`, permission `/device-mgt/admin/certificates/view`
- `cm:cert:delete`: delete certificates, role `Internal/devicemgt-admin`, permission `/device-mgt/admin/certificates/delete`
- `cm:cert:verify`: verify certificates, role `Internal/devicemgt-admin`, permission `/device-mgt/admin/certificates/verify`

When adding endpoints:

- Add the scope in the class-level `@Scopes`.
- Add the scope key to the operation-level Swagger extension.
- Use the existing `/device-mgt/admin/certificates/...` permission family unless product authorization requirements change.
- Keep roles explicit.

## CXF and Webapp Wiring

`WEB-INF/cxf-servlet.xml` defines one CXF server:

- server ID: `services`
- address: `/`
- service bean: `certificateServiceBean`
- Swagger resource: `swaggerResource`

`certificateServiceBean` is:

`io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.impl.CertificateManagementAdminServiceImpl`

Providers:

- `GsonMessageBodyHandler`
- `ErrorHandler`
- `SwaggerSerializers`

Swagger resource:

- `io.swagger.jaxrs.listing.ApiListingResource`

`WEB-INF/web.xml` defines:

- `CXFServlet` mapped to `/*`
- Swagger security filter init param: `ApiAuthorizationFilterImpl`
- `doAuthentication=true`
- `managed-api-enabled=true`
- `managed-api-owner=admin`
- `isSharedWithAllTenants=true`
- `basicAuth=true`
- confidential transport guarantee for `/*`
- `ApiOriginFilter`
- `HttpHeaderSecurityFilter`
- `ContentTypeBasedCachePreventionFilter`

`META-INF/webapp-classloading.xml` configures:

- `ParentFirst=false`
- Environments: `CXF3,Carbon`

## Service Lookup Requirements

`CertificateMgtAPIUtils` resolves OSGi services through:

`PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(...)`

Services used:

- `CertificateManagementService`
- `SCEPManager`
- `JWTClientManagerService`
- `SearchManagerService`

If a service is missing, `CertificateMgtAPIUtils` logs an error and throws `IllegalStateException`.

Implementation rules:

- Do not instantiate certificate core implementation classes directly from the admin API.
- Use `CertificateMgtAPIUtils` for OSGi service access.
- Keep transaction and DAO handling in certificate core.
- Keep tenant-flow switching limited and always end tenant flows in `finally`.

## Request DTOs and Response DTOs

`EnrollmentCertificate`:

- `serial`: certificate serial or special proxy-auth marker for Android subject-DN flow.
- `pem`: Base64 PEM certificate, CMS signature, or subject DN depending on endpoint/type.
- `tenantId`: present on the bean but not trusted by `addCertificate`; the current Carbon tenant ID is used.

`CertificateList`:

- Extends `BasePaginatedResult`.
- JSON property `certificates` contains `CertificateResponse` entries.
- `count` is set from total records.
- `next` and `previous` exist on the base type but are not currently populated.

`ValidationResponse`:

- `JWTToken`
- `deviceId`
- `deviceType`
- `tenantId`

`ErrorResponse`:

- `code`
- `message`
- `description`
- `moreInfo`
- `errorItems`

## Validation Rules

`RequestValidationUtil` currently validates:

- serial number must not be null or empty,
- `offset` must not be negative,
- `limit` must not be negative.

Invalid input throws `InputValidationException`, which is a `WebApplicationException` returning HTTP `400` with `ErrorResponse`.

The API does not currently validate:

- maximum serial length,
- PEM format before core conversion,
- `type` values beyond implementation branch checks,
- empty certificate arrays,
- duplicate serials inside the same submitted request body,
- `limit == 0`,
- overly large `limit`.

Add such validations in `RequestValidationUtil` if new behavior requires stronger API input guarantees.

## Error Handling

Main API exceptions:

- `InputValidationException`: HTTP `400`.
- `UnexpectedServerErrorException`: HTTP `500`.
- `MDMAPIException`: mapped by `ErrorHandler` to HTTP `500`.
- `BadRequestException`: available under `api.exception` for bad requests.

Core exceptions caught in implementation:

- `CertificateManagementException`
- `KeystoreException`
- `SCEPException`
- `JWTClientException`

Current response style:

- Some failure branches return `Response.serverError().entity(ErrorResponse).build()`.
- Some expected branches return simple strings such as `valid`, `invalid`, or deletion messages.
- Validation exceptions use structured `ErrorResponse`.

Implementation guidance:

- Use `InputValidationException` for request validation failures.
- Log server-side exceptions with context and throwable.
- Return concise messages to clients.
- Do not leak keystore passwords, private key material, tenant-flow internals, or stack traces.
- Keep core exception wrapping in core; the API should only map domain exceptions to HTTP.

## Security, CORS, and Transport

Authentication/API publishing:

- `doAuthentication=true`
- `basicAuth=true`
- `managed-api-enabled=true`
- `managed-api-owner=admin`
- `isSharedWithAllTenants=true`

Transport:

- `web.xml` requires `CONFIDENTIAL` transport for all resources.

CORS:

`ApiOriginFilter` adds:

- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: GET, POST, DELETE, PUT`
- `Access-Control-Allow-Headers: Content-Type`

Swagger OAuth security:

`SecurityDefinitionConfigurator` adds an OAuth2 application-flow security definition named `swagger_auth`, using token and authorize URLs based on the Swagger host.

When changing security behavior:

- Keep webapp descriptor settings, Swagger scopes, and product permission paths aligned.
- Review whether wildcard CORS is acceptable for new admin operations.
- Keep sensitive verification and deletion paths protected by scopes and authentication.

## Common Usage Flows

### Add Certificate

1. Admin submits certificate serial and Base64 PEM.
2. API resolves certificate core service.
3. API converts PEM to `X509Certificate`.
4. API rejects existing serials.
5. API saves certificates through core.
6. Core persists certificate rows in `DM_DEVICE_CERTIFICATE`.

### List Certificates

1. Admin supplies optional filters and pagination.
2. API builds `CertificatePaginationRequest`.
3. Core opens DAO connection.
4. DAO applies current tenant filter and optional serial/device/user filters.
5. API wraps result in `CertificateList`.

### Delete Certificate

1. Admin supplies serial number.
2. API checks deletion metadata through core.
3. Core reads metadata from device-management metadata service.
4. If enabled, core removes tenant-scoped certificate row.
5. API returns success, not found, unauthorized, or server error.

### Verify Android Certificate

1. Admin/security filter submits Android certificate material.
2. API chooses subject-DN or PEM verification.
3. Core checks certificate presence by DN/common name.
4. API returns `valid` or `invalid`.

### Verify iOS Certificate

1. Admin/security filter submits iOS certificate signature material.
2. Core extracts certificate and challenge token.
3. SCEP manager resolves the enrolled device and owning tenant.
4. API starts tenant flow, mints JWT, and returns validation response.

## Extension Guidelines

When adding or changing admin endpoints:

- Keep API annotations on `CertificateManagementAdminService`.
- Keep execution in `CertificateManagementAdminServiceImpl`.
- Put reusable request checks in `RequestValidationUtil`.
- Put service lookups in `CertificateMgtAPIUtils`.
- Add DTOs under `beans` when response shape is reused or documented.
- Do not call DAOs directly from API code.
- Do not mutate tenant context without `try/finally`.
- Add or update Swagger responses and scopes with every endpoint change.
- Preserve existing response formats unless API versioning is planned.

## Known Risks To Check Before Extending

- `getCertificate` searches by serial and returns a list, despite route shape suggesting exact lookup.
- `CertificateList` has `next` and `previous` fields but the implementation does not populate them.
- `EnrollmentCertificate#tenantId` is present but ignored during add; current tenant context is authoritative.
- `verifyCertificate` returns plain strings for Android but JSON `ValidationResponse` for iOS.
- Windows is documented as an allowable `type` but has no branch; it currently falls through to `invalid`.
- Wildcard CORS is enabled for the admin API.
- `ErrorResponse#toString()` returns `null`, so do not rely on it for diagnostics.
- `CertificateManagementAdminServiceImpl` imports some classes twice; avoid copying that style into new files.
- No source `permissions.xml` exists in this module; authorization behavior depends on scopes and managed API publishing metadata.
