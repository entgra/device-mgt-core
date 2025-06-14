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
import io.entgra.device.mgt.core.device.mgt.common.type.DeviceTypeMetaEntry;

import java.util.List;

/**
 * Service interface for managing device type metadata entries.
 * Provides operations to create, read, update, delete, and check existence
 * of metadata entries for specific device types within a tenant context.
 */
public interface DeviceTypeMetaDataManagementProviderService {

    /**
     * Creates a new metadata entry for the specified device type and metadata key.
     *
     * @param deviceType The name of the device type (e.g., "android", "ios").
     * @param metaEntry A {@link DeviceTypeMetaEntry} object containing the metadata key and value.
     *              The value is typically a JSON-formatted string.
     * @return {@code true} if the metadata entry was successfully created; {@code false} otherwise.
     * @throws DeviceManagementException If an error occurs while creating the metadata entry.
     */
    boolean createMetaEntry(String deviceType, DeviceTypeMetaEntry metaEntry) throws DeviceManagementException;

    /**
     * Retrieves the metadata value associated with a given device type and meta key.
     *
     * @param deviceType The device type name.
     * @param metaKey The metadata key.
     * @return The metadata value as a string, or null if no such entry exists.
     * @throws DeviceManagementException If an error occurs while retrieving the entry.
     */
    DeviceTypeMetaEntry getMetaEntry(String deviceType, String metaKey) throws DeviceManagementException;

    /**
     * Retrieves all metadata entries associated with the specified device type
     * for the current tenant.
     *
     * @param deviceType the name of the device type whose metadata entries are to be retrieved
     * @return a list of {@link DeviceTypeMetaEntry} objects representing metadata for the device type
     * @throws DeviceManagementException if an error occurs while accessing or retrieving the metadata entries
     */
    List<DeviceTypeMetaEntry> getMetaEntries(String deviceType) throws DeviceManagementException;

    /**
     * Updates an existing metadata entry for the specified device type, identified by the metadata key.
     *
     * @param deviceType The name of the device type (e.g., "android", "ios").
     * @param metaEntry A {@link DeviceTypeMetaEntry} object containing the metadata key and the new value.
     *              The value is typically a JSON-formatted string.
     * @return {@code true} if the metadata entry was successfully updated; {@code false} otherwise.
     * @throws DeviceManagementException If an error occurs while updating the metadata entry.
     */
    boolean updateMetaEntry(String deviceType, DeviceTypeMetaEntry metaEntry) throws DeviceManagementException;

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
