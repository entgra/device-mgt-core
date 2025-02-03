/*
 *  Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.notification.mgt.api.service;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.swagger.annotations.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Notifications related REST-API.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "NotificationService",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "NotificationManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/notifications"),
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
                        name = "Getting All Notifications",
                        description = "Getting All Notification Details",
                        key = "dm:notifications:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/notifications/view"}
                ),
                @Scope(
                        name = "Updating the Notification",
                        description = "Updating the Notifications",
                        key = "dm:notif:mark-checked",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/notifications/update"}
                )
        }
)
@Api(value = "Notification Management", description = "Notification Management related operations can be found here.")
@Path("/notification")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface NotificationService {
    String SCOPE = "scope";
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting All Notification Details",
            notes = "Get the details of all the notifications that were pushed to the devices registered with WSO2 EMM using this REST API.",
            tags = "Notification Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:notifications:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n  Successfully retrieved the Notifications",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = Response.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist.",
                            response = Response.class),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n  Notifications already exists.",
                            response = Response.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the resource.",
                            response = Response.class)
            })
    Response getLatestNotifications(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
            int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many notification details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
            int limit);

//    @POST
//    @Path("/create")
//    @ApiOperation(
//            produces = MediaType.APPLICATION_JSON,
//            httpMethod = HttpMethod.POST,
//            value = "Create a new notification",
//            notes = "Creates and stores a new notification in the system",
//            tags = {"notifications", "device_management"},
//            extensions = {
//                    @Extension(properties = {
//                            @ExtensionProperty(name = SCOPE, value = "dm:notifications:create")
//                    })
//            }
//    )
//    @ApiResponses(
//            value = {
//                    @ApiResponse(code = 201, message = "Created. Successfully created notification", response = Notification.class),
//                    @ApiResponse(code = 500, message = "Internal Server Error. Error occurred while creating notification", response = Response.class)
//            }
//    )
//    Response createNotification(Notification notification);
}
