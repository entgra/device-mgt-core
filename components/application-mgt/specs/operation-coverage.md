# Public Service Operation Coverage

## Scope and interpretation

Service inventory: 156 method declarations across 10 interfaces in
application.mgt.common.services, including overloads. Sources are linked per
interface. This is an inventory of declared service operations, not every public
implementation helper, consumer behavior, DAO method, configuration branch or device
agent capability. Those require additional reviews in their owning contracts.

Each row points to the starting specification and related requirements. **Partial**
means an owning document exists, but complete input/output/security/error coverage
for this exact signature has not been established. No row is marked fully covered
merely because its method name appears in a spec. Even the newly detailed contracts
retain caller-boundary and edge-case work in APP-GAP-14 through APP-GAP-18.

Source line locations and exact signatures are discovery aids. Update this inventory
when interfaces change; preserve requirement IDs. All acceptance execution remains
pending in the [verification matrix](verification-matrix.md). Implementation-only
helpers such as MSI metadata synchronization are covered through their calling
operations and [platform artifacts](storage/platform-artifacts.md).

## ApplicationManager

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/ApplicationManager.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `createEntAppRelease(int appId, EntAppReleaseWrapper releaseWrapper, boolean isPublished)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `createWebAppRelease(int appId, WebAppReleaseWrapper releaseWrapper, boolean isPublished)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `createPubAppRelease(int appId, PublicAppReleaseWrapper releaseWrapper, boolean isPublished)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `createCustomAppRelease(int appId, CustomAppReleaseWrapper releaseWrapper, boolean isPublished)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `uploadReleaseArtifactIfExist(T app)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `validatePublicAppReleasePackageName(String packageName)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `addAppDataIntoDB(ApplicationDTO applicationDTO, boolean isPublished)` | [Contract](creation/creation-workflow.md); APP-CRT-001/003 | Partial |
| `createApplication(T app, boolean isPublished)` | [Contract](creation/creation-workflow.md); APP-CRT-001/004 | Partial |
| `addAppToFavourites(int appId)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `removeAppFromFavourites(int appId)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `isFavouriteApp(int appId)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `isExistingAppName(String appName, String deviceTypeName)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `updateApplication(int applicationId, ApplicationUpdateWrapper applicationUpdateWrapper)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `deleteApplication(int applicationId)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `retireApplication(int applicationId)` | [Contract](catalog/retirement.md); APP-CAT-005 | Partial |
| `deleteApplicationRelease(String releaseUuid)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `getFavouriteApplications(Filter filter)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `deleteApplicationArtifacts(List<String> directoryPaths)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `getApplications(Filter filter)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `isHideableApp(List<ApplicationReleaseDTO> applicationReleaseDTOs)` | [Contract](lifecycle/lifecycle.md); APP-LC-001/003 | Partial |
| `isDeletableApp(List<ApplicationReleaseDTO> applicationReleaseDTOs)` | [Contract](lifecycle/lifecycle.md); APP-LC-001/003 | Partial |
| `getApplications(List<String> packageNames)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `createRelease(ApplicationDTO applicationDTO, ApplicationReleaseDTO applicationReleaseDTO, ApplicationType type, boolean isPublished)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `getApplication(int applicationId)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `getSubscribedAppsOfDevice(int deviceId, PaginationRequest request)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `getApplicationById(int id, String state)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `getApplicationByUuid(String uuid)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `getApplicationByUuid(String uuid, String state)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `getLifecycleStateChangeFlow(String releaseUuid)` | [Contract](lifecycle/lifecycle.md); APP-LC-001/003 | Partial |
| `changeLifecycleState(String releaseUuid, LifecycleChanger lifecycleChanger)` | [Contract](lifecycle/lifecycle.md); APP-LC-001/003 | Partial |
| `changeLifecycleState(ApplicationReleaseDTO applicationReleaseDTO, LifecycleChanger lifecycleChanger, String applicationType)` | [Contract](lifecycle/lifecycle.md); APP-LC-001/003 | Partial |
| `updateApplicationImageArtifact(String uuid, ApplicationArtifact applicationArtifact)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `updateApplicationArtifact(String deviceType, String uuid, ApplicationArtifact applicationArtifact)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `updateEntAppRelease(String releaseUuid, EntAppReleaseWrapper entAppReleaseWrapper)` | [Contract](releases/release-updates.md); APP-REL-006 | Partial |
| `updatePubAppRelease(String releaseUuid, PublicAppReleaseWrapper publicAppReleaseWrapper)` | [Contract](releases/release-updates.md); APP-REL-006 | Partial |
| `updateWebAppRelease(String releaseUuid, WebAppReleaseWrapper webAppReleaseWrapper)` | [Contract](releases/release-updates.md); APP-REL-006 | Partial |
| `updateCustomAppRelease(String releaseUuid, CustomAppReleaseWrapper customAppReleaseWrapper)` | [Contract](releases/release-updates.md); APP-REL-006 | Partial |
| `validateAppCreatingRequest(T param)` | [Contract](creation/creation-workflow.md); APP-CRT-001/004 | Partial |
| `validateReleaseCreatingRequest(T releases, String deviceType)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `validateEntAppReleaseCreatingRequest(EntAppReleaseWrapper releaseWrapper, String deviceType)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `validateCustomAppReleaseCreatingRequest(CustomAppReleaseWrapper releaseWrapper, String deviceType)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `validateWebAppReleaseCreatingRequest(WebAppReleaseWrapper releaseWrapper)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `validatePublicAppReleaseCreatingRequest(PublicAppReleaseWrapper releaseWrapper, String deviceType)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `validateImageArtifacts(Base64File iconFile, List<Base64File> screenshots)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `validateBase64File(Base64File file)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `validateImageArtifacts(Attachment iconFile, Attachment bannerFile, List<Attachment> attachmentList)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `validateBinaryArtifact(Base64File binaryFile)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `validateBinaryArtifact(Attachment binaryFile)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `addApplicationCategories(List<String> categories)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `getRegisteredTags()` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `getRegisteredCategories()` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `deleteApplicationTag(int appId, String tagName)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `deleteTag(String tagName)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `deleteUnusedTag(String tagName)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `updateTag(String oldTagName, String newTagName)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `addTags(List<String> tags)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `addApplicationTags(int appId, List<String> tags)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `addCategories(List<String> categories)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `deleteCategory(String categoryName)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `updateCategory(String oldCategoryName, String newCategoryName)` | [Contract](catalog/catalog.md); APP-CAT-001/004 | Partial |
| `getInstallableLifecycleState(String applicationType)` | [Contract](lifecycle/lifecycle.md); APP-LC-001/003 | Partial |
| `getInstallableLifecycleStates()` | [Contract](lifecycle/lifecycle.md); APP-LC-001/003 | Partial |
| `checkSubDeviceIdsForOperations(int operationId, int deviceId)` | [Contract](subscriptions/subscriptions.md); APP-SUB-003 | Partial |
| `updateSubStatus(int deviceId, List<Integer> operationId, String status)` | [Contract](subscriptions/subscriptions.md); APP-SUB-003 | Partial |
| `updateSubsStatus(int deviceId, int operationId, String status)` | [Contract](subscriptions/subscriptions.md); APP-SUB-003 | Partial |
| `getPlistArtifact(String uuid)` | [Contract](storage/platform-artifacts.md); APP-STO-005 | Partial |
| `getReleaseByPackageNames(List<String> packageIds)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |
| `updateAppIconInfo(ApplicationRelease applicationRelease, String oldPackageName)` | [Contract](storage/storage.md); APP-STO-001/002 | Partial |
| `deleteApplicationDataOfTenant(int tenantId)` | [Contract](runtime/runtime.md); APP-RUN-003 | Partial |
| `deleteApplicationDataByTenantId(int tenantId)` | [Contract](runtime/runtime.md); APP-RUN-003 | Partial |
| `deleteApplicationArtifactsByTenantId(int tenantId)` | [Contract](runtime/runtime.md); APP-RUN-003 | Partial |
| `getApplicationReleaseVersions(String uuid)` | [Contract](firmware/firmware.md); APP-FW-001/002 | Partial |
| `getAvailableFirmwaresForDevice(String deviceId, String currentVersion)` | [Contract](firmware/firmware.md); APP-FW-001/002 | Partial |
| `getDevicesByFirmwareMatchType(String uuid, FirmwareMatchType matchType, int groupId , io.entgra.device.mgt.core.application.mgt.common.PaginationRequest paginationRequest)` | [Contract](firmware/firmware.md); APP-FW-001/002 | Partial |
| `getAvailableDeviceFirmwareModels(String deviceType)` | [Contract](firmware/firmware.md); APP-FW-001/002 | Partial |
| `getApplicationReleases(int appId, ReleaseSearchFilter releaseSearchFilter)` | [Contract](releases/release-search.md); APP-REL-005 | Partial |
| `validateReleaseBinaryFileHash(String hash)` | [Contract](releases/release-management.md); APP-REL-001/004 | Partial |

## ApplicationStorageManager

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/ApplicationStorageManager.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `uploadImageArtifacts(ApplicationReleaseDTO applicationRelease, InputStream iconFile, InputStream bannerFile, List<InputStream> screenshots, int tenantId)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `getAppInstallerData(InputStream binaryFile, String deviceType)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `uploadReleaseArtifact(ApplicationReleaseDTO applicationRelease, String deviceType, InputStream binaryFile, int tenantId)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `copyImageArtifactsAndDeleteInstaller(String deletingAppHashValue, ApplicationReleaseDTO applicationReleaseDTO, int tenantId)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `deleteAppReleaseArtifact(String appReleaseHashVal, String folderName, String fileName, int tenantId)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `deleteAllApplicationReleaseArtifacts(List<String> directoryPaths, int tenantId)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `getFileStream(String hashVal, String folderName, String fileName, int tenantId)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `getFileStream(String deviceType, String tenantDomain)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `getMD5(InputStream inputStream)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `deleteAppFolderOfTenant(int tenantId)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `getAbsolutePathOfFile(String hashVal, String folderName, String fileName, int tenantId)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |

