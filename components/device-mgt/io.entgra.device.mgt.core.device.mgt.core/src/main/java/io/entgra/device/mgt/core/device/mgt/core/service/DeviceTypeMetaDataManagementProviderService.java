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
package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;

/**
 * Service interface for managing device type metadata entries.
 * Provides operations to create, read, update, delete, and check existence
 * of metadata entries for specific device types within a tenant context.
 */
public interface DeviceTypeMetaDataManagementProviderService {

    /**
     * Creates a new metadata entry for a given device type and meta key.
     *
     * @param deviceType The device type name.
     * @param metaKey The metadata key.
     * @param metaValue The metadata value (typically a JSON string).
     * @return true if the metadata entry was successfully created, false otherwise.
     * @throws DeviceManagementException If an error occurs while creating the entry.
     */
    boolean createMetaEntry(String deviceType, String metaKey, String metaValue) throws DeviceManagementException;

    /**
     * Retrieves the metadata value associated with a given device type and meta key.
     *
     * @param deviceType The device type name.
     * @param metaKey The metadata key.
     * @return The metadata value as a string, or null if no such entry exists.
     * @throws DeviceManagementException If an error occurs while retrieving the entry.
     */
    String getMetaEntry(String deviceType, String metaKey) throws DeviceManagementException;

    /**
     * Updates an existing metadata entry for a given device type and meta key.
     *
     * @param deviceType The device type name.
     * @param metaKey The metadata key.
     * @param metaValue The new metadata value.
     * @return true if the update was successful, false otherwise.
     * @throws DeviceManagementException If an error occurs while updating the entry.
     */
    boolean updateMetaEntry(String deviceType, String metaKey, String metaValue) throws DeviceManagementException;

    /**
     * Deletes a metadata entry for a given device type and meta key.
     *
     * @param deviceType The device type name.
     * @param metaKey The metadata key.
     * @return true if the metadata entry was successfully deleted, false otherwise.
     * @throws DeviceManagementException If an error occurs while deleting the entry.
     */
    boolean deleteMetaEntry(String deviceType, String metaKey) throws DeviceManagementException;

    /**
     * Checks whether a metadata entry exists for a given device type and meta key.
     *
     * @param deviceType The device type name.
     * @param metaKey The metadata key.
     * @return true if the metadata entry exists, false otherwise.
     * @throws DeviceManagementException If an error occurs while checking the existence.
     */
    boolean isMetaEntryExist(String deviceType, String metaKey) throws DeviceManagementException;
}
