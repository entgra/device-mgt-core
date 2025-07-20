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

import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementDAOException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.AbstractNotificationManagementDAOImpl;
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

public class GenericNotificationManagementDAOImpl extends AbstractNotificationManagementDAOImpl {
    private static final Log log = LogFactory.getLog(GenericNotificationManagementDAOImpl.class);

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
}
