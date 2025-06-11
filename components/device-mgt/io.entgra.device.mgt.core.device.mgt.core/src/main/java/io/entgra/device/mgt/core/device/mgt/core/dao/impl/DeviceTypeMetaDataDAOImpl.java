/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceTypeMetaDataDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceTypeMetaDataDAOImpl implements DeviceTypeMetaDataDAO {

    private static final Log log = LogFactory.getLog(DeviceTypeMetaDataDAOImpl.class);

    /**
     * Inserts a new META_KEY/META_VALUE pair into DM_DEVICE_TYPE_META.
     */
    public boolean createDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey, String metaValue)
            throws DeviceManagementDAOException {
        String insertSQL = "INSERT INTO DM_DEVICE_TYPE_META (META_KEY, META_VALUE, LAST_UPDATED_TIMESTAMP, TENANT_ID, DEVICE_TYPE_ID) " +
                "SELECT ?, ?, ?, ?, d.ID " +
                "FROM DM_DEVICE_TYPE d " +
                "WHERE d.NAME = ? AND d.PROVIDER_TENANT_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setString(1, metaKey);
            stmt.setString(2, metaValue);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setInt(4, tenantId);
            stmt.setString(5, deviceType);
            stmt.setInt(6, tenantId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String msg = "Failed to insert META_KEY: " + metaKey + " for deviceType: " + deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /**
     * Updates the value of an existing META_KEY.
     */
    public boolean updateDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey, String metaValue)
            throws DeviceManagementDAOException {
        String updateSQL = "UPDATE DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d ON m.DEVICE_TYPE_ID = d.ID " +
                "SET m.META_VALUE = ?, m.LAST_UPDATED_TIMESTAMP = ? " +
                "WHERE m.TENANT_ID = ? AND d.PROVIDER_TENANT_ID = ? AND d.NAME = ? AND m.META_KEY = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
            stmt.setString(1, metaValue);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, tenantId);
            stmt.setString(5, deviceType);
            stmt.setString(6, metaKey);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String msg = "Failed to update META_KEY: " + metaKey + " for deviceType: " + deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /**
     * Retrieves the META_VALUE for a given key.
     */
    public String getDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException {
        String selectSQL = "SELECT m.META_VALUE " +
                "FROM DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d ON m.DEVICE_TYPE_ID = d.ID " +
                "WHERE m.TENANT_ID = ? AND d.PROVIDER_TENANT_ID = ? AND d.NAME = ? AND m.META_KEY = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceType);
            stmt.setString(4, metaKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("META_VALUE");
                }
            }
        } catch (SQLException e) {
            String msg = "Failed to retrieve META_VALUE for META_KEY: " + metaKey + ", deviceType: " + deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return null;
    }

    /**
     * Deletes the given META_KEY from the database.
     */
    public boolean deleteDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException {
        String deleteSQL = "DELETE m FROM DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d ON m.DEVICE_TYPE_ID = d.ID " +
                "WHERE m.TENANT_ID = ? AND d.PROVIDER_TENANT_ID = ? AND d.NAME = ? AND m.META_KEY = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceType);
            stmt.setString(4, metaKey);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String msg = "Failed to delete META_KEY: " + metaKey + " for deviceType: " + deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /**
     * Checks if a META_KEY exists.
     */
    public boolean isDeviceTypeMetaEntryExist(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException {
        String checkSQL = "SELECT 1 FROM DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d ON m.DEVICE_TYPE_ID = d.ID " +
                "WHERE m.TENANT_ID = ? AND d.PROVIDER_TENANT_ID = ? AND d.NAME = ? AND m.META_KEY = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSQL)) {
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceType);
            stmt.setString(4, metaKey);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            String msg = "Failed to check existence of META_KEY: " + metaKey + " for deviceType: " + deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }
}
