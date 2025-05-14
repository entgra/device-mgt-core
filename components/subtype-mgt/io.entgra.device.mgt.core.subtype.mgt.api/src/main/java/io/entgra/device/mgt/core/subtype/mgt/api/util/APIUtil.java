/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.subtype.mgt.api.util;

import io.entgra.device.mgt.core.subtype.mgt.spi.DeviceSubTypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class APIUtil {
    private static final Log log = LogFactory.getLog(APIUtil.class);
    private static volatile DeviceSubTypeService deviceSubTypeService;

    private APIUtil() {}

    public static DeviceSubTypeService getSubtypeManagementService() {
        if (deviceSubTypeService == null) {
            synchronized (APIUtil.class) {
                if (deviceSubTypeService == null) {
                    deviceSubTypeService = (DeviceSubTypeService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getOSGiService(DeviceSubTypeService.class, null);
                    if (deviceSubTypeService == null) {
                        String msg = "Failed to get SubtypeManagementService";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return deviceSubTypeService;
    }
}
