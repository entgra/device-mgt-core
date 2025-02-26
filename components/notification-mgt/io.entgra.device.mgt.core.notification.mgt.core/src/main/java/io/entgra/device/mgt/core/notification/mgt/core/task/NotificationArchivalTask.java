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

package io.entgra.device.mgt.core.notification.mgt.core.task;

import io.entgra.device.mgt.core.device.mgt.core.task.impl.DynamicPartitionedScheduleTask;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationArchivalService;
import io.entgra.device.mgt.core.notification.mgt.core.impl.NotificationArchivalServiceImpl;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NotificationArchivalTask extends DynamicPartitionedScheduleTask {
    private static final Log log = LogFactory.getLog(NotificationArchivalTask.class);
    private NotificationArchivalService archivalService;

    @Override
    protected void setup() {
        this.archivalService = new NotificationArchivalServiceImpl();
        log.info("NotificationArchivalTask initialized.");
    }

    @Override
    protected void executeDynamicTask() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantIdStr = getProperty(Constants.TENANT_ID_KEY);
        if (tenantIdStr != null) {
            try {
                tenantId = Integer.parseInt(tenantIdStr);
            } catch (NumberFormatException e) {
                log.error("Invalid tenant ID property: " + tenantIdStr, e);
                return;
            }
        }
        log.info("Executing " + Constants.NOTIFICATION_ARCHIVAL_TASK_NAME + " for tenant: " + tenantId + " at "
                + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        long startTime = System.currentTimeMillis();
        if (tenantId == -1234) {
            try {
                archivalService.archiveOldNotifications(tenantId);
            } catch (NotificationArchivalException e) {
                log.error("Error during notification archival for tenant: " + tenantId, e);
            }
            try {
                archivalService.deleteExpiredArchivedNotifications(tenantId);
            } catch (NotificationArchivalException e) {
                log.error("Error during deletion of old archived notifications for tenant: " + tenantId, e);
            }
        } else {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantId(tenantId, true);
                try {
                    archivalService.archiveOldNotifications(tenantId);
                } catch (NotificationArchivalException e) {
                    log.error("Error during notification archival for tenant: " + tenantId, e);
                }
                try {
                    archivalService.deleteExpiredArchivedNotifications(tenantId);
                } catch (NotificationArchivalException e) {
                    log.error("Error during deletion of old archived notifications for tenant: " + tenantId, e);
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        long endTime = System.currentTimeMillis();
        long difference = endTime - startTime;
        log.info(Constants.NOTIFICATION_ARCHIVAL_TASK_NAME + " completed for tenant: " + tenantId
                + ". Total execution time: "
                + getDurationBreakdown(difference));
    }

    /**
     * Helper method to format duration into a human-readable string.
     * @param millis The duration in milliseconds.
     * @return Formatted duration string.
     */
    private String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        return (days + " Days " + hours + " Hours " + minutes + " Minutes " + seconds + " Seconds");
    }
}