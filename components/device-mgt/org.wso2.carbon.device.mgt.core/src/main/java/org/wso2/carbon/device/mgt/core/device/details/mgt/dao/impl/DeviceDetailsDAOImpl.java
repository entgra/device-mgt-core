/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.core.device.details.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsDAO;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsMgtDAOException;
import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoHashGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceDetailsDAOImpl implements DeviceDetailsDAO {

    private static final Log log = LogFactory.getLog(DeviceDetailsDAOImpl.class);

    @Override
    public void addDeviceInformation(int deviceId, int enrolmentId, DeviceInfo deviceInfo)
            throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();

            stmt = conn.prepareStatement("INSERT INTO DM_DEVICE_DETAIL (DEVICE_ID, DEVICE_MODEL, " +
                    "VENDOR, OS_VERSION, OS_BUILD_DATE, BATTERY_LEVEL, INTERNAL_TOTAL_MEMORY, INTERNAL_AVAILABLE_MEMORY, " +
                    "EXTERNAL_TOTAL_MEMORY, EXTERNAL_AVAILABLE_MEMORY,  CONNECTION_TYPE, " +
                    "SSID, CPU_USAGE, TOTAL_RAM_MEMORY, AVAILABLE_RAM_MEMORY, PLUGGED_IN, UPDATE_TIMESTAMP, ENROLMENT_ID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, deviceId);
            stmt.setString(2, deviceInfo.getDeviceModel());
            stmt.setString(3, deviceInfo.getVendor());
            stmt.setString(4, deviceInfo.getOsVersion());
            stmt.setString(5, deviceInfo.getOsBuildDate());
            stmt.setDouble(6, deviceInfo.getBatteryLevel());
            stmt.setDouble(7, deviceInfo.getInternalTotalMemory());
            stmt.setDouble(8, deviceInfo.getInternalAvailableMemory());
            stmt.setDouble(9, deviceInfo.getExternalTotalMemory());
            stmt.setDouble(10, deviceInfo.getExternalAvailableMemory());
            stmt.setString(11, deviceInfo.getConnectionType());
            stmt.setString(12, deviceInfo.getSsid());
            stmt.setDouble(13, deviceInfo.getCpuUsage());
            stmt.setDouble(14, deviceInfo.getTotalRAMMemory());
            stmt.setDouble(15, deviceInfo.getAvailableRAMMemory());
            stmt.setBoolean(16, deviceInfo.isPluggedIn());
            stmt.setLong(17, DeviceManagementDAOUtil.getCurrentUTCTime());
            stmt.setInt(18, enrolmentId);

            stmt.execute();

        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while inserting device details to database.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public void addDeviceProperties(Map<String, String> propertyMap, int deviceId, int enrolmentId)
            throws DeviceDetailsMgtDAOException {

        if (propertyMap.isEmpty()) {
            if(log.isDebugEnabled()) {
                log.debug("Property map of device id :" + deviceId + " is empty.");
            }
            return;
        }
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_DEVICE_INFO (DEVICE_ID, KEY_FIELD, VALUE_FIELD, ENROLMENT_ID) " +
                    "VALUES (?, ?, ?, ?)");

            for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                stmt.setInt(1, deviceId);
                stmt.setString(2, entry.getKey());
                stmt.setString(3, entry.getValue());
                stmt.setInt(4, enrolmentId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while inserting device properties to database.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public void updateDeviceProperties(Map<String, String> propertyMap, int deviceId, int enrolmentId)
            throws DeviceDetailsMgtDAOException {

        if (propertyMap.isEmpty()) {
            if(log.isDebugEnabled()) {
                log.debug("Property map of device id :" + deviceId + " is empty.");
            }
            return;
        }
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("UPDATE DM_DEVICE_INFO SET VALUE_FIELD = ? WHERE DEVICE_ID = ?" +
                    " AND KEY_FIELD = ? AND ENROLMENT_ID = ?");

            for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                stmt.setString(1, entry.getValue());
                stmt.setInt(2, deviceId);
                stmt.setString(3, entry.getKey());
                stmt.setInt(4, enrolmentId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while updating device properties to database.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public DeviceInfo getDeviceInformation(int deviceId, int enrolmentId) throws DeviceDetailsMgtDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DeviceInfo deviceInfo = null;
        try {
            conn = this.getConnection();

            String sql = "SELECT * FROM DM_DEVICE_DETAIL WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrolmentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                deviceInfo = new DeviceInfo();
                deviceInfo.setDeviceModel(rs.getString("DEVICE_MODEL"));
                deviceInfo.setVendor(rs.getString("VENDOR"));
                deviceInfo.setOsVersion(rs.getString("OS_VERSION"));
                deviceInfo.setOsBuildDate(rs.getString("OS_BUILD_DATE"));
                deviceInfo.setBatteryLevel(rs.getDouble("BATTERY_LEVEL"));
                deviceInfo.setInternalTotalMemory(rs.getDouble("INTERNAL_TOTAL_MEMORY"));
                deviceInfo.setInternalAvailableMemory(rs.getDouble("INTERNAL_AVAILABLE_MEMORY"));
                deviceInfo.setExternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
                deviceInfo.setExternalAvailableMemory(rs.getDouble("EXTERNAL_AVAILABLE_MEMORY"));
                deviceInfo.setConnectionType(rs.getString("CONNECTION_TYPE"));
                deviceInfo.setSsid(rs.getString("SSID"));
                deviceInfo.setCpuUsage(rs.getDouble("CPU_USAGE"));
                deviceInfo.setTotalRAMMemory(rs.getDouble("TOTAL_RAM_MEMORY"));
                deviceInfo.setAvailableRAMMemory(rs.getDouble("AVAILABLE_RAM_MEMORY"));
                deviceInfo.setPluggedIn(rs.getBoolean("PLUGGED_IN"));
                deviceInfo.setUpdatedTime(new java.util.Date(rs.getLong("UPDATE_TIMESTAMP")));
            }
            return deviceInfo;
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while fetching the details of the registered devices.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Map<String, String> getDeviceProperties(int deviceId, int enrolmentId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM  DM_DEVICE_INFO WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrolmentId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                map.put(rs.getString("KEY_FIELD"), rs.getString("VALUE_FIELD"));
            }
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while fetching the properties of the registered devices.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return map;
    }

    @Override
    public void deleteDeviceInformation(int deviceId, int enrollmentId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_DEVICE_DETAIL WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrollmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while deleting the device information from the data base.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteDeviceProperties(int deviceId, int enrollmentId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_DEVICE_INFO WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrollmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while deleting the device properties from the data base.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void addDeviceLocation(DeviceLocation deviceLocation, int enrollmentId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            if (StringUtils.isNotBlank(deviceLocation.getZip())
                    && deviceLocation.getZip().length() > 10) {
                log.error("Adding unusually long zip " + deviceLocation.getZip() + ", deviceId:"
                        + deviceLocation.getDeviceId() + ", enrollmentId:" + enrollmentId);
                deviceLocation.setZip(null);
            }
            conn = this.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_DEVICE_LOCATION (DEVICE_ID, LATITUDE, LONGITUDE, STREET1, " +
                    "STREET2, CITY, ZIP, STATE, COUNTRY, GEO_HASH, UPDATE_TIMESTAMP, ENROLMENT_ID, ALTITUDE, SPEED, BEARING, " +
                    "DISTANCE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setInt(1, deviceLocation.getDeviceId());
            stmt.setDouble(2, deviceLocation.getLatitude());
            stmt.setDouble(3, deviceLocation.getLongitude());
            stmt.setString(4, deviceLocation.getStreet1());
            stmt.setString(5, deviceLocation.getStreet2());
            stmt.setString(6, deviceLocation.getCity());
            stmt.setString(7, deviceLocation.getZip());
            stmt.setString(8, deviceLocation.getState());
            stmt.setString(9, deviceLocation.getCountry());
            stmt.setString(10, GeoHashGenerator.encodeGeohash(deviceLocation));
            if (deviceLocation.getUpdatedTime() == null) {
                stmt.setLong(11, DeviceManagementDAOUtil.getCurrentUTCTime() * 1000L);
            } else {
                stmt.setLong(11, DeviceManagementDAOUtil.convertLocalTimeIntoUTC(deviceLocation.getUpdatedTime()) * 1000L);
            }
            stmt.setInt(12, enrollmentId);
            stmt.setDouble(13, deviceLocation.getAltitude());
            stmt.setFloat(14, deviceLocation.getSpeed());
            stmt.setFloat(15, deviceLocation.getBearing());
            stmt.setDouble(16, deviceLocation.getDistance());
            stmt.execute();
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while adding the device location to database.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateDeviceLocation(DeviceLocation deviceLocation, int enrollmentId)
            throws DeviceDetailsMgtDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            if (StringUtils.isNotBlank(deviceLocation.getZip())
                    && deviceLocation.getZip().length() > 10) {
                log.error("Updating unusually long zip " + deviceLocation.getZip() + ", deviceId:"
                        + deviceLocation.getDeviceId() + ", enrollmentId:" + enrollmentId);
                deviceLocation.setZip(null);
            }

            conn = this.getConnection();
            stmt = conn.prepareStatement("UPDATE DM_DEVICE_LOCATION SET LATITUDE = ?, LONGITUDE = ?, " +
                    "STREET1 = ?, STREET2 = ?, CITY = ?, ZIP = ?, STATE = ?, COUNTRY = ?, GEO_HASH = ?, " +
                    "UPDATE_TIMESTAMP = ? WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?");
            stmt.setDouble(1, deviceLocation.getLatitude());
            stmt.setDouble(2, deviceLocation.getLongitude());
            stmt.setString(3, deviceLocation.getStreet1());
            stmt.setString(4, deviceLocation.getStreet2());
            stmt.setString(5, deviceLocation.getCity());
            stmt.setString(6, deviceLocation.getZip());
            stmt.setString(7, deviceLocation.getState());
            stmt.setString(8, deviceLocation.getCountry());
            stmt.setString(9, GeoHashGenerator.encodeGeohash(deviceLocation));
            stmt.setLong(10, DeviceManagementDAOUtil.getCurrentUTCTime() * 1000L);
            stmt.setInt(11, deviceLocation.getDeviceId());
            stmt.setInt(12, enrollmentId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while adding the device location to database.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public DeviceLocation getDeviceLocation(int deviceId, int enrollmentId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DeviceLocation location = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM  DM_DEVICE_LOCATION WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrollmentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                location = new DeviceLocation();
                location.setDeviceId(deviceId);
                location.setLatitude(rs.getDouble("LATITUDE"));
                location.setLongitude(rs.getDouble("LONGITUDE"));
                location.setStreet1(rs.getString("STREET1"));
                location.setStreet2(rs.getString("STREET2"));
                location.setCity(rs.getString("CITY"));
                location.setZip(rs.getString("ZIP"));
                location.setState(rs.getString("STATE"));
                location.setCountry(rs.getString("COUNTRY"));
                location.setUpdatedTime(new java.util.Date(rs.getLong("UPDATE_TIMESTAMP")));
            }

            return location;
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while fetching the location of the registered devices.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void deleteDeviceLocation(int deviceId, int enrollmentId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_DEVICE_LOCATION WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrollmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while deleting the device location from the data base.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void addDeviceLocationInfo(Device device, DeviceLocation deviceLocation, int tenantId)
            throws DeviceDetailsMgtDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        String errMessage;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "INSERT INTO " +
                            "DM_DEVICE_HISTORY_LAST_SEVEN_DAYS " +
                            "(DEVICE_ID, DEVICE_ID_NAME, TENANT_ID, DEVICE_TYPE_NAME, LATITUDE, LONGITUDE, SPEED, HEADING, " +
                            "TIMESTAMP, GEO_HASH, DEVICE_OWNER, DEVICE_ALTITUDE, DISTANCE) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, device.getId());
            stmt.setString(2, device.getDeviceIdentifier());
            stmt.setInt(3, tenantId);
            stmt.setString(4, device.getType());
            stmt.setDouble(5, deviceLocation.getLatitude());
            stmt.setDouble(6, deviceLocation.getLongitude());
            stmt.setFloat(7, deviceLocation.getSpeed());
            stmt.setFloat(8, deviceLocation.getBearing());
            if (deviceLocation.getUpdatedTime() == null) {
                stmt.setLong(9, System.currentTimeMillis());
            } else {
                stmt.setLong(9, deviceLocation.getUpdatedTime().getTime());
            }
            stmt.setString(10, GeoHashGenerator.encodeGeohash(deviceLocation));
            stmt.setString(11, device.getEnrolmentInfo().getOwner());
            stmt.setDouble(12, deviceLocation.getAltitude());
            stmt.setDouble(13, deviceLocation.getDistance());
            stmt.execute();

        } catch (SQLException e) {
            errMessage = "Error occurred while updating the device location information to database.";
            log.error(errMessage);
            throw new DeviceDetailsMgtDAOException(errMessage, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void addDeviceLocationsInfo(Device device, List<DeviceLocation> deviceLocation,
                                       int tenantId) throws DeviceDetailsMgtDAOException {
        Connection conn;
        String errMessage;
        String sql = "INSERT INTO " +
                "DM_DEVICE_HISTORY_LAST_SEVEN_DAYS " +
                "(DEVICE_ID, DEVICE_ID_NAME, TENANT_ID, DEVICE_TYPE_NAME, LATITUDE, LONGITUDE, SPEED, HEADING, " +
                "TIMESTAMP, GEO_HASH, DEVICE_OWNER, DEVICE_ALTITUDE, DISTANCE) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (DeviceLocation location : deviceLocation) {
                    stmt.setInt(1, device.getId());
                    stmt.setString(2, device.getDeviceIdentifier());
                    stmt.setInt(3, tenantId);
                    stmt.setString(4, device.getType());
                    stmt.setDouble(5, location.getLatitude());
                    stmt.setDouble(6, location.getLongitude());
                    stmt.setFloat(7, location.getSpeed());
                    stmt.setFloat(8, location.getBearing());
                    stmt.setLong(9, location.getUpdatedTime().getTime());
                    stmt.setString(10, GeoHashGenerator.encodeGeohash(location));
                    stmt.setString(11, device.getEnrolmentInfo().getOwner());
                    stmt.setDouble(12, location.getAltitude());
                    stmt.setDouble(13, location.getDistance());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

        } catch (SQLException e) {
            errMessage = "Error occurred while updating the device location information to database.";
            log.error(errMessage);
            throw new DeviceDetailsMgtDAOException(errMessage, e);
        }
    }

    @Override
    public void updateDeviceInformation(int deviceId, int enrollmentId, DeviceInfo newDeviceInfo) throws DeviceDetailsMgtDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();

            stmt = conn.prepareStatement("UPDATE DM_DEVICE_DETAIL SET DEVICE_MODEL = ?, VENDOR = ?, " +
                    "OS_VERSION = ?, OS_BUILD_DATE = ?, BATTERY_LEVEL = ?, INTERNAL_TOTAL_MEMORY = ?, " +
                    "INTERNAL_AVAILABLE_MEMORY = ?, EXTERNAL_TOTAL_MEMORY = ?, EXTERNAL_AVAILABLE_MEMORY = ?, " +
                    "CONNECTION_TYPE = ?, SSID = ?, CPU_USAGE = ?, TOTAL_RAM_MEMORY = ?, AVAILABLE_RAM_MEMORY = ?, " +
                    "PLUGGED_IN = ?, UPDATE_TIMESTAMP = ? WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?");

            stmt.setString(1, newDeviceInfo.getDeviceModel());
            stmt.setString(2, newDeviceInfo.getVendor());
            stmt.setString(3, newDeviceInfo.getOsVersion());
            stmt.setString(4, newDeviceInfo.getOsBuildDate());
            stmt.setDouble(5, newDeviceInfo.getBatteryLevel());
            stmt.setDouble(6, newDeviceInfo.getInternalTotalMemory());
            stmt.setDouble(7, newDeviceInfo.getInternalAvailableMemory());
            stmt.setDouble(8, newDeviceInfo.getExternalTotalMemory());
            stmt.setDouble(9, newDeviceInfo.getExternalAvailableMemory());
            stmt.setString(10, newDeviceInfo.getConnectionType());
            stmt.setString(11, newDeviceInfo.getSsid());
            stmt.setDouble(12, newDeviceInfo.getCpuUsage());
            stmt.setDouble(13, newDeviceInfo.getTotalRAMMemory());
            stmt.setDouble(14, newDeviceInfo.getAvailableRAMMemory());
            stmt.setBoolean(15, newDeviceInfo.isPluggedIn());
            stmt.setLong(16, System.currentTimeMillis());
            stmt.setInt(17, deviceId);
            stmt.setInt(18, enrollmentId);

            stmt.execute();

        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while updating device details.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }
}

