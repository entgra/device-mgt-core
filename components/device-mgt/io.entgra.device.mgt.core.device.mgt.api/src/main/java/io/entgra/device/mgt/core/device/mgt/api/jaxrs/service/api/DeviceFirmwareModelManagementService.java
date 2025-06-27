package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.application.mgt.common.DeviceTypes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Device Firmware Model Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceFirmwareModelManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/device-firmware-models"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "Device Management")
        }
)
@Path("/device-firmware-models")
@Api(value = "Device Firmware Management Service", description = "This an API intended to be use to manage device firmware models")
@Scopes(
        scopes = {
                @Scope(
                        name = "Get device firmware model",
                        description = "Get device firmware model)",
                        key = "dm:device-firmware-models:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-firmware-models/view"}
                )
        }
)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceFirmwareModelManagementService {
    @GET
    @Path("/device-types/{deviceType}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get device firmware models",
            notes = "Get device firmware models available for device type.",
            response = Response.class,
            tags = "Device Firmware Management Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-firmware-models:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "Created. \n Device firmware model is successfully created.",
                    response = Response.class),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while creating device firmware model.",
                    response = ErrorResponse.class)
    })
    Response getDeviceFirmwareModelsByDeviceType(
            @ApiParam(
                    name = "deviceType",
                    value = "Device type of the firmware model",
                    required = true)
            @PathParam("deviceType") String deviceType);
}
