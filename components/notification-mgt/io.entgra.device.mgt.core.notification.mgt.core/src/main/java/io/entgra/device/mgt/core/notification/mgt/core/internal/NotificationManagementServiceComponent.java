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

import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.MetadataManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.internal.TenantCreateObserver;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationConfigService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationManagementService;
import io.entgra.device.mgt.core.notification.mgt.core.config.NotificationConfigurationManager;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.impl.NotificationConfigServiceImpl;
import io.entgra.device.mgt.core.notification.mgt.core.impl.NotificationManagementServiceImpl;

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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;


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
            NotificationManagementDAOFactory.init(notificationConfigManager.getNotificationManagementRepository().getDataSourceConfig());

            NotificationManagementService notificationManagementService = new NotificationManagementServiceImpl();
            bundleContext.registerService(NotificationManagementService.class.getName(),
                    notificationManagementService, null);
        } catch (Throwable t) {
            String msg = "Error occurred while activating " + NotificationManagementServiceComponent.class.getName();
            log.error(msg, t);
        }


        try {
            NotificationConfigService notificationConfigurationService = new NotificationConfigServiceImpl();
            bundleContext.registerService(NotificationConfigService.class.getName(),
                    notificationConfigurationService, null);
        } catch (Throwable t) {
            String msg = "Error occurred while activating " + NotificationManagementServiceComponent.class.getName();
            log.error(msg, t);
        }

        try {
            MetadataManagementService metaDataManagementService = new MetadataManagementServiceImpl();
            bundleContext.registerService(MetadataManagementService.class.getName(),
                    metaDataManagementService, null);
        } catch (Throwable t) {
            String msg = "Error occurred while activating " + NotificationManagementServiceComponent.class.getName();
            log.error(msg, t);
        }



    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        // Do nothing
    }

    @Reference(
            name = "device.mgt.provider.service",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementProviderService")
    protected void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        NotificationManagementDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);

    }


    protected void unsetDeviceManagementProviderService(
            DeviceManagementProviderService deviceManagementProviderService) {
        NotificationManagementDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);

    }


}
