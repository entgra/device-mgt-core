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
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionEntity;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.SubscriptionsDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.service.SubscriptionManagementHelperService;

import java.util.List;

/*
//    @Override
//    public List<SubscriptionsDTO> getUserSubscriptionsByUUID(String uuid, String subscriptionStatus,
//                                                             PaginationRequest request, int offset, int limit)
//            throws ApplicationManagementException {
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
//        boolean unsubscribe = subscriptionStatus.equals("unsubscribed");
//        String status;
//
//        try {
//            ConnectionManagerUtil.openDBConnection();
//
//            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
//            if (applicationReleaseDTO == null) {
//                String msg = "Couldn't find an application release for application release UUID: " + uuid;
//                log.error(msg);
//                throw new NotFoundException(msg);
//            }
//            ApplicationDTO applicationDTO = this.applicationDAO.getAppWithRelatedRelease(uuid, tenantId);
//            int appReleaseId = applicationReleaseDTO.getId();
//            List<SubscriptionsDTO> userSubscriptionsWithDevices = new ArrayList<>();
//
//            List<SubscriptionsDTO> userSubscriptions =
//                    subscriptionDAO.getUserSubscriptionsByAppReleaseID(appReleaseId, unsubscribe, tenantId, offset, limit);
//            if (userSubscriptions == null) {
//                throw new ApplicationManagementException("User details not found for appReleaseId: " + appReleaseId);
//            }
//
//            DeviceManagementProviderService deviceManagementProviderService = HelperUtil.getDeviceManagementProviderService();
//
//            for (SubscriptionsDTO userSubscription : userSubscriptions) {
//
//                if (StringUtils.isNotBlank(request.getUserName()) && !request.getUserName().equals(userSubscription.getName())) {
//                    continue;
//                }
//
//                String userName = StringUtils.isNotBlank(request.getUserName()) ? request.getUserName() : userSubscription.getName();
//
//                // Retrieve owner details and device IDs for the user using the service layer
//                OwnerWithDeviceDTO ownerDetailsWithDevices =
//                        deviceManagementProviderService.getOwnersWithDeviceIds(userName, applicationDTO.getDeviceTypeId(),
//                                request.getOwner(), request.getDeviceName(), request.getDeviceStatus());
//
//                SubscriptionsDTO userSubscriptionDTO = new SubscriptionsDTO();
//                userSubscriptionDTO.setName(userSubscription.getName());
//                userSubscriptionDTO.setSubscribedBy(userSubscription.getSubscribedBy());
//                userSubscriptionDTO.setSubscribedTimestamp(userSubscription.getSubscribedTimestamp());
//                userSubscriptionDTO.setUnsubscribed(userSubscription.getUnsubscribed());
//                userSubscriptionDTO.setUnsubscribedBy(userSubscription.getUnsubscribedBy());
//                userSubscriptionDTO.setUnsubscribedTimestamp(userSubscription.getUnsubscribedTimestamp());
//                userSubscriptionDTO.setAppReleaseId(userSubscription.getAppReleaseId());
//
//                userSubscriptionDTO.setDeviceCount(ownerDetailsWithDevices.getDeviceCount());
//
//                // Fetch device subscriptions for each device ID associated with the user
//                List<DeviceSubscriptionData> pendingDevices = new ArrayList<>();
//                List<DeviceSubscriptionData> installedDevices = new ArrayList<>();
//                List<DeviceSubscriptionData> errorDevices = new ArrayList<>();
//                List<DeviceSubscriptionData> newDevices = new ArrayList<>();
//                List<DeviceSubscriptionData> subscribedDevices = new ArrayList<>();
//
//                List<Integer> deviceIds = ownerDetailsWithDevices.getDeviceIds();
//                Map<String, Integer> statusCounts = new HashMap<>();
//                statusCounts.put("PENDING", 0);
//                statusCounts.put("COMPLETED", 0);
//                statusCounts.put("ERROR", 0);
//                statusCounts.put("NEW", 0);
//                statusCounts.put("SUBSCRIBED", 0);
//
//                List<DeviceSubscriptionDTO> subscribedDeviceSubscriptions = new ArrayList<>();
//                if (unsubscribe) {
//                    subscribedDeviceSubscriptions = subscriptionDAO.getSubscriptionDetailsByDeviceIds(
//                            appReleaseId, !unsubscribe, tenantId, deviceIds, request.getActionStatus(), request.getActionType(),
//                            request.getActionTriggeredBy(), request.getTabActionStatus());
//                }
//
//                for (Integer deviceId : deviceIds) {
//                    List<DeviceSubscriptionDTO> deviceSubscriptions = subscriptionDAO.getSubscriptionDetailsByDeviceIds(
//                            userSubscription.getAppReleaseId(), unsubscribe, tenantId, deviceIds, request.getActionStatus(), request.getActionType(),
//                            request.getActionTriggeredBy(), request.getTabActionStatus());
//                    OwnerWithDeviceDTO ownerWithDeviceByDeviceId =
//                            deviceManagementProviderService.getOwnerWithDeviceByDeviceId(deviceId, request.getOwner(), request.getDeviceName(),
//                                    request.getDeviceStatus());
//                    if (ownerWithDeviceByDeviceId == null) {
//                        continue;
//                    }
//                    boolean isNewDevice = true;
//                    for (DeviceSubscriptionDTO subscription : deviceSubscriptions) {
//                        if (subscription.getDeviceId() == deviceId) {
//                            DeviceSubscriptionData deviceDetail = new DeviceSubscriptionData();
//                            deviceDetail.setDeviceId(subscription.getDeviceId());
//                            deviceDetail.setSubId(subscription.getId());
//                            deviceDetail.setDeviceOwner(ownerWithDeviceByDeviceId.getUserName());
//                            deviceDetail.setDeviceStatus(ownerWithDeviceByDeviceId.getDeviceStatus());
//                            deviceDetail.setDeviceName(ownerWithDeviceByDeviceId.getDeviceNames());
//                            deviceDetail.setActionType(subscription.getActionTriggeredFrom());
//                            deviceDetail.setStatus(subscription.getStatus());
//                            deviceDetail.setActionType(subscription.getActionTriggeredFrom());
//                            deviceDetail.setActionTriggeredBy(subscription.getSubscribedBy());
//                            deviceDetail.setActionTriggeredTimestamp(subscription.getSubscribedTimestamp());
//                            deviceDetail.setUnsubscribed(subscription.isUnsubscribed());
//                            deviceDetail.setUnsubscribedBy(subscription.getUnsubscribedBy());
//                            deviceDetail.setUnsubscribedTimestamp(subscription.getUnsubscribedTimestamp());
//                            deviceDetail.setType(ownerWithDeviceByDeviceId.getDeviceTypes());
//                            deviceDetail.setDeviceIdentifier(ownerWithDeviceByDeviceId.getDeviceIdentifiers());
//
//                            status = subscription.getStatus();
//                            switch (status) {
//                                case "COMPLETED":
//                                    installedDevices.add(deviceDetail);
//                                    statusCounts.put("COMPLETED", statusCounts.get("COMPLETED") + 1);
//                                    break;
//                                case "ERROR":
//                                case "INVALID":
//                                case "UNAUTHORIZED":
//                                    errorDevices.add(deviceDetail);
//                                    statusCounts.put("ERROR", statusCounts.get("ERROR") + 1);
//                                    break;
//                                case "IN_PROGRESS":
//                                case "PENDING":
//                                case "REPEATED":
//                                    pendingDevices.add(deviceDetail);
//                                    statusCounts.put("PENDING", statusCounts.get("PENDING") + 1);
//                                    break;
//                            }
//                            isNewDevice = false;
//                        }
//                    }
//                    if (isNewDevice) {
//                        boolean isSubscribedDevice = false;
//                        for (DeviceSubscriptionDTO subscribedDevice : subscribedDeviceSubscriptions) {
//                            if (subscribedDevice.getDeviceId() == deviceId) {
//                                DeviceSubscriptionData subscribedDeviceDetail = new DeviceSubscriptionData();
//                                subscribedDeviceDetail.setDeviceId(subscribedDevice.getDeviceId());
//                                subscribedDeviceDetail.setDeviceName(ownerWithDeviceByDeviceId.getDeviceNames());
//                                subscribedDeviceDetail.setDeviceOwner(ownerWithDeviceByDeviceId.getUserName());
//                                subscribedDeviceDetail.setDeviceStatus(ownerWithDeviceByDeviceId.getDeviceStatus());
//                                subscribedDeviceDetail.setSubId(subscribedDevice.getId());
//                                subscribedDeviceDetail.setActionTriggeredBy(subscribedDevice.getSubscribedBy());
//                                subscribedDeviceDetail.setActionTriggeredTimestamp(subscribedDevice.getSubscribedTimestamp());
//                                subscribedDeviceDetail.setActionType(subscribedDevice.getActionTriggeredFrom());
//                                subscribedDeviceDetail.setStatus(subscribedDevice.getStatus());
//                                subscribedDeviceDetail.setType(ownerWithDeviceByDeviceId.getDeviceTypes());
//                                subscribedDeviceDetail.setDeviceIdentifier(ownerWithDeviceByDeviceId.getDeviceIdentifiers());
//                                subscribedDevices.add(subscribedDeviceDetail);
//                                statusCounts.put("SUBSCRIBED", statusCounts.get("SUBSCRIBED") + 1);
//                                isSubscribedDevice = true;
//                                break;
//                            }
//                        }
//                        if (!isSubscribedDevice) {
//                            DeviceSubscriptionData newDeviceDetail = new DeviceSubscriptionData();
//                            newDeviceDetail.setDeviceId(deviceId);
//                            newDeviceDetail.setDeviceOwner(ownerWithDeviceByDeviceId.getUserName());
//                            newDeviceDetail.setDeviceStatus(ownerWithDeviceByDeviceId.getDeviceStatus());
//                            newDeviceDetail.setDeviceName(ownerWithDeviceByDeviceId.getDeviceNames());
//                            newDeviceDetail.setType(ownerWithDeviceByDeviceId.getDeviceTypes());
//                            newDeviceDetail.setDeviceIdentifier(ownerWithDeviceByDeviceId.getDeviceIdentifiers());
//                            newDevices.add(newDeviceDetail);
//                            statusCounts.put("NEW", statusCounts.get("NEW") + 1);
//                        }
//                    }
//                }
//
//                int totalDevices = deviceIds.size();
//                Map<String, Double> statusPercentages = new HashMap<>();
//                for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
//                    double percentage = ((double) entry.getValue() / totalDevices) * 100;
//                    String formattedPercentage = String.format("%.2f", percentage);
//                    statusPercentages.put(entry.getKey(), Double.valueOf(formattedPercentage));
//                }
//
//                List<DeviceSubscriptionData> requestedDevices = new ArrayList<>();
//                if (StringUtils.isNotBlank(request.getTabActionStatus())) {
//                    switch (request.getTabActionStatus()) {
//                        case "COMPLETED":
//                            requestedDevices = installedDevices;
//                            break;
//                        case "PENDING":
//                            requestedDevices = pendingDevices;
//                            break;
//                        case "ERROR":
//                            requestedDevices = errorDevices;
//                            break;
//                        case "NEW":
//                            requestedDevices = newDevices;
//                            break;
//                        case "SUBSCRIBED":
//                            requestedDevices = subscribedDevices;
//                            break;
//                    }
//                    userSubscriptionDTO.setDevices(new CategorizedSubscriptionResult(requestedDevices, request.getTabActionStatus()));
//                } else {
//                    CategorizedSubscriptionResult categorizedSubscriptionResult;
//                    if (subscribedDevices.isEmpty()) {
//                        categorizedSubscriptionResult =
//                                new CategorizedSubscriptionResult(installedDevices, pendingDevices, errorDevices, newDevices);
//                    } else {
//                        categorizedSubscriptionResult =
//                                new CategorizedSubscriptionResult(installedDevices, pendingDevices, errorDevices, newDevices,
//                                        subscribedDevices);
//                    }
//                    userSubscriptionDTO.setDevices(categorizedSubscriptionResult);
//                    userSubscriptionDTO.setStatusPercentages(statusPercentages);
//
//                }
//                userSubscriptionsWithDevices.add(userSubscriptionDTO);
//            }
//            return userSubscriptionsWithDevices;
//        } catch (ApplicationManagementDAOException e) {
//            String msg = "Error occurred while getting user subscriptions for the application release UUID: " + uuid;
//            log.error(msg, e);
//            throw new ApplicationManagementException(msg, e);
//        } catch (DBConnectionException e) {
//            String msg = "DB Connection error occurred while getting user subscriptions for UUID: " + uuid;
//            log.error(msg, e);
//            throw new ApplicationManagementException(msg, e);
//        } catch (DeviceManagementDAOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            ConnectionManagerUtil.closeDBConnection();
//        }
//    }
 */
public class UserBasedSubscriptionManagementHelperServiceImpl implements SubscriptionManagementHelperService {
    @Override
    public List<DeviceSubscription> getStatusBaseSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)
            throws ApplicationManagementException {
        return null;
    }

    @Override
    public List<SubscriptionEntity> getSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset) throws ApplicationManagementException {
        return null;
    }

    @Override
    public void getSubscriptionStatistics() throws ApplicationManagementException {

    }
}
