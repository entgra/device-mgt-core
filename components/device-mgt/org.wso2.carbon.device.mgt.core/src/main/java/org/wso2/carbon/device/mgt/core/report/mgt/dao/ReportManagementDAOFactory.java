package org.wso2.carbon.device.mgt.core.report.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.NotificationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.report.mgt.dao.impl.SQLServerReportDAOImpl;

import javax.sql.DataSource;
import java.sql.Connection;

public class ReportManagementDAOFactory {

    private static DataSource dataSource;
    private static String databaseEngine;
    private static final Log log = LogFactory.getLog(NotificationManagementDAOFactory.class);
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public static ReportDAO getReportDAO() {
        return new SQLServerReportDAOImpl();
    }
}
