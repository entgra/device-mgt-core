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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.DeviceStatisticManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.type.statistic.DeviceTypeStatistic;
import io.entgra.device.mgt.core.device.mgt.common.type.MetadataResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;

public class DeviceStatisticManagementServiceImpl implements DeviceStatisticManagementService {
    private static final Log log = LogFactory.getLog(DeviceStatisticManagementServiceImpl.class);

    @GET
    @Path("/{type}")
    @Override
    public Response getDeviceTypeStatisticDefinitions(@PathParam("type") String deviceType) {
        try {
            if (deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
            }
            MetadataResult<DeviceTypeStatistic> result = DeviceMgtAPIUtils
                    .getDeviceTypeStatisticManagementProviderService()
                    .getDeviceTypeStatisticsDefinitions(deviceType);
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred at server side while" +
                    " fetching device type statistic definitions for type: " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/{type}")
    @Override
    public Response createDeviceTypeStatisticDefinitions(@PathParam("type") String deviceType,
                                                         @Valid List<DeviceTypeStatistic> deviceTypeStatistic) {
        try {
            DeviceMgtAPIUtils.getDeviceTypeStatisticManagementProviderService()
                    .createDeviceTypeStatisticsDefinitions(deviceType, deviceTypeStatistic);
            return Response.status(Response.Status.CREATED).entity("Device type statistic definitions created" +
                    " and metadata created successfully.").build();
        } catch (DeviceManagementException e) {
            String msg = "Error while updating device type statistic definitions for device type: " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/{type}")
    @Override
    public Response updateDeviceTypeStatisticDefinitions(@PathParam("type") String deviceType,
                                                         @Valid List<DeviceTypeStatistic> deviceTypeStatistic) {
        try {
            DeviceMgtAPIUtils.getDeviceTypeStatisticManagementProviderService()
                    .updateDeviceTypeStatisticsDefinitions(deviceType, deviceTypeStatistic);
            return Response.ok().entity("Device type statistic definitions updated" +
                    " and metadata updated successfully.").build();
        } catch (DeviceManagementException e) {
            String msg = "Error while updating device type statistic definitions for device type: " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response deleteDeviceTypeStatisticDefinitions(String deviceType) {
        try {
            if (deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type for deletion";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
            }
            DeviceMgtAPIUtils.getDeviceTypeStatisticManagementProviderService()
                    .deleteDeviceTypeStatisticsDefinitions(deviceType);
            return Response.ok().entity("Device type statistic definitions deleted successfully").build();
        } catch (DeviceManagementException e) {
            String msg = "Error while deleting device type statistic definitions for device type: " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}

