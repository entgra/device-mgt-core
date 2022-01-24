/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.core.internal;

import io.entgra.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import org.wso2.carbon.device.mgt.common.DeviceStatusTaskPluginConfig;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationProviderService;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationProviderService;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.spi.DeviceTypeGeneratorService;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.config.license.LicenseConfig;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.dto.DeviceTypeServiceIdentifier;
import org.wso2.carbon.device.mgt.core.geo.task.GeoFenceEventOperationManager;
import org.wso2.carbon.device.mgt.core.operation.timeout.task.OperationTimeoutTaskManagerService;
import org.wso2.carbon.device.mgt.core.privacy.PrivacyComplianceProvider;
import org.wso2.carbon.device.mgt.core.push.notification.mgt.PushNotificationProviderRepository;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskManagerService;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManagerService;
import org.wso2.carbon.email.sender.core.service.EmailSenderService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class DeviceManagementDataHolder {

    private static final DeviceManagementDataHolder thisInstance = new DeviceManagementDataHolder();
    private RealmService realmService;
    private TenantManager tenantManager;
    private DeviceManagementProviderService deviceManagerProvider;
    private DeviceInformationManager deviceInformationManager;
    private LicenseManager licenseManager;
    private RegistryService registryService;
    private HeartBeatManagementService heartBeatService;
    private LicenseConfig licenseConfig;
    private ApplicationManager appManager;
    private AppManagementConfig appManagerConfig;
    private OperationManager operationManager;
    private ConfigurationContextService configurationContextService;
    private final HashMap<String,Boolean> requireDeviceAuthorization = new HashMap<>();
    private DeviceAccessAuthorizationService deviceAccessAuthorizationService;
    private GroupManagementProviderService groupManagementProviderService;
    private TaskService taskService;
    private EmailSenderService emailSenderService;
    private PushNotificationProviderRepository pushNotificationProviderRepository;
    private DeviceTaskManagerService deviceTaskManagerService;
    private DeviceStatusTaskManagerService deviceStatusTaskManagerService;
    private DeviceTypeGeneratorService deviceTypeGeneratorService;
    private PrivacyComplianceProvider privacyComplianceProvider;
    private EventConfigurationProviderService eventConfigurationService;
    private GeoLocationProviderService geoLocationProviderService;
    private GeoFenceEventOperationManager geoFenceEventOperationManager;
    private ExecutorService eventConfigExecutors;
    private OperationTimeoutTaskManagerService operationTimeoutTaskManagerService;

    private final Map<DeviceType, DeviceStatusTaskPluginConfig> deviceStatusTaskPluginConfigs = Collections.synchronizedMap(
            new HashMap<>());

    private final Map<String, OperationMonitoringTaskConfig> map = new HashMap<>();

    public Map<String, OperationMonitoringTaskConfig> getMap(){
        return this.map;
    }

    private DeviceManagementDataHolder() {}

    public static DeviceManagementDataHolder getInstance() {
        return thisInstance;
    }

    public RealmService getRealmService() {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
        this.setTenantManager(realmService);
    }

    public TenantManager getTenantManager() {
        return tenantManager;
    }

    private void setTenantManager(RealmService realmService) {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        this.tenantManager = realmService.getTenantManager();
    }

    public DeviceManagementProviderService getDeviceManagementProvider() {
        return deviceManagerProvider;
    }

    public void setDeviceManagementProvider(DeviceManagementProviderService deviceManagerProvider) {
        this.deviceManagerProvider = deviceManagerProvider;
    }

    public GroupManagementProviderService getGroupManagementProviderService() {
        return groupManagementProviderService;
    }

    public void setGroupManagementProviderService(
            GroupManagementProviderService groupManagementProviderService) {
        this.groupManagementProviderService = groupManagementProviderService;
    }

    public RegistryService getRegistryService() {
        if (registryService == null) {
            throw new IllegalStateException("Registry service is not initialized properly");
        }
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public LicenseManager getLicenseManager() {
        return licenseManager;
    }

    public void setLicenseManager(LicenseManager licenseManager) {
        this.licenseManager = licenseManager;
    }

    public LicenseConfig getLicenseConfig() {
        return licenseConfig;
    }

    public void setLicenseConfig(LicenseConfig licenseConfig) {
        this.licenseConfig = licenseConfig;
    }

    public ApplicationManager getAppManager() {
        return appManager;
    }

    public void setAppManager(ApplicationManager appManager) {
        this.appManager = appManager;
    }

    public AppManagementConfig getAppManagerConfig() {
        return appManagerConfig;
    }

    public void setAppManagerConfig(AppManagementConfig appManagerConfig) {
        this.appManagerConfig = appManagerConfig;
    }

    public OperationManager getOperationManager() {
        return operationManager;
    }

    public void setOperationManager(OperationManager operationManager) {
        this.operationManager = operationManager;
    }

    public ConfigurationContextService getConfigurationContextService() {
        if (configurationContextService == null) {
            throw new IllegalStateException("ConfigurationContext service is not initialized properly");
        }
        return configurationContextService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public void setRequireDeviceAuthorization(String pluginType, boolean requireAuthentication) {
        requireDeviceAuthorization.put(pluginType,requireAuthentication);
    }

    public boolean requireDeviceAuthorization(String pluginType) {
        return requireDeviceAuthorization.get(pluginType);
    }

    public DeviceAccessAuthorizationService getDeviceAccessAuthorizationService() {
        return deviceAccessAuthorizationService;
    }

    public void setDeviceAccessAuthorizationService(
            DeviceAccessAuthorizationService deviceAccessAuthorizationService) {
        this.deviceAccessAuthorizationService = deviceAccessAuthorizationService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public EmailSenderService getEmailSenderService() {
        return emailSenderService;
    }

    public void setEmailSenderService(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    public void setPushNotificationProviderRepository(
            PushNotificationProviderRepository pushNotificationProviderRepository) {
        this.pushNotificationProviderRepository = pushNotificationProviderRepository;
    }

    public PushNotificationProviderRepository getPushNotificationProviderRepository() {
        return pushNotificationProviderRepository;
    }

    public DeviceTaskManagerService getDeviceTaskManagerService() {
        return deviceTaskManagerService;
    }

    public void setDeviceTaskManagerService(DeviceTaskManagerService deviceTaskManagerService) {
        this.deviceTaskManagerService = deviceTaskManagerService;
    }

    public DeviceStatusTaskManagerService getDeviceStatusTaskManagerService() {
        return deviceStatusTaskManagerService;
    }

    public void setDeviceStatusTaskManagerService(DeviceStatusTaskManagerService deviceStatusTaskManagerService) {
        this.deviceStatusTaskManagerService = deviceStatusTaskManagerService;
    }

    public void addDeviceStatusTaskPluginConfig(DeviceType deviceType, DeviceStatusTaskPluginConfig deviceStatusTaskPluginConfig) {
        this.deviceStatusTaskPluginConfigs.put(deviceType, deviceStatusTaskPluginConfig);
    }

    public DeviceStatusTaskPluginConfig getDeviceStatusTaskPluginConfig(DeviceTypeServiceIdentifier deviceType) {
        return this.deviceStatusTaskPluginConfigs.get(deviceType);
    }

    public Map<DeviceType, DeviceStatusTaskPluginConfig> getDeviceStatusTaskPluginConfigs() {
        return this.deviceStatusTaskPluginConfigs;
    }

    public void removeDeviceStatusTaskPluginConfig(DeviceType deviceType) {
        this.deviceStatusTaskPluginConfigs.remove(deviceType);
    }

    public DeviceTypeGeneratorService getDeviceTypeGeneratorService() {
        return deviceTypeGeneratorService;
    }

    public void setDeviceTypeGeneratorService(
            DeviceTypeGeneratorService deviceTypeGeneratorService) {
        this.deviceTypeGeneratorService = deviceTypeGeneratorService;
    }

    public PrivacyComplianceProvider getPrivacyComplianceProvider() {
        return privacyComplianceProvider;
    }

    public void setPrivacyComplianceProvider(PrivacyComplianceProvider privacyComplianceProvider) {
        this.privacyComplianceProvider = privacyComplianceProvider;
    }

    public DeviceInformationManager getDeviceInformationManager() {
        return deviceInformationManager;
    }

    public void setDeviceInformationManager(DeviceInformationManager deviceInformationManager) {
        this.deviceInformationManager = deviceInformationManager;
    }

    public HeartBeatManagementService getHeartBeatService() {
        return heartBeatService;
    }

    public void setHeartBeatService(
            HeartBeatManagementService heartBeatService) {
        this.heartBeatService = heartBeatService;
    }

    public void setEventConfigurationProviderService(EventConfigurationProviderService eventConfigurationService) {
        this.eventConfigurationService = eventConfigurationService;
    }

    public EventConfigurationProviderService getEventConfigurationService() {
        return eventConfigurationService;
    }

    public GeoLocationProviderService getGeoLocationProviderService() {
        return geoLocationProviderService;
    }

    public void setGeoLocationProviderService(GeoLocationProviderService geoLocationProviderService) {
        this.geoLocationProviderService = geoLocationProviderService;
    }

    public GeoFenceEventOperationManager getGeoFenceEventOperationManager() {
        return geoFenceEventOperationManager;
    }

    public void setGeoFenceEventOperationManager(GeoFenceEventOperationManager geoFenceEventOperationManager) {
        this.geoFenceEventOperationManager = geoFenceEventOperationManager;
    }

    public ExecutorService getEventConfigExecutors() {
        return eventConfigExecutors;
    }

    public void setEventConfigExecutors(ExecutorService eventConfigExecutors) {
        this.eventConfigExecutors = eventConfigExecutors;
    }

    public OperationTimeoutTaskManagerService getOperationTimeoutTaskManagerService() {
        return operationTimeoutTaskManagerService;
    }

    public void setOperationTimeoutTaskManagerService(
            OperationTimeoutTaskManagerService operationTimeoutTaskManagerService) {
        this.operationTimeoutTaskManagerService = operationTimeoutTaskManagerService;
    }
}
