/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.caching.impl.CacheImpl;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.common.AppRegistrationCredentials;
import org.wso2.carbon.device.mgt.common.ApplicationRegistration;
import org.wso2.carbon.device.mgt.common.ApplicationRegistrationException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.EnrollmentConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationProviderService;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.cache.DeviceCacheKey;
import org.wso2.carbon.device.mgt.core.cache.GeoCacheKey;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.core.config.tenant.PlatformConfigurationManagementServiceImpl;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.util.DeviceIDHolder;
import org.wso2.carbon.device.mgt.core.report.mgt.Constants;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class DeviceManagerUtil {

    private static final Log log = LogFactory.getLog(DeviceManagerUtil.class);
    public static final String GENERAL_CONFIG_RESOURCE_PATH = "general";

    private  static boolean isDeviceCacheInitialized = false;
    private static boolean isGeoFenceCacheInitialized = false;

    public static Document convertToDocument(File file) throws DeviceManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new DeviceManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

    /**
     * Resolve data source from the data source definition.
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    public static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException("Device Management Repository data source configuration is null and thus, " +
                    "is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

    /**
     * Adds a new device type to the database if it does not exists.
     *
     * @param typeName device type
     * @param tenantId provider tenant Id
     * @param isSharedWithAllTenants is this device type shared with all tenants.
     * @return status of the operation
     */
    public static boolean registerDeviceType(String typeName, int tenantId, boolean isSharedWithAllTenants,
                                             DeviceTypeMetaDefinition deviceTypeDefinition)
            throws DeviceManagementException {
        boolean status;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            DeviceType deviceType = deviceTypeDAO.getDeviceType(typeName, tenantId);
            if (deviceType == null) {
                deviceType = new DeviceType();
                deviceType.setName(typeName);
                deviceType.setDeviceTypeMetaDefinition(deviceTypeDefinition);
                deviceTypeDAO.addDeviceType(deviceType, tenantId, isSharedWithAllTenants);
            } else {
                if (deviceTypeDefinition != null) {
                    deviceType.setDeviceTypeMetaDefinition(deviceTypeDefinition);
                    deviceTypeDAO.updateDeviceType(deviceType, tenantId);
                }
            }
            DeviceManagementDAOFactory.commitTransaction();
            status = true;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while registering the device type '"
                    + typeName + "'", e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("SQL occurred while registering the device type '"
                    + typeName + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return status;
    }

    /**
     * Get the DeviceType information from Database.
     *
     * @param typeName device type
     * @param tenantId provider tenant Id
     * @return DeviceType which contains info about the device-type.
     */
    public static DeviceType getDeviceType(String typeName, int tenantId) throws DeviceManagementException {
        DeviceType deviceType;
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            deviceType = deviceTypeDAO.getDeviceType(typeName, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while fetching the device type '"
                    + typeName + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("SQL Error occurred while fetching the device type '"
                    + typeName + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceType;
    }

    /**
     * Un-registers an existing device type from the device management metadata repository.
     *
     * @param typeName device type
     * @return status of the operation
     */
    public static boolean unregisterDeviceType(String typeName, int tenantId) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            DeviceType deviceType = deviceTypeDAO.getDeviceType(typeName, tenantId);
            if (deviceType != null) {
                deviceTypeDAO.removeDeviceType(typeName, tenantId);
            }
            DeviceManagementDAOFactory.commitTransaction();
            return true;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while registering the device type '" +
                                                        typeName + "'", e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("SQL occurred while registering the device type '" +
                                                        typeName + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    public static Map<String, String> convertDevicePropertiesToMap(List<Device.Property> properties) {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        for (Device.Property prop : properties) {
            propertiesMap.put(prop.getName(), prop.getValue());
        }
        return propertiesMap;
    }

    public static List<DeviceIdentifier> convertDevices(List<Device> devices) {

        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(device.getDeviceIdentifier());
            identifier.setType(device.getType());
            deviceIdentifiers.add(identifier);
        }
        return deviceIdentifiers;
    }

    public static List<DeviceIdentifier> getValidDeviceIdentifiers(List<Device> devices) {
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            if (device.getEnrolmentInfo() != null) {
                switch (device.getEnrolmentInfo().getStatus()) {
                    case BLOCKED:
                    case REMOVED:
                        break;
                    case SUSPENDED:
                        break;
                    default:
                        DeviceIdentifier identifier = new DeviceIdentifier();
                        identifier.setId(device.getDeviceIdentifier());
                        identifier.setType(device.getType());
                        deviceIdentifiers.add(identifier);
                }
            }
        }
        return deviceIdentifiers;
    }

    public static String getServerBaseHttpsUrl() {
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        String mgtConsoleTransport = CarbonUtils.getManagementTransport();
        ConfigurationContextService configContextService =
                DeviceManagementDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
        int httpsProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                        mgtConsoleTransport);
        if (httpsProxyPort > 0) {
            port = httpsProxyPort;
        }
        return "https://" + hostName + ":" + port;
    }

    public static String getServerBaseHttpUrl() {
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        ConfigurationContextService configContextService =
                DeviceManagementDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, "http");
        int httpProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                        "http");
        if (httpProxyPort > 0) {
            port = httpProxyPort;
        }
        return "http://" + hostName + ":" + port;
    }

    /**
     * returns the tenant Id of the specific tenant Domain
     */
    public static int getTenantId(String tenantDomain) throws DeviceManagementException {
        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return MultitenantConstants.SUPER_TENANT_ID;
            }
            TenantManager tenantManager = DeviceManagementDataHolder.getInstance().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            if (tenantId == -1) {
                throw new DeviceManagementException("invalid tenant Domain :" + tenantDomain);
            }
            return tenantId;
        } catch (UserStoreException e) {
            throw new DeviceManagementException("invalid tenant Domain :" + tenantDomain);
        }
    }

    public static int getTenantId() {
        return PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getTenantId();
    }

    public static int validateActivityListPageSize(int limit) throws OperationManagementException {
        if (limit == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                return deviceManagementConfig.getPaginationConfiguration().getActivityListPageSize();
            } else {
                throw new OperationManagementException("Device-Mgt configuration has not initialized. Please check the " +
                        "cdm-config.xml file.");
            }
        }
        return limit;
    }

    public static PaginationRequest validateOperationListPageSize(PaginationRequest paginationRequest)
            throws OperationManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getOperationListPageSize());
            } else {
                throw new OperationManagementException("Device-Mgt configuration has not initialized. Please check the " +
                        "cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static PaginationRequest validateNotificationListPageSize(PaginationRequest paginationRequest)
            throws NotificationManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getNotificationListPageSize());
            } else {
                throw new NotificationManagementException("Device-Mgt configuration has not initialized. " +
                        "Please check the cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static PaginationRequest validateDeviceListPageSize(PaginationRequest paginationRequest)
            throws DeviceManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getDeviceListPageSize());
            } else {
                throw new DeviceManagementException("Device-Mgt configuration has not initialized. " +
                        "Please check the cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static GroupPaginationRequest validateGroupListPageSize(GroupPaginationRequest paginationRequest)
            throws GroupManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance()
                    .getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration()
                        .getDeviceListPageSize());
            } else {
                throw new GroupManagementException("Device-Mgt configuration has not initialized. " +
                        "Please check the cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static int validateDeviceListPageSize(int limit) throws DeviceManagementException {
        if (limit == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                return deviceManagementConfig.getPaginationConfiguration().getDeviceListPageSize();
            } else {
                throw new DeviceManagementException("Device-Mgt configuration has not initialized. " +
                        "Please check the cdm-config.xml file.");
            }
        }
        return limit;
    }

    /**
     * Validate Metadata pagination request.
     *
     * @param paginationRequest {@link PaginationRequest} obtained from the user
     * @return                  {@link PaginationRequest} object validated/enriched
     * @throws MetadataManagementException if device management configuration has not initialized
     */
    public static PaginationRequest validateMetadataListPageSize(PaginationRequest paginationRequest) throws
            MetadataManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getMetadataListPageSize());
            } else {
                String msg = "Device-Mgt configuration has not initialized. Please check the cdm-config.xml file.";
                log.error(msg);
                throw new MetadataManagementException(msg);
            }
        }
        return paginationRequest;
    }

    public static boolean isPublishLocationResponseEnabled() throws DeviceManagementException {
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            return deviceManagementConfig.getOperationAnalyticsConfiguration().isPublishLocationResponseEnabled();
        } else {
            throw new DeviceManagementException("Device-Mgt configuration has not initialized. " +
                    "Please check the cdm-config.xml file.");
        }
    }

    public static boolean isPublishDeviceInfoResponseEnabled() throws DeviceManagementException {
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            return deviceManagementConfig.getOperationAnalyticsConfiguration().isPublishDeviceInfoResponseEnabled();
        } else {
            throw new DeviceManagementException("Device-Mgt configuration has not initialized. " +
                    "Please check the cdm-config.xml file.");
        }
    }

    public static boolean isPublishOperationResponseEnabled() throws DeviceManagementException {
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            return deviceManagementConfig.getOperationAnalyticsConfiguration()
                    .getOperationResponseConfigurations().isEnabled();
        } else {
            throw new DeviceManagementException("Device-Mgt configuration has not initialized. " +
                    "Please check the cdm-config.xml file.");
        }
    }

    public static List<String> getEnabledOperationsForResponsePublish() throws DeviceManagementException {
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            return deviceManagementConfig.getOperationAnalyticsConfiguration()
                    .getOperationResponseConfigurations().getOperations();
        } else {
            throw new DeviceManagementException("Device-Mgt configuration has not initialized. " +
                    "Please check the cdm-config.xml file.");
        }
    }

    public static DeviceIDHolder validateDeviceIdentifiers(List<DeviceIdentifier> deviceIDs) {
        List<DeviceIdentifier> errorDeviceIdList = new ArrayList<>();
        List<DeviceIdentifier> validDeviceIDList = new ArrayList<>();
        for (DeviceIdentifier deviceIdentifier : deviceIDs) {
            String deviceID = deviceIdentifier.getId();
            if (deviceID == null || deviceID.isEmpty()) {
                log.warn("When adding operation for devices, found a device identifiers which doesn't have defined "
                        + "the identity of the device, with the request. Hence ignoring the device identifier.");
                continue;
            }
            try {
                if (isValidDeviceIdentifier(deviceIdentifier)) {
                    validDeviceIDList.add(deviceIdentifier);
                } else {
                    errorDeviceIdList.add(deviceIdentifier);
                }
            } catch (DeviceManagementException e) {
                errorDeviceIdList.add(deviceIdentifier);
            }
        }

        DeviceIDHolder deviceIDHolder = new DeviceIDHolder();
        deviceIDHolder.setValidDeviceIDList(validDeviceIDList);
        deviceIDHolder.setErrorDeviceIdList(errorDeviceIdList);

        return deviceIDHolder;
    }

    public static boolean isValidDeviceIdentifier(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier,
                false);
        if (device == null || device.getDeviceIdentifier() == null ||
                device.getDeviceIdentifier().isEmpty() || device.getEnrolmentInfo() == null) {
            return false;
        } else if (EnrolmentInfo.Status.REMOVED.equals(device.getEnrolmentInfo().getStatus())) {
            return false;
        }
        return true;
    }

    public static boolean isDeviceExists(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier,
                false);
        return !(device == null || device.getDeviceIdentifier() == null ||
                device.getDeviceIdentifier().isEmpty() || device.getEnrolmentInfo() == null);
    }

    private static CacheManager getCacheManager() {
        return Caching.getCacheManagerFactory().getCacheManager(DeviceManagementConstants.DM_CACHE_MANAGER);
    }

    public static EventsPublisherService getEventPublisherService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        EventsPublisherService eventsPublisherService =
                (EventsPublisherService) ctx.getOSGiService(EventsPublisherService.class, null);
        if (eventsPublisherService == null) {
            String msg = "Event Publisher service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return eventsPublisherService;
    }

    /**
     * Retrieve EventConfigurationProviderService osgi service component
     * @return {@link EventConfigurationProviderService} service component
     */
    public static EventConfigurationProviderService getEventConfigService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        EventConfigurationProviderService eventConfigService =
                (EventConfigurationProviderService) ctx.getOSGiService(EventConfigurationProviderService.class, null);
        if (eventConfigService == null) {
            String msg = "Event configuration service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return eventConfigService;
    }

    public static void initializeDeviceCache() {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        int deviceCacheExpiry = config.getDeviceCacheConfiguration().getExpiryTime();
        long deviceCacheCapacity = config.getDeviceCacheConfiguration().getCapacity();
        CacheManager manager = getCacheManager();
        if (config.getDeviceCacheConfiguration().isEnabled()) {
            if(!isDeviceCacheInitialized) {
                isDeviceCacheInitialized = true;
                if (manager != null) {
                    if (deviceCacheExpiry > 0) {
                        manager.<DeviceCacheKey, Device>createCacheBuilder(DeviceManagementConstants.DEVICE_CACHE).
                                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                                        deviceCacheExpiry)).setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.
                                Duration(TimeUnit.SECONDS, deviceCacheExpiry)).setStoreByValue(true).build();
                        if(deviceCacheCapacity > 0 ) {
                            ((CacheImpl) manager.<DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE)).
                                    setCapacity(deviceCacheCapacity);
                        }
                    } else {
                        manager.<DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE);
                    }
                } else {
                    if (deviceCacheExpiry > 0) {
                        Caching.getCacheManager().
                                <DeviceCacheKey, Device>createCacheBuilder(DeviceManagementConstants.DEVICE_CACHE).
                                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                                        deviceCacheExpiry)).setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.
                                Duration(TimeUnit.SECONDS, deviceCacheExpiry)).setStoreByValue(true).build();
                        ((CacheImpl)(manager.<DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE))).
                                setCapacity(deviceCacheCapacity);
                    } else {
                        Caching.getCacheManager().<DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE);
                    }
                }
            }
        }
    }

    /**
     * Enable Geofence caching according to the configurations proviced by cdm-config.xml
     */
    public static void initializeGeofenceCache() {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        int geoCacheExpiry = config.getGeoFenceCacheConfiguration().getExpiryTime();
        long geoCacheCapacity = config.getGeoFenceCacheConfiguration().getCapacity();
        CacheManager manager = getCacheManager();
        if (config.getGeoFenceCacheConfiguration().isEnabled()) {
            if(!isGeoFenceCacheInitialized) {
                isGeoFenceCacheInitialized = true;
                if (manager != null) {
                    if (geoCacheExpiry > 0) {
                        manager.<GeoCacheKey, GeofenceData>createCacheBuilder(DeviceManagementConstants.GEOFENCE_CACHE).
                                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                                        geoCacheExpiry)).setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.
                                Duration(TimeUnit.SECONDS, geoCacheExpiry)).setStoreByValue(true).build();
                        if(geoCacheCapacity > 0 ) {
                            ((CacheImpl) manager.<GeoCacheKey, GeofenceData>getCache(DeviceManagementConstants.GEOFENCE_CACHE)).
                                    setCapacity(geoCacheCapacity);
                        }
                    } else {
                        manager.<GeoCacheKey, GeofenceData>getCache(DeviceManagementConstants.GEOFENCE_CACHE);
                    }
                } else {
                    if (geoCacheExpiry > 0) {
                        Caching.getCacheManager().
                                <GeoCacheKey, GeofenceData>createCacheBuilder(DeviceManagementConstants.GEOFENCE_CACHE).
                                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                                        geoCacheExpiry)).setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.
                                Duration(TimeUnit.SECONDS, geoCacheExpiry)).setStoreByValue(true).build();
                        ((CacheImpl)(manager.<GeoCacheKey, GeofenceData>getCache(DeviceManagementConstants.GEOFENCE_CACHE))).
                                setCapacity(geoCacheCapacity);
                    } else {
                        Caching.getCacheManager().<GeoCacheKey, GeofenceData>getCache(DeviceManagementConstants.GEOFENCE_CACHE);
                    }
                }
            }
        }
    }

    public static Cache<DeviceCacheKey, Device> getDeviceCache() {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        CacheManager manager = getCacheManager();
        Cache<DeviceCacheKey, Device> deviceCache = null;
        if (config.getDeviceCacheConfiguration().isEnabled()) {
            if(!isDeviceCacheInitialized) {
                initializeDeviceCache();
            }
            if (manager != null) {
                deviceCache = manager.getCache(DeviceManagementConstants.DEVICE_CACHE);
            } else {
                deviceCache =  Caching.getCacheManager(DeviceManagementConstants.DM_CACHE_MANAGER)
                        .getCache(DeviceManagementConstants.DEVICE_CACHE);
            }
        }
        return deviceCache;
    }

    /**
     * Get geofence cache object
     * @return {@link Cache<GeoCacheKey, GeofenceData>}
     */
    public static Cache<GeoCacheKey, GeofenceData> getGeoCache() {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        CacheManager manager = getCacheManager();
        Cache<GeoCacheKey, GeofenceData> geoCache = null;
        if (config.getGeoFenceCacheConfiguration().isEnabled()) {
            if(!isGeoFenceCacheInitialized) {
                initializeGeofenceCache();
            }
            if (manager != null) {
                geoCache = manager.getCache(DeviceManagementConstants.GEOFENCE_CACHE);
            } else {
                geoCache =  Caching.getCacheManager(DeviceManagementConstants.GEOFENCE_CACHE)
                        .getCache(DeviceManagementConstants.GEOFENCE_CACHE);
            }
        }
        return geoCache;
    }

    /**
     * Create an app and get app registration token from the application registration endpoint
     *
     * @return AppRegistrationToken object which contains access and refresh tokens
     * @throws ApplicationRegistrationException when application fails to connect with the app registration
     *                                          endpoint
     */
    @SuppressWarnings("PackageAccessibility")
    public static AppRegistrationCredentials getApplicationRegistrationCredentials(String host, String port,
                                                                                   String username, String password)
            throws ApplicationRegistrationException {
        if (host == null || port == null) {
            String msg = "Required gatewayHost or gatewayPort system property is null";
            log.error(msg);
            throw new ApplicationRegistrationException(msg);
        }
        String internalServerAddr = "https://".concat(host).concat(":").concat(port);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost apiEndpoint = new HttpPost(
                    internalServerAddr + DeviceManagementConstants.ConfigurationManagement
                            .APPLICATION_REGISTRATION_API_ENDPOINT);

            apiEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            apiEndpoint.setHeader(DeviceManagementConstants.ConfigurationManagement.AUTHORIZATION_HEADER,
                    DeviceManagementConstants.ConfigurationManagement.BASIC_AUTH.concat(" ")
                            .concat(getBase64EncodedCredentials(username + ":" + password)));
            apiEndpoint.setEntity(constructApplicationRegistrationPayload());
            HttpResponse response = client.execute(apiEndpoint);
            if (response != null) {
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                return new ObjectMapper().readValue(result.toString(), AppRegistrationCredentials.class);
            } else {
                String msg = "Response is 'NUll' for the Application Registration API call.";
                log.error(msg);
                throw new ApplicationRegistrationException(msg);
            }
        } catch (IOException e) {
            throw new ApplicationRegistrationException(
                    "Error occurred when invoking API. API endpoint: "
                    + internalServerAddr + DeviceManagementConstants.ConfigurationManagement
                            .APPLICATION_REGISTRATION_API_ENDPOINT, e);
        }
    }

    /**
     * Use default admin credentials and encode them in Base64
     *
     * @return Base64 encoded client credentials
     */
    private static String getBase64EncodedCredentials(String credentials) {
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    /**
     * Create a JSON payload for application registration
     *
     * @return Generated JSON payload
     */
    @SuppressWarnings("PackageAccessibility")
    private static StringEntity constructApplicationRegistrationPayload() {
        ApplicationRegistration applicationRegistration = new ApplicationRegistration();
        applicationRegistration.setApplicationName("MyApp");
        applicationRegistration.setAllowedToAllDomains(false);
        List<String> tags = new ArrayList<>();
        tags.add("windows");
        tags.add("device_management");
        applicationRegistration.setTags(tags);
        applicationRegistration.setValidityPeriod(3600);
        Gson gson = new Gson();
        String payload = gson.toJson(applicationRegistration);
        return new StringEntity(payload, ContentType.APPLICATION_JSON);
    }

    /**
     * Retrieves access token for a given device
     * @param scopes scopes for token
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @param deviceOwner owner of the device that is going to generate token
     * @return @{@link AccessTokenInfo} wrapped object of retrieved access token and refresh token
     * @throws JWTClientException if an error occurs when the jwt client creation or token retrieval
     */
    public static AccessTokenInfo getAccessTokenForDeviceOwner(String scopes, String clientId,
                                                               String clientSecret, String deviceOwner)
            throws JWTClientException {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        JWTClientManagerService jwtClientManagerService = (JWTClientManagerService) ctx
                .getOSGiService(JWTClientManagerService.class, null);
        JWTClient jwtClient = jwtClientManagerService.getJWTClient();
        return jwtClient.getAccessToken(clientId, clientSecret, deviceOwner, scopes);
    }

    /**
     * <h1>Generate a value for the passed os version</h1>
     *
     * <p>Value is generated by isolating each position of OS version then adding Zeros up until
     * Minor version and Revision have a constant number of digits.
     * <i>Eg: 5.1.1 will be 50000100001, 9 will be 90000000000</i>
     * </p>
     *
     * <p>Above conversion is done in order to fail proof in situations where 6.0.0 can be
     * smaller than 5.12.1.
     * <i>Eg: 5.12.1 will be 5121 and 6.0.0 will be 600 and 5121 > 600(this statement is incorrect)</i>
     * </p>
     *
     * @param osVersion os version(eg: 5.1.1)
     * @return {@link Long} generated value
     */
    public static Long generateOSVersionValue(String osVersion) {
        Matcher osMatcher = Pattern.compile("[0-9]+([.][0-9]+)*").matcher(osVersion);
        if (!osMatcher.find()) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to read OS version. OS version: " + osVersion + "is invalid. " +
                          "Please follow the following convention [0-9]+([.][0-9]+)*");
            }
            return null;
        }
        osVersion = osMatcher.group();

        String[] osVersions = osVersion.split("[.]");
        int osVersionsLength = osVersions.length;

        /*
         * <h1>Equation explanation</h1>
         *
         * <p>
          Eg: {@code osVersion == "5.1.1"} will generate an array of {@code ["5","1","1"]}
              Following loop for the above result can be broken down as below
              * Iteration 1 : {@code Math.pow} result = 5 00000 00000, {@code sum} = 5 00000 00000
              * Iteration 2 : {@code Math.pow} result = 1 00000, {@code sum} = 5 00001 00000
              * Iteration 3 : {@code Math.pow} result = 1, {@code sum} = 5 00001 00001
          To generate the above results I have multiplied the array values with powers of 10.
          The constraints used to generate the power of 10 is explained below,
         * </p>
         *
         * <p>
         {@code Constants.NUM_OF_OS_VERSION_POSITIONS - (i + 1)} was done in-order to identify
         which position of the OS version is been used for generation process, so correct number
         of Zeros can be generated.
         * </p>
         *
         * <p>
         {@code Constants.NUM_OF_OS_VERSION_DIGITS} this multiplication will make sure that the
         values generated will reduce in following order main OS version, minor OS version, Revision.
         * </p>
        */
        return IntStream
                .range(0, osVersionsLength)
                .mapToLong(i -> (long) (Math.pow(10, (Constants.NUM_OF_OS_VERSION_POSITIONS - (i + 1))
                                                     * Constants.NUM_OF_OS_VERSION_DIGITS)
                                        * Integer.parseInt(osVersions[i]))).sum();
    }

    /**
     * Revert a generated value back to a OS version
     *
     * @param osVersionValue value that should be reverted
     * @return {@link String} OS version
     */
    /* Following method is unused but was still included in case a requirement occurs to revert the
     * generated values in DM_DEVICE_INFO back to a OS versions */
    public static String reverseOSVersionValue(Long osVersionValue) {
        StringJoiner joiner = new StringJoiner(".");

        /*
        * <h1>Equation explanation</h1>
        *
        * <p>
        Eg: {@code osVersionValue == "5 00001 00001"}
              Following loop will divide to break down the above number to regenerate the os version
              * Iteration 1 : {@code osVersion} = 5 , {@code osVersionValue} = 00001 00001
              * Iteration 2 : {@code osVersion} = 1 , {@code osVersionValue} = 00001
              * Iteration 3 : {@code osVersion} = 1 , {@code osVersionValue} = 0
          Final array = {@code ["5","1","1"]}
          To generate the above results I have divided the generated value with powers of 10.
          The constraints used to generate the power of 10 is explained below,
        * </p>
        *
        * <p>
        {@code 10, (i - 1) * Constants.NUM_OF_OS_VERSION_DIGITS} this will break the generated value
        creating each OS version position in following order main OS version, minor OS version,
        Revision.
        * </p>
        *
        */
        for (int i = Constants.NUM_OF_OS_VERSION_POSITIONS; i > 0; i--) {
            long osVersion = Double.valueOf(
                    osVersionValue / Math.pow(10, (i - 1) * Constants.NUM_OF_OS_VERSION_DIGITS))
                    .longValue();
            osVersionValue = Double.valueOf(
                    osVersionValue % Math.pow(10, (i - 1) * Constants.NUM_OF_OS_VERSION_DIGITS))
                    .longValue();
            joiner.add(String.valueOf(osVersion));
        }
        return joiner.toString();
    }

    public static Object getConfiguration(String key) {

        PlatformConfigurationManagementService configMgtService =
                new PlatformConfigurationManagementServiceImpl();

        try {
            PlatformConfiguration tenantConfiguration = configMgtService.getConfiguration
                    (GENERAL_CONFIG_RESOURCE_PATH);
            List<ConfigurationEntry> configuration = tenantConfiguration.getConfiguration();

            if (configuration != null && !configuration.isEmpty()) {
                for (ConfigurationEntry cEntry : configuration) {
                    if (key.equalsIgnoreCase(cEntry.getName())) {
                        return cEntry.getValue();
                    }
                }
            }
        } catch (ConfigurationManagementException e) {
            log.error("Error while getting the configurations from registry.", e);
            return null;
        }
        return null;
    }

    /**
     * Retrieve the value stored in a property list by passing the Key
     * @param properties list of property
     * @param propertyName key of the property to be retrieved
     * @return value of the retrieved property
     */
    public static String getPropertyString(List<Device.Property> properties, String propertyName) {
        if (properties != null) {
            for (Device.Property property : properties) {
                if (property.getName() != null && property.getName().equals(propertyName)) {
                    return property.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Retrieve the Enrollment Configuration entry added to the Platform Configuration
     * @return enrollment configuration
     */
    public static EnrollmentConfiguration getEnrollmentConfigurationEntry() {
            Object enrollmentConfigEntry = DeviceManagerUtil.getConfiguration(
                    DeviceManagementConstants.Common.ENROLLMENT_CONFIGURATION);
            if (enrollmentConfigEntry != null) {
                Gson gson = new Gson();
                return gson.fromJson(enrollmentConfigEntry.toString(), EnrollmentConfiguration.class);
            }
        return null;
    }

    /**
     * Validates if the device is allowed to be enrolled based on the device serial number.
     * Enrollment Configuration which has been added in Platform Configuration has the validation factors.
     * Validation happens in two ways,
     * 1. List of Serial Numbers - If this is available checks if the device to be enrolled serial number is
     * in the given list of serial numbers
     * 2. List of Serial Numbers against a User - If [1] is not configured or the device serial number is not
     * in the [1] then this checks if the device to be enrolled serial number is in the list of serial numbers
     * which are against a User
     * @param enrollmentConfiguration which has the enrollment configurations of a tenant
     * @param deviceSerialNumber device serial number to be validated
     * @return a boolean value if the device can be enrolled
     */
    public static boolean isDeviceEnrollable(EnrollmentConfiguration enrollmentConfiguration,
                                             String deviceSerialNumber) {
        // enrollment configuration has not been set
        if (enrollmentConfiguration == null) {
            if (log.isDebugEnabled()) {
                log.debug("Enrollment configuration in platform configuration has not been set "
                          + "hence device can be enrolled.");
            }
            return true;
        } else {
            List<String> enrollmentConfigSerialNumbers = enrollmentConfiguration.getSerialNumbers();
            boolean isDeviceEnrollable = false;
            if (enrollmentConfigSerialNumbers != null && !enrollmentConfigSerialNumbers.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("List of serial numbers '" + enrollmentConfigSerialNumbers.toString() + "' has been"
                              + " added for enrollment configuration under platform configuration to validate "
                              + "the serial number '" + deviceSerialNumber + "'.");
                }
                isDeviceEnrollable = enrollmentConfigSerialNumbers.stream().anyMatch(deviceSerialNumber::equals);
            }
            if (isDeviceEnrollable) {
                return true;
            }
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            List<EnrollmentConfiguration.UserConfiguration> userConfigurations = enrollmentConfiguration
                    .getUserConfigurations();
            if (userConfigurations != null && !userConfigurations.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("List of serial numbers against users has been added for enrollment "
                              + "configuration under platform configuration.");
                }
                return userConfigurations.stream()
                        .filter(userConfiguration -> username.equals(userConfiguration.getUsername())).findFirst()
                        .filter(userConfiguration -> userConfiguration.getSerialNumbers().stream()
                                .anyMatch(deviceSerialNumber::equals)).isPresent();
            } else {
                if (enrollmentConfigSerialNumbers != null && !enrollmentConfigSerialNumbers.isEmpty()) {
                    // serial number of the device is not in the serial number based configuration nor in the serial
                    // numbers against user based configuration
                    return false;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Enrollment configuration has been but configuration does not contain any "
                              + "serial number based validation. It may be having the configuration to push "
                              + "devices to a specific group after a successful enrollment.");
                }
                // enrollment configuration has been set only to add device to a specific group and not to
                // validate device against serial number
                return true;
            }
        }
    }


    /**
     * Add devices to the configured groups.
     * Enrollment Configuration which has been added in Platform Configuration has this configuration.
     * @param enrollmentConfiguration which has the group configurations of a tenant
     * @param deviceSerialNumber serial number of the device
     * @param deviceIdentifier of the device
     * @throws DeviceManagementException when there is an error trying to add the device to the specified group
     */
    public static void addDeviceToConfiguredGroup(EnrollmentConfiguration enrollmentConfiguration,
                                                  String deviceSerialNumber,
                                                  DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        // enrollment configuration has not been set
        if (enrollmentConfiguration == null) {
            if (log.isDebugEnabled()) {
                log.debug("Enrollment configuration in platform configuration has not been set "
                          + "hence not adding to any specific group.");
            }
        } else {
            List<EnrollmentConfiguration.GroupConfiguration> groupConfigurations = enrollmentConfiguration
                    .getGroupConfigurations();
            if (groupConfigurations != null && !groupConfigurations.isEmpty()) {
                String groupName = groupConfigurations.stream()
                        .filter(groupConfiguration -> groupConfiguration.getSerialNumbers()
                                .stream().anyMatch(deviceSerialNumber::equals))
                        .findFirst().map(EnrollmentConfiguration.GroupConfiguration::getGroupName).orElse(null);
                if (log.isDebugEnabled()) {
                    log.debug("Enrollment configuration having group configuration has been configured under "
                              + "platform configuration");
                }
                if (groupName != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Device identifier '" + deviceIdentifier.getId() + "' of device type '"
                                  + deviceIdentifier.getType() + "' which has the serial number '" + deviceSerialNumber
                                  + "' has been configured to be added to the group '" + groupName + "'.");
                    }
                    GroupManagementProviderService groupManagementProviderService = DeviceManagementDataHolder
                            .getInstance().getGroupManagementProviderService();
                    try {
                        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(groupName, false);
                        if (deviceGroup != null) {
                            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
                            deviceIdentifiers.add(deviceIdentifier);
                            groupManagementProviderService.addDevices(deviceGroup.getGroupId(), deviceIdentifiers);
                        } else {
                            log.warn("Group name: " + groupName + " configured for device "
                                     + deviceIdentifier.toString() + " is invalid because the group does not exist.");
                        }
                    } catch (GroupManagementException e) {
                        String msg = "Error occurred while trying to add the device '" + deviceIdentifier.getId() +
                                     "' of device type '" + deviceIdentifier.getType() + "' to the group '"
                                     + groupName + "' which was configured via enrollment configuration of platform "
                                     + "configuration.";
                        log.error(msg);
                        throw new DeviceManagementException(msg, e);
                    } catch (DeviceNotFoundException e) {
                        String msg = "Device having device identifier '" + deviceIdentifier.getId()
                                     + "' of device type '" + deviceIdentifier.getType() + "' could not be found to "
                                     + "add the device to the group '" + groupName + "' which was configured via "
                                     + "enrollment configuration of platform " + "configuration.";
                        log.error(msg);
                        throw new DeviceManagementException(msg, e);
                    }
                }

            }
        }

    }

    public static String[] getRolesOfUser(String userName) throws UserStoreException {
        UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        String[] roleList;
        if (userRealm != null) {
            userRealm.getUserStoreManager().getRoleNames();
            roleList = userRealm.getUserStoreManager().getRoleListOfUser(userName);
        } else {
            String msg = "User realm is not initiated. Logged in user: " + userName;
            log.error(msg);
            throw new UserStoreException(msg);
        }
        return roleList;
    }

    /**
     * Retrieve the value of the user property from the user profile
     * @param username of the user
     * @param claimUri name of the claim
     * @return value for the claim uri of user
     * @throws UserStoreException when there is error in retrieving the user store manager
     */
    public static String getUserClaimValue(String username, String claimUri) throws UserStoreException {
        UserStoreManager userStoreManager = CarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getUserStoreManager();
        return userStoreManager.getUserClaimValue(username, claimUri, null);
    }

    /**
     * Create the parent path that the children groups can have
     * @param deviceGroup parent group
     * @return created parent path
     */
    public static String createParentPath(DeviceGroup deviceGroup) {
        if (DeviceGroupConstants.HierarchicalGroup.SEPERATOR.equals(deviceGroup.getParentPath())) {
            return deviceGroup.getParentPath() + deviceGroup.getGroupId();
        } else {
            return deviceGroup.getParentPath() + DeviceGroupConstants.HierarchicalGroup.SEPERATOR
                    + deviceGroup.getGroupId();
        }
    }
}
