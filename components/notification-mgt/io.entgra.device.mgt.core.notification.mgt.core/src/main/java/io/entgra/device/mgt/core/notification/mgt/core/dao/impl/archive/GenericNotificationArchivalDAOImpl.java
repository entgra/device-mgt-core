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
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.util.NotificationDAOUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GenericNotificationArchivalDAOImpl extends AbstractNotificationArchivalDAOImpl {
    private static final Log log = LogFactory.getLog(GenericNotificationArchivalDAOImpl.class);

    private static final String SOURCE_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getSourceDB();

    private static final String DESTINATION_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getDestinationDB();

    @Override
    public List<Integer> moveNotificationsToArchive(Timestamp cutoff, int tenantId)
            throws NotificationArchivalDAOException {
        List<Integer> notificationIds = new ArrayList<>();
        String selectSQL =
                "SELECT NOTIFICATION_ID, " +
                        "NOTIFICATION_CONFIG_ID, " +
                        "TENANT_ID, " +
                        "DESCRIPTION, " +
                        "TYPE, " +
                        "CREATED_TIMESTAMP " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION " +
                        "WHERE CREATED_TIMESTAMP < ? " +
                        "AND TENANT_ID = ?";
        String insertSQL =
                "INSERT INTO " + DESTINATION_DB + ".DM_NOTIFICATION_ARCH " +
                        "(NOTIFICATION_ID, " +
                        "NOTIFICATION_CONFIG_ID, " +
                        "TENANT_ID, " +
                        "DESCRIPTION, " +
                        "TYPE, " +
                        "CREATED_TIMESTAMP) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
        Connection sourceConn = null;
        Connection destConn = null;
        try {
            sourceConn = NotificationArchivalSourceDAOFactory.getConnection();
            destConn = NotificationArchivalDestDAOFactory.getConnection();
            try (PreparedStatement selectStmt = sourceConn.prepareStatement(selectSQL);
                 PreparedStatement insertStmt = destConn.prepareStatement(insertSQL)) {
                selectStmt.setTimestamp(1, cutoff);
                selectStmt.setInt(2, tenantId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("NOTIFICATION_ID");
                        insertStmt.setInt(1, id);
                        insertStmt.setInt(2, rs.getInt("NOTIFICATION_CONFIG_ID"));
                        insertStmt.setInt(3, rs.getInt("TENANT_ID"));
                        insertStmt.setString(4, rs.getString("DESCRIPTION"));
                        insertStmt.setString(5, rs.getString("TYPE"));
                        insertStmt.setTimestamp(6, rs.getTimestamp("CREATED_TIMESTAMP"));
                        insertStmt.addBatch();
                        notificationIds.add(id);
                    }
                    insertStmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving notifications from source DB to destination DB.";
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        } finally {
            NotificationDAOUtil.cleanupResources(sourceConn);
            NotificationDAOUtil.cleanupResources(destConn);
        }
        return notificationIds;
    }
}
