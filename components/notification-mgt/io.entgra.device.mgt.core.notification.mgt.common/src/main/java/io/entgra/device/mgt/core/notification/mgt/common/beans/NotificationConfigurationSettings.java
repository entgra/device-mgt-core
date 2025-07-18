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

import java.time.Duration;
import java.util.List;

public class NotificationConfigurationSettings {
    @JsonProperty("criticalCriteriaOnly")
    private NotificationConfigCriticalCriteria criticalCriteriaOnly;

    @JsonProperty("batchNotifications")
    private NotificationConfigBatchNotifications batchNotifications;

    @JsonProperty("pendingNotifyAgainIn")
    private long pendingNotifyAgainIn;

    @JsonProperty("archiveType")
    private String archiveType;

    @JsonProperty("archiveAfter")
    private String archiveAfter;

    @JsonProperty("deviceTypes")
    private List<String> deviceTypes;

    @JsonProperty("notificationTriggerPoints")
    private List<String> notificationTriggerPoints;

    public NotificationConfigCriticalCriteria getCriticalCriteriaOnly() {
        return criticalCriteriaOnly;
    }

    public void setCriticalCriteriaOnly(NotificationConfigCriticalCriteria criticalCriteriaOnly) {
        this.criticalCriteriaOnly = criticalCriteriaOnly;
    }

    public NotificationConfigBatchNotifications getBatchNotifications() {
        return batchNotifications;
    }

    public void setBatchNotifications(NotificationConfigBatchNotifications batchNotifications) {
        this.batchNotifications = batchNotifications;
    }

    public Duration getPendingNotifyAgainIn() {
        return Duration.ofSeconds(pendingNotifyAgainIn);
    }

    public void setPendingNotifyAgainIn(Duration pendingNotifyAgainIn) {
        this.pendingNotifyAgainIn = pendingNotifyAgainIn.getSeconds();
    }

    public String getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(String archiveType) {
        this.archiveType = archiveType;
    }

    public String getArchiveAfter() {
        return archiveAfter;
    }

    public void setArchiveAfter(String archiveAfter) {
        this.archiveAfter = archiveAfter;
    }

    public List<String> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public List<String> getNotificationTriggerPoints() {
        return notificationTriggerPoints;
    }

    public void setNotificationTriggerPoints(List<String> notificationTriggerPoints) {
        this.notificationTriggerPoints = notificationTriggerPoints;
    }
}
