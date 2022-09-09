/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.common;

import com.google.gson.Gson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistorySnapshotWrapper;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "Device", description = "This class carries all information related to a managed device.")
public class Device implements Serializable {

    private static final long serialVersionUID = 1998101711L;

    @ApiModelProperty(name = "id", value = "ID of the device in the device information database.",
            required = true)
    private int id;

    @ApiModelProperty(name = "name", value = "The device name that can be set on the device by the device user.",
            required = true)
    private String name;

    @ApiModelProperty(name = "type", value = "The OS type of the device.", required = true)
    private String type;

    @ApiModelProperty(name = "description", value = "Additional information on the device.", required = true)
    private String description;

    @ApiModelProperty(name = "deviceIdentifier", value = "This is a 64-bit number (as a hex string) that is randomly" +
            " generated when the user first sets up the device and should" +
            " remain constant for the lifetime of the user's device." +
            " The value may change if a factory reset is performed on " +
            "the device.",
            required = true)
    private String deviceIdentifier;

    @ApiModelProperty(name = "enrolmentInfo", value = "This defines the device registration related information. " +
            "It is mandatory to define this information.", required = true)
    private EnrolmentInfo enrolmentInfo;

    @ApiModelProperty(name = "features", value = "List of features.", required = true)
    private List<Feature> features;

    private List<Device.Property> properties;

    @ApiModelProperty(name = "advanceInfo", value = "This defines the device registration related information. " +
            "It is mandatory to define this information.", required = false)
    private DeviceInfo deviceInfo;

    @ApiModelProperty(name = "applications", value = "This represents the application list installed into the device",
    required = false)
    private List<Application> applications;

    @ApiModelProperty(name = "cost", value = "Cost charged per device.", required = false)
    private double cost;

    @ApiModelProperty(name = "daysUsed", value = "Number of days gone since device enrollment.",
            required = false)
    private int daysUsed;

    @ApiModelProperty(name = "deviceStatusInfo", value = "This defines the device status details. " +
            "It is mandatory to define this information.", required = false)
    private List<DeviceStatus> deviceStatusInfo = new ArrayList<>();

    @ApiModelProperty(
            name = "historySnapshot",
            value = "device history snapshots")
    @JsonProperty(value = "historySnapshot")
    private DeviceLocationHistorySnapshotWrapper historySnapshot;

    public Device() {
    }

    public Device(String name, String type, String description, String deviceId, EnrolmentInfo enrolmentInfo,
                  List<Feature> features, List<Property> properties) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.deviceIdentifier = deviceId;
        this.enrolmentInfo = enrolmentInfo;
        this.features = features;
        this.properties = properties;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getDaysUsed() {
        return daysUsed;
    }

    public void setDaysUsed(int daysUsed) {
        this.daysUsed = daysUsed;
    }

    public List<DeviceStatus> getDeviceStatusInfo() {
        return deviceStatusInfo;
    }

    public void setDeviceStatusInfo(List<DeviceStatus> deviceStatusInfo) {
        this.deviceStatusInfo = deviceStatusInfo;
    }

    public Device(String deviceId) {
        this.deviceIdentifier = deviceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public EnrolmentInfo getEnrolmentInfo() {
        return enrolmentInfo;
    }

    public void setEnrolmentInfo(EnrolmentInfo enrolmentInfo) {
        this.enrolmentInfo = enrolmentInfo;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public List<Device.Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Device.Property> properties) {
        this.properties = properties;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public DeviceLocationHistorySnapshotWrapper getHistorySnapshot() {
        return historySnapshot;
    }

    public void setHistorySnapshot(DeviceLocationHistorySnapshotWrapper historySnapshot) {
        this.historySnapshot = historySnapshot;
    }

    public static class Property {

        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Device))
            return false;

        Device device = (Device) o;

        return getDeviceIdentifier().equals(device.getDeviceIdentifier());

    }

    @Override
    public int hashCode() {
        return getDeviceIdentifier().hashCode();
    }

}
