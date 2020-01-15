package org.wso2.carbon.device.mgt.common;

import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;

import java.util.List;

public interface PolicyConfigurationManager {

    List<Policy> getPolicies() throws DeviceManagementException;

}
