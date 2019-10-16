/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.application.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.application.mgt.common.config.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.config.UIConfiguration;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.services.AppmDataHandler;
import org.wso2.carbon.device.application.mgt.common.services.ReviewManager;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.impl.AppmDataHandlerImpl;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.core.task.ScheduledAppSubscriptionTaskManager;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagementUtil;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.application.mgt.service" immediate="true"
 * @scr.reference name="org.wso2.carbon.device.manager"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementService"
 * unbind="unsetDeviceManagementService"
 * @scr.reference name="realm.service"
 * immediate="true"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="datasource.service"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 * @scr.reference name="app.mgt.ntask.component"
 * interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setTaskService"
 * unbind="unsetTaskService"
 */
@SuppressWarnings("unused")
public class ApplicationManagementServiceComponent {

    private static Log log = LogFactory.getLog(ApplicationManagementServiceComponent.class);


    @SuppressWarnings("unused")
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

            UIConfiguration uiConfiguration = ConfigurationManager.getInstance().
                    getConfiguration().getUiConfiguration();
            AppmDataHandler configManager = new AppmDataHandlerImpl(uiConfiguration);
            DataHolder.getInstance().setConfigManager(configManager);
            bundleContext.registerService(AppmDataHandler.class.getName(), configManager, null);

            ScheduledAppSubscriptionTaskManager taskManager = new ScheduledAppSubscriptionTaskManager();
            taskManager.scheduleCleanupTask();

            log.info("ApplicationManagement core bundle has been successfully initialized");
        } catch (Throwable e) {
            log.error("Error occurred while initializing app management core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    @SuppressWarnings("unused")
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
    protected void setRealmService(RealmService realmService) {
        DataHolder.getInstance().setRealmService(realmService);
    }

    @SuppressWarnings("unused")
    protected void unsetRealmService(RealmService realmService) {
        DataHolder.getInstance().setRealmService(null);
    }

    @SuppressWarnings("unused")
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
}
