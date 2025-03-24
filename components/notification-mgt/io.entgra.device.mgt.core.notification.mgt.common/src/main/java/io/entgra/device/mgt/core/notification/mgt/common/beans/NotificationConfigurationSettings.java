package io.entgra.device.mgt.core.notification.mgt.common.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

public class NotificationConfigurationSettings {
        @JsonProperty("criticalCriteriaOnly")
        private NotificationConfigCriticalCriteria criticalCriteriaOnly;

        @JsonProperty("batchNotifications")
        private NotificationConfigBatchNotifications batchNotifications;

        @JsonProperty("pendingNotifyAgainIn")
        private long pendingNotifyAgainIn;

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
}
