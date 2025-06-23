package io.entgra.device.mgt.core.device.mgt.core.device.firmware.model.mgt;

import io.entgra.device.mgt.core.device.mgt.common.app.mgt.DeviceFirmwareModel;
import io.entgra.device.mgt.core.device.mgt.common.device.firmware.model.mgt.DeviceFirmwareModelManagementService;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceFirmwareModelManagementException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.FirmwareDAO;
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

        try {
            DeviceManagementDAOFactory.getConnection();
            return firmwareDAO.addFirmwareModel(deviceFirmwareModel, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
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
    public List<DeviceFirmwareModel> getFirmwareModelsByDeviceType(String deviceType) {
        // todo: add device type to FirmwareModel DAO to support the functionality
        return Collections.emptyList();
    }
}
