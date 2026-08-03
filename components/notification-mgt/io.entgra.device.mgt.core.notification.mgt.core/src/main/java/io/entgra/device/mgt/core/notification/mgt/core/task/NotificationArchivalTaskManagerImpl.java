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

import io.entgra.device.mgt.core.notification.mgt.core.exception.NotificationArchivalTaskManagerException;
import io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementDataHolder;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.HashMap;
import java.util.Map;

public class NotificationArchivalTaskManagerImpl implements NotificationArchivalTaskManager {
    private static final Log log = LogFactory.getLog(NotificationArchivalTaskManagerImpl.class);

    @Override
    public void startTask() throws NotificationArchivalTaskManagerException {
        try {
            TaskService ts = NotificationManagementDataHolder.getInstance().getTaskService();
            if (ts == null) {
                throw new IllegalStateException("Task service is not initialized");
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String taskNameWithTenant = Constants.NOTIFICATION_ARCHIVAL_TASK_NAME + "_" + tenantId;
            ts.registerTaskType(Constants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
            TaskManager taskManager = ts.getTaskManager(Constants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
            if (!taskManager.isTaskScheduled(taskNameWithTenant)) {
                Map<String, String> properties = new HashMap<>();
                properties.put("tenantId", String.valueOf(tenantId));
                TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                triggerInfo.setCronExpression(Constants.CRON_EXPRESSION);
                TaskInfo taskInfo = new TaskInfo(taskNameWithTenant,
                        "io.entgra.device.mgt.core.notification.mgt.core.task.NotificationArchivalTask",
                        properties,
                        triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
                log.info("Notification archival task scheduled successfully for tenant " + tenantId + " with name: "
                        + taskNameWithTenant);
            } else {
                throw new NotificationArchivalTaskManagerException(
                        "Notification archival task is already active for tenant " + tenantId);
            }
        } catch (TaskException e) {
            String msg = "Error occurred while scheduling task for notification archival";
            log.error(msg, e);
            throw new NotificationArchivalTaskManagerException(msg, e);
        }
    }

    @Override
    public void stopTask() throws NotificationArchivalTaskManagerException {
        try {
            TaskService taskService = NotificationManagementDataHolder.getInstance().getTaskService();
            if (taskService != null && taskService.isServerInit()) {
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                String taskNameWithTenant = Constants.NOTIFICATION_ARCHIVAL_TASK_NAME + "_" + tenantId;
                TaskManager taskManager = taskService.getTaskManager(Constants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
                if (taskManager.isTaskScheduled(taskNameWithTenant)) {
                    taskManager.deleteTask(taskNameWithTenant);
                    log.info("Notification archival task stopped successfully for tenant " + tenantId
                            + " with name: " + taskNameWithTenant);
                } else {
                    log.info("Notification archival task is not currently scheduled for tenant " + tenantId + ".");
                }
            }
        } catch (TaskException e) {
            String msg = "Error occurred while stopping the " + Constants.NOTIFICATION_ARCHIVAL_TASK_NAME;
            log.error(msg, e);
            throw new NotificationArchivalTaskManagerException(msg, e);
        }
    }
}