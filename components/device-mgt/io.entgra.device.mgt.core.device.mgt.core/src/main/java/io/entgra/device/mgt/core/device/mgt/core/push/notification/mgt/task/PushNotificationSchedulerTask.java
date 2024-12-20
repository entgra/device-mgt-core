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
package io.entgra.device.mgt.core.device.mgt.core.push.notification.mgt.task;

import io.entgra.device.mgt.core.device.mgt.common.ServerCtxInfo;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationContext;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationStrategy;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationMapping;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationDAO;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationMappingDAO;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ${{@link PushNotificationSchedulerTask}} is for sending push notifications for given device batch.
 */
public class PushNotificationSchedulerTask implements Runnable {

    private static final Log log = LogFactory.getLog(PushNotificationSchedulerTask.class);
    private final OperationDAO operationDAO = OperationManagementDAOFactory.getOperationDAO();
    private final OperationMappingDAO operationMappingDAO = OperationManagementDAOFactory.getOperationMappingDAO();
    private final DeviceManagementProviderService provider = DeviceManagementDataHolder.getInstance()
            .getDeviceManagementProvider();

    @Override
    public void run() {
        try {
            Map<Integer, List<OperationMapping>> operationMappingsTenantMap = new HashMap<>();
            List<OperationMapping> operationsCompletedList = new LinkedList<>();
            if (log.isDebugEnabled()) {
                log.debug("Push notification job started");
            }
            try {
                //Get next available operation list per device batch
                OperationManagementDAOFactory.openConnection();
                try {
                    if (DeviceManagementDataHolder.getInstance().getHeartBeatService().isTaskPartitioningEnabled()) {
                        ServerCtxInfo serverCtxInfo = DeviceManagementDataHolder.getInstance().getHeartBeatService().getServerCtxInfo();
                        if (serverCtxInfo != null) {
                            operationMappingsTenantMap = operationDAO.getAllocatedOperationMappingsByStatus(Operation.Status
                                            .PENDING, Operation.PushNotificationStatus.SCHEDULED, DeviceConfigurationManager.getInstance()
                                            .getDeviceManagementConfig().getPushNotificationConfiguration().getSchedulerBatchSize(),
                                    serverCtxInfo.getActiveServerCount(), serverCtxInfo.getLocalServerHashIdx());
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Active server information not recorded yet.");
                            }
                        }
                    } else {
                        operationMappingsTenantMap = operationDAO.getOperationMappingsByStatus(Operation.Status
                                .PENDING, Operation.PushNotificationStatus.SCHEDULED, DeviceConfigurationManager.getInstance()
                                .getDeviceManagementConfig().getPushNotificationConfiguration().getSchedulerBatchSize());
                    }
                } catch (HeartBeatManagementException e) {
                    throw new RuntimeException(e);
                }
            } catch (OperationManagementDAOException e) {
                log.error("Unable to retrieve scheduled pending operations for task.", e);
            } finally {
                OperationManagementDAOFactory.closeConnection();
            }
            // Sending push notification to each device
            for (List<OperationMapping> operationMappings : operationMappingsTenantMap.values()) {
                for (OperationMapping operationMapping : operationMappings) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Sending push notification for operationId :" + operationMapping.getOperationId() +
                                    " to deviceId : " + operationMapping.getDeviceIdentifier().getId());
                        }
                        // Set tenant id and domain
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(operationMapping.getTenantId(), true);
                        // Get notification strategy for given device type
                        NotificationStrategy notificationStrategy = provider.getNotificationStrategyByDeviceType
                                (operationMapping.getDeviceIdentifier().getType());
                        // Send the push notification on given strategy
                        if (notificationStrategy != null) {
                            notificationStrategy.execute(new NotificationContext(operationMapping.getDeviceIdentifier(),
                                    provider.getOperation(operationMapping.getDeviceIdentifier().getType(), operationMapping
                                            .getOperationId())));
                            operationMapping.setPushNotificationStatus(Operation.PushNotificationStatus.COMPLETED);
                            operationsCompletedList.add(operationMapping);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Tenant '" + PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                        .getTenantDomain() + "' does not have push notification strategy.");
                            }
                        }
                    } catch (DeviceManagementException e) {
                        log.error("Error occurred while getting notification strategy for operation mapping " +
                                operationMapping.getDeviceIdentifier().getType(), e);
                    } catch (OperationManagementException e) {
                        log.error("Unable to get the operation for operation " + operationMapping.getOperationId(), e);
                    } catch (PushNotificationExecutionFailedException e) {
                        log.error("Error occurred while sending push notification to operation:  " + operationMapping
                                .getOperationId(), e);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
            // Update push notification status to competed for operations which already sent
            if (operationsCompletedList.size() > 0) {
                try {
                    OperationManagementDAOFactory.beginTransaction();
                    operationMappingDAO.updateOperationMapping(operationsCompletedList);
                    OperationManagementDAOFactory.commitTransaction();
                } catch (TransactionManagementException | OperationManagementDAOException e) {
                    OperationManagementDAOFactory.rollbackTransaction();
                    log.error("Error occurred while updating operation mappings for sent notifications ", e);
                } finally {
                    OperationManagementDAOFactory.closeConnection();
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Push notification job running completed.");
            }
        } catch (Throwable cause) {
            log.error("PushNotificationSchedulerTask failed due to " + cause.getMessage(), cause);
        }
    }
}
