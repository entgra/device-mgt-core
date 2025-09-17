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

package io.entgra.device.mgt.core.notification.mgt.core.dao.impl.archive;

import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalDAOException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.AbstractNotificationArchivalDAOImpl;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalDestDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class H2NotificationArchivalDAOImpl extends AbstractNotificationArchivalDAOImpl {
    private static final Log log = LogFactory.getLog(H2NotificationArchivalDAOImpl.class);

    private static final String SOURCE_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getSourceDB();

    private static final String DESTINATION_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getDestinationDB();

    @Override
    public void deleteExpiredArchivedNotifications(Timestamp cutoff, int tenantId)
            throws NotificationArchivalDAOException {
        String deleteUserActionsSQL =
                "DELETE FROM " + DESTINATION_DB + ".DM_NOTIFICATION_USER_ACTION_ARCH " +
                        "WHERE ACTION_TIMESTAMP < ? " +
                        "AND NOTIFICATION_ID IN (" +
                        "SELECT NOTIFICATION_ID " +
                        "FROM " + DESTINATION_DB + ".DM_NOTIFICATION_ARCH " +
                        "WHERE CREATED_TIMESTAMP < ? " +
                        "AND TENANT_ID = ?)";
        String deleteNotificationsSQL =
                "DELETE FROM " + DESTINATION_DB + ".DM_NOTIFICATION_ARCH " +
                        "WHERE CREATED_TIMESTAMP < ? " +
                        "AND TENANT_ID = ?";
        try {
            Connection destConn = NotificationArchivalDestDAOFactory.getConnection();
            try (PreparedStatement deleteUserActionsStmt = destConn.prepareStatement(deleteUserActionsSQL);
                 PreparedStatement deleteNotificationsStmt = destConn.prepareStatement(deleteNotificationsSQL)) {
                deleteUserActionsStmt.setTimestamp(1, cutoff);
                deleteUserActionsStmt.setTimestamp(2, cutoff);
                deleteUserActionsStmt.setInt(3, tenantId);
                deleteUserActionsStmt.executeUpdate();
                deleteNotificationsStmt.setTimestamp(1, cutoff);
                deleteNotificationsStmt.setInt(2, tenantId);
                deleteNotificationsStmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting expired archived notifications (MySQL/H2)";
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        }
    }
}
