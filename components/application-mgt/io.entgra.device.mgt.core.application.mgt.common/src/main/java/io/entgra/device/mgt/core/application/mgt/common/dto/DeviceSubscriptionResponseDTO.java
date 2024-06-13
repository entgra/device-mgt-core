package io.entgra.device.mgt.core.application.mgt.common.dto;

import io.entgra.device.mgt.core.application.mgt.common.CategorizedSubscriptionResult;

import java.util.Map;

public class DeviceSubscriptionResponseDTO {
    private int deviceCount;
    private Map<String, Double> statusPercentages;
    private CategorizedSubscriptionResult devices;

    public DeviceSubscriptionResponseDTO(int deviceCount, Map<String, Double> statusPercentages,
                                         CategorizedSubscriptionResult devices) {
        this.deviceCount = deviceCount;
        this.statusPercentages = statusPercentages;
        this.devices = devices;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public Map<String, Double> getStatusPercentages() {
        return statusPercentages;
    }

    public void setStatusPercentages(Map<String, Double> statusPercentages) {
        this.statusPercentages = statusPercentages;
    }

    public CategorizedSubscriptionResult getDevices() {
        return devices;
    }

    public void setDevices(CategorizedSubscriptionResult devices) {
        this.devices = devices;
    }
}
