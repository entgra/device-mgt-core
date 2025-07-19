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
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationArchivalDAO;
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
import java.util.Set;
import java.util.stream.Collectors;

public class GenericNotificationArchivalDAOImpl implements NotificationArchivalDAO {
    private static final Log log = LogFactory.getLog(GenericNotificationArchivalDAOImpl.class);

    private static final String SOURCE_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getSourceDB();

    private static final String DESTINATION_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getDestinationDB();

    @Override
    public List<Integer> moveNotificationsToArchive(Timestamp cutoff, int tenantId)
            throws NotificationArchivalException {
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
            throw new NotificationArchivalException(msg, e);
        } finally {
            NotificationDAOUtil.cleanupResources(sourceConn);
            NotificationDAOUtil.cleanupResources(destConn);
        }
        return notificationIds;
    }

    @Override
    public void moveUserActionsToArchive(List<Integer> notificationIds) throws NotificationArchivalException {
        if (notificationIds == null || notificationIds.isEmpty()) return;
        String inClause = notificationIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String insertSQL =
                "INSERT INTO " + DESTINATION_DB + ".DM_NOTIFICATION_USER_ACTION_ARCH " +
                "(ACTION_ID, " +
                        "NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP) " +
                "SELECT " +
                        "ACTION_ID, " +
                        "NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP " +
                "FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION " +
                "WHERE NOTIFICATION_ID " +
                        "IN (" + inClause + ")";
        String deleteSQL =
                "DELETE FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION " +
                "WHERE NOTIFICATION_ID " +
                        "IN (" + inClause + ")";
        try {
            Connection sourceConn = NotificationArchivalSourceDAOFactory.getConnection();
            Connection destConn = NotificationArchivalDestDAOFactory.getConnection();
            try (PreparedStatement insertStmt = destConn.prepareStatement(insertSQL);
                 PreparedStatement deleteStmt = sourceConn.prepareStatement(deleteSQL)) {
                for (int i = 0; i < notificationIds.size(); i++) {
                    insertStmt.setInt(i + 1, notificationIds.get(i));
                    deleteStmt.setInt(i + 1, notificationIds.get(i));
                }
                insertStmt.executeUpdate();
                deleteStmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving user actions";
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        }
    }

    @Override
    public int deleteOldNotifications(Timestamp cutoff, int tenantId) throws NotificationArchivalException {
        String sql =
                "DELETE " +
                        "FROM DM_NOTIFICATION " +
                        "WHERE CREATED_TIMESTAMP < ? " +
                        "AND TENANT_ID = ?";
        try {
            Connection conn = NotificationArchivalSourceDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, cutoff);
                stmt.setInt(2, tenantId);
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Failed to delete old notifications for tenant " + tenantId;
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        }
    }

    @Override
    public List<Integer> moveNotificationsToArchiveByConfig(Timestamp cutoff, int tenantId, int configId)
            throws NotificationArchivalException {
        List<Integer> movedIds = new ArrayList<>();
        String insertSQL =
                "INSERT INTO " + DESTINATION_DB + ".DM_NOTIFICATION_ARCH " +
                "(NOTIFICATION_ID, " +
                        "NOTIFICATION_CONFIG_ID, " +
                        "TENANT_ID, " +
                        "DESCRIPTION, " +
                        "TYPE, " +
                        "CREATED_TIMESTAMP) " +
                "SELECT " +
                        "NOTIFICATION_ID, " +
                        "NOTIFICATION_CONFIG_ID, " +
                        "TENANT_ID, " +
                        "DESCRIPTION, " +
                        "TYPE, " +
                        "CREATED_TIMESTAMP " +
                "FROM " + SOURCE_DB + ".DM_NOTIFICATION " +
                "WHERE TENANT_ID = ? " +
                        "AND NOTIFICATION_CONFIG_ID = ? " +
                        "AND CREATED_TIMESTAMP < ?";
        String selectIdsSQL =
                "SELECT NOTIFICATION_ID " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION " +
                "WHERE TENANT_ID = ? " +
                        "AND NOTIFICATION_CONFIG_ID = ? " +
                        "AND CREATED_TIMESTAMP < ?";
        try {
            Connection sourceConn = NotificationArchivalSourceDAOFactory.getConnection();
            Connection destConn = NotificationArchivalDestDAOFactory.getConnection();
            try (PreparedStatement insertStmt = destConn.prepareStatement(insertSQL);
                 PreparedStatement selectStmt = sourceConn.prepareStatement(selectIdsSQL)) {
                insertStmt.setInt(1, tenantId);
                insertStmt.setInt(2, configId);
                insertStmt.setTimestamp(3, cutoff);
                insertStmt.executeUpdate();
                selectStmt.setInt(1, tenantId);
                selectStmt.setInt(2, configId);
                selectStmt.setTimestamp(3, cutoff);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        movedIds.add(rs.getInt("NOTIFICATION_ID"));
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error moving notifications to archive for configId: " + configId;
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        }
        return movedIds;
    }

    @Override
    public List<Integer> moveNotificationsToArchiveExcludingConfigs(Timestamp cutoff, int tenantId,
                                                                    Set<Integer> excludedConfigIds)
            throws NotificationArchivalException {
        List<Integer> movedIds = new ArrayList<>();
        if (excludedConfigIds == null || excludedConfigIds.isEmpty()) {
            return moveNotificationsToArchiveByConfig(cutoff, tenantId, -1);
        }
        String placeholders = excludedConfigIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String selectIdsSQL =
                "SELECT NOTIFICATION_ID " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION " +
                        "WHERE TENANT_ID = ? " +
                        "AND CREATED_TIMESTAMP < ? " +
                        "AND NOTIFICATION_CONFIG_ID " +
                        "NOT IN (" + placeholders + ")";
        String insertSQL =
                "INSERT INTO " + DESTINATION_DB + ".DM_NOTIFICATION_ARCH " +
                        "(NOTIFICATION_ID, " +
                        "NOTIFICATION_CONFIG_ID, " +
                        "TENANT_ID, " +
                        "DESCRIPTION, " +
                        "TYPE, " +
                        "CREATED_TIMESTAMP) " +
                        "SELECT " +
                        "NOTIFICATION_ID, " +
                        "NOTIFICATION_CONFIG_ID, " +
                        "TENANT_ID, " +
                        "DESCRIPTION, " +
                        "TYPE, " +
                        "CREATED_TIMESTAMP " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION " +
                        "WHERE NOTIFICATION_ID " +
                        "IN (%s)";
        String deleteSQL =
                "DELETE FROM " + SOURCE_DB + ".DM_NOTIFICATION " +
                        "WHERE NOTIFICATION_ID " +
                        "IN (%s)";
        try {
            Connection sourceConn = NotificationArchivalSourceDAOFactory.getConnection();
            Connection destConn = NotificationArchivalDestDAOFactory.getConnection();
            // select the notification IDs to archive
            try (PreparedStatement selectStmt = sourceConn.prepareStatement(selectIdsSQL)) {
                selectStmt.setInt(1, tenantId);
                selectStmt.setTimestamp(2, cutoff);
                int i = 3;
                for (Integer configId : excludedConfigIds) {
                    selectStmt.setInt(i++, configId);
                }
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        movedIds.add(rs.getInt("NOTIFICATION_ID"));
                    }
                }
            }
            if (movedIds.isEmpty()) {
                return movedIds;
            }
            String idPlaceholders =
                    movedIds.stream().map(id -> "?").collect(Collectors.joining(","));
            // insert into archive
            try (PreparedStatement insertStmt = destConn.prepareStatement(
                    String.format(insertSQL, idPlaceholders))) {
                int idx = 1;
                for (Integer id : movedIds) {
                    insertStmt.setInt(idx++, id);
                }
                insertStmt.executeUpdate();
            }
            // delete from source
            try (PreparedStatement deleteStmt = sourceConn.prepareStatement(
                    String.format(deleteSQL, idPlaceholders))) {
                int idx = 1;
                for (Integer id : movedIds) {
                    deleteStmt.setInt(idx++, id);
                }
                deleteStmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error moving + deleting notifications excluding configIds";
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        }
        return movedIds;
    }

    @Override
    public int deleteOldNotificationsByConfig(Timestamp cutoff, int tenantId, int configId)
            throws NotificationArchivalException {
        String deleteSQL =
                "DELETE FROM DM_NOTIFICATION " +
                        "WHERE TENANT_ID = ? " +
                        "AND NOTIFICATION_CONFIG_ID = ? " +
                        "AND CREATED_TIMESTAMP < ?";
        try {
            Connection conn = NotificationArchivalSourceDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
                stmt.setInt(1, tenantId);
                stmt.setInt(2, configId);
                stmt.setTimestamp(3, cutoff);
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error deleting notifications by config ID";
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        }
    }

    @Override
    public int deleteOldNotificationsExcludingConfigs(Timestamp cutoff, int tenantId, Set<Integer> excludedConfigIds)
            throws NotificationArchivalException {
        String deleteSQL;
        if (excludedConfigIds == null || excludedConfigIds.isEmpty()) {
            deleteSQL =
                    "DELETE FROM DM_NOTIFICATION " +
                            "WHERE TENANT_ID = ? " +
                            "AND CREATED_TIMESTAMP < ?";
        } else {
            String placeholders =
                    excludedConfigIds.stream().map(id -> "?").collect(Collectors.joining(","));
            deleteSQL =
                    "DELETE FROM DM_NOTIFICATION " +
                    "WHERE TENANT_ID = ? " +
                            "AND CREATED_TIMESTAMP < ? " +
                            "AND NOTIFICATION_CONFIG_ID " +
                            "NOT IN (" + placeholders + ")";
        }
        try {
            Connection conn = NotificationArchivalSourceDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
                stmt.setInt(1, tenantId);
                stmt.setTimestamp(2, cutoff);
                if (excludedConfigIds != null && !excludedConfigIds.isEmpty()) {
                    int i = 3;
                    for (Integer configId : excludedConfigIds) {
                        stmt.setInt(i++, configId);
                    }
                }
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error deleting notifications excluding config IDs";
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        }
    }

    @Override
    public void archiveUserNotifications(List<Integer> notificationIds, String username)
            throws NotificationArchivalException {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        String placeholders = notificationIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));
        String selectQuery =
                "SELECT NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ? " +
                        "AND NOTIFICATION_ID " +
                        "IN (" + placeholders + ")";
        String insertQuery =
                "INSERT INTO " + DESTINATION_DB + ".DM_NOTIFICATION_USER_ACTION_ARCH " +
                        "(NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP) " +
                        "VALUES (?, ?, ?, ?)";
        String deleteQuery =
                "DELETE FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ? " +
                        "AND NOTIFICATION_ID " +
                        "IN (" + placeholders + ")";
        try {
            Connection sourceConn = NotificationArchivalSourceDAOFactory.getConnection();
            Connection destConn = NotificationArchivalDestDAOFactory.getConnection();
            try (PreparedStatement selectStmt = sourceConn.prepareStatement(selectQuery);
                 PreparedStatement insertStmt = destConn.prepareStatement(insertQuery);
                 PreparedStatement deleteStmt = sourceConn.prepareStatement(deleteQuery)) {
                selectStmt.setString(1, username);
                for (int i = 0; i < notificationIds.size(); i++) {
                    selectStmt.setInt(i + 2, notificationIds.get(i));
                }
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        insertStmt.setInt(1, rs.getInt("NOTIFICATION_ID"));
                        insertStmt.setString(2, rs.getString("USERNAME"));
                        insertStmt.setBoolean(3, rs.getBoolean("IS_READ"));
                        insertStmt.setTimestamp(4, rs.getTimestamp("ACTION_TIMESTAMP"));
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
                deleteStmt.setString(1, username);
                for (int i = 0; i < notificationIds.size(); i++) {
                    deleteStmt.setInt(i + 2, notificationIds.get(i));
                }
                deleteStmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        }
    }

    @Override
    public void archiveAllUserNotifications(String username) throws NotificationArchivalException {
        String selectQuery =
                "SELECT " +
                        "NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ?";
        String insertQuery =
                "INSERT INTO " + DESTINATION_DB + ".DM_NOTIFICATION_USER_ACTION_ARCH " +
                        "(NOTIFICATION_ID, " +
                        "USERNAME, " +
                        "IS_READ, " +
                        "ACTION_TIMESTAMP) " +
                        "VALUES (?, ?, ?, ?)";
        String deleteQuery =
                "DELETE FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION " +
                        "WHERE USERNAME = ?";
        try {
            Connection sourceConn = NotificationArchivalSourceDAOFactory.getConnection();
            Connection destConn = NotificationArchivalDestDAOFactory.getConnection();
            try (PreparedStatement selectStmt = sourceConn.prepareStatement(selectQuery);
                 PreparedStatement insertStmt = destConn.prepareStatement(insertQuery);
                 PreparedStatement deleteStmt = sourceConn.prepareStatement(deleteQuery)) {
                selectStmt.setString(1, username);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        insertStmt.setInt(1, rs.getInt("NOTIFICATION_ID"));
                        insertStmt.setString(2, rs.getString("USERNAME"));
                        insertStmt.setBoolean(3, rs.getBoolean("IS_READ"));
                        insertStmt.setTimestamp(4, rs.getTimestamp("ACTION_TIMESTAMP"));
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
                deleteStmt.setString(1, username);
                deleteStmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving all notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        }
    }

    @Override
    public void deleteExpiredArchivedNotifications(Timestamp cutoff, int tenantId)
            throws NotificationArchivalException {
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
            throw new NotificationArchivalException(msg, e);
        }
    }
}
