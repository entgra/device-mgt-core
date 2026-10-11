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

package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.dao;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.exception.DeviceTypeMgtPluginException;

import java.util.List;

public interface PluginDAO {

    Device getDevice(String deviceId) throws DeviceTypeMgtPluginException;

    boolean addDevice(Device device) throws DeviceTypeMgtPluginException;

    boolean updateDevice(Device device) throws DeviceTypeMgtPluginException;

    /**
     * Updates only the properties present in {@code device.getProperties()} whose value is
     * non-null, leaving every other column/property untouched. Unlike {@link #updateDevice(Device)},
     * which replaces the full row (and so nulls out any column not present in the given list),
     * this performs a targeted update of just the supplied, non-null properties. A property whose
     * name is not a known, declared property for this device type is ignored.
     *
     * @param device device identifier plus the (possibly partial) properties to write
     * @return true if at least one property was written, false if there was nothing to update
     */
    boolean partialUpdateDevice(Device device) throws DeviceTypeMgtPluginException;

    List<Device> getAllDevices() throws DeviceTypeMgtPluginException;

    boolean deleteDevices(List<String> deviceIdentifiers) throws DeviceTypeMgtPluginException;
}
