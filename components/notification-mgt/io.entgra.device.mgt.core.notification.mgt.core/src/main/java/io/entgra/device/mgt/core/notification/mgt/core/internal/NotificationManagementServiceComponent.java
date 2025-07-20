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

package io.entgra.device.mgt.core.notification.mgt.core.internal;

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceFeatureOperations;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationConfigService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationManagementService;
import io.entgra.device.mgt.core.notification.mgt.core.config.NotificationConfigurationManager;
import io.entgra.device.mgt.core.notification.mgt.core.config.archive.NotificationArchiveConfigManager;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.exception.NotificationArchivalTaskManagerException;
import io.entgra.device.mgt.core.notification.mgt.core.impl.NotificationConfigServiceImpl;
import io.entgra.device.mgt.core.notification.mgt.core.impl.NotificationManagementServiceImpl;
import io.entgra.device.mgt.core.notification.mgt.core.task.NotificationArchivalTaskManager;
import io.entgra.device.mgt.core.notification.mgt.core.task.NotificationArchivalTaskManagerImpl;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
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
        name = "io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementServiceComponent",
        immediate = true)
public class NotificationManagementServiceComponent {
    private static Log log = LogFactory.getLog(NotificationManagementServiceComponent.class);

    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        BundleContext bundleContext = componentContext.getBundleContext();
        try {
            NotificationConfigurationManager notificationConfigManager = NotificationConfigurationManager.getInstance();
            NotificationManagementDAOFactory.init(notificationConfigManager
                    .getNotificationManagementRepository().getDataSourceConfig());
            NotificationArchivalSourceDAOFactory.init(notificationConfigManager
                    .getNotificationManagementRepository().getDataSourceConfig());
            NotificationArchiveConfigManager notificationArchConfigManager =
                    NotificationArchiveConfigManager.getInstance();
            NotificationArchivalDestDAOFactory.init(notificationArchConfigManager
                    .getNotificationArchiveRepository().getDataSourceConfig());
            NotificationManagementService notificationManagementService = new NotificationManagementServiceImpl();
            bundleContext.registerService(NotificationManagementService.class.getName(),
                    notificationManagementService, null);
            NotificationConfigService notificationConfigurationService = new NotificationConfigServiceImpl();
            bundleContext.registerService(NotificationConfigService.class.getName(),
                    notificationConfigurationService, null);
            try {
                notificationConfigurationService.setDefaultNotificationArchiveMetadata(
                                Constants.DEFAULT_ARCHIVE_TYPE,
                                Constants.DEFAULT_ARCHIVE_PERIOD);
            } catch (NotificationConfigurationServiceException e) {
                log.error("Failed to set default notification archive metadata", e);
            }
            NotificationArchivalTaskManager archivalTaskManager = new NotificationArchivalTaskManagerImpl();
            try {
                archivalTaskManager.startTask();
            } catch (NotificationArchivalTaskManagerException e) {
                String message = e.getMessage();
                if (message != null && message.contains("is already active for tenant")) {
                    log.info("Notification archival task is already scheduled. Skipping...");
                } else {
                    log.error("Error occurred while starting the Notification Archival Task.", e);
                }
            }
        } catch (Throwable e) {
            String msg = "Error occurred while activating " + NotificationManagementServiceComponent.class.getName();
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
        NotificationManagementDataHolder.getInstance()
                .setDeviceManagementProviderService(deviceManagementProviderService);
    }

    /**
     * Unsets DeviceManagementProviderService.
     *
     * @param deviceManagementProviderService An instance of DeviceManagementProviderService
     */
    protected void unsetDeviceManagementProviderService(
            DeviceManagementProviderService deviceManagementProviderService) {
        NotificationManagementDataHolder.getInstance().setDeviceManagementProviderService(null);
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
        NotificationManagementDataHolder.getInstance().setMetaDataManagementService(metaDataManagementService);
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
        NotificationManagementDataHolder.getInstance().setMetaDataManagementService(null);
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
        NotificationManagementDataHolder.getInstance().setRealmService(realmService);
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
        NotificationManagementDataHolder.getInstance().setRealmService(null);
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
        NotificationManagementDataHolder.getInstance().setTaskService(taskService);
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
        NotificationManagementDataHolder.getInstance().setTaskService(null);
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
        NotificationManagementDataHolder.getInstance().setDeviceFeatureOperations(deviceFeatureOperations);
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
        NotificationManagementDataHolder.getInstance().setDeviceFeatureOperations(null);
        if (log.isDebugEnabled()) {
            log.debug("DeviceFeatureOperations service is unset successfully");
        }
    }

}
