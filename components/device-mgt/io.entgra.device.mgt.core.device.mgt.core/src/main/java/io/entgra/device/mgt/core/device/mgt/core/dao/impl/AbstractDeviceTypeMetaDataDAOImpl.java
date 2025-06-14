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

import io.entgra.device.mgt.core.device.mgt.common.type.DeviceTypeMetaEntry;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceTypeMetaDataDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDeviceTypeMetaDataDAOImpl implements DeviceTypeMetaDataDAO {

    /**
     * Retrieves a database connection from the {@link DeviceManagementDAOFactory}.
     *
     * @return A {@link Connection} object for interacting with the database.
     * @throws SQLException If an error occurs while obtaining the connection.
     */
    protected Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    protected static final Log log = LogFactory.getLog(AbstractDeviceTypeMetaDataDAOImpl.class);

    @Override
    public boolean isDeviceTypeMetaEntryExist(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException {
        String checkSQL = "SELECT 1 " +
                "FROM DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d ON m.DEVICE_TYPE_ID = d.ID " +
                "WHERE m.TENANT_ID = ? " +
                "AND d.PROVIDER_TENANT_ID = ? " +
                "AND d.NAME = ? " +
                "AND m.META_KEY = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSQL)) {
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceType);
            stmt.setString(4, metaKey);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            String msg = "Failed to check existence of META_KEY: " + metaKey + " for deviceType: " + deviceType
                    + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public DeviceTypeMetaEntry getDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException {
        String selectSQL = "SELECT m.META_KEY, m.META_VALUE " +
                "FROM DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d ON m.DEVICE_TYPE_ID = d.ID " +
                "WHERE m.TENANT_ID = ? " +
                "AND d.PROVIDER_TENANT_ID = ? " +
                "AND d.NAME = ? " +
                "AND m.META_KEY = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceType);
            stmt.setString(4, metaKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DeviceTypeMetaEntry entry = new DeviceTypeMetaEntry();
                    entry.setMetaKey(rs.getString("META_KEY"));
                    entry.setMetaValue(rs.getString("META_VALUE"));
                    return entry;
                }
            }
        } catch (SQLException e) {
            String msg = "Failed to retrieve metadata entry for META_KEY: " + metaKey +
                    ", deviceType: " + deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return null;
    }

    @Override
    public List<DeviceTypeMetaEntry> getDeviceTypeMetaEntries(String deviceType, int tenantId)
            throws DeviceManagementDAOException {
        String selectSQL = "SELECT m.META_KEY, m.META_VALUE " +
                "FROM DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d ON m.DEVICE_TYPE_ID = d.ID " +
                "WHERE m.TENANT_ID = ? " +
                "AND d.PROVIDER_TENANT_ID = ? " +
                "AND d.NAME = ?";
        List<DeviceTypeMetaEntry> metaEntries = new ArrayList<>();
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DeviceTypeMetaEntry metaEntry = new DeviceTypeMetaEntry();
                    metaEntry.setMetaKey(rs.getString("META_KEY"));
                    metaEntry.setMetaValue(rs.getString("META_VALUE"));
                    metaEntries.add(metaEntry);
                }
            }
        } catch (SQLException e) {
            String msg = "Failed to retrieve metadata entries for deviceType: " + deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return metaEntries;
    }

    @Override
    public boolean createDeviceTypeMetaEntry(String deviceType, int tenantId, DeviceTypeMetaEntry entry)
            throws DeviceManagementDAOException {
        try {
            String insertSQL = "INSERT INTO DM_DEVICE_TYPE_META " +
                    "(META_KEY, META_VALUE, LAST_UPDATED_TIMESTAMP, TENANT_ID, DEVICE_TYPE_ID) " +
                    "SELECT ?, ?, ?, ?, d.ID " +
                    "FROM DM_DEVICE_TYPE d " +
                    "WHERE d.NAME = ? " +
                    "AND d.PROVIDER_TENANT_ID = ?";
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                stmt.setString(1, entry.getMetaKey());
                stmt.setString(2, entry.getMetaValue());
                stmt.setLong(3, System.currentTimeMillis());
                stmt.setInt(4, tenantId);
                stmt.setString(5, deviceType);
                stmt.setInt(6, tenantId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            String msg = String.format("Failed to insert META_KEY: %s for deviceType: %s, tenantId: %d",
                    entry.getMetaKey(), deviceType, tenantId);
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }
}
