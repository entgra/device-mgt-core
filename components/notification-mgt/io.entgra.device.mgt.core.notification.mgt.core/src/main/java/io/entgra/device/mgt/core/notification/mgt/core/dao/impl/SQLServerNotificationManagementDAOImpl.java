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
import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementDAOException;
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

public class SQLServerNotificationManagementDAOImpl extends AbstractNotificationManagementDAOImpl {
    private static final Log log = LogFactory.getLog(SQLServerNotificationManagementDAOImpl.class);

    @Override
    public List<Notification> getLatestNotifications(int offset, int limit) throws NotificationManagementDAOException {
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
                        notification.setCreatedTimestamp(resultSet.getTimestamp("CREATED_TIMESTAMP"));
                        notifications.add(notification);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving notifications from SQL Server DB";
            log.error(msg, e);
            throw new NotificationManagementDAOException(msg, e);
        }
        return notifications;
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
            throw new NotificationManagementDAOException(msg, e);
        }
        return -1;
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
            query.append("OFFSET ? ROWS ");
            if (limit > 0) query.append("FETCH NEXT ? ROWS ONLY ");
            try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
                int idx = 1;
                ps.setString(idx++, username);
                ps.setInt(idx++, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                if (isRead != null) ps.setBoolean(idx++, isRead);
                ps.setInt(idx++, offset > 0 ? offset : 0);
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
            throw new NotificationManagementDAOException("Error in SQLServer getUserNotificationsWithStatus", e);
        }
        return new PaginatedUserNotificationResponse(result, totalCount);
    }
}
