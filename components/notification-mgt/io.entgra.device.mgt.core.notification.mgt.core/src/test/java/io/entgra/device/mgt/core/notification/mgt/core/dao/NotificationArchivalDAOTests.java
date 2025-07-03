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

import io.entgra.device.mgt.core.notification.mgt.core.common.BaseNotificationManagementTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Set;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class NotificationArchivalDAOTests extends BaseNotificationManagementTest {

    private static final Log log = LogFactory.getLog(NotificationArchivalDAOTests.class);

    @Mock
    private NotificationArchivalDAO archivalDAO;

    public static final int SUPER_TENANT_ID = -1234;
    public static final int CONFIG_ID = 1;
    private final String testUsername = "test-user";
    private final int dummyNotificationId = 101;

    @BeforeClass
    public void initialize() throws Exception {
        log.info("Initializing archival DAO test with mocks");
        super.initializeServices();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testArchiveUserNotifications() throws Exception {
        doNothing().when(archivalDAO).archiveUserNotifications(List.of(dummyNotificationId), testUsername);
        archivalDAO.archiveUserNotifications(List.of(dummyNotificationId), testUsername);
        verify(archivalDAO, times(1))
                .archiveUserNotifications(List.of(dummyNotificationId), testUsername);
        log.info("Archived user notifications for: " + testUsername);
    }

    @Test
    public void testArchiveAllUserNotifications() throws Exception {
        doNothing().when(archivalDAO).archiveAllUserNotifications(testUsername);
        archivalDAO.archiveAllUserNotifications(testUsername);
        verify(archivalDAO, times(1)).archiveAllUserNotifications(testUsername);
        log.info("Archived all user notifications for: " + testUsername);
    }

    @Test
    public void testMoveNotificationsToArchiveByConfig() throws Exception {
        List<Integer> expectedIds = List.of(dummyNotificationId);
        when(archivalDAO.moveNotificationsToArchiveByConfig(any(), eq(SUPER_TENANT_ID), eq(CONFIG_ID)))
                .thenReturn(expectedIds);
        List<Integer> archived = archivalDAO.moveNotificationsToArchiveByConfig(
                new Timestamp(System.currentTimeMillis()), SUPER_TENANT_ID, CONFIG_ID);
        Assert.assertEquals(archived, expectedIds);
        verify(archivalDAO, times(1))
                .moveNotificationsToArchiveByConfig(any(), eq(SUPER_TENANT_ID), eq(CONFIG_ID));
        log.info("Moved notifications by config.");
    }

    @Test
    public void testMoveNotificationsToArchiveExcludingConfigs() throws Exception {
        Set<Integer> excluded = Set.of(999);
        List<Integer> expectedIds = List.of(dummyNotificationId);
        when(archivalDAO.moveNotificationsToArchiveExcludingConfigs(any(), eq(SUPER_TENANT_ID), eq(excluded)))
                .thenReturn(expectedIds);
        List<Integer> result = archivalDAO.moveNotificationsToArchiveExcludingConfigs(
                new Timestamp(System.currentTimeMillis()), SUPER_TENANT_ID, excluded);
        Assert.assertEquals(result, expectedIds);
        verify(archivalDAO, times(1))
                .moveNotificationsToArchiveExcludingConfigs(any(), eq(SUPER_TENANT_ID), eq(excluded));
        log.info("Moved notifications excluding configs.");
    }

    @Test
    public void testMoveUserActionsToArchive() throws Exception {
        doNothing().when(archivalDAO).moveUserActionsToArchive(List.of(dummyNotificationId));
        archivalDAO.moveUserActionsToArchive(List.of(dummyNotificationId));
        verify(archivalDAO, times(1))
                .moveUserActionsToArchive(List.of(dummyNotificationId));
        log.info("Moved user actions to archive.");
    }

    @Test
    public void testDeleteOldNotificationsByConfig() throws Exception {
        when(archivalDAO.deleteOldNotificationsByConfig(any(), eq(SUPER_TENANT_ID), eq(CONFIG_ID)))
                .thenReturn(1);
        int deleted = archivalDAO.deleteOldNotificationsByConfig(
                new Timestamp(System.currentTimeMillis()), SUPER_TENANT_ID, CONFIG_ID);
        Assert.assertEquals(deleted, 1);
        verify(archivalDAO, times(1))
                .deleteOldNotificationsByConfig(any(), eq(SUPER_TENANT_ID), eq(CONFIG_ID));
        log.info("Deleted old notifications by config.");
    }

    @Test
    public void testDeleteExpiredArchivedNotifications() throws Exception {
        doNothing().when(archivalDAO).deleteExpiredArchivedNotifications(any(), eq(SUPER_TENANT_ID));
        archivalDAO.deleteExpiredArchivedNotifications(new Timestamp(System.currentTimeMillis()), SUPER_TENANT_ID);
        verify(archivalDAO, times(1))
                .deleteExpiredArchivedNotifications(any(), eq(SUPER_TENANT_ID));
        log.info("Deleted expired archived notifications.");
    }
}
