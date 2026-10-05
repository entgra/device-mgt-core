# Certificate Management Core Guide

This file describes the certificate management core module:

`components/certificate-mgt/io.entgra.device.mgt.core.certificate.mgt.core`

The module is an OSGi bundle that owns certificate-related business behavior for Entgra Device Management Core. It is used by the SCEP API and certificate admin API WAR modules, and it exposes services that other bundles/webapps retrieve from the Carbon OSGi runtime.

## Module Role

Certificate management core is responsible for:

- Loading certificate management configuration from Carbon config.
- Initializing the certificate management datasource and DAO factory.
- Optionally creating the certificate repository schema when the server starts with `-Dsetup`.
- Reading CA and RA certificates/private keys from the configured keystore.
- Generating self-signed X.509 certificates for internal/test flows.
- Signing CSRs and SCEP PKI messages with the CA/RA material.
- Persisting issued or uploaded certificates in `DM_DEVICE_CERTIFICATE`.
- Looking up certificates by serial number or alias.
- Verifying certificate signatures, PEM certificates, and subject DNs.
- Caching certificate lookup responses by serial number and common name.
- Resolving a validated SCEP device across tenants through `DeviceManagementProviderService`.

This module does not expose HTTP endpoints directly. HTTP exposure is handled by:

- `io.entgra.device.mgt.core.certificate.mgt.api`, the SCEP API WAR.
- `io.entgra.device.mgt.core.certificate.mgt.cert.admin.api`, the admin certificate API WAR.

Those API modules should call this core through OSGi services where possible.

## Important Packages

- `service`: Public certificate management service contract and implementation.
- `dao`: DAO contract, DAO factory, DAO exceptions, and JDBC utilities.
- `dao.impl`: Generic and DB-specific DAO implementations.
- `impl`: Certificate generation, SCEP message handling, keystore reading, and verification helpers.
- `internal`: OSGi component and module data holder.
- `config`: JAXB-bound configuration model and configuration manager.
- `config.datasource`: JNDI datasource configuration model.
- `cache`: Certificate cache contract and JCache implementation.
- `scep`: SCEP-specific service contract, implementation, exceptions, and tenant/device wrapper.
- `dto`: API-facing response/status DTOs used by callers.
- `bean`: Internal certificate persistence bean.
- `util`: Constants, XML/document utilities, schema initializer, serialization, and date/subject helpers.

## Build and Runtime Packaging

The module POM packages this module as an OSGi `bundle` using `maven-bundle-plugin`.

Important bundle behavior:

- `io.entgra.device.mgt.core.certificate.mgt.core.internal` and `io.entgra.device.mgt.core.certificate.mgt.core.util` are private packages.
- Most `io.entgra.device.mgt.core.certificate.mgt.core.*` packages are exported for consumers.
- `jscep` is embedded as a compile/runtime dependency.
- Bouncy Castle, commons, WSO2 Carbon, OSGi, datasource, cache, and device-management packages are imported.

The matching server feature is:

`features/certificate-mgt/io.entgra.device.mgt.core.certificate.mgt.server.feature`

That feature packages:

- the certificate core bundle,
- cryptographic dependencies,
- `certificate-config.xml`,
- `wso2certs.jks`,
- DB scripts for H2, MySQL, PostgreSQL, SQL Server, and Oracle,
- a config template under `conf_templates`.

## Configuration Requirements

The configuration file is expected at:

`${carbon.home}/repository/conf/certificate-config.xml`

`CertificateConfigurationManager` constructs this path with:

- `CarbonUtils.getCarbonConfigDirPath()`
- `CertificateManagementConstants.CERTIFICATE_CONFIG_XML_FILE`

The root XML element is `CertificateConfigurations`, mapped to `CertificateManagementConfig`.

Required sections:

- `CertificateKeystore`
- `ManagementRepository`
- `DefaultPageSize`

### Keystore Configuration

`CertificateKeystoreConfig` requires:

