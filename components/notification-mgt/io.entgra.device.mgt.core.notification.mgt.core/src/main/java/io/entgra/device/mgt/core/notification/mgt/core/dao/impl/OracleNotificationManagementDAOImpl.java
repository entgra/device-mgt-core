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

package io.entgra.device.mgt.core.notification.mgt.core.dao.impl;

import io.entgra.device.mgt.core.notification.mgt.common.dto.PaginatedUserNotificationResponse;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationPayload;
import io.entgra.device.mgt.core.notification.mgt.core.dao.AbstractNotificationManagementDAOImpl;
import io.entgra.device.mgt.core.notification.mgt.core.dao.util.NotificationDAOUtil;
import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationAction;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementDAOException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.List;

public class OracleNotificationManagementDAOImpl extends AbstractNotificationManagementDAOImpl {
    private static final Log log = LogFactory.getLog(OracleNotificationManagementDAOImpl.class);

    @Override
    public List<Notification> getLatestNotifications(int offset, int limit) throws NotificationManagementDAOException {
        List<Notification> notifications = new ArrayList<>();
        String query =
                "SELECT * FROM ( " +
                        "SELECT t.*, ROWNUM rnum FROM ( " +
                        "SELECT * FROM DM_NOTIFICATION ORDER BY CREATED_TIMESTAMP DESC " +
                        ") t WHERE ROWNUM <= ? " +
                        ") WHERE rnum > ?";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, offset + limit);
                preparedStatement.setInt(2, offset);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Notification notification = new Notification();
                        notification.setNotificationId(resultSet.getInt("NOTIFICATION_ID"));
                        notification.setNotificationConfigId(resultSet.getInt("NOTIFICATION_CONFIG_ID"));
                        notification.setTenantId(resultSet.getInt("TENANT_ID"));
                        notification.setDescription(resultSet.getString("DESCRIPTION"));
                        notification.setCreatedTimestamp(resultSet.getTimestamp("CREATED_TIMESTAMP"));
                        notifications.add(notification);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving notifications from Oracle DB";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        return notifications;
    }

    @Override
    public List<Notification> getNotificationsByIds(List<Integer> notificationIds)
            throws NotificationManagementDAOException {
        List<Notification> notifications = new ArrayList<>();
        if (notificationIds == null || notificationIds.isEmpty()) {
            return notifications;
        }
        StringBuilder query = new StringBuilder(
                "SELECT NOTIFICATION_ID, DESCRIPTION, TYPE, CREATED_TIMESTAMP " +
                        "FROM DM_NOTIFICATION " +
                        "WHERE TENANT_ID = ? AND NOTIFICATION_ID IN (");
        for (int i = 0; i < notificationIds.size(); i++) {
            query.append("?");
            if (i < notificationIds.size() - 1) {
                query.append(",");
            }
        }
        query.append(") ORDER BY CREATED_TIMESTAMP DESC");
        try (Connection connection = NotificationManagementDAOFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            int paramIndex = 1;
            preparedStatement.setInt(paramIndex++, tenantId);
            for (Integer id : notificationIds) {
                preparedStatement.setInt(paramIndex++, id);
            }
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Notification notification = new Notification();
                    notification.setNotificationId(rs.getInt("NOTIFICATION_ID"));
                    notification.setDescription(rs.getString("DESCRIPTION"));
                    notification.setType(rs.getString("TYPE"));
                    notification.setCreatedTimestamp(rs.getTimestamp("CREATED_TIMESTAMP"));
                    notifications.add(notification);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving notifications by IDs from Oracle DB";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        return notifications;
    }

    @Override
    public List<UserNotificationAction> getNotificationActionsByUser(
            String username, int limit, int offset, Boolean isRead) throws NotificationManagementDAOException {
        List<UserNotificationAction> userNotificationActions = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT " +
                        "NOTIFICATION_ID, " +
                        "ACTION_ID, " +
                        "IS_READ " +
                        "FROM DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ? ");
        if (isRead != null) {
            queryBuilder.append("AND IS_READ = ? ");
        }
        queryBuilder.append("ORDER BY ACTION_TIMESTAMP DESC ");
        boolean paginate = (limit > 0 && offset >= 0);
        String finalQuery;
        if (paginate) {
            finalQuery = "SELECT * FROM ( " +
                    " SELECT a.*, ROWNUM rnum FROM ( " +
                    queryBuilder.toString() +
                    ") a WHERE ROWNUM <= ? ) WHERE rnum > ?";
        } else {
            finalQuery = queryBuilder.toString();
        }
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(finalQuery)) {
                int paramIndex = 1;
                ps.setString(paramIndex++, username);
                if (isRead != null) {
                    ps.setBoolean(paramIndex++, isRead);
                }
                if (paginate) {
                    ps.setInt(paramIndex++, offset + limit);
                    ps.setInt(paramIndex++, offset);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UserNotificationAction action = new UserNotificationAction();
                        action.setNotificationId(rs.getInt("NOTIFICATION_ID"));
                        action.setActionId(rs.getInt("ACTION_ID"));
                        action.setRead(rs.getInt("IS_READ") == 1);
                        userNotificationActions.add(action);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error fetching user actions from Oracle DB for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        return userNotificationActions;
    }

    @Override
    public List<UserNotificationAction> getAllNotificationUserActions() throws NotificationManagementDAOException {
        List<UserNotificationAction> userNotificationActions = new ArrayList<>();
        String query =
                "SELECT " +
                        "NOTIFICATION_ID, " +
                        "ACTION_ID, " +
                        "IS_READ, " +
                        "USERNAME, " +
                        "ACTION_TIMESTAMP " +
                        "FROM DM_NOTIFICATION_USER_ACTION " +
                        "ORDER BY ACTION_TIMESTAMP " +
                        "DESC";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserNotificationAction userNotificationAction = new UserNotificationAction();
                    userNotificationAction.setNotificationId(rs.getInt("NOTIFICATION_ID"));
                    userNotificationAction.setActionId(rs.getInt("ACTION_ID"));
                    userNotificationAction.setRead(rs.getInt("IS_READ") == 1);
                    userNotificationAction.setUsername(rs.getString("USERNAME"));
                    userNotificationAction.setActionTimestamp(rs.getTimestamp("ACTION_TIMESTAMP"));
                    userNotificationActions.add(userNotificationAction);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving all notification user actions.";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        return userNotificationActions;
    }

    @Override
    public int getNotificationActionsCountByUser(String username, Boolean isRead)
            throws NotificationManagementDAOException {
        StringBuilder query = new StringBuilder(
                "SELECT COUNT(*) " +
                        "FROM DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ?");
        if (isRead != null) {
            query.append(" AND IS_READ = ?");
        }
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
                int paramIndex = 1;
                ps.setString(paramIndex++, username);
                if (isRead != null) {
                    ps.setInt(paramIndex++, isRead ? 1 : 0);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Oracle DB error counting user notifications.";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        return 0;
    }

    @Override
    public int insertNotification(int tenantId, int notificationConfigId, String type, String description)
            throws NotificationManagementDAOException {
        String sql =
                "INSERT INTO DM_NOTIFICATION " +
                        "(NOTIFICATION_CONFIG_ID, " +
                        "TENANT_ID, " +
                        "DESCRIPTION, " +
                        "TYPE) " +
                        "VALUES (?, ?, ?, ?) " +
                        "RETURNING NOTIFICATION_ID INTO ?";
        try (Connection conn = NotificationManagementDAOFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, notificationConfigId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, description);
            stmt.setString(4, type);
            stmt.registerOutParameter(6, java.sql.Types.INTEGER);
            stmt.executeUpdate();
            return stmt.getInt(6);
        } catch (SQLException e) {
            String msg = "Error inserting notification";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
    }

    @Override
    public void insertNotificationUserActions(int notificationId, List<String> usernames)
            throws NotificationManagementDAOException {
        String sql =
                "INSERT INTO DM_NOTIFICATION_USER_ACTION (" +
                        "NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ) VALUES (?, ?, ?)";
        PreparedStatement stmt = null;
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            for (String username : usernames) {
                stmt.setInt(1, notificationId);
                stmt.setString(2, username);
                stmt.setInt(3, 0);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            String msg = "Error inserting notification user actions";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        } finally {
            NotificationDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public PaginatedUserNotificationResponse getUserNotificationsWithStatus(
            String username, int limit, int offset, Boolean isRead) throws NotificationManagementDAOException {
        List<UserNotificationPayload> result = new ArrayList<>();
        int totalCount = 0;
        try (Connection connection = NotificationManagementDAOFactory.getConnection()) {
            StringBuilder countQuery = new StringBuilder(
                    "SELECT COUNT(*) " +
                            "FROM DM_NOTIFICATION_USER_ACTION ua " +
                            "JOIN DM_NOTIFICATION n " +
                            "ON ua.NOTIFICATION_ID = n.NOTIFICATION_ID " +
                            "WHERE ua.USERNAME = ? " +
                            "AND n.TENANT_ID = ? "
            );
            if (isRead != null) countQuery.append("AND ua.IS_READ = ? ");
            try (PreparedStatement ps = connection.prepareStatement(countQuery.toString())) {
                int idx = 1;
                ps.setString(idx++, username);
                ps.setInt(idx++, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                if (isRead != null) ps.setBoolean(idx++, isRead);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalCount = rs.getInt(1);
                }
            }
            StringBuilder query = new StringBuilder(
                    "SELECT " +
                            "ua.NOTIFICATION_ID, " +
                            "ua.IS_READ, " +
                            "ua.ACTION_TIMESTAMP, " +
                            "n.DESCRIPTION, " +
                            "n.TYPE, " +
                            "n.CREATED_TIMESTAMP " +
                            "FROM DM_NOTIFICATION_USER_ACTION ua " +
                            "JOIN DM_NOTIFICATION n " +
                            "ON ua.NOTIFICATION_ID = n.NOTIFICATION_ID " +
                            "WHERE ua.USERNAME = ? " +
                            "AND n.TENANT_ID = ? "
            );
            if (isRead != null) query.append("AND ua.IS_READ = ? ");
            query.append("ORDER BY ua.ACTION_TIMESTAMP DESC ");
            if (offset > 0) query.append("OFFSET ? ROWS ");
            if (limit > 0) query.append("FETCH NEXT ? ROWS ONLY ");
            try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
                int idx = 1;
                ps.setString(idx++, username);
                ps.setInt(idx++, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                if (isRead != null) ps.setBoolean(idx++, isRead);
                if (offset > 0) ps.setInt(idx++, offset);
                if (limit > 0) ps.setInt(idx++, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        boolean readStatus = rs.getBoolean("IS_READ");
                        result.add(new UserNotificationPayload(
                                rs.getInt("NOTIFICATION_ID"),
                                rs.getString("DESCRIPTION"),
                                rs.getString("TYPE"),
                                readStatus ? "READ" : "UNREAD",
                                username,
                                rs.getTimestamp("CREATED_TIMESTAMP")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            throw new NotificationManagementDAOException("Error in Oracle getUserNotificationsWithStatus", e);
        }
        return new PaginatedUserNotificationResponse(result, totalCount);
    }
}
