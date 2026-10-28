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

package io.entgra.device.mgt.core.realtime.notification.mgt.core.impl;

import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeArchivePeriod;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfig;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfigurationList;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationArchivalException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationManagementException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.service.RealtimeNotificationArchivalService;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.RealtimeNotificationArchivalDAO;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.archive.RealtimeNotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.archive.RealtimeNotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeConstants;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeNotificationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RealtimeNotificationArchivalServiceImpl implements RealtimeNotificationArchivalService {
    private static final Log log = LogFactory.getLog(RealtimeNotificationArchivalServiceImpl.class);

    private final RealtimeNotificationArchivalDAO archivalDAO;
    private final RealtimeNotificationArchivalDAO deleteDAO;

    public RealtimeNotificationArchivalServiceImpl() {
        archivalDAO = RealtimeNotificationArchivalDestDAOFactory.getNotificationArchivalDAO();
        deleteDAO = RealtimeNotificationArchivalSourceDAOFactory.getNotificationArchivalDAO();
    }

    @Override
    public void archiveOldNotifications(int tenantId) throws RealtimeNotificationArchivalException {
        log.info("Starting dynamic notification archival based on config-defined periods.");
        RealtimeNotificationConfigurationList configList;
        try {
            configList = RealtimeNotificationHelper.getNotificationConfigurationsFromMetadata();
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Failed to load notification configurations. Skipping archival.";
            log.error(msg, e);
            throw new RealtimeNotificationArchivalException(msg, e);
        }
        if (configList == null || configList.getNotificationConfigurations() == null) {
            log.warn("No notification configurations found. Skipping archival.");
            return;
        }
        try {
            RealtimeNotificationArchivalSourceDAOFactory.beginTransaction();
            RealtimeNotificationArchivalDestDAOFactory.beginTransaction();
            RealtimeNotificationHelper.setDefaultArchivalValuesIfAbsent(configList);
            String defaultArchiveType = configList.getDefaultArchiveType();
            RealtimeArchivePeriod defaultArchiveAfter = configList.getDefaultArchiveAfter();
            Set<Integer> alreadyHandledConfigIds = new HashSet<>();
            for (RealtimeNotificationConfig config : configList.getNotificationConfigurations()) {
                String archiveType = config.getNotificationSettings() != null ?
                        config.getNotificationSettings().getArchiveType() : null;
                RealtimeArchivePeriod archiveAfter = config.getNotificationSettings() != null ?
                        config.getNotificationSettings().getArchiveAfter() : null;
                if (RealtimeConstants.DEFAULT_ARCHIVE_TYPE.equalsIgnoreCase(archiveType)) {
                    if (archiveAfter == null) {
                        log.warn("Missing archiveAfter for config ID " + config.getId() +
                                ". Using default archive period.");
                        archiveAfter = defaultArchiveAfter;
                    }
                    Timestamp cutoff;
                    try {
                        cutoff = RealtimeNotificationHelper.resolveCutoffTimestamp(archiveAfter);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid archiveAfter '" + archiveAfter + "' for config ID " + config.getId()
                                + ". Using fallback.");
                        cutoff = RealtimeNotificationHelper.resolveCutoffTimestamp(defaultArchiveAfter);
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
            if (RealtimeConstants.DEFAULT_ARCHIVE_TYPE.equalsIgnoreCase(defaultArchiveType)) {
                Timestamp defaultCutoff;
                try {
                    defaultCutoff = RealtimeNotificationHelper.resolveCutoffTimestamp(defaultArchiveAfter);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid defaultArchiveAfter '" + defaultArchiveAfter +
                            "'. Falling back to DB.");
                    defaultCutoff = RealtimeNotificationHelper.resolveCutoffTimestamp(RealtimeConstants.DEFAULT_ARCHIVE_PERIOD);
                }
                log.info("Archiving default-config notifications older than " + defaultCutoff);
                List<Integer> moved = archivalDAO.moveNotificationsToArchiveExcludingConfigs(
                        defaultCutoff, tenantId, alreadyHandledConfigIds);
                log.info("Moved " + moved.size() + " default-config notifications");
                if (!moved.isEmpty()) {
                    archivalDAO.moveUserActionsToArchive(moved);
                }
            }
            log.info("RealtimeNotification archival completed successfully.");
            RealtimeNotificationArchivalSourceDAOFactory.commitTransaction();
            RealtimeNotificationArchivalDestDAOFactory.commitTransaction();
        } catch (Exception e) {
            RealtimeNotificationArchivalSourceDAOFactory.rollbackTransaction();
            RealtimeNotificationArchivalDestDAOFactory.rollbackTransaction();
            String msg = "Error during dynamic archival";
            log.error(msg, e);
            throw new RealtimeNotificationArchivalException(msg, e);
        } finally {
            RealtimeNotificationArchivalSourceDAOFactory.closeConnection();
            RealtimeNotificationArchivalDestDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteExpiredArchivedNotifications(int tenantId) throws RealtimeNotificationArchivalException {
        try {
            RealtimeNotificationArchivalDestDAOFactory.beginTransaction();
            log.info("Deleting archived notifications older than " + RealtimeConstants.DEFAULT_ARCHIVE_DELETE_PERIOD);
            Timestamp cutoff = RealtimeNotificationHelper.resolveCutoffTimestamp(RealtimeConstants.DEFAULT_ARCHIVE_DELETE_PERIOD);
            archivalDAO.deleteExpiredArchivedNotifications(cutoff, tenantId);
            RealtimeNotificationArchivalDestDAOFactory.commitTransaction();
        } catch (Exception e) {
            RealtimeNotificationArchivalDestDAOFactory.rollbackTransaction();
            String msg = "Error deleting expired archived notifications";
            log.error(msg, e);
            throw new RealtimeNotificationArchivalException(msg, e);
        } finally {
            RealtimeNotificationArchivalDestDAOFactory.closeConnection();
        }
    }
}