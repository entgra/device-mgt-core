package org.wso2.carbon.device.mgt.core.report.mgt.dao;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.report.mgt.Report;
import org.wso2.carbon.device.mgt.common.report.mgt.ReportManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;

import java.util.List;

public interface ReportDAO {

    //get all reports for user
    //using notification as dummy
   // List<Report> getAllReports(PaginationRequest request , int tenantId) throws NotificationManagementException;

  //  List<Report> getAllReports(int tenentId) throws NotificationManagementException;

    List<Report> getAllReports() throws NotificationManagementException;

    String getAllReportsString() throws NotificationManagementException;

    List<Device> getDevicesByDuration(String fromDate, String toDate) throws ReportManagementException;
}