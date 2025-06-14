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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.common.type.DeviceTypeMetaEntry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceTypeMetaDataManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/device-type/metadata"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Create metadata entry for a device type",
                        description = "Create metadata entry for a device type",
                        key = "dm:device-type:metadata:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/metadata/create"}
                ),
                @Scope(
                        name = "Update metadata entry for a device type",
                        description = "Update metadata entry for a device type",
                        key = "dm:device-type:metadata:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/metadata/update"}
                ),
                @Scope(
                        name = "Check metadata entry existence for a device type",
                        description = "Check metadata entry existence for a device type",
                        key = "dm:device-type:metadata:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/metadata/view"}
                ),
                @Scope(
                        name = "Delete metadata entry for a device type",
                        description = "Delete metadata entry for a device type",
                        key = "dm:device-type:metadata:delete",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/metadata/delete"}
                ),
        }
)
@Path("/device-type/metadata")
@Api(value = "Device Type Metadata Management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceTypeMetaDataManagementService {

    @POST
    @Path("/{type}")
    @ApiOperation(
            value = "Create metadata entry",
            notes = "Creates a new metadata entry for a specific device type.",
            httpMethod = "POST",
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            tags = "Device Type Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:metadata:create")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Successfully created the metadata entry.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the response."),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Timestamp when the resource was last modified.")
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid or missing parameters."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported"),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n Metadata entry already exists."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n An unexpected error occurred while creating metadata.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response createMetaEntry(
            @ApiParam(name = "type", value = "The device type, such as android, ios, and windows.", required = true)
            @PathParam("type") String deviceType,

            @ApiParam(name = "body", value = "DTO containing metadata key and value.", required = true)
            DeviceTypeMetaEntry metadataEntry
    );

    @PUT
    @Path("/{type}")
    @ApiOperation(
            value = "Update metadata entry",
            notes = "Updates an existing metadata entry for a specific device type.",
            httpMethod = "PUT",
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            tags = "Device Type Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:metadata:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "Successfully updated the metadata entry.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the response."),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource." +
                                                    "\n Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Timestamp when the resource was last modified." +
                                                    "\n Used by caches, or in conditional requests.")
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid or missing parameters."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The metadata entry was not found."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n Metadata entry could not be updated due to conflicting states."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. An unexpected error occurred while updating metadata.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response updateMetaEntry(
            @ApiParam(name = "type", value = "The device type, such as android, ios, and windows.", required = true)
            @PathParam("type") String deviceType,

            @ApiParam(name = "body", value = "DTO containing metadata key and value.", required = true)
            DeviceTypeMetaEntry metadataEntry
    );

    @GET
    @Path("/{type}/{metaKey}")
    @ApiOperation(
            value = "Check metadata entry existence",
            notes = "Check if a metadata entry exists for a given device type and meta key.",
            httpMethod = "GET",
            tags = "Device Metadata Management",
            extensions = @Extension(properties = @ExtensionProperty(name = Constants.SCOPE,
                    value = "dm:device-type:metadata:view"))
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Existence check completed successfully.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the response."
                            )
                    }
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. Missing or invalid parameters."
            ),
            @ApiResponse(
                    code = 404,
                    message = "Metadata entry not found for the given device type and meta key."
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal server error occurred while checking metadata entry.",
                    response = ErrorResponse.class
            )
    })
    Response getMetaEntry(
            @ApiParam(name = "type", value = "Device type (e.g., android, ios).", required = true)
            @PathParam("type") String deviceType,

            @ApiParam(name = "metaKey", value = "Metadata key to check for existence.", required = true)
            @PathParam("metaKey") String metaKey
    );

    @GET
    @Path("/{type}")
    @ApiOperation(
            value = "Retrieve all metadata entries for a device type",
            notes = "Fetches all metadata key-value pairs associated with the specified device type.",
            httpMethod = "GET",
            tags = "Device Metadata Management",
            extensions = @Extension(properties = @ExtensionProperty(name = Constants.SCOPE,
                    value = "dm:device-type:metadata:view"))
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Successfully retrieved metadata entries.",
                    response = DeviceTypeMetaEntry.class,
                    responseContainer = "List",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the response."
                            )
                    }
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. Invalid or missing parameters."
            ),
            @ApiResponse(
                    code = 404,
                    message = "Device type not found or no metadata entries available."
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal server error occurred while retrieving metadata entries.",
                    response = ErrorResponse.class
            )
    })
    Response getMetaEntries(
            @ApiParam(name = "type", value = "Device type (e.g., android, ios).", required = true)
            @PathParam("type") String deviceType
    );

    @DELETE
    @Path("/{type}/{metaKey}")
    @ApiOperation(
            value = "Delete metadata entry",
            notes = "Delete a metadata entry for a given device type.",
            httpMethod = "DELETE",
            tags = "Device Metadata Management",
            extensions = @Extension(properties = @ExtensionProperty(name = Constants.SCOPE,
                    value = "dm:device-type:metadata:delete"))
    )
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "Successfully deleted metadata entry.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the response body."
                            ),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource." +
                                            "\n Used by caches, or in conditional requests."
                            ),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified." +
                                            "\n Used by caches, or in conditional requests."
                            )
                    }
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. Invalid or missing parameters."
            ),
            @ApiResponse(
                    code = 404,
                    message = "Metadata entry not found."
            ),
            @ApiResponse(
                    code = 409,
                    message = "Conflict. Metadata entry could not be deleted due to related dependencies."
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal server error occurred while deleting metadata entry.",
                    response = ErrorResponse.class
            )
    })
    Response deleteMetaEntry(
            @ApiParam(name = "type", value = "Device type (e.g., android, ios).", required = true)
            @PathParam("type") String deviceType,

            @ApiParam(name = "metaKey", value = "Metadata key to delete.", required = true)
            @PathParam("metaKey") String metaKey
    );
}
