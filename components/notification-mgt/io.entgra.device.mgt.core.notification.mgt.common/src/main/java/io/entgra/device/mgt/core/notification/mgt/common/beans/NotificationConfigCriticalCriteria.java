package io.entgra.device.mgt.core.notification.mgt.common.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NotificationConfigCriticalCriteria {
        @JsonProperty("status")
        private boolean status;

        @JsonProperty("criticalCriteria")
        private List<String> criticalCriteria;

}
