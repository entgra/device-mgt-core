package org.wso2.carbon.device.mgt.core.report.mgt.dao.impl;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.report.mgt.Report;
import org.wso2.carbon.device.mgt.common.report.mgt.ReportManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.report.mgt.dao.ReportDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/***
 * This class hold SQL database implementations for Report generation and management
 */
public class SQLServerReportDAOImpl implements ReportDAO {

    @Override
    public List<Report> getAllReports() throws NotificationManagementException {
        List<Report> reports = new ArrayList<>();

        Report r1 = new Report();
        r1.setId(1);
        r1.setBody("Report 1");

        Report r2 = new Report();
        r2.setId(2);
        r2.setBody("Report 2");

        Report r3 = new Report();
        r3.setId(3);
        r3.setBody("Report 3");

        reports.add(r1);
        reports.add(r2);
        reports.add(r3);

        return reports;
    }

    @Override
    public String getAllReportsString() throws NotificationManagementException {
        return "String";
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    @Override
    public List<Device> getDevicesByDuration(String fromDate, String toDate) throws ReportManagementException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;

        //The query should be changed to get data between enrolment dates
        try {
            conn = this.getConnection();
            String sql = "SELECT " +
                    "d.ID AS DEVICE_ID,d.DESCRIPTION,d.NAME AS DEVICE_NAME,d.DEVICE_TYPE_ID,d.DEVICE_IDENTIFICATION," +
                    "e.OWNER,e.OWNERSHIP,e.STATUS,e.DATE_OF_LAST_UPDATE,e.DATE_OF_ENROLMENT,e.ID AS ENROLMENT_ID " +
                    "FROM DM_DEVICE AS d , DM_ENROLMENT AS e;";

            stmt = conn.prepareStatement(sql);
            // stmt.setInt(1, tenantId);
            // stmt.setString(2, fromDate);
            // stmt.setString(3, toDate);
            rs = stmt.executeQuery();
            devices = new ArrayList<>();
            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new ReportManagementException("Error occurred while retrieving information of all " +
                    "registered devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }
}
