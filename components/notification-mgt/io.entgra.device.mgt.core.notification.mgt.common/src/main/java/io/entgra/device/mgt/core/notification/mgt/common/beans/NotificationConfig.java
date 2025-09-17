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
package io.entgra.device.mgt.core.notification.mgt.common.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "NotificationConfig", description = "Notification Configurations")
public class NotificationConfig {

    @ApiModelProperty(name = "configId", value = "The unique ID of the notification configuration.",
            required = true)
    private int id;

    @ApiModelProperty(name = "deviceType", value = "Device type of the notification configuration.",
            required = true)
    private String deviceType;

    @ApiModelProperty(name = "configName", value = "The name of the notification configuration.",
            required = true)
    private String name;

    @ApiModelProperty(name = "description", value = "The description of the notification configuration.",
            required = true)
    private String description;

    @ApiModelProperty(name = "type", value = "The category of the notification (operation or task).",
            required = true)
    private String type;

    @ApiModelProperty(name = "code", value = "The operation or task code associated with the notification.",
            required = true)
    private String code;

    @ApiModelProperty(name = "enabled", value = "Indicates whether this notification configuration is enabled.",
            required = true)
    private boolean enabled = true;

    @ApiModelProperty(name = "recipients", value = "Details of the recipients of the notification.",
            required = true)
    private NotificationConfigRecipients recipients;

    @ApiModelProperty(name = "configuredBy", value = "Information about who configured the notification.",
            required = true)
    private ConfiguredBy configuredBy;

    @ApiModelProperty(name = "notificationSettings", value = "Settings for how notifications are sent.",
            required = true)
    private NotificationConfigurationSettings notificationSettings;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public NotificationConfigRecipients getRecipients() {
        return recipients;
    }

    public void setRecipients(NotificationConfigRecipients recipients) {
        this.recipients = recipients;
    }

    public ConfiguredBy getConfiguredBy() {
        return configuredBy;
    }

    public void setConfiguredBy(ConfiguredBy configuredBy) {
        this.configuredBy = configuredBy;
    }

    public NotificationConfigurationSettings getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(NotificationConfigurationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static class ConfiguredBy {
        @JsonProperty("user")
        private String user;

        @JsonProperty("lastModifiedAt")
        private String lastModifiedAt;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getLastModifiedAt() {
            return lastModifiedAt;
        }

        public void setLastModifiedAt(String lastModifiedAt) {
            this.lastModifiedAt = lastModifiedAt;
        }
    }
}
