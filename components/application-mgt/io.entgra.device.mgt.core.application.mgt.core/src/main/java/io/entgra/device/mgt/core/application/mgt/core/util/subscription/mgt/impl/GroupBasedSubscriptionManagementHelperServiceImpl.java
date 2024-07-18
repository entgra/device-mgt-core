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

package io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.impl;

import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscription;
import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscriptionData;
import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscriptionFilterCriteria;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionEntity;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.application.mgt.core.exception.NotFoundException;
import io.entgra.device.mgt.core.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.HelperUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.SubscriptionManagementHelperUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.service.SubscriptionManagementHelperService;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupManagementException;
import io.entgra.device.mgt.core.device.mgt.core.dto.GroupDetailsDTO;
import io.entgra.device.mgt.core.device.mgt.core.service.GroupManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.List;
import java.util.Objects;

public class GroupBasedSubscriptionManagementHelperServiceImpl implements SubscriptionManagementHelperService {
    private static final Log log = LogFactory.getLog(GroupBasedSubscriptionManagementHelperServiceImpl.class);
    private GroupBasedSubscriptionManagementHelperServiceImpl() {}
    private static class GroupBasedSubscriptionManagementHelperServiceImplHolder {
        private static final GroupBasedSubscriptionManagementHelperServiceImpl INSTANCE
                = new GroupBasedSubscriptionManagementHelperServiceImpl();
    }

    public static GroupBasedSubscriptionManagementHelperServiceImpl getInstance() {
        return GroupBasedSubscriptionManagementHelperServiceImplHolder.INSTANCE;
    }

    @Override
    public List<DeviceSubscription> getStatusBaseSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)
            throws ApplicationManagementException {

        final boolean isUnsubscribe = Objects.equals("unsubscribe", subscriptionInfo.getSubscriptionStatus());
        List<DeviceSubscriptionDTO> deviceSubscriptionDTOS;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        // todo: check and refactor
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO applicationReleaseDTO = applicationReleaseDAO.
                    getReleaseByUUID(subscriptionInfo.getApplicationUUID(), tenantId);

            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for application release UUID: " +
                        subscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }

            ApplicationDTO applicationDTO = this.applicationDAO.getAppWithRelatedRelease(subscriptionInfo.getApplicationUUID(), tenantId);
            if (applicationDTO == null) {
                String msg = "Application not found for the release UUID: " + subscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }

            String deviceSubscriptionStatus = SubscriptionManagementHelperUtil.getDeviceSubscriptionStatus(subscriptionInfo);
            DeviceSubscriptionFilterCriteria deviceSubscriptionFilterCriteria = subscriptionInfo.getDeviceSubscriptionFilterCriteria();

            GroupManagementProviderService groupManagementProviderService = HelperUtil.getGroupManagementProviderService();
            GroupDetailsDTO groupDetailsDTO = groupManagementProviderService.getGroupDetailsWithDevices(subscriptionInfo.getIdentifier(),
                    applicationDTO.getDeviceTypeId(), deviceSubscriptionFilterCriteria.getOwner(), deviceSubscriptionFilterCriteria.getName(),
                    deviceSubscriptionFilterCriteria.getDeviceStatus(), offset, limit);

            List<Integer> deviceIdsOwnByGroup = groupDetailsDTO.getDeviceIds();

            if (Objects.equals("NEW", deviceSubscriptionStatus)) {

                deviceSubscriptionDTOS = subscriptionDAO.getSubscriptionDetailsByDeviceIds(applicationReleaseDTO.getId(),
                        isUnsubscribe, tenantId, deviceIdsOwnByGroup, null,
                        subscriptionInfo.getSubscriptionType(), deviceSubscriptionFilterCriteria.getTriggeredBy(),
                        null, limit, offset);
                for (DeviceSubscriptionDTO deviceSubscriptionDTO: deviceSubscriptionDTOS) {
                    deviceIdsOwnByGroup.remove(deviceSubscriptionDTO.getDeviceId());
                }
            }

            deviceSubscriptionDTOS = subscriptionDAO.getSubscriptionDetailsByDeviceIds(applicationReleaseDTO.getId(),
                    isUnsubscribe, tenantId, deviceIdsOwnByGroup, deviceSubscriptionStatus, subscriptionInfo.getSubscriptionType(),
                    deviceSubscriptionFilterCriteria.getTriggeredBy(), deviceSubscriptionStatus, limit, offset);
        return SubscriptionManagementHelperUtil.getDeviceSubscriptionData(deviceSubscriptionDTOS,
                subscriptionInfo.getDeviceSubscriptionFilterCriteria());

        } catch (GroupManagementException e) {
            String msg = "Error encountered while retrieving group details for group: " + subscriptionInfo.getIdentifier();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException | DBConnectionException e) {
            String msg = "Error encountered while connecting to the database";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DeviceManagementException e) {
            throw new RuntimeException(e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

    }

    @Override
    public List<SubscriptionEntity> getSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)
            throws ApplicationManagementException {
        final boolean isUnsubscribe = Objects.equals("unsubscribe", subscriptionInfo.getSubscriptionStatus());
        final int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO applicationReleaseDTO = applicationReleaseDAO.
                    getReleaseByUUID(subscriptionInfo.getApplicationUUID(), tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for application release UUID: " +
                        subscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return subscriptionDAO.
                    getGroupsSubscriptionDetailsByAppReleaseID(applicationReleaseDTO.getId(), isUnsubscribe, tenantId, offset, limit);
        } catch (DBConnectionException | ApplicationManagementDAOException e) {
            String msg = "Error encountered while connecting to the database";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void getSubscriptionStatistics() throws ApplicationManagementException {

    }
}
