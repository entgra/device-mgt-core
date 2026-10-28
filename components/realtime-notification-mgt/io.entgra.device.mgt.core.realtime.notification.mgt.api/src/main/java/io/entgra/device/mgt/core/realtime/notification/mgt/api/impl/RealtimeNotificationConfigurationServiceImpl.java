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

package io.entgra.device.mgt.core.realtime.notification.mgt.api.impl;

import io.entgra.device.mgt.core.realtime.notification.mgt.api.util.RealtimeNotificationConfigurationApiUtil;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeArchivePeriod;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfig;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.beans.RealtimeNotificationConfigurationList;

import io.entgra.device.mgt.core.realtime.notification.mgt.api.service.RealtimeNotificationConfigurationService;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeInvalidNotificationConfigurationException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationConfigurationNotFoundException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationConfigurationServiceException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.service.RealtimeNotificationConfigService;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/notification-configuration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RealtimeNotificationConfigurationServiceImpl implements RealtimeNotificationConfigurationService {

    private static final Log log = LogFactory.getLog(RealtimeNotificationConfigurationServiceImpl.class);

    @GET
    @Override
    public Response getNotificationConfigurations(
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("name") String name,
            @QueryParam("type") String type,
            @QueryParam("deviceType") String deviceType,
            @QueryParam("code") String code) {
        try {
            RealtimeNotificationConfigService notificationConfigService =
                    RealtimeNotificationConfigurationApiUtil.getNotificationConfigurationService();
            RealtimeNotificationConfigurationList filteredConfigs =
                    notificationConfigService
                            .getFilteredNotificationConfigurations(name, type, code, deviceType, offset, limit);
            return Response.status(HttpStatus.SC_OK).entity(filteredConfigs).build();
        } catch (RealtimeNotificationConfigurationNotFoundException e) {
            String msg = "RealtimeNotification Configurations does not exist.";
            log.warn(e.getMessage());
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (RealtimeNotificationConfigurationServiceException e) {
            String msg = "Unexpected error occurred while retrieving notification configurations.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    public Response createNotificationConfig(RealtimeNotificationConfigurationList configurations) {
        try {
            RealtimeNotificationConfigService notificationConfigService =
                    RealtimeNotificationConfigurationApiUtil.getNotificationConfigurationService();
            RealtimeNotificationConfigurationList addedConfigurations =
                    notificationConfigService.addNotificationConfigContext(configurations);
            return Response.status(HttpStatus.SC_CREATED).entity(addedConfigurations).build();
        } catch (RealtimeInvalidNotificationConfigurationException e) {
            String msg = "Invalid configurations: ";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (RealtimeNotificationConfigurationServiceException e) {
            String msg = "Error creating notification configurations: " + e.getMessage();
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Override
    public Response updateNotificationConfig(RealtimeNotificationConfig config) {
        try {
            RealtimeNotificationConfigService notificationConfigService =
                    RealtimeNotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.getNotificationConfigByID(config.getId());
            notificationConfigService.updateNotificationConfigContext(config);
            return Response.status(HttpStatus.SC_OK).entity(config).build();
        } catch (RealtimeNotificationConfigurationNotFoundException e) {
            String msg = "RealtimeNotification configuration with ID " + config.getId() + " not found.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (RealtimeInvalidNotificationConfigurationException e) {
            String msg = "Invalid request: configuration or configuration ID is missing or invalid.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (RealtimeNotificationConfigurationServiceException e) {
            String msg = "Error updating notification configuration: " + e.getMessage();
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/{configId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response deleteNotificationConfig(@PathParam("configId") int configId) {
        try {
            RealtimeNotificationConfigService notificationConfigService =
                    RealtimeNotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.deleteNotificationConfigContext(configId);
            return Response.status(HttpStatus.SC_OK)
                    .entity("RealtimeNotification configuration deleted successfully.").build();
        } catch (RealtimeNotificationConfigurationNotFoundException e) {
            String msg = "Target notification configuration does not exist for the given tenant";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (RealtimeInvalidNotificationConfigurationException e) {
            String msg = "Invalid request: configuration or configuration ID is missing or invalid.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (RealtimeNotificationConfigurationServiceException e) {
            String msg = "Error occurred while deleting notification configuration with ID: " + configId;
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    public Response deleteNotificationConfigurations() {
        try {
            RealtimeNotificationConfigService notificationConfigService =
                    RealtimeNotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.deleteNotificationConfigurations();
            return Response.status(HttpStatus.SC_NO_CONTENT).build();
        } catch (RealtimeNotificationConfigurationServiceException e) {
            String msg = "No notification configuration was found for the tenant.";
            log.error(msg);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        }
    }

    @GET
    @Path("/{configId}")
    @Override
    public Response getNotificationConfig(@PathParam("configId") int configId) {
        try {
            RealtimeNotificationConfigService notificationConfigService =
                    RealtimeNotificationConfigurationApiUtil.getNotificationConfigurationService();
            RealtimeNotificationConfig config = notificationConfigService.getNotificationConfigByID(configId);
            return Response.status(HttpStatus.SC_OK).entity(config).build();
        } catch (RealtimeNotificationConfigurationNotFoundException e) {
            String msg = "Requested RealtimeNotification Configuration does not exist.";
            log.warn(e.getMessage());
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (RealtimeInvalidNotificationConfigurationException e) {
            String msg = "Invalid request: configuration or configuration ID is missing or invalid.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (RealtimeNotificationConfigurationServiceException e) {
            String msg = "Unexpected error occurred while retrieving notification configuration.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/defaults")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response updateDefaultArchiveSettings(RealtimeNotificationConfigurationList configList) {
        String defaultType = configList.getDefaultArchiveType();
        RealtimeArchivePeriod defaultAfter = configList.getDefaultArchiveAfter();
        try {
            if (defaultType == null || defaultType.isEmpty() ||
                    defaultAfter == null || defaultAfter.getValue() <= 0 || defaultAfter.getUnit() == null) {
                String msg = "Default archive type and period cannot be empty or invalid. Please provide valid values.";
                log.error(msg);
                return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
            }
            RealtimeNotificationConfigService notificationConfigService =
                    RealtimeNotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.setDefaultNotificationArchiveMetadata(defaultType, defaultAfter);
            return Response.status(HttpStatus.SC_OK).entity(configList).build();
        } catch (RealtimeInvalidNotificationConfigurationException e) {
            String msg = "Default archive type and period cannot be empty. Please provide valid values.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (RealtimeNotificationConfigurationServiceException e) {
            String msg = "Error occurred while updating the default archival settings";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }


    @GET
    @Path("/check")
    @Override
    public Response checkNotificationConfig(@QueryParam("deviceType") String deviceType,
                                            @QueryParam("code") String code) {
        try {
            RealtimeNotificationConfigService notificationConfigService =
                    RealtimeNotificationConfigurationApiUtil.getNotificationConfigurationService();
            boolean exists = notificationConfigService.configExists(deviceType, code);
            if (exists) {
                String msg = "A notification configuration already exists for deviceType=" + deviceType +
                        " and code=" + code;
                log.warn(msg);
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            } else {
                return Response.noContent().build();
            }
        } catch (RealtimeNotificationConfigurationNotFoundException e) {
            String msg = "RealtimeNotification Configurations does not exist.";
            log.warn(e.getMessage());
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (RealtimeInvalidNotificationConfigurationException e) {
            String msg = "Invalid request: device Type or the operation code is invalid.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (RealtimeNotificationConfigurationServiceException e) {
            String msg = "Error occurred while checking for existing notification configuration.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
