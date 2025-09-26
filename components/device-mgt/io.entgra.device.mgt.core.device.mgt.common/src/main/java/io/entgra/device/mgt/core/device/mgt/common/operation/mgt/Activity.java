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


package io.entgra.device.mgt.core.device.mgt.common.operation.mgt;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(value = "Activity", description = "An activity instance carries a unique identifier that can be " +
        "used to identify a particular operation instance uniquely")
public class Activity {

    public enum Type {
        CONFIG, MESSAGE, INFO, COMMAND, PROFILE, POLICY
    }

    @ApiModelProperty(
            name = "activityId",
            value = "Activity identifier",
            required = true,
            example = "ACTIVITY_1")
    @JsonProperty("activityId")
    private String activityId;

    @ApiModelProperty(
            name = "code",
            value = "Activity code",
            required = true,
            example = "DEVICE_RING")
    @JsonProperty("code")
    private String code;

    @ApiModelProperty(
            name = "operationId",
            value = "Operation Id",
            required = false,
            example = "10")
    @JsonProperty("operationId")
    private int operationId;

    @ApiModelProperty(
            name = "type",
            value = "Activity type",
            required = true,
            allowableValues = "CONFIG, MESSAGE, INFO, COMMAND, PROFILE, POLICY",
            example = "COMMAND")
    @JsonProperty("type")
    private Type type;

    @ApiModelProperty(
            name = "createdTimeStamp",
            value = "Timestamp recorded when the activity took place",
            required = true,
            example = "Thu Oct 06 11:18:47 IST 2016")
    @JsonProperty("createdTimestamp")
    private String createdTimeStamp;

    @ApiModelProperty(
            name = "activityStatuses",
            value = "Collection of statuses corresponding to the activity",
            required = true)
    @JsonProperty("activityStatuses")
    private List<ActivityStatus> activityStatus;

    @ApiModelProperty(
            name = "initiatedBy",
            value = "Initiated user",
            required = true)
    @JsonProperty("initiatedBy")
    private String initiatedBy;

    @ApiModelProperty(name = "appName", value = "App Name.")
    private String appName;

    @ApiModelProperty(name = "packageName",
            value = "package name of the application")
    private String packageName;
    @ApiModelProperty(name = "username",
            value = "username of subscribed person")
    private String username;
    @ApiModelProperty(name = "status",
            value = "Status of app install")
    private String status;

    @ApiModelProperty(name = "version",
            value = "Version of app")
    private String version;

    @ApiModelProperty(name = "triggeredBy",
            value = "Operation triggered by what")
    private String triggeredBy;

    @ApiModelProperty(name = "appType",
            value = "Type of application")
    private String appType;


    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(String createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public List<ActivityStatus> getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(List<ActivityStatus> activityStatus) {
        this.activityStatus = activityStatus;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Activity)) return false;
        Activity activity = (Activity) o;
        return Objects.equals(activityId, activity.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(activityId);
    }
}

