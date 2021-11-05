package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common;

import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.beans.TicketingClientDeviceInfo;

import java.io.IOException;

public interface TicketingClient {
    String createIssue(TicketingClientDeviceInfo deviceInfo) throws IOException;
}
