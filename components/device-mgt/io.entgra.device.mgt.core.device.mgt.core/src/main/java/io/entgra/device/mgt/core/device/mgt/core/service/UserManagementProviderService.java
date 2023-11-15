/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.*;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.UserManagementException;

import java.util.List;

/**
 * Proxy class for all Device Management related operations that take the corresponding plugin type in
 * and resolve the appropriate plugin implementation
 */
public interface UserManagementProviderService {

    /**
     * Method to retrieve the filtered roles of the user.
     *
     * @param userInfo new user data
     * @return BasicUserInfoWrapper which has the information about the added user.
     * @throws UserManagementException If some unusual behaviour is observed while adding the user.
     */
    BasicUserInfoWrapper addUser(UserInfo userInfo) throws UserManagementException;

    /**
     * Method to retrieve the filtered roles of the user.
     *
     * @param username name of the user
     * @return BasicUserInfo object which consists of the user data.
     * @throws UserManagementException If some unusual behaviour is observed while getting the user data.
     */
    BasicUserInfo getUser(String username) throws UserManagementException;

    /**
     * Method to retrieve the filtered roles of the user.
     *
     * @param username name of the user the permissions are retrieved from
     * @return List of permissions of the given user.
     * @throws UserManagementException If some unusual behaviour is observed while fetching the permissions of user.
     */
    List<String> getPermissions(String username) throws UserManagementException;

    /**
     * Method to retrieve the filtered roles of the user.
     *
     * @param username name of the user the roles are retrieved from
     * @return  List of roles of the given user.
     * @throws UserManagementException If some unusual behaviour is observed while fetching the roles of user.
     */
    List<String> getRoles(String username) throws UserManagementException;

    /**
     * Method to retrieve the filtered roles of the user.
     *
     * @param username name of the user the roles are retrieved from
     * @return Object with the updated user data.
     * @throws UserManagementException If some unusual behaviour is observed while updating user.
     */
    BasicUserInfo updateUser(String username, UserInfo userInfo) throws UserManagementException;


    /**
     * Method to retrieve the list of users.
     *
     * @param appliedFilter filter to be applied when retrieving the user list
     * @param appliedLimit  this value is set to 1-1 to get the whole set of users
     * @param domain        domain of the users
     * @param limit         the maximum number of the users to be retrieved
     * @param offset        the starting index of data retrieval
     * @throws UserManagementException If some unusual behaviour is observed while fetching the users.
     */
    BasicUserInfoList getUsers(String appliedFilter, int appliedLimit, String domain, int limit, int offset) throws UserManagementException;

    /**
     * Method to retrieve the list of users.
     *
     * @param basicUserInfo data needed for the user search
     * @param limit         the maximum number of the users to be retrieved
     * @param offset        the starting index of data retrieval
     * @throws UserManagementException If some unusual behaviour is observed while fetching the users.
     */
    BasicUserInfoList getUsersSearch(BasicUserInfo basicUserInfo, int offset, int limit) throws UserManagementException;

    /**
     * Method to retrieve the list of users based on filter.
     *
     * @param filter data needed for the user search
     * @param userStoreDomain domain of the user
     * @param limit the maximum number of the users to be retrieved
     * @param offset the starting index of data retrieval
     * @throws UserManagementException If some unusual behaviour is observed while fetching the users.
     */
    List<UserInfo> getUserNames(String filter, String userStoreDomain, int offset, int limit) throws UserManagementException;

}
