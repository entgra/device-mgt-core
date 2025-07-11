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
package io.entgra.device.mgt.core.application.mgt.core.internal;

import io.entgra.device.mgt.core.application.mgt.common.config.LifecycleState;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationStorageManager;
import io.entgra.device.mgt.core.application.mgt.common.services.AppmDataHandler;
import io.entgra.device.mgt.core.application.mgt.common.services.FileTransferService;
import io.entgra.device.mgt.core.application.mgt.common.services.ReviewManager;
import io.entgra.device.mgt.core.application.mgt.common.services.SPApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.services.SubscriptionManager;
import io.entgra.device.mgt.core.application.mgt.common.services.VPPApplicationManager;
import io.entgra.device.mgt.core.application.mgt.core.config.ConfigurationManager;
import io.entgra.device.mgt.core.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import io.entgra.device.mgt.core.application.mgt.core.impl.AppmDataHandlerImpl;
import io.entgra.device.mgt.core.application.mgt.core.impl.FileTransferServiceImpl;
import io.entgra.device.mgt.core.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.device.mgt.core.application.mgt.core.task.ScheduledAppSubscriptionTaskManager;
import io.entgra.device.mgt.core.application.mgt.core.util.ApplicationManagementUtil;
import io.entgra.device.mgt.core.device.mgt.common.device.firmware.model.mgt.DeviceFirmwareModelManagementService;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;


@SuppressWarnings("unused")
@Component(
        name = "io.entgra.device.mgt.core.application.mgt.core.internal.ApplicationManagementServiceComponent",
        immediate = true)
public class ApplicationManagementServiceComponent {

    private static Log log = LogFactory.getLog(ApplicationManagementServiceComponent.class);


    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        BundleContext bundleContext = componentContext.getBundleContext();
        try {
            String dataSourceName = ConfigurationManager.getInstance().getConfiguration().getDatasourceName();
            ApplicationManagementDAOFactory.init(dataSourceName);

            List<LifecycleState> lifecycleStates = ConfigurationManager.getInstance().
                    getConfiguration().getLifecycleStates();
            LifecycleStateManager lifecycleStateManager = ApplicationManagementUtil.getLifecycleStateMangerInstance();
            lifecycleStateManager.init(lifecycleStates);
            DataHolder.getInstance().setLifecycleStateManger(lifecycleStateManager);
            bundleContext.registerService(LifecycleStateManager.class.getName(), lifecycleStateManager, null);

            SPApplicationManager SPApplicationManager = ApplicationManagementUtil.getSPApplicationManagerInstance();
            DataHolder.getInstance().setISApplicationManager(SPApplicationManager);
            bundleContext.registerService(SPApplicationManager.class.getName(), SPApplicationManager, null);

            ApplicationManager applicationManager = ApplicationManagementUtil.getApplicationManagerInstance();
            applicationManager
                    .addApplicationCategories(ConfigurationManager.getInstance().getConfiguration().getAppCategories());
            DataHolder.getInstance().setApplicationManager(applicationManager);
            bundleContext.registerService(ApplicationManager.class.getName(), applicationManager, null);

            ReviewManager reviewManager = ApplicationManagementUtil.getReviewManagerInstance();
            DataHolder.getInstance().setReviewManager(reviewManager);
            bundleContext.registerService(ReviewManager.class.getName(), reviewManager, null);

            SubscriptionManager subscriptionManager = ApplicationManagementUtil.getSubscriptionManagerInstance();
            DataHolder.getInstance().setSubscriptionManager(subscriptionManager);
            bundleContext.registerService(SubscriptionManager.class.getName(), subscriptionManager, null);

            ApplicationStorageManager applicationStorageManager = ApplicationManagementUtil
                    .getApplicationStorageManagerInstance();
            DataHolder.getInstance().setApplicationStorageManager(applicationStorageManager);
            bundleContext.registerService(ApplicationStorageManager.class.getName(), applicationStorageManager, null);

            AppmDataHandler configManager = new AppmDataHandlerImpl();
            DataHolder.getInstance().setConfigManager(configManager);
            bundleContext.registerService(AppmDataHandler.class.getName(), configManager, null);

            // TODO: Get the new instance from extension like others
            VPPApplicationManager vppApplicationManager = ApplicationManagementUtil
                    .getVPPManagerInstance();
            DataHolder.getInstance().setVppApplicationManager(vppApplicationManager);
            bundleContext.registerService(VPPApplicationManager.class.getName(), vppApplicationManager, null);

            FileTransferService fileTransferService = FileTransferServiceImpl.getInstance();
            DataHolder.getInstance().setFileTransferService(fileTransferService);
            bundleContext.registerService(FileTransferService.class.getName(), fileTransferService, null);

            ScheduledAppSubscriptionTaskManager taskManager = new ScheduledAppSubscriptionTaskManager();
            taskManager.scheduleCleanupTask();

            log.info("ApplicationManagement core bundle has been successfully initialized");
        } catch (Throwable e) {
            log.error("Error occurred while initializing app management core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    @SuppressWarnings("unused")
    @Reference(
            name = "device.mgt.provider.service",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementService")
    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagementProviderService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ApplicationDTO Management OSGI Manager");
        }
        DataHolder.getInstance().setDeviceManagementService(deviceManagementProviderService);
    }

    @SuppressWarnings("unused")
    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementProviderService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing ApplicationDTO Management OSGI Manager");
        }
        DataHolder.getInstance().setDeviceManagementService(null);
    }

    @SuppressWarnings("unused")
    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        DataHolder.getInstance().setRealmService(realmService);
    }

    @SuppressWarnings("unused")
    protected void unsetRealmService(RealmService realmService) {
        DataHolder.getInstance().setRealmService(null);
    }

    @SuppressWarnings("unused")
    @Reference(
            name = "datasource.service",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {
        /*Not implemented. Not needed but to make sure the datasource service are registered, as it is needed create
         databases. */
    }

    @SuppressWarnings("unused")
    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        /*Not implemented. Not needed but to make sure the datasource service are registered, as it is needed to create
         databases.*/
    }

    @SuppressWarnings("unused")
    @Reference(
            name = "task.service",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    public void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the task service to Application Management SC.");
        }
        DataHolder.getInstance().setTaskService(taskService);
    }

    @SuppressWarnings("unused")
    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the task service from Application Management SC");
        }
        DataHolder.getInstance().setTaskService(null);
    }

    @Reference(
            name = "metadata.mgt.service",
            service = io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetadataManagementService")
    protected void setMetadataManagementService(MetadataManagementService metadataManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the metadata management service to Application Management Service.");
        }
        DataHolder.getInstance().setMetadataManagementService(metadataManagementService);
    }

    protected void unsetMetadataManagementService(MetadataManagementService metadataManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the metadata management service from Application Management Service.");
        }
        DataHolder.getInstance().setMetadataManagementService(null);
    }

    @Reference(
            name = "device.firmware.model.mgt.service",
            service = io.entgra.device.mgt.core.device.mgt.common.device.firmware.model.mgt.DeviceFirmwareModelManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceFirmwareModelManagementService")
    protected void setDeviceFirmwareModelManagementService(DeviceFirmwareModelManagementService deviceFirmwareModelManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the device firmware model management service.");
        }
        DataHolder.getInstance().setDeviceFirmwareModelManagementService(deviceFirmwareModelManagementService);
    }

    protected void unsetDeviceFirmwareModelManagementService(DeviceFirmwareModelManagementService deviceFirmwareModelManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the device firmware model management service.");
        }
        DataHolder.getInstance().setDeviceFirmwareModelManagementService(null);
    }
}
