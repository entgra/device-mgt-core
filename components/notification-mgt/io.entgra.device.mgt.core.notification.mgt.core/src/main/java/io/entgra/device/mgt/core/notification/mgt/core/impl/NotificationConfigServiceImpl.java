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
import io.entgra.device.mgt.core.notification.mgt.core.util.MetadataConstants;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationConfigService;
import io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.lang.reflect.Type;
import java.util.NoSuchElementException;

public class NotificationConfigServiceImpl implements NotificationConfigService {
    private static final Log log = LogFactory.getLog(NotificationConfigServiceImpl.class);
    private static final Gson gson = new Gson();
//     MetadataManagementService metadataManagementService = NotificationManagementDataHolder.getInstance().getMetaDataManagementService();

    public void addNotificationConfigContext(NotificationConfigurationList newConfigurations)
            throws NotificationConfigurationServiceException {

        if (newConfigurations == null || newConfigurations.isEmpty()) {
            throw new NotificationConfigurationServiceException("Cannot add empty configurations");
        }

        try {
            // Check if metadata already exists
            Metadata existingMetadata = NotificationManagementDataHolder.getInstance()
                    .getMetaDataManagementService()
                    .retrieveMetadata(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);

            NotificationConfigurationList configurations = new NotificationConfigurationList();

            // If metadata exists, deserialize it
            if (existingMetadata != null) {
                String metaValue = existingMetadata.getMetaValue();
                if (metaValue != null && !metaValue.isEmpty()) {
                    Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
                    NotificationConfigurationList existingConfigs = gson.fromJson(metaValue, listType);
                    if (existingConfigs != null && existingConfigs.getList() != null) {
                        //if deserialization successfull
                        configurations.setList(existingConfigs.getList());
                    }
                }
            }

            // Add all new configurations (assuming IDs are set correctly)
            for (NotificationConfig newConfig : newConfigurations.getList()) {
                // Check for duplicate IDs
                boolean isDuplicate = false;
                for (NotificationConfig existingConfig : configurations.getList()) {
                    if (existingConfig.getId() == newConfig.getId()) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    log.warn("Configuration with ID " + newConfig.getId() + " already exists, skipping");
                } else {
                    configurations.add(newConfig);
                }
            }

            // Serialize and save
            Metadata configMetadata = new Metadata();
            configMetadata.setMetaKey(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);
            configMetadata.setMetaValue(gson.toJson(configurations));

            if (existingMetadata != null) {
                NotificationManagementDataHolder.getInstance()
                        .getMetaDataManagementService()
                        .updateMetadata(configMetadata);
            } else {
                NotificationManagementDataHolder.getInstance()
                        .getMetaDataManagementService()
                        .createMetadata(configMetadata);
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
     * @param configID The unique identifier (operationCode) of the notification configuration to be deleted.
     * @throws NotificationConfigurationServiceException If no configuration is found with the specified operationCode, or
     * if any error occurs during the database transaction or processing
     * This method retrieves the existing notification configuration context for the given tenant, removes the
     * configuration matching the provided operationCode, and updates the Metadata context with the remaining configurations.
     */
    public void deleteNotificationConfigContext(int configID) throws NotificationConfigurationServiceException {

        try {
            Metadata existingMetadata = NotificationManagementDataHolder.getInstance().getMetaDataManagementService().retrieveMetadata(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata == null) {
                String message = "No notification configuration with Meta key found for tenant: ";
                throw new NoSuchElementException(message);
            }
            String metaValue = existingMetadata.getMetaValue();
            //  deserialize the metaValue to a list of NotificationConfig objects
            Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
            NotificationConfigurationList configurations = gson.fromJson(metaValue, listType);
            // Remove configuration with the given operationCode
            boolean isRemoved = configurations.getList().removeIf(config -> config.getId() == configID);
            if (!isRemoved) {
                String message = "No configuration found with config ID: " + configID;
                log.error(message);
                throw new NotificationConfigurationServiceException(message);
            }
            // Serialize the updated list back to JSON
            existingMetadata.setMetaValue(gson.toJson(configurations));
            NotificationManagementDataHolder.getInstance().getMetaDataManagementService().updateMetadata(existingMetadata);
        } catch (NoSuchElementException  e) {
            String msg = "No notification configuration context found for tenant: ";
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        } catch (IllegalArgumentException e){
            String msg = "Invalid notification configuration context: " + configID;
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg,e);
        } catch (MetadataManagementException e) {
            String msg = "Unexpected error occurred while deleting Notification Configurations: " + configID;
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        }
    }
    /**
     * Updates an existing notification configuration or adds a new configuration to the Metadata context for a given tenant.
     *
     * @param updatedConfig The notification configuration to be updated or added.
     *                      If a configuration with the same operationCode exists, it will be updated; otherwise, it will be added as a new entry.
     * @throws NotificationConfigurationServiceException If any error occurs during the database transaction or processing.
     * This method retrieves the existing notification configuration context for the given tenant. If a configuration with the same
     * operationCode as the provided configuration exists, it updates that configuration with the new details. Otherwise, it appends
     * the provided configuration as a new entry. The updated configurations are then serialized and saved back to the Metadata context.
    **/
    public void updateNotificationConfigContext(NotificationConfig updatedConfig)
            throws NotificationConfigurationServiceException {

        if (updatedConfig == null) {
            throw new NotificationConfigurationServiceException("Cannot update null configuration");
        }

        if (updatedConfig.getId() <= 0) {
            throw new NotificationConfigurationServiceException("Configuration ID must be positive");
        }

        try {
            // Retrieve existing metadata
            Metadata existingMetadata = NotificationManagementDataHolder.getInstance()
                    .getMetaDataManagementService()
                    .retrieveMetadata(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);

            if (existingMetadata == null) {
                throw new NotificationConfigurationServiceException(
                        "No notification configurations found to update");
            }

            String metaValue = existingMetadata.getMetaValue();
            if (metaValue == null || metaValue.isEmpty()) {
                throw new NotificationConfigurationServiceException(
                        "Empty metadata value for notification configurations");
            }

            // Deserialize the existing configurations
            Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
            NotificationConfigurationList configurations = gson.fromJson(metaValue, listType);

            if (configurations == null || configurations.getList() == null) {
                throw new NotificationConfigurationServiceException(
                        "Failed to deserialize existing configurations");
            }

            // Update the configuration
            boolean found = false;
            for (int i = 0; i < configurations.size(); i++) {
                if (configurations.get(i).getId() == updatedConfig.getId()) {
                    configurations.set(i, updatedConfig);
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new NotificationConfigurationServiceException(
                        "Configuration with ID " + updatedConfig.getId() + " not found");
            }

            // Serialize and update
            existingMetadata.setMetaValue(gson.toJson(configurations));
            NotificationManagementDataHolder.getInstance()
                    .getMetaDataManagementService()
                    .updateMetadata(existingMetadata);

        } catch (MetadataManagementException e) {
            String msg = "Error updating metadata: " + e.getMessage();
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        }
    }


    public void deleteNotificationConfigurations() throws NotificationConfigurationServiceException{
        try {
            NotificationManagementDataHolder.getInstance().getMetaDataManagementService().deleteMetadata(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);
        } catch (NoSuchElementException e ) {
            String msg = "No Meta Data found for Tenant ID";
            log.error(msg);
            throw new NotificationConfigurationServiceException(msg, e);
        }
        catch (MetadataManagementException e)
        {
            String message = "Unexpected error occurred while deleting notification configurations for tenant ID.";
            log.error(message, e);
            throw new NotificationConfigurationServiceException(message, e);
        }
    }

    public NotificationConfigurationList getNotificationConfigurations() throws NotificationConfigurationServiceException {
        NotificationConfigurationList configurations = new NotificationConfigurationList();
        log.info("created default configurations list" + gson.toJson(configurations));
        try {
            if (NotificationManagementDataHolder.getInstance().getMetaDataManagementService() == null) {
                log.error("MetaDataManagementService is null");
                throw new NotificationConfigurationServiceException("MetaDataManagementService is not available");
            }
            Metadata existingMetadata = NotificationManagementDataHolder.getInstance().getMetaDataManagementService().retrieveMetadata(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata == null) {
                if(log.isDebugEnabled()){
                    log.debug("No notification configurations found for tenant");
                }
                //continue with empty list
            }
            if(log.isDebugEnabled()){
                log.debug("existing meta data" + existingMetadata);
            }
            String metaValue = existingMetadata.getMetaValue();
            log.info("Meta value: " + metaValue);
            // Directly deserialize into a List of NotificationConfig
            Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
            NotificationConfigurationList configList = gson.fromJson(metaValue, listType);
            if (configList == null) {
                if(log.isDebugEnabled()){
                    log.debug("Meta value could not be deserialized.");
                }
                //continue with empty list
            }
            configurations.setList(configList.getList());
        }catch(NullPointerException e){
            String message = "Meta value doesn't exist for meta key.";
            log.error(message, e);
            throw new NotificationConfigurationServiceException(message, e);
        }
        catch (MetadataManagementException e) {
            if (e.getMessage().contains("not found")) {
                String message = "Notification configurations not found for tenant ID";
                log.warn(message);
                throw new NotificationConfigurationServiceException(message, e);
            } else {
                String message = "Unexpected error occurred while retrieving notification configurations for tenant ID.";
                log.error(message, e);
                throw new NotificationConfigurationServiceException(message, e);
            }
        }
        return configurations;
    }
    public NotificationConfig getNotificationConfigByID(int configID) throws NotificationConfigurationServiceException {
        try {
            Metadata metaData = NotificationManagementDataHolder.getInstance().getMetaDataManagementService().retrieveMetadata(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);
            if (metaData == null) {
                String message = "No notification configurations found for tenant";
                log.error(message);
                throw new NotificationConfigurationServiceException(message);
            }
            String metaValue = metaData.getMetaValue();
            // Directly deserialize into a List of NotificationConfig
            Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
            NotificationConfigurationList configurations = gson.fromJson(metaValue, listType);
            // Search for the configuration with the given operationCode
            if (configurations != null) {
                for (NotificationConfig config : configurations.getList()) {
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





