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
package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.common.TestDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DeviceTypeMetaDataProviderServiceTests extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceTypeMetaDataProviderServiceTests.class);
    private DeviceTypeMetaDataManagementProviderServiceImpl metaDataProviderService;

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDataSource();
        metaDataProviderService = new DeviceTypeMetaDataManagementProviderServiceImpl();

        try {
            DeviceManagementDAOFactory.beginTransaction();
            // Insert into DM_DEVICE_TYPE
            executeUpdate("INSERT INTO DM_DEVICE_TYPE " +
                    "(ID, NAME, DEVICE_TYPE_META, LAST_UPDATED_TIMESTAMP, PROVIDER_TENANT_ID, SHARED_WITH_ALL_TENANTS) " +
                    "VALUES (2, 'temperature_sensor', NULL, CURRENT_TIMESTAMP, " + TestDataHolder.ALTERNATE_TENANT_ID + ", FALSE)");
            DeviceManagementDAOFactory.commitTransaction();
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        try {
            DeviceManagementDAOFactory.beginTransaction();
            // Insert metadata
            executeUpdate("INSERT INTO DM_DEVICE_TYPE_META " +
                    "(ID, DEVICE_TYPE_ID, META_KEY, META_VALUE, LAST_UPDATED_TIMESTAMP, TENANT_ID) " +
                    "VALUES (2, 2, 'sampling_rate', '10s', " + System.currentTimeMillis() + ", " + TestDataHolder.ALTERNATE_TENANT_ID + ")");
            DeviceManagementDAOFactory.commitTransaction();
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test
    public void testGetMetaValue() throws Exception {
        String deviceType = "temperature_sensor";
        String metaKey = "sampling_rate";
        Optional<String> metaValue = Optional.ofNullable(metaDataProviderService.getMetaEntry(deviceType, metaKey));
        assertTrue(metaValue.isPresent(), "Metadata value should be present for existing key");
        assertEquals(metaValue.get(), "10s", "Metadata value should match expected");
    }

    @Test
    public void testIsMetaEntryExist() throws Exception {
        String deviceType = "temperature_sensor";
        boolean exists = metaDataProviderService.isMetaEntryExist(deviceType, "sampling_rate");
        assertTrue(exists, "Meta entry should exist for the provided key");
    }

    @Test
    public void testUpdateMetaValue() throws Exception {
        String deviceType = "temperature_sensor";
        String metaKey = "sampling_rate";
        String newValue = "30s";

        metaDataProviderService.updateMetaEntry(deviceType, metaKey, newValue);

        Optional<String> updatedValue = Optional.ofNullable(metaDataProviderService.getMetaEntry(deviceType, metaKey));
        assertTrue(updatedValue.isPresent(), "Metadata value should be present");
        assertEquals(newValue, updatedValue.get(), "Metadata value should be updated");
    }


    @Test
    public void testDeleteMetaEntry() throws Exception {
        String deviceType = "temperature_sensor";
        String metaKey = "sampling_rate";

        metaDataProviderService.deleteMetaEntry(deviceType, metaKey);

        boolean existsAfterDelete = metaDataProviderService.isMetaEntryExist(deviceType, metaKey);
        assertFalse(existsAfterDelete, "Meta entry should not exist after deletion");
    }
}
