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


package io.entgra.device.mgt.core.device.mgt.core.privacy.impl;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.PrivacyComplianceException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.privacy.PrivacyComplianceProvider;
import io.entgra.device.mgt.core.device.mgt.core.privacy.dao.PrivacyComplianceDAO;
import io.entgra.device.mgt.core.device.mgt.core.privacy.dao.PrivacyComplianceDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivacyComplianceProviderImpl implements PrivacyComplianceProvider {

    private static final Log log = LogFactory.getLog(PrivacyComplianceProviderImpl.class);

    PrivacyComplianceDAO complianceDAO;

    public PrivacyComplianceProviderImpl() {
        complianceDAO = DeviceManagementDAOFactory.getPrivacyComplianceDAO();
    }

    @Override
    public void deleteDevicesOfUser(String username) throws PrivacyComplianceException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting the requested users.");
        }
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            List<DeviceEnrollmentMapping> enrollmentMappings = complianceDAO.getDevicesOfUser(username, tenantId);
            if(enrollmentMappings == null || enrollmentMappings.isEmpty()){
                log.info("No enrolments found with the user..!");
                return;
            }
            Map<Integer, List<Integer>> deviceMap = new HashMap<>();
            int x = -1;
            for (DeviceEnrollmentMapping m : enrollmentMappings) {
                if (m.getDeviceId() != x) {
                    x = m.getDeviceId();
                    List<Integer> enrolments = new ArrayList<>();
                    enrolments.add(m.getEnrolmentId());
                    deviceMap.put(m.getDeviceId(), enrolments);
                } else {
                    deviceMap.get(m.getDeviceId()).add(m.getEnrolmentId());
                }
            }
            for (int deviceId : deviceMap.keySet()) {
                List<Integer> enrollmentIds = deviceMap.get(deviceId);
                for (Integer enrolmentId : enrollmentIds) {
                    complianceDAO.deleteDeviceOperationDetails(enrolmentId);
                    complianceDAO.deleteOperationEnrolmentMappings(enrolmentId);
                    complianceDAO.deleteDeviceApplications(deviceId, enrolmentId, tenantId);
                    complianceDAO.deleteDeviceDetails(deviceId, enrolmentId);
                    complianceDAO.deleteDeviceProperties(deviceId, enrolmentId, tenantId);
                    complianceDAO.deleteDeviceLocation(deviceId, enrolmentId);
                    complianceDAO.deleteDeviceEnrollments(deviceId, tenantId);
                }
                complianceDAO.deleteDevice(deviceId, tenantId);
                String message = String.format("Task Delete Device executed for user %s, and device %d was deleted.",
                        username, deviceId);
                DeviceManagementDataHolder.getInstance().getNotificationManagementService()
                        .handleTaskNotificationIfApplicable("DELETE_DEVICE", tenantId, message);
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (PrivacyComplianceDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting the devices and details of the given user";
            log.error(msg, e);
            throw new PrivacyComplianceException(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Database error occurred while deleting the devices and details of the given user";
            log.error(msg, e);
            throw new PrivacyComplianceException(msg, e);
        } catch (NotificationManagementException e) {
            String msg = "An Error occurred while updating handleTaskNotificationIfApplicable";
            log.error(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        log.info("Requested users device has been successfully removed..!");
    }

    @Override
    public void deleteDeviceDetails(DeviceIdentifier deviceIdentifier) throws PrivacyComplianceException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting the requested device details.");
        }
        try {
            Device device = this.getDevice(deviceIdentifier);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            complianceDAO.deleteDeviceOperationDetails(device.getEnrolmentInfo().getId());
            complianceDAO.deleteOperationEnrolmentMappings(device.getEnrolmentInfo().getId());
            complianceDAO.deleteDeviceApplications(device.getId(), device.getEnrolmentInfo().getId(), tenantId);
            complianceDAO.deleteDeviceDetails(device.getId(), device.getEnrolmentInfo().getId());
            complianceDAO.deleteDeviceProperties(device.getId(), device.getEnrolmentInfo().getId(), tenantId);
            complianceDAO.deleteDeviceLocation(device.getId(), device.getEnrolmentInfo().getId());
            complianceDAO.deleteDeviceEnrollments(device.getId(), tenantId);
            complianceDAO.deleteDevice(device.getId(), tenantId);
            String message = String.format("Task Delete Device executed for device %d.", device.getId());
            DeviceManagementDataHolder.getInstance().getNotificationManagementService()
                    .handleTaskNotificationIfApplicable("DELETE_DEVICE", tenantId, message);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Database error occurred while deleting the device details.";
            log.error(msg, e);
            throw new PrivacyComplianceException(msg, e);
        } catch (PrivacyComplianceDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting the device details.";
            log.error(msg, e);
            throw new PrivacyComplianceException(msg, e);
        } catch (NotificationManagementException e) {
            String msg = "An Error occurred while updating handleTaskNotificationIfApplicable";
            log.error(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }


    }

    private Device getDevice(DeviceIdentifier deviceId) throws PrivacyComplianceException {
        try {
            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId, false);
        } catch (DeviceManagementException e) {
            throw new PrivacyComplianceException(
                    "Error occurred while retrieving device info.", e);
        }
    }
}

