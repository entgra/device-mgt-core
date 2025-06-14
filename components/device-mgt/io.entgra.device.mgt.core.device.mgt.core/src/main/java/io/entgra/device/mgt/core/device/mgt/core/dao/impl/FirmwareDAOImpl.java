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

import io.entgra.device.mgt.core.device.mgt.common.app.mgt.DeviceFirmwareModel;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.FirmwareDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementation of the FirmwareDAO interface for managing device firmware models.
 */
public class FirmwareDAOImpl implements FirmwareDAO {
    private static final Log log = LogFactory.getLog(FirmwareDAOImpl.class);

    @Override
    public DeviceFirmwareModel getDeviceFirmwareModel(int deviceId, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        DeviceFirmwareModel deviceFirmwareModel = null;
        String sql = "SELECT ID, " +
                "FIRMWARE_MODEL, " +
                "DESCRIPTION " +
                "FROM DM_DEVICE_FIRMWARE_MODEL M " +
                "INNER JOIN DM_DEVICE_FIRMWARE_MODEL_MAPPING D " +
                "ON D.FIRMWARE_MODEL_ID = M.ID " +
                "WHERE D.DM_DEVICE_ID = ? AND M.TENANT_ID = ?";  //todo tenantId is not required for filtering
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
    public DeviceFirmwareModel addFirmwareModel(DeviceFirmwareModel deviceFirmwareModel, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        int firmwareId = -1;
        String sql = "INSERT INTO DM_DEVICE_FIRMWARE_MODEL (FIRMWARE_MODEL, DESCRIPTION, TENANT_ID) " +
                "VALUES (?, ?, ?)";

        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, deviceFirmwareModel.getFirmwareModelName());
                stmt.setString(2, deviceFirmwareModel.getDescription());
                stmt.setInt(3, tenantId);
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
    public DeviceFirmwareModel getExistingFirmwareModel(String firmwareModel, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        DeviceFirmwareModel deviceFirmwareModel = null;
        String sql = "SELECT ID, FIRMWARE_MODEL, DESCRIPTION " +
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
    public boolean addDeviceFirmwareMapping(int deviceId, int firmwareId, int tenantId) throws DeviceManagementDAOException {
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

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }
}
