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

public class GenericDeviceFeatureOperationDAOImpl implements DeviceFeatureOperationDAO {
    private static final Log log = LogFactory.getLog(GenericDeviceFeatureOperationDAOImpl.class);
    private static final String DEFAULT_FEATURE_TYPE = "operation";
    private static final String FEATURE_TYPE_TASK = "task";

    @Override
    public void updateDeviceFeatureDetails(List<DeviceFeatureInfo> featureList)
            throws DeviceFeatureOperationsDAOException {
        String selectQuery =
                "SELECT OPERATION_CODE, " +
                        "DEVICE_TYPE, " +
                        "TYPE " +
                        "FROM DM_FEATURE_DETAILS";
        String insertQuery =
                "INSERT INTO DM_FEATURE_DETAILS " +
                "(OPERATION_CODE, " +
                        "OPERATION_NAME, " +
                        "OPERATION_DESCRIPTION, " +
                        "DEVICE_TYPE, " +
                        "TYPE) " +
                "VALUES (?, ?, ?, ?, ?)";
        // Task features are one row per code; sync DEVICE_TYPE when catalog changes (e.g. ALL → NONE).
        String syncTaskDeviceTypeQuery =
                "UPDATE DM_FEATURE_DETAILS " +
                        "SET DEVICE_TYPE = ?, " +
                        "OPERATION_NAME = ?, " +
                        "OPERATION_DESCRIPTION = ? " +
                "WHERE OPERATION_CODE = ? " +
                        "AND TYPE = ?";
        try (Connection connection = DeviceFeatureOperationsDAOFactory.getConnection()) {
            Set<String> existingKeys = new HashSet<>();
            Set<String> existingTaskCodeKeys = new HashSet<>();
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                 ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("OPERATION_CODE");
                    String deviceType = rs.getString("DEVICE_TYPE");
                    String type = rs.getString("TYPE");
                    existingKeys.add(code + "|" + deviceType + "|" + type);
                    if (FEATURE_TYPE_TASK.equalsIgnoreCase(type)) {
                        existingTaskCodeKeys.add(code + "|" + FEATURE_TYPE_TASK);
                    }
                }
            }
            List<DeviceFeatureInfo> toInsert = new ArrayList<>();
            List<DeviceFeatureInfo> tasksToSync = new ArrayList<>();
            for (DeviceFeatureInfo featureInfo : featureList) {
                String featureType = resolveFeatureType(featureInfo.getType());
                if (FEATURE_TYPE_TASK.equalsIgnoreCase(featureType)) {
                    if (existingTaskCodeKeys.contains(featureInfo.getOperationCode() + "|" + FEATURE_TYPE_TASK)) {
                        tasksToSync.add(featureInfo);
                    } else {
                        toInsert.add(featureInfo);
                    }
                } else if (!existingKeys.contains(featureInfo.getOperationCode()
                        + "|" + featureInfo.getDeviceType() + "|" + featureType)) {
                    toInsert.add(featureInfo);
                }
            }
            if (!toInsert.isEmpty()) {
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    for (DeviceFeatureInfo featureInfo : toInsert) {
                        insertStmt.setString(1, featureInfo.getOperationCode());
                        insertStmt.setString(2, featureInfo.getName());
                        insertStmt.setString(3, featureInfo.getDescription());
                        insertStmt.setString(4, featureInfo.getDeviceType());
                        insertStmt.setString(5, resolveFeatureType(featureInfo.getType()));
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }
            if (!tasksToSync.isEmpty()) {
                try (PreparedStatement syncStmt = connection.prepareStatement(syncTaskDeviceTypeQuery)) {
                    for (DeviceFeatureInfo featureInfo : tasksToSync) {
                        syncStmt.setString(1, featureInfo.getDeviceType());
                        syncStmt.setString(2, featureInfo.getName());
                        syncStmt.setString(3, featureInfo.getDescription());
                        syncStmt.setString(4, featureInfo.getOperationCode());
                        syncStmt.setString(5, FEATURE_TYPE_TASK);
                        syncStmt.addBatch();
                    }
                    syncStmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating device feature details.";
            log.error(msg, e);
            throw new DeviceFeatureOperationsDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceFeatureInfo> getOperationDetails(String code, String name, String deviceType,
                                                       String featureType, boolean removeDeduplicateCode)
            throws DeviceFeatureOperationsDAOException {
        List<DeviceFeatureInfo> operationList = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT " +
                        "ID, " +
                        "OPERATION_CODE, " +
                        "OPERATION_NAME, " +
                        "OPERATION_DESCRIPTION, " +
                        "TYPE");
        if (!removeDeduplicateCode) {
            query.append(", DEVICE_TYPE");
        }
        query.append(" FROM DM_FEATURE_DETAILS WHERE 1=1");
        if (code != null && !code.isBlank()) {
            query.append(" AND OPERATION_CODE LIKE ?");
        }
        if (name != null) {
            query.append(" AND OPERATION_NAME LIKE ?");
        }
        if (deviceType != null) {
            query.append(" AND DEVICE_TYPE = ?");
        }
        if (featureType != null && !featureType.isBlank()) {
            query.append(" AND TYPE = ?");
        }
        try {
            Connection connection = DeviceFeatureOperationsDAOFactory.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
                int index = 1;
                if (code != null && !code.isBlank()) {
                    stmt.setString(index++, "%" + code + "%");
                }
                if (name != null) {
                    stmt.setString(index++, "%" + name + "%");
                }
                if (deviceType != null) {
                    stmt.setString(index++, deviceType);
                }
                if (featureType != null && !featureType.isBlank()) {
                    stmt.setString(index++, featureType);
                }
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
                        info.setType(rs.getString("TYPE"));
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
            String msg = "Error retrieving filtered feature details from DB.";
            log.error(msg, e);
            throw new DeviceFeatureOperationsDAOException(msg, e);
        }
        return operationList;
    }

    @Override
    public Map<String, Boolean> featureCodesExist(List<String> codes, String featureType)
            throws DeviceFeatureOperationsDAOException {
        Map<String, Boolean> result = new HashMap<>();
        if (codes == null || codes.isEmpty()) {
            return result;
        }
        String placeholders = String.join(",", Collections.nCopies(codes.size(), "?"));
        StringBuilder query = new StringBuilder(
                "SELECT OPERATION_CODE " +
                        "FROM DM_FEATURE_DETAILS " +
                        "WHERE OPERATION_CODE " +
                        "IN (" + placeholders + ")");
        if (featureType != null && !featureType.isBlank()) {
            query.append(" AND TYPE = ?");
        }
        try {
            Connection connection = DeviceFeatureOperationsDAOFactory.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
                int index = 1;
                for (String code : codes) {
                    stmt.setString(index++, code);
                    result.put(code, false);
                }
                if (featureType != null && !featureType.isBlank()) {
                    stmt.setString(index, featureType);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        result.put(rs.getString("OPERATION_CODE"), true);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error checking existence of feature codes.";
            log.error(msg, e);
            throw new DeviceFeatureOperationsDAOException(msg, e);
        }
        return result;
    }

    private static String resolveFeatureType(String type) {
        return (type == null || type.isBlank()) ? DEFAULT_FEATURE_TYPE : type;
    }
}
