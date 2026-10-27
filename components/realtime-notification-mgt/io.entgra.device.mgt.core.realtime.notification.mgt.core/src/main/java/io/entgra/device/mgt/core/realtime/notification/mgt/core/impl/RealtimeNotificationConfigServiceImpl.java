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

package io.entgra.device.mgt.core.realtime.notification.mgt.core.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.entgra.device.mgt.core.device.mgt.common.dto.DeviceFeatureInfo;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceFeatureOperationException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceFeatureOperations;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeArchivePeriod;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfigRecipients;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfigurationSettings;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeInvalidNotificationConfigurationException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationConfigurationNotFoundException;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeNotificationHelper;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeConstants;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfig;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfigurationList;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationConfigurationServiceException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.service.RealtimeNotificationConfigService;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.internal.RealtimeNotificationManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.ListIterator;
import java.util.List;
import java.util.stream.Collectors;

public class RealtimeNotificationConfigServiceImpl implements RealtimeNotificationConfigService {
    private static final Log log = LogFactory.getLog(RealtimeNotificationConfigServiceImpl.class);
    private static final Gson gson = new Gson();
    private final MetadataManagementService metaDataService =
            RealtimeNotificationManagementDataHolder.getInstance().getMetaDataManagementService();
    private static final Type NOTIFICATION_CONFIG_LIST_TYPE =
            new TypeToken<RealtimeNotificationConfigurationList>() {}.getType();
    private final DeviceFeatureOperations featureService =
            RealtimeNotificationManagementDataHolder.getInstance().getDeviceFeatureOperations();

