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

import com.google.gson.JsonArray;
import io.entgra.device.mgt.core.device.mgt.common.*;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.UserManagementException;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.device.mgt.core.notification.logger.impl.EntgraDeviceEnrolmentLoggerImpl;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.mgt.UserRealmProxy;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

public class UserManagementProviderServiceImpl implements UserManagementProviderService {

    private static final EntgraLogger log = new EntgraDeviceEnrolmentLoggerImpl(UserManagementProviderServiceImpl.class);

    private static final String ROLE_EVERYONE = "Internal/everyone";

    @Override
    public BasicUserInfoWrapper addUser(UserInfo userInfo) throws UserManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            String initialUserPassword;
            if (userInfo.getPassword() != null) {
                initialUserPassword = userInfo.getPassword();
            } else {
                initialUserPassword = this.generateInitialUserPassword();
            }

            Map<String, String> defaultUserClaims =
                    this.buildDefaultUserClaims(userInfo.getFirstname(), userInfo.getLastname(),
                            userInfo.getEmailAddress(), true);

            userStoreManager.addUser(userInfo.getUsername(), initialUserPassword,
                    userInfo.getRoles(), defaultUserClaims, null);
            // Outputting debug message upon successful addition of user
            if (log.isDebugEnabled()) {
                log.debug("User '" + userInfo.getUsername() + "' has successfully been added.");
            }

            BasicUserInfo createdUserInfo = this.getBasicUserInfo(userInfo.getUsername());
            // Outputting debug message upon successful retrieval of user
            if (log.isDebugEnabled()) {
                log.debug("User by username: " + userInfo.getUsername() + " was found.");
            }
            DeviceManagementProviderService managementProviderService = DeviceManagementDataHolder
                    .getInstance().getDeviceManagementProvider();
            String[] bits = userInfo.getUsername().split("/");
            String username = bits[bits.length - 1];
            String recipient = userInfo.getEmailAddress();
            Properties props = new Properties();
            props.setProperty("first-name", userInfo.getFirstname());
            props.setProperty("last-name", userInfo.getLastname());
            props.setProperty("username", username);
            props.setProperty("password", initialUserPassword);

