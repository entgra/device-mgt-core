package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationConfigurationSettings {
        @JsonProperty("criticalCriteriaOnly")
        private NotificationConfig.CriticalCriteriaOnly criticalCriteriaOnly = new NotificationConfig.CriticalCriteriaOnly();

        @JsonProperty("batchNotifications")
        private NotificationConfig.BatchNotifications batchNotifications = new NotificationConfig.BatchNotifications();

        @JsonProperty("pendingNotifyAgainIn")
        private String pendingNotifyAgainIn = "01 weeks";

}
