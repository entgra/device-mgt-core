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
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationManagementService;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
}
