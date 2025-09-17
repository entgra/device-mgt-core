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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.DeviceFeatureOperationService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.dto.DeviceFeatureInfo;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceFeatureOperationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.GET;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/device-operations")
public class DeviceFeatureOperationServiceImpl implements DeviceFeatureOperationService {
    private static final Log log = LogFactory.getLog(DeviceFeatureOperationServiceImpl.class);
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOperationDetails(
            @QueryParam("code") String code,
            @QueryParam("name") String name,
            @QueryParam("type") String type,
            @QueryParam("removeDeduplicateCode") @DefaultValue("false") boolean deduplicate) {
        try {
            List<DeviceFeatureInfo> operationList =
                    DeviceMgtAPIUtils.getDeviceFeatureOperations().getOperationDetails(code, name, type, deduplicate);
            return Response.ok(operationList).build();
        } catch (DeviceFeatureOperationException e) {
            String msg = "Error occurred while retrieving operation details.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}

