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
 */

package io.entgra.device.mgt.core.realtime.notification.mgt.core.impl;

import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfig;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfigBatchNotifications;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfigCriticalCriteria;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfigurationSettings;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.dto.RealtimeNotification;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.dto.RealtimeNotificationContext;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.dto.RealtimePaginatedUserNotificationResponse;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationArchivalException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationManagementDAOException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationManagementException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeTransactionManagementException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.service.RealtimeNotificationManagementService;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.RealtimeNotificationArchivalDAO;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.RealtimeNotificationManagementDAO;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.RealtimeNotificationManagementDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.archive.RealtimeNotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.archive.RealtimeNotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.internal.RealtimeNotificationManagementDataHolder;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeConstants;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeNotificationEventBroker;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeNotificationHelper;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeNotificationMessageRenderer;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeNotificationEventPayloadBuilder;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class RealtimeNotificationManagementServiceImpl implements RealtimeNotificationManagementService {
    private static final Log log = LogFactory.getLog(RealtimeNotificationManagementServiceImpl.class);
    /** Matches {@code DM_REALTIME_NOTIFICATION.NOTIFICATION_CONTEXT} / archive column size in CDM scripts. */
    private static final int MAX_NOTIFICATION_CONTEXT_LENGTH = 1000;
    private final RealtimeNotificationManagementDAO notificationDAO;
    private final RealtimeNotificationArchivalDAO notificationArchiveDAO;

    public RealtimeNotificationManagementServiceImpl() {
        notificationDAO = RealtimeNotificationManagementDAOFactory.getNotificationManagementDAO();
        notificationArchiveDAO = RealtimeNotificationArchivalDestDAOFactory.getNotificationArchivalDAO();
    }

    @Override
    public List<RealtimeNotification> getAllNotifications(int offset, int limit) throws RealtimeNotificationManagementException {
        try {
            RealtimeNotificationManagementDAOFactory.openConnection();
            return notificationDAO.getLatestNotifications(offset, limit);
        } catch (RealtimeNotificationManagementDAOException e) {
            String msg = "Error occurred while retrieving all notifications";
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public RealtimePaginatedUserNotificationResponse getUserNotificationsWithStatus(
            String username, int limit, int offset, Boolean isRead) throws RealtimeNotificationManagementException {
        try {
            RealtimeNotificationManagementDAOFactory.openConnection();
            return notificationDAO.getUserNotificationsWithStatus(username, limit, offset, isRead);
        } catch (RealtimeNotificationManagementDAOException e) {
            String msg = "Error occurred while retrieving user notifications";
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving user notifications with status";
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void updateNotificationActionForUser(List<Integer> notificationIds, String username, boolean isRead)
            throws RealtimeNotificationManagementException {
        int unreadCount = 0;
        try {
            RealtimeNotificationManagementDAOFactory.beginTransaction();
            notificationDAO.updateNotificationAction(notificationIds, username, isRead);
            unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
            RealtimeNotificationManagementDAOFactory.commitTransaction();
        } catch (RealtimeNotificationManagementDAOException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating notification actions";
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (RealtimeTransactionManagementException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating notification actions for user: " + username;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
        pushUnreadCount(username, unreadCount);
    }

    @Override
    public void updateAllNotificationActionForUser(String username, boolean isRead)
            throws RealtimeNotificationManagementException {
        int unreadCount = 0;
        try {
            RealtimeNotificationManagementDAOFactory.beginTransaction();
            notificationDAO.updateAllNotificationAction(username, isRead);
            unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
            RealtimeNotificationManagementDAOFactory.commitTransaction();
        } catch (RealtimeNotificationManagementDAOException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating all notification actions";
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (RealtimeTransactionManagementException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating all notification actions for user: " + username;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
        pushUnreadCount(username, unreadCount);
    }

    @Override
    public int getUserNotificationCount(String username, Boolean isRead) throws RealtimeNotificationManagementException {
        try {
            RealtimeNotificationManagementDAOFactory.openConnection();
            return notificationDAO.getNotificationActionsCountByUser(username, isRead);
        } catch (SQLException e) {
            String msg = "Error occurred while counting user notifications for user: " + username;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (RealtimeNotificationManagementDAOException e) {
            String msg = "Error occurred while retrieving user notifications for user: " + username;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Map<String, List<Integer>> deleteUserNotifications(List<Integer> notificationIds, String username)
            throws RealtimeNotificationManagementException {
        Map<String, List<Integer>> result = null;
        int unreadCount = 0;
        try {
            RealtimeNotificationManagementDAOFactory.beginTransaction();
            result = notificationDAO.deleteUserNotifications(notificationIds, username);
            unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
            RealtimeNotificationManagementDAOFactory.commitTransaction();
        } catch (RealtimeNotificationManagementDAOException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting notifications for user: " + username;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (RealtimeTransactionManagementException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting notifications for user: " + username;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
        pushUnreadCount(username, unreadCount);
        return result;
    }

    @Override
    public Map<String, List<Integer>> archiveUserNotifications(List<Integer> notificationIds, String username)
            throws RealtimeNotificationArchivalException {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return Map.of("archived", Collections.emptyList(), "invalid", Collections.emptyList());
        }
        Map<String, List<Integer>> result;
        boolean destinationOpened = false;
        boolean sourceOpened = false;
        try {
            RealtimeNotificationArchivalDestDAOFactory.beginTransaction();
            destinationOpened = true;
            RealtimeNotificationArchivalSourceDAOFactory.beginTransaction();
            sourceOpened = true;
            result = notificationArchiveDAO.archiveUserNotifications(notificationIds, username);
            RealtimeNotificationArchivalDestDAOFactory.commitTransaction();
            RealtimeNotificationArchivalSourceDAOFactory.commitTransaction();
        } catch (RealtimeTransactionManagementException | RuntimeException e) {
            if (destinationOpened) {
                RealtimeNotificationArchivalDestDAOFactory.rollbackTransaction();
            }
            if (sourceOpened) {
                RealtimeNotificationArchivalSourceDAOFactory.rollbackTransaction();
            }
            String msg = "Error occurred while archiving notifications for user: " + username;
            log.error(msg, e);
            throw new RealtimeNotificationArchivalException(msg, e);
        } finally {
            if (destinationOpened) {
                RealtimeNotificationArchivalDestDAOFactory.closeConnection();
            }
            if (sourceOpened) {
                RealtimeNotificationArchivalSourceDAOFactory.closeConnection();
            }
        }
        refreshUnreadCountAfterArchival(username);
        return result;
    }

    @Override
    public void deleteAllUserNotifications(String username, Boolean isRead) throws RealtimeNotificationManagementException {
        int unreadCount = 0;
        try {
            RealtimeNotificationManagementDAOFactory.beginTransaction();
            notificationDAO.deleteAllUserNotifications(username, isRead);
            unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
            RealtimeNotificationManagementDAOFactory.commitTransaction();
        } catch (RealtimeNotificationManagementDAOException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting all notifications.";
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (RealtimeTransactionManagementException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting all notifications for user: " + username;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
        pushUnreadCount(username, unreadCount);
    }

    @Override
    public void archiveAllUserNotifications(String username) throws RealtimeNotificationArchivalException {
        boolean destinationOpened = false;
        boolean sourceOpened = false;
        try {
            RealtimeNotificationArchivalDestDAOFactory.beginTransaction();
            destinationOpened = true;
            RealtimeNotificationArchivalSourceDAOFactory.beginTransaction();
            sourceOpened = true;
            notificationArchiveDAO.archiveAllUserNotifications(username);
            RealtimeNotificationArchivalDestDAOFactory.commitTransaction();
            RealtimeNotificationArchivalSourceDAOFactory.commitTransaction();
        } catch (RealtimeTransactionManagementException | RuntimeException e) {
            if (destinationOpened) {
                RealtimeNotificationArchivalDestDAOFactory.rollbackTransaction();
            }
            if (sourceOpened) {
                RealtimeNotificationArchivalSourceDAOFactory.rollbackTransaction();
            }
            String msg = "Error occurred while archiving all notifications for user: " + username;
            log.error(msg, e);
            throw new RealtimeNotificationArchivalException(msg, e);
        } finally {
            if (destinationOpened) {
                RealtimeNotificationArchivalDestDAOFactory.closeConnection();
            }
            if (sourceOpened) {
                RealtimeNotificationArchivalSourceDAOFactory.closeConnection();
            }
        }
        refreshUnreadCountAfterArchival(username);
    }

    /**
     * Retrieves and publishes the user's unread notification count after archival has completed.
     * Releases the count-query connection before delivering the update. Retrieval and delivery
     * failures are logged without failing the completed archival operation.
     *
     * @param username user whose unread notification count should be refreshed
     */
    private void refreshUnreadCountAfterArchival(String username) {
        int unreadCount;
        boolean connectionOpened = false;
        try {
            RealtimeNotificationManagementDAOFactory.openConnection();
            connectionOpened = true;
            unreadCount = notificationDAO.getUnreadNotificationCountForUser(username);
        } catch (SQLException | RealtimeNotificationManagementDAOException | RuntimeException e) {
            log.warn("Notifications were archived, but the unread count could not be retrieved for user: "
                    + username, e);
            return;
        } finally {
            if (connectionOpened) {
                RealtimeNotificationManagementDAOFactory.closeConnection();
            }
        }
        try {
            pushUnreadCount(username, unreadCount);
        } catch (RuntimeException e) {
            log.warn("Notifications were archived, but the unread count update could not be delivered for user: "
                    + username, e);
        }
    }

    @Override
    public void purgeNotificationsForDevices(List<Integer> deviceIds, int tenantId)
            throws RealtimeNotificationManagementException {
        try {
            RealtimeNotificationManagementDAOFactory.beginTransaction();
            notificationDAO.purgeNotificationsForDevices(deviceIds, tenantId);
            RealtimeNotificationManagementDAOFactory.commitTransaction();
        } catch (RealtimeNotificationManagementDAOException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while purging notifications for devices: " + deviceIds;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (RealtimeTransactionManagementException e) {
            String msg = "Error occurred while purging notifications for devices: " + deviceIds;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteNotificationsByTenantId(int tenantId) throws RealtimeNotificationManagementException {
        try {
            RealtimeNotificationManagementDAOFactory.beginTransaction();
            notificationDAO.deleteNotificationsByTenantId(tenantId);
            RealtimeNotificationManagementDAOFactory.commitTransaction();
        } catch (RealtimeNotificationManagementDAOException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting realtime notifications for tenant: " + tenantId;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (RealtimeTransactionManagementException e) {
            String msg = "Error occurred while deleting realtime notifications for tenant: " + tenantId;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean isNotificationConfigAvailable(String code) throws RealtimeNotificationManagementException {
        RealtimeNotificationConfig config = RealtimeNotificationHelper.getNotificationConfigurationByCode(code);
        return config != null && config.isEnabled();
    }

    @Override
    public void handleOperationNotificationIfApplicable(String operationCode, String operationStatus,
                                                        String deviceType, List<Integer> deviceIds,
                                                        int tenantId, String notificationTriggerPoint)
            throws RealtimeNotificationManagementException {
        try {
            RealtimeNotificationConfig config = RealtimeNotificationHelper.getNotificationConfigurationByCode(operationCode);
            if (config == null || !config.isEnabled()) {
                return;
            }
            RealtimeNotificationConfigurationSettings settings = config.getNotificationSettings();
            if (settings == null) {
                return;
            }
            String configDeviceTypes = config.getDeviceType();
            List<String> triggerPoints = settings.getNotificationTriggerPoints();
            if (configDeviceTypes == null || triggerPoints == null ||
                    !configDeviceTypes.equalsIgnoreCase(deviceType) ||
                    !triggerPoints.contains(notificationTriggerPoint)) {
                return;
            }
            String statusToCheck = (operationStatus != null) ? operationStatus : RealtimeConstants.PENDING;
            RealtimeNotificationConfigCriticalCriteria criticalCriteriaConfig = settings.getCriticalCriteriaOnly();
            if (criticalCriteriaConfig != null && criticalCriteriaConfig.isStatus()) {
                List<String> criticalCriteria = criticalCriteriaConfig.getCriticalCriteria();
                if (criticalCriteria == null || !criticalCriteria.contains(statusToCheck)) {
                    return;
                }
            }
            RealtimeNotificationConfigBatchNotifications batchConfig = settings.getBatchNotifications();
            boolean isBatch = batchConfig != null && batchConfig.isEnabled();
            if (deviceIds == null || deviceIds.isEmpty()) {
                return;
            }
            if (isBatch) {
                handleBatchOperationNotificationIfApplicable(config, deviceIds,
                        operationStatus, deviceType, tenantId);
            } else {
                try {
                    RealtimeNotificationManagementDAOFactory.beginTransaction();
                    for (Integer deviceId : deviceIds) {
                        if (deviceId == null) {
                            continue;
                        }
                        RealtimeNotificationContext context = buildOperationContext(
                                config, statusToCheck, deviceType, true);
                        publishNotification(tenantId, config, context,
                                Collections.singletonList(deviceId));
                    }
                    RealtimeNotificationManagementDAOFactory.commitTransaction();
                } catch (RealtimeNotificationManagementException e) {
                    RealtimeNotificationManagementDAOFactory.rollbackTransaction();
                    throw e;
                } catch (UserStoreException e) {
                    RealtimeNotificationManagementDAOFactory.rollbackTransaction();
                    String msg = "Error occurred while adding user actions";
                    log.error(msg, e);
                    throw new RealtimeNotificationManagementException(msg, e);
                } finally {
                    RealtimeNotificationManagementDAOFactory.closeConnection();
                }
            }
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Failed to handle notification for operation " + operationCode;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (RealtimeTransactionManagementException e) {
            String msg = "Error occurred while adding notification";
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        }
    }

    @Override
    public void handleBatchOperationNotificationIfApplicable(RealtimeNotificationConfig config,
                                                             List<Integer> deviceIds,
                                                             String operationStatus,
                                                             String deviceType,
                                                             int tenantId)
            throws RealtimeNotificationManagementException {
        if (!config.isEnabled()) {
            return;
        }
        if (deviceIds == null || deviceIds.isEmpty()) {
            return;
        }
        String status = (operationStatus != null) ? operationStatus : RealtimeConstants.PENDING;
        RealtimeNotificationConfigBatchNotifications batchConfig = config.getNotificationSettings().getBatchNotifications();
        boolean includeDeviceList = batchConfig.isIncludeDeviceListInBatch();
        RealtimeNotificationContext context = buildOperationContext(config, status, deviceType, includeDeviceList);
        List<Integer> distinctDeviceIds = new ArrayList<>(new LinkedHashSet<>(deviceIds));
        try {
            RealtimeNotificationManagementDAOFactory.beginTransaction();
            publishNotification(tenantId, config, context, distinctDeviceIds);
            RealtimeNotificationManagementDAOFactory.commitTransaction();
        } catch (RealtimeTransactionManagementException e) {
            String msg = "Error occurred while handling batch operation notification for config: " + config.getCode();
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } catch (RealtimeNotificationManagementException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            throw e;
        } catch (UserStoreException e) {
            RealtimeNotificationManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while handling batch operation notification for config: " + config.getCode();
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        } finally {
            RealtimeNotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void handleTaskNotificationIfApplicable(int tenantId, String taskCode, String message)
            throws RealtimeNotificationManagementException {
        if (taskCode == null || taskCode.isBlank()) {
            log.warn("Task code is empty. Skipping task notification.");
            return;
        }
        RealtimeNotificationConfig config = RealtimeNotificationHelper.getNotificationConfigurationByCode(taskCode);
        if (config == null || !config.isEnabled()
                || !RealtimeConstants.TASK.equalsIgnoreCase(config.getType())) {
            if (log.isDebugEnabled()) {
                log.debug("No enabled task notification config for code: " + taskCode);
            }
            return;
        }
        try {
            List<String> usernames = RealtimeNotificationHelper.extractUsernamesFromRecipients(
                    config.getRecipients(), tenantId);
            if (usernames == null || usernames.isEmpty()) {
                log.warn("No recipients resolved for task notification config: " + taskCode
                        + ". Skipping task notification.");
                return;
            }
            RealtimeNotificationContext context = new RealtimeNotificationContext();
            context.setMessage(message);
            try {
                RealtimeNotificationManagementDAOFactory.beginTransaction();
                int notificationId = notificationDAO.insertNotification(
                        tenantId, config.getId(), RealtimeConstants.TASK,
                        RealtimeNotificationMessageRenderer.toJson(context));
                notificationDAO.insertNotificationUserActions(notificationId, usernames);
                String description = RealtimeNotificationMessageRenderer.render(context, RealtimeConstants.TASK);
                for (String username : usernames) {
                    int count = notificationDAO.getUnreadNotificationCountForUser(username);
                    RealtimeNotificationEventBroker.pushMessage(
                            RealtimeNotificationEventPayloadBuilder.buildMessagePayload(description, count),
                            Collections.singletonList(username));
                }
                RealtimeNotificationManagementDAOFactory.commitTransaction();
            } catch (RealtimeNotificationManagementDAOException e) {
                RealtimeNotificationManagementDAOFactory.rollbackTransaction();
                String msg = "Error occurred while handling notification transaction for the task.";
                log.error(msg, e);
                throw new RealtimeNotificationManagementException(msg, e);
            } catch (RealtimeTransactionManagementException e) {
                String msg = "Error occurred while handling notification transaction for the task.";
                log.error(msg, e);
                throw new RealtimeNotificationManagementException(msg, e);
            } finally {
                RealtimeNotificationManagementDAOFactory.closeConnection();
            }
        } catch (UserStoreException e) {
            String msg = "Error resolving recipients for task notification: " + taskCode;
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        }
    }

    private void publishNotification(int tenantId, RealtimeNotificationConfig config, RealtimeNotificationContext context,
                                     List<Integer> deviceIds)
            throws RealtimeNotificationManagementException, UserStoreException {
        if (config == null) {
            throw new RealtimeNotificationManagementException("RealtimeNotification config must not be null");
        }
        if (context == null) {
            throw new RealtimeNotificationManagementException("RealtimeNotification context must not be null");
        }
        List<Integer> resolvedDeviceIds = deviceIds != null ? deviceIds : Collections.emptyList();
        // resolve labels only when needed; omit them if JSON would exceed VARCHAR(1000).
        if (Boolean.TRUE.equals(context.getIncludeDeviceList())
                && (context.getDeviceLabels() == null || context.getDeviceLabels().isEmpty())) {
            context.setDeviceLabels(resolveDeviceLabels(resolvedDeviceIds));
        }
        String contextJson = RealtimeNotificationMessageRenderer.toJson(context);
        if (contextJson != null && contextJson.length() > MAX_NOTIFICATION_CONTEXT_LENGTH) {
            context.setDeviceLabels(Collections.emptyList());
            context.setIncludeDeviceList(false);
            contextJson = RealtimeNotificationMessageRenderer.toJson(context);
            log.warn("Omitted deviceLabels from notification context to fit VARCHAR("
                    + MAX_NOTIFICATION_CONTEXT_LENGTH + ") limit.");
        }
        try {
            int notificationId = notificationDAO.insertNotification(
                    tenantId, config.getId(), config.getType(), contextJson);
            notificationDAO.insertNotificationDeviceMappings(notificationId, resolvedDeviceIds);
            List<String> usernames = RealtimeNotificationHelper.extractUsernamesFromRecipients(
                    config.getRecipients(), tenantId);
            if (usernames.isEmpty()) {
                return;
            }
            notificationDAO.insertNotificationUserActions(notificationId, usernames);
            String description = RealtimeNotificationMessageRenderer.render(context, config.getType());
            for (String username : usernames) {
                int count = notificationDAO.getUnreadNotificationCountForUser(username);
                RealtimeNotificationEventBroker.pushMessage(
                        RealtimeNotificationEventPayloadBuilder.buildMessagePayload(description, count),
                        Collections.singletonList(username));
            }
        } catch (RealtimeNotificationManagementDAOException e) {
            String msg = "Error occurred while publishing notification for config: " + config.getCode();
            log.error(msg, e);
            throw new RealtimeNotificationManagementException(msg, e);
        }
    }

    /**
     * Builds user-facing device labels ({@code 'Name' (identifier)}) for operation
     * notification messages from internal {@code DM_DEVICE.ID} values.
     * <p>
     * Returns an empty list when ids are missing, the device service is unavailable,
     * lookup fails, or no devices are found — callers then omit the device list.
     *
     * @param deviceIds internal device primary keys; may be {@code null} or empty
     * @return label strings for each resolved device; never {@code null}
     */
    private List<String> resolveDeviceLabels(List<Integer> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        DeviceManagementProviderService deviceService =
                RealtimeNotificationManagementDataHolder.getInstance().getDeviceManagementProviderService();
        if (deviceService == null) {
            return Collections.emptyList();
        }
        try {
            PaginationRequest request = new PaginationRequest(0, deviceIds.size());
            List<Device> devices = deviceService.getDevicesByDeviceIds(request, deviceIds);
            if (devices == null || devices.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> labels = new ArrayList<>();
            for (Device device : devices) {
                labels.add(formatDeviceNotificationLabel(
                        device.getName(), device.getDeviceIdentifier()));
            }
            return labels;
        } catch (DeviceManagementException e) {
            log.warn("Unable to resolve device names/identifiers for notification message.", e);
            return Collections.emptyList();
        }
    }

    /**
     * Formats a device as {@code 'Name' (identifier)} for notification text.
     * Kept local so realtime-notification-mgt does not depend on device.mgt.core.util.
     */
    private static String formatDeviceNotificationLabel(String name, String identifier) {
        String deviceName = name != null && !name.isBlank() ? name.trim() : "Unknown";
        String deviceId = identifier != null && !identifier.isBlank() ? identifier.trim() : "unknown";
        return "'" + deviceName + "' (" + deviceId + ")";
    }

    private RealtimeNotificationContext buildOperationContext(RealtimeNotificationConfig config, String operationStatus,
                                                      String deviceType, boolean includeDeviceList) {
        RealtimeNotificationContext context = new RealtimeNotificationContext();
        context.setOperationCode(config.getCode());
        context.setConfigDescription(config.getDescription());
        context.setOperationStatus(operationStatus);
        context.setDeviceType(deviceType);
        context.setIncludeDeviceList(includeDeviceList);
        return context;
    }

    private void pushUnreadCount(String username, int unreadCount) {
        RealtimeNotificationEventBroker.pushMessage(
                RealtimeNotificationEventPayloadBuilder.buildUnreadCountPayload(unreadCount),
                Collections.singletonList(username));
    }
}
