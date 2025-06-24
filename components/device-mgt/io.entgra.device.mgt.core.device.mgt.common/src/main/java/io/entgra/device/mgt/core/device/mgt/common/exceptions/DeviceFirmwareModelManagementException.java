package io.entgra.device.mgt.core.device.mgt.common.exceptions;

public class DeviceFirmwareModelManagementException extends Exception {
    public DeviceFirmwareModelManagementException(String message) {
        super(message);
    }

    public DeviceFirmwareModelManagementException(String message, Throwable cause) {
      super(message, cause);
    }
}
