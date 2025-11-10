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

package io.entgra.device.mgt.core.device.mgt.core.operation.timeout.task;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.operation.timeout.task.impl.OperationTimeoutInfo;

import java.util.List;

public interface OperationTimeoutCallback {

    /**
     * Called when an operation times out.
     * @param deviceIdentifier the device identifier
     * @param operation the timed-out operation
     * @param originalStatus the operation's status at timeout
     * @throws OperationTimeoutCallbackException if handling fails
     */
    void onOperationTimeout(DeviceIdentifier deviceIdentifier, Operation operation, Operation.Status originalStatus)
            throws OperationTimeoutCallbackException;

    /**
     * Called when multiple operations time out.
     * @param timeoutOperations list of timed-out operations
     * @throws OperationTimeoutCallbackException if handling fails
     */
    void onOperationTimeoutBatch(List<OperationTimeoutInfo> timeoutOperations)
            throws OperationTimeoutCallbackException;
}