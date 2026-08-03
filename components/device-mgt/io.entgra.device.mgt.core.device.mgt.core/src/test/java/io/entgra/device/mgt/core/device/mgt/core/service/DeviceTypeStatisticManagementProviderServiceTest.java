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

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.common.type.statistic.DeviceTypeStatistic;
import io.entgra.device.mgt.core.device.mgt.common.type.MetadataResult;
import org.mockito.Mockito;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class DeviceTypeStatisticManagementProviderServiceTest {

    private DeviceTypeStatisticManagementProviderService statisticService;
    private MetadataManagementService metadataManagementService;
    private static final String DEVICE_TYPE = "TEST_TYPE";

    @BeforeClass
    public void setUp() {
        metadataManagementService = Mockito.mock(MetadataManagementService.class);
        statisticService = new DeviceTypeStatisticManagementProviderServiceImpl(metadataManagementService);
    }

    @Test
    public void testGetDeviceTypeStatisticsDefinitions() throws DeviceManagementException, MetadataManagementException {
        // Prepare mock Metadata and expected DeviceTypeStatistic
        Metadata metadata = new Metadata();
        metadata.setMetaValue("[{\"title\":\"stat1\", \"uri\":\"https://example.com\", \"tableName\":\"event\"}]");

        Mockito.when(metadataManagementService.retrieveMetadata(Mockito.anyString()))
                .thenReturn(metadata);

        MetadataResult<DeviceTypeStatistic> result = statisticService.getDeviceTypeStatisticsDefinitions(DEVICE_TYPE);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isExists(), "Metadata should exist");
        Assert.assertNotNull(result.getDefinitions());
        Assert.assertEquals(result.getDefinitions().size(), 1);
        Assert.assertEquals(result.getDefinitions().get(0).getTitle(), "stat1");
    }

    @Test
    public void testCreateDeviceTypeStatisticsDefinitions() throws DeviceManagementException, MetadataManagementException {
        DeviceTypeStatistic stat = new DeviceTypeStatistic();
        stat.setTitle("stat2");
        stat.setUri("https://example.com");
        stat.setTableName("event");
        Metadata metadata = new Metadata();
        metadata.setMetaValue("[{\"title\":\"stat1\", \"uri\":\"https://example.com\", \"tableName\":\"event\"}]");
        Mockito.when(metadataManagementService.createMetadata(Mockito.any()))
                .thenReturn(metadata);

        boolean result = statisticService.createDeviceTypeStatisticsDefinitions(DEVICE_TYPE, List.of(stat));
        Assert.assertTrue(result);
    }

    @Test
    public void testUpdateDeviceTypeStatisticsDefinitions() throws DeviceManagementException, MetadataManagementException {
        DeviceTypeStatistic stat = new DeviceTypeStatistic();
        stat.setTitle("stat3");
        Metadata metadata = new Metadata();
        Mockito.when(metadataManagementService.updateMetadata(Mockito.any()))
                .thenReturn(metadata);

        boolean result = statisticService.updateDeviceTypeStatisticsDefinitions(DEVICE_TYPE, Arrays.asList(stat));
        Assert.assertTrue(result);
    }
    @Test
    public void testDeleteDeviceTypeStatisticsDefinitions() throws DeviceManagementException, MetadataManagementException {
        try {
            Mockito.when(metadataManagementService.deleteMetadata(Mockito.anyString()))
                    .thenReturn(true);
        } catch (MetadataManagementException e) {
            throw new RuntimeException(e);
        }

        boolean result = statisticService.deleteDeviceTypeStatisticsDefinitions(DEVICE_TYPE);
        Assert.assertTrue(result);
    }
}