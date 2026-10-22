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

package io.entgra.device.mgt.core.operation.template.dao.impl;

import com.google.gson.JsonParser;
import io.entgra.device.mgt.core.operation.template.dao.impl.util.OperationTemplateManagementUtil;
import io.entgra.device.mgt.core.operation.template.exception.DBConnectionException;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateManagementDAOException;
import io.entgra.device.mgt.core.operation.template.util.ConnectionManagerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation for MySQL
 */
public class OperationTemplateMySQLDAOImpl extends OperationTemplateDAOImpl {

    private static final Log log = LogFactory.getLog(OperationTemplateMySQLDAOImpl.class);

    @Override
    public List<String> getOperationRegisters(String deviceType, String subTypeId, String operationCode)
            throws OperationTemplateManagementDAOException {
        try {
            String sql = "SELECT JSON_EXTRACT(OPERATION_DEFINITION, '$.registers') AS REGISTERS " +
                    "FROM SUB_OPERATION_TEMPLATE WHERE DEVICE_TYPE = ? AND SUB_TYPE_ID = ? AND OPERATION_CODE = ?";
            Connection conn = ConnectionManagerUtils.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, deviceType);
                stmt.setString(2, subTypeId);
                stmt.setString(3, operationCode);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    String registers = rs.getString("REGISTERS");
                    return OperationTemplateManagementUtil.getRegisterList(
                            registers == null ? null : new JsonParser().parse(registers));
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieve registers for operation code: " +
                    operationCode;
            log.error(msg, e);
            throw new OperationTemplateManagementDAOException(msg, e);
        } catch (SQLException | RuntimeException e) {
            String msg = "Error occurred while retrieving registers for operation code: " + operationCode;
            log.error(msg, e);
            throw new OperationTemplateManagementDAOException(msg, e);
        }
    }

}
