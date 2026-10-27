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

package io.entgra.device.mgt.core.realtime.notification.mgt.api.impl;

import io.entgra.device.mgt.core.realtime.notification.mgt.api.beans.RealtimeNotificationActionRequest;
import io.entgra.device.mgt.core.realtime.notification.mgt.api.beans.RealtimeUserNotificationsRequest;
import io.entgra.device.mgt.core.realtime.notification.mgt.api.beans.RealtimeUsernameWithNotificationIdsRequest;
import io.entgra.device.mgt.core.realtime.notification.mgt.api.service.RealtimeNotificationService;
import io.entgra.device.mgt.core.realtime.notification.mgt.api.util.RealtimeNotificationManagementApiUtil;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeNotificationHelper;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.dto.RealtimeNotification;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.dto.RealtimePaginatedUserNotificationResponse;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationArchivalException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationManagementException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.service.RealtimeNotificationManagementService;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/notifications")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RealtimeNotificationServiceImpl implements RealtimeNotificationService {
    private static final Log log = LogFactory.getLog(RealtimeNotificationServiceImpl.class);

    @GET
    @Override
    public Response getAllNotifications(
            @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        RealtimeNotificationManagementService notificationService =
                RealtimeNotificationManagementApiUtil.getNotificationManagementService();
        try {
            List<RealtimeNotification> notifications = notificationService.getAllNotifications(offset, limit);
            if (notifications == null) {
                notifications = new ArrayList<>();
            }
            return Response.status(HttpStatus.SC_OK).entity(notifications).build();
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Error occurred while retrieving notifications";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/user-notifications")
    public Response getUserNotificationsWithStatus(RealtimeUserNotificationsRequest request) {
        RealtimeNotificationManagementService notificationService =
                RealtimeNotificationManagementApiUtil.getNotificationManagementService();
        try {
            if (request == null) {
                return Response.status(HttpStatus.SC_BAD_REQUEST).entity("Request body is required").build();
            }
            String authenticatedUser = RealtimeNotificationHelper.getAuthenticatedUser();
            RealtimePaginatedUserNotificationResponse response =
                    notificationService.getUserNotificationsWithStatus(
                            authenticatedUser, request.getLimit(), request.getOffset(), request.getIsRead());
            return Response.status(HttpStatus.SC_OK).entity(response).build();
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Failed to retrieve user notifications with status.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/action")
    public Response updateNotificationAction(RealtimeNotificationActionRequest request) {
        RealtimeNotificationManagementService notificationService =
                RealtimeNotificationManagementApiUtil.getNotificationManagementService();
        try {
            if (request == null) {
                return Response.status(HttpStatus.SC_BAD_REQUEST).entity("Request body is required").build();
            }
            String authenticatedUser = RealtimeNotificationHelper.getAuthenticatedUser();
            notificationService.updateNotificationActionForUser(
                    request.getNotificationIds(), authenticatedUser, request.isRead());
            String status = request.isRead() ? "READ" : "UNREAD";
            return Response.status(HttpStatus.SC_OK)
                    .entity("RealtimeNotification(s) marked as " + status).build();
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Failed to update notification action.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/action/all")
    public Response updateAllNotificationAction(@QueryParam("read") boolean isRead) {
        RealtimeNotificationManagementService notificationService =
                RealtimeNotificationManagementApiUtil.getNotificationManagementService();
        try {
            String authenticatedUser = RealtimeNotificationHelper.getAuthenticatedUser();
            notificationService.updateAllNotificationActionForUser(authenticatedUser, isRead);
            String status = isRead ? "READ" : "UNREAD";
            return Response.status(HttpStatus.SC_OK)
                    .entity("All notifications marked as " + status).build();
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Failed to update all notification actions.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }


    @DELETE
    @Path("/user-notifications")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSelectedNotifications(RealtimeUsernameWithNotificationIdsRequest request) {
        RealtimeNotificationManagementService notificationService =
                RealtimeNotificationManagementApiUtil.getNotificationManagementService();
        try {
            if (request == null) {
                return Response.status(HttpStatus.SC_BAD_REQUEST).entity("Request body is required").build();
            }
            String authenticatedUser = RealtimeNotificationHelper.getAuthenticatedUser();
            Map<String, List<Integer>> result = notificationService.deleteUserNotifications(
                    request.getNotificationIds(), authenticatedUser);
            return Response.status(HttpStatus.SC_OK).entity(result).build();
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Failed to delete selected notifications.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/user-notifications/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllNotifications(
            @QueryParam("read") Boolean isRead) {
        RealtimeNotificationManagementService notificationService =
                RealtimeNotificationManagementApiUtil.getNotificationManagementService();
        try {
            String authenticatedUser = RealtimeNotificationHelper.getAuthenticatedUser();
            notificationService.deleteAllUserNotifications(authenticatedUser, isRead);
            String msg = "All notifications deleted successfully";
            if (isRead != null) {
                msg = isRead ? "All read notifications deleted successfully" : "All unread notifications deleted successfully";
            }
            return Response.status(HttpStatus.SC_OK).entity(msg).build();
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Failed to delete all notifications.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/archive-selected")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response archiveSelectedNotifications(RealtimeUsernameWithNotificationIdsRequest request) {
        RealtimeNotificationManagementService notificationService =
                RealtimeNotificationManagementApiUtil.getNotificationManagementService();
        try {
            if (request == null) {
                return Response.status(HttpStatus.SC_BAD_REQUEST).entity("Request body is required").build();
            }
            String authenticatedUser = RealtimeNotificationHelper.getAuthenticatedUser();
            Map<String, List<Integer>> result = notificationService.archiveUserNotifications(
                    request.getNotificationIds(), authenticatedUser);
            return Response.status(HttpStatus.SC_OK).entity(result).build();
        } catch (RealtimeNotificationArchivalException e) {
            String msg = "Error archiving selected notifications.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Error occurred while getting authenticated user.";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/archive-all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response archiveAllNotifications() {
        RealtimeNotificationManagementService notificationService =
                RealtimeNotificationManagementApiUtil.getNotificationManagementService();
        try {
            String authenticatedUser = RealtimeNotificationHelper.getAuthenticatedUser();
            notificationService.archiveAllUserNotifications(authenticatedUser);
            String msg = "All notifications archived successfully";
            return Response.status(HttpStatus.SC_OK).entity(msg).build();
        } catch (RealtimeNotificationArchivalException e) {
            String msg = "Error archiving all notifications.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RealtimeNotificationManagementException e) {
            String msg = "Error occurred while getting authenticated user.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
