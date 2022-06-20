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

package org.wso2.carbon.device.mgt.core.metadata.mgt;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.common.metadata.mgt.MonitoringOperationTaskConfigManagementService;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import java.sql.SQLException;

/**
 * This class implements the MetadataManagementService.
 */
public class MonitoringOperationTaskConfigManagementServiceImpl implements MonitoringOperationTaskConfigManagementService {

    private static final Log log = LogFactory.getLog(MonitoringOperationTaskConfigManagementServiceImpl.class);

    private final MetadataDAO metadataDAO;

    public MonitoringOperationTaskConfigManagementServiceImpl() {
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
    }

    @Override
    public void addDefaultMonitoringOperationConfigIfNotExist(String deviceType) throws MetadataManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        addDefaultMonitoringOperationConfigIfNotExist(tenantId, deviceType);
    }

    @Override
    public void addDefaultMonitoringOperationConfigIfNotExist(int tenantId, String deviceType) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (!metadataDAO.isExist(tenantId, generateMonitoringOperationConfigMetaKey(deviceType))) {
                DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder
                        .getInstance().getDeviceManagementProvider();
                OperationMonitoringTaskConfig defaultOperationMonitoringConfig = deviceManagementProviderService.getDefaultOperationMonitoringTaskConfig(deviceType);
                Metadata metadata = constructMonitoringOperationConfigMetaData(deviceType, defaultOperationMonitoringConfig);
                metadataDAO.addMetadata(tenantId, metadata);
                if (log.isDebugEnabled()) {
                    log.debug("White label metadata entry has inserted successfully");
                }
            }
            MetadataManagementDAOFactory.commitTransaction();
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while inserting default whitelabel metadata entry.";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }

    }

    @Override
    public OperationMonitoringTaskConfig updateMonitoringOperationTaskConfig(String deviceType,
                                                                             OperationMonitoringTaskConfig operationMonitoringTaskConfig)
            throws MetadataManagementException {
        updateMonitoringOperationTaskConfigMetaData(deviceType, operationMonitoringTaskConfig);
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder
                .getInstance().getDeviceManagementProvider();
        deviceManagementProviderService.handleOperationMonitoringTaskConfigUpdate(deviceType, operationMonitoringTaskConfig);
        return operationMonitoringTaskConfig;
    }

    private void updateMonitoringOperationTaskConfigMetaData(String deviceType,
                                                             OperationMonitoringTaskConfig operationMonitoringTaskConfig)
            throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Creating Metadata : [" + operationMonitoringTaskConfig.toString() + "]");
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            Metadata operationConfigMeta = constructMonitoringOperationConfigMetaData(deviceType, operationMonitoringTaskConfig);
            MetadataManagementDAOFactory.beginTransaction();
            if (!metadataDAO.isExist(tenantId, generateMonitoringOperationConfigMetaKey(deviceType))) {
                metadataDAO.addMetadata(tenantId, operationConfigMeta);
            } else {
                metadataDAO.updateMetadata(tenantId, operationConfigMeta);
            }
            MetadataManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("Metadata entry created successfully. " + operationMonitoringTaskConfig.toString());
            }
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while creating the metadata entry. " + operationMonitoringTaskConfig.toString();
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException("Error occurred while creating metadata record", e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public OperationMonitoringTaskConfig getMonitoringOperationTaskConfig(String deviceType) throws MetadataManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving whitelabel theme for tenant: " + tenantId);
        }
        OperationMonitoringTaskConfig operationMonitoringTaskConfig = getMonitoringOperationTaskConfigFromMetaDataDB(deviceType);
        if (operationMonitoringTaskConfig != null) {
            return operationMonitoringTaskConfig;
        }
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDefaultOperationMonitoringTaskConfig(deviceType);
    }

    @Override
    public OperationMonitoringTaskConfig getMonitoringOperationTaskConfigFromMetaDataDB(String deviceType) throws MetadataManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving whitelabel theme for tenant: " + tenantId);
        }
        try {
            MetadataManagementDAOFactory.openConnection();
            Metadata metadata =  metadataDAO.getMetadata(tenantId, generateMonitoringOperationConfigMetaKey(deviceType));
            if (metadata != null) {
                return new Gson().fromJson(metadata.getMetaValue(), OperationMonitoringTaskConfig.class);
            }
            return null;
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving white label theme for tenant:" + tenantId;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }
    private Metadata constructMonitoringOperationConfigMetaData(String deviceType, OperationMonitoringTaskConfig omtConfig) {
        String omtConfigJson = new Gson().toJson(omtConfig);
        Metadata metadata = new Metadata();
        metadata.setMetaKey(generateMonitoringOperationConfigMetaKey(deviceType));
        metadata.setMetaValue(omtConfigJson);
        return metadata;
    }

    private String generateMonitoringOperationConfigMetaKey(String deviceType) {
        return MetaDataConstants.Key.MONITORING_OPERATION_TASK_CONFIG + deviceType;
    }
}
