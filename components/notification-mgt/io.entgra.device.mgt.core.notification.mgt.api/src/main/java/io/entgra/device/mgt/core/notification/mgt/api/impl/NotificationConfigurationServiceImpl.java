/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
import io.entgra.device.mgt.core.notification.mgt.api.util.NotificationManagementApiUtil;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;

import io.entgra.device.mgt.core.notification.mgt.api.service.NotificationConfigurationService;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationConfigService;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationManagementService;
import io.entgra.device.mgt.core.notification.mgt.core.impl.NotificationConfigServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/notification-configuration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NotificationConfigurationServiceImpl implements NotificationConfigurationService {

    private static final Log log = LogFactory.getLog(NotificationConfigurationServiceImpl.class);

    private boolean configurationsAreEmpty(NotificationConfigurationList configurations) {
        return configurations == null;
    }
    private boolean configurationIsEmpty(NotificationConfig configuration) {
        return configuration == null;
    }
    private boolean configurationIsValid(NotificationConfig config) {
        return config.getRecipients() != null;
    }
    private boolean configIDIsNUll(NotificationConfig config) {
        return config.getId() == null;
    }

    public Response createNotificationConfig(NotificationConfigurationList configurations)  {
        try {
            NotificationConfigurationList validConfigurations = new NotificationConfigurationList();
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            for (NotificationConfig config : configurations.getList()) {
                if (!configurationIsValid(config)) {
                    log.warn("Skipping invalid configuration: " + config);
                    continue; // Skip invalid config
                }
                validConfigurations.add(config);
            }
            if (validConfigurations.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Empty valid configurations");
                }
                return Response.status(Response.Status.BAD_REQUEST).entity("Empty Configurations.").build();
            }
            notificationConfigService.addNotificationConfigContext(validConfigurations);
            return Response.status(Response.Status.CREATED).entity("Notification configuration(s) created successfully.").build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Unexpected error occurred while creating notification configurations.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Response updateNotificationConfig(NotificationConfig config) {
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            if (configurationIsEmpty(config)) {
                String msg = "Configuration object cannot be null.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            if (configIDIsNUll(config)) {
                String msg = "Configuration ID is missing. Cannot update the notification configuration.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            if (configurationIsValid(config)) {
                    String msg = "Invalid configuration: Missing configType or recipients.";
                    log.error(msg);
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }
                log.info("Processing configuration: " + config.toString());
                notificationConfigService.updateNotificationConfigContext(config);
                return Response.status(Response.Status.OK).entity("Notification configuration(s) updated successfully.").build();
        }
         catch (NotificationConfigurationServiceException e) {
            String msg = "Error occurred while updating Meta data";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error Occured while updating Notification configuration(s) .").build();
        }
    }

    @Override
    public Response deleteNotificationConfig(String configId) {
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            if (configId == null) {
                String msg = "Received empty Configuration ID";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            notificationConfigService.deleteNotificationConfigContext(configId);
            return Response.status(Response.Status.NO_CONTENT).entity("Notification configuration deleted successfully.").build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Error occurred while deleting notification configuration(s).";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response deleteNotificationConfigurations()  {
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            notificationConfigService.deleteNotificationConfigurations();
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "No configurations found for the tenant: " + e.getMessage();
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
    }

    public Response getNotificationConfig(String configID)  {
        try {
            NotificationConfigService notificationConfigService =
                    NotificationConfigurationApiUtil.getNotificationConfigurationService();
            NotificationConfig config = notificationConfigService.getNotificationConfigByID(configID);
            if (config == null) {
                String msg = "Notification configuration with ID '" + configID + "' not found.";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(config).build();
        } catch (NotificationConfigurationServiceException e) {
            String msg = "Unexpected error occurred while retrieving notification configuration.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
