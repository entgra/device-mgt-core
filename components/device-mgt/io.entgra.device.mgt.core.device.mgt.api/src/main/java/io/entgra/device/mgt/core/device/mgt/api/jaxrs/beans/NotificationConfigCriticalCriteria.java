package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NotificationConfigCriticalCriteria {
        @JsonProperty("status")
        private boolean status = false;

        @JsonProperty("criticalCriteria")
        private List<String> criticalCriteria;


}
