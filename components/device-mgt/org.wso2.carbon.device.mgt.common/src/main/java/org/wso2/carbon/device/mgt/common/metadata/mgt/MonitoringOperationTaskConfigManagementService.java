/*
 *  Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.common.metadata.mgt;

import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;

/**
 * Defines the contract of WhiteLabelManagementService.
 */
public interface MonitoringOperationTaskConfigManagementService {

    /**
     * This method is updates monitoring operation config if exists. Otherwise it creates a new metadata entry
     * for monitoring operation config
     *
     * @throws MetadataManagementException if error while updating/creating monitoring operation config
     */
    OperationMonitoringTaskConfig updateMonitoringOperationTaskConfig(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig)
            throws MetadataManagementException;

    void addDefaultMonitoringOperationConfigIfNotExist(String deviceType) throws MetadataManagementException;

    void addDefaultMonitoringOperationConfigIfNotExist(int tenantId, String deviceType) throws MetadataManagementException;

    /**
     * This method is useful to get monitoring operation config. If it does not exist in metadata
     * table, this returns the default configuration for the provided device type
     *
     * @throws MetadataManagementException if error while getting monitoring operation config
     */
    OperationMonitoringTaskConfig getMonitoringOperationTaskConfig(String deviceType) throws MetadataManagementException;

    /**
     * This method is useful to get monitoring operation config from metadata table.
     * Returns null if it doesn't exist
     *
     * @throws MetadataManagementException if error while getting monitoring operation config
     */
    OperationMonitoringTaskConfig getMonitoringOperationTaskConfigFromMetaDataDB(String deviceType) throws MetadataManagementException;
}
