/*
 * Copyright (C) 2018 - 2025 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.DeviceFirmwareModel;
import io.entgra.device.mgt.core.device.mgt.common.device.firmware.model.mgt.DeviceFirmwareModelSearchFilter;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.FirmwareDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the FirmwareDAO interface for managing device firmware models.
 */
public class FirmwareDAOImpl implements FirmwareDAO {
    private enum QueryType {
        SELECT, COUNT
    }
    private static final Log log = LogFactory.getLog(FirmwareDAOImpl.class);

    @Override
    public DeviceFirmwareModel getDeviceFirmwareModel(int deviceId, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        DeviceFirmwareModel deviceFirmwareModel = null;
        String sql = "SELECT ID, " +
                "FIRMWARE_MODEL, " +
                "DESCRIPTION, " +
                "DEVICE_TYPE_ID " +
                "FROM DM_DEVICE_FIRMWARE_MODEL M " +
                "INNER JOIN DM_DEVICE_FIRMWARE_MODEL_MAPPING D " +
                "ON D.FIRMWARE_MODEL_ID = M.ID " +
                "WHERE D.DM_DEVICE_ID = ? AND D.TENANT_ID = ?";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, deviceId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        deviceFirmwareModel = new DeviceFirmwareModel();
                        deviceFirmwareModel.setFirmwareId(rs.getInt("ID"));
                        deviceFirmwareModel.setFirmwareModelName(rs.getString("FIRMWARE_MODEL"));
                        deviceFirmwareModel.setDescription(rs.getString("DESCRIPTION"));
                        deviceFirmwareModel.setDeviceTypeId(rs.getInt("DEVICE_TYPE_ID"));
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving device firmware model for device ID: " + deviceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return deviceFirmwareModel;
    }