## AppmDataHandler

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/AppmDataHandler.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `getLifecycleConfiguration()` | [Contract](lifecycle/lifecycle.md); APP-LC-001 | Partial |
| `getLifecycleConfiguration(String applicationType)` | [Contract](lifecycle/lifecycle.md); APP-LC-001 | Partial |
| `getArtifactStream(int tenantId, String appHashValue, String folderName, String artifactName)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `getAgentStream(String tenantDomain, String deviceType)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |

## DeviceConnector

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/DeviceConnector.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `sendOperationToDevice(AppOperation appOperation, DeviceIdentifier deviceIdentifier)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001 | Partial |
| `sendOperationToGroup(AppOperation appOperation, String groupID)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001 | Partial |
| `sendOperationToUser(AppOperation appOperation, List<String> userList)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001 | Partial |
| `sendOperationToRole(AppOperation appOperation, String role)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001 | Partial |

## FileDownloaderService

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/FileDownloaderService.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `download(URL downloadUrl)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |

## FileTransferService

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/FileTransferService.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `generateUploadLink(FileMetaEntry fileMetaEntry)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `resolve(String artifactHolder, InputStream chunk)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `writeChunk(ChunkDescriptor chunkDescriptor)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `isExistsOnLocal(URL downloadUrl)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |
| `resolve(URL downloadUrl)` | [Contract](storage/storage.md); APP-STO-001/003 | Partial |

## ReviewManager

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/ReviewManager.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `addReview(ReviewWrapper reviewWrapper, String uuid, boolean allowMultipleReviews)` | [Contract](reviews/reviews.md); APP-REV-001/002 | Partial |
| `addReplyComment(ReviewWrapper reviewWrapper, String uuid, int parentReviewId)` | [Contract](reviews/reviews.md); APP-REV-001/002 | Partial |
| `getAllReleaseReviews(PaginationRequest request, String uuid)` | [Contract](reviews/reviews.md); APP-REV-001/002 | Partial |
| `getAllAppReviews(PaginationRequest request, String uuid)` | [Contract](reviews/reviews.md); APP-REV-001/002 | Partial |
| `getAllAppReviewsOfUser(PaginationRequest request, String uuid)` | [Contract](reviews/reviews.md); APP-REV-001/002 | Partial |
| `deleteReview(String uuid, int reviewId, boolean isPriviledgedUser)` | [Contract](reviews/reviews.md); APP-REV-001/002 | Partial |
| `updateReview(ReviewWrapper updatingReview, int reviewId, String uuid, boolean isPrivilegedUser)` | [Contract](reviews/reviews.md); APP-REV-001/002 | Partial |
| `getAppReleaseRating(String appReleaseUuid)` | [Contract](reviews/reviews.md); APP-REV-001/002 | Partial |
| `getAppRating(String appReleaseUuid)` | [Contract](reviews/reviews.md); APP-REV-001/002 | Partial |

## SPApplicationManager

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/SPApplicationManager.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `detachSPApplications(int identityServerId, String spUID, List<Integer> appIds)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `attachSPApplications(int identityServerId, String spUID, List<Integer> appIds)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `getIdentityServerResponse(int identityServerId)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `getIdentityServers()` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `createIdentityServer(IdentityServerDTO identityServerDTO)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `updateIdentityServer(IdentityServerDTO updateIdentityServerDTO, int id)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `deleteIdentityServer(int id)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `isIdentityServerNameExist(String name)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `isIdentityServerUrlExist(String url)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `retrieveSPApplicationFromIdentityServer(int identityServerId, Integer offset, Integer limit)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `getSPApplications(int identityServerId, String spUID)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `createSPApplication(T app, int identityServerId, String spId, boolean isPublished)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `validateAttachAppsRequest(int identityServerId, String serviceProviderId, List<Integer> appIds)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `validateDetachAppsRequest(int identityServerId, String spId, List<Integer> appIds)` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |
| `getIdentityServiceProviders()` | [Contract](integrations/integrations.md); APP-INT-001 | Partial |

## SubscriptionManager

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/SubscriptionManager.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `performBulkAppOperation(String applicationUUID, List<T> params, String subType, String action, Properties properties)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001/003 | Partial |
| `performBulkAppOperation(String applicationUUID, List<T> params, String subType, String action, Properties properties, boolean isOperationReExecutingDisabled)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001/003 | Partial |
| `createScheduledSubscription(ScheduledSubscriptionDTO subscriptionDTO)` | [Contract](subscriptions/subscriptions.md); APP-SCH-001 | Partial |
| `cleanScheduledSubscriptions()` | [Contract](subscriptions/subscriptions.md); APP-SCH-001 | Partial |
| `checkAppSubscription(int id, String packageName)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001/003 | Partial |
| `getPendingScheduledSubscription(String taskName)` | [Contract](subscriptions/subscriptions.md); APP-SCH-001 | Partial |
| `updateScheduledSubscriptionStatus(int id, ExecutionStatus status)` | [Contract](subscriptions/subscriptions.md); APP-SCH-001 | Partial |
| `performEntAppSubscription(String applicationUUID, List<T> params, String subType, String action, boolean requiresUpdatingExternal)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001/003 | Partial |
| `installAppsForDevice(DeviceIdentifier deviceIdentifier, List<App> apps)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001/003 | Partial |
| `updateSubscriptionStatus(int deviceId, int subId, String status, String deviceIdentifier)` | [Contract](subscriptions/subscriptions.md); APP-SUB-001/003 | Partial |
| `getAppInstalledDevices(PaginationRequest request, String appUUID)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |
| `getAppInstalledSubscribers(int offsetValue, int limitValue, String appUUID, String subType, Boolean uninstalled, String searchName)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |
| `getAppSubscriptionDetails(PaginationRequest request, String appUUID, String actionStatus, String action, String installedVersion)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |
| `getAppInstalledSubscribeDevices(PaginationRequest request, String appUUID, String subType, String subTypeName)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |
| `getOperationAppDetails(String id)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |
| `getSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |
| `getStatusBaseSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |
| `getStatistics(SubscriptionInfo subscriptionInfo)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |
| `getSubscriptionOperationsByUUIDAndDeviceID(int deviceId, String uuid)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |
| `getSubscriptionCountsByUUID(String uuid)` | [Contract](subscriptions/reporting.md); APP-SUB-004/005 | Partial |

## VPPApplicationManager

[Interface source](../io.entgra.device.mgt.core.application.mgt.common/src/main/java/io/entgra/device/mgt/core/application/mgt/common/services/VPPApplicationManager.java)

| Operation signature | Owning spec / related requirements | Coverage |
| --- | --- | --- |
| `addUser(VppUserDTO userDTO)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |
| `getUserByDMUsername(String emmUsername)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |
| `updateUser(VppUserDTO userDTO)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |
| `syncUsers(String clientId)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |
| `syncAssets(int nextPageIndex)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |
| `getAssetByAppId(int appId)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |
| `callVPPBackend(String url, String payload, String accessToken, String method)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |
| `addAssociation(VppAssetDTO asset, List<VppUserDTO> vppUsers)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |
| `getAssociation(int assetId, int userId)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |
| `getUserAssociation(String adamId, String emmUsername)` | [Contract](integrations/integrations.md); APP-INT-002 | Partial |

## Closing a partial row

Inspect the implementation and callers for the signature; specify validation,
tenant/permission boundaries, success/empty/null outputs, exceptions, mutations,
transaction/async behavior and compatibility restrictions. Link acceptance scenarios
and retain anomalies in the gap register. Mark source-described only after this
review, and runtime-verified only with dated execution evidence. Explicitly excluded
operations need a reason and owner rather than being dropped from the denominator.
