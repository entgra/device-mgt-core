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
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;

public class NotificationHelper {
    private static final Log log = LogFactory.getLog(NotificationHelper.class);
    private static final Gson gson = new Gson();

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
        String tenantDomain = getTenantDomain();
        Set<String> tenantAware = new HashSet<>();
        for (String u : usernameSet) {
            tenantAware.add(toTenantAwareUsername(u, tenantDomain));
        }
        return new ArrayList<>(tenantAware);
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
        if (log.isDebugEnabled()) {
            log.debug("Fetching notification configuration for code: " + code);
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Fetching all notification configurations from metadata.");
        }
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
     * Validates that a user exists in the system and returns the tenant-aware username
     *
     * @param username the username to validate
     * @return tenant-aware username
     */
    public static String getTenantAwareUsernameIfUserExists(String username) throws NotificationManagementException {
        if (username == null || username.trim().isEmpty()) {
            String msg = "Username must not be null or empty.";
            log.warn(msg);
            throw new NotificationManagementException(msg);
        }
        try {
            String tenantDomain = getTenantDomain();
            // normalize to the tenant-aware username we use for notification storage/queries.
            String tenantAwareUsername = toTenantAwareUsername(username, tenantDomain);
            String userToValidate = stripTenantDomainIfMatches(tenantAwareUsername, tenantDomain);
            UserStoreManager userStoreManager =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
            if (!userStoreManager.isExistingUser(userToValidate)) {
                String msg = "User by username: " + username + " does not exist.";
                throw new NotificationManagementException(msg);
            }
            return tenantAwareUsername;
        } catch (UserStoreException e) {
            String msg = "Error while retrieving the user.";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        }
    }

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
            UserStoreManager userStoreManager =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
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
                String tenantDomain = getTenantDomain();
                String userToValidate = stripTenantDomainIfMatches(user, tenantDomain);
                if (!userStoreManager.isExistingUser(userToValidate)) {
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

    /**
     * resolves the tenant domain
     * if the tenant domain cannot be resolved from the thread-local carbon context, this falls back to
     * {@code carbon.super} when the tenant context is confirmed to be the super. for non-super tenants, fail fast.
     * @return tenant domain (never blank)
     */
    private static String getTenantDomain() {
        final PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String domain = ctx.getTenantDomain();
        if (domain != null && !domain.trim().isEmpty()) {
            return domain.trim();
        }
        int tenantId = ctx.getTenantId();
        if (tenantId == Constants.SUPER_TENANT_ID) {
            return Constants.SUPER_TENANT_DOMAIN;
        }
        String msg = "Tenant domain is not available in the thread-local Carbon context for tenantId: " + tenantId;
        log.error(msg);
        throw new IllegalStateException(msg);
    }

    /**
     * converts a username into a tenant-aware username for notification storage or publishing,
     * with validation to ensure the tenant domain is correct.
     * - for the super tenant, usernames are stored/published without the suffix {@code @carbon.super}.
     * - for sub-tenants, usernames are ensured to be tenant-aware:
     * -- if the username already contains {@code @} and the domain matches {@code tenantDomain}, it is returned as-is.
     * -- if the username contains {@code @} but the domain does not match {@code tenantDomain},
     *    the domain is replaced with {@code tenantDomain}.
     * -- if the username does not contain {@code @}, {@code @tenantDomain} is appended.
     * - if the username is {@code null}, {@code null} is returned.
     * - if the username is empty or only whitespace, it is trimmed and returned as an empty string.
     * @param username the raw username
     * @param tenantDomain the tenant domain to validate against
     * @return a tenant-aware username with the correct tenant domain
     */
    private static String toTenantAwareUsername(String username, String tenantDomain) {
        if (username == null) {
            return null;
        }
        String user = username.trim();
        if (user.isEmpty()) {
            return user;
        }
        if (Constants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
            return user.replace("@" + Constants.SUPER_TENANT_DOMAIN, "");
        }
        String suffix = "@" + tenantDomain;
        if (user.endsWith(suffix)) {
            return user;
        }
        return user + suffix;
    }

    /**
     * strips the tenant domain from a tenant-aware username, if it matches the given tenant domain.
     * this is used for user-store validations where the tenant realm expects the local username
     * (without {@code @tenantDomain}).
     * @param username username possibly containing {@code @tenantDomain}
     * @param tenantDomain tenant domain to strip if present
     * @return local username if stripped, otherwise the input username
     */
    private static String stripTenantDomainIfMatches(String username, String tenantDomain) {
        if (username == null) {
            return null;
        }
        String user = username.trim();
        if (user.isEmpty() || tenantDomain == null || tenantDomain.trim().isEmpty()) {
            return user;
        }
        String suffix = "@" + tenantDomain.trim();
        if (user.endsWith(suffix)) {
            return user.substring(0, user.length() - suffix.length());
        }
        return user;
    }
}
