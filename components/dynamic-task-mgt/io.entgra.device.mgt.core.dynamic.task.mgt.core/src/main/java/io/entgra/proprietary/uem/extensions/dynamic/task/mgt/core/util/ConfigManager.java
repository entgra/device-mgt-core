/*
 * Copyright (C) 2018 - 2025 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.core.util;

import com.google.gson.stream.JsonReader;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.DynamicTaskPlatformConfigurations;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskManagementConfigException;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.constant.Constants;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.internal.DynamicTaskManagementExtensionServiceDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class ConfigManager {
    private static final Log log = LogFactory.getLog(ConfigManager.class);
    private volatile DynamicTaskPlatformConfigurations dynamicTaskPlatformConfigurations;

    private ConfigManager() {
    }

    /**
     * Get configurations.
     *
     * @return {@link DynamicTaskPlatformConfigurations}
     * @throws DynamicTaskManagementConfigException Throws when failed to retrieve configurations.
     */
    public static DynamicTaskPlatformConfigurations getConfigurations() throws DynamicTaskManagementConfigException {
        try {
            ReferenceHolder.INSTANCE.init();
            return ReferenceHolder.INSTANCE.dynamicTaskPlatformConfigurations;
        } catch (FileNotFoundException e) {
            String msg = "Configuration file cannot be located in path [" + Constants.CONFIG_PATH + "].";
            log.error(msg, e);
            throw new DynamicTaskManagementConfigException(msg, e);
        }
    }

    /**
     * Initialize configurations located int repository/conf/dynamic-tasks.json
     *
     * @throws FileNotFoundException Throws if the configuration file is don't exist.
     */
    synchronized private void init() throws FileNotFoundException {
        if (dynamicTaskPlatformConfigurations == null) {
            final JsonReader jsonReader = new JsonReader(new FileReader(Constants.CONFIG_PATH));
            dynamicTaskPlatformConfigurations =
                    DynamicTaskManagementExtensionServiceDataHolder.getGson().fromJson(jsonReader,
                            DynamicTaskPlatformConfigurations.class);
            DynamicTaskManagementUtil.populateConfigurableDeviceTypes(dynamicTaskPlatformConfigurations);
        }
    }

    private static class ReferenceHolder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }
}
