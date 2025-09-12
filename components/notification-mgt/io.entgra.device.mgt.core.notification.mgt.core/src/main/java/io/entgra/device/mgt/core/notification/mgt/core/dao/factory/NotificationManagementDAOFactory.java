/*
 *  Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.notification.mgt.core.dao.factory;

import io.entgra.device.mgt.core.notification.mgt.common.util.NotificationManagementConstants;
import io.entgra.device.mgt.core.notification.mgt.common.exception.IllegalTransactionStateException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.TransactionManagementException;
import io.entgra.device.mgt.core.notification.mgt.common.exception.UnsupportedDatabaseEngineException;
import io.entgra.device.mgt.core.notification.mgt.core.config.datasource.JNDILookupDefinition;
import io.entgra.device.mgt.core.notification.mgt.core.config.datasource.NotificationDatasourceConfiguration;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationManagementDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.impl.GenericNotificationManagementDAOImpl;
import io.entgra.device.mgt.core.notification.mgt.core.dao.impl.H2NotificationManagementDAOImpl;
import io.entgra.device.mgt.core.notification.mgt.core.dao.impl.OracleNotificationManagementDAOImpl;
import io.entgra.device.mgt.core.notification.mgt.core.dao.impl.PostgreNotificationManagementDAOImpl;
import io.entgra.device.mgt.core.notification.mgt.core.dao.impl.SQLServerNotificationManagementDAOImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

public class NotificationManagementDAOFactory {
    private static final Log log = LogFactory.getLog(NotificationManagementDAOFactory.class);
    private static final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private static DataSource dataSource;
    private static String productName;

    public static void init(NotificationDatasourceConfiguration notificationDatasourceConfiguration) {
        dataSource = resolveDatasource(notificationDatasourceConfiguration);
        if (dataSource == null) {
            throw new IllegalStateException("Datasource is not initialized properly");
        }
        try {
            productName = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while initializing database product name", e);
        }
    }

    private static DataSource resolveDatasource(NotificationDatasourceConfiguration notificationDatasourceConfiguration) {
        if (notificationDatasourceConfiguration == null) {
            throw new IllegalArgumentException("Null is retrieved for Datasource configuration");
        }
        JNDILookupDefinition jndiLookupDefinition = notificationDatasourceConfiguration.getJndiLookupDefinition();
        if (jndiLookupDefinition == null) {
            throw new IllegalArgumentException("Null is retrieved for JNDI lookup definition");
        }
        String datasourceName = jndiLookupDefinition.getJndiName();
        List<JNDILookupDefinition.JNDIProperty> jndiProperties = jndiLookupDefinition.getJndiProperties();

        if (jndiProperties == null || jndiProperties.isEmpty()) {
            return lookupDatasource(datasourceName);
        }
        Hashtable<Object, Object> jndiPropertiesTable = new Hashtable<>();
        for (JNDILookupDefinition.JNDIProperty property : jndiProperties) {
            jndiPropertiesTable.put(property.getName(), property.getValue());
        }
        return lookupDatasource(datasourceName, jndiPropertiesTable);
    }

    private static DataSource lookupDatasource(String datasourceName) {
        try {
            return InitialContext.doLookup(datasourceName);
        } catch (NamingException e) {
            String msg = "Error occurred while JNDI lookup for the datasource";
            throw new IllegalStateException(msg, e);
        }
    }

    private static DataSource lookupDatasource(String datasourceName, Hashtable<Object, Object> jndiProperties) {
        try {
            InitialContext initialContext = new InitialContext(jndiProperties);
            return (DataSource) initialContext.lookup(datasourceName);
        } catch (NamingException e) {
            String msg = "Error occurred while JNDI lookup for the datasource";
            throw new IllegalStateException(msg, e);
        }
    }

    public static NotificationManagementDAO getNotificationManagementDAO() {
        if (productName == null) {
            throw new IllegalStateException("Database is not initialized properly.");
        }
        switch (productName) {
            case NotificationManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                return new OracleNotificationManagementDAOImpl();
            case NotificationManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                return new SQLServerNotificationManagementDAOImpl();
            case NotificationManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                return new PostgreNotificationManagementDAOImpl();
            case NotificationManagementConstants.DataBaseTypes.DB_TYPE_H2:
                return new H2NotificationManagementDAOImpl();
            case NotificationManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                return new GenericNotificationManagementDAOImpl();
            default:
                throw new UnsupportedDatabaseEngineException("Unsupported database product: " + productName);
        }
    }

    public static void beginTransaction() throws TransactionManagementException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("A transaction is already active within the context of " +
                    "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling");
        }
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new TransactionManagementException("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static void openConnection() throws SQLException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("A transaction is already active within the context of " +
                    "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling");
        }
        conn = dataSource.getConnection();
        currentConnection.set(conn);
    }

    public static Connection getConnection() {
        Connection connection = currentConnection.get();
        if (connection == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction.");
        }
        return connection;
    }

    public static void closeConnection() {
        Connection connection = currentConnection.get();
        if (connection == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction.");
        }
        try {
            connection.close();
        } catch (SQLException e) {
            log.warn("Error encountered while closing the connection", e);
        }
        currentConnection.remove();
    }

    public static void rollbackTransaction() {
        Connection connection = currentConnection.get();
        if (connection == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction.");
        }
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error("Error encountered while performing rollback operation on transaction", e);
        }
    }

    public static void commitTransaction() {
        Connection connection = currentConnection.get();
        if (connection == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction.");
        }
        try {
            connection.commit();
        } catch (SQLException e) {
            log.error("Error encountered while committing the transaction", e);
        }
    }
}

