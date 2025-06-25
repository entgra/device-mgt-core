/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;
import io.entgra.device.mgt.core.device.mgt.core.TestUtils;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;

public class DeviceTypeEventProviderServiceTests extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceTypeEventProviderServiceTests.class);
    private DeviceTypeEventManagementProviderServiceImpl deviceTypeEventManagementProviderService;

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDataSource(); // Initialize the database connection
        MetadataManagementService mockMetadataService = mock(MetadataManagementService.class);
        deviceTypeEventManagementProviderService =
                new DeviceTypeEventManagementProviderServiceImpl(mockMetadataService);
    }

    @Test
    public void createDeviceTypeMetaWithEvents_JSONProcessingTest() throws Exception {
        String deviceType = "air_quality";
        List<DeviceTypeEvent> deviceTypeEvents = TestUtils.getDeviceTypeEvents();
        try {
            // Update the event definitions from the database
            deviceTypeEventManagementProviderService.createDeviceTypeMetaWithEvents(deviceType, deviceTypeEvents);
        } catch (DeviceManagementException e) {
            throw new DeviceManagementException("Error occurred while creating event definitions for a device type" + e);
        }
    }

    @Test(dependsOnMethods = {"createDeviceTypeMetaWithEvents_JSONProcessingTest"})
    public void updateDeviceTypeMetaWithEvents_JSONProcessingTest() throws Exception {
        String deviceType = "air_quality";
        List<DeviceTypeEvent> deviceTypeEvents = TestUtils.getDeviceTypeEvents();
        try {
            // Update the event definitions from the database
            deviceTypeEventManagementProviderService.updateDeviceTypeMetaWithEvents(deviceType, deviceTypeEvents);
        } catch (DeviceManagementException e) {
            throw new DeviceManagementException("Error occurred while updating event definitions for a device type" + e);
        }
    }

    @Test(dependsOnMethods = {"createDeviceTypeMetaWithEvents_JSONProcessingTest"})
    public void getDeviceTypeEventDefinitionsTest() throws Exception {
        String deviceType = "air_quality";
        try {
            List<DeviceTypeEvent> response = deviceTypeEventManagementProviderService.getDeviceTypeEventDefinitions(deviceType);
            // Assert
            assertNotNull(response, "Response should not be null");
        } catch (DeviceManagementException e) {
            throw new DeviceManagementException("Error occurred while retrieving the event definitions for a device type" + e);
        }
    }

    @Test(dependsOnMethods = {"createDeviceTypeMetaWithEvents_JSONProcessingTest"})
    public void deleteDeviceTypeEventDefinitionsTest() throws Exception {
        String deviceType = "air_quality";
        try {
            // Delete the event definitions from the database
            deviceTypeEventManagementProviderService.deleteDeviceTypeEventDefinitions(deviceType);
        } catch (DeviceManagementException e) {
            throw new DeviceManagementException("Error occurred while deleting event definitions for a device type" + e);
        }
    }
}
