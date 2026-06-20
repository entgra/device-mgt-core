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

package io.entgra.device.mgt.core.notification.mgt.common.service;

import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalException;

public interface NotificationArchivalService {

    /**
     * Archives notifications and their associated user actions that are older than a defined retention period.
     * This method orchestrates the data movement and deletion between main and archive tables.
     *
     * @param tenantId relevant tenant id.
     * @throws NotificationArchivalException If an error occurs during the archival process.
     */
    void archiveOldNotifications(int tenantId) throws NotificationArchivalException;

    /**
     * Deletes archived notifications and their associated user actions from the archive tables
     * (DM_NOTIFICATION_ARCH and DM_NOTIFICATION_USER_ACTION_ARCH) that are older than the
     * configured {@code DEFAULT_ARCHIVE_DELETE_PERIOD}.
     *
     * @param tenantId relevant tenant id.
     * @throws NotificationArchivalException If an error occurs during the archival process.
     */
    void deleteExpiredArchivedNotifications(int tenantId) throws NotificationArchivalException;
}