            EmailMetaInfo metaInfo = new EmailMetaInfo(recipient, props);
            BasicUserInfoWrapper userInfoWrapper = new BasicUserInfoWrapper();
            String message;
            try {
                managementProviderService.sendRegistrationEmail(metaInfo);
                message = "An invitation mail will be sent to this user to initiate device enrollment.";
            } catch (ConfigurationManagementException e) {
                message = "Mail Server is not configured. Email invitation will not be sent.";
            } catch (DeviceManagementException e) {
                throw new RuntimeException(e);
            }
            userInfoWrapper.setBasicUserInfo(createdUserInfo);
            userInfoWrapper.setMessage(message);
            return userInfoWrapper;
        }  catch (UserStoreException e) {
            String msg = "Error occurred while trying to add user '" + userInfo.getUsername() + "' to the " +
                    "underlying user management system";
            log.error(msg, e);
            throw new UserManagementException(msg, e);
        }
    }

    @Override
    public BasicUserInfo getUser(String username) throws UserManagementException {
        try {
            return this.getBasicUserInfo(username);
        } catch (UserStoreException e) {
            String message = "Error occurred while getting data of user '" + username + "'";
            log.error(message, e);
            throw new UserManagementException(message, e);
        }
    }

    @Override
    public List<String> getPermissions(String username) throws UserManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();

            List<String> roles = getFilteredRoles(userStoreManager, username);
            List<String> permissions = new ArrayList<>();
            UserRealm userRealm =  DeviceManagementDataHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantId);
            // Get permissions for each role
            for (String roleName : roles) {
                try {
                    permissions.addAll(getPermissionsListFromRole(roleName, userRealm, tenantId));
                } catch (UserAdminException e) {
                    String message = "Error occurred while retrieving the permissions of role '" + roleName + "'";
                    log.error(message, e);
                    throw new UserManagementException(message, e);
                }
            }
            return permissions;
        }  catch (UserStoreException e) {
            String message = "Error occurred while trying to retrieve roles of the user '" + username + "'";
            log.error(message, e);
            throw new UserManagementException(message, e);
        }
    }

    @Override
    public List<String> getRoles(String username) throws UserManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();

            List<String> roles = getFilteredRoles(userStoreManager, username);
            return roles;
        }  catch (UserStoreException e) {
            String message = "Error occurred while trying to retrieve roles of the user '" + username + "'";
            log.error(message, e);
            throw new UserManagementException(message, e);
        }
    }

    @Override
    public BasicUserInfo updateUser(String username, UserInfo userInfo) throws UserManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();

            Map<String, String> defaultUserClaims =
                    this.buildDefaultUserClaims(userInfo.getFirstname(), userInfo.getLastname(),
                            userInfo.getEmailAddress(), false);
            if (StringUtils.isNotEmpty(userInfo.getPassword())) {
                // Decoding Base64 encoded password
                userStoreManager.updateCredentialByAdmin(username,
                        userInfo.getPassword());
                log.debug("User credential of username: " + username + " has been changed");
            }
            List<String> currentRoles = this.getFilteredRoles(userStoreManager, username);

            List<String> newRoles = new ArrayList<>();
            if (userInfo.getRoles() != null) {
                newRoles = Arrays.asList(userInfo.getRoles());
            }

            List<String> rolesToAdd = new ArrayList<>(newRoles);
            List<String> rolesToDelete = new ArrayList<>();

            for (String role : currentRoles) {
                if (newRoles.contains(role)) {
                    rolesToAdd.remove(role);
                } else {
                    rolesToDelete.add(role);
                }
            }
            rolesToDelete.remove(ROLE_EVERYONE);
            rolesToAdd.remove(ROLE_EVERYONE);
            userStoreManager.updateRoleListOfUser(username,
                    rolesToDelete.toArray(new String[rolesToDelete.size()]),
                    rolesToAdd.toArray(new String[rolesToAdd.size()]));
            userStoreManager.setUserClaimValues(username, defaultUserClaims, null);
            // Outputting debug message upon successful addition of user
            if (log.isDebugEnabled()) {
                log.debug("User by username: " + username + " was successfully updated.");
            }

            return this.getBasicUserInfo(username);
        }  catch (UserStoreException e) {
            String message = "Error occurred while trying to retrieve roles of the user '" + username + "'";
            log.error(message, e);
            throw new UserManagementException(message, e);
        }
    }

    @Override
    public BasicUserInfoList getUsers(String appliedFilter, int appliedLimit, String domain, int limit, int offset)
            throws UserManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            List<BasicUserInfo> userList, offsetList;

            //As the listUsers function accepts limit only to accommodate offset we are passing offset + limit
            List<String> users = Arrays.asList(userStoreManager.listUsers(appliedFilter, appliedLimit));
            if (domain != null && !domain.isEmpty()) {
                users = getUsersFromDomain(domain, users);
            }
            userList = new ArrayList<>(users.size());
            BasicUserInfo user;
            for (String username : users) {
                if (DeviceManagementConstants.User.APIM_RESERVED_USER.equals(username) || DeviceManagementConstants.User.RESERVED_USER.equals(username)) {
                    continue;
                }
                user = getBasicUserInfo(username);
                userList.add(user);
            }

            int toIndex = offset + limit;
            int listSize = userList.size();
            int lastIndex = listSize - 1;

            if (offset <= lastIndex) {
                if (toIndex <= listSize) {
                    offsetList = userList.subList(offset, toIndex);
                } else {
                    offsetList = userList.subList(offset, listSize);
                }
            } else {
                offsetList = new ArrayList<>();
            }

            BasicUserInfoList result = new BasicUserInfoList();
            result.setList(offsetList);
            result.setCount(userList.size());

            return result;
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of users.";
            log.error(msg, e);
            throw new UserManagementException(msg, e);
        }
    }

    @Override
    public BasicUserInfoList getUsersSearch(BasicUserInfo basicUserInfo, int offset, int limit) throws UserManagementException {
        List<BasicUserInfo> filteredUserList = new ArrayList<>();
        List<String> commonUsers = null, tempList;
        try {
            if (basicUserInfo.getUsername() != null && StringUtils.isNotEmpty(basicUserInfo.getUsername())) {
                commonUsers = getUserList(null, basicUserInfo.getUsername());
            }
            if (commonUsers != null) {
                commonUsers.remove(DeviceManagementConstants.User.APIM_RESERVED_USER);
                commonUsers.remove(DeviceManagementConstants.User.RESERVED_USER);
            }

            if (!skipSearch(commonUsers) && basicUserInfo.getFirstname() != null && StringUtils.isNotEmpty(basicUserInfo.getFirstname())) {
                tempList = getUserList(DeviceManagementConstants.User.CLAIM_FIRST_NAME, basicUserInfo.getFirstname());
                if (commonUsers == null) {
                    commonUsers = tempList;
                } else {
                    commonUsers.retainAll(tempList);
                }
            }

            if (!skipSearch(commonUsers) && basicUserInfo.getLastname() != null && StringUtils.isNotEmpty(basicUserInfo.getLastname())) {
                tempList = getUserList(DeviceManagementConstants.User.CLAIM_LAST_NAME, basicUserInfo.getLastname());
                if (commonUsers == null || commonUsers.size() == 0) {
                    commonUsers = tempList;
                } else {
                    commonUsers.retainAll(tempList);
                }
            }

            if (!skipSearch(commonUsers)  && basicUserInfo.getEmailAddress() != null && StringUtils.isNotEmpty(basicUserInfo.getEmailAddress())) {
                tempList = getUserList(DeviceManagementConstants.User.CLAIM_EMAIL_ADDRESS, basicUserInfo.getEmailAddress());
                if (commonUsers == null || commonUsers.size() == 0) {
                    commonUsers = tempList;
                } else {
                    commonUsers.retainAll(tempList);
                }
            }

            BasicUserInfo newBasicUserInfo;
            if (commonUsers != null) {
                for (String user : commonUsers) {
                    newBasicUserInfo = new BasicUserInfo();
                    newBasicUserInfo.setUsername(user);
                    newBasicUserInfo.setEmailAddress(getClaimValue(user, DeviceManagementConstants.User.CLAIM_EMAIL_ADDRESS));
                    newBasicUserInfo.setFirstname(getClaimValue(user, DeviceManagementConstants.User.CLAIM_FIRST_NAME));
                    newBasicUserInfo.setLastname(getClaimValue(user, DeviceManagementConstants.User.CLAIM_LAST_NAME));
                    filteredUserList.add(newBasicUserInfo);
                }
            }

            int toIndex = offset + limit;
            int listSize = filteredUserList.size();
            int lastIndex = listSize - 1;

            List<BasicUserInfo> offsetList;
            if (offset <= lastIndex) {
                if (toIndex <= listSize) {
                    offsetList = filteredUserList.subList(offset, toIndex);
                } else {
                    offsetList = filteredUserList.subList(offset, listSize);
                }
            } else {
                offsetList = new ArrayList<>();
            }

            BasicUserInfoList basicUserInfoList = new BasicUserInfoList();
            basicUserInfoList.setList(offsetList);
            basicUserInfoList.setCount(commonUsers != null ? commonUsers.size() : 0);
            return basicUserInfoList;
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of users.";
            log.error(msg, e);
            throw new UserManagementException(msg, e);
        }
    }

    @Override
    public List<UserInfo> getUserNames(String filter, String userStoreDomain, int offset, int limit) throws UserManagementException {
        List<UserInfo> userList;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            String[] users;
            if (userStoreDomain.equals("all")) {
                users = userStoreManager.listUsers(filter + "*", limit);
            } else {
                users = userStoreManager.listUsers(userStoreDomain + "/" + filter + "*", limit);
            }
            userList = new ArrayList<>();
            UserInfo user;
            for (String username : users) {
                if (DeviceManagementConstants.User.APIM_RESERVED_USER.equals(username) || DeviceManagementConstants.User.RESERVED_USER.equals(username)) {
                    continue;
                }
                user = new UserInfo();
                user.setUsername(username);
                user.setEmailAddress(getClaimValue(username, DeviceManagementConstants.User.CLAIM_EMAIL_ADDRESS));
                user.setFirstname(getClaimValue(username, DeviceManagementConstants.User.CLAIM_FIRST_NAME));
                user.setLastname(getClaimValue(username, DeviceManagementConstants.User.CLAIM_LAST_NAME));
                userList.add(user);
            }
            return userList;
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of users using the filter : " + filter;
            log.error(msg, e);
            throw new UserManagementException(msg, e);
        }
    }

    @Override
    public List<UserInfo> updateUserClaimsForDevices(String username, JsonArray deviceList, String domain) throws UserManagementException {
        return null;
    }

