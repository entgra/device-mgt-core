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

import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;

import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

/**
 * DataHolder is responsible for holding the references to OSGI Services.
 */
public class NotificationManagementDataHolder {

    private DeviceManagementProviderService deviceManagementService;
    private TenantManager tenantManager;
    private MetadataManagementService metaDataManagementService;
    private RealmService realmService;
    private TaskService taskService;

    private static NotificationManagementDataHolder thisInstance = new NotificationManagementDataHolder();

    public static NotificationManagementDataHolder getInstance() {
        return thisInstance;
    }

    public DeviceManagementProviderService getDeviceManagementProviderService() {
        return deviceManagementService;
    }

    public void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementService) {
        this.deviceManagementService = deviceManagementService;
    }

    public TenantManager getTenantManager() {
        if (tenantManager == null) {
            throw new IllegalStateException("Tenant manager is not initialized properly");
        }
        return tenantManager;
    }

    private void setTenantManager(TenantManager tenantManager) {
        this.tenantManager = tenantManager;
    }

    public MetadataManagementService getMetaDataManagementService() {
        return metaDataManagementService;
    }

    public void setMetaDataManagementService(MetadataManagementService metaDataManagementService) {
        this.metaDataManagementService = metaDataManagementService;
    }

    public RealmService getRealmService() {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
        setTenantManager(realmService != null ?
                realmService.getTenantManager() : null);
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
}
