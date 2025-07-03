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

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationSettings;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.notification.mgt.core.common.BaseNotificationManagementTest;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import org.mockito.InjectMocks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for NotificationConfigServiceImpl
 */
public class NotificationConfigServiceImplTest extends BaseNotificationManagementTest {

    private static final Log log = LogFactory.getLog(NotificationConfigServiceImplTest.class);

    @InjectMocks
    private NotificationConfigServiceImpl service;

    @Mock
    private MetadataManagementService metaDataService;

    @Captor
    private ArgumentCaptor<Metadata> metadataCaptor;

    private Gson gson = new Gson();

    @BeforeClass
    public void initialize() throws Exception {
        log.info("Initializing feature manager tests");
        super.initializeServices();
        MockitoAnnotations.initMocks(this);
        service = new NotificationConfigServiceImpl();
        try {
            Field field = NotificationConfigServiceImpl.class.getDeclaredField("metaDataService");
            field.setAccessible(true);
            field.set(service, metaDataService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock metadata service", e);
        }
    }

    @Test
    public void testSetDefaultNotificationArchiveMetadata_CreateMetadata() throws Exception {
        when(metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY)).thenReturn(null);
        service.setDefaultNotificationArchiveMetadata("time", "2 weeks");
        verify(metaDataService, atLeastOnce()).createMetadata(metadataCaptor.capture());
        Metadata captured = metadataCaptor.getValue();
        NotificationConfigurationList parsed =
                gson.fromJson(captured.getMetaValue(), NotificationConfigurationList.class);
        assertEquals("time", parsed.getDefaultArchiveType());
        assertEquals("2 weeks", parsed.getDefaultArchiveAfter());
    }

    @Test
    public void testAddNotificationConfigContext_NewEntry() throws Exception {
        when(metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY)).thenReturn(null);
        NotificationConfigurationList list = new NotificationConfigurationList();
        NotificationConfig config = new NotificationConfig();
        config.setCode("example-code");
        config.setNotificationSettings(new NotificationConfigurationSettings());
        list.setNotificationConfigurations(Collections.singletonList(config));
        service.addNotificationConfigContext(list);
        verify(metaDataService).createMetadata(any(Metadata.class));
    }

    @Test(expectedExceptions = NotificationConfigurationServiceException.class)
    public void testAddNotificationConfigContext_ThrowsOnDuplicate() throws Exception {
        NotificationConfig existing = new NotificationConfig();
        existing.setId(1);
        existing.setCode("duplicate");
        NotificationConfigurationList existingList = new NotificationConfigurationList();
        existingList.setNotificationConfigurations(Collections.singletonList(existing));
        Metadata metadata = new Metadata();
        metadata.setMetaValue(gson.toJson(existingList));
        when(metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY)).thenReturn(metadata);
        NotificationConfig newConfig = new NotificationConfig();
        newConfig.setCode("duplicate");
        NotificationConfigurationList input = new NotificationConfigurationList();
        input.setNotificationConfigurations(Collections.singletonList(newConfig));
        service.addNotificationConfigContext(input);
    }

    @Test
    public void testDeleteNotificationConfigContext_Success() throws Exception {
        NotificationConfig config = new NotificationConfig();
        config.setId(1);
        config.setCode("test");
        NotificationConfigurationList list = new NotificationConfigurationList();
        list.setNotificationConfigurations(new ArrayList<>(Collections.singletonList(config)));
        Metadata metadata = new Metadata();
        metadata.setMetaValue(gson.toJson(list));
        when(metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY)).thenReturn(metadata);
        service.deleteNotificationConfigContext(1);
        verify(metaDataService, atLeastOnce()).updateMetadata(any(Metadata.class));
    }

    @Test(expectedExceptions = NotificationConfigurationServiceException.class)
    public void testDeleteNotificationConfigContext_NotFound() throws Exception {
        NotificationConfigurationList list = new NotificationConfigurationList();
        list.setNotificationConfigurations(new ArrayList<>());
        Metadata metadata = new Metadata();
        metadata.setMetaValue(gson.toJson(list));
        when(metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY)).thenReturn(metadata);
        service.deleteNotificationConfigContext(99);
    }

    @Test
    public void testUpdateNotificationConfigContext_Success() throws Exception {
        NotificationConfig config = new NotificationConfig();
        config.setId(5);
        config.setCode("abc");
        NotificationConfigurationList list = new NotificationConfigurationList();
        list.setNotificationConfigurations(new ArrayList<>(Collections.singletonList(config)));
        Metadata metadata = new Metadata();
        metadata.setMetaValue(gson.toJson(list));
        when(metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY)).thenReturn(metadata);
        NotificationConfig updated = new NotificationConfig();
        updated.setId(5);
        updated.setCode("updated");
        service.updateNotificationConfigContext(updated);
        verify(metaDataService, atLeastOnce()).updateMetadata(any(Metadata.class));
    }

    @Test
    public void testGetNotificationConfigurations_Empty() throws Exception {
        when(metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY)).thenReturn(null);
        NotificationConfigurationList result = service.getNotificationConfigurations();
        assertNotNull(result);
        assertTrue(result.getNotificationConfigurations().isEmpty());
    }

    @Test
    public void testGetNotificationConfigByID_Success() throws Exception {
        NotificationConfig config = new NotificationConfig();
        config.setId(10);
        config.setCode("cfg10");
        NotificationConfigurationList list = new NotificationConfigurationList();
        list.setNotificationConfigurations(Collections.singletonList(config));
        Metadata metadata = new Metadata();
        metadata.setMetaValue(gson.toJson(list));
        when(metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY)).thenReturn(metadata);
        NotificationConfig fetched = service.getNotificationConfigByID(10);
        assertNotNull(fetched);
        assertEquals("cfg10", fetched.getCode());
    }

    @Test(expectedExceptions = NotificationConfigurationServiceException.class)
    public void testGetNotificationConfigByID_NotFound() throws Exception {
        NotificationConfigurationList list = new NotificationConfigurationList();
        list.setNotificationConfigurations(new ArrayList<>());
        Metadata metadata = new Metadata();
        metadata.setMetaValue(gson.toJson(list));
        when(metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY)).thenReturn(metadata);
        service.getNotificationConfigByID(100);
    }

    @Test
    public void testDeleteNotificationConfigurations() throws Exception {
        service.deleteNotificationConfigurations();
        verify(metaDataService).deleteMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
    }
}
