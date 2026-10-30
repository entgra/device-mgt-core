# Application Management Source Map

Status: Draft evidence index; source inspection only, 2026-10-27.
Core baseline: `293d35abfdf57fb461a1b745fbe735b581555be4`.
Proprietary commons baseline: `79b2891a67df7f71627b1d3456132107290e6bc7`.
Owner: Application Management component owner (assignment pending).

Links are relative to this repository, with sibling-repository links explicitly
identified. Line numbers below apply to the recorded commits; method names are
the durable lookup key. This is a curated evidence index, not an assertion that
every branch of every class has been validated.

## Core

| Key | Source | Evidence locations |
| --- | --- | --- |
| AM | [ApplicationManagerImpl](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/ApplicationManagerImpl.java) | create 183-375; releases 380-590,1754; favourites 672; list 1312; DB create 1605; delete 2218,2348; lifecycle 2619,2711; update 2847; tags/categories 3041-3464; validation 4038; status sync 4480; tenant cleanup 4639; firmware 4783; release list 5267 |
| Contract | [ApplicationManager](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/ApplicationManager.java) | Public application service interface |
| SUB | [SubscriptionManagerImpl](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/SubscriptionManagerImpl.java) | bulk operations 167; scheduling 374-512; status 1478; enrollment 1569; reporting 1640 onward |
| REVIEW | [ReviewManagerImpl](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/ReviewManagerImpl.java) | add 78; replies 169; update 279; listing 380; deletion 587; ratings 652 |
| STORE | [ApplicationStorageManagerImpl](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/ApplicationStorageManagerImpl.java) | images 66; installer parsing 129; release upload 157; deletion 235; reads 252; tenant cleanup 316 |
| TRANSFER | [FileTransferServiceImpl](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/FileTransferServiceImpl.java) | generateUploadLink 66; resolve/writeChunk 81-103; download resolution 105 onward |
| DATA | [AppmDataHandlerImpl](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/AppmDataHandlerImpl.java) | lifecycle config 57; artifact stream 189; agent stream 244 |
| SP | [SPApplicationManagerImpl](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/SPApplicationManagerImpl.java) | identity-server CRUD 79-209; remote listing 453; validation 522; attach/detach 613; create-and-attach 652 |
| VPP | [VppApplicationManagerImpl](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/VppApplicationManagerImpl.java) | users 102-260; syncAssets 261; association 440; backend/token 575-602 |
| TX | [ConnectionManagerUtil](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/util/ConnectionManagerUtil.java) | open/get/begin/commit/rollback/close; both deprecated and current APIs |
| DS | [ApplicationManagementServiceComponent](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/internal/ApplicationManagementServiceComponent.java) | activate 67-125; deactivate 128 |
| LC | [LifecycleStateManager](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/lifecycle/LifecycleStateManager.java) | configuration initialization, state flags and transition authorization |
| TASK | [ScheduledAppSubscriptionTask](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/task/ScheduledAppSubscriptionTask.java) | task execution |
| SCHED | [ScheduledAppSubscriptionTaskManager](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/task/ScheduledAppSubscriptionTaskManager.java) | task scheduling and cleanup registration |
| CLEAN | [ScheduledAppSubscriptionCleanupTask](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/task/ScheduledAppSubscriptionCleanupTask.java) | cleanup execution |

## Persistence and deployment

