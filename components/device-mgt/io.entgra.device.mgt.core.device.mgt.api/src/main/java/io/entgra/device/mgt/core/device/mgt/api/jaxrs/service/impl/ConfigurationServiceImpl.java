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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.ConfigurationManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.MDMAppConstants;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationEntry;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.PlatformConfiguration;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyManagementException;
import io.entgra.device.mgt.core.policy.mgt.core.util.PolicyManagerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/configuration")
public class ConfigurationServiceImpl implements ConfigurationManagementService {

    private static final Log log = LogFactory.getLog(ConfigurationServiceImpl.class);

    @GET
    @Override
    public Response getConfiguration(@HeaderParam("If-Modified-Since") String ifModifiedSince) {
        String msg;
        try {
            PlatformConfiguration config = DeviceMgtAPIUtils.getPlatformConfigurationManagementService().
                    getConfiguration(MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
            ConfigurationEntry configurationEntry = new ConfigurationEntry();
            configurationEntry.setContentType("text");
            configurationEntry.setName("notifierFrequency");
            configurationEntry.setValue(PolicyManagerUtil.getMonitoringFrequency());
            List<ConfigurationEntry> configList = config.getConfiguration();
            if (configList == null) {
                configList = new ArrayList<>();
                configList.add(configurationEntry);
            }
            config.setConfiguration(configList);
            return Response.ok().entity(config).build();
        } catch (ConfigurationManagementException | PolicyManagementException e) {
            msg = "Error occurred while retrieving the general platform configuration";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Override
    public Response updateConfiguration(PlatformConfiguration config) {
        try {
            RequestValidationUtil.validateUpdateConfiguration(config);
            DeviceMgtAPIUtils.getPlatformConfigurationManagementService().saveConfiguration(config,
                    MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
            //Schedule the task service
            DeviceMgtAPIUtils.scheduleTaskService(DeviceMgtAPIUtils.getNotifierFrequency(config));

            PlatformConfiguration updatedConfig = DeviceMgtAPIUtils.getPlatformConfigurationManagementService().
                    getConfiguration(MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
            return Response.ok().entity(updatedConfig).build();
        } catch (ConfigurationManagementException e) {
            String msg = "Error occurred while updating the general platform configuration";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}
