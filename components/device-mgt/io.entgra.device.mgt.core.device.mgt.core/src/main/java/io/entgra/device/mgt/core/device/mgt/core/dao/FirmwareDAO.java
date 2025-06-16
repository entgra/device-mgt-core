/*
 * Copyright (C) 2018 - 2025 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.app.mgt.DeviceFirmwareModel;

/**
 * Data Access Object interface for managing device firmware models.
 */
public interface FirmwareDAO {
    /**
     * Retrieves the firmware model associated with a specific device.
     * @param deviceId the ID of the device for which the firmware model is to be retrieved.
     * @return {@link DeviceFirmwareModel} containing the firmware model details for the specified device.
     * @throws DeviceManagementDAOException if an error occurs while accessing the database.
     */
    DeviceFirmwareModel getDeviceFirmwareModel(int deviceId, int tenantId) throws DeviceManagementDAOException;

    /**
     * Create a new firmware model
     * @param deviceFirmwareModel the firmware model to be added
     * @param tenantId the ID of the tenant to which the firmware model belongs
     * @return the ID of the newly created firmware model
     * @throws DeviceManagementDAOException if an error occurs while accessing the database
     */
    DeviceFirmwareModel addFirmwareModel(DeviceFirmwareModel deviceFirmwareModel, int tenantId) throws DeviceManagementDAOException;

    /**
     * Gets an existing firmware model by its name and tenant ID.
     * @param firmwareModel the name of the firmware model to be retrieved
     * @param tenantId the ID of the tenant to which the firmware model belongs
     * @return {@link DeviceFirmwareModel} containing the details of the existing firmware model
     * @throws DeviceManagementDAOException if an error occurs while accessing the database
     */
    DeviceFirmwareModel getExistingFirmwareModel(String firmwareModel, int tenantId) throws DeviceManagementDAOException;

    /**
     * Saves the mapping between a device and a firmware model.
     * @param deviceId the ID of the device to be mapped
     * @param firmwareId the ID of the firmware model to be mapped
     * @param tenantId the ID of the tenant to which the device belongs
     * @return true if the mapping was successfully added, false otherwise
     * @throws DeviceManagementDAOException if an error occurs while accessing the database
     */
    boolean addDeviceFirmwareMapping(int deviceId, int firmwareId, int tenantId) throws DeviceManagementDAOException;
}
