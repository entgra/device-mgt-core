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
        private long sendBatchNotification;

        @JsonProperty("includeDeviceListInBatch")
        private boolean includeDeviceListInBatch = true;

    public boolean isIncludeDeviceListInBatch() {
        return includeDeviceListInBatch;
    }

    public void setIncludeDeviceListInBatch(boolean includeDeviceListInBatch) {
        this.includeDeviceListInBatch = includeDeviceListInBatch;
    }

    public Duration getSendBatchNotification() {
        return Duration.ofSeconds(sendBatchNotification);
    }

    public void setSendBatchNotification(Duration sendBatchNotification) {
        this.sendBatchNotification = sendBatchNotification.getSeconds();
    }
}
