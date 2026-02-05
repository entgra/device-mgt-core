/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.report.mgt;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import io.entgra.device.mgt.core.device.mgt.common.Count;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.BadRequestException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.ReportManagementException;
import io.entgra.device.mgt.core.device.mgt.common.report.mgt.ReportManagementService;
import io.entgra.device.mgt.core.device.mgt.common.report.mgt.ReportParameters;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.GroupDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.GroupManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.GroupManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.core.util.HttpReportingUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is the service class for reports which calls dao classes and its method which are used for
 * report generation tasks.
 */
public class ReportManagementServiceImpl implements ReportManagementService {

    private static final Log log = LogFactory.getLog(ReportManagementServiceImpl.class);

    private DeviceDAO deviceDAO;
    private GroupDAO groupDAO;

    public ReportManagementServiceImpl() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.groupDAO = GroupManagementDAOFactory.getGroupDAO();
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
            throws ReportManagementException, BadRequestException {
        if (request == null ||
            StringUtils.isBlank(request.getDeviceType()) ||
            request.getProperties() == null ||
            !request.getProperties().containsKey(Constants.OS_VERSION) ||
            StringUtils.isBlank((String) request.getProperty(Constants.OS_VERSION))) {
            String msg = "Error Invalid data received from the request.\n" +
                         "osVersion and device type cannot be null or empty.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        String deviceType = request.getDeviceType();
        if (!deviceType.equals(Constants.ANDROID) && !deviceType.equals(Constants.IOS)) {
            String msg = "Error Invalid device type:" + deviceType + " received. Valid device types " +
                         "are android and ios.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        try {
            int tenantId = DeviceManagementDAOUtil.getTenantId();
            PaginationResult paginationResult = new PaginationResult();
            DeviceManagerUtil.validateDeviceListPageSize(request);

            String osVersion = (String) request.getProperty(Constants.OS_VERSION);
            Long osVersionValue = DeviceManagerUtil.generateOSVersionValue(osVersion);
            if (osVersionValue == null){
                String msg = "Failed to generate OS value, received OS version: " + osVersion +
                             " is in incorrect format([0-9]+([.][0-9]+)*) or version is invalid.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            request.setProperty(Constants.OS_VALUE, osVersionValue);

            try {
                DeviceManagementDAOFactory.openConnection();

                List<Device> devices = deviceDAO.getDevicesExpiredByOSVersion(
                        request, tenantId);
                int deviceCount = deviceDAO.getCountOfDeviceExpiredByOSVersion(
                        deviceType, osVersionValue, tenantId);
                paginationResult.setData(devices);
                paginationResult.setRecordsFiltered(devices.size());
                paginationResult.setRecordsTotal(deviceCount);

                return paginationResult;
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the data source.";
                log.error(msg, e);
                throw new ReportManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }

        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving expired devices by a OS version " +
                         "for the tenant.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating the request.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }

    @Override
    public PaginationResult getDevicesByEncryptionStatus(PaginationRequest request, boolean isEncrypted)
            throws ReportManagementException {
        if (request == null) {
            String msg = "Error. The request must be a not null value.";
            log.error(msg);
            throw new ReportManagementException(msg);
        }
        try {
            int tenantId = DeviceManagementDAOUtil.getTenantId();
            PaginationResult paginationResult = new PaginationResult();

            DeviceManagerUtil.validateDeviceListPageSize(request);

            try {
                DeviceManagementDAOFactory.openConnection();
                List<Device> devices = deviceDAO.getDevicesByEncryptionStatus(request, tenantId, isEncrypted);
                int deviceCount = deviceDAO.getCountOfDevicesByEncryptionStatus(tenantId, isEncrypted);
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
            String msg = "Error occurred while retrieving expired devices by encryption status for the tenant";
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
                        dateFormat.format(previousDate) + " - " + dateFormat.format(endDate), sum);
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

    @Override
    public PaginationResult getDeviceNotAssignedToGroups(PaginationRequest paginationRequest ,
                                                         List<String> groupNames)
            throws ReportManagementException, DeviceTypeNotFoundException {
        PaginationResult paginationResult = new PaginationResult();
        try {
            int tenantId = DeviceManagementDAOUtil.getTenantId();
            DeviceManagerUtil.validateDeviceListPageSize(paginationRequest);
            String deviceType = paginationRequest.getDeviceType();
            DeviceType deviceTypeObj = DeviceManagerUtil.getDeviceType(deviceType, tenantId);
            if (deviceTypeObj == null) {
                String msg = "Error, device of type: " + deviceType + " does not exist";
                log.error(msg);
                throw new DeviceTypeNotFoundException(msg);
            }
            try {
                GroupManagementDAOFactory.openConnection();
                List<Device> devices = groupDAO.getGroupUnassignedDevices(paginationRequest ,
                                                                          groupNames);
                paginationResult.setData(devices);
                return paginationResult;
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the data source";
                log.error(msg, e);
                throw new ReportManagementException(msg, e);
            } catch (GroupManagementDAOException e) {
                String msg = "Error occurred while retrieving the devices that are not assigned " +
                             "to queried groups";
                log.error(msg, e);
                throw new ReportManagementException(msg, e);
            } finally {
                GroupManagementDAOFactory.closeConnection();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating device list page size or loading  " +
                         "device types";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }

    @Override
    public List<String> getDeviceOperators() throws ReportManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get device operators");
        }
        try {
            int tenantId = DeviceManagementDAOUtil.getTenantId();
            try {
                DeviceManagementDAOFactory.openConnection();
                return deviceDAO.getOperators(tenantId);
            } catch (DeviceManagementDAOException e) {
                String msg = "Error occurred while obtaining the device operators.";
                log.error(msg, e);
                throw new ReportManagementException(msg, e);
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the data source.";
                log.error(msg, e);
                throw new ReportManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }

    @Override
    public List<String> getAgentVersions() throws ReportManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get agent versions");
        }
        try {
            int tenantId = DeviceManagementDAOUtil.getTenantId();
            try {
                DeviceManagementDAOFactory.openConnection();
                return deviceDAO.getAgentVersions(tenantId);
            } catch (DeviceManagementDAOException e) {
                String msg = "Error occurred while obtaining the agent versions.";
                log.error(msg, e);
                throw new ReportManagementException(msg, e);
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the data source.";
                log.error(msg, e);
                throw new ReportManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }

    @Override
    public JsonObject generateBirtReport(ReportParameters reportParameters) throws ReportManagementException, BadRequestException, NotFoundException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String generateReportURL = HttpReportingUtil.getBirtReportHost();
            if (!StringUtils.isBlank(generateReportURL)) {
                int tenantId = DeviceManagementDAOUtil.getTenantId();
                generateReportURL += Constants.BirtReporting.BIRT_REPORTING_API_REPORT_PATH;
                Map<String, Object> parameters = reportParameters.getParameters();
                if (parameters.containsKey(Constants.BirtReporting.TENANT_ID)) {
                    parameters.replace(Constants.BirtReporting.TENANT_ID, String.valueOf(tenantId));
                } else {
                    parameters.put(Constants.BirtReporting.TENANT_ID, String.valueOf(tenantId));
                }
                reportParameters.setParameters(parameters);

                HttpPost httpPost = new HttpPost(generateReportURL);
                StringEntity requestEntity = new StringEntity(
                        reportParameters.getJSONString(), ContentType.APPLICATION_JSON);
                httpPost.setEntity(requestEntity);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                switch (statusCode) {
                    case 200:
                    case 202:
                    case 208:
                        return new Gson().fromJson(EntityUtils.toString(httpResponse.getEntity()), JsonObject.class);
                    case 400:
                        throw new BadRequestException("Parameters mismatch.");
                    case 404:
                        throw new NotFoundException("Requested design file not found.");
                    default:
                        throw new ReportManagementException("Failed to create directory.");
                }
            } else {
                String msg = "BIRT reporting host is not defined in the iot-server.sh properly.";
                log.error(msg);
                throw new ReportManagementException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while invoking BIRT runtime API.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }

    @Override
    public JsonObject downloadBirtTemplate(String templateName) throws ReportManagementException, BadRequestException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            String downloadURL = HttpReportingUtil.getBirtReportHost();
            if (!StringUtils.isBlank(downloadURL)) {

                downloadURL += Constants.BirtReporting.BIRT_REPORTING_API_TEMPLATE
                        + Constants.BirtReporting.BIRT_REPORTING_API_DOWNLOAD_TEMPLATE_URL
                        + "?templateURL=" + templateName;

                HttpPost httpPost = new HttpPost(downloadURL);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                switch (statusCode) {
                    case 200:
                        return new Gson().fromJson(EntityUtils.toString(httpResponse.getEntity()), JsonObject.class);
                    case 400:
                        throw new BadRequestException("Invalid file URL.");
                    default:
                        throw new ReportManagementException("Failed to create directory.");
                }
            } else {
                String msg = "BIRT reporting host is not defined in the iot-server.sh properly.";
                log.error(msg);
                throw new ReportManagementException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while invoking BIRT runtime API.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }

    @Override
    public JsonObject deleteBirtTemplate(List<String> templateNames) throws ReportManagementException, BadRequestException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            String deleteURL = HttpReportingUtil.getBirtReportHost();
            if (!StringUtils.isBlank(deleteURL)) {
                deleteURL += Constants.BirtReporting.BIRT_REPORTING_API_TEMPLATE
                        + "?fileNames=" + String.join(",", templateNames);

                HttpDelete httpDelete = new HttpDelete(deleteURL);
                HttpResponse httpResponse = httpClient.execute(httpDelete);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                switch (statusCode) {
                    case 200:
                        return new Gson().fromJson(EntityUtils.toString(httpResponse.getEntity()), JsonObject.class);
                    case 400:
                        throw new BadRequestException("Invalid template names");
                    case 500:
                        JsonObject errorResponse = new Gson().fromJson(EntityUtils.toString(httpResponse.getEntity()), JsonObject.class);
                        throw new ReportManagementException(errorResponse.get("message").getAsString());
                    default:
                        throw new ReportManagementException("Error Occurred While Deleting File");
                }

            } else {
                String msg = "BIRT reporting host is not defined in the iot-server.sh properly.";
                log.error(msg);
                throw new ReportManagementException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while invoking BIRT runtime API.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }

    @Override
    public JsonObject getReportData(JsonObject reportParameters, int limit, int offset)
            throws ReportManagementException, BadRequestException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String getReportDataURL = HttpReportingUtil.getBirtReportHost();
            if (!StringUtils.isBlank(getReportDataURL)) {
                int tenantId = DeviceManagementDAOUtil.getTenantId();
                getReportDataURL += Constants.BirtReporting.BIRT_REPORTING_API_REPORT_DATA_PATH
                        + "?limit=" + limit + "&offset=" + offset;

                JsonObject parameters = reportParameters.getAsJsonObject("parameters");
                parameters.addProperty(Constants.BirtReporting.TENANT_ID, String.valueOf(tenantId));
                reportParameters.add("parameters", parameters);

                HttpPost httpPost = new HttpPost(getReportDataURL);
                httpPost.setEntity(new StringEntity(reportParameters.toString(), ContentType.APPLICATION_JSON));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                switch (statusCode) {
                    case 200:
                        String jsonResponse = EntityUtils.toString(httpResponse.getEntity());
                        return new Gson().fromJson(jsonResponse, JsonObject.class);
                    case 400:
                        throw new BadRequestException("Design file name is required");
                    case 500:
                        JsonObject errorResponse =
                                new Gson().fromJson(EntityUtils.toString(httpResponse.getEntity()), JsonObject.class);
                        throw new ReportManagementException(errorResponse.get("message").getAsString());
                    default:
                        throw new ReportManagementException("Failed to retrieve report data. HTTP " + statusCode);
                }
            } else {
                String msg = "BIRT reporting host is not defined in the iot-server.sh properly.";
                log.error(msg);
                throw new ReportManagementException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while invoking BIRT runtime API.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
    }
    @Override
    public JsonArray getBirtReportParameters()
            throws ReportManagementException, BadRequestException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String paramsURL = HttpReportingUtil.getBirtReportHost();
            if (!StringUtils.isBlank(paramsURL)) {
                paramsURL += Constants.BirtReporting.BIRT_REPORTING_API_REPORT_PATH
                        + "params";
                HttpGet httpGet = new HttpGet(paramsURL);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                switch (statusCode) {
                    case 200:
                        return new Gson().fromJson(
                                EntityUtils.toString(httpResponse.getEntity()),
                                JsonArray.class
                        );
                    case 400:
                        throw new BadRequestException("Invalid request");
                    default:
                        throw new ReportManagementException(
                                "Failed to fetch report parameters. HTTP " + statusCode
                        );
                }
            } else {
                throw new ReportManagementException(
                        "BIRT reporting host is not defined"
                );
            }
        } catch (IOException e) {
            throw new ReportManagementException(
                    "Error occurred while invoking BIRT runtime API", e
            );
        }
    }
    @Override
    public JsonObject getBirtReportPreview(String fileName)
            throws ReportManagementException, BadRequestException {
        if (StringUtils.isBlank(fileName)) {
            throw new BadRequestException("Design file name is required");
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String previewURL = HttpReportingUtil.getBirtReportHost();
            if (StringUtils.isBlank(previewURL)) {
                throw new ReportManagementException(
                        "BIRT reporting host is not defined"
                );
            }
            previewURL += Constants.BirtReporting.BIRT_REPORTING_API_REPORT_PATH
                    + "preview?fileName="
                    + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
            HttpGet httpGet = new HttpGet(previewURL);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            switch (statusCode) {
                case 200:
                    return new Gson().fromJson(
                            EntityUtils.toString(httpResponse.getEntity()),
                            JsonObject.class
                    );
                case 400:
                    throw new BadRequestException(
                            EntityUtils.toString(httpResponse.getEntity())
                    );
                case 404:
                    throw new ReportManagementException(
                            "Preview not available for report: " + fileName
                    );
                default:
                    throw new ReportManagementException(
                            "Failed to fetch report preview. HTTP " + statusCode
                    );
            }
        } catch (IOException e) {
            throw new ReportManagementException(
                    "Error occurred while invoking BIRT preview API", e
            );
        }
    }


}