- `CertificateKeystoreLocation`
- `CertificateKeystoreType`
- `CertificateKeystorePassword`
- `CACertAlias`
- `CAPrivateKeyPassword`
- `RACertAlias`
- `RAPrivateKeyPassword`

The default feature config points to:

`${carbon.home}/repository/resources/security/wso2certs.jks`

with type `JKS`, CA alias `cacert`, and RA alias `racert`.

`CertificateKeystoreConfig#setCertificateKeystoreLocation` replaces `${carbon.home}` with the JVM `carbon.home` system property. A missing, unreadable, wrongly typed, or incorrectly passworded keystore causes `KeystoreException` from `KeyStoreReader`.

### Datasource Configuration

`CertificateManagementRepository` requires a `DataSourceConfiguration`.

The default feature config uses:

`jdbc/DM_DS`

The datasource is resolved by `CertificateManagementDAOFactory` through `CertificateManagementDAOUtil.lookupDataSource`.

Runtime requirements:

- The JNDI datasource must be available before certificate core activation.
- The datasource connection metadata must expose a supported database product name.
- The `DM_DEVICE_CERTIFICATE` table must exist unless `-Dsetup` is used and schema initialization succeeds.

## OSGi Activation and Services

The OSGi entry point is `CertificateManagementServiceComponent`.

On activation it:

1. Initializes certificate config through `CertificateConfigurationManager.getInstance().initConfig()`.
2. Reads `CertificateManagementConfig`.
3. Extracts `DataSourceConfig`.
4. Initializes `CertificateManagementDAOFactory`.
5. If `-Dsetup` is set, checks `DM_DEVICE_CERTIFICATE` and creates schema through `CertificateMgtSchemaInitializer` if missing.
6. Registers `CertificateManagementService` using `CertificateManagementServiceImpl.getInstance()`.
7. Registers `SCEPManager` using a new `SCEPManagerImpl()`.

Services exposed:

- `io.entgra.device.mgt.core.certificate.mgt.core.service.CertificateManagementService`
- `io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPManager`

OSGi service dependencies:

- `DeviceManagementProviderService` is mandatory and dynamic.

The dependency is stored in `CertificateManagementDataHolder`. `SCEPManagerImpl` uses it to resolve a device and its owning tenant during iOS SCEP certificate validation flows.

Activation catches `Throwable` and logs an initialization error. It does not rethrow, so bundle activation failure can be logged without crashing the entire Carbon runtime. Any downstream component that expects the registered OSGi services should still handle missing services.

## Public Service Contract

`CertificateManagementService` is the main component contract. It groups several responsibilities:

Keystore and CA/RA access:

- `getCACertificate()`
- `getRACertificate()`
- `getRootCertificates(byte[] ca, byte[] ra)`
- `getCertificateByAlias(String alias)`

Certificate generation and signing:

- `generateX509Certificate()`
- `generateCertificateFromCSR(PrivateKey privateKey, PKCS10CertificationRequest request, String issueSubject)`
- `getSignedCertificateFromCSR(String binarySecurityToken)`
- `generateAlteredCertificateFromCSR(String csr)`

SCEP support:

- `getCACertSCEP()`
- `getCACapsSCEP()`
- `getPKIMessageSCEP(InputStream inputStream)`

Verification:

- `verifySignature(String headerSignature)`
- `verifyPEMSignature(X509Certificate requestCertificate)`
- `verifySubjectDN(String requestDN)`
- `extractCertificateFromSignature(String headerSignature)`
- `extractChallengeToken(X509Certificate certificate)`

Persistence and lookup:

- `saveCertificate(List<Certificate> certificate)`
- `pemToX509Certificate(String pem)`
- `retrieveCertificate(String serialNumber)`
- `getAllCertificates(CertificatePaginationRequest request)`
- `getCertificates()`
- `searchCertificates(String serialNumber)`
- `getCertificateBySerial(String serial)`
- `removeCertificate(String serialNumber)`
- `getValidateMetaValue()`

