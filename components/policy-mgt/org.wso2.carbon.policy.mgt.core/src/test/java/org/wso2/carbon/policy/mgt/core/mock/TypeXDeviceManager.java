/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.policy.mgt.core.mock;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;

import java.util.List;

public class TypeXDeviceManager implements DeviceManager {

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public boolean saveConfiguration(PlatformConfiguration configuration)
            throws DeviceManagementException {
        return false;
    }

    @Override public PlatformConfiguration getConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return false;
    }

    @Override
    public void deleteDevices(List<String> deviceIdentifiers) throws DeviceManagementException {
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status) throws DeviceManagementException {
        return false;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return null;
    }

    @Override public boolean updateDeviceProperties(DeviceIdentifier deviceId, List<Device.Property> list)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceId) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceId, String currentOwner, EnrolmentInfo.Status status)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public License getLicense(String languageCode) throws LicenseManagementException {
        return null;
    }

    @Override
    public void addLicense(License license) throws LicenseManagementException {

    }

    @Override
    public boolean requireDeviceAuthorization() {
        return false;
    }
}
