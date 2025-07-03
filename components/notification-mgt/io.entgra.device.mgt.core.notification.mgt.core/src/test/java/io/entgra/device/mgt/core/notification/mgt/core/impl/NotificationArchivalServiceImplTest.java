/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.notification.mgt.core.impl;

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationSettings;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationArchivalDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementDataHolder;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.Whitebox;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Test class for NotificationArchivalServiceImpl
 */
@PrepareForTest({LogFactory.class, NotificationManagementDataHolder.class,
        NotificationHelper.class,
        NotificationArchivalDestDAOFactory.class,
        NotificationArchivalSourceDAOFactory.class,
        Constants.class,
        NotificationArchivalServiceImpl.class
})
public class NotificationArchivalServiceImplTest extends PowerMockTestCase {

    @Mock
    private NotificationArchivalDAO archivalDAOMock;

    @Mock
    private NotificationArchivalDAO deleteDAOMock;

    @Mock
    private MetadataManagementService metadataServiceMock;

    @Mock
    private RealmService realmServiceMock;

    @Mock
    private UserRealm userRealmMock;

    @Mock
    private UserStoreManager userStoreManagerMock;

    @Mock
    private Log mockLog;

    private NotificationArchivalServiceImpl service;

    @BeforeMethod
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(NotificationArchivalServiceImpl.class, "log", mockLog);
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(any(Class.class))).thenReturn(mockLog);
        Mockito.when(mockLog.isDebugEnabled()).thenReturn(true);
        Mockito.when(mockLog.isInfoEnabled()).thenReturn(true);
        Mockito.when(mockLog.isWarnEnabled()).thenReturn(true);
        Mockito.when(mockLog.isErrorEnabled()).thenReturn(true);
        PowerMockito.mockStatic(NotificationManagementDataHolder.class);
        NotificationManagementDataHolder mockDataHolder = Mockito.mock(NotificationManagementDataHolder.class);
        Mockito.when(NotificationManagementDataHolder.getInstance()).thenReturn(mockDataHolder);
        Mockito.when(mockDataHolder.getMetaDataManagementService()).thenReturn(metadataServiceMock);
        Mockito.when(mockDataHolder.getRealmService()).thenReturn(realmServiceMock);
        Mockito.when(realmServiceMock.getTenantUserRealm(anyInt())).thenReturn(userRealmMock);
        Mockito.when(userRealmMock.getUserStoreManager()).thenReturn(userStoreManagerMock);
        PowerMockito.mockStatic(NotificationArchivalDestDAOFactory.class);
        PowerMockito.when(NotificationArchivalDestDAOFactory.getNotificationArchivalDAO()).thenReturn(archivalDAOMock);
        PowerMockito.mockStatic(NotificationArchivalSourceDAOFactory.class);
        PowerMockito.when(NotificationArchivalSourceDAOFactory.getNotificationArchivalDAO()).thenReturn(deleteDAOMock);
        PowerMockito.doNothing().when(NotificationArchivalSourceDAOFactory.class, "beginTransaction");
        PowerMockito.doNothing().when(NotificationArchivalSourceDAOFactory.class, "commitTransaction");
        PowerMockito.doNothing().when(NotificationArchivalSourceDAOFactory.class, "rollbackTransaction");
        PowerMockito.doNothing().when(NotificationArchivalSourceDAOFactory.class, "closeConnection");
        PowerMockito.doNothing().when(NotificationArchivalDestDAOFactory.class, "beginTransaction");
        PowerMockito.doNothing().when(NotificationArchivalDestDAOFactory.class, "commitTransaction");
        PowerMockito.doNothing().when(NotificationArchivalDestDAOFactory.class, "rollbackTransaction");
        PowerMockito.doNothing().when(NotificationArchivalDestDAOFactory.class, "closeConnection");
        PowerMockito.mockStatic(NotificationHelper.class);
        NotificationConfigurationList initialConfigList = new NotificationConfigurationList();
        initialConfigList.setDefaultArchiveAfter("6 days");
        initialConfigList.setDefaultArchiveType(Constants.DEFAULT_ARCHIVE_TYPE);
        NotificationConfig config = new NotificationConfig();
        config.setId(1);
        config.setCode("TEST_CODE");
        NotificationConfigurationSettings settings = new NotificationConfigurationSettings();
        settings.setArchiveType(Constants.DEFAULT_ARCHIVE_TYPE);
        settings.setArchiveAfter("7 days");
        config.setNotificationSettings(settings);
        initialConfigList.setNotificationConfigurations(Collections.singletonList(config));
        PowerMockito.when(NotificationHelper.getNotificationConfigurationsFromMetadata())
                .thenReturn(initialConfigList);
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq("7 days")))
                .thenReturn(new Timestamp(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)));
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq("6 days")))
                .thenReturn(new Timestamp(System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000)));
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq("5 years")))
                .thenReturn(new Timestamp(System.currentTimeMillis() - (5L * 365 * 24 * 60 * 60 * 1000)));
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq(Constants.DEFAULT_ARCHIVE_PERIOD)))
                .thenReturn(new Timestamp(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)));
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq(Constants.DEFAULT_ARCHIVE_DELETE_PERIOD)))
                .thenReturn(new Timestamp(System.currentTimeMillis() - (5L * 365 * 24 * 60 * 60 * 1000)));
        PowerMockito.doCallRealMethod().when(NotificationHelper.class,
                "setDefaultArchivalValuesIfAbsent", any(NotificationConfigurationList.class));
        service = new NotificationArchivalServiceImpl(archivalDAOMock, deleteDAOMock);
    }


    @Test
    public void testArchiveOldNotifications_success() throws Exception {
        int tenantId = 1;
        when(archivalDAOMock.moveNotificationsToArchiveByConfig(any(Timestamp.class), eq(tenantId), eq(1)))
                .thenReturn(Arrays.asList(100, 200));
        doNothing().when(archivalDAOMock).moveUserActionsToArchive(anyList());
        when(archivalDAOMock.deleteOldNotificationsByConfig(any(Timestamp.class), eq(tenantId), eq(1)))
                .thenReturn(0);
        when(archivalDAOMock.moveNotificationsToArchiveExcludingConfigs(any(Timestamp.class), eq(tenantId), anySet()))
                .thenReturn(Collections.singletonList(300));
        service.archiveOldNotifications(tenantId);
        verify(archivalDAOMock, times(1))
                .moveNotificationsToArchiveByConfig(any(Timestamp.class), eq(tenantId), eq(1));
        verify(archivalDAOMock, times(1))
                .moveUserActionsToArchive(Arrays.asList(100, 200));
        verify(archivalDAOMock, times(1))
                .deleteOldNotificationsByConfig(any(Timestamp.class), eq(tenantId), eq(1));
        verify(archivalDAOMock, times(1))
                .moveNotificationsToArchiveExcludingConfigs(any(Timestamp.class), eq(tenantId), anySet());
        PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
        NotificationArchivalSourceDAOFactory.beginTransaction();
        PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
        NotificationArchivalSourceDAOFactory.commitTransaction();
        PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
        NotificationArchivalSourceDAOFactory.closeConnection();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.beginTransaction();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.commitTransaction();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.closeConnection();
        verify(mockLog, atLeastOnce()).info(anyString());
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testArchiveOldNotifications_noConfigurationsFound() throws Exception {
        int tenantId = 1;
        NotificationConfigurationList emptyListContainer = new NotificationConfigurationList();
        emptyListContainer.setNotificationConfigurations(null);
        PowerMockito.when(NotificationHelper.getNotificationConfigurationsFromMetadata())
                .thenReturn(emptyListContainer);
        service.archiveOldNotifications(tenantId);
        verify(archivalDAOMock, never()).moveNotificationsToArchiveByConfig(any(), anyInt(), anyInt());
        verify(archivalDAOMock, never()).moveNotificationsToArchiveExcludingConfigs(any(), anyInt(), anySet());
        verify(archivalDAOMock, never()).moveUserActionsToArchive(anyList());
        verify(archivalDAOMock, never()).deleteOldNotificationsByConfig(any(), anyInt(), anyInt());
        verify(mockLog, times(1))
                .warn("No notification configurations found. Skipping archival.");
        PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
        NotificationArchivalSourceDAOFactory.commitTransaction();
        PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
        NotificationArchivalSourceDAOFactory.closeConnection();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.commitTransaction();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.closeConnection();
    }

    @Test
    public void testArchiveOldNotifications_configListEmpty() throws Exception {
        int tenantId = 1;
        NotificationConfigurationList emptyConfigList = new NotificationConfigurationList();
        emptyConfigList.setNotificationConfigurations(Collections.emptyList());
        emptyConfigList.setDefaultArchiveAfter(Constants.DEFAULT_ARCHIVE_PERIOD);
        emptyConfigList.setDefaultArchiveType(Constants.DEFAULT_ARCHIVE_TYPE);
        PowerMockito.when(NotificationHelper.getNotificationConfigurationsFromMetadata())
                .thenReturn(emptyConfigList);
        PowerMockito.doCallRealMethod().when(NotificationHelper.class,
                "setDefaultArchivalValuesIfAbsent", any(NotificationConfigurationList.class));
        when(archivalDAOMock.moveNotificationsToArchiveExcludingConfigs(any(Timestamp.class), eq(tenantId), anySet()))
                .thenReturn(Collections.singletonList(300));
        doNothing().when(archivalDAOMock).moveUserActionsToArchive(anyList());
        service.archiveOldNotifications(tenantId);
        verify(archivalDAOMock, never()).moveNotificationsToArchiveByConfig(any(), anyInt(), anyInt());
        verify(archivalDAOMock, never()).deleteOldNotificationsByConfig(any(), anyInt(), anyInt());
        verify(archivalDAOMock, times(1))
                .moveNotificationsToArchiveExcludingConfigs(any(Timestamp.class), eq(tenantId),
                        eq(Collections.emptySet()));
        verify(archivalDAOMock, times(1))
                .moveUserActionsToArchive(Collections.singletonList(300));
        verify(mockLog).info(contains("Archiving default-config notifications older than"));
        PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
        NotificationArchivalSourceDAOFactory.commitTransaction();
        PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
        NotificationArchivalSourceDAOFactory.closeConnection();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.commitTransaction();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.closeConnection();
    }


    @Test(expectedExceptions = NotificationArchivalException.class)
    public void testArchiveOldNotifications_exceptionRollback() throws Exception {
        int tenantId = 1;
        when(archivalDAOMock.moveNotificationsToArchiveByConfig(any(Timestamp.class), eq(tenantId), eq(1)))
                .thenThrow(new RuntimeException("DB error during move"));
        try {
            service.archiveOldNotifications(tenantId);
        } finally {
            PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
            NotificationArchivalSourceDAOFactory.beginTransaction();
            PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
            NotificationArchivalSourceDAOFactory.rollbackTransaction();
            PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
            NotificationArchivalSourceDAOFactory.closeConnection();
            PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
            NotificationArchivalDestDAOFactory.beginTransaction();
            PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
            NotificationArchivalDestDAOFactory.rollbackTransaction();
            PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
            NotificationArchivalDestDAOFactory.closeConnection();
            verify(mockLog).error(eq("Error during dynamic archival"), any(RuntimeException.class));
        }
    }

    @Test
    public void testDeleteExpiredArchivedNotifications_success() throws Exception {
        int tenantId = 1;
        doNothing().when(archivalDAOMock).deleteExpiredArchivedNotifications(any(Timestamp.class), eq(tenantId));
        service.deleteExpiredArchivedNotifications(tenantId);
        verify(archivalDAOMock, times(1))
                .deleteExpiredArchivedNotifications(any(Timestamp.class), eq(tenantId));
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.beginTransaction();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.commitTransaction();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.closeConnection();
        verify(mockLog, times(1))
                .info(contains("Deleting archived notifications older than"));
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationArchivalException.class)
    public void testDeleteExpiredArchivedNotifications_error() throws Exception {
        int tenantId = 1;
        doThrow(new RuntimeException("Delete DB error")).when(archivalDAOMock)
                .deleteExpiredArchivedNotifications(any(Timestamp.class), eq(tenantId));
        try {
            service.deleteExpiredArchivedNotifications(tenantId);
        } finally {
            PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
            NotificationArchivalDestDAOFactory.beginTransaction();
            PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
            NotificationArchivalDestDAOFactory.rollbackTransaction();
            PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
            NotificationArchivalDestDAOFactory.closeConnection();
            verify(mockLog).error(eq("Error deleting expired archived notifications"),
                    any(RuntimeException.class));
        }
    }

    @Test
    public void testArchiveOldNotifications_invalidArchiveAfter_fallback() throws Exception {
        int tenantId = 1;
        NotificationConfigurationList configList = new NotificationConfigurationList();
        configList.setDefaultArchiveAfter("6 days");
        configList.setDefaultArchiveType(Constants.DEFAULT_ARCHIVE_TYPE);
        NotificationConfig config = new NotificationConfig();
        config.setId(1);
        NotificationConfigurationSettings settings = new NotificationConfigurationSettings();
        settings.setArchiveType(Constants.DEFAULT_ARCHIVE_TYPE);
        settings.setArchiveAfter("invalid duration");
        config.setNotificationSettings(settings);
        configList.setNotificationConfigurations(Collections.singletonList(config));
        PowerMockito.when(NotificationHelper.getNotificationConfigurationsFromMetadata()).thenReturn(configList);
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq("invalid duration")))
                .thenThrow(new IllegalArgumentException("Invalid format"));
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq("6 days")))
                .thenReturn(new Timestamp(System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000)));
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq(Constants.DEFAULT_ARCHIVE_PERIOD)))
                .thenReturn(new Timestamp(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)));
        when(archivalDAOMock.moveNotificationsToArchiveByConfig(any(Timestamp.class), eq(tenantId), eq(1)))
                .thenReturn(Arrays.asList(100));
        doNothing().when(archivalDAOMock).moveUserActionsToArchive(anyList());
        when(archivalDAOMock.deleteOldNotificationsByConfig(any(Timestamp.class), eq(tenantId), eq(1)))
                .thenReturn(0);
        when(archivalDAOMock.moveNotificationsToArchiveExcludingConfigs(any(Timestamp.class), eq(tenantId), anySet()))
                .thenReturn(Collections.emptyList());
        service.archiveOldNotifications(tenantId);
        verify(mockLog).warn(contains("Invalid archiveAfter 'invalid duration' for config ID 1. Using fallback."));
        verify(mockLog).info(contains("Archiving notifications for config ID: 1 older than:"));
        verify(archivalDAOMock).moveNotificationsToArchiveByConfig(any(Timestamp.class), eq(tenantId), eq(1));
        verify(archivalDAOMock).moveUserActionsToArchive(Arrays.asList(100));
        verify(archivalDAOMock).deleteOldNotificationsByConfig(any(Timestamp.class), eq(tenantId), eq(1));
        verify(archivalDAOMock, times(1))
                .moveNotificationsToArchiveExcludingConfigs(any(Timestamp.class), eq(tenantId), anySet());
    }

    @Test
    public void testArchiveOldNotifications_defaultArchiveTypeNotDefault() throws Exception {
        int tenantId = 1;
        NotificationConfigurationList configList = new NotificationConfigurationList();
        configList.setDefaultArchiveAfter("6 days");
        configList.setDefaultArchiveType("delete");
        NotificationConfig config = new NotificationConfig();
        config.setId(1);
        NotificationConfigurationSettings settings = new NotificationConfigurationSettings();
        settings.setArchiveType("delete");
        settings.setArchiveAfter("7 days");
        config.setNotificationSettings(settings);
        configList.setNotificationConfigurations(Collections.singletonList(config));
        PowerMockito.when(NotificationHelper.getNotificationConfigurationsFromMetadata()).thenReturn(configList);
        service.archiveOldNotifications(tenantId);
        verify(archivalDAOMock, never()).moveNotificationsToArchiveByConfig(any(), anyInt(), anyInt());
        verify(archivalDAOMock, never()).deleteOldNotificationsByConfig(any(), anyInt(), anyInt());
        verify(archivalDAOMock, never()).moveNotificationsToArchiveExcludingConfigs(any(), anyInt(), anySet());
        PowerMockito.verifyStatic(NotificationArchivalSourceDAOFactory.class, times(1));
        NotificationArchivalSourceDAOFactory.commitTransaction();
        PowerMockito.verifyStatic(NotificationArchivalDestDAOFactory.class, times(1));
        NotificationArchivalDestDAOFactory.commitTransaction();
    }

    @Test
    public void testArchiveOldNotifications_defaultConfigInvalidArchiveAfter_fallbackToConstants() throws Exception {
        int tenantId = 1;
        NotificationConfigurationList configList = new NotificationConfigurationList();
        configList.setDefaultArchiveAfter("invalid default duration");
        configList.setDefaultArchiveType(Constants.DEFAULT_ARCHIVE_TYPE);
        configList.setNotificationConfigurations(Collections.emptyList());
        PowerMockito.when(NotificationHelper.getNotificationConfigurationsFromMetadata()).thenReturn(configList);
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq("invalid default duration")))
                .thenThrow(new IllegalArgumentException("Invalid format"));
        PowerMockito.when(NotificationHelper.resolveCutoffTimestamp(eq(Constants.DEFAULT_ARCHIVE_PERIOD)))
                .thenReturn(new Timestamp(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)));
        when(archivalDAOMock.moveNotificationsToArchiveExcludingConfigs(any(Timestamp.class), eq(tenantId), anySet()))
                .thenReturn(Arrays.asList(400, 500));
        doNothing().when(archivalDAOMock).moveUserActionsToArchive(anyList());
        service.archiveOldNotifications(tenantId);
        verify(mockLog, times(1))
                .warn(contains("Invalid defaultArchiveAfter 'invalid default duration'. Falling back to DB."));
        verify(mockLog, times(1))
                .info(contains("Archiving default-config notifications older than"));
        verify(archivalDAOMock).moveNotificationsToArchiveExcludingConfigs(any(Timestamp.class),
                eq(tenantId), eq(Collections.emptySet()));
        verify(archivalDAOMock).moveUserActionsToArchive(Arrays.asList(400, 500));
    }
}
