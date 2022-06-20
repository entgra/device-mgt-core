/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.core.task.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManagerService;
import org.wso2.carbon.device.mgt.core.task.TaskConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceTaskManagerServiceImpl implements DeviceTaskManagerService {

    public static final String TASK_TYPE = "DEVICE_MONITORING";
    private static final String TASK_CLASS = "org.wso2.carbon.device.mgt.core.task.impl.DeviceDetailsRetrieverTask";


//    private DeviceTaskManager deviceTaskManager;

    private static final Log log = LogFactory.getLog(DeviceTaskManagerServiceImpl.class);

    @Override
    public void startTasks(String deviceType, OperationMonitoringTaskConfig defaultOperationMonitoringTaskConfig)
            throws DeviceMgtTaskException {
        log.info("Task adding for " + deviceType);

        try {

            List<Tenant> tenants = getAllTenants();

            if (log.isDebugEnabled()) {
                log.debug("Task is running for " + tenants.size() + " tenants and the device type is " + deviceType);
            }
            for (Tenant tenant : tenants) {
                // Start task for super tenant
                if (tenant.getId() == MultitenantConstants.SUPER_TENANT_ID) {
                    startTask(deviceType, defaultOperationMonitoringTaskConfig);
                    continue;
                }
                // Start task for rest of the tenants
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantId(tenant.getId(), true);
                    startTask(deviceType, defaultOperationMonitoringTaskConfig);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

        } catch (UserStoreException e) {
            log.error("Error occurred while trying to get the available tenants " +
                    "from device manager provider service.", e);
        }
    }

    private List<Tenant> getAllTenants() throws UserStoreException {
        RealmService realmService = DeviceManagementDataHolder.getInstance().
                getRealmService();
        Tenant superTenant = new Tenant();
        superTenant.setId(MultitenantConstants.SUPER_TENANT_ID);
        superTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        superTenant.setAdminName(realmService.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
                .getRealmConfiguration().getAdminUserName());
        superTenant.setActive(true);
        Tenant[] tenants = realmService.getTenantManager().getAllTenants();
        List<Tenant> tenantList = new ArrayList<>(Arrays.asList(tenants));
        tenantList.add(superTenant);
        return tenantList;
    }


    @Override
    public void startTask(String deviceType, OperationMonitoringTaskConfig defaultOperationMonitoringTaskConfig) throws DeviceMgtTaskException {
        try {
            OperationMonitoringTaskConfig operationMonitoringTaskPlatformConfig = DeviceManagementDataHolder.
                    getInstance().getMonitoringOperationTaskConfigManagementService().getMonitoringOperationTaskConfigFromMetaDataDB(deviceType);
            OperationMonitoringTaskConfig operationMonitoringTaskConfig =
                    operationMonitoringTaskPlatformConfig != null ? operationMonitoringTaskPlatformConfig : defaultOperationMonitoringTaskConfig;
            registerTask(deviceType, operationMonitoringTaskConfig);
        } catch (MetadataManagementException e) {
            log.error("Error occurred while retrieving the operation task configuration from metadata db", e);
        }

    }

    @Override
    public void registerTask(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig) throws DeviceMgtTaskException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            if (operationMonitoringTaskConfig == null || !operationMonitoringTaskConfig.hasEnabledOperations()) {
                return;
            }
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            taskService.registerTaskType(TASK_TYPE);

            if (log.isDebugEnabled()) {
                log.debug("Device details retrieving task is started for the tenant id " + tenantId);
                //                log.debug("Device details retrieving task is at frequency of : " + deviceTaskManager
                //                        .getTaskFrequency());
                log.debug(
                        "Device details retrieving task is at frequency of : " + operationMonitoringTaskConfig
                                .getFrequency());
            }

            TaskManager taskManager = taskService.getTaskManager(TASK_TYPE);

            //            triggerInfo.setIntervalMillis(deviceTaskManager.getTaskFrequency());

            String taskName = getTaskName(deviceType, tenantId);
            if (!taskManager.isTaskScheduled(taskName)) {
                TaskInfo taskInfo = constructTaskInfo(operationMonitoringTaskConfig, deviceType, taskName, tenantId);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new DeviceMgtTaskException(
                        "Device details retrieving task is already started for this tenant " + tenantId);
            }
        } catch (TaskException | DeviceMgtTaskException e) {
            throw new DeviceMgtTaskException("Error occurred while creating the task for tenant " + tenantId,
                    e);
        }
    }

    private void stopTaskIfHasEnabledOperations(String deviceType, int tenantId) throws DeviceManagementException {
        OperationMonitoringTaskConfig config = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider().getOperationMonitoringTaskConfig(deviceType);
        if (config.hasEnabledOperations()) {
            String taskName = getTaskName(deviceType, tenantId);
            try {
                TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
                if (taskService != null && taskService.isServerInit()) {
                    TaskManager taskManager = taskService.getTaskManager(TASK_TYPE);
                    taskManager.deleteTask(taskName);
                }
            } catch (TaskException e) {
                log.error("Error occurred while deleting the task: " + taskName);
            }
        }
    }

    @Override
    public void stopTaskIfScheduled(String taskName)
            throws TaskException {
        TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
        if (taskService != null && taskService.isServerInit()) {
            TaskManager taskManager = taskService.getTaskManager(TASK_TYPE);
            if (taskManager.isTaskScheduled(taskName)) {
                taskManager.deleteTask(taskName);
            }
        }

    }

    @Override
    public void stopTasksOfAllTenants(String deviceType) {
        try {
            List<Tenant> tenants = getAllTenants();
            for (Tenant tenant : tenants) {
                if (tenant.getId() == MultitenantConstants.SUPER_TENANT_ID) {
                    stopTaskIfHasEnabledOperations(deviceType, tenant.getId());
                    continue;
                }
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantId(tenant.getId(), true);
                    stopTaskIfHasEnabledOperations(deviceType, tenant.getId());
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }

            }
//            List<TaskInfo> allMonitoringTaskInfo = DeviceManagementDataHolder.getInstance().
//                    getTaskService().getTaskManager(TASK_TYPE).getAllTasks();
//            for (TaskInfo info : allMonitoringTaskInfo) {
//                stopTask(info.getName());
//            }
        } catch (Exception e) {
            log.error("Error occurred while trying to stop device management retrieval tasks");
        }
    }

    @Override
    public void updateTask(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig)
            throws DeviceMgtTaskException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        //        deviceTaskManager = new DeviceTaskManagerImpl();
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            TaskManager taskManager = taskService.getTaskManager(TASK_TYPE);
            String taskName = getTaskName(deviceType, tenantId);
            if (!operationMonitoringTaskConfig.hasEnabledOperations()) {
                stopTaskIfScheduled(taskName);
            } else if (taskManager.isTaskScheduled(taskName)){
                taskManager.deleteTask(taskName);
                TaskInfo taskInfo = constructTaskInfo(operationMonitoringTaskConfig, deviceType, taskName, tenantId);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                registerTask(deviceType, operationMonitoringTaskConfig);
            }


        } catch (TaskException e) {
            throw new DeviceMgtTaskException("Error occurred while updating the task for tenant " + tenantId,
                    e);
        }
    }

    private String getTaskName(String deviceType, int tenantId) {
        return deviceType + tenantId;
    }

    private TaskInfo constructTaskInfo(OperationMonitoringTaskConfig operationMonitoringTaskConfig, String deviceType,
                                       String taskName, int tenantId) {
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
        triggerInfo.setIntervalMillis(operationMonitoringTaskConfig.getFrequency());
        triggerInfo.setRepeatCount(-1);

        Map<String, String> properties = constructTaskProperties(tenantId, deviceType, operationMonitoringTaskConfig);
        return new TaskInfo(taskName, TASK_CLASS, properties, triggerInfo);
    }

    private Map<String, String> constructTaskProperties(int tenantId, String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig) {
        String operationConfJson = new Gson().toJson(operationMonitoringTaskConfig);
        Map<String, String> properties = new HashMap<>();
        properties.put(TaskConstants.TENANT_ID_KEY, String.valueOf(tenantId));
        properties.put(TaskConstants.DEVICE_TYPE_KEY, deviceType);
        properties.put(TaskConstants.OPERATION_MONITORING.OPERATION_CONF_KEY, operationConfJson);
        return properties;
    }
}

