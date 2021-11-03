package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.api.service.impl;

import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.api.service.DeviceAPIClientService;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.TicketingClient;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.api.service.addons.UVDeskClient;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.beans.TicketingClientDeviceInfo;

public class DeviceAPIClientServiceImpl implements DeviceAPIClientService {
    public String sendToClient(TicketingClientDeviceInfo deviceInfo){
        TicketingClient client = new UVDeskClient();
        return (client.createIssue(deviceInfo));
    }
}
