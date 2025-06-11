package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.DeviceTypeMetaDataManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class DeviceTypeMetaDataManagementServiceImpl implements DeviceTypeMetaDataManagementService {

    private static final Log log = LogFactory.getLog(DeviceTypeMetaDataManagementServiceImpl.class);

    @POST
    @Path("/{type}")
    @Override
    public Response createMetaEntry(@PathParam("type") String deviceType,
                                    @QueryParam("metaKey") String metaKey,
                                    @QueryParam("metaValue") String metaValue) {

        if (StringUtils.isEmpty(deviceType) || StringUtils.isEmpty(metaKey) || StringUtils.isEmpty(metaValue)) {
            String msg = "Missing required query parameters: deviceType, metaKey, or metaValue.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        try {
            if (DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService()
                    .isMetaEntryExist(deviceType, metaKey)) {
                String msg = String.format("Metadata entry already exists for device type '%s' and key '%s'.",
                        deviceType, metaKey);
                log.warn(msg);
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            }
            if (DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService()
                    .createMetaEntry(deviceType, metaKey, metaValue)) {
                String msg = String.format("Successfully created metadata entry for device type '%s'.", deviceType);
                log.info(msg);
                return Response.status(Response.Status.CREATED).entity(msg).build();
            } else {
                String msg = String.format("Failed to create metadata entry for device type '%s'.", deviceType);
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while creating metadata entry for device type: " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }


    @PUT
    @Path("/{type}")
    @Override
    public Response updateMetaEntry(@PathParam("type") String deviceType,
                                    @QueryParam("metaKey") String metaKey,
                                    @QueryParam("metaValue") String metaValue) {
        if (StringUtils.isEmpty(deviceType) || StringUtils.isEmpty(metaKey) || StringUtils.isEmpty(metaValue)) {
            String msg = "Missing required query parameters: deviceType, metaKey, or metaValue.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        try {
            if (!DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService().isMetaEntryExist(deviceType, metaKey)) {
                String msg = String.format("Metadata entry for device type '%s' and key '%s' does not exist.", deviceType, metaKey);
                log.warn(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            if (DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService().updateMetaEntry(deviceType, metaKey, metaValue)) {
                String msg = String.format("Successfully updated metadata entry for device type '%s'.", deviceType);
                log.info(msg);
                return Response.ok().entity(msg).build();
            } else {
                String msg = String.format("Failed to update metadata entry for device type '%s'.", deviceType);
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating metadata entry for device type: " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/{type}/exist")
    @Override
    public Response isMetaEntryExist(@PathParam("type") String deviceType,
                                     @QueryParam("metaKey") String metaKey) {

        if (StringUtils.isEmpty(deviceType) || StringUtils.isEmpty(metaKey)) {
            String msg = "Missing required query parameters: deviceType or metaKey.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("deviceType", deviceType);
            result.put("metaKey", metaKey);
            result.put("exists", DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService()
                    .isMetaEntryExist(deviceType, metaKey));
            return Response.ok().entity(result).build();
        } catch (DeviceManagementException e) {
            String errMsg = String.format("Error occurred while checking existence of metadata entry for device type '%s'.", deviceType);
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @DELETE
    @Path("/{type}")
    @Override
    public Response deleteMetaEntry(@PathParam("type") String deviceType,
                                    @QueryParam("metaKey") String metaKey) {

        if (StringUtils.isEmpty(deviceType) || StringUtils.isEmpty(metaKey)) {
            String errorMsg = "Missing required query parameters: deviceType or metaKey.";
            log.error(errorMsg);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
        }
        try {
            if (DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService()
                    .deleteMetaEntry(deviceType, metaKey)) {
                String msg = String.format("Successfully deleted metadata entry for device type '%s' and key '%s'."
                        , deviceType, metaKey);
                log.info(msg);
                return Response.ok().entity(msg).build();
            } else {
                String msg = String.format("Metadata entry for device type '%s' and key '%s' was not found."
                        , deviceType, metaKey);
                log.warn(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
        } catch (DeviceManagementException e) {
            String msg = String.format("Error occurred while deleting metadata entry for device type '%s' and key '%s'.",
                    deviceType, metaKey);
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
