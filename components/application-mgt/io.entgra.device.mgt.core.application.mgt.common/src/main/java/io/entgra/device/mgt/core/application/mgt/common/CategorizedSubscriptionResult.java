package io.entgra.device.mgt.core.application.mgt.common;

import java.util.List;

public class CategorizedSubscriptionResult {
    private List<DeviceSubscriptionData> installedDevices;
    private List<DeviceSubscriptionData> pendingDevices;
    private List<DeviceSubscriptionData> errorDevices;

    public CategorizedSubscriptionResult(List<DeviceSubscriptionData> installedDevices,
                                         List<DeviceSubscriptionData> pendingDevices,
                                         List<DeviceSubscriptionData> errorDevices) {
        this.installedDevices = installedDevices;
        this.pendingDevices = pendingDevices;
        this.errorDevices = errorDevices;
    }

    public List<DeviceSubscriptionData> getInstalledDevices() {
        return installedDevices;
    }

    public void setInstalledDevices(List<DeviceSubscriptionData> installedDevices) {
        this.installedDevices = installedDevices;
    }

    public List<DeviceSubscriptionData> getPendingDevices() {
        return pendingDevices;
    }

    public void setPendingDevices(List<DeviceSubscriptionData> pendingDevices) {
        this.pendingDevices = pendingDevices;
    }

    public List<DeviceSubscriptionData> getErrorDevices() {
        return errorDevices;
    }

    public void setErrorDevices(List<DeviceSubscriptionData> errorDevices) {
        this.errorDevices = errorDevices;
    }
}
