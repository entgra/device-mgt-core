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

import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.dto.PaginatedUserNotificationResponse;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationAction;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementDAOException;

import java.util.List;
import java.util.Map;

/**
 * DAO class for Notification management
 */
public interface NotificationManagementDAO {
    /**
     * Retrieve the latest notifications for the tenant.
     *
     * @return {@link List<Notification>}
     * @throws NotificationManagementDAOException Throws when error occurred while retrieving notifications.
     */
    List<Notification> getLatestNotifications(int offset, int limit) throws NotificationManagementDAOException;

    /**
     * Retrieves a paginated list of notifications from the database based on a given list of notification IDs.
     * The results are filtered by the current tenant ID and ordered by creation timestamp in descending order
     * (i.e., most recent first). Only selected fields — notification ID, description, and type — are returned.
     *
     * @param notificationIds List of notification IDs to filter the query. Must not be null or empty.
     * @return A list of {@link Notification} objects containing ID, description, and type for each matched record.
     * @throws NotificationManagementDAOException If any SQL or connection error occurs during query execution.
     */
    List<Notification> getNotificationsByIds(List<Integer> notificationIds)
            throws NotificationManagementDAOException;

    /**
     * Retrieves a paginated list of NotificationAction records for the specified user.
     *
     * @param username the user to filter actions for
     * @param offset pagination offset
     * @param limit pagination limit
     * @param isRead notification read status of the given user
     * @return list of NotificationAction entries for the user
     * @throws NotificationManagementDAOException if a DB error occurs
     */
    List<UserNotificationAction> getNotificationActionsByUser(String username, int limit, int offset, Boolean isRead)
            throws NotificationManagementDAOException;

    /**
     * Updates the action type (e.g., READ or UNREAD) for the specified notifications
     * for the given user.
     *
     * @param notificationIds List of notification IDs to update.
     * @param username        Username for whom the action is to be updated.
     * @param isRead      Action type to set (e.g., "READ", "UNREAD").
     * @throws NotificationManagementDAOException If an error occurs while updating the notifications.
     */
    void updateNotificationAction(List<Integer> notificationIds, String username, boolean isRead)
            throws NotificationManagementDAOException;

    /**
     * Retrieves all notification actions performed by all users.
     *
     * @return a list of {@link UserNotificationAction} objects representing actions taken by users
     *         on notifications (e.g., READ, DISMISSED).
     * @throws NotificationManagementDAOException if an error occurs while retrieving the data from the database.
     */
    List<UserNotificationAction> getAllNotificationUserActions() throws NotificationManagementDAOException;

    /**
     * Retrieves the total number of notification actions for a specific user, optionally filtered by action status.
     *
     * @param username the username whose notification action count is to be retrieved.
     * @param isRead   (optional) the action status to filter by (e.g., "READ", "UNREAD").
     *                 If null or empty, all actions are counted regardless of status.
     * @return the count of matching notification actions for the given user.
     * @throws NotificationManagementDAOException if an error occurs while querying the database.
     */
    int getNotificationActionsCountByUser(String username, Boolean isRead) throws NotificationManagementDAOException;


    /**
     * Inserts a new notification entry into the notification database.
     *
     * @param tenantId         The ID of the tenant for whom the notification is being created.
     * @param notificationConfigId The ID of the notification configuration to associate with the notification.
     * @param type             The type of the notification.
     * @param description      A description providing details of the notification.
     * @return The ID of the newly inserted notification.
     * @throws NotificationManagementDAOException If an error occurs while inserting the notification.
     */
    int insertNotification(int tenantId, int notificationConfigId, String type, String description)
            throws NotificationManagementDAOException;

    /**
     * Inserts user-specific actions related to a given notification.
     *
     * @param notificationId The ID of the notification for which user actions are being inserted.
     * @param usernames      A list of usernames to associate with the notification actions.
     * @throws NotificationManagementDAOException If an error occurs while inserting the user actions.
     */
    void insertNotificationUserActions(int notificationId, List<String> usernames) throws NotificationManagementDAOException;

    /**
     * Retrieves the count of unread notifications for a specific user.
     *
     * @param username The username for which to retrieve the count of unread notifications.
     * @return The number of unread notifications for the given user.
     * @throws NotificationManagementDAOException if a database access error occurs
     *         or the query execution fails.
     */
    int getUnreadNotificationCountForUser(String username) throws NotificationManagementDAOException;

    /**
     * Deletes the notifications for a given user in the database.
     *
     * @param notificationIds List of notification IDs to delete.
     * @param username        The username of the user whose notifications are to be deleted.
     * @return A map containing two entries:
     *         "deleted" - list of notification IDs that were successfully deleted,
     *         "invalid" - list of notification IDs that did not exist for the user.
     * @throws NotificationManagementDAOException if a database error occurs during deletion.
     */
    Map<String, List<Integer>> deleteUserNotifications(List<Integer> notificationIds, String username)
            throws NotificationManagementDAOException;

    /**
     * Deletes all notifications for the given user from the active user notification table.
     *
     * @param username the username whose notifications should be deleted.
     * @throws NotificationManagementDAOException if an error occurs during the deletion process.
     */
    void deleteAllUserNotifications(String username) throws NotificationManagementDAOException;

    /**
     * Retrieves a paginated list of user notifications along with their read/unread status.
     *
     * @param username the username of the user whose notifications should be retrieved
     * @param limit    the maximum number of notifications to return (for pagination)
     * @param offset   the number of records to skip before starting to return results (for pagination)
     * @param isRead   filter by read/unread status; if null, both read and unread notifications are included
     * @return a {@link PaginatedUserNotificationResponse} containing the list of notifications and the total count
     * @throws NotificationManagementDAOException if an error occurs while accessing the database
     */
    PaginatedUserNotificationResponse getUserNotificationsWithStatus(
            String username, int limit, int offset, Boolean isRead) throws NotificationManagementDAOException;
}
