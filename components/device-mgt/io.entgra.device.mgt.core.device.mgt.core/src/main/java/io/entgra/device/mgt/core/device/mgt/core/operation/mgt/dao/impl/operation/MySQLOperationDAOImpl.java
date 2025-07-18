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

package io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.impl.operation;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.ActivityHolder;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.ActivityStatus;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationResponse;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.DeviceOperationDetails;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.impl.GenericOperationDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the implementation of OperationDAO which can be used to support MySQl db syntax.
 */
public class MySQLOperationDAOImpl extends GenericOperationDAOImpl {

    private static final Log log = LogFactory.getLog(MySQLOperationDAOImpl.class);

    @Override
    public List<Activity> getActivityList(List<Integer> activityIds) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Activity activity;
        List<Activity> activities = new ArrayList<>();

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();

            String sql1 = "SELECT " +
                    "    eom.ENROLMENT_ID," +
                    "    eom.CREATED_TIMESTAMP," +
                    "    eom.UPDATED_TIMESTAMP," +
                    "    eom.OPERATION_ID," +
                    "    eom.OPERATION_CODE," +
                    "    eom.INITIATED_BY," +
                    "    eom.TYPE," +
                    "    eom.STATUS," +
                    "    eom.DEVICE_ID," +
                    "    eom.DEVICE_IDENTIFICATION," +
                    "    eom.DEVICE_TYPE," +
                    "    opr.ID AS OP_RES_ID," +
                    "    opr.RECEIVED_TIMESTAMP," +
                    "    opr.OPERATION_RESPONSE," +
                    "    opr.IS_LARGE_RESPONSE " +
                    "FROM " +
                    "    DM_ENROLMENT_OP_MAPPING eom " +
                    "        LEFT JOIN " +
                    "    DM_DEVICE_OPERATION_RESPONSE opr ON opr.EN_OP_MAP_ID = eom.ID " +
                    "WHERE " +
                    "    eom.OPERATION_ID IN (";

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < activityIds.size(); i++) {
                builder.append("?,");
            }
            sql1 += builder.deleteCharAt(builder.length() - 1).toString() + ") AND eom.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql1);
            int i;
            for (i = 0; i < activityIds.size(); i++) {
                stmt.setInt(i + 1, activityIds.get(i));
            }
            stmt.setInt(i + 1, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            rs = stmt.executeQuery();

            int operationId = 0;
            int enrolmentId = 0;
            int responseId = 0;
            ActivityStatus activityStatus = new ActivityStatus();
            List<Integer> largeResponseIDs = new ArrayList<>();
            while (rs.next()) {
                activity = new Activity();

                if (operationId != rs.getInt("OPERATION_ID")) {
                    activities.add(activity);
                    List<ActivityStatus> statusList = new ArrayList<>();
                    activityStatus = new ActivityStatus();

                    operationId = rs.getInt("OPERATION_ID");
                    enrolmentId = rs.getInt("ENROLMENT_ID");

                    activity.setType(Activity.Type.valueOf(rs.getString("TYPE")));
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                    activity.setInitiatedBy(rs.getString("INITIATED_BY"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));

                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(
                                new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());

                    }
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                        responseId = rs.getInt("OP_RES_ID");
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(responseId);
                        } else {
                            operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                    activityStatus.setResponses(operationResponses);
                    statusList.add(activityStatus);
                    activity.setActivityStatus(statusList);
                    activity.setActivityId(OperationDAOUtil.getActivityId(rs.getInt("OPERATION_ID")));
                }

                if (operationId == rs.getInt("OPERATION_ID") && enrolmentId != rs.getInt("ENROLMENT_ID")) {
                    activityStatus = new ActivityStatus();

                    activity.setType(Activity.Type.valueOf(rs.getString("TYPE")));
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                    activity.setInitiatedBy(rs.getString("INITIATED_BY"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(
                                new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
                    }
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                        responseId = rs.getInt("OP_RES_ID");
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(responseId);
                        } else {
                            operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                    activityStatus.setResponses(operationResponses);
                    activity.getActivityStatus().add(activityStatus);

                    enrolmentId = rs.getInt("ENROLMENT_ID");
                }

                if (rs.getInt("OP_RES_ID") != 0 && responseId != rs.getInt("OP_RES_ID") && rs.getTimestamp(
                        "RECEIVED_TIMESTAMP") != null) {
                    responseId = rs.getInt("OP_RES_ID");
                    if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                        largeResponseIDs.add(responseId);
                    } else {
                        activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                    }
                }
            }
            if(!largeResponseIDs.isEmpty()) {
                populateLargeOperationResponses(activities, largeResponseIDs);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while getting the operation details from " + "the database.", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activities;
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset)
            throws OperationManagementDAOException {
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String sql = "SELECT " +
                         "    eom.ENROLMENT_ID," +
                         "    eom.CREATED_TIMESTAMP," +
                         "    eom.UPDATED_TIMESTAMP," +
                         "    eom.OPERATION_ID," +
                         "    eom.OPERATION_CODE," +
                         "    eom.INITIATED_BY," +
                         "    eom.TYPE," +
                         "    eom.STATUS," +
                         "    eom.DEVICE_ID," +
                         "    eom.DEVICE_IDENTIFICATION," +
                         "    eom.DEVICE_TYPE," +
                         "    opr.ID AS OP_RES_ID," +
                         "    opr.RECEIVED_TIMESTAMP," +
                         "    opr.OPERATION_RESPONSE," +
                         "    opr.IS_LARGE_RESPONSE " +
                         "FROM " +
                         "    DM_ENROLMENT_OP_MAPPING eom FORCE INDEX (IDX_ENROLMENT_OP_MAPPING) " +
                         "LEFT JOIN " +
                         "    DM_DEVICE_OPERATION_RESPONSE opr ON opr.EN_OP_MAP_ID = eom.ID " +
                         "INNER JOIN " +
                         "  (SELECT DISTINCT OPERATION_ID FROM DM_ENROLMENT_OP_MAPPING ORDER BY OPERATION_ID ASC limit ? , ? ) eom_ordered " +
                         "         ON eom_ordered.OPERATION_ID = eom.OPERATION_ID " +
                         "WHERE " +
                         "    eom.UPDATED_TIMESTAMP > ? " +
                         "        AND eom.TENANT_ID = ? " +
                         "ORDER BY eom.OPERATION_ID, eom.UPDATED_TIMESTAMP";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, offset);
                stmt.setInt(2, limit);
                stmt.setLong(3, timestamp);
                stmt.setInt(4, tenantId);

                try (ResultSet rs = stmt.executeQuery()) {
                    ActivityHolder activityHolder = OperationDAOUtil.getActivityHolder(rs);
                    List<Integer> largeResponseIDs = activityHolder.getLargeResponseIDs();
                    List<Activity> activities = activityHolder.getActivityList();

                    if (!largeResponseIDs.isEmpty()) {
                        populateLargeOperationResponses(activities, largeResponseIDs);
                    }
                    return activities;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting the operation details from the database.";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfterByUser(long timestamp, String user, int limit, int offset)
            throws OperationManagementDAOException {
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String sql = "SELECT " +
                    "    eom.ENROLMENT_ID," +
                    "    eom.CREATED_TIMESTAMP," +
                    "    eom.UPDATED_TIMESTAMP," +
                    "    eom.OPERATION_ID," +
                    "    eom.OPERATION_CODE," +
                    "    eom.INITIATED_BY," +
                    "    eom.TYPE," +
                    "    eom.STATUS," +
                    "    eom.DEVICE_ID," +
                    "    eom.DEVICE_IDENTIFICATION," +
                    "    eom.DEVICE_TYPE," +
                    "    opr.ID AS OP_RES_ID," +
                    "    opr.RECEIVED_TIMESTAMP," +
                    "    opr.OPERATION_RESPONSE," +
                    "    opr.IS_LARGE_RESPONSE " +
                    "FROM " +
                    "    DM_ENROLMENT_OP_MAPPING eom FORCE INDEX (IDX_ENROLMENT_OP_MAPPING) " +
                    "LEFT JOIN " +
                    "    DM_DEVICE_OPERATION_RESPONSE opr ON opr.EN_OP_MAP_ID = eom.ID " +
                    "INNER JOIN " +
                    "    (SELECT DISTINCT OPERATION_ID FROM DM_ENROLMENT_OP_MAPPING WHERE INITIATED_BY = ? " +
                    "       ORDER BY OPERATION_ID ASC limit ? , ? ) eom_ordered " +
                    "       ON eom_ordered.OPERATION_ID = eom.OPERATION_ID " +
                    "WHERE " +
                    "    eom.UPDATED_TIMESTAMP > ? " +
                    "        AND eom.TENANT_ID = ? " +
                    "ORDER BY eom.OPERATION_ID, eom.UPDATED_TIMESTAMP";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user);
                stmt.setInt(2, offset);
                stmt.setInt(3, limit);
                stmt.setLong(4, timestamp);
                stmt.setInt(5, tenantId);

                try (ResultSet rs = stmt.executeQuery()) {
                    ActivityHolder activityHolder = OperationDAOUtil.getActivityHolder(rs);
                    List<Integer> largeResponseIDs = activityHolder.getLargeResponseIDs();
                    List<Activity> activities = activityHolder.getActivityList();
                    if (!largeResponseIDs.isEmpty()) {
                        populateLargeOperationResponses(activities, largeResponseIDs);
                    }
                    return activities;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting the operation details from the database.";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
    }

//    @Override
//    public boolean updateOperationStatus(int enrolmentId, int operationId, Operation.Status status)
//            throws OperationManagementDAOException {
//        String query =
//                "UPDATE DM_ENROLMENT_OP_MAPPING " +
//                        "SET STATUS=?, " +
//                        "UPDATED_TIMESTAMP=? " +
//                "WHERE ENROLMENT_ID=? " +
//                        "AND OPERATION_ID=?";
//        try (Connection connection = OperationManagementDAOFactory.getConnection();
//             PreparedStatement stmt = connection.prepareStatement(query)) {
//            long time = DeviceManagementDAOUtil.getCurrentUTCTime();
//            stmt.setString(1, status.toString());
//            stmt.setTimestamp(2, new Timestamp(time));
//            stmt.setInt(3, enrolmentId);
//            stmt.setInt(4, operationId);
//            return stmt.executeUpdate() > 0;
//        } catch (SQLException e) {
//            throw new OperationManagementDAOException("Error updating operation status in MySQL.", e);
//        }
//    }
//
//    @Override
//    public DeviceOperationDetails getDeviceOperationDetails(int enrolmentId, int operationId)
//            throws OperationManagementDAOException {
//        DeviceOperationDetails deviceOperationDetails = null;
//        String query =
//                "SELECT " +
//                        "DEVICE_ID, " +
//                        "OPERATION_CODE, " +
//                        "DEVICE_TYPE " +
//                        "FROM DM_ENROLMENT_OP_MAPPING " +
//                "WHERE ENROLMENT_ID = ? " +
//                        "AND OPERATION_ID = ?";
//        try (Connection connection = OperationManagementDAOFactory.getConnection();
//             PreparedStatement stmt = connection.prepareStatement(query)) {
//            stmt.setInt(1, enrolmentId);
//            stmt.setInt(2, operationId);
//            try (ResultSet resultSet = stmt.executeQuery()) {
//                if (resultSet.next()) {
//                    deviceOperationDetails = new DeviceOperationDetails(
//                            resultSet.getInt("DEVICE_ID"),
//                            resultSet.getString("OPERATION_CODE"),
//                            resultSet.getString("DEVICE_TYPE")
//                    );
//                }
//            }
//        } catch (SQLException e) {
//            throw new OperationManagementDAOException("Error fetching operation details from MySQL.", e);
//        }
//        return deviceOperationDetails;
//    }
//
//    @Override
//    public List<DeviceOperationDetails> getUpdatedOperationsByDeviceTypeAndStatus(
//            String deviceType, String requiredStatus) throws OperationManagementDAOException {
//        List<DeviceOperationDetails> operationDetailsList = new ArrayList<>();
//        String query =
//                "SELECT " +
//                        "DEVICE_ID, " +
//                        "OPERATION_ID, " +
//                        "OPERATION_CODE " +
//                        "FROM DM_ENROLMENT_OP_MAPPING " +
//                        "WHERE DEVICE_TYPE = ? " +
//                        "AND STATUS = ?";
//        try (Connection connection = OperationManagementDAOFactory.getConnection();
//             PreparedStatement stmt = connection.prepareStatement(query)) {
//            stmt.setString(1, deviceType);
//            stmt.setString(2, requiredStatus);
//            try (ResultSet resultSet = stmt.executeQuery()) {
//                while (resultSet.next()) {
//                    int deviceId = resultSet.getInt("DEVICE_ID");
//                    String operationCode = resultSet.getString("OPERATION_CODE");
//                    operationDetailsList.add(new DeviceOperationDetails(deviceId, operationCode, deviceType));
//                }
//            }
//        } catch (SQLException e) {
//            throw new OperationManagementDAOException("Error fetching updated operation details for device type: "
//                    + deviceType + " with status: " + requiredStatus, e);
//        }
//        return operationDetailsList;
//    }
}
