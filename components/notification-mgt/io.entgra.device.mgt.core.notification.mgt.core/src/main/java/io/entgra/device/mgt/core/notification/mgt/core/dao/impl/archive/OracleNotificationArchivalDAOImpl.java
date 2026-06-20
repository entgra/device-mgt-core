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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

public class OracleNotificationArchivalDAOImpl extends AbstractNotificationArchivalDAOImpl {
    private static final Log log = LogFactory.getLog(OracleNotificationArchivalDAOImpl.class);

    private static final String SOURCE_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getSourceDB();

    private static final String DESTINATION_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getDestinationDB();

    @Override
    public int deleteOldNotifications(Timestamp cutoff, int tenantId)
            throws NotificationArchivalDAOException {
        String sql =
                "DELETE FROM " + SOURCE_DB + ".DM_NOTIFICATION "
                + "WHERE CREATED_TIMESTAMP < ? " +
                        "AND TENANT_ID = ?";
        Connection src = NotificationArchivalSourceDAOFactory.getConnection();
        try (PreparedStatement stmt = src.prepareStatement(sql)) {
            stmt.setTimestamp(1, cutoff);
            stmt.setInt(2, tenantId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            String msg = "Failed to delete old notifications for tenant " + tenantId;
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        }
    }

    @Override
    public Map<String, List<Integer>> archiveUserNotifications(List<Integer> notificationIds, String username)
            throws NotificationArchivalDAOException {
        List<Integer> archived = new ArrayList<>();
        List<Integer> invalid = new ArrayList<>();
        if (notificationIds == null || notificationIds.isEmpty()) {
            return Map.of("archived", Collections.emptyList(), "invalid", Collections.emptyList());
        }
        String placeholders =
                notificationIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String selectQuery =
                "SELECT " +
                        "NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ? " +
                        "AND NOTIFICATION_ID " +
                        "IN (" + placeholders + ")";
        String insertQuery =
                "INSERT " +
                        "INTO " + DESTINATION_DB + ".DM_NOTIFICATION_USER_ACTION_ARCH " +
                        "(NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP) " +
                        "VALUES (?, ?, ?, ?)";
        String deleteQuery =
                "DELETE " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ? " +
                        "AND NOTIFICATION_ID " +
                        "IN (" + placeholders + ")";
        try {
            Connection sourceConn = NotificationArchivalSourceDAOFactory.getConnection();
            Connection destConn = NotificationArchivalDestDAOFactory.getConnection();
            try (
                    PreparedStatement selectStmt = sourceConn.prepareStatement(selectQuery);
                    PreparedStatement insertStmt = destConn.prepareStatement(insertQuery);
                    PreparedStatement deleteStmt = sourceConn.prepareStatement(deleteQuery)
            ) {
                // set username and IDs for select
                selectStmt.setString(1, username);
                for (int i = 0; i < notificationIds.size(); i++) {
                    selectStmt.setInt(i + 2, notificationIds.get(i));
                }
                // fetch valid notifications and batch insert
                Set<Integer> validIds = new HashSet<>();
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("NOTIFICATION_ID");
                        validIds.add(id);
                        insertStmt.setInt(1, id);
                        insertStmt.setString(2, rs.getString("USERNAME"));
                        insertStmt.setBoolean(3, rs.getBoolean("IS_READ"));
                        insertStmt.setTimestamp(4, rs.getTimestamp("ACTION_TIMESTAMP"));
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
                // determine invalid IDs
                for (Integer id : notificationIds) {
                    if (!validIds.contains(id)) {
                        invalid.add(id);
                    } else {
                        archived.add(id);
                    }
                }
                // delete valid notifications
                if (!archived.isEmpty()) {
                    deleteStmt.setString(1, username);
                    for (int i = 0; i < archived.size(); i++) {
                        deleteStmt.setInt(i + 2, archived.get(i));
                    }
                    deleteStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        }
        Map<String, List<Integer>> result = new HashMap<>();
        result.put("archived", archived);
        result.put("invalid", invalid);
        return result;
    }

    @Override
    public void archiveAllUserNotifications(String username)
            throws NotificationArchivalDAOException {
        String selectSQL =
                "SELECT " +
                        "NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP "
                + "FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION "
                + "WHERE USERNAME = ?";
        String insertSQL =
                "INSERT INTO " + DESTINATION_DB + ".DM_NOTIFICATION_USER_ACTION_ARCH "
                + "(NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP) "
                + "VALUES (?, ?, ?, ?)";
        String deleteSQL =
                "DELETE FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION "
                + "WHERE USERNAME = ?";
        Connection src = NotificationArchivalSourceDAOFactory.getConnection();
        Connection dst = NotificationArchivalDestDAOFactory.getConnection();
        try (PreparedStatement sel = src.prepareStatement(selectSQL);
             PreparedStatement ins = dst.prepareStatement(insertSQL);
             PreparedStatement del = src.prepareStatement(deleteSQL)) {
            sel.setString(1, username);
            try (ResultSet rs = sel.executeQuery()) {
                while (rs.next()) {
                    ins.setInt(1, rs.getInt("NOTIFICATION_ID"));
                    ins.setString(2, rs.getString("USERNAME"));
                    ins.setBoolean(3, rs.getBoolean("IS_READ"));
                    ins.setTimestamp(4, rs.getTimestamp("ACTION_TIMESTAMP"));
                    ins.addBatch();
                }
            }
            ins.executeBatch();
            del.setString(1, username);
            del.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while archiving all notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        }
    }

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
            String msg = "Error occurred while deleting expired archived notifications (Oracle)";
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        }
    }
}
