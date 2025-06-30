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

import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationArchivalService;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationArchivalDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationArchivalServiceImpl implements NotificationArchivalService {
    private static final Log log = LogFactory.getLog(NotificationArchivalServiceImpl.class);

    private final NotificationArchivalDAO archivalDAO;
    private final NotificationArchivalDAO deleteDAO;

    public NotificationArchivalServiceImpl() {
        archivalDAO = NotificationArchivalDestDAOFactory.getNotificationArchivalDAO();
        deleteDAO = NotificationArchivalSourceDAOFactory.getNotificationArchivalDAO();
    }

    @Override
    public void archiveOldNotifications(int tenantId) throws NotificationArchivalException {
        try {
            log.info("Starting dynamic notification archival based on config-defined periods.");
            NotificationArchivalSourceDAOFactory.beginTransaction();
            NotificationArchivalDestDAOFactory.beginTransaction();
            // get notification config list from metadata
            NotificationConfigurationList configList = NotificationHelper.getNotificationConfigurationsFromMetadata();
            if (configList == null || configList.getNotificationConfigurations() == null) {
                log.warn("No notification configurations found. Skipping archival.");
                NotificationArchivalSourceDAOFactory.commitTransaction();
                NotificationArchivalDestDAOFactory.commitTransaction();
                return;
            }
            NotificationHelper.setDefaultArchivalValuesIfAbsent(configList);
            String defaultArchiveType = configList.getDefaultArchiveType();
            String defaultArchiveAfter = configList.getDefaultArchiveAfter();
            Set<Integer> alreadyHandledConfigIds = new HashSet<>();
            for (NotificationConfig config : configList.getNotificationConfigurations()) {
                String archiveType = config.getNotificationSettings() != null ?
                        config.getNotificationSettings().getArchiveType() : null;
                String archiveAfter = config.getNotificationSettings() != null ?
                        config.getNotificationSettings().getArchiveAfter() : null;
                if (Constants.DEFAULT_ARCHIVE_TYPE.equalsIgnoreCase(archiveType)) {
                    if (archiveAfter == null || archiveAfter.trim().isEmpty()) {
                        log.warn("Missing archiveAfter for config ID " + config.getId() +
                                ". Using default archive period.");
                        archiveAfter = defaultArchiveAfter;
                    }
                    Timestamp cutoff;
                    try {
                        cutoff = NotificationHelper.resolveCutoffTimestamp(archiveAfter);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid archiveAfter '" + archiveAfter + "' for config ID " + config.getId()
                                + ". Using fallback.");
                        cutoff = NotificationHelper.resolveCutoffTimestamp(defaultArchiveAfter);
                    }
                    log.info("Archiving notifications for config ID: " + config.getId() +
                            " older than: " + cutoff);
                    List<Integer> moved = archivalDAO.moveNotificationsToArchiveByConfig(
                            cutoff, tenantId, config.getId());
                    log.info("Moved " + moved.size() + " notifications for config ID " + config.getId());
                    if (!moved.isEmpty()) {
                        archivalDAO.moveUserActionsToArchive(moved);
                        log.info("Moved user actions for config ID " + config.getId());
                    }
                    archivalDAO.deleteOldNotificationsByConfig(cutoff, tenantId, config.getId());
                    alreadyHandledConfigIds.add(config.getId());
                }
            }
            // archive all others using default DB config
            if (Constants.DEFAULT_ARCHIVE_TYPE.equalsIgnoreCase(defaultArchiveType)) {
                Timestamp defaultCutoff;
                try {
                    defaultCutoff = NotificationHelper.resolveCutoffTimestamp(defaultArchiveAfter);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid defaultArchiveAfter '" + defaultArchiveAfter +
                            "'. Falling back to DB.");
                    defaultCutoff = NotificationHelper.resolveCutoffTimestamp(Constants.DEFAULT_ARCHIVE_PERIOD);
                }
                log.info("Archiving default-config notifications older than " + defaultCutoff);
                List<Integer> moved = archivalDAO.moveNotificationsToArchiveExcludingConfigs(
                        defaultCutoff, tenantId, alreadyHandledConfigIds);
                log.info("Moved " + moved.size() + " default-config notifications");
                if (!moved.isEmpty()) {
                    archivalDAO.moveUserActionsToArchive(moved);
                }
            }
            log.info("Notification archival completed successfully.");
            NotificationArchivalSourceDAOFactory.commitTransaction();
            NotificationArchivalDestDAOFactory.commitTransaction();
        } catch (Exception e) {
            NotificationArchivalSourceDAOFactory.rollbackTransaction();
            NotificationArchivalDestDAOFactory.rollbackTransaction();
            String msg = "Error during dynamic archival";
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        } finally {
            NotificationArchivalSourceDAOFactory.closeConnection();
            NotificationArchivalDestDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteExpiredArchivedNotifications(int tenantId) throws NotificationArchivalException {
        try {
            log.info("Deleting archived notifications older than " + Constants.DEFAULT_ARCHIVE_DELETE_PERIOD);
            Timestamp cutoff = NotificationHelper.resolveCutoffTimestamp(Constants.DEFAULT_ARCHIVE_DELETE_PERIOD);
            NotificationArchivalDestDAOFactory.beginTransaction();
            archivalDAO.deleteExpiredArchivedNotifications(cutoff, tenantId);
            NotificationArchivalDestDAOFactory.commitTransaction();
        } catch (Exception e) {
            NotificationArchivalDestDAOFactory.rollbackTransaction();
            String msg = "Error deleting expired archived notifications";
            log.error(msg, e);
            throw new NotificationArchivalException(msg, e);
        } finally {
            NotificationArchivalDestDAOFactory.closeConnection();
        }
    }
}