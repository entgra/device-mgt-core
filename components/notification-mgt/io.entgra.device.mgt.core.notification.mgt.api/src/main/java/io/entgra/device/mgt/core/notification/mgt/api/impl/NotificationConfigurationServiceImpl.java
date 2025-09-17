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

package io.entgra.device.mgt.core.notification.mgt.api.impl;

import io.entgra.device.mgt.core.notification.mgt.api.util.NotificationConfigurationApiUtil;
import io.entgra.device.mgt.core.notification.mgt.common.beans.ArchivePeriod;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;

import io.entgra.device.mgt.core.notification.mgt.api.service.NotificationConfigurationService;
import io.entgra.device.mgt.core.notification.mgt.common.exception.InvalidNotificationConfigurationException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationNotFoundException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationConfigService;
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
public class NotificationConfigurationServiceImpl implements NotificationConfigurationService {

    private static final Log log = LogFactory.getLog(NotificationConfigurationServiceImpl.class);

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
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            NotificationConfigurationList filteredConfigs =
                    notificationConfigService
                            .getFilteredNotificationConfigurations(name, type, code, deviceType, offset, limit);
            return Response.status(HttpStatus.SC_OK).entity(filteredConfigs).build();
        } catch (NotificationConfigurationNotFoundException e) {
            String msg = "Notification Configurations does not exist.";
            log.warn(e.getMessage());
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Unexpected error occurred while retrieving notification configurations.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    public Response createNotificationConfig(NotificationConfigurationList configurations) {
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            NotificationConfigurationList addedConfigurations =
                    notificationConfigService.addNotificationConfigContext(configurations);
            return Response.status(HttpStatus.SC_CREATED).entity(addedConfigurations).build();
        } catch (InvalidNotificationConfigurationException e) {
            String msg = "Invalid configurations: ";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Error creating notification configurations: " + e.getMessage();
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Override
    public Response updateNotificationConfig(NotificationConfig config) {
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.getNotificationConfigByID(config.getId());
            notificationConfigService.updateNotificationConfigContext(config);
            return Response.status(HttpStatus.SC_OK).entity(config).build();
        } catch (NotificationConfigurationNotFoundException e) {
            String msg = "Notification configuration with ID " + config.getId() + " not found.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (InvalidNotificationConfigurationException e) {
            String msg = "Invalid request: configuration or configuration ID is missing or invalid.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (NotificationConfigurationServiceException e) {
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
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.deleteNotificationConfigContext(configId);
            return Response.status(HttpStatus.SC_OK)
                    .entity("Notification configuration deleted successfully.").build();
        } catch (NotificationConfigurationNotFoundException e) {
            String msg = "Target notification configuration does not exist for the given tenant";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (InvalidNotificationConfigurationException e) {
            String msg = "Invalid request: configuration or configuration ID is missing or invalid.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Error occurred while deleting notification configuration with ID: " + configId;
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    public Response deleteNotificationConfigurations() {
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.deleteNotificationConfigurations();
            return Response.status(HttpStatus.SC_NO_CONTENT).build();
        } catch (NotificationConfigurationServiceException e) {
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
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            NotificationConfig config = notificationConfigService.getNotificationConfigByID(configId);
            return Response.status(HttpStatus.SC_OK).entity(config).build();
        } catch (NotificationConfigurationNotFoundException e) {
            String msg = "Requested Notification Configuration does not exist.";
            log.warn(e.getMessage());
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (InvalidNotificationConfigurationException e) {
            String msg = "Invalid request: configuration or configuration ID is missing or invalid.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (NotificationConfigurationServiceException e) {
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
    public Response updateDefaultArchiveSettings(NotificationConfigurationList configList) {
        String defaultType = configList.getDefaultArchiveType();
        ArchivePeriod defaultAfter = configList.getDefaultArchiveAfter();
        try {
            if (defaultType == null || defaultType.isEmpty() ||
                    defaultAfter == null || defaultAfter.getValue() <= 0 || defaultAfter.getUnit() == null) {
                String msg = "Default archive type and period cannot be empty or invalid. Please provide valid values.";
                log.error(msg);
                return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
            }
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.setDefaultNotificationArchiveMetadata(defaultType, defaultAfter);
            return Response.status(HttpStatus.SC_OK).entity(configList).build();
        } catch (InvalidNotificationConfigurationException e) {
            String msg = "Default archive type and period cannot be empty. Please provide valid values.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (NotificationConfigurationServiceException e) {
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
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            boolean exists = notificationConfigService.configExists(deviceType, code);
            if (exists) {
                String msg = "A notification configuration already exists for deviceType=" + deviceType +
                        " and code=" + code;
                log.warn(msg);
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            } else {
                return Response.noContent().build();
            }
        } catch (NotificationConfigurationNotFoundException e) {
            String msg = "Notification Configurations does not exist.";
            log.warn(e.getMessage());
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (InvalidNotificationConfigurationException e) {
            String msg = "Invalid request: device Type or the operation code is invalid.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Error occurred while checking for existing notification configuration.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
