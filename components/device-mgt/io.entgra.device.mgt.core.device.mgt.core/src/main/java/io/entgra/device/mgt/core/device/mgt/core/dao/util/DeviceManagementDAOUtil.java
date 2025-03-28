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
package io.entgra.device.mgt.core.device.mgt.core.dao.util;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceInfo;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceLocationHistorySnapshot;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceMonitoringData;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public final class DeviceManagementDAOUtil {

    private static final Log log = LogFactory.getLog(DeviceManagementDAOUtil.class);

    public static void cleanupResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing database connection", e);
            }
        }
    }

    public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
    }

    /**
     * Get id of the current tenant.
     *
     * @return tenant id
     * @throws DeviceManagementDAOException if an error is observed when getting tenant id
     */
    public static int getTenantId() throws DeviceManagementDAOException {
        CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
        int tenantId = context.getTenantId();
        if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
            return tenantId;
        }
        String tenantDomain = context.getTenantDomain();
        if (tenantDomain == null) {
            String msg = "Tenant domain is not properly set and thus, is null";
            throw new DeviceManagementDAOException(msg);
        }
        TenantManager tenantManager = DeviceManagementDataHolder.getInstance().getTenantManager();
        try {
            tenantId = tenantManager.getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg =
                    "Error occurred while retrieving id from the domain of tenant " + tenantDomain;
            throw new DeviceManagementDAOException(msg);
        }
        return tenantId;
    }

    public static DataSource lookupDataSource(String dataSourceName,
                                              final Hashtable<Object, Object> jndiProperties) {
        try {
            if (jndiProperties == null || jndiProperties.isEmpty()) {
                return (DataSource) InitialContext.doLookup(dataSourceName);
            }
            final InitialContext context = new InitialContext(jndiProperties);
            return (DataSource) context.lookup(dataSourceName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
        }
    }
/*
    public static Device loadDevice(ResultSet rs) throws SQLException {
		Device device = new Device();
		device.setId(rs.getInt("ID"));
		device.setName(rs.getString("NAME"));
		device.setDescription(rs.getString("DESCRIPTION"));
		device.setType(rs.getString("DEVICE_TYPE_ID"));
		device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
		return device;
	}*/

    public static EnrolmentInfo loadEnrolment(ResultSet rs) throws SQLException {
        return loadEnrolment(rs, false);
    }

    public static EnrolmentInfo loadEnrolment(ResultSet rs, boolean isTagsProvided) throws SQLException {
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setId(rs.getInt("ENROLMENT_ID"));
        enrolmentInfo.setOwner(rs.getString("OWNER"));
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.valueOf(rs.getString("OWNERSHIP")));
        enrolmentInfo.setTransferred(rs.getBoolean("IS_TRANSFERRED"));
        enrolmentInfo.setDateOfEnrolment(rs.getTimestamp("DATE_OF_ENROLMENT").getTime());
        enrolmentInfo.setDateOfLastUpdate(rs.getTimestamp("DATE_OF_LAST_UPDATE").getTime());
        enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(rs.getString("STATUS")));
        if (isTagsProvided) {
            List<String> tags = new ArrayList<>();
            String tagColumn = rs.getString("TAGS");
            if (tagColumn != null) {
                for (String tag : tagColumn.split(",")) {
                    tags.add(tag.trim());
                }
            }
            enrolmentInfo.setTags(tags);
        }
        return enrolmentInfo;
    }

    /* This is used to set the enrollment data of the billing query */
    public static EnrolmentInfo loadEnrolmentBilling(ResultSet rs) throws SQLException {
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setDateOfEnrolment(rs.getTimestamp("DATE_OF_ENROLMENT").getTime());
        enrolmentInfo.setDateOfLastUpdate(rs.getTimestamp("DATE_OF_LAST_UPDATE").getTime());
        enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(rs.getString("STATUS")));
        return enrolmentInfo;
    }

    public static EnrolmentInfo loadEnrolmentStatus(ResultSet rs) throws SQLException {
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setId(rs.getInt("ENROLMENT_ID"));
        enrolmentInfo.setTransferred(rs.getBoolean("IS_TRANSFERRED"));
        enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(rs.getString("STATUS")));
        return enrolmentInfo;
    }

    public static EnrolmentInfo loadMatchingEnrolment(ResultSet rs) throws SQLException {
        Map<EnrolmentInfo.Status, EnrolmentInfo> enrolmentInfos = new HashMap<>();
        EnrolmentInfo enrolmentInfo = loadEnrolment(rs);
        if (EnrolmentInfo.Status.ACTIVE.equals(enrolmentInfo.getStatus())) {
            return enrolmentInfo;
        }
        enrolmentInfos.put(enrolmentInfo.getStatus(), enrolmentInfo);
        while (rs.next()) {
            enrolmentInfo = loadEnrolment(rs);
            if (EnrolmentInfo.Status.ACTIVE.equals(enrolmentInfo.getStatus())) {
                return enrolmentInfo;
            }
            enrolmentInfos.put(enrolmentInfo.getStatus(), enrolmentInfo);
        }
        if (enrolmentInfos.containsKey(EnrolmentInfo.Status.UNREACHABLE)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.UNREACHABLE);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.INACTIVE)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.INACTIVE);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.DISENROLLMENT_REQUESTED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.DISENROLLMENT_REQUESTED);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.CREATED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.CREATED);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.REMOVED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.REMOVED);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.UNCLAIMED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.UNCLAIMED);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.SUSPENDED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.SUSPENDED);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.BLOCKED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.BLOCKED);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.CONFIGURED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.CONFIGURED);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.READY_TO_CONNECT)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.READY_TO_CONNECT);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.RETURN_PENDING)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.RETURN_PENDING);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.RETURNED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.RETURNED);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.DEFECTIVE)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.DEFECTIVE);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.WARRANTY_PENDING)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.WARRANTY_PENDING);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.WARRANTY_SENT)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.WARRANTY_SENT);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.WARRANTY_REPLACED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.WARRANTY_REPLACED);
        } else if (enrolmentInfos.containsKey(EnrolmentInfo.Status.ASSIGNED)) {
            return enrolmentInfos.get(EnrolmentInfo.Status.ASSIGNED);
        }
        return enrolmentInfo;
    }

    public static Device loadDevice(ResultSet rs) throws SQLException {
        return loadDevice(rs, false);
    }

    public static Device loadDevice(ResultSet rs, boolean isTagsProvided) throws SQLException {
        Device device = new Device();
        device.setId(rs.getInt("DEVICE_ID"));
        device.setName(rs.getString("DEVICE_NAME"));
        device.setDescription(rs.getString("DESCRIPTION"));
        device.setType(rs.getString("DEVICE_TYPE"));
        device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
        device.setLastUpdatedTimeStamp(rs.getTimestamp("LAST_UPDATED_TIMESTAMP").getTime());
        device.setEnrolmentInfo(loadEnrolment(rs, isTagsProvided));
        return device;
    }

    /* This is used to set the device data of the billing query */
    public static Device loadDeviceBilling(ResultSet rs) throws SQLException {
        Device device = new Device();
        device.setId(rs.getInt("DEVICE_ID"));
        device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
        device.setName(rs.getString("NAME"));
        device.setDescription(rs.getString("DESCRIPTION"));
        device.setEnrolmentInfo(loadEnrolmentBilling(rs));
        return device;
    }

    public static Device loadDeviceIds(ResultSet rs) throws SQLException {
        Device device = new Device();
        device.setId(rs.getInt("DEVICE_ID"));
        device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
        device.setEnrolmentInfo(loadEnrolmentStatus(rs));
        return device;
    }

    public static DeviceMonitoringData loadDevice(ResultSet rs, String deviceTypeName) throws SQLException {
        Device device = new Device();
        device.setId(rs.getInt("DEVICE_ID"));
        device.setName(rs.getString("DEVICE_NAME"));
        device.setDescription(rs.getString("DESCRIPTION"));
        device.setType(deviceTypeName);
        device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
        device.setEnrolmentInfo(loadEnrolment(rs));

        DeviceMonitoringData deviceMonitoringData = new DeviceMonitoringData();
        deviceMonitoringData.setLastUpdatedTime(rs
                .getTimestamp("LAST_UPDATED_TIMESTAMP").getTime());
        deviceMonitoringData.setTenantId(rs.getInt("TENANT_ID"));
        deviceMonitoringData.setDevice(device);
        return deviceMonitoringData;
    }

    //This method will retrieve most appropriate device information when there are multiple device enrollments for
    //a single device. Here we'll consider only active status.
    public static Device loadActiveDevice(ResultSet rs, boolean deviceInfoIncluded) throws SQLException {
        Device device = loadDevice(rs);
        if (deviceInfoIncluded) {
            device.setDeviceInfo(loadDeviceInfo(rs));
        }
        return device;
    }

    //This method will retrieve most appropriate device information when there are multiple device enrollments for
    //a single device. We'll give the highest priority to active devices.
    public static Device loadMatchingDevice(ResultSet rs, boolean deviceInfoIncluded) throws SQLException {
        Device device = loadDevice(rs);
        if (deviceInfoIncluded) {
            device.setDeviceInfo(loadDeviceInfo(rs));
        }
        return device;
    }

    public static DeviceType loadDeviceType(ResultSet rs) throws SQLException {
        DeviceType deviceType = new DeviceType();
        deviceType.setId(rs.getInt("ID"));
        deviceType.setName(rs.getString("NAME"));
        return deviceType;
    }

    public static DeviceInfo loadDeviceInfo(ResultSet rs) throws SQLException {
        DeviceInfo deviceInfo = new DeviceInfo();
//                deviceInfo.setIMEI(rs.getString("IMEI"));
//                deviceInfo.setIMSI(rs.getString("IMSI"));
        deviceInfo.setDeviceModel(rs.getString("DEVICE_MODEL"));
        deviceInfo.setVendor(rs.getString("VENDOR"));
        deviceInfo.setOsVersion(rs.getString("OS_VERSION"));
        deviceInfo.setOsBuildDate(rs.getString("OS_BUILD_DATE"));
        deviceInfo.setBatteryLevel(rs.getDouble("BATTERY_LEVEL"));
        deviceInfo.setInternalTotalMemory(rs.getDouble("INTERNAL_TOTAL_MEMORY"));
        deviceInfo.setInternalAvailableMemory(rs.getDouble("INTERNAL_AVAILABLE_MEMORY"));
        deviceInfo.setExternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
        deviceInfo.setExternalAvailableMemory(rs.getDouble("EXTERNAL_AVAILABLE_MEMORY"));
//                deviceInfo.setOperator(rs.getString("OPERATOR"));
        deviceInfo.setConnectionType(rs.getString("CONNECTION_TYPE"));
//                deviceInfo.setMobileSignalStrength(rs.getDouble("MOBILE_SIGNAL_STRENGTH"));
        deviceInfo.setSsid(rs.getString("SSID"));
        deviceInfo.setCpuUsage(rs.getDouble("CPU_USAGE"));
        deviceInfo.setTotalRAMMemory(rs.getDouble("TOTAL_RAM_MEMORY"));
        deviceInfo.setAvailableRAMMemory(rs.getDouble("AVAILABLE_RAM_MEMORY"));
        deviceInfo.setPluggedIn(rs.getBoolean("PLUGGED_IN"));
        deviceInfo.setUpdatedTime(new Date(rs.getLong("UPDATE_TIMESTAMP")));
        return deviceInfo;
    }

    public static DeviceLocationHistorySnapshot loadDeviceLocation(ResultSet rs) throws SQLException {
        DeviceLocationHistorySnapshot deviceLocationHistory = new DeviceLocationHistorySnapshot();
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(rs.getString("DEVICE_ID_NAME"));
        deviceIdentifier.setType(rs.getString("DEVICE_TYPE_NAME"));
        deviceLocationHistory.setDeviceIdentifier(deviceIdentifier);
        deviceLocationHistory.setTenantId(rs.getInt("TENANT_ID"));
        deviceLocationHistory.setLatitude(rs.getDouble("LATITUDE"));
        deviceLocationHistory.setLongitude(rs.getDouble("LONGITUDE"));
        deviceLocationHistory.setSpeed(rs.getFloat("SPEED"));
        deviceLocationHistory.setBearing(rs.getFloat("HEADING"));
        deviceLocationHistory.setAltitude(rs.getDouble("DEVICE_ALTITUDE"));
        deviceLocationHistory.setDistance(rs.getDouble("DISTANCE"));
        deviceLocationHistory.setOwner(rs.getString("DEVICE_OWNER"));
        deviceLocationHistory.setUpdatedTime(new Date(rs.getLong("TIMESTAMP")));
        deviceLocationHistory.setGeoHash(rs.getString("GEO_HASH"));
        return deviceLocationHistory;
    }

    public static long getCurrentUTCTime() {
        return Instant.now().getEpochSecond();
    }

    public static long convertLocalTimeIntoUTC(Date localDate) {
        LocalDateTime l = localDate.toInstant()
                .atZone(ZoneId.of("GMT"))
                .toLocalDateTime();
        return Timestamp.valueOf(l).getTime() / 1000;
    }
}
