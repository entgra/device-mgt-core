/*
 * Copyright (C) 2018 - 2025 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;

import java.util.List;

public interface DeviceTypeEventManagementProviderService {

    List<DeviceTypeEvent> getDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException;

    String getDeviceTypeEventDefinitionsAsJson(String deviceType) throws DeviceManagementException;

    boolean isDeviceTypeMetaExist(String deviceType) throws DeviceManagementException;

    boolean createDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents) throws DeviceManagementException;

    boolean updateDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementException;

    boolean deleteDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException;
}
