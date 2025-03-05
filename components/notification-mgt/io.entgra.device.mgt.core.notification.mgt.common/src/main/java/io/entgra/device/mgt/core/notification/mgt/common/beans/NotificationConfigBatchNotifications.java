package io.entgra.device.mgt.core.notification.mgt.common.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.List;

public class NotificationConfigBatchNotifications {
        @JsonProperty("enabled")
        private boolean enabled = true;

        @JsonProperty("groupingCriteria")
        private List<String> groupingCriteria = List.of("operationCode", "deviceGroup");

        @JsonProperty("sendBatchNotification")
        private Duration sendBatchNotification;

        @JsonProperty("includeDeviceListInBatch")
        private boolean includeDeviceListInBatch = true;

}
