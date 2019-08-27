/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.UserNotFoundException;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/admin/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceManagementAdminServiceImpl implements DeviceManagementAdminService {

    private static final Log log = LogFactory.getLog(DeviceManagementAdminServiceImpl.class);

    @Override
    @GET
    public Response getDevicesByName(@QueryParam("name") @Size(max = 45) String name,
                                     @QueryParam("type") @Size(min = 2, max = 45) String type,
                                     @QueryParam("tenant-domain") String tenantDomain,
                                     @HeaderParam("If-Modified-Since") String ifModifiedSince,
                                     @QueryParam("offset") int offset,
                                     @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        int currentTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (MultitenantConstants.SUPER_TENANT_ID != currentTenantId) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(
                            "Current logged in user is not authorized to perform this operation").build()).build();
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(DeviceMgtAPIUtils.getTenantId(tenantDomain));

            PaginationRequest request = new PaginationRequest(offset, limit);
            request.setDeviceType(type);
            request.setDeviceName(name);
            List<Device> devices = DeviceMgtAPIUtils.getDeviceManagementService().
                    getDevicesByNameAndType(request, false);

            // setting up paginated result
            DeviceList deviceList = new DeviceList();
            deviceList.setList(devices);
            deviceList.setCount(devices.size());

            return Response.status(Response.Status.OK).entity(deviceList).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred at server side while fetching device list.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @PUT
    @Override
    @Path("/device-owner")
    public Response updateEnrollOwner(
            @QueryParam("owner") String owner,
            List<String> deviceIdentifiers){
        try {
            if (DeviceMgtAPIUtils.getDeviceManagementService().updateEnrollment(owner, deviceIdentifiers)) {
                String msg = "Device owner is updated successfully.";
                return Response.status(Response.Status.OK).entity(msg).build();
            }
            String msg = "Device owner updating is failed.";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidDeviceException e) {
            String msg = "Invalid device identifiers are found with the request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred when updating the device owner.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UserNotFoundException e) {
            String msg = "Couldn't found the owner in user store to update the owner of devices.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }
}