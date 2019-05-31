/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponse;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.DeviceTypes;
import org.wso2.carbon.device.application.mgt.common.SubAction;
import org.wso2.carbon.device.application.mgt.common.SubsciptionType;
import org.wso2.carbon.device.application.mgt.common.SubscribingDeviceIdHolder;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.SubscriptionDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.HelperUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.app.mgt.MobileApp;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.exceptions.UnknownApplicationTypeException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.MDMAndroidOperationUtil;
import org.wso2.carbon.device.mgt.core.util.MDMIOSOperationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the default implementation for the Subscription Manager.
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    private static final Log log = LogFactory.getLog(SubscriptionManagerImpl.class);
    private SubscriptionDAO subscriptionDAO;
    private ApplicationDAO applicationDAO;
    private LifecycleStateManager lifecycleStateManager;

    public SubscriptionManagerImpl() {
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
        this.subscriptionDAO = ApplicationManagementDAOFactory.getSubscriptionDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
    }

    @Override
    public <T> ApplicationInstallResponse performBulkAppInstallation(String applicationUUID, List<T> params,
            String subType, String action) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application release which has UUID " + applicationUUID + " to " + params.size()
                    + " users.");
        }
        try {
            if (params.isEmpty()) {
                String msg = "In order to install application release which has UUID " + applicationUUID + ", you should"
                        + " provide list of subscribers. But found an empty list of users.";
                log.error(msg);
                throw new BadRequestException(msg);
            }

            boolean isValidSubType = Arrays.stream(SubsciptionType.values())
                    .anyMatch(sub -> sub.name().equalsIgnoreCase(subType));
            if (!isValidSubType) {
                String msg = "Found invalid subscription type to install application release witch has UUID: "
                        + applicationUUID + ". Subscription type is " + subType;
                log.error(msg);
                throw new BadRequestException(msg);
            }

            DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                    .getDeviceManagementProviderService();
            GroupManagementProviderService groupManagementProviderService = HelperUtil
                    .getGroupManagementProviderService();
            List<Device> filteredDevices = new ArrayList<>();
            List<Device> devices = new ArrayList<>();
            List<String> subscribers = new ArrayList<>();
            List<DeviceIdentifier> errorDeviceIdentifiers = new ArrayList<>();
            ApplicationInstallResponse applicationInstallResponse;

            ApplicationDTO applicationDTO = getApplicationDTO(applicationUUID);
            if (SubsciptionType.DEVICE.toString().equals(subType)) {
                for (T param : params) {
                    DeviceIdentifier deviceIdentifier = (DeviceIdentifier) param;
                    if (StringUtils.isEmpty(deviceIdentifier.getId()) || StringUtils
                            .isEmpty(deviceIdentifier.getType())) {
                        log.warn("Found a device identifier which has either empty identity of the device or empty"
                                + " device type. Hence ignoring the device identifier. ");
                    }
                    if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType())) {
                        DeviceType deviceType = APIUtil.getDeviceTypeData(applicationDTO.getDeviceTypeId());

                        if (!deviceType.getName().equals(deviceIdentifier.getType())) {
                            String msg =
                                    "Found a device identifier which is not matched with the application device Type. "
                                            + "Application device type is " + deviceType.getName() + " and the "
                                            + "identifier of which has a " + "different device type is "
                                            + deviceIdentifier.getId();
                            log.warn(msg);
                            errorDeviceIdentifiers.add(deviceIdentifier);
                        }
                    }
                    devices.add(deviceManagementProviderService.getDevice(deviceIdentifier, false));
                }
            } else if (SubsciptionType.USER.toString().equals(subType)) {
                for (T param : params) {
                    String username = (String) param;
                    subscribers.add(username);
                    devices.addAll(deviceManagementProviderService.getDevicesOfUser(username));
                }
            } else if (SubsciptionType.ROLE.toString().equals(subType)) {
                for (T param : params) {
                    String roleName = (String) param;
                    subscribers.add(roleName);
                    devices.addAll(deviceManagementProviderService.getAllDevicesOfRole(roleName));
                }
            } else if (SubsciptionType.GROUP.toString().equals(subType)) {
                for (T param : params) {
                    String groupName = (String) param;
                    subscribers.add(groupName);
                    devices.addAll(groupManagementProviderService.getAllDevicesOfGroup(groupName));
                }
            }

            if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType())) {
                DeviceType deviceType = APIUtil.getDeviceTypeData(applicationDTO.getDeviceTypeId());
                String deviceTypeName = deviceType.getName();
                for (Device device : devices) {
                    if (deviceTypeName.equals(device.getType())) {
                        filteredDevices.add(device);
                    }
                }
                applicationInstallResponse = performActionOnDevices(deviceTypeName, filteredDevices, applicationDTO,
                        subType, subscribers, action);

                //todo add db insert to here
            } else {
                //todo improve this
                applicationInstallResponse = performActionOnDevices(null, devices, applicationDTO, subType,
                        subscribers, action);
                //todo add db insert to here
            }
            applicationInstallResponse.setErrorDevices(errorDeviceIdentifiers);
            return applicationInstallResponse;
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting devices of given users or given roles.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (GroupManagementException e) {
            String msg = "Error occurred while getting devices of given groups";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }
    }

    private ApplicationInstallResponse performActionOnDevices(String deviceType, List<Device> devices,
            ApplicationDTO applicationDTO, String subType, List<String> subscribers, String action)
            throws ApplicationManagementException {

        SubscribingDeviceIdHolder subscribingDeviceIdHolder = getSubscribingDeviceIdHolder(devices);
        List<Activity> activityList = new ArrayList<>();
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        List<DeviceIdentifier> ignoredDeviceIdentifiers = new ArrayList<>();
        Map<String, List<DeviceIdentifier>> deviceIdentifierMap = new HashMap<>();

        if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
            deviceIdentifiers = new ArrayList<>(subscribingDeviceIdHolder.getSubscribableDevices().keySet());
            ignoredDeviceIdentifiers = new ArrayList<>(subscribingDeviceIdHolder.getSubscribedDevices().keySet());

            if (deviceIdentifiers.isEmpty()) {
                ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
                applicationInstallResponse.setIgnoredDeviceIdentifiers(ignoredDeviceIdentifiers);
                return applicationInstallResponse;
            }
        } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
            deviceIdentifiers = new ArrayList<>(subscribingDeviceIdHolder.getSubscribedDevices().keySet());
            ignoredDeviceIdentifiers = new ArrayList<>(subscribingDeviceIdHolder.getSubscribableDevices().keySet());
            if (deviceIdentifiers.isEmpty()) {
                ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
                applicationInstallResponse.setIgnoredDeviceIdentifiers(ignoredDeviceIdentifiers);
                return applicationInstallResponse;
            }
        }

        if (deviceType == null) {
            for (DeviceIdentifier identifier : deviceIdentifiers) {
                List<DeviceIdentifier> identifiers;
                if (!deviceIdentifierMap.containsKey(identifier.getType())) {
                    identifiers = new ArrayList<>();
                    identifiers.add(identifier);
                    deviceIdentifierMap.put(identifier.getType(), identifiers);
                } else {
                    identifiers = deviceIdentifierMap.get(identifier.getType());
                    identifiers.add(identifier);
                    deviceIdentifierMap.put(identifier.getType(), identifiers);
                }
            }

            for (String type : deviceIdentifierMap.keySet()) {
                Activity activity = addAppInstallOperationToDevices(applicationDTO,
                        new ArrayList<>(deviceIdentifierMap.get(type)), type);
                activityList.add(activity);
            }

            ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
            applicationInstallResponse.setActivities(activityList);
            applicationInstallResponse.setIgnoredDeviceIdentifiers(ignoredDeviceIdentifiers);
            return applicationInstallResponse;
        }

        //todo consider action

        Activity activity = addAppInstallOperationToDevices(applicationDTO, deviceIdentifiers, deviceType);
        activityList.add(activity);
        ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
        applicationInstallResponse.setActivities(activityList);
        applicationInstallResponse.setIgnoredDeviceIdentifiers(ignoredDeviceIdentifiers);

        //todo
        addSubscriptions(applicationDTO.getApplicationReleaseDTOs().get(0).getId(), activity,
                subscribingDeviceIdHolder.getSubscribableDevices(),
                new ArrayList<>(subscribingDeviceIdHolder.getDeviceSubscriptions().keySet()), subscribers, subType);
        return applicationInstallResponse;
    }

    private SubscribingDeviceIdHolder getSubscribingDeviceIdHolder(List<Device> devices)
            throws ApplicationManagementException {
        Map<DeviceIdentifier, Integer> subscribedDevices = new HashMap<>();
        Map<DeviceIdentifier, Integer> subscribableDevices = new HashMap<>();

        List<Integer> filteredDeviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());
        //get device subscriptions for given device id list.
        Map<Integer, DeviceSubscriptionDTO> deviceSubscriptions = getDeviceSubscriptions(filteredDeviceIds);
        for (Device device : devices) {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            DeviceSubscriptionDTO deviceSubscriptionDTO = deviceSubscriptions.get(device.getId());
            if (deviceSubscriptionDTO != null && !deviceSubscriptionDTO.isUnsubscribed() && Operation.Status.COMPLETED
                    .toString().equals(deviceSubscriptionDTO.getStatus())) {
                subscribedDevices.put(deviceIdentifier, device.getId());
            } else {
                subscribableDevices.put(deviceIdentifier, device.getId());
            }
        }

        SubscribingDeviceIdHolder subscribingDeviceIdHolder = new SubscribingDeviceIdHolder();
        subscribingDeviceIdHolder.setSubscribableDevices(subscribableDevices);
        subscribingDeviceIdHolder.setSubscribedDevices(subscribedDevices);
        subscribingDeviceIdHolder.setDeviceSubscriptions(deviceSubscriptions);
        return subscribingDeviceIdHolder;
    }

    private ApplicationDTO getApplicationDTO(String uuid) throws ApplicationManagementException {
        ApplicationDTO applicationDTO;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            applicationDTO = this.applicationDAO.getApplicationByUUID(uuid, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't fond an application for application release UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (!lifecycleStateManager.getInstallableState()
                    .equals(applicationDTO.getApplicationReleaseDTOs().get(0).getCurrentState())) {
                String msg = "You are trying to install an application which is not in the installable state of "
                        + "its Life-Cycle. hence you are not permitted to install this application. If you "
                        + "required to install this particular application, please change the state of "
                        + "application release from : " + applicationDTO.getApplicationReleaseDTOs().get(0)
                        .getCurrentState() + " to " + lifecycleStateManager.getInstallableState();
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            return applicationDTO;
        } catch (LifecycleManagementException e) {
            String msg = "Error occured when getting life-cycle state from life-cycle state manager.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while getting application data for application release UUID: " + uuid;
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private void addSubscriptions(int applicationReleaseId, Activity activity,
            Map<DeviceIdentifier, Integer> compatibleDevices, List<Integer> subDeviceIds, List<String> subscribers,
            String subType) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String subscriber = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            List<Integer> deviceResubscribingIds = new ArrayList<>();
            List<Integer> deviceSubscriptingIds;

            if (SubsciptionType.USER.toString().equals(subType)) {
                List<String> subscribedUsers = subscriptionDAO.getSubscribedUsernames(subscribers, tenantId);
                if (!subscribedUsers.isEmpty()) {
                    subscriptionDAO
                            .updateUserSubscription(tenantId, subscriber, false, subscribedUsers, applicationReleaseId);
                    subscribers.removeAll(subscribedUsers);
                }
                subscriptionDAO.subscribeUserToApplication(tenantId, subscriber, subscribers, applicationReleaseId);
            }

            //todo add for other subscription types

            List<Integer> deviceIds = new ArrayList<>();
            List<ActivityStatus> activityStatuses = activity.getActivityStatus();
            for (ActivityStatus status : activityStatuses) {
                if (status.getStatus().equals(ActivityStatus.Status.PENDING)) {
                    deviceIds.add(compatibleDevices.get(status.getDeviceIdentifier()));
                }
            }

            int operationId = Integer.parseInt(activity.getActivityId().split("ACTIVITY_")[1]);
            if (!subDeviceIds.isEmpty()) {
                deviceResubscribingIds = subscriptionDAO.updateDeviceSubscription(subscriber, subDeviceIds, subType,
                        Operation.Status.PENDING.toString(), applicationReleaseId, tenantId);
                deviceIds.removeAll(subDeviceIds);
            }
            deviceSubscriptingIds = subscriptionDAO
                    .subscribeDeviceToApplication(subscriber, deviceIds, subType, Operation.Status.PENDING.toString(),
                            applicationReleaseId, tenantId);
            deviceSubscriptingIds.addAll(deviceResubscribingIds);
            subscriptionDAO.addOperationMapping(operationId, deviceSubscriptingIds, tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when adding subscription data for application release UUID: "
                    + applicationReleaseId;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred when getting database connection to add new device subscriptions to application.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg =
                    "SQL Error occurred when adding new device subscription to application release which has UUID: "
                            + applicationReleaseId;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private Map<Integer, DeviceSubscriptionDTO> getDeviceSubscriptions(List<Integer> filteredDeviceIds)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        try {
            ConnectionManagerUtil.openDBConnection();
            return this.subscriptionDAO.getDeviceSubscriptions(filteredDeviceIds, tenantId);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occured when getting device subscriptions for given device IDs";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting database connection for getting device subscriptions.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

    }

    private Activity addAppInstallOperationToDevices(ApplicationDTO application,
            List<DeviceIdentifier> deviceIdentifierList, String deviceType) throws ApplicationManagementException {
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();
        try {
            Operation operation = generateOperationPayloadByDeviceType(deviceType, application, null);
            //todo refactor add operation code to get successful operations
            return deviceManagementProviderService.addOperation(deviceType, operation, deviceIdentifierList);
        } catch (OperationManagementException e) {
            throw new ApplicationManagementException(
                    "Error occurred while adding the application install " + "operation to devices", e);
        } catch (InvalidDeviceException e) {
            //This exception should not occur because the validation has already been done.
            throw new ApplicationManagementException("The list of device identifiers are invalid");
        }
    }

    private Operation generateOperationPayloadByDeviceType(String deviceType, ApplicationDTO application, String action)
            throws ApplicationManagementException {
        try {

            //todo rethink and modify the {@link MobileApp} usage
            MobileApp mobileApp = new MobileApp();
            if (DeviceTypes.ANDROID.toString().equalsIgnoreCase(deviceType)) {
                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    return MDMAndroidOperationUtil.createInstallAppOperation(mobileApp);
                } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                    return MDMAndroidOperationUtil.createAppUninstallOperation(mobileApp);
                } else {
                    String msg = "Invalid Action is found. Action: " + action;
                    log.error(msg);
                    throw new ApplicationManagementException(msg);
                }
            } else if (DeviceTypes.IOS.toString().equalsIgnoreCase(deviceType)) {
                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    return MDMIOSOperationUtil.createInstallAppOperation(mobileApp);
                } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                    return MDMIOSOperationUtil.createAppUninstallOperation(mobileApp);
                } else {
                    String msg = "Invalid Action is found. Action: " + action;
                    log.error(msg);
                    throw new ApplicationManagementException(msg);
                }
            } else {
                String msg = "Invalid device type is found. Device Type: " + deviceType;
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }

        } catch (UnknownApplicationTypeException e) {
            String msg = "Unknown Application type is found.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        }
    }

}
