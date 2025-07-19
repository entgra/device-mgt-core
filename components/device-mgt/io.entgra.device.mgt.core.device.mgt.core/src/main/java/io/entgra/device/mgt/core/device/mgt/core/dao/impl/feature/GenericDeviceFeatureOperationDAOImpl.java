/*
 *  Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.device.mgt.core.dao.impl.feature;

import io.entgra.device.mgt.core.device.mgt.common.dto.DeviceFeatureInfo;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceFeatureOperationsDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceFeatureOperationDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericDeviceFeatureOperationDAOImpl implements DeviceFeatureOperationDAO {
    private static final Log log = LogFactory.getLog(GenericDeviceFeatureOperationDAOImpl.class);

    @Override
    public void updateDeviceFeatureDetails(List<DeviceFeatureInfo> featureList) throws DeviceManagementDAOException {
        String insertQuery = "INSERT INTO DM_OPERATION_DETAILS " +
                "(OPERATION_CODE, OPERATION_NAME, OPERATION_DESCRIPTION, DEVICE_TYPE) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "OPERATION_NAME = VALUES(OPERATION_NAME), " +
                "OPERATION_DESCRIPTION = VALUES(OPERATION_DESCRIPTION)";
        try {
            Connection connection = DeviceFeatureOperationsDAOFactory.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                for (DeviceFeatureInfo featureInfo : featureList) {
                    preparedStatement.setString(1, featureInfo.getOperationCode());
                    preparedStatement.setString(2, featureInfo.getName());
                    preparedStatement.setString(3, featureInfo.getDescription());
                    preparedStatement.setString(4, featureInfo.getDeviceType());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating device feature details.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceFeatureInfo> getOperationDetails(String code, String name, String type)
            throws DeviceManagementDAOException {
        List<DeviceFeatureInfo> operationList = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT " +
                        "ID, " +
                        "OPERATION_CODE, " +
                        "OPERATION_NAME, " +
                        "OPERATION_DESCRIPTION, " +
                        "DEVICE_TYPE " +
                        "FROM DM_OPERATION_DETAILS " +
                        "WHERE 1=1");
        if (code != null) {
            query.append(" AND OPERATION_CODE LIKE ?");
        }
        if (name != null) {
            query.append(" AND OPERATION_NAME LIKE ?");
        }
        if (type != null) {
            query.append(" AND DEVICE_TYPE = ?");
        }
        try {
            Connection connection = DeviceFeatureOperationsDAOFactory.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
                int index = 1;
                if (code != null) stmt.setString(index++, "%" + code + "%");
                if (name != null) stmt.setString(index++, "%" + name + "%");
                if (type != null) stmt.setString(index++, type);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        DeviceFeatureInfo info = new DeviceFeatureInfo();
                        info.setId(rs.getInt("ID"));
                        info.setOperationCode(rs.getString("OPERATION_CODE"));
                        info.setName(rs.getString("OPERATION_NAME"));
                        info.setDescription(rs.getString("OPERATION_DESCRIPTION"));
                        info.setDeviceType(rs.getString("DEVICE_TYPE"));
                        operationList.add(info);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error retrieving filtered operation details from DB.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return operationList;
    }
}
