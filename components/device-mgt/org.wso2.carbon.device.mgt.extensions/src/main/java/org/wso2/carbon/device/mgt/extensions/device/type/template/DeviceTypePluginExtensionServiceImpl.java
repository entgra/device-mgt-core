/*
 *   Copyright (c) 2019, Entgra (Pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 *   Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.extensions.device.type.template;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceTypePluginDAOManager;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypePluginExtensionException;
import org.wso2.carbon.device.mgt.extensions.spi.DeviceTypePluginExtensionService;

import java.util.HashMap;
import java.util.Map;

public class DeviceTypePluginExtensionServiceImpl implements DeviceTypePluginExtensionService {

    private static final Log log = LogFactory.getLog(DeviceTypePluginExtensionServiceImpl.class);

    private static volatile Map<String, DeviceTypePluginDAOManager> pluginDAOManagers = new HashMap<>();

    @Override
    public void addPluginDAOManager(String deviceType, DeviceTypePluginDAOManager pluginDAOManager)
            throws DeviceTypePluginExtensionException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (pluginDAOManager == null) {
            String msg = "Cannot save DeviceTypePluginDAOManager against tenant id " + tenantId
                         + " and device type: " + deviceType + " since DeviceTypePluginDAOManager is null";
            log.error(msg);
            throw new DeviceTypePluginExtensionException(msg);
        }
        if (!pluginDAOManagers.containsKey(tenantId + deviceType)) {
            if (log.isDebugEnabled()) {
                log.debug("Saving DeviceTypePluginDAOManager against tenant id " + tenantId +
                          " and device type: " + deviceType);
            }
            pluginDAOManagers.put(tenantId + deviceType, pluginDAOManager);
        }
    }

    @Override
    public DeviceTypePluginDAOManager getPluginDAOManager(String deviceType) throws DeviceTypePluginExtensionException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (pluginDAOManagers.containsKey(tenantId + deviceType)) {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving DeviceTypePluginDAOManager against tenant id " + tenantId +
                          " and device type: " + deviceType);
            }
            return pluginDAOManagers.get(tenantId + deviceType);
        } else {
            String msg = "DeviceTypePluginDAOManager could not be found against tenant id " + tenantId +
                         " and device type: " + deviceType;
            log.error(msg);
            throw new DeviceTypePluginExtensionException(msg);
        }
    }
}
