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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeviceTypeMetaDataDAOImpl extends AbstractDeviceTypeMetaDataDAOImpl {

    @Override
    public boolean updateDeviceTypeMetaEntry(String deviceType, int tenantId, DeviceTypeMetaEntry metaEntry)
            throws DeviceManagementDAOException {
        try {
            String sql = "UPDATE DM_DEVICE_TYPE_META m " +
                    "JOIN DM_DEVICE_TYPE d ON m.DEVICE_TYPE_ID = d.ID " +
                    "SET m.META_VALUE = ?, m.LAST_UPDATED_TIMESTAMP = ? " +
                    "WHERE m.TENANT_ID = ? " +
                    "AND d.PROVIDER_TENANT_ID = ? " +
                    "AND d.NAME = ? " +
                    "AND m.META_KEY = ?";
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, metaEntry.getMetaValue());
                stmt.setLong(2, System.currentTimeMillis());
                stmt.setInt(3, tenantId);
                stmt.setInt(4, tenantId);
                stmt.setString(5, deviceType);
                stmt.setString(6, metaEntry.getMetaKey());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            String msg = "Failed to update META_KEY in MySQL.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean deleteDeviceTypeMetaEntry(String deviceType, int tenantId, String metaKey)
            throws DeviceManagementDAOException {
        try {
            String sql = "DELETE m FROM DM_DEVICE_TYPE_META m " +
                    "JOIN DM_DEVICE_TYPE d ON m.DEVICE_TYPE_ID = d.ID " +
                    "WHERE m.TENANT_ID = ? " +
                    "AND d.PROVIDER_TENANT_ID = ? " +
                    "AND d.NAME = ? " +
                    "AND m.META_KEY = ?";
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.setInt(2, tenantId);
                stmt.setString(3, deviceType);
                stmt.setString(4, metaKey);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            String msg = "Failed to delete META_KEY in MySQL.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }
}

