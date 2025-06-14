package io.entgra.device.mgt.core.device.mgt.common.app.mgt;

public class DeviceFirmwareModel {
    private int firmwareId;
    private String firmwareModelName;
    private String description;
    private String deviceType;

    public int getFirmwareId() {
        return firmwareId;
    }

    public void setFirmwareId(int firmwareId) {
        this.firmwareId = firmwareId;
    }

    public String getFirmwareModelName() {
        return firmwareModelName;
    }

    public void setFirmwareModelName(String firmwareModelName) {
        this.firmwareModelName = firmwareModelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
