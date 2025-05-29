/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.subtype.mgt.api.service;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = {"subtype-management", "device_management"})
@Path("/subtypes")
@SwaggerDefinition(
        info = @Info(
                description = "Subtype Management",
                version = "v1.0.0",
                title = "SubtypeManagementService API",
                extensions = @Extension(properties = {
                        @ExtensionProperty(name = "name", value = "SubtypeManagementService"),
                        @ExtensionProperty(name = "context", value = "/api/subtype-mgt/v1.0/subtypes"),
                })
        ),
        consumes = {MediaType.APPLICATION_JSON},
        produces = {MediaType.APPLICATION_JSON},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        tags = {
                @Tag(name = "device_management", description = "Device management"),
                @Tag(name = "subtype-management", description = "Subtype management")
        }
)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Scopes(
        scopes = {
                @Scope(
                        name = "Subtype view",
                        description = "Subtype view",
                        key = "dm:subtypes:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/subtypes/view"}
                ),
                @Scope(
                        name = "Subtype create",
                        description = "Subtype create",
                        key = "dm:subtypes:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/subtypes/create"}
                ),
                @Scope(
                        name = "Subtype update",
                        description = "Subtype update",
                        key = "dm:subtypes:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/subtypes/update"}
                )
        }
)
public interface SubtypeManagementService {
    String SCOPE = "scope";

    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.POST,
            value = "Add device subtype",
            notes = "Create device subtype",
            tags = {"subtype-management", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:subtypes:create")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n  Successfully created the device subtype",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = Response.class),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n  device subtype already exists.",
                            response = Response.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the resource.",
                            response = Response.class)
            }
    )
    Response addDeviceSubType(
            @ApiParam(
                    name = "deviceSubType",
                    value = "New device subtype",
                    required = true)
            DeviceSubType deviceSubType);

    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.POST,
            value = "Update device subtype",
            notes = "Update device subtype",
            tags = {"subtype-management", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:subtypes:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n  Successfully update the device subtype",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = Response.class),
                    @ApiResponse(
                            code = 404,
                            message = "Device subtype not found.",
                            response = Response.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the resource.",
                            response = Response.class)
            }
    )
    Response updateDeviceSubType(
            @ApiParam(
                    name = "deviceSubType",
                    value = "Updated device subtype",
                    required = true)
            DeviceSubType deviceSubType
    );

    @GET
    @Path("/device-types/{deviceType}/subtypes/{subTypeId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Retrieve device subtype definition.",
            notes = "Returns the device subtype definition based on the given device type and subtype id.",
            tags = {"subtype-management", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:subtypes:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200,
                            message = "OK. \n Successfully retrieves the device subtype.",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = Response.class),
                    @ApiResponse(
                            code = 404,
                            message = "Device subtype not found.",
                            response = Response.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while retrieving device subtype.",
                            response = Response.class)
            }
    )
    Response getDeviceSubType(
            @ApiParam(
                    name = "subTypeId",
                    value = "Id of the device subtype",
                    required = true)
            @PathParam("subTypeId") String subTypeId,
            @ApiParam(
                    name = "deviceType",
                    value = "Device type",
                    required = true)
            @PathParam("deviceType") String deviceType);

    @GET
    @Path("/device-types/{deviceType}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Retrieve all device subtype definitions.",
            notes = "Returns all the device subtype definitions based on the given device type.",
            tags = {"subtype-management", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:subtypes:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200,
                            message = "OK. \n Successfully retrieves the device subtypes.",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = Response.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while retrieving device subtypes.",
                            response = Response.class)
            }
    )
    Response getAllDeviceSubTypes(
            @ApiParam(
                    name = "deviceType",
                    value = "Device type",
                    required = true)
            @PathParam("deviceType") String deviceType);

    @GET
    @Path("/device-types/{deviceType}/count")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Retrieve the count of the subtypes available for given device type.",
            notes = "Retrieve the count of the subtypes available for given device type.",
            tags = {"subtype-management", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:subtypes:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200,
                            message = "OK. \n Successfully retrieves the device subtype count.",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = Response.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while retrieving device subtypes.",
                            response = Response.class)
            }
    )
    Response getDeviceSubTypeCount(
            @ApiParam(
                    name = "deviceType",
                    value = "Device type",
                    required = true)
            @PathParam("deviceType") String deviceType
    );

    @GET
    @Path("/device-types/{deviceType}/providers/{subTypeName}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Retrieve device subtype by subtype provider's name.",
            notes = "Returns device subtype by the provided subtype provider's name.",
            tags = {"subtype-management", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:subtypes:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200,
                            message = "OK. \n Successfully retrieves the device subtype.",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = Response.class),
                    @ApiResponse(
                            code = 404,
                            message = "Device subtype not found.",
                            response = Response.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while retrieving device subtypes.",
                            response = Response.class)
            }
    )
    Response getDeviceSubTypeByProvider(
            @ApiParam(
                    name = "subTypeName",
                    value = "Device subtype name",
                    required = true)
            @PathParam("subTypeName") String subTypeName,
            @ApiParam(
                    name = "deviceType",
                    value = "Device type",
                    required = true)
            @PathParam("deviceType") String deviceType);
}
