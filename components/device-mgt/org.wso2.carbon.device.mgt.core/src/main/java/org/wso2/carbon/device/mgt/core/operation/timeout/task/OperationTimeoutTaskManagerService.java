/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.operation.timeout.task;

import org.wso2.carbon.device.mgt.core.config.operation.timeout.OperationTimeout;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskException;

/**
 * This interface defines the methods that should be implemented by the management service of
 * DeviceStatusMonitoringTask.
 */
public interface OperationTimeoutTaskManagerService {

    /**
     * This method will start the task.
     *
     * @param deviceType - DeviceType
     * @param deviceStatusTaskConfig - DeviceStatusTaskConfig
     * @throws DeviceStatusTaskException
     */
    void startTask(DeviceType deviceType, OperationTimeout config)
            throws OperationTimeoutTaskException;

    /**
     * This method will stop the task.
     *
     * @param deviceType - DeviceType
     * @param deviceStatusTaskConfig - DeviceStatusTaskConfig
     * @throws DeviceStatusTaskException
     */
    void stopTask(DeviceType deviceType, OperationTimeout config)
            throws OperationTimeoutTaskException;

    /**
     * This will update the task frequency which it runs.
     *
     * @param deviceType
     * @param deviceStatusTaskConfig - DeviceStatusTaskConfig
     * @throws DeviceStatusTaskException
     */
    void updateTask(DeviceType deviceType, OperationTimeout config)
            throws OperationTimeoutTaskException;

    /**
     * This will check weather the task is scheduled.
     * @param deviceType - Device Type
     * @throws DeviceStatusTaskException
     */
    boolean isTaskScheduled(DeviceType deviceType) throws OperationTimeoutTaskException;
}