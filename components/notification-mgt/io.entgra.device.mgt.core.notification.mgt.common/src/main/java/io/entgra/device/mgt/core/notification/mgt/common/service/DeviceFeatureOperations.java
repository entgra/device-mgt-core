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

package io.entgra.device.mgt.core.notification.mgt.common.service;

import io.entgra.device.mgt.core.notification.mgt.common.dto.DeviceFeatureInfo;
import io.entgra.device.mgt.core.notification.mgt.common.exception.DeviceFeatureOperationException;

import java.util.List;

/**
 * Interface representing operations for device features.
 * This interface provides methods to retrieve device feature operations.
 */
public interface DeviceFeatureOperations {

    /**
     * Retrieves a list of device feature operations based on the device configuration.
     *
     * @return A list of {@link DeviceFeatureInfo} objects representing device feature operations.
     * @throws DeviceFeatureOperationException If there is an error while fetching device feature operations.
     */
    List<DeviceFeatureInfo> getDeviceFeatureOperations() throws DeviceFeatureOperationException;
}

