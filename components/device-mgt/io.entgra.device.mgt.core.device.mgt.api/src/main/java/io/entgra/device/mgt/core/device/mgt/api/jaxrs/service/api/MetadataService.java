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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.MetadataList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.swagger.annotations.*;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Metadata related REST-API implementation.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Metadata Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceMetadataManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/metadata"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "View metadata records",
                        description = "View metadata records",
                        key = "dm:metadata:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/metadata/view"}
                ),
                @Scope(
                        name = "Create a metadata record",
                        description = "Create a metadata record",
                        key = "dm:metadata:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/metadata/create"}
                ),
                @Scope(
                        name = "Update a metadata record",
                        description = "Updating a specified metadata record",
                        key = "dm:metadata:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/metadata/update"}
                ),
                @Scope(
                        name = "Delete a metadata record",
                        description = "Delete a specified metadata record",
                        key = "dm:metadata:remove",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/metadata/remove"}
                )
        }
)
@Api(value = "Device Metadata Management")
@Path("/metadata")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MetadataService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get all metadata entries",
            notes = "Provides a list of metadata entries, which are stored as key-value pairs.",
            tags = "Device Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:metadata:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of metadata entries.",
                            response = MetadataList.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while fetching the metadata entry list.",
                            response = ErrorResponse.class)
            })
    Response getAllMetadataEntries(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many metadata entries you require from the starting pagination index/offset.",
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit);

    @GET
    @Path("/{metaKey}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get metadata by metaKey",
            notes = "Retrieve a metadata entry by providing a metaKey value",
            tags = "Device Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:metadata:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the requested metadata entry.",
                            response = Metadata.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while fetching the metadata entry.",
                            response = ErrorResponse.class)
            })
    Response getMetadataEntry(
            @ApiParam(
                    name = "metaKey",
                    value = "Key of the metadata",
                    required = true)
            @PathParam("metaKey") String metaKey);

    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_PUT,
            value = "Create metadata entry",
            notes = "Create a entry in metadata repository",
            tags = "Device Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:metadata:create")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully created the metadata entry.",
                            response = Metadata.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n The provided metadataKey is already exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while creating the metadata entry.",
                            response = ErrorResponse.class)
            })
    Response createMetadataEntry(
            Metadata metadata
    );

    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_PUT,
            value = "Update metadata entry",
            notes = "Update metadata entry by the provided metaKey of the Metadata object, if the metaKey is not " +
                    "already exist a new entry will be inserted.",
            tags = "Device Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:metadata:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the provided metadata entry.",
                            response = Metadata.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while updating the metadata entry.",
                            response = ErrorResponse.class)
            })
    Response updateMetadataEntry(
            Metadata metadata
    );

    @DELETE
    @Path("/{metaKey}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "Delete metadata entry",
            notes = "Delete metadata entry by providing a metaKey value",
            tags = "Device Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:metadata:remove")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the requested metadata entry.",
                            response = Metadata.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The requested metadata entry to be deleted is not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while deleting the metadata entry.",
                            response = ErrorResponse.class)
            })
    Response deleteMetadataEntry(
            @ApiParam(
                    name = "metaKey",
                    value = "Key of the metadata",
                    required = true)
            @PathParam("metaKey") String metaKey
    );

}
