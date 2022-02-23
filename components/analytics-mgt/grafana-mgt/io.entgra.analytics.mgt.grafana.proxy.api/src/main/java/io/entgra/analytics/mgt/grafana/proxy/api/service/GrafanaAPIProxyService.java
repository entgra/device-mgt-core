/*
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package io.entgra.analytics.mgt.grafana.proxy.api.service;

import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Grafana API Proxy Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "GrafanaAPIManagement"),
                                @ExtensionProperty(name = "context", value = "/api/grafana-mgt/v1.0/api"),
                        })
                }
        ),
        tags = {
                @Tag(name = "analytics_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Using Grafana APIs required for Grafana iframes",
                        description = "Grafana API proxy to validate requests.",
                        key = "perm:grafana:api:view",
                        roles = {"Internal/grafanamgt-user"},
                        permissions = {"/analytics-mgt/grafana-mgt/api/view"}
                )
        }
)

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.MEDIA_TYPE_WILDCARD)
@Api(value = "Grafana API Management", description = "Grafana api related operations can be found here.")
public interface GrafanaAPIProxyService {

    String SCOPE = "scope";

    @POST
    @Path("/ds/query")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Grafana query API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:grafana:api:view")
                    })
            }
    )
    Response queryDatasource(JsonObject body, @Context HttpHeaders headers, @Context UriInfo requestUriInfo);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/frontend-metrics")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Grafana frontend-metric API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:grafana:api:view")
                    })
            }
    )
    Response frontendMetrics(JsonObject body, @Context HttpHeaders headers, @Context UriInfo requestUriInfo);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dashboards/uid/{uid}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Grafana dashboard details API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:grafana:api:view")
                    })
            }
    )
    Response getDashboard(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) throws ClassNotFoundException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/annotations")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Grafana annotations API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:grafana:api:view")
                    })
            }
    )
    Response getAnnotations(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) throws ClassNotFoundException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/alerts/states-for-dashboard")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Get Grafana alert states for dashboard details API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:grafana:api:view")
                    })
            }
    )
    Response getAlertStateForDashboards(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) throws ClassNotFoundException;
}
