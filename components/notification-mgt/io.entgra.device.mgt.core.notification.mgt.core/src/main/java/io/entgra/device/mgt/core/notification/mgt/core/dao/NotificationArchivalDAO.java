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

import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalDAOException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NotificationArchivalDAO {

    /**
     * Moves notifications older than the specified timestamp to the archive table.
     *
     * @param cutoffTimestamp The timestamp before which notifications should be archived.
     * @param tenantId relevant tenant id.
     * @return A list of NOTIFICATION_IDs that were moved.
     * @throws NotificationArchivalDAOException If a database error occurs during the move.
     */
    List<Integer> moveNotificationsToArchive(Timestamp cutoffTimestamp, int tenantId)
            throws NotificationArchivalDAOException;

    /**
     * Moves user actions associated with the given notification IDs to the archive table.
     *
     * @param notificationIds The list of notification IDs whose user actions need to be archived.
     * @throws NotificationArchivalDAOException If a database error occurs during the move.
     */
    void moveUserActionsToArchive(List<Integer> notificationIds) throws NotificationArchivalDAOException;

    /**
     * Deletes notifications older than the specified timestamp from the main table.
     *
     * @param cutoffTimestamp The timestamp before which notifications should be deleted.
     * @param tenantId relevant tenant id.
     * @return The number of deleted notifications.
     * @throws NotificationArchivalDAOException If a database error occurs during the deletion.
     */
    int deleteOldNotifications(Timestamp cutoffTimestamp, int tenantId) throws NotificationArchivalDAOException;

    /**
     * Moves notifications for a specific configuration ID older than the given cutoff to the archive table.
     *
     * @param cutoff   The cutoff timestamp.
     * @param tenantId The tenant ID.
     * @param configId The notification config ID.
     * @return List of moved notification IDs.
     * @throws NotificationArchivalDAOException if a database error occurs.
     */
    List<Integer> moveNotificationsToArchiveByConfig(Timestamp cutoff, int tenantId, int configId)
            throws NotificationArchivalDAOException;

    /**
     * Moves all notifications excluding the given configuration IDs older than the cutoff to the archive.
     *
     * @param cutoff            The cutoff timestamp.
     * @param tenantId          The tenant ID.
     * @param excludedConfigIds Set of config IDs to exclude from archival.
     * @return List of moved notification IDs.
     * @throws NotificationArchivalDAOException if a database error occurs.
     */
    List<Integer> moveNotificationsToArchiveExcludingConfigs(Timestamp cutoff, int tenantId,
                                                             Set<Integer> excludedConfigIds)
            throws NotificationArchivalDAOException;

    /**
     * Deletes notifications for a specific config ID older than the cutoff.
     *
     * @param cutoff   The cutoff timestamp.
     * @param tenantId The tenant ID.
     * @param configId The config ID.
     * @return Number of deleted notifications.
     * @throws NotificationArchivalDAOException if a database error occurs.
     */
    int deleteOldNotificationsByConfig(Timestamp cutoff, int tenantId, int configId)
            throws NotificationArchivalDAOException;

    /**
     * Deletes notifications excluding the given config IDs older than the cutoff.
     *
     * @param cutoff            The cutoff timestamp.
     * @param tenantId          The tenant ID.
     * @param excludedConfigIds Set of config IDs to exclude.
     * @return Number of deleted notifications.
     * @throws NotificationArchivalDAOException if a database error occurs.
     */
    int deleteOldNotificationsExcludingConfigs(Timestamp cutoff, int tenantId, Set<Integer> excludedConfigIds)
            throws NotificationArchivalDAOException;

    /**
     * Archives notifications from source to archive tables for the given user.
     * Separates valid notifications from invalid ones.
     *
     * @param notificationIds List of notification IDs to archive.
     * @param username        Username of the user.
     * @return Map with keys:
     *         "archived" - notifications successfully archived,
     *         "invalid"  - notifications not found in source table.
     * @throws NotificationArchivalDAOException on DB error.
     */
    Map<String, List<Integer>> archiveUserNotifications(List<Integer> notificationIds, String username)
            throws NotificationArchivalDAOException;

    /**
     * Archives all notifications for the given user by moving them from the active
     * user notification table to the archived table.
     *
     * @param username the username whose notifications should be archived.
     * @throws NotificationArchivalDAOException if an error occurs during the archival process.
     */
    void archiveAllUserNotifications(String username) throws NotificationArchivalDAOException;

    /**
     * Deletes archived notifications and their associated user actions that are older than the specified
     * cutoff timestamp for the given tenant. This helps to clean up old data from the archive tables.
     *
     * @param cutoff   The timestamp before which data should be deleted.
     * @param tenantId The tenant ID for which archival data should be cleaned up.
     * @throws NotificationArchivalDAOException If an error occurs during the deletion process.
     */
    void deleteExpiredArchivedNotifications(Timestamp cutoff, int tenantId) throws NotificationArchivalDAOException;
}
