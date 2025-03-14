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

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.MetadataList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.MetadataService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * This is the service class for metadata management.
 */
@Path("/metadata")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MetadataServiceImpl implements MetadataService {

    private static final Log log = LogFactory.getLog(MetadataServiceImpl.class);

    @GET
    @Override
    public Response getAllMetadataEntries(
            @QueryParam("offset") int offset,
            @DefaultValue("5")
            @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        MetadataList metadataList = new MetadataList();
        try {
            MetadataManagementService metadataManagementService = DeviceMgtAPIUtils.getMetadataManagementService();
            PaginationResult result = metadataManagementService.retrieveAllMetadata(request);
            metadataList.setCount(result.getRecordsTotal());
            metadataList.setMetadataList((List<Metadata>) result.getData());
            return Response.status(Response.Status.OK).entity(metadataList).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while retrieving metadata list for given parameters [offset:" +
                    offset + ", limit:" + limit + " ]";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/{metaKey}")
    public Response getMetadataEntry(
            @PathParam("metaKey") String metaKey) {
        Metadata metadata;
        try {
            if (metaKey.contains("-")) {
                metaKey = metaKey.replace('-', '_');
            }
            metadata = DeviceMgtAPIUtils.getMetadataManagementService().retrieveMetadata(metaKey);
            return Response.status(Response.Status.OK).entity(metadata).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting the metadata entry for metaKey:" + metaKey;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    public Response createMetadataEntry(Metadata metadata) {
        RequestValidationUtil.validateMetadata(metadata);
        try {
            Metadata createdMetadata = DeviceMgtAPIUtils.getMetadataManagementService().createMetadata(metadata);
            DeviceManagerUtil.removeBillingCache();
            return Response.status(Response.Status.CREATED).entity(createdMetadata).build();
        } catch (MetadataKeyAlreadyExistsException e) {
            String msg = "Metadata entry metaKey:" + metadata.getMetaKey() + " is already exist.";
            log.error(msg, e);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while creating the metadata entry for metaKey:" + metadata.getMetaKey();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Override
    public Response updateMetadataEntry(Metadata metadata) {
        RequestValidationUtil.validateMetadata(metadata);
        try {
            Metadata updatedMetadata = DeviceMgtAPIUtils.getMetadataManagementService().updateMetadata(metadata);
            DeviceManagerUtil.removeBillingCache();
            return Response.status(Response.Status.OK).entity(updatedMetadata).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while updating the metadata entry for metaKey:" + metadata.getMetaKey();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    @Path("/{metaKey}")
    public Response deleteMetadataEntry(
            @PathParam("metaKey") String metaKey) {
        try {
            if (metaKey.contains("-")) {
                metaKey = metaKey.replace('-', '_');
            }
            DeviceMgtAPIUtils.getMetadataManagementService().deleteMetadata(metaKey);
            return Response.status(Response.Status.OK).entity("Metadata entry is deleted successfully.").build();
        } catch (MetadataKeyNotFoundException e) {
            String msg = "Metadata entry metaKey:" + metaKey + " is not found.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while deleting the metadata entry for metaKey:" + metaKey;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
