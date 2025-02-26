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
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.core.dto.notification.mgt.NotificationConfigDTO;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:configurations:create"),
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
            @RequestBody List<NotificationConfigDTO> configurations
    );


    @GET
    @ApiOperation(
            produces = MediaType.TEXT_PLAIN,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Test Notification Configuration",
            notes = "This endpoint is for testing purposes and returns a simple text response.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:configurations:create"),
                            @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/notification-configuration")
                    })
            }

    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Test response received.",
                            response = String.class
                    ),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n An error occurred while testing the endpoint.",
                            response = ErrorResponse.class
                    )
            }
    )
    @Produces(MediaType.TEXT_PLAIN)
    Response testNotificationConfig();
}
