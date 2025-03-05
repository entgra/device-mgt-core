package io.entgra.device.mgt.core.notification.mgt.common.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

public class NotificationConfigurationSettings {
        @JsonProperty("criticalCriteriaOnly")
        private NotificationConfigCriticalCriteria criticalCriteriaOnly;

        @JsonProperty("batchNotifications")
        private NotificationConfigBatchNotifications batchNotifications;

        @JsonProperty("pendingNotifyAgainIn")
        private Duration pendingNotifyAgainIn;

}
