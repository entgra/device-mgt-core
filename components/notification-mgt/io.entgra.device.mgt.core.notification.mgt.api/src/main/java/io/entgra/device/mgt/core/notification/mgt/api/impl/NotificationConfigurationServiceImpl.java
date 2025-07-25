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
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;

import io.entgra.device.mgt.core.notification.mgt.api.service.NotificationConfigurationService;
import io.entgra.device.mgt.core.notification.mgt.common.exception.InvalidNotificationConfigurationException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            @QueryParam("code") String code) {
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            NotificationConfigurationList filteredConfigs =
                    notificationConfigService.getFilteredNotificationConfigurations(name, type, code, offset, limit);
            return Response.status(HttpStatus.SC_OK).entity(filteredConfigs).build();
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
            String msg = "Invalid configurations: " + String.join(", ", e.getValidationErrors());
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Error creating notification configurations: " + e.getMessage();
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/{configId}")
    @Override
    public Response updateNotificationConfigById(@PathParam("configId") int configId,
                                                 NotificationConfig config) {
        try {
            if (configId != config.getId()) {
                String msg = "Path ID " + configId + " does not match configuration ID " + config.getId();
                log.error(msg);
                return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
            }
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            NotificationConfig existingConfig = notificationConfigService.getNotificationConfigByID(configId);
            if (existingConfig == null) {
                String msg = "Configuration with ID " + configId + " not found";
                log.error(msg);
                return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
            }
            notificationConfigService.updateNotificationConfigContext(config);
            return Response.status(HttpStatus.SC_OK).entity(config).build();
        } catch (NotificationConfigurationServiceException e) {
            if (e.getMessage().contains("not found")) {
                return Response.status(HttpStatus.SC_NOT_FOUND).entity(e.getMessage()).build();
            }
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
            if (configId <= 0) {
                String msg = "Received empty or Invalid Configuration ID";
                log.error(msg);
                return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
            }
            notificationConfigService.deleteNotificationConfigContext(configId);
            return Response.status(HttpStatus.SC_OK).entity("Notification configuration deleted successfully.").build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Error occurred while deleting notification configuration with ID: " + configId;
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response deleteNotificationConfigurations() {
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.deleteNotificationConfigurations();
            return Response.status(HttpStatus.SC_NO_CONTENT).build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "No configurations found for the tenant: " + e.getMessage();
            log.error(msg);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        }
    }

    @GET
    @Path("/{configId}")
    @Override
    public Response getNotificationConfig(@PathParam("configId") int configId) {
        try {
            if (configId <= 0) {
                String msg = "Invalid configuration ID: " + configId;
                log.error(msg);
                return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
            }
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            NotificationConfig config = notificationConfigService.getNotificationConfigByID(configId);
            if (config == null) {
                String msg = "Notification configuration with ID '" + configId + "' not found.";
                log.error(msg);
                return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
            }
            return Response.status(HttpStatus.SC_OK).entity(config).build();
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
        String defaultAfter = configList.getDefaultArchiveAfter();
        if (defaultType == null || defaultAfter == null ||
                defaultType.isEmpty() || defaultAfter.isEmpty()) {
            String msg = "Default archive type and period must not be empty.";
            log.error(msg);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(msg).build();
        }
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.setDefaultNotificationArchiveMetadata(defaultType, defaultAfter);
            return Response.status(HttpStatus.SC_OK).entity(configList).build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Error updating default archival settings: " + e.getMessage();
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
