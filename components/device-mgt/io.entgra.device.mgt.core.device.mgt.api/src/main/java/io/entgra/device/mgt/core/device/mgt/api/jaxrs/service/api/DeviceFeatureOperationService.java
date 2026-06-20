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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.common.dto.DeviceFeatureInfo;
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

import javax.ws.rs.GET;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Device related REST-API. This can be used to manipulated device related details.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceFeatureManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/device-operations"),
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
                        name = "Getting Feature Details of a Device",
                        description = "Getting Feature Details of a Device",
                        key = "dm:devices:features:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/features/view"}
                ),
        }
)
@Path("/device-operations")
@Api(value = "Device Management", description = "This API carries all device operation-related endpoints.")
public interface DeviceFeatureOperationService {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve Operation Details",
            notes = "Retrieve operation details based on operation code, name, or device type. " +
                    "Only one of 'code' or 'name' should be specified. If both are given, request will fail.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:devices:features:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. Successfully retrieved operation details.",
                            response = DeviceFeatureInfo.class,
                            responseContainer = "List"
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. Only one of 'code' or 'name' must be provided.",
                            response = ErrorResponse.class
                    ),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. The specified operation can not be found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. Error while retrieving operation details.",
                            response = ErrorResponse.class
                    )
            }
    )
    Response getOperationDetails(
            @ApiParam(
                    name = "code",
                    value = "The operation code to search for (supports partial match). Cannot be used with 'name'.",
                    required = false)
            @QueryParam("code") String code,
            @ApiParam(
                    name = "name",
                    value = "The operation name to search for (supports partial match). Cannot be used with 'code'.",
                    required = false)
            @QueryParam("name") String name,
            @ApiParam(
                    name = "type",
                    value = "The device type to filter by (e.g., android, ios, windows).",
                    required = false)
            @QueryParam("type") String type,
            @ApiParam(
                    name = "removeDeduplicateCode",
                    value = "If true, removes duplicate operations by code when 'type' is not specified.",
                    required = false)
            @DefaultValue("false")
            @QueryParam("removeDeduplicateCode") boolean deduplicate

    );
}
