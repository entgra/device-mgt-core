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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.StartupOperationConfig;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.DynamicTaskContext;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManager;
import org.wso2.carbon.device.mgt.core.task.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceTaskManagerImpl implements DeviceTaskManager {

    private static final Log log = LogFactory.getLog(DeviceTaskManagerImpl.class);
    private final String deviceType;
    static volatile Map<Integer, Map<String, Map<String, Long>>> map = new HashMap<>();
    private static final Map<Integer, List<String>> startupConfigMap = new HashMap<>();
    private MonitoringOperation monitoringOperation;
    private StartupOperationConfig startupOperationConfig;

    public DeviceTaskManagerImpl(String deviceType,
                                 MonitoringOperation operationMonitoringTaskConfig,
                                 StartupOperationConfig startupOperationConfig) {
        this.deviceType = deviceType;
        this.monitoringOperation = operationMonitoringTaskConfig;
        this.startupOperationConfig = startupOperationConfig;
    }

    public DeviceTaskManagerImpl(String deviceType,
                                 MonitoringOperation monitoringOperation) {
        this.monitoringOperation = monitoringOperation;
        this.deviceType = deviceType;
    }

    public DeviceTaskManagerImpl(String deviceType) {
        this.deviceType = deviceType;
    }

    private List<String> getStartupOperations() {
        if (startupOperationConfig != null) {
            return startupOperationConfig.getStartupOperations();
        }
        return null;
    }

//    @Override
//    public String getTaskImplementedClazz() throws DeviceMgtTaskException {
//        return DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getTaskConfiguration().
//                getTaskClazz();
//    }

    @Override
    public boolean isTaskEnabled() throws DeviceMgtTaskException {
        return monitoringOperation.isEnabled();
    }


    @Override
    public void addOperations(DynamicTaskContext dynamicTaskContext) throws DeviceMgtTaskException {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        //list operations for device type
        boolean isValidOperation = this.isValidOperation();
        if (!isValidOperation) {
            if (log.isDebugEnabled()) {
                log.debug("No operations are available.");
            }
            return;
        }
        CommandOperation operation = new CommandOperation();
        operation.setEnabled(true);
        operation.setType(Operation.Type.COMMAND);
        operation.setCode(monitoringOperation.getTaskName());
        try {
            deviceManagementProviderService.addTaskOperation(deviceType, operation, dynamicTaskContext);
        } catch (OperationManagementException e) {
            throw new DeviceMgtTaskException("Error occurred while adding task operations to devices", e);
        }
    }

    private boolean isValidOperation() throws DeviceMgtTaskException {
        List<String> opNames = new ArrayList<>();
        Long milliseconds = System.currentTimeMillis();
        Map<String, Long> mp = Utils.getTenantedTaskOperationMap(map, deviceType);

        if (!mp.containsKey(monitoringOperation.getTaskName())) {
            opNames.add(monitoringOperation.getTaskName());
            mp.put(monitoringOperation.getTaskName(), milliseconds);
            return true;
        } else {
            Long lastExecutedTime = mp.get(monitoringOperation.getTaskName());
            long evalTime = lastExecutedTime + (monitoringOperation.getFrequency() * monitoringOperation.getRecurrentTimes());
            if (evalTime <= milliseconds) {
                opNames.add(monitoringOperation.getTaskName());
                mp.put(monitoringOperation.getTaskName(), milliseconds);
                return true;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Valid operation names are : " + Arrays.toString(opNames.toArray()));
        }
        return false;
    }

    private void addStartupOperations(List<String> startupOperations, List<DeviceIdentifier> validDeviceIdentifiers
            , DeviceManagementProviderService deviceManagementProviderService) throws DeviceMgtTaskException {
        boolean isStartupConfig = Utils.getIsTenantedStartupConfig(startupConfigMap, deviceType);
        if (isStartupConfig) {
            try {
                Operation operation;
                for (String startupOp : startupOperations) {
                    if ("SERVER_VERSION".equals(startupOp)) {
                        operation = new ProfileOperation();
                        operation.setPayLoad(ServerConfiguration.getInstance().getFirstProperty("Version"));
                    } else {
                        operation = new CommandOperation();
                    }
                    operation.setType(Operation.Type.COMMAND);
                    operation.setEnabled(true);
                    operation.setCode(startupOp);
                    deviceManagementProviderService.addOperation(deviceType, operation, validDeviceIdentifiers);
                }
            } catch (InvalidDeviceException e) {
                throw new DeviceMgtTaskException("Invalid DeviceIdentifiers found.", e);
            } catch (OperationManagementException e) {
                throw new DeviceMgtTaskException("Error occurred while adding the operations to devices", e);
            }
        }
    }

    private List<MonitoringOperation> getOperationListforTask() throws DeviceMgtTaskException, DeviceManagementException {

        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder
                .getInstance().getDeviceManagementProvider();
        return deviceManagementProviderService.getMonitoringOperationList(
                deviceType);//Get task list from each device type
    }

    private List<String> getStartupOperationListForTask() {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance()
                .getDeviceManagementProvider();
        return deviceManagementProviderService.getStartupOperations(deviceType);
    }


    @Override
    public boolean isTaskOperation(String opName) {

        try {
            List<MonitoringOperation> monitoringOperations = this.getOperationListforTask();
            List<String> startupOperations = this.getStartupOperationListForTask();
            for (MonitoringOperation taop : monitoringOperations) {
                if (taop.getTaskName().equalsIgnoreCase(opName)) {
                    return true;
                }
            }
            if (startupOperations != null && !startupOperations.isEmpty()) {
                for (String operation : startupOperations) {
                    if (opName.equalsIgnoreCase(operation)) {
                        return true;
                    }
                }
            }
        } catch (DeviceMgtTaskException | DeviceManagementException e) {
            // ignoring the error, no need to throw, If error occurs, return value will be false.
        }
        return false;
    }

}
