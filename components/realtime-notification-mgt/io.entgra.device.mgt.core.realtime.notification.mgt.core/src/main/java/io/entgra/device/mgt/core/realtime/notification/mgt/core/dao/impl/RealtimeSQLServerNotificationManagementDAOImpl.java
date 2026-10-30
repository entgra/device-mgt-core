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

package io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.impl;

import io.entgra.device.mgt.core.realtime.notification.mgt.common.dto.RealtimePaginatedUserNotificationResponse;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.dto.RealtimeUserNotificationPayload;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.RealtimeAbstractNotificationManagementDAOImpl;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.dto.RealtimeNotification;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationManagementDAOException;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.RealtimeNotificationManagementDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RealtimeSQLServerNotificationManagementDAOImpl extends RealtimeAbstractNotificationManagementDAOImpl {
    private static final Log log = LogFactory.getLog(RealtimeSQLServerNotificationManagementDAOImpl.class);

    @Override
    public List<RealtimeNotification> getLatestNotifications(int offset, int limit) throws RealtimeNotificationManagementDAOException {
        List<RealtimeNotification> notifications = new ArrayList<>();
        String query =
                "SELECT * FROM DM_REALTIME_NOTIFICATION " +
                        "ORDER BY CREATED_TIMESTAMP DESC " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try {
            Connection connection = RealtimeNotificationManagementDAOFactory.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, offset);
                preparedStatement.setInt(2, limit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        RealtimeNotification notification = new RealtimeNotification();
                        notification.setNotificationId(resultSet.getInt("NOTIFICATION_ID"));
                        notification.setNotificationConfigId(resultSet.getInt("NOTIFICATION_CONFIG_ID"));
                        notification.setTenantId(resultSet.getInt("TENANT_ID"));
                        notification.setDescription(resultSet.getString("NOTIFICATION_CONTEXT"));
                        notification.setCreatedTimestamp(resultSet.getTimestamp("CREATED_TIMESTAMP"));
                        notifications.add(notification);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving notifications from SQL Server DB";
            log.error(msg, e);
            throw new RealtimeNotificationManagementDAOException(msg, e);
        }
        return notifications;
    }

    @Override
    public int insertNotification(int tenantId, int notificationConfigId, String type, String notificationContext)
            throws RealtimeNotificationManagementDAOException {
        String sql =
                "INSERT INTO DM_REALTIME_NOTIFICATION " +
                        "(NOTIFICATION_CONFIG_ID, " +
                        "TENANT_ID, " +
                        "NOTIFICATION_CONTEXT, " +
                        "TYPE) " +
                        "OUTPUT INSERTED.NOTIFICATION_ID " +
                        "VALUES (?, ?, ?, ?)";
        try (Connection conn = RealtimeNotificationManagementDAOFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationConfigId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, notificationContext);
            stmt.setString(4, type);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            String msg = "Error inserting notification";
            log.error(msg, e);
            throw new RealtimeNotificationManagementDAOException(msg, e);
        }
        return -1;
    }

}
