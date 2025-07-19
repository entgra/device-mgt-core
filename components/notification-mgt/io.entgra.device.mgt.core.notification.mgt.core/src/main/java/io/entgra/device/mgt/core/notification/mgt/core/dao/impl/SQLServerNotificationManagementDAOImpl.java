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

import io.entgra.device.mgt.core.notification.mgt.core.dao.util.NotificationDAOUtil;
import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationAction;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationManagementDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SQLServerNotificationManagementDAOImpl implements NotificationManagementDAO {
    private static final Log log = LogFactory.getLog(SQLServerNotificationManagementDAOImpl.class);

    @Override
    public List<Notification> getLatestNotifications(int offset, int limit) throws NotificationManagementException {
        List<Notification> notifications = new ArrayList<>();
        String query =
                "SELECT * FROM DM_NOTIFICATION " +
                        "ORDER BY CREATED_TIMESTAMP DESC " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, offset);
                preparedStatement.setInt(2, limit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Notification notification = new Notification();
                        notification.setNotificationId(resultSet.getInt("NOTIFICATION_ID"));
                        notification.setNotificationConfigId(resultSet.getInt("NOTIFICATION_CONFIG_ID"));
                        notification.setTenantId(resultSet.getInt("TENANT_ID"));
                        notification.setDescription(resultSet.getString("DESCRIPTION"));
                        notification.setPriority(resultSet.getInt("PRIORITY"));
                        notification.setCreatedTimestamp(resultSet.getTimestamp("CREATED_TIMESTAMP"));
                        notifications.add(notification);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving notifications from SQL Server DB";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
        return notifications;
    }

    @Override
    public List<Notification> getNotificationsByIds(List<Integer> notificationIds)
            throws NotificationManagementException {
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
            String msg = "Error occurred while retrieving notifications by IDs from SQL Server DB";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
        return notifications;
    }

    @Override
    public List<UserNotificationAction> getNotificationActionsByUser(
            String username, int limit, int offset, Boolean isRead) throws NotificationManagementException {
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
        if (limit > 0) {
            queryBuilder.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        }
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(queryBuilder.toString())) {
                int paramIndex = 1;
                ps.setString(paramIndex++, username);
                if (isRead != null) {
                    ps.setBoolean(paramIndex++, isRead);
                }
                if (limit > 0) {
                    ps.setInt(paramIndex++, offset);
                    ps.setInt(paramIndex++, limit);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UserNotificationAction action = new UserNotificationAction();
                        action.setNotificationId(rs.getInt("NOTIFICATION_ID"));
                        action.setActionId(rs.getInt("ACTION_ID"));
                        action.setRead(rs.getBoolean("IS_READ"));
                        userNotificationActions.add(action);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving notification actions for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
        return userNotificationActions;
    }

    @Override
    public void updateNotificationAction(List<Integer> notificationIds, String username, String actionType)
            throws NotificationManagementException {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        boolean isRead;
        if ("READ".equalsIgnoreCase(actionType)) {
            isRead = true;
        } else if ("UNREAD".equalsIgnoreCase(actionType)) {
            isRead = false;
        } else {
            throw new NotificationManagementException("Invalid action type: " + actionType);
        }
        String placeholders = notificationIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));
        String query =
                "UPDATE DM_NOTIFICATION_USER_ACTION " +
                        "SET IS_READ = ? " +
                        "WHERE USERNAME = ? " +
                        "AND NOTIFICATION_ID " +
                        "IN (" + placeholders + ")";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setBoolean(1, isRead);
                ps.setString(2, username);
                for (int i = 0; i < notificationIds.size(); i++) {
                    ps.setInt(i + 3, notificationIds.get(i));
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating notification actions for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
    }

    @Override
    public List<UserNotificationAction> getAllNotificationUserActions() throws NotificationManagementException {
        List<UserNotificationAction> userNotificationActions = new ArrayList<>();
        String query =
                "SELECT NOTIFICATION_ID, " +
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
                    userNotificationAction.setRead(rs.getBoolean("IS_READ"));
                    userNotificationAction.setUsername(rs.getString("USERNAME"));
                    userNotificationAction.setActionTimestamp(rs.getTimestamp("ACTION_TIMESTAMP"));
                    userNotificationActions.add(userNotificationAction);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving all notification user actions.";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
        return userNotificationActions;
    }

    @Override
    public int getNotificationActionsCountByUser(String username, Boolean isRead)
            throws NotificationManagementException {
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
                    ps.setBoolean(paramIndex++, isRead);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error counting user notifications";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
        return 0;
    }

    @Override
    public int getUnreadNotificationCountForUser(String username) throws NotificationManagementException {
        int count = 0;
        String sql =
                "SELECT COUNT(*) " +
                        "AS UNREAD_COUNT " +
                        "FROM DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ? " +
                        "AND IS_READ = 0";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        count = rs.getInt("UNREAD_COUNT");
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error retrieving unread notification count for user: "
                    + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
        return count;
    }

    @Override
    public int insertNotification(int tenantId, int notificationConfigId, String type, String description)
            throws NotificationManagementException {
        String sql =
                "INSERT INTO DM_NOTIFICATION " +
                        "(NOTIFICATION_CONFIG_ID, " +
                        "TENANT_ID, " +
                        "DESCRIPTION, " +
                        "TYPE) " +
                        "OUTPUT INSERTED.NOTIFICATION_ID " +
                        "VALUES (?, ?, ?, ?)";
        try (Connection conn = NotificationManagementDAOFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationConfigId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, description);
            stmt.setString(4, type);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            String msg = "Error inserting notification";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
        return -1;
    }

    @Override
    public void insertNotificationUserActions(int notificationId, List<String> usernames)
            throws NotificationManagementException {
        String sql =
                "INSERT INTO DM_NOTIFICATION_USER_ACTION (" +
                        "NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ) " +
                        "VALUES (?, ?, ?)";
        PreparedStatement stmt = null;
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            for (String username : usernames) {
                stmt.setInt(1, notificationId);
                stmt.setString(2, username);
                stmt.setBoolean(3, false);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            String msg = "Error inserting notification user actions";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } finally {
            NotificationDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteUserNotifications(List<Integer> notificationIds, String username)
            throws NotificationManagementException {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        String placeholders = notificationIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));
        String query =
                "DELETE " +
                        "FROM DM_NOTIFICATION_USER_ACTION " +
                "WHERE USERNAME = ? " +
                        "AND NOTIFICATION_ID " +
                        "IN (" + placeholders + ")";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                for (int i = 0; i < notificationIds.size(); i++) {
                    stmt.setInt(i + 2, notificationIds.get(i));
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting notifications for user: " + username + " (SQL Server)";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
    }

    @Override
    public void deleteAllUserNotifications(String username) throws NotificationManagementException {
        String query =
                "DELETE " +
                        "FROM DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ?";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting all notifications for user (SQL Server): " + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
    }
}
