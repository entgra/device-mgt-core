/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.service.api;

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
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.geo.service.Alert;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.GeofenceWrapper;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "geo_services"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/geo-services"),
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
                        name = "View Analytics",
                        description = "",
                        key = "perm:geo-service:analytics-view",
                        permissions = {"/device-mgt/devices/owning-device/view-analytics"}
                ),
                @Scope(
                        name = "Manage Alerts",
                        description = "",
                        key = "perm:geo-service:alerts-manage",
                        permissions = {"/device-mgt/devices/owning-device/manage-alerts"}
                ),
                @Scope(
                        name = "Manage Geo Fences",
                        description = "",
                        key = "perm:geo-service:geo-fence",
                        permissions = {"/device-mgt/devices/owning-device/manage-geo-fence"}
                )
        }
)
@Path("/geo-services")
@Api(value = "Geo Service",
        description = "This carries all the resources related to the geo service functionalities.")
public interface GeoLocationBasedService {
    /**
     * Retrieve Analytics for the device type
     */
    @GET
    @Path("stats/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting the Location Details of a Device",
            notes = "Get the location details of a device during a define time period.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:analytics-view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoDeviceStats(
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "from",
                    value = "Define the time to start getting the geo location history of the device in the Epoch or UNIX format.",
                    required = true)
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "Define the time to finish getting the geo location history of the device in the Epoch or UNIX format.",
                    required = true)
            @QueryParam("to") long to);

    /**
     * Get data to show device locations in a map
     */
    @GET
    @Path("stats/device-locations")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting the Devices in a Defined Geofence",
            notes = "Get the details of the devices that are within the defined geofence coordinates. The geofence you are defining is enclosed with four coordinates in the shape of a square or rectangle. This is done by defining two points of the geofence. The other two points are automatically created using the given points. You can define the zoom level or scale of the map too.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:analytics-view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid parameters found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    @Deprecated
    Response getGeoDeviceLocations(
            @ApiParam(
                    name = "deviceType",
                    value = "Optional Device type name.")
            @QueryParam("deviceType") String deviceType,
            @ApiParam(
                    name = "minLat",
                    value = "Define the minimum latitude of the geofence.",
                    required = true,
                    defaultValue ="79.85213577747345")
            @QueryParam("minLat") double minLat,
            @ApiParam(
                    name = "maxLat",
                    value = "Define the maximum latitude of the geofence.",
                    required = true,
                    defaultValue ="79.85266149044037")
            @QueryParam("maxLat") double maxLat,
            @ApiParam(
                    name = "minLong",
                    value = "Define the minimum longitude of the geofence.",
                    required = true,
                    defaultValue ="6.909673257977737")
            @QueryParam("minLong") double minLong,
            @ApiParam(
                    name = "maxLong",
                    value = "Define the maximum longitude of the geofence",
                    required = true,
                    defaultValue ="6.909673257977737")
            @QueryParam("maxLong") double maxLong,
            @ApiParam(
                    name = "zoom",
                    value = "Define the level to zoom or scale the map. You can define any value between 1 to 14.",
                    required = true,
                    defaultValue ="2")
            @QueryParam("zoom") int zoom);

    @Path("stats/geo-view")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting geo view of devices",
            notes = "Get the details of the devices that are within the map. The map area is enclosed with four " +
                    "coordinates in the shape of a square or rectangle. This is done by defining two points of the " +
                    "map. The other two points are automatically created using the given points. " +
                    "You can define the zoom level or scale of the map too.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:analytics-view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid parameters found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoDeviceView(
            @ApiParam(
                    name = "minLat",
                    value = "Define the minimum latitude of the geofence.",
                    required = true,
                    defaultValue ="79.85213577747345")
            @QueryParam("minLat") double minLat,
            @ApiParam(
                    name = "maxLat",
                    value = "Define the maximum latitude of the geofence.",
                    required = true,
                    defaultValue ="79.85266149044037")
            @QueryParam("maxLat") double maxLat,
            @ApiParam(
                    name = "minLong",
                    value = "Define the minimum longitude of the geofence.",
                    required = true,
                    defaultValue ="6.909673257977737")
            @QueryParam("minLong") double minLong,
            @ApiParam(
                    name = "maxLong",
                    value = "Define the maximum longitude of the geofence",
                    required = true,
                    defaultValue ="6.909673257977737")
            @QueryParam("maxLong") double maxLong,
            @ApiParam(
                    name = "zoom",
                    value = "Define the level to zoom or scale the map. You can define any value between 1 to 14.",
                    required = true,
                    defaultValue ="2")
            @QueryParam("zoom") int zoom,
            @ApiParam(
                    name = "deviceType",
                    value = "Optional Device type name.")
            @QueryParam("deviceType") List<String> deviceTypes,
            @ApiParam(
                    name = "deviceIdentifier",
                    value = "Optional Device Identifier.")
            @QueryParam("deviceIdentifier") List<String> deviceIdentifiers,
            @ApiParam(
                    name = "status",
                    value = "Optional Device status.")
            @QueryParam("status") List<EnrolmentInfo.Status> statuses,
            @ApiParam(
                    name = "ownership",
                    value = "Optional Device ownership.")
            @QueryParam("ownership") List<String> ownerships,
            @ApiParam(
                    name = "owner",
                    value = "Optional Device owner.")
            @QueryParam("owner") List<String> owners,
            @ApiParam(
                    name = "noClusters",
                    value = "Optional include devices only.")
            @QueryParam("noClusters") boolean noClusters,
            @ApiParam(
                    name = "createdBefore",
                    value = "Optional Device created before timestamp.")
            @QueryParam("createdBefore") long createdBefore,
            @ApiParam(
                    name = "createdAfter",
                    value = "Optional Device created after timestamp..")
            @QueryParam("createdAfter") long createdAfter,
            @ApiParam(
                    name = "updatedBefore",
                    value = "Optional Device updated before timestamp.")
            @QueryParam("updatedBefore") long updatedBefore,
            @ApiParam(
                    name = "updatedAfter",
                    value = "Optional Device updated after timestamp.")
            @QueryParam("updatedAfter") long updatedAfter);

    /**
     * Create Geo alerts
     */
    @POST
    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "POST",
            value = "Retrieving a Specific Geo Alert Type from a Device",
            notes = "Retrieve a specific geo alert from a device, such as getting a speed alert that was sent to a device.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response createGeoAlerts(
            @ApiParam(
                    name = "alert",
                    value = "The alert object",
                    required = true)
            @Valid Alert alert,
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed,Exit, or Stationary.",
                    required = true)
            @PathParam("alertType") String alertType);


    /**
     * Create Geo alerts for geo clusters
     */
    @POST
    @Path("/alerts/{alertType}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "POST",
            value = "Create Geo alerts for geo clusters",
            notes = "Creating geo alerts for cluster of devices",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n A geo alert with this name already exists.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response createGeoAlertsForGeoClusters(
            @ApiParam(
                    name = "alert",
                    value = "The alert object",
                    required = true)
            @Valid Alert alert,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Stationary",
                    required = true)
            @PathParam("alertType") String alertType);

    /**
     * Update Geo alerts
     */
    @PUT
    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "PUT",
            value = "Updating the Geo Alerts of a Device",
            notes = "Update the a geo alert that was sent to a device.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response updateGeoAlerts(
            @ApiParam(
                    name = "alert",
                    value = "The alert object",
                    required = true)
            @Valid Alert alert,
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Exit, or Stationary",
                    required = true)
            @PathParam("alertType") String alertType);

    /**
     * Update Geo alerts for geo clusters
     */
    @PUT
    @Path("alerts/{alertType}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Update Geo alerts for geo clusters",
            notes = "Updating an existing geo alert that was defined for geo clusters",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response updateGeoAlertsForGeoClusters(
            @ApiParam(
                    name = "alert",
                    value = "The alert object",
                    required = true)
            @Valid Alert alert,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Stationary",
                    required = true)
            @PathParam("alertType") String alertType);

    /**
     * Retrieve Geo alerts
     */
    @GET
    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting a Geo Alert from a Device",
            notes = "Retrieve a specific geo alert from a device, such as getting a speed alert that was sent to a device.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
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
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoAlerts(
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android. or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Exit, or Stationary",
                    required = true)
            @PathParam("alertType") String alertType);

    /**
     * Retrieve Geo alerts for geo clusters
     */
    @GET
    @Path("alerts/{alertType}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Retrieve Geo alerts for geo clusters",
            notes = "Retrieve all the defined alerts for a specific alert type",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
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
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoAlertsForGeoClusters(
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Stationary",
                    required = true)
            @PathParam("alertType") String alertType);


    /**
     * Retrieve Geo alerts history
     */
    @GET
    @Path("alerts/history/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting the Geo Service Alert History of a Device",
            notes = "Get the geo alert history of a device during the defined time period.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
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
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoAlertsHistory(
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "from",
                    value = "Define the time to start getting the geo location history of the device in the Epoch or UNIX format.",
                    required = true)
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "Define the time to finish getting the geo location history of the device in the Epoch or UNIX format.",
                    required = true)
            @QueryParam("to") long to);

    /**
     * Retrieve Geo alerts history for geo clusters
     */
    @GET
    @Path("alerts/history")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Retrieve Geo alerts history for geo clusters",
            notes = "Retrieving geo alert history of all defined alerts for geo clusters",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
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
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoAlertsHistoryForGeoClusters(
            @ApiParam(
                    name = "from",
                    value = "Get stats from what time",
                    required = true)
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "Get stats up to what time",
                    required = true)
            @QueryParam("to") long to);


    /**
     * Remove geo alerts
     */

    @DELETE
    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "DELETE",
            value = "Deleting a Geo Alert from a Device",
            notes = "Delete a specific geo alert from a device, such as deleting a speed alert that was sent to the device.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response removeGeoAlerts(
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "deviceType",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Exit, or Stationary",
                    required = true)
            @PathParam("alertType") String alertType,
            @ApiParam(
                    name = "queryName",
                    value = "When you define a geofence you define a fence name for it. That name needs to be defined" +
                            " here.",
                    required = true)
            @QueryParam("queryName") String queryName);

    /**
     * Remove geo alerts for geo clusters
     */

    @DELETE
    @Path("alerts/{alertType}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "DELETE",
            value = "Deletes Geo alerts for geo clusters",
            notes = "Deleting any type of a geo alert that was defined for geo clusters",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response removeGeoAlertsForGeoClusters(
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Stationary",
                    required = true)
            @PathParam("alertType") String alertType,
            @ApiParam(
                    name = "queryName",
                    value = "The query name.",
                    required = true)
            @QueryParam("queryName") String queryName);

    @POST
    @Path("/geo-fence")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "POST",
            value = "Create Geo fence",
            notes = "Create a new geo fence",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:geo-fence")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Geofence data found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = ErrorResponse.class)
    })
    Response createGeofence(@ApiParam(name = "fence", value = "Geo fence data")GeofenceWrapper geofenceWrapper);


    @GET
    @Path("/geo-fence/{fenceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Get Geo fence",
            notes = "Get existing geo fence",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:geo-fence")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 404,
                    message = "Not found. \n No Geofence found for the Id",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeofence(
            @ApiParam(
                name = "fenceId",
                value = "Id of the fence",
                required = true)
            @PathParam("fenceId") int fenceId,
            @ApiParam(
                    name = "requireEventData",
                    value = "Require geofence event data")
            @QueryParam("requireEventData") boolean requireEventData);


    @GET
    @Path("/geo-fence")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Get Geo fences",
            notes = "Get all geo fence",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:geo-fence")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeofence(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "name",
                    value = "Geo Fence name")
            @QueryParam("name") String name,
            @ApiParam(
                    name = "requireEventData",
                    value = "Require geofence event data")
            @QueryParam("requireEventData") boolean requireEventData);


    @DELETE
    @Path("/geo-fence/{fenceId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete Geo fence",
            notes = "Delete an existing geo fence",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:geo-fence")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 404,
                    message = "Not found. \n No geofences found for the Id",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response deleteGeofence(
            @ApiParam(
                    name = "fenceId",
                    value = "Id of the fence",
                    required = true)
            @PathParam("fenceId") int fenceId);


    @PUT
    @Path("/geo-fence/{fenceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "PUT",
            value = "Update Geo fence",
            notes = "Update an existing geo fence",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:geo-fence")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Geofence data found.",
                    response = Response.class),
            @ApiResponse(
                    code = 404,
                    message = "Not found. \n No Geofence found for the Id",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response updateGeofence(
            @ApiParam(name = "fence", value = "Geo fence data")
                    GeofenceWrapper geofenceWrapper,
            @ApiParam(
                    name = "fenceId",
                    value = "Id of the fence",
                    required = true)
            @PathParam("fenceId") int fenceId,
            @ApiParam(name = "eventIds", value = "Event id list to be removed") @QueryParam("eventIds") int[] eventIds);
}

