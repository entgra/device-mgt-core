/*
 *  Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.jaxrs.service.api.MonitoringOperationConfigMetaService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the service class for metadata management.
 */
@Path("/monitoring-operation-config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MonitoringOperationConfigMetaServiceImpl implements MonitoringOperationConfigMetaService {

    private static final Log log = LogFactory.getLog(MonitoringOperationConfigMetaServiceImpl.class);

    @PUT
    @Override
    @Path("/{deviceType}")
    public Response updateMonitoringOperationConfig(@PathParam("deviceType") String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig) {
        try {
            RequestValidationUtil.validateMonitoringOperationConfig(operationMonitoringTaskConfig, deviceType);
            OperationMonitoringTaskConfig newOperationMonitoringTaskConfig = DeviceMgtAPIUtils.getMonitoringOperationTaskConfigManagementService().
                    updateMonitoringOperationTaskConfig(deviceType, operationMonitoringTaskConfig);
            return Response.status(Response.Status.CREATED).entity(newOperationMonitoringTaskConfig).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while creating monitoring operation task config";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating device type " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/{deviceType}")
    public Response getMonitoringOperationConfig(@PathParam("deviceType") String deviceType) {
        try {
            RequestValidationUtil.validateDeviceType(deviceType);
            OperationMonitoringTaskConfig whiteLabelTheme = DeviceMgtAPIUtils.getMonitoringOperationTaskConfigManagementService().
                    getMonitoringOperationTaskConfig(deviceType);
            return Response.status(Response.Status.OK).entity(whiteLabelTheme).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting monitoring operation task config";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating device type " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}