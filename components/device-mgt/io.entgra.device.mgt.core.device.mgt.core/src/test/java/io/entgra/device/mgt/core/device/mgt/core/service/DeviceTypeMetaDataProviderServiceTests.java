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

import io.entgra.device.mgt.core.device.mgt.common.type.DeviceTypeMetaEntry;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.common.TestDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for DeviceTypeMetaDataManagementProviderServiceImpl,
 * covering CRUD operations for device type metadata entries.
 */
public class DeviceTypeMetaDataProviderServiceTests extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceTypeMetaDataProviderServiceTests.class);
    private DeviceTypeMetaDataManagementProviderServiceImpl deviceTypeMetaDataProviderService;
    private final String deviceType = "temperature_sensor";

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDataSource();
        deviceTypeMetaDataProviderService = new DeviceTypeMetaDataManagementProviderServiceImpl();
        RealmService realmService = new InMemoryRealmService();
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        realmService.getTenantManager().getSuperTenantDomain();
        DeviceConfigurationManager.getInstance().initConfig();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            // Insert into DM_DEVICE_TYPE
            executeUpdate("INSERT INTO DM_DEVICE_TYPE " +
                    "(ID, NAME, DEVICE_TYPE_META, LAST_UPDATED_TIMESTAMP, " +
                    "PROVIDER_TENANT_ID, SHARED_WITH_ALL_TENANTS) " +
                    "VALUES (2, 'temperature_sensor', NULL, CURRENT_TIMESTAMP, " +
                    TestDataHolder.SUPER_TENANT_ID + ", FALSE)"
            );
            DeviceManagementDAOFactory.commitTransaction();
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        createMetaEntry("sampling_rate", "10s");
        createMetaEntry("unit", "Celsius");
    }

    /**
     * Helper method to create a device type metadata entry with the given key and value.
     * This method asserts that the creation is successful.
     *
     * @param key   the metadata key to create
     * @param value the metadata value to associate with the key
     * @throws Exception if there is an error during metadata creation
     */
    private void createMetaEntry(String key, String value) throws Exception {
        DeviceTypeMetaEntry entry = new DeviceTypeMetaEntry();
        entry.setMetaKey(key);
        entry.setMetaValue(value);
        boolean created = deviceTypeMetaDataProviderService.createMetaEntry(deviceType, entry);
        assertTrue(created, "Failed to create test meta entry for key: " + key);
    }

    @Test(priority = 0, description = "Create a metadata entry and verify it can be retrieved")
    public void testCreateMetaEntries() throws Exception {
        createMetaEntry("battery_life", "100h");
        DeviceTypeMetaEntry created = deviceTypeMetaDataProviderService.
                getMetaEntry(deviceType, "battery_life");
        assertNotNull(created, "Created entry should be retrievable");
        assertEquals(created.getMetaKey(), "battery_life");
        assertEquals(created.getMetaValue(), "100h");
    }

    @Test(priority = 1, description = "Retrieve a metadata entry by key and verify its value")
    public void testGetMetaValue() throws Exception {
        String metaKey = "sampling_rate";
        String expectedValue = "10s";
        DeviceTypeMetaEntry entry = deviceTypeMetaDataProviderService
                .getMetaEntry("temperature_sensor", metaKey);
        assertNotNull(entry, "Metadata entry should not be null for existing key");
        assertEquals(metaKey, entry.getMetaKey(), "Metadata key should match expected");
        assertEquals(expectedValue, entry.getMetaValue(), "Metadata value should match expected");
    }

    @Test(priority = 2, description = "Retrieve all metadata entries for a device type and verify entries exist")
    public void testGetMetaEntries() throws Exception {
        List<DeviceTypeMetaEntry> entries = deviceTypeMetaDataProviderService.getMetaEntries(deviceType);
        assertNotNull(entries, "Metadata entries list should not be null");
        assertFalse(entries.isEmpty(), "Metadata entries list should not be empty");
        assertTrue(entries.size() >= 2, "There should be at least 2 metadata entries");
        boolean hasSamplingRate = entries.stream()
                .anyMatch(e ->
                        "sampling_rate".equals(e.getMetaKey()) && "10s".equals(e.getMetaValue()));
        boolean hasUnit = entries.stream()
                .anyMatch(e ->
                        "unit".equals(e.getMetaKey()) && "Celsius".equals(e.getMetaValue()));
        assertTrue(hasSamplingRate, "List should contain 'sampling_rate' entry");
        assertTrue(hasUnit, "List should contain 'unit' entry");
    }

    @Test(priority = 3, description = "Check if a metadata entry exists by key")
    public void testIsMetaEntryExist() throws Exception {
        assertTrue(deviceTypeMetaDataProviderService.isMetaEntryExist
                        ("temperature_sensor", "sampling_rate"),
                "Meta entry should exist for the provided key");
    }

    @Test(priority = 4, description = "Update an existing metadata entry and verify the updated value")
    public void testUpdateMetaValue() throws Exception {
        String deviceType = "temperature_sensor";
        String metaKey = "sampling_rate";
        String newValue = "30s";
        DeviceTypeMetaEntry metaEntry = new DeviceTypeMetaEntry();
        metaEntry.setMetaKey(metaKey);
        metaEntry.setMetaValue(newValue);
        deviceTypeMetaDataProviderService.updateMetaEntry(deviceType, metaEntry);
        DeviceTypeMetaEntry updatedEntry = deviceTypeMetaDataProviderService.getMetaEntry(deviceType, metaKey);
        assertNotNull(updatedEntry, "Metadata entry should not be null");
        assertEquals(metaKey, updatedEntry.getMetaKey(), "Metadata key should match");
        assertEquals(newValue, updatedEntry.getMetaValue(), "Metadata value should be updated");
    }

    @Test(priority = 5, description = "Delete a metadata entry and verify it no longer exists")
    public void testDeleteMetaEntry() throws Exception {
        String deviceType = "temperature_sensor";
        String metaKey = "sampling_rate";
        deviceTypeMetaDataProviderService.deleteMetaEntry(deviceType, metaKey);
        assertFalse(deviceTypeMetaDataProviderService.isMetaEntryExist(deviceType, metaKey),
                "Meta entry should not exist after deletion");
    }

    @AfterClass
    public void tearDown() throws Exception {
        deviceTypeMetaDataProviderService.deleteMetaEntry(deviceType, "battery_life");
        deviceTypeMetaDataProviderService.deleteMetaEntry(deviceType, "sampling_rate");
        deviceTypeMetaDataProviderService.deleteMetaEntry(deviceType, "unit");
    }
}
