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

package io.entgra.device.mgt.core.subtype.mgt.dao.impl;

import io.entgra.device.mgt.core.subtype.mgt.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.subtype.mgt.dao.util.DAOUtil;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.exception.DBConnectionException;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for MySQL
 */
public class DeviceSubTypeMySQLDAOImpl extends DeviceSubTypeDAOImpl {

    private static final Log log = LogFactory.getLog(DeviceSubTypeMySQLDAOImpl.class);

    @Override
    public List<DeviceSubType> getDeviceSubTypesByModule(int moduleId, int tenantId, String deviceType)
            throws SubTypeMgtDAOException {
        try {
            String sql = "SELECT * FROM DM_DEVICE_SUB_TYPE WHERE TENANT_ID = ? AND DEVICE_TYPE = ? " +
                    "AND JSON_CONTAINS(TYPE_DEFINITION, ?, '$.compatibleComModulesIds') ORDER BY SUB_TYPE_ID";
            Connection conn = ConnectionManagerUtil.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.setString(2, deviceType);
                stmt.setString(3, String.valueOf(moduleId));
                try (ResultSet rs = stmt.executeQuery()) {
                    List<DeviceSubType> supportedDeviceSubTypes = new ArrayList<>();
                    while (rs.next()) {
                        supportedDeviceSubTypes.add(DAOUtil.loadDeviceSubType(rs));
                    }
                    return supportedDeviceSubTypes;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieve device sub types supported by " +
                    "module ID: " + moduleId;
            log.error(msg, e);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieve device sub types supported by module ID: " +
                    moduleId;
            log.error(msg, e);
            throw new SubTypeMgtDAOException(msg, e);
        }
    }
}
