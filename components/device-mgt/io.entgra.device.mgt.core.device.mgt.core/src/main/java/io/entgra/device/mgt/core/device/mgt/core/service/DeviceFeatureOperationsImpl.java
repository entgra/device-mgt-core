/*
 *  Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.Feature;
import io.entgra.device.mgt.core.device.mgt.common.FeatureManager;
import io.entgra.device.mgt.core.device.mgt.common.dto.DeviceFeatureInfo;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceFeatureOperationException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceFeatureOperationsDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceFeatureOperationDAO;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeviceFeatureOperationsImpl implements DeviceFeatureOperations {

    private static final Log log = LogFactory.getLog(DeviceFeatureOperationsImpl.class);
    private final DeviceFeatureOperationDAO deviceFeatureOperationDAO;

    public DeviceFeatureOperationsImpl() {
        this.deviceFeatureOperationDAO = DeviceFeatureOperationsDAOFactory.getDeviceFeatureOperationDAO();
    }

    public static final String FEATURE_TYPE_OPERATION = "operation";

    @Override
    public List<DeviceFeatureInfo> getDeviceFeatureOperations() throws DeviceFeatureOperationException {
        List<DeviceFeatureInfo> featureList = new ArrayList<>();
        DeviceManagementProviderService deviceManagementProvider = DeviceManagementDataHolder
                .getInstance().getDeviceManagementProvider();
        try {
            List<DeviceType> deviceTypes = deviceManagementProvider.getDeviceTypes();
            if (deviceTypes == null || deviceTypes.isEmpty()) {
                log.warn("No device types available.");
                return featureList;
            }
            for (DeviceType deviceType : deviceTypes) {
                String deviceTypeName = deviceType.getName();
                try {
                    FeatureManager featureManager = deviceManagementProvider.getFeatureManager(deviceTypeName);
                    List<Feature> features = featureManager.getFeatures(FEATURE_TYPE_OPERATION);
                    if (features == null || features.isEmpty()) {
                        log.warn("No device features available for device type: " + deviceTypeName);
                        continue;
                    }
                    for (Feature feature : features) {
                        DeviceFeatureInfo featureInfo = new DeviceFeatureInfo();
                        featureInfo.setOperationCode(feature.getCode());
                        featureInfo.setName(feature.getName());
                        featureInfo.setDescription(feature.getDescription());
                        featureInfo.setDeviceType(deviceTypeName);
                        featureList.add(featureInfo);
                    }
                } catch (DeviceTypeNotFoundException e) {
                    log.error("Feature manager not found for device type: " + deviceTypeName, e);
                }
            }
            DeviceFeatureOperationsDAOFactory.beginTransaction();
            deviceFeatureOperationDAO.updateDeviceFeatureDetails(featureList);
            DeviceFeatureOperationsDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceFeatureOperationsDAOFactory.rollbackTransaction();
            String msg = "An error occurred while retrieving device types.";
            log.error(msg, e);
            throw new DeviceFeatureOperationException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "An error occurred while retrieving device management services.";
            log.error(msg, e);
            throw new DeviceFeatureOperationException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceFeatureOperationException(msg, e);
        } finally {
            DeviceFeatureOperationsDAOFactory.closeConnection();
        }
        return featureList;
    }

    @Override
    public List<DeviceFeatureInfo> getOperationDetails(String code, String name, String type)
            throws DeviceFeatureOperationException {
        List<DeviceFeatureInfo> operationList;
        try {
            DeviceFeatureOperationsDAOFactory.openConnection();
            operationList = deviceFeatureOperationDAO.getOperationDetails(code, name, type);
        } catch (SQLException e) {
            String msg = "Error retrieving operation details from DB table.";
            log.error(msg, e);
            throw new DeviceFeatureOperationException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error retrieving operation details.";
            log.error(msg, e);
            throw new DeviceFeatureOperationException(msg, e);
        } finally {
            DeviceFeatureOperationsDAOFactory.closeConnection();
        }
        return operationList;
    }
}
