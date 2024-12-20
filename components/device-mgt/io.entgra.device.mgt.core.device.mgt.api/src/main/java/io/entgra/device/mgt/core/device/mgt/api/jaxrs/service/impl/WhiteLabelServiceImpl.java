/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.WhiteLabelService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.FileResponse;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.NotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.WhiteLabelTheme;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.WhiteLabelThemeCreateRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This is the service class for metadata management.
 */
@Path("/whitelabel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WhiteLabelServiceImpl implements WhiteLabelService {

    private static final Log log = LogFactory.getLog(WhiteLabelServiceImpl.class);

    @GET
    @Override
    @Path("/{tenantDomain}/favicon")
    public Response getWhiteLabelFavicon(@PathParam("tenantDomain") String tenantDomain) {
        try {
            FileResponse fileResponse = DeviceMgtAPIUtils.getWhiteLabelManagementService().getWhiteLabelFavicon(tenantDomain);
            return sendFileStream(fileResponse);
        } catch (NotFoundException e) {
            String msg = "Favicon white label image cannot be found in the system. Uploading the favicon white label image again might help solve the issue.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting favicon";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/{tenantDomain}/logo")
    public Response getWhiteLabelLogo(@PathParam("tenantDomain") String tenantDomain) {
        try {
            FileResponse fileResponse = DeviceMgtAPIUtils.getWhiteLabelManagementService().getWhiteLabelLogo(tenantDomain);
            return sendFileStream(fileResponse);
        } catch (NotFoundException e) {
            String msg = "Logo white label image cannot be found in the system. Uploading the logo white label image again might help solve the issue.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting logo";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/{tenantDomain}/icon")
    public Response getWhiteLabelLogoIcon(@PathParam("tenantDomain") String tenantDomain) {
        try {
            FileResponse fileResponse = DeviceMgtAPIUtils.getWhiteLabelManagementService().getWhiteLabelLogoIcon(tenantDomain);
            return sendFileStream(fileResponse);
        } catch (NotFoundException e) {
            String msg = "Icon white label image cannot be found in the system. Uploading the icon white label image again might help solve the issue.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting logo";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Override
    public Response updateWhiteLabelTheme(WhiteLabelThemeCreateRequest whiteLabelThemeCreateRequest) {
        RequestValidationUtil.validateWhiteLabelTheme(whiteLabelThemeCreateRequest);
        try {
            WhiteLabelTheme createdWhiteLabelTheme = DeviceMgtAPIUtils.getWhiteLabelManagementService().updateWhiteLabelTheme(whiteLabelThemeCreateRequest);
            return Response.status(Response.Status.CREATED).entity(createdWhiteLabelTheme).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while creating whitelabel for tenant";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    public Response getWhiteLabelTheme() {
        try {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            WhiteLabelTheme whiteLabelTheme = DeviceMgtAPIUtils.getWhiteLabelManagementService().getWhiteLabelTheme(tenantDomain);
            return Response.status(Response.Status.CREATED).entity(whiteLabelTheme).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting whitelabel for tenant";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving tenant details of whitelabel";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Override
    @Path("/reset")
    public Response resetWhiteLabel() {
        try {
            DeviceMgtAPIUtils.getWhiteLabelManagementService().resetToDefaultWhiteLabelTheme();
            return Response.status(Response.Status.CREATED).entity("White label theme deleted successfully.").build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while resetting whitelabel for tenant";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Useful to send file responses
     */
    private Response sendFileStream(FileResponse fileResponse) {
        try (ByteArrayInputStream binaryDuplicate = new ByteArrayInputStream(fileResponse.getFileContent())) {
            Response.ResponseBuilder response = Response
                    .ok(binaryDuplicate, fileResponse.getMimeType());
            response.status(Response.Status.OK);
            response.header("Content-Length", fileResponse.getFileContent().length);
            return response.build();
        } catch (IOException e) {
            String msg = "Error occurred while creating input stream from buffer array. ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
