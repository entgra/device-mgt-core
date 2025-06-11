package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceTypeMetaDataDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import java.sql.SQLException;

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
    public boolean createMetaEntry(String deviceType, String metaKey, String metaValue) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean isCreated = deviceTypeMetaDataDAO.createDeviceTypeMetaEntry(deviceType, tenantId, metaKey, metaValue);
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
    public String getMetaEntry(String deviceType, String metaKey) throws DeviceManagementException {
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
    public boolean updateMetaEntry(String deviceType, String metaKey, String metaValue) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean isUpdated = deviceTypeMetaDataDAO.updateDeviceTypeMetaEntry(deviceType, tenantId, metaKey, metaValue);
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
            String msg = "Error occurred while opening a connection to the data source to check metadata entry existence.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
