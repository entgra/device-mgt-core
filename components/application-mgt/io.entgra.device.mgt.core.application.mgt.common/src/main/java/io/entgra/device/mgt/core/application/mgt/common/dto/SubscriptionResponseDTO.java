package io.entgra.device.mgt.core.application.mgt.common.dto;

import java.util.List;

public class SubscriptionResponseDTO {

    private String UUID;
    private List<SubscriptionsDTO> subscriptions;
    private List<DeviceOperationDTO> DevicesOperations;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public List<DeviceOperationDTO> getDevicesOperations() {
        return DevicesOperations;
    }

    public void setDevicesOperations(List<DeviceOperationDTO> devicesOperations) {
        DevicesOperations = devicesOperations;
    }

    public List<SubscriptionsDTO> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<SubscriptionsDTO> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
