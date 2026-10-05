package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.admin;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.DeviceFirmwareModel;
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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Device Firmware Model Management Admin Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceFirmwareModelManagementAdminService"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/admin/device-firmware-models"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "Device Management")
        }
)
@Path("/admin/device-firmware-models")
@Api(value = "Device Firmware Management Admin Service", description = "This an API intended to be used by server administration to manage device firmware models")
@Scopes(
        scopes = {
                @Scope(
                        name = "Create device firmware model",
                        description = "Create device firmware model)",
                        key = "dm:admin:device-firmware-models:create",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/device-firmware-models/create"}
                )
        }
)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceFirmwareModelManagementAdminService {
    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create new device firmware model",
            notes = "This is an API that can be used to create device firmware models.",
            response = Response.class,
            tags = "Device Firmware Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:admin:device-firmware-models:create")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device firmware model is successfully created.",
                    response = Response.class),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 409,
                    message = "Conflict. \n The specified resource already exists.",
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
    Response createDeviceFirmwareModel(
            @ApiParam(
                    name = "deviceFirmwareModel",
                    value = "Device firmware model to be create.",
                    required = true)
            DeviceFirmwareModel deviceFirmwareModel);
}
