package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.api.service;

import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.beans.TicketingClientDeviceInfo;

import java.io.IOException;

public interface DeviceAPIClientService {
    String sendToClient(TicketingClientDeviceInfo deviceInfo) throws IOException;
}
