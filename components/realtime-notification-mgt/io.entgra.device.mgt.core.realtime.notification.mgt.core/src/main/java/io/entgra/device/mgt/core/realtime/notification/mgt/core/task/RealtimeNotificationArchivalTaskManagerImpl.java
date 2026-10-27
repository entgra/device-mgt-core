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

package io.entgra.device.mgt.core.realtime.notification.mgt.core.task;

import io.entgra.device.mgt.core.realtime.notification.mgt.core.exception.RealtimeNotificationArchivalTaskManagerException;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.internal.RealtimeNotificationManagementDataHolder;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.HashMap;
import java.util.Map;

public class RealtimeNotificationArchivalTaskManagerImpl implements RealtimeNotificationArchivalTaskManager {
    private static final Log log = LogFactory.getLog(RealtimeNotificationArchivalTaskManagerImpl.class);

    @Override
    public void startTask() throws RealtimeNotificationArchivalTaskManagerException {
        try {
            TaskService ts = RealtimeNotificationManagementDataHolder.getInstance().getTaskService();
            if (ts == null) {
                throw new IllegalStateException("Task service is not initialized");
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String taskNameWithTenant = RealtimeConstants.NOTIFICATION_ARCHIVAL_TASK_NAME + "_" + tenantId;
            ts.registerTaskType(RealtimeConstants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
            TaskManager taskManager = ts.getTaskManager(RealtimeConstants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
            if (!taskManager.isTaskScheduled(taskNameWithTenant)) {
                Map<String, String> properties = new HashMap<>();
                properties.put("tenantId", String.valueOf(tenantId));
                TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                triggerInfo.setCronExpression(RealtimeConstants.CRON_EXPRESSION);
                TaskInfo taskInfo = new TaskInfo(taskNameWithTenant,
                        "io.entgra.device.mgt.core.realtime.notification.mgt.core.task.RealtimeNotificationArchivalTask",
                        properties,
                        triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
                log.info("RealtimeNotification archival task scheduled successfully for tenant " + tenantId + " with name: "
                        + taskNameWithTenant);
            } else {
                throw new RealtimeNotificationArchivalTaskManagerException(
                        "RealtimeNotification archival task is already active for tenant " + tenantId);
            }
        } catch (TaskException e) {
            String msg = "Error occurred while scheduling task for notification archival";
            log.error(msg, e);
            throw new RealtimeNotificationArchivalTaskManagerException(msg, e);
        }
    }

    @Override
    public void stopTask() throws RealtimeNotificationArchivalTaskManagerException {
        try {
            TaskService taskService = RealtimeNotificationManagementDataHolder.getInstance().getTaskService();
            if (taskService != null && taskService.isServerInit()) {
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                String taskNameWithTenant = RealtimeConstants.NOTIFICATION_ARCHIVAL_TASK_NAME + "_" + tenantId;
                TaskManager taskManager = taskService.getTaskManager(RealtimeConstants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
                if (taskManager.isTaskScheduled(taskNameWithTenant)) {
                    taskManager.deleteTask(taskNameWithTenant);
                    log.info("RealtimeNotification archival task stopped successfully for tenant " + tenantId
                            + " with name: " + taskNameWithTenant);
                } else {
                    log.info("RealtimeNotification archival task is not currently scheduled for tenant " + tenantId + ".");
                }
            }
        } catch (TaskException e) {
            String msg = "Error occurred while stopping the " + RealtimeConstants.NOTIFICATION_ARCHIVAL_TASK_NAME;
            log.error(msg, e);
            throw new RealtimeNotificationArchivalTaskManagerException(msg, e);
        }
    }
}