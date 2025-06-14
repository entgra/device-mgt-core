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
package io.entgra.device.mgt.core.device.mgt.common.app.mgt;

public class DeviceFirmwareModel {
    private int firmwareId;
    private String firmwareModelName;
    private String description;
    private String deviceType;

    public DeviceFirmwareModel(String firmwareModelName, String description) {
        this.firmwareModelName = firmwareModelName;
        this.description = description;
    }

    public DeviceFirmwareModel() {}

    public int getFirmwareId() {
        return firmwareId;
    }

    public void setFirmwareId(int firmwareId) {
        this.firmwareId = firmwareId;
    }

    public String getFirmwareModelName() {
        return firmwareModelName;
    }

    public void setFirmwareModelName(String firmwareModelName) {
        this.firmwareModelName = firmwareModelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
