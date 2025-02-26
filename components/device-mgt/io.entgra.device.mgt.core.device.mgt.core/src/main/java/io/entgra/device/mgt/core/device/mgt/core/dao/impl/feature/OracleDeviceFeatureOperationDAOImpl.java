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
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceFeatureOperationDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceFeatureOperationsDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OracleDeviceFeatureOperationDAOImpl implements DeviceFeatureOperationDAO {
    private static final Log log = LogFactory.getLog(OracleDeviceFeatureOperationDAOImpl.class);
    public void updateDeviceFeatureDetails(List<DeviceFeatureInfo> featureList) throws DeviceManagementDAOException {
        String insertQuery = "MERGE INTO DM_OPERATION_DETAILS target " +
                "USING (SELECT ? AS OPERATION_CODE, ? " +
                "AS OPERATION_NAME, ? " +
                "AS OPERATION_DESCRIPTION, ? " +
                "AS DEVICE_TYPE " +
                "FROM dual) source " +
                "ON (target.OPERATION_CODE = source.OPERATION_CODE) " +
                "WHEN MATCHED THEN " +
                "UPDATE SET target.OPERATION_NAME = source.OPERATION_NAME, " +
                "target.OPERATION_DESCRIPTION = source.OPERATION_DESCRIPTION " +
                "WHEN NOT MATCHED THEN " +
                "INSERT (OPERATION_CODE, OPERATION_NAME, OPERATION_DESCRIPTION, DEVICE_TYPE) " +
                "VALUES " +
                "(source.OPERATION_CODE, source.OPERATION_NAME, source.OPERATION_DESCRIPTION, source.DEVICE_TYPE)";
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
            String msg = "Error occurred while updating device feature details in Oracle.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }
}

