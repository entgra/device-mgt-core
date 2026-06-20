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

package io.entgra.device.mgt.core.notification.mgt.common.dto;

import java.util.List;

public class PaginatedUserNotificationResponse {
    private List<UserNotificationPayload> notifications;
    private int totalCount;

    public PaginatedUserNotificationResponse(List<UserNotificationPayload> notifications, int totalCount) {
        this.notifications = notifications;
        this.totalCount = totalCount;
    }

    public List<UserNotificationPayload> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<UserNotificationPayload> notifications) {
        this.notifications = notifications;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}

