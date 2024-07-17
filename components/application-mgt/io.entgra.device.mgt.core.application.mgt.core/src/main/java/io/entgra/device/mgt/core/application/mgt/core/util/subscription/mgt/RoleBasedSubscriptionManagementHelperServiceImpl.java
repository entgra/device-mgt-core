/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt;

import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.SubscriptionsDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.application.mgt.core.exception.NotFoundException;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.HelperUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.bean.DeviceSubscriptionStatus;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.bean.RoleBasedSubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.service.SubscriptionManagementHelperService;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RoleBasedSubscriptionManagementHelperServiceImpl implements SubscriptionManagementHelperService {
    private static final Log log = LogFactory.getLog(RoleBasedSubscriptionManagementHelperServiceImpl.class);
    private final RoleBasedSubscriptionInfo roleBasedSubscriptionInfo;
    private final boolean isUnsubscribe;
    public RoleBasedSubscriptionManagementHelperServiceImpl(RoleBasedSubscriptionInfo roleBasedSubscriptionInfo) {
        this.roleBasedSubscriptionInfo = roleBasedSubscriptionInfo;
        this.isUnsubscribe = "unsubscribed".equals(roleBasedSubscriptionInfo.getSubscriptionStatus());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DeviceSubscriptionDTO> getStatusBaseSubscriptions(int limit, int offset) throws ApplicationManagementException {
        List<DeviceSubscriptionDTO> deviceSubscriptionDTOS;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            UserStoreManager userStoreManager = DataHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getUserStoreManager();
            String[] usersWithRole =
                    userStoreManager.getUserListOfRole(roleBasedSubscriptionInfo.getRoleName());
            List<Device> deviceListOwnByRole = new ArrayList<>();
            for (String user : usersWithRole) {
                PaginationRequest paginationRequest = new PaginationRequest(offset, limit);
                paginationRequest.setOwner(user);
                paginationRequest.setStatusList(Arrays.asList("ACTIVE", "INACTIVE", "UNREACHABLE"));
                PaginationResult ownDeviceIds = HelperUtil.getDeviceManagementProviderService().
                        getAllDevicesIdList(paginationRequest);
                if (ownDeviceIds.getData() != null) {
                    deviceListOwnByRole.addAll((List<Device>)ownDeviceIds.getData());
                }
            }

            List<Integer> deviceIds = (List<Integer>) deviceListOwnByRole.stream().map(Device::getId);

            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO applicationReleaseDTO = applicationReleaseDAO.
                    getReleaseByUUID(roleBasedSubscriptionInfo.getApplicationUUID(), tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for application release UUID: " +
                        roleBasedSubscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }

            if (Objects.equals(DeviceSubscriptionStatus.NEW.toString(),
                    roleBasedSubscriptionInfo.getDeviceSubscriptionStatus().toString())) {

                deviceSubscriptionDTOS = subscriptionDAO.getSubscriptionDetailsByDeviceIds(applicationReleaseDTO.getId(),
                        isUnsubscribe, tenantId, deviceIds, null, RoleBasedSubscriptionInfo.TRIGGERED_FROM_VALUE,
                        null, null, limit, offset);
                for (Integer deviceId : deviceIds) {
                    DeviceSubscriptionDTO deviceSubscriptionDTO = new DeviceSubscriptionDTO();
                    deviceSubscriptionDTO.setDeviceId(deviceId);
                    deviceSubscriptionDTOS.remove(deviceSubscriptionDTO);
                }
                return deviceSubscriptionDTOS;
            }

            deviceSubscriptionDTOS =  subscriptionDAO.getSubscriptionDetailsByDeviceIds(applicationReleaseDTO.getId(),
                    isUnsubscribe, tenantId, deviceIds, roleBasedSubscriptionInfo.getDeviceSubscriptionStatus().
                            toString(), RoleBasedSubscriptionInfo.TRIGGERED_FROM_VALUE,
                    null, null, limit, offset);

        } catch (UserStoreException e) {
            String msg = "Error encountered while getting the user management store for tenant id " + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error encountered while getting device details";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException | DBConnectionException e) {
            String msg = "Error encountered while connecting to the database";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        return deviceSubscriptionDTOS;
    }

    @Override
    public List<SubscriptionsDTO> getSubscriptions(int limit, int offset) throws ApplicationManagementException {
        final int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO applicationReleaseDTO = applicationReleaseDAO.
                    getReleaseByUUID(roleBasedSubscriptionInfo.getApplicationUUID(), tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for application release UUID: " +
                        roleBasedSubscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return subscriptionDAO.
                    getRoleSubscriptionsByAppReleaseID(applicationReleaseDTO.getId(), isUnsubscribe, tenantId, offset, limit);
        } catch (DBConnectionException | ApplicationManagementDAOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getSubscriptionStatistics() throws ApplicationManagementException {

    }
}