    /**
     * Generates the next available ID for a new notification configuration.
     *
     * @param existingConfigs The list of existing notification configurations.
     * @return The next available ID (max existing ID + 1), or 1 if the list is empty.
     */
    private int generateNextId(List<RealtimeNotificationConfig> existingConfigs) {
        if (existingConfigs == null || existingConfigs.isEmpty()) {
            return 1;
        }
        return existingConfigs.stream()
                .mapToInt(RealtimeNotificationConfig::getId)
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
     * @throws RealtimeNotificationConfigurationServiceException if the configuration is invalid.
     */
    private void validateConfiguration(RealtimeNotificationConfig config) throws RealtimeNotificationConfigurationServiceException {
        if (config == null) {
            String msg = "Configuration cannot be null";
            log.error(msg);
            throw new RealtimeNotificationConfigurationServiceException(msg);
        }
        if (config.getId() <= 0) {
            String msg = "Invalid configuration ID: " + config.getId();
            log.error(msg);
            throw new RealtimeNotificationConfigurationServiceException(msg);
        }
        if (config.getRecipients() == null ||
                config.getCode() == null || config.getCode().trim().isEmpty() ||
                config.getConfiguredBy() == null) {
            String msg = "Invalid configuration: missing required fields. ConfigID=" + config.getId();
            log.error(msg);
            throw new RealtimeNotificationConfigurationServiceException(msg);
        }
    }

    /**
     * Checks whether the given notification configuration list is null or empty.
     *
     * @param configurations The notification configuration list to check.
     * @return True if the list or its contents are null or empty; false otherwise.
     */
    private boolean configurationsAreEmpty(RealtimeNotificationConfigurationList configurations) {
        return configurations == null || configurations.getNotificationConfigurations() == null
                || configurations.getNotificationConfigurations().isEmpty();
    }

    @Override
    public void setDefaultNotificationArchiveMetadata(String defaultType, RealtimeArchivePeriod defaultAfter)
            throws RealtimeNotificationConfigurationServiceException {
        if (defaultType == null || defaultType.isEmpty() ||
                defaultAfter == null || defaultAfter.getValue() <= 0 || defaultAfter.getUnit() == null) {
            String msg = "Default archive type and period must not be empty or invalid.";
            log.error(msg);
            throw new RealtimeInvalidNotificationConfigurationException(msg);
        }
        try {
            Metadata existingMetadata = getExistingMetadata(false);
            RealtimeNotificationConfigurationList configList;
            if (existingMetadata != null && existingMetadata.getMetaValue() != null) {
                configList = gson.fromJson(existingMetadata.getMetaValue(), RealtimeNotificationConfigurationList.class);
            } else {
                configList = new RealtimeNotificationConfigurationList();
            }
            if (configList.getNotificationConfigurations() == null) {
                configList.setNotificationConfigurations(new ArrayList<>());
            }
            configList.setDefaultArchiveType(defaultType);
            configList.setDefaultArchiveAfter(defaultAfter);
            if (existingMetadata != null) {
                existingMetadata.setMetaValue(gson.toJson(configList));
                metaDataService.updateMetadata(existingMetadata);
            } else {
                Metadata metadata = new Metadata();
                metadata.setMetaKey(RealtimeConstants.NOTIFICATION_CONFIG_META_KEY);
                metadata.setMetaValue(gson.toJson(configList));
                metaDataService.createMetadata(metadata);
            }
        } catch (MetadataManagementException e) {
            String msg = "Failed to set default notification archive metadata.";
            log.error(msg, e);
            throw new RealtimeNotificationConfigurationServiceException(msg, e);
        }
    }

    @Override
    public void ensureDefaultTaskNotificationConfigs() throws RealtimeNotificationConfigurationServiceException {
        if (featureService == null) {
            log.warn("DeviceFeatureOperations is unavailable. Skipping default task notification configs.");
            return;
        }
        List<DeviceFeatureInfo> taskFeatures;
        try {
            featureService.getDeviceFeatureOperations();
            taskFeatures = featureService.getOperationDetails(
                    null, null, null, RealtimeConstants.TASK, false);
        } catch (DeviceFeatureOperationException e) {
            String msg = "Failed to load task features for default notification configs.";
            log.error(msg, e);
            throw new RealtimeNotificationConfigurationServiceException(msg, e);
        }
        if (taskFeatures == null || taskFeatures.isEmpty()) {
            log.warn("No task features found in catalog. Skipping default task notification configs.");
            return;
        }
        try {
            Metadata existingMetadata = getExistingMetadata(false);
            RealtimeNotificationConfigurationList configList;
            if (existingMetadata != null && existingMetadata.getMetaValue() != null) {
                configList = gson.fromJson(existingMetadata.getMetaValue(), NOTIFICATION_CONFIG_LIST_TYPE);
            } else {
                configList = new RealtimeNotificationConfigurationList();
            }
            if (configList.getNotificationConfigurations() == null) {
                configList.setNotificationConfigurations(new ArrayList<>());
            }
            RealtimeNotificationHelper.setDefaultArchivalValuesIfAbsent(configList);
            List<RealtimeNotificationConfig> configs = configList.getNotificationConfigurations();
            boolean changed = false;
            for (DeviceFeatureInfo taskFeature : taskFeatures) {
                if (taskFeature == null || taskFeature.getOperationCode() == null
                        || taskFeature.getOperationCode().isBlank()) {
                    continue;
                }
                String taskCode = taskFeature.getOperationCode().trim();
                String catalogDeviceType = taskFeature.getDeviceType();
                if (catalogDeviceType == null || catalogDeviceType.isBlank()) {
                    log.warn("Skipping default task config for '" + taskCode
                            + "': catalog DeviceType is missing.");
                    continue;
                }
                boolean alreadyPresent = configs.stream()
                        .anyMatch(config -> RealtimeConstants.TASK.equalsIgnoreCase(config.getType())
                                && taskCode.equalsIgnoreCase(config.getCode()));
                if (alreadyPresent) {
                    continue;
                }
                configs.add(buildDefaultTaskConfig(taskFeature, generateNextId(configs)));
                changed = true;
            }
            if (!changed) {
                return;
            }
            configList.setNotificationConfigurations(configs);
            if (existingMetadata != null) {
                existingMetadata.setMetaValue(gson.toJson(configList));
                metaDataService.updateMetadata(existingMetadata);
            } else {
                Metadata metadata = new Metadata();
                metadata.setMetaKey(RealtimeConstants.NOTIFICATION_CONFIG_META_KEY);
                metadata.setMetaValue(gson.toJson(configList));
                metaDataService.createMetadata(metadata);
            }
        } catch (MetadataManagementException e) {
            String msg = "Failed to persist default task notification configs.";
            log.error(msg, e);
            throw new RealtimeNotificationConfigurationServiceException(msg, e);
        }
    }

    /**
     * Builds an enabled default task config from a catalog feature, with recipient user {@code admin}.
     */
    private RealtimeNotificationConfig buildDefaultTaskConfig(DeviceFeatureInfo taskFeature, int id) {
        RealtimeNotificationConfig config = new RealtimeNotificationConfig();
        config.setId(id);
        config.setName(taskFeature.getName());
        config.setDescription(taskFeature.getDescription());
        config.setType(RealtimeConstants.TASK);
        config.setCode(taskFeature.getOperationCode().trim());
        config.setDeviceType(taskFeature.getDeviceType().trim());
        config.setEnabled(true);
        RealtimeNotificationConfigRecipients recipients = new RealtimeNotificationConfigRecipients();
        recipients.setUsers(new ArrayList<>(Collections.singletonList(RealtimeConstants.ADMIN)));
        recipients.setRoles(new ArrayList<>());
        config.setRecipients(recipients);
        RealtimeNotificationConfig.ConfiguredBy configuredBy = new RealtimeNotificationConfig.ConfiguredBy();
        configuredBy.setUser(RealtimeConstants.ADMIN);
        configuredBy.setLastModifiedAt(Instant.now().toString());
        config.setConfiguredBy(configuredBy);
        RealtimeNotificationConfigurationSettings settings = new RealtimeNotificationConfigurationSettings();
        settings.setArchiveType(RealtimeConstants.DEFAULT_ARCHIVE_TYPE);
        settings.setArchiveAfter(RealtimeConstants.DEFAULT_ARCHIVE_PERIOD);
        config.setNotificationSettings(settings);
        return config;
    }

    /**
     * Adds new notification configuration contexts to the metadata storage.
     *
     * If a configuration with the same ID already exists, it will be skipped to avoid duplication.
     * If metadata already exists for notification configurations, it will be updated.
     * otherwise, new metadata will be created.
     * @param newConfigurations A list of new notification configurations to be added.
     * @return {@link RealtimeNotificationConfigurationList addNotificationConfigContext}
     * @throws RealtimeNotificationConfigurationServiceException If the input is invalid or if an error occurs while
     *                                                   accessing or updating metadata.
     */
    @Override
    public RealtimeNotificationConfigurationList addNotificationConfigContext(RealtimeNotificationConfigurationList newConfigurations)
            throws RealtimeNotificationConfigurationServiceException {
        for (RealtimeNotificationConfig config : newConfigurations.getNotificationConfigurations()) {
            RealtimeNotificationHelper.validateRecipients(config.getRecipients());
        }
        if (configurationsAreEmpty(newConfigurations)) {
            String msg = "Received empty configurations list, Cannot add empty configurations";
            log.error(msg);
            throw new RealtimeInvalidNotificationConfigurationException(msg);
        }
        try {
            Metadata existingMetadata = getExistingMetadata(false);
            RealtimeNotificationConfigurationList existingConfigurations = new RealtimeNotificationConfigurationList();
            if (existingMetadata != null && existingMetadata.getMetaValue() != null) {
                existingConfigurations = gson.fromJson(existingMetadata.getMetaValue(), NOTIFICATION_CONFIG_LIST_TYPE);
            }
            List<RealtimeNotificationConfig> finalConfigs = new ArrayList<>();
            if (existingConfigurations.getNotificationConfigurations() != null) {
                finalConfigs.addAll(existingConfigurations.getNotificationConfigurations());
            }
            List<String> operationCodesToValidate = new ArrayList<>();
            List<String> taskCodesToValidate = new ArrayList<>();
            for (RealtimeNotificationConfig config : newConfigurations.getNotificationConfigurations()) {
                if (config.getCode() == null) {
                    continue;
                }
                if (RealtimeConstants.OPERATION.equalsIgnoreCase(config.getType())) {
                    operationCodesToValidate.add(config.getCode());
                } else if (RealtimeConstants.TASK.equalsIgnoreCase(config.getType())) {
                    taskCodesToValidate.add(config.getCode());
                }
            }
            validateFeatureCodes(operationCodesToValidate, RealtimeConstants.OPERATION);
            validateFeatureCodes(taskCodesToValidate, RealtimeConstants.TASK);
            for (RealtimeNotificationConfig newConfig : newConfigurations.getNotificationConfigurations()) {
                if (newConfig.getId() != 0) {
                    String msg = "Config ID must not be provided. It will be auto-generated. Provided ID=" + newConfig.getId();
                    log.error(msg);
                    throw new RealtimeInvalidNotificationConfigurationException(msg);
                }
                // check for duplicates by deviceType and code
                boolean duplicate = finalConfigs.stream().anyMatch(config ->
                        newConfig.getDeviceType().equalsIgnoreCase(config.getDeviceType()) &&
                                newConfig.getCode().equalsIgnoreCase(config.getCode())
                );
                if (duplicate) {
                    String msg = "Duplicate notification configuration exists for deviceType="
                            + newConfig.getDeviceType() + " and code=" + newConfig.getCode();
                    log.error(msg);
                    throw new RealtimeInvalidNotificationConfigurationException(msg);
                }
                newConfig.setId(generateNextId(finalConfigs));
                try {
                    validateConfiguration(newConfig);
                } catch (RealtimeNotificationConfigurationServiceException e) {
                    String msg = "Invalid configuration: " + e.getMessage() +
                            ". ConfigID=" + (newConfig != null ? newConfig.getId() : "null");
                    log.error(msg);
                    throw new RealtimeInvalidNotificationConfigurationException(msg);
                }
                finalConfigs.add(newConfig);
            }
            existingConfigurations.setNotificationConfigurations(finalConfigs);
            if (existingMetadata != null) {
                existingMetadata.setMetaValue(gson.toJson(existingConfigurations));
                metaDataService.updateMetadata(existingMetadata);
            } else {
                Metadata newMetadata = new Metadata();
                newMetadata.setMetaKey(RealtimeConstants.NOTIFICATION_CONFIG_META_KEY);
                newMetadata.setMetaValue(gson.toJson(existingConfigurations));
                metaDataService.createMetadata(newMetadata);
            }
            return existingConfigurations;
        } catch (MetadataManagementException e) {
            String msg = "Error creating or updating metadata: " + e.getMessage();
            log.error(msg, e);
            throw new RealtimeNotificationConfigurationServiceException(msg, e);
        }
    }

    /**
     * Deletes a specific notification configuration from the Metadata context for a given tenant.
     *
     * @param configId The unique identifier (operationCode) of the notification configuration to be deleted.
     * @throws RealtimeNotificationConfigurationServiceException If no configuration is found with the specified operationCode, or
     * if any error occurs during the database transaction or processing
     * This method retrieves the existing notification configuration context for the given tenant, removes the
     * configuration matching the provided operationCode, and updates the Metadata context with the remaining configurations.
     */
    @Override
    public void deleteNotificationConfigContext(int configId) throws RealtimeNotificationConfigurationServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting notification configuration with ID: " + configId);
        }
        if (configId <= 0) {
            String msg = "Received empty or Invalid Configuration ID";
            log.error(msg);
            throw new RealtimeInvalidNotificationConfigurationException(msg);
        }
        try {
            Metadata existingMetadata = getExistingMetadata(true);
            if (existingMetadata == null || existingMetadata.getMetaValue() == null) {
                String msg = "RealtimeNotification configuration metadata not found.";
                log.warn(msg);
                throw new RealtimeNotificationConfigurationNotFoundException(msg);
            }
            Gson gson = new Gson();
            RealtimeNotificationConfigurationList configList = gson.fromJson(existingMetadata.getMetaValue(),
                    NOTIFICATION_CONFIG_LIST_TYPE);
            if (configList == null || configList.getNotificationConfigurations() == null) {
                throw new RealtimeNotificationConfigurationServiceException("Invalid format in notification " +
                        "configuration metadata.");
            }
            boolean removed = configList.getNotificationConfigurations()
                    .removeIf(c -> c.getId() == configId);
            if (!removed) {
                String msg = "RealtimeNotification configuration with ID: " + configId + " not found.";
                log.warn(msg);
                throw new RealtimeNotificationConfigurationNotFoundException(msg);
            }
            String updatedData = gson.toJson(configList);
            existingMetadata.setMetaKey(RealtimeConstants.NOTIFICATION_CONFIG_META_KEY);
            existingMetadata.setMetaValue(updatedData);
            metaDataService.updateMetadata(existingMetadata);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deleted notification configuration with ID: " + configId);
            }
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while deleting notification configurations.";
            log.error(msg, e);
            throw new RealtimeNotificationConfigurationServiceException(msg, e);
        }
    }

    /**
     * Updates an existing notification configuration or adds a new configuration to the Metadata context for a given tenant.
     *
     * @param updatedConfig The notification configuration to be updated or added.
     * If a configuration with the same operationCode exists, it will be updated; otherwise, it will be added as a new entry.
     * @throws RealtimeNotificationConfigurationServiceException If any error occurs during the database transaction or processing.
     * This method retrieves the existing notification configuration context for the given tenant. If a configuration with the same
     * operationCode as the provided configuration exists, it updates that configuration with the new details. Otherwise, it appends
     * the provided configuration as a new entry. The updated configurations are then serialized and saved back to the Metadata context.
     **/
    @Override
    public void updateNotificationConfigContext(RealtimeNotificationConfig updatedConfig)
            throws RealtimeNotificationConfigurationServiceException {
        RealtimeNotificationHelper.validateRecipients(updatedConfig.getRecipients());
        validateConfiguration(updatedConfig);
        if (RealtimeConstants.OPERATION.equalsIgnoreCase(updatedConfig.getType())) {
            validateFeatureCodes(Collections.singletonList(updatedConfig.getCode()), RealtimeConstants.OPERATION);
        } else if (RealtimeConstants.TASK.equalsIgnoreCase(updatedConfig.getType())) {
            validateFeatureCodes(Collections.singletonList(updatedConfig.getCode()), RealtimeConstants.TASK);
        }
        try {
            Metadata existingMetadata = getExistingMetadata(true);
            if (existingMetadata == null || existingMetadata.getMetaValue() == null) {
                String msg =  "RealtimeNotification configurations do not exist. Ensure configuration is set before continuing.";
                log.error(msg);
                throw new RealtimeNotificationConfigurationNotFoundException(msg);
            }
            RealtimeNotificationConfigurationList existingList =
                    gson.fromJson(existingMetadata.getMetaValue(), NOTIFICATION_CONFIG_LIST_TYPE);
            List<RealtimeNotificationConfig> configList = existingList.getNotificationConfigurations();
            if (configList == null) {
                throw new RealtimeNotificationConfigurationServiceException("Invalid configuration list in metadata.");
            }
            boolean duplicate = configList.stream().anyMatch(config ->
                    config.getId() != updatedConfig.getId() &&
                            updatedConfig.getDeviceType().equalsIgnoreCase(config.getDeviceType()) &&
                            updatedConfig.getCode().equalsIgnoreCase(config.getCode())
            );
            if (duplicate) {
                String msg = "Duplicate notification configuration exists for deviceType="
                        + updatedConfig.getDeviceType() + " and code=" + updatedConfig.getCode();
                log.error(msg);
                throw new RealtimeInvalidNotificationConfigurationException(msg);
            }
            boolean found = false;
            ListIterator<RealtimeNotificationConfig> iterator = configList.listIterator();
            while (iterator.hasNext()) {
                if (iterator.next().getId() == updatedConfig.getId()) {
                    iterator.set(updatedConfig);
                    found = true;
                    break;
                }
            }
            if (!found) {
                String msg =  "RealtimeNotification configuration with ID " + updatedConfig.getId() + " not found.";
                log.error(msg);
                throw new RealtimeNotificationConfigurationNotFoundException(msg);
            }
            existingMetadata.setMetaValue(gson.toJson(existingList));
            metaDataService.updateMetadata(existingMetadata);
        } catch (MetadataManagementException e) {
            String msg = "Error updating metadata: " + e.getMessage();
            log.error(msg, e);
            throw new RealtimeNotificationConfigurationServiceException(msg, e);
        }
    }

    /**
     * Deletes all notification configuration metadata for the current tenant.
     *
     * @throws RealtimeNotificationConfigurationServiceException If metadata is not found or if an error occurs during deletion.
     */
    @Override
    public void deleteNotificationConfigurations() throws RealtimeNotificationConfigurationServiceException {
        try {
            boolean cleared = metaDataService.clearMetadataValue(RealtimeConstants.NOTIFICATION_CONFIG_META_KEY);
            if (!cleared) {
                String msg = "No metadata value found to clear for key: " + RealtimeConstants.NOTIFICATION_CONFIG_META_KEY;
                log.warn(msg);
            }
        } catch (MetadataKeyNotFoundException e) {
            String msg = "No metadata entry found for key: " + RealtimeConstants.NOTIFICATION_CONFIG_META_KEY;
            log.error(msg, e);
            throw new RealtimeNotificationConfigurationServiceException(msg, e);
        } catch (MetadataManagementException e) {
            String message = "Unexpected error occurred while clearing notification configurations for tenant ID.";
            log.error(message, e);
            throw new RealtimeNotificationConfigurationServiceException(message, e);
        }
    }

    /**
     * Retrieves the list of notification configurations stored in metadata for the current tenant.
     *
     * @return A {@link RealtimeNotificationConfigurationList} containing all configured notifications.
     * @throws RealtimeNotificationConfigurationServiceException If metadata retrieval fails, the metadata is missing,
     *                                                   or deserialization fails.
     */
    @Override
    public RealtimeNotificationConfigurationList getNotificationConfigurations()
            throws RealtimeNotificationConfigurationServiceException {
        if (metaDataService == null) {
            String message = "MetaDataManagementService is not available";
            log.error(message);
            throw new RealtimeNotificationConfigurationServiceException(message);
        }
        Metadata existingMetadata = getExistingMetadata(false);
        if (existingMetadata == null || existingMetadata.getMetaValue() == null) {
            if (log.isDebugEnabled()) {
                log.debug("No notification configurations found for tenant. Returning empty list.");
            }
            return buildEmptyConfigurationList();
        }
        RealtimeNotificationConfigurationList configList =
                gson.fromJson(existingMetadata.getMetaValue(), NOTIFICATION_CONFIG_LIST_TYPE);
        if (configList == null || configList.getNotificationConfigurations() == null) {
            if (log.isDebugEnabled()) {
                log.debug("Meta value could not be deserialized or is empty. Returning empty list.");
            }
            return buildEmptyConfigurationList();
        }
        RealtimeNotificationConfigurationList configurations = new RealtimeNotificationConfigurationList();
        configurations.setNotificationConfigurations(configList.getNotificationConfigurations());
        configurations.setDefaultArchiveAfter(configList.getDefaultArchiveAfter());
        configurations.setDefaultArchiveType(configList.getDefaultArchiveType());
        RealtimeNotificationHelper.setDefaultArchivalValuesIfAbsent(configurations);
        return configurations;
    }

    /**
     * Retrieves a specific notification configuration by its configuration ID.
     *
     * @param configID The unique identifier of the notification configuration.
     * @return A {@link RealtimeNotificationConfig} object corresponding to the given ID.
     * @throws RealtimeNotificationConfigurationServiceException If the configuration is not found or an error occurs during retrieval.
     */
    @Override
    public RealtimeNotificationConfig getNotificationConfigByID(int configID)
            throws RealtimeNotificationConfigurationServiceException {
        if (configID <= 0) {
            String msg = "Invalid request: configuration ID is missing or invalid";
            log.error(msg);
            throw new RealtimeInvalidNotificationConfigurationException(msg);
        }
        Metadata metaData = getExistingMetadata(true);
        if (metaData == null || metaData.getMetaValue() == null) {
            String msg = "No notification configurations found for tenant";
            log.error(msg);
            throw new RealtimeNotificationConfigurationNotFoundException(msg);
        }
        RealtimeNotificationConfigurationList configurations =
                gson.fromJson(metaData.getMetaValue(), NOTIFICATION_CONFIG_LIST_TYPE);
        if (configurations == null || configurations.getNotificationConfigurations() == null) {
            String msg = "Configuration with config ID '" + configID + "' not found for tenant.";
            log.error(msg);
            throw new RealtimeNotificationConfigurationNotFoundException(msg);
        }
        return configurations.getNotificationConfigurations().stream()
                .filter(c -> c.getId() == configID)
                .findFirst()
                .orElseThrow(() -> new RealtimeNotificationConfigurationNotFoundException(
                        "Configuration with config ID '" + configID + "' not found for tenant."));
    }

    @Override
    public RealtimeNotificationConfigurationList getFilteredNotificationConfigurations
            (String name, String type, String code, String deviceType, int offset, int limit)
            throws RealtimeNotificationConfigurationServiceException {
        RealtimeNotificationConfigurationList allConfigurations;
        try {
            allConfigurations = getNotificationConfigurations();
        } catch (RealtimeNotificationConfigurationNotFoundException e) {
            allConfigurations = buildEmptyConfigurationList();
        }
        List<RealtimeNotificationConfig> filteredConfigs = allConfigurations.getNotificationConfigurations().stream()
                .filter(config -> {
                    boolean matchesName = (name == null || config.getName() != null &&
                            config.getName().toLowerCase().contains(name.toLowerCase()));
                    boolean matchesType = (type == null || config.getType() != null &&
                            config.getType().equalsIgnoreCase(type));
                    boolean matchesCode = (code == null || config.getCode() != null &&
                            config.getCode().toLowerCase().contains(code.toLowerCase()));
                    boolean matchesDeviceType = (deviceType == null || config.getDeviceType() != null &&
                            config.getDeviceType().toLowerCase().contains(deviceType.toLowerCase()));
                    return matchesName && matchesType && matchesCode && matchesDeviceType;
                })
                .collect(Collectors.toList());
        int totalCount = filteredConfigs.size();
        int fromIndex = Math.max(0, Math.min(offset, filteredConfigs.size()));
        int toIndex = Math.max(fromIndex, Math.min(offset + limit, filteredConfigs.size()));
        List<RealtimeNotificationConfig> pagedConfigs = filteredConfigs.subList(fromIndex, toIndex);
        RealtimeNotificationConfigurationList result = new RealtimeNotificationConfigurationList();
        result.setNotificationConfigurations(pagedConfigs);
        result.setDefaultArchiveAfter(allConfigurations.getDefaultArchiveAfter());
        result.setDefaultArchiveType(allConfigurations.getDefaultArchiveType());
        result.setTotalCount(totalCount);
        return result;
    }

    /**
     * Builds an empty RealtimeNotificationConfigurationList with default archival values set.
     *
     * @return a RealtimeNotificationConfigurationList instance containing no configurations,
     *         but with default archival settings applied.
     */
    private RealtimeNotificationConfigurationList buildEmptyConfigurationList() {
        RealtimeNotificationConfigurationList emptyList = new RealtimeNotificationConfigurationList();
        RealtimeNotificationHelper.setDefaultArchivalValuesIfAbsent(emptyList);
        return emptyList;
    }

    /**
     * Validates a list of operation codes using DeviceFeatureOperations.
     *
     * @param codes List of operation codes to validate.
     * @throws RealtimeInvalidNotificationConfigurationException If any code is invalid.
     * @throws RealtimeNotificationConfigurationServiceException If validation fails due to internal errors.
     */
    private void validateFeatureCodes(List<String> codes, String featureType)
            throws RealtimeNotificationConfigurationServiceException {
        if (codes == null || codes.isEmpty()) {
            return;
        }
        try {
            Map<String, Boolean> validationMap = featureService.validateFeatureCodes(codes, featureType);
            for (Map.Entry<String, Boolean> entry : validationMap.entrySet()) {
                if (!entry.getValue()) {
                    String msg = "Invalid " + featureType + " code: " + entry.getKey();
                    log.error(msg);
                    throw new RealtimeInvalidNotificationConfigurationException(msg);
                }
            }
        } catch (DeviceFeatureOperationException e) {
            String msg = "Error validating " + featureType + " codes: " + codes;
            log.error(msg, e);
            throw new RealtimeNotificationConfigurationServiceException(msg, e);
        }
    }

    /**
     * Retrieves the existing notification configuration metadata.
     *
     * @param throwIfMissing Whether to throw an exception if metadata or its value is missing.
     * @return The {@link Metadata} object if found, or null if not found and throwIfMissing is false.
     * @throws RealtimeNotificationConfigurationNotFoundException if metadata is missing and throwIfMissing is true.
     * @throws RealtimeNotificationConfigurationServiceException if retrieval fails.
     */
    private Metadata getExistingMetadata(boolean throwIfMissing)
            throws RealtimeNotificationConfigurationServiceException {
        try {
            Metadata metadata = metaDataService.retrieveMetadata(RealtimeConstants.NOTIFICATION_CONFIG_META_KEY);
            if (metadata == null || metadata.getMetaValue() == null) {
                if (throwIfMissing) {
                    throw new RealtimeNotificationConfigurationNotFoundException(
                            "RealtimeNotification configuration metadata not found.");
                }
                return null;
            }
            return metadata;
        } catch (MetadataManagementException e) {
            String msg = "Error retrieving metadata for notification configurations.";
            log.error(msg, e);
            throw new RealtimeNotificationConfigurationServiceException(msg, e);
        }
    }

    @Override
    public boolean configExists(String deviceType, String code)
            throws RealtimeNotificationConfigurationServiceException {
        if (deviceType == null || code == null) {
            String msg = "Device type and code must not be null.";
            log.error(msg);
            throw new RealtimeInvalidNotificationConfigurationException(msg);
        }
        RealtimeNotificationConfigurationList configurations = getNotificationConfigurations();
        return configurations.getNotificationConfigurations().stream()
                .anyMatch(config ->
                        deviceType.equalsIgnoreCase(config.getDeviceType()) &&
                                code.equalsIgnoreCase(config.getCode())
                );
    }
}
