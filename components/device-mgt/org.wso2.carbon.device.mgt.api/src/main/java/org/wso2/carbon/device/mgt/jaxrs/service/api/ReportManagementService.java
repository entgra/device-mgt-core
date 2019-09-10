package org.wso2.carbon.device.mgt.jaxrs.service.api;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.jaxrs.NotificationList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceNotificationManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/reports"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Scopes(
        scopes = {
                @org.wso2.carbon.apimgt.annotations.api.Scope(
                        name = "Getting All Device Notification Details",
                        description = "Getting All Device Notification Details",
                        key = "perm:notifications:view",
                        permissions = {"/device-mgt/notifications/view"}
                ),
                @Scope(
                        name = "Updating the Device Notification Status",
                        description = "Updating the Device Notification Status",
                        key = "perm:notifications:mark-checked",
                        permissions = {"/device-mgt/notifications/view"}
                ),
                @Scope(
                        name = "Getting Details of Registered Devices",
                        description = "Getting Details of Registered Devices",
                        key = "perm:devices:view",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Details of a Device",
                        description = "Getting Details of a Device",
                        key = "perm:devices:details",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Update the device specified by device id",
                        description = "Update the device specified by device id",
                        key = "perm:devices:update",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Delete the device specified by device id",
                        description = "Delete the device specified by device id",
                        key = "perm:devices:delete",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Feature Details of a Device",
                        description = "Getting Feature Details of a Device",
                        key = "perm:devices:features",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Advanced Search for Devices",
                        description = "Advanced Search for Devices",
                        key = "perm:devices:search",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Installed Application Details of a Device",
                        description = "Getting Installed Application Details of a Device",
                        key = "perm:devices:applications",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Device Operation Details",
                        description = "Getting Device Operation Details",
                        key = "perm:devices:operations",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Get the details of the policy that is enforced on a device.",
                        description = "Get the details of the policy that is enforced on a device.",
                        key = "perm:devices:effective-policy",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Policy Compliance Details of a Device",
                        description = "Getting Policy Compliance Details of a Device",
                        key = "perm:devices:compliance-data",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Change device status.",
                        description = "Change device status.",
                        key = "perm:devices:change-status",
                        permissions = {"/device-mgt/devices/change-status"}
                ),
        }
)

@Api(value = "Device Report Management", description = "Device report related operations can be found here.")
@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ReportManagementService {

    @GET
    @Path("/devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Registered Devices",
            notes = "Provides details of all the devices enrolled with WSO2 IoT Server.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of notifications.",
                            response = NotificationList.class,
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client already has the latest version " +
                                    "of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid notification status type received. \n" +
                                    "Valid status types are NEW | CHECKED",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n There are no notification.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while fetching the notification list.",
                            response = ErrorResponse.class)
            })
    Response getDevicesByDuration(
            @QueryParam("status") String status,
            @QueryParam("ownership") String ownership,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit) throws NotificationManagementException;
}