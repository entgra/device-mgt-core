/*
 *  Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.dto.DeviceFeatureInfo;

import java.util.List;

public interface DeviceFeatureOperationDAO {
    /**
     * Updates or inserts device feature details into the DM_OPERATION_DETAILS table.
     * @param deviceFeatureInfoList A list of {@link DeviceFeatureInfo} to be updated or inserted.
     * @throws DeviceManagementDAOException If any error occurs while processing the update.
     */
    void updateDeviceFeatureDetails(List<DeviceFeatureInfo> deviceFeatureInfoList)
            throws DeviceManagementDAOException;

    /**
     * Retrieves a list of device operation details filtered by operation code, operation name, and/or device type.
     * If any of the provided parameters are {@code null} or empty, they will be ignored in the filtering process.
     * This allows for partial filtering or fetching all records when no filters are provided.
     * @param code the operation code to filter by (optional).
     * @param name the operation name to filter by (optional).
     * @param type the device type to filter by (optional).
     * @return a list of {@link DeviceFeatureInfo} objects that match the provided filters.
     * @throws DeviceManagementDAOException if an error occurs while accessing the database.
     */
    List<DeviceFeatureInfo> getOperationDetails(String code, String name, String type)
            throws DeviceManagementDAOException;

}
