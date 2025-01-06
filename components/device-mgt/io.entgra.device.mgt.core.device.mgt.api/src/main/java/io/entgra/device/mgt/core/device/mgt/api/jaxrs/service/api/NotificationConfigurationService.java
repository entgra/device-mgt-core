package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api;

import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.application.mgt.common.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.core.dto.notification.mgt.NotificationConfigDTO;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
                        roles = {"Internal/configuration-admin"},
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
}
