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
 *
 *
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.StartupOperationConfig;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManager;
import org.wso2.carbon.device.mgt.core.task.TaskConstants;

import java.time.LocalDateTime;
import java.util.Map;

public class DeviceDetailsRetrieverTask extends DynamicPartitionedScheduleTask {

    private static final Log log = LogFactory.getLog(DeviceDetailsRetrieverTask.class);
    private int tenantId;

    private MonitoringOperation monitoringOperation;
    private String deviceType;

    @Override
    public void setProperties(Map<String, String> map) {
        super.setProperties(map);
        deviceType = map.get(TaskConstants.DEVICE_TYPE_KEY);
        monitoringOperation = new Gson().
                fromJson(map.get(TaskConstants.OPERATION_MONITORING.OPERATION_CONF_KEY), MonitoringOperation.class);
        tenantId = Integer.parseInt(map.get(TaskConstants.TENANT_ID_KEY));
    }

    @Override
    public void executeDynamicTask() {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance()
                .getDeviceManagementProvider();
        if (log.isDebugEnabled()) {
            log.debug("Device details retrieving task started to run for tenant " + tenantId);
        }
        if (log.isDebugEnabled()) {
            log.debug("Task is running for " + tenantId + " tenants and the device type is " + deviceType);
        }
        StartupOperationConfig startupOperationConfig = deviceManagementProviderService
                .getStartupOperationConfig(deviceType);
        if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
            this.executeTask(monitoringOperation, startupOperationConfig);
            return;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            this.executeTask(monitoringOperation, startupOperationConfig);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    /**
     * Execute device detail retriever task
     * @param startupOperationConfig which contains startup operations and realted details
     */
    private void executeTask(MonitoringOperation monitoringOperation,
                             StartupOperationConfig startupOperationConfig) {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance()
                .getDeviceManagementProvider();
        try {
            if (!deviceManagementProviderService.isDeviceEnrolled()) {
                if (log.isDebugEnabled()) {
                    log.debug("No devices are enrolled for tenant " + tenantId + ". Hence the task won't be running.");
                }
                return;
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while checking if any device is enrolled for tenant " + tenantId;
            log.error(msg, e);
        }
        DeviceTaskManager deviceTaskManager = new DeviceTaskManagerImpl(deviceType,
                monitoringOperation,
                startupOperationConfig);
        if (log.isDebugEnabled()) {
            log.debug("Device details retrieving task started to run.");
        }
        //pass the configurations also from here, monitoring tasks
        try {
            deviceTaskManager.addOperations(getTaskContext());
        } catch (DeviceMgtTaskException e) {
            log.error("Error occurred while trying to add the operations to " +
                    "device to retrieve device details.", e);
        }
    }

    @Override
    protected void setup() {

    }
}