## Service Implementation Guide

`CertificateManagementServiceImpl` is a singleton-style service facade. `getInstance()` lazily constructs one service instance and initializes shared `KeyStoreReader` and `CertificateGenerator` instances.

The implementation divides work this way:

- Keystore and crypto-heavy methods delegate to `KeyStoreReader` or `CertificateGenerator`.
- Repository reads open a DAO connection with `CertificateManagementDAOFactory.openConnection()`.
- Repository writes/removes start transactions with `CertificateManagementDAOFactory.beginTransaction()`.
- DAO exceptions are converted to `CertificateManagementException`.
- Keystore, crypto, certificate parse, and SCEP failures are converted to `KeystoreException`.

Read-only service methods should follow this pattern:

```java
try {
    CertificateManagementDAOFactory.openConnection();
    CertificateDAO certificateDAO = CertificateManagementDAOFactory.getCertificateDAO();
    return certificateDAO.someRead(...);
} catch (SQLException e) {
    throw new CertificateManagementException("Error occurred while opening a connection ...", e);
} catch (CertificateManagementDAOException e) {
    throw new CertificateManagementException("Error occurred while ...", e);
} finally {
    CertificateManagementDAOFactory.closeConnection();
}
```

Write methods should follow this pattern:

```java
try {
    CertificateManagementDAOFactory.beginTransaction();
    CertificateDAO certificateDAO = CertificateManagementDAOFactory.getCertificateDAO();
    certificateDAO.someWrite(...);
    CertificateManagementDAOFactory.commitTransaction();
} catch (TransactionManagementException e) {
    throw new CertificateManagementException("Error occurred while ...", e);
} catch (CertificateManagementDAOException e) {
    CertificateManagementDAOFactory.rollbackTransaction();
    throw new CertificateManagementException("Error occurred while ...", e);
}
```

When adding new service operations:

- Keep tenant awareness at the service/DAO boundary.
- Do not expose raw `SQLException` or `CertificateManagementDAOException` to API modules.
- Use domain exceptions with messages that name the operation and key input, such as serial number.
- Close DAO connections in `finally` for read operations.
- Roll back transactions before throwing for failed write operations.
- Prefer the OSGi service contract over direct construction from API modules.

## Certificate Generation and Verification

`CertificateGenerator` owns certificate creation, CSR processing, SCEP response construction, and signature/PEM/subject verification.

Important behavior:

- Adds Bouncy Castle provider before generating certificates.
- Uses RSA and SHA256-with-RSA constants from `CertificateManagementConstants`.
- Generates validity dates through `CommonUtil`.
- Uses CA private key and issuer subject when signing CSRs.
- Stores generated certificates in the DB by calling `saveCertInKeyStore`.
- Extracts certificate details into `CertificateResponse` by deserializing stored certificate bytes.
- For CSR signing, uses `UNIQUE_IDENTIFIER` or `SERIALNUMBER` from the CSR subject as the certificate serial source when available, otherwise falls back to a generated serial number.
- Adds key usage and challenge password extensions where appropriate.
- For SCEP PKI messages, decodes incoming CMS data, signs the CSR, builds a `CertRep`, and encodes the response with RA material.

Despite the method name `saveCertInKeyStore`, generated and uploaded certificates are persisted in the database through `CertificateDAO`; the configured JKS is used for CA/RA material.

Verification paths:

- `verifySignature` succeeds when a certificate can be extracted from a Base64 CMS signature.
- `verifyPEMSignature` extracts the common name from an X.509 certificate and checks repository presence.
- `verifyCertificateDN` supports DN strings containing `CN=` and optionally SCEP tenant OU values like `OU=tenant_<TENANT_ID>`.
- `extractCertificateFromSignature` resolves certificates by serial and tenant when SCEP OU is present.

## Keystore Reader Guide

