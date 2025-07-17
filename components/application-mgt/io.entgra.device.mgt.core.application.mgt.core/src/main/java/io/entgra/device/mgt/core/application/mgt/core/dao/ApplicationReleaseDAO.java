/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.application.mgt.core.dao;

import io.entgra.device.mgt.core.application.mgt.common.Rating;
import io.entgra.device.mgt.core.application.mgt.common.ReleaseSearchFilter;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

/**
 * This is responsible for ApplicationDTO Release related DAO operations.
 */
public interface ApplicationReleaseDAO {

    /**
     * To create an ApplicationDTO release.
     *
     * @param applicationRelease ApplicationDTO Release that need to be created.
     * @return Unique ID of the relevant release.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationReleaseDTO createRelease(ApplicationReleaseDTO applicationRelease, int appId, int tenantId) throws
            ApplicationManagementDAOException;

    /**
     * To update an ApplicationDTO release.
     *
     * @param applicationRelease ApplicationReleaseDTO that need to be updated.
     * @param tenantId           Id of the tenant
     * @return the updated ApplicationDTO Release
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception
     */
    ApplicationReleaseDTO updateRelease(ApplicationReleaseDTO applicationRelease, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * To update an ApplicationDTO release.
     * @param uuid UUID of the ApplicationReleaseDTO that need to be updated.
     * @param rating given stars for the application.
     * @param ratedUsers number of users who has rated for the application release.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception
     */
    void updateRatingValue(String uuid, double rating, int ratedUsers) throws ApplicationManagementDAOException;

    /**
     * To retrieve rating of an application release.
     *
     * @param uuid UUID of the application Release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    Rating getReleaseRating(String uuid, int tenantId) throws ApplicationManagementDAOException;

    List<Double> getReleaseRatings(String uuid, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To delete a particular release.
     *
     * @param id      ID of the ApplicationDTO which the release need to be deleted.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    void deleteRelease(int id) throws ApplicationManagementDAOException;

    void deleteReleases(List<Integer> applicationReleaseIds) throws ApplicationManagementDAOException;

    ApplicationReleaseDTO getReleaseByUUID(String uuid, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Retrieves a list of application release details based on the given list of UUIDs for a specific tenant.
     *
     * @param uuids a list of UUIDs corresponding to the application releases
     * @param tenantId the tenant ID within which to search for the releases
     * @return a list of {@link ApplicationReleaseDTO} objects matching the provided UUIDs
     * @throws ApplicationManagementDAOException if an error occurs while accessing the database
     */
    List<ApplicationReleaseDTO> getReleasesByUUIDs(List<String> uuids, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Retrieves the application release details for the specified version within a given tenant.
     *
     * @param version the version string of the application release to retrieve
     * @param tenantId the tenant ID within which to search for the release
     * @return the {@link ApplicationReleaseDTO} corresponding to the given version, or {@code null} if not found
     * @throws ApplicationManagementDAOException if an error occurs while accessing the database
     */
    ApplicationReleaseDTO getReleaseByVersion(int applicationId, String version, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To verify whether application release exist or not for the given app release version.
     *
     * @param hashVal Hash value of the application release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    boolean verifyReleaseExistenceByHash(String hashVal, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * To verify whether application release exist or not for the given app release version.
     *
     * @param releaseUuid ID of the application.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    String getPackageName(String releaseUuid, int tenantId) throws ApplicationManagementDAOException;


    String getReleaseHashValue(String uuid, int tenantId) throws ApplicationManagementDAOException;

    /***
     *
     * @param packageName Application release package name
     * @param tenantId Tenant ID
     * @return True if application release package name already exist in the IoT server, Otherwise returns False.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    boolean isActiveReleaseExisitForPackageName(String packageName, int tenantId, String inactiveState)
            throws ApplicationManagementDAOException;

    boolean hasExistInstallableAppRelease(String releaseUuid, String installableStateName, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * This method is responsible to return list of application releases which contains one of the
     * providing package name.
     *
     * @param packages List of package names
     * @param tenantId Tenant Id
     * @return List of application releases {@link ApplicationReleaseDTO}
     * @throws ApplicationManagementDAOException if error occurred while getting application releases from the DB.
     */
    List<ApplicationReleaseDTO> getReleaseByPackages(List<String> packages, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * Delete Application releases of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteReleasesByTenant(int tenantId) throws ApplicationManagementDAOException;

    /**
     * Retrieves a list of application releases for the given application ID that have a version greater than the specified version.
     * Filters results by release status and release type for the given tenant.
     *
     * @param appId the ID of the application
     * @param version the base version to compare against; only releases with a higher version will be returned
     * @param status the status to filter application releases (e.g., "RELEASE_READY")
     * @param appReleaseType the type of the release (e.g., "TEST", "PRODUCTION"); can be {@code null} to fetch all types
     * @param tenantId the tenant ID
     * @return a list of {@link ApplicationReleaseDTO} objects representing the filtered application releases
     * @throws ApplicationManagementDAOException if an error occurs while retrieving data from the database
     */
    List<ApplicationReleaseDTO> getAppReleasesAfterVersion(int appId, String version, String status, String appReleaseType, int tenantId) throws ApplicationManagementDAOException;

    List<ApplicationReleaseDTO> getAppReleasesBeforeVersion(int appId, String version, String status, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Retrieves the version of the latest installed application release for the given application ID and tenant.
     *
     * @param appId the ID of the application
     * @param tenantId the tenant ID
     * @return the version of the installed application release
     * @throws ApplicationManagementDAOException if an error occurs while retrieving the release version
     */
    String getInstalledReleaseVersionByApp(int deviceId, int appId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Retrieves a list of application releases for a given application ID filtered by release status and release type
     * within the specified tenant.
     *
     * @param appId the ID of the application
     * @param status the status of the application releases to filter by (e.g., "RELEASE_READY")
     * @param appReleaseType the type of the release (e.g., "TEST", "PRODUCTION"); can be {@code null} to include all types
     * @param tenantId the tenant ID for which the releases are retrieved
     * @return a list of {@link ApplicationReleaseDTO} objects matching the given criteria
     * @throws ApplicationManagementDAOException if an error occurs while accessing the database
     */
    List<ApplicationReleaseDTO> getReleasesByAppAndStatus(int appId, String status, String appReleaseType, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Get application releases based on {@link ReleaseSearchFilter}
     *
     * @param releaseSearchFilter {@link ReleaseSearchFilter}
     * @param tenantId            Tenant ID
     * @return List of {@link ApplicationReleaseDTO}
     * @throws ApplicationManagementDAOException Throws when error encountered while fetching releases.
     */
    List<ApplicationReleaseDTO> getReleases(ReleaseSearchFilter releaseSearchFilter, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Get application releases matching count based on {@link ReleaseSearchFilter}
     *
     * @param releaseSearchFilter {@link ReleaseSearchFilter}
     * @param tenantId            Tenant ID
     * @return Full matching count of releases.
     * @throws ApplicationManagementDAOException Throws when error encountered while fetching releases.
     */
    int getReleasesCount(ReleaseSearchFilter releaseSearchFilter, int tenantId) throws ApplicationManagementDAOException;

}
