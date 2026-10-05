# Certificate Management Regular API Guide

This file describes the regular certificate management API module:

`components/certificate-mgt/io.entgra.device.mgt.core.certificate.mgt.api`

This module is a CXF/JAX-RS WAR that exposes the SCEP-facing certificate signing endpoint. It is the smaller of the two certificate API modules. The admin certificate API lives in `io.entgra.device.mgt.core.certificate.mgt.cert.admin.api`.

## Module Role

The regular certificate API is responsible for:

- Exposing the SCEP certificate signing HTTP endpoint.
- Publishing Swagger/API metadata for the SCEP API.
- Declaring the API management scope needed to sign CSRs.
- Accepting a Base64/binary security token body as `text/plain`.
- Returning the signed X.509 certificate as Base64-encoded `text/plain`.
- Converting core keystore/certificate failures into HTTP `500` responses.
- Supplying shared JSON message-body and API error handling providers.
- Being packaged as a Carbon webapp WAR for deployment through the certificate API feature.

The module does not own certificate persistence, CA/RA key access, SCEP message processing, or DAO logic. Those responsibilities belong to:

`components/certificate-mgt/io.entgra.device.mgt.core.certificate.mgt.core`

## Build and Deployment

The module POM packages this module as a WAR.

Important build settings:

- Packaging type: `war`
- WAR name: `api#scep-mgt#v1.0`
- CXF libraries are excluded from the WAR with `WEB-INF/lib/*cxf*.jar`
- The module depends on certificate core with `provided` scope.
- Swagger, CXF JAX-RS, Spring web/context, Gson, JAXB, commons-codec, and Entgra API management annotations are used.

The WAR name maps to Carbon webapp deployment conventions. At runtime, `api#scep-mgt#v1.0.war` is exposed as an API webapp path equivalent to `/api/scep-mgt/v1.0`.

The matching feature module is:

`features/certificate-mgt/io.entgra.device.mgt.core.certificate.mgt.api.feature`

That feature packages this WAR for product installation.

## Important Packages

- `api`: JAX-RS service contract and Swagger annotations.
- `api.impl`: Runtime JAX-RS implementation.
- `api.beans`: Error response DTOs.
- `api.common`: Gson provider, `MDMAPIException`, and error mapper.
- `api.exception`: Web application exceptions and simple message payload.
- `api.util`: Response payload builder helper.
- `src/main/webapp/WEB-INF`: CXF and webapp deployment descriptors.
- `src/main/webapp/META-INF`: Carbon classloading descriptor.

## HTTP Contract

Service contract:

`CertificateMgtService`

Implementation:

`CertificateMgtServiceImpl`

Class-level path:

`/scep`

Operation:

`POST /scep/sign-csr`

Consumes:

`text/plain`

Produces:

`text/plain`

Input:

- Request body: Base64-encoded CSR/binary security token string.
- Header: `If-Modified-Since`, declared for API consistency but not used by the implementation.

Output:

- `200 OK` with Base64-encoded signed certificate.
- `500 Internal Server Error` with error payload/message when signing, keystore access, or certificate encoding fails.

Declared API scope:

- Name: `Sign CSR`
- Key: `dm:sign-csr`
- Role: `Internal/devicemgt-user`
- Permission: `/device-mgt/certificates/manage`

Swagger metadata declares:

- API display name: `SCEP Management`
- Context: `/api/device-mgt/v1.0/scep`
- Tag: `scep_management`

## CXF and Webapp Wiring

`WEB-INF/cxf-servlet.xml` defines two CXF servers:

- `services` at `/`, currently exposing Swagger resources and providers.
- `certificateService` at `/certificates`, exposing `certificateServiceBean`.

`certificateServiceBean` is:

`io.entgra.device.mgt.core.certificate.mgt.api.impl.CertificateMgtServiceImpl`

Providers:

- `GsonMessageBodyHandler`
- `ErrorHandler`
- `SwaggerSerializers` for Swagger resources

Swagger resource:

- `io.swagger.jaxrs.listing.ApiListingResource`

`WEB-INF/web.xml` defines:

- `CXFServlet` mapped to `/*`
- `doAuthentication=true`
- `basicAuth=true`
- `managed-api-enabled=true`
- `managed-api-owner=admin`
- `isSharedWithAllTenants=true`
- `HttpHeaderSecurityFilter`
- `ContentTypeBasedCachePreventionFilter`

`META-INF/webapp-classloading.xml` configures:

- `ParentFirst=false`
- Environments: `CXF3,Carbon`

When modifying CXF routing, keep the class-level `@Path`, service bean address, Swagger base path, and WAR deployment name aligned. This module currently has both a bean address of `/certificates` and a service path of `/scep`; verify effective runtime paths before changing external clients.

## Implementation Flow

`CertificateMgtServiceImpl#getSignedCertFromCSR` performs this flow:

