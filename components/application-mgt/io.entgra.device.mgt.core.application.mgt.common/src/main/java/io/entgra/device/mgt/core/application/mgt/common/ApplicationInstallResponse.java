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
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class ApplicationInstallResponse {
    @ApiModelProperty(
            name = "ignoredDeviceIdentifiers",
            value = "List of devices that application release is already installed.",
            dataType = "List[io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier]"
    )
    private List<DeviceWithSubscriptionStatus> ignoredDeviceIdentifiers;

    @ApiModelProperty(
            name = "errorDevices",
            value = "List of devices that either device identity is not exist or device type doesn't compatible with the supported device type of the .",
            dataType = "List[io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier]"
    )
    private List<DeviceIdentifier> errorDeviceIdentifiers;


    @ApiModelProperty(
            name = "activity",
            value = "Activity corresponding to the operation"
    )
    private List<Activity> activities;

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activity) {
        this.activities = activity;
    }

    public List<DeviceWithSubscriptionStatus> getIgnoredDeviceIdentifiers() {
        return ignoredDeviceIdentifiers;
    }

    public void setIgnoredDeviceIdentifiers(List<DeviceWithSubscriptionStatus> ignoredDeviceIdentifiers) {
        this.ignoredDeviceIdentifiers = ignoredDeviceIdentifiers;
    }

    public List<DeviceIdentifier> getErrorDeviceIdentifiers() { return errorDeviceIdentifiers; }

    public void setErrorDeviceIdentifiers(List<DeviceIdentifier> errorDeviceIdentifiers) { this.errorDeviceIdentifiers = errorDeviceIdentifiers; }
}
