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
package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.type.DeviceTypeMetaEntry;

import java.util.List;

/**
 * DAO interface for performing CRUD operations on device type metadata
 * stored in the DM_DEVICE_TYPE_META table. This interface supports
 * handling metadata entries identified by a meta key, device type, and tenant ID.
 */
public interface DeviceTypeMetaDataDAO {

    /**
     * Retrieves all metadata entries associated with a given device type and tenant ID.
     *
     * @param deviceType the name of the device type for which metadata entries are to be retrieved
     * @param tenantId the tenant ID associated with the device type
     * @return a list of {@link DeviceTypeMetaEntry} objects containing metadata key-value pairs
     * @throws DeviceManagementDAOException if an error occurs while accessing the database
     */
    List<DeviceTypeMetaEntry> getDeviceTypeMetaEntries(String deviceType, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * Creates a new metadata entry for a given device type and tenant.
     *
     * @param deviceType The name of the device type (e.g., "android", "ios").
     * @param tenantId   The ID of the tenant creating the metadata entry.
     * @param entry      A {@link DeviceTypeMetaEntry} object containing the metadata key and value.
     *                   The value is typically a JSON-formatted string.
     * @return {@code true} if the metadata entry was successfully created; {@code false} otherwise.
     * @throws DeviceManagementDAOException If a database access error occurs while creating the entry.
     */
    boolean createDeviceTypeMetaEntry(String deviceType, int tenantId, DeviceTypeMetaEntry entry)
            throws DeviceManagementDAOException;

    /**
     * Retrieves the metadata value associated with a specific device type, tenant, and meta key.
     *
     * @param deviceType The device type name.
     * @param tenantId   The tenant ID.
     * @param metaKey    The metadata key.
     * @return The metadata value as a string, or null if no entry exists.
     * @throws DeviceManagementDAOException If a database access error occurs.
     */
    DeviceTypeMetaEntry getDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException;

    /**
     * Updates an existing metadata entry for a given device type and tenant, identified by the metadata key.
     *
     * @param deviceType The name of the device type (e.g., "android", "ios").
     * @param tenantId   The ID of the tenant performing the update.
     * @param entry      A {@link DeviceTypeMetaEntry} object containing the metadata key and the new value.
     *                   The metadata value is typically a JSON-formatted string.
     * @return {@code true} if the metadata entry was successfully updated (i.e., a row was affected); {@code false} otherwise.
     * @throws DeviceManagementDAOException If a database access error occurs during the update.
     */
    boolean updateDeviceTypeMetaEntry(String deviceType, int tenantId, DeviceTypeMetaEntry entry)
            throws DeviceManagementDAOException;

    /**
     * Deletes a metadata entry for a device type, tenant, and meta key.
     *
     * @param deviceType The device type name.
     * @param tenantId   The tenant ID.
     * @param metaKey    The metadata key.
     * @return true if a row was deleted, false if no matching row was found.
     * @throws DeviceManagementDAOException If a database access error occurs.
     */
    boolean deleteDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException;

    /**
     * Checks whether a metadata entry exists for the given device type, tenant, and meta key.
     *
     * @param deviceType The device type name.
     * @param tenantId   The tenant ID.
     * @param metaKey    The metadata key.
     * @return true if the metadata entry exists, false otherwise.
     * @throws DeviceManagementDAOException If a database access error occurs.
     */
    boolean isDeviceTypeMetaEntryExist(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException;
}
