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
package io.entgra.device.mgt.core.realtime.notification.mgt.common.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.List;

public class RealtimeNotificationConfigurationSettings {
    @JsonProperty("criticalCriteriaOnly")
    private RealtimeNotificationConfigCriticalCriteria criticalCriteriaOnly;

    @JsonProperty("batchNotifications")
    private RealtimeNotificationConfigBatchNotifications batchNotifications;

    @JsonProperty("pendingNotifyAgainIn")
    private long pendingNotifyAgainIn;

    @JsonProperty("archiveType")
    private String archiveType;

    @JsonProperty("archiveAfter")
    private RealtimeArchivePeriod archiveAfter;

    @JsonProperty("notificationTriggerPoints")
    private List<String> notificationTriggerPoints;

    public RealtimeNotificationConfigCriticalCriteria getCriticalCriteriaOnly() {
        return criticalCriteriaOnly;
    }

    public void setCriticalCriteriaOnly(RealtimeNotificationConfigCriticalCriteria criticalCriteriaOnly) {
        this.criticalCriteriaOnly = criticalCriteriaOnly;
    }

    public RealtimeNotificationConfigBatchNotifications getBatchNotifications() {
        return batchNotifications;
    }

    public void setBatchNotifications(RealtimeNotificationConfigBatchNotifications batchNotifications) {
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

    public RealtimeArchivePeriod getArchiveAfter() {
        return archiveAfter;
    }

    public void setArchiveAfter(RealtimeArchivePeriod archiveAfter) {
        this.archiveAfter = archiveAfter;
    }

    public List<String> getNotificationTriggerPoints() {
        return notificationTriggerPoints;
    }

    public void setNotificationTriggerPoints(List<String> notificationTriggerPoints) {
        this.notificationTriggerPoints = notificationTriggerPoints;
    }
}
