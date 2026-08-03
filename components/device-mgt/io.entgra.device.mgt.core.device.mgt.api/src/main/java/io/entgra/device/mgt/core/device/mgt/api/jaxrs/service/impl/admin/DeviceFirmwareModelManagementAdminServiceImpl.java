package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.admin;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.admin.DeviceFirmwareModelManagementAdminService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.DeviceFirmwareModel;
import io.entgra.device.mgt.core.device.mgt.common.device.firmware.model.mgt.DeviceFirmwareModelManagementService;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceFirmwareModelManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;

@Path("/admin/device-firmware-models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceFirmwareModelManagementAdminServiceImpl implements DeviceFirmwareModelManagementAdminService {
    private static final Log logger = LogFactory.getLog(DeviceFirmwareModelManagementAdminServiceImpl.class);

    @POST
    @Override
    public Response createDeviceFirmwareModel(DeviceFirmwareModel deviceFirmwareModel) {
        if (deviceFirmwareModel == null) {
            String msg = "Device firmware model is null";
            logger.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        if (deviceFirmwareModel.getDeviceType() == null) {
            String msg = "Device type is null";
            logger.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        if (deviceFirmwareModel.getFirmwareModelName() == null) {
            String msg = "Firmware model name is null";
            logger.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        DeviceFirmwareModelManagementService deviceFirmwareModelManagementService = DeviceMgtAPIUtils
                .getDeviceFirmwareModelManagementService();
        try {
            for (DeviceFirmwareModel models : deviceFirmwareModelManagementService
                    .getFirmwareModelsByDeviceType(deviceFirmwareModel.getDeviceType())) {
                if (models.getFirmwareModelName().equals(deviceFirmwareModel.getFirmwareModelName())) {
                    String msg = "Device firmware model already exists with name "
                            + deviceFirmwareModel.getFirmwareModelName() + " and device type " + deviceFirmwareModel.getDeviceType();
                    logger.error(msg);
                    return Response.status(Response.Status.CONFLICT).entity(msg).build();
                }
            }

            DeviceFirmwareModel createdDeviceFirmwareModel = DeviceMgtAPIUtils.getDeviceFirmwareModelManagementService()
                    .createDeviceFirmwareModel(deviceFirmwareModel);
            return Response.status(Response.Status.CREATED).entity(createdDeviceFirmwareModel).build();
        } catch (DeviceFirmwareModelManagementException e) {
            String msg = "Failed to create device firmware model: " + e.getMessage();
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
