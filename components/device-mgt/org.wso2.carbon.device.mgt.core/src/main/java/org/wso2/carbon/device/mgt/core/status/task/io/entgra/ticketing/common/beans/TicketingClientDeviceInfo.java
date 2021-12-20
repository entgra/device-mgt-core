package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.beans;

public class TicketingClientDeviceInfo {
    private String subject;
    private String deviceType;
    private String deviceIdentifier;
    private int deviceId;
    private String deviceName;

    public TicketingClientDeviceInfo(String subject, String deviceType, String deviceIdentifier, int deviceId,
                                     String deviceName){
        this.subject=subject;
        this.deviceType=deviceType;
        this.deviceIdentifier =deviceIdentifier;
        this.deviceId =deviceId;
        this.deviceName =deviceName;
    }

    public TicketingClientDeviceInfo(){ }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDeviceType() { return deviceType; }

    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
