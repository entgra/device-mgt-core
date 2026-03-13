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

package io.entgra.device.mgt.core.notification.mgt.core.dao;

import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.dto.PaginatedUserNotificationResponse;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationAction;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationPayload;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementDAOException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

public class AbstractNotificationManagementDAOImpl implements NotificationManagementDAO {
    private static final Log log = LogFactory.getLog(AbstractNotificationManagementDAOImpl.class);

    @Override
    public List<Notification> getLatestNotifications(int offset, int limit) throws NotificationManagementDAOException {
        List<Notification> notifications = new ArrayList<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String query =
                "SELECT * FROM DM_NOTIFICATION " +
                        "ORDER BY CREATED_TIMESTAMP " +
                        "WHERE TENANT_ID = ? " +
                        "DESC LIMIT ? OFFSET ?";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setInt(2, limit);
                preparedStatement.setInt(3, offset);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    Notification notification = new Notification();
                    while (resultSet.next()) {
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
            String msg = "Error occurred while retrieving notifications from DB";
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
                "SELECT NOTIFICATION_ID, " +
                        "DESCRIPTION, " +
                        "TYPE, " +
                        "CREATED_TIMESTAMP " +
                        "FROM DM_NOTIFICATION " +
                        "WHERE TENANT_ID = ? " +
                        "AND NOTIFICATION_ID IN (");
        for (int i = 0; i < notificationIds.size(); i++) {
            query.append("?");
            if (i < notificationIds.size() - 1) {
                query.append(",");
            }
        }
        query.append(") ORDER BY CREATED_TIMESTAMP DESC");
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                int paramIndex = 1;
                preparedStatement.setInt(paramIndex++, tenantId);
                for (Integer id : notificationIds) {
                    preparedStatement.setInt(paramIndex++, id);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    Notification notification = new Notification();
                    while (resultSet.next()) {
                        notification.setNotificationId(resultSet.getInt("NOTIFICATION_ID"));
                        notification.setDescription(resultSet.getString("DESCRIPTION"));
                        notification.setType(resultSet.getString("TYPE"));
                        notification.setCreatedTimestamp(resultSet.getTimestamp("CREATED_TIMESTAMP"));
                        notifications.add(notification);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving notifications by IDs from DB";
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
                "SELECT NOTIFICATION_ID, ACTION_ID, IS_READ " +
                        "FROM DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ? ");
        if (isRead != null) {
            queryBuilder.append("AND IS_READ = ? ");
        }
        queryBuilder.append("ORDER BY ACTION_TIMESTAMP DESC ");
        if (limit > 0) {
            queryBuilder.append("LIMIT ? ");
        }
        if (offset > 0) {
            queryBuilder.append("OFFSET ? ");
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
                    ps.setInt(paramIndex++, limit);
                }
                if (offset > 0) {
                    ps.setInt(paramIndex++, offset);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    UserNotificationAction action = new UserNotificationAction();
                    while (rs.next()) {
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
            throw new NotificationManagementDAOException(msg, e);
        }
        return userNotificationActions;
    }

    @Override
    public void updateNotificationAction(List<Integer> notificationIds, String username, boolean isRead)
            throws NotificationManagementDAOException {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
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
            throw new NotificationManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateAllNotificationAction(String username, boolean isRead)
            throws NotificationManagementDAOException {
        String query =
                "UPDATE DM_NOTIFICATION_USER_ACTION " +
                        "SET IS_READ = ? " +
                        "WHERE USERNAME = ?";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setBoolean(1, isRead);
                ps.setString(2, username);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating all notification actions for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<UserNotificationAction> getAllNotificationUserActions() throws NotificationManagementDAOException {
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
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    UserNotificationAction userNotificationAction = new UserNotificationAction();
                    while (resultSet.next()) {
                        userNotificationAction.setNotificationId(resultSet.getInt("NOTIFICATION_ID"));
                        userNotificationAction.setActionId(resultSet.getInt("ACTION_ID"));
                        userNotificationAction.setRead(resultSet.getBoolean("IS_READ"));
                        userNotificationAction.setUsername(resultSet.getString("USERNAME"));
                        userNotificationAction.setActionTimestamp(resultSet.getTimestamp("ACTION_TIMESTAMP"));
                        userNotificationActions.add(userNotificationAction);
                    }
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
            throw new NotificationManagementDAOException(msg, e);
        }
        return 0;
    }

    @Override
    public int getUnreadNotificationCountForUser(String username) throws NotificationManagementDAOException {
        int count = 0;
        String sql =
                "SELECT COUNT(*) " +
                        "AS UNREAD_COUNT " +
                        "FROM DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ? " +
                        "AND IS_READ = false";
        try {
            Connection conn = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    count = rs.getInt("UNREAD_COUNT");
                }
            }
        } catch (SQLException e) {
            String msg = "Error retrieving unread notification count for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        return count;
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
                        "VALUES (?, ?, ?, ?)";
        int notificationId = -1;
        try {
            Connection conn = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, notificationConfigId);
                stmt.setInt(2, tenantId);
                stmt.setString(3, description);
                stmt.setString(4, type);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    notificationId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            String msg = "Error inserting notification";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        return notificationId;
    }

    @Override
    public void insertNotificationUserActions(int notificationId, List<String> usernames)
            throws NotificationManagementDAOException {
        String sql =
                "INSERT INTO DM_NOTIFICATION_USER_ACTION " +
                        "(NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ) " +
                        "VALUES (?, ?, ?)";
        try {
            Connection conn = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (String username : usernames) {
                    stmt.setInt(1, notificationId);
                    stmt.setString(2, username);
                    stmt.setBoolean(3, false);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error inserting notification user actions";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
    }

    @Override
    public Map<String, List<Integer>> deleteUserNotifications(List<Integer> notificationIds, String username)
            throws NotificationManagementDAOException {
        if (notificationIds == null || notificationIds.isEmpty()) {
            Map<String, List<Integer>> result = new HashMap<>();
            result.put("deleted", Collections.emptyList());
            result.put("invalid", Collections.emptyList());
            return result;
        }
        Map<String, List<Integer>> result = new HashMap<>();
        List<Integer> deleted = new ArrayList<>();
        List<Integer> invalid = new ArrayList<>();
        String placeholders = notificationIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));
        String selectQuery =
                "SELECT NOTIFICATION_ID " +
                "FROM DM_NOTIFICATION_USER_ACTION " +
                "WHERE USERNAME = ? " +
                        "AND NOTIFICATION_ID " +
                        "IN (" + placeholders + ")";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
                stmt.setString(1, username);
                for (int i = 0; i < notificationIds.size(); i++) {
                    stmt.setInt(i + 2, notificationIds.get(i));
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    Set<Integer> validIds = new HashSet<>();
                    while (rs.next()) {
                        validIds.add(rs.getInt("NOTIFICATION_ID"));
                    }
                    for (Integer id : notificationIds) {
                        if (validIds.contains(id)) {
                            deleted.add(id);
                        } else {
                            invalid.add(id);
                        }
                    }
                }
            }
            if (!deleted.isEmpty()) {
                String validPlaceholders = deleted.stream()
                        .map(id -> "?")
                        .collect(Collectors.joining(", "));
                String finalDeleteQuery =
                        "DELETE " +
                                "FROM DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ? " +
                                "AND NOTIFICATION_ID " +
                                "IN (" + validPlaceholders + ")";
                try (PreparedStatement stmt = connection.prepareStatement(finalDeleteQuery)) {
                    stmt.setString(1, username);
                    for (int i = 0; i < deleted.size(); i++) {
                        stmt.setInt(i + 2, deleted.get(i));
                    }
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        result.put("deleted", deleted);
        result.put("invalid", invalid);
        return result;
    }

    @Override
    public void deleteAllUserNotifications(String username) throws NotificationManagementDAOException {
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
            String msg = "Error occurred while deleting all notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
    }

    @Override
    public PaginatedUserNotificationResponse getUserNotificationsWithStatus(
            String username, int limit, int offset, Boolean isRead) throws NotificationManagementDAOException {
        List<UserNotificationPayload> result = new ArrayList<>();
        int totalCount = 0;
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            StringBuilder countQuery = new StringBuilder(
                    "SELECT COUNT(*) " +
                            "FROM DM_NOTIFICATION_USER_ACTION ua " +
                            "JOIN DM_NOTIFICATION n " +
                            "ON ua.NOTIFICATION_ID = n.NOTIFICATION_ID " +
                            "WHERE ua.USERNAME = ? " +
                            "AND n.TENANT_ID = ? "
            );
            if (isRead != null) {
                countQuery.append("AND ua.IS_READ = ? ");
            }
            try (PreparedStatement ps = connection.prepareStatement(countQuery.toString())) {
                int paramIndex = 1;
                ps.setString(paramIndex++, username);
                ps.setInt(paramIndex++, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                if (isRead != null) {
                    ps.setBoolean(paramIndex++, isRead);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalCount = rs.getInt(1);
                    }
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
            if (isRead != null) {
                query.append("AND ua.IS_READ = ? ");
            }
            query.append("ORDER BY ua.ACTION_TIMESTAMP DESC ");
            if (limit > 0) query.append("LIMIT ? ");
            if (offset > 0) query.append("OFFSET ? ");
            try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
                int paramIndex = 1;
                ps.setString(paramIndex++, username);
                ps.setInt(paramIndex++, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                if (isRead != null) {
                    ps.setBoolean(paramIndex++, isRead);
                }
                if (limit > 0) {
                    ps.setInt(paramIndex++, limit);
                }
                if (offset > 0) {
                    ps.setInt(paramIndex++, offset);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        boolean readStatus = rs.getBoolean("IS_READ");
                        String actionType = readStatus ? "READ" : "UNREAD";

                        result.add(new UserNotificationPayload(
                                rs.getInt("NOTIFICATION_ID"),
                                rs.getString("DESCRIPTION"),
                                rs.getString("TYPE"),
                                actionType,
                                username,
                                rs.getTimestamp("CREATED_TIMESTAMP")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving user notifications with status";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        return new PaginatedUserNotificationResponse(result, totalCount);
    }
}
