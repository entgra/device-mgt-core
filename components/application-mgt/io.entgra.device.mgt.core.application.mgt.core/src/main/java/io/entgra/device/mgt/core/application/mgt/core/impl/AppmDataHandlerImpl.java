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

package io.entgra.device.mgt.core.application.mgt.core.impl;

import io.entgra.device.mgt.core.application.mgt.common.ApplicationType;
import io.entgra.device.mgt.core.application.mgt.common.config.LifecycleState;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationStorageManagementException;
import io.entgra.device.mgt.core.application.mgt.common.exception.LifecycleManagementException;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationStorageManager;
import io.entgra.device.mgt.core.application.mgt.common.services.AppmDataHandler;
import io.entgra.device.mgt.core.application.mgt.core.exception.BadRequestException;
import io.entgra.device.mgt.core.application.mgt.core.exception.NotFoundException;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.device.mgt.core.application.mgt.core.util.APIUtil;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppmDataHandlerImpl implements AppmDataHandler {

    private static final Log log = LogFactory.getLog(AppmDataHandlerImpl.class);
    private final LifecycleStateManager lifecycleStateManager;

    public AppmDataHandlerImpl() {
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
    }

    @Override
    public Map<String, LifecycleState> getLifecycleConfiguration() throws LifecycleManagementException {
        return lifecycleStateManager.getLifecycleConfig();
    }

    @Override
    public Map<String, LifecycleState> getLifecycleConfiguration(String applicationType)
            throws LifecycleManagementException {

        // Validate application type
        if (applicationType == null || applicationType.trim().isEmpty()) {
            String msg = "Application type cannot be null or empty";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }

        try {
            // Validate if it's a valid application type
            ApplicationType.valueOf(applicationType.toUpperCase());
        } catch (IllegalArgumentException e) {
            String msg = String.format("Invalid application type: %s. Supported types are: %s",
                    applicationType, Arrays.toString(ApplicationType.values()));
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }

        Map<String, LifecycleState> allStates = lifecycleStateManager.getLifecycleConfig();
        Map<String, LifecycleState> filteredStates = new HashMap<>();

        // Filter states based on applicableTypes
        for (Map.Entry<String, LifecycleState> entry : allStates.entrySet()) {
            LifecycleState state = entry.getValue();
            if (state.isApplicableFor(applicationType)) {
                // Filter proceeding states
                List<String> validProceedingStates = filterProceedingStates(
                        state.getProceedingStates(),
                        allStates,
                        applicationType
                );

                filteredStates.put(entry.getKey(), getFilteredState(state, validProceedingStates));
            }
        }

        validateLifecycleConfiguration(filteredStates, applicationType);
        return filteredStates;
    }

    // Getting a defensive copy with valid proceeding states
    private static LifecycleState getFilteredState(LifecycleState state, List<String> proceedingStates) {
        LifecycleState clone = new LifecycleState();
        clone.setProceedingStates(proceedingStates);
        clone.setEndState(state.isEndState());
        clone.setDeletableState(state.isDeletableState());
        clone.setInitialState(state.isInitialState());
        clone.setScopeMapping(state.getScopeMapping());
        clone.setApplicableTypes(state.getApplicableTypes());
        clone.setName(state.getName());
        clone.setAppUpdatable(state.isAppUpdatable());
        clone.setAppInstallable(state.isAppInstallable());
        clone.setPermission(state.getPermission());
        return clone;
    }

    /**
     *
     * @param proceedingStates
     * @param allStates
     * @param applicationType
     * @return
     */
    private List<String> filterProceedingStates(
            List<String> proceedingStates,
            Map<String, LifecycleState> allStates,
            String applicationType) {
        if (proceedingStates == null) {
            return new ArrayList<>();
        }

        return proceedingStates.stream()
                .filter(stateName -> {
                    LifecycleState state = allStates.get(stateName.toUpperCase());
                    return state != null && state.isApplicableFor(applicationType);
                })
                .collect(Collectors.toList());
    }


    private void validateLifecycleConfiguration(
            Map<String, LifecycleState> states,
            String applicationType) throws LifecycleManagementException {

        // Ensure we have at least one initial state
        if (states.values().stream()
                .noneMatch(LifecycleState::isInitialState)) {
            String msg = String.format(
                    "No initial state defined for application type: %s",
                    applicationType
            );
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }

        // Ensure we have at least one end state
        if (states.values().stream()
                .noneMatch(LifecycleState::isEndState)) {
            String msg = String.format(
                    "No end state defined for application type: %s",
                    applicationType
            );
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }

        // Validate that all proceeding states are valid
        for (LifecycleState state : states.values()) {
            List<String> proceedingStates = state.getProceedingStates();
            if (proceedingStates != null) {
                for (String proceedingState : proceedingStates) {
                    if (!states.containsKey(proceedingState.toUpperCase())) {
                        String msg = String.format(
                                "Invalid proceeding state %s for state %s in application type %s",
                                proceedingState, state.getName(), applicationType
                        );
                        log.error(msg);
                        throw new LifecycleManagementException(msg);
                    }
                }
            }
        }
    }

    @Override
    public InputStream getArtifactStream(int tenantId, String appHashValue, String folderName, String artifactName)
            throws ApplicationManagementException {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        validateArtifactDownloadRequest(tenantId, appHashValue, folderName, artifactName);
        try {
            InputStream inputStream = applicationStorageManager
                    .getFileStream(appHashValue, folderName, artifactName, tenantId);
            if (inputStream == null) {
                String msg = "Couldn't find the file in the file system. Tenant Id: " + tenantId + " App Has Value: "
                        + appHashValue + " Folder Name: " + folderName + " Artifact name: " + artifactName;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return inputStream;
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred when getting input stream of the " + artifactName + " file.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * Validate the artifact downloading request
     * @param tenantId Tenat Id
     * @param appHashValue Application hash value
     * @param folderName Folder Name
     * @param artifactName Artifact name
     * @throws BadRequestException if there is an invalid data to retrieve application artifact.
     */
    private void validateArtifactDownloadRequest(int tenantId, String appHashValue, String folderName,
            String artifactName) throws BadRequestException {
        if (tenantId != -1234 && tenantId <= 0) {
            String msg = "Found invalid tenant Id to get application artifact. Tenant Id: " + tenantId;
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (StringUtils.isBlank(appHashValue)) {
            String msg = "Found invalid application has value to get application artifact. Application hash value: "
                    + appHashValue;
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (StringUtils.isBlank(folderName)) {
            String msg = "Found invalid folder name to get application artifact. Folder name: " + folderName;
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (StringUtils.isBlank(artifactName)) {
            String msg = "Found invalid artifact name to get application artifact. Artifact name: " + artifactName;
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    @Override
    public InputStream getAgentStream(String tenantDomain, String deviceType) throws ApplicationManagementException {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            DeviceType deviceTypeObj = DataHolder.getInstance().getDeviceManagementService().getDeviceType(deviceType);
            if (deviceTypeObj == null) {
                String msg = "Couldn't find a registered device type called " + deviceType + " in the system.";
                log.error(msg);
                throw new NotFoundException(msg);
            }

            InputStream inputStream = applicationStorageManager.getFileStream(deviceType, tenantDomain);
            if (inputStream == null) {
                String msg = "Couldn't find the device type agent in the server. Device type: " + deviceType
                        + " Tenant Domain: " + tenantDomain;
                log.error(msg);
                throw new BadRequestException(msg);
            }
            return inputStream;
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred when getting input stream of the " + deviceType + " agent.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = " Error occurred when getting device type details. Device type " + deviceType;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }
}
