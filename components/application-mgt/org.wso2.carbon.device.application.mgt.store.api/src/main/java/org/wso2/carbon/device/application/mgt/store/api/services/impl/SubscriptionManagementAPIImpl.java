/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.application.mgt.store.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponse;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.store.api.services.SubscriptionManagementAPI;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Implementation of Subscription Management related APIs.
 */
@Produces({"application/json"})
@Path("/subscription")
public class SubscriptionManagementAPIImpl implements SubscriptionManagementAPI{

    private static Log log = LogFactory.getLog(SubscriptionManagementAPIImpl.class);

    @Override
    @POST
    @Path("/install/{uuid}/devices")
    public Response installApplicationForDevices(
            @PathParam("uuid") String uuid,
            @Valid List<DeviceIdentifier> deviceIdentifiers) {
        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
            ApplicationInstallResponse response = subscriptionManager
                    .installApplicationForDevices(uuid, deviceIdentifiers);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUI: " + uuid;
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for installing application which has UUID: " + uuid
                    + ". Hence verify the payload";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to install the aplication.";
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg =
                    "Error occurred while installing the application release which has UUID: " + uuid + " for devices";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @POST
    @Path("/install/{uuid}/{subType}")
    public Response addBulkAppInstalltion(
            @PathParam("uuid") String uuid,
            @PathParam("uuid") String subType,
            @Valid List<String> subscribers) {
        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
            ApplicationInstallResponse response = subscriptionManager.addBulkApInstallation(uuid, subscribers, subType);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUID: " + uuid + ". Hence, verify the payload";
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for installing application which has UUID: " + uuid
                    + ". Hence verify the payload";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to install "
                    + "the application.";
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while installing the application release which has UUID: " + uuid
                    + " for user devices";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    //todo remove following unwanted APIs

    @Override
    @POST
    @Path("/install/{uuid}/users")
    public Response installApplicationForUsers(
            @PathParam("uuid") String uuid,
            @Valid List<String> users) {
        if (users.isEmpty()) {
            String msg = "In order to install application release which has UUID " + uuid + ", you should provide list "
                    + "of users. But found an empty list of users.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
            ApplicationInstallResponse response = subscriptionManager.installApplicationForUsers(uuid, users);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUID: " + uuid + ". Hence, verify the payload";
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for installing application which has UUID: " + uuid
                    + ". Hence verify the payload";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to install "
                    + "the application.";
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while installing the application release which has UUID: " + uuid
                    + " for user devices";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @POST
    @Path("/install/{uuid}/roles")
    public Response installApplicationForRoles(
            @PathParam("uuid") String uuid,
            @Valid List<String> roles) {
        return Response.status(Response.Status.BAD_REQUEST).entity("").build();
    }

    @Override
    @POST
    @Path("/install/{uuid}/groups")
    public Response installApplicationForGroups(
            @PathParam("uuid") String uuid,
            @Valid List<String> groups) {
        return Response.status(Response.Status.BAD_REQUEST).entity("").build();
    }


    @Override
    @POST
    @Path("/uninstall/{uuid}/devices")
    public Response uninstallApplicationForDevices(
            @PathParam("uuid") String uuid,
            @Valid List<DeviceIdentifier> deviceIdentifiers) {
        return Response.status(Response.Status.BAD_REQUEST).entity("").build();
    }

    @Override
    @POST
    @Path("/uninstall/{uuid}/users")
    public Response uninstallApplicationForUsers(
            @PathParam("uuid") String uuid,
            @Valid List<String> users) {
        return Response.status(Response.Status.BAD_REQUEST).entity("").build();
    }

    @Override
    @POST
    @Path("/uninstall/{uuid}/roles")
    public Response uninstallApplicationForRoles(
            @PathParam("uuid") String uuid,
            @Valid List<String> roles) {
        return Response.status(Response.Status.BAD_REQUEST).entity("").build();
    }

    @Override
    @POST
    @Path("/uninstall/{uuid}/groups")
    public Response uninstallApplicationForGroups(
            @PathParam("uuid") String uuid,
            @Valid List<String> groups) {
        return Response.status(Response.Status.BAD_REQUEST).entity("").build();

    }

}
