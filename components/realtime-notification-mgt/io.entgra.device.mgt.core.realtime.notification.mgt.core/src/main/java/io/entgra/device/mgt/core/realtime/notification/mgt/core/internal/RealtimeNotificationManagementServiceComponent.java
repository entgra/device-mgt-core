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

package io.entgra.device.mgt.core.realtime.notification.mgt.core.internal;

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceFeatureOperations;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.service.RealtimeNotificationConfigService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.service.RealtimeNotificationManagementService;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.config.RealtimeNotificationConfigurationManager;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.config.archive.RealtimeNotificationArchiveConfigManager;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.RealtimeNotificationManagementDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.archive.RealtimeNotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.archive.RealtimeNotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.exception.RealtimeNotificationArchivalTaskManagerException;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.impl.RealtimeNotificationConfigServiceImpl;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.impl.RealtimeNotificationManagementServiceImpl;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.task.RealtimeNotificationArchivalTaskManager;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.task.RealtimeNotificationArchivalTaskManagerImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "io.entgra.device.mgt.core.realtime.notification.mgt.core.internal.RealtimeNotificationManagementServiceComponent",
        immediate = true)
public class RealtimeNotificationManagementServiceComponent {
    private static Log log = LogFactory.getLog(RealtimeNotificationManagementServiceComponent.class);

    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        BundleContext bundleContext = componentContext.getBundleContext();
        try {
            RealtimeNotificationConfigurationManager notificationConfigManager = RealtimeNotificationConfigurationManager.getInstance();
            RealtimeNotificationManagementDAOFactory.init(notificationConfigManager
                    .getNotificationManagementRepository().getDataSourceConfig());
            RealtimeNotificationArchivalSourceDAOFactory.init(notificationConfigManager
                    .getNotificationManagementRepository().getDataSourceConfig());
            RealtimeNotificationArchiveConfigManager notificationArchConfigManager =
                    RealtimeNotificationArchiveConfigManager.getInstance();
            RealtimeNotificationArchivalDestDAOFactory.init(notificationArchConfigManager
                    .getNotificationArchiveRepository().getDataSourceConfig());
            RealtimeNotificationManagementService notificationManagementService = new RealtimeNotificationManagementServiceImpl();
            bundleContext.registerService(RealtimeNotificationManagementService.class.getName(),
                    notificationManagementService, null);
            RealtimeNotificationConfigService notificationConfigurationService = new RealtimeNotificationConfigServiceImpl();
            bundleContext.registerService(RealtimeNotificationConfigService.class.getName(),
                    notificationConfigurationService, null);
            RealtimeNotificationArchivalTaskManager archivalTaskManager = new RealtimeNotificationArchivalTaskManagerImpl();
            try {
                archivalTaskManager.startTask();
            } catch (RealtimeNotificationArchivalTaskManagerException e) {
                String message = e.getMessage();
                if (message != null && message.contains("is already active for tenant")) {
                    log.info("RealtimeNotification archival task is already scheduled. Skipping...");
                } else {
                    log.error("Error occurred while starting the RealtimeNotification Archival Task.", e);
                }
            }
        } catch (Throwable e) {
            String msg = "Error occurred while activating " + RealtimeNotificationManagementServiceComponent.class.getName();
            log.error(msg, e);
        }
    }
    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        // Do nothing
    }

    /**
     * Sets DeviceManagementProviderService.
     *
     * @param deviceManagementProviderService An instance of DeviceManagementProviderService
     */
    @Reference(
            name = "device.mgt.provider.service",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementProviderService")
    protected void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        RealtimeNotificationManagementDataHolder.getInstance()
                .setDeviceManagementProviderService(deviceManagementProviderService);
    }

    /**
     * Unsets DeviceManagementProviderService.
     *
     * @param deviceManagementProviderService An instance of DeviceManagementProviderService
     */
    protected void unsetDeviceManagementProviderService(
            DeviceManagementProviderService deviceManagementProviderService) {
        RealtimeNotificationManagementDataHolder.getInstance().setDeviceManagementProviderService(null);
    }

    /**
     * Sets MetadataManagementService.
     *
     * @param metaDataManagementService An instance of MetadataManagementService
     */
    @Reference(
            name = "io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementServiceComponent",
            service = io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            bind = "setMetadataManagementService",
            unbind = "unsetMetadataManagementService")
    protected void setMetadataManagementService(MetadataManagementService metaDataManagementService) {
        RealtimeNotificationManagementDataHolder.getInstance().setMetaDataManagementService(metaDataManagementService);
        if (log.isDebugEnabled()) {
            log.debug("Meta data Management Service is set successfully");
        }
    }

    /**
     * Unsets MetadataManagementService.
     *
     * @param metaDataManagementService An instance of MetadataManagementService
     */
    protected void unsetMetadataManagementService(MetadataManagementService metaDataManagementService) {
        RealtimeNotificationManagementDataHolder.getInstance().setMetaDataManagementService(null);
        if (log.isDebugEnabled()) {
            log.debug("Meta data Management Service is unset successfully");
        }
    }

    /**
     * Sets RealmService.
     *
     * @param realmService An instance of RealmService
     */
    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        RealtimeNotificationManagementDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets RealmService.
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        RealtimeNotificationManagementDataHolder.getInstance().setRealmService(null);
    }

    /**
     * Sets TaskService.
     *
     * @param taskService An instance of RealmService
     */
    @Reference(
            name = "ntask.component",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            bind = "setTaskService",
            unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) {
        RealtimeNotificationManagementDataHolder.getInstance().setTaskService(taskService);
        if (log.isDebugEnabled()) {
            log.debug("Task service is set successfully");
        }
    }

    /**
     * Unsets TaskService.
     *
     * @param taskService An instance of RealmService
     */
    protected void unsetTaskService(TaskService taskService) {
        RealtimeNotificationManagementDataHolder.getInstance().setTaskService(null);
        if (log.isDebugEnabled()) {
            log.debug("Task service is unset successfully");
        }
    }

    /**
     * Sets DeviceFeatureOperations service.
     *
     * @param deviceFeatureOperations An instance of DeviceFeatureOperations
     */
    @Reference(
            name = "device.feature.operations.service",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceFeatureOperations.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceFeatureOperations")
    protected void setDeviceFeatureOperations(DeviceFeatureOperations deviceFeatureOperations) {
        RealtimeNotificationManagementDataHolder.getInstance().setDeviceFeatureOperations(deviceFeatureOperations);
        if (log.isDebugEnabled()) {
            log.debug("DeviceFeatureOperations service is set successfully");
        }
    }

    /**
     * Unsets DeviceFeatureOperations service.
     *
     * @param deviceFeatureOperations An instance of DeviceFeatureOperations
     */
    protected void unsetDeviceFeatureOperations(DeviceFeatureOperations deviceFeatureOperations) {
        RealtimeNotificationManagementDataHolder.getInstance().setDeviceFeatureOperations(null);
        if (log.isDebugEnabled()) {
            log.debug("DeviceFeatureOperations service is unset successfully");
        }
    }

}
