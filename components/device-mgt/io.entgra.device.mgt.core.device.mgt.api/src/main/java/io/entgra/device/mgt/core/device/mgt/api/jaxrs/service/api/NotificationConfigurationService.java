package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api;

import io.entgra.device.mgt.core.application.mgt.common.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/notification-configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public interface NotificationConfigurationService{
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create Notification Configuration",
            notes = "Creates new notification configurations based on the input received from the UI.",
            tags = "Notification Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:configurations:create")
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
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")
                            }),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid configuration data received.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the configuration.",
                            response = ErrorResponse.class)
            })
    Response createNotificationConfig(
            @ApiParam(
                    name = "configurations",
                    value = "A list of configuration objects representing the notification settings. This includes the type of notification, recipients, and other related metadata.",
                    required = true)
            @RequestBody List<Map<String, Object>> configurations);

}
