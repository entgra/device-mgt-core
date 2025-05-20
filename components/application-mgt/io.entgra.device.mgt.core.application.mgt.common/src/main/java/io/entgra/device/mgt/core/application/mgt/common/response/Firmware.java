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
package io.entgra.device.mgt.core.application.mgt.common.response;

import io.entgra.device.mgt.core.application.mgt.common.OperationStatusBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "ApplicationReleaseDTO", description = "This class holds the details when releasing an ApplicationDTO to application store")
public class Firmware {

    @ApiModelProperty(name = "versionId",
            required = true,
            value = "Id of the application release (i.e Firmware)")
    private int versionId;

    @ApiModelProperty(name = "description",
            value = "Description of the application release")
    private String description;

    @ApiModelProperty(name = "firmwareVersion",
            required = true,
            value = "Version of the application release (i.e Firmware)")
    private String firmwareVersion;

    @ApiModelProperty(name = "firmwareReleaseId",
            required = true,
            value = "UUID of the application release")
    private String firmwareReleaseId;

    @ApiModelProperty(name = "installerPath",
            required = true,
            value = "ApplicationDTO storing location")
    private String downloadUrl;

    @ApiModelProperty(name = "iconPath",
            value = "icon file storing location")
    private String iconPath;

    @ApiModelProperty(name = "releaseChannel",
            value = "Release type of the application release",
            required = true,
            example = "TEST, PRODUCTION")
    private String releaseChannel;

    @ApiModelProperty(name = "currentStatus",
            value = "Current Status of the Application Release.",
            required = true,
            example = "CREATED, IN-REVIEW, PUBLISHED etc")
    private String currentStatus;

    @ApiModelProperty(name = "packageName",
            required = true,
            value = "package name of the application")
    private String packageName;

    @ApiModelProperty(name = "operationStatus",
            value = "Indicates the current status of the firmware operation. Required when the operation is in a " +
                    "pending state, depending on the specific use case.")
    private OperationStatusBean operationStatus;

    @ApiModelProperty(name = "deviceModels",
            required = true,
            value = "List of device models that the firmware is supported.")
    private List<String> deviceModels;

    @ApiModelProperty(name = "buildNumber",
            value = "Build number of the firmware.")
    private String buildNumber;

    @ApiModelProperty(name = "fileSize",
            value = "File size of the firmware.")
    private long fileSize;

    public String getReleaseChannel() {
        return releaseChannel;
    }

    public void setReleaseChannel(String releaseChannel) {
        this.releaseChannel = releaseChannel;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getFirmwareVersion() { return firmwareVersion; }

    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

    public String getFirmwareReleaseId() { return firmwareReleaseId; }

    public void setFirmwareReleaseId(String firmwareReleaseId) { this.firmwareReleaseId = firmwareReleaseId; }

    public String getCurrentStatus() { return currentStatus; }

    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getPackageName() { return packageName; }

    public void setPackageName(String packageName) { this.packageName = packageName; }

    public OperationStatusBean getOperationStatus() { return operationStatus; }

    public void setOperationStatus(OperationStatusBean operationStatus) { this.operationStatus = operationStatus; }

    public int getVersionId() { return versionId; }

    public void setVersionId(int versionId) { this.versionId = versionId; }

    public List<String> getDeviceModels() { return deviceModels; }

    public void setDeviceModels(List<String> deviceModels) { this.deviceModels = deviceModels; }

    public String getDownloadUrl() { return downloadUrl; }

    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getIconPath() { return iconPath; }

    public void setIconPath(String iconPath) { this.iconPath = iconPath; }

    public String getBuildNumber() { return buildNumber; }

    public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }

    public long getFileSize() { return fileSize; }

    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
}