`KeyStoreReader` owns all configured keystore access and cached certificate repository reads.

It loads the keystore using:

- configured type,
- configured path,
- configured keystore password.

It retrieves:

- CA certificate by `CACertAlias`.
- CA private key by `CACertAlias` and `CAPrivateKeyPassword`.
- RA certificate by `RACertAlias`.
- RA private key by `RACertAlias` and `RAPrivateKeyPassword`.

It also reads persisted certificates from `DM_DEVICE_CERTIFICATE` through `CertificateDAO`.

Lookup behavior:

- `getCertificateBySerial(serialNumber)` checks `CertificateCacheManager` first.
- Cache misses open a DAO connection and call `retrieveCertificate`.
- If a certificate is found, it is deserialized, common name is extracted, and the response is cached.
- `getCertificateBySerial(serialNumber, tenantId)` is used for cross-tenant/SCEP-aware lookups.
- `getCertificateByAlias(alias)` reads the certificate and deserializes it without using the serial cache.

Any new keystore or certificate retrieval logic should preserve the distinction between:

- CA/RA material in the configured JKS/PKCS12 keystore.
- Device/client certificates stored in the database.

## SCEP Manager Guide

`SCEPManager` exposes one operation:

- `getValidatedDevice(DeviceIdentifier deviceIdentifier)`

`SCEPManagerImpl` uses `DeviceManagementProviderService#getTenantedDevice` to find the device and its tenant. It then starts a super-tenant flow, retrieves `RealmService`, resolves the tenant domain, and returns a `TenantedDeviceWrapper` containing:

- tenant ID,
- tenant domain,
- device.

Exceptions are wrapped as `SCEPException`.

This service is used by the admin certificate verification API for iOS flows. It enables certificate challenge tokens to be mapped back to an enrolled device and tenant before issuing JWT claims.

## DAO Layer Guide

`CertificateDAO` defines repository operations:

- Add one certificate.
- Add a list of certificates.
- Retrieve by serial number in the current tenant.
- Retrieve by serial number and explicit tenant ID.
- List all certificates with pagination and filters.
- List all certificates for the current tenant.
- Remove by serial number in the current tenant.
- Search by serial number in the current tenant.

`CertificateManagementDAOFactory` is the entry point for DAO instances and connection control.

Supported DB engines:

- H2: `GenericCertificateDAOImpl`
- MySQL: `GenericCertificateDAOImpl`
- PostgreSQL: `PostgreSQLCertificateDAOImpl`
- SQL Server: `SQLServerCertificateDAOImpl`
- Oracle: `OracleCertificateDAOImpl`

The factory stores connection state in `ThreadLocal<Connection>` and tracks transaction state in `ThreadLocal<TxState>`.

DAO connection rules:

- Call `openConnection()` before read-only DAO calls.
- Call `beginTransaction()` before write/delete DAO calls.
- DAO implementations get the current connection with `CertificateManagementDAOFactory.getConnection()`.
- Do not call `openConnection()` or `beginTransaction()` while another connection is active in the same thread.
- Always call `closeConnection()`, `commitTransaction()`, or `rollbackTransaction()` according to operation type.

`AbstractCertificateDAOImpl` implements common SQL for:

- insert single certificate,
- insert certificate batch,
- retrieve by serial and current tenant,
- retrieve by serial and explicit tenant,
- search serial with `LIKE`,
- list all current-tenant certificates,
- remove current-tenant certificate.

Concrete DAO classes implement dialect-specific pagination SQL.

Tenant handling:

- Most DAO methods use `PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId()`.
- Batch insert and single insert store the tenant ID from the certificate bean.
- Current username is read from `PrivilegedCarbonContext` and stored in `USERNAME`.
- Explicit tenant lookup is available for SCEP certificate verification across tenant boundaries.

Certificate storage:

- X.509 certificates are serialized with `Serializer.serialize`.
- Stored bytes are deserialized by `Serializer.deserialize`.
- `CertificateGenerator.extractCertificateDetails` fills response fields such as validity dates, serial, issuer, subject, and version.

## Repository Schema

The logical table is `DM_DEVICE_CERTIFICATE`.

DAO code expects these columns:

- `ID`
- `SERIAL_NUMBER`
- `CERTIFICATE`
- `TENANT_ID`
- `USERNAME`
- `DEVICE_IDENTIFIER`

The feature DB scripts currently define `ID`, `SERIAL_NUMBER`, `CERTIFICATE`, `TENANT_ID`, and `USERNAME`. The DAO code also inserts, filters, and reads `DEVICE_IDENTIFIER`. The test H2 schema includes `DEVICE_IDENTIFIER`, so production DB scripts should be checked before relying on device identifier filtering or insert paths.

When adding schema fields:

- Update all DB scripts under `features/certificate-mgt/.../dbscripts/certMgt`.
- Update test SQL under `src/test/resources/sql`.
- Update all dialect DAO implementations if filtering, ordering, or pagination is affected.
- Keep tenant constraints in every lookup/delete path.

## Cache Behavior

`CertificateCacheManagerImpl` uses JCache.

Cache names:

- manager: `CERTIFICATE_CACHE_MANAGER`
- cache: `CERTIFICATE_CACHE`

Key prefixes:

- serial: `S_`
- common name: `C_`

Cache configuration is read from `DeviceConfigurationManager.getInstance().getDeviceManagementConfig()`. Certificate cache initialization depends on device-management configuration, not certificate-management configuration.

Cache usage in this module is read-through:

- Check cache by serial.
- If not found, read from DAO.
- If found in DAO, deserialize, enrich common name, and cache.

When changing cache behavior:

- Keep cache keys stable.
- Make sure null cache manager or disabled cache paths are considered.
- Do not use cache as the source of truth; the database remains authoritative.

## Exception Handling Model

Main exception types:

- `CertificateManagementException`: service/config/schema level failures visible to core callers.
- `CertificateManagementDAOException`: DAO and SQL mapping failures.
- `KeystoreException`: keystore, certificate parsing, signing, SCEP crypto, and verification failures.
- `TransactionManagementException`: transaction setup failures.
- `IllegalTransactionStateException`: improper nested/missing connection usage.
- `UnsupportedDatabaseEngineException`: unsupported DB product name in DAO factory.
- `SCEPException`: SCEP device/tenant validation failures.

General rules:

- DAO implementations throw `CertificateManagementDAOException`, not `SQLException`.
- Service methods wrap DAO failures in `CertificateManagementException`.
- Crypto and keystore helper methods wrap checked cryptographic, IO, certificate, CMS, and provider exceptions in `KeystoreException`.
- SCEP device validation wraps device-management and user-store failures in `SCEPException`.
- API modules should translate these domain exceptions to HTTP responses, usually `400`, `404`, or `500` depending on the caller contract.

Current activation code logs initialization failures and does not rethrow. Callers should not assume the OSGi service exists unless service lookup succeeds.

## Common Usage Flows

### Startup

1. Carbon starts the certificate core bundle.
2. `CertificateManagementServiceComponent.activate` initializes config.
3. DAO factory resolves the datasource and DB engine.
4. Optional `-Dsetup` initializes schema.
5. `CertificateManagementService` and `SCEPManager` are registered as OSGi services.
6. API WARs can retrieve the services from `PrivilegedCarbonContext`.

### Add Certificates From Admin API

1. Admin API receives `EnrollmentCertificate[]`.
2. API resolves `CertificateManagementService` from OSGi.
3. PEM strings are converted with `pemToX509Certificate`.
4. Existing serials are checked through `getCertificateBySerial`.
5. Certificates are saved through `saveCertificate`.
6. `CertificateGenerator.saveCertInKeyStore` persists the list through `CertificateDAO.addCertificate`.

### Sign CSR

