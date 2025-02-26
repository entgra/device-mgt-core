package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NotificationConfigBatchNotifications {
        @JsonProperty("enabled")
        private boolean enabled = true;

        @JsonProperty("groupingCriteria")
        private List<String> groupingCriteria = List.of("operationCode", "deviceGroup");

        @JsonProperty("sendBatchNotification")
        private String sendBatchNotification = "after 1 hours";

        @JsonProperty("includeDeviceListInBatch")
        private boolean includeDeviceListInBatch = true;

}
