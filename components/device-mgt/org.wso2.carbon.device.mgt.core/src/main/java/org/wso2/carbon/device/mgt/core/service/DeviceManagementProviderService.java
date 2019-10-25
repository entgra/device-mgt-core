/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.device.mgt.core.service;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceTransferRequest;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.UnauthorizedDeviceAccessException;
import org.wso2.carbon.device.mgt.common.exceptions.UserNotFoundException;
import org.wso2.carbon.device.mgt.common.StartupOperationConfig;
import org.wso2.carbon.device.mgt.common.configuration.mgt.AmbiguousConfigurationException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.DeviceConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistory;
import org.wso2.carbon.device.mgt.common.device.details.DeviceData;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.dto.DeviceTypeVersion;
import org.wso2.carbon.device.mgt.core.geo.GeoCluster;
import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxy class for all Device Management related operations that take the corresponding plugin type in
 * and resolve the appropriate plugin implementation
 */
public interface DeviceManagementProviderService {

    /**
     * Method to retrieve all the devices of a given device type.
     *
     * @param deviceType Device-type of the required devices
     * @return List of devices of given device-type.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getAllDevices(String deviceType) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices of a given device type.
     *
     * @param deviceType Device-type of the required devices
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices of given device-type.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getAllDevices(String deviceType, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices registered in the system.
     *
     * @return List of registered devices.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getAllDevices() throws DeviceManagementException;

    /**
     * Method to retrieve all the devices registered in the system.
     *
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of registered devices.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getAllDevices(boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices registered in the system.
     *
     * @param since - Date value where the resource was last modified
     * @return List of registered devices.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getDevices(Date since) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices registered in the system.
     *
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of registered devices.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getDevices(Date since, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    PaginationResult getDevicesByType(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    PaginationResult getDevicesByType(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    PaginationResult getAllDevices(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    PaginationResult getAllDevices(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDeviceWithTypeProperties(DeviceIdentifier deviceId) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(String deviceId, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Returns the device of specified id owned by user with given username.
     *
     * @param deviceId - Device Id
     * @param owner - Username of the owner
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, String owner, boolean requireDeviceInfo) throws DeviceManagementException;


    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @param since - Date value where the resource was last modified
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, Date since) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @param since - Date value where the resource was last modified
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, Date since, boolean requireDeviceInfo) throws DeviceManagementException;

    /***
     *
     * @param deviceData Device data,
     * @param requireDeviceInfo A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return {@link Device}, null when device is not available.
     * @throws {@link DeviceManagementException}
     */
    Device getDevice(DeviceData deviceData, boolean requireDeviceInfo) throws DeviceManagementException;


    /**
     * Retrieves a list of devices based on a given criteria of properties
     *
     * @param deviceProps properties by which devices need to be drawn
     * @return list of devices
     * @throws DeviceManagementException
     */
    List<Device> getDevicesBasedOnProperties(Map deviceProps) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @param since - Date value where the resource was last modified
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(String deviceId, Date since, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Returns the device of specified id and owned by user with given username.
     *
     * @param deviceId - Device Id
     * @param owner - Username of the owner
     * @param since - Date value where the resource was last modified
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, String owner, Date since, boolean requireDeviceInfo)
            throws DeviceManagementException;

