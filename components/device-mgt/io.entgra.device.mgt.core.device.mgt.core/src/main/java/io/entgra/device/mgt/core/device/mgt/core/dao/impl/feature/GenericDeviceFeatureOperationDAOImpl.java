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
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceFeatureOperationsDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceFeatureOperationsDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceFeatureOperationDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class GenericDeviceFeatureOperationDAOImpl implements DeviceFeatureOperationDAO {
    private static final Log log = LogFactory.getLog(GenericDeviceFeatureOperationDAOImpl.class);

    @Override
    public void updateDeviceFeatureDetails(List<DeviceFeatureInfo> featureList)
            throws DeviceFeatureOperationsDAOException {
        String selectQuery =
                "SELECT OPERATION_CODE, " +
                        "DEVICE_TYPE " +
                        "FROM DM_OPERATION_DETAILS";
        String insertQuery =
                "INSERT INTO DM_OPERATION_DETAILS " +
                "(OPERATION_CODE, " +
                        "OPERATION_NAME, " +
                        "OPERATION_DESCRIPTION, " +
                        "DEVICE_TYPE) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection connection = DeviceFeatureOperationsDAOFactory.getConnection()) {
            // fetch existing code + deviceType pairs
            Set<String> existingKeys = new HashSet<>();
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                 ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("OPERATION_CODE") + "|" + rs.getString("DEVICE_TYPE");
                    existingKeys.add(key);
                }
            }
            // filter out already existing records
            List<DeviceFeatureInfo> toInsert = featureList.stream()
                    .filter(f -> !existingKeys.contains(f.getOperationCode()
                            + "|" + f.getDeviceType()))
                    .collect(Collectors.toList());
            if (!toInsert.isEmpty()) {
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    for (DeviceFeatureInfo featureInfo : toInsert) {
                        insertStmt.setString(1, featureInfo.getOperationCode());
                        insertStmt.setString(2, featureInfo.getName());
                        insertStmt.setString(3, featureInfo.getDescription());
                        insertStmt.setString(4, featureInfo.getDeviceType());
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating device feature details.";
            log.error(msg, e);
            throw new DeviceFeatureOperationsDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceFeatureInfo> getOperationDetails(String code, String name, String type,
                                                       boolean removeDeduplicateCode)
            throws DeviceFeatureOperationsDAOException {
        List<DeviceFeatureInfo> operationList = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT " +
                        "ID, " +
                        "OPERATION_CODE, " +
                        "OPERATION_NAME, " +
                        "OPERATION_DESCRIPTION");
        if (!removeDeduplicateCode) {
            query.append(", DEVICE_TYPE");
        }
        query.append(" FROM DM_OPERATION_DETAILS WHERE 1=1");
        if (code != null && !code.isBlank()) {
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
                if (code != null && !code.isBlank()) stmt.setString(index++, "%" + code + "%");
                if (name != null) stmt.setString(index++, "%" + name + "%");
                if (type != null) stmt.setString(index++, type);
                try (ResultSet rs = stmt.executeQuery()) {
                    Map<String, DeviceFeatureInfo> dedupedMap = new LinkedHashMap<>();
                    while (rs.next()) {
                        String operationCode = rs.getString("OPERATION_CODE");
                        if (removeDeduplicateCode && dedupedMap.containsKey(operationCode)) {
                            continue;
                        }
                        DeviceFeatureInfo info = new DeviceFeatureInfo();
                        info.setId(rs.getInt("ID"));
                        info.setOperationCode(operationCode);
                        info.setName(rs.getString("OPERATION_NAME"));
                        info.setDescription(rs.getString("OPERATION_DESCRIPTION"));
                        if (!removeDeduplicateCode) {
                            info.setDeviceType(rs.getString("DEVICE_TYPE"));
                        }
                        if (removeDeduplicateCode) {
                            dedupedMap.put(operationCode, info);
                        } else {
                            operationList.add(info);
                        }
                    }
                    if (removeDeduplicateCode) {
                        operationList.addAll(dedupedMap.values());
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error retrieving filtered operation details from DB.";
            log.error(msg, e);
            throw new DeviceFeatureOperationsDAOException(msg, e);
        }
        return operationList;
    }

    @Override
    public Map<String, Boolean> operationCodesExist(List<String> codes) throws DeviceFeatureOperationsDAOException {
        Map<String, Boolean> result = new HashMap<>();
        if (codes == null || codes.isEmpty()) return result;
        String placeholders = String.join(",", Collections.nCopies(codes.size(), "?"));
        String query =
                "SELECT OPERATION_CODE " +
                        "FROM DM_OPERATION_DETAILS " +
                        "WHERE OPERATION_CODE " +
                        "IN (" + placeholders + ")";
        try {
            Connection connection = DeviceFeatureOperationsDAOFactory.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
                for (int i = 0; i < codes.size(); i++) {
                    stmt.setString(i + 1, codes.get(i));
                    result.put(codes.get(i), false);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String code = rs.getString("OPERATION_CODE");
                        result.put(code, true);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error checking existence of operation codes.";
            log.error(msg, e);
            throw new DeviceFeatureOperationsDAOException(msg, e);
        }
        return result;
    }
}
