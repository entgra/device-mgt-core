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

import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

public interface NotificationArchivalDAO {

    /**
     * Moves notifications older than the specified timestamp to the archive table.
     *
     * @param cutoffTimestamp The timestamp before which notifications should be archived.
     * @param tenantId relevant tenant id.
     * @return A list of NOTIFICATION_IDs that were moved.
     * @throws NotificationArchivalException If a database error occurs during the move.
     */
    List<Integer> moveNotificationsToArchive(Timestamp cutoffTimestamp, int tenantId)
            throws NotificationArchivalException;

    /**
     * Moves user actions associated with the given notification IDs to the archive table.
     *
     * @param notificationIds The list of notification IDs whose user actions need to be archived.
     * @throws NotificationArchivalException If a database error occurs during the move.
     */
    void moveUserActionsToArchive(List<Integer> notificationIds) throws NotificationArchivalException;

    /**
     * Deletes notifications older than the specified timestamp from the main table.
     *
     * @param cutoffTimestamp The timestamp before which notifications should be deleted.
     * @param tenantId relevant tenant id.
     * @return The number of deleted notifications.
     * @throws NotificationArchivalException If a database error occurs during the deletion.
     */
    int deleteOldNotifications(Timestamp cutoffTimestamp, int tenantId) throws NotificationArchivalException;

    /**
     * Moves notifications for a specific configuration ID older than the given cutoff to the archive table.
     *
     * @param cutoff   The cutoff timestamp.
     * @param tenantId The tenant ID.
     * @param configId The notification config ID.
     * @return List of moved notification IDs.
     * @throws NotificationArchivalException if a database error occurs.
     */
    List<Integer> moveNotificationsToArchiveByConfig(Timestamp cutoff, int tenantId, int configId)
            throws NotificationArchivalException;

    /**
     * Moves all notifications excluding the given configuration IDs older than the cutoff to the archive.
     *
     * @param cutoff            The cutoff timestamp.
     * @param tenantId          The tenant ID.
     * @param excludedConfigIds Set of config IDs to exclude from archival.
     * @return List of moved notification IDs.
     * @throws NotificationArchivalException if a database error occurs.
     */
    List<Integer> moveNotificationsToArchiveExcludingConfigs(Timestamp cutoff, int tenantId,
                                                             Set<Integer> excludedConfigIds)
            throws NotificationArchivalException;

    /**
     * Deletes notifications for a specific config ID older than the cutoff.
     *
     * @param cutoff   The cutoff timestamp.
     * @param tenantId The tenant ID.
     * @param configId The config ID.
     * @return Number of deleted notifications.
     * @throws NotificationArchivalException if a database error occurs.
     */
    int deleteOldNotificationsByConfig(Timestamp cutoff, int tenantId, int configId)
            throws NotificationArchivalException;

    /**
     * Deletes notifications excluding the given config IDs older than the cutoff.
     *
     * @param cutoff            The cutoff timestamp.
     * @param tenantId          The tenant ID.
     * @param excludedConfigIds Set of config IDs to exclude.
     * @return Number of deleted notifications.
     * @throws NotificationArchivalException if a database error occurs.
     */
    int deleteOldNotificationsExcludingConfigs(Timestamp cutoff, int tenantId, Set<Integer> excludedConfigIds)
            throws NotificationArchivalException;

    /**
     * Archive one or more notifications for a given user from the DB.
     *
     * @param notificationIds A list of notification IDs to be deleted.
     * @param username        The username associated with the notifications.
     * @throws NotificationArchivalException If an error occurs while deleting the notifications.
     */
    void archiveUserNotifications(List<Integer> notificationIds, String username)
            throws NotificationArchivalException;

    /**
     * Archives all notifications for the given user by moving them from the active
     * user notification table to the archived table.
     *
     * @param username the username whose notifications should be archived.
     * @throws NotificationArchivalException if an error occurs during the archival process.
     */
    void archiveAllUserNotifications(String username) throws NotificationArchivalException;

    /**
     * Deletes archived notifications and their associated user actions that are older than the specified
     * cutoff timestamp for the given tenant. This helps to clean up old data from the archive tables.
     *
     * @param cutoff   The timestamp before which data should be deleted.
     * @param tenantId The tenant ID for which archival data should be cleaned up.
     * @throws NotificationArchivalException If an error occurs during the deletion process.
     */
    void deleteExpiredArchivedNotifications(Timestamp cutoff, int tenantId) throws NotificationArchivalException;
}
