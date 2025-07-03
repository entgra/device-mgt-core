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

package io.entgra.device.mgt.core.notification.mgt.core.common;

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.core.config.NotificationConfigurationManager;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementServiceComponent;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationManagerUtil;
import io.entgra.device.mgt.core.notification.mgt.core.util.TestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeSuite;
import org.w3c.dom.Document;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.service.RealmService;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

/**
 * Base test class for notification management tests
 */
public abstract class BaseNotificationManagementTest {
    private static final Log log = LogFactory.getLog(BaseNotificationManagementTest.class);

    private DataSource dataSource;
    protected DeviceManagementProviderService deviceMgtService;
    protected MetadataManagementService metaService;
    protected RealmService realmService;

    protected static final String ADMIN_USER = "admin";

    @BeforeSuite
    public void setupDataSource() throws Exception {
        this.initDatSource();
        this.initSQLScript();
        this.initiatePrivilegedCarbonContext();
        NotificationConfigurationManager.getInstance().initConfig();
    }

    protected void initializeServices() throws Exception {
        initDatSource();
        initSQLScript();
        NotificationConfigurationManager.getInstance().initConfig();
        NotificationConfigurationManager.getInstance().initConfig();
        DeviceManagementServiceComponent.notifyStartupListeners();
        NotificationManagementDataHolder.getInstance().setDeviceManagementProviderService(deviceMgtService);
        NotificationManagementDataHolder.getInstance().setRealmService(realmService);
        NotificationManagementDataHolder.getInstance().setMetaDataManagementService(metaService);
        NotificationManagementDataHolder.getInstance().setTaskService(null);
    }

    public void initDatSource() throws Exception {
        DataSourceConfig config = this.readDataSourceConfig();
        String uniqueUrl = "jdbc:h2:mem:cdm-test-db-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;MODE=MYSQL";
        config.setUrl(uniqueUrl);
        this.dataSource = this.getDataSource(config);
        NotificationManagementDAOFactory.init(dataSource);
        NotificationArchivalDestDAOFactory.init(dataSource);
        NotificationArchivalSourceDAOFactory.init(dataSource);
    }

    public void initiatePrivilegedCarbonContext() throws Exception {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
    }

    private DataSource getDataSource(DataSourceConfig config) {
        PoolProperties properties = new PoolProperties();
        properties.setUrl(config.getUrl());
        properties.setDriverClassName(config.getDriverClassName());
        properties.setUsername(config.getUser());
        properties.setPassword(config.getPassword());
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }

    private DataSourceConfig readDataSourceConfig() throws NotificationManagementException {
        try {
            File file = new File("src/test/resources/config/datasource/data-source-config.xml");
            Document doc = NotificationManagerUtil.convertToDocument(file);
            JAXBContext testDBContext = JAXBContext.newInstance(DataSourceConfig.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            return (DataSourceConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new NotificationManagementException("Error occurred while reading data source config", e);
        }
    }

    protected void initSQLScript() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getDataSource().getConnection();
            stmt = conn.createStatement();
            stmt.execute("DROP ALL OBJECTS");
            stmt.execute("RUNSCRIPT FROM './src/test/resources/sql/h2.sql'");
        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
