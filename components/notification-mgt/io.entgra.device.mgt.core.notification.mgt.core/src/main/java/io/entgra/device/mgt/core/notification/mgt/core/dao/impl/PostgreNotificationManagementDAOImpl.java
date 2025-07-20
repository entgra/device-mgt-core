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

import io.entgra.device.mgt.core.notification.mgt.core.dao.AbstractNotificationManagementDAOImpl;
import io.entgra.device.mgt.core.notification.mgt.core.dao.util.NotificationDAOUtil;
import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementDAOException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgreNotificationManagementDAOImpl extends AbstractNotificationManagementDAOImpl {
    private static final Log log = LogFactory.getLog(PostgreNotificationManagementDAOImpl.class);

    @Override
    public List<Notification> getLatestNotifications(int offset, int limit) throws NotificationManagementDAOException {
        List<Notification> notifications = new ArrayList<>();
        String query =
                "SELECT * FROM DM_NOTIFICATION " +
                        "ORDER BY CREATED_TIMESTAMP " +
                        "DESC LIMIT ? OFFSET ?";
        try {
            Connection connection = NotificationManagementDAOFactory.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, limit);
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
            String msg = "Error occurred while retrieving notifications from PostgreSQL DB";
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
                        "VALUES (?, ?, ?, ?) " +
                        "RETURNING NOTIFICATION_ID";
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
    public void insertNotificationUserActions(int notificationId, List<String> usernames)
            throws NotificationManagementDAOException {
        String sql =
                "INSERT INTO DM_NOTIFICATION_USER_ACTION (" +
                        "NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ) " +
                        "VALUES (?, ?, ?)";
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = NotificationManagementDAOFactory.getConnection();
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
            throw new NotificationManagementDAOException(msg, e);
        } finally {
            NotificationDAOUtil.cleanupResources(stmt, null);
        }
    }
}
