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
import io.entgra.device.mgt.core.device.mgt.common.type.statistic.DeviceTypeStatistic;
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

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceStatisticManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/statistics"),
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
                        name = "Add Statistic Definitions for a device type",
                        description = "Add Statistic Definitions for a device type",
                        key = "dm:device-type:statistic:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/statistic/create"}
                ),
                @Scope(
                        name = "Update Statistic Definitions for a device type",
                        description = "Update Statistic Definitions for a device type",
                        key = "dm:device-type:statistic:modify",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/statistic/modify"}
                ),
                @Scope(
                        name = "Get Statistic Definitions of a Device Type",
                        description = "Get Statistic Definitions of a Device Type",
                        key = "dm:device-type:statistic:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/statistic/view"}
                ),
                @Scope(
                        name = "Delete Statistic Definitions for a device type",
                        description = "Delete Statistic Definitions for a device type",
                        key = "dm:device-type:statistic:delete",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/statistic/delete"}
                ),
        }
)
@Path("/statistics")
@Api(value = "Device Statistic Management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceStatisticManagementService {

    @POST
    @Path("/{type}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding Statistic Definitions",
            notes = "Add statistic definitions for a device type.",
            tags = "Device Statistic Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:statistic:create")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully added the statistic definitions.",
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
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                            "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request. \n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict.\n The statistic definitions already exist."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deploying the " +
                                    "statistic definitions for a device type.",
                            response = ErrorResponse.class)
            }
    )
    Response createDeviceTypeStatisticDefinitions(
            @ApiParam(name = "type", value = "The device type, such as android, ios, and windows.")
            @PathParam("type") String deviceType,
            @ApiParam(name = "deviceTypeStatistics", value = "Add the data to complete the  DeviceTypeStatistic object.",
                    required = true)
            @Valid List<DeviceTypeStatistic> deviceTypeStatistic);

    @PUT
    @Path("/{type}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating Statistic Definitions ",
            notes = "Updating statistic definitions for a device type.",
            tags = "Device Statistic Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:statistic:modify")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the statistic definitions.",
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
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                            "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request. \n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict.\n Device type statistic definitions not updated " +
                                    "due to devices that are already enrolled."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while updating " +
                                    "statistic definitions for a device type.",
                            response = ErrorResponse.class)
            }
    )
    Response updateDeviceTypeStatisticDefinitions(
            @ApiParam(name = "type", value = "The device type, such as android, ios, and windows.")
            @PathParam("type") String deviceType,
            @ApiParam(name = "deviceTypeStatistics", value = "Add the data to complete the  DeviceTypeStatistic object.",
                    required = true)
            @Valid List<DeviceTypeStatistic> deviceTypeStatistic);

    @DELETE
    @Path("/{type}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete Statistic Definitions",
            notes = "Delete statistic definitions for a device type.",
            tags = "Device Statistic Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:statistic:delete")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 204,
                            message = "OK. \n Successfully deleted the statistic definitions.",
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
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                            "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request. \n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict.\n Device type statistic definitions not deleted " +
                                    "due to devices that are already enrolled."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deleting the " +
                                    "statistic definitions for a device type.",
                            response = ErrorResponse.class)
            }
    )
    Response deleteDeviceTypeStatisticDefinitions(@ApiParam(name = "type", value = "The device type, such as " +
            "android, ios, and windows.")
                                                  @PathParam("type") String deviceType);

    @GET
    @Path("/{type}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Statistic Definitions",
            notes = "Get statistic definitions for a device type.",
            tags = "Device Statistic Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:statistic:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the statistic definitions.",
                            response = DeviceTypeStatistic.class,
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
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                            "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request. \n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the " +
                                    "statistic definitions for a device type.",
                            response = ErrorResponse.class)
            }
    )
    Response getDeviceTypeStatisticDefinitions(
            @ApiParam(name = "type", value = "The type of the device, such as android, ios, or windows.")
            @PathParam("type") String deviceType);
}