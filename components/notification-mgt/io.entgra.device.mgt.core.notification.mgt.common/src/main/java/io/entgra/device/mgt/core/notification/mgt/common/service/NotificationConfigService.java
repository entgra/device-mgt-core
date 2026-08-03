/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.notification.mgt.common.service;

import io.entgra.device.mgt.core.notification.mgt.common.beans.ArchivePeriod;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;

import java.util.List;

public interface NotificationConfigService {

    /**
     * Sets the default notification archival metadata values for all notifications.
     * This method updates the metadata entry identified by the {@code NOTIFICATION_CONFIG_META_KEY}
     * to include or overwrite the default archival type and archival period. If the metadata does
     * not already exist, a new entry will be created. If it exists, the values will be updated
     * while preserving existing notification configurations.
     * @param defaultType  The default archival type to set (e.g., "DB", "ELK").
     * @param defaultAfter The default archival period to set (e.g., "12 months", "6 weeks").
     * @throws NotificationConfigurationServiceException If an error occurs while updating or creating the metadata.
     */
    void setDefaultNotificationArchiveMetadata(String defaultType, ArchivePeriod defaultAfter)
            throws NotificationConfigurationServiceException;

    /**
     * Retrieve the  notification configurations for a tenant.
     *
     * @return {@link NotificationConfigurationList addNotificationConfigContext}
     * @throws NotificationConfigurationServiceException Throws when error occurred while retrieving notifications.
     */
    NotificationConfigurationList addNotificationConfigContext(NotificationConfigurationList configurations)
            throws NotificationConfigurationServiceException;

    /**
     * Deletes the notification configuration context associated with the given configuration ID.
     *
     * @param configID The unique identifier of the notification configuration to be deleted.
     * @throws NotificationConfigurationServiceException If an error occurs during the deletion process.
     */
    void deleteNotificationConfigContext(int configID) throws NotificationConfigurationServiceException;

    /**
     * Updates the context of an existing notification configuration.
     *
     * @param updatedConfig The updated notification configuration object containing new context information.
     * @throws NotificationConfigurationServiceException If an error occurs while updating the configuration context.
     */
    void updateNotificationConfigContext(NotificationConfig updatedConfig)
            throws NotificationConfigurationServiceException;

    /**
     * Delete Notification Configurations for a tenant.
     *
     * @return {@link Object < Notification Configuration>}
     * @throws NotificationConfigurationServiceException Throws when error occurred while retrieving notifications.
     */
    void deleteNotificationConfigurations() throws NotificationConfigurationServiceException;

    /**
     * Retrieve the  list of notification configurations for a tenant.
     *
     * @return {@link List < Notification Configurations>}
     * @throws NotificationConfigurationServiceException Throws when error occurred while retrieving notifications.
     */
    NotificationConfigurationList getNotificationConfigurations() throws NotificationConfigurationServiceException;

    /**
     * Retrieve a notification Configuration By Config ID for a tenant.
     *
     * @return {@link Object < Notification Configuration>}
     * @throws NotificationConfigurationServiceException Throws when error occurred while retrieving notifications.
     */
    NotificationConfig getNotificationConfigByID(int configID) throws NotificationConfigurationServiceException;

    /**
     * Retrieves a filtered and paginated list of notification configurations based on the provided criteria.
     * This method applies filtering on the name, type, and code of the notification configurations,
     * and returns a sublist based on the given offset and limit for pagination.
     *
     * @param name   Optional filter to match configuration names (case-insensitive with partial match).
     * @param type   Optional filter to match configuration type (case-insensitive with exact match).
     * @param code   Optional filter to match configuration codes (case-insensitive with partial match).
     * @param deviceType   Optional filter to match device type (case-insensitive with partial match).
     * @param offset The starting index of the paginated result.
     * @param limit  The maximum number of configurations to return in the result.
     * @return A {@link NotificationConfigurationList} containing the filtered and paginated configurations.
     * @throws NotificationConfigurationServiceException If an error occurs during metadata retrieval or processing.
     */
    NotificationConfigurationList getFilteredNotificationConfigurations(String name, String type, String code,
                                                                        String deviceType, int offset, int limit)
            throws NotificationConfigurationServiceException;

    /**
     * Checks whether a notification configuration exists for the given device type and operation code.
     *
     * @param deviceType the type of device (e.g., android, ios, windows).
     * @param code the operation or task code associated with the notification.
     * @return {@code true} if a matching notification configuration already exists,
     *         {@code false} otherwise.
     * @throws NotificationConfigurationServiceException if an error occurs while checking the configuration.
     */
    boolean configExists(String deviceType, String code) throws NotificationConfigurationServiceException;
}
