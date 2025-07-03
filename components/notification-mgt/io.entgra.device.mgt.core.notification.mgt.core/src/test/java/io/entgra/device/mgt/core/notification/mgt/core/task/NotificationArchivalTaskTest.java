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

package io.entgra.device.mgt.core.notification.mgt.core.task;

import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationArchivalService;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.lang.reflect.Field;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

public class NotificationArchivalTaskTest {

    private NotificationArchivalTask task;
    private NotificationArchivalService mockService;

    @BeforeMethod
    public void setUp() throws Exception {
        task = new NotificationArchivalTask();
        mockService = mock(NotificationArchivalService.class);
        Field field = NotificationArchivalTask.class.getDeclaredField("archivalService");
        field.setAccessible(true);
        field.set(task, mockService);
    }

    @AfterMethod
    public void tearDown() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testExecuteDynamicTask_SuperTenant() throws Exception {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
        task.executeDynamicTask();
        verify(mockService, times(1))
                .archiveOldNotifications(-1234);
        verify(mockService, times(1))
                .deleteExpiredArchivedNotifications(-1234);
    }

    @Test
    public void testExecuteDynamicTask_RegularTenant() throws Exception {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(2);
        Field props = NotificationArchivalTask.class.getSuperclass().getDeclaredField("properties");
        props.setAccessible(true);
        java.util.Map<String, String> fakeProps = new java.util.HashMap<>();
        fakeProps.put(Constants.TENANT_ID_KEY, "2");
        props.set(task, fakeProps);
        task.executeDynamicTask();
        verify(mockService, times(1))
                .archiveOldNotifications(2);
        verify(mockService, times(1))
                .deleteExpiredArchivedNotifications(2);
    }

    @Test
    public void testExecuteDynamicTask_InvalidTenantIdProperty() throws Exception {
        Field props = NotificationArchivalTask.class.getSuperclass().getDeclaredField("properties");
        props.setAccessible(true);
        java.util.Map<String, String> badProps = new java.util.HashMap<>();
        badProps.put(Constants.TENANT_ID_KEY, "invalid123");
        props.set(task, badProps);
        task.executeDynamicTask();
        verify(mockService, never()).archiveOldNotifications(anyInt());
        verify(mockService, never()).deleteExpiredArchivedNotifications(anyInt());
    }

    @Test
    public void testExecuteDynamicTask_WithExceptions() throws Exception {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
        doThrow(new NotificationArchivalException("archive error"))
                .when(mockService).archiveOldNotifications(-1234);
        doThrow(new NotificationArchivalException("delete error"))
                .when(mockService).deleteExpiredArchivedNotifications(-1234);
        task.executeDynamicTask();
        verify(mockService, times(1))
                .archiveOldNotifications(-1234);
        verify(mockService, times(1))
                .deleteExpiredArchivedNotifications(-1234);
    }
}
