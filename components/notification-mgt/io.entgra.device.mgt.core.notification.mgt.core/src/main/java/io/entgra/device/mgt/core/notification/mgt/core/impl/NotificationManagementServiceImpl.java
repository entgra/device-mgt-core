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

package io.entgra.device.mgt.core.notification.mgt.core.impl;

import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigBatchNotifications;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigCriticalCriteria;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationSettings;
import io.entgra.device.mgt.core.notification.mgt.common.dto.PaginatedUserNotificationResponse;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementDAOException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.TransactionManagementException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationArchivalDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementDataHolder;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationEventBroker;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationHelper;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationManagementService;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationManagementDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NotificationManagementServiceImpl implements NotificationManagementService {
    private static final Log log = LogFactory.getLog(NotificationManagementServiceImpl.class);
    private final NotificationManagementDAO notificationDAO;
    private final NotificationArchivalDAO notificationArchiveDAO;

    public NotificationManagementServiceImpl() {
        notificationDAO = NotificationManagementDAOFactory.getNotificationManagementDAO();
        notificationArchiveDAO = NotificationArchivalDestDAOFactory.getNotificationArchivalDAO();
    }

    @Override
    public List<Notification> getAllNotifications(int offset, int limit) throws NotificationManagementException {
        try {
            NotificationManagementDAOFactory.openConnection();
            return notificationDAO.getLatestNotifications(offset, limit);
        } catch (NotificationManagementDAOException e) {
            String msg = "Error occurred while retrieving all notifications";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public PaginatedUserNotificationResponse getUserNotificationsWithStatus(
            String username, int limit, int offset, Boolean isRead) throws NotificationManagementException {
        username = NotificationHelper.validateUserExists(username);
        try {
            NotificationManagementDAOFactory.openConnection();
            return notificationDAO.getUserNotificationsWithStatus(username, limit, offset, isRead);
        } catch (NotificationManagementDAOException e) {
            String msg = "Error occurred while retrieving user notifications";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving user notifications with status";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void updateNotificationActionForUser(List<Integer> notificationIds, String username, boolean isRead)
            throws NotificationManagementException {
        username = NotificationHelper.validateUserExists(username);
        try {
            NotificationManagementDAOFactory.beginTransaction();
            notificationDAO.updateNotificationAction(notificationIds, username, isRead);
            NotificationManagementDAOFactory.commitTransaction();
            int unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
            String payload = String.format("{\"unreadCount\":%d}", unreadCount);
            NotificationEventBroker.pushMessage(payload, Collections.singletonList(username));
        } catch (NotificationManagementDAOException e) {
            String msg = "Error occurred while updating notification actions";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            NotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating notification actions for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getUserNotificationCount(String username, Boolean isRead) throws NotificationManagementException {
        try {
            username = NotificationHelper.validateUserExists(username);
            NotificationManagementDAOFactory.openConnection();
            return notificationDAO.getNotificationActionsCountByUser(username, isRead);
        } catch (SQLException e) {
            String msg = "Error occurred while counting user notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } catch (NotificationManagementDAOException e) {
            String msg = "Error occurred while retrieving user notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Map<String, List<Integer>> deleteUserNotifications(List<Integer> notificationIds, String username)
            throws NotificationManagementException {
        username = NotificationHelper.validateUserExists(username);
        try {
            NotificationManagementDAOFactory.beginTransaction();
            Map<String, List<Integer>> result = notificationDAO.deleteUserNotifications(notificationIds, username);
            NotificationManagementDAOFactory.commitTransaction();
            int unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
            String payload = String.format("{\"unreadCount\":%d}", unreadCount);
            NotificationEventBroker.pushMessage(payload, Collections.singletonList(username));
            return result;
        } catch (NotificationManagementDAOException e) {
            NotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            NotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Map<String, List<Integer>> archiveUserNotifications(List<Integer> notificationIds, String username)
            throws NotificationArchivalException {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return Map.of("archived", Collections.emptyList(), "invalid", Collections.emptyList());
        }
        try {
            username = NotificationHelper.validateUserExists(username);
            NotificationArchivalDestDAOFactory.beginTransaction();
            NotificationArchivalSourceDAOFactory.beginTransaction();
            Map<String, List<Integer>> result =
                    notificationArchiveDAO.archiveUserNotifications(notificationIds, username);
            NotificationArchivalDestDAOFactory.commitTransaction();
            NotificationArchivalSourceDAOFactory.commitTransaction();
            try {
                NotificationManagementDAOFactory.openConnection();
                int unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
                String payload = String.format("{\"unreadCount\":%d}", unreadCount);
                NotificationEventBroker.pushMessage(payload, Collections.singletonList(username));
            } catch (SQLException | NotificationManagementDAOException e) {
                String msg = "Error occurred while retrieving unread notification count for user: " + username;
                log.error(msg, e);
                throw new NotificationArchivalException(msg, e);
            } finally {
                NotificationManagementDAOFactory.closeConnection();
            }
            return result;
        } catch (NotificationManagementException e) {
            NotificationArchivalDestDAOFactory.rollbackTransaction();
            NotificationArchivalSourceDAOFactory.rollbackTransaction();
            String msg = "Error occurred while archiving user notifications for user: " + username +
                    "user doesn't exist";
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        } catch (TransactionManagementException e) {
            NotificationArchivalDestDAOFactory.rollbackTransaction();
            NotificationArchivalSourceDAOFactory.rollbackTransaction();
            String msg = "Error occurred while archiving notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        } finally {
            NotificationArchivalDestDAOFactory.closeConnection();
            NotificationArchivalSourceDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteAllUserNotifications(String username) throws NotificationManagementException {
        username = NotificationHelper.validateUserExists(username);
        try {
            NotificationManagementDAOFactory.beginTransaction();
            notificationDAO.deleteAllUserNotifications(username);
            NotificationManagementDAOFactory.commitTransaction();
            int unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
            String payload = String.format("{\"unreadCount\":%d}", unreadCount);
            NotificationEventBroker.pushMessage(payload, Collections.singletonList(username));
        } catch (NotificationManagementDAOException e) {
            String msg = "Error occurred while deleting all notifications.";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            NotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting all notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void archiveAllUserNotifications(String username) throws NotificationArchivalException {
        try {
            username = NotificationHelper.validateUserExists(username);
            NotificationArchivalDestDAOFactory.beginTransaction();
            NotificationArchivalSourceDAOFactory.beginTransaction();
            notificationArchiveDAO.archiveAllUserNotifications(username);
            NotificationArchivalDestDAOFactory.commitTransaction();
            NotificationArchivalSourceDAOFactory.commitTransaction();
            try {
                NotificationManagementDAOFactory.openConnection();
                int unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
                String payload = String.format("{\"unreadCount\":%d}", unreadCount);
                NotificationEventBroker.pushMessage(payload, Collections.singletonList(username));
            } catch (SQLException | NotificationManagementDAOException e) {
                String msg = "Error occurred while retrieving unread notification count for user: " + username;
                log.error(msg, e);
                throw new NotificationArchivalException(msg, e);
            } finally {
                NotificationManagementDAOFactory.closeConnection();
            }
        } catch (NotificationManagementException e) {
            String msg = "Error occurred while archiving user notifications for user: " + username +
                    "user doesn't exist";
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while archiving all notifications for user: " + username;
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        } finally {
            NotificationArchivalDestDAOFactory.closeConnection();
            NotificationArchivalSourceDAOFactory.closeConnection();
        }
    }

    @Override
    public void handleOperationNotificationIfApplicable(String operationCode, String operationStatus,
                                                        String deviceType, List<Integer> deviceEnrollmentIDs,
                                                        int tenantId, String notificationTriggerPoint)
            throws NotificationManagementException {
        try {
            NotificationConfig config = NotificationHelper.getNotificationConfigurationByCode(operationCode);
            if (config == null || !config.isEnabled()) return;
            NotificationConfigurationSettings settings = config.getNotificationSettings();
            if (settings == null) return;
            String configDeviceTypes = config.getDeviceType();
            List<String> triggerPoints = settings.getNotificationTriggerPoints();
            if (configDeviceTypes == null || triggerPoints == null ||
                    !configDeviceTypes.equalsIgnoreCase(deviceType) ||
                    !triggerPoints.contains(notificationTriggerPoint)) {
                return;
            }
            String statusToCheck = (operationStatus != null) ? operationStatus : Constants.PENDING;
            NotificationConfigCriticalCriteria criticalCriteriaConfig = settings.getCriticalCriteriaOnly();
            if (criticalCriteriaConfig != null && criticalCriteriaConfig.isStatus()) {
                List<String> criticalCriteria = criticalCriteriaConfig.getCriticalCriteria();
                if (criticalCriteria == null || !criticalCriteria.contains(statusToCheck)) {
                    return;
                }
            }
            NotificationConfigBatchNotifications batchConfig = settings.getBatchNotifications();
            boolean isBatch = batchConfig != null && batchConfig.isEnabled();
            if (isBatch) {
                handleBatchOperationNotificationIfApplicable(config, deviceEnrollmentIDs,
                        operationStatus, deviceType, tenantId);
            } else {
                try {
                    NotificationManagementDAOFactory.beginTransaction();
                    for (int deviceEnrollmentID : deviceEnrollmentIDs) {
                        String description = String.format("The operation %s (%s) for device with id %d of type %s is %s.",
                                config.getCode(), config.getDescription(), deviceEnrollmentID, deviceType, statusToCheck);
                        int notificationId = notificationDAO.insertNotification(
                                tenantId, config.getId(), config.getType(), description);
                        List<String> usernames = NotificationHelper.extractUsernamesFromRecipients(
                                config.getRecipients(), tenantId);
                        if (!usernames.isEmpty()) {
                            notificationDAO.insertNotificationUserActions(notificationId, usernames);
                            for (String username : usernames) {
                                int count = notificationDAO.getUnreadNotificationCountForUser(username);
                                String payload = String.format(
                                        "{\"message\":\"%s\",\"unreadCount\":%d}", description, count);
                                NotificationEventBroker.pushMessage(payload, Collections.singletonList(username));
                            }
                        }
                    }
                    NotificationManagementDAOFactory.commitTransaction();
                } catch (NotificationManagementDAOException e) {
                    NotificationManagementDAOFactory.rollbackTransaction();
                    String msg = "Error occurred while adding notification";
                    log.error(msg, e);
                    throw new NotificationManagementException(msg, e);
                } catch (UserStoreException e) {
                    String msg = "Error occurred while adding user actions";
                    log.error(msg, e);
                    throw new NotificationManagementException(msg, e);
                } finally {
                    NotificationManagementDAOFactory.closeConnection();
                }
            }
        } catch (NotificationManagementException e) {
            String msg = "Failed to handle notification for operation " + operationCode;
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            NotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding notification";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
    }

    @Override
    public void handleBatchOperationNotificationIfApplicable(NotificationConfig config,
                                                             List<Integer> deviceIds,
                                                             String operationStatus,
                                                             String deviceType,
                                                             int tenantId)
            throws NotificationManagementException {
        if (!config.isEnabled()) return;
        String status = (operationStatus != null) ? operationStatus : Constants.PENDING;
        NotificationConfigBatchNotifications batchConfig = config.getNotificationSettings().getBatchNotifications();
        boolean includeDeviceList = batchConfig.isIncludeDeviceListInBatch();
        String description;
        if (includeDeviceList) {
            description = String.format("The operation %s (%s) for device with ids %s of type %s is %s.",
                    config.getCode(), config.getDescription(), deviceIds.toString(), deviceType, status);
        } else {
            description = String.format("The operation %s (%s) for devices of type %s is %s.",
                    config.getCode(), config.getDescription(), deviceType, status);
        }
        try {
            NotificationManagementDAOFactory.beginTransaction();
            int notificationId = notificationDAO.insertNotification(
                    tenantId, config.getId(), config.getType(), description);
            List<String> usernames = NotificationHelper.extractUsernamesFromRecipients(
                    config.getRecipients(), tenantId);
            if (!usernames.isEmpty()) {
                notificationDAO.insertNotificationUserActions(notificationId, usernames);
                for (String username : usernames) {
                    int count = notificationDAO.getUnreadNotificationCountForUser(username);
                    String payload = String.format(
                            "{\"message\":\"%s\",\"unreadCount\":%d}", description, count);
                    NotificationEventBroker.pushMessage(payload, Collections.singletonList(username));
                }
            }
            NotificationManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException | UserStoreException | NotificationManagementDAOException e) {
            NotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while handling batch operation notification for config: " + config.getCode();
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void handleTaskNotificationIfApplicable(int tenantId, String message)
            throws NotificationManagementException {
        try {
            UserStoreManager userStoreManager = NotificationManagementDataHolder.getInstance()
                    .getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            String[] adminUsers = userStoreManager.getUserListOfRole(Constants.ADMIN);
            List<String> usernames = Arrays.asList(adminUsers);
            if (!usernames.isEmpty()) {
                String description = String.format(message);
                try {
                    NotificationManagementDAOFactory.beginTransaction();
                    int notificationId = notificationDAO.insertNotification(
                            tenantId, 0, "task", description);
                    notificationDAO.insertNotificationUserActions(notificationId, usernames);
                    for (String username : usernames) {
                        int count = notificationDAO.getUnreadNotificationCountForUser(username);
                        String payload = String.format("{\"message\":\"%s\",\"unreadCount\":%d}", description, count);
                        NotificationEventBroker.pushMessage(payload, Collections.singletonList(username));
                    }
                    NotificationManagementDAOFactory.commitTransaction();
                } catch (Exception e) {
                    NotificationManagementDAOFactory.rollbackTransaction();
                    String msg = "Error occurred while handling notification transaction for the task.";
                    log.error(msg, e);
                    throw new NotificationManagementException(msg, e);
                } finally {
                    NotificationManagementDAOFactory.closeConnection();
                }
            }
        } catch (UserStoreException e) {
            String msg = "Error retrieving users with role 'admin'";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
    }
}
