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

package io.entgra.device.mgt.core.notification.mgt.api.impl;

import io.entgra.device.mgt.core.notification.mgt.api.service.NotificationService;
import io.entgra.device.mgt.core.notification.mgt.api.util.NotificationManagementApiUtil;
import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.dto.PaginatedUserNotificationResponse;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationPayload;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationManagementService;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/notifications")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NotificationServiceImpl implements NotificationService {
    private static final Log log = LogFactory.getLog(NotificationServiceImpl.class);

    @GET
    @Override
    public Response getLatestNotifications(
        @QueryParam("offset") int offset, @QueryParam("limit") int limit)  {
        NotificationManagementService notificationService = NotificationManagementApiUtil.getNotificationManagementService();
        try {
            List<Notification> notifications = notificationService.getLatestNotifications(offset, limit);
            if (notifications == null) {
                return Response.status(HttpStatus.SC_NOT_FOUND).entity("No notifications found").build();
            }
            return Response.status(HttpStatus.SC_OK).entity(notifications).build();
        } catch (NotificationManagementException e) {
            String msg = "Error occurred while retrieving notifications";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/user")
    public Response getUserNotificationsWithStatus(@QueryParam("username") String username,
                                                   @QueryParam("status") Boolean isRead,
                                                   @QueryParam("limit") int limit,
                                                   @QueryParam("offset") int offset) {
        NotificationManagementService notificationService =
                NotificationManagementApiUtil.getNotificationManagementService();
        try {
            List<UserNotificationPayload> payloads =
                    notificationService.getUserNotificationsWithStatus(username, limit, offset, isRead);
            int totalCount = notificationService.getUserNotificationCount(username, isRead);
            PaginatedUserNotificationResponse response =
                    new PaginatedUserNotificationResponse(payloads, totalCount);
            return Response.status(HttpStatus.SC_OK).entity(response).build();
        } catch (NotificationManagementException e) {
            String msg = "Failed to retrieve user notifications with status for user: " + username;
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/mark-action")
    public Response updateNotificationAction(@QueryParam("notificationId") List<Integer> notificationIds,
                                             @QueryParam("username") String username,
                                             @QueryParam("action") String actionType) {
        NotificationManagementService notificationService =
                NotificationManagementApiUtil.getNotificationManagementService();
        try {
            notificationService.updateNotificationActionForUser(notificationIds, username, actionType);
            return Response.status(HttpStatus.SC_OK)
                    .entity("Notification(s) marked as " + actionType).build();
        } catch (NotificationManagementException e) {
            String msg = "Failed to update notification action for user: " + username;
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUserNotifications(
            @QueryParam("username") String username,
            @QueryParam("all") @DefaultValue("false") boolean deleteAll,
            List<Integer> notificationIds
    ) {
        NotificationManagementService notificationService =
                NotificationManagementApiUtil.getNotificationManagementService();
        try {
            if (deleteAll) {
                notificationService.deleteAllUserNotifications(username);
            } else {
                notificationService.deleteUserNotifications(notificationIds, username);
            }
            return Response.status(HttpStatus.SC_OK).entity("Notifications deleted successfully").build();
        } catch (NotificationManagementException e) {
            String msg = "Failed to delete notifications for user: " + username;
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/archive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response archiveUserNotifications(
            @QueryParam("username") String username,
            @QueryParam("all") @DefaultValue("false") boolean archiveAll,
            List<Integer> notificationIds
    ) {
        NotificationManagementService notificationService =
                NotificationManagementApiUtil.getNotificationManagementService();
        try {
            if (archiveAll) {
                notificationService.archiveAllUserNotifications(username);
            } else {
                notificationService.archiveUserNotifications(notificationIds, username);
            }
            return Response.status(HttpStatus.SC_OK).entity("Notifications archived successfully").build();
        } catch (NotificationArchivalException e) {
            String msg = "Error occurred during archiving notifications for user: " + username;
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
