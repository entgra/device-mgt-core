/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt;

import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscriptionData;
import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscriptionFilterCriteria;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;

import java.util.List;

public class SubscriptionManagementHelperUtil {
    public static List<DeviceSubscriptionData> getDeviceSubscriptionData(List<DeviceSubscriptionDTO> deviceSubscriptionDTOS,
                                                                         DeviceSubscriptionFilterCriteria deviceSubscriptionFilterCriteria) {
        // todo: filtering
        for (DeviceSubscriptionDTO deviceSubscriptionDTO : deviceSubscriptionDTOS) {

        }
        return null;
    }

    public static String getDeviceSubscriptionStatus(SubscriptionInfo subscriptionInfo) {
        return getDeviceSubscriptionStatus(subscriptionInfo.getDeviceSubscriptionFilterCriteria().
                        getFilteringDeviceSubscriptionStatus(), subscriptionInfo.getDeviceSubscriptionStatus());
    }

    public static String getDeviceSubscriptionStatus(String deviceSubscriptionStatusFilter, String deviceSubscriptionStatus) {
        return (deviceSubscriptionStatusFilter != null && !deviceSubscriptionStatusFilter.isEmpty()) ?
                deviceSubscriptionStatusFilter : deviceSubscriptionStatus;
    }
}
