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

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.SSOConfigurationService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.SSOConfigurationManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

@Path("/sso-config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SSOConfigurationServiceImpl implements SSOConfigurationService {

    private static final Log log = LogFactory.getLog(SSOConfigurationServiceImpl.class);

    @Override
    public Response getSSOConfiguration() {
        int tenantId = 0;
        try {
            tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            SSOConfigurationManagementService ssoService = DeviceMgtAPIUtils.getSSOConfigurationManagementService();
            Map<String, String> ssoConfig = ssoService.getSSOConfiguration(tenantId);
            return Response.status(Response.Status.OK).entity(ssoConfig).build();
        } catch (MetadataKeyNotFoundException e) {
            String msg = "SSO Configuration not found for tenant ID: " + tenantId;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(Collections.singletonMap("error", msg)).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while retrieving SSO configuration.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("error", msg)).build();
        }
    }
}