1. Caller submits Base64 CSR/binary security token.
2. Core decodes/parses CSR in `CertificateGenerator`.
3. CA private key and CA certificate material are read from the configured keystore.
4. A signed X.509 certificate is generated.
5. The issued certificate is saved in `DM_DEVICE_CERTIFICATE`.
6. Caller receives the signed certificate material.

### SCEP CA Cert and Caps

1. `getCACapsSCEP` returns static CA capabilities text from `CertificateManagementConstants.POST_BODY_CA_CAPS`.
2. `getCACertSCEP` reads CA and RA certificates.
3. If both exist, the response is encoded as degenerate CMS data and marked `CA_RA_CERT_RECEIVED`.

### SCEP PKI Message

1. Incoming CMS signed data is decoded.
2. RA certificate/private key decrypts the request envelope.
3. CA private key signs the CSR.
4. RA certificate/private key signs/encrypts the SCEP response.
5. The issued certificate is persisted.

### Verify Android Certificate

1. Admin API receives type `android` and an enrollment certificate payload.
2. If serial contains `proxy-mutual-auth-header`, subject DN verification is used.
3. Otherwise PEM is converted to X.509 and verified by common name lookup.
4. A non-empty common name in the matched `CertificateResponse` means `valid`.

### Verify iOS Certificate

1. Admin API receives type `ios` and a CMS signature payload.
2. Core extracts the X.509 certificate from the signature.
3. Challenge token is extracted from the certificate extension.
4. `SCEPManager` validates the device and resolves tenant metadata.
5. Admin API creates JWT claims for the tenant/device.

## Implementation Requirements and Invariants

- Keep all certificate repository access tenant-scoped unless an explicit tenant ID is required for SCEP.
- Keep CA/RA credentials in the configured keystore, not in the database.
- Keep issued/client certificates in `DM_DEVICE_CERTIFICATE`.
- Use prepared statements for SQL parameters.
- Keep DAO methods free of HTTP concerns.
- Keep API modules free of SQL and transaction handling.
- Keep OSGi service registration in the internal component.
- Do not bypass `CertificateManagementDAOFactory` for JDBC connections.
- Do not expose raw SQL, crypto, or keystore exceptions past the service/API boundary.
- Update feature DB scripts and test SQL together.
- When adding a new public core capability, add it to `CertificateManagementService` and `CertificateManagementServiceImpl`, then consume it from API code through OSGi lookup.

## Known Risks To Check Before Extending

- Production certificate DB scripts currently appear to be missing `DEVICE_IDENTIFIER`, while DAO code expects it.
- PostgreSQL pagination SQL uses `LIMIT ? OFFSET ?`; verify parameter order before changing or relying on pagination semantics.
- The SCEP signing API directly constructs `CertificateGenerator` instead of retrieving `CertificateManagementService` from OSGi. New API code should prefer the service lookup pattern.
- `CertificateCacheManagerImpl` depends on device-management cache configuration; validate cache initialization when using certificate cache outside normal device-management startup.
- Activation logs and swallows `Throwable`, so failed certificate-core initialization can surface later as missing OSGi services.

## Test Coverage Guide

The module has TestNG tests under `src/test/java` and resources under `src/test/resources`.

Existing test areas include:

- certificate DTO behavior,
- certificate generation,
- negative generation paths,
- key generation,
- common utilities,
- certificate cache manager,
- certificate management service implementation,
- negative service paths.

Test resources include:

- CA/RA certificates and private key material,
- `wso2certs.jks`,
- test `certificate-config.xml`,
- test datasource config,
- test H2 SQL,
- Carbon home config stubs.

When changing this module:

- Add or update TestNG tests near the affected class.
- Update `src/test/resources/sql/h2.sql` for repository schema changes.
- Keep fixture certificate material usage isolated to tests.
- For service changes, test both success and wrapped-exception behavior.
- For DAO changes, test tenant filtering and pagination/filter combinations.