//    @Override
//    public EmailMetaInfo inviteToEnrollDevice(EnrollmentInvitation enrollmentInvitation) throws UserManagementException {
//        try {
//            Set<String> recipients = new HashSet<>();
//            recipients.addAll(enrollmentInvitation.getRecipients());
//            Properties props = new Properties();
//            String username = DeviceMgtAPIUtils.getAuthenticatedUser();
//            String firstName = getClaimValue(username, DeviceManagementConstants.User.CLAIM_FIRST_NAME);
//            String lastName = getClaimValue(username, DeviceManagementConstants.User.CLAIM_LAST_NAME);
//            if (firstName == null) {
//                firstName = username;
//            }
//            if (lastName == null) {
//                lastName = "";
//            }
//            props.setProperty("first-name", firstName);
//            props.setProperty("last-name", lastName);
//            props.setProperty("device-type", enrollmentInvitation.getDeviceType());
//            EmailMetaInfo metaInfo = new EmailMetaInfo(recipients, props);
//            return metaInfo;
//        } catch (DeviceManagementException e) {
//            String msg = "Error occurred while inviting user to enrol their device";
//            log.error(msg, e);
//            throw new UserManagementException(msg, e);
//        } catch (UserStoreException e) {
//            String msg = "Error occurred while getting claim values to invite user";
//            log.error(msg, e);
//            throw new UserManagementException(msg, e);
//        } catch (ConfigurationManagementException e) {
//            String msg = "Error occurred while sending the email invitations. Mail server not configured.";
//            throw new UserManagementException(msg, e);
//        }
//    }


    @Override
    public int getCount() throws UserManagementException {
        return 0;
    }


    /**
     * User search provides an AND search result and if either of the filter returns an empty set of users, there is no
     * need to carry on the search further. This method decides whether to carry on the search or not.
     *
     * @param commonUsers current filtered user list.
     * @return <code>true</code> if further search is needed.
     */
    private boolean skipSearch(List<String> commonUsers) {
        return commonUsers != null && commonUsers.size() == 0;
    }


    /**
     * Searches users which matches a given filter based on a claim
     *
     * @param claim the claim value to apply the filter. If <code>null</code> users will be filtered by username.
     * @param filter the search query.
     * @return <code>List<String></code> of users which matches.
     * @throws UserStoreException If unable to search users.
     */
    private ArrayList<String> getUserList(String claim, String filter) throws UserStoreException {
        String defaultFilter = "*";

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        org.wso2.carbon.user.core.UserStoreManager  userStoreManager = (org.wso2.carbon.user.core.UserStoreManager) DeviceManagementDataHolder.getInstance().
                getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();

        String appliedFilter = filter + defaultFilter;

        String[] users;
        if (log.isDebugEnabled()) {
            log.debug("Searching Users - claim: " + claim + " filter: " + appliedFilter);
        }
        if (StringUtils.isEmpty(claim)) {
            users = userStoreManager.listUsers(appliedFilter, -1);
        } else {
            users = userStoreManager.getUserList(claim, appliedFilter, null);
        }

        if (log.isDebugEnabled()) {
            log.debug("Returned user count: " + users.length);
        }

        return new ArrayList<>(Arrays.asList(users));
    }


    /**
     * Iterates through the list of all users and returns a list of users from the specified user store domain
     * @param domain user store domain name
     * @param users list of all users from UserStoreManager
     * @return list of users from specified user store domain
     */
    public List<String> getUsersFromDomain(String domain, List<String> users) {
        List<String> userList = new ArrayList<>();
        for(String username : users) {
            String[] domainName = username.split("/");
            if(domain.equals(DeviceManagementConstants.User.PRIMARY_USER_STORE) && domainName.length == 1) {
                userList.add(username);
            } else if (domainName[0].equals(domain) && domainName.length > 1) {
                userList.add(username);
            }
        }
        return userList;
    }

    private String generateInitialUserPassword() {
        int passwordLength = 6;
        //defining the pool of characters to be used for initial password generation
        String lowerCaseCharset = "abcdefghijklmnopqrstuvwxyz";
        String upperCaseCharset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numericCharset = "0123456789";
        SecureRandom randomGenerator = new SecureRandom();
        String totalCharset = lowerCaseCharset + upperCaseCharset + numericCharset;
        int totalCharsetLength = totalCharset.length();
        StringBuilder initialUserPassword = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) {
            initialUserPassword.append(
                    totalCharset.charAt(randomGenerator.nextInt(totalCharsetLength)));
        }
        if (log.isDebugEnabled()) {
            log.debug("Initial user password is created for new user: " + initialUserPassword);
        }
        return initialUserPassword.toString();
    }

    private BasicUserInfo getBasicUserInfo(String username) throws UserStoreException {
        BasicUserInfo userInfo = new BasicUserInfo();
        userInfo.setUsername(username);
        userInfo.setEmailAddress(getClaimValue(username, DeviceManagementConstants.User.CLAIM_EMAIL_ADDRESS));
        userInfo.setFirstname(getClaimValue(username, DeviceManagementConstants.User.CLAIM_FIRST_NAME));
        userInfo.setLastname(getClaimValue(username, DeviceManagementConstants.User.CLAIM_LAST_NAME));
        userInfo.setCreatedDate(getClaimValue(username, DeviceManagementConstants.User.CLAIM_CREATED));
        userInfo.setModifiedDate(getClaimValue(username, DeviceManagementConstants.User.CLAIM_MODIFIED));
        return userInfo;
    }

    private String getClaimValue(String username, String claimUri) throws UserStoreException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().
                getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
        return userStoreManager.getUserClaimValue(username, claimUri, null);
    }

    private Map<String, String> buildDefaultUserClaims(String firstName, String lastName, String emailAddress,
                                                       boolean isFresh) {
        Map<String, String> defaultUserClaims = new HashMap<>();
        defaultUserClaims.put(DeviceManagementConstants.User.CLAIM_FIRST_NAME, firstName);
        defaultUserClaims.put(DeviceManagementConstants.User.CLAIM_LAST_NAME, lastName);
        defaultUserClaims.put(DeviceManagementConstants.User.CLAIM_EMAIL_ADDRESS, emailAddress);
        if (isFresh) {
            defaultUserClaims.put(DeviceManagementConstants.User.CLAIM_CREATED, String.valueOf(Instant.now().getEpochSecond()));
        } else {
            defaultUserClaims.put(DeviceManagementConstants.User.CLAIM_MODIFIED, String.valueOf(Instant.now().getEpochSecond()));
        }
        if (log.isDebugEnabled()) {
            log.debug("Default claim map is created for new user: " + defaultUserClaims.toString());
        }
        return defaultUserClaims;
    }

    private List<String> getFilteredRoles(UserStoreManager userStoreManager, String username)
            throws UserStoreException {
        String[] roleListOfUser;
        roleListOfUser = userStoreManager.getRoleListOfUser(username);
        List<String> filteredRoles = new ArrayList<>();
        for (String role : roleListOfUser) {
            if (!(role.startsWith("Internal/") || role.startsWith("Authentication/"))) {
                filteredRoles.add(role);
            }
        }
        return filteredRoles;
    }

    /**
     * Returns a list of permissions of a given role
     * @param roleName name of the role
     * @param tenantId the user's tenetId
     * @param userRealm user realm of the tenant
     * @return list of permissions
     * @throws UserAdminException If unable to get the permissions
     */
    private static List<String> getPermissionsListFromRole(String roleName, UserRealm userRealm, int tenantId)
            throws UserAdminException {
        org.wso2.carbon.user.core.UserRealm userRealmCore;
        try {
            userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
        } catch (ClassCastException e) {
            String message = "Provided UserRealm object is not an instance of org.wso2.carbon.user.core.UserRealm";
            log.error(message, e);
            throw new UserAdminException(message, e);
        }
        UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
        List<String> permissionsList = new ArrayList<>();
        final UIPermissionNode rolePermissions = userRealmProxy.getRolePermissions(roleName, tenantId);
        iteratePermissions(rolePermissions, permissionsList);
        return permissionsList;
    }

    /**
     * Extract permissions from a UiPermissionNode using recursions
     * @param uiPermissionNode an UiPermissionNode Object to extract permissions
     * @param list provided list to add permissions
     */
    public static void iteratePermissions(UIPermissionNode uiPermissionNode, List<String> list) {
        // To prevent NullPointer exceptions
        if (uiPermissionNode == null) {
            return;
        }
        for (UIPermissionNode permissionNode : uiPermissionNode.getNodeList()) {
            if (permissionNode != null) {
                if(permissionNode.isSelected()){
                    list.add(permissionNode.getResourcePath());
                }
                if (permissionNode.getNodeList() != null
                        && permissionNode.getNodeList().length > 0) {
                    iteratePermissions(permissionNode, list);
                }
            }
        }
    }

}
