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

package io.entgra.device.mgt.core.notification.mgt.api.service;

import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.notification.mgt.api.beans.ErrorResponse;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Notification configurations related REST-API.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "NotificationConfigurationService",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "NotificationConfigurationManagement"),
                                @ExtensionProperty(name = "context",
                                        value = "/api/notification-mgt/v1.0/notification-configuration")
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
                        name = "View Notification Configurations",
                        description = "View notification configurations",
                        key = "dm:notificationConfig:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/notifications/notification-configurations/view"}
                ),
                @Scope(
                        name = "Update Notification Configuration",
                        description = "Update a notification configuration",
                        key = "dm:notificationConfig:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/notifications/notification-configurations/update"}
                ),
                @Scope(
                        name = "Create Notification Configuration",
                        description = "Create new notification configurations",
                        key = "dm:notificationConfig:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/notifications/notification-configurations/create"}
                ),
                @Scope(
                        name = "Delete Notification Configuration",
                        description = "Delete new notification configurations",
                        key = "dm:notificationConfig:delete",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/notifications/notification-configurations/delete"}
                )
        }
)
@Path("/notification-configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Notification Configuration Management",
        description = "Notification Configuration Management related operations can be found here.")
public interface NotificationConfigurationService {
    String SCOPE = "scope";

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "View Notification Configurations",
            notes = "Retrieve the list of notification configurations for the current tenant.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:notificationConfig:view"),
                            @ExtensionProperty(name = "context",
                                    value = "/api/notification-mgt/v1.0/notification-configuration")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved notification configurations.",
                            response = Response.class
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No configurations found for the tenant.",
                            response = Response.class
                    ),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n  Notifications already exists.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while retrieving configurations.",
                            response = Response.class
                    )
            }
    )
    Response getNotificationConfigurations(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many notification configurations you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "code",
                    value = "Search notification configurations by code.",
                    required = false)
            @QueryParam("name") String code,
            @ApiParam(
                    name = "name",
                    value = "Search notification configurations by name.",
                    required = false)
            @QueryParam("name") String name,
            @ApiParam(
                    name = "type",
                    value = "Filter notification configurations by type.",
                    required = false)
            @QueryParam("type") String type,
            @ApiParam(
                    name = "deviceType",
                    value = "Filter notification configurations by deviceType.",
                    required = false)
            @QueryParam("deviceType") String deviceType);


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create Notification Configuration",
            notes = "Creates new notification configurations based on the input received from the UI.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:notificationConfig:create"),
                            @ExtensionProperty(name = "context",
                                    value = "/api/notification-mgt/v1.0/notification-configuration")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Successfully created the new notification configurations.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"
                                    ),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\nUsed by caches," +
                                                    " or in conditional requests."
                                    )
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid configuration data received.",
                            response = ErrorResponse.class
                    ),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. A notification configuration already exists for the given device type and code.",
                            response = Response.class
                    ),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the configuration.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response createNotificationConfig(
            @ApiParam(
                    name = "configurations",
                    value = "A list of configuration objects representing the notification settings. " +
                            "This includes the type of notification, recipients, and other related metadata.",
                    required = true
            )
            NotificationConfigurationList configurations
    );

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update Notification Configuration",
            notes = "Update a notification configuration based on the input received from the UI.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:notificationConfig:update"),
                            @ExtensionProperty(name = "context",
                                    value = "/api/notification-mgt/v1.0/notification-configuration")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. Successfully updated the notification configuration.",
                            response = NotificationConfig.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"
                                    ),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified."
                                    )
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. Invalid configuration data received.",
                            response = ErrorResponse.class
                    ),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. The resource to be updated does not exist."
                    ),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. A notification configuration already exists for the given device type and code.",
                            response = Response.class
                    ),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. Server error occurred while updating the configuration.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response updateNotificationConfig(
            NotificationConfig configuration
    );

    @GET
    @Path("/{configId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get Notification Configuration by ID",
            notes = "Retrieve a specific notification configuration by its unique identifier.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:notificationConfig:view"),
                            @ExtensionProperty(name = "context",
                                    value = "/api/notification-mgt/v1.0/notification-configuration/{configId}")
                    })
            }
    )
    @ApiResponses(
            value = {@ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully retrieved the requested configuration.",
                    response = NotificationConfig.class
            ), @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The requested configuration does not exist.",
                    response = ErrorResponse.class
            ), @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while retrieving the configuration.",
                    response = ErrorResponse.class
            ), @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid configuration data received.",
                    response = ErrorResponse.class
            )}
    )
    Response getNotificationConfig(
            @ApiParam(
                    name = "configId",
                    value = "config ID of the notification configuration to be retrieved.",
                    required = true
            )
            @PathParam("configId") int configId
    );

    @DELETE
    @Path("/{configId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete Notification Configuration",
            notes = "Delete a notification configuration based on configuration ID ",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:notificationConfig:delete"),
                            @ExtensionProperty(name = "context",
                                    value = "/api/notification-mgt/v1.0/notification-configuration")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the new notification configuration.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"
                                    ),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\nUsed by caches," +
                                                    " or in conditional requests."
                                    )
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid configuration data received.",
                            response = ErrorResponse.class
                    ),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The resource to be deleted does not exist."
                    ),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the configuration.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response deleteNotificationConfig(
            @ApiParam(
                    name = "configurations",
                    value = "The configuration ID",
                    required = true
            )
            @PathParam("configId") int configId
    );

    @DELETE
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete Notification Configuration(s)",
            notes = "Deletes the entire notification configuration list of tenant",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:notificationConfig:delete"),
                            @ExtensionProperty(name = "context",
                                    value = "/api/notification-mgt/v1.0/notification-configuration")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the new notification configuration.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"
                                    ),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\nUsed by caches, " +
                                                    "or in conditional requests."
                                    )
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid configuration data received.",
                            response = ErrorResponse.class
                    ),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The resource to be deleted does not exist."
                    ),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the configuration.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response deleteNotificationConfigurations();

    @PUT
    @Path("/defaults")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update Default Archival Settings",
            notes = "Sets the default archive type and duration for notifications that do not have" +
                    " specific configurations.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:notificationConfig:update"),
                            @ExtensionProperty(name = "context",
                                    value = "/api/notification-mgt/v1.0/notification-configuration")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \nSuccessfully updated the default archival settings.",
                            response = NotificationConfigurationList.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"
                                    ),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified. " +
                                                    "Used by caches, or in conditional requests."
                                    )
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. Invalid or missing archival settings provided.",
                            response = ErrorResponse.class
                    ),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. Failed to update the default archival " +
                                    "settings due to a server error.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response updateDefaultArchiveSettings(
            @ApiParam(
                    name = "archiveDefaults",
                    value = "DTO containing the default archive type and archive period (e.g., '6 days', '3 months').",
                    required = true
            )
            NotificationConfigurationList archiveDefaults
    );

    @Path("/check")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Check if a Notification Configuration exists",
            notes = "Checks whether a notification configuration already exists for the given device type and code.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(
                                    name = SCOPE,
                                    value = "dm:notificationConfig:view"),
                            @ExtensionProperty(
                                    name = "context",
                                    value = "/api/notification-mgt/v1.0/notification-configuration/check")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. No conflicting notification configuration found.",
                            response = Response.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. No configuration found for the tenant.",
                            response = Response.class),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. A notification configuration already exists for the given device type and code.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. Error occurred while checking configurations.",
                            response = Response.class)
            }
    )
    Response checkNotificationConfig(
            @ApiParam(
                    name = "deviceType",
                    value = "Device type of the notification configuration.",
                    required = true)
            @QueryParam("deviceType")
            String deviceType,
            @ApiParam(
                    name = "code",
                    value = "The operation or task code associated with the notification.",
                    required = true)
            @QueryParam("code")
            String code
    );
}
