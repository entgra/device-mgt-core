package org.wso2.carbon.device.mgt.common.report.mgt;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;

public interface ReportManagementService {

    /**
     * This method is used to call the getDevicesByDuration method from DeviceDAO
     * @param request
     * @param fromDate
     * @param toDate
     * @return PaginationResult
     * @throws DeviceManagementException
     * @throws ReportManagementException
     */
    PaginationResult getDevicesByDuration(PaginationRequest request, String fromDate, String toDate) throws ReportManagementException;
}
