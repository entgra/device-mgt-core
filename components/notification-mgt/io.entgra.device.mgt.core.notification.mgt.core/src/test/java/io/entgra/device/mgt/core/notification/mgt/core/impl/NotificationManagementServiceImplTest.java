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

import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigRecipients;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationSettings;
import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationAction;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationPayload;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationArchivalDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationManagementDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationEventBroker;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationHelper;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Test class for NotificationManagementServiceImpl
 */
@PrepareForTest({
        NotificationManagementDAOFactory.class,
        NotificationArchivalDestDAOFactory.class,
        NotificationArchivalSourceDAOFactory.class,
        NotificationEventBroker.class,
        NotificationHelper.class
})
public class NotificationManagementServiceImplTest extends PowerMockTestCase {

    @Mock
    private NotificationManagementDAO notificationDAOMock;

    @Mock
    private NotificationArchivalDAO notificationArchivalDAOMock;

    @Mock
    private NotificationManagementServiceImpl service;

    @BeforeMethod
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(NotificationManagementDAOFactory.class);
        PowerMockito.mockStatic(NotificationArchivalDestDAOFactory.class);
        PowerMockito.mockStatic(NotificationArchivalSourceDAOFactory.class);
        PowerMockito.mockStatic(NotificationEventBroker.class);
        PowerMockito.mockStatic(NotificationHelper.class);
        when(NotificationManagementDAOFactory
                .getNotificationManagementDAO()).thenReturn(notificationDAOMock);
        when(NotificationArchivalDestDAOFactory
                .getNotificationArchivalDAO()).thenReturn(notificationArchivalDAOMock);
        service = new NotificationManagementServiceImpl(notificationDAOMock, notificationArchivalDAOMock);
    }

    @Test
    public void testGetLatestNotifications_success() throws Exception {
        List<Notification> mockList = Collections.singletonList(new Notification());
        when(notificationDAOMock.getLatestNotifications(0, 10)).thenReturn(mockList);
        List<Notification> result = service.getLatestNotifications(0, 10);
        assertEquals(result, mockList);
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testGetLatestNotifications_exception() throws Exception {
        when(notificationDAOMock.getLatestNotifications(anyInt(), anyInt()))
                .thenThrow(new NotificationManagementException());
        service.getLatestNotifications(0, 5);
    }

    @Test
    public void testGetUserNotificationsWithStatus_success() throws Exception {
        UserNotificationAction action = new UserNotificationAction();
        action.setNotificationId(1);
        action.setRead(false);
        Notification notification = new Notification();
        notification.setNotificationId(1);
        notification.setDescription("desc");
        notification.setType("type");
        notification.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        when(notificationDAOMock.getNotificationActionsByUser("user", 5, 0, false))
                .thenReturn(Collections.singletonList(action));
        when(notificationDAOMock.getNotificationsByIds(Collections.singletonList(1)))
                .thenReturn(Collections.singletonList(notification));
        List<UserNotificationPayload> result =
                service.getUserNotificationsWithStatus("user", 5, 0, false);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getActionType(), "UNREAD");
    }

    @Test
    public void testUpdateNotificationActionForUser_success() throws Exception {
        doNothing().when(notificationDAOMock)
                .updateNotificationAction(anyList(), eq("user"), eq("READ"));
        when(notificationDAOMock.getUnreadNotificationCountForUser("user")).thenReturn(5);
        PowerMockito.doNothing().when(NotificationEventBroker.class);
        NotificationEventBroker.pushMessage(anyString(), anyList());
        service.updateNotificationActionForUser(Arrays.asList(1, 2), "user", "READ");
        verify(notificationDAOMock).updateNotificationAction(anyList(), eq("user"), eq("READ"));
    }

    @Test
    public void testDeleteUserNotifications_success() throws Exception {
        doNothing().when(notificationDAOMock).deleteUserNotifications(anyList(), eq("user"));
        when(notificationDAOMock.getUnreadNotificationCountForUser("user")).thenReturn(3);
        PowerMockito.doNothing().when(NotificationEventBroker.class);
        NotificationEventBroker.pushMessage(anyString(), anyList());
        service.deleteUserNotifications(Arrays.asList(10), "user");
        verify(notificationDAOMock).deleteUserNotifications(anyList(), eq("user"));
    }

    @Test
    public void testArchiveUserNotifications_success() throws Exception {
        doNothing().when(notificationArchivalDAOMock).archiveUserNotifications(anyList(), eq("user"));
        service.archiveUserNotifications(Arrays.asList(1, 2, 3), "user");
        verify(notificationArchivalDAOMock).archiveUserNotifications(anyList(), eq("user"));
    }

    @Test
    public void testDeleteAllUserNotifications_success() throws Exception {
        doNothing().when(notificationDAOMock).deleteAllUserNotifications("user");
        when(notificationDAOMock.getUnreadNotificationCountForUser("user")).thenReturn(0);
        PowerMockito.doNothing().when(NotificationEventBroker.class);
        NotificationEventBroker.pushMessage(anyString(), anyList());
        service.deleteAllUserNotifications("user");
        verify(notificationDAOMock).deleteAllUserNotifications("user");
    }

    @Test
    public void testArchiveAllUserNotifications_success() throws Exception {
        doNothing().when(notificationArchivalDAOMock).archiveAllUserNotifications("user");
        service.archiveAllUserNotifications("user");
        verify(notificationArchivalDAOMock).archiveAllUserNotifications("user");
    }

    @Test
    public void testHandleTaskNotificationIfApplicable_success() throws Exception {
        NotificationConfig config = new NotificationConfig();
        config.setId(1);
        config.setType("INFO");
        NotificationConfigurationSettings settings = new NotificationConfigurationSettings();
        config.setNotificationSettings(settings);
        NotificationConfigRecipients recipients = new NotificationConfigRecipients();
        recipients.setUsers(Collections.singletonList("admin"));
        config.setRecipients(recipients);
        PowerMockito.when(NotificationHelper.getNotificationConfigurationByCode("TASK_CODE"))
                .thenReturn(config);
        PowerMockito.when(NotificationHelper
                        .extractUsernamesFromRecipients(any(NotificationConfigRecipients.class), anyInt()))
                .thenReturn(Collections.singletonList("admin"));
        when(notificationDAOMock.insertNotification(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1);
        doNothing().when(notificationDAOMock).insertNotificationUserActions(anyInt(), anyList());
        when(notificationDAOMock.getUnreadNotificationCountForUser("admin"))
                .thenReturn(1);
        PowerMockito.mockStatic(NotificationEventBroker.class);
        PowerMockito.doNothing().when(NotificationEventBroker.class, "pushMessage", anyString(), anyList());
        service.handleTaskNotificationIfApplicable("TASK_CODE", 1, "Task completed");
        verify(notificationDAOMock)
                .insertNotification(eq(1), eq(1), eq("INFO"), contains("Task completed"));
        verify(notificationDAOMock)
                .insertNotificationUserActions(eq(1), eq(Collections.singletonList("admin")));
        verify(notificationDAOMock).getUnreadNotificationCountForUser("admin");
        PowerMockito.verifyStatic(NotificationEventBroker.class, times(1));
        NotificationEventBroker.pushMessage(anyString(), eq(Collections.singletonList("admin")));
    }
}