1. Receives the text body as `binarySecurityToken`.
2. Creates a `CertificateGenerator`.
3. Calls `certificateGenerator.getSignedCertificateFromCSR(binarySecurityToken)`.
4. If the returned certificate is `null`, returns `500` with message `Error occurred while signing the CSR.`
5. Otherwise Base64-encodes `signedCert.getEncoded()`.
6. Returns `200 OK` with the encoded certificate string.
7. Converts `KeystoreException` to `UnexpectedServerErrorException`.
8. Converts `CertificateEncodingException` to `UnexpectedServerErrorException`.

Important current behavior:

- The implementation calls `getSignedCertificateFromCSR` twice in the success path: once for the null check and once to get the certificate. Be careful before copying this pattern because CSR signing can have side effects through certificate persistence.
- The implementation directly constructs `CertificateGenerator` instead of resolving `CertificateManagementService` from OSGi. New API code should prefer the OSGi service facade used by the admin API module.
- The endpoint returns `text/plain`, not JSON, for successful signing.

## Dependency on Core

This API needs certificate core because signing relies on:

- configured CA certificate,
- configured CA private key,
- certificate parsing,
- X.509 certificate generation,
- certificate persistence through `CertificateDAO`.

The module declares certificate core as `provided`, meaning core must be installed as an OSGi bundle in the runtime. The WAR should not package or own the core bundle classes as private business logic.

Preferred pattern for future endpoints:

```java
CertificateManagementService service =
        (CertificateManagementService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(CertificateManagementService.class, null);
```

or a small API utility wrapper, matching the admin API's `CertificateMgtAPIUtils`.

## Error Handling

Main error classes:

- `UnexpectedServerErrorException`: returns HTTP `500` with an `ErrorResponse`.
- `BadRequestException`: available for HTTP `400`, though this endpoint currently does not use it.
- `MDMAPIException`: mapped by `ErrorHandler` to HTTP `500`.
- `Message`: simple string error payload used by the null signing response path.
- `ErrorResponse`: structured error model with `code`, `message`, `description`, `moreInfo`, and `errorItems`.

`GsonMessageBodyHandler` serializes/deserializes JSON request and response bodies for providers that use JSON. The CSR signing success path is `text/plain`, so Gson is mostly relevant for error payloads and future JSON endpoints.

Implementation guidance:

- Validate missing/empty CSR input before calling core if new behavior is added.
- Return `400 Bad Request` for malformed user input.
- Return `500 Internal Server Error` for signing, keystore, persistence, or encoding failures.
- Do not leak private key, keystore path, password, or raw stack traces in response entities.
- Log server-side details with `log.error(msg, e)` and return a concise external message.

## Security and API Publishing

The API is marked as authenticated:

- `doAuthentication=true`
- `basicAuth=true`

It is also marked for managed API publishing:

- `managed-api-enabled=true`
- `managed-api-owner=admin`
- `isSharedWithAllTenants=true`

Authorization intent is declared through Entgra `@Scopes` and Swagger extension metadata. The signing operation requires `dm:sign-csr`.

When adding endpoints:

- Add a matching `@Scope` entry at service-interface level.
- Add the scope key to each `@ApiOperation` extension.
- Keep permission paths under the existing `/device-mgt/...` permission tree unless product authorization requirements change.
- Update Swagger tags and context if the endpoint belongs to a different API family.

## Request and Response Requirements

Request body requirements:

- The body is expected as a plain string.
- It should be a Base64-encoded binary security token/CSR accepted by core signing logic.
- The endpoint does not currently enforce non-empty input before calling core.

Response requirements:

- Success response body is a Base64 string representing the encoded signed X.509 certificate.
- Error responses may be structured `ErrorResponse` or simple `Message`, depending on the failure branch.

Future improvements should normalize error response types.

## Extension Guidelines

When adding or changing this API:

- Keep the JAX-RS contract in `CertificateMgtService`.
- Keep implementation in `CertificateMgtServiceImpl`.
- Keep business behavior in certificate core, not in the WAR.
- Retrieve core services through OSGi for new operations.
- Update `cxf-servlet.xml` only when adding new service beans/providers.
- Update `web.xml` only when changing authentication, API publishing, or filters.
- Keep successful CSR signing response as `text/plain` unless client contracts are intentionally versioned.
- Add Swagger annotations and scope metadata for every public operation.
- Avoid direct DAO access from API classes.

## Known Risks To Check Before Extending

- Direct `CertificateGenerator` construction bypasses the public `CertificateManagementService` facade.
- CSR signing is invoked twice in the current success path.
- Effective runtime path should be verified because CXF has service address `/certificates` while the resource path is `/scep`.
- The `If-Modified-Since` header is documented but unused.
- `ErrorResponse#toString()` returns `null`, so do not rely on it for logging or response formatting.
- No source `permissions.xml` exists in this module; authorization behavior depends on scopes and managed API publishing metadata.

