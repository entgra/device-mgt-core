/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.common.device.firmware.model.mgt;

import io.entgra.device.mgt.core.device.mgt.common.app.mgt.DeviceFirmwareModel;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceFirmwareModelManagementException;

import java.util.List;

// todo: add comments
public interface DeviceFirmwareModelManagementService {
    DeviceFirmwareModel createDeviceFirmwareModel(DeviceFirmwareModel deviceFirmwareModel) throws DeviceFirmwareModelManagementException;
    DeviceFirmwareModel getDeviceFirmwareModelByFirmwareModelName(String firmwareModelName) throws DeviceFirmwareModelManagementException;
    List<DeviceFirmwareModel> getFirmwareModelsByDeviceType(String deviceType) throws DeviceFirmwareModelManagementException;

    /**
     * Adds a new firmware version of a device
     * @param deviceId the ID of the device to which the firmware version is to be added
     * @param firmwareVersion the firmware version to be added
     * @return true if the firmware version was added successfully, false otherwise
     * @throws DeviceFirmwareModelManagementException if an error occurs while adding the firmware version
     */
    boolean addDeviceFirmwareVersion(int deviceId, String firmwareVersion) throws DeviceFirmwareModelManagementException;
}
