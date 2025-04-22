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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.ws.rs.GET;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "SSO Configuration Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "SSOConfigurationManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/sso-config"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "View SSO Configuration",
                        description = "View SSO Configuration details",
                        key = "dm:ssoconfig:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/sso-config/view"}
                )
        }
)
@Api(value = "SSO Configuration Management")
@Path("/sso-config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SSOConfigurationService {
    @GET
    @ApiOperation(
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get SSO Configuration",
            notes = "Retrieve SSO Configuration for the given tenant ID",
            tags = "Tenant Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:ssoconfig:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. Successfully retrieved SSO configuration."),
                    @ApiResponse(
                            code = 404,
                            message = "SSO Configuration not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error.",
                            response = ErrorResponse.class)
            })
    Response getSSOConfiguration();
}