- [DAO interfaces and implementations](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/dao): application, release, review, lifecycle, subscription, visibility, SP and VPP persistence.
- [Feature configuration](../../../features/application-mgt/io.entgra.device.mgt.core.application.mgt.server.feature/src/main/resources/conf/application-mgt.xml): extension classes, storage parameters, lifecycle states (78-199).
- [Datasource configuration](../../../features/application-mgt/io.entgra.device.mgt.core.application.mgt.server.feature/src/main/resources/conf/datasources/application-mgt-datasources.xml).
- [Database scripts](../../../features/application-mgt/io.entgra.device.mgt.core.application.mgt.server.feature/src/main/resources/dbscripts/cdm/application-mgt): H2, MySQL, PostgreSQL, SQL Server, Oracle. [MySQL](../../../features/application-mgt/io.entgra.device.mgt.core.application.mgt.server.feature/src/main/resources/dbscripts/cdm/application-mgt/mysql.sql): release 20; subscription 94; schedule 227; favourites 268; firmware mapping 333.
- [Feature POM](../../../features/application-mgt/io.entgra.device.mgt.core.application.mgt.server.feature/pom.xml) and [templates](../../../features/application-mgt/io.entgra.device.mgt.core.application.mgt.server.feature/src/main/resources/conf_templates/templates/repository/conf).
- [OSGi constitution](../../../docs/constitutions/OSGI_COMPONENT_CONSTITUTION.md) and [CXF constitution](../../../docs/constitutions/CXF_WEBAPP_CONSTITUTION.md). Transaction example qualifications are recorded in APP-GAP-01; these documents are not modified here.

## Consumers

These links require a sibling `proprietary-commons` checkout. Its code is read-only
for this effort. Source inspection does not prove deployed API versions match it.

- [Publisher implementation](../../../../proprietary-commons/components/extensions/application-mgt/io.entgra.proprietary.commons.extension.application.mgt.publisher.api/src/main/java/io/entgra/proprietary/commons/extension/application/mgt/publisher/api/impl/ApplicationManagementPublisherAPIImpl.java): create 185-278; release 281-420; shared create 948; hash precheck 964; upload links 984.
- [Publisher interface](../../../../proprietary-commons/components/extensions/application-mgt/io.entgra.proprietary.commons.extension.application.mgt.publisher.api/src/main/java/io/entgra/proprietary/commons/extension/application/mgt/publisher/api/ApplicationManagementPublisherAPI.java): authoritative route/scope declarations for that checkout.
- [Store API implementations](../../../../proprietary-commons/components/extensions/application-mgt/io.entgra.proprietary.commons.extension.application.mgt.store.api/src/main/java/io/entgra/proprietary/commons/extension/application/mgt/store/api/services/impl): application, subscription, reviews, firmware and VPP adapters. Detailed endpoint-by-endpoint validation remains APP-GAP-10.
- [Artifact API](../../../../proprietary-commons/components/extensions/application-mgt/io.entgra.proprietary.commons.extension.application.mgt.api/src/main/java/io/entgra/proprietary/commons/extension/application/mgt/api/ArtifactDownloadAPI.java).

UI behavior, agent protocols, live VPP/identity-server APIs and product upgrade
migrations are dependency boundaries, not certified by this core-only baseline.

## Tests

- [TestNG suite](../io.entgra.device.mgt.core.application.mgt.core/src/test/resources/testng.xml) lists the configured test classes; `InitTest` is commented out.
- [ApplicationManagementTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/management/ApplicationManagementTest.java): `createApplication`, `testAddApplication`, `addApplicationCategories`; many other methods are disabled empty placeholders.
- [ApplicationManagementDAOTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/dao/ApplicationManagementDAOTest.java).
- [LifecycleManagementTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/LifecycleManagementTest.java): state/transition predicates, not concurrent persistence proof.
- [ArtifactParserTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/ArtifactParserTest.java).
- [StorageManagementUtilTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/StorageManagementUtilTest.java).
- [ConfigurationTest](../io.entgra.device.mgt.core.application.mgt.core/src/test/java/io/entgra/device/mgt/core/application/mgt/core/ConfigurationTest.java).

## Specification source references

Header-level references are consolidated here. These describe the recorded baseline;
method names are lookup keys, and line numbers are not current-checkout guarantees.

