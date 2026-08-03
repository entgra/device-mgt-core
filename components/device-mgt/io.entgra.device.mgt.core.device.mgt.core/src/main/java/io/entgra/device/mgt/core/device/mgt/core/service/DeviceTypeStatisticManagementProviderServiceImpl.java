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
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.common.type.statistic.DeviceTypeStatistic;
import io.entgra.device.mgt.core.device.mgt.common.type.MetadataResult;
import java.util.List;

import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.DEVICE_STATISTIC_META_KEY_PATTERN;

public class DeviceTypeStatisticManagementProviderServiceImpl implements DeviceTypeStatisticManagementProviderService {

    private final DeviceTypeMetaDefinitionProcessor<DeviceTypeStatistic> processor;

    public DeviceTypeStatisticManagementProviderServiceImpl(MetadataManagementService metadataManagementService) {
        this.processor = new DeviceTypeMetaDefinitionProcessor<>(
                metadataManagementService,
                DEVICE_STATISTIC_META_KEY_PATTERN,
                DeviceTypeStatistic.class
        );
    }

    @Override
    public MetadataResult<DeviceTypeStatistic> getDeviceTypeStatisticsDefinitions(String deviceType)
            throws DeviceManagementException {
        return processor.getDefinitions(deviceType);
    }

    @Override
    public boolean createDeviceTypeStatisticsDefinitions(
            String deviceType, List<DeviceTypeStatistic> deviceTypeStatistics) throws DeviceManagementException {
        return processor.createDefinitions(deviceType, deviceTypeStatistics, null);
    }

    @Override
    public boolean updateDeviceTypeStatisticsDefinitions(
            String deviceType, List<DeviceTypeStatistic> deviceTypeStatistics) throws DeviceManagementException {
        return processor.updateDefinitions(deviceType, deviceTypeStatistics, null);
    }

    @Override
    public boolean deleteDeviceTypeStatisticsDefinitions(String deviceType) throws DeviceManagementException {
        return processor.deleteDefinitions(deviceType);
    }
}