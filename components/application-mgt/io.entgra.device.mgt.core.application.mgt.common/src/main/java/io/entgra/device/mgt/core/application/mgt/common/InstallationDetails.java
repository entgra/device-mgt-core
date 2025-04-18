/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.application.mgt.common;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class InstallationDetails {
    @ApiModelProperty(
            name = "applicationUUID",
            value = "Application ID",
            required = true
    )
    private String applicationUUID;

    @ApiModelProperty(
            name = "deviceIdentifiers",
            value = "List of device identifiers.",
            required = true,
            dataType = "List[io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier]"
    )
    private List<DeviceIdentifier> deviceIdentifiers;

    public String getApplicationUUID() {
        return applicationUUID;
    }

    public void setApplicationUUID(String applicationUUID) {
        this.applicationUUID = applicationUUID;
    }

    public List<DeviceIdentifier> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifiers(List<DeviceIdentifier> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }
}