| Specification | Source focus |
| --- | --- |
| [Application Management Architecture Baseline](architecture.md) | Evidence: [source map](source-map.md), especially DS, AM, TX and consumer links. |
| [Cross-Cutting Application Management Requirements](cross-cutting-requirements.md) | Sources: [TX, AM, SUB and DS](source-map.md#core). |
| [Application Creation Workflow](creation/creation-workflow.md) | Sources: [AM and Publisher](source-map.md). Existing field/type contracts: [shared](creation/app-creation.md), [enterprise](creation/app-creation-enterprise.md), [public](creation/app-creation-public.md), [web](creation/app-creation-web.md), [custom](creation/app-creation-custom.md). |
| [Release Management Baseline](releases/release-management.md) | Sources: [AM](source-map.md#core) createEntAppRelease/createPubAppRelease/ createWebAppRelease/createCustomAppRelease, createRelease (1754), update*AppRelease (3577 onward), deleteApplicationRelease (2348); Publisher methods (281 onward). |
| [Publication and Lifecycle](lifecycle/lifecycle.md) | Evidence: [AM/LC and packaged configuration](source-map.md). |
| [Artifacts, Transfer and Storage](storage/storage.md) | Evidence: [STORE, TRANSFER, DATA and AM](source-map.md#core). |
| [Application Catalog and Organization](catalog/catalog.md) | Sources: [AM](source-map.md#core) list 1312-1479, detail 1836 onward, delete/retire 2218-2443, update 2847, tags/categories 3041-3464; application DAO. |
| [Subscriptions, Installation and Scheduling](subscriptions/subscriptions.md) | Sources: [SUB, TASK, SCHED, CLEAN and AM](source-map.md#core). |
| [Reviews, Replies and Ratings](reviews/reviews.md) | Source: [REVIEW](source-map.md#core), addReview 78, updateReview 279, deleteReview 587. |
| [Firmware Models and Release Availability](firmware/firmware.md) | Source: [AM](source-map.md#core), getAvailableFirmwaresForDevice 4783, getDevicesByFirmwareMatchType 5125, getAvailableDeviceFirmwareModels 5216, getApplicationReleases 5267; [custom creation](creation/app-creation-custom.md). |
| [Identity-Service-Provider and VPP Integrations](integrations/integrations.md) | Evidence: [SP and VPP](source-map.md#core); consumer adapters are indexed in source-map. |
| [Runtime, Deployment and Tenant Cleanup](runtime/runtime.md) | Sources: [DS, TX, AM, STORE and feature resources](source-map.md). |

## Baseline expansion

B2 inspection: core `9479230e961922e7170da25adb9aaf17278a1c70` on 2026-10-28.
Source anchors below support the added contracts; no execution evidence is implied.

- [Application manager](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/ApplicationManagerImpl.java): retireApplication 2305; update methods 3577/3662/3739/3812; validator 3959 vicinity; getPlistArtifact 4544; getApplicationReleases 5267; syncHashInMetadata 5338.
- [Release filter](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/ReleaseSearchFilter.java): fields and defaults.
- [Generic release DAO](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/dao/impl/application/release/GenericApplicationReleaseDAOImpl.java): getReleases 1090 and getReleasesCount 1197.
- [Generic application DAO](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/dao/impl/application/GenericApplicationDAOImpl.java): retireApplication 845 and list/count exclusions.
- [Subscription manager](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/SubscriptionManagerImpl.java): legacy reporting 1640 onward, provider dispatch 1945 onward, history/count 1981/2030.
- [Reporting provider](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/util/subscription/mgt/SubscriptionManagementServiceProvider.java).
- [Reporting helpers](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/util/subscription/mgt/impl/).
- [Reporting utility](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/util/subscription/mgt/SubscriptionManagementHelperUtil.java).
- [Operation coverage](operation-coverage.md): interface-level inventory and remaining partial contracts.

## Enterprise creation and publication

Source review baseline: core `4db344a2a94ed4483c44e56adb7a31c8d0dabb7a`;
sampled commons adapter `875cea5ab95de2e5067b73fdd22e73dc3e935ff8`.
These references support the enterprise contract; they do not refresh unrelated
specifications or establish installed-product behavior. Method names are lookup
keys; line locations apply to these revisions.

| Source | Contract evidence |
| --- | --- |
| [Application manager](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/ApplicationManagerImpl.java) | createApplication 183; remote dispatch 197/282/306; synchronous initial 234; additional enterprise 340/380; artifact preparation 593/791/1011; mappings/first release/icon write 1605/1734; createRelease 1754; parent lookup 1836; OS range 1907; UUID/DTO lifecycle 2619/2711; initial history helper 3567; application/release validation 4038/4274 |
| [Wrapper conversion](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/util/APIUtil.java) | convertToAppDTO 318; releaseWrapperToReleaseDTO 386; name trimming, type and field conversion |
| [Application helper](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/util/ApplicationManagementUtil.java) | Link construction 169; cleanup 275; release/remote selection 289/309; release-list mutation 644 |
| [Storage implementation](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/impl/ApplicationStorageManagerImpl.java) | getAppInstallerData 129; uploadReleaseArtifact 157; platform parsing and tenant/hash path |
| [Release DAO](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/dao/impl/application/release/GenericApplicationReleaseDAOImpl.java) | createRelease 54; tenant/hash 508; active package 617; installable/self-match/UUID subquery 651 |
| [Lifecycle manager](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/lifecycle/LifecycleStateManager.java) | init; isValidStateChange; getInitialState; getInstallableState; permission, case, same-state and type applicability distinctions |
| [Lifecycle DAO](../io.entgra.device.mgt.core.application.mgt.core/src/main/java/io/entgra/device/mgt/core/application/mgt/core/dao/impl/lifecyclestate/GenericLifecycleStateDAOImpl.java) | addLifecycleState 173 inserts actor/time/reason and tenant/release references; engine variants remain separate verification targets |
| [Device-management icon service](../../device-mgt/io.entgra.device.mgt.core.device.mgt.core/src/main/java/io/entgra/device/mgt/core/device/mgt/core/service/DeviceManagementProviderServiceImpl.java) | saveApplicationIcon 5820; independent DeviceManagementDAOFactory transaction |
| [MySQL schema](../../../features/application-mgt/io.entgra.device.mgt.core.application.mgt.server.feature/src/main/resources/dbscripts/cdm/application-mgt/mysql.sql) | AP_APP 4; AP_APP_RELEASE 20; AP_APP_LIFECYCLE_STATE 69; FK and absent uniqueness constraints |
| [Packaged lifecycle](../../../features/application-mgt/io.entgra.device.mgt.core.application.mgt.server.feature/src/main/resources/conf/application-mgt.xml) | Initial state, permissions, successors and ApplicableTypes; effective deployment overrides are unknown |
| [Publisher implementation](../../../../proprietary-commons/components/extensions/application-mgt/io.entgra.proprietary.commons.extension.application.mgt.publisher.api/src/main/java/io/entgra/proprietary/commons/extension/application/mgt/publisher/api/impl/ApplicationManagementPublisherAPIImpl.java) | createEntApp 185; createEntAppRelease 281; addLifecycleState 738; validation-before-create 948; exception/status/entity distinctions |
| [Publisher interface](../../../../proprietary-commons/components/extensions/application-mgt/io.entgra.proprietary.commons.extension.application.mgt.publisher.api/src/main/java/io/entgra/proprietary/commons/extension/application/mgt/publisher/api/ApplicationManagementPublisherAPI.java) | createEntApp/createEntAppRelease/addLifecycleState route and scope declarations; distinct from core destination-state permissions |

Existing configured tests remain listed in [Tests](#tests). In particular,
LifecycleManagementTest checks flags and successor membership, not the complete
isValidStateChange permission predicate, state/history writes or race behavior.
New [enterprise cases](verification-matrix.md#enterprise-creation-and-publication-cases)
have source mappings and planned fixtures; no execution result is inferred here.