    /**
     * Returns the device of specified id with the given status.
     *
     * @param deviceId device Id
     * @param status - Status of the device
     *
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status) throws DeviceManagementException;

    /**
     * Returns the device of specified id with the given status.
     *
     * @param deviceId device Id
     * @param status - Status of the device
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesOfUser(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesOfUser(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices filtered by the ownership with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByOwnership(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to get the list of devices filtered by the ownership with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByOwnership(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user.
     *
     * @param userName Username of the user
     * @return List of devices owned by a particular user
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesOfUser(String userName) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user.
     *
     * @param userName Username of the user
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices owned by a particular user
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesOfUser(String userName, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * This method returns the list of device owned by a user of given device type.
     *
     * @param userName   user name.
     * @param deviceType device type name
     * @return List of device owned by the given user and type.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesOfUser(String userName, String deviceType) throws DeviceManagementException;

    /**
     * This method returns the list of device owned by a user of given device type.
     *
     * @param userName   user name.
     * @param deviceType device type name
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of device owned by the given user and type.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesOfUser(String userName, String deviceType, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by users of a particular user-role.
     *
     * @param roleName Role name of the users
     * @return List of devices owned by users of a particular role
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getAllDevicesOfRole(String roleName) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by users of a particular user-role.
     *
     * @param roleName Role name of the users
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices owned by users of a particular role
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getAllDevicesOfRole(String roleName, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination and filter info
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByStatus(PaginationRequest request) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination and filter info
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByStatus(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices that matches with the given device name.
     *
     * @param request PaginationRequest object holding the data for pagination and filter info
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices that matches with the given device name.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesByNameAndType(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices that matches with the given device name with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByName(PaginationRequest request) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices that matches with the given device name with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByName(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status.
     *
     * @param status Device status
     * @return List of devices
     * @throws DeviceManagementException
     */
    List<Device> getDevicesByStatus(EnrolmentInfo.Status status) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status.
     *
     * @param status Device status
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices
     * @throws DeviceManagementException
     */
    List<Device> getDevicesByStatus(EnrolmentInfo.Status status, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the device count of user.
     *
     * @return device count
     * @throws DeviceManagementException If some unusual behaviour is observed while counting
     *                                   the devices
     */
    int getDeviceCount(String username) throws DeviceManagementException;

    /**
     * Method to get the count of all types of devices.
     *
     * @return device count
     * @throws DeviceManagementException If some unusual behaviour is observed while counting
     *                                   the devices
     */
    int getDeviceCount() throws DeviceManagementException;

    /**
     * Method to get the count of devices with given status and type.
     *
     * @param deviceType Device type name
     * @param status Device status
     *
     * @return device count
     * @throws DeviceManagementException If some unusual behaviour is observed while counting
     *                                   the devices
     */
    int getDeviceCount(String deviceType, EnrolmentInfo.Status status) throws DeviceManagementException;

    /**
     * Method to get the count of all types of devices with given status.
     *
     * @param status Device status
     *
     * @return device count
     * @throws DeviceManagementException If some unusual behaviour is observed while counting
     *                                   the devices
     */
    int getDeviceCount(EnrolmentInfo.Status status) throws DeviceManagementException;

    HashMap<Integer, Device> getTenantedDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementException;

    void sendEnrolmentInvitation(String templateName, EmailMetaInfo metaInfo) throws DeviceManagementException,
            ConfigurationManagementException;

    void sendRegistrationEmail(EmailMetaInfo metaInfo) throws DeviceManagementException, ConfigurationManagementException;

    FeatureManager getFeatureManager(String deviceType) throws DeviceTypeNotFoundException;

    /**
     * Proxy method to get the tenant configuration of a given platform.
     *
     * @param deviceType Device platform
     * @return Tenant configuration settings of the particular tenant and platform.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   configuration.
     */
    PlatformConfiguration getConfiguration(String deviceType) throws DeviceManagementException;

    /**
     * This method is used to check whether the device is enrolled with the give user.
     *
     * @param deviceId identifier of the device that needs to be checked against the user.
     * @param user     username of the device owner.
     * @return true if the user owns the device else will return false.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the device.
     */
    boolean isEnrolled(DeviceIdentifier deviceId, String user) throws DeviceManagementException;

    /**
     * This method is used to get notification strategy for given device type
     *
     * @param deviceType Device type
     * @return Notification Strategy for device type
     * @throws DeviceManagementException
     */
    NotificationStrategy getNotificationStrategyByDeviceType(String deviceType) throws DeviceManagementException;

    License getLicense(String deviceType, String languageCode) throws DeviceManagementException;

    void addLicense(String deviceType, License license) throws DeviceManagementException;

    boolean modifyEnrollment(Device device) throws DeviceManagementException;

    boolean enrollDevice(Device device) throws DeviceManagementException;

    boolean saveConfiguration(PlatformConfiguration configuration) throws DeviceManagementException;

    boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean deleteDevices(List<String> deviceIdentifiers) throws DeviceManagementException, InvalidDeviceException;

    boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean setActive(DeviceIdentifier deviceId, boolean status) throws DeviceManagementException;

    List<String> getAvailableDeviceTypes() throws DeviceManagementException;

    List<String> getPolicyMonitoringEnableDeviceTypes() throws DeviceManagementException;

    boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException;

    boolean setOwnership(DeviceIdentifier deviceId, String ownershipType) throws DeviceManagementException;

    boolean isClaimable(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean setStatus(DeviceIdentifier deviceId, String currentOwner,
                      EnrolmentInfo.Status status) throws DeviceManagementException;

    boolean setStatus(String currentOwner, EnrolmentInfo.Status status) throws DeviceManagementException;

    void notifyOperationToDevices(Operation operation,
                                  List<DeviceIdentifier> deviceIds) throws DeviceManagementException;

    Activity addOperation(String type, Operation operation,
                          List<DeviceIdentifier> devices) throws OperationManagementException, InvalidDeviceException;

    List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException;

    PaginationResult getOperations(DeviceIdentifier deviceId,
                                   PaginationRequest request) throws OperationManagementException;

    List<? extends Operation> getPendingOperations(
            DeviceIdentifier deviceId) throws OperationManagementException;

    Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException;

    Operation getNextPendingOperation(DeviceIdentifier deviceId, long notNowOperationFrequency)
            throws OperationManagementException;

    void updateOperation(DeviceIdentifier deviceId, Operation operation) throws OperationManagementException;

    boolean updateProperties(DeviceIdentifier deviceId, List<Device.Property> properties) throws DeviceManagementException;

    Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException;

    List<? extends Operation> getOperationsByDeviceAndStatus(DeviceIdentifier identifier,
                                                             Operation.Status status)
            throws OperationManagementException, DeviceManagementException;

    Operation getOperation(String type, int operationId) throws OperationManagementException;

    Activity getOperationByActivityId(String activity) throws OperationManagementException;

    List<Activity> getOperationByActivityIds(List<String> idList) throws OperationManagementException;

    Activity getOperationByActivityIdAndDevice(String activity, DeviceIdentifier deviceId) throws OperationManagementException;

    List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementException;

    List<Activity> getFilteredActivities(String operationCode, int limit, int offset) throws OperationManagementException;

    int getTotalCountOfFilteredActivities(String operationCode) throws OperationManagementException;

    List<Activity> getActivitiesUpdatedAfterByUser(long timestamp, String user, int limit, int offset) throws OperationManagementException;

    int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementException;

    int getActivityCountUpdatedAfterByUser(long timestamp, String user) throws OperationManagementException;

    List<MonitoringOperation> getMonitoringOperationList(String deviceType);

    List<String> getStartupOperations(String deviceType);

    int getDeviceMonitoringFrequency(String deviceType);

    OperationMonitoringTaskConfig getDeviceMonitoringConfig(String deviceType);

    StartupOperationConfig getStartupOperationConfig(String deviceType);

    boolean isDeviceMonitoringEnabled(String deviceType);

    PolicyMonitoringManager getPolicyMonitoringManager(String deviceType);

    /**
     * Change device status.
     *
     * @param deviceIdentifier {@link DeviceIdentifier} object
     * @param newStatus        New status of the device
     * @return Whether status is changed or not
     * @throws DeviceManagementException on errors while trying to change device status
     */
    boolean changeDeviceStatus(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status newStatus)
            throws DeviceManagementException;
    
    /**
     * This will handle add and update of device type services.
     * @param deviceManagementService
     */
    void registerDeviceType(DeviceManagementService deviceManagementService) throws DeviceManagementException;

    /**
     * This retrieves the device type info for the given type
     * @param deviceType name of the type.
     * @throws DeviceManagementException
     */
    DeviceType getDeviceType(String deviceType) throws DeviceManagementException;

    /**
     * This retrieves the device type info for the given type
     * @throws DeviceManagementException
     */
    List<DeviceType> getDeviceTypes() throws DeviceManagementException;

    /**
     * This retrieves the device location histories
     *
     * @param deviceIdentifier Device Identifier object
     * @param from Specified start timestamp
     * @param to Specified end timestamp
     * @throws DeviceManagementException
     * @return list of device's location histories
     */
    List<DeviceLocationHistory> getDeviceLocationInfo(DeviceIdentifier deviceIdentifier, long from, long to)
            throws DeviceManagementException;

    /**
     * This retrieves the device pull notification payload and passes to device type pull notification subscriber.
     * @throws PullNotificationExecutionFailedException
     */
    void notifyPullNotificationSubscriber(DeviceIdentifier deviceIdentifier, Operation operation)
            throws PullNotificationExecutionFailedException;

    List<Integer> getDeviceEnrolledTenants() throws DeviceManagementException;

    List<GeoCluster> findGeoClusters(String deviceType, GeoCoordinate southWest, GeoCoordinate northEast,
                                            int geohashLength) throws DeviceManagementException;

    int getDeviceCountOfTypeByStatus(String deviceType, String deviceStatus) throws DeviceManagementException;

    List<String> getDeviceIdentifiersByStatus(String deviceType, String deviceStatus) throws DeviceManagementException;

    boolean bulkUpdateDeviceStatus(String deviceType, List<String> deviceList, String status) throws DeviceManagementException;

    boolean updateEnrollment(String owner, List<String> deviceIdentifiers)
            throws DeviceManagementException, UserNotFoundException, InvalidDeviceException;

    boolean addDeviceTypeVersion(DeviceTypeVersion deviceTypeVersion) throws DeviceManagementException;

    List<DeviceTypeVersion> getDeviceTypeVersions(String typeName) throws DeviceManagementException;

    boolean updateDeviceTypeVersion(DeviceTypeVersion deviceTypeVersion) throws DeviceManagementException;

    boolean isDeviceTypeVersionChangeAuthorized(String typeName, String version) throws DeviceManagementException;

    DeviceTypeVersion getDeviceTypeVersion(String deviceTypeName, String version) throws
            DeviceManagementException;
    /**
     * Retrieves a list of configurations of a specific device
     * using the device's properties
     * @param propertyMap properties by which devices need to be drawn
     * @return list of device configuration
     * @throws DeviceManagementException if any service level or DAO level error occurs
     * @throws DeviceNotFoundException if there is no any device can found for specified properties
     * @throws UnauthorizedDeviceAccessException if the required token property is not found on
     * @throws AmbiguousConfigurationException if configuration is ambiguous
     * the property payload
     */
    DeviceConfiguration getDeviceConfiguration(Map<String, String> propertyMap)
            throws DeviceManagementException, DeviceNotFoundException, UnauthorizedDeviceAccessException,
                   AmbiguousConfigurationException;

    /**
     * Transfer device from super tenant to another tenant
     *
     * @param deviceTransferRequest DTO of the transfer request
     * @return tru if device transferee, otherwise false
     */
    List<String> transferDeviceToTenant(DeviceTransferRequest deviceTransferRequest) throws DeviceManagementException, DeviceNotFoundException;

    /**
     * This method retrieves a list of subscribed devices.
     *
     * @param devicesIds devices ids of the subscribed devices.
     * @param offsetValue offset value for get paginated request.
     * @param limitValue limit value for get paginated request.
     * @param status status of the devices.
     * @return {@link PaginationResult}
     * @throws DeviceManagementException if any service level or DAO level error occurs.
     */
    PaginationResult getAppSubscribedDevices(int offsetValue, int limitValue,
                                             List<Integer> devicesIds, String status) throws DeviceManagementException;
}
