/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.subtype.mgt.api.service.impl;

import io.entgra.device.mgt.core.subtype.mgt.api.exception.BadRequestException;
import io.entgra.device.mgt.core.subtype.mgt.api.service.SubtypeManagementService;
import io.entgra.device.mgt.core.subtype.mgt.api.util.APIUtil;
import io.entgra.device.mgt.core.subtype.mgt.api.util.RequestValidationUtil;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtPluginException;
import io.entgra.device.mgt.core.subtype.mgt.spi.DeviceSubTypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.ws.rs.core.Response;
import java.util.List;

public class SubtypeManagementServiceImpl implements SubtypeManagementService {
    private static final Log log = LogFactory.getLog(SubtypeManagementServiceImpl.class);

    @Override
    public Response addDeviceSubType(DeviceSubType deviceSubType) {
        try {
            RequestValidationUtil.validateSubType(deviceSubType);
            DeviceSubTypeService deviceSubTypeService = APIUtil.getSubtypeManagementService();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (deviceSubTypeService.checkDeviceSubTypeExist(deviceSubType.getSubTypeId(),
                    tenantId, deviceSubType.getDeviceType())) {
                String msg = "Already exist subtype for subtype id: " + deviceSubType.getSubTypeId();
                log.error(msg);
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            }
            DeviceSubType existingSubType = deviceSubTypeService.getDeviceSubTypeByProvider(deviceSubType.getSubTypeName(),
                    tenantId, deviceSubType.getDeviceType());

            if (existingSubType != null) {
                String msg = "Already exist subtype for subtype name: " + deviceSubType.getSubTypeName();
                log.error(msg);
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            }

            deviceSubTypeService.addDeviceSubType(deviceSubType);
            return Response.status(Response.Status.CREATED).entity(deviceSubType).build();
        } catch (BadRequestException e) {
            String msg = e.getMessage();
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (SubTypeMgtPluginException e) {
            String msg = "Error encountered while creating subtype for subtype id: " + deviceSubType.getSubTypeId();
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response updateDeviceSubType(DeviceSubType deviceSubType) {
        try {
            RequestValidationUtil.validateSubType(deviceSubType);
            DeviceSubTypeService deviceSubTypeService = APIUtil.getSubtypeManagementService();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (!deviceSubTypeService.checkDeviceSubTypeExist(deviceSubType.getSubTypeId(), tenantId, deviceSubType.getDeviceType())) {
                String msg = "Can't find existing subtype for subtype id: " + deviceSubType.getSubTypeId();
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            if (deviceSubTypeService.updateDeviceSubType(deviceSubType.getSubTypeId(), tenantId, deviceSubType.getDeviceType(),
                    deviceSubType.getSubTypeName(), deviceSubType.getTypeDefinition())) {
                return Response.status(Response.Status.OK).entity(deviceSubType).build();
            } else {
                String msg = "Failed to update device subtype for subtype id: " + deviceSubType.getSubTypeId();
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (BadRequestException e) {
            String msg = e.getMessage();
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (SubTypeMgtPluginException e) {
            String msg = "Error encountered while updating subtype for subtype id: " + deviceSubType.getSubTypeId();
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response getDeviceSubType(String subTypeId, String deviceType) {
        if (subTypeId == null || subTypeId.isEmpty()) {
            String msg = "subTypeId cannot be null or empty";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        if (deviceType == null || deviceType.isEmpty()) {
            String msg = "deviceType cannot be null or empty";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        try {
            DeviceSubTypeService deviceSubTypeService = APIUtil.getSubtypeManagementService();
            DeviceSubType deviceSubType = deviceSubTypeService
                    .getDeviceSubType(subTypeId, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), deviceType);
            if (deviceSubType == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.status(Response.Status.OK).entity(deviceSubType).build();
        } catch (SubTypeMgtPluginException e) {
            String msg = "Error encountered while getting subtype for subtype id: " + subTypeId;
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response getAllDeviceSubTypes(String deviceType) {
        if (deviceType == null || deviceType.isEmpty()) {
            String msg = "deviceType cannot be null or empty";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        try {
            DeviceSubTypeService deviceSubTypeService = APIUtil.getSubtypeManagementService();
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService
                    .getAllDeviceSubTypes(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), deviceType);
            return Response.status(Response.Status.OK).entity(deviceSubTypes).build();
        } catch (SubTypeMgtPluginException e) {
            String msg = "Error encountered while getting subtypes for device type:" + deviceType;
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response getDeviceSubTypeCount(String deviceType) {
        if (deviceType == null || deviceType.isEmpty()) {
            String msg = "deviceType cannot be null or empty";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        try {
            return Response.status(Response.Status.OK).entity(APIUtil.getSubtypeManagementService()
                    .getDeviceSubTypeCount(deviceType)).build();
        } catch (SubTypeMgtPluginException e) {
            String msg = "Error encountered while getting subtypes for device type:" + deviceType;
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response getDeviceSubTypeByProvider(String subTypeName, String deviceType) {
        if (subTypeName == null || subTypeName.isEmpty()) {
            String msg = "subTypeName cannot be null or empty";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        if (deviceType == null || deviceType.isEmpty()) {
            String msg = "deviceType cannot be null or empty";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        try {
            DeviceSubTypeService deviceSubTypeService = APIUtil.getSubtypeManagementService();
            DeviceSubType deviceSubType = deviceSubTypeService
                    .getDeviceSubTypeByProvider(subTypeName, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), deviceType);

            if (deviceSubType == null) {
                String msg = "Device subtype is not found for" + subTypeName;
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }

            return Response.status(Response.Status.OK).entity(deviceSubType).build();
        } catch (SubTypeMgtPluginException e) {
            String msg = "Error encountered while getting subtype for device type:" + deviceType + " and provider:" + subTypeName;
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
