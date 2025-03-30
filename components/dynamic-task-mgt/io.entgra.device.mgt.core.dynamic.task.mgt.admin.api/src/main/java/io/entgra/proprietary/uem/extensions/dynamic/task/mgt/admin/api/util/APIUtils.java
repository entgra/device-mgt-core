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

package io.entgra.device.mgt.core.dynamic.task.mgt.admin.api.util;

import io.entgra.device.mgt.core.dynamic.task.mgt.common.DynamicTaskConfigurationManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

// Make sure to use DOUBLE LOCKING mechanism instead of SYNCHRONIZED methods to avoid thread blocking behavior when
// retrieving OSGi services from the bundle context.

public class APIUtils {
    private static final Log log = LogFactory.getLog(APIUtils.class);

    private static volatile DynamicTaskConfigurationManagementService dynamicTaskConfigurationManagementService;

    /**
     * Get {@link DynamicTaskConfigurationManagementService} service from OSGi bundle context.
     *
     * @return {@link DynamicTaskConfigurationManagementService}
     */
    public static DynamicTaskConfigurationManagementService getDynamicTaskConfigurationManagementService() {
        if (dynamicTaskConfigurationManagementService == null) {
            synchronized (APIUtils.class) {
                if (dynamicTaskConfigurationManagementService == null) {
                    dynamicTaskConfigurationManagementService =
                            (DynamicTaskConfigurationManagementService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(DynamicTaskConfigurationManagementService.class, null);
                    if (dynamicTaskConfigurationManagementService == null) {
                        String msg =
                                "Failed to acquire " + DynamicTaskConfigurationManagementService.class.getName() + " " +
                                        "from bundle context";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                    return dynamicTaskConfigurationManagementService;
                }
            }
        }
        return dynamicTaskConfigurationManagementService;
    }
}
