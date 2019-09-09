package org.wso2.carbon.device.mgt.core.report.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.report.mgt.ReportManagementException;
import org.wso2.carbon.device.mgt.common.report.mgt.ReportManagementService;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.report.mgt.dao.ReportDAO;
import org.wso2.carbon.device.mgt.core.report.mgt.dao.ReportManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportManagementServiceImpl implements ReportManagementService {

    private static final Log log = LogFactory.getLog(ReportManagementServiceImpl.class);

    private ReportDAO reportDAO;
    private DeviceDAO deviceDAO;

    public ReportManagementServiceImpl() {
        this.reportDAO = ReportManagementDAOFactory.getReportDAO();
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
    }

    @Override
    public PaginationResult getDevicesByDuration(PaginationRequest request, String fromDate, String toDate) throws ReportManagementException {
        List<Device> devices = new ArrayList<>();
        PaginationResult paginationResult = new PaginationResult();
        try {
            request = DeviceManagerUtil.validateDeviceListPageSize(request);
            DeviceManagementDAOFactory.openConnection();
            devices = deviceDAO.getDevicesByDuration(request, DeviceManagementDAOUtil.getTenantId(), fromDate, toDate);
            paginationResult.setData(devices);
            paginationResult.setRecordsTotal(devices.size());
            return paginationResult;
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection " +
                    "to the data source";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating device list page size";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
