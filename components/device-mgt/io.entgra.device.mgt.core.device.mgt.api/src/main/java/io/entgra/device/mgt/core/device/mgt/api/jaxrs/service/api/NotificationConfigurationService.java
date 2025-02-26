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

import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.application.mgt.common.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.NotificationConfig;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.NotificationConfigurationList;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.device.mgt.core.dto.notification.mgt.NotificationConfigWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.core.dto.notification.mgt.NotificationConfigDTO;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Notification Configuration Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "NotificationConfigurationManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/notification-configuration")
                        })
                }
        ),
        tags = {
                @Tag(name = "notification_management")
        }
)

@Api(value = "Notification Configuration Management")
@Scopes(
        scopes = {
                @io.entgra.device.mgt.core.apimgt.annotations.Scope(
                        name = "Create Notification Configuration",
                        description = "Create new notification configurations",
                        key = "dm:configurations:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/notification-configuration/create"}
                ),
        }
)
@Path("/notification-configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public interface NotificationConfigurationService {

    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Create Notification Configuration",
            notes = "Creates new notification configurations based on the input received from the UI.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:notificationConfig:create"),
                            @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/notification-configuration")
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
                                            description = "Date and time the resource was last modified.\nUsed by caches, or in conditional requests."
                                    )
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid configuration data received.",
                            response = ErrorResponse.class
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
                    value = "A list of configuration objects representing the notification settings. This includes the type of notification, recipients, and other related metadata.",
                    required = true
            )
            @RequestBody NotificationConfigurationList configurations
    ) throws NotificationConfigurationServiceException;



    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_PUT,
            value = "Update Notification Configuration",
            notes = "Update notification configurations based on the input received from the UI.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:notificationConfig:update"),
                            @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/notification-configuration")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the new notification configurations.",
                            response = NotificationConfigurationList.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"
                                    ),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\nUsed by caches, or in conditional requests."
                                    )
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid configuration data received.",
                            response = ErrorResponse.class
                    ),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the configuration.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response updateNotificationConfig(
            @ApiParam(
                    name = "configurations",
                    value = "A list of configuration objects representing the notification settings. This includes the type of notification, recipients, and other related metadata.",
                    required = true
            )
            @RequestBody NotificationConfig configuration
    ) throws NotificationConfigurationServiceException;

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "View Notification Configurations",
            notes = "Retrieve the list of notification configurations for the current tenant.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:notificationConfig:view"),
                            @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/notification-configuration")
                    })
            }

    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved notification configurations.",
                            response = NotificationConfig.class,
                            responseContainer = "List"
                    ),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No configurations found for the tenant.",
                            response = ErrorResponse.class
                    ),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while retrieving configurations.",
                            response = ErrorResponse.class
                    )
            }
    )
    @Produces(MediaType.APPLICATION_JSON)
    Response getNotificationConfigurations() throws NotificationConfigurationServiceException;


    @GET
    @Path("/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get Notification Configuration by ID",
            notes = "Retrieve a specific notification configuration by its unique identifier.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:configurations:read"),
                            @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/notification-configuration/{id}")
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
            ),  @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid configuration data received.",
                    response = ErrorResponse.class
            )}
    )

     Response getNotificationConfig(
            @ApiParam(
                    name = "id",
                    value = "Operation ID of the notification configuration to be retrieved.",
                    required = true
            )
            @PathParam("id") String configId
    ) throws NotificationConfigurationServiceException;

    @DELETE
    @Path("/{configId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "delete Notification Configuration",
            notes = "delete a notification configuration based on configuration ID ",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:notificationConfig:delete"),
                            @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/notification-configuration")
                    })
            }
    )
    Response deleteNotificationConfig(
            @ApiParam(
                    name = "configurations",
                    value = "The configuration ID",
                    required = true
            )
            @PathParam("configId") String configId
    ) throws NotificationConfigurationServiceException;


    @DELETE
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "Delete Notification Configuration(s)",
            notes = "Deletes the entire notification configuration list of tenant",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:configurations:delete"),
                            @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/notification-configuration")
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
                                            description = "Date and time the resource was last modified.\nUsed by caches, or in conditional requests."
                                    )
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid configuration data received.",
                            response = ErrorResponse.class
                    ),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the configuration.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response deleteNotificationConfigurations() throws NotificationConfigurationServiceException;


}
