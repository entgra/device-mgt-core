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

/**
 * DAO interface for performing CRUD operations on device type metadata
 * stored in the DM_DEVICE_TYPE_META table. This interface supports
 * handling metadata entries identified by a meta key, device type, and tenant ID.
 */
public interface DeviceTypeMetaDataDAO {

    /**
     * Creates a new metadata entry for a device type and tenant with a given meta key and value.
     *
     * @param deviceType The device type name.
     * @param tenantId The tenant ID.
     * @param metaKey The metadata key.
     * @param metaValue The metadata value (typically JSON string).
     * @return true if the entry was successfully created, false otherwise.
     * @throws DeviceManagementDAOException If a database access error occurs.
     */
    boolean createDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey, String metaValue)
            throws DeviceManagementDAOException;

    /**
     * Retrieves the metadata value associated with a specific device type, tenant, and meta key.
     *
     * @param deviceType The device type name.
     * @param tenantId The tenant ID.
     * @param metaKey The metadata key.
     * @return The metadata value as a string, or null if no entry exists.
     * @throws DeviceManagementDAOException If a database access error occurs.
     */
    String getDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException;

    /**
     * Updates an existing metadata entry for a device type and tenant identified by the meta key.
     *
     * @param deviceType The device type name.
     * @param tenantId The tenant ID.
     * @param metaKey The metadata key.
     * @param metaValue The new metadata value.
     * @return true if the update was successful (row affected), false otherwise.
     * @throws DeviceManagementDAOException If a database access error occurs.
     */
    boolean updateDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey, String metaValue)
            throws DeviceManagementDAOException;

    /**
     * Deletes a metadata entry for a device type, tenant, and meta key.
     *
     * @param deviceType The device type name.
     * @param tenantId The tenant ID.
     * @param metaKey The metadata key.
     * @return true if a row was deleted, false if no matching row was found.
     * @throws DeviceManagementDAOException If a database access error occurs.
     */
    boolean deleteDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException;

    /**
     * Checks whether a metadata entry exists for the given device type, tenant, and meta key.
     *
     * @param deviceType The device type name.
     * @param tenantId The tenant ID.
     * @param metaKey The metadata key.
     * @return true if the metadata entry exists, false otherwise.
     * @throws DeviceManagementDAOException If a database access error occurs.
     */
    boolean isDeviceTypeMetaEntryExist(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException;

}
