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

package io.entgra.device.mgt.core.notification.mgt.core.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationHelper;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationConfigService;
import io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class NotificationConfigServiceImpl implements NotificationConfigService {
    private static final Log log = LogFactory.getLog(NotificationConfigServiceImpl.class);
    private static final Gson gson = new Gson();
    private final MetadataManagementService metaDataService =
            NotificationManagementDataHolder.getInstance().getMetaDataManagementService();

    private int generateNextId(List<NotificationConfig> existingConfigs) {
        if (existingConfigs == null || existingConfigs.isEmpty()) {
            return 1;
        }
        return existingConfigs.stream()
                .mapToInt(NotificationConfig::getId)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public void setDefaultNotificationArchiveMetadata(String defaultType, String defaultAfter)
            throws NotificationConfigurationServiceException {
        try {
            Metadata existingMetadata =
                    metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            NotificationConfigurationList configList;
            if (existingMetadata != null && existingMetadata.getMetaValue() != null) {
                configList = gson.fromJson(existingMetadata.getMetaValue(), NotificationConfigurationList.class);
            } else {
                configList = new NotificationConfigurationList();
            }
            if (configList.getNotificationConfigurations() == null) {
                configList.setNotificationConfigurations(new ArrayList<>());
            }
            configList.setDefaultArchiveType(defaultType);
            configList.setDefaultArchiveAfter(defaultAfter);
            Metadata metadata = new Metadata();
            metadata.setMetaKey(Constants.NOTIFICATION_CONFIG_META_KEY);
            metadata.setMetaValue(gson.toJson(configList));
            if (existingMetadata != null) {
                metaDataService.updateMetadata(metadata);
            } else {
                metaDataService.createMetadata(metadata);
            }
        } catch (MetadataManagementException e) {
            String msg = "Failed to set default notification archive metadata.";
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        }
    }

    /**
     * Adds new notification configuration contexts to the metadata storage.
     *
     * If a configuration with the same ID already exists, it will be skipped to avoid duplication.
     * If metadata already exists for notification configurations, it will be updated.
     * otherwise, new metadata will be created.
     * @param newConfigurations A list of new notification configurations to be added.
     * @throws NotificationConfigurationServiceException If the input is invalid or if an error occurs while
     *                                                   accessing or updating metadata.
     */
    @Override
    public void addNotificationConfigContext(NotificationConfigurationList newConfigurations)
            throws NotificationConfigurationServiceException {
        if (newConfigurations == null || newConfigurations.isEmpty()) {
            throw new NotificationConfigurationServiceException("Cannot add empty configurations");
        }
        try {
            Metadata existingMetadata = metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            NotificationConfigurationList existingConfigurations = new NotificationConfigurationList();
            if (existingMetadata != null && existingMetadata.getMetaValue() != null) {
                Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
                existingConfigurations = gson.fromJson(existingMetadata.getMetaValue(), listType);
            }
            List<NotificationConfig> merged = new ArrayList<>();
            if (existingConfigurations.getNotificationConfigurations() != null) {
                merged.addAll(existingConfigurations.getNotificationConfigurations());
            }
            for (NotificationConfig newConfig : newConfigurations.getNotificationConfigurations()) {
                if (newConfig.getId() == 0) {
                    newConfig.setId(generateNextId(merged));
                }
                boolean isDuplicateId =
                        merged.stream().anyMatch(c -> c.getId() == newConfig.getId());
                boolean isDuplicateCode =
                        merged.stream().anyMatch(c -> c.getCode().equals(newConfig.getCode()));
                if (isDuplicateId) {
                    throw new NotificationConfigurationServiceException("Duplicate ID " + newConfig.getId());
                }
                if (isDuplicateCode) {
                    throw new NotificationConfigurationServiceException("Duplicate Code " + newConfig.getCode());
                }
                merged.add(newConfig);
            }
            NotificationConfigurationList updatedList = new NotificationConfigurationList();
            updatedList.setNotificationConfigurations(merged);
            updatedList.setDefaultArchiveType(existingConfigurations.getDefaultArchiveType());
            updatedList.setDefaultArchiveAfter(existingConfigurations.getDefaultArchiveAfter());
            Metadata newMetadata = new Metadata();
            newMetadata.setMetaKey(Constants.NOTIFICATION_CONFIG_META_KEY);
            newMetadata.setMetaValue(gson.toJson(updatedList));
            if (existingMetadata != null) {
                metaDataService.updateMetadata(newMetadata);
            } else {
                metaDataService.createMetadata(newMetadata);
            }
        } catch (MetadataManagementException e) {
            String msg = "Error creating or updating metadata: " + e.getMessage();
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        }
    }

    /**
     * Deletes a specific notification configuration from the Metadata context for a given tenant.
     *
     * @param configId The unique identifier (operationCode) of the notification configuration to be deleted.
     * @throws NotificationConfigurationServiceException If no configuration is found with the specified operationCode, or
     * if any error occurs during the database transaction or processing
     * This method retrieves the existing notification configuration context for the given tenant, removes the
     * configuration matching the provided operationCode, and updates the Metadata context with the remaining configurations.
     */
    @Override
    public void deleteNotificationConfigContext(int configId) throws NotificationConfigurationServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting notification configuration with ID: " + configId);
        }
        try {
            Metadata existingMetadata = metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata != null && existingMetadata.getMetaValue() != null) {
                String metaValue = existingMetadata.getMetaValue();
                Gson gson = new Gson();
                Type listType = new TypeToken<NotificationConfigurationList>() {
                }.getType();
                NotificationConfigurationList configList = gson.fromJson(metaValue, listType);
                if (configList != null && configList.getNotificationConfigurations() != null) {
                    boolean removed = false;
                    Iterator<NotificationConfig> iterator = configList.getNotificationConfigurations().iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().getId() == configId) {
                            iterator.remove();
                            removed = true;
                            break;
                        }
                    }
                    if (removed) {
                        NotificationConfigurationList updatedList = new NotificationConfigurationList();
                        updatedList.setNotificationConfigurations(configList.getNotificationConfigurations());
                        updatedList.setDefaultArchiveType(configList.getDefaultArchiveType());
                        updatedList.setDefaultArchiveAfter(configList.getDefaultArchiveAfter());
                        String updatedData = gson.toJson(updatedList);
                        Metadata updatedMetadata = new Metadata();
                        updatedMetadata.setMetaKey(Constants.NOTIFICATION_CONFIG_META_KEY);
                        updatedMetadata.setMetaValue(updatedData);
                        metaDataService.updateMetadata(updatedMetadata);
                        if (log.isDebugEnabled()) {
                            log.debug("Successfully deleted notification configuration with ID: " + configId);
                        }
                    } else {
                        String msg = "Notification configuration with ID: " + configId + " not found.";
                        log.warn(msg);
                        throw new NotificationConfigurationServiceException(msg);
                    }
                } else {
                    String msg = "Invalid format in notification configuration metadata.";
                    log.warn(msg);
                    throw new NotificationConfigurationServiceException(msg);
                }
            } else {
                String msg = "Notification configuration metadata not found.";
                log.warn(msg);
                throw new NotificationConfigurationServiceException(msg);
            }
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while retrieving notification configuration metadata.";
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        }
    }

    /**
     * Updates an existing notification configuration or adds a new configuration to the Metadata context for a given tenant.
     *
     * @param updatedConfig The notification configuration to be updated or added.
     * If a configuration with the same operationCode exists, it will be updated; otherwise, it will be added as a new entry.
     * @throws NotificationConfigurationServiceException If any error occurs during the database transaction or processing.
     * This method retrieves the existing notification configuration context for the given tenant. If a configuration with the same
     * operationCode as the provided configuration exists, it updates that configuration with the new details. Otherwise, it appends
     * the provided configuration as a new entry. The updated configurations are then serialized and saved back to the Metadata context.
     **/
    @Override
    public void updateNotificationConfigContext(NotificationConfig updatedConfig)
            throws NotificationConfigurationServiceException {
        if (updatedConfig == null || updatedConfig.getId() <= 0) {
            throw new NotificationConfigurationServiceException("Invalid configuration");
        }
        try {
            Metadata existingMetadata =
                    metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata == null || existingMetadata.getMetaValue() == null) {
                throw new NotificationConfigurationServiceException("No configurations found");
            }
            Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
            NotificationConfigurationList existingList = gson.fromJson(existingMetadata.getMetaValue(), listType);
            List<NotificationConfig> configList = existingList.getNotificationConfigurations();
            boolean found = false;
            for (int i = 0; i < configList.size(); i++) {
                if (configList.get(i).getId() == updatedConfig.getId()) {
                    configList.set(i, updatedConfig);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new NotificationConfigurationServiceException("Configuration not found");
            }
            NotificationConfigurationList updatedList = new NotificationConfigurationList();
            updatedList.setNotificationConfigurations(configList);
            updatedList.setDefaultArchiveType(existingList.getDefaultArchiveType());
            updatedList.setDefaultArchiveAfter(existingList.getDefaultArchiveAfter());
            existingMetadata.setMetaValue(gson.toJson(updatedList));
            metaDataService.updateMetadata(existingMetadata);
        } catch (MetadataManagementException e) {
            String msg = "Error updating metadata: " + e.getMessage();
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        }
    }

    /**
     * Deletes all notification configuration metadata for the current tenant.
     *
     * @throws NotificationConfigurationServiceException If metadata is not found or if an error occurs during deletion.
     */
    @Override
    public void deleteNotificationConfigurations() throws NotificationConfigurationServiceException {
        try {
            metaDataService.deleteMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
        } catch (NoSuchElementException e) {
            String msg = "No Meta Data found for Tenant ID";
            log.error(msg);
            throw new NotificationConfigurationServiceException(msg, e);
        } catch (MetadataManagementException e) {
            String message = "Unexpected error occurred while deleting notification configurations for tenant ID.";
            log.error(message, e);
            throw new NotificationConfigurationServiceException(message, e);
        }
    }

    /**
     * Retrieves the list of notification configurations stored in metadata for the current tenant.
     *
     * @return A {@link NotificationConfigurationList} containing all configured notifications.
     * @throws NotificationConfigurationServiceException If metadata retrieval fails, the metadata is missing,
     *                                                   or deserialization fails.
     */
    @Override
    public NotificationConfigurationList getNotificationConfigurations()
            throws NotificationConfigurationServiceException {
        NotificationConfigurationList configurations = new NotificationConfigurationList();
        log.info("Created default configurations list: " + gson.toJson(configurations));
        try {
            if (metaDataService == null) {
                log.error("MetaDataManagementService is null");
                throw new NotificationConfigurationServiceException("MetaDataManagementService is not available");
            }
            Metadata existingMetadata =
                    metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata == null
                    || existingMetadata.getMetaValue() == null
                    || existingMetadata.getMetaValue().isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No notification configurations found for tenant. Returning empty list.");
                }
                NotificationHelper.setDefaultArchivalValuesIfAbsent(configurations);
                return configurations;
            }
            String metaValue = existingMetadata.getMetaValue();
            log.info("Meta value: " + metaValue);
            Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
            NotificationConfigurationList configList = gson.fromJson(metaValue, listType);
            if (configList == null || configList.getNotificationConfigurations() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Meta value could not be deserialized or is empty. Returning empty list.");
                }
                NotificationHelper.setDefaultArchivalValuesIfAbsent(configurations);
                return configurations;
            }
            configurations.setNotificationConfigurations(configList.getNotificationConfigurations());
            configurations.setDefaultArchiveAfter(configList.getDefaultArchiveAfter());
            configurations.setDefaultArchiveType(configList.getDefaultArchiveType());
            NotificationHelper.setDefaultArchivalValuesIfAbsent(configurations);
        } catch (MetadataManagementException e) {
            String message = "Unexpected error occurred while retrieving notification configurations for tenant ID.";
            log.error(message, e);
            throw new NotificationConfigurationServiceException(message, e);
        }
        return configurations;
    }

    /**
     * Retrieves a specific notification configuration by its configuration ID.
     *
     * @param configID The unique identifier of the notification configuration.
     * @return A {@link NotificationConfig} object corresponding to the given ID.
     * @throws NotificationConfigurationServiceException If the configuration is not found or an error occurs during retrieval.
     */
    @Override
    public NotificationConfig getNotificationConfigByID(int configID) throws NotificationConfigurationServiceException {
        try {
            Metadata metaData = metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            if (metaData == null) {
                String message = "No notification configurations found for tenant";
                log.error(message);
                throw new NotificationConfigurationServiceException(message);
            }
            String metaValue = metaData.getMetaValue();
            Type listType = new TypeToken<NotificationConfigurationList>() {
            }.getType();
            NotificationConfigurationList configurations = gson.fromJson(metaValue, listType);
            if (configurations != null) {
                for (NotificationConfig config : configurations.getNotificationConfigurations()) {
                    if (config.getId() == configID) {
                        return config;
                    }
                }
            }
            String msg = "Configuration with config ID '" + configID + "' not found for tenant.";
            log.error(msg);
            throw new NotificationConfigurationServiceException(msg);
        } catch (MetadataManagementException e) {
            String message = "Error retrieving notification configuration by configID.";
            log.error(message, e);
            throw new NotificationConfigurationServiceException(message, e);
        }
    }
}
