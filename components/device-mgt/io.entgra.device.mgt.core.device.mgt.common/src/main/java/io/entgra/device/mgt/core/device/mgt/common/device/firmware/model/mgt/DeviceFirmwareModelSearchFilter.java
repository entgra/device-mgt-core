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
package io.entgra.device.mgt.core.device.mgt.common.device.firmware.model.mgt;

import java.util.List;
import java.util.Map;

public class DeviceFirmwareModelSearchFilter {
    private List<Integer> firmwareModelIds;
    private List<String> firmwareVersions;
    private String firmwareModelName;
    private String deviceIdentifier;
    private String firmwareVersion;
    private int offset = -1;
    private int limit = -1;
    private int groupId;
    private Map<String, String> customProperty;
    private DeviceFirmwareResult deviceFirmwareResult;


    public String getFirmwareModelName() {
        return firmwareModelName;
    }

    public void setFirmwareModelName(String firmwareModelName) {
        this.firmwareModelName = firmwareModelName;
    }

    public List<Integer> getFirmwareModelIds() {
        return firmwareModelIds;
    }

    public void setFirmwareModelIds(List<Integer> firmwareModelIds) {
        this.firmwareModelIds = firmwareModelIds;
    }

    public List<String> getFirmwareVersions() {
        return firmwareVersions;
    }

    public void setFirmwareVersions(List<String> firmwareVersions) {
        this.firmwareVersions = firmwareVersions;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Map<String, String> getCustomProperty() {
        return customProperty;
    }

    public void setCustomProperty(Map<String, String> customProperty) {
        this.customProperty = customProperty;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public DeviceFirmwareResult getDeviceFirmwareResult() {
        return deviceFirmwareResult;
    }

    public void setDeviceFirmwareResult(DeviceFirmwareResult deviceFirmwareResult) {
        this.deviceFirmwareResult = deviceFirmwareResult;
    }

    @Override
    public String toString() {
        return "DeviceFirmwareModelSearchFilter{" +
                "firmwareModelIds=" + firmwareModelIds +
                ", firmwareVersions=" + firmwareVersions +
                ", firmwareModelName='" + firmwareModelName + '\'' +
                ", deviceIdentifier='" + deviceIdentifier + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", offset=" + offset +
                ", limit=" + limit +
                ", groupId=" + groupId +
                ", customProperty=" + customProperty +
                ", deviceFirmwareResult=" + deviceFirmwareResult +
                '}';
    }
}
