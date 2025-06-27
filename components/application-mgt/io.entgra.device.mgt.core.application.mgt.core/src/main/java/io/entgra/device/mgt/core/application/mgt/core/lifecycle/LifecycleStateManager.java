/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.application.mgt.core.lifecycle;

import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherServiceImpl;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherStartupHandler;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherService;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import io.entgra.device.mgt.core.application.mgt.common.ApplicationType;
import io.entgra.device.mgt.core.application.mgt.common.config.LifecycleState;
import io.entgra.device.mgt.core.application.mgt.common.exception.LifecycleManagementException;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.DefaultPermission;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionUtils;
import io.entgra.device.mgt.core.device.mgt.core.search.mgt.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents the activities related to lifecycle management
 */
public class LifecycleStateManager {

    private Map<String, LifecycleState> lifecycleStates;
    private static final Log log = LogFactory.getLog(LifecycleStateManager.class);

    public void init(List<LifecycleState> states) throws LifecycleManagementException {
        lifecycleStates = new HashMap<>();
        APIPublisherService publisher = new APIPublisherServiceImpl();
        List<DefaultPermission> allDefaultPermissions = new ArrayList<>();
        for (LifecycleState lifecycleState : states) {
            if (lifecycleState.getProceedingStates() != null) {
                lifecycleState.getProceedingStates().replaceAll(String::toUpperCase);
            }
            lifecycleStates.put(lifecycleState.getName().toUpperCase(), lifecycleState);
            DefaultPermission defaultPermission = new DefaultPermission();
            defaultPermission.setName(PermissionUtils.ADMIN_PERMISSION_REGISTRY_PATH + lifecycleState.getPermission());
            defaultPermission.setScopeMapping(lifecycleState.getScopeMapping());
            allDefaultPermissions.add(defaultPermission);
        }
        try {
            APIPublisherStartupHandler.updateScopeMetadataEntryAndRegistryWithDefaultScopes(allDefaultPermissions);
            publisher.addDefaultScopesIfNotExist(allDefaultPermissions);
        } catch (APIManagerPublisherException e) {
            String errorMsg = "Failed to update API publisher with default permissions.";
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, e);
        }
    }

    public Map<String, LifecycleState> getLifecycleConfig() throws LifecycleManagementException {
        if (lifecycleStates == null) {
            String msg = "Lifecycle configuration in not initialized.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
        return lifecycleStates;
    }

    public List<String> getNextLifecycleStates(String currentLifecycleState) {
        return lifecycleStates.get(currentLifecycleState.toUpperCase()).getProceedingStates();
    }

    public boolean isValidStateChange(String currentState, String nextState, String username, int tenantId)
            throws LifecycleManagementException {
        UserRealm userRealm;
        String permission = getPermissionForStateChange(nextState);
        if (permission != null) {
            try {
                userRealm = DataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
                if (userRealm != null && userRealm.getAuthorizationManager() != null && userRealm
                        .getAuthorizationManager()
                        .isUserAuthorized(username, PermissionUtils.getAbsolutePermissionPath(permission),
                                Constants.UI_EXECUTE)) {
                    if (currentState.equalsIgnoreCase(nextState)) {
                        return true;
                    }
                    LifecycleState matchingState = getMatchingState(currentState);
                    if (matchingState != null) {
                        return getMatchingNextState(matchingState.getProceedingStates(), nextState);
                    }
                    return false;
                }
                return false;
            } catch (UserStoreException e) {
                String msg = "UserStoreException exception from changing the state from : " + currentState + "  to: "
                        + nextState + " with username : " + username + " and tenant Id : " + tenantId;
                log.error(msg, e);
                throw new LifecycleManagementException(msg, e);
            }
        } else {
            String msg = "Required permissions cannot be found for the state : " + nextState;
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    private LifecycleState getMatchingState(String currentState) {
        for (Map.Entry<String, LifecycleState> lifecycleState : lifecycleStates.entrySet()) {
            if (lifecycleState.getKey().equalsIgnoreCase(currentState)) {
                return lifecycleState.getValue();
            }
        }
        return null;
    }

    private boolean getMatchingNextState(List<String> proceedingStates, String nextState) {
        for (String stateName : proceedingStates) {
            if (stateName.equalsIgnoreCase(nextState)) {
                return true;
            }
        }
        return false;
    }

    private String getPermissionForStateChange(String nextState) {
        for (Map.Entry<String, LifecycleState> lifecycleState : lifecycleStates.entrySet()) {
            if (lifecycleState.getKey().equalsIgnoreCase(nextState)) {
                return lifecycleState.getValue().getPermission();
            }
        }
        return null;
    }

    public boolean isDeletableState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isDeletableState();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public boolean isUpdatableState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isAppUpdatable();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public boolean isInstallableState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isAppInstallable();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public boolean isInitialState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isInitialState();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public boolean isEndState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isEndState();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public String getInitialState() throws LifecycleManagementException {
        String initialState;
        for (Map.Entry<String, LifecycleState> lifecycleState : lifecycleStates.entrySet()) {
            if (lifecycleState.getValue().isInitialState()) {
                initialState = lifecycleState.getKey();
                return initialState;
            }
        }
        String msg = "Haven't defined the initial state in the application-manager.xml. Please add initial state "
                + "to the <LifecycleStates> section in the app-manager.xml";
        log.error(msg);
        throw new LifecycleManagementException(msg);
    }

    public String getEndState() throws LifecycleManagementException {
        String endState = null;
        for (Map.Entry<String, LifecycleState> stringStateEntry : lifecycleStates.entrySet()) {
            if (stringStateEntry.getValue().isEndState()) {
                endState = stringStateEntry.getKey();
                break;
            }
        }
        if (endState == null) {
            String msg = "Haven't defined the end state in the application-manager.xml. Please add end state "
                    + "to the <LifecycleStates> section in the app-manager.xml";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
        return endState;
    }

    /**
     * Retrieves the installable lifecycle state for a given application type.
     *
     * <p>This method validates the provided application type against known {@link ApplicationType} values.
     * Then it searches for a single lifecycle state marked as installable for the specified application type,
     * as defined in the application's lifecycle configuration (e.g., <LifecycleStates> section in app-manager.xml).</p>
     *
     * @param applicationType the type of application (e.g., ENTERPRISE, PUBLIC, WEB_APP etc.) for which the installable state is requested
     * @return the name of the installable lifecycle state for the specified application type
     * @throws LifecycleManagementException if:
     * <ul>
     *   <li>the application type is invalid</li>
     *   <li>no installable state is found</li>
     *   <li>more than one installable state is defined for the application type</li>
     * </ul>
     */
    public String getInstallableState(String applicationType) throws LifecycleManagementException {

        // Validate application type
        boolean isValidApplicationType = Arrays.stream(ApplicationType.values())
                .anyMatch(type -> type.name().equalsIgnoreCase(applicationType));

        if (!isValidApplicationType) {
            String msg = "Requesting installable lifecycle status for invalid application type: " + applicationType;
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }

        // Filter installable lifecycle states
        List<String> installableStates = lifecycleStates.entrySet().stream()
                .filter(entry -> entry.getValue().isAppInstallable() && entry.getValue().isApplicableFor(applicationType))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (installableStates.isEmpty()) {
            String msg = "Haven't defined the installable state in the application-manager.xml. Please add installable "
                    + "state to the <LifecycleStates> section in the app-manager.xml";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
        if (installableStates.size() > 1) {
            String msg = "More than one application installable lifecycle state for the application type: "
                    + applicationType + " has been defined in the app-manager.xml. Please verify the " +
                    "application-manager.xml";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
        return installableStates.get(0);
    }

    /**
     * Retrieves the installable lifecycle states for different application categories (apps and firmware)
     * as defined in the lifecycle configuration (typically from `app-manager.xml`).
     *
     * <p>This method performs the following validations:
     * <ul>
     *     <li>Ensures that only one installable state is defined for device apps (i.e., applications of type
     *         ENTERPRISE, WEB_APP, WEB_CLIP, PUBLIC) and one for firmware (i.e., CUSTOM).</li>
     *     <li>Validates that device app types must be fully matched as a set. Partial matches are considered invalid.</li>
     *     <li>Throws exceptions if no installable state is found, multiple installable states exist for the same category,
     *         or the applicable types are misconfigured.</li>
     * </ul>
     *
     * @return a map containing installable states with keys "apps" and/or "firmware" mapped to the respective state names
     * @throws LifecycleManagementException if no installable state is found, multiple installable states exist for the same category,
     *                                      or the configuration is invalid or ambiguous
     */
    public Map<String, String> getInstallableStates() throws LifecycleManagementException {
        Map<String, String> installableStates = new HashMap<>();

        Set<String> expectedDeviceAppTypes = Set.of(
                ApplicationType.ENTERPRISE.name(),
                ApplicationType.WEB_APP.name(),
                ApplicationType.WEB_CLIP.name(),
                ApplicationType.PUBLIC.name()
        );
        String expectedFirmwareType = ApplicationType.CUSTOM.name();

        boolean appsStateSet = false;
        boolean firmwareStateSet = false;

        for (Map.Entry<String, LifecycleState> entry : lifecycleStates.entrySet()) {
            LifecycleState state = entry.getValue();

            if (!state.isAppInstallable()) {
                continue;
            }

            List<String> applicableTypes = state.getApplicableTypes();
            Set<String> applicableTypeSet = new HashSet<>(applicableTypes);

            // Full match with expected device app types
            if (applicableTypeSet.equals(expectedDeviceAppTypes)) {
                if (appsStateSet) {
                    String msg = "Multiple installable states defined for apps in app-manager.xml. Only one is allowed.";
                    log.error(msg);
                    throw new LifecycleManagementException(msg);
                }
                installableStates.put("apps", state.getName());
                appsStateSet = true;

                // Exact single match with firmware
            } else if (applicableTypeSet.size() == 1 && applicableTypeSet.contains(expectedFirmwareType)) {
                if (firmwareStateSet) {
                    String msg =  "Multiple installable states defined for firmware in app-manager.xml. Only one is allowed.";
                    log.error(msg);
                    throw new LifecycleManagementException(msg);
                }
                installableStates.put("firmware", state.getName());
                firmwareStateSet = true;

                // Partial or invalid configuration
            } else {
                String msg = "Invalid applicableTypes in installable state '" + state.getName() +
                        "'. Must match either all of: " + expectedDeviceAppTypes +
                        " or exactly: " + expectedFirmwareType;
                log.error(msg);
                throw new LifecycleManagementException(msg);
            }
        }

        if (!appsStateSet && !firmwareStateSet) {
            String msg = "No valid installable states defined in app-manager.xml for either apps or firmware.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
        return installableStates;
    }

    public boolean isStateExist(String currentState) {
        for (Map.Entry<String, LifecycleState> stringStateEntry : lifecycleStates.entrySet()) {
            if (stringStateEntry.getKey().equalsIgnoreCase(currentState)) {
                return true;
            }
        }
        return false;
    }

    public void setLifecycleStates(Map<String, LifecycleState> lifecycleStates) {
        this.lifecycleStates = lifecycleStates;
    }
}
