package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.LocalDateTime;

public class NotificationConfigurationSettings {
        @JsonProperty("criticalCriteriaOnly")
        private NotificationConfigCriticalCriteria criticalCriteriaOnly;

        @JsonProperty("batchNotifications")
        private NotificationConfigBatchNotifications batchNotifications;

        @JsonProperty("pendingNotifyAgainIn")
        private Duration pendingNotifyAgainIn;

}

