/*
 *  Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.notification.mgt.api.util;

import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class NotificationManagementApiUtil {
    private static volatile NotificationManagementService notificationManagementService;

    public static NotificationManagementService getNotificationManagementService() {
        if (notificationManagementService == null) {
            synchronized (NotificationManagementApiUtil.class) {
                if (notificationManagementService == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    notificationManagementService = (NotificationManagementService)
                            ctx.getOSGiService(NotificationManagementService.class, null);
                    if (notificationManagementService == null) {
                        throw new IllegalStateException("Notification Management Service is not initialize");
                    }
                }
            }
        }
        return notificationManagementService;
    }

    /**
     * Gets the authenticated username from the thread-local Carbon Context.
     * @return String authenticated username
     */
    public static String getAuthenticatedUser() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        return ctx.getUsername();
    }
}
