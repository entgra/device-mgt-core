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

package io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory;

import io.entgra.device.mgt.core.realtime.notification.mgt.common.util.RealtimeNotificationManagementConstants;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeIllegalTransactionStateException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeTransactionManagementException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeUnsupportedDatabaseEngineException;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.config.datasource.RealtimeJNDILookupDefinition;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.config.datasource.RealtimeNotificationDatasourceConfiguration;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.RealtimeNotificationManagementDAO;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.RealtimeAbstractNotificationManagementDAOImpl;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.impl.RealtimeOracleNotificationManagementDAOImpl;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.impl.RealtimePostgreNotificationManagementDAOImpl;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.impl.RealtimeSQLServerNotificationManagementDAOImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

public class RealtimeNotificationManagementDAOFactory {
    private static final Log log = LogFactory.getLog(RealtimeNotificationManagementDAOFactory.class);
    private static final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private static DataSource dataSource;
    private static String productName;

    public static void init(RealtimeNotificationDatasourceConfiguration notificationDatasourceConfiguration) {
        dataSource = resolveDatasource(notificationDatasourceConfiguration);
        if (dataSource == null) {
            throw new IllegalStateException("Datasource is not initialized properly");
        }
        try (Connection connection = dataSource.getConnection()) {
            productName = connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while initializing database product name", e);
        }
    }

    private static DataSource resolveDatasource(RealtimeNotificationDatasourceConfiguration notificationDatasourceConfiguration) {
        if (notificationDatasourceConfiguration == null) {
            throw new IllegalArgumentException("Null is retrieved for Datasource configuration");
        }
        RealtimeJNDILookupDefinition jndiLookupDefinition = notificationDatasourceConfiguration.getJndiLookupDefinition();
        if (jndiLookupDefinition == null) {
            throw new IllegalArgumentException("Null is retrieved for JNDI lookup definition");
        }
        String datasourceName = jndiLookupDefinition.getJndiName();
        List<RealtimeJNDILookupDefinition.JNDIProperty> jndiProperties = jndiLookupDefinition.getJndiProperties();

        if (jndiProperties == null || jndiProperties.isEmpty()) {
            return lookupDatasource(datasourceName);
        }
        Hashtable<Object, Object> jndiPropertiesTable = new Hashtable<>();
        for (RealtimeJNDILookupDefinition.JNDIProperty property : jndiProperties) {
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

    public static RealtimeNotificationManagementDAO getNotificationManagementDAO() {
        if (productName == null) {
            throw new IllegalStateException("Database is not initialized properly.");
        }
        switch (productName) {
            case RealtimeNotificationManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                return new RealtimeOracleNotificationManagementDAOImpl();
            case RealtimeNotificationManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                return new RealtimeSQLServerNotificationManagementDAOImpl();
            case RealtimeNotificationManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                return new RealtimePostgreNotificationManagementDAOImpl();
            case RealtimeNotificationManagementConstants.DataBaseTypes.DB_TYPE_H2:
            case RealtimeNotificationManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                return new RealtimeAbstractNotificationManagementDAOImpl();
            default:
                throw new RealtimeUnsupportedDatabaseEngineException("Unsupported database product: " + productName);
        }
    }

    public static void beginTransaction() throws RealtimeTransactionManagementException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new RealtimeIllegalTransactionStateException("A transaction is already active within the context of " +
                    "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling");
        }
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeError) {
                    e.addSuppressed(closeError);
                }
            }
            throw new RealtimeTransactionManagementException("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static void openConnection() throws SQLException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new RealtimeIllegalTransactionStateException("A transaction is already active within the context of " +
                    "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling");
        }
        conn = dataSource.getConnection();
        currentConnection.set(conn);
    }

    public static Connection getConnection() {
        Connection connection = currentConnection.get();
        if (connection == null) {
            throw new RealtimeIllegalTransactionStateException("No connection is associated with the current transaction.");
        }
        return connection;
    }

    public static void closeConnection() {
        Connection connection = currentConnection.get();
        if (connection == null) {
            throw new RealtimeIllegalTransactionStateException("No connection is associated with the current transaction.");
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
            throw new RealtimeIllegalTransactionStateException("No connection is associated with the current transaction.");
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
            throw new RealtimeIllegalTransactionStateException("No connection is associated with the current transaction.");
        }
        try {
            connection.commit();
        } catch (SQLException e) {
            log.error("Error encountered while committing the transaction", e);
        }
    }
}

