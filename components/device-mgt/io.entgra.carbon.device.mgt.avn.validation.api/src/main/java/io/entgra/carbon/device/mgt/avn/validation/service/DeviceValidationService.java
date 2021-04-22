/*
 *  Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.entgra.carbon.device.mgt.avn.validation.service;

import io.entgra.carbon.device.mgt.avn.validation.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "AvnDeviceValidation"),
                                @ExtensionProperty(name = "context",
                                        value = "/api/device-mgt-avn/v1.0/avn"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "Device management configuration service")
        }
)
@Path("/avn")
@Api(value = "Device Management Configuration")
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(
        scopes = {
                @Scope(
                        name = "Updating Details of a User",
                        description = "Updating Details of a User",
                        key = "perm:users:update",
                        permissions = {"/device-mgt/users/manage"}
                ),
        }
)
public interface DeviceValidationService {

    @POST
    @Path("/validate")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Validating device",
            notes = "This API is responsible for validating devices using VIN & REGO",
            tags = "Device Validation",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully validated the device.",
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
                                            description = "Date and time the resource has been modified " +
                                                    "the last time.Used by caches, or in " +
                                                    "conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad request.\n Invalid VIN & REGO received"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found.\n VIN / REGO not found"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while " +
                                    "validating device.")
            })
    @Produces(MediaType.APPLICATION_JSON)
    Response validateDevice(
            @ApiParam(
                    name = "vin",
                    value = "VIN number of the vehicle",
                    required = true)
            @QueryParam("vin")
                    String vin,
            @ApiParam(
                    name = "rego",
                    value = "REGO number of the vehicle",
                    required = true)
            @QueryParam("rego")
                    String rego,
            @ApiParam(
                    name = "username",
                    value = "User name of the user",
                    required = true)
            @QueryParam("username")
                    String username,
            @ApiParam(
                    name = "role",
                    value = "Role to be changed to",
                    required = true)
            @QueryParam("role")
                    String role);
}

