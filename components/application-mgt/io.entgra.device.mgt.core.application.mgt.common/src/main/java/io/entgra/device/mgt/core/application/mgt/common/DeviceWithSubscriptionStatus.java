package io.entgra.device.mgt.core.application.mgt.common;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;

public class DeviceWithSubscriptionStatus {
    private DeviceIdentifier deviceIdentifier;
    private String subscriptionStatus;

    public DeviceWithSubscriptionStatus(DeviceIdentifier deviceIdentifier, String subscriptionStatus) {
        this.deviceIdentifier = deviceIdentifier;
        this.subscriptionStatus = subscriptionStatus;
    }

    public DeviceWithSubscriptionStatus(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }
}
