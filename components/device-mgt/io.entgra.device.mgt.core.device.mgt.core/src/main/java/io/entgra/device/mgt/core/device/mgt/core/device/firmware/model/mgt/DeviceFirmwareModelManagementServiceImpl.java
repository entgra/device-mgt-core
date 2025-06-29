package io.entgra.device.mgt.core.device.mgt.core.device.firmware.model.mgt;

import io.entgra.device.mgt.core.device.mgt.common.app.mgt.DeviceFirmwareModel;
import io.entgra.device.mgt.core.device.mgt.common.device.firmware.model.mgt.DeviceFirmwareModelManagementService;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceFirmwareModelManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.FirmwareDAO;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class DeviceFirmwareModelManagementServiceImpl implements DeviceFirmwareModelManagementService {
    private static final Log logger = LogFactory.getLog(DeviceFirmwareModelManagementServiceImpl.class);
    private final FirmwareDAO firmwareDAO;

    public DeviceFirmwareModelManagementServiceImpl() {
        firmwareDAO = DeviceManagementDAOFactory.getFirmwareDAO();
    }

    @Override
    public DeviceFirmwareModel createDeviceFirmwareModel(DeviceFirmwareModel deviceFirmwareModel) throws DeviceFirmwareModelManagementException {
        if (deviceFirmwareModel == null) {
            throw new IllegalArgumentException("Device firmware model cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Creating device firmware model [" + deviceFirmwareModel.getFirmwareModelName() + "]");
        }

        DeviceType deviceType;
        try {
            DeviceManagementProviderService deviceManagementProvider = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider();
            deviceType = deviceManagementProvider.getDeviceType(deviceFirmwareModel.getDeviceType());
        } catch (DeviceManagementException e) {
            String msg = "Error while retrieving device type for firmware model [" + deviceFirmwareModel.getFirmwareModelName() + "]";
            logger.error(msg, e);
            throw new DeviceFirmwareModelManagementException(msg, e);
        }

        try {
            DeviceManagementDAOFactory.getConnection();
            return firmwareDAO.addFirmwareModel(deviceFirmwareModel, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), deviceType.getId());
        } catch (SQLException e) {
            String msg = "SQL exception encountered while creating device firmware model [" + deviceFirmwareModel.getFirmwareModelName() + "]";
            logger.error(msg, e);
            throw new DeviceFirmwareModelManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error encountered while creating device firmware model [" + deviceFirmwareModel.getFirmwareModelName() + "]";
            logger.error(msg, e);
            throw new DeviceFirmwareModelManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public DeviceFirmwareModel getDeviceFirmwareModelByFirmwareModelName(String firmwareModelName) throws DeviceFirmwareModelManagementException {
        try {
            DeviceManagementDAOFactory.getConnection();
            return firmwareDAO.getExistingFirmwareModel(firmwareModelName, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (SQLException e) {
            String msg = "SQL exception encountered while getting device firmware model [" + firmwareModelName + "]";
            logger.error(msg, e);
            throw new DeviceFirmwareModelManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error encountered while getting device firmware model [" + firmwareModelName + "]";
            logger.error(msg, e);
            throw new DeviceFirmwareModelManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceFirmwareModel> getFirmwareModelsByDeviceType(String deviceType) throws DeviceFirmwareModelManagementException {
        DeviceType type;
        try {
            DeviceManagementProviderService deviceManagementProvider = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider();
            type = deviceManagementProvider.getDeviceType(deviceType);
        } catch (DeviceManagementException e) {
            String msg = "Error while retrieving device type for device type name [" + deviceType + "]";
            logger.error(msg, e);
            throw new DeviceFirmwareModelManagementException(msg, e);
        }
        if (type == null) {
            String msg = "Device type not found for device type name [" + deviceType + "]";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            return firmwareDAO.getAllFirmwareModelsByDeviceType(type.getId(), tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error encountered while retrieving firmware models for device type [" + deviceType + "]";
            logger.error(msg, e);
            throw new DeviceFirmwareModelManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL exception encountered while retrieving firmware models for device type [" + deviceType + "]";
            logger.error(msg, e);
            throw new DeviceFirmwareModelManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    public boolean addDeviceFirmwareVersion(int deviceId, String firmwareVersion) throws DeviceFirmwareModelManagementException {
        if (deviceId <= 0 || firmwareVersion == null || firmwareVersion.isEmpty()) {
            throw new IllegalArgumentException("Invalid device ID or firmware version");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Adding firmware version [" + firmwareVersion + "] for device ID [" + deviceId + "]");
        }

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceFirmwareModel deviceFirmwareModel = firmwareDAO.getDeviceFirmwareModel(deviceId, tenantId);
            return firmwareDAO.saveFirmwareVersionOfDevice(deviceId, firmwareVersion, deviceFirmwareModel.getFirmwareId() ,tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error encountered while adding firmware version for device ID [" + deviceId + "]";
            logger.error(msg, e);
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceFirmwareModelManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Transaction management error while adding firmware version for device ID [" + deviceId + "]";
            logger.error(msg, e);
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceFirmwareModelManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
