package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;


import io.entgra.device.mgt.core.application.mgt.common.DeviceTypes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.DeviceFirmwareModelManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.DeviceFirmwareModel;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceFirmwareModelManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;

public class DeviceFirmwareManagementServiceImpl implements DeviceFirmwareModelManagementService {
    private static final Log logger = LogFactory.getLog(DeviceFirmwareManagementServiceImpl.class);
    @GET
    @Path("/device-types/{deviceType}")
    @Override
    public Response getDeviceFirmwareModelsByDeviceType(@PathParam("deviceType") String deviceType) {
        try {
            List<DeviceFirmwareModel> deviceFirmwareModels = DeviceMgtAPIUtils.getDeviceFirmwareModelManagementService()
                    .getFirmwareModelsByDeviceType(deviceType.toLowerCase());
            return Response.ok(deviceFirmwareModels).build();
        } catch (DeviceFirmwareModelManagementException e) {
            String message = "Error encountered while retrieving device firmware models for device type [" + deviceType + "]";
            logger.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }
}
