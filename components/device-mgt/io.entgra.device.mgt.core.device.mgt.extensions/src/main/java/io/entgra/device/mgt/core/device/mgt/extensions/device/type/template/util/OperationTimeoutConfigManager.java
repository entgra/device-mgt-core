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
package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.util;

import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config.OperationTimeoutEntry;
import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config.OperationTimeoutConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OperationTimeoutConfigManager {

    private static final Log log = LogFactory.getLog(OperationTimeoutConfigManager.class);
    private static final Map<String, OperationTimeoutConfig> timeoutConfigs = new ConcurrentHashMap<>();

    /**
     * Adds or updates the operation timeout configuration for a device type.
     *
     * @param deviceType the device type
     * @param config the operation timeout configuration
     */
    public static void addOperationTimeoutConfig(String deviceType, OperationTimeoutConfig config) {
        if (deviceType != null && config != null) {
            timeoutConfigs.put(deviceType, config);
            if (log.isDebugEnabled()) {
                log.debug("Operation timeout configuration cached for device type: " + deviceType);
            }
        }
    }

    /**
     * Gets the operation timeout configuration for a specific device type.
     *
     * @param deviceType the device type
     * @return the operation timeout configuration, or null if not found
     */
    public static OperationTimeoutConfig getOperationTimeoutConfig(String deviceType) {
        return timeoutConfigs.get(deviceType);
    }

    /**
     * Gets a specific operation timeout for a device type and operation code.
     *
     * @param deviceType the device type
     * @param operationCode the operation code
     * @return the operation timeout object, or null if not found
     */
    public static OperationTimeoutEntry getOperationTimeout(String deviceType, String operationCode) {
        OperationTimeoutConfig config = timeoutConfigs.get(deviceType);
        if (config != null) {
            return config.getOperationTimeout(operationCode);
        }
        return null;
    }

    /**
     * Gets the timeout value in milliseconds for a specific operation.
     *
     * @param deviceType the device type
     * @param operationCode the operation code
     * @return the timeout in milliseconds, or -1 if not found
     */
    public static long getTimeoutValue(String deviceType, String operationCode) {
        OperationTimeoutEntry timeout = getOperationTimeout(deviceType, operationCode);
        return timeout != null ? timeout.getTimeout() : -1;
    }

    /**
     * Checks if a timeout is configured for a specific operation.
     *
     * @param deviceType the device type
     * @param operationCode the operation code
     * @return true if timeout is configured, false otherwise
     */
    public static boolean hasTimeout(String deviceType, String operationCode) {
        return getOperationTimeout(deviceType, operationCode) != null;
    }

    /**
     * Removes the operation timeout configuration for a device type.
     *
     * @param deviceType the device type
     */
    public static void removeOperationTimeoutConfig(String deviceType) {
        timeoutConfigs.remove(deviceType);
        if (log.isDebugEnabled()) {
            log.debug("Operation timeout configuration removed for device type: " + deviceType);
        }
    }

    /**
     * Clears all cached operation timeout configurations.
     */
    public static void clearCache() {
        timeoutConfigs.clear();
        if (log.isDebugEnabled()) {
            log.debug("All operation timeout configurations cleared from cache");
        }
    }

    /**
     * Gets the initial status for an operation.
     *
     * @param deviceType the device type
     * @param operationCode the operation code
     * @return the initial status, or null if not found
     */
    public static String getInitialStatus(String deviceType, String operationCode) {
        OperationTimeoutEntry timeout = getOperationTimeout(deviceType, operationCode);
        return timeout != null ? timeout.getInitialStatus() : null;
    }

    /**
     * Gets the next status for an operation.
     *
     * @param deviceType the device type
     * @param operationCode the operation code
     * @return the next status, or null if not found
     */
    public static String getNextStatus(String deviceType, String operationCode) {
        OperationTimeoutEntry timeout = getOperationTimeout(deviceType, operationCode);
        return timeout != null ? timeout.getNextStatus() : null;
    }
}