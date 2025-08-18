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
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceFeatureOperationException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceFeatureOperations;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceFeatureOperationsImpl;
import io.entgra.device.mgt.core.notification.mgt.common.exception.InvalidNotificationConfigurationException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationNotFoundException;
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
import java.util.Collections;
import java.util.Map;
import java.util.ListIterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class NotificationConfigServiceImpl implements NotificationConfigService {
    private static final Log log = LogFactory.getLog(NotificationConfigServiceImpl.class);
    private static final Gson gson = new Gson();
    private final MetadataManagementService metaDataService =
            NotificationManagementDataHolder.getInstance().getMetaDataManagementService();
    private static final Type NOTIFICATION_CONFIG_LIST_TYPE =
            new TypeToken<NotificationConfigurationList>() {}.getType();

    /**
     * Generates the next available ID for a new notification configuration.
     *
     * @param existingConfigs The list of existing notification configurations.
     * @return The next available ID (max existing ID + 1), or 1 if the list is empty.
     */
    private int generateNextId(List<NotificationConfig> existingConfigs) {
        if (existingConfigs == null || existingConfigs.isEmpty()) {
            return 1;
        }
        return existingConfigs.stream()
                .mapToInt(NotificationConfig::getId)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Validates the given notification configuration.
     * - Configuration must not be null.
     * - ID must be greater than 0.
     * - Must have recipients, a non-empty code, and a configuredBy field.
     *
     * @param config The notification configuration to validate.
     * @throws NotificationConfigurationServiceException if the configuration is invalid.
     */
    private void validateConfiguration(NotificationConfig config) throws NotificationConfigurationServiceException {
        if (config == null) {
            String msg = "Configuration cannot be null";
            log.error(msg);
            throw new NotificationConfigurationServiceException(msg);
        }
        if (config.getId() <= 0) {
            String msg = "Invalid configuration ID: " + config.getId();
            log.error(msg);
            throw new NotificationConfigurationServiceException(msg);
        }
        if (config.getRecipients() == null ||
                config.getCode() == null || config.getCode().trim().isEmpty() ||
                config.getConfiguredBy() == null) {
            String msg = "Invalid configuration: missing required fields. ConfigID=" + config.getId();
            log.error(msg);
            throw new NotificationConfigurationServiceException(msg);
        }
    }

    /**
     * Checks whether the given notification configuration list is null or empty.
     *
     * @param configurations The notification configuration list to check.
     * @return True if the list or its contents are null or empty; false otherwise.
     */
    private boolean configurationsAreEmpty(NotificationConfigurationList configurations) {
        return configurations == null || configurations.getNotificationConfigurations() == null
                || configurations.getNotificationConfigurations().isEmpty();
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
     * @return {@link NotificationConfigurationList addNotificationConfigContext}
     * @throws NotificationConfigurationServiceException If the input is invalid or if an error occurs while
     *                                                   accessing or updating metadata.
     */
    @Override
    public NotificationConfigurationList addNotificationConfigContext(NotificationConfigurationList newConfigurations)
            throws NotificationConfigurationServiceException {
        if (configurationsAreEmpty(newConfigurations)) {
            String msg = "Received empty configurations list, Cannot add empty configurations";
            log.error(msg);
            throw new InvalidNotificationConfigurationException(msg);
        }
        List<String> validationErrors = new ArrayList<>();
        try {
            Metadata existingMetadata = metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            NotificationConfigurationList existingConfigurations = new NotificationConfigurationList();
            if (existingMetadata != null && existingMetadata.getMetaValue() != null) {
                existingConfigurations = gson.fromJson(existingMetadata.getMetaValue(), NOTIFICATION_CONFIG_LIST_TYPE);
            }
            List<NotificationConfig> finalConfigs = new ArrayList<>();
            if (existingConfigurations.getNotificationConfigurations() != null) {
                finalConfigs.addAll(existingConfigurations.getNotificationConfigurations());
            }
            // collect all operation codes to validate
            List<String> allCodesToValidate = new ArrayList<>();
            for (NotificationConfig config : newConfigurations.getNotificationConfigurations()) {
                if ("operation".equalsIgnoreCase(config.getType()) && config.getCode() != null) {
                    allCodesToValidate.add(config.getCode());
                }
            }
            // validate operation codes
            if (!allCodesToValidate.isEmpty()) {
                DeviceFeatureOperations featureService = new DeviceFeatureOperationsImpl();
                Map<String, Boolean> codeValidationMap = featureService.validateOperationCodes(allCodesToValidate);
                codeValidationMap.forEach((code, exists) -> {
                    if (!exists) {
                        validationErrors.add("Invalid operation code: " + code);
                    }
                });
            }
            for (NotificationConfig newConfig : newConfigurations.getNotificationConfigurations()) {
                if (newConfig.getId() == 0) {
                    newConfig.setId(generateNextId(finalConfigs));
                }
                try {
                    validateConfiguration(newConfig);
                } catch (NotificationConfigurationServiceException e) {
                    validationErrors.add("Config ID " + (newConfig != null ? newConfig.getId() : "null")
                            + ": " + e.getMessage());
                    continue;
                }
                boolean duplicateFound = finalConfigs.stream().anyMatch(
                        c -> c.getId() == newConfig.getId() || c.getCode().equals(newConfig.getCode())
                );
                if (duplicateFound) {
                    validationErrors.add("Duplicate ID or Code for config: ID=" + newConfig.getId() +
                            ", Code=" + newConfig.getCode());
                    continue;
                }
                finalConfigs.add(newConfig);
            }
            if (!validationErrors.isEmpty()) {
                throw new InvalidNotificationConfigurationException(validationErrors);
            }
            existingConfigurations.setNotificationConfigurations(finalConfigs);
            Metadata newMetadata = new Metadata();
            newMetadata.setMetaKey(Constants.NOTIFICATION_CONFIG_META_KEY);
            newMetadata.setMetaValue(gson.toJson(existingConfigurations));
            if (existingMetadata != null) {
                metaDataService.updateMetadata(newMetadata);
            } else {
                metaDataService.createMetadata(newMetadata);
            }
            return existingConfigurations;
        } catch (MetadataManagementException e) {
            String msg = "Error creating or updating metadata: " + e.getMessage();
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        } catch (DeviceFeatureOperationException e) {
            String msg = "Error validating operation codes related to the notification config";
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
            if (existingMetadata == null || existingMetadata.getMetaValue() == null) {
                String msg = "Notification configuration metadata not found.";
                log.warn(msg);
                throw new NotificationConfigurationNotFoundException(msg);
            }
            Gson gson = new Gson();
            NotificationConfigurationList configList = gson.fromJson(existingMetadata.getMetaValue(),
                    NOTIFICATION_CONFIG_LIST_TYPE);
            if (configList == null || configList.getNotificationConfigurations() == null) {
                throw new NotificationConfigurationServiceException("Invalid format in notification " +
                        "configuration metadata.");
            }
            boolean removed = configList.getNotificationConfigurations()
                    .removeIf(c -> c.getId() == configId);
            if (!removed) {
                String msg = "Notification configuration with ID: " + configId + " not found.";
                log.warn(msg);
                throw new NotificationConfigurationNotFoundException(msg);
            }
            String updatedData = gson.toJson(configList);
            Metadata updatedMetadata = new Metadata();
            updatedMetadata.setMetaKey(Constants.NOTIFICATION_CONFIG_META_KEY);
            updatedMetadata.setMetaValue(updatedData);
            metaDataService.updateMetadata(updatedMetadata);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deleted notification configuration with ID: " + configId);
            }
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while retrieving/updating notification configuration metadata.";
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
        validateConfiguration(updatedConfig);
        // validate operation code if type is "operation"
        try {
            DeviceFeatureOperations featureService = new DeviceFeatureOperationsImpl();
            Map<String, Boolean> validationMap =
                    featureService.validateOperationCodes(Collections.singletonList(updatedConfig.getCode()));
            if (!validationMap.getOrDefault(updatedConfig.getCode(), false)) {
                String msg = "Invalid operation code for configuration ID " + updatedConfig.getId() +
                        ": " + updatedConfig.getCode();
                log.error(msg);
                throw new InvalidNotificationConfigurationException(msg);
            }
        } catch (DeviceFeatureOperationException e) {
            String msg = "Error validating operation code for configuration ID " + updatedConfig.getId();
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        }
        try {
            Metadata existingMetadata =
                    metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata == null || existingMetadata.getMetaValue() == null) {
                String msg =  "No configurations found.";
                log.error(msg);
                throw new NotificationConfigurationNotFoundException(msg);
            }
            NotificationConfigurationList existingList =
                    gson.fromJson(existingMetadata.getMetaValue(), NOTIFICATION_CONFIG_LIST_TYPE);
            List<NotificationConfig> configList = existingList.getNotificationConfigurations();
            if (configList == null) {
                throw new NotificationConfigurationServiceException("Invalid configuration list in metadata.");
            }
            boolean found = false;
            ListIterator<NotificationConfig> iterator = configList.listIterator();
            while (iterator.hasNext()) {
                if (iterator.next().getId() == updatedConfig.getId()) {
                    iterator.set(updatedConfig);
                    found = true;
                    break;
                }
            }
            if (!found) {
                String msg =  "Notification configuration with ID " + updatedConfig.getId() + " not found.";
                log.error(msg);
                throw new NotificationConfigurationNotFoundException(msg);
            }
            existingMetadata.setMetaValue(gson.toJson(existingList));
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
        try {
            if (metaDataService == null) {
                String message = "MetaDataManagementService is not available";
                log.error(message);
                throw new NotificationConfigurationServiceException(message);
            }
            Metadata existingMetadata = metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata == null
                    || existingMetadata.getMetaValue() == null
                    || existingMetadata.getMetaValue().isEmpty()) {
                String message = "No notification configurations found for tenant.";
                log.debug(message);
                throw new NotificationConfigurationNotFoundException(message);
            }
            NotificationConfigurationList configList =
                    gson.fromJson(existingMetadata.getMetaValue(), NOTIFICATION_CONFIG_LIST_TYPE);
            if (configList == null || configList.getNotificationConfigurations() == null) {
                log.debug("Meta value could not be deserialized or is empty. Returning empty list.");
                return buildEmptyConfigurationList();
            }
            NotificationConfigurationList configurations = new NotificationConfigurationList();
            configurations.setNotificationConfigurations(configList.getNotificationConfigurations());
            configurations.setDefaultArchiveAfter(configList.getDefaultArchiveAfter());
            configurations.setDefaultArchiveType(configList.getDefaultArchiveType());
            NotificationHelper.setDefaultArchivalValuesIfAbsent(configurations);
            return configurations;
        } catch (MetadataManagementException e) {
            String message = "Unexpected error occurred while retrieving notification configurations for tenant.";
            log.error(message, e);
            throw new NotificationConfigurationServiceException(message, e);
        }
    }

    /**
     * Retrieves a specific notification configuration by its configuration ID.
     *
     * @param configID The unique identifier of the notification configuration.
     * @return A {@link NotificationConfig} object corresponding to the given ID.
     * @throws NotificationConfigurationServiceException If the configuration is not found or an error occurs during retrieval.
     */
    @Override
    public NotificationConfig getNotificationConfigByID(int configID)
            throws NotificationConfigurationServiceException {
        try {
            Metadata metaData = metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            if (metaData == null || metaData.getMetaValue() == null || metaData.getMetaValue().isEmpty()) {
                String msg = "No notification configurations found for tenant";
                log.error(msg);
                throw new NotificationConfigurationNotFoundException(msg);
            }
            NotificationConfigurationList configurations =
                    gson.fromJson(metaData.getMetaValue(), NOTIFICATION_CONFIG_LIST_TYPE);
            if (configurations == null || configurations.getNotificationConfigurations() == null) {
                String msg = "Configuration with config ID '" + configID + "' not found for tenant.";
                log.error(msg);
                throw new NotificationConfigurationNotFoundException(msg);
            }
            return configurations.getNotificationConfigurations().stream()
                    .filter(c -> c.getId() == configID)
                    .findFirst()
                    .orElseThrow(() -> new NotificationConfigurationNotFoundException(
                            "Configuration with config ID '" + configID + "' not found for tenant."));
        } catch (MetadataManagementException e) {
            String message = "Error retrieving notification configuration by configID.";
            log.error(message, e);
            throw new NotificationConfigurationServiceException(message, e);
        }
    }

    @Override
    public NotificationConfigurationList getFilteredNotificationConfigurations
            (String name, String type, String code, int offset, int limit)
            throws NotificationConfigurationServiceException {

        NotificationConfigurationList allConfigurations;
        try {
            allConfigurations = getNotificationConfigurations();
        } catch (NotificationConfigurationNotFoundException e) {
            allConfigurations = buildEmptyConfigurationList();
        }
        List<NotificationConfig> filteredConfigs = allConfigurations.getNotificationConfigurations().stream()
                .filter(config -> {
                    boolean matchesName = (name == null || config.getName() != null &&
                            config.getName().toLowerCase().contains(name.toLowerCase()));
                    boolean matchesType = (type == null || config.getType() != null &&
                            config.getType().equalsIgnoreCase(type));
                    boolean matchesCode = (code == null || config.getCode() != null &&
                            config.getCode().toLowerCase().contains(code.toLowerCase()));
                    return matchesName && matchesType && matchesCode;
                })
                .collect(Collectors.toList());
        int fromIndex = Math.max(0, Math.min(offset, filteredConfigs.size()));
        int toIndex = Math.max(fromIndex, Math.min(offset + limit, filteredConfigs.size()));
        List<NotificationConfig> pagedConfigs = filteredConfigs.subList(fromIndex, toIndex);
        NotificationConfigurationList result = new NotificationConfigurationList();
        result.setNotificationConfigurations(pagedConfigs);
        result.setDefaultArchiveAfter(allConfigurations.getDefaultArchiveAfter());
        result.setDefaultArchiveType(allConfigurations.getDefaultArchiveType());
        return result;
    }

    /**
     * Builds an empty NotificationConfigurationList with default archival values set.
     *
     * @return a NotificationConfigurationList instance containing no configurations,
     *         but with default archival settings applied.
     */
    private NotificationConfigurationList buildEmptyConfigurationList() {
        NotificationConfigurationList emptyList = new NotificationConfigurationList();
        NotificationHelper.setDefaultArchivalValuesIfAbsent(emptyList);
        return emptyList;
    }
}
