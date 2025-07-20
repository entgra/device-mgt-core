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
import java.util.Set;
import java.util.stream.Collectors;

public class SQLServerNotificationArchivalDAOImpl extends AbstractNotificationArchivalDAOImpl {
    private static final Log log = LogFactory.getLog(SQLServerNotificationArchivalDAOImpl.class);

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
                "SELECT " +
                        "NOTIFICATION_ID, " +
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
        Connection src = NotificationArchivalSourceDAOFactory.getConnection();
        Connection dst = NotificationArchivalDestDAOFactory.getConnection();
        try (PreparedStatement sel = src.prepareStatement(selectSQL);
             PreparedStatement ins = dst.prepareStatement(insertSQL)) {
            sel.setTimestamp(1, cutoff);
            sel.setInt(2, tenantId);
            try (ResultSet rs = sel.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("NOTIFICATION_ID");
                    ins.setInt(1, id);
                    ins.setInt(2, rs.getInt("NOTIFICATION_CONFIG_ID"));
                    ins.setInt(3, rs.getInt("TENANT_ID"));
                    ins.setString(4, rs.getString("DESCRIPTION"));
                    ins.setString(5, rs.getString("TYPE"));
                    ins.setTimestamp(6, rs.getTimestamp("CREATED_TIMESTAMP"));
                    ins.addBatch();
                    notificationIds.add(id);
                }
            }
            ins.executeBatch();
        } catch (SQLException e) {
            String msg = "Error occurred while archiving notifications from source DB to destination DB.";
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        } finally {
            NotificationDAOUtil.cleanupResources(src);
            NotificationDAOUtil.cleanupResources(dst);
        }
        return notificationIds;
    }

    @Override
    public void moveUserActionsToArchive(List<Integer> notificationIds) throws NotificationArchivalDAOException {
        if (notificationIds == null || notificationIds.isEmpty()) return;
        String placeholders =
                notificationIds.stream().map(id -> "?").collect(Collectors.joining(","));
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
                        "IN (" + placeholders + ")";
        String deleteSQL =
                "DELETE FROM " + SOURCE_DB + ".DM_NOTIFICATION_USER_ACTION " +
                        "WHERE NOTIFICATION_ID " +
                        "IN (" + placeholders + ")";
        Connection src = NotificationArchivalSourceDAOFactory.getConnection();
        Connection dst = NotificationArchivalDestDAOFactory.getConnection();
        try (PreparedStatement ins = dst.prepareStatement(insertSQL);
             PreparedStatement del = src.prepareStatement(deleteSQL)) {
            for (int i = 0; i < notificationIds.size(); i++) {
                ins.setInt(i + 1, notificationIds.get(i));
                del.setInt(i + 1, notificationIds.get(i));
            }
            ins.executeUpdate();
            del.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while archiving user actions";
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> moveNotificationsToArchiveByConfig(Timestamp cutoff, int tenantId, int configId)
            throws NotificationArchivalDAOException {
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
        String selectSQL =
                "SELECT " +
                        "NOTIFICATION_ID " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION " +
                        "WHERE TENANT_ID = ? " +
                        "AND NOTIFICATION_CONFIG_ID = ? " +
                        "AND CREATED_TIMESTAMP < ?";
        Connection src = NotificationArchivalSourceDAOFactory.getConnection();
        Connection dst = NotificationArchivalDestDAOFactory.getConnection();
        try (PreparedStatement ins = dst.prepareStatement(insertSQL);
             PreparedStatement sel = src.prepareStatement(selectSQL)) {
            ins.setInt(1, tenantId);
            ins.setInt(2, configId);
            ins.setTimestamp(3, cutoff);
            ins.executeUpdate();
            sel.setInt(1, tenantId);
            sel.setInt(2, configId);
            sel.setTimestamp(3, cutoff);
            try (ResultSet rs = sel.executeQuery()) {
                while (rs.next()) {
                    movedIds.add(rs.getInt("NOTIFICATION_ID"));
                }
            }
        } catch (SQLException e) {
            String msg = "Error moving notifications to archive for configId: " + configId;
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        } finally {
            NotificationDAOUtil.cleanupResources(src);
            NotificationDAOUtil.cleanupResources(dst);
        }
        return movedIds;
    }

    @Override
    public List<Integer> moveNotificationsToArchiveExcludingConfigs(
            Timestamp cutoff, int tenantId, Set<Integer> excludedConfigIds)
            throws NotificationArchivalDAOException {
        if (excludedConfigIds == null || excludedConfigIds.isEmpty()) {
            return moveNotificationsToArchiveByConfig(cutoff, tenantId, -1);
        }
        List<Integer> movedIds = new ArrayList<>();
        String placeholders =
                excludedConfigIds.stream().map(id -> "?").collect(Collectors.joining(","));
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
                        "AND CREATED_TIMESTAMP < ? " +
                        "AND NOTIFICATION_CONFIG_ID " +
                        "NOT IN (" + placeholders + ")";
        String selectSQL =
                "SELECT NOTIFICATION_ID " +
                        "FROM " + SOURCE_DB + ".DM_NOTIFICATION " +
                        "WHERE TENANT_ID = ? " +
                        "AND CREATED_TIMESTAMP < ? " +
                        "AND NOTIFICATION_CONFIG_ID " +
                        "NOT IN (" + placeholders + ")";
        Connection src = NotificationArchivalSourceDAOFactory.getConnection();
        Connection dst = NotificationArchivalDestDAOFactory.getConnection();
        try (PreparedStatement ins = dst.prepareStatement(insertSQL);
             PreparedStatement sel = src.prepareStatement(selectSQL)) {
            ins.setInt(1, tenantId);
            ins.setTimestamp(2, cutoff);
            sel.setInt(1, tenantId);
            sel.setTimestamp(2, cutoff);
            int idx = 3;
            for (Integer cfg : excludedConfigIds) {
                ins.setInt(idx, cfg);
                sel.setInt(idx, cfg);
                idx++;
            }
            ins.executeUpdate();
            try (ResultSet rs = sel.executeQuery()) {
                while (rs.next()) {
                    movedIds.add(rs.getInt("NOTIFICATION_ID"));
                }
            }
        } catch (SQLException e) {
            String msg = "Error moving notifications excluding configIds";
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        }
        return movedIds;
    }
}
