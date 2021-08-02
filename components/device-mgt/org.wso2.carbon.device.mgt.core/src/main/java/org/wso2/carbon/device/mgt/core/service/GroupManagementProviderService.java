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
 *
 *
 * Copyright (c) 2021, Entgra (pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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

package org.wso2.carbon.device.mgt.core.service;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupNotExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.RoleDoesNotExistException;

import java.util.List;

/**
 * Interface for Group Management Services
 */
public interface GroupManagementProviderService {

    /**
     * Add new device group and create default role with default permissions.
     *
     * @param deviceGroup        to add
     * @param defaultRole        of the deviceGroup
     * @param defaultPermissions of the default role
     * @throws GroupManagementException
     */
    void createGroup(DeviceGroup deviceGroup, String defaultRole,
                     String[] defaultPermissions) throws GroupManagementException, GroupAlreadyExistException;

    /**
     * Update existing device group.
     *
     * @param deviceGroup  to update.
     * @param groupId of the group.
     * @throws GroupManagementException
     */
    void updateGroup(DeviceGroup deviceGroup, int groupId) throws GroupManagementException, GroupNotExistException;

    /**
     * Delete existing device group.
     *
     * @param groupId to be deleted.
     * @param isDeleteChildren to delete the children groups or not.
     * @return status of the delete operation.
     * @throws GroupManagementException
     */
    boolean deleteGroup(int groupId, boolean isDeleteChildren) throws GroupManagementException;

    /**
     * Get the device group provided the device group id.
     *
     * @param groupId of the group.
     * @param requireGroupProps to include group properties
     * @return group with details.
     * @throws GroupManagementException
     */
    DeviceGroup getGroup(int groupId, boolean requireGroupProps) throws GroupManagementException;

    /**
     * Get the device group provided the device group id and depth of children groups.
     *
     * @param groupId of the group.
     * @param requireGroupProps to include group properties.
     * @param depth of children groups to retrieve.
     * @return {@link DeviceGroup} group with details.
     * @throws GroupManagementException on error during retrieval of group
     */
    DeviceGroup getGroup(int groupId, boolean requireGroupProps, int depth) throws GroupManagementException;

    /**
     * Get the device group provided the device group name.
     *
     * @param groupName of the group.
     * @param requireGroupProps to include group properties
     * @return group with details.
     * @throws GroupManagementException
     */
    DeviceGroup getGroup(String groupName, boolean requireGroupProps) throws GroupManagementException;

    /**
     * Get the device group provided the device group id and depth of children groups.
     *
     * @param groupName of the group.
     * @param requireGroupProps to include group properties.
     * @param depth of children groups to retrieve.
     * @return {@link DeviceGroup} group with details.
     * @throws GroupManagementException on error during retrieval of group
     */
    DeviceGroup getGroup(String groupName, boolean requireGroupProps, int depth) throws GroupManagementException;

    /**
     * Get all device groups in tenant.
     *
     * @param requireGroupProps to include group properties
     * @return list of groups.
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(boolean requireGroupProps) throws GroupManagementException;

    /**
     * Get all device groups for user.
     *
     * @param username   of the user.
     * @param requireGroupProps to include group properties
     * @return list of groups
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(String username, boolean requireGroupProps) throws GroupManagementException;

    /**
     * Get device groups with pagination.
     *
     * @param paginationRequest to filter results
     * @param requireGroupProps to include group properties
     * @return list of groups.
     * @throws GroupManagementException
     */
    PaginationResult getGroups(GroupPaginationRequest paginationRequest, boolean requireGroupProps)
            throws GroupManagementException;

    /**
     * Get device groups belongs to specified user with pagination.
     *
     * @param username   of the user.
     * @param paginationRequest to filter results
     * @param requireGroupProps to include group properties
     * @return list of groups.
     * @throws GroupManagementException
     */
    PaginationResult getGroups(String username, GroupPaginationRequest paginationRequest, boolean requireGroupProps)
            throws GroupManagementException;

    /**
     * Get device groups with children groups hierarchically which belongs to specified user with pagination.
     *
     * @param username of the user.
     * @param request to filter results
     * @param requireGroupProps to include group properties
     * @return {@link PaginationResult} paginated groups.
     * @throws GroupManagementException on error during retrieval of groups with hierarchy
     */
    PaginationResult getGroupsWithHierarchy(String username, GroupPaginationRequest request,
            boolean requireGroupProps) throws GroupManagementException;

    /**
     * Get all device group count in tenant
     *
     * @return group count
     * @throws GroupManagementException
     */
    int getGroupCount() throws GroupManagementException;

    /**
     * Get all device group count in tenant by group status
     *
     * @return group count
     * @throws GroupManagementException
     */
    int getGroupCountByStatus(String status) throws GroupManagementException;

    /**
     * Get device group count of user
     *
     * @param username of the user
     * @param parentPath of the group
     * @return group count
     * @throws GroupManagementException
     */
    int getGroupCount(String username, String parentPath) throws GroupManagementException;

    /**
     * Manage device group sharing with user with list of roles.
     *
     * @param groupId  of the group
     * @param newRoles to be shared
     * @throws GroupManagementException UserDoesNotExistException
     */
    void manageGroupSharing(int groupId, List<String> newRoles)
            throws GroupManagementException, RoleDoesNotExistException;

    /**
     * Get all sharing roles for device group
     *
     * @param groupId   of the group
     * @return list of roles
     * @throws GroupManagementException
     */
    List<String> getRoles(int groupId) throws GroupManagementException;

    /**
     * Get all devices in device group as paginated result.
     *
     * @param groupId   of the group
     * @param startIndex for pagination.
     * @param rowCount   for pagination.
     * @param requireDeviceProps to include device properties.
     * @return list of devices in group.
     * @throws GroupManagementException
     */
    List<Device> getDevices(int groupId, int startIndex, int rowCount, boolean requireDeviceProps)
            throws GroupManagementException;

    /**
     * Get all devices in device group as paginated result.
     *
     * @param groupName of the group.
     * @param requireDeviceProps to include device properties.
     * @return list of devices in group.
     * @throws GroupManagementException
     */
    List<Device> getAllDevicesOfGroup(String groupName, boolean requireDeviceProps) throws GroupManagementException;

    /**
     * Get all devices that are in one of the given device status and belongs to given group.
     *
     * @param groupName Group name.
     * @param deviceStatuses Device statuses list.
     * @param requireDeviceProps to include device properties.
     * @return List of devices in group.
     * @throws GroupManagementException if error occurred while fetching devices
     */
    List<Device> getAllDevicesOfGroup(String groupName, List<String> deviceStatuses, boolean requireDeviceProps)
            throws GroupManagementException;

    /**
     * This method is used to retrieve the device count of a given group.
     *
     * @param groupId   of the group
     * @return returns the device count.
     * @throws GroupManagementException
     */
    int getDeviceCount(int groupId) throws GroupManagementException;

    /**
     * Add device to device group.
     *
     * @param groupId   of the group.
     * @param deviceIdentifiers of devices.
     * @throws GroupManagementException
     */
    void addDevices(int groupId, List<DeviceIdentifier> deviceIdentifiers)
            throws GroupManagementException, DeviceNotFoundException;

    /**
     * Remove device from device group.
     *
     * @param groupId   of the group.
     * @param deviceIdentifiers of devices.
     * @throws GroupManagementException
     */
    void removeDevice(int groupId, List<DeviceIdentifier> deviceIdentifiers)
            throws GroupManagementException, DeviceNotFoundException;
    /**
     * Get device groups of user with permission.
     *
     * @param username   of the user.
     * @param permission to filter.
     * @return group list with specified permissions.
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(String username, String permission, boolean requireGroupProps)
            throws GroupManagementException;

    /**
     * Get groups which contains particular device.
     *
     * @param deviceIdentifier of the device.
     * @return groups contain the device.
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(DeviceIdentifier deviceIdentifier, boolean requireGroupProps) throws GroupManagementException;

    /**
     * Get groups which contains particular device.
     *
     * @param device interested devoce.
     * @return groups contain the device.
     * @throws GroupManagementException
     */
    public List<DeviceGroup> getGroups(Device device, boolean requireGroupProps)
            throws GroupManagementException;

    /**
     * Checks for the default group existence and create group based on device ownership.
     * @param groupName of the group
     * @return DeviceGroup object
     * @throws GroupManagementException
     */
    DeviceGroup createDefaultGroup(String groupName) throws GroupManagementException;

    /**
     * Check device is belonging to a Device Group.
     *
     * @param groupId          of Device Group.
     * @param deviceIdentifier of the device.
     * @throws GroupManagementException on errors.
     */
    boolean isDeviceMappedToGroup(int groupId, DeviceIdentifier deviceIdentifier) throws GroupManagementException;

}
