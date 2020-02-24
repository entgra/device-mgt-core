/*
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.core.report.mgt;

import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Count;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.ReportManagementException;
import org.wso2.carbon.device.mgt.common.report.mgt.ReportManagementService;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * This is the service class for reports which calls dao classes and its method which are used for
 * report generation tasks.
 */
public class ReportManagementServiceImpl implements ReportManagementService {

    private static final Log log = LogFactory.getLog(ReportManagementServiceImpl.class);

    private DeviceDAO deviceDAO;

    public ReportManagementServiceImpl() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
    }

    @Override
    public PaginationResult getDevicesByDuration(PaginationRequest request, String fromDate,
                                                 String toDate)
            throws ReportManagementException {
        PaginationResult paginationResult = new PaginationResult();
        try {
            request = DeviceManagerUtil.validateDeviceListPageSize(request);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating device list page size";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            List<Device> devices = deviceDAO.getDevicesByDuration(
                    request,
                    DeviceManagementDAOUtil.getTenantId(),
                    fromDate,
                    toDate
            );
            paginationResult.setData(devices);
            //TODO: Should change the following code to a seperate count method from deviceDAO to get the count
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
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getDevicesByDurationCount(List<String> statusList, String ownership, String fromDate, String toDate)
            throws ReportManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDevicesByDurationCount(
                    statusList, ownership, fromDate, toDate, DeviceManagementDAOUtil.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred in while retrieving device count by status for " + statusList + "devices.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public JsonObject getCountOfDevicesByDuration(PaginationRequest request, List<String> statusList, String fromDate,
                                                   String toDate)
            throws ReportManagementException {
        try {
            request = DeviceManagerUtil.validateDeviceListPageSize(request);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating device list page size";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            List<Count> dateList = deviceDAO.getCountOfDevicesByDuration(
                    request,
                    statusList,
                    DeviceManagementDAOUtil.getTenantId(),
                    fromDate,
                    toDate
            );
            return buildCount(fromDate, toDate, dateList);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection " +
                    "to the data source";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID between " + fromDate + " to " + toDate;
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Error occurred while building count";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public PaginationResult getDevicesExpiredByOSVersion(PaginationRequest request)
            throws ReportManagementException, DeviceTypeNotFoundException {
        if (request == null ||
            StringUtils.isBlank(request.getDeviceType()) ||
            !request.getProperties().containsKey(Constants.OS_BUILD_DATE) ||
            (Long) request.getProperty(Constants.OS_BUILD_DATE) == 0) {

            String msg = "Error Invalid data received from the request.\n" +
                         "osBuildDate cannot be null or 0 and device type cannot be null or empty";
            log.error(msg);
            throw new ReportManagementException(msg);
        }
        try {
            int tenantId = DeviceManagementDAOUtil.getTenantId();
            String deviceType = request.getDeviceType();
            PaginationResult paginationResult = new PaginationResult();

            DeviceManagerUtil.validateDeviceListPageSize(request);

            DeviceType deviceTypeObj = DeviceManagerUtil.getDeviceType(
                    deviceType, tenantId);
            if (deviceTypeObj == null) {
                String msg = "Error, device of type: " + deviceType + " does not exist";
                log.error(msg);
                throw new DeviceTypeNotFoundException(msg);
            }

            try {
                DeviceManagementDAOFactory.openConnection();
                List<Device> devices = deviceDAO.getDevicesExpiredByOSVersion(request, tenantId);
                int deviceCount = deviceDAO.getCountOfDeviceExpiredByOSVersion(
                        deviceType,
                        (Long) request.getProperty(Constants.OS_BUILD_DATE),
                        tenantId);
                paginationResult.setData(devices);
                paginationResult.setRecordsFiltered(devices.size());
                paginationResult.setRecordsTotal(deviceCount);

                return paginationResult;
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the data source";
                log.error(msg, e);
                throw new ReportManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }

        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving expired devices by a OS build date " +
                         "for the tenant";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating the request";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }

    //NOTE: This is just a temporary method for retrieving device counts
    public JsonObject buildCount(String start, String end, List<Count> countList) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int prevDateAmount = 0;
        boolean isDaily = false;

        Date startDate = dateFormat.parse(start);
        Date endDate = dateFormat.parse(end);

        //Check duration between two given dates
        long gap = endDate.getTime() - startDate.getTime();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(gap);

        if (diffInDays < 7) {
            isDaily = true;
        } else if (diffInDays < 30) {
            prevDateAmount = -7;
        } else {
            prevDateAmount = -30;
        }
        JsonObject resultObject = new JsonObject();
        if (!isDaily) {
            //Divide date duration into week or month blocks
            while (endDate.after(startDate)) {
                int sum = 0;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.add(Calendar.DAY_OF_YEAR, prevDateAmount);
                Date previousDate = calendar.getTime();
                if (startDate.after(previousDate)) {
                    previousDate = startDate;
                }
                //Loop count list which came from database to add them into week or month blocks
                for (Count count : countList) {
                    if (dateFormat.parse(
                            count.getDate()).after(previousDate) &&
                            dateFormat.parse(count.getDate()).before(endDate
                            )) {
                        sum = sum + count.getCount();
                    }
                }
                //Map date blocks and counts
                resultObject.addProperty(
                        dateFormat.format(endDate) + " - " + dateFormat.format(previousDate), sum);
                endDate = previousDate;

            }
        } else {
            for (Count count : countList) {
                resultObject.addProperty(count.getDate() + " - " + count.getDate(), count.getCount());
            }
        }
        return resultObject;
    }

    @Override
    public PaginationResult getAppNotInstalledDevices(PaginationRequest request, String packageName, String version)
            throws ReportManagementException, DeviceTypeNotFoundException {
        PaginationResult paginationResult = new PaginationResult();
        if(StringUtils.isBlank(packageName)){
            String msg = "Error, application package name is not given";
            log.error(msg);
            throw new ReportManagementException(msg);
        }
        try {
            int tenantId = DeviceManagementDAOUtil.getTenantId();
            request = DeviceManagerUtil.validateDeviceListPageSize(request);

            String deviceType = request.getDeviceType();
            DeviceType deviceTypeObj = DeviceManagerUtil.getDeviceType(
                    deviceType, tenantId);
            if (deviceTypeObj == null) {
                String msg = "Error, device of type: " + deviceType + " does not exist";
                log.error(msg);
                throw new DeviceTypeNotFoundException(msg);
            }

            try {
                DeviceManagementDAOFactory.openConnection();
                List<Device> devices = deviceDAO.getAppNotInstalledDevices(
                        request,
                        tenantId,
                        packageName,
                        version
                );
                paginationResult.setData(devices);
                int deviceCount = deviceDAO.getCountOfAppNotInstalledDevices(
                        request,
                        tenantId,
                        packageName,
                        version);
                paginationResult.setRecordsTotal(deviceCount);
                return paginationResult;
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection " +
                        "to the data source";
                log.error(msg, e);
                throw new ReportManagementException(msg, e);
            }  finally {
                DeviceManagementDAOFactory.closeConnection();
            }

        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating device list page size";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }
}
