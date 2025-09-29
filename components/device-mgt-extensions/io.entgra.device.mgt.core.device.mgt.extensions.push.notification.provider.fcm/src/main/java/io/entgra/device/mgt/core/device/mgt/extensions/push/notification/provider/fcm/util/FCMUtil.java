/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.util;

import com.google.auth.oauth2.GoogleCredentials;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.push.notification.ContextMetadata;
import io.entgra.device.mgt.core.device.mgt.core.config.push.notification.PushNotificationConfiguration;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.FCMNotificationStrategy;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.concurrent.TimeUnit;

public class FCMUtil {

    private static final Log log = LogFactory.getLog(FCMUtil.class);
    private static volatile FCMUtil instance;
    private static final Map<Integer, GoogleCredentials> TENANT_CREDENTIALS_MAP = new HashMap<>();
    private static final String FCM_CREDENTIALS_FILE_NAME = "service-account.json";
    private static final String FCM_SERVICE_ACCOUNT_PATH = CarbonUtils.getCarbonHome() + File.separator +
            "repository" + File.separator + "resources";
    private static final String[] FCM_SCOPES = { "https://www.googleapis.com/auth/firebase.messaging" };
    private Properties contextMetadataProperties;
    private static final int SUPER_TENANT_ID = -1234;
    private static ConnectionPool connectionPool;
    private static OkHttpClient client;
    private static final String TENANTS_DIRECTORY = "tenants";

    private FCMUtil() {
        this.initContextConfigs();
        this.initDefaultFCMCredentials();
        this.initTenantedFCMCredentials();
        this.initPooledConnection();
    }

    /**
     * Initialize the context metadata properties from the cdm-config.xml. This file includes the fcm server URL
     * to be invoked when sending the wakeup call to the device.
     */
    private void initContextConfigs() {
        PushNotificationConfiguration pushNotificationConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getPushNotificationConfiguration();
        List<ContextMetadata> contextMetadata = pushNotificationConfiguration.getContextMetadata();
        Properties properties = new Properties();
        if (contextMetadata != null) {
            for (ContextMetadata metadata : contextMetadata) {
                properties.setProperty(metadata.getKey(), metadata.getValue());
            }
        }
        contextMetadataProperties = properties;
    }

    /**
     * Initialize the connection pool for the OkHttpClient instance.
     */
    private void initPooledConnection() {
        connectionPool = new ConnectionPool(25, 1, TimeUnit.MINUTES);
        client = new OkHttpClient.Builder().connectionPool(connectionPool).build();
    }

    /**
     * Get the Pooled OkHttpClient instance
     * @return OkHttpClient instance
     */
    public OkHttpClient getHttpClient() {
        return client;
    }

    /**
     * Get the instance of FCMUtil. FCMUtil is a singleton class which should not be
     * instantiating more than once. Instantiating the class requires to read the service account file from
     * the filesystem and instantiation of the GoogleCredentials object which are costly operations.
     * @return FCMUtil instance
     */
    public static FCMUtil getInstance() {
        if (instance == null) {
            synchronized (FCMUtil.class) {
                if (instance == null) {
                    instance = new FCMUtil();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the tenant specific GoogleCredentials instances for FCM communication. The credentials
     * should be placed in the <CARBON_HOME>/repository/resources/tenants/<TENANT_ID>/service-account.json
     * If no tenanted specific credentials found, the default credentials will be used.
     */
    private void initTenantedFCMCredentials() {
        Path tenantsResourcePath = Paths.get(FCM_SERVICE_ACCOUNT_PATH + File.separator + TENANTS_DIRECTORY);
        if (Files.exists(tenantsResourcePath) && Files.isDirectory(tenantsResourcePath)) {
            try (Stream<Path> tenants = Files.list(tenantsResourcePath)) {
                tenants
                        .filter(Files::isDirectory)
                        .forEach(tenant -> {
                            String tenantId = tenant.getFileName().toString();
                            Path serviceAccountPath = tenant.resolve(FCM_CREDENTIALS_FILE_NAME);
                            if (Files.exists(serviceAccountPath)) {
                                try {
                                    GoogleCredentials defaultApplication = GoogleCredentials.
                                            fromStream(Files.newInputStream(serviceAccountPath)).
                                            createScoped(FCM_SCOPES);
                                    TENANT_CREDENTIALS_MAP.put(Integer.parseInt(tenantId), defaultApplication);
                                } catch (IOException e) {
                                    String msg = "Failed to initialize credentials application for FCM communication";
                                    log.error(msg);
                                    throw new IllegalStateException(msg, e);
                                }
                            } else {
                                log.error("No FCM application credentials found for tenant: " + tenantId);
                            }
                        });
            } catch (IOException e) {
                String msg = "Error while instantiating FCM configuration for tenants";
                log.error(msg);
                throw new IllegalStateException(msg, e);
            }
        } else {
            String msg = "No custom FCM credentials found for any tenant";
            log.info(msg);
        }
    }

    /**
     * Initialize the default GoogleCredentials instance for super tenant and other tenants which are
     * not provided their own FCM credentials.
     */
    private void initDefaultFCMCredentials() {
        try {
            Path serviceAccountPath = Paths.get(FCM_SERVICE_ACCOUNT_PATH + File.separator +
                    FCM_CREDENTIALS_FILE_NAME);
            GoogleCredentials defaultApplication = GoogleCredentials.
                    fromStream(Files.newInputStream(serviceAccountPath)).
                    createScoped(FCM_SCOPES);
            TENANT_CREDENTIALS_MAP.put(SUPER_TENANT_ID, defaultApplication);
        } catch (IOException e) {
            String msg = "Failed to initialize credentials application for FCM communication";
            log.error(msg);
            throw new IllegalStateException(msg, e);
        }
    }

    /**
     * Get the tenant specific GoogleCredentials instance for FCM API invocation.
     * If no tenanted specific credentials found, the default credentials will be returned.
     * @param tenantId Tenant ID of the tenant
     * @return GoogleCredentials instance for the tenant
     */
    public GoogleCredentials getTenantedApplication(int tenantId) {
        if (TENANT_CREDENTIALS_MAP.containsKey(tenantId)) {
            return TENANT_CREDENTIALS_MAP.get(tenantId);
        }
        return TENANT_CREDENTIALS_MAP.get(SUPER_TENANT_ID);
    }

    public Properties getContextMetadataProperties() {
        return contextMetadataProperties;
    }
}
