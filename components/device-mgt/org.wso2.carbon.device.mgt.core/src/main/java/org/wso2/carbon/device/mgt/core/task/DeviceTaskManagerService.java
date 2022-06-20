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


package org.wso2.carbon.device.mgt.core.task;

import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.ntask.common.TaskException;

public interface DeviceTaskManagerService {

    /**
     * This method will start the task for all the tenants.
     * @param operationMonitoringTaskConfig - OperationMonitoringTaskConfig
     * @throws DeviceMgtTaskException
     */
    void startTasks(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig)
            throws DeviceMgtTaskException;

    /**
     * This method will start the task for the provided tenant id.
     *
     * @param operationMonitoringTaskConfig - OperationMonitoringTaskConfig
     * @throws DeviceMgtTaskException
     */
    void startTask(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig) throws DeviceMgtTaskException;

    void registerTask(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig) throws DeviceMgtTaskException;

    /**
     * This method will stop the task if scheduled.
     * @throws DeviceMgtTaskException
     */
    void stopTaskIfScheduled(String deviceType)
            throws TaskException;

    /**
     * This method will stop the task scheduled for all the tenants
     * @throws DeviceMgtTaskException
     */
    void stopTasksOfAllTenants(String deviceType)
            throws DeviceMgtTaskException;

    /**
     * This will update the task according to the provided operation monitoring task configuration
     * @param operationMonitoringTaskConfig - OperationMonitoringTaskConfig
     * @throws DeviceMgtTaskException
     */
    void updateTask(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig)
            throws DeviceMgtTaskException;


}