    @Override
    public DeviceFirmwareModel addFirmwareModel(DeviceFirmwareModel deviceFirmwareModel, int tenantId, int deviceTypeId)
            throws DeviceManagementDAOException {
        Connection conn;
        int firmwareId = -1;
        String sql = "INSERT INTO DM_DEVICE_FIRMWARE_MODEL (FIRMWARE_MODEL, DESCRIPTION, DEVICE_TYPE_ID, TENANT_ID) " +
                "VALUES (?, ?, ?)";

        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, deviceFirmwareModel.getFirmwareModelName());
                stmt.setString(2, deviceFirmwareModel.getDescription());
                stmt.setInt(3, deviceTypeId);
                stmt.setInt(4, tenantId);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        firmwareId = rs.getInt(1);
                    }
                }
                deviceFirmwareModel.setFirmwareId(firmwareId);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while saving device firmware model: " + deviceFirmwareModel.getFirmwareModelName();
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return deviceFirmwareModel;
    }

    @Override
    public DeviceFirmwareModel getExistingFirmwareModel(String firmwareModel, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        DeviceFirmwareModel deviceFirmwareModel = null;
        String sql = "SELECT ID, FIRMWARE_MODEL, DESCRIPTION, DEVICE_TYPE_ID " +
                "FROM DM_DEVICE_FIRMWARE_MODEL " +
                "WHERE FIRMWARE_MODEL = ? AND TENANT_ID = ?";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, firmwareModel);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        deviceFirmwareModel = new DeviceFirmwareModel();
                        deviceFirmwareModel.setFirmwareId(rs.getInt("ID"));
                        deviceFirmwareModel.setFirmwareModelName(rs.getString("FIRMWARE_MODEL"));
                        deviceFirmwareModel.setDescription(rs.getString("DESCRIPTION"));
                        deviceFirmwareModel.setDeviceTypeId(rs.getInt("DEVICE_TYPE_ID"));
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving current firmware model by name: " + firmwareModel;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return deviceFirmwareModel;
    }

    @Override
    public boolean addDeviceFirmwareMapping(int deviceId, int firmwareId, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        boolean isAdded = false;
        String sql = "INSERT INTO DM_DEVICE_FIRMWARE_MODEL_MAPPING (DM_DEVICE_ID, FIRMWARE_MODEL_ID, TENANT_ID) " +
                "VALUES (?, ?, ?)";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, deviceId);
                stmt.setInt(2, firmwareId);
                stmt.setInt(3, tenantId);
                int rowsAffected = stmt.executeUpdate();
                isAdded = rowsAffected > 0;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding existing firmware for device ID: " + deviceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return isAdded;
    }

    public boolean isFirmwareVersionAvailable(int deviceId, int firmwareModelId, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        boolean isAvailable = false;
        String sql = "SELECT FIRMWARE_VERSION FROM DM_DEVICE_FIRMWARE_MODEL_MAPPING " +
                "WHERE DM_DEVICE_ID = ? AND FIRMWARE_MODEL_ID = ? AND TENANT_ID = ?";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, deviceId);
                stmt.setInt(2, firmwareModelId);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        isAvailable = rs.getString("FIRMWARE_VERSION") != null
                                && !rs.getString("FIRMWARE_VERSION").isEmpty();
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while checking firmware version availability for device ID: " + deviceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return isAvailable;
    }

    public boolean saveFirmwareVersionOfDevice(int deviceId, String firmwareVersion, int firmwareModelId, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        boolean isSaved = false;
        String sql = "UPDATE DM_DEVICE_FIRMWARE_MODEL_MAPPING " +
                "SET FIRMWARE_VERSION = ? " +
                "WHERE DM_DEVICE_ID = ? AND FIRMWARE_MODEL_ID = ? AND TENANT_ID = ?";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, firmwareVersion);
                stmt.setInt(2, deviceId);
                stmt.setInt(3, firmwareModelId);
                stmt.setInt(4, tenantId);
                int rowsAffected = stmt.executeUpdate();
                isSaved = rowsAffected > 0;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while saving firmware version for device ID: " + deviceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return isSaved;
    }

    public List<DeviceFirmwareModel> getAllFirmwareModelsByDeviceType(int deviceTypeId, int tenantId)
            throws DeviceManagementDAOException {
        List<DeviceFirmwareModel> firmwareModels = new ArrayList<>();
        DeviceFirmwareModel firmwareModel;
        String sql = "SELECT ID, FIRMWARE_MODEL, DESCRIPTION " +
                "FROM DM_DEVICE_FIRMWARE_MODEL " +
                "WHERE DEVICE_TYPE_ID = ? AND TENANT_ID = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deviceTypeId);
            stmt.setInt(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    firmwareModel = new DeviceFirmwareModel();
                    firmwareModel.setFirmwareId(rs.getInt("ID"));
                    firmwareModel.setFirmwareModelName(rs.getString("FIRMWARE_MODEL"));
                    firmwareModel.setDescription(rs.getString("DESCRIPTION"));
                    firmwareModels.add(firmwareModel);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving firmware models for device type ID: " + deviceTypeId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return firmwareModels;
    }

    @Override
    public List<Device> getFilteredDevicesByFirmwareVersion(DeviceFirmwareModelSearchFilter searchFilter,
                                                                    int tenantId, boolean requireMatchingDevices)
            throws DeviceManagementDAOException {

        List<Device> devices = new ArrayList<>();
        Device device;
        EnrolmentInfo enrolmentInfo;
        String sql = this.getBaseQuery(QueryType.SELECT);
        try (PreparedStatement stmt = this.buildFilteredQuery(sql, searchFilter, tenantId, requireMatchingDevices);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                device = new Device();
                device.setId(rs.getInt("DEVICE_ID"));
                device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
                device.setDescription(rs.getString("DESCRIPTION"));
                enrolmentInfo = new EnrolmentInfo();
                enrolmentInfo.setId(rs.getInt("ENROLMENT_ID"));
                enrolmentInfo.setOwner(rs.getString("OWNER"));
                enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.valueOf(rs.getString("OWNERSHIP")));
                enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(rs.getString("STATUS")));
                enrolmentInfo.setDateOfLastUpdate(rs.getTimestamp("DATE_OF_LAST_UPDATE").getTime());
                device.setType(rs.getString("DEVICE_TYPE"));
                device.setEnrolmentInfo(enrolmentInfo);
                devices.add(device);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving filtered devices based on Firmware Model parameters";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return devices;
    }

    @Override
    public int getCountOfFilteredDevicesByFirmwareVersion(DeviceFirmwareModelSearchFilter searchFilter,
                                                          int tenantId, boolean requireMatchingDevices)
            throws DeviceManagementDAOException {

        int recordsTotal = 0;
        String sql = getBaseQuery(QueryType.COUNT);
        try(PreparedStatement stmt = buildFilteredQuery(sql, searchFilter, tenantId, requireMatchingDevices);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                recordsTotal = rs.getInt("RECORDS_TOTAL");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving filtered devices based on Firmware Model parameters";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return recordsTotal;
    }

    private PreparedStatement buildFilteredQuery(String sql, DeviceFirmwareModelSearchFilter searchFilter,
                                                 int tenantId, boolean requireMatchingDevices) throws SQLException {
        Connection conn;
        boolean isFirmwareVersionsProvided = false;
        boolean isFirmwareModelIdsProvided = false;
        boolean isFirmwareNameFilterProvided = false;
        boolean isDeviceIdentifierProvided = false;
        boolean isFirmwareVersionProvided = false;
        boolean isCustomPropertyProvided = false;
        boolean isDevicGroupFilterProvided = false;
        StringBuilder sb = new StringBuilder(sql);

        sb.append("WHERE ");

        String firmwareVersionPlaceholders = null;
        List<String> firmwareVersions = searchFilter.getFirmwareVersions();
        if(firmwareVersions != null && !firmwareVersions.isEmpty()) {
            firmwareVersionPlaceholders = String.join(", ", Collections.nCopies(firmwareVersions.size(), "?"));
            isFirmwareVersionsProvided = true;
        }
        String firmwareModelPlaceholders = null;
        List<Integer> firmwareModelIds = searchFilter.getFirmwareModelIds();

        if(firmwareModelIds != null && !firmwareModelIds.isEmpty()) {
            firmwareModelPlaceholders = String.join(", ", Collections.nCopies(firmwareModelIds.size(), "?"));
            isFirmwareModelIdsProvided = true;
        }

        if (isFirmwareVersionsProvided) {
            if (requireMatchingDevices) {
                sb.append("dfmm.FIRMWARE_VERSION IN(").append(firmwareVersionPlaceholders).append(")");
            } else {
                sb.append("dfmm.FIRMWARE_VERSION NOT IN(").append(firmwareVersionPlaceholders).append(")");
            }
            sb.append(" AND ");
        }
        if (isFirmwareModelIdsProvided) {
            if (requireMatchingDevices) {
                sb.append("dfmm.FIRMWARE_MODEL_ID IN(").append(firmwareModelPlaceholders).append(")");
            } else {
                sb.append("dfmm.FIRMWARE_MODEL_ID NOT IN(").append(firmwareModelPlaceholders).append(")");
            }
            sb.append(" AND ");
        }

        if (searchFilter.getFirmwareModelName() != null && !searchFilter.getFirmwareModelName().isEmpty()) {
            sb.append("EXISTS (" +
                    "SELECT FIRMWARE_MODEL " +
                    "FROM DM_DEVICE_FIRMWARE_MODEL dfm " +
                    "WHERE dfm.ID = dfmm.FIRMWARE_MODEL_ID " +
                    "AND dfm.FIRMWARE_MODEL LIKE ? )");
            isFirmwareNameFilterProvided = true;
        }
        if (searchFilter.getDeviceIdentifier() != null && !searchFilter.getDeviceIdentifier().isEmpty()) {
            sb.append("d.DEVICE_IDENTIFICATION = ? AND ");
            isDeviceIdentifierProvided = true;
        }
        if (searchFilter.getFirmwareVersion() != null && !searchFilter.getFirmwareVersion().isEmpty()) {
            sb.append("dfmm.FIRMWARE_VERSION = ? AND ");
            isFirmwareVersionProvided = true;
        }
        if (searchFilter.getGroupId() > 0) {
            isDevicGroupFilterProvided = true;
            sb.append("EXISTS (" +
                    "SELECT 1 " +
                    "FROM DM_DEVICE_GROUP_MAP dgm " +
                    "WHERE dgm.DEVICE_ID = d.ID " +
                    "AND dgm.GROUP_ID = ? )");
        }
        if (searchFilter.getCustomProperty() != null && !searchFilter.getCustomProperty().isEmpty()) {
            isCustomPropertyProvided = true;
            boolean firstCondition = true;
            for (Map.Entry<String, String> entry : searchFilter.getCustomProperty().entrySet()) {
                if (!firstCondition) {
                    sb.append("AND ");
                }
                sb.append("EXISTS (" +
                        "SELECT VALUE_FIELD " +
                        "FROM DM_DEVICE_INFO di " +
                        "WHERE di.DEVICE_ID = d.ID " +
                        "AND di.KEY_FIELD = '").append(entry.getKey()).append("' AND di.VALUE_FIELD LIKE ? )");
                firstCondition = false;
            }
        }
        sb.append("fmm.TENANT_ID = ? OFFSET ? LIMIT ?");

        conn = this.getConnection();
        int index = 1;
        PreparedStatement stmt = conn.prepareStatement(sb.toString());
        if (isFirmwareVersionsProvided) {
            for (String firmwareVersion : firmwareVersions) {
                stmt.setString(index++, firmwareVersion);
            }
        }
        if(isFirmwareModelIdsProvided) {
            for (Integer firmwareModelId : firmwareModelIds) {
                stmt.setInt(index++, firmwareModelId);
            }
        }
        if (isFirmwareNameFilterProvided) {
            stmt.setString(index++, "%" + searchFilter.getFirmwareModelName() +"%");
        }
        if (isDeviceIdentifierProvided) {
            stmt.setString(index++, searchFilter.getDeviceIdentifier());
        }
        if (isFirmwareVersionProvided) {
            stmt.setString(index++, searchFilter.getFirmwareVersion());
        }
        if (isDevicGroupFilterProvided) {
            stmt.setInt(index++, searchFilter.getGroupId());
        }
        if (isCustomPropertyProvided) {
            for (Map.Entry<String, String> entry : searchFilter.getCustomProperty().entrySet()) {
                stmt.setString(index++, "%" + entry.getValue() + "%");
            }
        }
        stmt.setInt(index++, tenantId);
        stmt.setInt(index++, searchFilter.getOffset());
        stmt.setInt(index, searchFilter.getLimit());
        return stmt;
    }

    private String getBaseQuery(QueryType queryType) {
        String query = "";
        if(queryType.equals(QueryType.SELECT)) {
            query += "SELECT d.ID as DEVICE_ID, " +
                    "d.DEVICE_IDENTIFICATION, " +
                    "d.NAME, " +
                    "d.DESCRIPTION, " +
                    "de.ID as ENROLMENT_ID, " +
                    "de.OWNER, " +
                    "de.OWNERSHIP, " +
                    "de.STATUS, " +
                    "de.DATE_OF_LAST_UPDATE " +
                    "de.DEVICE_TYPE ";
        } else if (queryType.equals(QueryType.COUNT)) {
            query += "SELECT COUNT(*) AS RECORDS_TOTAL ";
        }
        query += "FROM DM_DEVICE_FIRMWARE_MODEL_MAPPING dfmm" +
                "INNER JOIN DM_DEVICE d ON dfmm.DM_DEVICE_ID = d.ID " +
                "INNER JOIN DM_ENROLMENT de ON d.ID = de.DEVICE_ID ";
        return query;
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }
}
