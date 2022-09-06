/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 *   Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.LifecycleStateDevice;
import org.wso2.carbon.device.mgt.common.MDMAppConstants;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import org.wso2.carbon.device.mgt.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceStatusException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidStatusException;
import org.wso2.carbon.device.mgt.common.exceptions.UserNotFoundException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.LifecycleStateDeviceList;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

    @Override
    @Path("/count")
    @GET
    public Response getDeviceCount(@QueryParam("status") String status) {
        int deviceCount;
        try {
            if (status == null) {
                deviceCount = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceCount();
            } else {
                deviceCount = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceCount(EnrolmentInfo.Status.valueOf(status));
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching device count.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceCount).build();
    }

    @PUT
    @Override
    @Path("/device-owner")
    public Response updateEnrollOwner(
            @QueryParam("owner") String owner,
            List<String> deviceIdentifiers){
        try {
            if (DeviceMgtAPIUtils.getDeviceManagementService().updateEnrollment(owner, true, deviceIdentifiers)) {
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
            String msg = "Error occurred when updating device owners.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UserNotFoundException e) {
            String msg = "Couldn't found the owner in user store to update the owner of devices.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @PUT
    @Override
    @Path("/permanent-delete")
    public Response deleteDevicesPermanently(List<String> deviceIdentifiers) {
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            if (!deviceManagementProviderService.deleteDevices(deviceIdentifiers)) {
                String msg = "Found un-deployed device type.";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
            return Response.status(Response.Status.OK).entity(true).build();
        } catch (DeviceManagementException e) {
            String msg = "Error encountered while permanently deleting devices";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        catch (InvalidDeviceException e) {
            String msg = "Found Invalid devices";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Path("/{deviceId}/{featureCode}")
    @Override
    public Response triggerCorrectiveActions(
            @PathParam("deviceId") String deviceIdentifier,
            @PathParam("featureCode") String featureCode,
            List<String> actions){
        DeviceManagementProviderService deviceManagementService = DeviceMgtAPIUtils.getDeviceManagementService();
        PlatformConfigurationManagementService platformConfigurationManagementService = DeviceMgtAPIUtils
                .getPlatformConfigurationManagementService();
        try {
            PlatformConfiguration config = platformConfigurationManagementService
                    .getConfiguration(MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
            List<ConfigurationEntry> configList = config.getConfiguration();
            deviceManagementService.triggerCorrectiveActions(deviceIdentifier, featureCode, actions, configList);
        } catch (ConfigurationManagementException e) {
            String msg = "Error occurred while processing platform configuration.";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Bad request, can't proceed. Hence verify the request and re-try";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting device data which has ID: " + deviceIdentifier;
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceNotFoundException e) {
            String msg = "Couldn't find an device for device identifier: " + deviceIdentifier;
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity("Triggered action successfully").build();
    }

    /**
     * This change the device status
     *
     * @param deviceId   Id of the device
     * @param nextStatus next status of the device
     * @return response
     */
    @POST
    @Path("/status")
    public Response changeDeviceStatus(
            @QueryParam("deviceId") String deviceId,
            @QueryParam("nextStatus") EnrolmentInfo.Status nextStatus) {

        try {
            if (nextStatus == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Next status is required")
                                .build()).build();
            }

            RequestValidationUtil.validateDeviceIdentifier(Constants.ANY, deviceId);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            Device device = dms.getDevice(deviceId, false);
            if (device == null) {
                String message = "Device does not exist with id '" + deviceId + "'";
                log.error(message);
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(message).build()).build();
            }
            LifecycleStateDevice updatedInfo = DeviceMgtAPIUtils.getDeviceStateManagementService()
                    .changeDeviceStatus(device, nextStatus);
            return Response.status(Response.Status.OK).entity(updatedInfo).build();
        } catch (InvalidStatusException e) {
            String msg = "Error occured while changing status: Invalid status or invalid status change";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting the device '" + deviceId + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceStatusException e) {
            String msg = "Error occurred while getting device Status";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * Get the devicelifecycle history
     *
     * @param type     Device type
     * @param deviceId Device id
     * @param offset is starting number of the record which we want tot get
     * @param limit is the number of records we want starting from offset
     * @return lifecycle history
     */

    @Override
    @GET
    @Path("/{type}/{deviceId}/lifecycle")
    public Response getDeviceLifecycle(
            @PathParam("type") String type,
            @PathParam("deviceId") String deviceId,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {

        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            PaginationRequest request = new PaginationRequest(offset, limit);
            PaginationResult result;

            RequestValidationUtil.validateDeviceIdentifier(type, deviceId);
            DeviceManagementProviderService deviceManagementProviderService =
                    DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, type);
            Device device = deviceManagementProviderService.getDevice(deviceIdentifier, false);
            if (device == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            LifecycleStateDeviceList states = new LifecycleStateDeviceList();
            result = DeviceMgtAPIUtils.getDeviceStateManagementService()
                    .getDeviceLifecycleHistory(request, device);
            states.setLifecycleStates((List<LifecycleStateDevice>) result.getData());
            states.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(states).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting the device '" + deviceId + "'";
            log.error(msg);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceStatusException e) {
            String msg = "Error occurred while getting device Status";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}
