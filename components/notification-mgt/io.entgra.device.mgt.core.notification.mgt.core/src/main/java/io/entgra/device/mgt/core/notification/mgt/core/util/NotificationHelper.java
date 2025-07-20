/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.notification.mgt.core.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.notification.mgt.common.beans.ArchivePeriod;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigRecipients;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;
import io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationHelper {
    private static final Log log = LogFactory.getLog(NotificationHelper.class);
    private static final Gson gson = new Gson();
    private static final int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

    /**
     * Extracts all usernames from the given recipients object including users and roles.
     *
     * @param recipients Recipients containing users and roles.
     * @param tenantId   Tenant ID to get the correct user store.
     * @return List of usernames.
     * @throws UserStoreException if there is an error accessing the user store.
     */
    public static List<String> extractUsernamesFromRecipients(NotificationConfigRecipients recipients, int tenantId)
            throws UserStoreException {
        Set<String> usernameSet = new HashSet<>();
        if (recipients == null) {
            return new ArrayList<>();
        }
        UserStoreManager userStoreManager = NotificationManagementDataHolder.getInstance()
                .getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
        List<String> users = recipients.getUsers();
        if (users != null) {
            usernameSet.addAll(users);
        }
        List<String> roles = recipients.getRoles();
        if (roles != null) {
            for (String role : roles) {
                String[] usersWithRole = userStoreManager.getUserListOfRole(role);
                usernameSet.addAll(Arrays.asList(usersWithRole));
            }
        }
        return new ArrayList<>(usernameSet);
    }

    /**
     * Retrieves a specific notification configuration by its code from metadata.
     *
     * @param code The configuration code to look for.
     * @return NotificationConfig object if found, otherwise null.
     * @throws NotificationManagementException if there's an error retrieving or parsing the metadata.
     */
    public static NotificationConfig getNotificationConfigurationByCode(String code)
            throws NotificationManagementException {
        log.info("Fetching notification configuration for code: " + code);
        MetadataManagementService metaDataService = NotificationManagementDataHolder
                .getInstance().getMetaDataManagementService();
        try {
            if (metaDataService == null) {
                log.error("MetaDataManagementService is null");
                throw new NotificationManagementException("MetaDataManagementService is not available");
            }
            Metadata existingMetadata = metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No notification configurations found for tenant");
                }
                return null;
            }
            if (log.isDebugEnabled()) {
                log.debug("Existing metadata: " + existingMetadata);
            }
            String metaValue = existingMetadata.getMetaValue();
            log.info("Meta value: " + metaValue);
            Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
            NotificationConfigurationList configList = gson.fromJson(metaValue, listType);
            if (configList == null || configList.getNotificationConfigurations() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Meta value could not be deserialized or config list is empty.");
                }
                return null;
            }
            for (NotificationConfig config : configList.getNotificationConfigurations()) {
                if (config.getCode().equalsIgnoreCase(code)) {
                    return config;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("No configuration found matching code: " + code);
            }
            return null;
        } catch (NullPointerException e) {
            String message = "Meta value doesn't exist for meta key.";
            log.error(message, e);
            throw new NotificationManagementException(message, e);
        } catch (MetadataManagementException e) {
            if (e.getMessage().contains("not found")) {
                String message = "Notification configurations not found for tenant ID";
                log.warn(message);
                throw new NotificationManagementException(message, e);
            } else {
                String message = "Unexpected error occurred while retrieving notification configurations for " +
                        "tenant ID.";
                log.error(message, e);
                throw new NotificationManagementException(message, e);
            }
        }
    }

    /**
     * Resolves a timestamp in the past based on a duration string like "6 days", "2 weeks", etc.
     *
     * @param period Duration ArchivePeriod (e.g., "7 days", "2 months") an object of a int and a string.
     * @return Timestamp object representing the cutoff time.
     * @throws IllegalArgumentException if the format is invalid.
     */
    public static Timestamp resolveCutoffTimestamp(ArchivePeriod period) {
        if (period == null) {
            return null;
        }
        int amount = period.getValue();
        String unit = period.getUnit();
        Calendar cal = Calendar.getInstance();
        switch (unit) {
            case "days":
                cal.add(Calendar.DAY_OF_MONTH, -amount);
                break;
            case "weeks":
                cal.add(Calendar.WEEK_OF_YEAR, -amount);
                break;
            case "months":
                cal.add(Calendar.MONTH, -amount);
                break;
            case "years":
                cal.add(Calendar.YEAR, -amount);
                break;
            default:
                throw new IllegalArgumentException("Unsupported time unit: " + unit);
        }
        return new Timestamp(cal.getTimeInMillis());
    }

    /**
     * Fetches all notification configurations from metadata storage.
     *
     * @return NotificationConfigurationList object if found, otherwise null.
     * @throws NotificationManagementException if there's an error retrieving the configurations.
     */
    public static NotificationConfigurationList getNotificationConfigurationsFromMetadata()
            throws NotificationManagementException {
        log.info("Fetching all notification configurations from metadata.");
        MetadataManagementService metaDataService = NotificationManagementDataHolder
                .getInstance().getMetaDataManagementService();
        try {
            if (metaDataService == null) {
                log.error("MetaDataManagementService is null. Skipping notification configuration loading.");
                return null;
            }
            Metadata existingMetadata = metaDataService.retrieveMetadata(Constants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata == null) {
                log.warn("No notification configuration metadata found.");
                return null;
            }
            String metaValue = existingMetadata.getMetaValue();
            Type listType = new TypeToken<NotificationConfigurationList>() {}.getType();
            return new Gson().fromJson(metaValue, listType);
        } catch (MetadataManagementException e) {
            String message = "Unexpected error occurred while retrieving notification configurations for tenant ID.";
            log.error(message, e);
            throw new NotificationManagementException(message, e);
        }
    }


    /**
     * Applies default archive values to a configuration list if they are missing.
     *
     * @param configurations The configuration list to update with default values.
     */
    public static void setDefaultArchivalValuesIfAbsent(NotificationConfigurationList configurations) {
        if (configurations.getDefaultArchiveAfter() == null) {
            configurations.setDefaultArchiveAfter(Constants.DEFAULT_ARCHIVE_PERIOD);
        }
        if (configurations.getDefaultArchiveType() == null || configurations.getDefaultArchiveType().isEmpty()) {
            configurations.setDefaultArchiveType(Constants.DEFAULT_ARCHIVE_TYPE);
        }
    }

    /**
     * Validates that a user exists in the system.
     * Checks if the provided username is not null or empty and exists in the user store.
     *
     * @param username the username to validate
     */
    public static void validateUserExists(String username) throws NotificationManagementException {
        if (username == null || username.trim().isEmpty()) {
            String msg = "Username must not be null or empty.";
            log.warn(msg);
            throw new NotificationManagementException(msg);
        }
        try {
            UserStoreManager userStoreManager = NotificationManagementDataHolder.getInstance()
                    .getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                String msg = "User by username: " + username + " does not exist.";
                throw new NotificationManagementException(msg);
            }
        } catch (UserStoreException e) {
            String msg = "Error while retrieving the user.";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
    }

    /**
     * Validates that all users and roles in the recipients exist in the system.
     * Checks if the recipients object is not null.
     * Returns an appropriate Response if any user or role does not exist or if there is an error.
     *
     * @param recipients the NotificationConfigRecipients object containing users and roles
     * @return a Response with error status if any user or role is invalid or does not exist, otherwise null
     */
    /**
     * Validates that all users and roles in the recipients exist in the system.
     * Throws NotificationConfigurationServiceException if any user or role is invalid or does not exist.
     *
     * @param recipients the NotificationConfigRecipients object containing users and roles
     * @throws NotificationConfigurationServiceException if recipients is null or any user/role does not exist
     */
    public static void validateRecipients(NotificationConfigRecipients recipients)
            throws NotificationConfigurationServiceException {
        if (recipients == null) {
            String msg = "Recipients must not be null.";
            log.warn(msg);
            throw new NotificationConfigurationServiceException(msg);
        }
        try {
            UserStoreManager userStoreManager = NotificationManagementDataHolder.getInstance()
                    .getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            // validate roles
            for (String role : recipients.getRoles()) {
                if (!userStoreManager.isExistingRole(role)) {
                    String msg = "No role exists with the name: " + role;
                    log.warn(msg);
                    throw new NotificationConfigurationServiceException(msg);
                }
            }
            // validate users
            for (String user : recipients.getUsers()) {
                if (!userStoreManager.isExistingUser(user)) {
                    String msg = "User by username: " + user + " does not exist.";
                    log.warn(msg);
                    throw new NotificationConfigurationServiceException(msg);
                }
            }
        } catch (UserStoreException e) {
            String msg = "Error while validating recipients.";
            log.error(msg, e);
            throw new NotificationConfigurationServiceException(msg, e);
        }
    }
}
