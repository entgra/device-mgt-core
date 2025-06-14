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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.DeviceTypeMetaDataManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.type.DeviceTypeMetaEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/device-type/metadata")
public class DeviceTypeMetaDataManagementServiceImpl implements DeviceTypeMetaDataManagementService {

    private static final Log log = LogFactory.getLog(DeviceTypeMetaDataManagementServiceImpl.class);

    @POST
    @Path("/{type}")
    @Override
    public Response createMetaEntry(@PathParam("type") String deviceType,
                                    DeviceTypeMetaEntry metaEntry) {
        String metaKey = metaEntry.getMetaKey();
        if (isInvalidMetaRequest(deviceType, metaEntry)) {
            String msg = "Missing required fields: deviceType, metaKey, or metaValue.";
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
                    .createMetaEntry(deviceType, metaEntry)) {
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
                                    DeviceTypeMetaEntry metaEntry) {
        String metaKey = metaEntry.getMetaKey();
        if (isInvalidMetaRequest(deviceType, metaEntry)) {
            String msg = "Missing required fields: deviceType, metaKey, or metaValue.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        try {
            if (!DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService().isMetaEntryExist(deviceType, metaKey)) {
                String msg = String.format("Metadata entry for device type '%s' and key '%s' does not exist."
                        , deviceType, metaKey);
                log.warn(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            if (DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService()
                    .updateMetaEntry(deviceType, metaEntry)) {
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
    @Path("/{type}/{metaKey}")
    @Override
    public Response getMetaEntry(@PathParam("type") String deviceType,
                                 @PathParam("metaKey") String metaKey) {

        if (isInvalidPathParams(deviceType, metaKey)) {
            String msg = "Missing required path parameters: deviceType or metaKey.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        try {
            DeviceTypeMetaEntry metaEntry = DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService()
                    .getMetaEntry(deviceType, metaKey);
            if (metaEntry == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok().entity(metaEntry).build();
        } catch (DeviceManagementException e) {
            String errMsg = String.format("Error occurred while retrieving metadata entry for " +
                    "device type '%s' and key '%s'.", deviceType, metaKey);
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @GET
    @Path("/{type}")
    @Override
    public Response getMetaEntries(@PathParam("type") String deviceType) {
        if (StringUtils.isBlank(deviceType)) {
            String msg = "Missing required path parameter: deviceType.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        try {
            List<DeviceTypeMetaEntry> metaEntries = DeviceMgtAPIUtils.getDeviceTypeMetaDataManagementProviderService()
                    .getMetaEntries(deviceType);
            if (metaEntries == null || metaEntries.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok().entity(metaEntries).build();
        } catch (DeviceManagementException e) {
            String errMsg = String.format("Error occurred while retrieving metadata entries for device type '%s'."
                    , deviceType);
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @DELETE
    @Path("/{type}/{metaKey}")
    @Override
    public Response deleteMetaEntry(@PathParam("type") String deviceType,
                                    @PathParam("metaKey") String metaKey) {

        if (isInvalidPathParams(deviceType, metaKey)) {
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
            String msg = String.format("Error occurred while deleting metadata entry for device type '%s' and key '%s'."
                    , deviceType, metaKey);
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Validates a metadata entry creation or update request.
     *
     * @param deviceType the device type for which the metadata entry is associated.
     * @param metaEntry  the metadata entry containing key and value.
     * @return true if the request is invalid due to missing or blank fields; false otherwise.
     */
    private static boolean isInvalidMetaRequest(String deviceType, DeviceTypeMetaEntry metaEntry) {
        return StringUtils.isBlank(deviceType) ||
                metaEntry == null ||
                StringUtils.isBlank(metaEntry.getMetaKey()) ||
                StringUtils.isBlank(metaEntry.getMetaValue());
    }

    /**
     * Validates required path parameters for metadata operations.
     *
     * @param deviceType the device type provided in the path.
     * @param metaKey    the metadata key provided in the path.
     * @return true if either path parameter is missing or blank; false otherwise.
     */
    private static boolean isInvalidPathParams(String deviceType, String metaKey) {
        return StringUtils.isBlank(deviceType) || StringUtils.isBlank(metaKey);
    }
}
