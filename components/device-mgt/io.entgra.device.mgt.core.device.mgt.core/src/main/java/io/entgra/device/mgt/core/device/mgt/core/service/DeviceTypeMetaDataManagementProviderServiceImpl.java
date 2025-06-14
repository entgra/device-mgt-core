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

package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.type.DeviceTypeMetaEntry;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceTypeMetaDataDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Service implementation for managing device type metadata.
 */
public class DeviceTypeMetaDataManagementProviderServiceImpl implements DeviceTypeMetaDataManagementProviderService {

    private static final Log log = LogFactory.getLog(DeviceTypeMetaDataManagementProviderServiceImpl.class);
    private final DeviceTypeMetaDataDAO deviceTypeMetaDataDAO;

    public DeviceTypeMetaDataManagementProviderServiceImpl() {
        this.deviceTypeMetaDataDAO = DeviceManagementDAOFactory.getDeviceTypeMetaDataDAO();
    }

    @Override
    public boolean createMetaEntry(String deviceType, DeviceTypeMetaEntry metaEntry) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean isCreated = deviceTypeMetaDataDAO.createDeviceTypeMetaEntry(deviceType, tenantId, metaEntry);
            DeviceManagementDAOFactory.commitTransaction();
            return isCreated;
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction to create metadata entry.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while creating metadata entry.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public DeviceTypeMetaEntry getMetaEntry(String deviceType, String metaKey) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceTypeMetaDataDAO.getDeviceTypeMetaEntry(deviceType, tenantId, metaKey);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving metadata entry.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to retrieve metadata entry.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceTypeMetaEntry> getMetaEntries(String deviceType) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceTypeMetaDataDAO.getDeviceTypeMetaEntries(deviceType, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving metadata entries for device type: " + deviceType;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to retrieve metadata entries.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean updateMetaEntry(String deviceType, DeviceTypeMetaEntry metaEntry) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean isUpdated = deviceTypeMetaDataDAO.updateDeviceTypeMetaEntry(deviceType, tenantId, metaEntry);
            DeviceManagementDAOFactory.commitTransaction();
            return isUpdated;
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction to update metadata entry.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating metadata entry.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean deleteMetaEntry(String deviceType, String metaKey) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean isDeleted = deviceTypeMetaDataDAO.deleteDeviceTypeMetaEntry(deviceType, tenantId, metaKey);
            DeviceManagementDAOFactory.commitTransaction();
            return isDeleted;
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction to delete metadata entry.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting metadata entry.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean isMetaEntryExist(String deviceType, String metaKey) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceTypeMetaDataDAO.isDeviceTypeMetaEntryExist(deviceType, tenantId, metaKey);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while checking metadata entry existence.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection " +
                    "to the data source to check metadata entry existence.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
