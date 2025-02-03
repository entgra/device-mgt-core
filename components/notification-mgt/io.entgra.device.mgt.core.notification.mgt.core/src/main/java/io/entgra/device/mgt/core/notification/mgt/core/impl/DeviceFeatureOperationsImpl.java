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

package io.entgra.device.mgt.core.notification.mgt.core.impl;

import io.entgra.device.mgt.core.device.mgt.common.Feature;
import io.entgra.device.mgt.core.device.mgt.common.FeatureManager;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.notification.mgt.common.dto.DeviceFeatureInfo;
import io.entgra.device.mgt.core.notification.mgt.common.exception.DeviceFeatureOperationException;
import io.entgra.device.mgt.core.notification.mgt.common.service.DeviceFeatureOperations;
import io.entgra.device.mgt.core.notification.mgt.core.dao.DeviceFeatureOperationDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.exception.NotificationManagementDAOException;
import io.entgra.device.mgt.core.notification.mgt.core.util.DeviceFeatureOperationsUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class DeviceFeatureOperationsImpl implements DeviceFeatureOperations {

    private static final Log log = LogFactory.getLog(DeviceFeatureOperationsImpl.class);
    private final DeviceFeatureOperationDAO deviceFeatureOperationDAO;

    public DeviceFeatureOperationsImpl() {
        this.deviceFeatureOperationDAO = NotificationManagementDAOFactory.getDeviceFeatureOperationDAO();
    }

    public static final String PLATFORM_ANDROID = "android";
    public static final String PLATFORM_WINDOWS = "windows";
    public static final String PLATFORM_IOS = "ios";
    public static final String FEATURE_TYPE_OPERATION = "operation";

    @Override
    public List<DeviceFeatureInfo> getDeviceFeatureOperations() throws DeviceFeatureOperationException {
        List<DeviceFeatureInfo> featureList = new ArrayList<>();
        try {
            DeviceManagementProviderService dms = DeviceFeatureOperationsUtil.getDeviceManagementService();
            String[] platforms = {PLATFORM_ANDROID, PLATFORM_WINDOWS, PLATFORM_IOS};
            for (String platform : platforms) {
                FeatureManager fm = dms.getFeatureManager(platform);
                List<Feature> features = fm.getFeatures(FEATURE_TYPE_OPERATION);
                if (features == null || features.isEmpty()) {
                    log.warn("No device features available for platform: " + platform);
                    continue;
                }
                for (Feature feature : features) {
                    DeviceFeatureInfo featureInfo = new DeviceFeatureInfo();
                    featureInfo.setOperationCode(feature.getCode());
                    featureInfo.setName(feature.getName());
                    featureInfo.setDescription(feature.getDescription());
                    featureInfo.setDeviceType(platform);
                    featureList.add(featureInfo);
                }
            }
            NotificationManagementDAOFactory.openConnection();
            deviceFeatureOperationDAO.updateDeviceFeatureDetails(featureList);
        } catch (DeviceFeatureOperationException e) {
            String msg = "An error occurred while retrieving device feature operations.";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (DeviceTypeNotFoundException e) {
            String msg = "An error occurred while retrieving device types.";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "An error occurred while retrieving device management services.";
            log.error(msg, e);
            throw new DeviceFeatureOperationException(msg, e);
        } catch (NotificationManagementDAOException e) {
            String msg = "An error occurred while handling device feature operations.";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
        return featureList;
    }
}

