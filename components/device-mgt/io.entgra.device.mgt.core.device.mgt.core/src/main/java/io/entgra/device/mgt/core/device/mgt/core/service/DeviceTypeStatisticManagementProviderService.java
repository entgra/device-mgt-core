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
import io.entgra.device.mgt.core.device.mgt.common.type.statistic.DeviceTypeStatistic;
import io.entgra.device.mgt.core.device.mgt.common.type.MetadataResult;
import java.util.List;

/**
 * This interface defines the contract for managing statistic definitions associated with a device type.
 * It includes operations to create, update, retrieve, and delete device type statistic metadata.
 */
public interface DeviceTypeStatisticManagementProviderService {

    /**
     * Retrieves statistic metadata and definitions associated with a given device type.
     *
     * @param deviceType The name of the device type.
     * @return A {@link MetadataResult} containing the existence flag and a list of {@link DeviceTypeStatistic} definitions.
     * @throws DeviceManagementException If an error occurs while fetching the definitions.
     */
    MetadataResult<DeviceTypeStatistic> getDeviceTypeStatisticsDefinitions(String deviceType) throws DeviceManagementException;

    /**
     * Persists statistic metadata definitions for the specified device type.
     *
     * @param deviceType The name of the device type.
     * @param deviceTypeStatistics A list of statistic definitions to be associated with the device type.
     * @return {@code true} if the operation is successful, {@code false} otherwise.
     * @throws DeviceManagementException If an error occurs during the creation process.
     */
    boolean createDeviceTypeStatisticsDefinitions(String deviceType, List<DeviceTypeStatistic> deviceTypeStatistics)
            throws DeviceManagementException;

    /**
     * Updates statistic metadata definitions for the specified device type.
     *
     * @param deviceType The name of the device type.
     * @param deviceTypeStatistics A list of updated statistic definitions to be associated with the device type.
     * @return {@code true} if the update is successful, {@code false} otherwise.
     * @throws DeviceManagementException If an error occurs during the update process.
     */
    boolean updateDeviceTypeStatisticsDefinitions(String deviceType, List<DeviceTypeStatistic> deviceTypeStatistics)
            throws DeviceManagementException;

    /**
     * Deletes all statistic definitions associated with the given device type.
     *
     * @param deviceType The name of the device type.
     * @return {@code true} if deletion is successful, {@code false} otherwise.
     * @throws DeviceManagementException If an error occurs during the deletion process.
     */
    boolean deleteDeviceTypeStatisticsDefinitions(String deviceType) throws DeviceManagementException;

}
