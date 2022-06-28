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

package org.wso2.carbon.device.mgt.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class holds configurations for Operation Monitoring Task.
 */
public class OperationMonitoringTaskConfig {

    private boolean isEnabled;
    private List<MonitoringOperation> monitoringOperation = new ArrayList<>();

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean hasEnabledOperations() {
        boolean hasEnabledOperations = !getEnabledMonitoringOperations().isEmpty();
        return hasEnabledOperations && isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public List<MonitoringOperation> getMonitoringOperation() {
        return monitoringOperation;
    }

    public void setMonitoringOperation(List<MonitoringOperation> monitoringOperation) {
        this.monitoringOperation = monitoringOperation;
    }

    public List<MonitoringOperation> getEnabledMonitoringOperations() {
        return monitoringOperation.stream().filter(MonitoringOperation::isEnabled).collect(Collectors.toList());
    }

}
