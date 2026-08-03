/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.core.operation.timeout.task.impl;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;

public class OperationTimeoutInfo {

    private DeviceIdentifier deviceIdentifier;
    private Operation operation;
    private Operation.Status originalStatus;
    private long timeoutOccurredAt;

    public OperationTimeoutInfo(DeviceIdentifier deviceIdentifier, Operation operation, Operation.Status originalStatus) {
        this.deviceIdentifier = deviceIdentifier;
        this.operation = operation;
        this.originalStatus = originalStatus;
        this.timeoutOccurredAt  = System.currentTimeMillis();
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Operation.Status getOriginalStatus() {
        return originalStatus;
    }

    public void setOriginalStatus(Operation.Status originalStatus) {
        this.originalStatus = originalStatus;
    }

    public long getTimeoutOccurredAt() {
        return timeoutOccurredAt;
    }

    public void setTimeoutOccurredAt(long timeoutOccurredAt) {
        this.timeoutOccurredAt = timeoutOccurredAt;
    }
}