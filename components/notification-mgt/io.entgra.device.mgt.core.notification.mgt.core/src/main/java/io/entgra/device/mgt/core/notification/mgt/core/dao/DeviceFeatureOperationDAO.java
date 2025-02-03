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

package io.entgra.device.mgt.core.notification.mgt.core.dao;

import io.entgra.device.mgt.core.notification.mgt.common.dto.DeviceFeatureInfo;
import io.entgra.device.mgt.core.notification.mgt.common.exception.DeviceFeatureOperationException;

import java.util.List;

public interface DeviceFeatureOperationDAO {
    /**
     * Updates or inserts device feature details into the DM_OPERATION_DETAILS table.
     *
     * @param deviceFeatureInfoList A list of {@link DeviceFeatureInfo} to be updated or inserted.
     * @throws DeviceFeatureOperationException If any error occurs while processing the update.
     */
    void updateDeviceFeatureDetails(List<DeviceFeatureInfo> deviceFeatureInfoList) throws DeviceFeatureOperationException;
}
