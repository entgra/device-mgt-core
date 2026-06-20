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

package io.entgra.device.mgt.core.notification.mgt.core.util;

import io.entgra.device.mgt.core.notification.mgt.common.beans.ArchivePeriod;

public class Constants {
    public static final String CDM_CONFIG_FILE_NAME = "cdm-config.xml";
    public static final String TENANT_ID_KEY = "TENANT_ID";
    public static final String NOTIFICATION_ARCHIVAL_TASK_NAME = "NOTIFICATION_ARCHIVAL_TASK";
    public static final String NOTIFICATION_ARCHIVAL_TASK_TYPE = "NOTIFICATION_ARCHIVAL_TASK_TYPE";
    public static final String CRON_EXPRESSION = "0 0 2 * * ?";
    public static final String DEFAULT_ARCHIVE_TYPE = "DB";
    public static final String PENDING = "PENDING";
    public static final String NOTIFICATION_CONFIG_META_KEY = "notification-config" ;
    public static final String ADMIN = "admin" ;
    public static final String OPERATION = "operation" ;
    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    public static final int SUPER_TENANT_ID = -1234;

    public static final ArchivePeriod DEFAULT_ARCHIVE_PERIOD = new ArchivePeriod(12, "months");
    public static final ArchivePeriod DEFAULT_ARCHIVE_DELETE_PERIOD = new ArchivePeriod(5, "years");
}
